package org.myfx.controls.aione.ServiceCommon.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

// 全局雪花ID生成器（单机部署填0,0即可，不用改）
public class SnowflakeGenerator {
    // 机器ID=0，机房ID=0（分布式时再调整）
    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake(0, 0);

    // 生成雪花ID（返回Long类型，对应MySQL的BIGINT）
    public static Long generateId() {
        return SNOWFLAKE.nextId();
    }
}
