app.service('loginService',function ($http) {
    this.showUser=function () {
        return $http.get('../loginUser/showUser.do');
    }
});