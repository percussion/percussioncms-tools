#!/usr/bin/env bash

echo "Using Java version:"
java -version

echo "Prod command example: Workbench.cmd prod"
echo "Dev command example: Workbench.cmd dev <port_number>"

unameOut="$(uname -s)"
case "${unameOut}" in
    Linux*)     machine=Linux;;
    Darwin*)    machine=Mac;;
    CYGWIN*)    machine=Cygwin;;
    MINGW*)     machine=MinGw;;
    *)          machine="UNKNOWN:${unameOut}"
esac
echo ${machine}

if ["$1"==""]; then
	prod = true
fi

if ["$1"=="dev"]; then
	devUsage
fi

function devUsage(){
	if ["$2"==""]; then
		checkPort
	fi
}

function checkPort() {
	echo "Please provide valid debug port number"
	exit 1
}

if [[ ${machine} -eq "Mac" ]]
then
  chmod +x ./workbench/Eclipse.app/Contents/MacOS/eclipse
  if ["$prod"=="true"]; then
	./workbench/Eclipse.app/Contents/MacOS/eclipse
  else
	./workbench/Eclipse.app/Contents/MacOS/eclipse --launcher.stickyVmargs -vmargs -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=$2
  fi
else
  chmod +x ./workbench/eclipse
  if ["$prod"=="true"]; then
	./workbench/eclipse
  else
	./workbench/eclipse --launcher.stickyVmargs -vmargs -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=$2
  fi
fi