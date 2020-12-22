package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.fegin.GmallPmsClient;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuAttrValueMapper;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.pms.service.SkuImagesService;
import com.atguigu.gmall.pms.service.SpuAttrValueService;
import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.pms.vo.SpuAttrValueVo;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuMapper;
import com.atguigu.gmall.pms.service.SpuService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Autowired
    private SpuDescMapper spuDescMapper;

    @Autowired
    private SpuAttrValueService spuAttrValueService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private SkuImagesService imagesService;

    @Autowired
    private SkuAttrValueService skuAttrValueService;

    @Autowired
    private GmallPmsClient pmsClient;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo querySpuByCidAndPage(Long cid, PageParamVo paramVo) {
        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<>();
        //如果用户选择了分类，并且查询本类
        if (cid != 0){
            wrapper.eq("category_id", cid);
        }
        String key = paramVo.getKey();
        //判断关键字是否为空
        if (StringUtils.isNotBlank(key)){
            wrapper.and(t -> t.eq("id", key).or().like("name",key));
        }

        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                wrapper
        );

        return new PageResultVo(page);
    }

    @GlobalTransactional
    @Override
    public void bigSave(SpuVo spu) {
        //1.保存spu相关信息
        //1.1.保存spu的基本信息：pms_spu
        Long spuId = saveSpu(spu);

        //1.2.保存spu的描述信息：pms_spu-desc
        saveSpuDesc(spu, spuId);

        //1.3.保存spu的基本属性信息：pms_spu_attr_value
        saveBaseAttr(spu, spuId);


        //2.保存sku相关信息
        //2.1.保存sku的基本信息：pms_sku
        saveSkuInfo(spu, spuId);

//        int i = 1 / 0;

    }

    private void saveSkuInfo(SpuVo spu, Long spuId) {
        List<SkuVo> skus = spu.getSkus();
        if (CollectionUtils.isEmpty(skus)){
            return;
        }
        skus.forEach(sku ->{
            sku.setSpuId(spuId);
            sku.setBrandId(spu.getBrandId());
            sku.setCatagoryId(spu.getCategoryId());
            //设置默认图片
            List<String> images = sku.getImages();
            if (!CollectionUtils.isEmpty(images)){
                sku.setDefaultImage(StringUtils.isBlank(sku.getDefaultImage()) ? sku.getDefaultImage() : images.get(0));
            }
            this.skuMapper.insert(sku);
            Long skuId = sku.getId();
            //2.2.保存sku的图片信息：pms_sku_images
            //先拿到图片集合 判断是否为空
            if (!CollectionUtils.isEmpty(images)){
                //将images转化为Collection图片集合
                imagesService.saveBatch(images.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setUrl(image);
                    //设置状态，与默认图片地址一样说明就是默认图片，设置为1，否则设置为0
                    skuImagesEntity.setDefaultStatus(StringUtils.equals(sku.getDefaultImage(), image) ? 1 : 0);
                    return skuImagesEntity;
                }).collect(Collectors.toList()));
            }

            //2.3.保存sku的销售属性信息：pms_sku_attr_value
            List<SkuAttrValueEntity> saleAttrs = sku.getSaleAttrs();
            if (CollectionUtils.isEmpty(saleAttrs)){
                saleAttrs.forEach(skuAttrValueEntity -> skuAttrValueEntity.setSkuId(skuId));
                this.skuAttrValueService.saveBatch(saleAttrs);
            }

            //3.保存sku的营销信息    在 SkuBoundsServiceImpl
            SkuSaleVo skuSaleVo = new SkuSaleVo();
            BeanUtils.copyProperties(sku,skuSaleVo);
            skuSaleVo.setSkuId(skuId);
            this.pmsClient.saveSales(skuSaleVo);

            //3.1.保存sku的积分优惠：sms_sku_bounds

            //3.2.保存sku的满减信息：sms_sku_full_reduction

            //3.3.保存sku的打折信息：sms_sku_ladder

        });
    }

    private void saveBaseAttr(SpuVo spu, Long spuId) {
        List<SpuAttrValueVo> baseAttrs = spu.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)){
            this.spuAttrValueService.saveBatch(baseAttrs.stream().map(spuAttrValueVo -> {
                SpuAttrValueEntity spuAttrValueEntity = new SpuAttrValueEntity();
                BeanUtils.copyProperties(spuAttrValueVo, spuAttrValueEntity);
                spuAttrValueEntity.setSpuId(spuId);
                return spuAttrValueEntity;
            }).collect(Collectors.toList()));
        }
    }

    private void saveSpuDesc(SpuVo spu, Long spuId) {
        List<String> spuImages = spu.getSpuImages();
        if (!CollectionUtils.isEmpty(spuImages)){
            SpuDescEntity spuDescEntity = new SpuDescEntity();
            spuDescEntity.setSpuId(spuId);
            spuDescEntity.setDecript(StringUtils.join(spuImages,","));
            this.spuDescMapper.insert(spuDescEntity);
        }
    }

    private Long saveSpu(SpuVo spu) {
        spu.setCreateTime(new Date());
        spu.setUpdateTime(spu.getCreateTime());
        this.save(spu);
        Long spuId = spu.getId();
        return spuId;
    }

}