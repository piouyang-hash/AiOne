package org.myfx.controls.aione.Demo;

import lombok.Data;

@Data
public class TianGameNews {
    private String id;           // 新闻ID
    private String url;          // 详情链接
    private String ctime;        // 发布时间
    private String title;        // 标题
    private String picUrl;       // 图片链接
    private String source;       // 来源
    private String description;  // 描述
}