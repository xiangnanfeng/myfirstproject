app.controller('searchController',function ($scope,$location,searchService) {

    $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','currentPage':1,'pageSize':20,'sortField':'','sort':''};
    //每次搜索都将搜索项复原，不然会产生影响
    $scope.clear=function () {

       $scope.searchMap.category='';
       $scope.searchMap.brand='';
       $scope.searchMap.spec={};
       $scope.searchMap.currentPage=1;
       $scope.searchMap.price='';
       $scope.searchMap.sortField='';
       $scope.searchMap.sort='';
    }

    //搜索商品
    $scope.searchItem=function () {
        searchService.searchItem($scope.searchMap).success(
            function (response) {
                $scope.responseMap=response;
                $scope.showPage=[];
                $scope.buildShowPage();
            }
        );
    }

    $scope.findBrand=function () {

        var brandList = $scope.responseMap.brandList;

        for(var i=0;i<brandList.length;i++){

            if($scope.searchMap.keywords.indexOf(brandList[i].text)>=0){
                return true;
            }
        }
        return false;
    }

    //页面上搜索指定页面时，调此方法
    $scope.pageFind=function (currentPage) {
        currentPage = parseInt(currentPage);
        if(currentPage<1 || currentPage>$scope.responseMap.totalPages){
            return;
        }
        $scope.searchMap.currentPage=currentPage;
        $scope.searchItem();
    }
    $scope.showPage=[];//构建前端要显示的页码
    //构建分页栏
    $scope.buildShowPage=function () {
        $scope.firstPage=1;
        $scope.lastPage=5;

        if($scope.responseMap.totalPages.length<=5){
            $scope.firstPage=1;
            $scope.lastPage=$scope.responseMap.totalPages;
        }else{
            if($scope.searchMap.currentPage>$scope.responseMap.totalPages-2){
                $scope.firstPage=$scope.responseMap.totalPages-5
                $scope.lastPage=$scope.responseMap.totalPages;
            }else if($scope.searchMap.currentPage<3){
                $scope.firstPage=1;
                $scope.lastPage=5;
            }else {
                $scope.firstPage=$scope.searchMap.currentPage-2;
                $scope.lastPage=$scope.searchMap.currentPage+2;
            }

        }
        for(var i=$scope.firstPage;i<=$scope.lastPage;i++){
            $scope.showPage.push(i)
        }
    }



    //添加搜索项，将键值都传递过来，
    $scope.addSearchOption=function (key, value) {
        if(key=='category' || key=='brand' || key=='price'){//判断，如果键是category或者brand就往里面存子
            $scope.searchMap[key]=value;
        }else {
            $scope.searchMap.spec[key]=value;//否则说明添加的是规格，则往规格里面存值
        }
        $scope.searchItem();
    }
    //移除搜索项
    $scope.remSearchOption=function (key) {
        if(key=='category' || key=='brand' ||key=='price'){
            $scope.searchMap[key]='';
        }else {
            delete $scope.searchMap.spec[key];
        }
        $scope.searchItem();
    }

    //根据价格排序查询查询
    $scope.sort=function (fieldName, sort) {
        $scope.searchMap.sortField=fieldName;
        $scope.searchMap.sort=sort;
        $scope.searchItem();
    }
    
    //当从网站首页跳转过来的时候从地址栏获取参数
    $scope.getParameter=function () {
        $scope.searchMap.keywords=$location.search()["searchContext"];//获取地址栏参数需要在控制层注入$location服务
        $scope.searchItem();
    }
});