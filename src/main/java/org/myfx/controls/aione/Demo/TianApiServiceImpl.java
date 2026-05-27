package org.myfx.controls.aione.Demo;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class TianApiServiceImpl implements TianApiService {

    @Resource
    private RestTemplate restTemplate;

    @Value("${tianapi.key}")
    private String apiKey;

    @Value("${tianapi.game-url}")
    private String gameUrl;

    @Value("${tianapi.enmaxim-url}")
    private String enMaximUrl;

    /**
     * 恢复原版可用写法：获取游戏资讯
     */
    @Override
    public TianApiResponse<TianApiResult<TianGameNews>> getGameNews(Integer num) {
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("key", apiKey);
        paramMap.add("num", num);
        paramMap.add("form", 1);
        paramMap.add("rand", 1);

        ParameterizedTypeReference<TianApiResponse<TianApiResult<TianGameNews>>> typeReference =
                new ParameterizedTypeReference<>() {};

        ResponseEntity<TianApiResponse<TianApiResult<TianGameNews>>> responseEntity =
                restTemplate.exchange(gameUrl,
                        HttpMethod.POST,
                        new HttpEntity<>(paramMap),
                        typeReference);

        return responseEntity.getBody();
    }

    /**
     * 恢复原版可用写法：获取英语美句
     */
    @Override
    public TianApiResponse<TianEnMaxim> getEnMaxim() {
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("key", apiKey);

        ParameterizedTypeReference<TianApiResponse<TianEnMaxim>> typeReference =
                new ParameterizedTypeReference<>() {};

        // 使用 exchange 方法
        ResponseEntity<TianApiResponse<TianEnMaxim>> responseEntity =
                restTemplate.exchange(enMaximUrl,
                        HttpMethod.POST,
                        new HttpEntity<>(paramMap),
                        typeReference);

        return responseEntity.getBody();
    }
}