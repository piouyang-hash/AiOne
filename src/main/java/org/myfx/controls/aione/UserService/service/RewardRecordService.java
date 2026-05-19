package org.myfx.controls.aione.UserService.service;

/**
 * 打赏记录 Service 接口
 */
public interface RewardRecordService {

    /**
     * 新增打赏记录
     * @param amount 打赏金额（5、10、15 档次）
     * @param message 打赏留言（可为空）
     * @return 新增是否成功（true 成功，false 失败）
     */
    boolean addRewardRecord(Integer amount, String message);
}
