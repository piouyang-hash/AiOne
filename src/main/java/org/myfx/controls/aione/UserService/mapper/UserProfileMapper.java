package org.myfx.controls.aione.UserService.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.AppTypeEnum;
import org.myfx.controls.aione.UserService.model.entity.UserProfile;

@Mapper
public interface UserProfileMapper {

    /**
     * 新增用户资料
     * （注：appType已包含在UserProfile实体中，无需单独传参）
     *
     * @param userProfile 用户资料实体（包含userId、appType、nickname等字段）
     */
    void insert(UserProfile userProfile);

    /**
     * 根据用户ID删除关联的所有用户资料
     * 无应用隔离：删除指定用户在所有应用下的全部资料
     *
     * @param userId 用户ID（关联user表的主键）
     * @return 受影响的行数（0=未删除，>=1=删除成功，行数为该用户下的应用资料数量）
     */
    int deleteByUserId(@Param("userId") Integer userId);

    /**
     * 更新用户资料（支持动态更新，空值字段不更新）
     * 多应用隔离：可更新指定用户在指定应用下的资料
     *
     * @param userProfile 用户资料实体（至少包含userId，可选包含nickname/bio/avatarUrl/appType等）
     */
    void update(UserProfile userProfile);

    /**
     * 根据用户ID+应用标识查询用户资料
     * 多应用隔离：确保只查询指定用户在指定应用下的唯一资料
     *
     * @param userId 用户ID（关联user表的主键）
     * @param appType  应用标识枚举（1=阅读器，2=AI聊天，3=拓展位）
     * @return 用户资料实体（无匹配数据返回null）
     */
    UserProfile findByUserIdAndAppType(@Param("userId") Integer userId, @Param("appType") AppTypeEnum appType);
}