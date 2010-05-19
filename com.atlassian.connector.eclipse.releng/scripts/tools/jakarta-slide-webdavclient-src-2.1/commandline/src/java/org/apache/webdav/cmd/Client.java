/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/commandline/src/java/org/apache/webdav/cmd/Client.java,v 1.21.2.2 2004/10/02 17:39:11 luetzkendorf Exp $
 * $Revision: 1.21.2.2 $
 * $Date: 2004/10/02 17:39:11 $
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

import antlr.RecognitionException;
import antlr.TokenStreamException;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.HttpsURL;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.webdav.lib.Ace;
import org.apache.webdav.lib.Lock;
import org.apache.webdav.lib.Privilege;
import org.apache.webdav.lib.Property;
import org.apache.webdav.lib.PropertyName;
import org.apache.webdav.lib.ResponseEntity;
import org.apache.webdav.lib.WebdavResource;
import org.apache.webdav.lib.methods.DepthSupport;
import org.apache.webdav.lib.methods.LockMethod;
import org.apache.webdav.lib.properties.AclProperty;
import org.apache.webdav.lib.properties.LockDiscoveryProperty;
import org.apache.webdav.lib.properties.PrincipalCollectionSetProperty;
import org.apache.webdav.lib.properties.ResourceTypeProperty;
import org.apache.webdav.lib.util.QName;


/**
 * The Slide client, the command line version for WebDAV client.
 *
 */
final class Client {


    private Spool spool;
    private InputStream in;
    private PrintStream out;
    private boolean displayPrompt = true;

    /** The path for the display information. */
    private String path = "";

    /** The command prompt for the display information. */
    private String commandPrompt = null;

    /** The http URL on the client connection. */
    private HttpURL httpURL;

    /** The debug level. */
    private int debugLevel = DEBUG_OFF;

    /** The WebDAV resource. */
    private WebdavResource webdavResource = null;

    /** The current path on the local system. */
    private File dir = new File(".");

    /////////////////////////////////////////////////////////////////

    Client(InputStream in, PrintStream out)
    {
        this.spool = new Spool(in,out);
        this.in  = in;
        this.out = new PrintStream(spool.getOutputStream());
        updatePrompt(getPath());
    }

    void run()
    {
       while(true) {
          try {
             InputStream in=spool.getInputStream();
             ClientLexer lexer = new ClientLexer(new DataInputStream(in));
             ClientParser parser = new ClientParser(lexer);
             parser.setClient(this);
             parser.commands();
          }
          catch(TokenStreamException ex) {
             handleException(ex);
          }
          catch(RecognitionException ex) {
             handleException(ex);
          }
       }
       // TODO test EOF  ???
    }

    ///////////////////////////////////////////////////////////////////
    // Helper funtions for the parser and error handlers
    ///////////////////////////////////////////////////////////////////

    void printInvalidCommand(String command)
    {
        out.println("Error: invalid command: " + command);
    }

    void printUsage(String command)
    {
        out.println("Syntax error. \"help\" for more info");
//        out.println("Usage: " + command + " ...");
//        out.println
//            ("[Help] open " +
//             "http://hostname[:port][/path]");
//        out.println("Syntax: grant <namespace> <permission> on <path> to <principal>");
//        out.println("Syntax: revoke <namespace> <permission> on <path> from <principal>");
//        out.println("Syntax: deny <namespace> <permission> on <path> to <principal>");
//        out.println("Syntax: lock <path>");
//        out.println("Syntax: unlock <path>");
//    out.println("Syntax: vc/VersionControl <path>");
//        else out.println("Syntax: eReport path");
//        else out.println("Syntax: lReport <property> \n History URLs are needed");

    }

    void prompt()
    {
        if (displayPrompt)
            out.print(getPrompt());
    }

   void print(String line) {
      out.println(line);
   }

    private void handleException(Exception ex)
    {
        if (ex instanceof HttpException) {
            if (((HttpException) ex).getReasonCode() == HttpStatus.SC_METHOD_NOT_ALLOWED) {
                out.println("Warning: Not WebDAV-enabled?");
            }
            else if (((HttpException) ex).getReasonCode() == HttpStatus.SC_UNAUTHORIZED) {
                out.println("Warning: Unauthorized");
            }
            else {
                out.println("Warning: " + ex.getMessage());
            }
        }
        else if (ex instanceof IOException) {
            out.println("Error: " + ex.getMessage());
        }
        else {
            out.println("Fatal Error: " + ex.getMessage());
            ex.printStackTrace(out);
            out.println("Please, email to slide-user@jakarta.apache.org");
            System.exit(-1);
        }
    }

    /////////////////////////////////////////////////////////////////
    // Commands available on the command line
    /////////////////////////////////////////////////////////////////

    void setEchoEnabled(boolean isEnabled)
    {
        spool.setEcho(isEnabled);
        setDisplayPrompt(isEnabled);

        // if this is an interactive client keep the prompt
        if (this.in==System.in)
            setDisplayPrompt(true);
    }

    void executeScript(String scriptname)
    {
        try {
            FileInputStream script = new FileInputStream(scriptname);
            Client scriptClient = new Client(script,out);
            scriptClient.setDisplayPrompt(false);
            out.println("Executing script: " + scriptname);
            scriptClient.run();
            out.println("Script " + scriptname + " complete.");
            script.close();
        }
        catch (FileNotFoundException ex) {
            out.println("Error: Script " + scriptname + " not found.");
        }
        catch (IOException ex) {
            out.println("Error: " + ex.toString() + " during execution of " + scriptname);
        }
    }

    void enableSpoolToFile(String filename)
    {
        out.println("Spool to file: " + filename);
        try {
            spool.enable(filename);
        }
        catch (FileNotFoundException ex) {
            out.println("Error: Could not spool to file: " + filename);
            disableSpoolToFile();
        }
    }

    void disableSpoolToFile()
    {
        spool.disable();
        out.println("Spooling disabled");
    }


    /** Debug level for all debug messages */
    final static int DEBUG_ON  = Integer.MAX_VALUE;

    /** Debug level for no debug messages */
    final static int DEBUG_OFF = 0;

    /**
     * Set the debug level
     */
    void setDebug(int level)
    {
        this.debugLevel=level;
        if (webdavResource != null) webdavResource.setDebug(debugLevel);

        switch (level) {
            case DEBUG_ON:  out.println("The debug flag is on.");  break;
            case DEBUG_OFF: out.println("The debug flag is off."); break;
            default: out.println("The debug level is " + level); break;
        }
    }

    void connect(String uri)
    {
                
        if (!uri.endsWith("/") && !uri.endsWith("\\")) {
            // append / to the path
             uri+="/";
        }

        out.println("connect " + uri);

        try {
            // Set up for processing WebDAV resources
            httpURL = uriToHttpURL(uri);
            if (webdavResource == null) {
                webdavResource = new WebdavResource(httpURL);
                webdavResource.setDebug(debugLevel);
                
                // is not a collection?
                if (!((ResourceTypeProperty)webdavResource.getResourceType()).isCollection()) {
                    webdavResource = null;
                    httpURL = null;
                    out.println("Error: " + uri + " is not a collection! Use open/connect only for collections!");
                }
                
            } else {
                webdavResource.close();
                webdavResource.setHttpURL(httpURL);
            }
            setPath(webdavResource.getPath());
        }
        catch (HttpException we) {
            out.print("HttpException.getReasonCode(): "+ we.getReasonCode());
            if (we.getReasonCode() == HttpStatus.SC_UNAUTHORIZED) {
                try {
                    out.print("UserName: ");
                    BufferedReader in =
                        new BufferedReader(new InputStreamReader(System.in));
                    String userName = in.readLine();
                    if ((userName==null) || (userName.length()==0)) {
                        disconnect();
                        return;
                    }
                    userName = userName.trim();
                    System.out.print("Password: ");
                    String password = in.readLine();
                    if (password != null)
                        password= password.trim();
                    try {
                        if (webdavResource != null)
                            webdavResource.close();
                    } catch (IOException e) {
                    } finally {
                        httpURL = null;
                        webdavResource = null;
                    }
                    httpURL = uriToHttpURL(uri);
                    // It should be used like this way.
                    httpURL.setUserinfo(userName, password);
                    webdavResource = new WebdavResource(httpURL);
                    webdavResource.setDebug(debugLevel);
                    setPath(webdavResource.getPath());


                    if (!((ResourceTypeProperty)webdavResource.getResourceType()).isCollection()) {
                        webdavResource = null;
                        httpURL = null;
                        out.println("Error: " + uri + " is not a collection! Use open/connect only for collections!");
                    }
                }
                catch (Exception ex) {
                    handleException(ex);
                    httpURL = null;
                    webdavResource = null;
                }
            }
            else  {
                handleException(we);
                httpURL = null;
                webdavResource = null;
            }
        }
        catch (Exception ex) {
            handleException(ex);
            webdavResource = null;
            httpURL = null;
        }
        updatePrompt(getPath());
    }

    void disconnect()
    {
        out.println("disconnect");
        try {
            webdavResource.close();
        } catch (IOException e) {
        } finally {
            // Make sure the connection closed.
            httpURL = null;
            webdavResource = null;
        }
        updatePrompt(getPath());
    }

    void options(String path)
    {
        out.println("options " + path);

        String param = path;
        try {
            boolean succeeded = false;
            if (param != null) {
                if (!param.startsWith("/")) {
                    httpURL = uriToHttpURL(param);
                    Enumeration enm = null;
                    try {
                        // OPTIONS business logic
                        enm =
                            webdavResource.optionsMethod(httpURL);
                        while (enm.hasMoreElements()) {
                            out.print(enm.nextElement());
                            if (enm.hasMoreElements()) {
                                out.print(", ");
                            } else {
                                out.println();
                            }
                        }
                    } catch (HttpException we) {
                        if (we.getReasonCode() ==
                            HttpStatus.SC_UNAUTHORIZED) {
                            BufferedReader in =
                                new BufferedReader(new InputStreamReader(System.in));
                            out.print("UserName: ");
                            String userName = in.readLine();
                            if (userName != null &&
                                userName.length() > 0) {
                                userName = userName.trim();
                                out.print("Password: ");
                                String password = in.readLine();
                                if (password != null)
                                    password= password.trim();
                                try {
                                    // OPTIONS business logic
                                    httpURL.setUserinfo(userName,
                                        password);
                                    enm = webdavResource.
                                        optionsMethod(httpURL);
                                    while (
                                        enm.hasMoreElements()) {
                                        out.print
                                            (enm.nextElement());
                                        if (enm.
                                            hasMoreElements()) {
                                            out.print
                                                (", ");
                                        } else {
                                            out.println();
                                        }
                                    }
                                } catch (Exception e) {
                                    out.println("Error: "
                                        + e.getMessage());
                                }
                            }
                        } else {
                            out.println("Error: " +
                                    we.getMessage());
                        }
                    } catch (IOException e) {
                        out.println(
                            "Error: Check! " + e.getMessage());
                    }
                    httpURL = null;
                    return;
                } else
                if (webdavResource != null) {
                    succeeded =
                        webdavResource.optionsMethod(param);
                } else {
                    out.println("Not connected yet.");
                }
            } else
            if (webdavResource != null) {
                succeeded = webdavResource.optionsMethod("*");
            } else {
                out.println("Not connected yet.");
            }

            if (succeeded) {
                out.print
                    ("Allowed methods by http OPTIONS: ");
                Enumeration allowed =
                    webdavResource.getAllowedMethods();
                while (allowed.hasMoreElements()) {
                    out.print(allowed.nextElement());
                    if (allowed.hasMoreElements())
                        out.print(", ");
                 }
                Enumeration davCapabilities =
                    webdavResource.getDavCapabilities();
                if (davCapabilities.hasMoreElements())
                    out.print("\nDAV: ");
                while (davCapabilities.hasMoreElements()) {
                    out.print
                        (davCapabilities.nextElement());
                    if (davCapabilities.hasMoreElements())
                        out.print(", ");
                }
                out.println();
            }
        }
        catch (Exception ex) {
            handleException(ex);
        }
    }

    void help(String topic)
    {
        if (topic==null) {
            printSlideClientUsage();
        }
        else {
            out.println("No help available on " + topic);
        }
    }

    void lpwd()
    {
        try {
            out.println(dir.getCanonicalPath());
        }
        catch (IOException ex) {
            out.println("Warning: Not found the path");
        }
    }
    
    private File getFileByPath(String path) {
        
        if (path != null) {
             // set a new file if '\' or '/' at the begin of path 
             // or ':' at the 2nd position of path exists.
             // if not: take the old parent entry of file and add a '/' to path.
             return(path.startsWith("/") ||
                    path.startsWith("\\") || 
                  ((path.length() > 1) && (path.charAt(1) == ':'))  ) ?
                    new File(path) :
                    new File(dir, "/"+path);
         } else {
             return dir;    
         }    
  
    }


    void lcd(String path)
    {
        File anotherDir = getFileByPath(path); 
                   
        if (anotherDir.isDirectory()) {
            dir = anotherDir;
        } else {
            out.println("Warning: path not found!");
        }
        
        updatePrompt(getPath());

    }
    
   

    void lls(String options, String path)
    {
        // set default option.
        char option = 'F';
        if ((options!=null) && (options.indexOf('l') > 0))
            option = 'l';
    
        File temp = getFileByPath(path); 
        

       if (!temp.exists() || !temp.isDirectory()) {
           out.println("Warning: path not found!");
           return;
        }

        String[] list = temp.list();
        // TODO: consider of options like '-a' for all and so on.
        switch (option) {
        case 'l':
            for (int i = 0; i < list.length; i++) {
                String s = list[i];
                File file = new File(temp, s);
                for (int j = 0; j < 4; j++) {
                    switch (j)  {
                        case 0:
                            // Print the filename.
                            out.print(s);
                            for (int k = list[i].length();
                                k < 35; k++)
                                out.print(" ");
                            break;
                        case 1:
                            s = Long.toString(file.length());
                            for (int k = 10 - s.length();
                                k > 0 ; k--)
                                out.print(" ");
                            // don't cut the size.
                            out.print(s + " ");
                            break;
                        case 2:
                            // cut the description.
                            s = file.isDirectory() ?
                                "DIR" : "";
                            out.print(" " +
                                ((s.length() > 5) ?
                                s.substring(0, 5) : s));
                            for (int k = s.length(); k < 5; k++)
                                out.print(" ");
                            break;
                        case 3:
                            s = new SimpleDateFormat().format(
                                new Date(file.lastModified()));
                            out.print(" " + s);
                        default:
                    }
                }
                // Start with a new line.
                out.println();
            }
            break;
        case 'F':
            int i = 0;
            for (; i < list.length; i++) {
                out.print(list[i] + " ");
                for (int j = list[i].length();
                    j < 25; j++) {
                    out.print(" ");
                }
                if (i % 3 == 2)
                   out.println();
            }
            if (list.length > 0 && i % 3 != 0) {
                out.println();
            }
            break;
        default:
        } // end of switch

    }

    void pwc()
    {
        out.println(getPath());
    }

    void cd(String path)
    {
    	String currentPath = webdavResource.getPath();
    	
        try {
            String cdPath = checkUri(path + "/");
            webdavResource.setPath(cdPath);
            
            if (webdavResource.exists()) {
                if (webdavResource.isCollection()) {
                    setPath(webdavResource.getPath());
                } else {
                    out.println("Warning: Not a collection");
                    webdavResource.setPath(currentPath);
                }
            } else {
                out.println("Warning: Not found the path");
                webdavResource.setPath(currentPath);
            }
        }
        catch (Exception ex) {
            handleException(ex);
            try {
                webdavResource.setPath(currentPath);    
            } catch (Exception e) {
                handleException(e);
            }

        }

        updatePrompt(getPath());
    }

    void ls(String options, String path)
    {
        // set default option.
        char option = 'F';
        if ((options!=null) && (options.indexOf('l') > 0))
            option = 'l';

        try {
            // Check that the path is ok.
            if (path != null) {
                path = checkUri(path + "/");
                webdavResource.setPath(path);
            } else {
                path = checkUri("./");
                webdavResource.setPath(path);
            }
            switch (option) {
                
            case 'l':

                Vector basiclist = webdavResource.listBasic();
                for (int i = 0; i < basiclist.size(); i++) {
                    String[] longFormat =
                        (String []) basiclist.elementAt(i);
                        
                        // name -> position 4
                        String s = longFormat[4];
                        int len = s.length();
                        out.print(s);
                        for (int k = len; k < 20; k++)
                            out.print(" ");
                        

                        // size -> position 1            
                        s = longFormat[1];
                        len = s.length();
                        for (int k = 10 - len;
                            k > 0 ; k--)
                            out.print(" ");
                        // don't cut the size.
                        out.print(s + " ");
                                
                                
                        // description   -> position 2                                
                        // cut the description.
                        s = longFormat[2];
                        len = s.length();

                        out.print(" " +
                            ((len > 20) ?
                            s.substring(0, 20) : s));
                        for (int k = len; k < 20; k++)
                            out.print(" ");
                            
                        // date -> position 3                            
                        s = longFormat[3];
                        len = s.length();
                        out.println(" " + s);
                    }
                    
                    
                
                break;
                
                
            case 'F':
                String[] list = webdavResource.list();
                if (list != null) {
                    int i = 0;
                    for (; i < list.length; i++) {
                        out.print(list[i] + " ");
                        for (int j = list[i].length(); j < 25; j++) {
                            out.print(" ");
                        }
                        if (i % 3 == 2)
                           out.println();
                    }
                    if (list.length > 0 && i % 3 != 0)
                        out.println();
                }
                break;
            default:
            } // end of switch
        }
        catch (Exception ex) {
            handleException(ex);
        }
    }

    void propfind(String path, Vector properties)
    {
        try {
            path = checkUri(path);
            out.print("Getting properties '" + path + "': ");

            Enumeration propertyValues =
                webdavResource.propfindMethod(path, properties);
            if (propertyValues.hasMoreElements()){
                while (propertyValues.hasMoreElements()){
                    out.println(propertyValues.nextElement());
                }
            }
            else {
                out.println("failed.");
                out.println(webdavResource.getStatusMessage());
            }
        }
        catch (Exception ex) {
            handleException(ex);
        }
    }

    void status()
    {
      out.println(webdavResource.getStatusMessage());
    }

    void propfindall(String path)
    {
        try {
            path=checkUri(path);
            out.print("Getting properties '" + path + "': ");
            Enumeration responses = webdavResource.propfindMethod(path, DepthSupport.DEPTH_0);
            if (!responses.hasMoreElements()) {
                out.println("failed (no response received).");
                out.println(webdavResource.getStatusMessage());
                return;
            }
            else {
                out.println();
            }
            ResponseEntity response = (ResponseEntity) responses.nextElement();
            Enumeration properties = response.getProperties();
            while (properties.hasMoreElements()){
                Property property = (Property)properties.nextElement();
                out.println("   " + property.getName() + " : " + property.getPropertyAsString());
            }
        }
        catch (Exception ex) {
            handleException(ex);
        }
    }

    void proppatch(String path, String prop, String value)
    {
        String name=prop;
        try {
            path=checkUri(path);
            out.print("Putting property(" + name + ", " + value +
                ") to '" + path + "': ");
            if (webdavResource.proppatchMethod(
                path, new PropertyName("DAV:",name), value, true)) {
                out.println("succeeded.");
            } else {
                out.println("failed.");
                out.println(webdavResource.getStatusMessage());
            }
        }
        catch (Exception ex) {
            handleException(ex);
        }
    }
    
    String getLocalTragetFileName(String path, String filename) {
        
        String srcFileName = null;
        String tarFileName = null;
 

        // get traget filename from last portion of path
        StringTokenizer st = new StringTokenizer(path, "/\\");
        while (st.hasMoreTokens()) {
            srcFileName = st.nextToken();
        }
                
        File targetFile = getFileByPath((filename != null) ? filename : srcFileName); 
        
        try {           
            if (targetFile.isDirectory()) {
                tarFileName = targetFile.getCanonicalPath() + "/"+ srcFileName;    
            } else {
                tarFileName = targetFile.getCanonicalPath();    
            }         
        } catch (IOException e) {
            System.err.println(e.toString());
            return null;
        }           

        return tarFileName;
    }

    void get(String path, String filename)
    {
        
        filename = getLocalTragetFileName( path,  filename);
        
        try {
            // The resource on the remote.
            String src = checkUri(path);
            // The file on the local.
            String dest = (filename!=null)
                ? filename
                : URIUtil.getName(src.endsWith("/")
                                  ? src.substring(0, src.length() - 1)
                                  : src);

            out.println("get " + src + " " + dest);

            // For the overwrite operation;
            String y = "y";
            File file = new File(dest);
            // Checking the local file.
            if (file.exists()) {

                // FIXME: interactive ?
                out.print("Aleady exists. " +
                    "Do you want to overwrite it(y/n)? ");
                BufferedReader in =
                    new BufferedReader(new InputStreamReader(System.in));
                y = in.readLine();
            }
            if (y.trim().equalsIgnoreCase("y") ||
                (y != null && y.length() == 0)) {
                out.print("Downloading  '" + src +
                    "' to '" + dest + "': ");
                if (webdavResource.getMethod(src, file)) {
                    out.println("succeeded.");
                } else {
                    out.println("failed.");
                    out.println(webdavResource.getStatusMessage());
                }
            }
        }
        catch (Exception ex) {
            handleException(ex);
        }
    }
    
    String getRemoteTragetFileName(String filename, String path) {
        
        String srcPathName = null;
        String target = null;
       
 

        // get traget filename from last portion of filename
        StringTokenizer st = new StringTokenizer(filename, "/\\");
        while (st.hasMoreTokens()) {
            srcPathName = st.nextToken();
        }
        
        
        try {
                
            if (path != null) {
                target = checkUri(path);
                
                // check is path a collection ?
                String currentPath = webdavResource.getPath();
                
                webdavResource.setPath(target);
                
                if (webdavResource.exists()) {
                    if (webdavResource.isCollection()) {
                        target += "/" + srcPathName;
                    } 
                } 
                
                webdavResource.setPath(currentPath);
                
            } else {
                target = checkUri(getPath() + "/" + srcPathName);
            }
                
                            
        } catch (Exception ex) {
        }
        
        return target;
               

    }
    
    


    void put(String filename, String path)
    {
        String y = "y";

        try {
            String src  = filename;
            String dest = getRemoteTragetFileName( filename,  path);
            
            String currentPath = webdavResource.getPath();
            
            try {
                webdavResource.setPath(dest);
                if (webdavResource.exists()) {
                    out.print("Aleady exists. " +
                              "Do you want to overwrite it(y/n)? ");
                    BufferedReader in =
                          new BufferedReader(new InputStreamReader(System.in));
                    y = in.readLine();
                }
                webdavResource.setPath(currentPath);
            } catch (Exception ex) {
            } 
            
            if (y.trim().equalsIgnoreCase("y") ||
                  (y != null && y.length() == 0)) {
 
            
                File file = getFileByPath(src);
                
                if (file.exists()) {
                    out.print("Uploading  '" + file.getCanonicalPath() + "' to '" + dest + "' ");
                    
                    if (webdavResource.putMethod(dest, file)) {
                        out.println("succeeded.");
                    }
                    else {
                        out.println("failed.");
                        out.println(webdavResource.getStatusMessage());
                    }
                }
                else {
                    out.println("Warning: File not exists");
                }
            }
        }
        catch (Exception ex) {
            handleException(ex);
        }
    }

    // TODO multi
    void delete(String path)
    {
        try {
            path = checkUri(path);
            out.print("Deleting '" + path + "': ");
            if (webdavResource.deleteMethod(path)) {
                out.println("succeeded.");
            } else {
                out.println("failed.");
                out.println(webdavResource.getStatusMessage());
            }
        }
        catch (Exception ex) {
            handleException(ex);
        }
    }

    // TODO multi
    void mkcol(String path)
    {
        try {
            path = checkUri(path);
            out.print("Making '" + path + "' collection: ");
            if (webdavResource.mkcolMethod(path)) {
                out.println("succeeded.");
            } else {
                out.println("failed.");
                out.println(webdavResource.getStatusMessage());
            }
        }
        catch (Exception ex) {
            handleException(ex);
        }
    }

    void copy(String src, String dst)
    {
        try {
            src = checkUri(src);
            dst = checkUri(dst);
            out.print("Copying '" + src + "' to '" + dst + "': ");
            if (webdavResource.copyMethod(src, dst)) {
                out.println("succeeded.");
            } else {
                out.println("failed.");
                out.println(webdavResource.getStatusMessage());
            }
        }
        catch (Exception ex) {
            handleException(ex);
        }
    }

    void move(String src, String dst)
    {
        try {
            src = checkUri(src);
            dst = checkUri(dst);
            out.print("Moving '" + src + "' to '" + dst + "': ");
            if (webdavResource.moveMethod(src, dst)) {
                out.println("succeeded.");
            } else {
                out.println("failed.");
                out.println(webdavResource.getStatusMessage());
            }
        }
        catch (Exception ex) {
            handleException(ex);
        }
    }

    void locks(String path)
    {
        try {
            path=checkUri(path);
            LockDiscoveryProperty lockDiscoveryProperty=webdavResource.lockDiscoveryPropertyFindMethod(path);
            if (lockDiscoveryProperty==null) {
                out.println("Server did not return a LockDiscoveryProperty.");
                out.println(webdavResource.getStatusMessage());
                return;
            }
            Lock[] locks=lockDiscoveryProperty.getActiveLocks();
            showLocks(path,locks);
        }
        catch (Exception ex) {
            handleException(ex);
        }
    }

    void lock(String path, String timeout, String scope, String owner)
    {
       try {
            // Test the parameter
            //
            // Timeout
            int to=0; 
            if ((timeout != null) && (timeout.toLowerCase().substring(0,5).equals("-tinf")) ) { //infinite
                to = LockMethod.TIMEOUT_INFINITY;   
            } else {
                to = (timeout == null)? 120 : Integer.parseInt(timeout.substring(2));
            }
            
            
            // scope
            short scopeType = ((scope != null) && (scope.substring(2).toLowerCase().equals("shared")) ) ?
                LockMethod.SCOPE_SHARED : LockMethod.SCOPE_EXCLUSIVE;
           
            // owner
            owner = (owner != null) ? (owner.substring(2)) : owner;
 
            path = checkUri(path);
            out.print("Locking '" + path + "' "); 
            out.print( (owner != null) ? "owner:'"+ owner + "' " : "");
            out.print("scope:" +scopeType+" timeout:"+to+ " :");
            if (webdavResource.lockMethod(path, owner, to, scopeType)) {
                out.println("succeeded.");
            } else {
                out.println("failed.");
                out.println(webdavResource.getStatusMessage());
            }
        }
        catch (Exception ex) {
            handleException(ex);
        }
    }

    
    void unlock(String path, String owner)
    {
        try {
            // Test the parameter
            //
            // owner
            owner = (owner != null) ? (owner.substring(2)) : owner;
            
            path = checkUri(path);
            out.print("Unlocking '" + path + "'");
            out.print((owner!=null)? (" owner:"+owner+": ") : (": "));
            if (webdavResource.unlockMethod(path, owner)) {
                out.println("succeeded.");
            } else {
                out.println("failed.");
                out.println(webdavResource.getStatusMessage());
            }
        }
        catch (Exception ex) {
            handleException(ex);
        }
    }


    void grant(String permission,String path, String principal)
    {
        grant(new QName("DAV:",permission),path,principal);

        // FIME lookup permission ?
//                if (namespace==null) {
//            namespace=resolveNamespace(permission);
//            if (namespace==null) {
//                out.println("Could not resolve namespace for permission " + permission);
//                continue;
//            }
//        }
    }

    void grant(QName permission,String path, String principal)
    {
        try  {
            principal=checkPrincipal(principal,webdavResource,path);
            grant(webdavResource, permission, path, principal);
        }
        catch (Exception ex) {
            handleException(ex);
        }
    }

    void deny(String permission,String path, String principal)
    {
        deny(new QName("DAV:",permission),path,principal);
    }

    void deny(QName permission,String path, String principal)
    {
        try  {
            principal=checkPrincipal(principal,webdavResource,path);
            deny(webdavResource, permission, path, principal);
        }
        catch (Exception ex) {
            handleException(ex);
        }
    }

    void revoke(String permission,String path, String principal)
    {
        revoke(new QName("DAV:",permission),path,principal);
    }

    void revoke(QName permission,String path, String principal)
    {
        try  {
            principal=checkPrincipal(principal,webdavResource,path);
            revoke(webdavResource, permission, path, principal);
        }
        catch (Exception ex) {
            handleException(ex);
        }
    }

    void acl(String path)
    {
        try
        {
            AclProperty acl=null;
            if (path!=null) {
                path=checkUri(path);
            }
            else {
                path=webdavResource.getPath();
            }
            acl = webdavResource.aclfindMethod(path);

            if (acl==null)
            {
                out.println("Error: PropFind didn't return an AclProperty!");
                return;
            }
            out.println();
            showAces(path, acl.getAces());
        }
        catch (Exception ex)
        {
            handleException(ex);
        }
    }

    void principalcollectionset(String path)
    {
        try
        {
            PrincipalCollectionSetProperty set=null;
            if (path!=null) {
                path=checkUri(path);
            }
            else {
                path=webdavResource.getPath();
            }
            set = webdavResource.principalCollectionSetFindMethod(path);

            if (set==null)
            {
                out.println("Error: PropFind didn't return an PrincipalCollectionSetProperty!");
                return;
            }
            String[] hrefs=set.getHrefs();
            if (hrefs==null) {
                out.println("No PrincipalCollectionSet for " + path);
            }
            out.println();
            out.println("PrincipalCollectionSet for " + path + ":");
            for (int i=0 ; i<hrefs.length ; i++)
                out.println("   " + hrefs[i]);
            out.println();
        }
        catch (Exception ex)
        {
        }
    }

    ///////////////////////////////////////////////////////////////////

    void checkin(String path)
    {
        try {
            path = checkUri(path);
            out.print("checking in '" + path + "': ");
            if (webdavResource.checkinMethod(path)) {
                out.println("succeeded.");
            } else {
                out.println("failed.");
                out.println(webdavResource.getStatusMessage());
            }
        }
        catch (Exception ex) {
            handleException(ex);
        }
    }

    void checkout(String path)
    {
        try {
            path = checkUri(path);
            out.print("checking out '" + path + "': ");
            if (webdavResource.checkoutMethod(path)) {
                out.println("succeeded.");
            } else {
                out.println("failed.");
                out.println(webdavResource.getStatusMessage());
            }
        }
        catch (Exception ex) {
            handleException(ex);
        }
    }

    void uncheckout(String path)
    {
        try {
            path = checkUri(path);
            out.print("unchecking out '" + path + "': ");
            if (webdavResource.uncheckoutMethod(path)) {
                out.println("succeeded.");
            } else {
                out.println("failed.");
                out.println(webdavResource.getStatusMessage());
            }
        }
        catch (Exception ex) {
            handleException(ex);
        }
    }


    void versioncontrol(String path)
    {
        try {
            path = checkUri(path);
            out.print("setting up VersionControl '" + path + "': ");
            if (webdavResource.versionControlMethod(path)) {
                out.println("succeeded.");
            } else {
                out.println("failed.");
                out.println(webdavResource.getStatusMessage());
            }
        }
        catch (Exception ex) {
            handleException(ex);
        }
    }

    void versioncontrol(String target, String path)
    {
        try {
            path = checkUri(path);
            out.print("creating versioncontroled Resource '" + target + "' based on '" + path + "' : ");
            if (webdavResource.versionControlMethod(path, target)) {
                out.println("succeeded.");
            } else {
                out.println("failed.");
                out.println(webdavResource.getStatusMessage());
            }
        }
        catch (Exception ex) {
            handleException(ex);
        }
    }

    void report(String path, Vector properties)
    {
        Enumeration propertyValues;
        try {
            path = checkUri(path);
            out.println("Getting version-tree Report of '" + path + "':");

            if ((properties!=null) && (properties.size()>0)) {
                propertyValues =
                    webdavResource.reportMethod(uriToHttpURL(path), properties, 1);
            }
            else  {
                propertyValues =
                    webdavResource.reportMethod(uriToHttpURL(path), 1);
            }

            if (propertyValues.hasMoreElements()){
                while (propertyValues.hasMoreElements()){
                    out.println(propertyValues.nextElement().toString());
                }
            }
            else {
                out.println("failed.");
                out.println(webdavResource.getStatusMessage());
            }
        }
        catch (Exception ex) {
            handleException(ex);
        }
    }

    void ereport(String path, String srcFilename)
    {
        try {
            String sQuery ="";
            if (srcFilename==null) {
                sQuery = "<D:expand-property xmlns:D='DAV:'><D:property name='version-history'><D:property name='version-set'><D:property name='successor-set'><D:property name='comment'/></D:property></D:property></D:property></D:expand-property>";
            }
            else  {
                File file = new File(dir.getCanonicalPath(), srcFilename);
                if (!file.exists()) {
                    out.println("report src file not found");
                    return;
                }
                InputStream isQuery = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(isQuery));

                while (reader.ready()) {
                    sQuery += reader.readLine();
                }
                reader.close();

                sQuery.replace('\t',' ');
                out.println (sQuery);
            }

            path = checkUri(path);
            out.println("expand-property Report of '" + path + "':");

            Enumeration propertyValues =
                webdavResource.reportMethod(uriToHttpURL(path), sQuery, 1);
            if (propertyValues.hasMoreElements()){
                while (propertyValues.hasMoreElements()){
                    out.println(displayXML(propertyValues.nextElement().toString(), 0));
                }
            }
            else {
                out.println("failed.");
                out.println(webdavResource.getStatusMessage());
            }
        }
        catch (Exception ex) {
            handleException(ex);
        }
    }

    /**
     * lreport path properties on historyUris
     */
    void lreport(String path, Vector properties, Vector historyUris)
    {
        try {
            path = checkUri(path);
            out.println("Getting version-tree Report of '" + path + "':");

            Enumeration propertyValues =
                webdavResource.reportMethod(uriToHttpURL(path), properties, historyUris, 1);
            if (propertyValues.hasMoreElements()) {
                while (propertyValues.hasMoreElements()) {
                    out.println(propertyValues.nextElement().toString());
                }
            }
            else {
                out.println("failed.");
                out.println(webdavResource.getStatusMessage());
            }
        }
        catch (Exception ex) {
            handleException(ex);
        }
    }

    void mkws(String path)
    {
        try {
            path = checkUri(path);
            out.print("Making '" + path + "' workspace: ");
            if (webdavResource.mkWorkspaceMethod(path)) {
                out.println("succeeded.");
            }
            else {
                out.println("failed.");
                out.println(webdavResource.getStatusMessage());
            }
        }
        catch (Exception ex) {
            handleException(ex);
        }
    }


    /**
     * Update
     *
     * @param path
     * @param version
     */
    void update(String path, String version){
        try {
            path = checkUri(path);
            out.print("Updateing resource " + path + ": ");
            if (webdavResource.updateMethod(path, version)) {
                out.println("succeeded.");
            } else {
                out.println("failed!");
                out.println(webdavResource.getStatusMessage());
            }
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    void beginTransaction(String timeout, String owner)
    {
        try {
            checkUri(null);

            // Test the parameter
            //
            // Timeout
            int to=0; 
            if ((timeout != null) && (timeout.toLowerCase().substring(0,5).equals("-tinf")) ) { //infinite
                to = LockMethod.TIMEOUT_INFINITY;   
            } else {
                to = (timeout == null)? 120 : Integer.parseInt(timeout.substring(2));
            }
            
            // owner
            owner = (owner != null) ? (owner.substring(2)) : owner;
 
            out.print("Starting transaction "); 
            if (webdavResource.startTransaction(owner, to)) {
                out.println("succeeded.");
                out.println("Handle: '"+webdavResource.getTransactionHandle()+"'");
            } else {
                out.println("failed.");
                out.println(webdavResource.getStatusMessage());
            }
        }
        catch (Exception ex) {
            handleException(ex);
        }
    }

    void commitTransaction()
    {
        try {
            checkUri(null);

            out.print("Committing transaction: '" + webdavResource.getTransactionHandle() + "' ");
            if (webdavResource.commitTransaction()) {
                out.println("succeeded.");
            } else {
                out.println("failed.");
                out.println(webdavResource.getStatusMessage());
            }
        }
        catch (Exception ex) {
            handleException(ex);
        }
    }
    
    void abortTransaction()
    {
        try {
            checkUri(null);

            out.print("Rolling back transaction: '" +webdavResource.getTransactionHandle()+ "' ");
            if (webdavResource.abortTransaction()) {
                out.println("succeeded.");
            } else {
                out.println("failed.");
                out.println(webdavResource.getStatusMessage());
            }
        }
        catch (Exception ex) {
            handleException(ex);
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    // Support methods
    ///////////////////////////////////////////////////////////////////

    private void setDisplayPrompt(boolean displayPrompt)
    {
        this.displayPrompt = displayPrompt;
    }

    /**
     * Determine which URI to use at the prompt.
     *
     * @param uri the path to be set.
     * @return the absolute path.
     */
    private String checkUri(String uri) throws IOException
    {

        if (webdavResource == null) {
            throw new IOException("Not connected yet.");
        }

        if (uri==null) {
            uri=webdavResource.getPath();
        }

        if (!uri.startsWith("/")) {
            uri = getPath() + uri;
        }

        return normalize(uri);
    }

    private String checkPrincipal(String principal, WebdavResource webdavResource, String path) throws HttpException,IOException
    {
        // Do not complete reserved words
        String[] types={"all","authenticated","unauthenticated","property","self"};
        for (int i=0; i<types.length ; i++)
        {
            if (types[i].equals(principal))
                return principal;
        }

        // FIXME: when search support is complete enhance this
        //        to really search for the principal
        if (!principal.startsWith("/") && !principal.startsWith("http")) {
            PrincipalCollectionSetProperty set = webdavResource.principalCollectionSetFindMethod(path);
            if ((set!=null) && (set.getHrefs()!=null) && (set.getHrefs().length==1))
                principal = set.getHrefs()[0] + "/" + principal;
        }
        return normalize(principal);
    }

    /**
     * Set the path.
     *
     * @param path the path string.
     */
    private void setPath(String path)
    {
        if (!path.endsWith("/")) {
            path = path + "/";
        }

        this.path = normalize(path);
    }


    /**
     * Get the path.
     *
     * @return the path string.
     */
    private String getPath()
    {
        return path;
    }


    /**
     * Update the command prompt for the display.
     *
     * @param path the path string
     */
    private void updatePrompt(String path)
    {
        StringBuffer buff = new StringBuffer();
        try {
            buff.append("[" + httpURL.getHost().toUpperCase() + ":" );
            buff.append(path+ "] ");
            buff.append(dir.getCanonicalPath());
            
        } catch (Exception e) {
            buff.append("[ Slide ]");
        }
        buff.append(" $ ");
        commandPrompt = buff.toString();
    }


    /**
     * Get the prompt.
     *
     * @return the prompt to be displayed.
     */
    private String getPrompt()
    {
        return commandPrompt;
    }


    /**
     * Return a context-relative path, beginning with a "/", that represents
     * the canonical version of the specified path after ".." and "." elements
     * are resolved out.  If the specified path attempts to go outside the
     * boundaries of the current context (i.e. too many ".." path elements
     * are present), return <code>null</code> instead.
     *
     * @param path the path to be normalized.
     * @return the normalized path.
     */
    private String normalize(String path)
    {
        if (path == null)
            return null;

        String normalized = path;

        // Normalize the slashes and add leading slash if necessary
        if (normalized.indexOf('\\') >= 0)
            normalized = normalized.replace('\\', '/');
        if (!normalized.startsWith("/"))
            normalized = "/" + normalized;

        // Resolve occurrences of "/./" in the normalized path
        while (true) {
            int index = normalized.indexOf("/./");
            if (index < 0)
            break;
            normalized = normalized.substring(0, index) +
            normalized.substring(index + 2);
        }

        // Resolve occurrences of "/../" in the normalized path
        while (true) {
            int index = normalized.indexOf("/../");
            if (index < 0)
            break;
            if (index == 0)
            return ("/");  // The only left path is the root.
            int index2 = normalized.lastIndexOf('/', index - 1);
            normalized = normalized.substring(0, index2) +
            normalized.substring(index + 3);
        }

        // Resolve occurrences of "//" in the normalized path
        while (true) {
            int index = normalized.indexOf("//");
            if (index < 0)
            break;
            normalized = normalized.substring(0, index) +
            normalized.substring(index + 1);
        }

        // Return the normalized path that we have completed
        return (normalized);
    }

    /**
     * Print the Slide client commands for use
     */
    private void printSlideClientUsage()
    {
        out.println("Commands:");
        out.println("  options {http_URL|path}       " +
            "Print available http methods");
        out.println("  open [http_URL]               " +
            "Connect to specified URL");
        out.println("  close                         " +
            "Close current connection");
        out.println("  exit                          " +
            "Exit Slide");
        out.println("  help                          " +
            "Print this help message");
        out.println("  debug {ON|OFF}                " +
            "set debugmode");
        out.println("  lpwd                          " +
            "Print local working directory");
        out.println("  lcd [path]                    " +
            "Change local working directory");
        out.println("  lls [-lF] [path]              " +
            "List contents of local directory");
        out.println("  pwc                           " +
            "Print working collection");
        out.println("  cc [path]                     " +
            "Change working collection");
        out.println("  ls [-lF] [path]               " +
            "List contents of collection");
        out.println("  url                           " +
            "Print working URL");
        out.println("  status                        " +
            "Print latest http status message");
        out.println("  get path [file]               " +
            "Get a resource to a file");
        out.println("  put {URL|file} [path]         " +
            "Put a given file or URL to path");
        out.println("  mkcol path ...                " +
            "Make new collections");
        out.println("  delete path ...               " +
            "Delete resources");
        out.println("  copy source destination       " +
            "Copy resource from source to destination path");
        out.println("  move source destination       " +
            "Move resource from source to destination path");
        out.println("  lock path [-t(xxx|inf)] "       +
            "[-s(SHARED|EXCLUSIVE)] [-oOWNER]\n"       +
            "                                "         +
            "Lock specified resource.\n"               +
            "                                "         +
            "default:\n"                               +
            "                                "         +
            "lock file -t120 -sexclusive with current owner");
        out.println("  unlock path [-oOWNER]         " +
            "Unlock specified resource.\n"+
            "                                "         +
            "default:\n"                               +
            "                                "         +
            "unlock file with current owner");
        out.println("  locks [<path>]                " +
            "Displays locks on specified resource");
        out.println("  propget path property ...     " +
            "Print value of specified property");
        out.println("  propgetall [<path>]           " +
            "Print value of all properties");
        out.println("  propput path property value   " +
            "Set property with given value");
        out.println("  set URLencode {on|off}        " +
            "Set URL encoding flag, default: on");
        out.println("  set debug {on|off}            " +
            "Set debug level, default: off");
        out.println("  acl [<path>]                  " +
            "Displays the ACL of path");
        out.println("  principalcol [<path>]         " +
            "Displays the principal collection set of path");
        out.println("  grant  [<namespace>] <permission> [on <path>] to <principal>");
        out.println("  deny   [<namespace>] <permission> [on <path>] to <principal>");
        out.println("  revoke [<namespace>] <permission> [on <path>] from <principal>");
        out.println("  versioncontrol path           "+
            "set a versionable resource under versioncontrol");
        out.println("  versioncontrol URL path       "+
            "create a new versioncontrolled resource at path based on history URL");
        out.println("  checkout path                 "+
            "checkout of a checkedin resource");
        out.println("  checkin path                  "+
            "checkin of a checkedout resource");
        out.println("  uncheckout path               "+
            "undoing changes on Resource since checkedout (including checkout)");
        out.println("  report path [<property>]      "+
            "getting report (version-tree) for any resource");
        out.println("  eReport path                  "+
            "getting report (expand-property) for any resource");
        out.println("  LReport path [<property>] ON [<historyuri>] "+
            "getting report (locate-by-history)");
        out.println("  mkws path ...                 " +
            "Make new workspace");
        out.println("  update path target                 " +
            "Update a resource identified by path to version identified by target");
        out.println("  begin                          starts a new transaction (only if server supports this)");
        out.println("  commit                         commits the transaction started by begin (only if server supports this)");
        out.println("  abort                          aborts and rolls back the transaction started by begin (only if server supports this)");
        out.println
            ("Aliases: help=?, open=connect, ls=dir, pwc=pwd, cc=cd, " +
             "lls=ldir, copy=cp,\n move=mv, delete=del=rm, mkcol=mkdir, " +
             "propget=propfind, propput=proppatch,\n exit=quit=bye");
        out.println("Comment : Once executed, the debug mode will " +
                           "be active.\n\t\tBecause it's not triggered by " +
                           "methods yet.");
    }


    /**
     * Sleep
     */
//    private static void pause(int secs)
//    {
//
//        try {
//            Thread.sleep( secs * 1000 );
//        } catch( InterruptedException ex ) {
//        }
//    }

//    private String resolveNamespace(String permission)
//    {
//        String DEFAULT_NAMESPACE = "DAV:";
//        String SLIDE_NAMESPACE = "http://jakarta.apache.org/slide/";
//        String namespace=null;
//
//        if (permission==null)
//            return null;
//
//        if ((permission.equalsIgnoreCase("all")) ||
//            (permission.equalsIgnoreCase("read")) ||
//            (permission.equalsIgnoreCase("write")) ||
//            (permission.equalsIgnoreCase("read-acl")) ||
//            (permission.equalsIgnoreCase("write-acl")))
//        {
//            namespace=DEFAULT_NAMESPACE;
//        }
//
//        if ((permission.equalsIgnoreCase("read-object")) ||
//            (permission.equalsIgnoreCase("read-revision-metadata")) ||
//            (permission.equalsIgnoreCase("read-revision-content")) ||
//            (permission.equalsIgnoreCase("create-object")) ||
//            (permission.equalsIgnoreCase("remove-object")) ||
//            (permission.equalsIgnoreCase("lock-object")) ||
//            (permission.equalsIgnoreCase("read-locks")) ||
//            (permission.equalsIgnoreCase("create-revision-metadata")) ||
//            (permission.equalsIgnoreCase("modify-revision-metadata")) ||
//            (permission.equalsIgnoreCase("remove-revision-metadata")) ||
//            (permission.equalsIgnoreCase("create-revision-content")) ||
//            (permission.equalsIgnoreCase("modify-revision-content")) ||
//            (permission.equalsIgnoreCase("remove-revision-content")) ||
//            (permission.equalsIgnoreCase("grant-permission")) ||
//            (permission.equalsIgnoreCase("revoke-permission")))
//        {
//            namespace=SLIDE_NAMESPACE;
//        }
//
//        return namespace;
//    }

    private boolean grant(WebdavResource webdavResource, QName permission, String path, String principal) throws HttpException, IOException
    {
        out.println("grant " + permission + " on " + path + " to " + principal);
        return addPermission(webdavResource, permission, path, principal, false);
    }

    private boolean deny(WebdavResource webdavResource, QName permission, String path, String principal) throws HttpException, IOException
    {
        out.println("deny " + permission + " on " + path + " to " + principal);
        return addPermission(webdavResource, permission, path, principal, true);
    }

    private boolean revoke(WebdavResource webdavResource, QName permission, String path, String principal) throws HttpException, IOException
    {
        out.println("revoke " + permission + " on " + path + " from " + principal);
        return removePermission(webdavResource, permission, path, principal);
    }

    private boolean addPermission(WebdavResource webdavResource, QName permission, String path, String principal, boolean negative) throws HttpException, IOException
    {

        AclProperty acl = webdavResource.aclfindMethod(path);
        if (acl==null)
        {
            out.println("Error: PropFind didn't return an AclProperty!");
            return false;
        }
        Ace[] aces=acl.getAces();

        if (aces==null)
            aces=new Ace[0];

        if (debugLevel>5) {
            out.println();
            out.println("ACL from server");
            showAces(path, aces);
        }

        Ace ace=null;
        for (int i=0; i<aces.length && (ace==null); i++)
        {
            if ((aces[i].isNegative()==negative) && !aces[i].isProtected()
                && !aces[i].isInherited() && aces[i].getPrincipal().equals(principal))
            {
                if (debugLevel>5)
                    out.println("found ace");
                ace=aces[i];
            }
        }
        if (ace==null)
        {
            Ace[] oldAces=aces;
            aces=new Ace[oldAces.length+1];
            System.arraycopy(oldAces,0,aces,0,oldAces.length);
            ace=new Ace(principal, negative, false, false,null);
            aces[oldAces.length]=ace;
        }

        Privilege privilege=new Privilege(permission.getNamespaceURI(), permission.getLocalName(), null);
        ace.addPrivilege(privilege);

        if (debugLevel>5) {
            out.println();
            out.println("ACL with updated privileges");
            showAces(path, aces);
        }

        boolean success = webdavResource.aclMethod(path,aces);

        if (!success)
            out.println(webdavResource.getStatusMessage());

        if (debugLevel>5) {
            acl = webdavResource.aclfindMethod(path);
            if (acl==null)
                out.println("Error: PropFind didn't return an AclProperty!");
            else
            {
                aces=acl.getAces();
                out.println();
                out.println("ACL from server after update");
                showAces(path, aces);
            }
        }

        return success;
    }

    private boolean removePermission(WebdavResource webdavResource, QName permission, String path, String principal) throws HttpException, IOException
    {
        AclProperty acl = webdavResource.aclfindMethod(path);
        if (acl==null)
        {
            out.println("Error: PropFind didn't return an AclProperty!");
            return false;
        }
        Ace[] aces=acl.getAces();

        if (aces==null)
        {
            out.println("Privilege not found");
            return false;
        }

        if (debugLevel>5) {
            out.println();
            out.println("ACL from server");
            showAces(path, aces);
        }

        boolean found=false;
        Privilege privilege=new Privilege(permission.getNamespaceURI(), permission.getLocalName(), null);
        for (int i=0; i<aces.length; i++)
        {
            if (!aces[i].isProtected() && !aces[i].isInherited() && aces[i].getPrincipal().equals(principal))
            {
                if (debugLevel>5)
                    out.println("found ace");
                boolean removed = aces[i].removePrivilege(privilege);
                found = found || removed;
                if (removed)
                    out.println("Privilege removed");
            }
        }

        if (!found)
        {
            out.println("Privilege not found");
            return false;
        }

        if (debugLevel>5) {
            out.println();
            out.println("ACL with updated privileges");
            showAces(path, aces);
        }

        boolean success = webdavResource.aclMethod(path,aces);

        if (!success)
            out.println(webdavResource.getStatusMessage());

        if (debugLevel>5) {
            acl = webdavResource.aclfindMethod(path);
            if (acl==null)
                out.println("Error: PropFind didn't return an AclProperty!");
            else
            {
                aces=acl.getAces();
                out.println();
                out.println("ACL from server after update");
                showAces(path, aces);
            }
        }

        return success;
    }

    private void showAces(String path, Ace[] aces)
    {
        if ((aces==null) || (aces.length==0)) {
            out.println("ACL for " + path + " is empty.");
            return;
        }

        out.println("ACL for " + path + ":");
        out.println("------------------------------------------------------------");
        for (int i=0; i<aces.length ; i++)
        {
            Ace ace=aces[i];
            out.println((!ace.isNegative()?"granted":"denied") +
                " to " + ace.getPrincipal() + " " +
                "   (" + (ace.isProtected()?"protected":"not protected") + ")" +
                "   (" + (ace.isInherited()? ("inherited from '" + ace.getInheritedFrom() + "'"): "not inherited") +")");

            Enumeration privileges=ace.enumeratePrivileges();
            while (privileges.hasMoreElements())
            {
                Privilege priv=(Privilege)privileges.nextElement();
                out.println("   " + priv.getNamespace() + priv.getName() + "   " + (priv.getParameter()==null?"":("("+priv.getParameter()+")")));
            }
        }
        out.println("------------------------------------------------------------");
    }

    private void showLocks(String path, Lock[] locks)
    {
        if ((locks==null) || (locks.length==0)) {
            out.println("No locks on " + path);
            return;
        }

        out.println("Locks for " + path + ":");
        out.println("------------------------------------------------------------");
        for (int i=0; i<locks.length ; i++)
        {
            int lockScope = locks[i].getLockScope();
            if (lockScope==Lock.SCOPE_EXCLUSIVE) {
                out.print("Exclusive ");
            }
            else if (lockScope==Lock.SCOPE_SHARED) {
                out.print("Shared ");
            }
            else if (lockScope==-1) {
                out.print("Unknown scope ");
            }
            else {
                out.println("!!! Internal error !!!");
                return;
            }

            int lockType = locks[i].getLockType();
            if (lockType==Lock.TYPE_WRITE) {
                out.println("write lock");
            }
            else if (lockType==-1) {
                out.println("unknown type");
            }
            else {
                out.println("!!! Internal error !!!");
                return;
            }

            int depth=locks[i].getDepth();
            if (depth==DepthSupport.DEPTH_INFINITY) {
                out.println("   depth: infinity");
            }
            else if (depth==-1) {
                // unknown
            }
            else {
                out.println("   depth: " + depth);
            }

            String owner=locks[i].getOwner();
            if (owner!=null)
                out.println("   owner: " + owner);

            int timeout=locks[i].getTimeout();
            if (timeout!=-1)
                out.println("   timeout: " + timeout);

            String token=locks[i].getLockToken();
            if (token!=null)
                out.println("   token: " + token);
        }
    }

    private String displayXML(String xmlString, int count)
    {
        String sResult ="";

        if(xmlString.startsWith("</")) {
            count --;
            //out.println("cl: " + count);
            for (int cc = count; cc > 0; cc--) {
                sResult += "\t";
            }

            try {
                sResult += xmlString.substring(0, xmlString.indexOf(">") + 1)+"\n";
                xmlString = xmlString.substring(xmlString.indexOf(">") + 1);
                //count --;
                sResult += displayXML(xmlString, count);
            }
            catch (Exception any) {
                //sResult += "endtag" + any;
            }

        }
        else if (xmlString.startsWith("<")) {
            //out.println("op: " + count);
            for (int cc = count; cc > 0; cc--) {
                sResult += "\t";
            }
            try {
                sResult += xmlString.substring(0, xmlString.indexOf(">") + 1)+"\n";
                xmlString = xmlString.substring(xmlString.indexOf(">") + 1);
                count ++;
                sResult += displayXML(xmlString, count);
            }
            catch (Exception any) {
                //sResult += "starttag" + any;
            }
        }
        else {
            //out.println("em: " + count);
            for (int cc = count; cc > 0; cc--) {
                sResult += "\t";
            }
            try {
                sResult += xmlString.substring(0, xmlString.indexOf("<"))+ "\n";
                xmlString = xmlString.substring(xmlString.indexOf("<"));
                sResult += displayXML(xmlString, count);
            }
            catch (Exception any) {
                //sResult += any;
            }
        }
        return sResult;
    }

    private static HttpURL uriToHttpURL(String uri) throws URIException {
        return uri.startsWith("https") ? new HttpsURL(uri)
                                       : new HttpURL(uri);
    }
}
