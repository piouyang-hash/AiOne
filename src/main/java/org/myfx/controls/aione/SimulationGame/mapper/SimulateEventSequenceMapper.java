package org.myfx.controls.aione.SimulationGame.mapper;

import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.SimulationGame.entity.SimulateEventSequence;

import java.util.List;

/**
 * 模拟游戏-事件执行序列规则Mapper接口
 */
public interface SimulateEventSequenceMapper {

    /**
     * 新增事件序列规则（不处理自动生成的时间字段）
     * @param sequence 事件序列规则实体
     * @return 影响行数
     */
    int insert(SimulateEventSequence sequence);

    /**
     * 按版本号查询事件序列规则
     * @param version 版本号
     * @return 该版本下的所有事件序列规则列表
     */
    List<SimulateEventSequence> selectByVersion(@Param("version") Integer version);

    /**
     * 按版本号+执行次序查询事件序列规则（唯一结果）
     * @param version 版本号（如1=周一、2=周二）
     * @param seqNum 当日执行次序（如1=第1件事、2=第2件事）
     * @return 该版本+次序对应的唯一事件序列规则（无结果返回null）
     */
    SimulateEventSequence selectByVersionAndSeqNum(
            @Param("version") Integer version,
            @Param("seqNum") Integer seqNum
    );

    /**
     * 查询所有事件序列规则
     * @return 所有事件序列规则列表
     */
    List<SimulateEventSequence> listAllEventSequence();

    /**
     * 查找第一个事件（最小版本号中最小执行次序的事件）
     * @return 第一个事件序列规则，无数据返回null
     */
    SimulateEventSequence selectMinVersionMinSeq();

    /**
     * 按版本号删除事件序列规则
     * @param version 版本号
     * @return 影响行数
     */
    int deleteByVersion(@Param("version") Integer version);
}