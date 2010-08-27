package com.atlassian.connector.eclipse.monitor.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.connector.eclipse.monitor.server.servlet.UploadServlet;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;
import com.thoughtworks.xstream.core.BaseException;

public class UsageDataUtil2 {
	
	public interface UserInteractionEventCallback {
		boolean visit(UserInteractionEvent uie);
	}

	public static final XStream xs;
	private static final Logger log = LoggerFactory.getLogger(UploadServlet.class);

	static {
		xs = new XStream();
		xs.alias("kind", InteractionEvent.Kind.class);
		xs.alias("interactionEvent", InteractionEvent.class);
		xs.addImplicitCollection(null, "interactionEvent", InteractionEvent.class);
	}

	private UsageDataUtil2() {
	}
	
	public static void processFile(FileItem file, UserInteractionEventCallback callback) throws IOException {
		log.debug(String.format("Parsing %s", file.getName()));
		
		if (file.getContentType().startsWith("application/zip")) {
			ZipInputStream zip = new ZipInputStream(file.getInputStream());
			ZipEntry ze;
			
			int firstDot = file.getName().indexOf(".");
			if (firstDot == -1 || firstDot == 0) {
				log.warn("Silently ignoring upload because file name doesn't have '.'");
			}
			
			final String uid = file.getName().substring(0, firstDot);
			
			while((ze = zip.getNextEntry()) != null) {
				log.debug(String.format("Processing %s", ze.getName()));
				
				if (ze.isDirectory()) continue;
				if (!ze.getName().endsWith(".xml")) continue;
				
				processStream(zip, callback, uid);
			}
		} else {
			log.warn(String.format("Silently ignoring upload from someone (not a ZIP file): %s", file.getName()));
		}
	}

	// TODO: naive implementation, may require improvements
	public static void processStream(InputStream is, UserInteractionEventCallback callback, String uid) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			IOUtils.copy(is, os);
		} catch (IOException e1) {
			log.error("Unable to read input stream", e1);
			return;
		}
		
		String xml = null;
		try {
			xml = "<list>" + os.toString(FilePart.DEFAULT_CHARSET) + "</list>";
		} catch (UnsupportedEncodingException e1) {
			log.error("Unable to read input stream", e1);
			return;
		} 
		
		if (xml == null) {
			return;
		}
		
		try {
			Object o = xs.fromXML(xml);
			if (o != null && o instanceof List<?>) {
				List<?> events = (List<?>) o;
				for (Object event : events) {
					if(!callback.visit(new UserInteractionEvent((InteractionEvent) event, uid, null))) {
						break;
					}
				}
			}
		} catch(BaseException e) {
			log.error("Unable to deserialize XML", e);
		}
	}
	
}