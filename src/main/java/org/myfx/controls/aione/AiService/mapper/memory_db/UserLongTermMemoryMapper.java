package org.myfx.controls.aione.AiService.mapper.memory_db;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.AiService.entity.memory_db.UserLongTermMemory;

import java.util.List;

/**
 * 用户长期记忆向量表 Mapper 接口
 * 对应 PostgreSQL 表：public.user_long_term_memory
 */
@Mapper
public interface UserLongTermMemoryMapper {

//    /**
//     * 批量插入/更新（核心：仿照原 doAdd 逻辑，ON CONFLICT 自动更新）
//     * @param memoryList 记忆列表
//     */
//    void batchInsertOrUpdate(@Param("list") List<UserLongTermMemory> memoryList);

    // 新增：单条插入（核心，幂等更新）
    void insert(UserLongTermMemory memory);

    /**
     * RAG检索：查询用户的相似记忆（适配Spring AI的相似度阈值逻辑）
     * @param userId        用户ID（过滤条件）
     * @param queryEmbeddingStr 查询文本的向量字符串
     * @param similarityThreshold 相似度阈值（0~1，比如0.8）
     * @param topK          返回条数
     * @return 相似记忆列表
     */
    List<UserLongTermMemory> searchSimilarMemory(
            @Param("userId") Integer userId,
            @Param("queryEmbeddingStr") String queryEmbeddingStr,
            @Param("similarityThreshold") Double similarityThreshold,
            @Param("topK") int topK
    );

//    /**
//     * 根据ID查询
//     * @param id 主键ID
//     * @return 记忆实体
//     */
//    UserLongTermMemory selectById(@Param("id") UUID id);
//
//    /**
//     * 根据用户ID查询所有记忆
//     * @param userId 用户ID
//     * @return 记忆列表
//     */
//    List<UserLongTermMemory> selectByUserId(@Param("userId") Integer userId);
//
//    /**
//     * 查询所有记忆
//     * @return 记忆列表
//     */
//    List<UserLongTermMemory> selectAll();
//
//    /**
//     * 根据ID删除
//     * @param id 主键ID
//     */
//    void deleteById(@Param("id") UUID id);
//
//    /**
//     * 批量删除
//     * @param ids ID列表
//     */
//    void batchDeleteByIds(@Param("ids") List<UUID> ids);
}