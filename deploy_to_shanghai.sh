rm -rf tmp/*
cp -r webcache/target/classes tmp/
cp -r webcache/target/webcache-1.0-bin/lib tmp/
cp -r webcache/src/main/scripts tmp/
rsync -cavz --exclude=.svn tmp/ m07:~/workspace/webcache/target/
for i in 76 77 78 79 80 81 82 83 84 85  86 87 88 89 90 91 92 93 94 95
  do
    ssh m07 rsync -cavz ~/workspace/webcache/target/ i6$i.dong.shgt.qihoo.net:~/workspace/webcache/target
    ssh m07 ssh i6$i.dong.shgt.qihoo.net python ~/workspace/webcache/target/scripts/generate_system_properties.py ~/workspace/webcache/target/classes/system.properties 20120818
  done
