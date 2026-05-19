package org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db.upper.impl;

import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiEmotionRealState;
import org.myfx.controls.aione.AiService.entity.user_behavior_db.UserFeatureScore;
import org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db.AiEmotionRealStateService;
import org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db.upper.StatusService;
import org.myfx.controls.aione.AiService.service.base.status.user_behavior_db.UserFeatureScoreService;
import org.myfx.controls.aione.AiService.vo.StatusVO;
import org.myfx.controls.aione.ServiceCommon.context.UserContext;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
// Lombok 构造器注入（替代@Autowired，你要求的注解）
@RequiredArgsConstructor
public class StatusServiceImpl implements StatusService {

    // 注入依赖
    private final AiEmotionRealStateService aiEmotionRealStateService;
    private final UserFeatureScoreService userFeatureScoreService;

    @Override
    public StatusVO getCurrentStatus() {
        // 1. 获取当前登录用户ID
        Integer userId = UserContext.getUserId();

        // 2. 调用两个Service获取数据
        AiEmotionRealState aiEmotion = aiEmotionRealStateService.getCurrentAiEmotion(userId);
        UserFeatureScore userFeature = userFeatureScoreService.getUserFeatureScoreByUserId(userId);

        // 3. 创建VO对象
        StatusVO statusVO = new StatusVO();
        statusVO.setUserId(userId);

        // 4. Spring自带工具：第一次复制 → AI情感属性（likeValue、familiarity、activityValue）
        BeanUtils.copyProperties(aiEmotion, statusVO);

        // 5. Spring自带工具：第二次复制 → 用户特征属性（activityScore、favorScore、familiarScore）
        BeanUtils.copyProperties(userFeature, statusVO);

        // 6. 返回最终VO
        return statusVO;
    }
}