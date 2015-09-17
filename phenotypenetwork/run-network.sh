#!/bin/bash
export CLASSPATH=.
for i in jar/*; do export CLASSPATH=$CLASSPATH:$i; done

groovy GenerateMouseRepresentation2 /tmp/phene.owl ../data/phenotypes.txt
groovy InfoContent ../data/phenotypes.txt > ../data/phenotypes-info.txt
./simgic
#parallel --jobs 24 < commands.txt

#groovy SimGICSimilarityParallel.groovy 0 10000 &
#groovy SimGICSimilarityParallel.groovy 10000 20000 &
#groovy SimGICSimilarityParallel.groovy 20000 30000 
#groovy SimGICSimilarityParallel.groovy 15000 20000 &
#groovy SimGICSimilarityParallel.groovy 20000 25000 &
#groovy SimGICSimilarityParallel.groovy 25000 30000 
#groovy SimGICSimilarityParallel.groovy 30000 40000 &
#groovy SimGICSimilarityParallel.groovy 40000 50000 &
#groovy SimGICSimilarityParallel.groovy 50000 60000 
#groovy SimGICSimilarityParallel.groovy 60000 70000 &
#groovy SimGICSimilarityParallel.groovy 70000 90000 &
#groovy SimGICSimilarityParallel.groovy 90000 120000


cd all
#for i in `seq 0 1000 110000`; do cat phenotypes-simgic-$i-`expr $i + 1000` >> all-simgic; done
# more here...
cd ..