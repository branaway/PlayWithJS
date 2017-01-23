//version: 0.9.6.2
package japidviews.js.books;
import java.util.*;
import java.io.*;
import cn.bran.japid.tags.Each;
import static cn.bran.play.JapidPlayAdapter.*;
import static play.data.validation.Validation.*;
import static play.templates.JavaExtensions.*;
import play.data.validation.Error;
import play.i18n.Messages;
import play.mvc.Scope.*;
import play.data.validation.Validation;
import play.i18n.Lang;
import controllers.*;
import japidviews._layouts.*;
import models.*;
import play.mvc.Http.*;
//
// NOTE: This file was generated from: japidviews/js/books/japid.html
// Change to this file will be lost next time the template file is compiled.
//
public class japid extends cn.bran.play.JapidTemplateBase
{
	public static final String sourceTemplate = "japidviews/js/books/japid.html";
	 private void initHeaders() {
		putHeader("Content-Type", "text/html; charset=utf-8");
		setContentType("text/html; charset=utf-8");
	}
	{
	}

// - add implicit fields with Play

	final play.mvc.Http.Request request = play.mvc.Http.Request.current(); 
	final play.mvc.Http.Response response = play.mvc.Http.Response.current(); 
	final play.mvc.Scope.Session session = play.mvc.Scope.Session.current();
	final play.mvc.Scope.RenderArgs renderArgs = play.mvc.Scope.RenderArgs.current();
	final play.mvc.Scope.Params params = play.mvc.Scope.Params.current();
	final play.data.validation.Validation validation = play.data.validation.Validation.current();
	final cn.bran.play.FieldErrors errors = new cn.bran.play.FieldErrors(validation);
	final play.Play _play = new play.Play(); 

// - end of implicit fields with Play 


	public japid() {
	super((StringBuilder)null);
	initHeaders();
	}
	public japid(StringBuilder out) {
		super(out);
		initHeaders();
	}
	public japid(cn.bran.japid.template.JapidTemplateBaseWithoutPlay caller) {
		super(caller);
	}

/* based on https://github.com/branaway/Japid/issues/12
 */
	public static final String[] argNames = new String[] {/* args of the template*/"book",  };
	public static final String[] argTypes = new String[] {/* arg types of the template*/"Book",  };
	public static final Object[] argDefaults= new Object[] {null, };
	public static java.lang.reflect.Method renderMethod = getRenderMethod(japidviews.js.books.japid.class);

	{
		setRenderMethod(renderMethod);
		setArgNames(argNames);
		setArgTypes(argTypes);
		setArgDefaults(argDefaults);
		setSourceTemplate(sourceTemplate);
	}
////// end of named args stuff

	private Book book; // line 1, japidviews/js/books/japid.html
	public cn.bran.japid.template.RenderResult render(Book book) {
		this.book = book;
		try {super.layout();} catch (RuntimeException __e) { super.handleException(__e);} // line 1, japidviews/js/books/japid.html
		return getRenderResult();
	}

	public static cn.bran.japid.template.RenderResult apply(Book book) {
		return new japid().render(book);
	}

	@Override protected void doLayout() {
		beginDoLayout(sourceTemplate);
;// line 1, japid.html
		p("<!DOCTYPE html>\n" + 
"<html>\n" + 
"<head>\n" + 
"<meta charset=\"UTF-8\">\n" + 
"</head>\n" + 
"<body>\n" + 
"    <p>");// line 1, japid.html
		try { Object o = escape(book.title); if (o.toString().length() ==0) { p(escape(null)); } else { p(o); } } catch (NullPointerException npe) { p(escape(null)); }// line 9, japid.html
		p(", ");// line 9, japid.html
		try { Object o = escape(book.year); if (o.toString().length() ==0) { p(escape(null)); } else { p(o); } } catch (NullPointerException npe) { p(escape(null)); }// line 9, japid.html
		p(", ");// line 9, japid.html
		try { Object o = escape(book.id); if (o.toString().length() ==0) { p(escape(null)); } else { p(o); } } catch (NullPointerException npe) { p(escape(null)); }// line 9, japid.html
		p("</p>\n" + 
"</body>\n" + 
"</html>");// line 9, japid.html
		
		endDoLayout(sourceTemplate);
	}

}