main.factory('messageService', function() {
	var shared = {
		dialogMessage : ''
	};

	var sharedUser = {
		user : null
	};

	return {
		getShared : function() {
			return shared;
		},
		getSharedUser : function() {
			return sharedUser;
		},
		setMessage : function(message) {
			shared.dialogMessage = message;
			return null;
		},
		setUser : function(user) {
			sharedUser.user = user;
			return null;
		}
	}
});

