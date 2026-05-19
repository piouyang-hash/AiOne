package org.myfx.controls.aione.AiService.entity.ai_chat_db.token;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "AI Token类型配置实体（多厂商+输入输出解耦）")
public class AiTokenType {

    @Schema(description = "Token类型ID（主键）")
    private Long typeId;

    @Schema(description = "厂商编码 DEEPSEEK-深度求索 DOUBAO-火山豆包 OPENAI-GPT")
    private String vendor;

    @Schema(description = "Token方向 1-输入(提示词) 2-输出(回答)")
    private Integer tokenSide;

    @Schema(description = "类型唯一编码 例：DEEPSEEK_INPUT、DEEPSEEK_OUTPUT")
    private String typeCode;

    @Schema(description = "类型名称 例：DeepSeek输入Token、DeepSeek输出Token")
    private String typeName;

    @Schema(description = "每百万Token价格（元），例：10.00=10元/百万Token")
    private BigDecimal pricePerMillion;

    @Schema(description = "状态 0-禁用 1-启用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}