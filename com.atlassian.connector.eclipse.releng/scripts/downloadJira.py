#!/usr/bin/python
import sys
from subprocess import *
import shlex
import os
import re
import glob
import shutil

jira_version = "4.2-SNAPSHOT"

def download(user, password):
	p1 = Popen(shlex.split("wget -q -O - --http-user=%s --http-password=%s https://maven.atlassian.com/content/groups/internal/com/atlassian/jira/atlassian-jira-standalone/%s/maven-metadata.xml" % (user, password, jira_version)), stdout=PIPE)
	output = p1.communicate()[0]
	m = re.search("<timestamp>(.*)</timestamp>", output)
	timestamp = m.group(1)

	m = re.search("<buildNumber>(.*)</buildNumber>", output)
	buildNo = m.group(1)

	fn = "atlassian-jira-standalone-4.2-%s-%s.tgz" % (timestamp, buildNo)

	try:
		os.unlink(fn)
	except OSError:
		pass

	p1 = Popen(shlex.split("wget -q --http-user=%s --http-password=%s https://maven.atlassian.com/content/groups/internal/com/atlassian/jira/atlassian-jira-standalone/%s/%s" % (user, password, jira_version, fn)))
	p1.wait()

	return fn

def downloadSoap(user, password):
	p1 = Popen(shlex.split("wget -q -O - --http-user=%s --http-password=%s https://maven.atlassian.com/content/repositories/atlassian-public-snapshot/com/atlassian/jira/plugins/atlassian-jira-rpc-plugin/%s/maven-metadata.xml" % (user, password, jira_version)), stdout=PIPE)
	output = p1.communicate()[0]
	m = re.search("<timestamp>(.*)</timestamp>", output)
	timestamp = m.group(1)

	m = re.search("<buildNumber>(.*)</buildNumber>", output)
	buildNo = m.group(1)

	fn = "atlassian-jira-rpc-plugin-4.2-%s-%s.jar" % (timestamp, buildNo)

	try:
		os.unlink(fn)
	except OSError:
		pass

	p1 = Popen(shlex.split("wget -q --http-user=%s --http-password=%s https://maven.atlassian.com/content/repositories/atlassian-public-snapshot/com/atlassian/jira/plugins/atlassian-jira-rpc-plugin/%s/%s" % (user, password, jira_version, fn)))
	p1.wait()

	return fn

def unpack(fn):
	p1 = Popen(["tar", "-xzf", fn])
	p1.wait()

def shutdown(dir):
	try:
		p1 = Popen(["%s/bin/shutdown.sh" % (dir)])
		p1.wait()
	except OSError:
		pass

def start(dir):
	p1 = Popen(["%s/bin/startup.sh" % (dir)])
	p1.wait()

def configure(dir):
	server_xml = "%s/conf/server.xml" % (dir)
	os.rename(server_xml, server_xml + ".orig")
	input = open(server_xml + ".orig")
	output = open(server_xml, "w")
	for l in input:
		l = l.replace('Server port="8005"', 'Server port="8071"')
		l = l.replace('Connector port="8080"', 'Connector port="8070"')
		l = l.replace('Context path="" docBase', 'Context path="/jira-enterprise-snapshot" docBase')
		output.write(l)
	input.close()
	output.close()

	jira_xml = "%s/atlassian-jira/WEB-INF/classes/jira-application.properties" % (dir)
	os.rename(jira_xml, jira_xml + ".orig")
	input = open(jira_xml + ".orig")
	output = open(jira_xml, "w")
	for l in input:
		l = l.replace('jira.home =\n', 'jira.home = /opt/j2ee/domains/atlassian.com/eclipse-connector-integration-tests-jira/atlassian-jira-enterprise-snapshot-home\n')
		output.write(l)
	input.close()
	output.close()

def usage():
	print "%s username password\n" % (sys.argv[0])

if __name__ == "__main__":
	if len(sys.argv) != 3:
		usage()
		sys.exit(1)

	if not 'JAVA_HOME' in os.environ.keys():
		os.environ['JAVA_HOME'] = '/opt/java/sdk/current/' 

	fn = download(*sys.argv[1:3])
	dir = "atlassian-jira-enterprise-%s-standalone" % (jira_version)
	rpc = downloadSoap(*sys.argv[1:3])
	shutdown(dir)
	unpack(fn)
	configure(dir)
	for old_rpc in glob.glob("%s/atlassian-jira/WEB-INF/lib/atlassian-jira-rpc-plugin*" % (dir)):
		try:
			os.unlink()
		except Error, e:
			pass
	
	shutil.copy(rpc, "%s/atlassian-jira/WEB-INF/lib/" % (dir))	
	start(dir)
