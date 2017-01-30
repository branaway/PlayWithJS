var auth = function() {
	return {

		/**
		 * login a user to display the login form, depending on the HTTP method
		 * path: http://localhost:9000/js/auth/login
		 */
		login : function(user_name, user_pwd) {
			if (request().method == "GET")
				return new File("public/login.html")
			else {
				if (user_name) {
					session().put("_user", user_name)
					return "welcome: " + user_name
				} else {
					return new File("public/login.html")
				}
			}
		},

		/**
		 * logout the current user path:
		 * http://localhost:9000/js/auth/logout
		 */
		logout : function() {
			session().put("_user", "")
			return "logout OK"
		},
	}
}();
