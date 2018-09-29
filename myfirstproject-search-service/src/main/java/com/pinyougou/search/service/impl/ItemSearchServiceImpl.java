package com.pinyougou.search.service.impl;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Service(timeout = 50000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> searchItem(Map searchMap) {
        Map<String, Object> resultMap = new HashMap<>();

        Map<String, Object> searchList = searchList(searchMap);

        resultMap.putAll(searchList);

        //查询出按item_category分类后的规格表，分组查询 商品分类列表
        List<String> categoryList = ItemGroup(searchMap);

        resultMap.put("categoryList", categoryList);
        //取出前端传递过来的category值
        String category = (String) searchMap.get("category");

        if (!"".equals(category)) {
            //按照category的值到缓存中查出模板的typeId，然后再根据typeId查出对应的品牌和规格列表
            Map<String, Object> brandAndSpec = searchBrandAndSpec(category);

            resultMap.putAll(brandAndSpec);
        } else {
            if (categoryList.size() > 0) {
                Map<String, Object> brandAndSpec = searchBrandAndSpec(categoryList.get(0));
                resultMap.putAll(brandAndSpec);
            }
        }

        return resultMap;
    }

    @Override
    public void importItemList(List<TbItem> list) {
        if(list.size()>0 && list!=null) {
            solrTemplate.saveBeans(list);
            solrTemplate.commit();
        }
    }

    @Override
    public void deleteSearch(Long[] ids) {
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(Arrays.asList(ids));
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    //查询所有商品
    private Map<String, Object> searchList(Map map) {
        HighlightQuery query = new SimpleHighlightQuery();

        //从map集合中获取传递过来的当前页码，如果当前没有传递页码，则默认为1
        Integer currentPage = (Integer) map.get("currentPage");
        //从map集合获取传递过来的每页最大显示数，如果没有，则默认为20
        Integer pageSize = (Integer) map.get("pageSize");

        if (currentPage == null) {
            currentPage = 1;
        }
        if (pageSize == null) {
            pageSize = 20;
        }
        query.setOffset((currentPage - 1) * pageSize);

        query.setRows(pageSize);

        String stringKeywords = (String) map.get("keywords");


        String keywords = stringKeywords.replaceAll(" ", "");

        Criteria criteria = null;
        if (!"".equals(keywords)) {
            //页面输入的关键字当中可能有空格存在，必须全部替换成空字符串0

            criteria = new Criteria("item_keywords").is(keywords);
        } else {
            criteria = new Criteria().expression("*:*");
        }

        //获取排序方式及字段
        String sort = (String) map.get("sort");
        String sortField = (String) map.get("sortField");
        if (!"".equals(sort) && !"".equals(sortField)) {
            if("ASC".equals(sort)){
                Sort sortQuery = new Sort(Sort.Direction.ASC,sortField);
                query.addSort(sortQuery);
            }
            if("DESC".equals(sort)){
                Sort sortQuery = new Sort(Sort.Direction.DESC,sortField);
                query.addSort(sortQuery);
            }

        }


        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");

        highlightOptions.setSimplePrefix("<em style='color:red'>");
        highlightOptions.setSimplePostfix("</em>");

        query.setHighlightOptions(highlightOptions);

        query.addCriteria(criteria);

        //按分类过滤
        if (!"".equals(map.get("category"))) {
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria criteria1 = new Criteria("item_category").is(map.get("category"));
            filterQuery.addCriteria(criteria1);
            query.addFilterQuery(filterQuery);
        }
        //按品牌过滤
        if (!"".equals(map.get("brand"))) {
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria criteria1 = new Criteria("item_brand").is(map.get("brand"));
            filterQuery.addCriteria(criteria1);
            query.addFilterQuery(filterQuery);
        }
        //按规格列表过滤
        if (map.get("spec") != null) {
            Map<String, String> specMap = (Map<String, String>) map.get("spec");
            Set<String> specSet = specMap.keySet();
            for (String spec : specSet) {
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria criteria1 = new Criteria("item_spec_" + spec).is(specMap.get(spec));
                filterQuery.addCriteria(criteria1);
                query.addFilterQuery(filterQuery);
            }
        }

        //按照价格区间筛选
        if (!"".equals(map.get("price"))) {

            FilterQuery filterQuery1 = new SimpleFilterQuery();

            String price = (String) map.get("price");

            String[] priceArray = price.split("-");
            //声明条件，筛选价格大于priceArray[0]的,比如 500-1000，此处筛选的时大于500的
            Criteria criteria1 = new Criteria("item_price").greaterThanEqual(priceArray[0]);

            filterQuery1.addCriteria(criteria1);

            query.addFilterQuery(filterQuery1);

            if (!"*".equals(priceArray[1])) {//先判断是不是*，如果不是的话，则要声明一条件，筛选价格小于priceArray[1]，此处筛选的时小于1000的 ，如果priceArray[1]=*，那么则没有上限

                FilterQuery filterQuery2 = new SimpleFilterQuery();

                Criteria criteria2 = new Criteria("item_price").lessThanEqual(priceArray[1]);

                filterQuery2.addCriteria(criteria2);

                query.addFilterQuery(filterQuery2);
            }
        }

        HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage(query, TbItem.class);

        List<HighlightEntry<TbItem>> highlightEntries = highlightPage.getHighlighted();

        for (HighlightEntry<TbItem> highlightEntry : highlightEntries) {

            TbItem item = highlightEntry.getEntity();

            List<HighlightEntry.Highlight> highlights = highlightEntry.getHighlights();

            for (HighlightEntry.Highlight highlight : highlights) {

                List<String> stringList = highlight.getSnipplets();

                for (String str : stringList) {

                    item.setTitle(str);
                }
            }
        }
        //查询出总的页码
        int totalPages = highlightPage.getTotalPages();
        //查询出总的记录数
        long totalElements = highlightPage.getTotalElements();

        Map<String, Object> resultMap = new HashMap<>();

        resultMap.put("totalPages", totalPages);

        resultMap.put("totalElements", totalElements);

        resultMap.put("rows", highlightPage.getContent());

        return resultMap;
    }

    //根据前端输入的关键字，然后分组
    private List<String> ItemGroup(Map map) {

        List<String> list = new ArrayList();

        Query query = new SimpleQuery("*:*");

        String stringKeywords = (String) map.get("keywords");


        String keywords = stringKeywords.replaceAll(" ", "");

        Criteria criteria = null;
        if (!"".equals(keywords)) {
            //页面输入的关键字当中可能有空格存在，必须全部替换成空字符串0

            criteria = new Criteria("item_keywords").is(keywords);
        } else {
            criteria = new Criteria().expression("*:*");
        }



        query.addCriteria(criteria);


        GroupOptions groupOptions = new GroupOptions();

        groupOptions.addGroupByField("item_category");

        query.setGroupOptions(groupOptions);

        GroupPage<TbItem> itemGroupPage = solrTemplate.queryForGroupPage(query, TbItem.class);

        GroupResult<TbItem> groupResult = itemGroupPage.getGroupResult("item_category");

        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();

        List<GroupEntry<TbItem>> groupEntryList = groupEntries.getContent();

        for (GroupEntry<TbItem> groupEntry : groupEntryList) {
            list.add(groupEntry.getGroupValue());
        }
        return list;

    }

    //根据品牌和规格查询
    private Map<String, Object> searchBrandAndSpec(String category) {

        Map map = new HashMap();

        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);

        if (typeId != null) {

            List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);

            map.put("brandList", brandList);

            List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);

            map.put("specList", specList);
        }

        return map;
    }
}
