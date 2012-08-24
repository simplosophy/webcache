for i in 76 77 78 79 80 81 82 83 84 85 86 87 88 89 90 91 92 93 94 95
do  
  echo $i
   ssh m07 "ssh i6$i.dong.shgt.qihoo.net 'WEBCACHE_HOME=~/workspace/webcache/target nohup sh ~/workspace/webcache/target/scripts/convert_uidx_2_idx.sh > ~/workspace/webcache/convert.out 2>~/workspace/webcache/convert.err &'" &
   sleep 3s
done
