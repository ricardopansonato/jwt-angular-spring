var appModule = angular.module('myApp', ['ngCookies']);

appModule.controller('MainCtrl', ['mainService','$scope','$http','$cookies',
        function(mainService, $scope, $http, $cookies) {
            $scope.greeting = 'Welcome to the JSON Web Token / AngularJR / Spring example!';
            $scope.token = null;
            $scope.error = null;
            $scope.roleUser = false;
            $scope.roleAdmin = false;
            $scope.roleFoo = false;

            $scope.login = function() {
                $scope.error = null;
                mainService.login($scope.userName).then(function(token) {
                    $scope.checkRoles();
                },
                function(error){
                    $scope.error = error
                });
            }

            $scope.checkRoles = function() {
                mainService.hasRole('user').then(function(user) {$scope.roleUser = user});
                mainService.hasRole('admin').then(function(admin) {$scope.roleAdmin = admin});
                mainService.hasRole('foo').then(function(foo) {$scope.roleFoo = foo});
            }

            $scope.logout = function() {
                delete $cookies["X-AUTH-TOKEN"];
            }

            $scope.loggedIn = function() {
                var authToken = $cookies["X-AUTH-TOKEN"];
                return authToken != null;
            }
        } ]);



appModule.service('mainService', function($http, $location) {
    return {
        login : function(username) {
            return $http.post('/user/login', {name: username}).then(function(response) {
                $location.path('/');
            });
        },

        hasRole : function(role) {
            return $http.get('/api/role/' + role).then(function(response){
                console.log(response);
                return response.data;
            });
        }
    };
});
