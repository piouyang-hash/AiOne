package org.myfx.controls.aione.AiService.service.base.ai_chat_db.impl;

import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.AiService.common.ai_chat_db.AiPersonalityEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.AiPersonalityStrengthEnum;
import org.myfx.controls.aione.AiService.dto.AddAiPersonalityConfigDTO;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiPersonalityConfig;
import org.myfx.controls.aione.AiService.event.AiChatSessionCreatedEvent;
import org.myfx.controls.aione.AiService.mapper.ai_chat_db.AiPersonalityConfigMapper;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiPersonalityConfigService;
import org.myfx.controls.aione.ServiceCommon.utils.SnowflakeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Collections;
import java.util.List;

/**
 * AI基本性格配置业务层实现类
 */
@Service
@RequiredArgsConstructor
public class AiPersonalityConfigServiceImpl implements AiPersonalityConfigService {

    private final AiPersonalityConfigMapper aiPersonalityConfigMapper;

    @Override
    public Long addAiPersonalityConfig(AddAiPersonalityConfigDTO dto) {
        // 1. 幂等性检查：先检查是否已存在相同配置
        AiPersonalityConfig existingConfig = aiPersonalityConfigMapper.selectPriorityConfig(dto.getUserId());
        if (existingConfig != null) {
            // 已存在，直接返回已有配置的ID
            return existingConfig.getId();
        }

        // 2. DTO转换为实体类
        AiPersonalityConfig config = new AiPersonalityConfig();

        // 3. 自动生成雪花ID（主键）
        Long snowflakeId = SnowflakeGenerator.generateId();
        config.setId(snowflakeId);

        // 4. 填充DTO传入的参数
        config.setUserId(dto.getUserId()); // Integer类型用户ID
        config.setAiPersonalityCode(dto.getAiPersonalityCode());

        // 5. 填充默认值：强度默认1级、是否有效默认1
        config.setPersonalityStrengthCode(AiPersonalityStrengthEnum.LEVEL_1); // 等级默认1级
        config.setIsValid(1); // 有效标识默认1

        // 6. 调用Mapper插入（时间由数据库自动管理）
        aiPersonalityConfigMapper.insertAiPersonalityConfig(config);

        // 7. 返回生成的主键ID
        return snowflakeId;
    }

    // ====================== 事务监听器（监听事件，初始化AI性格） ======================
    /**
     * 监听会话创建事件（并入主事务，事务提交前执行）
     * TransactionalEventListener 保证和createUserChatSession的事务绑定
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleChatSessionCreatedEvent(AiChatSessionCreatedEvent event) {
        // 1. 获取事件中的参数
        Integer userId = event.getUserId();

        // 2. 构建新增性格配置DTO（默认性格=中性，code=1）
        AddAiPersonalityConfigDTO dto = new AddAiPersonalityConfigDTO();
        dto.setUserId(userId); // 绑定当前用户ID
        dto.setAiPersonalityCode(AiPersonalityEnum.NEUTRAL); // 默认性格：中性（code=1）

        // 3. 调用新增性格配置方法，初始化AI性格
        addAiPersonalityConfig(dto);
    }

    @Override
    public AiPersonalityConfig getCurrentStrongestPersonality(Integer userId) {
        AiPersonalityConfig strongestPersonality = null;

        // 查询【用户-会话专属】配置（最高优先级）
        if (userId != null) {
            strongestPersonality = aiPersonalityConfigMapper.selectPriorityConfig(userId);
        }

        return strongestPersonality;
    }

    @Override
    public List<AiPersonalityConfig> listAiPersonalityConfigByUserId(Integer userId) {
        // 代码层空值校验（SQL不处理）
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 查询结果：无数据返回空List，避免NPE
        List<AiPersonalityConfig> configList = aiPersonalityConfigMapper.selectAiPersonalityConfigByUserId(userId);
        return configList == null ? Collections.emptyList() : configList;
    }

    // ====================== 1. 批量：按userId+sessionId关闭/打开AI性格 ======================
    @Override
    public int toggleAiPersonalityValidByUser(Integer userId, Integer isValid) {
        // 基础参数校验
        if (userId == null) {
            throw new IllegalArgumentException("参数不合法：userId不能为空");
        }
        if (isValid == null || (isValid != 0 && isValid != 1)) {
            throw new IllegalArgumentException("参数不合法：isValid只能为0（关闭）或1（打开）");
        }
        // 最小实现：调用mapper
        return aiPersonalityConfigMapper.switchValidByUserId(userId, isValid);
    }

    // ====================== 2. 精准更新：按id+userId更新某一个AI性格 ======================
    @Override
    public int updateAiPersonalityByIdUser(Long id, Integer userId, AiPersonalityEnum aiPersonalityCode, AiPersonalityStrengthEnum personalityStrengthCode) {
        // 基础参数校验（必填项）
        if (id == null) {
            throw new IllegalArgumentException("参数不合法：性格配置ID不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("参数不合法：userId不能为空");
        }
        // 非必填项：至少更新一个字段（性格/强度）
        if (aiPersonalityCode == null && personalityStrengthCode == null) {
            throw new IllegalArgumentException("参数不合法：至少要更新aiPersonalityCode或personalityStrengthCode其中一个");
        }

        // 最小实现：封装实体，调用mapper
        AiPersonalityConfig config = new AiPersonalityConfig();
        config.setId(id);
        config.setUserId(userId);
        config.setAiPersonalityCode(aiPersonalityCode); // 传null则不更新（mapper里的if判断会过滤）
        config.setPersonalityStrengthCode(personalityStrengthCode); // 传null则不更新
        return aiPersonalityConfigMapper.updateByIdAndUserId(config);
    }

    // ====================== 3. 精准：按id关闭/打开某一个AI性格 ======================
    @Override
    public int toggleAiPersonalityValidById(Long id, Integer isValid) {
        // 基础参数校验
        if (id == null) {
            throw new IllegalArgumentException("参数不合法：性格配置ID不能为空");
        }
        if (isValid == null || (isValid != 0 && isValid != 1)) {
            throw new IllegalArgumentException("参数不合法：isValid只能为0（关闭）或1（打开）");
        }
        // 最小实现：调用mapper
        return aiPersonalityConfigMapper.switchValidById(id, isValid);
    }
}