package org.myfx.controls.aione.UserService.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.UserService.common.SubscriptionStatusEnum;
import org.myfx.controls.aione.UserService.common.SubscriptionTypeEnum;
import org.myfx.controls.aione.UserService.model.entity.UserSubscription;

import java.util.List;

/**
 * 用户订阅 Mapper 接口
 */
@Mapper
public interface UserSubscriptionMapper {

    /**
     * 新增订阅记录
     *
     * @param userSubscription 订阅实体
     * @return 影响行数
     */
    int insert(UserSubscription userSubscription);

    /**
     * 根据ID删除订阅记录
     *
     * @param id 订阅记录ID（雪花ID）
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据ID更新订阅记录（全字段更新）
     *
     * @param userSubscription 订阅实体
     * @return 影响行数
     */
    int updateById(UserSubscription userSubscription);

    /**
     * 根据ID查询订阅记录
     *
     * @param id 订阅记录ID（雪花ID）
     * @return 订阅实体
     */
    UserSubscription selectById(@Param("id") Long id);

    /**
     * 根据用户ID查询订阅记录列表
     *
     * @param userId 用户ID
     * @return 订阅记录列表
     */
    List<UserSubscription> selectByUserId(@Param("userId") Integer userId);

    /**
     * 查询用户指定服务的有效订阅（生效中）
     *
     * @param userId      用户ID
     * @param serviceType 服务类型（枚举code）
     * @return 有效订阅记录
     */
    UserSubscription selectEffectiveByUserIdAndServiceType(
            @Param("userId") Integer userId,
            @Param("serviceType") SubscriptionTypeEnum serviceType);

    /**
     * 批量更新订阅状态（如批量过期）
     *
     * @param ids     订阅ID列表
     * @param status  目标状态
     * @return 影响行数
     */
    int batchUpdateStatus(
            @Param("ids") List<Long> ids,
            @Param("status") SubscriptionStatusEnum status);
}