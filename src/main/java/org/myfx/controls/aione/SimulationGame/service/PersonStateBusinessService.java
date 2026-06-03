package org.myfx.controls.aione.SimulationGame.service;

import org.myfx.controls.aione.SimulationGame.entity.LocationEventEffect;
import org.myfx.controls.aione.SimulationGame.entity.PersonState;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import lombok.RequiredArgsConstructor;

/**
 * 人物状态-地点事件 组合业务服务
 * 上层业务类：封装两个Service的联动逻辑
 */
@Service
@RequiredArgsConstructor // 构造器注入（Spring推荐依赖注入方式）
public class PersonStateBusinessService {

    // 注入两个业务Service（面向接口注入，不要注入实现类）
    private final LocationEventEffectService locationEventEffectService;
    private final PersonStateService personStateService;

    /**
     * 根据【地点编码+事件编码】，更新人物状态（固定ID=1的人物）
     * @param locationCode 地点编码（如：HOME）
     * @param eventCode    事件编码（如：SLEEP）
     * @return 数据库更新行数
     */
    public int updatePersonStateByCodes(String locationCode, String eventCode) {
        // 1. 双编码非空校验
        Assert.hasText(locationCode, "地点编码不能为空");
        Assert.hasText(eventCode, "事件编码不能为空");

        // 2. 根据【双编码】查询地点事件效果（替换原来的ID查询）
        LocationEventEffect eventEffect = locationEventEffectService.getByLocationAndEventCode(locationCode, eventCode);
        Assert.notNull(eventEffect, "未查询到对应的地点+事件效果信息");

        // 3. 固定查询ID=1的人物状态（不变）
        PersonState personState = personStateService.getPersonStateById(1);
        Assert.notNull(personState, "未查询到ID为1的人物状态信息");

        // 4. 封装人物状态：属性叠加 + 覆盖编码（逻辑完全不变）
        // 给你优化后的属性赋值（直接替换原来的代码）
        personState.setHunger(limitValue(personState.getHunger() + eventEffect.getHungerEffect()));
        personState.setEnergy(limitValue(personState.getEnergy() + eventEffect.getEnergyEffect()));
        personState.setMood(limitValue(personState.getMood() + eventEffect.getMoodEffect()));
        personState.setCurrentLocationCode(locationCode);
        personState.setCurrentEventCode(eventCode);

        // 5. 调用原有方法更新（不变）
        return personStateService.editPersonStateById(personState);
    }

    // 工具方法：限制数值 0-100
    private Double limitValue(Double value) {
        return Math.max(0.0, Math.min(100.0, value));
    }
}