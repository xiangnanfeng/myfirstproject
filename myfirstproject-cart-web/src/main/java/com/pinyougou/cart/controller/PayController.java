package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeiXinPayService;
import com.pinyougou.pojo.TbPayLog;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import utils.IdWorker;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/WXpay")
public class PayController {

    @Reference
    private WeiXinPayService weiXinPayService;

    @Reference
    private OrderService orderService;

    @RequestMapping("/createPay")
    public Map createPay() {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        TbPayLog payLog = orderService.getPayLog(username);

        if(payLog==null){

            return new HashMap();
        }else {
            return weiXinPayService.createPay(payLog.getOutTradeNo(), payLog.getTotalFee()+"");
        }
    }

    @RequestMapping("/queryPay")
    public Result queryPay(String out_trade_no) {

        Map queryMap = null;
        String trade_state = null;
        Result result = null;
        int x = 0;
        while (true) {//循环查询
            queryMap = weiXinPayService.queryPay(out_trade_no);

            trade_state = (String) queryMap.get("trade_state");
            if (queryMap == null) {
                result = new Result(false, "支付失败");
            }
            if ("SUCCESS".equals(trade_state)) {
                result = new Result(true, "支付成功");
                orderService.updateOrderStatus(out_trade_no, (String) queryMap.get("transaction_id"));
                break;
            }
            if(x>=100){
                result = new Result(false, "支付超时");
                break;
            }
            try {
                Thread.sleep(3000);//每隔三秒查询一次
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x++;
        }

        return result;
    }
}
