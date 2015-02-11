var app = angular.module('app', ['ngSanitize']);

app.filter('fromNow', function () {
    return function (date) {
        moment.lang('fr');
        return moment(moment(date, 'YYYY-MM-DD HH:mm').format()).fromNow();
    }
});


function NewsRenderer($scope, $http) {

    $scope.tops = {
        "items": [
        ]
    }

    $http.get('/getTop').success(function (data) {
        console.dir(data);
        $scope.tops.items=data.liste;
    }).error(function (err) {
            console.log("err : " + err);
        });

    $("img").error(function(){
        $(this.parentNode).hide();
        $(this.parentNode).next().hide();
    });
}