package org.myfx.controls.aione.AiService.service.base.my_memory_db.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.entity.my_memory_db.BaseUserInfo;
import org.myfx.controls.aione.AiService.mapper.my_memory_db.BaseUserInfoMapper;
import org.myfx.controls.aione.AiService.service.base.my_memory_db.UserInfoService;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.LogicalDeleteEnum;
import org.myfx.controls.aione.ServiceCommon.utils.SnowflakeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户信息业务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserInfoServiceImpl implements UserInfoService {

    private final BaseUserInfoMapper baseUserInfoMapper;

    /**
     * 初始化用户信息：仅传userId，其他字段默认（雪花ID自动生成，性别/年龄/身份为null）
     */
    @Override
    public void initUserInfo(Integer userId) {
        // 1. 校验用户是否已存在（避免重复初始化）,包括已经删除的
        BaseUserInfo existUser = baseUserInfoMapper.selectByUserIdWithDeleted(userId);
        if (existUser != null) {
            throw new RuntimeException("用户ID[" + userId + "]已存在，无需重复初始化");
        }

        // 2. 构建用户信息对象（仅填充必传字段）
        BaseUserInfo userInfo = new BaseUserInfo();
        userInfo.setId(SnowflakeGenerator.generateId()); // 业务层生成雪花ID
        userInfo.setUserId(userId); // 仅接收的入参
        // gender/age/identity 初始为null（符合表结构设计）
        // create_time/update_time 由数据库默认值填充，无需手动设置

        // 3. 调用Mapper插入
        int insertCount = baseUserInfoMapper.insert(userInfo);
        if (insertCount != 1) {
            throw new RuntimeException("用户ID[" + userId + "]初始化失败");
        }
    }

    @Override
    public int completeUserBaseInfo(BaseUserInfo baseUserInfo) {
        // 基础参数校验：userId是更新条件，不能为空（避免更新全表风险）
        if (baseUserInfo == null || baseUserInfo.getUserId() == null) {
            throw new IllegalArgumentException("参数不合法：用户ID（userId）不能为空");
        }
        // 极简实现：直接调用Mapper接口，无额外业务逻辑
        return baseUserInfoMapper.updateByUserId(baseUserInfo);
    }

    @Override
    public BaseUserInfo getUserBaseInfoByUserId(Integer userId) {
        // 基础参数校验：userId不能为空
        if (userId == null) {
            throw new IllegalArgumentException("参数不合法：用户ID（userId）不能为空");
        }
        // 极简实现：直接调用Mapper接口
        return baseUserInfoMapper.selectNotDeletedByUserId(userId);
    }

    /**
     * SAGA补偿：注册失败-物理删除临时用户信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean compensatePhysicalDeleteRegisterFail(Integer userId) {
        // 1. 基础校验
        if (userId == null || userId <= 0) {
            log.error("[SAGA补偿-物理删除用户信息] userId非法：{}", userId);
            return false;
        }

        // 2. 幂等校验：查询用户信息是否存在
        BaseUserInfo userInfo = baseUserInfoMapper.selectByUserIdWithDeleted(userId);
        if (userInfo == null) {
            log.info("[SAGA补偿-物理删除用户信息] 用户[{}]信息不存在（已删除/未创建），补偿成功（幂等）", userId);
            return true;
        }

        // 3. 执行物理删除
        int affectedRows = baseUserInfoMapper.physicalDeleteByUserId(userId);
        if (affectedRows > 0) {
            log.info("[SAGA补偿-物理删除用户信息] 用户[{}]注册失败，临时信息已物理删除（补偿成功）", userId);
            return true;
        } else {
            log.warn("[SAGA补偿-物理删除用户信息] 用户[{}]未删除到记录（创建超5分钟/已并发删除），补偿完成", userId);
            return true;
        }
    }

    /**
     * SAGA补偿：注销失败-逻辑复原用户信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean compensateRecoverCancelFail(Integer userId) {
        // 1. 基础校验
        if (userId == null || userId <= 0) {
            log.error("[SAGA补偿-逻辑复原用户信息] userId非法：{}", userId);
            return false;
        }

        // 2. 校验：用户信息必须存在
        BaseUserInfo userInfo = baseUserInfoMapper.selectByUserIdWithDeleted(userId);
        if (userInfo == null) {
            log.error("[SAGA补偿-逻辑复原用户信息] 用户[{}]信息不存在，无法复原", userId);
            return false;
        }

        // 3. 幂等校验：仅复原已逻辑删除的记录
        if (userInfo.getIsDeleted() != LogicalDeleteEnum.DELETED) {
            log.info("[SAGA补偿-逻辑复原用户信息] 用户[{}]信息未逻辑删除，补偿成功（幂等）", userId);
            return true;
        }

        // 4. 执行逻辑复原
        int affectedRows = baseUserInfoMapper.updateLogicalRecoverByUserId(userId);
        if (affectedRows > 0) {
            log.info("[SAGA补偿-逻辑复原用户信息] 用户[{}]注销失败，信息已复原为未删除状态（补偿成功）", userId);
            return true;
        } else {
            log.warn("[SAGA补偿-逻辑复原用户信息] 用户[{}]未复原到记录（已并发复原/状态变更），补偿完成", userId);
            return true;
        }
    }

    /**
     * SAGA正向操作：注销用户-逻辑删除用户信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean logicalDeleteForCancel(Integer userId) {
        if (userId == null || userId <= 0) {
            log.error("[注销-逻辑删除用户信息] userId非法：{}", userId);
            return false;
        }

        int affectedRows = baseUserInfoMapper.updateLogicalDeleteByUserId(userId);
        if (affectedRows > 0) {
            log.info("[注销-逻辑删除用户信息] 用户[{}]信息已逻辑删除", userId);
            return true;
        } else {
            log.warn("[注销-逻辑删除用户信息] 用户[{}]信息未删除（已删除/不存在）", userId);
            return false;
        }
    }
}