package me.glindholm.theplugin.commons.cfg.xstream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.JDom2Reader;
import com.thoughtworks.xstream.io.xml.JDom2Writer;

import me.glindholm.theplugin.commons.cfg.ServerCfgFactoryException;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jetbrains.annotations.NotNull;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;

public abstract class BasePrivateConfigurationDao<T> {
    private static final String ATLASSIAN_DIR_NAME = ".atlassian";
	private static final String ATLASSIAN_IDE_CONNECTOR_DIR_NAME = "ide-connector";

    public BasePrivateConfigurationDao() {
    }

    void writeXmlFile(final Element element, @NotNull final File outputFile) throws IOException {
        StringWriter sw = new StringWriter();
        new XMLOutputter(Format.getPrettyFormat()).output(element, sw);
        sw.flush();
        sw.close();
        String str = sw.toString();

        // PL-2987 - FileWriter uses incorrect encoding
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(outputFile),
                Charset.forName("UTF-8").newEncoder());
        PrintWriter out = new PrintWriter(new BufferedWriter(outputStreamWriter));
        out.write(str);
        out.flush();
        out.close();
    }

    static void saveJDom(final Object object, final Element rootElement) {
        if (object == null) {
            throw new NullPointerException("Serialized object cannot be null");
        }
		final JDom2Writer writer = new JDom2Writer(rootElement);
        final XStream xStream = JDomXStreamUtil.getProjectJDomXStream(true);
        xStream.marshal(object, writer);
    }

    public static String getPrivateCfgDirectoryPath() {
        return System.getProperty("user.home") + File.separator + ATLASSIAN_DIR_NAME
                + File.separator + ATLASSIAN_IDE_CONNECTOR_DIR_NAME;
    }

    abstract String getRootElementName();

    public Document createJDom(final T t) {
        Document document = new Document(new Element(getRootElementName()));
        saveJDom(t, document.getRootElement());
        return document;
    }

    protected static File getPrivateCfgDirectorySavePath() throws ServerCfgFactoryException {

        final File ideConnectorHomeDir = new File(getPrivateCfgDirectoryPath());
        if (ideConnectorHomeDir.exists() == false) {
            if (ideConnectorHomeDir.mkdirs() == false) {
                throw new ServerCfgFactoryException("Cannot create directory [" + ideConnectorHomeDir.getAbsolutePath() + "]");
            }
        }


        if (ideConnectorHomeDir.isDirectory() && ideConnectorHomeDir.canWrite()) {
            return ideConnectorHomeDir;
        }
        throw new ServerCfgFactoryException("[" + ideConnectorHomeDir.getAbsolutePath() + "] is not writable"
                + " or is not a directory");
    }

    protected static <T1> T1 loadJDom(final Element rootElement, Class<T1> clazz, Boolean saveAll)
            throws ServerCfgFactoryException {
		final int childCount = rootElement.getChildren().size();
		if (childCount != 1) {
			throw new ServerCfgFactoryException("Cannot travers JDom tree. Exactly one child node expected, but found ["
					+ childCount + "]");
		}
		final JDom2Reader reader = new JDom2Reader((Element) rootElement.getChildren().get(0));
		final XStream xStream = JDomXStreamUtil.getProjectJDomXStream(saveAll);
		try {
			return clazz.cast(xStream.unmarshal(reader));
		} catch (ClassCastException e) {
			throw new ServerCfgFactoryException("Cannot load " + clazz.getSimpleName() + " due to ClassCastException: "
					+ e.getMessage(), e);
		} catch (Exception e) {
			throw new ServerCfgFactoryException("Cannot load " + clazz.getSimpleName() + ": "
					+ e.getMessage(), e);
		}
	}

    public boolean isDirReady() throws ServerCfgFactoryException {
		final File atlassianDir = getPrivateCfgDirectorySavePath();

		return (atlassianDir.isDirectory() && atlassianDir.canRead());
    }

}