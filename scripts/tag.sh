#!/bin/bash
v=$1
if test -z "$v"
then
      echo "Provide tag please."
else
      git tag -a v$v -m "v$v"
      git push --tags
fi