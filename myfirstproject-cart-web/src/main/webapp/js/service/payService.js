app.service("payService",function ($http) {
    this.createPay=function () {
        return $http.get('WXpay/createPay.do');
    }

    this.queryPay=function (out_trade_no) {//参数是订单编号
        return $http.get('WXpay/queryPay.do?out_trade_no='+out_trade_no);
    }
});