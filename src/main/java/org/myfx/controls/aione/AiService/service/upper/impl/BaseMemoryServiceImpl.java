package org.myfx.controls.aione.AiService.service.upper.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.entity.my_memory_db.BaseHobby;
import org.myfx.controls.aione.AiService.entity.my_memory_db.BaseUserInfo;
import org.myfx.controls.aione.AiService.service.base.my_memory_db.BaseHobbyService;
import org.myfx.controls.aione.AiService.service.base.my_memory_db.UserHobbyRelationService;
import org.myfx.controls.aione.AiService.service.base.my_memory_db.UserInfoService;
import org.myfx.controls.aione.AiService.service.upper.BaseMemoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 基础记忆服务实现类（组合式业务）
 * ⚠️ 重要声明：该类为组合式业务入口，仅允许注入「原子级基础服务」（如UserInfoService、UserHobbyRelationService），
 *    禁止注入任何「子业务逻辑服务」（如XXBizService），否则会直接导致循环依赖！
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BaseMemoryServiceImpl implements BaseMemoryService {

    // 注入原子级基础服务：用户信息服务（仅聚合调用，不包含业务逻辑）
    private final UserInfoService userInfoService;

    // 注入原子级基础服务：用户爱好关联服务（预留扩展，当前接口暂未使用）
    private final UserHobbyRelationService userHobbyRelationService;

    // 注入：爱好基础服务（用于解析爱好ID→爱好名称）
    private final BaseHobbyService baseHobbyService;

    // ====================== 手动传userId的方法（核心实现） ======================
    @Override
    public String getUserProfilePrompt(Integer userId) {
        // 1. 参数校验（手动传参必须校验userId非空）
        if (userId == null) {
            throw new IllegalArgumentException("手动传入的用户ID不能为空");
        }

        // 2. 打印关键信息（标注是手动传参）
        log.info("【手动传userId】开始获取用户基础画像提示词，userId：{}", userId);

        // 3. 调用已有方法查询用户信息
        BaseUserInfo userInfo = userInfoService.getUserBaseInfoByUserId(userId);

        // 4. 处理字段：null/空值填“未知”，性别转中文描述
        String genderDesc = resolveGender(userInfo);
        String ageDesc = resolveAge(userInfo);
        String identityDesc = resolveIdentity(userInfo);

        // 5. 拼接用户基础画像提示词（简洁、适配AI对话场景）
        return String.format(
                "性别为【%s】，年龄为【%s】，身份为【%s】。",
                genderDesc, ageDesc, identityDesc
        );
    }

    @Override
    public String getUserHobbyPrompt(Integer userId) {
        // 1. 参数校验（手动传参必须校验userId非空）
        if (userId == null) {
            throw new IllegalArgumentException("手动传入的用户ID不能为空");
        }

        // 2. 打印关键信息（标注是手动传参）
        log.info("【手动传userId】开始获取用户爱好画像提示词，userId：{}", userId);

        // 3. 调用UserInfoService查询用户基础信息，从中获取userInfoId（long类型的id）
        BaseUserInfo userInfo = userInfoService.getUserBaseInfoByUserId(userId);
        if (userInfo == null) {
            throw new IllegalArgumentException("未查询到用户基础信息，userId：" + userId);
        }
        Long userInfoId = userInfo.getId(); // 从用户基础信息中获取long类型的id（雪花ID）
        if (userInfoId == null || userInfoId <= 0) {
            throw new IllegalArgumentException("用户基础信息中ID不合法，userId：" + userId);
        }

        // 4. 调用爱好关联服务，查询用户所有爱好ID
        List<Integer> hobbyIds = userHobbyRelationService.listUserHobbyIds(userInfoId, userId);

        // 5. 处理空列表：直接返回“未知/暂无爱好”
        if (hobbyIds == null || hobbyIds.isEmpty()) {
            return "暂无已知爱好。"; // 补充换行符，和其他提示词格式统一
        }

        // 6. 遍历爱好ID，解析为爱好中文名称（从枚举desc获取）
        List<String> hobbyNames = hobbyIds.stream()
                .map(hobbyId -> {
                    // 调用爱好基础服务，根据ID查询爱好信息
                    BaseHobby baseHobby = baseHobbyService.getHobbyById(hobbyId);
                    // 爱好信息为空时，显示“未知爱好”
                    return baseHobby != null && baseHobby.getHobbyName() != null
                            ? baseHobby.getHobbyName().getDesc()
                            : "未知爱好";
                })
                .collect(Collectors.toList());

        // 7. 拼接爱好提示词（逗号分隔爱好名称）
        String hobbyStr = String.join("、", hobbyNames);
        return String.format("爱好为【%s】。", hobbyStr);
    }

    // ====================== 原有私有解析方法（保留） ======================
    /**
     * 解析性别字段（转中文，null填未知）
     */
    private String resolveGender(BaseUserInfo userInfo) {
        if (userInfo == null || userInfo.getGender() == null) {
            return "未知";
        }
        // 适配GenderEnum枚举（1=男，2=女，0=未知）
        return switch (userInfo.getGender()) {
            case MALE -> "男";       // 假设GenderEnum.MALE对应值1
            case FEMALE -> "女";     // 假设GenderEnum.FEMALE对应值2
            case UNKNOWN -> "未知";  // 假设GenderEnum.UNKNOWN对应值0
            default -> "未知";
        };
    }

    /**
     * 解析年龄字段（null/0填未知，否则转字符串）
     */
    private String resolveAge(BaseUserInfo userInfo) {
        if (userInfo == null || userInfo.getAge() == null || userInfo.getAge() == 0) {
            return "未知";
        }
        return userInfo.getAge().toString();
    }

    /**
     * 解析身份字段（null/空填未知）
     */
    private String resolveIdentity(BaseUserInfo userInfo) {
        if (userInfo == null || userInfo.getIdentity() == null || userInfo.getIdentity().isBlank()) {
            return "未知";
        }
        return userInfo.getIdentity().trim();
    }
}