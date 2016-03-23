(function () {
  'use strict';

  angular
    .module('dashboardJsApp')
    .config(usersConfig);

  usersConfig.$inject = ['$stateProvider'];
  function usersConfig($stateProvider) {
    $stateProvider
      .state('tools.users', {
        url: '/users',
        templateUrl: 'app/users/users.html',
        access: {
          requiresLogin: true
        }
      });
  }
})();
