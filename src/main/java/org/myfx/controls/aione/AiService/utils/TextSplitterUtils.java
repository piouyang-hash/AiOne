package org.myfx.controls.aione.AiService.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class TextSplitterUtils {

    // 定义需要切分的标点符号集合
    private static final Set<Character> PUNCTUATIONS = Set.of(
           // ',', '，',
             '.', '。',
            '?', '？',
            '!', '！'
    );

    // 先定义：网址允许的合法字符（和我们之前聊的一致）
    private static boolean isUrlLegalChar(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                (c >= '0' && c <= '9') ||
                c == ':' || c == '/' || c == '.' || c == '?' || c == '=' || c == '&' || c == '-' || c == '_';
    }

    // 推荐：将ObjectMapper声明为静态单例，复用提升性能
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 将流式文本按照标点符号和最小长度切分为 List
     *
     * @param inputFlux 输入的流式文本
     * @param minLength 最小切分长度
     * @return 包含切分后句子的 List
     */
    public static Mono<List<String>> splitFluxToList(Flux<String> inputFlux, int minLength) {
        // 使用 StringBuilder 作为缓冲区
        StringBuilder buffer = new StringBuilder();
        List<String> result = new ArrayList<>();

        // 定义匹配标点的正则表达式（包含中英文逗号、句号、问号、感叹号）
        // 如果你需要增加其他符号，可以直接加在 [] 里面
        String regex = "[,，.。?？!！]";
        Pattern pattern = Pattern.compile(regex);

        return inputFlux
                .concatWith(Flux.just("")) // 确保最后能触发收尾逻辑
                .doOnNext(chunk -> {
                    buffer.append(chunk);

                    // 循环检测缓冲区是否满足切分条件
                    while (true) {
                        Matcher matcher = pattern.matcher(buffer.toString());
                        boolean found = false;

                        while (matcher.find()) {
                            int endIdx = matcher.end(); // 标点符号的位置（包含标点）

                            // 检查当前标点前的长度是否满足最小值
                            if (endIdx >= minLength) {
                                String sentence = buffer.substring(0, endIdx);
                                result.add(sentence);

                                // 从缓冲区移除已切分的部分
                                buffer.delete(0, endIdx);
                                found = true;
                                break; // 重新开始从头匹配新的 buffer
                            }
                        }

                        // 如果没有找到满足长度的标点，或者 buffer 已经处理完，跳出循环
                        if (!found) break;
                    }
                })
                .then(Mono.fromCallable(() -> {
                    // Flux 结束后的最后处理：如果 buffer 还有剩余，全部加入 list
                    if (!buffer.isEmpty()) {
                        result.add(buffer.toString());
                    }
                    return result;
                }));
    }

    /**
     * 清理结果集：去掉每一句末尾的逗号或句号（问号保留）
     *
     * @param sentences 切分好的字符串列表
     * @return 清理后的字符串列表
     */
    public static List<String> cleanPunctuation(List<String> sentences) {
        if (sentences == null || sentences.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> cleanedList = new ArrayList<>();
        for (String s : sentences) {
            if (s == null || s.isEmpty()) {
                continue;
            }

            String trimmed = s.trim();
            int length = trimmed.length();
            char lastChar = trimmed.charAt(length - 1);

            // 判断最后一个字符是否为逗号或句号（包含中英文）
            if (lastChar == ',' || lastChar == '，' || lastChar == '.' || lastChar == '。') {
                cleanedList.add(trimmed.substring(0, length - 1));
            } else {
                cleanedList.add(trimmed);
            }
        }
        return cleanedList;
    }


    /**
     * 【公共静态方法】
     * 自动读取 resources/AiOutPut.txt → 逐字符流式输出（加速版）
     */
    public static Flux<String> generateCharFlux() {
        try {
            // 1. 读取 resource 下的 AiOutPut.txt 文件
            ClassPathResource resource = new ClassPathResource("AiOutPut.txt");
            byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
            String content = new String(bytes, StandardCharsets.UTF_8);

            // 2. 逐字符流转Flux，延迟10ms（加速！原100ms）
            return Flux.fromIterable(() ->
                            content.chars()
                                    .mapToObj(c -> String.valueOf((char) c))
                                    .iterator()
                    )
                    .delayElements(Duration.ofMillis(10)); // 🔥 速度加快10倍

        } catch (Exception e) {
            // 文件不存在/读取失败 → 返回空流
            return Flux.empty();
        }
    }

    /**
     * 高级流式切分器：将字符碎片流转换为符合语义边界的句子流。
     * * 【核心特性】：
     * 1. 语义保护：自动识别双引号("")、中文引号(“”)和 Markdown 代码块(```)，
     * 处于这些区域内部的标点符号不会触发切分，确保引用内容和代码逻辑的完整性。
     * 2. 强换行策略：遇到换行符(\n)时忽略 minLength 限制立即切分，适配列表与分段场景。
     * 3. 智能消抖：自动合并并忽略切分点后紧跟的连续换行符，防止产生空句子。
     * 4. 线程安全：每个订阅者独立维护 StringBuilder 缓冲区，支持高并发调用。
     * * @param inputFlux 原始字符块流
     * @param minLength 触发标点切分的最小字符长度阈值（换行符不受此限制）
     * @return 干净、完整的句子流
     */
    // ====================== 2. 对外主方法（仅保留响应式骨架，极简清晰） ======================
    public static Flux<String> splitStream(Flux<String> inputFlux, int minLength) {
        return Flux.defer(() -> {
            // 【状态安全】原buffer保留，仅作用域不变，线程安全
            StringBuilder buffer = new StringBuilder();

            return inputFlux
                    .flatMap(chunk -> handleTextChunk(buffer, chunk, minLength))
                    .concatWith(handleRemainingBuffer(buffer));
        });
    }

    /**
     * 🔥 新增：移除 --- 分隔线（匹配---\n、---\r\n，直接删除，不参与切分）
     */
    private static void removeSeparatorLines(StringBuilder buffer) {
        String separator = "---\n";
        String separatorWin = "---\r\n";
        // 循环删除开头的分隔线
        while (true) {
            if (buffer.indexOf(separator) == 0) {
                buffer.delete(0, separator.length());
            } else if (buffer.indexOf(separatorWin) == 0) {
                buffer.delete(0, separatorWin.length());
            } else {
                break;
            }
        }
    }

    // ====================== 3. 核心拆分：处理单个文本块（原flatMap核心逻辑） ======================
    /**
     * 处理单个流式文本块，返回切分后的句子集合
     */
    private static Flux<String> handleTextChunk(StringBuilder buffer, String chunk, int minLength) {
        // 原空值判断，完全保留
        if (chunk == null || chunk.isEmpty()) {
            return Flux.empty();
        }
        buffer.append(chunk);

        List<String> sentences = new ArrayList<>();
        processBufferLoop(buffer, sentences, minLength);

        return Flux.fromIterable(sentences);
    }

    // ====================== 4. 核心拆分：缓冲循环切分（原while(true) 大循环） ======================
    /**
     * 循环处理缓冲，直到无可用切分点
     */
    private static void processBufferLoop(StringBuilder buffer, List<String> sentences, int minLength) {
        while (true) {
            // 🔥 新增：先删除所有 --- 分隔线，从源头过滤
            removeSeparatorLines(buffer);

            // 【核心】查找切分点（原最复杂的for循环，完全抽离）
            SplitResult splitResult = findSplitIndex(buffer, minLength);
            int splitIndex = splitResult.splitIndex();
            boolean isNewlineSplit = splitResult.isNewlineSplit();

            if (splitIndex == -1) {
                break;
            }

            // 原切分、清理逻辑
            String fullSentence = buffer.substring(0, splitIndex + 1);
            String cleaned = cleanSingleSentence(fullSentence);
            if (!cleaned.isEmpty()) {
                sentences.add(cleaned);
            }

            // 删除已切分内容
            buffer.delete(0, splitIndex + 1);

            // 处理连续换行（原逻辑完全抽离）
            if (isNewlineSplit) {
                removeConsecutiveNewlines(buffer);
            }
        }
    }

    // ====================== 5. 核心拆分：查找切分点（原最复杂的嵌套逻辑，独立维护） ======================
    /**
     * 切分结果封装：避免多返回值，状态清晰
     * @param splitIndex 切分下标，-1=无切分点
     * @param isNewlineSplit 是否为换行符触发的切分
     */
    private record SplitResult(int splitIndex, boolean isNewlineSplit) {}

    /**
     * 终极文本切分：保护所有特殊区间不被切分
     * 保护范围：网址 | 反引号` | 代码块``` | 加粗** | 双引号" | 单引号' | 小括号() | Markdown表格
     */
    private static SplitResult findSplitIndex(StringBuilder buffer, int minLength) {
        int splitIndex = -1;
        boolean isNewlineSplit = false;

        // 保护状态
        boolean inQuote = false;
        boolean inSingleQuote = false;
        boolean inInlineCode = false;
        boolean inCodeBlock = false;
        boolean inBold = false;
        boolean inUrl = false;
        boolean inParenthesis = false;
        boolean inMarkdownTable = false;

        for (int i = 0; i < buffer.length(); i++) {
            char c = buffer.charAt(i);

            // ====================== 1. 表格检测（最优先！） ======================
            if (!inMarkdownTable) {
                // 检测表格起始：连续两行包含 | 的模式（更可靠）
                if (isStartOfMarkdownTable(buffer, i)) {
                    inMarkdownTable = true;
                }
            } else {
                // 表格内：检测是否退出表格（遇到空行或非表格行）
                if (c == '\n' && i + 1 < buffer.length()) {
                    char next = buffer.charAt(i + 1);
                    if (next != ' ' && next != '|' && next != '\n') {
                        // 下一行不是 | 开头 → 认为表格结束
                        inMarkdownTable = false;
                    }
                }
            }

            // ====================== 表格内完全禁止切分 ======================
            if (inMarkdownTable) {
                splitIndex = -1;   // 强制不切分
                continue;          // 继续遍历，但不记录任何切分点
            }

            // ====================== 其他保护状态（保持原有逻辑） ======================
            // 1. URL
            if (!inUrl) {
                if (i + 7 < buffer.length() && buffer.substring(i, i + 7).equals("http://") ||
                        i + 8 < buffer.length() && buffer.substring(i, i + 8).equals("https://")) {
                    inUrl = true;
                    continue;
                }
            } else if (!isUrlLegalChar(c)) {
                inUrl = false;
            }

            // 2. 代码块 + 行内代码
            if (c == '`') {
                if (i + 2 < buffer.length() && buffer.charAt(i+1) == '`' && buffer.charAt(i+2) == '`') {
                    inCodeBlock = !inCodeBlock;
                    i += 2;
                    continue;
                } else {
                    inInlineCode = !inInlineCode;
                    continue;
                }
            }

            // 3. 行内和代码块合并了

            // 4. **bold**
            if (c == '*' && i + 1 < buffer.length() && buffer.charAt(i + 1) == '*') {
                inBold = !inBold;
                i++;
                continue;
            }

            // 5. 引号
            if (c == '"' || c == '“' || c == '”') { inQuote = !inQuote; continue; }
            // 单引号 / 撇号处理 —— 区分真正的引号和英文所有格/缩写
            if (c == '\'' || c == '‘' || c == '’') {
                // 判断是否是英文所有格或缩写（如 Spring’s, don’t, it’s）
                boolean isLikelyApostrophe = isApostropheInWord(buffer, i);

                if (isLikelyApostrophe) {
                    // 所有格/缩写：不切换状态！直接跳过
                    // （也可以 i++ 继续，但这里 continue 更安全）
                } else {
                    // 真正的单引号（成对使用）
                    inSingleQuote = !inSingleQuote;
                }
                continue;
            }

            // 6. 括号
            if (c == '(' || c == '（') { inParenthesis = true; continue; }
            if (c == ')' || c == '）') { inParenthesis = false; continue; }

            // ====================== 切分逻辑（只有不在任何保护中才执行） ======================
            if (!inQuote && !inSingleQuote && !inInlineCode && !inCodeBlock &&
                    !inBold && !inUrl && !inParenthesis) {

                if (c == '\n') {
                    splitIndex = i;
                    isNewlineSplit = true;
                    break;   // 找到就立即返回
                }

                if (PUNCTUATIONS.contains(c) && i + 1 >= minLength) {
                    splitIndex = i;
                    break;
                }
            }
        }

        return new SplitResult(splitIndex, isNewlineSplit);
    }

    private static boolean isApostropheInWord(StringBuilder buffer, int i) {
        if (i == 0 || i >= buffer.length() - 1) return true;

        char prev = buffer.charAt(i - 1);
        char next = buffer.charAt(i + 1);

        // 情况1：常见所有格（word's 或 words'）
        if (Character.isLetter(prev)) {
            // s' 形式（复数所有格，如 monks'）
            if (prev == 's' || prev == 'S') {
                return true;                    // 直接视为撇号，不切换状态
            }
            // 普通 's（单数所有格，如 Spring's）
            if (Character.isLetter(next) || next == 's' || next == 'S' || next == ' ') {
                return true;
            }
        }

        // 情况2：缩写（don't, it's, you're 等）
        if (Character.isLetter(prev) && Character.isLetter(next)) {
            return true;
        }

        // 情况3：其他常见情况（数字年份如 90's, 2020's 等）
        if (Character.isDigit(prev)) {
            return true;
        }

        return false;   // 其他情况才当作真正的引号
    }

    private static boolean isStartOfMarkdownTable(StringBuilder sb, int i) {
        // 简单但有效的检测：当前行或接下来几行包含 | 分隔符 + 对齐行
        if (i > 0 && sb.charAt(i - 1) != '\n') return false;

        // 向后扫描最多 3 行
        int pipeCount = 0;
        int dashCount = 0;
        for (int j = i; j < Math.min(i + 200, sb.length()); j++) {
            char c = sb.charAt(j);
            if (c == '|') pipeCount++;
            if (c == '-') dashCount++;

            if (c == '\n') {
                if (pipeCount >= 2) return true;   // 至少有两个 | 就是表格行
                if (dashCount > 3) return true;    // --- 分隔行
                pipeCount = 0;
                dashCount = 0;
            }
        }
        return false;
    }

    // ====================== 6. 工具方法抽离：独立可测试 ======================
    /**
     * 移除连续换行符（原while循环抽离）
     */
    private static void removeConsecutiveNewlines(StringBuilder buffer) {
        while (!buffer.isEmpty() && (buffer.charAt(0) == '\n' || buffer.charAt(0) == '\r')) {
            buffer.deleteCharAt(0);
            // 原日志保留，按需开启
            // System.out.println("  [忽略] 紧跟在换行符后的连续换行");
        }
    }


    /**
     * 处理缓冲剩余内容（原concatWith兜底逻辑）
     */
    private static Flux<String> handleRemainingBuffer(StringBuilder buffer) {
        return Flux.defer(() -> {
            if (!buffer.isEmpty()) {
                String last = cleanSingleSentence(buffer.toString());
                return last.isEmpty() ? Flux.empty() : Flux.just(last);
            }
            return Flux.empty();
        });
    }


    /**
     * 仅清理【句子开头】的 Markdown 标题符号（#、##、###...最多6个）
     * 安全无副作用：绝不误删正文中间/结尾的 # 符号
     */
    private static String cleanMarkdownSymbols(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        // 🔥 终极安全版：只删开头的标题 #，其他#全部保留
        return s.replaceAll("^#{1,6}\\s*", "");
    }

    /**
     * 独立方法2：清理句子末尾标点 + 首尾空格
     * 单一职责：只处理标点和空白
     */
    private static String trimTrailingPunctuation(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return s.trim()
                // 只删除末尾的逗号、句号
                .replaceAll("[,，.。]$", "")
                .trim();
    }

    /**
     * 【对外统一入口】组合清理逻辑
     * 原有调用方完全不用改！零侵入
     */
    public static String cleanSingleSentence(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        // 先清格式 → 再清标点（顺序可随意调整）
        String cleaned = cleanMarkdownSymbols(s);
        cleaned = trimTrailingPunctuation(cleaned);
        return cleaned;
    }

    /**
     * 2. 核心合并算法（线程安全：序号和列表完全隔离）
     * 将 Flux<String> 转换为一个包含标准 JSON 的 Flux
     */
    public static Flux<String> collectSentencesToJson(Flux<String> sentenceFlux) {
        // 利用 collectList 在流结束时聚集所有句子
        return sentenceFlux.collectList().map(allSentences -> {
            // 使用 IntStream 生成带序号的 Map: {1: "第一句", 2: "第二句"}
            // 这里在方法内部处理，天然线程安全
            String segmentsJson = IntStream.range(0, allSentences.size())
                    .mapToObj(i -> String.format("\"%d\": \"%s\"", i + 1, allSentences.get(i).replace("\"", "\\\"")))
                    .collect(Collectors.joining(", ", "{", "}"));

            return String.format(
                    "{\"total\": %d, \"segments\": %s}",
                    allSentences.size(),
                    segmentsJson
            );
        }).flux(); // 转回 Flux 以适配接口
    }

    /**
     * 生成带【每段独立时间戳】的分段JSON
     * 时间戳 = 句子真实生成时间
     * @param allSentencesWithTime 带内容+时间戳的列表
     * @return 合法JSON字符串
     */
    public static String generateJsonFromList(List<Map<String, Object>> allSentencesWithTime) {
        Map<String, Object> jsonObj = new HashMap<>();

        int total = allSentencesWithTime.size();
        jsonObj.put("total", total);

        // 组装分段数据（包含content + timestamp）
        Map<Integer, Map<String, Object>> segments = new HashMap<>();
        for (int i = 0; i < allSentencesWithTime.size(); i++) {
            Integer seq = i + 1;
            segments.put(seq, allSentencesWithTime.get(i));
        }
        jsonObj.put("segments", segments);

        // 序列化
        try {
            return OBJECT_MAPPER.writeValueAsString(jsonObj);
        } catch (JsonProcessingException e) {
            log.error("生成带时间戳的JSON失败", e);
            return "{}";
        }
    }

}