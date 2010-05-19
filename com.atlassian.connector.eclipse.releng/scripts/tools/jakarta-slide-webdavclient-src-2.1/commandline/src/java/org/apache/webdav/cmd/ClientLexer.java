// $ANTLR 2.7.3: "Client.g" -> "ClientLexer.java"$

/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/commandline/src/java/org/apache/webdav/cmd/ClientLexer.java,v 1.9 2004/08/02 15:45:51 unico Exp $
 * $Revision: 1.9 $
 * $Date: 2004/08/02 15:45:51 $
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


import java.io.InputStream;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.TokenStreamRecognitionException;
import antlr.CharStreamException;
import antlr.CharStreamIOException;
import antlr.ANTLRException;
import java.io.Reader;
import java.util.Hashtable;
import antlr.CharScanner;
import antlr.InputBuffer;
import antlr.ByteBuffer;
import antlr.CharBuffer;
import antlr.Token;
import antlr.CommonToken;
import antlr.RecognitionException;
import antlr.NoViableAltForCharException;
import antlr.MismatchedCharException;
import antlr.TokenStream;
import antlr.ANTLRHashString;
import antlr.LexerSharedInputState;
import antlr.collections.impl.BitSet;
import antlr.SemanticException;

/**
 * The Slide WebDAV client scanner.
 *
 * @version     $Revision: 1.9 $ $Date: 2004/08/02 15:45:51 $
 */
public class ClientLexer extends antlr.CharScanner implements SlideTokenTypes, TokenStream
 {
public ClientLexer(InputStream in) {
	this(new ByteBuffer(in));
}
public ClientLexer(Reader in) {
	this(new CharBuffer(in));
}
public ClientLexer(InputBuffer ib) {
	this(new LexerSharedInputState(ib));
}
public ClientLexer(LexerSharedInputState state) {
	super(state);
	caseSensitiveLiterals = false;
	setCaseSensitive(true);
	literals = new Hashtable();
	literals.put(new ANTLRHashString("spool", this), new Integer(8));
	literals.put(new ANTLRHashString("copy", this), new Integer(33));
	literals.put(new ANTLRHashString("mkdir", this), new Integer(31));
	literals.put(new ANTLRHashString("uncheckout", this), new Integer(63));
	literals.put(new ANTLRHashString("delete", this), new Integer(34));
	literals.put(new ANTLRHashString("abort", this), new Integer(48));
	literals.put(new ANTLRHashString("grant", this), new Integer(52));
	literals.put(new ANTLRHashString("off", this), new Integer(10));
	literals.put(new ANTLRHashString("acl", this), new Integer(57));
	literals.put(new ANTLRHashString("help", this), new Integer(4));
	literals.put(new ANTLRHashString("lcd", this), new Integer(22));
	literals.put(new ANTLRHashString("ldir", this), new Integer(26));
	literals.put(new ANTLRHashString("mv", this), new Integer(74));
	literals.put(new ANTLRHashString("lock", this), new Integer(49));
	literals.put(new ANTLRHashString("to", this), new Integer(53));
	literals.put(new ANTLRHashString("versioncontrol", this), new Integer(59));
	literals.put(new ANTLRHashString("mkws", this), new Integer(67));
	literals.put(new ANTLRHashString("mkcol", this), new Integer(30));
	literals.put(new ANTLRHashString("from", this), new Integer(56));
	literals.put(new ANTLRHashString("dir", this), new Integer(29));
	literals.put(new ANTLRHashString("propfind", this), new Integer(37));
	literals.put(new ANTLRHashString("checkin", this), new Integer(61));
	literals.put(new ANTLRHashString("quit", this), new Integer(69));
	literals.put(new ANTLRHashString("open", this), new Integer(17));
	literals.put(new ANTLRHashString("locks", this), new Integer(51));
	literals.put(new ANTLRHashString("status", this), new Integer(7));
	literals.put(new ANTLRHashString("exit", this), new Integer(68));
	literals.put(new ANTLRHashString("unlock", this), new Integer(50));
	literals.put(new ANTLRHashString("rm", this), new Integer(36));
	literals.put(new ANTLRHashString("set", this), new Integer(71));
	literals.put(new ANTLRHashString("lls", this), new Integer(25));
	literals.put(new ANTLRHashString("proppatch", this), new Integer(42));
	literals.put(new ANTLRHashString("propput", this), new Integer(75));
	literals.put(new ANTLRHashString("commit", this), new Integer(47));
	literals.put(new ANTLRHashString("propgetall", this), new Integer(41));
	literals.put(new ANTLRHashString("close", this), new Integer(72));
	literals.put(new ANTLRHashString("principalcollectionset", this), new Integer(58));
	literals.put(new ANTLRHashString("run", this), new Integer(11));
	literals.put(new ANTLRHashString("deny", this), new Integer(54));
	literals.put(new ANTLRHashString("report", this), new Integer(64));
	literals.put(new ANTLRHashString("put", this), new Integer(45));
	literals.put(new ANTLRHashString("connect", this), new Integer(16));
	literals.put(new ANTLRHashString("propget", this), new Integer(38));
	literals.put(new ANTLRHashString("revoke", this), new Integer(55));
	literals.put(new ANTLRHashString("echo", this), new Integer(12));
	literals.put(new ANTLRHashString("disconnect", this), new Integer(18));
	literals.put(new ANTLRHashString("debug", this), new Integer(14));
	literals.put(new ANTLRHashString("move", this), new Integer(32));
	literals.put(new ANTLRHashString("bye", this), new Integer(70));
	literals.put(new ANTLRHashString("cd", this), new Integer(23));
	literals.put(new ANTLRHashString("cp", this), new Integer(73));
	literals.put(new ANTLRHashString("propfindall", this), new Integer(40));
	literals.put(new ANTLRHashString("del", this), new Integer(35));
	literals.put(new ANTLRHashString("get", this), new Integer(44));
	literals.put(new ANTLRHashString("lpwd", this), new Integer(19));
	literals.put(new ANTLRHashString("cc", this), new Integer(24));
	literals.put(new ANTLRHashString("on", this), new Integer(13));
	literals.put(new ANTLRHashString("options", this), new Integer(15));
	literals.put(new ANTLRHashString("begin", this), new Integer(46));
	literals.put(new ANTLRHashString("ereport", this), new Integer(65));
	literals.put(new ANTLRHashString("pwd", this), new Integer(21));
	literals.put(new ANTLRHashString("principalcol", this), new Integer(76));
	literals.put(new ANTLRHashString("update", this), new Integer(60));
	literals.put(new ANTLRHashString("checkout", this), new Integer(62));
	literals.put(new ANTLRHashString("lreport", this), new Integer(66));
	literals.put(new ANTLRHashString("ls", this), new Integer(28));
	literals.put(new ANTLRHashString("pwc", this), new Integer(20));
}

public Token nextToken() throws TokenStreamException {
	Token theRetToken=null;
tryAgain:
	for (;;) {
		Token _token = null;
		int _ttype = Token.INVALID_TYPE;
		resetText();
		try {   // for char stream error handling
			try {   // for lexical error handling
				switch ( LA(1)) {
				case '\t':  case ' ':
				{
					mWS(true);
					theRetToken=_returnToken;
					break;
				}
				case '\n':  case '\r':
				{
					mEOL(true);
					theRetToken=_returnToken;
					break;
				}
				case '-':
				{
					mOPTIONSTRING(true);
					theRetToken=_returnToken;
					break;
				}
				case '!':  case '"':  case '#':  case '$':
				case '%':  case '&':  case '(':  case ')':
				case '+':  case '.':  case '/':  case '0':
				case '1':  case '2':  case '3':  case '4':
				case '5':  case '6':  case '7':  case '8':
				case '9':  case ':':  case 'A':  case 'B':
				case 'C':  case 'D':  case 'E':  case 'F':
				case 'G':  case 'H':  case 'I':  case 'J':
				case 'K':  case 'L':  case 'M':  case 'N':
				case 'O':  case 'P':  case 'Q':  case 'R':
				case 'S':  case 'T':  case 'U':  case 'V':
				case 'W':  case 'X':  case 'Y':  case 'Z':
				case '\\':  case '_':  case 'a':  case 'b':
				case 'c':  case 'd':  case 'e':  case 'f':
				case 'g':  case 'h':  case 'i':  case 'j':
				case 'k':  case 'l':  case 'm':  case 'n':
				case 'o':  case 'p':  case 'q':  case 'r':
				case 's':  case 't':  case 'u':  case 'v':
				case 'w':  case 'x':  case 'y':  case 'z':
				{
					mSTRING(true);
					theRetToken=_returnToken;
					break;
				}
				case '<':
				{
					mQNAME(true);
					theRetToken=_returnToken;
					break;
				}
				case '?':
				{
					mQUESTION(true);
					theRetToken=_returnToken;
					break;
				}
				default:
				{
					if (LA(1)==EOF_CHAR) {uponEOF(); _returnToken = makeToken(Token.EOF_TYPE);}
				else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				}
				if ( _returnToken==null ) continue tryAgain; // found SKIP token
				_ttype = _returnToken.getType();
				_ttype = testLiteralsTable(_ttype);
				_returnToken.setType(_ttype);
				return _returnToken;
			}
			catch (RecognitionException e) {
				throw new TokenStreamRecognitionException(e);
			}
		}
		catch (CharStreamException cse) {
			if ( cse instanceof CharStreamIOException ) {
				throw new TokenStreamIOException(((CharStreamIOException)cse).io);
			}
			else {
				throw new TokenStreamException(cse.getMessage());
			}
		}
	}
}

	public final void mWS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = WS;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case ' ':
		{
			match(' ');
			break;
		}
		case '\t':
		{
			match('\t');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		
		_ttype = Token.SKIP;
		
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mEOL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = EOL;
		int _saveIndex;
		
		if ((LA(1)=='\r') && (LA(2)=='\n')) {
			match("\r\n");
		}
		else if ((LA(1)=='\r') && (true)) {
			match('\r');
		}
		else if ((LA(1)=='\n')) {
			match('\n');
		}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mOPTIONSTRING(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = OPTIONSTRING;
		int _saveIndex;
		
		match('-');
		{
		int _cnt112=0;
		_loop112:
		do {
			if ((_tokenSet_0.member(LA(1)))) {
				mCHARS(false);
			}
			else {
				if ( _cnt112>=1 ) { break _loop112; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
			}
			
			_cnt112++;
		} while (true);
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mCHARS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = CHARS;
		int _saveIndex;
		
		switch ( LA(1)) {
		case 'a':  case 'b':  case 'c':  case 'd':
		case 'e':  case 'f':  case 'g':  case 'h':
		case 'i':  case 'j':  case 'k':  case 'l':
		case 'm':  case 'n':  case 'o':  case 'p':
		case 'q':  case 'r':  case 's':  case 't':
		case 'u':  case 'v':  case 'w':  case 'x':
		case 'y':  case 'z':
		{
			matchRange('a','z');
			break;
		}
		case 'A':  case 'B':  case 'C':  case 'D':
		case 'E':  case 'F':  case 'G':  case 'H':
		case 'I':  case 'J':  case 'K':  case 'L':
		case 'M':  case 'N':  case 'O':  case 'P':
		case 'Q':  case 'R':  case 'S':  case 'T':
		case 'U':  case 'V':  case 'W':  case 'X':
		case 'Y':  case 'Z':
		{
			matchRange('A','Z');
			break;
		}
		case '0':  case '1':  case '2':  case '3':
		case '4':  case '5':  case '6':  case '7':
		case '8':  case '9':
		{
			matchRange('0','9');
			break;
		}
		case '.':
		{
			match('.');
			break;
		}
		case ':':
		{
			match(':');
			break;
		}
		case '/':
		{
			match('/');
			break;
		}
		case '$':
		{
			match('$');
			break;
		}
		case '#':
		{
			match('#');
			break;
		}
		case '%':
		{
			match('%');
			break;
		}
		case '&':
		{
			match('&');
			break;
		}
		case '(':
		{
			match('(');
			break;
		}
		case ')':
		{
			match(')');
			break;
		}
		case '!':
		{
			match('!');
			break;
		}
		case '+':
		{
			match('+');
			break;
		}
		case '\\':
		{
			match('\\');
			break;
		}
		case '_':
		{
			match('_');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSTRING(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = STRING;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case '!':  case '#':  case '$':  case '%':
		case '&':  case '(':  case ')':  case '+':
		case '.':  case '/':  case '0':  case '1':
		case '2':  case '3':  case '4':  case '5':
		case '6':  case '7':  case '8':  case '9':
		case ':':  case 'A':  case 'B':  case 'C':
		case 'D':  case 'E':  case 'F':  case 'G':
		case 'H':  case 'I':  case 'J':  case 'K':
		case 'L':  case 'M':  case 'N':  case 'O':
		case 'P':  case 'Q':  case 'R':  case 'S':
		case 'T':  case 'U':  case 'V':  case 'W':
		case 'X':  case 'Y':  case 'Z':  case '\\':
		case '_':  case 'a':  case 'b':  case 'c':
		case 'd':  case 'e':  case 'f':  case 'g':
		case 'h':  case 'i':  case 'j':  case 'k':
		case 'l':  case 'm':  case 'n':  case 'o':
		case 'p':  case 'q':  case 'r':  case 's':
		case 't':  case 'u':  case 'v':  case 'w':
		case 'x':  case 'y':  case 'z':
		{
			mCHARS(false);
			{
			_loop116:
			do {
				switch ( LA(1)) {
				case '!':  case '#':  case '$':  case '%':
				case '&':  case '(':  case ')':  case '+':
				case '.':  case '/':  case '0':  case '1':
				case '2':  case '3':  case '4':  case '5':
				case '6':  case '7':  case '8':  case '9':
				case ':':  case 'A':  case 'B':  case 'C':
				case 'D':  case 'E':  case 'F':  case 'G':
				case 'H':  case 'I':  case 'J':  case 'K':
				case 'L':  case 'M':  case 'N':  case 'O':
				case 'P':  case 'Q':  case 'R':  case 'S':
				case 'T':  case 'U':  case 'V':  case 'W':
				case 'X':  case 'Y':  case 'Z':  case '\\':
				case '_':  case 'a':  case 'b':  case 'c':
				case 'd':  case 'e':  case 'f':  case 'g':
				case 'h':  case 'i':  case 'j':  case 'k':
				case 'l':  case 'm':  case 'n':  case 'o':
				case 'p':  case 'q':  case 'r':  case 's':
				case 't':  case 'u':  case 'v':  case 'w':
				case 'x':  case 'y':  case 'z':
				{
					mCHARS(false);
					break;
				}
				case '-':
				{
					match('-');
					break;
				}
				default:
				{
					break _loop116;
				}
				}
			} while (true);
			}
			break;
		}
		case '"':
		{
			_saveIndex=text.length();
			match('"');
			text.setLength(_saveIndex);
			{
			_loop118:
			do {
				if ((_tokenSet_1.member(LA(1)))) {
					matchNot('"');
				}
				else {
					break _loop118;
				}
				
			} while (true);
			}
			_saveIndex=text.length();
			match('"');
			text.setLength(_saveIndex);
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		String txt = new String(text.getBuffer(),_begin,text.length()-_begin);
		txt = txt.replace('\\', '/');
		text.setLength(_begin); text.append(txt);
		
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mQNAME(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = QNAME;
		int _saveIndex;
		
		match('<');
		mSTRING(false);
		match(" xmlns=\"");
		mSTRING(false);
		match("\">");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mALPHANUM(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ALPHANUM;
		int _saveIndex;
		
		switch ( LA(1)) {
		case 'A':  case 'B':  case 'C':  case 'D':
		case 'E':  case 'F':  case 'G':  case 'H':
		case 'I':  case 'J':  case 'K':  case 'L':
		case 'M':  case 'N':  case 'O':  case 'P':
		case 'Q':  case 'R':  case 'S':  case 'T':
		case 'U':  case 'V':  case 'W':  case 'X':
		case 'Y':  case 'Z':  case 'a':  case 'b':
		case 'c':  case 'd':  case 'e':  case 'f':
		case 'g':  case 'h':  case 'i':  case 'j':
		case 'k':  case 'l':  case 'm':  case 'n':
		case 'o':  case 'p':  case 'q':  case 'r':
		case 's':  case 't':  case 'u':  case 'v':
		case 'w':  case 'x':  case 'y':  case 'z':
		{
			mALPHA(false);
			break;
		}
		case '0':  case '1':  case '2':  case '3':
		case '4':  case '5':  case '6':  case '7':
		case '8':  case '9':
		{
			mDIGIT(false);
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mALPHA(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ALPHA;
		int _saveIndex;
		
		switch ( LA(1)) {
		case 'a':  case 'b':  case 'c':  case 'd':
		case 'e':  case 'f':  case 'g':  case 'h':
		case 'i':  case 'j':  case 'k':  case 'l':
		case 'm':  case 'n':  case 'o':  case 'p':
		case 'q':  case 'r':  case 's':  case 't':
		case 'u':  case 'v':  case 'w':  case 'x':
		case 'y':  case 'z':
		{
			mLOWALPHA(false);
			break;
		}
		case 'A':  case 'B':  case 'C':  case 'D':
		case 'E':  case 'F':  case 'G':  case 'H':
		case 'I':  case 'J':  case 'K':  case 'L':
		case 'M':  case 'N':  case 'O':  case 'P':
		case 'Q':  case 'R':  case 'S':  case 'T':
		case 'U':  case 'V':  case 'W':  case 'X':
		case 'Y':  case 'Z':
		{
			mUPALPHA(false);
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mDIGIT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DIGIT;
		int _saveIndex;
		
		matchRange('0','9');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mLOWALPHA(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LOWALPHA;
		int _saveIndex;
		
		matchRange('a','z');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mUPALPHA(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = UPALPHA;
		int _saveIndex;
		
		matchRange('A','Z');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mQUESTION(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = QUESTION;
		int _saveIndex;
		
		match('?');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	
	private static final long[] mk_tokenSet_0() {
		long[] data = new long[1025];
		data[0]=576403002173161472L;
		data[1]=576460746263625726L;
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = new long[2048];
		data[0]=-17179869192L;
		for (int i = 1; i<=1023; i++) { data[i]=-1L; }
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	
	}
