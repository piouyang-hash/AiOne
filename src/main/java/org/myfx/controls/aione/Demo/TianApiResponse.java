package org.myfx.controls.aione.Demo;

import lombok.Data;

/**
 * 天行接口统一返回实体（泛型，适配所有接口）
 * T = 具体的业务数据（游戏新闻/英语句子）
 */
@Data
public class TianApiResponse<T> {
    private String msg;
    private Integer code;
    private T result; // 核心：result直接接收泛型，不强制套list
}