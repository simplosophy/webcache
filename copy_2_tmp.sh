rm -rf tmp/*
cp -r webcache/target/classes tmp/
cp -r webcache/target/webcache-1.0-bin/lib tmp/
cp -r webcache/src/main/scripts tmp/
rsync -cavz --exclude=.svn tmp/ m07:~/workspace/webcache/target/
