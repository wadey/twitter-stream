#!/bin/sh
mvn compile exec:java -Dexec.mainClass="im.wades.TwitterFollowStreamHandler" "$@"
