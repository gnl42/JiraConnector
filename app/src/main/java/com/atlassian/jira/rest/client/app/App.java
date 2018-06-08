package com.atlassian.jira.rest.client.app;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.ServerInfo;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    private static final String OPTION_JIRA_HOST_URL = "host";
    private static final String OPTION_JIRA_USERNAME = "username";
    private static final String OPTION_JIRA_PASSWORD = "password";
    private static final String ERROR_PARAMETER      = "Parameter {} is missing";
    private static final String COMMAND_LINE_SYNTAX  = "use the following parameters";

    public static void main(String[] args) {
        CommandLineParser commandLineParser = new DefaultParser();
        HelpFormatter helpFormatter = new HelpFormatter();
        CommandLine commandLine;

        Options options = new Options()
                .addOption("h", OPTION_JIRA_HOST_URL, true, null)
                .addOption("u", OPTION_JIRA_USERNAME, true, null)
                .addOption("p", OPTION_JIRA_PASSWORD, true, null)
                ;

        try {
            commandLine = commandLineParser.parse(options, args);
        } catch (ParseException e) {
            log.error("Could not parse options", e);
            helpFormatter.printHelp(COMMAND_LINE_SYNTAX, options);
            return;
        }

        if (!commandLine.hasOption(OPTION_JIRA_HOST_URL)) {
            log.error(ERROR_PARAMETER, OPTION_JIRA_HOST_URL);
            helpFormatter.printHelp(COMMAND_LINE_SYNTAX, options);
            return;
        }

        if (!commandLine.hasOption(OPTION_JIRA_USERNAME)) {
            log.error(ERROR_PARAMETER, OPTION_JIRA_USERNAME);
            helpFormatter.printHelp(COMMAND_LINE_SYNTAX, options);
            return;
        }

        if (!commandLine.hasOption(OPTION_JIRA_PASSWORD)) {
            log.error(ERROR_PARAMETER, OPTION_JIRA_PASSWORD);
            helpFormatter.printHelp(COMMAND_LINE_SYNTAX, options);
            return;
        }

        String url = commandLine.getOptionValue(OPTION_JIRA_HOST_URL);
        String username = commandLine.getOptionValue(OPTION_JIRA_USERNAME);
        String password = commandLine.getOptionValue(OPTION_JIRA_PASSWORD);

        JiraRestClient client;

        try {
            client = new AsynchronousJiraRestClientFactory()
                    .createWithBasicHttpAuthentication(new URI(url), username, password);
        } catch (URISyntaxException e) {
            log.error(e.getMessage(), e);
            return;
        }

        try {
            ServerInfo serverInfo = client.getMetadataClient().getServerInfo().claim();
            log.info("Found JIRA version {}", serverInfo.getVersion());
        } catch (RestClientException e) {
            log.error("Error accessing JIRA, please check URL and credentials");
        }
    }
}
