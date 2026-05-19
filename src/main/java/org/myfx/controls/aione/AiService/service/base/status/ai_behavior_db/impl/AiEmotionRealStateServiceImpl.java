package org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db.impl;

import cn.hutool.core.lang.Assert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.common.ai_chat_db.EmotionTypeEnum;
import org.myfx.controls.aione.AiService.dto.AiEmotionScoreOperateDTO;
import org.myfx.controls.aione.AiService.dto.redis.AiActivityScoreRedisDTO;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiEmotionRealState;
import org.myfx.controls.aione.AiService.mapper.ai_chat_db.AiEmotionRealStateMapper;
import org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db.AiEmotionRealStateService;
import org.myfx.controls.aione.AiService.utils.AiActivityScoreRedisUtil;
import org.myfx.controls.aione.ServiceCommon.utils.SnowflakeGenerator;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI情绪实时状态业务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiEmotionRealStateServiceImpl implements AiEmotionRealStateService {

    private final AiEmotionRealStateMapper aiEmotionRealStateMapper;

    private final AiActivityScoreRedisUtil aiActivityScoreRedisUtil;

    /**
     * 初始化AI感情
     * 核心逻辑：生成雪花ID + 设置默认情绪值 + 插入数据库
     */
    @Override
    public AiEmotionRealState initAiEmotion(Integer userId) {

        // 1. 参数校验
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 2. 先查是否已经存在（幂等关键）
        AiEmotionRealState exist = aiEmotionRealStateMapper
                .selectByUserId(userId);

        if (exist != null) {
            return exist;
        }

        // 3. 不存在才初始化
        AiEmotionRealState emotionState = new AiEmotionRealState();
        emotionState.setId(SnowflakeGenerator.generateId());
        emotionState.setUserId(userId);
        emotionState.setLikeValue(0);
        emotionState.setActivityValue(50);
        emotionState.setFamiliarity(0);
        emotionState.setIsValid(1);

        // 4. 插入数据库
        aiEmotionRealStateMapper.insert(emotionState);

        // 5. 新增：开始AI活跃度递增流程
        startAiActivityScoreIncrement(userId);

        return emotionState;
    }

    @Override
    public List<Integer> listHighActivityUserIds() {
        // 调用Mapper查询所有活跃度=100的用户ID
        return aiEmotionRealStateMapper.selectUserIdsByActivityValue100();
    }

    @Override
    public List<Integer> listHighLikeUserIds(Integer likeThreshold) {
        // 严格校验：阈值必须在 -100 ~ 100 之间（满足你的要求）
        if (likeThreshold == null || likeThreshold < -100 || likeThreshold > 100) {
            throw new IllegalArgumentException("喜爱度阈值必须在 -100 到 100 之间");
        }
        // 调用Mapper，传入动态阈值
        return aiEmotionRealStateMapper.selectUserIdsByLikeValueGtThreshold(likeThreshold);
    }

    /**
     * 开始AI活跃度递增（封装新增逻辑：查询真实状态→填充DTO→保存到Redis）
     * @param userId 用户ID（非空）
     */
    @Override
    public void startAiActivityScoreIncrement(Integer userId) {
        try {
            // 步骤1：查询用户AI情绪真实状态
            AiEmotionRealState aiEmotionRealState = aiEmotionRealStateMapper.selectByUserId(userId);
            if (aiEmotionRealState == null) {
                log.warn("用户{} AI情绪真实状态未查询到，活跃度默认设为50", userId);
                // 兜底：构建空状态DTO并保存
                buildAndSaveAiActivityScoreDTO(userId, 50);
                return;
            }

            // 步骤2：获取activityValue值（统一兜底为50）
            Integer activityValue = aiEmotionRealState.getActivityValue();
            if (activityValue == null) {
                log.warn("用户{} AI情绪真实状态中activityValue为空，活跃度默认设为50", userId);
                activityValue = 50;
            }
            // 校验分值范围（0-100），超出则修正
            activityValue = Math.max(0, Math.min(100, activityValue));

            // 步骤3：构建DTO并保存到Redis
            buildAndSaveAiActivityScoreDTO(userId, activityValue);

        } catch (Exception e) {
            log.error("用户{} AI活跃度递增流程执行失败", userId, e);
            // 可根据业务选择是否抛异常（这里选择不抛，避免影响主事务）
            // throw new RuntimeException("AI活跃度递增失败", e);
        }
    }

    /**
     * 构建AiActivityScoreRedisDTO并调用工具类保存
     * @param userId 用户ID
     * @param activityScore 活跃度分值
     */
    private void buildAndSaveAiActivityScoreDTO(Integer userId, Integer activityScore) {
        // 1. 构建DTO并填充核心字段
        AiActivityScoreRedisDTO scoreDTO = new AiActivityScoreRedisDTO();
        scoreDTO.setUserId(userId);
        scoreDTO.setActivityScore(activityScore);

        // 填充初始化时间戳：初始化场景下，明确设置初始化时间为当前系统毫秒时间戳
        scoreDTO.setInitializationTimestamp(System.currentTimeMillis());
        // 填充上次更新时间戳：初始化场景下，更新时间与初始化时间保持一致
        scoreDTO.setLastUpdateTimestamp(scoreDTO.getInitializationTimestamp());

        // 2. 调用Redis工具类保存
        aiActivityScoreRedisUtil.saveAiActivityScore(scoreDTO);
    }

    /**
     * 获取当前AI感情状态
     */
    @Override
    public AiEmotionRealState getCurrentAiEmotion(Integer userId) {
        // 1. 参数非空校验
        if (userId == null) {
            throw new IllegalArgumentException("获取AI感情失败：用户ID不能为空");
        }
        // 2. 调用Mapper查询
        return aiEmotionRealStateMapper.selectByUserId(userId);
    }

    /**
     * 切换AI感情有效性（关闭/打开）
     */
    @Override
    public int toggleAiEmotionValidStatus(AiEmotionRealState aiEmotionRealState) {
        // 1. 核心参数校验
        if (aiEmotionRealState == null
                || aiEmotionRealState.getUserId() == null
                || aiEmotionRealState.getIsValid() == null) {
            throw new IllegalArgumentException("切换AI感情状态失败：userId、isValid不能为空");
        }
        // 2. 调用Mapper更新is_valid
        return aiEmotionRealStateMapper.updateIsValidByUserId(aiEmotionRealState);
    }

    /**
     * 更新AI心情分数（活跃度/喜爱值/熟悉度）
     * @param operateDTO AI心情分数操作DTO
     * @return 更新前的旧分值（无记录/无变化返回null）
     */
    @Override
    public Integer updateAiEmotionScore(AiEmotionScoreOperateDTO operateDTO) {
        // 1. 基础参数校验（DTO已通过JSR380注解做基础校验，此处补充业务语义校验）
        Integer userId = operateDTO.getUserId();
        Integer aiBehaviorId = operateDTO.getAiBehaviorId(); // 保留AI行为ID，用于关联行为字典（可扩展日志/溯源）
        EmotionTypeEnum emotionType = operateDTO.getEmotionType();
        Integer addScore = operateDTO.getAddScore();

        // 1.1 核心非空/范围校验（Assert简化，异常信息更简洁）
        Assert.isTrue(userId != null && userId > 0, "更新AI心情分数失败：用户ID不能为空且需为正整数");
        Assert.isTrue(aiBehaviorId != null && aiBehaviorId > 0, "更新AI心情分数失败：AI行为ID不能为空且需为正整数");
        Assert.notNull(emotionType, "更新AI心情分数失败：情绪类型不能为空");
        Assert.isTrue(addScore != null && addScore >= -100 && addScore <= 100,
                "更新AI心情分数失败：情绪变动分值需在-100~100之间");

        // 2. 查询数据库中该用户的旧情绪分值记录（AI情绪表按userId存储，无sessionId）
        AiEmotionRealState oldEmotion = aiEmotionRealStateMapper.selectByUserId(userId);
        if (oldEmotion == null) {
            log.warn("【AI心情分数】更新用户[{}]{}分值失败：无该用户的AI情绪记录，返回null", userId, emotionType.getDesc());
            return null; // 无旧记录，返回null
        }

        // 3. 根据情绪类型计算新分值（核心逻辑：旧值+加分值，按类型做范围兜底）
        Integer oldValue;
        int rawNewValue; // 原始计算值（未兜底）
        Integer newValue;
        AiEmotionRealState updateObj = new AiEmotionRealState();
        updateObj.setUserId(userId); // 仅更新该用户的情绪分值

        // 3.1 按情绪类型匹配旧值 + 计算原始新值
        oldValue = switch (emotionType) {
            case ACTIVITY -> oldEmotion.getActivityValue() == null ? 0 : oldEmotion.getActivityValue();
            case LIKE -> oldEmotion.getLikeValue() == null ? 0 : oldEmotion.getLikeValue();
            case FAMILIAR -> oldEmotion.getFamiliarity() == null ? 0 : oldEmotion.getFamiliarity();
        };
        rawNewValue = oldValue + addScore; // 先计算原始值（未兜底）

        // 3.2 按情绪类型做范围兜底 + 新增超出范围日志
        newValue = switch (emotionType) {
            case ACTIVITY, FAMILIAR -> {
                // 活跃度/熟悉度：0~100（非负范围）
                if (rawNewValue > 100) {
                    log.info("【AI心情分数】用户[{}]{}分值超出上限100（原始计算值={}），暂存为100",
                            userId, emotionType.getDesc(), rawNewValue);
                    yield 100;
                } else if (rawNewValue < 0) {
                    log.info("【AI心情分数】用户[{}]{}分值超出下限0（原始计算值={}），暂存为0",
                            userId, emotionType.getDesc(), rawNewValue);
                    yield 0;
                } else {
                    yield rawNewValue;
                }
            }
            case LIKE -> {
                // 喜爱值：-100~100（正负范围）
                if (rawNewValue > 100) {
                    log.info("【AI心情分数】用户[{}]{}分值超出上限100（原始计算值={}），暂存为100",
                            userId, emotionType.getDesc(), rawNewValue);
                    yield 100;
                } else if (rawNewValue < -100) {
                    log.info("【AI心情分数】用户[{}]{}分值超出下限-100（原始计算值={}），暂存为-100",
                            userId, emotionType.getDesc(), rawNewValue);
                    yield -100;
                } else {
                    yield rawNewValue;
                }
            }
        };

        // 3.3 设置更新字段（仅更新当前情绪类型的分值）
        switch (emotionType) {
            case ACTIVITY -> updateObj.setActivityValue(newValue);
            case LIKE -> updateObj.setLikeValue(newValue);
            case FAMILIAR -> updateObj.setFamiliarity(newValue);
        }

        // 4. 校验分值是否有变化（无变化则返回null，避免无意义更新）
        if (newValue.equals(oldValue)) {
            log.info("【AI心情分数】用户[{}]{}分值无变化（旧值={}+加分值={}=新值={}），返回null",
                    userId, emotionType.getDesc(), oldValue, addScore, newValue);
            return null;
        }

        // 5. 执行动态更新（仅更新选中的情绪字段）
        int affectedRows = aiEmotionRealStateMapper.updateEmotionByUserId(updateObj);
        // 校验影响行数（理论上userId唯一，影响行数只能是1）
        if (affectedRows != 1) {
            log.warn("【AI心情分数】更新用户[{}]{}分值异常：预期影响1行，实际影响{}行",
                    userId, emotionType.getDesc(), affectedRows);
        }

        // 6. 日志输出完整更新链路（含AI行为ID溯源）
        log.info("【AI心情分数】更新用户[{}]{}分值完成（关联AI行为ID={}）：旧值={} + 加分值={} = 原始计算值={} → 兜底后新值={}，返回修改前分数={}",
                userId, emotionType.getDesc(), aiBehaviorId, oldValue, addScore, rawNewValue, newValue, oldValue);
        return oldValue; // 更新成功，返回修改前的分数
    }

    /**
     * 实现：补满指定用户的某类AI情绪分值（将该情绪分值置为100）
     */
    @Override
    public AiEmotionScoreOperateDTO fillFullAiEmotionScore(Integer userId, EmotionTypeEnum emotionType) {
        // 1. 基础参数校验（Assert简化，替代if判断）
        Assert.notNull(userId, "用户ID不能为空");
        Assert.isTrue(userId > 0, "用户ID需为正整数，当前值：{}", userId);
        Assert.notNull(emotionType, "情绪类型不能为空");

        // 2. 初始化返回的DTO对象（移除aiBehaviorId的设置，无需该字段）
        AiEmotionScoreOperateDTO operateDTO = new AiEmotionScoreOperateDTO();
        operateDTO.setUserId(userId);
        operateDTO.setEmotionType(emotionType);

        // 3. 查询数据库中该用户的AI情绪旧分值记录（仅按userId查询，无emotionType入参）
        AiEmotionRealState oldScore = aiEmotionRealStateMapper.selectByUserId(userId);

        // 4. 提取该情绪类型的旧分值（null按0处理，完全模仿featureType的switch写法）
        Integer oldValue = switch (emotionType) {
            case ACTIVITY -> oldScore == null ? 0 : (oldScore.getActivityValue() == null ? 0 : oldScore.getActivityValue());
            case LIKE -> oldScore == null ? 0 : (oldScore.getLikeValue() == null ? 0 : oldScore.getLikeValue());
            case FAMILIAR -> oldScore == null ? 0 : (oldScore.getFamiliarity() == null ? 0 : oldScore.getFamiliarity());
        };

        // 5. 设置DTO的scoreBefore（补满前的原始分值）
        operateDTO.setScoreBefore(oldValue);

        // 6. 计算补满所需的加分值（100 - 旧分值 → 旧值+加分值=100）
        Integer addScore = 100 - oldValue;
        operateDTO.setAddScore(addScore);

        // 7. 日志打印补满准备信息
        if (addScore == 0) {
            log.info("【AI情绪分值】用户[{}]{}分值无需补满：当前值已为100（旧值={}）",
                    userId, emotionType.getDesc(), oldValue);
            return operateDTO;
        }
        log.info("【AI情绪分值】准备补满用户[{}]{}分值：旧值={}，需加分值={}（100 - {}）",
                userId, emotionType.getDesc(), oldValue, addScore, oldValue);

        // 8. 调用更新方法完成补满（替换为实际的更新方法）
        Integer updateResult = updateAiEmotionScore(operateDTO);

        // 9. 日志补充补满结果
        if (updateResult != null) {
            log.info("【AI情绪分值】补满用户[{}]{}分值成功：旧值={} → 新值=100",
                    userId, emotionType.getDesc(), oldValue);
        } else {
            log.info("【AI情绪分值】补满用户[{}]{}分值无变化：旧值已为100（旧值={}）",
                    userId, emotionType.getDesc(), oldValue);
        }

        // 10. 返回包含完整操作信息的DTO
        return operateDTO;
    }

    /**
     * 实现：补满指定用户的「AI活跃度分值」（固定情绪类型为ACTIVITY）
     */
    @Override
    public AiEmotionScoreOperateDTO fillFullAiActivityScore(Integer userId) {
        // 直接调用主方法，固定传入活跃度情绪类型枚举，无需重复校验参数（主方法已做完整校验）
        return fillFullAiEmotionScore(userId, EmotionTypeEnum.ACTIVITY);
    }
}