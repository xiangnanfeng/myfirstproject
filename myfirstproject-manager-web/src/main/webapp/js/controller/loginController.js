app.controller('loginController',function ($scope,loginService) {
    $scope.showUser=function () {
        loginService.showUser().success(
            function (response) {
                $scope.username=response.username;
            }
        );
    }
});