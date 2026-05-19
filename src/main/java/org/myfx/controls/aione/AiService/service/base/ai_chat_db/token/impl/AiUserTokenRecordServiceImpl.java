package org.myfx.controls.aione.AiService.service.base.ai_chat_db.token.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.common.ai_chat_db.TokenChangeTypeEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.TokenChangeWayEnum;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.token.AiUserTokenRecord;
import org.myfx.controls.aione.AiService.mapper.ai_chat_db.token.AiUserTokenRecordMapper;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.token.AiUserTokenRecordService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * AI用户Token变动流水 业务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiUserTokenRecordServiceImpl implements AiUserTokenRecordService {

    private final AiUserTokenRecordMapper aiUserTokenRecordMapper;

    @Override
    public int addAiUserTokenRecord(AiUserTokenRecord record) {
        Assert.notNull(record, "Token流水实体不能为空");
        Assert.notNull(record.getUserId(), "用户ID不能为空");
        Assert.notNull(record.getChangeType(), "变动类型不能为空");
        Assert.notNull(record.getChangeWay(), "变动方式不能为空");
        return aiUserTokenRecordMapper.insert(record);
    }

    @Override
    public List<AiUserTokenRecord> getAiUserTokenRecordListByUserId(Integer userId) {
        Assert.notNull(userId, "用户ID不能为空");
        return aiUserTokenRecordMapper.selectByUserId(userId);
    }

    @Override
    public List<AiUserTokenRecord> getAiUserTokenRecordListByUserIdAndType(Integer userId, TokenChangeTypeEnum changeType) {
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(changeType, "变动类型不能为空");
        return aiUserTokenRecordMapper.selectByUserIdAndType(userId, changeType);
    }

    @Override
    public List<AiUserTokenRecord> getAiUserTokenRecordListByUserIdAndWay(Integer userId, TokenChangeWayEnum changeWay) {
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(changeWay, "变动方式不能为空");
        return aiUserTokenRecordMapper.selectByUserIdAndWay(userId, changeWay);
    }

    @Override
    public List<AiUserTokenRecord> getAiUserTokenRecordListByUserIdAndTypeAndWay(Integer userId, TokenChangeTypeEnum changeType, TokenChangeWayEnum changeWay) {
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(changeType, "变动类型不能为空");
        Assert.notNull(changeWay, "变动方式不能为空");
        return aiUserTokenRecordMapper.selectByUserIdAndTypeAndWay(userId, changeType, changeWay);
    }

    @Override
    public int removeAiUserTokenRecordByUserId(Integer userId) {
        Assert.notNull(userId, "用户ID不能为空");
        return aiUserTokenRecordMapper.deleteByUserId(userId);
    }
}
