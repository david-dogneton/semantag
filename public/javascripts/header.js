var app = angular.module('app', []);

function Search($scope, $http) {
    var form = document.getElementById("search-form");

    $('.prompt').keyup(function(event){
        if(event.keyCode == 13){
            $(".search").addClass('loading');
        }
    });

    $('.validate').click(function(event){
        form.submit();
    });


/*    $("img").error(function(){
        $(this.parentNode).hide();
        $(this.parentNode).next().hide();
    });*/
/*
    $http.get('/entitiesTitles/').success(function (data) {
        alert('OKdddddddddddddddddddddddddddddd');

        $('.ui.search')
            .search({
                source: data.liste
            })
        ;
    }).error(function (err) {
            console.log("err : " + err);
        });*/
}
