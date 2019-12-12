#!/bin/bash

mvn exec:java -Dexec.mainClass="com.talent518.ftp.gui.LoadFrame" -Dexec.args="$*"
