#! /bin/bash
find . \! -path '*/\.*' \! -name "*.class" \! -name "*.png" \! -name "*.properties" -type f | xargs grep -L "Copyright"
