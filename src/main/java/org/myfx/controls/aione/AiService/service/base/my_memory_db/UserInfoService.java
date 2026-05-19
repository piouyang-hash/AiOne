package org.myfx.controls.aione.AiService.service.base.my_memory_db;

import org.myfx.controls.aione.AiService.entity.my_memory_db.BaseUserInfo;

/**
 * 用户信息业务接口
 */
public interface UserInfoService {

    /**
     * 初始化用户基本信息
     * @param userId 用户ID（唯一）
     * @throws RuntimeException 用户已存在时抛出异常
     */
    void initUserInfo(Integer userId);

    /**
     * 完善用户基本信息（动态更新非空字段）
     * @param baseUserInfo 用户基本信息实体（至少包含userId，非空字段会被更新）
     * @return 影响行数
     */
    int completeUserBaseInfo(BaseUserInfo baseUserInfo);

    /**
     * 根据用户ID（Integer）查询用户基本信息
     * @param userId 用户ID（Integer，非空）
     * @return 用户基本信息（无匹配则返回null）
     */
    BaseUserInfo getUserBaseInfoByUserId(Integer userId);

    /**
     * SAGA补偿：注册失败-物理删除用户基本信息
     * @param userId 用户ID
     * @return true=补偿成功，false=补偿失败
     */
    boolean compensatePhysicalDeleteRegisterFail(Integer userId);

    /**
     * SAGA补偿：注销失败-逻辑复原用户基本信息
     * @param userId 用户ID
     * @return true=补偿成功，false=补偿失败
     */
    boolean compensateRecoverCancelFail(Integer userId);

    /**
     * SAGA正向操作：注销用户-逻辑删除用户基本信息（兜底，便于SAGA统一调用）
     * @param userId 用户ID
     * @return true=操作成功，false=操作失败
     */
    boolean logicalDeleteForCancel(Integer userId);
}