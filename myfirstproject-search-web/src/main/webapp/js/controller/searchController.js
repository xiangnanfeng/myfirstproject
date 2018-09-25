app.controller('searchController',function ($scope,searchService) {

    $scope.searchMap={};

    $scope.responseMap={};
    $scope.searchItem=function () {
        searchService.searchItem($scope.searchMap).success(
            function (response) {
                $scope.responseMap.rows=response.rows;
            }
        );
    }
});