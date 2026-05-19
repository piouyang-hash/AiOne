package org.myfx.controls.aione.AiService.service.base.my_memory_db;

import org.myfx.controls.aione.AiService.entity.my_memory_db.UserHobbyRelation;

import java.util.List;

/**
 * 用户-爱好关联关系业务接口
 * 封装用户与爱好的关联/解绑/查询核心逻辑，保证参数合法性+语义化调用
 */
public interface UserHobbyRelationService {

    /**
     * 新增单个用户-爱好关联关系
     * @param relation 用户-爱好关联关系实体（需包含userInfoId、userId、hobbyId核心字段）
     * @return 是否新增成功（true=成功，false=失败）
     */
    boolean addUserHobbyRelation(UserHobbyRelation relation);

    /**
     * 批量新增用户-爱好关联关系（推荐：一次给用户绑定多个爱好时使用）
     * @param relations 用户-爱好关联关系列表（列表非空，且每个实体需包含核心字段）
     * @return 是否批量新增成功（true=全部成功，false=至少一条失败）
     */
    boolean batchAddUserHobbyRelations(List<UserHobbyRelation> relations);

    /**
     * 解绑用户的某个爱好（双重校验：避免误操作其他用户数据）
     * @param userInfoId 关联base_user_info的主键ID（雪花ID）
     * @param hobbyId 爱好ID（base_hobby的主键）
     * @param userId 用户ID（业务维度，base_user_info的user_id）
     * @return 是否解绑成功（true=成功，false=无匹配数据）
     */
    boolean removeUserHobby(Long userInfoId, Integer hobbyId, Integer userId);

    /**
     * 解绑用户的所有爱好（比如用户注销时清空爱好关联）
     * @param userInfoId 关联base_user_info的主键ID（雪花ID）
     * @param userId 用户ID（业务维度，base_user_info的user_id）
     * @return 是否解绑成功（true=成功，false=无匹配数据）
     */
    boolean removeAllUserHobbies(Long userInfoId, Integer userId);

    /**
     * 查询用户的所有爱好ID（用于展示用户已绑定的爱好）
     * @param userInfoId 关联base_user_info的主键ID（雪花ID）
     * @param userId 用户ID（业务维度，base_user_info的user_id）
     * @return 爱好ID列表（无数据返回空列表，避免空指针）
     */
    List<Integer> listUserHobbyIds(Long userInfoId, Integer userId);

    /**
     * 查询某个爱好对应的所有用户InfoId（用于统计爱好的用户数）
     * @param hobbyId 爱好ID（base_hobby的主键）
     * @return 用户InfoId列表（无数据返回空列表，避免空指针）
     */
    List<Long> listUserInfoIdsByHobbyId(Integer hobbyId);
}