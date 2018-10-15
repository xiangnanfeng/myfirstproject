package com.pinyougou.pay.service;

import java.util.Map;

public interface WeiXinPayService {

    public Map createPay(String out_trade_no, String total_fee);
}
