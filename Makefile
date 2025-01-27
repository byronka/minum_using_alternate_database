##
# Project name - used to set the jar's file name
##
PROJ_NAME := coupons
HOST_NAME := coupons.com
HOST_USER := mrcoupon

##
# default target(s)
##
all:: help

#: run the system
run::
	 ./mvnw compile exec:java

#: clean up any output files
clean::
	 rm -fr out
	 ./mvnw clean

#: run the tests
test::
	 ./mvnw test

#: run tests, and build a coverage report
test_coverage::
	 @./mvnw jacoco:prepare-agent test jacoco:report

#: run the H2 database console
h2_console:: jar
	 echo "See coupons.config for the connection string, username and password to use"
	 java -cp target/coupons-*-jar-with-dependencies.jar org.h2.tools.Console

render_coverage_report::
	 ./mvnw jacoco:report

target/jacocoagent.jar:
	 ./mvnw dependency:copy -Dartifact='org.jacoco:org.jacoco.agent:0.8.10' -DoutputDirectory=./target
	 unzip target/org.jacoco.agent-0.8.10.jar jacocoagent.jar -d target

#: set up for local dev testing - use sample db, run with code coverage
local_dev: clean target/jacocoagent.jar
	 @echo "*************************"
	 @echo "Running Coupons locally with a test database"
	 @echo "*************************"
	 @echo
	 bash -c "trap 'trap - SIGINT SIGTERM ERR; make render_coverage_report; exit 1' SIGINT SIGTERM ERR; make run_with_coverage"

JMX_PROPERTIES=-Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false
DEBUG_PROPERTIES=-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=y


#: run the system off the jar
runjar::
	 java $(JMX_PROPERTIES) -jar target/coupons-*-jar-with-dependencies.jar

#: run the system in debug mode
rundebug::
	 MAVEN_OPTS="$(DEBUG_PROPERTIES) $(JMX_PROPERTIES)" ./mvnw compile exec:java

#: run the system off the jar in debug mode
runjardebug::
	 java $(DEBUG_PROPERTIES) $(JMX_PROPERTIES) -jar target/coupons-*-jar-with-dependencies.jar

#: run the application, recording code coverage the whole time
run_with_coverage::
	 MAVEN_OPTS=-javaagent:target/jacocoagent.jar=destfile=target/jacoco.exec ./mvnw compile exec:java

#: download the production database to this directory as db.tar.gz
prod_db_download::
	 @echo "create a compressed tar of the database"
	 ssh $(HOST_USER)@$(HOST_NAME) "cd coupons && tar zcf db.tar.gz db"
	 @echo "download that tar"
	 scp $(HOST_USER)@$(HOST_NAME):~/coupons/db.tar.gz ./
	 @echo "remove the compressed tar from the server"
	 ssh $(HOST_USER)@$(HOST_NAME) "rm coupons/db.tar.gz"

#: download the production database to this directory as db.tar.gz
test_db_download::
	 @echo "create a compressed tar of the database"
	 ssh $(HOST_USER)@$(HOST_NAME) "cd coupons_test && tar zcf db.tar.gz db"
	 @echo "download that tar"
	 scp $(HOST_USER)@$(HOST_NAME):~/coupons_test/db.tar.gz ./
	 @echo "remove the compressed tar from the server"
	 ssh $(HOST_USER)@$(HOST_NAME) "rm coupons_test/db.tar.gz"

#: jar up the program
jar::
	 ./mvnw package -Dmaven.test.skip
	 @echo "Your new jar is at target/coupons-*-jar-with-dependencies.jar"

#: bundle up all the files we need on the cloud server
bundle:: jar
	 @echo "create a directory of target/$(PROJ_NAME)"
	 @mkdir -p target/$(PROJ_NAME)
	 @echo "copy the scripts from docs/cloud/scripts/ into target/$(PROJ_NAME)"
	 @cp docs/cloud/scripts/* target/$(PROJ_NAME)
	 @echo "copy the appropriate coupons config file: coupons_prod.config"
	 @cp docs/cloud/configs/coupons_prod.config target/$(PROJ_NAME)/coupons.config
	 @echo "copy jar to target/$(PROJ_NAME)"
	 @cp target/coupons-*-jar-with-dependencies.jar target/$(PROJ_NAME)/coupons.jar
	 @echo "output the most recent git status and store it in code_status.txt"
	 @git log --oneline|head -1 > target/$(PROJ_NAME)/code_status.txt && git diff >> target/$(PROJ_NAME)/code_status.txt
	 @echo "copy webapp resources into the target/$(PROJ_NAME) directory"
	 @rsync --recursive --update --perms src/main/webapp/ target/$(PROJ_NAME)
	 @echo "create a compressed tar of the contents in target/$(PROJ_NAME)"
	 @cd target/$(PROJ_NAME) && tar -czf "../$(PROJ_NAME).tar.gz" *

#: bundle up all the files we need on the cloud server
bundle_test:: jar
	 @echo "create a directory of target/$(PROJ_NAME)"
	 @mkdir -p target/$(PROJ_NAME)
	 @echo "copy the scripts from docs/cloud/scripts/ into target/$(PROJ_NAME)"
	 @cp docs/cloud/scripts/* target/$(PROJ_NAME)
	 @echo "copy the appropriate coupons config file: coupons_test.config"
	 @cp docs/cloud/configs/coupons_test.config target/$(PROJ_NAME)/coupons.config
	 @echo "copy jar to target/$(PROJ_NAME)"
	 @cp target/coupons-*-jar-with-dependencies.jar target/$(PROJ_NAME)/coupons.jar
	 @echo "output the most recent git status and store it in code_status.txt"
	 @git log --oneline|head -1 > target/$(PROJ_NAME)/code_status.txt && git diff >> target/$(PROJ_NAME)/code_status.txt
	 @echo "copy webapp resources into the target/$(PROJ_NAME) directory"
	 @rsync --recursive --update --perms src/main/webapp/ target/$(PROJ_NAME)
	 @echo "create a compressed tar of the contents in target/$(PROJ_NAME)"
	 @cd target/$(PROJ_NAME) && tar -czf "../$(PROJ_NAME).tar.gz" *


#: send to the cloud but don't run.  Depends on tests passing.
deliver:: bundle
	 @echo "ship bundle to our production server"
	 scp -rq target/$(PROJ_NAME).tar.gz $(HOST_USER)@$(HOST_NAME):~/

#: send to the cloud but don't run.  Depends on tests passing.
deliver_test:: bundle_test
	    @echo "ship bundle to our production server for a test environment"
	    @scp -rq target/$(PROJ_NAME).tar.gz $(HOST_USER)@$(HOST_NAME):~/coupons_test.tar.gz

pre_prod::
	 @echo "about to clean the out directory and run tests in preparation for deploy to prod"
	 @echo 5...
	 @sleep 1
	 @echo 4...
	 @sleep 1
	 @echo 3...
	 @sleep 1
	 @echo 2...
	 @sleep 1
	 @echo 1...
	 @sleep 1

# This command does a lot.  It runs a command on the production server to create a new directory, coupons_test, which won't fail if it does exist,
# then it un-tars our files into that directory, then it replaces some strings in the config file (because
# otherwise our test server would conflict with the prod server's ports), and then it restarts that system.
#: run the software in the cloud as a test environment.
deploy_test:: deliver_test
	    @ssh $(HOST_USER)@$(HOST_NAME) "\
    		echo 'make the coupons_test directory if it does not already exist' &&\
    		mkdir -p coupons_test && \
    		echo 'untar the coupons_test.tar.gz file into coupons_test' &&\
    		tar zxf coupons_test.tar.gz -C coupons_test && \
    		echo 'remove coupons_test.tar.gz' &&\
    		rm coupons_test.tar.gz && \
    		echo 'change working directory to coupons_test' &&\
    		cd coupons_test && \
    		echo 'replace some configuration values in minum.config' &&\
    		sed -i  -e 's/SERVER_PORT=.*$$/SERVER_PORT=8081/g' -e 's/SSL_SERVER_PORT=.*$$/SSL_SERVER_PORT=9443/g' -e 's/REDIRECT_TO_SECURE=.*$$/REDIRECT_TO_SECURE=false/g' minum.config &&\
    		echo 'restart coupons_test' &&\
    		sudo systemctl restart cg_test.service"

# This command does a lot.  It runs a command on the production server to create a new directory, coupons, which
# won't fail if it does exist, then it un-tars our files into that directory, and then it restarts that system.
#: run the software in the cloud.  Depends on deliver.
deploy_prod:: pre_prod clean test deliver
	 @ssh $(HOST_USER)@$(HOST_NAME) "\
  		 echo 'make the coupons directory if it does not already exist' &&\
		 mkdir -p coupons && \
		 echo 'decompress the coupons.tar.gz file into coupons' &&\
		 tar zxf coupons.tar.gz -C coupons && \
		 echo 'remove coupons.tar.gz' &&\
		 rm coupons.tar.gz && \
		 echo 'create a backup of the entire coupons directory using coupons/backup.sh' &&\
		 directory=coupons coupons/backup.sh && \
		 echo 'change directory into coupons' &&\
		 cd coupons && \
		 echo 'restart the system' &&\
		 sudo systemctl restart cg.service"
	 @echo
	 @echo "A backup was made at coupons_previous.tar.gz"

# a handy debugging tool.  If you want to see the value of any
# variable in this file, run something like this from the
# command line:
#
#     make print-FOO
#
print-%::
	    @echo $* = $($*)

# This is a handy helper.  This prints a menu of items
# from this file - just put hash+colon over a target and type
# the description of that target.  Run this from the command
# line with "make help"
help::
	 @echo
	 @echo Help
	 @echo ----
	 @echo
	 @grep -B1 -E "^[a-zA-Z0-9_-]+:([^\=]|$$)" Makefile \
     | grep -v -- -- \
     | sed 'N;s/\n/###/' \
     | sed -n 's/^#: \(.*\)###\(.*\):.*/\2###\1/p' \
     | column -t  -s '###'