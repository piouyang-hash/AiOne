package org.myfx.controls.aione.AiService.mapper.ai_chat_db.order;

import org.apache.ibatis.annotations.Mapper;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.order.AiRechargeGoods;
import java.util.List;

/**
 * AI充值商品 Mapper 接口
 */
@Mapper
public interface AiRechargeGoodsMapper {

    /**
     * 插入充值商品
     */
    int insert(AiRechargeGoods goods);

    /**
     * 根据ID查询商品
     */
    AiRechargeGoods selectById(Long id);

    /**
     * 查询所有充值商品
     */
    List<AiRechargeGoods> selectAll();
}