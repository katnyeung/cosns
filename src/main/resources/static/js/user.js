main.filter('hashTag', function() {
	return function(val) {
		return val.replace(/#(\S[^#^\n]*)/g, '<a href="/h/$1" class="badge badge-info">#$1</a> &nbsp;')
	};
});

main.factory('userService', function($http, $rootScope) {
	var shared = {
		messageService : null
	};

	return {
		login : function(email, password) {
			if (email) {
				return $http({
					url : "/user/login",
					method : "POST",
					headers : {
						'Content-Type' : 'application/json'
					},
					data : {
						'email' : email,
						'password' : password,

					}
				}).then(function(response) {
					if (response.data.status == 'success') {
						shared.messageService.setMessage(response.data.remarks);
						shared.messageService.setUser(response.data.user);
					} else {
						shared.messageService.setMessage(response.data.remarks);
					}
					return response;
				});
			}
		},
		logout : function() {
			return $http({
				url : "/user/logout",
				method : "GET",
				headers : {
					'Content-Type' : 'application/json'
				}
			}).then(function(response) {
				shared.messageService.setUser(response.data.user);

			});
		},
		register : function(email, password, passwordAgain) {
			return $http({
				url : "/user/register",
				method : "POST",
				headers : {
					'Content-Type' : 'application/json'
				},
				data : {
					'email' : email,
					'password' : password,
					'passwordAgain' : passwordAgain,
				}
			}).then(function(response) {
				if (response.data.status == 'success') {
					shared.messageService.setMessage(response.data.remarks);
				} else {
					shared.messageService.setMessage(response.data.remarks);
					shared.messageService.setMessage(response.data.remarks);
				}
				return response;
			});

		},
		setMessageService : function(messageService) {
			shared.messageService = messageService;
		},
		getShared : function() {
			return shared;
		}
	}
});