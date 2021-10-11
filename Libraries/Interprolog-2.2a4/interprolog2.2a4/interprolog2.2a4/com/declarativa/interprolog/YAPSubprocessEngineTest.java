/* 
** Author(s): Miguel Calejo
** Contact:   interprolog@declarativa.com, http://www.declarativa.com
** Copyright (C) Declarativa, Portugal, 2000-2005
** Use and distribution, without any warranties, under the terms of the 
** GNU Library General Public License, readable in http://www.fsf.org/copyleft/lgpl.html
*/
package com.declarativa.interprolog;
import junit.framework.*;
import java.util.*;
import com.declarativa.interprolog.util.*;

public class YAPSubprocessEngineTest extends SubprocessEngineTest {
	public YAPSubprocessEngineTest(String name){
		super(name);
	}
	// JUnit reloads all classes, clobbering variables, 
	// so the path should be obtained from System properties or other external means:
	protected AbstractPrologEngine buildNewEngine(){
		AbstractPrologEngine engine = new YAPSubprocessEngine();
		engine.deterministicGoal("yap_flag(unknown,error)"); // so our tests find behavior similar to other Prologs
		return engine;
	}
	//public void testNewInterrupt(){} // not functional at the moment...
	public void testDigestingBadGoal(){} // atomRead aborting silently in YAP with threads! Other tests failing too
	// Atom='bad goal', atom_codes(Atom,Chars), read_from_chars(Chars,T), nonvar(T).
	public void testBuildTermModel(){} // not functional for terms with vars at the moment...
}
