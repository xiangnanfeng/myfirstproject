package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeiXinPayService;
import org.springframework.beans.factory.annotation.Value;
import utils.HttpClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeiXinPayServiceImpl implements WeiXinPayService {

    @Value("${appid}")
    private String appid;

    @Value("${partner}")
    private String partner;

    @Value("${partnerkey}")
    private String partnerkey;

    @Value("${notifyurl}")
    private String notifyurl;

    @Override
    public Map createPay(String out_trade_no, String total_fee) {

        Map<String,String> paramMap = new HashMap<>();
        //设置请求参数
        paramMap.put("appid",appid);
        paramMap.put("mch_id",partner);
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
        paramMap.put("body","微信支付");
        paramMap.put("out_trade_no",out_trade_no);
        paramMap.put("total_fee",total_fee);
        paramMap.put("spbill_create_ip","192.168.25.135");
        paramMap.put("notify_url",notifyurl);
        paramMap.put("trade_type","NATIVE");
        try {
            String paramXml = WXPayUtil.generateSignedXml(paramMap, partnerkey);
            //创建请求客户端
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            //设置参数
            httpClient.setHttps(true);
            httpClient.setXmlParam(paramXml);
            httpClient.post();//发送请求
            String responseResult = httpClient.getContent();//接收响应的返回结果

            Map<String, String> stringMap = WXPayUtil.xmlToMap(responseResult);//将响应回来的结果转换成map集合

            Map<String,String> map = new HashMap<>();

            map.put("code_url",stringMap.get("code_url"));//支付地址，靠他生成二维码
            map.put("total_fee",total_fee);//总金额
            map.put("out_trade_no",out_trade_no);//订单编号

            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }
}
