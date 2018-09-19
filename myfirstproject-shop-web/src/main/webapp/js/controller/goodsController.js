//控制层
app.controller('goodsController', function ($scope, $controller, goodsService, uploadService, itemCatService, typeTemplateService, $location) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        goodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        goodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function () {
        var id = $location.search()['id'];
        if (id != null) {
            goodsService.findOne(id).success(
                function (response) {
                    $scope.entity = response;
                    editor.html(response.goodsDesc.introduction);
                    $scope.entity.goodsDesc.itemImages = JSON.parse(response.goodsDesc.itemImages);
                    $scope.entity.goodsDesc.customAttributeItems = JSON.parse(response.goodsDesc.customAttributeItems);

                }
            );
        }
    }
    $scope.entity = {goods: {}, goodsDesc: {itemImages: [], specificationItems: []}};

    //保存
    $scope.add = function () {
        $scope.entity.goodsDesc.introduction = editor.html();
        goodsService.add($scope.entity).success(
            function (response) {
                if (response.success) {
                    //重新查询
                    alert(response.message);//
                    $scope.entity = {goods: {}, goodsDesc: {}};
                    editor.html('');
                } else {
                    alert(response.message);
                }
            }
        );
    }


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                    $scope.selectIds = [];
                } else {
                    alert(response.message);
                    $scope.selectIds = [];
                }
            }
        );
    }

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }
    //定义一个数组，用来存储商品状态，其角标和返回的数字相同
    $scope.array = ['未审核', '已审核', '已驳回', '关闭'];
    $scope.image = {};
    //上传图片方法
    $scope.upLoadFile = function () {
        uploadService.uploadFile().success(
            function (response) {
                if (response.success) {
                    $scope.image.url = response.message;
                    alert(response.message);
                } else {
                    alert(response.message);
                }
            }
        );
    }
    //添加图片
    $scope.addImage = function () {
        $scope.entity.goodsDesc.itemImages.push($scope.image);
    }
    //删除图片
    $scope.deleImage = function ($index) {
        $scope.entity.goodsDesc.itemImages.splice($index, 1);
    }
    //在itemcat表中查询父id是0的类别
    $scope.queryItemCat1List = function () {
        itemCatService.findByParentId(0).success(
            function (response) {
                $scope.itemCat1List = response;
            }
        );
    }
    //定义一个检测变量变化的方法，当entity.goods.category1Id发生变化时执行此方法
    $scope.$watch('entity.goods.category1Id', function (newValue, oldValue) {
        if (newValue != undefined) {
            itemCatService.findByParentId(newValue).success(
                function (response) {
                    $scope.itemCat2List = response;
                }
            );
            if ($location.search()['id'] == null) {
                $scope.entity.goods.category3Id = undefined;
            }
            $scope.entity.goods.typeTemplateId = undefined;
        }
    });
    //定义一个检测变量变化的方法，当entity.goods.category2Id发生变化时执行此方法
    $scope.$watch('entity.goods.category2Id', function (newValue, oldValue) {
        if (newValue != undefined) {
            itemCatService.findByParentId(newValue).success(
                function (response) {
                    $scope.itemCat3List = response;
                }
            );
        }

    });
    //定义一个检测变量变化的方法，当entity.goods.category3Id发生变化时执行此方法
    $scope.$watch('entity.goods.category3Id', function (newValue, oldValue) {
        if (newValue != undefined) {
            //当entity.goods.category3Id变量发生变化时执行findOne方法查询类别
            itemCatService.findOne(newValue).success(
                function (response) {
                    $scope.entity.goods.typeTemplateId = response.typeId;
                    //当entity.goods.category3Id变量发生变化时执行findOne方法查询模板
                    typeTemplateService.findOne(response.typeId).success(
                        function (response) {
                            $scope.brandIds = JSON.parse(response.brandIds);
                            if ($location.search()['id'] == null) {

                                $scope.entity.goodsDesc.customAttributeItems = JSON.parse(response.customAttributeItems);
                            }
                        }
                    );
                }
            );
        }
    });
    //当entity.goods.typeTemplateId的值发生变化时去查询规格
    $scope.$watch('entity.goods.typeTemplateId', function (newValue, oldValue) {

        if (newValue != undefined) {
            typeTemplateService.findSpecList(newValue).success(
                function (response) {
                    $scope.specList = response;
                }
            );
        }
    });
    $scope.updateSpecAttribute = function ($event, name, value) {
        var object = $scope.searchObjectByKey(
            $scope.entity.goodsDesc.specificationItems, 'attributeName', name);
        if (object != null) {
            if ($event.target.checked) {
                object.attributeValue.push(value);
            } else {//取消勾选
                object.attributeValue.splice(object.attributeValue.indexOf(value), 1);//移除选
                项
//如果选项都取消了，将此条记录移除
                if (object.attributeValue.length == 0) {
                    $scope.entity.goodsDesc.specificationItems.splice(
                        $scope.entity.goodsDesc.specificationItems.indexOf(object), 1);
                }
            }
        } else {
            $scope.entity.goodsDesc.specificationItems.push(
                {"attributeName": name, "attributeValue": [value]});
        }
    }

    $scope.createItemList = function () {
        $scope.entity.itemList = [{spec: {}, price: 0, num: 99999, status: '0', isDefault: '0'}]
        ;//初始
        var items = $scope.entity.goodsDesc.specificationItems;
        for (var i = 0; i < items.length; i++) {
            $scope.entity.itemList =
                addColumn($scope.entity.itemList, items[i].attributeName, items[i].attributeValue);
        }
    }
//添加列值
    addColumn = function (list, columnName, conlumnValues) {
        var newList = [];//新的集合
        for (var i = 0; i < list.length; i++) {
            var oldRow = list[i];
            for (var j = 0; j < conlumnValues.length; j++) {
                var newRow = JSON.parse(JSON.stringify(oldRow));//深克隆
                newRow.spec[columnName] = conlumnValues[j];
                newList.push(newRow);
            }
        }
        return newList;
    }
    //查询所有商品分类，用于在在商品管理上显示商品分类

    $scope.ItemCatList = [];//定义一个数组，将查询到的分类全部存储在里面，然后再一一查询对应

    $scope.findItemCatList = function () {
        itemCatService.findAll().success(
            function (response) {
                for (var i = 0; i < response.length; i++) {
                    $scope.ItemCatList[response[i].id] = response[i].name;
                }
            }
        );
    }

});	
