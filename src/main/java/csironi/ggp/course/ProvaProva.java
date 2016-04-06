package csironi.ggp.course;

import org.ggp.base.util.reflection.ProjectSearcher;

public class ProvaProva {

	public static void main(String[] args){
		for (Class<?> gamerClass : ProjectSearcher.INTERNAL_PROPNET_GAMERS.getConcreteClasses()) {
			System.out.println(gamerClass.getSimpleName());
		}
	}

}
