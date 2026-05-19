package org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.common.ai_chat_db.EmotionTypeEnum;
import org.myfx.controls.aione.AiService.dto.AiEmotionScoreOperateDTO;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiEmotionLogDetail;
import org.myfx.controls.aione.AiService.mapper.ai_chat_db.AiEmotionLogDetailMapper;
import org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db.AiEmotionLogDetailService;
import org.myfx.controls.aione.ServiceCommon.utils.SnowflakeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;

/**
 * AI情绪变动流水表 业务实现类
 * 核心：非空校验+Mapper调用，符合流水记录“只增/查/删，不更新”的业务规则
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiEmotionLogDetailServiceImpl implements AiEmotionLogDetailService {

    // 注入Mapper接口
    private final AiEmotionLogDetailMapper emotionLogDetailMapper;

    /**
     * 新增AI情绪变动日志明细（仿照用户行为积分明细写法）
     * @param operateDTO AI心情分数操作DTO（含日志新增核心参数）
     * @return 新增影响行数（正常返回1）
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务兜底，异常回滚
    public int addEmotionLogDetail(AiEmotionScoreOperateDTO operateDTO) {
        // 1. 参数校验（补充日志新增特有的必填项校验，Assert兜底）
        Integer userId = operateDTO.getUserId();
        Integer aiBehaviorId = operateDTO.getAiBehaviorId();
        EmotionTypeEnum emotionType = operateDTO.getEmotionType();
        Integer addScore = operateDTO.getAddScore();
        Integer scoreBefore = operateDTO.getScoreBefore();

        // 1.1 基础非空/合法性校验（DTO注解校验+Assert兜底，避免注解失效）
        Assert.notNull(userId, "参数不合法：用户ID（userId）不能为空");
        Assert.isTrue(userId > 0, "参数不合法：用户ID（userId）需为正整数（Integer类型）");
        Assert.notNull(aiBehaviorId, "参数不合法：AI行为ID（aiBehaviorId）不能为空");
        Assert.isTrue(aiBehaviorId > 0, "参数不合法：AI行为ID（aiBehaviorId）需为正整数（Integer类型）");
        Assert.notNull(emotionType, "参数不合法：情绪类型（emotionType）不能为空");
        Assert.notNull(addScore, "参数不合法：情绪变动分值（addScore）不能为空");
        Assert.isTrue(addScore >= -100 && addScore <= 100, "参数不合法：情绪变动分值需在-100~100之间");

        // 1.2 日志新增特有校验：变动前分值必填
        Assert.notNull(scoreBefore, "参数不合法：变动前分值（scoreBefore）不能为空");
        Assert.isTrue(scoreBefore >= -100 && scoreBefore <= 100, "参数不合法：变动前分值需在-100~100之间");

        // 2. 计算变动后分值（按情绪类型做范围兜底，与AI心情分数更新逻辑一致）
        int valueAfter = scoreBefore + addScore;
        valueAfter = switch (emotionType) {
            // 活跃度/熟悉度：0~100（非负范围）
            case ACTIVITY, FAMILIAR -> Math.max(0, Math.min(100, valueAfter));
            // 喜爱值：-100~100（正负范围）
            case LIKE -> Math.max(-100, Math.min(100, valueAfter));
        };

        // 3. 组装日志明细对象（严格匹配AiEmotionLogDetail字段，无sessionId）
        AiEmotionLogDetail logDetail = new AiEmotionLogDetail();
        logDetail.setId(SnowflakeGenerator.generateId()); // 生成雪花ID（主键，需确保工具类可用）
        logDetail.setUserId(userId);
        logDetail.setBehaviorId(aiBehaviorId);
        logDetail.setEmotionType(emotionType);
        logDetail.setChangeValue(addScore); // 变动分值=DTO中的addScore
        logDetail.setValueAfter(valueAfter); // 变动后分值（兜底后）
        // 变动原因：拼接默认描述（可根据业务扩展为自定义）
        logDetail.setChangeReason(String.format("用户[%d]触发AI行为[%d]，%s变动%d分",
                userId, aiBehaviorId, emotionType.getDesc(), addScore));
        logDetail.setBehaviorTime(System.currentTimeMillis()); // 行为时间戳（毫秒）

        // 4. 执行插入操作
        int affectedRows = emotionLogDetailMapper.insert(logDetail);

        // 5. 校验插入结果（确保仅新增1条）
        if (affectedRows != 1) {
            log.error("新增AI情绪变动日志明细失败，用户ID：{}，AI行为ID：{}，预期影响1行，实际影响{}行",
                    userId, aiBehaviorId, affectedRows);
            throw new RuntimeException("新增AI情绪变动日志明细失败");
        }
        return affectedRows;
    }

    /**
     * 删除日志：校验ID非空，调用Mapper物理删除
     */
    @Override
    public int removeEmotionLogDetailById(Long id) {
        // 非空校验：主键ID不能为空
        Assert.notNull(id, "情绪变动日志ID不能为空");
        // 调用Mapper删除
        return emotionLogDetailMapper.deleteById(id);
    }

    /**
     * 根据ID查询：校验ID非空，调用Mapper查询
     */
    @Override
    public AiEmotionLogDetail getEmotionLogDetailById(Long id) {
        // 非空校验：主键ID不能为空
        Assert.notNull(id, "情绪变动日志ID不能为空");
        // 调用Mapper查询
        return emotionLogDetailMapper.selectById(id);
    }

    /**
     * 条件查询：允许传null（查全部），调用Mapper条件查询
     */
    @Override
    public List<AiEmotionLogDetail> listEmotionLogDetailByCondition(AiEmotionLogDetail logDetail) {
        // 传null时默认查全部，避免Mapper返回null（兜底返回空列表）
        List<AiEmotionLogDetail> result = emotionLogDetailMapper.selectList(logDetail);
        return result == null ? Collections.emptyList() : result;
    }

    /**
     * 按用户+情绪类型查询：双非空校验，调用Mapper查询
     */
    @Override
    public List<AiEmotionLogDetail> listEmotionLogDetailByUserIdAndEmotionType(Integer userId, EmotionTypeEnum emotionType) {
        // 非空校验：用户ID不能为空
        Assert.notNull(userId, "用户ID不能为空");
        // 非空校验：情绪类型不能为空
        Assert.notNull(emotionType, "情绪类型不能为空");
        // 调用Mapper查询，兜底返回空列表
        List<AiEmotionLogDetail> result = emotionLogDetailMapper.selectByUserIdAndEmotionType(userId, emotionType);
        return result == null ? Collections.emptyList() : result;
    }

    /**
     * 实现：查询最新1条指定类型的情感日志
     */
    @Override
    public AiEmotionLogDetail getLatestEmotionLog(Integer userId, EmotionTypeEnum emotionType) {
        // 1. 非空校验（用户ID+情感类型均不能为空）
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(emotionType, "情感类型不能为空");

        // 2. 查询最新1条指定类型的情感记录（limitNum=1）
        List<AiEmotionLogDetail> logList = emotionLogDetailMapper.selectLatestNByUserIdAndEmotionType(
                userId,
                emotionType,
                1 // 核心修改：仅查询最新1条
        );

        // 3. 按规则返回结果：有记录返回第一条，无则返回null
        if (logList == null || logList.isEmpty()) {
            return null;
        }
        return logList.get(0);
    }

    /**
     * 实现：查询最新1条活跃度记录（调用通用方法，指定情感类型为ACTIVITY）
     */
    @Override
    public AiEmotionLogDetail getLatestActivityLog(Integer userId) {
        // 调用通用的最新情感日志查询方法，固定传入活跃度类型
        return this.getLatestEmotionLog(userId, EmotionTypeEnum.ACTIVITY);
    }
}