package csironi.ggp.course;

import org.ggp.base.util.statemachine.cache.RefactoredTtlCache;

public class ProvaProva {

	public static void main(String[] args){
		//for (Class<?> gamerClass : ProjectSearcher.INTERNAL_PROPNET_GAMERS.getConcreteClasses()) {
		//	System.out.println(gamerClass.getSimpleName());
		//}


		RefactoredTtlCache<String, String> theCache = new RefactoredTtlCache<String, String>(1);

		System.out.println("Empty? " + theCache.isEmpty());

		theCache.put("Chiara", "CRF");

		System.out.println("Empty? " + theCache.isEmpty());

		System.out.println("Old value for Annalisa: " + theCache.put("Annalisa", "NLS"));
		System.out.println("Old value for Annalisa: " + theCache.put("Annalisa", "NNL"));
		theCache.put("Luisa", "LSH");
		theCache.put("Federico", "FDR");
		theCache.put("Joost", "JST");

		System.out.println("Luisa? " + theCache.containsKey("Luisa"));

		System.out.println("Luisa = " + theCache.get("Luisa"));

		System.out.println("FDR? " + theCache.containsValue("FDR"));

		System.out.println(theCache);

		theCache.prune();

		System.out.println(theCache);

		theCache.get("Annalisa");
		theCache.get("Federico");
		theCache.get("Luisa");

		System.out.println(theCache);

		theCache.prune();

		System.out.println(theCache);

		theCache.prune();

		System.out.println(theCache);

	}

}
