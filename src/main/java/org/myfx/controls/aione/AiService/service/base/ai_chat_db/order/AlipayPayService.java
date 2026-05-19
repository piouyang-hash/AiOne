package org.myfx.controls.aione.AiService.service.base.ai_chat_db.order;

import com.alipay.api.AlipayApiException;
import org.myfx.controls.aione.AiService.dto.order.AlipayPayDTO;

/**
 * 支付宝支付业务接口
 */
public interface AlipayPayService {

    /**
     * 生成支付宝电脑端支付表单HTML
     * @param payDTO 支付参数
     * @return 支付宝返回的自动提交表单
     * @throws AlipayApiException 支付宝接口异常
     */
    String generatePayForm(AlipayPayDTO payDTO) throws AlipayApiException;
}