/*
 * Created on Jul 26, 2015
 *
 * Copyright (c) 2004-2015 Peter Johan Salomonsen (http://www.petersalomonsen.com)
 *
 * http://www.frinika.com
 *
 * This file is part of Frinika.
 *
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
var controllers = angular.module('frinikaControllers', []);
   
controllers.controller("mainCtrl", ['$scope','$http','$location',function($scope,$http,$location) {
	
}]);

controllers.controller("midiSetupCtrl", ['$scope','$http','$location',function($scope,$http,$location) {
    $scope.refresh = function() {
	$http.get("restservices/mididevices/indevices").success(function(data) {
	    $scope.midiDeviceInfo = data;
	    $scope.midiInDevices = {};

	    for(var n=0;n<data.availableMidiInDevices.length;n++) {
		var md = data.availableMidiInDevices[n];
		$scope.midiInDevices[md] = {};
	    }

	    for(var n=0;n<data.currentMidiInDevices.length;n++) {
		var md = data.currentMidiInDevices[n];
		$scope.midiInDevices[md].connected = true;
	    }


	});        
    };
    
    $scope.reconnect = function() {
	$scope.midiDeviceInfo.currentMidiInDevices = [];
	for(var k in $scope.midiInDevices) {
	    if($scope.midiInDevices[k].connected) {
		$scope.midiDeviceInfo.currentMidiInDevices.push(k);
	    }
	}
	$http.post("restservices/mididevices/indevices",$scope.midiDeviceInfo).success(function() {
	    $scope.refresh();
	});
    };
    
    $scope.refresh();
}]);