#!/usr/bin/env bash

echo "Using Java version:"
java -version

unameOut="$(uname -s)"
case "${unameOut}" in
    Linux*)     machine=Linux;;
    Darwin*)    machine=Mac;;
    CYGWIN*)    machine=Cygwin;;
    MINGW*)     machine=MinGw;;
    *)          machine="UNKNOWN:${unameOut}"
esac
echo ${machine}

if [[ ${machine} -eq "Mac" ]]
then
  chmod +x ./workbench/Eclipse.app/Contents/MacOS/eclipse
  ./workbench/Eclipse.app/Contents/MacOS/eclipse
else
  chmod +x ./workbench/eclipse
  ./workbench/eclipse
fi

