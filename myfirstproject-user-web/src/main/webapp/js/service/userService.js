//服务层
app.service('userService',function($http){
	    	
    this.register=function (user,encode) {
        return $http.post('user/add.do?encode='+encode,user);
    }

    this.sendCode=function (phone) {
        return $http.get('user/sendCode.do?phone='+phone);
    }
});
