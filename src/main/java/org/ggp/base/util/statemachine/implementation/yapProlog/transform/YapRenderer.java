/**
 *
 */
package org.ggp.base.util.statemachine.implementation.yapProlog.transform;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlDistinct;
import org.ggp.base.util.gdl.grammar.GdlFunction;
import org.ggp.base.util.gdl.grammar.GdlLiteral;
import org.ggp.base.util.gdl.grammar.GdlNot;
import org.ggp.base.util.gdl.grammar.GdlOr;
import org.ggp.base.util.gdl.grammar.GdlProposition;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlRule;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.gdl.grammar.GdlVariable;

/**
 * GDL syntax -> Prolog syntax
 *
 * from util.gdl.scrambler.GdlRenderer
 *
 * @author Dubs
 */
public class YapRenderer {

	public static String renderYap(Gdl gdl)
	{
		if (gdl instanceof GdlTerm) {
			return renderTerm((GdlTerm) gdl);

		} else if (gdl instanceof GdlLiteral) {
			return renderLiteral((GdlLiteral) gdl);

		} else if (gdl instanceof GdlRule) {
			return renderRule((GdlRule) gdl);

		} else {
			throw new RuntimeException("Unexpected Gdl type " + gdl.getClass());
		}
	}

	public static String renderTerm(GdlTerm term)
	{
		if (term instanceof GdlConstant) {
			return renderConstant((GdlConstant) term);

		} else if (term instanceof GdlVariable) {
			return renderVariable((GdlVariable) term);

		} else if (term instanceof GdlFunction) {
			return renderFunction((GdlFunction) term);

		} else {
			throw new RuntimeException("Unexpected GdlTerm type " + term.getClass());
		}
	}

	protected static String renderSentence(GdlSentence sentence)
	{
		if (sentence instanceof GdlProposition) {
			return renderProposition((GdlProposition) sentence);

		} else if (sentence instanceof GdlRelation) {
			return renderRelation((GdlRelation) sentence);

		} else {
			throw new RuntimeException("Unexpected GdlSentence type " + sentence.getClass());
		}
	}

	protected static String renderLiteral(GdlLiteral literal)
	{
		if (literal instanceof GdlSentence) {
			return renderSentence((GdlSentence) literal);

		} else if (literal instanceof GdlNot) {
			return renderNot((GdlNot) literal);

		} else if (literal instanceof GdlOr) {
			return renderOr((GdlOr) literal);

		} else if (literal instanceof GdlDistinct) {
			return renderDistinct((GdlDistinct) literal);

		} else {
			throw new RuntimeException("Unexpected GdlLiteral type " + literal.getClass());
		}
	}

	/**
	 * Call MappingYapFacilitator
	 */
	protected static String renderConstant(GdlConstant constant)
	{
		//return MappingYapFacilitator.scrambleConstant(constant.getValue());
		return constant.getValue();
	}

	/**
	 * Call MappingYapFacilitator
	 */
	protected static String renderVariable(GdlVariable variable)
	{
		//return MappingYapFacilitator.scrambleVariable(variable.getName());
		String newVariable = variable.getName();
		return newVariable.substring(1, newVariable.length()).toUpperCase();
	}

	protected static String renderFunction(GdlFunction function)
	{
		StringBuilder sb = new StringBuilder();

		sb.append(renderConstant(function.getName()) + "(");
		for (GdlTerm term : function.getBody())
		{
			sb.append(renderTerm(term) + ",");
		}
		sb.replace(sb.length()-1, sb.length(), ")");

		return sb.toString();
	}

	protected static String renderRelation(GdlRelation relation)
	{
		StringBuilder sb = new StringBuilder();

		sb.append(renderConstant(relation.getName()) + "(");
		for (GdlTerm term : relation.getBody())
		{
			sb.append(renderTerm(term) + ",");
		}
		sb.replace(sb.length()-1, sb.length(), ")");

		return sb.toString();
	}

	protected static String renderProposition(GdlProposition proposition)
	{
		return renderConstant(proposition.getName());
	}

	protected static String renderNot(GdlNot not)
	{
		return "not(" + renderLiteral(not.getBody()) + ")";
	}

	protected static String renderDistinct(GdlDistinct distinct)
	{
		return "distinct(" + renderTerm(distinct.getArg1()) + "," + renderTerm(distinct.getArg2()) + ")";
	}

	protected static String renderOr(GdlOr or)
	{
		StringBuilder sb = new StringBuilder();

		sb.append("or(");
		for (int i = 0; i < or.arity(); i++)
		{
			sb.append(renderLiteral(or.get(i)) + ",");
		}
		sb.replace(sb.length()-1, sb.length(), ")");

		return sb.toString();
	}

	protected static String renderRule(GdlRule rule)
	{
		StringBuilder sb = new StringBuilder();

		sb.append(renderSentence(rule.getHead()) + ":-");
		for (GdlLiteral literal : rule.getBody())
		{
			sb.append(renderLiteral(literal) + ",");
		}
		sb.delete(sb.length()-1, sb.length());

		return sb.toString();
	}
}
