//服务层
app.service('contentService',function($http){
	    	
	//根据广告分类ID查询广告
	this.findContenByCategoryId=function(categoryId){
		return $http.get('content/categoryId.do?categegoryId='+categoryId);
	}
});
