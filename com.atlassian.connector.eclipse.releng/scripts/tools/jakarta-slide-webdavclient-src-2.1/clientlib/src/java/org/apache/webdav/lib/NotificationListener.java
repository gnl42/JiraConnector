/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/NotificationListener.java,v 1.8.2.2 2004/12/22 18:21:30 dflorey Exp $
 * $Revision: 1.8.2.2 $
 * $Date: 2004/12/22 18:21:30 $
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

package org.apache.webdav.lib;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.webdav.lib.methods.DepthSupport;
import org.apache.webdav.lib.methods.XMLResponseMethodBase;
import org.apache.webdav.lib.util.XMLPrinter;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.AttributesImpl;

import de.zeigermann.xml.simpleImporter.DefaultSimpleImportHandler;
import de.zeigermann.xml.simpleImporter.SimpleImporter;
import de.zeigermann.xml.simpleImporter.SimplePath;

/**
 * The NotificationListener class encapsulates all methods that are
 * required for dealing with WebDAV notifications.
 * It implements poll and push based notification handling.
 * 
 */
public class NotificationListener {
    private static Logger logger = Logger.getLogger(NotificationListener.class.getName());

    protected static final Timer timer = new Timer();

    private final static int CONNECTION_TIMEOUT = 30000;
    
    private String notificationHost, repositoryHost, repositoryDomain;
    private int notificationPort, repositoryPort;
    private Protocol protocol;
    private Credentials credentials;
    private boolean udp = true;
    
    private List subscribers = new ArrayList();
    private String subscribersAsString;

    /**
     * 
     * @param host The ip-address or hostname on which the udp or http-server is running (e.g. "myhost.mydomain.mytld")
     * @param port The port where the udp or http-server is listening on (e.g. 4444)
     * @param repositoryHost The ip-adress or hostname of the WebDAV-repository
     * @param repositoryPort The port of the WebDAV-repository (e.g. 8080)
     * @param protocol The protocol that should be used to connect to the WebDAV-repository (http or https) 
     * @param credentials The credentials which are used to connect to the WebDAV-repository 
     * @param repositoryDomain The repository domain (e.g. "/slide")
     * @param pollInterval The poll interval that will be used if no notifications are revieved via UDP/TCP (in milliseconds)
     * @param udp If set to true, UDP server will be started, otherwise TCP server (must match the repository notification mode)
     */
    public NotificationListener(String host, int port, String repositoryHost, int repositoryPort, Protocol protocol, Credentials credentials, String repositoryDomain, int pollInterval, boolean udp) {
    	this.credentials = credentials;
    	this.notificationHost = host;
        this.notificationPort = port;
        this.repositoryHost = repositoryHost;
        this.repositoryPort = repositoryPort;
        this.protocol = protocol;
        this.repositoryDomain = repositoryDomain;
        this.udp = udp;
        
        if ( udp ) {
            Thread listenerThread = new Thread(new Runnable() {
                public void run() {
                    DatagramSocket serverSocket = null;
                    try {
                        serverSocket = new DatagramSocket(notificationPort);
                        while (true) {
                            byte[] buf = new byte[256];
                            DatagramPacket packet = new DatagramPacket(buf, buf.length);
                            serverSocket.receive(packet);
                            BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buf)));
                            parseNotification(reader);
                        }
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "Error while listening to socket", e);
                    }
                }
            });
            listenerThread.setDaemon(true);
            listenerThread.start();
        } else {
            Thread listenerThread = new Thread(new Runnable() {
                public void run() {
                    ServerSocket serverSocket = null;
                    try {
                        serverSocket = new ServerSocket(notificationPort);
                        while (true) {
                            new ConnectionThread(serverSocket.accept()).start();
                        }
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "Error while listening to socket", e);
                    }
                }
            });
            listenerThread.setDaemon(true);
            listenerThread.start();
        }

        TimerTask poll = new TimerTask() {
            public void run() {
                if ( subscribersAsString != null ) {
                    poll(subscribersAsString);
                }
            }
        };
        timer.schedule(poll, pollInterval, pollInterval);
    }

    /**
     * Registers a Subscriber with the remote server. 
     * 
     * @param method the "notification type", determines for what events do you
     *               want do subscribe. one of  "Update", "Update/newmember",
     *               "Delete", "Move".
     * @param uri the resource for that you subscribe
     * @param depth the depth of the collection tree that you want to observe
     * @param lifetime the duration for that you want to observe (in seconds)
     * @param notificationDelay the time the server waits before it sends a notify
     *                          message to the host provided in the constructor
     *                          (in seconds)
     * @param listener the Subscriber that is called on incomming notifications
     * @param credentials credentials for authentication on the server observed
     * @return boolean true if subscription succeeded, false if subscription failed
     *
     * @see WebdavResource#subscribeMethod
     * @see http://msdn.microsoft.com/library/default.asp?url=/library/en-us/e2k3/e2k3/_webdav_subscribe.asp
     */
    public boolean subscribe(String method, String uri, int depth, int lifetime, int notificationDelay, Subscriber listener, Credentials credentials) {
        SubscribeMethod subscribeMethod = new SubscribeMethod(repositoryDomain+uri);
        subscribeMethod.addRequestHeader(SubscribeMethod.H_NOTIFICATION_TYPE, method);
        if ( udp ) {
        	subscribeMethod.addRequestHeader(SubscribeMethod.H_CALL_BACK, "httpu://"+notificationHost+":"+notificationPort);
        } else {
        	subscribeMethod.addRequestHeader(SubscribeMethod.H_CALL_BACK, "http://"+notificationHost+":"+notificationPort);
        }
        subscribeMethod.addRequestHeader(SubscribeMethod.H_NOTIFICATION_DELAY, String.valueOf(notificationDelay));
        subscribeMethod.addRequestHeader(SubscribeMethod.H_SUBSCRIPTION_LIFETIME, String.valueOf(lifetime));
        subscribeMethod.addRequestHeader(SubscribeMethod.H_DEPTH, ((depth == DepthSupport.DEPTH_INFINITY ) ? "infinity" : String.valueOf(depth)));
        try {
            subscribeMethod.setDoAuthentication(true);
            HttpState httpState = new HttpState();
            httpState.setCredentials(null, repositoryHost, credentials);
            HttpConnection httpConnection = new HttpConnection(repositoryHost, repositoryPort, protocol);
            httpConnection.setConnectionTimeout(CONNECTION_TIMEOUT);
            int state = subscribeMethod.execute(httpState, httpConnection);
            if ( state == HttpStatus.SC_OK ) {
                String subscriptionId = subscribeMethod.getResponseHeader(SubscribeMethod.H_SUBSCRIPTION_ID).getValue();
                logger.log(Level.INFO, "Received subscription id="+subscriptionId+", listener: "+listener);
                int id = Integer.valueOf(subscriptionId).intValue();
                synchronized ( subscribers ) {
                    subscribers.add(new Subscription(id, uri, listener));
                }
                if ( subscribersAsString == null ) {
                    subscribersAsString = String.valueOf(id);
                } else {
                    subscribersAsString = subscribersAsString + ", "+String.valueOf(id);
                }
                return true;
            } else {
                logger.log(Level.SEVERE, "Subscription for uri='"+uri+"' failed. State: "+state);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Subscription of listener '"+listener+"' failed!", e);
        }
        return false;
    }

    public boolean unsubscribe(String uri, Subscriber listener, Credentials credentials) {
        UnsubscribeMethod unsubscribeMethod = new UnsubscribeMethod(repositoryDomain+uri);
        synchronized ( subscribers ) {
            for ( Iterator i = subscribers.iterator(); i.hasNext(); ) {
                Subscription subscription = (Subscription)i.next();
                if ( subscription.getSubscriber().equals(listener) ) {
                    String id = String.valueOf(subscription.getId());
                    unsubscribeMethod.addRequestHeader(UnsubscribeMethod.H_SUBSCRIPTION_ID, id);
                    try {
                        unsubscribeMethod.setDoAuthentication(true);
                        HttpState httpState = new HttpState();
                        httpState.setCredentials(null, repositoryHost, credentials);
                        HttpConnection httpConnection = new HttpConnection(repositoryHost, repositoryPort, protocol);
                        httpConnection.setConnectionTimeout(CONNECTION_TIMEOUT);
                        int state = unsubscribeMethod.execute(httpState, httpConnection);
                        if ( state == HttpStatus.SC_OK ) {
                            i.remove();
                            return true;
                        } else {
                            logger.log(Level.SEVERE, "Unsubscription failed. State: "+state);
                        }
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "Unsubscription of listener '"+listener+"' failed!", e);
                    }
                }
            }
        }
        logger.log(Level.SEVERE, "Listener not unsubscribed!");
		return false;
    }

    public void fireEvent(Map information, Credentials credentials) throws IOException  {
        EventMethod eventMethod = new EventMethod(repositoryDomain);
        eventMethod.addEvent(new Event(information));
        fireEvent(eventMethod, credentials);
    }

    public void fireVetoableEvent(Map information, Credentials credentials) throws IOException  {
        EventMethod eventMethod = new EventMethod(repositoryDomain);
        eventMethod.addVetoableEvent(new Event(information));
        fireEvent(eventMethod, credentials);
    }

    protected void fireEvent(EventMethod eventMethod, Credentials credentials) throws IOException {
        eventMethod.setDoAuthentication(true);
        HttpState httpState = new HttpState();
        httpState.setCredentials(null, repositoryHost, credentials);
        int state = eventMethod.execute(httpState, new HttpConnection(repositoryHost, repositoryPort, protocol));
        if ( state == HttpStatus.SC_OK ) {
        } else {
            logger.log(Level.SEVERE, "Event failed. State: "+state);
        }
    }

    protected void fireEvent(int id, Map information) {
        for ( Iterator i = subscribers.iterator(); i.hasNext(); ) {
            Subscription subscriber = (Subscription)i.next();
            if ( subscriber.getId() == id ) {
                subscriber.fireEvent(information);
                break;
            }
        }
    }

    protected void poll(String notifiedSubscribers) {
        StringBuffer registeredSubscribers = new StringBuffer(256);
        StringTokenizer tokenizer = new StringTokenizer(notifiedSubscribers, ",");
        boolean first = true;
        while ( tokenizer.hasMoreTokens() ) {
            String subscriber = tokenizer.nextToken().trim();
            if ( isRegistered(Integer.valueOf(subscriber).intValue()) ) {
                if ( !first ) registeredSubscribers.append(',');
                registeredSubscribers.append(subscriber);
                first = false;
            }
        }
        if ( !first ) {
            String pollSubscribers = registeredSubscribers.toString();
            logger.log(Level.INFO, "Poll for subscribers: "+pollSubscribers);
            PollMethod pollMethod = new PollMethod(repositoryDomain+"/");
            pollMethod.addRequestHeader(SubscribeMethod.H_SUBSCRIPTION_ID, pollSubscribers);
            try {
                pollMethod.setDoAuthentication(true);
                HttpState httpState = new HttpState();
                httpState.setCredentials(null, repositoryHost, credentials);
	            HttpConnection httpConnection = new HttpConnection(repositoryHost, repositoryPort, protocol);
	            httpConnection.setConnectionTimeout(CONNECTION_TIMEOUT);
                int state = pollMethod.execute(httpState, httpConnection);
                if ( state == HttpStatus.SC_MULTI_STATUS ) {
                    List events = pollMethod.getEvents();
                    for ( Iterator i = events.iterator(); i.hasNext(); ) {
                        Event event = (Event)i.next();
                        fireEvent(event.getId(), event.getInformation());
                    }
                } else {
                    logger.log(Level.SEVERE, "Poll failed. State: "+state);
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Poll for subscribers '"+subscribers+"' failed!");
            }
        }
    }
    
    private boolean isRegistered(int id) {
        for ( Iterator i = subscribers.iterator(); i.hasNext(); ) {
            Subscription subscription = (Subscription)i.next();
            if ( subscription.getId() == id ) return true;
        }
        return false;
    }

    private void parseNotification(BufferedReader reader) throws IOException {
        String inputLine;
        if ( (inputLine = reader.readLine()) != null ) {
            if ( inputLine.startsWith("NOTIFY") ) {
                while ( (inputLine = reader.readLine()) != null ) {
                    if ( inputLine.startsWith(SubscribeMethod.H_SUBSCRIPTION_ID_RESPONSE) ) {
                        String subscribers = inputLine.substring(SubscribeMethod.H_SUBSCRIPTION_ID_RESPONSE.length()+2);
                        logger.log(Level.INFO, "Notification received for subscribers: "+subscribers);
                        poll(subscribers);
                    }
                }
            }
        }
        reader.close();
    }

    public class Event {
        int id;
        Map information = new HashMap();

        public Event() {
        }

        public Event(int id) {
            this.id = id;
        }

        public Event(Map information) {
            this.information = information;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public void addInformation(String key, String value) {
            information.put(key, value);
        }

        public Map getInformation() {
            return information;
        }
    }

    private class Subscription {
        private int id;
		private String uri;
        private Subscriber subscriber;

        public Subscription(int id, String uri, Subscriber subscriber) {
            this.id = id;
            this.uri = uri;
            this.subscriber = subscriber;
        }

        public void fireEvent(Map information) {
            subscriber.notify(uri, information);
        }
	
		public Subscriber getSubscriber() {
			return subscriber;
		}
			
        public int getId() {
            return id;
        }
    }

    private class ConnectionThread extends Thread {
        private Socket socket = null;

        public ConnectionThread(Socket socket) {
            super("ConnectionThread");
            this.socket = socket;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                parseNotification(in);
                socket.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error while listening to connection", e);
            }
        }
    }

    private class PollMethod extends PutMethod {
        public static final String NAME = "POLL";

        protected final static String E_SUBSCRIPTION_ID = "subscriptionID";
        protected final static String E_LISTENER = "li";
        protected final static String E_FIRE_EVENTS = "fire-events";
        protected final static String E_EVENT = "event";
        protected final static String E_VETOABLE_EVENT = "vetoable-event";
        protected final static String E_INFORMATION = "information";
        protected final static String E_STATUS = "status";

        public final static String A_NAME = "name";

        protected final static String SUBSCRIPTION= ":"+E_SUBSCRIPTION_ID;
        protected final static String ID = E_LISTENER;
        protected final static String EVENT = ":"+E_EVENT;
        protected final static String INFORMATION = ":"+E_INFORMATION;
        protected final static String STATUS = ":"+E_STATUS;
        protected final static String STATUS_OK = "HTTP/1.1 200 OK";

        public PollMethod() {
        }

        public PollMethod(String uri) {
            super(uri);
        }

        public String getName() {
            return NAME;
        }

        public List getEvents() {
            List events = new ArrayList();
            try {
                SimpleImporter importer = new SimpleImporter();
                importer.setIncludeLeadingCDataIntoStartElementCallback(true);
                ResponseHandler handler = new ResponseHandler(events);
                importer.addSimpleImportHandler(handler);
                importer.parse(new InputSource(getResponseBodyAsStream()));
                return handler.getEvents();
            } catch (Throwable exception) {
                logger.log(Level.SEVERE, "Exception while polling for new events: ", exception);
            }
            return events;
        }

        private class ResponseHandler extends DefaultSimpleImportHandler {
            private List events;
            private int id;
            private Event event;
            private boolean parseEvents;

            public ResponseHandler(List listeners) {
                this.events = listeners;
            }

            public List getEvents() {
                return events;
            }

            public void startElement(SimplePath path, String name, AttributesImpl attributes, String leadingCDdata) {
                if (path.matches(STATUS)) {
                    parseEvents = false;
                    if ( leadingCDdata.equals(STATUS_OK) ) parseEvents = true;
                }
                if ( parseEvents ) {
                    if (path.matches(SUBSCRIPTION+"/"+ID)) {
                        id = Integer.valueOf(leadingCDdata).intValue();
                        event = new Event(id);
                        events.add(event);
                    } else if (path.matches(INFORMATION)) {
                        String key = attributes.getValue(PollMethod.A_NAME);
                        String value = leadingCDdata;
                        event.addInformation(key, value);
                    }
                }
            }
        }
    }

    private class SubscribeMethod extends PutMethod {
        public static final String NAME = "SUBSCRIBE";

        public final static String H_NOTIFICATION_TYPE = "Notification-type";
        public final static String H_NOTIFICATION_DELAY = "Notification-delay";
        public final static String H_SUBSCRIPTION_LIFETIME = "Subscription-lifetime";
        public final static String H_SUBSCRIPTION_ID = "Subscription-ID";
        public final static String H_SUBSCRIPTION_ID_RESPONSE = "Subscription-id";
        public final static String H_CALL_BACK = "Call-back";
        public final static String H_DEPTH = "Depth";
        
        public SubscribeMethod(String uri) {
            super(uri);
        }

        public String getName() {
            return NAME;
        }
    }

    private class UnsubscribeMethod extends PutMethod {
        public static final String NAME = "UNSUBSCRIBE";

        public final static String H_SUBSCRIPTION_ID = "Subscription-id";
        
        public UnsubscribeMethod(String uri) {
            super(uri);
        }

        public String getName() {
            return NAME;
        }
    }

    private class EventMethod extends XMLResponseMethodBase {
        protected final static String E_FIRE_EVENTS = "fire-events";
        protected final static String E_EVENT = "event";
        protected final static String E_VETOABLE_EVENT = "vetoable-event";
        protected final static String E_INFORMATION = "information";
        protected final static String E_STATUS = "status";

        protected final static String A_INFORMATION_KEY = "name";

        public static final String NAME = "EVENT";

        private List vetoableEvents = new ArrayList();
        private List events = new ArrayList();

        public EventMethod(String uri) {
            super(uri);
        }

        public void addEvent(Event event) {
            events.add(event);
        }

        public void addVetoableEvent(Event event) {
            vetoableEvents.add(event);
        }

        public String getName() {
            return NAME;
        }

        /**
         * DAV requests that contain a body must override this function to
         * generate that body.
         *
         * <p>The default behavior simply returns an empty body.</p>
         */
        protected String generateRequestBody() {
            XMLPrinter printer = new XMLPrinter();
            printer.writeXMLHeader();
            printer.writeElement("D", "DAV:", E_FIRE_EVENTS, XMLPrinter.OPENING);
            for ( Iterator i = events.iterator(); i.hasNext(); ) {
                Event event = (Event)i.next();
                printer.writeElement("D", E_EVENT, XMLPrinter.OPENING);
                Map information = event.getInformation();
                for ( Iterator j = information.entrySet().iterator(); j.hasNext(); ) {
                    Map.Entry entry = (Map.Entry)j.next();
                    String name = (String)entry.getKey();
                    String value = (String)entry.getValue();
                    printer.writeElement("D", E_INFORMATION+" "+A_INFORMATION_KEY+"=\""+name+"\"", XMLPrinter.OPENING);
                    printer.writeText(value);
                    printer.writeElement("D", E_INFORMATION, XMLPrinter.CLOSING);
                }
                printer.writeElement("D", E_EVENT, XMLPrinter.CLOSING);
            }
            for ( Iterator i = vetoableEvents.iterator(); i.hasNext(); ) {
                Event event = (Event)i.next();
                printer.writeElement("D", E_VETOABLE_EVENT, XMLPrinter.OPENING);
                Map information = event.getInformation();
                for ( Iterator j = information.entrySet().iterator(); j.hasNext(); ) {
                    Map.Entry entry = (Map.Entry)j.next();
                    String name = (String)entry.getKey();
                    String value = (String)entry.getValue();
                    printer.writeElement("D", E_INFORMATION+" "+A_INFORMATION_KEY+"=\""+name+"\"", XMLPrinter.OPENING);
                    printer.writeText(value);
                    printer.writeElement("D", E_INFORMATION, XMLPrinter.CLOSING);
                }
                printer.writeElement("D", E_VETOABLE_EVENT, XMLPrinter.CLOSING);
            }
            printer.writeElement("D", E_FIRE_EVENTS, XMLPrinter.CLOSING);
            return printer.toString();
        }
    }
}