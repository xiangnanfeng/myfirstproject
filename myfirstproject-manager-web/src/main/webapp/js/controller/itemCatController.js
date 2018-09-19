 //控制层 
app.controller('itemCatController' ,function($scope,$controller,itemCatService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中
	$scope.findAll=function(){
		itemCatService.findAll().success(
			function(response){
				$scope.list=response;
			}
		);
	}

	//分页
	$scope.findPage=function(page,rows){
		itemCatService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}
		);
	}

	//查询实体
	$scope.findOne=function(id){
		itemCatService.findOne(id).success(
			function(response){
				$scope.entity= response;
			}
		);
	}

	//保存
	$scope.save=function(){
		var serviceObject;//服务层对象
		if($scope.entity.id!=null){//如果有ID
			serviceObject=itemCatService.update( $scope.entity ); //修改
		}else{

			serviceObject=itemCatService.add( $scope.entity  );//增加
		}
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询
                    $scope.findByParentId($scope.entity.parentId);//重新查询
				}else{
					alert(response.message);
				}
			}
		);
	}


	//批量删除
	$scope.dele=function(){
		//获取选中的复选框
		itemCatService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}else{
					alert(response.message);
                    $scope.selectIds=[];
				}
			}
		);
	}

	$scope.searchEntity={};//定义搜索对象

	//搜索
	$scope.search=function(page,rows){
		itemCatService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}
		);
	}
	$scope.entity={};
	//根据父id查询产品
	$scope.findByParentId = function (parenId) {

		$scope.entity.parentId=parenId;

		itemCatService.findByParentId(parenId).success(function (response) {

			$scope.list = response;
        })
    }

    $scope.number=1;

	$scope.setNumber=function (num) {
		$scope.number=num;
    }

    $scope.queryItemCat=function (pojo) {
		if($scope.number==1){
			$scope.navigate1=null;
			$scope.navigate2=null;
		}
		if($scope.number==2){
            $scope.navigate1=pojo;
            $scope.navigate2=null;
		}
		if($scope.number==3){
            $scope.navigate2=pojo;
		}
        $scope.findByParentId(pojo.id);
    }
});	
