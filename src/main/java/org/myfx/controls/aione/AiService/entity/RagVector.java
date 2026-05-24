package org.myfx.controls.aione.AiService.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * RAG向量实体类（无DO后缀）
 */
@Data
public class RagVector {
    /**
     * 自增主键
     */
    private Long id;

    /**
     * 原始文本（新增！检索时能看到的原文）
     */
    private String content;

    /**
     * 原始向量：1024维 float 数组（业务使用，纯数组，无List）
     */
    private float[] vectorList;

    /**
     * 字符串向量：逗号分隔的1024维向量（数据库存储使用）
     */
    private String vector;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 【核心】set方法：传入 float[] 数组
     * 自动拼接为逗号分隔字符串，赋值给数据库存储字段 vector
     */
    public void setVectorList(float[] vectorList) {
        this.vectorList = vectorList;
        // 数组转逗号分隔字符串（适配MySQL存储）
        if (vectorList != null && vectorList.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < vectorList.length; i++) {
                sb.append(vectorList[i]);
                if (i < vectorList.length - 1) {
                    sb.append(",");
                }
            }
            this.vector = sb.toString();
        }
    }
}