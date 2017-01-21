# PlayWithJS

The Template controller for JavaScript extension of Play 1.x series controllers is defined in the nashornPlay project. The sample project is in the jsPlay directory. 

## How to use the Play JavaScipt extension:

### build the  nashornPlay jar
 
 - cd nashornPlay
 - play dependencies
 - play dependencies --sync
 - play bm
   specify 1.2 as the minimum play version requirement.
 - now copy the jar named nashorPlay.jar to your target project's lib directory.

### Run the sample project
 - clone the jsPlay project.
 - copy the jar named nashorPlay.jar from nashornPlay project lib directory to jsPlay/lib.
 - get the required dependencies by running "cd jsPlay; play dependencies --sync"
 - play run to start it. 
 - browse the url: "http://locahost:9000" and study the js/books.js file to learn how to write database access code in ECMAScipt. 
 - the URL mapping for JS controller is in the conf/route file. Look for a mapping for JSController.process. 
 
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
            _before: function(){
            
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

Here are some of the most useful API methods with Models:
        
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
            public Float rating = 6.0f;
            public Boolean available;
        }

     

- There are a few ways that an application return data to clients. Here are some of the example:

        action1 : function() {
            var books = Book.find("order by year", []).fetch();
            return books;
        }

In the above action, Play JPA feature built in a model named Book is used to return all the books ordered by the year field. The books is returned to the client in JSON format, which is the default data format for generic JavaScript objects and Java objects, including simple object and collections. The books collection (a java.util.List object in the above case) is returned to the client as a JSON array.

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


        jpa2: function(fromRow, maxResults) {
            // the find return a query which can be set limit the returned data
            var query = JPA.find(Book.class, "select title, author.name from Book");
            return query.from(fromRow).fetch(maxResults);
        }

 The above code shows how to use the JPAQuery methods to serve paged data. 
 
## URL mapping

The routing rule for JS cotroller is
    
    *     /js/{_module}/{_method}             JSController.process
    
The module file (basically one JS file) defines one module. All the js files must be located in the "js" directory. Nested directory is not supported. 

The query parameters will be mapped to the JavaScript action function parameters, as long as they share the same name. The orders of the parameters in the client or of the server side action does not matter. 

Please note that the NashornController tries to coerce the input values to native Java objects before set them to the JavaScript actions. Here are some of the conversion examples:

1. "string" -> Java String with the quotation marks stripped off. 
2. 2 -> Java Integer
3. 101L -> Java Long 
4. 4.0 -> Double
5. 5.0F -> Float
6. '1' -> String "1" // Strings in single quotes are not parsed. 
7. "true"/"false" -> Boolean type
8. String of possible date format will be tried for a proper conversion to Java Date object. 

