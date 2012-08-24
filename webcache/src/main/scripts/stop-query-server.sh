#!/bin/sh
ps ax | grep -i 'start-http-query-server.sh' | grep -v grep | awk '{print $1}' | xargs kill
ps ax | grep -i 'QueryServer' |grep java | grep -v grep | awk '{print $2}' | xargs kill
exit 0
