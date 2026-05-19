package org.myfx.controls.aione.AiService.service.base.ai_chat_db.token.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.token.AiTokenType;
import org.myfx.controls.aione.AiService.mapper.ai_chat_db.token.AiTokenTypeMapper;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.token.AiTokenTypeService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * AI Token类型配置 业务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiTokenTypeServiceImpl implements AiTokenTypeService {

    private final AiTokenTypeMapper aiTokenTypeMapper;

    @Override
    public int addAiTokenType(AiTokenType tokenType) {
        // 参数非空校验
        Assert.notNull(tokenType, "Token类型实体不能为空");
        Assert.notNull(tokenType.getVendor(), "厂商编码不能为空");
        Assert.notNull(tokenType.getTokenSide(), "Token方向不能为空");
        Assert.notNull(tokenType.getTypeCode(), "类型编码不能为空");
        Assert.notNull(tokenType.getTypeName(), "类型名称不能为空");
        return aiTokenTypeMapper.insert(tokenType);
    }

    @Override
    public AiTokenType getAiTokenTypeById(Long typeId) {
        // 参数非空校验
        Assert.notNull(typeId, "Token类型ID不能为空");
        return aiTokenTypeMapper.selectByTypeId(typeId);
    }

    // ====================== 实现1：根据code查询 ======================
    @Override
    public AiTokenType getAiTokenTypeByTypeCode(String typeCode) {
        Assert.notNull(typeCode, "Token类型编码不能为空");
        return aiTokenTypeMapper.selectByTypeCode(typeCode);
    }

    // ====================== 实现2：获取默认DeepSeek类型列表 ======================
    @Override
    public List<AiTokenType> getDefaultAiTokenTypeList() {
        List<AiTokenType> defaultList = new ArrayList<>();
        // 查询默认输入类型
        AiTokenType inputType = aiTokenTypeMapper.selectByTypeCode("DEEPSEEK_INPUT");
        // 查询默认输出类型
        AiTokenType outputType = aiTokenTypeMapper.selectByTypeCode("DEEPSEEK_OUTPUT");

        // 非空校验，避免返回null
        Assert.notNull(inputType, "默认Token类型DEEPSEEK_INPUT不存在，请初始化数据");
        Assert.notNull(outputType, "默认Token类型DEEPSEEK_OUTPUT不存在，请初始化数据");

        defaultList.add(inputType);
        defaultList.add(outputType);
        return defaultList;
    }

    @Override
    public int updateAiTokenType(AiTokenType tokenType) {
        // 参数非空校验
        Assert.notNull(tokenType, "Token类型实体不能为空");
        Assert.notNull(tokenType.getTypeId(), "Token类型ID不能为空");
        return aiTokenTypeMapper.updateByTypeId(tokenType);
    }

    @Override
    public int removeAiTokenTypeById(Long typeId) {
        // 参数非空校验
        Assert.notNull(typeId, "Token类型ID不能为空");
        return aiTokenTypeMapper.deleteByTypeId(typeId);
    }
}