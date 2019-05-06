main.filter('hashTag', function() {
	return function(val) {
		return val.replace(/#(\S[^#^\n]*)/g, '<a href="/h/$1" class="badge badge-info">#$1</a> &nbsp;')
	};
});

main.factory('postService', function($http, $rootScope) {
	var shared = {
		messageService : null
	};

	return {
		likePost : function(post, from) {
			return $http({
				url : "/post/likePost",
				method : "POST",
				headers : {
					'Content-Type' : 'application/json'
				},
				data : {
					postId : post.postId
				}
			}).then(function(response) {
				if (response.data.status === 'success') {
					if (response.data.type === 'incr') {
						post.likeCount++;
						post.liked = true;
					} else if (response.data.type === 'decr') {
						post.likeCount--;
						post.liked = false;
					}

					shared.messageService.setMessage(response.data.remarks);
					$rootScope.$broadcast('postUpdate', from);

				}
				return response;
			});
		},
		retweetPost : function(post, from) {
			return $http({
				url : "/post/retweetPost",
				method : "POST",
				headers : {
					'Content-Type' : 'application/json'
				},
				data : {
					postId : post.postId
				}
			}).then(function(response) {
				if (response.data.status === 'success') {
					post.retweetCount++;
					post.retweeted = true;

					shared.messageService.setMessage(response.data.remarks);
					$rootScope.$broadcast('postUpdate', from);

				}
				return response;
			});
		},
		removePost : function(post, from) {
			return $http({
				url : "/post/removePost",
				method : "POST",
				headers : {
					'Content-Type' : 'application/json'
				},
				data : {
					postId : post.postId
				}
			}).then(function(response) {
				if (response.data.status === 'success') {
					post.removed = true;

					shared.messageService.setMessage(response.data.remarks);
					$rootScope.$broadcast('postUpdate', from);
				}
				return response;
			});
		},
		loadPosts : function(functionName, callback) {
			$http({
				url : "/post/" + functionName,
				method : "GET",
				headers : {
					'Content-Type' : 'application/json'
				}
			}).then(function(response) {
				if (response.data.status === 'success') {
					if (response.data.postList && response.data.postList.length > 0) {
						callback(response.data.postList);
					} else {
						callback([]);
					}
				} else {
					share.messageService.setMessage(response.data.remarks);
				}

			});
		},
		searchPosts : function(keyword, orderByType, callback) {
			if (keyword) {
				$http({
					url : "/post/searchPosts",
					method : "POST",
					headers : {
						'Content-Type' : 'application/json'
					},
					data : {
						keyword : keyword,
						orderBy : orderByType
					}
				}).then(function(response) {
					if (response.data.status === 'success') {
						if (response.data) {
							callback(response.data);
						} else {
							callback([]);
						}
					} else {
						share.messageService.setMessage(response.data.remarks);
					}

				});
			}
		},
		setMessageService : function(messageService) {
			shared.messageService = messageService;
		},
		getShared : function() {
			return shared;
		}
	}
});