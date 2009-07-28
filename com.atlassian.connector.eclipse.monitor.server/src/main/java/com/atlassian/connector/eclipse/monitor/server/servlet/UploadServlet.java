package com.atlassian.connector.eclipse.monitor.server.servlet;

import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.mylyn.monitor.core.InteractionEvent;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;
import com.thoughtworks.xstream.core.BaseException;

/**
 * Servlet implementation class UploadServlet
 */
public class UploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(UploadServlet.class);
	
	// XStream is thread safe (based on their web site)
	private final XStream xs;

    /**
     * Default constructor. 
     */
    public UploadServlet() {
		xs = new XStream();
		xs.registerConverter(new AbstractSingleValueConverter() {
			@SuppressWarnings("unchecked")
			public boolean canConvert(Class type) {
				return type.equals(InteractionEvent.Kind.class);
			}
			
			public Object fromString(String value) {
				return InteractionEvent.Kind.fromString(value);
			}
		});
		
		xs.setMode(XStream.NO_REFERENCES);
		xs.alias("kind", InteractionEvent.Kind.class);
		xs.alias("interactionEvent", InteractionEvent.class);
		xs.addImplicitCollection(null, "interactionEvent", InteractionEvent.class);
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.warn("GET is not supported");
		response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!ServletFileUpload.isMultipartContent(request)) {
			log.warn("Multipart content is required");
			response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}
		
		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		
		List<?> files = null;
		try {
			files = upload.parseRequest(request);
		} catch(FileUploadException e) {
			log.error("Upload failed", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		
		if (files != null) {
			for (Object fileObj : files) {
				processFile((FileItem) fileObj);
			}
		}
	}
	
	private void processFile(FileItem file) throws IOException {
		log.debug(String.format("Parsing %s", file.getName()));
		
		if (file.getContentType().startsWith("application/zip")) {
			ZipInputStream zip = new ZipInputStream(file.getInputStream());
			ZipEntry ze;
			
			while((ze = zip.getNextEntry()) != null) {
				if (ze.isDirectory()) continue;
				
				// don't know how to force XStream to deserialize collection here, using this loop fallback
				while(true) {
					try {
						Object o = xs.fromXML(zip);
						if (o != null && o instanceof InteractionEvent) {
							storeInteractionEvent((InteractionEvent) o);
						}
					} catch(BaseException e) {
						// deserialization failed - either bad data or end of stream, we don't care
						break;
					}
				}
			}
		} else {
			// don't know how to force XStream to deserialize collection here, using this loop fallback
			while(true) {
				try {
					Object o = xs.fromXML(file.getInputStream());
					if (o != null && o instanceof InteractionEvent) {
						storeInteractionEvent((InteractionEvent) o);
					}
				} catch(BaseException e) {
					// deserialization failed - either bad data or end of stream, we don't care
					break;
				}
			}
		}
	}
	
	private void storeInteractionEvent(InteractionEvent ie) {
		try {
			UserTransaction tx = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
			tx.begin();
			
			tx.commit();
		} catch(RuntimeException e) {
			log.error("Exception while storing InteractionEvent", e);
		} catch (Exception e) {
			log.error("Exception while storing InteractionEvent", e);
		}
	}

}
