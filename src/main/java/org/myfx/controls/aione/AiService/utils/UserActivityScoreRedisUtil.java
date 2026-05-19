package org.myfx.controls.aione.AiService.utils;

import cn.hutool.core.lang.Assert;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.dto.redis.UserActivityScoreRedisDTO;
import org.myfx.controls.aione.AiService.utils.DecayUtils.ScoreLinearDecayUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 用户特征分值（活跃度）Redis操作工具类
 * 基于专用的userFeatureScoreRedisDTOTemplate封装，仅处理活跃度相关存储
 */
@Component
@Slf4j
public class UserActivityScoreRedisUtil {

    // 关键1：注入适配活跃度DTO的专用RedisTemplate（同步修改Bean名称+泛型类型）
    @Resource(name = "userActivityScoreRedisDTOTemplate")
    private RedisTemplate<String, UserActivityScoreRedisDTO> userActivityScoreRedisDTOTemplate;

    // 关键2：Redis Key前缀（仅保留用户维度，移除会话相关）
    private static final String ACTIVITY_SCORE_KEY_PREFIX = "feature:score:user:activity:";


    /**
     * 保存用户活跃度分值到Redis（核心方法：直接传入活跃度DTO，动态计算过期时间）
     * 规则：仅Redis中无该用户的活跃度数据时才写入，已有数据则不写入
     * @param scoreDTO 用户活跃度Redis DTO（必传，且需包含userId）
     */
    public void saveUserActivityScore(UserActivityScoreRedisDTO scoreDTO) {
        // 简化空值校验（Assert替代冗长if）
        Assert.notNull(scoreDTO, "保存用户活跃度失败：活跃度DTO为空！");
        Integer userId = scoreDTO.getUserId();
        Assert.notNull(userId, "保存用户活跃度失败：DTO中用户ID为空！");

        // 核心：校验Redis中是否已有该Key，有则直接返回不写入
        String redisKey = buildRedisKey(userId);
        UserActivityScoreRedisDTO existDTO = userActivityScoreRedisDTOTemplate.opsForValue().get(redisKey);
        if (existDTO != null) {
            log.info("用户{} Redis中已存在活跃度数据，无需重复写入（Redis Key：{}）", userId, redisKey);
            return;
        }

        // 1. 从DTO中获取衰减计算的核心参数
        Integer initialScore = scoreDTO.getActivityScore();

        // 2. 调用工具类计算「衰减至0的总时间（毫秒）」
        long totalDecayTime = ScoreLinearDecayUtil.calculateZeroDecayTotalTime(initialScore);

        // 3. 按要求处理：
        // 3.1 总衰减时间=0（分数为0）→ 不存入Redis，直接返回
        if (totalDecayTime == 0) {
            log.info("用户{} 活跃度分数为0，无需存入Redis", userId);
            return;
        }
        // 3.2 极端情况（理论上不存在）：总衰减时间<0 → 抛运行时异常
        Assert.isTrue(totalDecayTime > 0, "用户{} 衰减时间异常！总衰减时间={}毫秒（应为≥0）", userId, totalDecayTime);

        // 4. 计算Redis过期时间（秒）：直接转秒，无兜底（已校验totalDecayTime>0）
        long expireSeconds = totalDecayTime / 1000;

        // 5. 存入Redis（仅走到这一步说明Key不存在+分数有效）
        userActivityScoreRedisDTOTemplate.opsForValue().set(redisKey, scoreDTO, expireSeconds, TimeUnit.SECONDS);

        // 6. 日志简化：仅打印有效存入的信息（移除会话ID）
        log.info("用户{} 活跃度已存入Redis | 分数：{} | 过期时间：{}秒（{}分钟）",
                userId, initialScore, expireSeconds, expireSeconds / 60.0);
    }

    /**
     * 构造Redis Key（仅保留用户ID，移除会话维度）
     */
    private String buildRedisKey(Integer userId) {
        return ACTIVITY_SCORE_KEY_PREFIX + userId;
    }

    /**
     * 根据用户ID删除Redis中的活跃度DTO（仅当数据存在时删除），并返回删除前的衰减具体值
     * @param userId 用户ID（必传）
     * @return 删除前的衰减具体值（衰减后分数 - 初始分数，无数据返回1，衰减值最大为0，用于后续减分）
     * 核心逻辑：返回值为「衰减后分数 - 初始分数」（负数表示衰减值，0表示无衰减），适配后续“相加即减分”的业务逻辑
     */
    public Integer deleteUserActivityScore(Integer userId) {
        // 简化参数校验（Assert替代if，直接抛异常）
        Assert.notNull(userId, "删除用户活跃度失败：用户ID为空！");

        // 2. 构建Redis Key（仅用户维度）
        String redisKey = buildRedisKey(userId);

        // 3. 先查询数据是否存在
        UserActivityScoreRedisDTO deletedDTO = userActivityScoreRedisDTOTemplate.opsForValue().get(redisKey);
        if (deletedDTO == null) {
            return 1; // 无数据返回1（保持原有业务规则）
        }

        // 4. 从DTO中提取参数，计算衰减后分数 + 衰减具体值
        Integer initialScore = deletedDTO.getActivityScore(); // 初始分数
        Long initialTimestamp = deletedDTO.getUserOfflineTimestamp(); // 初始时间戳
        Integer currentScore = ScoreLinearDecayUtil.calculateCurrentScore(initialScore, initialTimestamp); // 衰减后分数

        // 核心：衰减值 = 衰减后分数 - 初始分数（负数=实际衰减值，0=无衰减）
        Integer decayValue = currentScore - initialScore;

        // 5. 数据存在 → 执行删除操作
        Boolean deleteSuccess = userActivityScoreRedisDTOTemplate.delete(redisKey);
        if (deleteSuccess) {
            return decayValue;
        } else {
            // 极端情况：查询到数据但删除失败（如并发删除）
            log.warn("用户{} 活跃度数据查询存在，但删除失败！Redis Key：{} | 初始分数：{} | 衰减后分数：{} | 衰减调整值：{}",
                    userId, redisKey, initialScore, currentScore, decayValue);
        }

        // 6. 返回衰减的具体值（无论删除是否成功，均返回计算结果）
        return decayValue;
    }

    // 可选扩展：新增读取活跃度的方法（便于后续使用）
    public UserActivityScoreRedisDTO getUserActivityScore(Integer userId) {
        Assert.notNull(userId, "查询用户活跃度失败：用户ID为空！");
        String redisKey = buildRedisKey(userId);
        return userActivityScoreRedisDTOTemplate.opsForValue().get(redisKey);
    }

}