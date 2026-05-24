package org.myfx.controls.aione.AiService.service.base.ai_chat_db.order;


import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.AiService.common.ai_chat_db.order.PayStatusEnum;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.order.AiRechargeGoods;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.order.AiRechargeOrder;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.token.AiUserPointBalanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 支付宝支付回调 事务业务类
 * 无接口，单类实现，保证事务一致性
 */
@Service
@RequiredArgsConstructor
public class AlipayCallbackService {

    // 注入所需服务
    private final AiRechargeOrderService aiRechargeOrderService;
    private final AiRechargeGoodsService aiRechargeGoodsService;
    private final AiUserPointBalanceService aiUserPointBalanceService;

    /**
     * 支付成功回调核心业务（事务保证）
     * @param orderId 业务订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void rechargeCallback(Long orderId) {
        // 1. 查询订单
        AiRechargeOrder order = aiRechargeOrderService.getRechargeOrderById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在，订单ID：" + orderId);
        }

        // 2. 幂等校验：已支付直接返回，不重复处理
        if (PayStatusEnum.PAY_SUCCESS == order.getStatus()) {
            return;
        }

        // 3. 更新订单状态为支付成功
        aiRechargeOrderService.updateRechargeOrderStatus(
                orderId,
                PayStatusEnum.PAY_SUCCESS,
                order.getUserId()
        );

        // 4. 查询商品信息（从订单中取商品ID）
        AiRechargeGoods goods = aiRechargeGoodsService.getRechargeGoodsById(order.getGoodsId());
        if (goods == null) {
            throw new RuntimeException("商品不存在，商品ID：" + order.getGoodsId());
        }

        // 5. 增加用户积分（从商品中取积分数量）
        Long addPoint = goods.getPoint();
        // 注意：userId类型转换（如果你的userId是Long，去掉intValue()）
        aiUserPointBalanceService.addTotalPointByUserId(order.getUserId(), addPoint);
    }
}
