/**
 *
 */
package org.ggp.base.util.statemachine.implementation.yapProlog.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ggp.base.util.gdl.factory.GdlFactory;
import org.ggp.base.util.gdl.factory.exceptions.GdlFormatException;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.statemachine.proverStructure.ProverMove;
import org.ggp.base.util.statemachine.proverStructure.ProverRole;
import org.ggp.base.util.symbol.factory.exceptions.SymbolFormatException;

/**
 * @author Dubs
 *
 */
public class YapEngineSupport {

	public static MappingYapFacilitator yapMapping;


	/**
	 * Mapping in both ways (Gdl syntax <-> Yap Prolog):
	 * 		- GdlSentence
	 * 		- Role
	 * 		- Move
	 *	+ Translation of the description
	 */
	public YapEngineSupport()
	{
		//System.out.println("Creazione YapEngineSupport");
		yapMapping = new MappingYapFacilitator();

		sentenceMapping = new HashMap<String, GdlSentence>();
		unsentenceMapping = new HashMap<GdlSentence, String>();

		moveMapping = new HashMap<String, ProverMove>();
		unmoveMapping = new HashMap<ProverMove, String>();

		roleMapping = new HashMap<String, ProverRole>();
		unroleMapping = new HashMap<ProverRole, String>();
	}




	/**
	 * Transforms the game description into the Yap Prolog syntax
	 * Used to initialize the description file in [YapEngine(List<Gdl> description)]
	 */
	/*ublic StringBuffer toProlog(List<Gdl> description)
	{
		StringBuilder sb = new StringBuilder();

		for(Gdl gdl : description)
			sb.append(YapRenderer.renderYap(gdl)+". \n");

		sb.delete(sb.length()-2, sb.length());

		return new StringBuffer(sb);
	}*/

	/**
	 * Transforms the game description into the Yap Prolog syntax
	 * Used to initialize the description file in [YapEngine(List<Gdl> description)]
	 */
	public String toProlog(List<Gdl> description)
	{
		String yapDescription = "";

		for(Gdl gdl : description)
			yapDescription += YapRenderer.renderYap(gdl)+". \n";

		return yapDescription;
	}


	/**
	 * Transforms the result of deterministicGoal into a GdlSentence
	 * Used to return a MachineState(Set<GdlSentence>) in [askToState(String[] response)]
	 * GdlSentence :
	 * 		GdlRelation(GdlConstant name, ImmutableList<GdlTerm> body)
	 * 		GdlProposition(GdlConstant name)
	 *
	 *  /!\ adding of "(true("+..+")" because it's to create a MachineState
	 */
	public String toGdlSentence(String s)
	{
		// GdlProposition :
		if(s.indexOf("(") == -1) return MappingYapFacilitator.unscrambleConstant(s);

		// GdlRelation :
		StringBuilder sb = new StringBuilder();
		List<StringBuilder> list = separate(new StringBuilder(s));

		sb.append("(true(");

		for(StringBuilder sb2 : list)
			sb.append(MappingYapFacilitator.unscrambleConstant(sb2.toString())+" ");

		sb.append("))");

		return sb.toString();
	}


	private List<StringBuilder> separate(StringBuilder sb)
	{
		List<StringBuilder> list = new LinkedList<StringBuilder>();

		int i = 0 , j = 0 ;
		while(i < sb.length())
		{
			if(sb.charAt(i) == '(')
			{
				list.add(new StringBuilder(sb.substring(j, i)));
				j = i+1;
			}
			if(sb.charAt(i) == ',')
			{
				list.add(new StringBuilder(sb.substring(j, i)));
				j = i+1;
			}
			if(sb.charAt(i) == ')')
			{
				list.add(new StringBuilder(sb.substring(j, i)));
				j = i+1;
			}

			i++;
		}

		return list;
	}


	/**
	 * Transforms the result of deterministicGoal into a GdlConstant
	 * Used to return a Role(GdlConstant) in [askToRoles(String[] response)]
	 * 		GdlConstant(String value)
	 */
	public String toGdlConstant(String s)
	{
		return MappingYapFacilitator.unscrambleConstant(s);
	}


	/**
	 * Transforms the result of deterministicGoal into a GdlTerm
	 * Used to return a List<Move> in [askToMoves(String[] response)]
	 * GdlTerm :
	 *		GdlConstant(String value)
	 *		GdlVariable(String name)
	 *		GdlFunction(GdlConstant name, ImmutableList<GdlTerm> body)
	 */
	public String toGdlTerm(String s)
	{
		// GdlConstant & GdlVariable :
		if(s.indexOf("(") == -1) return MappingYapFacilitator.unscrambleConstant(s);

		// GdlFunction :
		StringBuilder sb = new StringBuilder();
		List<StringBuilder> list = separate(new StringBuilder(s));

		sb.append("(");

		for(StringBuilder sb2 : list)
			sb.append(MappingYapFacilitator.unscrambleConstant(sb2.toString())+" ");

		sb.append(")");

		return sb.toString();
	}




	private Map<String, GdlSentence> sentenceMapping;
	private Map<GdlSentence, String> unsentenceMapping;
	/**
	 * Translation Yap Prolog -> Gdl syntax
	 * 		Set<GdlSentence> for [MachineState]
	 * AFTER calling deterministicGoal
	 */
	public Set<GdlSentence> askToState(String[] response)
	{
		Set<GdlSentence> contents = new HashSet<GdlSentence>();

		for(String s : response)
			contents.add(mapGdlSentence(s));

		//System.out.println("askToState :"+contents);
		return contents;
	}

	/**
	 * Translation Yap Prolog -> Gdl syntax
	 * 		Set<GdlSentence> for [performDepthCharge]
	 * AFTER calling deterministicGoal
	 * 		/!\ response[0] = theDepth
	 */
	private int theDepth;
	public int getPerformDepth(){ return theDepth; }
	public Set<GdlSentence> askToStatePerform(String[] response)
	{
		Set<GdlSentence> contents = new HashSet<GdlSentence>();

		theDepth = Integer.parseInt(response[0]);

		for(int i=1 ; i<response.length ; i++)
			contents.add(mapGdlSentence(response[i]));

		//System.out.println("askToState :"+contents);
		return contents;
	}

	private GdlSentence mapGdlSentence(String fakeSentence)
	{
		try{
			if(!sentenceMapping.containsKey(fakeSentence))
			{
				GdlSentence realSentence = (GdlSentence) GdlFactory.create(toGdlSentence(fakeSentence));
				sentenceMapping.put(fakeSentence, realSentence);
				unsentenceMapping.put(realSentence, fakeSentence);
			}
			return sentenceMapping.get(fakeSentence);

		} catch (GdlFormatException e) {
			e.printStackTrace();
			return null;
		} catch (SymbolFormatException e) {
			e.printStackTrace();
			return null;
		}
	}




	private Map<String, ProverRole> roleMapping;
	private Map<ProverRole, String> unroleMapping;
	/**
	 * Translation Yap Prolog -> Gdl syntax
	 * 		List<Role>
	 * AFTER calling deterministicGoal
	 * @throws SymbolFormatException
	 */
	public List<ProverRole> askToRoles(String[] response) throws SymbolFormatException
	{
		List<ProverRole> roles = new ArrayList<ProverRole>();

		for(String s : response)
			roles.add(mapRole(s));

		//System.out.println("askToRoles :"+roles);
		return roles;
	}

	private ProverRole mapRole(String fakeRole) throws SymbolFormatException
	{
		if(!roleMapping.containsKey(fakeRole))
		{
			ProverRole realRole = new ProverRole((GdlConstant) GdlFactory.createTerm(toGdlConstant(fakeRole)));
			roleMapping.put(fakeRole, realRole);
			unroleMapping.put(realRole, fakeRole);
		}
		return roleMapping.get(fakeRole);

	}




	private Map<String, ProverMove> moveMapping;
	private Map<ProverMove, String> unmoveMapping;
	/**
	 * Translation Yap Prolog -> Gdl syntax
	 * 		List<Move>
	 * AFTER calling deterministicGoal
	 */
	public List<ProverMove> askToMoves(String[] response)
	{
		List<ProverMove> moves = new LinkedList<ProverMove>();

		for(String s : response)
			moves.add(mapMove(s));

		//System.out.println("askToMoves :"+moves);
		return moves;
	}

	public ProverMove askToMove(String response)
	{
		return mapMove(response);
	}

	private ProverMove mapMove(String fakeMove)
	{
		try{
			if(!moveMapping.containsKey(fakeMove))
			{
				ProverMove realMove = new ProverMove (GdlFactory.createTerm(toGdlTerm(fakeMove)));
				moveMapping.put(fakeMove, realMove);
				unmoveMapping.put(realMove, fakeMove);
			}
			return moveMapping.get(fakeMove);

		} catch (SymbolFormatException e) {
			e.printStackTrace();
			return null;
		}
	}





	/**
	 * Translation Gdl syntax -> Yap Prolog
	 * 		Role -> String
	 * BEFORE deterministicGoal
	 */
	public String getFakeRole(ProverRole realRole)
	{
		if(!unroleMapping.containsKey(realRole))
		{
			String fakeRole = MappingYapFacilitator.scrambleConstant(realRole.toString());
			roleMapping.put(fakeRole, realRole);
			unroleMapping.put(realRole, fakeRole);
		}
		return unroleMapping.get(realRole);
	}


	/**
	 * Translation Gdl syntax -> Yap Prolog
	 * 		Move -> String
	 * BEFORE deterministicGoal
	 */
	public String getFakeMove(ProverMove realMove)
	{
		if(!unmoveMapping.containsKey(realMove))
		{
			String fakeMove = YapRenderer.renderTerm(realMove.getContents());
			moveMapping.put(fakeMove, realMove);
			unmoveMapping.put(realMove, fakeMove);
		}
		return unmoveMapping.get(realMove);
	}


	/**
	 * Translation Gdl syntax -> Yap Prolog
	 * 		GdlSentence -> String
	 * BEFORE deterministicGoal
	 */
	public String getFakeGdlSentence(GdlSentence realSentence)
	{
		if(!unsentenceMapping.containsKey(realSentence))
		{
			StringBuilder sb = new StringBuilder();
			sb = new StringBuilder(YapRenderer.renderYap(realSentence));
			sb.deleteCharAt(sb.length()-1);
			sb.delete(0, 5);
			String fakeSentence = new String(sb);
			sentenceMapping.put(fakeSentence, realSentence);
			unsentenceMapping.put(realSentence, fakeSentence);
		}
		return unsentenceMapping.get(realSentence);
	}


	/*
	 * Translation Gdl syntax -> Yap Prolog
	 * 		List<Move> & List<Role> -> List<List<String>> for [getNextState]
	 * BEFORE deterministticGoal
	 */
	/*
	public List<List<String>> getFakeRoleAndMove(List<Move> moves, List<Role> roles)
	{
		List<List<String>> list = new LinkedList<List<String>>();

		if(moves.size()==roles.size())
		{
			for(int i = 0 ; i < moves.size() ; i++)
			{
				List<String> roleWithMove = new LinkedList<String>();
				roleWithMove.add(getFakeRole(roles.get(i)));
				roleWithMove.add(getFakeMove(moves.get(i)));
				list.add(roleWithMove);
			}

		}

		return list;
	}
	*/


	/**
	 * Translation Gdl syntax -> Yap Prolog
	 * 		Set<GdlSentence> -> List<String> for [computeState]
	 * BEFORE deterministicGoal
	 */
	public List<String> getFakeMachineState(Set<GdlSentence> machinestate)
	{
		List<String> list = new LinkedList<String>();

		for(GdlSentence sentence : machinestate)
			list.add(getFakeGdlSentence(sentence));

		return list;
	}


	/**
	 * Translation Gdl syntax -> Yap Prolog
	 * 		List<Role> -> List<String> for [getRandomJointMove(_)]
	 * BEFORE deterministicGoal
	 */
	public List<String> getFakeRoles(List<ProverRole> roles)
	{
		List<String> list = new LinkedList<String>();

		for(ProverRole role : roles)
			list.add(getFakeRole(role));

		return list;
	}


	/**
	 * Translation Gdl syntax -> Yap Prolog
	 * 		List<Move> -> List<String> for [getNextState(_)]
	 * BEFORE deterministicGoal
	 */
	public List<String> getFakeMoves(List<ProverMove> moves)
	{
		List<String> list = new LinkedList<String>();

		for(ProverMove move : moves)
			list.add(getFakeMove(move));

		return list;
	}


}
