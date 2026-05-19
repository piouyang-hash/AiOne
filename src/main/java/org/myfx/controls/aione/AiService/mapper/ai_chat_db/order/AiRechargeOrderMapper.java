package org.myfx.controls.aione.AiService.mapper.ai_chat_db.order;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.AiService.common.ai_chat_db.order.PayStatusEnum;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.order.AiRechargeOrder;

import java.util.List;

/**
 * AI积分充值订单Mapper（AI聊天微服务内置订单，无需独立微服务）
 */
@Mapper
public interface AiRechargeOrderMapper {

    /**
     * 插入AI积分充值订单
     * @param aiRechargeOrder 充值订单实体
     * @return 影响行数
     */
    int insert(AiRechargeOrder aiRechargeOrder);

    /**
     * 根据订单ID（表主键id）查询AI积分充值订单
     * @param id 订单ID（充值记录表的主键id）
     * @return 充值订单
     */
    AiRechargeOrder selectByOrderId(Long id);

    /**
     * 根据用户ID查询该用户的所有AI积分充值订单
     * @param userId 用户ID
     * @return 充值订单列表
     */
    List<AiRechargeOrder> selectByUserId(@Param("userId") Integer userId);

    /**
     * 根据订单ID+用户ID查询订单（精准匹配，防止串查他人订单）
     * @param orderId 订单ID（对应表的id字段）
     * @param userId 用户ID（对应表的user_id字段）
     * @return 充值订单
     */
    AiRechargeOrder getByIdAndUserId(
            @Param("orderId") Long orderId,
            @Param("userId") Integer userId
    );

    /**
     * 根据ID更新AI积分充值订单状态（核心更新方法）
     * @param aiRechargeOrder 充值订单实体（至少包含id、status）
     * @return 影响行数
     */
    int updateById(AiRechargeOrder aiRechargeOrder);

    /**
     * 动态删除：删除指定ID+用户ID且状态匹配的AI积分充值订单
     * @param id 订单ID
     * @param userId 用户ID
     * @param status 要删除的订单状态
     * @return 实际删除的行数
     */
    int deleteByIdAndUserIdAndStatus(
            @Param("id") Long id,
            @Param("userId") Integer userId,
            @Param("status") PayStatusEnum status
    );
}