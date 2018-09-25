package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.ScoredPage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map<String, Object> searchItem(Map map) {
        //创建查询对象
        Query query = new SimpleQuery();
        //创建条件对象，此条件对象按照复制域的item_keywords查询，查询的东西是map.get("keywords")
        Criteria criteria = new Criteria("item_keywords").is(map.get("keywords"));
        //将条件对象添加到查询对象中
        query.addCriteria(criteria);
        //执行查询，
        ScoredPage<TbItem> items = solrTemplate.queryForPage(query, TbItem.class);

        List<TbItem> itemList = items.getContent();

        Map<String,Object> resultMap = new HashMap<String,Object>();

        resultMap.put("rows",itemList);

        return resultMap;
    }
}
