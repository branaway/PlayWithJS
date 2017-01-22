package nashornplay;

import java.util.List;

import play.Play;
import play.PlayPlugin;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.mvc.Controller;

public class NashornPlayPlugin extends PlayPlugin {

	@Override
	public void onLoad() {
//		System.out.println("NashornPlayPlugin onLoad()");
//		List<ApplicationClass> cons = Play.classes.getAssignableClasses(Controller.class);
//		cons.stream().forEach( c -> System.out.println(c));
//		System.out.println("NashornPlayPlugin see me?");
	}

	@Override
	public void onApplicationStart() {
//		System.out.println("NashornPlayPlugin onApplicationStart()");
	}

}
