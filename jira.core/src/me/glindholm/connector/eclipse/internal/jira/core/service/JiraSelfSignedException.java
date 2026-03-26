package me.glindholm.connector.eclipse.internal.jira.core.service;

public class JiraSelfSignedException extends JiraException {

    public JiraSelfSignedException() {
    }

    public JiraSelfSignedException(String message, Throwable cause) {
        super(message, cause);
    }

    public JiraSelfSignedException(String message) {
        super(message);
    }

    public JiraSelfSignedException(Throwable cause) {
        super(cause);
    }

}
