package controllers.nashornPlay;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.coveo.nashorn_modules.FilesystemFolder;
import com.coveo.nashorn_modules.Require;

import cn.bran.play.JapidController;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.NashornException;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.internal.runtime.ECMAException;
import jdk.nashorn.internal.runtime.ParserException;
import jdk.nashorn.internal.runtime.Undefined;
import nashornplay.etc.JavaUtils;
import nashornplay.etc.NashornExecutionException;
import nashornplay.etc.NashornTool;
import nashornplay.etc.NashornTool.FunctionInfo;
import nashornplay.etc.RenderGroovy;
import nashornplay.etc.RenderJackson;
import nashornplay.etc.RenderJapid;
import play.Logger;
import play.Play;
import play.db.jpa.Model;
import play.exceptions.CompilationException;
import play.mvc.Controller;
import play.mvc.Http.Request;
import play.mvc.Scope.Params;
import play.mvc.results.Result;
import play.utils.Utils.AlternativeDateFormat;
import play.vfs.VirtualFile;

/**
 * this version tries to use a single instance of engine to save memory
 * 
 * @author ran
 *
 */
public class NashornController2 extends Controller {
	public static String jsRoot = "js";
	private static final String COMMONJS = jsRoot + "/commonjs";
	private static final String _PARAMS = "_params";
	private static boolean shouldCoerceArg = Boolean
			.parseBoolean(Play.configuration.getProperty("jscontroller.coerce.args", "false"));

	static final String PLAY_HEADERS_JS = "/nashornplay/etc/playHeaders.js"; // resource
																				// path
	// static final String MODEL_HEADERS_JS = jsRoot + "/etc/modelHeaders.js";
	// // file
	// system
	// path
	// in
	// the
	// running
	// application

	private static String modelHeaders = "";

	private static AtomicBoolean modelHeadersUpdated = new AtomicBoolean(false);

	private static ScriptEngine engine;

	static {
		System.setProperty("nashorn.typeInfo.maxFiles", "20000");
		String[] options = new String[] { "-ot=true", "--language=es6" };
		engine = new NashornScriptEngineFactory().getScriptEngine(options);
		// enable the "require" plugin
		// https://github.com/coveo/nashorn-commonjs-modules
		enableRequire(engine);
		if (Play.mode.isProd()) {
			try {
				loadPlayHeaders(engine);
				_updateModelsHeader();
				loadModelDefs(engine);
			} catch (ScriptException e) {
				e.printStackTrace();
			}
			// somehow should load all the module definitions
		}
	}

	private static void enableRequire(ScriptEngine engine) {
		FilesystemFolder rootFolder = FilesystemFolder.create(new File(COMMONJS), "UTF-8");
		try {
			Require.enable((NashornScriptEngine) engine, rootFolder);
		} catch (ScriptException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private static void loadModelDefs(ScriptEngine engine) throws ScriptException {
		play.Logger.debug("load model headers to Nashorn context");
		engine.eval(modelHeaders);
	}

	private static void loadPlayHeaders(ScriptEngine engine) throws ScriptException {
		InputStream playHeaders = NashornController2.class.getResourceAsStream(PLAY_HEADERS_JS);
		play.Logger.debug("load %s to Nashorn context", PLAY_HEADERS_JS);
		try {
			// engine.eval("load('classpath:" + "playHeaders.js" + "')");
			engine.eval(new InputStreamReader(playHeaders, "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param _module
	 *            the js file that defines a module.
	 * @param _method
	 *            the method of the module to invoke
	 * @throws NoSuchMethodException
	 */
	public static void process(String _module, String _method) throws NoSuchMethodException {
		if (_method == null)
			_method = "index";

		if (_module.endsWith(".js"))
			_module += _module.substring(0, _module.lastIndexOf(".js"));
		// get
		// file
		// name
		// without
		// extension
		String fileName = jsRoot + "/" + _module + ".js";
		File srcFile = new File(fileName);
		if (Play.mode.isDev() && !srcFile.exists()) {
			notFound(fileName);
		}

		try {

			JSObject module = getModule(_module, engine, srcFile);

			// not implemented yet
			// Object optionCoerceArgsMember =
			// var.getMember("optionCoerceArgs");

			// method one:
			Object member = module.getMember(_method);
			if (member instanceof Undefined) {
				notFound("methd not found in the module: " + _module + "." + _method);
			}

			Object methParams = module.getMember(_method + _PARAMS);
			if (methParams instanceof Undefined) {
				error(_method + " parameter information was not stored in the engine");
			}
			Object[] args = processParams((FunctionInfo) methParams);

			Object before = module.getMember("_before");
			if (before instanceof JSObject) {
				// call the interceptor with the method name
				Object itorResult = ((JSObject) before).call(null, _method);
				processBeforeResult(_module, _method, itorResult);
			}

			Object r = ((JSObject) member).call(null, args);

			processResult(_module, _method, r);
		} catch (FileNotFoundException e) {
			notFound(_module);
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			if (e.getCause() instanceof ECMAException) {
				ECMAException cause = (ECMAException) e.getCause();
				Throwable cause2 = cause.getCause();
				if (cause2 instanceof ParserException) {
					convertToPlayCompilationError(fileName, (ParserException) cause2);
				} else {
					convertToPlayCompilationError(fileName, cause);
				}
			} else {
				convertToPlayCompilationError(fileName, e);
			}
		} catch (ECMAException e) {
			// Object t = e.getThrown();
			convertToPlayCompilationError(fileName, e);
		} catch (IllegalArgumentException e) {
			// e.printStackTrace();
			// need to parser the line like:
			// at
			tryCaptureScriptError(fileName, e);
		} catch (RuntimeException e) {
			tryCaptureScriptError(fileName, e);
		}
	}

	private static void processResult(String _module, String _method, Object r) {
		if (r instanceof RenderJapid) {
			RenderJapid rj = (RenderJapid) r;
			String template = jsRoot + "/" + _module + "/" + _method;
			JapidController.renderJapidWith(template, rj.args);
		} else if (r instanceof RenderGroovy) {
			RenderGroovy rj = (RenderGroovy) r;
			String template = jsRoot + "/" + _module + "/" + _method + ".html";
			renderTemplate(template, rj.args);
		} else if (r instanceof Result) {
			throw (Result) r;
		} else if (r instanceof File) {
			renderBinary((File) r);
		} else if (r instanceof String) {
			renderJSON(r);
		} else if (r instanceof Number) {
			renderJSON(r);
		} else if (r instanceof java.util.Date) {
			renderJSON(((java.util.Date) r).getTime());
		} else if (r instanceof java.sql.Date) {
			renderJSON(((java.sql.Date) r).getTime());
		} else if (r instanceof Undefined || r == null) {
			renderJSON("");
		} else if (r instanceof ScriptObjectMirror) {
			ScriptObjectMirror som = (ScriptObjectMirror) r;
			String className = som.getClassName();
			if ("Date".equals(className)) {
				Double timestampLocalTime = (Double) som.callMember("getTime");
				throw new RenderJackson(timestampLocalTime.longValue());
			} else {
				throw new RenderJackson(r);
			}
		} else if (r instanceof Model) {
			throw new RenderJackson(r);
		} else if (r instanceof Collection) {
			throw new RenderJackson(r);
		} else {
			throw new RenderJackson(r);
		}
	}

	private static void processBeforeResult(String _module, String _method, Object r) {
		if (r instanceof RenderJapid) {
			RenderJapid rj = (RenderJapid) r;
			JapidController.renderJapidWith(jsRoot + "/" + _module + "/" + _method, rj.args);
		} else if (r instanceof RenderGroovy) {
			RenderGroovy rj = (RenderGroovy) r;
			String template = jsRoot + "/" + _module + "/" + _method + ".html";
			renderTemplate(template, rj.args);
		} else if (r instanceof Result) {
			throw (Result) r;
		} else if (r instanceof File) {
			renderBinary((File) r);
		} else if (r instanceof String) {
			renderJSON(r);
		} else if (r instanceof Number) {
			renderJSON(r);
		} else if (r instanceof java.util.Date) {
			renderJSON(((java.util.Date) r).getTime());
		} else if (r instanceof java.sql.Date) {
			renderJSON(((java.sql.Date) r).getTime());
		} else if (r instanceof Undefined || r == null) {
			// // fall through
		} else if (r instanceof ScriptObjectMirror) {
			ScriptObjectMirror som = (ScriptObjectMirror) r;
			String className = som.getClassName();
			if ("Date".equals(className)) {
				Double timestampLocalTime = (Double) som.callMember("getTime");
				throw new RenderJackson(timestampLocalTime.longValue());
			} else {
				throw new RenderJackson(r);
			}
		} else if (r instanceof Model) {
			throw new RenderJackson(r);
		} else if (r instanceof Collection) {
			throw new RenderJackson(r);
		} else {
			throw new RenderJackson(r);
		}
	}

	private static void tryCaptureScriptError(String fileName, RuntimeException e) {
		// jdk.nashorn.internal.scripts.Script$Recompilation$24$535A$\^eval\_.books$getBookById-1(<eval>:32)
		List<StackTraceElement> goodLines = Arrays.stream(e.getStackTrace())
				.filter(st -> st.toString().contains("scripts.Script$")).collect(Collectors.toList());
		if (goodLines.size() > 0) {
			StackTraceElement ste = goodLines.get(0);
			Integer lineNum = ste.getLineNumber();
			String fname = ste.getFileName();
			if ("<eval>".equals(fname)) {
				fname = fileName;
			}
			String tempName = fname;
			VirtualFile vf = VirtualFile.fromRelativePath(tempName);
			NashornExecutionException ce = new NashornExecutionException(vf, "\"" + e.getMessage() + "\"", lineNum, 0,
					0);
			throw ce;
		} else {
			throw e;
		}
	}

	private static void convertToPlayCompilationError(String fileName, NashornException e) {
		String fname = e.getFileName();
		if ("<eval>".equals(fname)) {
			fname = fileName;
		}
		int line = e.getLineNumber();
		int col = e.getColumnNumber();
		// Object ecmaError = e.getEcmaError();
		String tempName = fname;
		VirtualFile vf = VirtualFile.fromRelativePath(tempName);
		CompilationException ce = new CompilationException(vf, "\"" + e.getMessage() + "\"", line, col, col + 1);
		throw ce;
	}

	private static void convertToPlayCompilationError(String fileName, ScriptException e) {
		String fname = e.getFileName();
		if ("<eval>".equals(fname)) {
			fname = fileName;
		}
		int line = e.getLineNumber();
		int col = e.getColumnNumber();
		// Object ecmaError = e.getEcmaError();
		String tempName = fname;
		VirtualFile vf = VirtualFile.fromRelativePath(tempName);
		CompilationException ce = new CompilationException(vf, "\"" + e.getMessage() + "\"", line, col, col + 1);
		throw ce;
	}

	/**
	 * assuming module definition in the js file: var _jsfile = function()
	 * {function GET(){} function POST{} return {GET:GET, POST:POST}}()
	 *
	 * This is using the script engine to cache the state
	 * 
	 * @param moduleName
	 * @param engine
	 * @param rawFile
	 * @return
	 * @throws ScriptException
	 * @throws FileNotFoundException
	 */
	private static JSObject getModule(String moduleName, ScriptEngine engine, File rawFile)
			throws ScriptException, FileNotFoundException {
		if (Play.mode.isDev()) {
			_updateModelsHeader();
			// is this too intrusive?
			engine.getBindings(ScriptContext.ENGINE_SCOPE).clear();

			enableRequire(engine);

			loadModelDefs(engine);
			// parse the header
			// engine.eval(new FileReader(PLAY_HEADERS_JS));
			// engine.eval("load('" + PLAY_HEADERS_JS + "');");
			loadPlayHeaders(engine); // XXX error reporting not to be mixed with
										// target action

			// remove old definition
			engine.getBindings(ScriptContext.ENGINE_SCOPE).remove(moduleName);

			evaluate(engine, rawFile);

			JSObject module = (JSObject) engine.get(moduleName);
			if (module == null) {
				String what = rawFile + " does not define [" + moduleName + "]. ";
				try {
					what += "\nIt defines: "
							+ NashornTool.extractTopLevelVariables(IOUtils.toString(new FileReader(rawFile)));
				} catch (IOException e) {
				} finally {
					error(what);
				}
				return null; // to fool the compiler
			} else {
				return parserModule(moduleName, engine);
			}

		} else { // production mode
			if (!modelHeadersUpdated.get()) {
				synchronized (engine) {
					_updateModelsHeader();
					loadModelDefs(engine);
				}
			}

			JSObject module = (JSObject) engine.get(moduleName);
			if (module == null) {
				synchronized (engine) {
					module = (JSObject) engine.get(moduleName);
					if (module == null) { // double check
						evaluate(engine, rawFile);
						module = parserModule(moduleName, engine);
					}
				}
			}
			return module;
		}
	}

	private static void _updateModelsHeader() {
		modelHeaders = getModelsHeader();
		modelHeadersUpdated.set(true);
	}

	private static JSObject parserModule(String moduleName, ScriptEngine engine) {
		JSObject module = (JSObject) engine.get(moduleName);
		if (module == null) {
			notFound("the module is not defined" + moduleName);
		} else {
			// parse the function parameters
			extractMethodInfo(moduleName, engine, module);
		}
		return module;
	}

	private static void extractMethodInfo(String moduleName, ScriptEngine engine, JSObject module) {
		Set<String> keys = module.keySet();
		keys.stream().forEach(k -> {
			Object member = module.getMember(k);
			if (member instanceof ScriptObjectMirror && ((ScriptObjectMirror) member).isFunction()) {
				String funcSource = member.toString();
				List<FunctionInfo> funcs = NashornTool.extractFuncs(k, funcSource);
				if (funcs.size() > 0) {
					FunctionInfo fi = funcs.get(0);
					// store the method signature in the engine scope
					// key by <modulename>.<method name>
					module.setMember(k + _PARAMS, fi);
					// engine.put(moduleName + "." + k, fi);
				} else {
					// might be an inline function
					FunctionInfo extractAnonymous = NashornTool.extractAnonymous(funcSource);
					if (extractAnonymous != null) {
						module.setMember(k + _PARAMS, extractAnonymous);
					} else {
						throw new RuntimeException("could not identify the parameter pattern: " + funcSource);
					}
				}
			}
		});
	}

	private static Object[] processParams(FunctionInfo fi) {
		Params params = Params.current();
		params.checkAndParse();
		Map<String, String[]> data = params.data;

		return fi.parameterNames.stream().map(k -> {
			String[] v = data.get(k);
			if (v != null) {
				if (v.length == 1) {
					return coerceArg(v[0]); // unwrap single element array
				} else {
					// convert string to typed object
					return Arrays.stream(v).map(ve -> coerceArg(ve)).toArray();
				}
			} else {
				// return null;
				return Undefined.getUndefined();
				// throw new RuntimeException("Missing argument for JavaScript
				// function " + fi.name + ": " + k);// XXX should throw
				// Execution error
			}
		}).toArray();
	}

	private static Object evaluate(ScriptEngine engine, Object url) throws ScriptException, FileNotFoundException {
		final long start = System.currentTimeMillis();
		try {
			if (url instanceof File) {
				return engine.eval("load('" + ((File) url).getPath() + "');");
			} else if (url instanceof String) {
				return engine.eval((String) url);
			} else
				return null;
		} finally {
			Logger.debug("Evaluated in " + (System.currentTimeMillis() - start) + " milliseconds");
		}
	}

	/**
	 * if the string is in "", it'll be unwrapped and be parsed to proper type.
	 * If quoted in single quote, it'll be unwrapped and returned as a String.
	 * 
	 * 
	 * 
	 * @param e
	 * @return
	 */
	public static Object coerceArg(String e) {
		String contentType = Request.current().contentType;
		if ("multipart/form-data".equals(contentType)) {
			// let's try to find an uploaded file
			File f = JavaUtils.bindFile(e);
			if (f != null)
				return f;
		}

		if (!shouldCoerceArg)
			return e;

		if (e.startsWith("\"") && e.endsWith("\"")) {
			// unwrap it
			e = e.substring(1, e.length() - 1);
		}

		if (e.startsWith("\'") && e.endsWith("\'")) {
			return e.substring(1, e.length() - 1);
		}

		if ("null".equals(e)) {
			return null;
		} else if (NumberUtils.isNumber(e)) {
			return NumberUtils.createNumber(e);
		} else {
			try {
				java.util.Date d = AlternativeDateFormat.getDefaultFormatter().parse(e);
				return d;
			} catch (ParseException e1) {
				// ok not a date
				if ("true".equalsIgnoreCase(e)) {
					return Boolean.TRUE;
				} else if ("false".equalsIgnoreCase(e)) {
					return Boolean.FALSE;
				} else
					return e;
			}
		}
	}

	private static String getModelsHeader() {
		// try in exposed controller method is not good.
		StringBuffer result = new StringBuffer();
		List<Class> models = Play.classloader.getAssignableClasses(Model.class);
		models.forEach(model -> {
			String shortName = model.getSimpleName();
			String nameWithPath = model.getCanonicalName();
			result.append("var " + shortName + " = Java.type(\"" + nameWithPath + "\");\n");
		});

		return result.toString();
	}

}