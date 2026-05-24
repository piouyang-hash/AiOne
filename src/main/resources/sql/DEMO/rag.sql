-- RAG向量存储表（新增原文content字段）
CREATE TABLE IF NOT EXISTS rag_vector
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',
    content     TEXT NOT NULL COMMENT '向量对应的原始文本（原文）', -- 新增！
    vector      TEXT NOT NULL COMMENT '1024维浮点向量，逗号分隔存储',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT 'RAG向量存储表';