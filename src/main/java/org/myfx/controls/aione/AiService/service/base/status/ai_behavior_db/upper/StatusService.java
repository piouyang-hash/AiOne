package org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db.upper;

import org.myfx.controls.aione.AiService.vo.StatusVO;

/**
 * 状态查询服务接口
 */
public interface StatusService {

    /**
     * 查询当前状态（无参数）
     * @return 状态VO
     */
    StatusVO getCurrentStatus();
}