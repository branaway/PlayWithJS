package nashornplay.etc;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import play.exceptions.UnexpectedException;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.results.Result;

/**
 * a messenger to carry arguments from JavaScript to Java Controller Groovy
 * based template
 * 
 * @author ran
 *
 */
public class RenderGroovy {
	public Map args = new HashMap<String, Object>();
	public Object arguments;

	public RenderGroovy(Object o) {
		this.arguments = o;
		if (o instanceof Map) {
			args = (Map) o;
		} else {
			throw new RuntimeException("the args to Play Groovy templates must be a map");
		}
	}
}
