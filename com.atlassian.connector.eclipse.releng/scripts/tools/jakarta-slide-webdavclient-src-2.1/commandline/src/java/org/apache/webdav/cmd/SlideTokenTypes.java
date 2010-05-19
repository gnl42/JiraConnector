// $ANTLR 2.7.3: "Client.g" -> "ClientLexer.java"$

/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/commandline/src/java/org/apache/webdav/cmd/SlideTokenTypes.java,v 1.7 2004/08/02 15:45:50 unico Exp $
 * $Revision: 1.7 $
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


public interface SlideTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int HELP = 4;
	int QUESTION = 5;
	int EOL = 6;
	int STATUS = 7;
	int SPOOL = 8;
	int STRING = 9;
	int OFF = 10;
	int RUN = 11;
	int ECHO = 12;
	int ON = 13;
	int DEBUG = 14;
	int OPTIONS = 15;
	int CONNECT = 16;
	int OPEN = 17;
	int DISCONNECT = 18;
	int LPWD = 19;
	int PWC = 20;
	int PWD = 21;
	int LCD = 22;
	int CD = 23;
	int CC = 24;
	int LLS = 25;
	int LDIR = 26;
	int OPTIONSTRING = 27;
	int LS = 28;
	int DIR = 29;
	int MKCOL = 30;
	int MKDIR = 31;
	int MOVE = 32;
	int COPY = 33;
	int DELETE = 34;
	int DEL = 35;
	int RM = 36;
	int PROPFIND = 37;
	int PROPGET = 38;
	int QNAME = 39;
	int PROPFINDALL = 40;
	int PROPGETALL = 41;
	int PROPPATCH = 42;
	int PROPSET = 43;
	int GET = 44;
	int PUT = 45;
	int BEGIN = 46;
	int COMMIT = 47;
	int ABORT = 48;
	int LOCK = 49;
	int UNLOCK = 50;
	int LOCKS = 51;
	int GRANT = 52;
	int TO = 53;
	int DENY = 54;
	int REVOKE = 55;
	int FROM = 56;
	int ACL = 57;
	int PRINCIPALCOLLECTIONSET = 58;
	int VERSIONCONTROL = 59;
	int UPDATE = 60;
	int CHECKIN = 61;
	int CHECKOUT = 62;
	int UNCHECKOUT = 63;
	int REPORT = 64;
	int EREPORT = 65;
	int LREPORT = 66;
	int MKWS = 67;
	int EXIT = 68;
	int QUIT = 69;
	int BYE = 70;
	int SET = 71;
	int CLOSE = 72;
	int CP = 73;
	int MV = 74;
	int PROPPUT = 75;
	int PRINCIPALCOL = 76;
	int WS = 77;
	int CHARS = 78;
	int ALPHANUM = 79;
	int ALPHA = 80;
	int LOWALPHA = 81;
	int UPALPHA = 82;
	int DIGIT = 83;
}
