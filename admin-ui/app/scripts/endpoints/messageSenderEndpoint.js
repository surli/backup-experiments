'use strict';

var upsServices = angular.module('upsConsole');

upsServices.factory('messageSenderEndpoint', function ($resource, apiPrefix) {
  return function ( applicationID, masterSecret ) {
    var url = apiPrefix + 'rest/sender';
    var paramDefaults = {};
    var actions = {
      send: {
        method: 'POST',
        headers: {
          'aerogear-sender': 'AeroGear UnifiedPush Console',
          'Authorization': 'Basic ' + btoa(applicationID + ':' + masterSecret)
        }
      }
    };

    return $resource(url, paramDefaults, actions);
  };
});
