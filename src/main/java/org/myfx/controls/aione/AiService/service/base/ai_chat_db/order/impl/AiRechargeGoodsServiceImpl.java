package org.myfx.controls.aione.AiService.service.base.ai_chat_db.order.impl;

import jakarta.annotation.Resource;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.order.AiRechargeGoods;
import org.myfx.controls.aione.AiService.mapper.ai_chat_db.order.AiRechargeGoodsMapper;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.order.AiRechargeGoodsService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI充值商品 业务实现类
 */
@Service
public class AiRechargeGoodsServiceImpl implements AiRechargeGoodsService {

    @Resource
    private AiRechargeGoodsMapper aiRechargeGoodsMapper;

    @Override
    public void addRechargeGoods(AiRechargeGoods goods) {
        aiRechargeGoodsMapper.insert(goods);
    }

    @Override
    public AiRechargeGoods getRechargeGoodsById(Long id) {
        return aiRechargeGoodsMapper.selectById(id);
    }

    @Override
    public List<AiRechargeGoods> getAllRechargeGoods() {
        return aiRechargeGoodsMapper.selectAll();
    }
}
