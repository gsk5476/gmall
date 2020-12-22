package com.atguigu.gmall.sms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuSaleVo {
    private Long skuId;

    //积分优惠信息接收  sms-SkuBoundsEntity
    private BigDecimal growBounds;
    private BigDecimal buyBounds;
    private List<Integer> work;

    //打折相关信息    sms-SkuLadderEntity
    private Integer fullCount;
    private BigDecimal discount;
    private Integer ladderAddOther;

    //满减信息  sms-SkuFullReductionEntity
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer fullAddOther;

}
