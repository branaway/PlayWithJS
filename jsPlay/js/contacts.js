var contacts = function() {
	return {
		
		/**
		 * the default function
		 */
		index: function() {
			return new File(__FILE__) // return the current file
		},
		
		/**
		 * path: http://localhost:9000/js/contacts/all
		 */
		all : function() {
			var contacts = Contact.find("order by birthdate", []).fetch();
			return contacts;
		},

		/**
		 * path: http://localhost:9000/js/contacts/some?howMany=3
		 */
		some : function(howMany) {
			var jpaQuery = JPA.find(Contact.class);
			if (howMany) {
				var contacts = jpaQuery.fetch(howMany)
				return {
					errorcode : 0,
					data : contacts
				}
			} else {
				return {
					errorcode : 1,
					msg : "howMany is not defined"
				}
			}
		},

		/**
		 * path: http://localhost:9000/js/contacts/getContactById?id=3
		 */
		getContactById : function(id) {
			// return foo(id); // 可以调用 ‘js/fun.js' 定义的函数
			var contact = Contact.findById(id);
			if (contact)
				return contact;
			else
				return {err: "could not find a contact by the id"};
		},
		
		/**
		 * path: http://localhost:9000/js/contacts/newContact?title=My Contact&year=1966
		 * or use curl: curl --form title="My Contact" --form year="1966" http://localhost:9000/js/contacts/newContact
		 */
		newContact : function (title, year) {
			var contact = new Contact();
			if (title)
				contact['title'] = title; // set on an attribute
			if (year)
				contact.year = year; // as direct javabean

			contact.save();

			return contact;
		},
		
		/**
		 * sample path: 
		 * http://localhost:9000/js/contacts/newContact2?jsonData={"title":"my contact", "year":1999}
		 * or use curl: curl --form title="My Contact" --form year="1966" http://localhost:9000/js/contacts/newContact
		 */
		newContact2 : function (jsonData) {
			var contact = new Contact();
			bindJson(jsonData, contact);
			
			contact.save();
			
			return contact;
		},
		
		/**
		 * path: http://localhost:9000/js/contacts/getFile?name=js/contacts.js
		 */
		getFile : function(name) {
			return new File(name) 
			// or return showFile(new File(name))
			// or return showFile(name)
		},
		
		
		/**
		 * path: http://localhost:9000/js/contacts/download?name=js/contacts.js
		 */
		download : function(name) {
			return downloadFile(name);
			// or return downloadFile(new File(name))
		},
		
		/**
		 * path: http://localhost:9000/js/contacts/jpa?year=1956
		 */
		jpa: function(year) {
			// the findBy return all data immediately
			var contacts = JPA.findBy(Contact.class, "select name, firstName, birthdate from Contact order by birthdate", []);
			return contacts;
		},

		/**
		 * path: http://localhost:9000/js/contacts/jpa2?fromRow=1&maxResults=2
		 */
		jpa2: function(fromRow, maxResults) {
			// the find return a query which can be set limit the returned data
			var query = Contact.find("select name, birthdate from Contact", []);
			return query.from(fromRow).fetch(maxResults);
		},
	
		/**
		 * let's render a value in Japid template. 
		 * the template is "japidroot/japidviews/js/contacts/japid.html"
		 */
		japid: function() {
			var contact = contacts.getContactById(new java.lang.Long(1))
			return renderJapid(contact) 
		},
		
		
		/**
		 * demo the require feature
		 * path: http://localhost:9000/js/contacts/toRequire
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
		 * upload a file
		 * path: http://localhost:9000/js/contacts/upload
		 */
		upload: function(file, comment) {
			if (request().method == "GET")
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
				var u = session().get("_user")
				print("current user: " + u)
				if (!u){
					return redirect("auth.login", {a: "1", b:"2"}) // note the redirect function use with some parameters
				}
			}
		}
	}
}();
