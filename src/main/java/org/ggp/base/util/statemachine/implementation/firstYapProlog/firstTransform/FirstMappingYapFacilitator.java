package org.ggp.base.util.statemachine.implementation.firstYapProlog.firstTransform;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableSet;

/**
 * from util.gdl.scrambler.MappingGdlScrambler
 * @author Dubs
 *
 */
public class FirstMappingYapFacilitator {

	// Prolog predicates we must not use
	private static final ImmutableSet<String> EXISTING = ImmutableSet.of("is","nl","op","cd");

	// GDL keywords we must not translate
	private static final ImmutableSet<String> KEYWORDS = ImmutableSet.of("init","true","next","role","does","goal","legal","terminal","base","input");

	// Alphabet lowercase
	private static final String[] LOWERCASE = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
	// Alphabet upperrcase
	private static final String[] UPPERCASE = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
	// Alphabet length
	private static final int LENGTH = 26;

	/*
	 * The 3 mappings in this order:
	 * 		- CONSTANT: Gdl syntax -> Yap Prolog
	 * 		- CONSTANT: Yap Prolog -> Gdl syntax
	 * 		- VARIABLE: Gdl syntax -> Yap Prolog
	 *
	 * 	/!\ [VARIABLE: Yap Prolog -> Gdl syntax] is useless because YAP does not send back any variable
	 */
	private static Map<String, String> scrambleConstantMapping;
	private static Map<String, String> unscrambleConstantMapping;
	private static Map<String, String> scrambleVariableMapping;

	// Index used for scrambling constants
	private static int constantIndex;
	// Index used for scrambling variables
	private static int variableIndex;

	// StringBuilder to create constants
	private static StringBuilder sbConstant;
	// StringBuilder to create variables
	private static StringBuilder sbVariable;



	/**
	 * Initialized in the YapEngineSupport constructor
	 */
	public FirstMappingYapFacilitator()
	{
		scrambleConstantMapping = new HashMap<String, String>();
		unscrambleConstantMapping = new HashMap<String, String>();
		scrambleVariableMapping = new HashMap<String, String>();

		constantIndex = 0 ; variableIndex = 0 ;

		sbConstant = new StringBuilder();
		sbVariable = new StringBuilder();
	}



	/**
	 *  Scramble a constant and map it
	 */
	public static String scrambleConstant(String realWord)
	{
		if(!shouldMap(realWord))
		{
			return realWord;
		}
		if(!scrambleConstantMapping.containsKey(realWord))
		{
			String fakeWord = new String(nextConstant());
			scrambleConstantMapping.put(realWord, fakeWord);
			unscrambleConstantMapping.put(fakeWord, realWord);
		}
		return scrambleConstantMapping.get(realWord);
	}

	/**
	 * CREATE the next constant
	 *
	 * Maximum: ( 702 - EXISTING.length) constants (26+26*26=702)=702
	 */
	private static StringBuilder nextConstant()
	{
		if(constantIndex<LENGTH) // 1-letter constants
		{
			sbConstant.replace(0, 1, LOWERCASE[constantIndex]);
			constantIndex++;

			return sbConstant;

		} else { // 2-letters constants

			if(constantIndex % LENGTH == 0) // the first letter of 2-letters constants
				sbConstant.replace(0, 1, LOWERCASE[(constantIndex/LENGTH)-1]);

			sbConstant.replace(1, 2, LOWERCASE[constantIndex % LENGTH]); // the second one
			constantIndex++;

			if(EXISTING.contains(sbConstant.toString())) // it's already a Prolog built-in predicate
				return nextConstant();

			else return sbConstant;

		}
	}



	/**
	 *  Scramble a variable and map it
	 */
	public static String scrambleVariable(String realWord)
	{
		if(!scrambleVariableMapping.containsKey(realWord))
			scrambleVariableMapping.put(realWord, new String(nextVariable()));

		return scrambleVariableMapping.get(realWord);
	}

	/**
	 * CREATE the next variable
	 *
	 * Maximum: 702 variables (26+26*26=702)
	 */
	private static StringBuilder nextVariable()
	{
		if(variableIndex<LENGTH) // 1-letter variables
		{
			sbVariable.replace(0, 1, UPPERCASE[variableIndex]);
			variableIndex++;

			return sbVariable;

		} else { // 2-letters variables

			if(variableIndex % LENGTH == 0) // the first letter of 2-letters variables
				sbVariable.replace(0, 1, UPPERCASE[(variableIndex/LENGTH)-1]);

			sbVariable.replace(1, 2, LOWERCASE[variableIndex % LENGTH]); // the second one
			variableIndex++;

			return sbVariable;
		}
	}



	/**
	 * Verify :
	 * 		if the constant is a keyword ("true", "next", ...) : returns FALSE
	 * 		if the constant is an Integer : parseInt it and returns FALSE
	 * 		else returns TRUE
	 *
	 * In fact, TRUE if @token have to be scrambled and mapped
	 */
	private static boolean shouldMap(String token) {
		if (KEYWORDS.contains(token)) {
			return false;
		}
		try {
			Integer.parseInt(token);
			return false;
		} catch (NumberFormatException e) {
			;
		}
		return true;
	}



	/**
	 * Unscramble a constant
	 * Used to transform the Yap Prolog answers into the Gdl syntax
	 * 		unscrambleVariable is useless because Yap returns bound variables (= constants)
	 */
	public static String unscrambleConstant(String fakeWord)
	{
		if (!shouldMap(fakeWord))
		{
			return fakeWord;
		}
		if (!unscrambleConstantMapping.containsKey(fakeWord)) {
			return fakeWord;
		}
		return unscrambleConstantMapping.get(fakeWord);
	}

}

