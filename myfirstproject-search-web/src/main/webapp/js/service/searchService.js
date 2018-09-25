app.service('searchService',function ($http) {
    this.searchItem=function (map) {
        return $http.post('search/searchItem.do',map);
    }
})