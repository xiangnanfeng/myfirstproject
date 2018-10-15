package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;

import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;

import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public List<Cart> addItemToCartList(List<Cart> cartList, Long itemId, Integer number) {
        //1.根据商品 SKU ID 查询 SKU 商品信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if(item==null){
            throw new RuntimeException("商品不存在");
        }
        if(!"1".equals(item.getStatus())){
            throw new RuntimeException("商品已下架");
        }
        //2.获取商家 ID
        String sellerId = item.getSellerId();
        //3.根据商家 ID 判断购物车列表中是否存在该商家的购物车
        Cart cart = searchSellerCart(cartList, sellerId);

        if(cart==null){//4.如果购物车列表中不存在该商家的购物车
            //4.1 新建购物车对象
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            List<TbOrderItem> orderItemList = new ArrayList<>();
            TbOrderItem orderItem = createOrderItem(item,number);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            //4.2 将新建的购物车对象添加到购物车列表
            cartList.add(cart);
        }else {//5.如果购物车列表中存在该商家的购物车
            // 查询购物车明细列表中是否存在该商品
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            TbOrderItem orderItem = searchOrderItem(orderItemList,itemId);
            if(orderItem==null){ //5.1. 如果没有，新增购物车明细
                orderItem=createOrderItem(item,number);
                orderItemList.add(orderItem);
            }else { //5.2. 如果有，在原购物车明细上添加数量，更改金额
                orderItem.setNum(orderItem.getNum()+number);
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));
                //如果数量操作后小于等于 0，则移除
                if(orderItem.getNum()<=0){
                    cart.getOrderItemList().remove(orderItem);//移除购物车明细
                }
                //如果移除后 cart 的明细数量为 0，则将 cart 移除
                if(cart.getOrderItemList().size()==0){
                    cartList.remove(cart);
                }

            }


        }
        return cartList;
    }

    @Override
    public List<Cart> findCartListByRedis(String username) {

        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);

        if(cartList==null){

            cartList=new ArrayList<>();
        }
        return cartList;
    }

    @Override
    public void saveCartListToRedis(List<Cart> cartList, String username) {

        redisTemplate.boundHashOps("cartList").put(username,cartList);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {

        for (Cart cart : cartList1) {

            for (TbOrderItem orderItem : cart.getOrderItemList()){

                cartList2=addItemToCartList(cartList2,orderItem.getItemId(),orderItem.getNum());
            }
        }
        return cartList2;
    }

    /**
     * 根据商品明细 ID 查询
     * @param orderItemList
     * @param itemId
     * @return
     */
    private TbOrderItem searchOrderItem(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            if(orderItem.getItemId().longValue()==itemId.longValue()){
                return  orderItem;
            }
        }
        return null;
    }

    /**
     * 创建订单明细
     * @param item
     * @param number
     * @return
     */
    private TbOrderItem createOrderItem(TbItem item, Integer number) {

        if(number<=0){
            throw new RuntimeException("数量错误");
        }

        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(number);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee( new BigDecimal(item.getPrice().doubleValue()*number) );
        return orderItem;
    }

    /**
     * 根据商家 ID 查询购物车对象
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart searchSellerCart(List<Cart> cartList,String sellerId){
        for (Cart cart : cartList) {
            if(sellerId.equals(cart.getSellerId())){
                return cart;
            }
        }
        return null;
    }
}
