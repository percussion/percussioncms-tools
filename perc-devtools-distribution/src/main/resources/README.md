#Percussion CMS Development Tools

Update Version: ${project.version}

This is the cross-platform distribution of the Percussion CMS tools.

Target CMS Server Versions:

* Percussion CMS 8.0
* Percussion Rhythmyx 7.3.2

Target OS Versions:

* OSX (MacOSX Big Sur)
* Linux Desktop (Experimental - Untested)
* Microsoft Windows (version 10)

## How to Install
Confirm that the current java version in your path is a 1.8 JRE.

```
java -version
```
Update your system to include a current 1.8 JRE if one is not present.

Open a Command Prompt or terminal and run the following command:

```shell
cd <Path to this file>
java -jar perc-devtools-distribution-${project.version}.jar /path/to/install/to
```

Replace /path/to/install/to with the directory that you want to install the development tools to.

On running the command, installation temp files will be created in your temporary directory
and the updated Workbench and other tools will be installed.

If the installation fails for any reason please [report an issue](https://www.github.com/percussion/percussioncms-tools/issues)

A seperate README.md file will be created in the devtools folder that contains info on the tools that are available and instructions on how to run them.