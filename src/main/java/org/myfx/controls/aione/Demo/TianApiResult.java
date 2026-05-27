package org.myfx.controls.aione.Demo;

import lombok.Data;
import java.util.List;

/**
 * 通用结果集（泛型 List）
 * 自动适配：List<TianGameNews> 或 List<TianEnMaxim>
 */
@Data
public class TianApiResult<T> {
    // 🔥 这里就是使用 TianEnMaxim / TianGameNews 的地方
    private List<T> list;
}