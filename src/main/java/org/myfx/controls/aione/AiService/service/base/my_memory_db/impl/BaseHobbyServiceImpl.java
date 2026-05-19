package org.myfx.controls.aione.AiService.service.base.my_memory_db.impl;

import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.AiService.common.my_memory_db.HobbyEnum;
import org.myfx.controls.aione.AiService.entity.my_memory_db.BaseHobby;
import org.myfx.controls.aione.AiService.mapper.my_memory_db.BaseHobbyMapper;
import org.myfx.controls.aione.AiService.service.base.my_memory_db.BaseHobbyService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 爱好业务服务实现类（仅做参数校验+Mapper调用，简化逻辑）
 */
@Service
@RequiredArgsConstructor
public class BaseHobbyServiceImpl implements BaseHobbyService {

    // 注入Mapper（底层数据操作）
    private final BaseHobbyMapper baseHobbyMapper;

    /**
     * 新增爱好：仅校验名称非空+幂等逻辑（避免重复插入），不合法抛参数错误/运行时错误
     */
    public boolean addHobby(HobbyEnum hobbyName) {
        // 基础参数校验：枚举不能为空
        if (hobbyName == null) {
            throw new IllegalArgumentException("参数不合法错误：爱好名称不能为空");
        }

        // ========== 新增幂等逻辑：先查询是否已存在该爱好 ==========
        BaseHobby existHobby = baseHobbyMapper.selectByName(hobbyName);
        if (existHobby != null) {
            // 抛运行时错误，简单实现幂等（避免重复插入）
            throw new RuntimeException("运行时错误：爱好【" + hobbyName.getDesc() + "】已存在，禁止重复新增");
        }

        // 组装BaseHobby实体（仅填写核心字段hobbyName）
        BaseHobby baseHobby = new BaseHobby();
        baseHobby.setHobbyName(hobbyName); // 设置枚举类型的hobbyName，MyBatis-Plus自动转code存入数据库

        // 调用Mapper插入实体（MyBatis-Plus通过@EnumValue自动把枚举转code存入hobby_name字段）
        int affectedRows = baseHobbyMapper.insertHobby(baseHobby);
        return affectedRows > 0;
    }

    /**
     * 根据ID查询爱好：仅校验ID合法，不合法抛参数错误
     */
    @Override
    public BaseHobby getHobbyById(Integer hobbyId) {
        // 基础参数校验：ID不能为空且>0
        if (hobbyId == null || hobbyId <= 0) {
            throw new IllegalArgumentException("参数不合法错误：爱好ID必须为正整数");
        }
        // 直接调用Mapper查询
        return baseHobbyMapper.selectById(hobbyId);
    }

    /**
     * 根据名称查询爱好：仅校验名称非空，不合法抛参数错误
     */
    @Override
    public BaseHobby getHobbyByName(HobbyEnum hobbyName) {
        // 基础参数校验：名称不能为空/空白
        if (hobbyName == null) {
            throw new IllegalArgumentException("参数不合法错误：爱好名称不能为空");
        }
        // 直接调用Mapper查询
        return baseHobbyMapper.selectByName(hobbyName);
    }

    /**
     * 查询所有爱好：无参数，直接调用Mapper
     */
    @Override
    public List<BaseHobby> listAllHobbies() {
        // 无参数校验，直接调用Mapper，保证返回非null（避免空指针）
        List<BaseHobby> hobbyList = baseHobbyMapper.selectAll();
        return hobbyList == null ? List.of() : hobbyList;
    }
}