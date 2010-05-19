// $ANTLR 2.7.3: "Client.g" -> "ClientParser.java"$

/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/commandline/src/java/org/apache/webdav/cmd/ClientParser.java,v 1.9 2004/08/02 15:45:50 unico Exp $
 * $Revision: 1.9 $
 * $Date: 2004/08/02 15:45:50 $
 *
 * ====================================================================
 *
 * Copyright 1999-2002 The Apache Software Foundation 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.webdav.cmd;

import java.io.*;
import java.util.*;
import org.apache.webdav.lib.PropertyName;
import org.apache.webdav.lib.util.QName;


import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;

/**
 * The Slide WebDAV client parser.
 *
 * @version     $Revision: 1.9 $ $Date: 2004/08/02 15:45:50 $
 */
public class ClientParser extends antlr.LLkParser       implements SlideTokenTypes
 {


    // ------------------------------------------------------------- properties

    /**
     * The Slide WebDAV client.
     */
    protected Client client;
    
    // --------------------------------------------------------- helper methods

    /**
     * Set a client.
     *
     * @param client a client
     */
    void setClient(Client client) {
        this.client = client;
    }


    /**
     * Get the text from a token.
     *
     * @param token a token
     * @return the token string
     */
    private String text(Token token) {
        return (token != null) ? token.getText() : null;
    }
    

    /**
     * Get the qualified name from a token.
     *
     * @param token a token
     * @return the qualified name
     */
    private QName qname(Token token) {
        if (token == null) return null;

        String tmp = token.getText();
        if (!tmp.startsWith("<")) {
            return new PropertyName("DAV:", text(token));
        }
        String namespaceURI = tmp.substring(tmp.indexOf('"') + 1,
                tmp.lastIndexOf('"'));
        String localName = tmp.substring(1, tmp.indexOf(' '));
        
        return new QName(namespaceURI, localName);
    }

    
    /**
     * Get the property name from a token.
     *
     * @param token a token
     * @return the property name
     */
    private PropertyName pname(Token token) {
        if (token == null) return null;

        String tmp = token.getText();
        if (!tmp.startsWith("<")) {
            return new PropertyName("DAV:", text(token));
        }

        String namespaceURI = tmp.substring(tmp.indexOf('"') + 1,
                tmp.lastIndexOf('"'));
        String localName = tmp.substring(1, tmp.indexOf(' '));
        
        return new PropertyName(namespaceURI, localName);
    }

    
    /**
     * Print the usage for a given command.
     *
     * @param command a command
     */
    private void printUsage(String command)
        throws RecognitionException, TokenStreamException {

        client.printUsage(command);
        skip(); // skip the rest of the line
    }


protected ClientParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public ClientParser(TokenBuffer tokenBuf) {
  this(tokenBuf,2);
}

protected ClientParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public ClientParser(TokenStream lexer) {
  this(lexer,2);
}

public ClientParser(ParserSharedInputState state) {
  super(state,2);
  tokenNames = _tokenNames;
}

	public final void commands() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			
			client.prompt();
			
			{
			int _cnt3=0;
			_loop3:
			do {
				if ((_tokenSet_0.member(LA(1)))) {
					command();
					
					client.prompt();
					
				}
				else {
					if ( _cnt3>=1 ) { break _loop3; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt3++;
			} while (true);
			}
		}
		catch (ANTLRException ex) {
			
			// XXX bad hack for bug #28100
			if (ex.toString().indexOf("null") != -1) System.exit(-1);
			// handle parse errors gracefully
			client.print("Error: "+ex.toString());
			
		}
	}
	
	public final void command() throws RecognitionException, TokenStreamException {
		
		
		{
		switch ( LA(1)) {
		case EXIT:
		case QUIT:
		case BYE:
		{
			exit();
			break;
		}
		case HELP:
		case QUESTION:
		{
			help();
			break;
		}
		case STRING:
		{
			invalid();
			break;
		}
		case EOL:
		{
			nothing();
			break;
		}
		case SPOOL:
		{
			spool();
			break;
		}
		case RUN:
		{
			run();
			break;
		}
		case ECHO:
		{
			echo();
			break;
		}
		case DEBUG:
		{
			debug();
			break;
		}
		case STATUS:
		{
			status();
			break;
		}
		case OPTIONS:
		{
			optionsmethod();
			break;
		}
		case CONNECT:
		case OPEN:
		{
			connect();
			break;
		}
		case DISCONNECT:
		{
			disconnect();
			break;
		}
		case LPWD:
		{
			lpwd();
			break;
		}
		case LCD:
		{
			lcd();
			break;
		}
		case LLS:
		case LDIR:
		{
			lls();
			break;
		}
		case PWC:
		case PWD:
		{
			pwc();
			break;
		}
		case CD:
		case CC:
		{
			cd();
			break;
		}
		case LS:
		case DIR:
		{
			ls();
			break;
		}
		case MKCOL:
		case MKDIR:
		{
			mkcol();
			break;
		}
		case MOVE:
		{
			move();
			break;
		}
		case COPY:
		{
			copy();
			break;
		}
		case DELETE:
		case DEL:
		case RM:
		{
			delete();
			break;
		}
		case GET:
		{
			get();
			break;
		}
		case PUT:
		{
			put();
			break;
		}
		case PROPFIND:
		case PROPGET:
		{
			propfind();
			break;
		}
		case PROPFINDALL:
		case PROPGETALL:
		{
			propfindall();
			break;
		}
		case PROPPATCH:
		case PROPSET:
		{
			proppatch();
			break;
		}
		case LOCK:
		{
			lock();
			break;
		}
		case UNLOCK:
		{
			unlock();
			break;
		}
		case LOCKS:
		{
			locks();
			break;
		}
		case GRANT:
		{
			grant();
			break;
		}
		case DENY:
		{
			deny();
			break;
		}
		case REVOKE:
		{
			revoke();
			break;
		}
		case ACL:
		{
			acl();
			break;
		}
		case PRINCIPALCOLLECTIONSET:
		{
			principalcollectionset();
			break;
		}
		case VERSIONCONTROL:
		{
			versioncontrol();
			break;
		}
		case REPORT:
		{
			report();
			break;
		}
		case EREPORT:
		{
			ereport();
			break;
		}
		case LREPORT:
		{
			lreport();
			break;
		}
		case MKWS:
		{
			mkws();
			break;
		}
		case CHECKIN:
		{
			checkin();
			break;
		}
		case CHECKOUT:
		{
			checkout();
			break;
		}
		case UNCHECKOUT:
		{
			uncheckout();
			break;
		}
		case UPDATE:
		{
			update();
			break;
		}
		case BEGIN:
		{
			begin();
			break;
		}
		case COMMIT:
		{
			commit();
			break;
		}
		case ABORT:
		{
			abort();
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
	}
	
	public final void exit() throws RecognitionException, TokenStreamException {
		
		
		{
		switch ( LA(1)) {
		case EXIT:
		{
			match(EXIT);
			break;
		}
		case QUIT:
		{
			match(QUIT);
			break;
		}
		case BYE:
		{
			match(BYE);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		match(EOL);
		
		System.exit(0);
		
	}
	
	public final void help() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case HELP:
			{
				match(HELP);
				break;
			}
			case QUESTION:
			{
				match(QUESTION);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(EOL);
			
			client.help(null);
			
		}
		catch (RecognitionException ex) {
			
			printUsage("help");
			
		}
	}
	
	public final void invalid() throws RecognitionException, TokenStreamException {
		
		Token  cmd = null;
		
		cmd = LT(1);
		match(STRING);
		{
		_loop101:
		do {
			if ((LA(1)==STRING)) {
				match(STRING);
			}
			else {
				break _loop101;
			}
			
		} while (true);
		}
		match(EOL);
		
		client.printInvalidCommand(text(cmd));
		
	}
	
	public final void nothing() throws RecognitionException, TokenStreamException {
		
		
		match(EOL);
	}
	
	public final void spool() throws RecognitionException, TokenStreamException {
		
		Token  file = null;
		
		try {      // for error handling
			match(SPOOL);
			{
			switch ( LA(1)) {
			case STRING:
			{
				file = LT(1);
				match(STRING);
				break;
			}
			case OFF:
			{
				match(OFF);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(EOL);
			
			if (file != null) {
			client.enableSpoolToFile(text(file));
			} else {
			client.disableSpoolToFile();
			}
			
		}
		catch (RecognitionException ex) {
			
			printUsage("spool");
			
		}
	}
	
	public final void run() throws RecognitionException, TokenStreamException {
		
		Token  script = null;
		
		try {      // for error handling
			match(RUN);
			script = LT(1);
			match(STRING);
			match(EOL);
			
			client.executeScript(text(script));
			
		}
		catch (RecognitionException ex) {
			
			printUsage("run");
			
		}
	}
	
	public final void echo() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			match(ECHO);
			
			boolean isEnabled;
			
			{
			switch ( LA(1)) {
			case ON:
			{
				match(ON);
				
				isEnabled = true;
				
				break;
			}
			case OFF:
			{
				match(OFF);
				
				isEnabled = true;
				
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(EOL);
			
			client.setEchoEnabled(isEnabled);
			
		}
		catch (RecognitionException ex) {
			
			printUsage("echo");
			
		}
	}
	
	public final void debug() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			
			int level;
			
			match(DEBUG);
			{
			switch ( LA(1)) {
			case ON:
			{
				match(ON);
				
				level = Client.DEBUG_ON;
				
				break;
			}
			case OFF:
			{
				match(OFF);
				
				level = Client.DEBUG_OFF;
				
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(EOL);
			
			client.setDebug(level);
			
		}
		catch (RecognitionException ex) {
			
			printUsage("debug");
			
		}
	}
	
	public final void status() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			match(STATUS);
			match(EOL);
			
			client.status();
			
		}
		catch (RecognitionException ex) {
			
			printUsage("status");
			
		}
	}
	
	public final void optionsmethod() throws RecognitionException, TokenStreamException {
		
		Token  path = null;
		
		try {      // for error handling
			match(OPTIONS);
			path = LT(1);
			match(STRING);
			match(EOL);
			
			client.options(text(path));
			
		}
		catch (RecognitionException ex) {
			
			printUsage("options");
			
		}
	}
	
	public final void connect() throws RecognitionException, TokenStreamException {
		
		Token  uri = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case CONNECT:
			{
				match(CONNECT);
				break;
			}
			case OPEN:
			{
				match(OPEN);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			uri = LT(1);
			match(STRING);
			match(EOL);
			
			client.connect(text(uri));
			
		}
		catch (RecognitionException ex) {
			
			printUsage("connect");
			
		}
	}
	
	public final void disconnect() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			match(DISCONNECT);
			match(EOL);
			
			client.disconnect();
			
		}
		catch (RecognitionException ex) {
			
			printUsage("disconnect");
			
		}
	}
	
	public final void lpwd() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			match(LPWD);
			match(EOL);
			
			client.lpwd();
			
		}
		catch (RecognitionException ex) {
			
			printUsage("lpwd");
			
		}
	}
	
	public final void lcd() throws RecognitionException, TokenStreamException {
		
		Token  path = null;
		
		try {      // for error handling
			match(LCD);
			path = LT(1);
			match(STRING);
			match(EOL);
			
			client.lcd(text(path));
			
		}
		catch (RecognitionException ex) {
			
			printUsage("lcd");
			
		}
	}
	
	public final void lls() throws RecognitionException, TokenStreamException {
		
		Token  option = null;
		Token  path = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case LLS:
			{
				match(LLS);
				break;
			}
			case LDIR:
			{
				match(LDIR);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case OPTIONSTRING:
			{
				option = LT(1);
				match(OPTIONSTRING);
				break;
			}
			case EOL:
			case STRING:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case STRING:
			{
				path = LT(1);
				match(STRING);
				break;
			}
			case EOL:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(EOL);
			
			client.lls(text(option), text(path));
			
		}
		catch (RecognitionException ex) {
			
			printUsage("lls");
			
		}
	}
	
	public final void pwc() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case PWC:
			{
				match(PWC);
				break;
			}
			case PWD:
			{
				match(PWD);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(EOL);
			
			client.pwc();
			
		}
		catch (RecognitionException ex) {
			
			printUsage("pwc");
			
		}
	}
	
	public final void cd() throws RecognitionException, TokenStreamException {
		
		Token  path = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case CD:
			{
				match(CD);
				break;
			}
			case CC:
			{
				match(CC);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			path = LT(1);
			match(STRING);
			match(EOL);
			
			client.cd(text(path));
			
		}
		catch (RecognitionException ex) {
			
			printUsage("cd");
			
		}
	}
	
	public final void ls() throws RecognitionException, TokenStreamException {
		
		Token  option = null;
		Token  path = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case LS:
			{
				match(LS);
				break;
			}
			case DIR:
			{
				match(DIR);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case OPTIONSTRING:
			{
				option = LT(1);
				match(OPTIONSTRING);
				break;
			}
			case EOL:
			case STRING:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case STRING:
			{
				path = LT(1);
				match(STRING);
				break;
			}
			case EOL:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(EOL);
			
			client.ls(text(option), text(path));
			
		}
		catch (RecognitionException ex) {
			
			printUsage("ls");
			
		}
	}
	
	public final void mkcol() throws RecognitionException, TokenStreamException {
		
		Token  path = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case MKCOL:
			{
				match(MKCOL);
				break;
			}
			case MKDIR:
			{
				match(MKDIR);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			path = LT(1);
			match(STRING);
			match(EOL);
			
			client.mkcol(text(path));
			
		}
		catch (RecognitionException ex) {
			
			printUsage("mkcol");
			
		}
	}
	
	public final void move() throws RecognitionException, TokenStreamException {
		
		Token  source = null;
		Token  destination = null;
		
		try {      // for error handling
			match(MOVE);
			source = LT(1);
			match(STRING);
			destination = LT(1);
			match(STRING);
			match(EOL);
			
			client.move(text(source), text(destination));
			
		}
		catch (RecognitionException ex) {
			
			printUsage("move");
			
		}
	}
	
	public final void copy() throws RecognitionException, TokenStreamException {
		
		Token  source = null;
		Token  destination = null;
		
		try {      // for error handling
			match(COPY);
			source = LT(1);
			match(STRING);
			destination = LT(1);
			match(STRING);
			match(EOL);
			
			client.copy(text(source), text(destination));
			
		}
		catch (RecognitionException ex) {
			
			printUsage("copy");
			
		}
	}
	
	public final void delete() throws RecognitionException, TokenStreamException {
		
		Token  path = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case DELETE:
			{
				match(DELETE);
				break;
			}
			case DEL:
			{
				match(DEL);
				break;
			}
			case RM:
			{
				match(RM);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			path = LT(1);
			match(STRING);
			match(EOL);
			
			client.delete(text(path));
			
		}
		catch (RecognitionException ex) {
			
			printUsage("delete");
			
		}
	}
	
	public final void get() throws RecognitionException, TokenStreamException {
		
		Token  path = null;
		Token  file = null;
		
		try {      // for error handling
			match(GET);
			path = LT(1);
			match(STRING);
			{
			switch ( LA(1)) {
			case STRING:
			{
				file = LT(1);
				match(STRING);
				break;
			}
			case EOL:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(EOL);
			
			client.get(text(path), text(file));
			
		}
		catch (RecognitionException ex) {
			
			printUsage("get");
			
		}
	}
	
	public final void put() throws RecognitionException, TokenStreamException {
		
		Token  file = null;
		Token  path = null;
		
		try {      // for error handling
			match(PUT);
			file = LT(1);
			match(STRING);
			{
			switch ( LA(1)) {
			case STRING:
			{
				path = LT(1);
				match(STRING);
				break;
			}
			case EOL:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(EOL);
			
			client.put(text(file), text(path));
			
		}
		catch (RecognitionException ex) {
			
			printUsage("put");
			
		}
	}
	
	public final void propfind() throws RecognitionException, TokenStreamException {
		
		Token  path = null;
		Token  prop = null;
		Token  nsprop = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case PROPFIND:
			{
				match(PROPFIND);
				break;
			}
			case PROPGET:
			{
				match(PROPGET);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			path = LT(1);
			match(STRING);
			
			Vector properties = new Vector();
			
			{
			int _cnt43=0;
			_loop43:
			do {
				switch ( LA(1)) {
				case STRING:
				{
					prop = LT(1);
					match(STRING);
					
					properties.add(pname(prop));
					
					break;
				}
				case QNAME:
				{
					nsprop = LT(1);
					match(QNAME);
					
					properties.add(pname(nsprop));
					
					break;
				}
				default:
				{
					if ( _cnt43>=1 ) { break _loop43; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				}
				_cnt43++;
			} while (true);
			}
			match(EOL);
			
			client.propfind(text(path), properties);
			
		}
		catch (RecognitionException ex) {
			
			printUsage("propfind");
			
		}
	}
	
	public final void propfindall() throws RecognitionException, TokenStreamException {
		
		Token  path = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case PROPFINDALL:
			{
				match(PROPFINDALL);
				break;
			}
			case PROPGETALL:
			{
				match(PROPGETALL);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case STRING:
			{
				path = LT(1);
				match(STRING);
				break;
			}
			case EOL:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(EOL);
			
			client.propfindall(text(path));
			
		}
		catch (RecognitionException ex) {
			
			printUsage("propfindall");
			
		}
	}
	
	public final void proppatch() throws RecognitionException, TokenStreamException {
		
		Token  path = null;
		Token  prop = null;
		Token  value = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case PROPPATCH:
			{
				match(PROPPATCH);
				break;
			}
			case PROPSET:
			{
				match(PROPSET);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			path = LT(1);
			match(STRING);
			prop = LT(1);
			match(STRING);
			value = LT(1);
			match(STRING);
			match(EOL);
			
			client.proppatch(text(path), text(prop), text(value));
			
		}
		catch (RecognitionException ex) {
			
			printUsage("proppatch");
			
		}
	}
	
	public final void lock() throws RecognitionException, TokenStreamException {
		
		Token  path = null;
		Token  os1 = null;
		Token  os2 = null;
		Token  os3 = null;
		
		try {      // for error handling
			match(LOCK);
			{
			switch ( LA(1)) {
			case STRING:
			{
				path = LT(1);
				match(STRING);
				break;
			}
			case EOL:
			case OPTIONSTRING:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case OPTIONSTRING:
			{
				os1 = LT(1);
				match(OPTIONSTRING);
				{
				switch ( LA(1)) {
				case OPTIONSTRING:
				{
					os2 = LT(1);
					match(OPTIONSTRING);
					{
					switch ( LA(1)) {
					case OPTIONSTRING:
					{
						os3 = LT(1);
						match(OPTIONSTRING);
						break;
					}
					case EOL:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					break;
				}
				case EOL:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				break;
			}
			case EOL:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(EOL);
			
			
				        String[] opt = {text(os1), text(os2), text(os3), null};
				        
				        int parNr[]  = {3, 3, 3};
				        String err = null;
				            
				        for (int i = 0 ; i< opt.length ;i++) {
				            
				            if (opt[i] != null) {
			
				                if ( opt[i].toLowerCase().startsWith("-t")) {
				                    parNr[0] = i;
				                } else
			
				                if ( opt[i].toLowerCase().startsWith("-s")) {
				                    parNr[1] = i;
				                } else
				                
				                if ( opt[i].toLowerCase().startsWith("-o")) {
				                    parNr[2] = i;
				                } else {
				                    err = "Wrong parameter: "+ opt[i];
				                }
				            }
				        }
			
					if (err == null)
				client.lock(text(path), opt[parNr[0]], opt[parNr[1]], opt[parNr[2]]);
			else
				client.print(err); 	
			
		}
		catch (RecognitionException ex) {
			
			printUsage("lock");
			
		}
	}
	
	public final void unlock() throws RecognitionException, TokenStreamException {
		
		Token  path = null;
		Token  os = null;
		
		try {      // for error handling
			match(UNLOCK);
			{
			switch ( LA(1)) {
			case STRING:
			{
				path = LT(1);
				match(STRING);
				break;
			}
			case EOL:
			case OPTIONSTRING:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case OPTIONSTRING:
			{
				os = LT(1);
				match(OPTIONSTRING);
				break;
			}
			case EOL:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(EOL);
			
			String owner = text(os);
			String err = null;
				
			
				        if ((owner != null) && (!owner.toLowerCase().startsWith("-o")) ) {
			err = "Wrong parameter: "+ owner;
			}
			client.unlock(text(path), owner);
			
		}
		catch (RecognitionException ex) {
			
			printUsage("unlock");
			
		}
	}
	
	public final void locks() throws RecognitionException, TokenStreamException {
		
		Token  path = null;
		
		try {      // for error handling
			match(LOCKS);
			{
			switch ( LA(1)) {
			case STRING:
			{
				path = LT(1);
				match(STRING);
				break;
			}
			case EOL:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(EOL);
			
			client.locks(text(path));
			
		}
		catch (RecognitionException ex) {
			
			printUsage("locks");
			
		}
	}
	
	public final void grant() throws RecognitionException, TokenStreamException {
		
		Token  permission = null;
		Token  nspermisssion = null;
		Token  path = null;
		Token  principal = null;
		
		try {      // for error handling
			match(GRANT);
			{
			switch ( LA(1)) {
			case STRING:
			{
				permission = LT(1);
				match(STRING);
				break;
			}
			case QNAME:
			{
				nspermisssion = LT(1);
				match(QNAME);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case ON:
			{
				match(ON);
				path = LT(1);
				match(STRING);
				break;
			}
			case TO:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(TO);
			principal = LT(1);
			match(STRING);
			match(EOL);
			
			if (permission != null)
			client.grant(text(permission), text(path), text(principal));
			else
			client.grant(qname(nspermisssion), text(path), text(principal));
			
		}
		catch (RecognitionException ex) {
			
			client.printUsage("grant");
			
		}
	}
	
	public final void deny() throws RecognitionException, TokenStreamException {
		
		Token  permission = null;
		Token  nspermisssion = null;
		Token  path = null;
		Token  principal = null;
		
		try {      // for error handling
			match(DENY);
			{
			switch ( LA(1)) {
			case STRING:
			{
				permission = LT(1);
				match(STRING);
				break;
			}
			case QNAME:
			{
				nspermisssion = LT(1);
				match(QNAME);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case ON:
			{
				match(ON);
				path = LT(1);
				match(STRING);
				break;
			}
			case TO:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(TO);
			principal = LT(1);
			match(STRING);
			match(EOL);
			
			if (permission != null)
			client.deny(text(permission), text(path), text(principal));
			else
			client.deny(qname(nspermisssion), text(path), text(principal));
			
		}
		catch (RecognitionException ex) {
			
			client.printUsage("deny");
			
		}
	}
	
	public final void revoke() throws RecognitionException, TokenStreamException {
		
		Token  permission = null;
		Token  nspermisssion = null;
		Token  path = null;
		Token  principal = null;
		
		try {      // for error handling
			match(REVOKE);
			{
			switch ( LA(1)) {
			case STRING:
			{
				permission = LT(1);
				match(STRING);
				break;
			}
			case QNAME:
			{
				nspermisssion = LT(1);
				match(QNAME);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case ON:
			{
				match(ON);
				path = LT(1);
				match(STRING);
				break;
			}
			case FROM:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(FROM);
			principal = LT(1);
			match(STRING);
			match(EOL);
			
			if (permission != null)
			client.revoke(text(permission), text(path), text(principal));
			else
			client.revoke(qname(nspermisssion), text(path),
			text(principal));
			
		}
		catch (RecognitionException ex) {
			
			client.printUsage("revoke");
			
		}
	}
	
	public final void acl() throws RecognitionException, TokenStreamException {
		
		Token  path = null;
		
		try {      // for error handling
			match(ACL);
			{
			switch ( LA(1)) {
			case STRING:
			{
				path = LT(1);
				match(STRING);
				break;
			}
			case EOL:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(EOL);
			
			client.acl(text(path));
			
		}
		catch (RecognitionException ex) {
			
			printUsage("acl");
			
		}
	}
	
	public final void principalcollectionset() throws RecognitionException, TokenStreamException {
		
		Token  path = null;
		
		try {      // for error handling
			match(PRINCIPALCOLLECTIONSET);
			{
			switch ( LA(1)) {
			case STRING:
			{
				path = LT(1);
				match(STRING);
				break;
			}
			case EOL:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(EOL);
			
			client.principalcollectionset(text(path));
			
		}
		catch (RecognitionException ex) {
			
			printUsage("principalcollectionset");
			
		}
	}
	
	public final void versioncontrol() throws RecognitionException, TokenStreamException {
		
		Token  target = null;
		Token  path = null;
		
		try {      // for error handling
			match(VERSIONCONTROL);
			{
			if ((LA(1)==STRING) && (LA(2)==STRING)) {
				target = LT(1);
				match(STRING);
			}
			else if ((LA(1)==STRING) && (LA(2)==EOL)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			path = LT(1);
			match(STRING);
			match(EOL);
			
			if (target == null)
			client.versioncontrol(text(path));
			else
			client.versioncontrol(text(target), text(path));
			
		}
		catch (RecognitionException ex) {
			
			printUsage("versioncontrol");
			
		}
	}
	
	public final void report() throws RecognitionException, TokenStreamException {
		
		Token  path = null;
		Token  prop = null;
		Token  nsprop = null;
		
		try {      // for error handling
			match(REPORT);
			path = LT(1);
			match(STRING);
			
			Vector properties = new Vector();
			
			{
			switch ( LA(1)) {
			case STRING:
			{
				prop = LT(1);
				match(STRING);
				
				properties.add(pname(prop));
				
				break;
			}
			case QNAME:
			{
				nsprop = LT(1);
				match(QNAME);
				
				properties.add(pname(nsprop));
				break;
			}
			case EOL:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(EOL);
			
			client.report(text(path), properties);
			
		}
		catch (RecognitionException ex) {
			
			printUsage("report");
			
		}
	}
	
	public final void ereport() throws RecognitionException, TokenStreamException {
		
		Token  path = null;
		Token  filename = null;
		
		try {      // for error handling
			match(EREPORT);
			path = LT(1);
			match(STRING);
			{
			switch ( LA(1)) {
			case STRING:
			{
				filename = LT(1);
				match(STRING);
				break;
			}
			case EOL:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(EOL);
			
			client.ereport(text(path), text(filename));
			
		}
		catch (RecognitionException ex) {
			
			printUsage("ereport");
			
		}
	}
	
	public final void lreport() throws RecognitionException, TokenStreamException {
		
		Token  path = null;
		Token  prop = null;
		Token  nsprop = null;
		Token  uri = null;
		
		try {      // for error handling
			match(LREPORT);
			path = LT(1);
			match(STRING);
			
			Vector properties = new Vector();
			
			{
			int _cnt93=0;
			_loop93:
			do {
				switch ( LA(1)) {
				case STRING:
				{
					prop = LT(1);
					match(STRING);
					
					properties.add(pname(prop));
					
					break;
				}
				case QNAME:
				{
					nsprop = LT(1);
					match(QNAME);
					
					properties.add(pname(nsprop));
					
					break;
				}
				default:
				{
					if ( _cnt93>=1 ) { break _loop93; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				}
				_cnt93++;
			} while (true);
			}
			match(ON);
			
			Vector historyUris = new Vector();
			
			{
			int _cnt95=0;
			_loop95:
			do {
				if ((LA(1)==STRING)) {
					uri = LT(1);
					match(STRING);
					
					historyUris.add(text(uri));
					
				}
				else {
					if ( _cnt95>=1 ) { break _loop95; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt95++;
			} while (true);
			}
			match(EOL);
			
			client.lreport(text(path), properties, historyUris);
			
		}
		catch (RecognitionException ex) {
			
			printUsage("lreport");
			
		}
	}
	
	public final void mkws() throws RecognitionException, TokenStreamException {
		
		Token  path = null;
		
		try {      // for error handling
			match(MKWS);
			path = LT(1);
			match(STRING);
			match(EOL);
			
			client.mkws(text(path));
			
		}
		catch (RecognitionException ex) {
			
			printUsage("mkws");
			
		}
	}
	
	public final void checkin() throws RecognitionException, TokenStreamException {
		
		Token  path = null;
		
		try {      // for error handling
			match(CHECKIN);
			path = LT(1);
			match(STRING);
			match(EOL);
			
			client.checkin(text(path));
			
		}
		catch (RecognitionException ex) {
			
			printUsage("checkin");
			
		}
	}
	
	public final void checkout() throws RecognitionException, TokenStreamException {
		
		Token  path = null;
		
		try {      // for error handling
			match(CHECKOUT);
			path = LT(1);
			match(STRING);
			match(EOL);
			
			client.checkout(text(path));
			
		}
		catch (RecognitionException ex) {
			
			printUsage("checkout");
			
		}
	}
	
	public final void uncheckout() throws RecognitionException, TokenStreamException {
		
		Token  path = null;
		
		try {      // for error handling
			match(UNCHECKOUT);
			path = LT(1);
			match(STRING);
			match(EOL);
			
			client.uncheckout(text(path));
			
		}
		catch (RecognitionException ex) {
			
			printUsage("uncheckout");
			
		}
	}
	
	public final void update() throws RecognitionException, TokenStreamException {
		
		Token  path = null;
		Token  target = null;
		
		try {      // for error handling
			match(UPDATE);
			path = LT(1);
			match(STRING);
			target = LT(1);
			match(STRING);
			match(EOL);
			
			client.update(text(path), text(target));
			
		}
		catch (RecognitionException ex) {
			
			printUsage("update <path> <historyURL>");
			
		}
	}
	
	public final void begin() throws RecognitionException, TokenStreamException {
		
		Token  timeout = null;
		Token  owner = null;
		
		try {      // for error handling
			match(BEGIN);
			{
			switch ( LA(1)) {
			case STRING:
			{
				timeout = LT(1);
				match(STRING);
				{
				switch ( LA(1)) {
				case STRING:
				{
					owner = LT(1);
					match(STRING);
					break;
				}
				case EOL:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				break;
			}
			case EOL:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(EOL);
			
			client.beginTransaction(text(timeout), text(owner));
			
		}
		catch (RecognitionException ex) {
			
			printUsage("begin");
			
		}
	}
	
	public final void commit() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			match(COMMIT);
			
			client.commitTransaction();
			
		}
		catch (RecognitionException ex) {
			
			printUsage("commit");
			
		}
	}
	
	public final void abort() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			match(ABORT);
			
			client.abortTransaction();
			
		}
		catch (RecognitionException ex) {
			
			printUsage("abort");
			
		}
	}
	
	public final void skip() throws RecognitionException, TokenStreamException {
		
		
		{
		_loop105:
		do {
			switch ( LA(1)) {
			case STRING:
			{
				match(STRING);
				break;
			}
			case HELP:
			case QUESTION:
			case STATUS:
			case SPOOL:
			case OFF:
			case RUN:
			case ECHO:
			case ON:
			case DEBUG:
			case OPTIONS:
			case CONNECT:
			case OPEN:
			case DISCONNECT:
			case LPWD:
			case PWC:
			case PWD:
			case LCD:
			case CD:
			case CC:
			case LLS:
			case LDIR:
			case LS:
			case DIR:
			case MKCOL:
			case MKDIR:
			case MOVE:
			case COPY:
			case DELETE:
			case DEL:
			case RM:
			case PROPFIND:
			case PROPGET:
			case PROPFINDALL:
			case PROPGETALL:
			case PROPPATCH:
			case GET:
			case PUT:
			case BEGIN:
			case COMMIT:
			case ABORT:
			case LOCK:
			case UNLOCK:
			case LOCKS:
			case GRANT:
			case TO:
			case DENY:
			case REVOKE:
			case FROM:
			case ACL:
			case PRINCIPALCOLLECTIONSET:
			case VERSIONCONTROL:
			case UPDATE:
			case CHECKIN:
			case CHECKOUT:
			case UNCHECKOUT:
			case REPORT:
			case EREPORT:
			case LREPORT:
			case MKWS:
			case EXIT:
			case QUIT:
			case BYE:
			case SET:
			case CLOSE:
			case CP:
			case MV:
			case PROPPUT:
			case PRINCIPALCOL:
			{
				all_tokens();
				break;
			}
			default:
			{
				break _loop105;
			}
			}
		} while (true);
		}
		match(EOL);
		/* skip all */
	}
	
	public final void all_tokens() throws RecognitionException, TokenStreamException {
		
		
		switch ( LA(1)) {
		case EXIT:
		{
			match(EXIT);
			break;
		}
		case QUIT:
		{
			match(QUIT);
			break;
		}
		case BYE:
		{
			match(BYE);
			break;
		}
		case HELP:
		{
			match(HELP);
			break;
		}
		case QUESTION:
		{
			match(QUESTION);
			break;
		}
		case RUN:
		{
			match(RUN);
			break;
		}
		case SPOOL:
		{
			match(SPOOL);
			break;
		}
		case STATUS:
		{
			match(STATUS);
			break;
		}
		case ECHO:
		{
			match(ECHO);
			break;
		}
		case ON:
		{
			match(ON);
			break;
		}
		case OFF:
		{
			match(OFF);
			break;
		}
		case SET:
		{
			match(SET);
			break;
		}
		case DEBUG:
		{
			match(DEBUG);
			break;
		}
		case OPTIONS:
		{
			match(OPTIONS);
			break;
		}
		case OPEN:
		{
			match(OPEN);
			break;
		}
		case CONNECT:
		{
			match(CONNECT);
			break;
		}
		case CLOSE:
		{
			match(CLOSE);
			break;
		}
		case DISCONNECT:
		{
			match(DISCONNECT);
			break;
		}
		case LPWD:
		{
			match(LPWD);
			break;
		}
		case LCD:
		{
			match(LCD);
			break;
		}
		case LLS:
		{
			match(LLS);
			break;
		}
		case LDIR:
		{
			match(LDIR);
			break;
		}
		case PWC:
		{
			match(PWC);
			break;
		}
		case PWD:
		{
			match(PWD);
			break;
		}
		case CC:
		{
			match(CC);
			break;
		}
		case CD:
		{
			match(CD);
			break;
		}
		case LS:
		{
			match(LS);
			break;
		}
		case DIR:
		{
			match(DIR);
			break;
		}
		case GET:
		{
			match(GET);
			break;
		}
		case PUT:
		{
			match(PUT);
			break;
		}
		case MKCOL:
		{
			match(MKCOL);
			break;
		}
		case MKDIR:
		{
			match(MKDIR);
			break;
		}
		case DELETE:
		{
			match(DELETE);
			break;
		}
		case DEL:
		{
			match(DEL);
			break;
		}
		case RM:
		{
			match(RM);
			break;
		}
		case COPY:
		{
			match(COPY);
			break;
		}
		case CP:
		{
			match(CP);
			break;
		}
		case MOVE:
		{
			match(MOVE);
			break;
		}
		case MV:
		{
			match(MV);
			break;
		}
		case LOCK:
		{
			match(LOCK);
			break;
		}
		case UNLOCK:
		{
			match(UNLOCK);
			break;
		}
		case LOCKS:
		{
			match(LOCKS);
			break;
		}
		case PROPGET:
		{
			match(PROPGET);
			break;
		}
		case PROPFIND:
		{
			match(PROPFIND);
			break;
		}
		case PROPGETALL:
		{
			match(PROPGETALL);
			break;
		}
		case PROPFINDALL:
		{
			match(PROPFINDALL);
			break;
		}
		case PROPPUT:
		{
			match(PROPPUT);
			break;
		}
		case PROPPATCH:
		{
			match(PROPPATCH);
			break;
		}
		case ACL:
		{
			match(ACL);
			break;
		}
		case PRINCIPALCOL:
		{
			match(PRINCIPALCOL);
			break;
		}
		case GRANT:
		{
			match(GRANT);
			break;
		}
		case DENY:
		{
			match(DENY);
			break;
		}
		case REVOKE:
		{
			match(REVOKE);
			break;
		}
		case TO:
		{
			match(TO);
			break;
		}
		case FROM:
		{
			match(FROM);
			break;
		}
		case PRINCIPALCOLLECTIONSET:
		{
			match(PRINCIPALCOLLECTIONSET);
			break;
		}
		case VERSIONCONTROL:
		{
			match(VERSIONCONTROL);
			break;
		}
		case REPORT:
		{
			match(REPORT);
			break;
		}
		case EREPORT:
		{
			match(EREPORT);
			break;
		}
		case LREPORT:
		{
			match(LREPORT);
			break;
		}
		case MKWS:
		{
			match(MKWS);
			break;
		}
		case CHECKIN:
		{
			match(CHECKIN);
			break;
		}
		case CHECKOUT:
		{
			match(CHECKOUT);
			break;
		}
		case UNCHECKOUT:
		{
			match(UNCHECKOUT);
			break;
		}
		case UPDATE:
		{
			match(UPDATE);
			break;
		}
		case BEGIN:
		{
			match(BEGIN);
			break;
		}
		case COMMIT:
		{
			match(COMMIT);
			break;
		}
		case ABORT:
		{
			match(ABORT);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"\"help\"",
		"QUESTION",
		"EOL",
		"\"status\"",
		"\"spool\"",
		"STRING",
		"\"off\"",
		"\"run\"",
		"\"echo\"",
		"\"on\"",
		"\"debug\"",
		"\"options\"",
		"\"connect\"",
		"\"open\"",
		"\"disconnect\"",
		"\"lpwd\"",
		"\"pwc\"",
		"\"pwd\"",
		"\"lcd\"",
		"\"cd\"",
		"\"cc\"",
		"\"lls\"",
		"\"ldir\"",
		"OPTIONSTRING",
		"\"ls\"",
		"\"dir\"",
		"\"mkcol\"",
		"\"mkdir\"",
		"\"move\"",
		"\"copy\"",
		"\"delete\"",
		"\"del\"",
		"\"rm\"",
		"\"propfind\"",
		"\"propget\"",
		"QNAME",
		"\"propfindall\"",
		"\"propgetall\"",
		"\"proppatch\"",
		"PROPSET",
		"\"get\"",
		"\"put\"",
		"\"begin\"",
		"\"commit\"",
		"\"abort\"",
		"\"lock\"",
		"\"unlock\"",
		"\"locks\"",
		"\"grant\"",
		"\"to\"",
		"\"deny\"",
		"\"revoke\"",
		"\"from\"",
		"\"acl\"",
		"\"principalcollectionset\"",
		"\"versioncontrol\"",
		"\"update\"",
		"\"checkin\"",
		"\"checkout\"",
		"\"uncheckout\"",
		"\"report\"",
		"\"ereport\"",
		"\"lreport\"",
		"\"mkws\"",
		"\"exit\"",
		"\"quit\"",
		"\"bye\"",
		"\"set\"",
		"\"close\"",
		"\"cp\"",
		"\"mv\"",
		"\"propput\"",
		"\"principalcol\"",
		"WS",
		"CHARS",
		"ALPHANUM",
		"ALPHA",
		"LOWALPHA",
		"UPALPHA",
		"DIGIT"
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { -81065343182709776L, 127L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	
	}
