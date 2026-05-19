package org.myfx.controls.aione.AiService.service.base.ai_chat_db.order;

import org.myfx.controls.aione.AiService.entity.ai_chat_db.order.AiRechargeOrder;

import java.math.BigDecimal;

/**
 * AI充值订单 Service接口
 * 仅包含：创建订单、更新订单状态 两个核心方法
 */
public interface AiRechargeOrderService {

    /**
     * 创建AI充值订单
     * @param amount 充值金额
     * @return 订单ID（Long类型）
     */
    Long createRechargeOrder(BigDecimal amount);

    /**
     * 根据订单ID查询充值订单详情
     * @param orderId 订单主键ID
     * @return 订单实体信息
     */
    AiRechargeOrder getRechargeOrderById(Long orderId);

    /**
     * 更新AI充值订单状态
     * 【参数你后续自行补充】
     */
    void updateRechargeOrderStatus();
}