package org.myfx.controls.aione.AiService.utils;

/**
 * RAG向量检索核心算法
 * 欧式距离 + 余弦相似度
 */
public class VectorSearchUtil {

    /**
     * 1. 余弦相似度（文本检索首选！越大越相似）
     */
    public static float cosineSimilarity(float[] vec1, float[] vec2) {
        // 维度不一致直接返回0
        if (vec1 == null || vec2 == null || vec1.length != vec2.length || vec1.length != 1024) {
            return 0.0f;
        }

        float dotProduct = 0.0f;
        float norm1 = 0.0f;
        float norm2 = 0.0f;

        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            norm1 += Math.pow(vec1[i], 2);
            norm2 += Math.pow(vec2[i], 2);
        }

        float denominator = (float) (Math.sqrt(norm1) * Math.sqrt(norm2));
        if (denominator == 0) return 0.0f;
        return dotProduct / denominator;
    }

    /**
     * 2. 欧式距离（越小越相似）
     */
    public static float euclideanDistance(float[] vec1, float[] vec2) {
        if (vec1 == null || vec2 == null || vec1.length != vec2.length || vec1.length != 1024) {
            return Float.MAX_VALUE;
        }

        float sum = 0.0f;
        for (int i = 0; i < vec1.length; i++) {
            sum += Math.pow(vec1[i] - vec2[i], 2);
        }
        return (float) Math.sqrt(sum);
    }
}