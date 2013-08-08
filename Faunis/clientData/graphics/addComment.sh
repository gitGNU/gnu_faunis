#! /bin/bash

for image in $(find . -name "*.png" -type f)
do
    mogrify -comment "Copyright 2012, 2013 Simon Ley alias \"skarute\"\n\nThis file is part of Faunis.\n\nFaunis is free software: you can redistribute it and/or modify\nit under the terms of the GNU Affero General Public License as\npublished by the Free Software Foundation, either version 3 of\nthe License, or (at your option) any later version.\n\nFaunis is distributed in the hope that it will be useful,\nbut WITHOUT ANY WARRANTY; without even the implied warranty of\nMERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the\nGNU Affero General Public License for more details.\n\nYou should have received a copy of the GNU Affero General\nPublic License along with Faunis. If not, see\n<http://www.gnu.org/licenses/>." "$image"
done
