/* 
** Author(s): Miguel Calejo
** Contact:   interprolog@declarativa.com, http://www.declarativa.com
** Copyright (C) Declarativa, Portugal, 2000-2011
** Use and distribution, without any warranties, under the terms of the 
** GNU Library General Public License, readable in http://www.fsf.org/copyleft/lgpl.html
*/
package com.declarativa.interprolog;
import com.declarativa.interprolog.util.*;
import java.io.*;
import java.util.*;
/** A TermModel specialization so serialization is done faster, based on string representations of the term. On the Prolog side
a specification for this can be obtained with buildInitiallyFlatTermModel(T,M) */
public class InitiallyFlatTermModel extends TermModel{
	private String canonicalTerm;
	private transient boolean stillFlat=true;
	private transient StreamTokenizer ST;
	private transient HashMap<String,Integer> variables; /** Variable names and their numbers - order of the first occurrence */
		
	public InitiallyFlatTermModel(Object n){
		super(n);
	}
	public InitiallyFlatTermModel(){ 
		super(); 
	}
	public InitiallyFlatTermModel(Object n,TermModel[] c,boolean isAList, boolean isFlatList){
		super(n,c,isAList,isFlatList);
	}
	/** build this term's tree (children, node and hasListFunctor fields) by parsing its string representation in canonicalTerm  */
	private void inflate() throws IOException{
		if (canonicalTerm==null) throw new IPException("Inconsistent canonicalTerm");
		//....parse and build tree... 
		// Term=2+X+[1,2,3,4]+'Y'+zz3+'_bad',browseTerm(Term), buildInitiallyFlatTermModel(Term,_M) ,ipPrologEngine(_E),ipObjectSpec('IPClassObject',Class,['com.declarativa.interprolog.gui.TermModelWindow'],_),javaMessage(Class,_,null,'TermModelWindow',[_M,_E],0,_).
		// System.out.println("canonicalTerm:"+canonicalTerm);
		ST = new StreamTokenizer(new StringReader(canonicalTerm));
		variables = new HashMap<String,Integer>();
		ST.wordChars(95,95); // "_" can be in words
		ST.wordChars(36,36); // "$" can be in words
		
		int NT = parseTerm(ST.nextToken(),this);
		// dump(NT);
		
		if (NT!=StreamTokenizer.TT_EOF) 
		throw new IPException("Extra garbage after "+NT+"in "+canonicalTerm);
		
		// System.out.println("toString:"+toString());
		// test tree:
		/*
		node = "+";
		children = new TermModel[]{new TermModel(new Integer(1)), new TermModel(new Integer(2))};*/
		stillFlat=false;
	}
	private void dump(int NT) throws IOException{
		System.out.println("Dumping term tokens. canonicalTerm:"+canonicalTerm);
		while(NT!=StreamTokenizer.TT_EOF){
			//System.out.println("NT="+NT+" Type="+ST.ttype + " string:"+ST.sval+" number:"+ST.nval);
			if (NT==StreamTokenizer.TT_NUMBER){
				System.out.println(ST.nval);
			} else if (NT==StreamTokenizer.TT_WORD && ST.sval.startsWith("_"))
				System.out.println("Variable:"+ST.sval);
			else if (NT==39 && ST.sval.equals(".") ) 
				System.out.println("list operator");
			else if (NT==StreamTokenizer.TT_WORD || NT==39 /* ' */ ) 
				System.out.println("Atom:"+ST.sval);
			else if (NT==91){
				int NT2 = ST.nextToken();
				if (NT2==93) System.out.println("[]");
				else ST.pushBack();
			}
			else if (NT==44 /* , */)
				System.out.println(",");
			else if (NT==41)
				System.out.println(")");
			else if (NT==40)
				System.out.println("(");
			else throw new IPException("Unexpected char in "+canonicalTerm+":"+NT);
			NT = ST.nextToken();
		}
		System.out.println("---end of dump");
	}
	
	private int parseTerm(int NT,TermModel term /* node and children null */) throws IOException{
			if (NT==StreamTokenizer.TT_NUMBER){
				if ((int)ST.nval == ST.nval) term.node = new Integer((int)ST.nval);
				else term.node = new Double(ST.nval);
			} else if (NT==StreamTokenizer.TT_WORD && ST.sval.startsWith("_"))
				term.node = new VariableNode(lookupVariable(ST.sval)); 
			else if (NT==StreamTokenizer.TT_WORD || NT==39 /* ' */ ) 
				term.node = ST.sval;
			else if (NT==39 && ST.sval.equals(".") ) 
				term.node="."; // list functor...
			else if (NT==91){
				int NT2 = ST.nextToken();
				if (NT2==93) term.node="[]";
				else { 
					// list starting...
					term.node="."; term.hasListFunctor=true; 
					ArrayList<TermModel> listItems = new ArrayList<TermModel>();
					NT = parseTermArgs(NT2, listItems);
					if (NT==124 /*|*/) throw new IPException("Open tail lists not admissible here"); 
					term.isFlatList=true;
					term.children = listItems.toArray(new TermModel[0]);
					if (NT!=93 /*]*/) throw new IPException("Missing ] in "+canonicalTerm);
					return ST.nextToken(); // list can not be a functor for a larger term...
				}
			}		
			else throw new IPException("Unexpected char in "+canonicalTerm+":"+NT);
			NT = ST.nextToken();
			if (NT==40 /* ( */) {
				ArrayList<TermModel> args = new ArrayList<TermModel>();
				NT = parseTermArgs(ST.nextToken(), args);
				term.children = args.toArray(new TermModel[0]);
				if (NT==41 /* ) */) {
					if (term.node.equals(".")&&term.getChildCount()==2)
						term.hasListFunctor=true;
					return ST.nextToken();
				} else throw new IPException("Missing ) in "+canonicalTerm);
			} else {
				if (term.node.equals("[]")) term.hasListFunctor=true;
				return NT;
			}
	}

	private int parseTermArgs(int NT,ArrayList<TermModel> args) throws IOException{
		TermModel arg = new TermModel(); 
		args.add(arg);
		NT = parseTerm(NT,arg);
		while (NT==44 /* , */) {
			arg = new TermModel(); args.add(arg);
			NT = parseTerm(ST.nextToken(),arg);
		};		
		return NT;
	}

	private int lookupVariable(String varName){
		Integer i = variables.get(varName);
		if (i==null) {
			i = new Integer(variables.size());
			variables.put(varName,i);
		}
		return i.intValue();
	}
	
	/** hack to parse the term and build the TermModel tree after unserializing this object */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
		in.defaultReadObject(); // why not ... super.readObject...??
		inflate();
	}

	/** rebuilds field canonicalTerm and nullifies node and children, so that the term looses its tree and
	becomes ready for quicker serialization; unfortunately this method must be invoked explicitly... because 
	writeObject can not do its job (in readObjects fashion, which fortunately dispenses an explicit call to inflate()...): 
	it is messaged by the system after the object graph to be serialized is build... so writeObject
	can NOT nullify node and children temporarily :-( If you wish to keep the original term, reassign
	the term structure with setNode and setChildren AFTER the serialization*/
	public void deflate(){
		if (node!=null) canonicalTerm = toString(true);
		//System.out.println("deflate...:"+canonicalTerm);
		node=null; children=null;
	}
	public static ObjectExamplePair example(){
		//InitiallyFlatTermModel A = new InitiallyFlatTermModel(); 
		InitiallyFlatTermModel A = new InitiallyFlatTermModel(); 
		//A.addChildren(new TermModel[]{new TermModel(1)});
		A.canonicalTerm="a";
		InitiallyFlatTermModel B = new InitiallyFlatTermModel(); 
		//InitiallyFlatTermModel B = new InitiallyFlatTermModel(); 
		//B.addChildren(new TermModel[]{new TermModel(2)});
		B.canonicalTerm="[]"; B.hasListFunctor=true; B.isFlatList=true;
		return new ObjectExamplePair("InitiallyFlatTermModel",A,B);
	}
	public String toString(){
		if (stillFlat) return "Canonical:("+canonicalTerm+")";
		else return super.toString();
	}
	
}