#!/bin/bash

ls file-icons | awk '{system("convert -resize 26x26 file-icons/"$1" src/main/resources/icons/"$1);}'
