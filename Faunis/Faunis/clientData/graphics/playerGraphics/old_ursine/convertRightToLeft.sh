#! /bin/bash

for animation in stand walk
do
    for _image in $(find $animation/rightArm/ -type d -name .svn -prune -o -name "*.png" -printf "%f\n")
    do
        _source="$animation/rightArm/$_image"
        _mirrored_image=${_image//right/LEFT}
        _mirrored_image=${_mirrored_image//left/right}
        _mirrored_image=${_mirrored_image//LEFT/left}
        _target="$animation/leftArm/$_mirrored_image"
        echo "$_source -> $_target"
        if !(test -e "$_target")
        then
            convert "$_source" -flop -fill cyan1 -opaque green1 "$_target"
        else
            echo "Could not convert, file $_target already exists."
        fi
    done

    for _image in $(find $animation/rightLeg/ -type d -name .svn -prune -o -name "*.png" -printf "%f\n")
    do
        _source="$animation/rightLeg/$_image"
        _mirrored_image=${_image//right/LEFT}
        _mirrored_image=${_mirrored_image//left/right}
        _mirrored_image=${_mirrored_image//LEFT/left}
        _target="$animation/leftLeg/$_mirrored_image"
        echo "$_source -> $_target"
        if !(test -e "$_target")
        then
            convert "$_source" -flop -fill blue1 -opaque red1 "$_target"
        else
            echo "Could not convert, file $_target already exists."
        fi
    done
done