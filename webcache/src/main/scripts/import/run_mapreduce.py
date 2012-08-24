#!/usr/bin/python

import os
import sys
import logging
import time
from internal import hdfs
from task_runner import map_task

if len(sys.argv) < 4:
    print 'usage: python run_mapreduce.py [input crawler base_merge files] [output hdfs_dir] [N node_count]'
    exit()

INPUT_PATH = '/user/crawler/sharding/base_merge_20120727_20120802/Segment0%d{%d..%d}*.sf'
OUTPUT_PATH = '/user/zhubotao/webcache/input_100/'
N = 20

INPUT_PATH = sys.argv[1]+'/Segment0%d{%d..%d}*.sf'
OUTPUT_PATH = sys.argv[2]
N = int(sys.argv[3])


logging.basicConfig(level=logging.DEBUG, format='%(asctime)s %(levelname)-8s %(message)s',
    datefmt='%Y-%m-%d %H:%M:%S', filemode='a+')

#for i in range(0, 10):
def run_task(i):
    current_input_dir = INPUT_PATH % (i/2, i % 2 * 5, i %2 * 5 + 4)
    current_output = OUTPUT_PATH + ('Segment%02d.sf' % i)
    logging.info('current input dir: %s' % current_input_dir)
    logging.info('current output dir: %s' % current_output)

r = os.system(
    '~/workspace/mapreduce/submit.py --mr_input %s --mr_output %s --mr_map_cmd ./hbase_dump_to_url_html --is_compressed true --N %d --logtostderr --mr_reduce_tasks 0 --mr_cache_archives /wly/web.tar.gz#web --mr_ignore_input_error --mr_multi_output --mr_min_split_size 1024 --mr_job_name webcache_input_%d --mr_max_map_failures_percent 5' % (
        current_input_dir, current_output, N, i))
#    r = os.system(cmd_to_run)
if r != 0:
    logging.error('last command failed with return code %d' % r)

#    break
for i in range(0, 20):
    run_task(i)
