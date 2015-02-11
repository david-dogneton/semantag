/**
 * Created by Administrator on 06/04/14.
 */
var app = angular.module('app', []);

app.filter('fromNow', function () {
    return function (date) {
        moment.lang('fr');
        return moment(moment(date, 'YYYY-MM-DD HH:mm').format()).fromNow();
    }
});

function NewsRenderer($scope, $http) {

    $scope.news = {
        "items": []
    };



//    $http.get( { url : "get",
//        method : "GET",
//        params: { tonParam : saValeur }
//    }).
//        success(function(data, status){
//            // et c'est le GG
//        }).
//        error(function(data, status) {
//            // dommage
//        });

    $scope.limite = 4;





}