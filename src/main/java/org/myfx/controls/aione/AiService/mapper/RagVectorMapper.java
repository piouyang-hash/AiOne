package org.myfx.controls.aione.AiService.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.AiService.entity.RagVector;

import java.util.List;

@Mapper
public interface RagVectorMapper {

    /**
     * 插入向量（仅使用字符串类型 vector）
     */
    int insert(RagVector ragVector);

    /**
     * 根据ID查询
     */
    RagVector selectById(@Param("id") Long id);

    /**
     * 查询所有向量（用于手写检索）
     */
    List<RagVector> selectAll();
}