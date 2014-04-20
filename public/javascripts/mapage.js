var app = angular.module('app', []);

app.filter('fromNow', function () {
    return function (date) {
        moment.lang('fr');
        return moment(moment(date, 'YYYY-MM-DD HH:mm').format()).fromNow();
    }
});

function NewsRenderer($scope, $http) {
    $scope.afficher = 1;

    $scope.gererCoeur = function ($mailUser, $urlArticle) {
        $http.post("/changerCoeur", {mailUser: $mailUser, urlArticle: $urlArticle});
        var rank = 0;
        var currentRank = 0;
        for (var item in $scope.news["items"]) {
            if ($scope.news["items"][item].url == $urlArticle) rank = currentRank;
            currentRank++;
        }
        if(($scope.news["items"][rank])['coeurPresent'] == 0) ($scope.news["items"][rank])['coeurPresent'] = 1;
        else ($scope.news["items"][rank])['coeurPresent'] = 0;
    }

    $scope.enregistrerLecture = function ($mailUser, $urlArticle) {
        console.log("URL de l'article lu : " + $urlArticle);
        $http.post("/enregistrerLecture", {mailUser: $mailUser, urlArticle: $urlArticle});
    }

    $scope.news = {
        "items": [

        ]
    };


    $http.get('/getArtRecommandes').success(function (data) {
        console.log("data : " + data);
        console.dir(data);
        $scope.news.items = data.liste;

    }).error(function (err) {
            console.log("err : " + err);
        });
    $scope.limite = 7;
    $scope.filtrage = [];
    $scope.domaines = {
        "items": [
        ]
    }
    $scope.filtrage['domaine'] = "";

    $http.get('/getDomaines').success(function (data) {
        console.dir(data);
        $scope.domaines.items = data.liste;
    }).error(function (err) {
            console.log("err : " + err);
        });





}