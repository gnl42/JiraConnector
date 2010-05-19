package connector;

import java.io.*;

import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.HttpsURL;
import org.apache.webdav.connector.*;
import org.apache.webdav.lib.WebdavResource;

import javax.resource.ResourceException;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import javax.naming.InitialContext;
import javax.naming.Context;



/**
 * Implementation of the test servlet.
 */
public class TestServlet extends HttpServlet {
    static String HOST = "http://localhost:8888/slide/files";
    static String USER = "root";
    static String PASSWORD = "root";
    static int TIMEOUT = 10;

    // Reference to the factory
    private WebDAVConnectionFactory _factory;

    
    /**
     * <code>init()</code> stores the factory for efficiency since JNDI
     * is relatively slow.
     */
    public void init() throws ServletException {
        try {
            Context ic = new InitialContext();

            _factory = (WebDAVConnectionFactory) ic.lookup("java:comp/env/WebDAV-Connector");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    public void service(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        WebDAVConnection conn1 = null;

        UserTransaction tx = null;
        try {
            Context ic = new InitialContext();
            tx = (UserTransaction) ic.lookup("java:comp/UserTransaction");

            tx.begin();

            System.out.println("Tx: " + tx);
            out.println("Tx: " + tx + "<br>");

            System.out.println("Factory: " + _factory);
            out.println("Factory: " + _factory + "<br>");
            
            WebDAVConnectionSpec spec = new WebDAVConnectionSpec(HOST, USER, PASSWORD, TIMEOUT);
            
            conn1 = (WebDAVConnection) _factory.getConnection(spec);
            out.println("Connection1: " + conn1 + "<br>");
            System.out.println("Connection1: " + conn1);

            WebdavResource wr1 = conn1.getWebdavResource();
            // outside of any transaction:
            HttpURL httpURL = new HttpURL(HOST.toCharArray());
            httpURL.setUserinfo(USER, PASSWORD);

            WebdavResource wr2 = new WebdavResource(httpURL);

            out.println("WR1: " + wr1 + "<br>");
            System.out.println("WR1: " + wr1);
            out.println("WR2: " + wr2 + "<br>");
            System.out.println("WR2: " + wr2);

            
            wr1.putMethod(HOST+"/file1", "Content");
            String thisIsWhatTx1Sees =  wr1.getMethodDataAsString(HOST+"/file1");
            String thisIsWhatTx2Sees = wr2.getMethodDataAsString(HOST+"/file1");

            out.println("WR1 sees " + thisIsWhatTx1Sees + "<br>");
            System.out.println("WR1 sees " + thisIsWhatTx1Sees);
            out.println("WR2 sees this before commit" + thisIsWhatTx2Sees + "<br>");
            System.out.println("WR2 sees this before commit " + thisIsWhatTx2Sees);

            tx.commit();
            
            thisIsWhatTx2Sees = wr2.getMethodDataAsString(HOST+"/file1");
            out.println("WR2 sees this after commit " + thisIsWhatTx2Sees + "<br>");
            System.out.println("WR2 sees this after commit " + thisIsWhatTx2Sees);
            
        } catch (Exception e) {
            if (tx != null)
                try {
                    tx.rollback();
                } catch (IllegalStateException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (SecurityException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (SystemException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            System.out.println(e);
            e.printStackTrace();
            throw new ServletException(e);
        } finally {
            if (conn1 != null)
                try {
                    conn1.close();
                } catch (ResourceException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
        }
    }
}
