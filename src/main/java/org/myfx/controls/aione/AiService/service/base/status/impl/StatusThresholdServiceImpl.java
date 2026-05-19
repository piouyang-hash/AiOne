package org.myfx.controls.aione.AiService.service.base.status.impl;

import jakarta.annotation.Resource;
import org.myfx.controls.aione.AiService.service.base.status.StatusThresholdService;
import org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db.AiEmotionRealStateService;
import org.myfx.controls.aione.AiService.service.base.status.user_behavior_db.UserFeatureScoreService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 状态阈值业务实现类
 * Status Threshold Service Impl
 */
@Service
public class StatusThresholdServiceImpl implements StatusThresholdService {

    @Resource
    private AiEmotionRealStateService aiEmotionRealStateService;
    @Resource
    private UserFeatureScoreService userFeatureScoreService;

    @Override
    public List<Integer> listTriggerEventEndActiveMsgUserIdsByActivity() {
        // 1. 获取两个业务层的用户ID列表（均为Integer类型）
        List<Integer> highActivityUserIds = aiEmotionRealStateService.listHighActivityUserIds();
        List<Integer> lowActivityUserIds = userFeatureScoreService.listLowActivityUserIds();

        // 2. 计算两个列表的交集（用Set提升交集计算效率）
        return getIntersection(highActivityUserIds, lowActivityUserIds);
    }

    @Override
    public List<Integer> listTriggerEventEndActiveMsgUserIdsByLike() {
        // 调用：传入固定阈值 5（喜爱度 >5）
        return aiEmotionRealStateService.listHighLikeUserIds(5);
    }


    // ====================== 【新增】统一主动消息触发方法 ======================
    @Override
    public List<Integer> listTriggerActiveMsgUserIds() {
        // 1. 获取两个条件的用户列表
        List<Integer> activityUserIds = listTriggerEventEndActiveMsgUserIdsByActivity();
        List<Integer> likeUserIds = listTriggerEventEndActiveMsgUserIdsByLike();

        // 2. 取并集（满足任一条件即可）+ 自动去重
        return getUnion(activityUserIds, likeUserIds);
    }

    // ====================== 工具方法：计算两个列表的并集（去重） ======================
    /**
     * 获取两个列表的并集（去重）
     */
    private List<Integer> getUnion(List<Integer> list1, List<Integer> list2) {
        // 【优化】和交集保持一致，做空值防护
        if (list1 == null && list2 == null) {
            return new ArrayList<>();
        }

        Set<Integer> unionSet = new HashSet<>();
        // 非空才添加
        if (list1 != null) unionSet.addAll(list1);
        if (list2 != null) unionSet.addAll(list2);

        return new ArrayList<>(unionSet);
    }

    /**
     * 私有工具方法：计算两个Integer列表的交集（避免空指针，高效计算）
     * @param list1 列表1
     * @param list2 列表2
     * @return 交集列表（无交集返回空列表）
     */
    private List<Integer> getIntersection(List<Integer> list1, List<Integer> list2) {
        // 处理空列表，避免空指针
        if (list1 == null || list1.isEmpty() || list2 == null || list2.isEmpty()) {
            return new ArrayList<>();
        }

        // 用HashSet存储list1，O(1)查询效率，提升交集计算速度
        Set<Integer> set = new HashSet<>(list1);
        List<Integer> intersection = new ArrayList<>();
        for (Integer userId : list2) {
            if (set.contains(userId)) {
                intersection.add(userId);
                // 移除已匹配的元素，避免重复（如果列表有重复ID）
                set.remove(userId);
            }
        }
        return intersection;
    }
}