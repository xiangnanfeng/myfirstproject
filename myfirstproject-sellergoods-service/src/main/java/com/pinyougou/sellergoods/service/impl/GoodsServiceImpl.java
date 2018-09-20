package com.pinyougou.sellergoods.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojogroup.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbBrandMapper brandMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbSellerMapper sellerMapper;

    @Autowired
    private TbItemMapper itemMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(Goods goods) {
        TbGoods tbGoods = goods.getGoods();
        tbGoods.setAuditStatus("0");
        goodsMapper.insert(tbGoods);
        Long id = tbGoods.getId();
        TbGoodsDesc goodsDesc = goods.getGoodsDesc();
        goodsDesc.setGoodsId(id);
        goodsDescMapper.insert(goodsDesc);
        List<TbItem> itemList = goods.getItemList();
        itemListSave(goods,itemList);
    }

    private void setItemValues(Goods goods,TbItem item){
        //商品分类
        item.setCategoryid(goods.getGoods().getCategory3Id());//三级分类ID
        item.setCreateTime(new Date());//创建日期
        item.setUpdateTime(new Date());//更新日期

        item.setGoodsId(goods.getGoods().getId());//商品ID
        item.setSellerId(goods.getGoods().getSellerId());//商家ID

        //分类名称
        TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
        item.setCategory(itemCat.getName());
        //品牌名称
        TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
        item.setBrand(brand.getName());
        //商家名称(店铺名称)
        TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
        item.setSeller(seller.getNickName());

        //图片
        List<Map> imageList = JSON.parseArray( goods.getGoodsDesc().getItemImages(), Map.class) ;
        if(imageList.size()>0){
            item.setImage( (String)imageList.get(0).get("url"));
        }

    }


    /**
     * 修改
     */
    private void itemListSave(Goods goods,List<TbItem> itemList){
        if ("1".equals(goods.getGoods().getIsEnableSpec())) {

            for (TbItem item : itemList) {
                String title = goods.getGoods().getGoodsName();
                Map<String, String> map = JSON.parseObject(item.getSpec(), Map.class);
                Set<String> set = map.keySet();
                for (String str : set) {
                    title += " " + map.get(str);
                }
                item.setTitle(title);
                setItemValues(goods,item);
                itemMapper.insert(item);
            }
        }else {
            TbItem item=new TbItem();
            item.setTitle(goods.getGoods().getGoodsName());//商品 KPU+规格描述串作为
            item.setPrice( goods.getGoods().getPrice() );//价格
            item.setStatus("1");//状态
            item.setIsDefault("1");//是否默认
            item.setNum(99999);//库存数量
            item.setSpec("{}");
            setItemValues(goods,item);
            itemMapper.insert(item);
        }
    }
    @Override
    public void update(Goods goods) {
        TbGoods tbGoods = goods.getGoods();
        tbGoods.setAuditStatus("0");
        goodsMapper.updateByPrimaryKey(tbGoods);
        TbGoodsDesc tbGoodsDesc = goods.getGoodsDesc();
        goodsDescMapper.updateByPrimaryKey(tbGoodsDesc);
        Long tbGoodsId = tbGoods.getId();
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(tbGoodsId);
        itemMapper.deleteByExample(example);
        List<TbItem> itemList = goods.getItemList();
        itemListSave(goods,itemList);

    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public Goods findOne(Long id) {
        Goods goods = new Goods();
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
        goods.setGoods(tbGoods);
        TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
        goods.setGoodsDesc(tbGoodsDesc);
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(id);
        List<TbItem> itemList = itemMapper.selectByExample(example);
        goods.setItemList(itemList);
        return goods;
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            goodsMapper.deleteByPrimaryKey(id);
            goodsDescMapper.deleteByPrimaryKey(id);
            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andGoodsIdEqualTo(id);
            itemMapper.deleteByExample(example);
        }
    }


    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbGoodsExample example = new TbGoodsExample();
        Criteria criteria = example.createCriteria();
        //查询时排除被运营商删除了的商品（此处是逻辑删除）1表示被删除的，要查询没有被删除的 。
        criteria.andIsDeleteIsNull();
        if (goods != null) {
            if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
                criteria.andSellerIdLike(goods.getSellerId());
            }
            if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
                criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
            }
            if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
                criteria.andAuditStatusLike("%" + goods.getAuditStatus() + "%");
            }
            if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
                criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
            }
            if (goods.getCaption() != null && goods.getCaption().length() > 0) {
                criteria.andCaptionLike("%" + goods.getCaption() + "%");
            }
            if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
                criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
            }
            if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
                criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
            }
            if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
                criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
            }

        }

        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void updateStatus(Long[] ids, String status) {
        if(ids.length>0){
            for (Long id : ids) {
                TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
                tbGoods.setAuditStatus(status);
                goodsMapper.updateByPrimaryKey(tbGoods);
            }
        }
    }

    @Override
    public void del(Long[] ids) {
        for (Long id : ids) {
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            tbGoods.setIsDelete("1");
            goodsMapper.updateByPrimaryKey(tbGoods);
        }
    }

    @Override
    public void marketable(Long[] ids) {
        if(ids.length>0){
            for (Long id : ids) {
                TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
                tbGoods.setIsMarketable("0");
                goodsMapper.updateByPrimaryKey(tbGoods);
            }
        }
    }

}