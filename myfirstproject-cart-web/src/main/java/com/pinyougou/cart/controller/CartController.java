package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import utils.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
@CrossOrigin(origins="http://localhost:9105")
public class CartController {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @Reference
    private CartService cartService;



    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        if("anonymousUser".equals(username)){//如果用户没有登录，则从cookie中查询

            String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");

            if(cartListString==null || "".equals(cartListString)){

                cartListString = "[]";
            }
            List<Cart> cartList = JSON.parseArray(cartListString, Cart.class);

            return cartList;
        }else {
            //如果登录了，则先把cookie中的购物车查询出来

            String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");

            if(cartListString==null || "".equals(cartListString)){

                cartListString = "[]";
            }
            List<Cart> cartListByCookie = JSON.parseArray(cartListString, Cart.class);//从cookie查询出来的购物车

            List<Cart> cartListByRedis = cartService.findCartListByRedis(username);//从缓存中查询出来的购物车

            if(cartListByCookie.size()>0){

                cartListByRedis = cartService.mergeCartList(cartListByCookie, cartListByRedis);//对cookie和缓存中的购物车进行合并


                CookieUtil.deleteCookie(request,response,"cartList");//把cookie中的购物车数据删除

                cartService.saveCartListToRedis(cartListByRedis,username);//把合并后的购物车存入缓存
            }

            return cartListByRedis;
        }

    }

    @RequestMapping("/addItemToCartList")
    public Result addItemToCartList(Long itemId,Integer number){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            //先从cookie当中查询出购物车集合
            List<Cart> cartList = findCartList();
            //然后再调用服务层的addItemToCartList方法添加订单到购物车
            cartList = cartService.addItemToCartList(cartList, itemId, number);

            if("anonymousUser".equals(username)){//说明未登录,存储到cokkie中

                //将添加过后的订单集合从新设置给cookie
                CookieUtil.setCookie(request,response,"cartList",JSON.toJSONString(cartList),3600*12,"UTF-8");

            }else {//说明已登录

                cartService.saveCartListToRedis(cartList,username);
            }

            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();

            return new Result(false,"添加失败");
        }
    }

}
