package org.myfx.controls.aione.AiService.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.nio.charset.StandardCharsets;

/**
 * Prompt模板文件读取工具类（Spring组件，可注入使用）
 */
@Slf4j
@Component // 关键：添加@Component，让Spring管理
public class PromptTemplateReader {

    // 模板文件根路径（resources下的目录）
    private static final String PROMPT_TEMPLATE_ROOT = "prompts/";

    /**
     * 读取指定名称的Prompt模板文件并返回PromptTemplate实例
     * @param templateFileName 模板文件名（比如main-prompt.template）
     * @return PromptTemplate实例（UTF-8 编码）
     * @throws RuntimeException 文件读取失败时抛出
     */
    public PromptTemplate readTemplateFile(String templateFileName) { // 非静态方法，返回类型改为PromptTemplate
        try {
            // 拼接完整路径
            String filePath = PROMPT_TEMPLATE_ROOT + templateFileName;
            // 读取ClassPath下的文件（Spring标准方式）
            ClassPathResource resource = new ClassPathResource(filePath);
            if (!resource.exists()) {
                log.error("Prompt模板文件不存在：{}", filePath);
                throw new RuntimeException("模板文件不存在：" + filePath);
            }
            // 读取文件内容为UTF-8字符串（避免中文乱码）
            byte[] contentBytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
            String templateContent = new String(contentBytes, StandardCharsets.UTF_8);
            log.info("成功读取模板文件：{}，内容长度：{}字符", templateFileName, templateContent.length());

            // 核心修改：参考示例创建PromptTemplate实例并返回
            return new PromptTemplate(templateContent);
        } catch (Exception e) {
            log.error("读取Prompt模板文件失败，文件名：{}", templateFileName, e);
            throw new RuntimeException("读取Prompt模板文件失败：" + templateFileName, e);
        }
    }

    /**
     * 【重载方法】默认读取 PromptTemplate.txt 模板文件，直接返回文件内容字符串
     * @return 模板文件内容（UTF-8 编码）
     * @throws RuntimeException 文件读取失败时抛出
     */
    public String readTemplateFile() {
        // 调用带参方法，传入默认文件名，复用所有逻辑
        PromptTemplate promptTemplate = readTemplateFile("PromptTemplate.txt");
        // 返回模板内容字符串
        return promptTemplate.getTemplate();
    }

}