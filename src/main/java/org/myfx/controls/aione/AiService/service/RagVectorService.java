package org.myfx.controls.aione.AiService.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.entity.RagVector;
import org.myfx.controls.aione.AiService.mapper.RagVectorMapper;
import org.myfx.controls.aione.AiService.utils.VectorSearchUtil;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RagVectorService {

    @Resource
    private RagVectorMapper ragVectorMapper;

    // 你项目中的向量生成模型（和你之前的一致）
    @Resource
    private EmbeddingModel embeddingModel;

    /**
     * 仿照PG逻辑：文本 → float[]向量 → 插入MySQL
     * @param text 待向量化的文本
     */
    public void addVector(String text) {
        try {
            // 1. 参数校验
            Assert.hasText(text, "文本内容不能为空！");
            log.info("开始处理文本向量插入，文本内容：{}", text.substring(0, Math.min(text.length(), 20)));

            // 2. 生成 float[] 向量
            float[] embeddingArray = embeddingModel.embed(text);
            log.info("向量生成成功，向量维度：{}", embeddingArray.length);

            // 3. 封装实体
            RagVector ragVector = new RagVector();
            ragVector.setContent(text); // 🔥 新增：设置原始文本（原文）
            ragVector.setVectorList(embeddingArray);

            // 4. 插入数据库
            ragVectorMapper.insert(ragVector);
            log.info("文本向量插入成功！数据库自增ID：{}", ragVector.getId());

        } catch (Exception e) {
            log.error("文本向量插入失败！", e);
            throw new RuntimeException("向量插入失败：" + e.getMessage(), e);
        }
    }

    /**
     * 查询所有向量并精简打印：校验维度 + 前15位数字（检查是否重复）
     */
    public void queryAndPrintAllVectors() {
        try {
            // 1. 查询全部向量
            var vectorList = ragVectorMapper.selectAll();
            log.info("==================== 共查询到 {} 条向量数据 ====================", vectorList.size());

            if (vectorList.isEmpty()) {
                log.info("暂无向量数据！");
                return;
            }

            // 2. 遍历每条向量，精简打印
            for (RagVector ragVector : vectorList) {
                Long id = ragVector.getId();
                String vectorStr = ragVector.getVector();

                // 字符串转 float[] 数组
                float[] vector = stringToFloatArray(vectorStr);

                // ===================== 核心校验打印 =====================
                log.info("向量ID：{} | 向量维度：{}", id, vector.length);
                // 打印前15个值（检查是否重复）
                log.info("向量前15位：{}", Arrays.toString(Arrays.copyOf(vector, Math.min(15, vector.length))));
                // 省略超长部分，提示即可
                if (vector.length > 15) {
                    log.info("向量ID：{} | 后续数据已省略...", id);
                }
                log.info("--------------------------------------------------------");
            }
            log.info("======================================================================");

        } catch (Exception e) {
            log.error("查询并打印向量失败！", e);
        }
    }

    // ===================== 1. 余弦相似度检索（推荐！RAG标准用法） =====================
    public List<Map<String, Object>> searchByCosine(String queryText, int topN) {
        try {
            // 1. 入参校验
            Assert.hasText(queryText, "查询文本不能为空！");
            topN = Math.max(topN, 1);

            // 2. 生成查询向量
            float[] queryVector = embeddingModel.embed(queryText);

            // 3. 查询数据库所有向量
            List<RagVector> allVectors = ragVectorMapper.selectAll();
            if (allVectors.isEmpty()) return Collections.emptyList();

            // 4. 计算相似度 + 封装结果
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (RagVector vec : allVectors) {
                float[] dbVector = stringToFloatArray(vec.getVector());
                float similarity = VectorSearchUtil.cosineSimilarity(queryVector, dbVector);

                Map<String, Object> map = new HashMap<>();
                map.put("id", vec.getId());
                map.put("content", vec.getContent());
                map.put("similarity", similarity); // 相似度
                map.put("vectorPreview", getVectorPreview(dbVector)); // 向量前10位预览
                resultList.add(map);
            }

            // 5. 相似度【降序】排序（越大越靠前）
            resultList.sort((a, b) -> Float.compare((Float) b.get("similarity"), (Float) a.get("similarity")));

            // 6. 取TopN
            List<Map<String, Object>> topResult = resultList.stream().limit(topN).collect(Collectors.toList());
            log.info("余弦检索完成，查询文本：{}，返回Top{}结果", queryText, topN);
            return topResult;

        } catch (Exception e) {
            log.error("余弦检索失败", e);
            throw new RuntimeException("检索失败：" + e.getMessage());
        }
    }

    // ===================== 2. 欧式距离检索 =====================
    public List<Map<String, Object>> searchByEuclidean(String queryText, int topN) {
        try {
            Assert.hasText(queryText, "查询文本不能为空！");
            topN = Math.max(topN, 1);

            // 1. 生成查询向量
            float[] queryVector = embeddingModel.embed(queryText);

            // 2. 查询所有向量
            List<RagVector> allVectors = ragVectorMapper.selectAll();
            if (allVectors.isEmpty()) return Collections.emptyList();

            // 3. 计算距离
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (RagVector vec : allVectors) {
                float[] dbVector = stringToFloatArray(vec.getVector());
                float distance = VectorSearchUtil.euclideanDistance(queryVector, dbVector);

                Map<String, Object> map = new HashMap<>();
                map.put("id", vec.getId());
                map.put("content", vec.getContent());
                map.put("distance", distance); // 距离
                map.put("vectorPreview", getVectorPreview(dbVector));
                resultList.add(map);
            }

            // 4. 距离【升序】排序（越小越靠前）
            resultList.sort((a, b) -> Float.compare((Float) a.get("distance"), (Float) b.get("distance")));

            return resultList.stream().limit(topN).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("欧式距离检索失败", e);
            throw new RuntimeException("检索失败：" + e.getMessage());
        }
    }

    // ===================== 工具：向量前10位预览 =====================
    private String getVectorPreview(float[] vector) {
        if (vector == null || vector.length == 0) return "无向量";
        return Arrays.toString(Arrays.copyOf(vector, Math.min(10, vector.length)));
    }

    /**
     * 工具方法：数据库字符串向量 → float[] 数组
     */
    private float[] stringToFloatArray(String vectorStr) {
        if (vectorStr == null || vectorStr.isEmpty()) {
            return new float[0];
        }
        String[] strArr = vectorStr.split(",");
        float[] floatArr = new float[strArr.length];
        for (int i = 0; i < strArr.length; i++) {
            floatArr[i] = Float.parseFloat(strArr[i].trim());
        }
        return floatArr;
    }

}