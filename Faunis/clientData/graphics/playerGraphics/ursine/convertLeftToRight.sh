#! /bin/bash

for animation in stand walk
do
    for _image in $(find $animation/leftArm/ -type d -name .svn -prune -o -name "*.png" -printf "%f\n")
    do
        _source="$animation/leftArm/$_image"
        _mirrored_image=${_image//right/LEFT}
        _mirrored_image=${_mirrored_image//left/right}
        _mirrored_image=${_mirrored_image//LEFT/left}
        _target="$animation/rightArm/$_mirrored_image"
        echo "$_source -> $_target"
        if !(test -e "$_target")
        then
            convert "$_source" -flop -fill green1 -opaque cyan1 "$_target"
        else
            echo "Could not convert, file $_target already exists."
        fi
    done

    for _image in $(find $animation/leftLeg/ -type d -name .svn -prune -o -name "*.png" -printf "%f\n")
    do
        _source="$animation/leftLeg/$_image"
        _mirrored_image=${_image//right/LEFT}
        _mirrored_image=${_mirrored_image//left/right}
        _mirrored_image=${_mirrored_image//LEFT/left}
        _target="$animation/rightLeg/$_mirrored_image"
        echo "$_source -> $_target"
        if !(test -e "$_target")
        then
            convert "$_source" -flop -fill red1 -opaque blue1 "$_target"
        else
            echo "Could not convert, file $_target already exists."
        fi
    done
done
