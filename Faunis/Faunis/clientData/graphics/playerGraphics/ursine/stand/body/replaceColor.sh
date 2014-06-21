#! /bin/bash

for _source in $(ls down.png -1)
do
        _target=changed_${_source}
        if !(test -e "$_target")
        then
            convert "$_source" -flop "$_target"
        else
            echo "Could not convert, file $_target already exists."
        fi
done