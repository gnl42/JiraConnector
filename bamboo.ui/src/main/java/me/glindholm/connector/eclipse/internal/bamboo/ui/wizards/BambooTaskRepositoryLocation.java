package me.glindholm.connector.eclipse.internal.bamboo.ui.wizards;

import java.net.Proxy;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryLocation;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;

@SuppressWarnings("restriction")
public class BambooTaskRepositoryLocation extends TaskRepositoryLocation {

    public BambooTaskRepositoryLocation(final TaskRepository taskRepository) {
        super(taskRepository);
    }

    @Override
    public Proxy getProxyForHost(final String host, final String proxyType) {
        if (!taskRepository.isDefaultProxyEnabled()) {
            final String proxyHost = taskRepository.getProperty(TaskRepository.PROXY_HOSTNAME);
            final String proxyPort = taskRepository.getProperty(TaskRepository.PROXY_PORT);
            if (proxyHost != null && proxyHost.length() > 0 && proxyPort != null) {
                try {
                    final int proxyPortNum = Integer.parseInt(proxyPort);
                    final AuthenticationCredentials credentials = taskRepository.getCredentials(AuthenticationType.PROXY);
                    return WebUtil.createProxy(proxyHost, proxyPortNum, credentials);
                } catch (final NumberFormatException e) {
                    StatusHandler.log(new RepositoryStatus(taskRepository, IStatus.ERROR, ITasksCoreConstants.ID_PLUGIN, 0,
                            "Error occured while configuring proxy. Invalid port \"" //$NON-NLS-1$
                                    + proxyPort + "\" specified.", //$NON-NLS-1$
                            e));
                }
            }
        } else {
            return WebUtil.getProxy(host, proxyType);
        }

        return null;
    }

}
