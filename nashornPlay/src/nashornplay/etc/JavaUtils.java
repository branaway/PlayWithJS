package nashornplay.etc;

import java.io.File;
import java.util.List;
import java.util.Map;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.internal.runtime.Undefined;
import play.data.Upload;
import play.data.binding.TypeBinder;
import play.mvc.Http.Request;
import play.mvc.results.Redirect;

/**
 * locate the File object created during multipart/form-data parser. Modeled
 * after the FileBinder.
 * 
 * @author ran
 *
 */
public class JavaUtils {

	public static File bindFile(Object fi) {
		if (fi instanceof File)
			return (File) fi;

		if (fi instanceof String) {
			String fieldName = (String) fi;
			Request req = Request.current();
			if (req != null && req.args != null) {
				List<Upload> uploads = (List<Upload>) req.args.get("__UPLOADS");
				if (uploads != null) {
					for (Upload upload : uploads) {
						if (fieldName.equals(upload.getFieldName())) {
							if (upload.getFileName().trim().length() > 0) {
								File file = upload.asFile();
								return file;
							}
						}
					}
				}
			}
		}
		return null;
	}

	public static Redirect redirect(Object url, Map<String, String> args) {
		if (url instanceof Undefined)
			args = null;

		if (url instanceof String) {
			String theUrl = (String) url;

			if (!theUrl.startsWith("/js")) {

				theUrl = theUrl.replaceAll("\\.", "/");

				if (theUrl.startsWith("/"))
					theUrl = "/js" + theUrl;
				else
					theUrl = "/js/" + theUrl;
			}

			return new Redirect(theUrl, args);
		}
		return new Redirect(url.toString(), args);
	}
}
