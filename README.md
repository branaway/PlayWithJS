# PlayWithJS

The nashornPlay project enables using JavaScript, or ECMAScript 5.1 as of JDK8 Nashorn engine, to code Play 1.x controllers. Controllers in JavaScript are cleaner due to the succinct JavaScript syntax. It opens doors for programmers with JavaScript experience to deliver applications from front-end to back-end, while enjoying the convenience and high performance that Play web engine offers. 

The project provides a base controller that an application need to extend from. The controller can be empty or can have any number of regular Java actions, as long as they don't take the reserved name "process" that the base controller exposes. Any requests mapped to the process action are considered JavaScript actions. 

The JavaScrits file must be located in the js directory. 


The base controller for JavaScript extension of Play 1.x series controllers is defined in the nashornPlay project. The sample project is in the jsPlay directory. 

## How to use the Play JavaScipt extension:

### build the  nashornPlay jar
 
 - cd nashornPlay
 - play dependencies --sync
 - play bm
   specify 1.2 as the minimum play version requirement.
 - now copy the jar named nashorPlay.jar to your target project's lib directory.

### Run the sample project
 - clone the jsPlay project.
 - copy the jar named nashorPlay.jar from nashornPlay project lib directory to jsPlay/lib.
 - get the required dependencies by running "cd jsPlay; play dependencies --sync"
 - "play run" to start it. 
 - browse the url: "http://locahost:9000" and study the js/books.js file to learn how to write database access code in ECMAScipt. 
 - The create an Eclipse project, issue command "play ec". Use "play idealize" to create projects for IDEA.
 - the URL mapping for JS controller is in the conf/route file. Look for a mapping for JSController.process. 
 - the data models are defined in the files in the app/models directory. The initial data is defined in the conf/data.yml file. 

 
### Use nashornplay in a new project.
 - create new Play project with "play new"
 - copy the jar named nashorPlay.jar from nashornPlay project lib directory to your target project's lib. 
 - create an empty controller to dispatch JavaScript requests. The controller contains no method, but it must extend the controllers.nashornplay.NashornController. 
 - put an entry in the conf/route:  "*     /js/{_module}/{_method}    JSController.process", where the JSController is the name of your new controller. The action name must be "process", which is provided by the NashornController. 
 - create a directory tree: js and js/etc. The etc file will contain model declarations by the controller. The generated file is named "modelHeaders.js". 


## the structure of a JS controller file is:

    var myModule = function(){
        return {
            action1: function(p1, p2) {...},
            action2: function(p3, p4...) {...},
            ...
            _before: function(actionName){
            
            }
    }();

- The name of the top level variable must be the same as the root name of the javascript file, which is myModule.js in this case.
- all the action functions must be exported by being associated with the return object properties.
- the "_before" function is optional, but when presented, it would be invoked before any other target functions. It's an interceptor  function for programmers to hook up any logic, such as security authorization. The target action function is eventually invoked if and only if nothing is returned from the _before function. Otherwise whatever is returned from the _before function is returned to the client. 
- about returned object. All returned object are converted to a JSON value, a JSON object or a JSON collections. The following rules take effect when objects are returned from JavaScript actions:

1. The primitive types are returned  as is. This includes numbers and strings. Strings are wrapped in a pair of double quotes. 
2. Dates are returned as the the number of milliseconds since January 1, 1970, 00:00:00 GMT.
3. A Java File object will be streamed back to the client as a binary stream. 
4. All Java/JavaScript objects and collections are returned to clients in JSON format. 



## RDBMS Access

RDBMS access is based on the Play JPQ support built in the Model class, which means we still need Java for creating models. We'll probably investigate pure JavaScript options in future iterations. The Good news is that Play's JPA API is very concise and effective, and the JavaScript code to work with it is clean and a pleasure to use.

All the model classes defined in the app/models directory is available for scripting. JS actions simply call the rich API of Play models to interact to DBs. 

Here are some of the most useful API methods with Models. For more usage reference please see [Play 1.4 JPA reference](https://www.playframework.com/documentation/1.4.x/jpa)

        
        public static long count(Class<? extends GenericModel> modelClass, String query, Object... params)
        public static List<T>  findAll(Class<T> modelClass)
        public static T findById(Class<T> modelClass, Object id)
        public static List <T> findBy(Class<T> modelClass, String query, Object... params)
        public static JPAQuery find(Class<? extends GenericModel> modelClass, String query, Object... params)
        public static JPAQuery find(Class<? extends GenericModel> modelClass) 
        public static JPAQuery all(Class<? extends GenericModel> modelClass) 
        public static int delete(Class<? extends GenericModel> modelClass, String query, Object... params) 
        
        // notice some of the methods return a JPAObject, which offers fine control of returned data set, mainly paged data set. Here are the main methods of JPAObject:
        /*
         * Retrieve all results of the query
         * @return A list of entities
         */
        public <T> List<T> fetch() {

        /*
         * Retrieve results of the query
         * @param max Max results to fetch
         * @return A list of entities
         */
        public <T> List<T> fetch(int max) {

        /*
         * Set the position to start
         * @param position Position of the first element
         * @return A new query
         */
        public <T> JPAQuery from(int position) {

        /*
         * Retrieve a page of result
         * @param page Page number (start at 1)
         * @param length (page length)
         * @return a list of entities
         */
        public <T> List<T> fetch(int page, int length) {


To create a model object, simply create a new instance of the model object and invoke the save() method after setting all the necessary properties. 

An exemplary model is defined as:

        package models;
        import javax.persistence.Entity;
        import javax.persistence.ManyToOne;
        import play.data.validation.Required;
        import play.db.jpa.Model;
        
        @Entity
        public class Book extends Model {
            @Required
            public String title;
            @Required
            public Integer year;
        
            @Required
            @ManyToOne
            public Contact author;
            
            // optional fields
            public Integer votes;
            public Integer rank;
            public Float rating = 6.0f; // this field has a default value
            public Boolean available;
        }

     

Here are some of the examples in the jsPlay project:

----

        action1 : function() {
            var books = Book.find("order by year", []).fetch();
            return books;
        }

In the above action, Play JPA feature built in a model named Book is used to return all the books ordered by the year field. The books is returned to the client in JSON format, which is the default data format for generic JavaScript objects and Java objects, including simple object and collections. The books collection (a java.util.List object in the above case) is returned to the client as a JSON array.

There are many methods of Java List object that can be used to manipulate the contained element. Study the java.util.List API for effective scripting.  

----

        some : function(howMany) {
            var jpaQuery = JPA.find(Book.class);
            if (howMany) {
                var books = jpaQuery.fetch(howMany)
                return {
                    errorcode : 0,
                    data : books
                }
            } else {
                return "oops!"
            }
        }

In this case, a JPAQuery object is obtained by invoking the built-in find(...) method of models. The query object, which invoked on its fetch object with the maximum number of rows, will return up to that number of rows. Then the rows are wrapped in a JavaScript object and is returned to the client in JSON format. In case nothing is found, a simple String is returned to the client. In this case it's a simple string without being boxed up. 

To invoke the action, a client uses a URL similar to this:

        http://localhost:9000/js/books/some?howMany=3
    
The query parameter howMany is mapped to the function some's parameter of the same name. 

----

        newBook : function (title, year) {
            var book = new Book();
            if (title)
                book['title'] = title; // set on an attribute
            if (year)
                book.year = year; // as direct javabean

            book.save();

            return book;
        }
        
The above action creates a new instance of Book and sets two properties before saving it to the back-end database. Once saved, the object would contain assigned ID and other default property values.     

If the values for the properties of the new object is posted as JSON object, one can use the following pattern to set all the value on a new instance of the object, as in:
        
----

        newBook2 : function (jsonData) {
            var book = new Book();
            var json = JSON.parse(jsonData)
            for (var p in json) {
                book[p] = json[p]
            }
            book.save();
            return book;
        }
        
The for loop construct in JavaScript is able to iterate through all the properties of an JavaScript object. Also notice is that the JSON.parse function is used to convert a string representation of JSON back to a valid JavaScript object.

----

        jpa2: function(fromRow, maxResults) {
            // the find return a query which can be set limit the returned data
            var query = JPA.find(Book.class, "select title, author.name from Book");
            return query.from(fromRow).fetch(maxResults);
        }

The above code shows how to use the JPAQuery methods to serve paged data. 

----
        jpa2: function(fromRow, maxResults) {
            var query = JPA.find(Book.class, "select title, author.name from Book");
            return query.from(fromRow).fetch(maxResults);
        },
 
The above action uses the JPA class directly to do full a JPQL query and return a page of data. 

The client would get something back like this:

    [
        ["The Shawshank Redemption", "Bort"],
        ["The Godfather", "鈴木"]
    ]

Note that the returned data does not contain the property names, but just the raw data, which is good for reducing data size. The reason that only raw data is generated is that an explicit JPQL query has been specified. 

If the above query has been like :
    
    var query = Book.find("", []);
        
The result would have been like below, where the full data content is returned, including the nested "author" object, which is another Model object defined in Contact.java:

    [ {
      "id" : 4,
      "title" : "The Good",
      "year" : 1943,
      "author" : {
        "id" : 1,
        "firstname" : "Maxime",
        "name" : "Dantec",
        "birthdate" : 500601600000,
        "email" : "hello@warry.fr"
      },
      "votes" : 233,
      "rank" : 4,
      "rating" : 8.5,
      "available" : true
    }, {
      "id" : 5,
      "title" : "My Fair Lady",
      "year" : 1966,
      "author" : {
        "id" : 1,
        "firstname" : "Maxime",
        "name" : "Dantec",
        "birthdate" : 500601600000,
        "email" : "hello@warry.fr"
      },
      "votes" : 2345,
      "rank" : 5,
      "rating" : 6.0
    } ]


## Automatic variables available in the actions

The following variables are automatically available in the actions. 

- request: the HTTP request
- response: the HTTP response
- flash: a short-lived storage that is available in one round of request/response. It can survive a redirect though.
- session: cookie based storage for a browsing session
- params: the parameters associated with an HTTP request

Study the following links for better understanding the above variables:

- [params](https://playframework.com/documentation/1.4.0/controllers#params)
- [session and scopes](https://playframework.com/documentation/1.4.0/controllers#session)

## File uploading

There is a convenient function to retrieve an uploaded file from action. Study this action:

        /**
         * upload a file
         * path: http://localhost:9000/js/books/upload
         */
        upload: function(file, comment) {
            if (request.method == "GET")
                return new File("public/upload.html")
            else {
                // the file should have been mapped to the upload
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
        
which is invoked by a file upload form:

    <form method="post" action="/js/books/upload" enctype="multipart/form-data">
        <div>
            <label for="file">File:</label> <input type="file" name="file" size="60"/>
        </div>
        <div>
            <label for="comment">Comment:</label> <input type="text" id="comment"
                name="comment" size="60"/>
        </div>
        <div>
            <button type="submit">Submit</button>
        </div>
    </form>
    

The uploaded file has been assign to the file variable which is of java.util.File type, like in a Java Play action. It's up to the action to properly dispose the file object. In the above case it's simply deleted. 

The parameter names must strictly match those of the form fields, or we won't get the file.

## URL mapping

The routing rule for the JS cotroller is:
    
    *     /js/{_module}/{_method}             JSController.process
    
The module file (basically one JS file) defines a module. All the js files must be located in the "js" directory. Nested directory is not supported. 

The query parameters will be mapped to the JavaScript action function parameters, as long as they share the same name. The orders of the parameters in the client or of the server side action does not matter. 

Please note that the NashornController tries to coerce the input values to native Java objects before set them to the JavaScript actions. Here are some of the conversion examples:

1. "string" -> Java String with the quotation marks stripped off. 
2. 2 -> Java Integer
3. 101L -> Java Long 
4. 4.0 -> Double
5. 5.0F -> Float
6. '1' -> String "1" // Strings in single quotes are not parsed. 
7. "true"/"false" -> Boolean type
8. String of possible date format will be tried for a proper conversion to a Java Date object. 

## Using Japid in JavaScript controller

The following action uses a [Japid](http://github.com/branaway/Japid) template to render the book:

        /**
         * let's render a value in Japid template. 
         * the template is "japidroot/japidviews/js/books/japid.html"
         */
        japid: function() {
            var book = books.getBookById(new java.lang.Long(1))
            return renderJapid(book) 
        },
 The template is located in "japidroot/japidviews/js/books/" directory and must match the name of the action, "japid" in the above case. Here is what's inside the template:

     `(Book book)
    <!DOCTYPE html>
    <html>
    <body>
        <p>~{book.title}, ~{book.year}, ~book.id</p>
    </body>
    </html>
    
BTW, the ~{...} (with or without the curly braces) is the template expression to display a value with HTML sensitive characters escaped. The un-escaped version is ${}. 
 
## Other rendering functions

In classic Play 1.x, various renderXXX() are defined to return different type of HTTP response. Similarly the following rendering functions are available in actions. 

Note 1: Play 1.x uses exceptions to generate returned values, whereas NashornPlay uses "return" to return a rendering method.
Note 2: programmers don't need to explicitly call these renderXXX to generate proper response. If all you need is generate JSON response, you just return the object directly and JSON serialization automatically applies.   

- renderText(myString): e.g., return renderText("hello")
- renderJSON({...}): e.g., return renderJSON(book). Note, GSON is used to render objects 
- renderJackson({...}): render JSON with Jackson library. This is the default rendering method if plain objects (Java or JavaScript native) are returned in actions.   
- renderXml({})
- renderJapid(...)
- renderHtml(string)
- redirect(url): generate an HTTP redirect to the URL
- forbidden(message): 403


## Using other JavaScript resources in actions

JDK Nashorn engine offeres load('...') function for one to load other javascript resource in the global space. The books.js loads another javascript file by means of this mechanism. 

nashornPlay has experimental code to enable using "require" in actions. CommonJS modules can be put in "js/commonjs" and can be loaded by "require(...)" in actions. The mechanism depends on a third-party module located [here](https://github.com/coveo/nashorn-commonjs-modules). Please check out the toRequire() function in the books.js for a sample.

