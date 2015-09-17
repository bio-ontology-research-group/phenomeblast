#!/bin/bash

groovy GenerateMouseRepresentation /tmp/phene-classified.owl all/phenotypes.txt
groovy InfoContent all/phenotypes.txt > all/phenotypes-info.txt

parallel -j 3 < commands.txt

cd all
#for i in `seq 0 1000 110000`; do cat phenotypes-simgic-$i-`expr $i + 1000` >> all-simgic; done
# more here...
cd ..
