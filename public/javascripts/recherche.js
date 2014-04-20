var app = angular.module('app', []);

app.filter('fromNow', function () {
    return function (date) {
        moment.lang('fr');
        return moment(moment(date, 'YYYY-MM-DD HH:mm').format()).fromNow();
    }
});

function GestionEntites($scope, $http) {

}


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


}