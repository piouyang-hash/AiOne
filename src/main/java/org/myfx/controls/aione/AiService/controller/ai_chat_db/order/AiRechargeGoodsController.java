package org.myfx.controls.aione.AiService.controller.ai_chat_db.order;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.order.AiRechargeGoods;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.order.AiRechargeGoodsService;
import org.myfx.controls.aione.AiService.vo.order.AiRechargeGoodsVO;
import org.myfx.controls.aione.ServiceCommon.AppResponse;
import org.myfx.controls.aione.ServiceCommon.SwaggerResponseConstants;
import org.myfx.controls.aione.ServiceCommon.annotation.AiAppCors;
import org.myfx.controls.aione.ServiceCommon.annotation.CheckJwt;
import org.myfx.controls.aione.ServiceCommon.annotation.CleanupThreadLocal;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI积分充值商品 控制器
 */
@RestController
@RequestMapping("/ai/recharge/goods")
@Tag(name = "AI充值档位查询接口", description = "AI充值档位查询，检验操作")
@CleanupThreadLocal
@RequiredArgsConstructor
@AiAppCors
@Slf4j
public class AiRechargeGoodsController {

    @Resource
    private AiRechargeGoodsService aiRechargeGoodsService;

    /**
     * 根据ID查询充值商品详情
     */
    @Operation(
            summary = "查询充值商品详情",
            description = """
                根据商品ID查询充值档位信息：
                1. 登录态接口，需携带有效的JWT Token；
                2. 传入商品主键ID（雪花ID）；
                3. 返回商品金额、积分、名称、描述等全部信息；
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/detail/{id}")
    @CheckJwt
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    public AppResponse<AiRechargeGoodsVO> getRechargeGoodsDetail(@PathVariable Long id) {
        // 1. 查询数据库原始实体
        AiRechargeGoods goods = aiRechargeGoodsService.getRechargeGoodsById(id);

        // 2. 创建VO对象
        AiRechargeGoodsVO goodsVO = new AiRechargeGoodsVO();

        // 3. Spring自带工具类：拷贝基础属性
        BeanUtils.copyProperties(goods, goodsVO);

        // 4. 返回VO
        return AppResponse.success(goodsVO, "查询商品详情成功");
    }

    /**
     * 查询所有启用的充值商品列表
     */
    @Operation(
            summary = "查询所有启用的充值商品",
            description = """
                查询系统所有可用的积分充值档位：
                1. 登录态接口，需携带有效的JWT Token；
                2. 仅返回状态为【启用】的商品；
                3. 按充值金额升序排序，前端直接展示即可；
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/list")
    @CheckJwt
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    public AppResponse<List<AiRechargeGoodsVO>> getAllRechargeGoods() {
        // 1. 查询数据库原始实体列表
        List<AiRechargeGoods> goodsList = aiRechargeGoodsService.getAllRechargeGoods();

        // 2. 批量拷贝属性转VO
        List<AiRechargeGoodsVO> voList = goodsList.stream().map(goods -> {
            AiRechargeGoodsVO vo = new AiRechargeGoodsVO();
            BeanUtils.copyProperties(goods, vo);
            return vo;
        }).collect(Collectors.toList());

        // 3. 返回VO列表
        return AppResponse.success(voList, "查询商品列表成功");
    }
}