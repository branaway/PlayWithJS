'use strict';
load('js/fun.js');

// the name must match that of this source file
var books = function() {
	return {
		
		/**
		 * the default function
		 */
		index: function() {
			return new File(__FILE__) // return the current file
		},
		
		/**
		 * path: http://localhost:9000/js/books/all
		 */
		all : function() {
			var books = Book.find("order by year", []).fetch();
			return books;
		},

		/**
		 * path: http://localhost:9000/js/books/some?howMany=3
		 */
		some : function(howMany) {
			var jpaQuery = JPA.find(Book.class);
			if (howMany) {
				var books = jpaQuery.fetch(howMany)
				return {
					errorcode : 0,
					data : books
				}
			} else {
				return {
					errorcode : 1,
					msg : "howMany is not defined"
				}
			}
		},

		/**
		 * path: http://localhost:9000/js/books/getBookById?id=3
		 */
		getBookById : function(id) {
			// return foo(id); // 可以调用 ‘js/fun.js' 定义的函数
			var book = Book.findById(id);
			if (book)
				 return renderJapid(0, book); 
//				 the template is"japidroot/japidviews/js/books/getBookById.html"
//				return book;
//				return id;
			else
				return "ooops!";
		},
		
		/**
		 * path: http://localhost:9000/js/books/newBook?title=My Book&year=1966
		 * or use curl: curl --form title="My Book" --form year="1966" http://localhost:9000/js/books/newBook
		 */
		newBook : function (title, year) {
			var book = new Book();
			if (title)
				book['title'] = title; // set on an attribute
			if (year)
				book.year = year; // as direct javabean

			book.save();

			return book;
		},
		
		/**
		 * sample path: 
		 * http://localhost:9000/js/books/newBook2?jsonData={"title":"my book", "year":1999}
		 * or use curl: curl --form title="My Book" --form year="1966" http://localhost:9000/js/books/newBook
		 */
		newBook2 : function (jsonData) {
			var book = new Book();
			var json = JSON.parse(jsonData)
			for (var p in json) {
				book[p] = json[p]
			}

			book.save();
			
			return book;
		},
		
		/**
		 * path: http://localhost:9000/js/books/getFile?name=/js/books.js
		 */
		getFile : function(name) {
			return new File(name)
		},
		
		/**
		 * path: http://localhost:9000/js/books/jpa?year=1956
		 */
		jpa: function(year) {
			// the findBy return all data immediately
			var books = JPA.findBy(Book.class, "select title, year, rank from Book where year > ?1 order by year", year);
			return books;
		},

		/**
		 * path: http://localhost:9000/js/books/jpa2?fromRow=1&maxResults=2
		 */
		jpa2: function(fromRow, maxResults) {
			// the find return a query which can be set limit the returned data
			var query = Book.find("select title, author.name from Book", []);
			// the above is equivalent to:
			//			var query = JPA.find(Book.class, "select title, author.name from Book");
			return query.from(fromRow).fetch(maxResults);
		},
		
		/**
		 * path: http://localhost:9000/js/books/jpa3
		 */
		jpa3: function() {
			// would generate the all the combination of the two field values
			return JPA.findBy(Book.class, "select b.title, c.name from Book b, Contact c");
		},
		
		now: function() {
			return new Date()
		},
		
		/**
		 * let's render a value in Japid template. 
		 * the template is "japidroot/japidviews/js/books/japid.html"
		 */
		japid: function() {
			var book = books.getBookById(new java.lang.Long(1))
			return renderJapid(book) 
		},
		
		
		
		/**
		 * let's render a value in Play's default Groovy based template. 
		 * The view is at: "app/views/js/books/groovy.html"
		 */
		groovy: function() {
			var book = Book.findById(new java.lang.Long(1))
			return render({book: book}) // the argument must a map
		},
		
		
		/**
		 * demo the require feature
		 * path: http://localhost:9000/js/books/toRequire
		 */
		toRequire: function (){
			print("--------------")
			var math = require("./maths.js")
			print("|||" + math.qqq())
			var sum = math.add(1, 2)
			print(sum)

			var say = require("./hello.js")
			say.sayit("me");
			return "OK";
		},
		
		/**
		 * login a user to display the login form, depending on the HTTP method
		 * path: http://localhost:9000/js/books/login
		 */
		login: function(user_name, user_pwd){
			if (request.method == "GET")
				return new File("public/login.html")
			else {
				if (user_name) {
					session.put("_user", user_name)
					return "welcome: " + user_name
				}
				else {
					return new File("public/login.html")
				}
			}
		},
	
		/**
		 * logout the current user
		 * path: http://localhost:9000/js/books/logout
		 */
		logout: function(){
			session.put("_user", "")
			return "logout OK"
		},
		
		
		/**
		 * upload a file
		 * path: http://localhost:9000/js/books/upload
		 */
		upload: function(file, comment) {
			if (request.method == "GET")
				return new File("public/upload.html")
			else {
				// the file should have been mapped to the upload
//				var file = getUploadedFile(fileName);
//				var file = JavaUtils.bindFile(fileName);
				if (file.exists()){
					var r = {name: file.getName(), size: file.length(), comment: comment}
					file.delete();
					return r;
				}
				else {
					return "could not find the uploaded file..."
				}
			}
		},
		/**
		 * 特殊interceptor function， 在真正函数调用之前被调用。 常用于安全检查。 如果欲立刻返回给用户错误信息
		 * 可以简单粗暴返回 return Forbidden()。 也可以直接调用另外的函数， 例如 login（）。
		 * 如果不想中断目标函数的访问，不要调用任何 return 语句。
		 */
		_before : function(functionName, args) {
			if (functionName == "all") {
				var u = session.get("_user")
				print("current user: " + u)
				if (!u){
					return redirect("books.login", {a: "1", b:"2"}) // note the redirect function use with some parameters
//					return books.login() // note direct call
				}
			}
		}
	}
}();
