package org.myfx.controls.aione.AiService.service.base.ai_chat_db.impl;

import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.AiService.common.ai_chat_db.AiMoodEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.AiMoodStrengthEnum;
import org.myfx.controls.aione.AiService.dto.AddAiMoodConfigDTO;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiMoodConfig;
import org.myfx.controls.aione.AiService.event.AiChatSessionCreatedEvent;
import org.myfx.controls.aione.AiService.mapper.ai_chat_db.AiMoodConfigMapper;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiMoodConfigService;
import org.myfx.controls.aione.ServiceCommon.utils.SnowflakeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Collections;
import java.util.List;

/**
 * AI心情配置业务层实现类
 */
@Service
@RequiredArgsConstructor
public class AiMoodConfigServiceImpl implements AiMoodConfigService {

    private final AiMoodConfigMapper aiMoodConfigMapper;

    @Override
    public Long addAiMoodConfig(AddAiMoodConfigDTO dto) {
        // 1. 幂等性检查：先检查是否已存在相同配置
        AiMoodConfig existingConfig = aiMoodConfigMapper.selectPriorityConfig(dto.getUserId());
        if (existingConfig != null) {
            // 已存在，直接返回已有配置的ID
            return existingConfig.getId();
        }

        // 2. DTO转换为实体类
        AiMoodConfig config = new AiMoodConfig();

        // 3. 自动生成雪花ID（主键）
        Long snowflakeId = SnowflakeGenerator.generateId();
        config.setId(snowflakeId);

        // 4. 填充DTO传入的参数
        config.setUserId(dto.getUserId()); // Integer类型用户ID
        config.setAiMoodCode(dto.getAiMoodCode());

        // 5. 填充默认值：强度默认1级、是否有效默认1
        config.setAiStrengthCode(AiMoodStrengthEnum.LEVEL_1); // 等级默认1级
        config.setIsValid(1); // 有效标识默认1

        // 6. 调用Mapper插入（时间由数据库自动管理）
        aiMoodConfigMapper.insertAiMoodConfig(config);

        // 7. 返回生成的主键ID
        return snowflakeId;
    }

    // ====================== 事务监听器（监听事件，初始化AI心情） ======================
    /**
     * 监听会话创建事件（并入主事务，事务提交后执行）
     * TransactionalEventListener 保证和createUserChatSession的事务绑定
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleChatSessionCreatedEvent(AiChatSessionCreatedEvent event) {
        // 1. 获取事件中的参数
        Integer userId = event.getUserId();

        // 2. 构建新增心情配置DTO（默认心情=平静，code=10）
        AddAiMoodConfigDTO dto = new AddAiMoodConfigDTO();
        dto.setUserId(userId); // 绑定当前会话的用户ID
        dto.setAiMoodCode(AiMoodEnum.NO_MOOD); // 默认心情：无心情（code=1）

        // 3. 调用新增心情配置方法，初始化AI心情
        addAiMoodConfig(dto);
    }

    @Override
    public AiMoodConfig getCurrentStrongestMood(Integer userId) {
        AiMoodConfig strongestMood = null;

        // 查询用户配置
        if (userId != null) {
            strongestMood = aiMoodConfigMapper.selectPriorityConfig(userId);
        }

        return strongestMood;
    }

    @Override
    public List<AiMoodConfig> listAiMoodConfigByUserId(Integer userId) {
        // 代码层空值校验（SQL不处理）
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        // 查询结果：无数据返回空List，避免NPE
        List<AiMoodConfig> configList = aiMoodConfigMapper.selectAiMoodConfigByUserId(userId);
        return configList == null ? Collections.emptyList() : configList;
    }

    // ====================== 1. 批量：按userId关闭/打开AI心情 ======================
    @Override
    public int toggleAiMoodValidByUser(Integer userId, Integer isValid) {
        // 基础参数校验
        if (userId == null) {
            throw new IllegalArgumentException("参数不合法：userId不能为空");
        }
        if (isValid == null || (isValid != 0 && isValid != 1)) {
            throw new IllegalArgumentException("参数不合法：isValid只能为0（关闭）或1（打开）");
        }
        // 最小实现：调用mapper
        return aiMoodConfigMapper.switchValidByUser(userId, isValid);
    }

    // ====================== 2. 精准更新：按id+userId更新某一个AI心情 ======================
    @Override
    public int updateAiMoodByIdAndUser(Long id, Integer userId, AiMoodEnum aiMoodCode, AiMoodStrengthEnum aiStrengthCode) {
        // 基础参数校验（必填项）
        if (id == null) {
            throw new IllegalArgumentException("参数不合法：心情配置ID不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("参数不合法：userId不能为空");
        }
        // 非必填项：至少更新一个字段（心情/强度）
        if (aiMoodCode == null && aiStrengthCode == null) {
            throw new IllegalArgumentException("参数不合法：至少要更新aiMoodCode或aiStrengthCode其中一个");
        }

        // 最小实现：封装实体，调用mapper
        AiMoodConfig config = new AiMoodConfig();
        config.setId(id);
        config.setUserId(userId);
        config.setAiMoodCode(aiMoodCode); // 传null则不更新（mapper里的if判断会过滤）
        config.setAiStrengthCode(aiStrengthCode); // 传null则不更新
        return aiMoodConfigMapper.updateByIdAndUser(config);
    }

    // ====================== 3. 精准：按id关闭/打开某一个AI心情 ======================
    @Override
    public int toggleAiMoodValidById(Long id, Integer isValid) {
        // 基础参数校验
        if (id == null) {
            throw new IllegalArgumentException("参数不合法：心情配置ID不能为空");
        }
        if (isValid == null || (isValid != 0 && isValid != 1)) {
            throw new IllegalArgumentException("参数不合法：isValid只能为0（关闭）或1（打开）");
        }
        // 最小实现：调用mapper
        return aiMoodConfigMapper.switchValidById(id, isValid);
    }
}