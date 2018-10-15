 //控制层 
app.controller('userController' ,function($scope,userService){
    $scope.register=function () {
        if($scope.entity.password!=$scope.password){
            alert("两次密码不一致！");
        }
        userService.register($scope.entity,$scope.encode).success(
            function (response) {
                if(response.success){
                    alert(response.message);
                    $scope.entity="";
                    $scope.password="";
                    $scope.encode="";
                }else {
                    alert(response.message);
                    $scope.entity="";
                    $scope.password="";
                    $scope.encode="";
                }
            }
        );
    }

    $scope.sendCode=function () {
        userService.sendCode($scope.entity.phone).success(
            function (response) {
                if(response.success){
                    alert(response.message);
                }else {
                    alert(response.message);
                }
            }
        );
    }
});	
