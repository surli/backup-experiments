angular.module('upsConsole')
  .controller('AppDetailController', function( $rootScope, $q, $routeParams, $modal, applicationsEndpoint, messageSenderEndpoint, metricsEndpoint, ContextProvider, Notifications ) {

    var self = this;

    this.app = null; // is retrieved in canActivate
    this.notifications = null; // is retrieved in canActivate
    this.tab = $routeParams.tab;

    this.contextPath = ContextProvider.contextPath();

    this.canActivate = function() {
      return $q.all([
        applicationsEndpoint.getWithMetrics({appId: $routeParams.app})
          .then(function( app ) {
            self.app = app;
            if ( !app.variants.length ) {
              self.tab = 'variants';
            }
          }),
        metricsEndpoint.fetchApplicationMetrics($routeParams.app, null, 1)
          .then(function( data ) {
            self.notifications = data.pushMetrics;
          })
      ]);
    };

    this.sendNotification = function() {
      $modal.open({
        templateUrl: 'dialogs/send-push-notification.html',
        controller: function( $scope, $modalInstance ) {

          $scope.app = self.app;

          // default message
          $scope.pushData = {
            
            'message': {
              'sound': 'default',
              'alert': '',
              'priority':'normal',
              'simple-push': 'version=' + new Date().getTime()
            },
            'criteria' : {}
          };

          $scope.send = function() {
            if ($scope.selectedVariants && $scope.selectedVariants.length > 0) {
              $scope.pushData.criteria.variants = $scope.selectedVariants;
            }
            if($scope.aliases) {
              $scope.pushData.criteria.alias = $scope.aliases.split(',');
            }
            if($scope.deviceTypes) {
              $scope.pushData.criteria.deviceType = $scope.deviceTypes.split(',');
            }
            if($scope.categories) {
              $scope.pushData.criteria.categories = $scope.categories.split(',');
            }

            messageSenderEndpoint( self.app.pushApplicationID, self.app.masterSecret ).send({}, $scope.pushData)
              .then(function() {
                self.app.$messageCount += 1;
                self.notifications.unshift({ submitDate: new Date().getTime() });
                $modalInstance.close();
                $rootScope.$broadcast('upsNotificationSent', $scope.pushData, $scope.app);
                Notifications.success('Notification was successfully sent');
              })
              .catch(function() {
                Notifications.error('Failed to sent notification');
              });
          };

          $scope.cancel = function() {
            $modalInstance.dismiss();
          };
        }
      });
    };

  });
