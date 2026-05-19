package org.myfx.controls.aione.AiService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 测试用DTO，包含字符内容和ID标识
 */
@Data // Lombok注解，自动生成getter/setter/toString等（也可手动编写）
@NoArgsConstructor // 无参构造
@AllArgsConstructor // 全参构造
@Schema(description = "测试数据传输对象，用于封装字符流数据")
public class TestDTO {

    @Schema(
            description = "单个字符内容",
            example = "编",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String content;

    @Schema(
            description = "唯一标识，仅第一个数据ID为1、最后一个数据ID为10，其余为null",
            example = "1",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Integer id;
}