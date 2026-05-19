package org.myfx.controls.aione.AiService.aiClient;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Getter
@Component
public class DeepSeekTokenizer {

    private final HuggingFaceTokenizer tokenizer;

    /**
     * 构造方法：从 Spring 配置文件读取 Tokenizer 路径
     */
    public DeepSeekTokenizer(@Value("${deepseek.tokenizer.path}") String localPath) throws IOException {
        Path tokenizerDir = Paths.get(localPath);

        // 检查路径是否存在
        if (!Files.exists(tokenizerDir)) {
            throw new FileNotFoundException("❌ 配置文件中 Tokenizer 路径不存在: " + localPath);
        }

        // 初始化 Tokenizer
        Map<String, String> options = new HashMap<>();
        options.put("maxLength", "131072");
        options.put("truncation", "false");
        this.tokenizer = HuggingFaceTokenizer.newInstance(tokenizerDir, options);

        System.out.println("✅ DeepSeek Tokenizer 已从配置文件加载成功！");
        System.out.println("📍 加载位置: " + localPath);
    }

    /**
     * 计算文本的 token 数量（包含特殊 token）
     */
    public int countTokens(String text) {
        if (text == null || text.isEmpty()) return 0;

        // 强制进行一次编码转换确认，或者直接使用 encode
        // 在某些版本的 Tokenizer 库中，直接传入 String 会触发默认编码转换
        // 尝试改用以下方式：
        Encoding encoding = tokenizer.encode(text);
        return (int) encoding.getIds().length;
    }

    /**
     * 只统计正文 token 数（不含 BOS/EOS）
     */
    public int countTokensWithoutSpecial(String text) {
        long[] ids = tokenizer.encode(text, false, false).getIds();
        return ids.length;
    }
}