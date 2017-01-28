

/**
 * 
 */

var Ok = Java.type("play.mvc.results.Ok");  // k is in lower case;
function ok() {new Ok()}

var RenderText = Java.type("play.mvc.results.RenderText");
function renderText(o) {return new RenderText(o);}

var RenderJson = Java.type("play.mvc.results.RenderJson"); 
function renderJson(o) {return new RenderJson(o);}

var RenderHtml = Java.type("play.mvc.results.RenderHtml"); 
function renderHtml(o) {return new RenderHtml(o);}

var RenderXml = Java.type("play.mvc.results.RenderXml"); 
function renderXml(o) {return new RenderXml(o);}

var RenderFile = Java.type("play.mvc.results.RenderBinary"); 
function showFile(o) {return new RenderFile(o);} // inline
function downloadFile(o) {return new RenderFile(o, o.name, false);} // attachment

var Redirect = Java.type("play.mvc.results.Redirect"); 
//function redirect(url, args) {
//	print(url)
//	
//	if (args == undefined)
//		args = null;
//	
//	
//	if (typeof(url) == "string" && !url.startsWith('/js')){
//		url.replace(/\./, '/')
//		print(url)
//		if (url.startsWith('/'))
//			url = "/js" + url;
//		else
//			url = "/js/" + url;
//	}
//	
//	if (typeof url == "Function") {
//		print(url)
//		url = url.name;
//	}
//		
//	return new Redirect(url, args);
//}

var RenderError = Java.type("play.mvc.results.Error"); 
function renderError(o) {return new RenderError(o);}

var NotFound = Java.type("play.mvc.results.NotFound"); 
function notFound(o) {return new NotFound(o);}

var Forbidden = Java.type("play.mvc.results.Forbidden"); 
function forbidden(o) {return new Forbidden(o);}

var RenderJapid = Java.type("nashornplay.etc.RenderJapid"); 
function renderJapid() {return new RenderJapid(arguments);}

var RenderGroovy = Java.type("nashornplay.etc.RenderGroovy"); 
function render(args) {return new RenderGroovy(args);}

var RenderJackson = Java.type("nashornplay.etc.RenderJackson"); 
function renderJackson(o) {return new RenderJackson(o);}


var Request = Java.type("play.mvc.Http.Request"); 
var request = Request.current();

var Response = Java.type("play.mvc.Http.Response"); 
var response = Response.current();

var Flash = Java.type("play.mvc.Scope.Flash"); 
var flash = Flash.current();

var Session = Java.type("play.mvc.Scope.Session"); 
var session = Session.current();

var JavaUtils = Java.type("nashornplay.etc.JavaUtils");
var getUploadedFile = function(name) { return JavaUtils.bindFile(name)}
// the above method is probably not useful anymore since the file field mapping would have been 
// done by the NashornController. 

var redirect = function(target, args) {
	return JavaUtils.redirect(target, args);
}

var params = request.params.data; // a Map object

//
var JPA = Java.type("play.db.jpa.JPA");
/**
 * some of the useful API by JPA
 	public static long count(Class<? extends GenericModel> modelClass, String query, Object... params) {
	public static <T extends GenericModel> List<T>  findAll(Class<T> modelClass) {
	public static <T extends GenericModel> T findById(Class<T> modelClass, Object id) {
	public static <T extends GenericModel> List <T> findBy(Class<T> modelClass, String query, Object... params) {
	public static JPAQuery find(Class<? extends GenericModel> modelClass, String query, Object... params) {
	public static JPAQuery find(Class<? extends GenericModel> modelClass) {
	public static JPAQuery all(Class<? extends GenericModel> modelClass) {
	public static int delete(Class<? extends GenericModel> modelClass, String query, Object... params) {

The JPAQuery offers a nice API:

         * Retrieve all results of the query
         * @return A list of entities

        public <T> List<T> fetch() {

         * Retrieve results of the query
         * @param max Max results to fetch
         * @return A list of entities
        public <T> List<T> fetch(int max) {

         * Set the position to start
         * @param position Position of the first element
         * @return A new query
        public <T> JPAQuery from(int position) {

         * Retrieve a page of result
         * @param page Page number (start at 1)
         * @param length (page length)
         * @return a list of entities
        public <T> List<T> fetch(int page, int length) {

 */

// java
var File = Java.type("java.io.File");
