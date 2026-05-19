package org.myfx.controls.aione.AiService.service.base.ai_chat_db.token.impl;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.token.AiUserPointRecord;
import org.myfx.controls.aione.AiService.mapper.ai_chat_db.token.AiUserPointRecordMapper;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.token.AiUserPointRecordService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * AI用户算力积分流水 业务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiUserPointRecordServiceImpl implements AiUserPointRecordService {

    @Resource
    private AiUserPointRecordMapper aiUserPointRecordMapper;

    @Override
    public int addAiUserPointRecord(AiUserPointRecord record) {
        Assert.notNull(record, "积分流水实体不能为空");
        Assert.notNull(record.getUserId(), "用户ID不能为空");
        Assert.notNull(record.getRecordId(), "流水ID不能为空");
        return aiUserPointRecordMapper.insert(record);
    }

    @Override
    public List<AiUserPointRecord> getByUserId(Integer userId) {
        Assert.notNull(userId, "用户ID不能为空");
        return aiUserPointRecordMapper.selectByUserId(userId);
    }

    @Override
    public List<AiUserPointRecord> getByUserIdAndType(Integer userId, Integer changeType) {
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(changeType, "变动类型不能为空");
        return aiUserPointRecordMapper.selectByUserIdAndType(userId, changeType);
    }

    @Override
    public List<AiUserPointRecord> getByUserIdAndWay(Integer userId, String changeWay) {
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(changeWay, "变动方式不能为空");
        return aiUserPointRecordMapper.selectByUserIdAndWay(userId, changeWay);
    }

    @Override
    public List<AiUserPointRecord> getByUserIdAndTypeAndWay(Integer userId, Integer changeType, String changeWay) {
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(changeType, "变动类型不能为空");
        Assert.notNull(changeWay, "变动方式不能为空");
        return aiUserPointRecordMapper.selectByUserIdAndTypeAndWay(userId, changeType, changeWay);
    }

    @Override
    public int deleteByUserId(Integer userId) {
        Assert.notNull(userId, "用户ID不能为空");
        return aiUserPointRecordMapper.deleteByUserId(userId);
    }
}