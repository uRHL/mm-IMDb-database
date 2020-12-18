#!/bin/bash

#parameters
originaBulkFile="../../output/bulkTasks.json"
tempDir="temp"

#To use curl, the response size must be at most 4MB
#4000 lines per file ~3MB 
lines_per_file=4000
i=0

mkdir $tempDir
cd $tempDir/
split -l $lines_per_file -d $originaBulkFile

#count the total number of segments created from the original file
total_files=$(find . -maxdepth 1 -type f | wc -l)

while [ $i -lt $total_files ]; do
    if [ $i -lt 10 ]; then
        tempFileName="@x0$i"    
    else
        tempFileName="@x$i"
    fi            
    (( i += 1 ))
    curl -H "Content-Type: application/x-ndjson" -XPOST localhost:9200/films/_bulk?pretty --data-binary "$tempFileName"    
done

#temporal files no longer needed. Delete them
cd ..
rm -r $tempDir
