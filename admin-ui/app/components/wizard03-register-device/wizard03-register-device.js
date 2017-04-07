angular.module('upsConsole')
  .controller('Wizard03RegisterDeviceController', function( $rootScope, $router, variantModal, createAppWizard, appModal, $timeout, $interval, applicationsEndpoint ) {

    this.canActivate = function() {
      if ( !createAppWizard.app ) {
        $rootScope.$broadcast('upsNavigate', '/wizard/create-app');
        return false;
      }
      if ( !createAppWizard.variant ) {
        $rootScope.$broadcast('upsNavigate', '/wizard/add-variant');
        return false;
      }
      return true;
    };

    var self = this;

    this.app = createAppWizard.app;
    this.variant = createAppWizard.variant;

    this.addVariant = function() {
      return variantModal.add()
        .then(function( variant ) {
          console.log('success');
          console.log(variant);
          createAppWizard.variant = variant;
          $rootScope.$broadcast('upsNavigate', '/wizard/register-device');
        })
        .catch(function() {
          console.log('error');
        })
    };

    this.editAppName = function() {
      var appClone = angular.extend( {}, self.app );
      appModal.editName( appClone )
        .then(function( updatedApp ) {
          angular.extend( self.app, updatedApp );
        });
    };

    this.editVariant = function() {
      var variantClone = angular.extend({}, self.variant);
      return variantModal.edit( self.app, variantClone )
        .then(function( updatedVariant ) {
          angular.extend(self.variant, updatedVariant);
        });
    };

    function detectInstallations() {
      return applicationsEndpoint.getWithMetrics({appId: createAppWizard.app.pushApplicationID})
        .then(function( data ) {
          return data.$deviceCount > 0;
        })
        .then(function( installationDetected ) {
          if ( installationDetected ) {
            $rootScope.$broadcast('upsNavigate', '/wizard/send-push-notification');
          }
        });
    }

    var intervalForDetectInstallations;

    this.activate = function() {
      $timeout(function() { // timeout is a workaround for bug in the router - canDeactivate is called right after activate
        intervalForDetectInstallations = $interval(function () {
          detectInstallations().then(function (installationDetected) {
            if (installationDetected) {
              $interval.cancel(intervalForDetectInstallations);
            }
          });
        }, 1500);
      }, 500);
    };

    this.canDeactivate = function() {
      if (intervalForDetectInstallations) {
        $interval.cancel(intervalForDetectInstallations);
      }
      return true;
    };

  });


