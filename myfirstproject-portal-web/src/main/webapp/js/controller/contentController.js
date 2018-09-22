 //控制层 
app.controller('contentController' ,function($scope,contentService){

    $scope.findContenByCategoryId=function (categoryId) {
        contentService.findContenByCategoryId(categoryId).success(
        	function (response) {
                $scope.contentList1=response;
            }
		);
    }

});	
