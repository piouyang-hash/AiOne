package org.myfx.controls.aione.AiService.service.base.ai_chat_db.order;

import org.myfx.controls.aione.AiService.common.ai_chat_db.order.PayStatusEnum;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.order.AiRechargeOrder;

import java.math.BigDecimal;
import java.util.List;

/**
 * AI充值订单 Service接口
 * 仅包含：创建订单、更新订单状态 两个核心方法
 */
public interface AiRechargeOrderService {

    /**
     * 创建AI充值订单
     * @param goodsId 充值商品ID（雪花ID）
     * @return 订单ID（Long类型）
     */
    Long createRechargeOrder(Long goodsId);

    /**
     * 根据订单ID查询充值订单详情
     * @param orderId 订单主键ID
     * @return 订单实体信息
     */
    AiRechargeOrder getRechargeOrderById(Long orderId);

    // --------------- 新增：查询当前用户所有充值订单 ---------------
    /**
     * 查询当前登录用户的所有AI积分充值订单
     * @return 充值订单列表（无订单时返回空列表，不会返回null）
     */
    List<AiRechargeOrder> getRechargeOrdersByCurrentUser();

    /**
     * 获取当前用户的待支付订单（仅允许存在1条，多条则抛出异常）
     * @return 待支付订单，无则返回null
     */
    AiRechargeOrder getCurrentUserUnpaidOrder();


    /**
     * 【主方法】更新充值订单状态
     * 仅允许操作【待支付(WAIT_PAY)】状态的订单
     * @param id 订单ID
     * @param targetStatus 目标状态
     * @param userId 用户ID
     */
    void updateRechargeOrderStatus(Long id, PayStatusEnum targetStatus, Integer userId);

    /**
     * 【简化方法】关闭订单（状态固定为：已取消）
     * @param id 订单ID
     */
    void closeOrder(Long id);

}