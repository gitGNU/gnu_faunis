#! /bin/bash 

for linkfile in $(find . -name "settings.properties" -prune -o -name "*.properties" -printf "%p\n")
do
    cat ${linkfile} | rev | cut -c 5- | rev | sponge ${linkfile}
done