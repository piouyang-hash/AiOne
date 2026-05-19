package org.myfx.controls.aione.SimulationGame.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 全局游戏线性时间实体
 * 核心规则：每1分钟扫描一次，global_game_seconds +60
 * 全表仅存在 ID=1 的一条数据
 */
@Data
@TableName("simulate_game_global_time")
@Schema(description = "模拟游戏-全局线性时间配置（唯一时间基准）")
public class GameGlobalTime {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID，固定为1", example = "1")
    private Integer id;

    @Schema(description = "全局游戏总秒数（核心）", example = "600")
    private Integer globalGameSeconds;

    @Schema(description = "记录创建时间", hidden = true)
    private LocalDateTime createTime;

    @Schema(description = "记录更新时间", hidden = true)
    private LocalDateTime updateTime;
}