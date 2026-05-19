package org.myfx.controls.aione.AiService.aiClient.AMTest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.ai.chat.client.ChatClient;

/**
 * AI模型客户端包装类
 * 整合【底层AI调用客户端】与【业务计费Token配置】，实现模型与计费规则强绑定
 */
@Data
@AllArgsConstructor
@Schema(description = "AI模型客户端（含技术调用能力 + 计费Token配置）")
public class AiModelClient {

    @Schema(description = "原生Spring AI对话客户端，用于实际发起AI请求", hidden = true)
    private final ChatClient chatClient;

    @Schema(description = "模型唯一标识名称", example = "deepseek")
    private final String modelName;

    @Schema(description = "输入Token类型ID（关联ai_token_type表，用于计费统计）", example = "1")
    private final Long inputTypeId;

    @Schema(description = "输出Token类型ID（关联ai_token_type表，用于计费统计）", example = "2")
    private final Long outputTypeId;
}
