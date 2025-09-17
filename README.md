JiraConnector
========================================

JiraConnector is a fork of the 'Atlassian Connector for Eclipse' plugin

It provides Jira integration withing Eclipse.

Works with Jira 8.0+

Requires Java 21, Eclipse 2021-12 or later
=======
# Java library for interacting with Atlassian Jira Server/Data Center/Cloud 8, 9 and 10


Fork of https://bitbucket.org/atlassian/jira-rest-java-client/src/master/
### Changes:
- Needs Java 21
- Uses java.time instead of joda time
- OSGI bundles

Updated for https://github.com/gnl42/JiraConnector

----
## Building
- (one time) mvn install -f atlassian-combined.osgi/pom.xml
- mvn package

### Adding Jira repository over HTTPS
* Add certificate to the keystore
* Add keystore to eclipse command line options

#### Add certificate to the keystore

* Generate certificate
  
`keytool -printcert -sslserver <Jira server> -rfc >> public.crt`

* Import certificate

`keytool -importcert -alias jira -keystore <path to keystore>/cacerts -file public.crt`

#### Add keystore to eclipse command line options

* Add the lines below to <eclipse root directory>/eclipse.ini 

`-Djavax.net.ssl.trustStore=/usr/lib/jvm/temurin-21-jdk/lib/security/cacerts`

`-Djavax.net.ssl.trustStorePassword=<keystore password>`
