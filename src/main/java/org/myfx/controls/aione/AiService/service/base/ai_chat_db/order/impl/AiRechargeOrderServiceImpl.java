package org.myfx.controls.aione.AiService.service.base.ai_chat_db.order.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.common.ai_chat_db.order.OrderTypeEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.order.PayStatusEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.order.PayTypeEnum;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.order.AiRechargeOrder;
import org.myfx.controls.aione.AiService.mapper.ai_chat_db.order.AiRechargeOrderMapper;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.order.AiRechargeOrderService;
import org.myfx.controls.aione.ServiceCommon.context.UserContext;
import org.myfx.controls.aione.ServiceCommon.utils.SnowflakeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

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
    public Long createRechargeOrder(Long goodsId) {
        // 1. 核心参数校验
        if (goodsId == null || goodsId <= 0) {
            throw new IllegalArgumentException("商品ID不能为空且必须合法");
        }

        // 2. 校验用户是否存在未支付订单（原有逻辑保留）
        AiRechargeOrder unpaidOrder = getCurrentUserUnpaidOrder();
        if (unpaidOrder != null) {
            throw new RuntimeException("存在未支付的充值订单，请先取消后再创建新订单");
        }

        // 3. 生成订单雪花ID
        Long orderId = SnowflakeGenerator.generateId();

        // 4. 获取当前登录用户ID
        Integer userId = UserContext.getUserId();
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户未登录或用户ID非法");
        }

        // 5. 订单备注
        String remark = "AI积分充值订单";

        // 6. 组装订单实体（仅设置商品ID，无任何商品查询）
        AiRechargeOrder order = new AiRechargeOrder();
        order.setId(orderId);
        order.setUserId(userId);
        order.setGoodsId(goodsId); // 核心：直接绑定商品ID
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

        // 返回订单ID
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
        Assert.notNull(rechargeOrder, STR."查询的订单不存在，订单ID：\{orderId}");

        // 4. 返回订单信息
        return rechargeOrder;
    }

    // --------------- 新增：查询当前用户所有充值订单 ---------------
    @Override
    public List<AiRechargeOrder> getRechargeOrdersByCurrentUser() {
        // 1. 从上下文获取当前用户ID
        Integer userId = UserContext.getUserId();

        // 2. 校验用户ID有效性（非空校验）
        Assert.notNull(userId, "用户未登录或登录状态已失效");

        // 3. 调用Mapper查询当前用户所有订单
        List<AiRechargeOrder> rechargeOrders = aiRechargeOrderMapper.selectByUserId(userId);

        // 4. 处理空结果（避免返回null，返回空列表更安全）
        return rechargeOrders != null ? rechargeOrders : List.of();
    }

    @Override
    public AiRechargeOrder getCurrentUserUnpaidOrder() {
        // 1. 获取当前登录用户ID
        Integer userId = UserContext.getUserId();
        Assert.notNull(userId, "用户未登录");

        // 2. 调用Mapper：查询当前用户 待支付 订单
        List<AiRechargeOrder> unpaidOrderList = aiRechargeOrderMapper.selectByUserIdAndStatus(
                userId,
                PayStatusEnum.WAIT_PAY
        );

        // 3. 核心校验：待支付订单数量 > 1 → 数据异常，直接报错
        if (unpaidOrderList.size() > 1) {
            log.error("用户{}存在多条待支付订单，数据异常", userId);
            throw new RuntimeException("系统异常：存在多个未支付订单，请联系管理员");
        }

        // 4. 无订单返回null，有则返回唯一的订单
        return unpaidOrderList.isEmpty() ? null : unpaidOrderList.get(0);
    }

    @Override
    public void updateRechargeOrderStatus(Long id, PayStatusEnum targetStatus, Integer userId) {
        // 1. 断言校验
        Assert.notNull(id, "订单ID不能为空");
        Assert.notNull(targetStatus, "目标状态不能为空");
        Assert.notNull(userId, "用户ID不能为空");

        // 2. 封装订单参数
        AiRechargeOrder order = new AiRechargeOrder();
        order.setId(id);
        order.setUserId(userId);
        // 固定原始状态：仅允许操作 WAIT_PAY 待支付订单
        order.setOriginalStatus(PayStatusEnum.WAIT_PAY);
        order.setStatus(targetStatus);

        // 3. 调用Mapper更新
        aiRechargeOrderMapper.updateById(order);
    }

    /**
     * 【简化方法】关闭订单（状态固定为 已取消）
     * @param id 订单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeOrder(Long id) {
        // 从上下文获取用户ID
        Integer userId = UserContext.getUserId();
        // 调用主方法，固定状态为已取消
        updateRechargeOrderStatus(id, PayStatusEnum.CANCELLED, userId);
    }
}