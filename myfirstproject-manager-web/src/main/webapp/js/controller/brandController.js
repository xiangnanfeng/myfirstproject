app.controller("brandController",function($scope,$controller,brandService){


    $controller("baseController",{$scope:$scope});//继承baseController.js

    //发送请求，查询所有品牌
    $scope.findAll= function () {
        brandService.findAll().success(
            function (response) {
                $scope.list=response;
            }
        );
    };

    //分页查询品牌，定义一个方法，携带两个参数 发送get请求，
    $scope.findPage = function (page,size) {
        brandService.findPage(page,size).success(
            function(response) {
                $scope.list=response.rows;
                $scope.paginationConf.totalItems=response.total;
            }
        );
    }

    //添加品牌方法
    $scope.save=function(){
        var object = null;
        if($scope.entity.id!=null){
            object=brandService.update($scope.entity);
        }else{
            object=brandService.add($scope.entity);
        }
        object.success(
            function (response) {
                if(response.success){
                    $scope.reloadList();
                }else{
                    alert(response.message);
                }
            }
        );
    }
    //添加修改品牌的方法
    $scope.findOne=function(id){
        brandService.findOne(id).success(
            function (response) {
                $scope.entity=response;
            }
        )
    }
    //根据存入数组的id传递到后端删除
    $scope.dele=function(){
        brandService.dele($scope.selectIds).success(
            function (response) {
                if(response.success){
                    $scope.reloadList();
                }else{
                    alert(response.message);
                }
            }
        );
    }
    //根据指定的输入数据进行搜索分页查询
    $scope.searchEntity={};
    $scope.search=function (page,size) {
        brandService.search(page,size,$scope.searchEntity).success(
            function(response) {
                $scope.list=response.rows;
                $scope.paginationConf.totalItems=response.total;
            }
        );
    }
});