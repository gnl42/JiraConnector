package me.glindholm.theplugin.commons.configuration;

import me.glindholm.theplugin.commons.util.HttpConfigurableAdapter;

/**
 * User: jgorycki Date: Jan 20, 2009 Time: 2:48:30 PM
 */
public class PluginConfigurationBean implements PluginConfiguration {
    private JiraConfigurationBean jiraConfiguration = new JiraConfigurationBean();
    private GeneralConfigurationBean generalConfigurationBean = new GeneralConfigurationBean();
    private HttpConfigurableAdapter httpConfigurableAdapter;

    /**
     * Default constructor.
     */
    public PluginConfigurationBean() {
    }

    /**
     * Copying constructor.
     * <p>
     * Makes a deep copy of provided configuration.
     *
     * @param cfg configuration to be deep copied.
     */
    public PluginConfigurationBean(final PluginConfiguration cfg) {
        setConfiguration(cfg);
    }

    /**
     * Deep copies provided configuration.
     *
     * @param cfg configuration to be copied to current configuration object.
     */
    @Override
    public void setConfiguration(final PluginConfiguration cfg) {

        setGeneralConfigurationData(new GeneralConfigurationBean(cfg.getGeneralConfigurationData()));
        setJIRAConfigurationData(new JiraConfigurationBean(cfg.getJIRAConfigurationData()));
        transientSetHttpConfigurable(cfg.transientGetHttpConfigurable());
    }

    /**
     * For storage purposes.
     * <p/>
     * Does not use the JDK1.5 'return a subclass' due to problem with XML serialization.
     */
    /**
     * For storage purposes.
     * <p/>
     * Does not use the JDK1.5 'return a subclass' due to problem with XML serialization.
     */
    @Override
    public JiraConfigurationBean getJIRAConfigurationData() {
        return jiraConfiguration;
    }

    /**
     * For storage purposes.
     * <p/>
     * Does not use the JDK1.5 'return a subclass' due to problem with XML serialization.
     */
    public void setJIRAConfigurationData(final JiraConfigurationBean newConfiguration) {
        jiraConfiguration = newConfiguration;
    }

    @Override
    public GeneralConfigurationBean getGeneralConfigurationData() {
        return generalConfigurationBean;
    }

    @Override
    public void setGeneralConfigurationData(final GeneralConfigurationBean aGeneralConfigurationBean) {
        generalConfigurationBean = aGeneralConfigurationBean;
    }

    @Override
    public void transientSetHttpConfigurable(final HttpConfigurableAdapter aHttpConfigurableAdapter) {
        httpConfigurableAdapter = aHttpConfigurableAdapter;
    }

    @Override
    public HttpConfigurableAdapter transientGetHttpConfigurable() {
        return httpConfigurableAdapter;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final PluginConfigurationBean that = (PluginConfigurationBean) o;

        if (!generalConfigurationBean.equals(that.generalConfigurationBean)) {
            return false;
        }

        return jiraConfiguration.equals(that.jiraConfiguration);
    }

    private static final int ONE_EFF = 31;

    @Override
    public int hashCode() {
        int result = 0;
        result = ONE_EFF * result + (jiraConfiguration != null ? jiraConfiguration.hashCode() : 0);
        result = ONE_EFF * result + (generalConfigurationBean != null ? generalConfigurationBean.hashCode() : 0);

        return result;
    }
}
