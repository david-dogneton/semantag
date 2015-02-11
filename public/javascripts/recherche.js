var app = angular.module('app', []);

app.filter('fromNow', function () {
    return function (date) {
        moment.lang('fr');
        return moment(moment(date, 'YYYY-MM-DD HH:mm').format()).fromNow();
    }
});

function GestionEntites($scope, $http) {
    $scope.gererCoeurEntite = function ($mailUser, $urlEntite, $idEntite) {
        $http.post("/enregistrerLikeEntite", {mailUser: $mailUser, urlEntite: $urlEntite});
        if ($('#' + $idEntite).attr('class').indexOf('cliquee') > -1) {
            $('#' + $idEntite).removeClass('cliquee')
        }
        else {
            $('#' + $idEntite).addClass('cliquee')
        }
    }
}

function GestionArticles($scope, $http) {


    $scope.gererCoeurArticle = function ($mailUser, $urlArticle, $idArticle) {
        $http.post("/changerCoeur", {mailUser: $mailUser, urlArticle: $urlArticle});
        if ($('#' + $idArticle).attr('class').indexOf('cliquee') > -1) {
            $('#' + $idArticle).removeClass('cliquee')
        }
        else {
            $('#' + $idArticle).addClass('cliquee')
        }
    }
}



function NewsRenderer($scope, $http) {

    $scope.tops = {
        "items": [
        ]
    }

    $http.get('/getTop').success(function (data) {
        console.dir(data);
        $scope.tops.items = data.liste;
    }).error(function (err) {
            console.log("err : " + err);
        });


}