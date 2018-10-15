app.controller('itemController',function($scope,$http){
	
	$scope.number=1;
	
	$scope.addNumber=function(x){
		$scope.number+=x;
		if($scope.number<1){
			$scope.number=1;
		}
	}
	
	$scope.specification={};
	$scope.selectSpecification=function(key,value){
		$scope.specification[key]=value;
		$scope.searchSku();
	}
	
	$scope.isSelected=function(key,value){
		if($scope.specification[key]==value){
			return true;
		}
		return false;
	}
	
	$scope.skuItem={};
	$scope.initSpecification=function(){
		$scope.skuItem=ItemList[0];
		$scope.specification= JSON.parse(JSON.stringify($scope.skuItem.spec)) ;
	}
	
	$scope.compare=function(spec1,spec2){
		for(var key in spec1){
			if(spec1[key]!=spec2[key]){
				return false;
			}
		}
		for(var key in spec2){
			if(spec2[key]!=spec1[key]){
				return false;
			}
		}
		return true;
	}
	
	$scope.searchSku=function(){
		for(var i=0;i<ItemList.length;i++ ){
				if( $scope.compare(ItemList[i].spec ,$scope.specification )){
					$scope.skuItem=ItemList[i];
					return ;
				}
		}
        $scope.skuItem={id:0,title:'您访问的页面走丢了',price:0};//如果没有匹配的
	}

    $scope.addToCart=function(){
        $http.get('http://localhost:9107/cart/addItemToCartList.do?itemId='+$scope.skuItem.id+'&number='+$scope.number,{'withCredentials':true}).success(
        	function (response) {
				if(response.success){
                    location.href='http://localhost:9107/cart.html';
				}else {
					alert(response.message);
				}
            }
		);
    }
});