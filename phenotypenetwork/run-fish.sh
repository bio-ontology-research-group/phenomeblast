#!/bin/bash

#groovy GenerateRepresentationForFish /tmp/phene-fish.owl ../data/fish-phenotypes.txt
groovy InfoContent ../data/fish-phenotypes.txt > ../data/fish-phenotypes-info.txt
./simgic-fish
groovy EvaluatePhenomeNET -d ../data/fish-c-all-phenotypes.txt -i ../data/fish-c-all.txt -p mgi-gene-positive.txt -o fish-gene-eval4.txt
groovy GetAUC fish-gene-eval4.txt
