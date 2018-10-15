package com.pinyougou.cart.service;


import com.pinyougou.pojogroup.Cart;

import java.util.List;

public interface CartService {
    //向购物车添加商品
    List<Cart> addItemToCartList(List<Cart> cartList, Long itemId, Integer number);

    //从缓存中查询购物车
    List<Cart> findCartListByRedis(String username);

    //向缓存中存储购物车里的商品
    void saveCartListToRedis(List<Cart> cartList,String username);

    //当登录过后，应该对缓存中的购物车和cookie中的购物车进行合并
    List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);
}
