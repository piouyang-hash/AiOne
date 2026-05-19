package org.myfx.controls.aione.AiService.utils;

import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.dto.redis.AiActivityScoreRedisDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * AI特征分值（活跃度）Redis操作工具类
 * 基于专用的aiActivityScoreRedisDTOTemplate封装，仅处理AI活跃度相关存储
 */
@Component
@Slf4j
public class AiActivityScoreRedisUtil {

    // 关键1：注入适配AI活跃度DTO的专用RedisTemplate（替换Bean名称+泛型类型）
    @Autowired
    private RedisTemplate<String, AiActivityScoreRedisDTO> aiActivityScoreRedisDTOTemplate;

    // 关键2：Redis Key前缀（替换为指定的emotion:score:ai:activity）
    private static final String ACTIVITY_SCORE_KEY_PREFIX = "emotion:score:ai:activity:";

    // 新增：构造器注入BatchPoissonProcessComponent（核心算法类）
    @Autowired
    private BatchPoissonProcessComponent batchPoissonProcessComponent;

    /**
     * 保存AI活跃度分值到Redis（核心方法：直接传入活跃度DTO，动态计算过期时间）
     * 调整规则：仅Redis中无该用户的活跃度数据时才写入，已有数据则不写入
     * 核心修改：过期时间改用BatchPoissonProcessComponent.calculateTimeToReach100计算（返回秒数）
     * @param scoreDTO AI活跃度Redis DTO（必传，且需包含userId）
     */
    public void saveAiActivityScore(AiActivityScoreRedisDTO scoreDTO) {
        // 空值校验（增强健壮性）
        if (scoreDTO == null) {
            log.warn("保存AI活跃度失败：活跃度DTO为空！");
            return;
        }
        Integer userId = scoreDTO.getUserId();
        if (userId == null) {
            log.warn("保存AI活跃度失败：DTO中用户ID为空！userId={}", userId);
            return;
        }

        // 核心：先校验Redis中是否已有该Key，有则直接返回不写入
        String redisKey = buildRedisKey(userId);
        AiActivityScoreRedisDTO existDTO = aiActivityScoreRedisDTOTemplate.opsForValue().get(redisKey);
        if (existDTO != null) {
            log.info("AI用户{} Redis中已存在活跃度数据，无需重复写入（Redis Key：{}）", userId, redisKey);
            return;
        }

        // 1. 从DTO中获取递增计算的核心参数
        Integer initialScore = scoreDTO.getActivityScore();
        // 兜底：默认分数为0
        if (initialScore == null) {
            initialScore = 50;
        }

        // 2. 核心修改：调用BatchPoissonProcessComponent计算过期时间（秒数）
        // 算法：calculateTimeToReach100(initialNumber) → 返回达到100所需的秒数
        long expireSeconds = batchPoissonProcessComponent.calculateTimeToReach100(initialScore);

        // 3. 按要求处理过期时间：
        // 3.1 过期时间=0 → 不存入Redis，直接返回
        if (expireSeconds == 0) {
            log.info("AI用户{} 活跃度分数对应的过期时间为0，无需存入Redis | 初始分数：{}", userId, initialScore);
            return;
        }
        // 3.2 极端情况：过期时间<0 → 抛运行时异常（算法返回非法值）
        if (expireSeconds < 0) {
            String errorMsg = String.format("AI用户%d 过期时间计算异常！BatchPoissonProcess返回值=%d秒（应为≥0）",
                    userId, expireSeconds);
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        // 4. 存入Redis（仅走到这一步说明Key不存在+分数有效+过期时间合法）
        aiActivityScoreRedisDTOTemplate.opsForValue().set(redisKey, scoreDTO, expireSeconds, TimeUnit.SECONDS);

        // 5. 日志简化：仅打印有效存入的信息
        log.info("AI用户{} 活跃度已存入Redis | 分数：{} | 过期时间：{}秒（{}分钟）",
                userId, initialScore, expireSeconds, expireSeconds / 60.0);
    }

    /**
     * 构造Redis Key（仅保留用户ID，移除会话ID）
     */
    private String buildRedisKey(Integer userId) {
        return ACTIVITY_SCORE_KEY_PREFIX + userId;
    }

    /**
     * 根据用户ID删除Redis中的AI活跃度DTO（仅当数据存在时删除），并返回删除前的活跃度递增值
     * @param userId 用户ID（必传，非空）
     * @return 不同场景返回值说明：
     *         1. Redis中无该用户的活跃度数据 → 返回-1；
     *         2. 参数不合法（userId为空）→ 直接抛IllegalArgumentException，无返回值；
     *         3. 时间戳参数缺失（上次收到消息/初始化时间戳均为空）→ 直接抛RuntimeException，无返回值；
     *         4. 正常计算场景 → 返回「递增后分数 - 初始分数」（即本次递增的具体分值）：
     *            - 取值范围：0 ≤ 返回值 ≤ (100 - 初始分数)；
     *            - 0表示无递增（分数未变化）；
     *            - 正数表示从初始分数递增到当前分数的差值（用于后续业务的加分逻辑）。
     * 核心逻辑：基于泊松过程计算从「起始时间戳」到「删除时」的活跃度递增分值，删除数据后返回该递增值
     */
    public Integer deleteAiActivityScore(Integer userId) {
        // 1. 空值校验（参数不合法直接抛异常，无返回值）
        if (userId == null) {
            String errorMsg = String.format("删除AI活跃度失败：参数不合法！用户ID为空（userId=%s）", userId);
            log.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        // 2. 构建Redis Key
        String redisKey = buildRedisKey(userId);

        // 3. 先查询数据是否存在：无数据直接返回-1
        AiActivityScoreRedisDTO deletedDTO = aiActivityScoreRedisDTOTemplate.opsForValue().get(redisKey);
        if (deletedDTO == null) {
            log.info("AI用户{} 活跃度数据删除失败：Redis中无该用户数据（Key：{}）", userId, redisKey);
            return -1;
        }

        // 4. 提取基础参数并兜底：初始化分数（确保非空）
        Integer initialScore = deletedDTO.getActivityScore();
        if (initialScore == null) {
            initialScore = 0;
            log.warn("AI用户{} 活跃度初始分数为空，兜底为0（Key：{}）", userId, redisKey);
        }

        // 5. 提取递增计算的起始时间戳（优先取上次收到用户消息时间戳，兜底到初始化时间戳）
        Long initialTimestamp = deletedDTO.getLastReceiveUserMsgTimestamp();
        if (initialTimestamp == null) {
            initialTimestamp = deletedDTO.getInitializationTimestamp();
            // 时间戳全缺失：抛运行时异常，无返回值
            if (initialTimestamp == null) {
                String errorMsg = String.format("删除AI活跃度失败：AI用户%d 无有效时间戳（上次收到消息/初始化时间戳均缺失），参数不完整！", userId);
                log.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }
            log.info("AI用户{} 上次收到消息时间戳为空，使用初始化时间戳计算递增（时间戳：{}）", userId, initialTimestamp);
        }

        // 6. 计算时间差（秒）：当前时间 - 起始时间戳（确保非负）
        long currentTimestamp = System.currentTimeMillis();
        int timeSeconds = (int) ((currentTimestamp - initialTimestamp) / 1000);
        timeSeconds = Math.max(0, timeSeconds);
        log.debug("AI用户{} 活跃度递增计算时间差：{}秒（起始时间戳：{}，当前时间戳：{}）",
                userId, timeSeconds, initialTimestamp, currentTimestamp);

        // 7. 核心：调用泊松过程计算递增后的当前分数（限制在0-100，且不低于初始分数）
        int currentScore = batchPoissonProcessComponent.calculateIncrementTo100(initialScore, timeSeconds);
        currentScore = Math.max(initialScore, Math.min(100, currentScore)); // 兜底：递增后分数≥初始分数，且≤100

        // 8. 计算递增值：递增后分数 - 初始分数（正数=实际递增的分值，0=无递增）
        Integer incrementValue = currentScore - initialScore;
        log.debug("AI用户{} 活跃度递增计算结果：初始分数{} → 递增后分数{} → 递增值{}",
                userId, initialScore, currentScore, incrementValue);

        // 9. 执行删除操作
        Boolean deleteSuccess = aiActivityScoreRedisDTOTemplate.delete(redisKey);

        // 10. 日志输出（区分删除成功/失败）
        if (deleteSuccess) {
            log.info("AI用户{} 活跃度数据删除成功 | Redis Key：{} | 初始分数：{} | 递增后分数：{} | 本次递增值：{} | 计算时间差：{}秒",
                    userId, redisKey, initialScore, currentScore, incrementValue, timeSeconds);
        } else {
            log.warn("AI用户{} 活跃度数据查询存在，但删除失败！Redis Key：{} | 初始分数：{} | 递增后分数：{} | 本次递增值：{} | 计算时间差：{}秒",
                    userId, redisKey, initialScore, currentScore, incrementValue, timeSeconds);
        }

        // 11. 返回递增值（无论删除是否成功，均返回计算出的递增值）
        return incrementValue;
    }

    // 扩展：读取AI活跃度的方法
    public AiActivityScoreRedisDTO getAiActivityScore(Integer userId) {
        if (userId == null) {
            return null;
        }
        String redisKey = buildRedisKey(userId);
        return aiActivityScoreRedisDTOTemplate.opsForValue().get(redisKey);
    }

    /**
     * 校验指定userId对应的Key是否唯一（仅返回布尔值）
     * @param userId 用户ID（必传）
     * @return true=Key唯一（数量=1）；false=Key不唯一（数量>1）/无匹配/参数为空
     */
    private boolean isActivityScoreKeyUniqueByUserId(Integer userId) {
        // 1. 空值校验 → 直接返回false
        if (userId == null) {
            log.warn("校验AI活跃度Key唯一性失败：用户ID为空！");
            return false;
        }

        // 2. 构造Key前缀
        String keyPrefix = ACTIVITY_SCORE_KEY_PREFIX + userId;
        Set<String> matchedKeys = aiActivityScoreRedisDTOTemplate.keys(keyPrefix);

        // 3. 分场景判断并返回布尔值
        if (CollectionUtils.isEmpty(matchedKeys)) {
            log.info("AI用户ID{}无活跃度Key（前缀：{}）→ 不唯一", userId, keyPrefix);
            return false;
        } else if (matchedKeys.size() == 1) {
            log.info("AI用户ID{}的活跃度Key唯一：{} → 返回true", userId, matchedKeys.iterator().next());
            return true;
        } else {
            log.warn("AI用户ID{}查询到{}个活跃度Key（Key列表：{}）→ 不唯一",
                    userId, matchedKeys.size(), String.join(",", matchedKeys));
            return false;
        }
    }

    /**
     * 仅通过userId，查询Redis Key对应的AI活跃度DTO（保证仅返回一个）
     * @param userId 用户ID（必传）
     * @return 唯一DTO（Key不唯一/无匹配则返回null）
     */
    public AiActivityScoreRedisDTO getUniqueAiActivityScoreByUserId(Integer userId) {
        // 1. 先校验Key是否唯一
        boolean isUnique = isActivityScoreKeyUniqueByUserId(userId);
        if (!isUnique) {
            return null;
        }

        // 2. Key唯一 → 重新获取Key并读取DTO
        String keyPrefix = ACTIVITY_SCORE_KEY_PREFIX + userId;
        Set<String> matchedKeys = aiActivityScoreRedisDTOTemplate.keys(keyPrefix);
        String targetKey = matchedKeys.iterator().next();
        AiActivityScoreRedisDTO scoreDTO = aiActivityScoreRedisDTOTemplate.opsForValue().get(targetKey);

        // 3. 日志提示DTO结果
        if (scoreDTO != null) {
            log.info("AI用户ID{}查询到唯一活跃度DTO：Key={}，活跃度={}",
                    userId, targetKey, scoreDTO.getActivityScore());
        } else {
            log.warn("AI用户ID{}的活跃度Key{}存在，但值为空！", userId, targetKey);
        }

        return scoreDTO;
    }
}