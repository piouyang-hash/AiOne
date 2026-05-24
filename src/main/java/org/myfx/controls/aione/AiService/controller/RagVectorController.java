package org.myfx.controls.aione.AiService.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.service.RagVectorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * RAG向量测试控制器（全GET请求，浏览器直接访问测试）
 */
@RestController
@RequestMapping("/rag")
@Slf4j
public class RagVectorController {

    @Resource
    private RagVectorService ragVectorService;

    /**
     * 新增向量（GET，浏览器直接传文本测试）
     * 链接示例：http://localhost:8080/rag/add?text=这是测试文本
     */
    @GetMapping("/add")
    public String addVector(@RequestParam("text") String text) {
        try {
            ragVectorService.addVector(text);
            return "向量插入成功！";
        } catch (Exception e) {
            return "向量插入失败：" + e.getMessage();
        }
    }

    /**
     * 查询所有向量并打印校验（维度+前15位，检查是否重复）
     * 链接：http://localhost:8080/rag/queryAll
     */
    @GetMapping("/queryAll")
    public String queryAllVectors() {
        try {
            ragVectorService.queryAndPrintAllVectors();
            return "向量查询成功，请查看控制台日志！";
        } catch (Exception e) {
            return "向量查询失败：" + e.getMessage();
        }
    }

    /**
     * 余弦相似度检索（RAG推荐）
     * 测试链接：http://localhost:8080/rag/searchCosine?text=测试查询&topN=3
     */
    @GetMapping("/searchCosine")
    public Object searchCosine(
            @RequestParam("text") String text,
            @RequestParam(defaultValue = "3") int topN) {
        try {
            return ragVectorService.searchByCosine(text, topN);
        } catch (Exception e) {
            return "检索失败：" + e.getMessage();
        }
    }

    /**
     * 欧式距离检索
     * 测试链接：http://localhost:8080/rag/searchEuclidean?text=测试查询&topN=3
     */
    @GetMapping("/searchEuclidean")
    public Object searchEuclidean(
            @RequestParam("text") String text,
            @RequestParam(defaultValue = "3") int topN) {
        try {
            return ragVectorService.searchByEuclidean(text, topN);
        } catch (Exception e) {
            return "检索失败：" + e.getMessage();
        }
    }

    /**
     * 【无参一键测试】
     * 自动插入5条不同句子 + 自动用指定句子检索
     * 测试链接：http://localhost:8080/rag/testAuto
     */
    @GetMapping("/testAuto")
    public Object testAutoProcess() {
        try {
            // ===================== 第一步：自动插入5条不同的测试句子 =====================
            log.info("=== 开始自动插入测试句子 ===");
            ragVectorService.addVector("人工智能是未来的核心技术");
            ragVectorService.addVector("Java编程开发效率很高");
            ragVectorService.addVector("MySQL数据库存储向量数据");
            ragVectorService.addVector("春天的花开得非常美丽");
            ragVectorService.addVector("火锅是我最喜欢的美食");
            log.info("=== 5条测试句子插入完成 ===");

            // ===================== 第二步：固定指定句子，自动检索（匹配人工智能） =====================
            String searchText = "人工智能技术发展前景";
            log.info("=== 开始检索，查询句子：{} ===", searchText);
            var result = ragVectorService.searchByCosine(searchText, 3);

            // 返回最终检索结果
            return "全自动测试完成！\n检索句子：" + searchText + "\nTop3匹配结果：" + result;

        } catch (Exception e) {
            return "全自动测试失败：" + e.getMessage();
        }
    }
}