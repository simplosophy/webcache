rm -rf tmp/*
cp -r webcache/target/classes tmp/
cp -r webcache/target/webcache-1.0-bin/lib tmp/
cp -r webcache/src/main/scripts tmp/
rsync -cavz --exclude=.svn tmp/ m07:~/tmp/
for i in 94 95
  do
    ssh m07 rsync -cavz ~/tmp/ t$i.dong.shgt.qihoo.net:~/workspace/webcache/target
    ssh m07 ssh t$i.dong.shgt.qihoo.net python ~/workspace/webcache/target/scripts/generate_system_properties_test.py  ~/workspace/webcache/target/classes/system.properties
  done
