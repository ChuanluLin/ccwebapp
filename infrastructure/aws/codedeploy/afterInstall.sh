#!/bin/bash

# sudo systemctl stop tomcat.service

# sudo rm -rf /opt/tomcat/webapps/docs  /opt/tomcat/webapps/examples /opt/tomcat/webapps/host-manager  /opt/tomcat/webapps/manager /opt/tomcat/webapps/ROOT

# sudo chown tomcat:tomcat /opt/tomcat/webapps/ROOT.war

ps aux | grep demo | xargs kill -9
nohup java -jar ~/demo-0.0.1-SNAPSHOT.jar >/dev/null 2>&1

# cleanup log files
# sudo rm -rf /opt/tomcat/logs/catalina*
# sudo rm -rf /opt/tomcat/logs/*.log
# sudo rm -rf /opt/tomcat/logs/*.txt