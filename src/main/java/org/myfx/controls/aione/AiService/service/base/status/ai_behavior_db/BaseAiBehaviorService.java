package org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db;

import org.myfx.controls.aione.AiService.common.ai_chat_db.AiBehaviorEnum;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.BaseAiBehavior;

import java.util.List;

/**
 * 基础AI行为字典业务接口
 * 适配最新表结构：仅存储行为编码+行为名称，分值独立至ai_behavior_score表
 */
public interface BaseAiBehaviorService {

    // ========== 核心方法：新增/更新 行为名称 ==========
    /**
     * 新增AI行为（指定编码+名称）
     * @param aiBehaviorCode AI行为编码（如：CHAT、LIKE、WAIT）
     * @param behaviorName AI行为名称（如：用户聊天互动、用户点赞）
     * @return 是否新增成功（true=成功，false=失败）
     */
    boolean addAiBehavior(AiBehaviorEnum aiBehaviorCode, String behaviorName);

    /**
     * 根据AI行为编码更新行为名称
     * @param aiBehaviorCode AI行为编码
     * @param behaviorName 新的行为名称
     * @return 是否更新成功（true=成功，false=无匹配记录）
     */
    boolean updateAiBehaviorNameByCode(AiBehaviorEnum aiBehaviorCode, String behaviorName);

    // ========== 查询方法（保留，无修改） ==========
    /**
     * 根据AI行为ID查询行为信息（唯一ID）
     * @param aiBehaviorId AI行为ID（正整数）
     * @return AI行为信息（null=无匹配数据）
     */
    BaseAiBehavior getAiBehaviorById(Integer aiBehaviorId);

    /**
     * 根据AI行为编码查询行为信息（唯一编码）
     * @param aiBehaviorCode AI行为编码
     * @return AI行为信息（null=无匹配数据）
     */
    BaseAiBehavior getAiBehaviorByCode(AiBehaviorEnum aiBehaviorCode);

    /**
     * 查询所有AI行为列表
     * @return 所有AI行为列表（无数据返回空列表）
     */
    List<BaseAiBehavior> listAllAiBehaviors();

    // ========== 更新/删除 方法（保留，适配新结构） ==========
    /**
     * 根据AI行为ID更新行为信息（有值则更）
     * @param aiBehavior AI行为实体（至少包含aiBehaviorId，可选aiBehaviorCode/behaviorName）
     * @return 是否更新成功（true=成功，false=无匹配记录）
     */
    boolean updateAiBehaviorById(BaseAiBehavior aiBehavior);

    /**
     * 根据AI行为编码删除行为
     * @param aiBehaviorCode AI行为编码
     * @return 是否删除成功（true=成功，false=无匹配记录）
     */
    boolean deleteAiBehaviorByCode(AiBehaviorEnum aiBehaviorCode);

    /**
     * 根据AI行为ID删除行为
     * @param aiBehaviorId AI行为ID
     * @return 是否删除成功（true=成功，false=无匹配记录）
     */
    boolean deleteAiBehaviorById(Integer aiBehaviorId);
}