'use strict';

var upsServices = angular.module('upsConsole');

upsServices.factory('dashboardEndpoint', function ($resource, apiPrefix) {
  return $resource( apiPrefix + 'rest/metrics/dashboard/:verb', {}, {
    totals: {
      method: 'GET'
    },
    warnings: {
      method: 'GET',
      isArray: true,
      params: {
        verb: 'warnings'
      }
    },
    latestActiveApps: {
      method: 'GET',
      isArray: true,
      params: {
        verb: 'active',
        count: 4
      }
    }
  });
});
