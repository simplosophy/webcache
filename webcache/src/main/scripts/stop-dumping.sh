#!/bin/sh
ps ax | grep -i 'DumpData' |grep java | grep -v grep | awk '{print $1}' | xargs kill
exit 0
