package org.myfx.controls.aione.AiService.service.base.ai_chat_db.order;

import org.myfx.controls.aione.AiService.entity.ai_chat_db.order.AiRechargeGoods;
import java.util.List;

/**
 * AI充值商品 业务接口
 */
public interface AiRechargeGoodsService {

    /**
     * 新增充值商品
     */
    void addRechargeGoods(AiRechargeGoods goods);

    /**
     * 根据ID查询商品
     */
    AiRechargeGoods getRechargeGoodsById(Long id);

    /**
     * 查询所有启用的充值商品
     */
    List<AiRechargeGoods> getAllRechargeGoods();
}