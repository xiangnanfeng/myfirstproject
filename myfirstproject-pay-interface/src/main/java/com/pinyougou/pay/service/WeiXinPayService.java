package com.pinyougou.pay.service;

import java.util.Map;

public interface WeiXinPayService {
    //向微信指定接口发送支付请求，
    public Map createPay(String out_trade_no, String total_fee);

    //生成二维码支付页面后，查询是否支付
    public Map queryPay(String out_trade_no);

    //如果支付超时，则关闭微信支付二维码接口
    public Map closePay(String out_trade_no);
}
