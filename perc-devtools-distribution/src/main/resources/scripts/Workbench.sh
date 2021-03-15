#!/bin/bash -bm
source DevOptions.sh
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

if ["$1"==""]; then
	usage
fi

function usage() {
  echo "Usage: $0 {prod | dev}"
  echo "Where:  command may be prod, dev"
  echo "Where:  prod is without debug port enabled suitable for production and dev is with debug port enabled suitable for developers"
  echo "Provide Valid <port_number> if using dev mode"
  echo "Prod command example: Workbench.cmd prod"
  echo "Dev command example: Workbench.cmd dev <port_number>"
  exit 1
}


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
  if ["$1"=="prod"]; then
	./workbench/Eclipse.app/Contents/MacOS/eclipse
  else
	./workbench/Eclipse.app/Contents/MacOS/eclipse --launcher.stickyVmargs -vmargs -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=$2
  fi
else
  chmod +x ./workbench/eclipse
  if ["$1"=="prod"]; then
	./workbench/eclipse
  else
	./workbench/eclipse --launcher.stickyVmargs -vmargs -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=$2
  fi
fi