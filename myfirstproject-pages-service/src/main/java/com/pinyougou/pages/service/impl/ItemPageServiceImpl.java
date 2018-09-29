package com.pinyougou.pages.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pages.service.ItemPageService;
import com.pinyougou.pojo.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbItemMapper itemMapper;

    @Value("${pagedir}")
    private String pagedir;

    @Override
    public boolean getItemHtml(Long goodsId) {

        Configuration configuration = freeMarkerConfigurer.getConfiguration();

        try {
            Template template = configuration.getTemplate("item.ftl");
            Map map = new HashMap();
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            Long category1Id = goods.getCategory1Id();
            Long category2Id = goods.getCategory2Id();
            Long category3Id = goods.getCategory3Id();
            TbItemCat tbItemCat1 = itemCatMapper.selectByPrimaryKey(category1Id);
            TbItemCat tbItemCat2 = itemCatMapper.selectByPrimaryKey(category2Id);
            TbItemCat tbItemCat3 = itemCatMapper.selectByPrimaryKey(category3Id);

            map.put("category1",tbItemCat1.getName());
            map.put("category2",tbItemCat2.getName());
            map.put("category3",tbItemCat3.getName());
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            map.put("goods",goods);
            map.put("goodsDesc",goodsDesc);

            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andGoodsIdEqualTo(goodsId);
            criteria.andStatusEqualTo("1");
            example.setOrderByClause("is_default desc");
            List<TbItem> itemList = itemMapper.selectByExample(example);
            map.put("itemList",itemList);
            Writer writer = new FileWriter(pagedir+goodsId+".html");
            template.process(map,writer);
            writer.close();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
