#!/bin/bash

# mvn exec:java -Dexec.mainClass="SFTPUtil" -Dexec.args="$*"

# mvn exec:java -Dexec.mainClass="com.talent518.ftp.gui.MainFrame" -Dexec.args="$*"

java -jar target/ftp-gui-*-jar-with-dependencies.jar $*
