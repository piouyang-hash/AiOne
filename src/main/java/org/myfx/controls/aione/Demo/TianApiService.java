package org.myfx.controls.aione.Demo;

public interface TianApiService {
    // 获取游戏新闻 → 返回 List<TianGameNews>
    TianApiResponse<TianApiResult<TianGameNews>> getGameNews(Integer num);

    // 获取英语句子 → 返回 List<TianEnMaxim>
    // ✅ TianEnMaxim 在这里正式绑定！
    TianApiResponse<TianEnMaxim> getEnMaxim();
}