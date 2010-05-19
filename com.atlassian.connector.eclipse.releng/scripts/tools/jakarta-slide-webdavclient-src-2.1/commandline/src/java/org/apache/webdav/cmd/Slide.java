/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/commandline/src/java/org/apache/webdav/cmd/Slide.java,v 1.5 2004/07/28 09:30:33 ib Exp $
 * $Revision: 1.5 $
 * $Date: 2004/07/28 09:30:33 $
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


import org.apache.commons.httpclient.contrib.ssl.*;
import org.apache.commons.httpclient.protocol.Protocol;


/**
 * The Slide client, the command line version for WebDAV client.
 *
 */
public class Slide {

    /**
     * The version information for the Slide client.
     */
    public final static String version = "Slide client @VERSION@";

    public static void main(String[] args) {
        Client client = new Client(System.in,System.out);
        
        String remoteHost = null;

        ////////////  BEGIN Command line arguments //////////////
        String argOptions = null;

        // parse arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                if (argOptions != null)
                    argOptions += args[i].substring(1);
                else
                    argOptions = args[i];
            } else {
                remoteHost = args[i];
            }
        }

        // print options
        if (argOptions != null) {
            char option;
            for (int i = 0; i < argOptions.length(); i++) {
                option = argOptions.charAt(i);
                switch (option) {
                    case '-':
                        break;
                    case 'h':
                        printCmdLineUsage();
                        break;
                    case 'v':
                        System.out.println(version);
                        break;
                    case 'd':
                        client.setDebug(Client.DEBUG_ON);
                        break;
                    case 's':
                        Protocol.registerProtocol("https", 
                                                  new Protocol("https",
                                                               new EasySSLProtocolSocketFactory(), 
                                                               443));
                        break;
                    default:
                        System.exit(-1);
                }
            }
        }
        ////////////  END Command line arguments //////////////

        if (remoteHost != null) {
            client.connect(remoteHost);
        }

        client.run();
    }

    /**
     * Print the commands options from startup
     */
    private static void printCmdLineUsage()
    {

        System.out.println("Usage: Slide [-vdhs] " +
            "http://hostname[:port][/path]");
        System.out.println
            ("  Default protocol: http, port: 80, path: /");
        System.out.println("Options:");
        System.out.println("  -v: Print version information.");
        System.out.println("  -d: Debug.");
        System.out.println("  -h: Print this help message.");
        System.out.println("  -s: use EasySSLProtocol");
        System.out.println(
            "Please, email bug reports to slide-user@jakarta.apache.org");
    }
}

