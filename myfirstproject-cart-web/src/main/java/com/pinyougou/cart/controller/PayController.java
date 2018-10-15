package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeiXinPayService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import utils.IdWorker;

import java.util.Map;

@RestController
@RequestMapping("/WXpay")
public class PayController {

    @Reference
    private WeiXinPayService weiXinPayService;

    @RequestMapping("/createPay")
    public Map createPay(){

        return weiXinPayService.createPay(new IdWorker().nextId() + "", "1");
    }
}
