/* 
** Author(s): Miguel Calejo
** Contact:   interprolog@declarativa.com, http://www.declarativa.com
** Copyright (C) Declarativa, Portugal, 2000-2005
** Use and distribution, without any warranties, under the terms of the 
** GNU Library General Public License, readable in http://www.fsf.org/copyleft/lgpl.html
*/
package com.declarativa.interprolog;
import com.declarativa.interprolog.util.*;
import java.io.*;
import java.net.*;

/** A PrologEngine encapsulating a <a href='http://xsb.sourceforge.net'>XSB Prolog</a> engine, accessed over TCP/IP sockets. 
*/
public class XSBSubprocessEngine extends SubprocessEngine{
    protected PrologImplementationPeer makeImplementationPeer(){
    	return new XSBPeer(this);
    }
    public XSBSubprocessEngine(String prologCommand, boolean debug, boolean loadFromJar){
    	super(prologCommand, debug, loadFromJar);
    }
    public XSBSubprocessEngine(String prologCommand, boolean debug){
    	super(prologCommand, debug);
    }
    public XSBSubprocessEngine(String prologCommand){
    	super(prologCommand);
    }
    public XSBSubprocessEngine(boolean debug){
    	super(debug);
    }
    /** @see com.declarativa.interprolog.AbstractPrologEngine#AbstractPrologEngine(String prologBinDirectoryOrCommand, boolean debug, boolean loadFromJar)*/
    public XSBSubprocessEngine(){
    	super();
    }	
}


