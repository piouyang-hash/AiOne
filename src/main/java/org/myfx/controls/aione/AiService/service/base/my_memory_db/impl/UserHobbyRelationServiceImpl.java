package org.myfx.controls.aione.AiService.service.base.my_memory_db.impl;

import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.AiService.entity.my_memory_db.UserHobbyRelation;
import org.myfx.controls.aione.AiService.mapper.my_memory_db.UserHobbyRelationMapper;
import org.myfx.controls.aione.AiService.service.base.my_memory_db.UserHobbyRelationService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户-爱好关联关系业务实现类
 * 仅做基础参数校验 + Mapper调用，无复杂业务逻辑
 */
@Service
@RequiredArgsConstructor
public class UserHobbyRelationServiceImpl implements UserHobbyRelationService {

    private final UserHobbyRelationMapper userHobbyRelationMapper;

    /**
     * 新增单个关联关系：校验实体+核心字段合法性
     */
    @Override
    public boolean addUserHobbyRelation(UserHobbyRelation relation) {
        // 参数校验：实体非空 + 核心字段合法
        if (relation == null) {
            throw new IllegalArgumentException("参数不合法：关联关系实体不能为空");
        }
        if (relation.getUserInfoId() == null || relation.getUserInfoId() <= 0) {
            throw new IllegalArgumentException("参数不合法：userInfoId必须为正整数");
        }
        if (relation.getUserId() == null || relation.getUserId() <= 0) {
            throw new IllegalArgumentException("参数不合法：userId必须为正整数");
        }
        if (relation.getHobbyId() == null || relation.getHobbyId() <= 0) {
            throw new IllegalArgumentException("参数不合法：hobbyId必须为正整数");
        }
        // 调用Mapper新增
        int affectedRows = userHobbyRelationMapper.insert(relation);
        return affectedRows > 0;
    }

    /**
     * 批量新增关联关系：校验列表+每个实体的核心字段
     */
    @Override
    public boolean batchAddUserHobbyRelations(List<UserHobbyRelation> relations) {
        // 参数校验：列表非空 + 非空列表
        if (relations == null || relations.isEmpty()) {
            throw new IllegalArgumentException("参数不合法：关联关系列表不能为空");
        }
        // 校验列表中每个实体的核心字段
        for (UserHobbyRelation relation : relations) {
            if (relation == null || relation.getUserInfoId() == null || relation.getUserInfoId() <= 0
                    || relation.getUserId() == null || relation.getUserId() <= 0
                    || relation.getHobbyId() == null || relation.getHobbyId() <= 0) {
                throw new IllegalArgumentException("参数不合法：列表中存在无效的关联关系实体");
            }
        }
        // 调用Mapper批量新增
        int affectedRows = userHobbyRelationMapper.batchInsert(relations);
        return affectedRows == relations.size(); // 批量新增成功=受影响行数=列表大小
    }

    /**
     * 解绑用户某个爱好：校验三个核心参数合法性
     */
    @Override
    public boolean removeUserHobby(Long userInfoId, Integer hobbyId, Integer userId) {
        // 参数校验：所有参数为正整数
        if (userInfoId == null || userInfoId <= 0) {
            throw new IllegalArgumentException("参数不合法：userInfoId必须为正整数");
        }
        if (hobbyId == null || hobbyId <= 0) {
            throw new IllegalArgumentException("参数不合法：hobbyId必须为正整数");
        }
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("参数不合法：userId必须为正整数");
        }
        // 调用Mapper删除
        int affectedRows = userHobbyRelationMapper.deleteByUserInfoIdAndHobbyIdAndUserId(userInfoId, hobbyId, userId);
        return affectedRows > 0;
    }

    /**
     * 解绑用户所有爱好：校验两个核心参数合法性
     */
    @Override
    public boolean removeAllUserHobbies(Long userInfoId, Integer userId) {
        // 参数校验：所有参数为正整数
        if (userInfoId == null || userInfoId <= 0) {
            throw new IllegalArgumentException("参数不合法：userInfoId必须为正整数");
        }
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("参数不合法：userId必须为正整数");
        }
        // 调用Mapper删除
        int affectedRows = userHobbyRelationMapper.deleteByUserInfoIdAndUserId(userInfoId, userId);
        return affectedRows > 0;
    }

    /**
     * 查询用户所有爱好ID：校验参数 + 保证返回非空列表
     */
    @Override
    public List<Integer> listUserHobbyIds(Long userInfoId, Integer userId) {
        // 参数校验：所有参数为正整数
        if (userInfoId == null || userInfoId <= 0) {
            throw new IllegalArgumentException("参数不合法：userInfoId必须为正整数");
        }
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("参数不合法：userId必须为正整数");
        }
        // 调用Mapper查询，保证返回非空列表（避免空指针）
        List<Integer> hobbyIds = userHobbyRelationMapper.selectHobbyIdsByUserInfoIdAndUserId(userInfoId, userId);
        return hobbyIds == null ? List.of() : hobbyIds;
    }

    /**
     * 查询爱好对应的所有用户InfoId：校验参数 + 保证返回非空列表
     */
    @Override
    public List<Long> listUserInfoIdsByHobbyId(Integer hobbyId) {
        // 参数校验：hobbyId为正整数
        if (hobbyId == null || hobbyId <= 0) {
            throw new IllegalArgumentException("参数不合法：hobbyId必须为正整数");
        }
        // 调用Mapper查询，保证返回非空列表（避免空指针）
        List<Long> userInfoIds = userHobbyRelationMapper.selectUserInfoIdsByHobbyId(hobbyId);
        return userInfoIds == null ? List.of() : userInfoIds;
    }
}