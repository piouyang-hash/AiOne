package org.myfx.controls.aione.Demo;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tianapi")
public class TianApiController {

    @Resource
    private TianApiService tianApiService;

    /**
     * 获取游戏资讯
     */
    @GetMapping("/game/news")
    public TianApiResponse<TianApiResult<TianGameNews>> getGameNews(
            @RequestParam(defaultValue = "5") Integer num
    ) {
        return tianApiService.getGameNews(num);
    }

    /**
     * 获取英语美句
     */
    @GetMapping("/en/maxim")
    public TianApiResponse<TianEnMaxim> getEnMaxim() {
        return tianApiService.getEnMaxim();
    }
}