#!/usr/bin/python
# -*- coding:utf-8 -*-

import sys
import os
import time
import logging
from internal import hdfs

if len(sys.argv) < 2:
    print "usage: python dump.py [input map_reduce input file]"
    exit()

input_dir = '/user/zhubotao/webcache/input_100/Segment0%d.sf'
input_dir = sys.argv[1] + '/Segment0%d.sf'

import_cmd = 'sh start-dumping.sh %s'
logging.basicConfig(level=logging.DEBUG, format='%(asctime)s %(levelname)-8s %(message)s',
  datefmt='%Y-%m-%d %H:%M:%S', filemode='a+')

os.chdir(os.getenv('WEBCACHE_HOME')+'/scripts')

for i in range(0, 10):
  current_input_dir = input_dir % i
  logging.info('current input dir: %s' % current_input_dir)
  while True:
    files = hdfs.GetFiles(current_input_dir + '/_SUCCESS')
    old_index = 0
    found = False
    for f in files:
      if f[1].endswith('_SUCCESS'):
        found = True

    if not found:
      logging.info('not found')
      time.sleep(30)
      continue
    else:
      logging.info('found')

    cmd_to_run = import_cmd % (current_input_dir)
    logging.info('running ' + cmd_to_run)
    r = os.system(cmd_to_run)
    if r != 0:
      logging.error('last command failed with return code %d' % r)
      sys.exit(1)

    break
