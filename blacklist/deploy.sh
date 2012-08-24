rsync -cavz --exclude=.svn . m07:~/workspace/webcache/blacklist
ssh m07 rsync -cavz ~/workspace/webcache/blacklist/ i676.dong.shgt.qihoo.net:~/workspace/webcache/blacklist
