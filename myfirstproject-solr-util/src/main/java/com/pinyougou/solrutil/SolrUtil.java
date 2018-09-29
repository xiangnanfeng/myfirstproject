package com.pinyougou.solrutil;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;


import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private TbItemMapper itemMapper;

    public void itemData(){

        //声明一个查询条件，只查询状态为1的产品
        TbItemExample example = new TbItemExample();

        TbItemExample.Criteria criteria = example.createCriteria();

        criteria.andStatusEqualTo("1");
        //先将item数据从数据库里面全部查出来
        List<TbItem> tbItems = itemMapper.selectByExample(example);

        for (TbItem item : tbItems) {
            //查询出item表的字段内容，将其转换为map对象
            String spec = item.getSpec();
            Map map = JSON.parseObject(spec, Map.class);
            //将转换过后的map对象存入TbItem类中增加的specMap字段；此字段为动态字段 能动态构建索引
            item.setSpecMap(map);

        }
        //将tbItems集合中的数据导入到索引库
        solrTemplate.saveBeans(tbItems);

        solrTemplate.commit();
    }

    public static void main(String[] args) {
        ApplicationContext applicationContext =
                new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        SolrUtil solrUtil = (SolrUtil) applicationContext.getBean("solrUtil");
        solrUtil.itemData();
    }
}
