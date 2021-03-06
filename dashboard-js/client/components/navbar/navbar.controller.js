(function () {
  'use strict';

  angular
    .module('dashboardJsApp')
    .controller('NavbarCtrl', navbarCtrl);

  navbarCtrl.$inject = ['$scope', '$rootScope', '$location', 'Auth', 'envConfigService', 'iGovNavbarHelper', 'tasksSearchService',
    '$state', 'tasks', 'lunaService', 'Modal', '$stateParams', 'processes', '$localStorage', 'signDialog'];
  function navbarCtrl($scope, $rootScope, $location, Auth, envConfigService, iGovNavbarHelper, tasksSearchService,
                      $state, tasks, lunaService, Modal, $stateParams, processes, $localStorage, signDialog) {
    $scope.menu = [{
      'title': 'Задачі',
      'link': '/tasks'
    }];

    envConfigService.loadConfig(function (config) {
      iGovNavbarHelper.isTest = config.bTest;
    });

    $scope.isAdmin = Auth.isAdmin;
    $scope.areInstrumentsVisible = false;
    $scope.iGovNavbarHelper = iGovNavbarHelper;

    $scope.isVisible = function(menuType){
      //$scope.menus = [{
      if(menuType === 'all'){
        return Auth.isLoggedIn() && Auth.hasOneOfRoles('manager', 'admin', 'kermit');
      }
      if(menuType === 'finished'){
        return Auth.isLoggedIn() && Auth.hasOneOfRoles('manager', 'admin', 'kermit', 'supervisor');
      }
      return Auth.isLoggedIn();
    };

    $scope.isVisibleInstrument = function(menuType){
      if(menuType === 'tools.users'){
        return Auth.isLoggedIn() && Auth.hasOneOfRoles('superadmin');
      }
      if(menuType === 'tools.groups'){
        return Auth.isLoggedIn() && Auth.hasOneOfRoles('superadmin');
      }
      return Auth.isLoggedIn();
    };

    $scope.getCurrentUserName = function() {
      var user = Auth.getCurrentUser();
      return user.firstName + ' ' + user.lastName;
    };

    $scope.goToServices = function() {
      $location.path('/services');
    };

    $scope.goToEscalations = function() {
      $location.path('/escalations');
    };

    $scope.goToReports = function () {
      $location.path('/reports');
    };

    $scope.logout = function() {
      Auth.logout();
      $location.path('/login');
    };

    $scope.isActive = function(route) {
      return route === $location.path();
    };

    $scope.goToUsers = function () {
      $location.path('/users');
    };

    $scope.goToGroups = function () {
      $location.path('/groups');
    };

    $scope.goToDeploy = function () {
      $location.path('/deploy');
    };

    $scope.goToProfile = function () {
      $location.path('/profile');
    };

    var bSelectedTasksSortReverse = false;

    $scope.$on('set-sort-order-reverse-true', function () {
      bSelectedTasksSortReverse = true;
    });

    $scope.$on('set-sort-order-reverse-false', function () {
      bSelectedTasksSortReverse = false;
    });

    $scope.tasksSearch = iGovNavbarHelper.tasksSearch;
    var tempCountValue = 0;

    $scope.searchInputKeyup = function ($event) {
      if ($event.keyCode === 13 && $scope.tasksSearch.value) {
        $scope.tasksSearch.loading=true;
        $scope.tasksSearch.count=0;
        $scope.tasksSearch.submited=true;
        if($scope.tasksSearch.archive) {
          tasks.getProcesses($scope.tasksSearch.value).then(function (res) {
            var response = JSON.parse(res);
            $scope.archive = response[0];
            $scope.archive.aVisibleAttributes = [];
            angular.forEach($scope.archive.aAttribute, function (oAttribute) {
              if (oAttribute.oAttributeName.nOrder !== -1){
                $scope.archive.aVisibleAttributes.push(oAttribute);
              }
            });
            $scope.archive.aVisibleAttributes.sort(function (a, b) {
              return a.oAttributeName.nOrder - b.oAttributeName.nOrder;
            });
            $scope.switchArchive = true;
          })
        } else {
          tasksSearchService.searchTaskByUserInput($scope.tasksSearch.value, $scope.iGovNavbarHelper.currentTab, bSelectedTasksSortReverse)
            .then(function(res) {
              if(res.aIDs.length > 1){
                if(bSelectedTasksSortReverse){
                  tempCountValue = (res.aIDs.length - res.nCurrentIndex) + ' / ' + res.aIDs.length;
                } else {
                  tempCountValue = (res.nCurrentIndex + 1) + ' / ' + res.aIDs.length;
                }
                $scope.tasksSearch.count = '... / ' + res.aIDs.length;
              } else {
                tempCountValue = res.aIDs.length;
                $scope.tasksSearch.count = res.aIDs.length;
              }
            })
            .finally(function(res) {
              $scope.tasksSearch.loading=false;
            });
        }
      }
      if($event.keyCode === 8 || $event.keyCode === 46) {
        $scope.switchArchive = false;
      }
    };

    $scope.$on('update-search-counter', function () {
      $scope.tasksSearch.count = tempCountValue;
    });

    $scope.closeArchive = function () {
      $scope.switchArchive = false;
    };

    $scope.archiveTextValue = function () {
      return isNaN($scope.tasksSearch.value);
    };

    $scope.isSelectedInstrumentsMenu = function(menuItem) {
      return menuItem.state==$state.current.name;
    };

    $scope.assignTask = function (id) {

      tasks.assignTask(id, Auth.getCurrentUser().id)
        .then(function (result) {
          /*Modal.assignDocument(function (event) {

          }, 'Документ успiшно створено');*/
        })
        .catch(function (e) {
          /*Modal.assignDocument(function (event) {

          }, 'Документ успiшно створено');*/
        });
    };

    $scope.usersDocumentsBPs = [];
    $scope.showOrHideSelect = {show:false,type:''};
    $scope.hasDocuments = function () {
      var user = Auth.getCurrentUser().id;
      if(user) {
        tasks.isUserHasDocuments(user).then(function (res) {
          if(Array.isArray(res) && res.length > 0) {
            $scope.usersDocumentsBPs = res.filter(function (item) {
              return item.oSubjectRightBP.sID_BP.charAt(0) === '_' && item.oSubjectRightBP.sID_BP.split('_')[1] === 'doc';
            });
            $scope.userTasksBPs = res.filter(function (item) {
              return item.oSubjectRightBP.sID_BP.indexOf('_doc_') !== 0;
            })
          }
        })
      }
    };
    $scope.hasDocuments();

    $scope.document = {};
    $scope.openCloseUsersSelect = function (type) {
      $scope.showOrHideSelect.type = type;
      $scope.showOrHideSelect.show = !$scope.showOrHideSelect.show;
    };

    $scope.showCreateDocButton = function () {
      return $stateParams.type === "unassigned" || $stateParams.type === "selfAssigned" || $stateParams.type === 'documents';
    };

    $scope.hideNaviWhenLoginPage = function () {
      return $location.path() === '/';
    };

    $scope.onSelectDocument = function (item) {
      tasks.createNewDocument(item.oSubjectRightBP.sID_BP).then(function (res) {
        if(res.snID_Process) {
          tempCountValue = 0;
          var val = res.snID_Process + lunaService.getLunaValue(res.snID_Process);
          tasksSearchService.searchTaskByUserInput(val, 'documents')
            .then(function(res) {
              $scope.assignTask(res.aIDs[0], val)
            });
          $scope.showOrHideSelect.show = false;
        }
      });
    };

    $scope.onSelectTask = function (task) {
      tasks.createNewTask(task.oSubjectRightBP.sID_BP).then(function (res) {
        localStorage.setItem('creating', JSON.stringify(res.data[0]));
        $state.go('tasks.typeof.create', {id:res.data[0].deploymentId});
        $scope.showOrHideSelect.show = false;
      })
    };

    function setEcpStatusToLS(status) {
      var stringifyStatus = JSON.stringify(status);
      localStorage.setItem('auto-ecp-status', stringifyStatus);
    }

    var ecpStatusInLS = localStorage.getItem('auto-ecp-status');

    if(ecpStatusInLS !== null) {
      $rootScope.checkboxForAutoECP = JSON.parse(ecpStatusInLS);
    }else {
      $rootScope.checkboxForAutoECP = {status : true};
      setEcpStatusToLS($rootScope.checkboxForAutoECP);
    }

    $scope.$watch('checkboxForAutoECP.status', function (newVal) {
      var savedStatus = localStorage.getItem('auto-ecp-status');
      var res = savedStatus !== null ? JSON.parse(savedStatus) : null;
      if(res && newVal !== undefined && res.status !== newVal) {
        setEcpStatusToLS($rootScope.checkboxForAutoECP);
      }
    });

    $scope.showSignDialog = function () {
      signDialog.signContent({id: "someid", content: "sign this string"}, function (signedContent) {
        console.log('Sign Result ' + JSON.stringify(signedContent));
      }, function () {
        console.log('Sign Dismissed');
      })
    }

  }
})();
