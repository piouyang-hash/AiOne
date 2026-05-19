package org.myfx.controls.aione.AiService.service.base.ai_chat_db.order.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.common.ai_chat_db.order.OrderTypeEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.order.PayStatusEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.order.PayTypeEnum;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.order.AiRechargeOrder;
import org.myfx.controls.aione.AiService.mapper.ai_chat_db.order.AiRechargeOrderMapper;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.order.AiRechargeOrderService;
import org.myfx.controls.aione.ServiceCommon.utils.SnowflakeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;

/**
 * AI充值订单 Service实现类
 * 无具体业务逻辑，仅搭建框架
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiRechargeOrderServiceImpl implements AiRechargeOrderService {

    // 注入Mapper（对应你之前的XML）
    private final AiRechargeOrderMapper aiRechargeOrderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRechargeOrder(BigDecimal amount) {
        // 1. 校验充值金额合法性（必填、大于0）
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("充值金额不能为空且必须大于0");
        }

        // 2. 生成雪花ID（订单ID）
        Long orderId = SnowflakeGenerator.generateId();

        // 3. 获取当前用户ID
        Integer userId = 2;
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户未登录或用户ID非法");
        }

        // 4. 计算积分
        BigDecimal pointBigDecimal = amount.multiply(new BigDecimal("1000000"));
        Long point = pointBigDecimal.longValueExact();

        // 5. 自动生成备注
        String remark = String.format("充值了%s元", amount);

        // 6. 组装订单实体
        AiRechargeOrder order = new AiRechargeOrder();
        order.setId(orderId);
        order.setUserId(userId);
        order.setAmount(amount);
        order.setPoint(point);
        order.setBusinessType(OrderTypeEnum.RECHARGE);
        order.setPayType(PayTypeEnum.ALIPAY);
        order.setStatus(PayStatusEnum.WAIT_PAY);
        order.setRemark(remark);

        // 7. 插入数据库
        int rows = aiRechargeOrderMapper.insert(order);
        if (rows <= 0) {
            log.error("创建充值订单失败，orderId:{}", orderId);
            throw new RuntimeException("创建订单失败，请重试");
        }

        // 🔥 直接返回订单ID（Long类型）
        return orderId;
    }

    // --------------- 新增：根据ID查询订单 ---------------
    @Override
    public AiRechargeOrder getRechargeOrderById(Long orderId) {
        // 1. 校验订单ID不能为空
        Assert.notNull(orderId, "订单ID不能为空");

        // 2. 调用Mapper查询订单
        AiRechargeOrder rechargeOrder = aiRechargeOrderMapper.selectByOrderId(orderId);

        // 3. 校验订单是否存在
        Assert.notNull(rechargeOrder, "查询的订单不存在，订单ID：" + orderId);

        // 4. 返回订单信息
        return rechargeOrder;
    }

    /**
     * 更新充值订单状态
     * 后续补充：参数接收、权限校验、调用mapper.updateById()
     */
    @Override
    public void updateRechargeOrderStatus() {
        // TODO 你后续补充具体实现
    }
}