package org.myfx.controls.aione.UserService.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.myfx.controls.aione.UserService.model.entity.RewardRecord;

/**
 * 打赏记录 Mapper 接口
 */
@Mapper // 标记为 MyBatis 映射接口，由 Spring 自动扫描
public interface RewardRecordMapper {

    /**
     * 新增打赏记录
     * @param rewardRecord 打赏记录实体
     * @return 影响行数（1 表示成功，0 表示失败）
     */
    int insertRewardRecord(RewardRecord rewardRecord);
}