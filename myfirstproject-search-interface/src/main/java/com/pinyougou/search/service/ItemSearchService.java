package com.pinyougou.search.service;

import com.pinyougou.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

    Map<String,Object> searchItem(Map map);

    void importItemList(List<TbItem> list);

    /**
     * 删除solr索引库，运营商删除的商品部分索引库
     * @param ids
     */
    void deleteSearch(Long[] ids);
}
