#! /usr/bin/python
##  -*- coding: utf-8 -*-

import re, os
import subprocess

## path可以包含通配符，IsDirectory 返回 True 如果有一个文件名是路径的话
def IsDirectory(path):
  hadoop_cmd = os.environ["HADOOP_HOME"] + "/bin/hadoop"
  cmd = [hadoop_cmd, "fs", "-ls", path]
  p = subprocess.Popen(cmd, stdout=subprocess.PIPE)
  for line in p.stdout:
    line = line.rstrip()
    splits = line.split()
    if len(splits) != 8:
      continue
    m = re.match("([-d])[-rwx]{9}", splits[0])
    if m and m.group(1) == "-":
      if splits[7].endswith(path):
        p.stdout.close()
        return False
  p.stdout.close()
  return True

## 判断文件是否是隐藏路径
def IsHiden(path):
  dirs = path.split("/")
  for d in dirs:
    if d.startswith("_"):
      return True
  return False

def GetFiles(path):
  hadoop_cmd = os.environ["HADOOP_HOME"] + "/bin/hadoop"
  cmd = [hadoop_cmd, "fs", "-ls", path]
  files = []
  p = subprocess.Popen(cmd, stdout=subprocess.PIPE)
  for line in p.stdout:
    line = line.rstrip()
    splits = line.split()
    if len(splits) != 8:
      continue
    m = re.match("([-d])[-rwx]{9}", splits[0])
    if not m: continue
    isDirectory = True if m.group(1) == "d" else False
    files.append((isDirectory, splits[7]))
  p.stdout.close()
  return files

def main():
  print IsDirectory("/user/tianjia/indexer/test/dict.sf")
  print IsDirectory("/user/tianjia/indexer/test/dict.sf/term_*")

  print IsDirectory("/tmp/a.kv")
  parent = os.path.dirname("/tmp/a.kv")
  for file in GetFiles(parent):
    print file[0], file[1]

if __name__ == "__main__":
  main()

