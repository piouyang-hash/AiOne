package org.myfx.controls.aione.AiService.mapper.my_memory_db;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.AiService.entity.my_memory_db.UserHobbyRelation;

import java.util.List;

/**
 * 用户爱好关联表Mapper接口
 */
@Mapper
public interface UserHobbyRelationMapper {

    /**
     * 新增用户-爱好关联关系（单个）
     */
    int insert(UserHobbyRelation relation);

    /**
     * 批量新增用户-爱好关联关系（常用，比如一次给用户加多个爱好）
     */
    int batchInsert(@Param("relations") List<UserHobbyRelation> relations);

    /**
     * 删除用户的某个爱好（双重校验：userInfoId+hobbyId+userId）
     * @param userInfoId 关联base_user_info的主键ID
     * @param hobbyId 爱好ID
     * @param userId 用户ID（业务维度）
     * @return 受影响行数
     */
    int deleteByUserInfoIdAndHobbyIdAndUserId(
            @Param("userInfoId") Long userInfoId,
            @Param("hobbyId") Integer hobbyId,
            @Param("userId") Integer userId // 新增：userId参数
    );

    /**
     * 删除用户的所有爱好（双重校验：userInfoId+userId）
     * @param userInfoId 关联base_user_info的主键ID
     * @param userId 用户ID（业务维度）
     * @return 受影响行数
     */
    int deleteByUserInfoIdAndUserId(
            @Param("userInfoId") Long userInfoId,
            @Param("userId") Integer userId // 新增：userId参数
    );

    /**
     * 根据userInfoId+userId查询该用户的所有爱好ID
     * @param userInfoId 关联base_user_info的主键ID
     * @param userId 用户ID（业务维度）
     * @return 爱好ID列表
     */
    List<Integer> selectHobbyIdsByUserInfoIdAndUserId(
            @Param("userInfoId") Long userInfoId,
            @Param("userId") Integer userId // 新增：userId参数
    );

    /**
     * 根据爱好ID查询所有用户ID（base_user_info的id）
     */
    List<Long> selectUserInfoIdsByHobbyId(@Param("hobbyId") Integer hobbyId);

}