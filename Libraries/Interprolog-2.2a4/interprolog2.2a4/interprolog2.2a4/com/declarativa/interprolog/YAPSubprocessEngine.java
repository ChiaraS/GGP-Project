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

/** A PrologEngine encapsulating a <a href='http://www.ncc.up.pt/~vsc/Yap/'>YAP Prolog</a> engine, accessed over TCP/IP sockets. 
*/
public class YAPSubprocessEngine extends SubprocessEngine{
    protected PrologImplementationPeer makeImplementationPeer(){
    	return new YAPPeer(this);
    }
    public YAPSubprocessEngine(String prologCommand, boolean debug, boolean loadFromJar){
    	super(prologCommand, debug, loadFromJar);
    }
    public YAPSubprocessEngine(String prologCommand, boolean debug){
    	super(prologCommand, debug);
    }
    public YAPSubprocessEngine(String prologCommand){
    	super(prologCommand);
    }
    public YAPSubprocessEngine(boolean debug){
    	super(debug);
    }
    public YAPSubprocessEngine(){
    	super();
    }
	protected PrologOutputObjectStream buildPrologOutputObjectStream(OutputStream os) throws IOException{
		//return new PrologOutputObjectStream(os,true /* use escape byte mechanism */);
		return new PrologOutputObjectStream(os);
	}	
	public boolean realCommand(String s){
			progressMessage("COMMAND:"+s+".");
		sendAndFlushLn("("+s+"), write('"+YAPPeer.REGULAR_PROMPT+"'), flush_output, !, fail."); // to make sure YAP doesn't hang showing variables
		return true; // we do not really know
	}
	protected void prepareInterrupt(String myHost) throws IOException{ // requires successful startup steps
		// YAP does not like to receive "signal 2"...
		intServerSocket = new ServerSocket(0);
		command("setupWindowsInterrupt('"+myHost+"',"+intServerSocket.getLocalPort()+")");
		intSocket = intServerSocket.accept();
		progressMessage("interrupt prepared");
		available=true; // kludge
	}
	protected synchronized void doInterrupt(){
	    setDetectPromptAndBreak(true);
	    try {
			byte[] ctrlc = {3};
			progressMessage("Attempting to interrupt Prolog...");
			OutputStream IS = intSocket.getOutputStream();
			IS.write(ctrlc); IS.flush();			
	    } catch(IOException e) {throw new IPException("Exception in interrupt():"+e);}
	    cleanupInterrupt(); // susceptible to race conditions... ;-)
	    waitUntilAvailable();
	    // sendAndFlushLn("abort."); // supposedly leaves break mode... it does not work in XSB 3.2
	    //sendAndFlushLn("end_of_file."); leave break mode... BUT this would resume the interrupted computation :-(
	    // waitUntilAvailable();
		interruptTasks(); // kludge
		progressMessage("Leaving doInterrupt");
	}
	protected synchronized void cleanupInterrupt(){
		sendAndFlushLn("a"); // ask the Yap tracer to abort
	}
}


