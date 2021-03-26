# Percussion CMS Developer Tools

The Percussion CMS Developer tools are a collection of tools and utilities that areuseful when working with Percussion.

As of version 8.0.2, the tools are compatible with Java 1.8 Runtime. 

Launcher scripts are provided in cmd (Windows) or shell script (Linux / OSX).  

Note for Linux / OSX tools: Behavior of the tools on Linux and OSX should be considered experimental. Additionally, the shell scripts rely on the bash shell, so if you do not have bash installed please do so using [Homebrew](https://brew.sh/) or another means before running them.

## CMS Workbench
Workbench is the primary tool for modeling Content Types and creating Templates that can be used for publishing content.

There are three distributions of the Workbench available:

* OSX
* Windows
* Linux Desktop (GTK)

The Workbench has been updated to Java 1.8 (supplied by the users system) and is currently at the Helios
version of the Eclipse platform.

## Content Explorer
The Content Explorer is included as a standalone java application.  This can be used
as an alternative to launching the Content Explorer with Web Start or as a Java Applet. 

Launchers (from the devtools folder)

* Windows: 
``` shell
start <InstallDir>/devtools/ContentExplorer.cmd
```  

* Linux: 
```shell
./<InstallDir>/devtools/ContentExplorer.sh
```

The ContentExplorer jar is self-contained, and the launcher and jar may be deployed to end user systems.
The only downside to this approach is that updates will not be pushed to clients deployed in this way automatically like they are with the jnlp / WebStart deployment.

Content Explorer can be used against Rhythmyx 7.3.2 or Percussion CMS 8.0

## Package Builder
The Package Builder tool can be used to create pre-defined packages of CMS objects
for deployment on a remote CMS server.  Packages managed by Package Builder are intended to
be maintained and managed from the source server such as a development server.

Pushing updated package versions to remote users of the package from the source server to the target.

Launchers:
* Windows:
```shell
start <InstallDir>\devtools\PackageBuilder.cmd
```
* Linux or OSX:
```shell
start <InstallDir>\devtools\PackageBuilder.cmd
```
## Package Installer
The Package Installer tool can be used to install packages created with the Package Builder.

Package Installer supports UI or headless operation.

### UI Mode
UI Launchers:
* Windows:
```shell
start <InstallDir>\devtools\PackageInstaller.cmd
```
* Linux or OSX:
```shell
./<InstallDir>/devtools/PackageInstaller.sh
```
### Headless Mode
Operating Package Installer in headless mode can be helpful in scenarios where you want to automate installing packages to a remote server as part of a continuous integration flow.

```shell
cd <InstallDir>/devtools
./PackageInstaller.sh -usage
Usage:
  -package [full path to .ppkg file - required]
  -host [host name or IP - required]
  -port [port - required]
  -user [username - required]
  -password [password - required]
  -usessl [true or false - optional - defaults to false]
  -acceptwarnings [true or false if you want to accept
    warnings and continue install - optional - defaults to false]

```
## Server Administrator
The Server Administrator tool allows for remote configuration of some configurations options on a running Percussion CMS installation.

* Windows:
```shell
start <InstallDir>\devtools\ServerAdmin.cmd
```
* Linux or OSX:
```shell
./<InstallDir>/devtools/ServerAdmin.sh
```
## Adding Custom JVM Parameters
Except Workbench, all launcher scripts support a JAVA_OPTS environment variable.  To enable custom -D params or debugging edit the file:

* Windows:
```shell
<InstallDir>\devtools\DevOptions.bat
```
* Linux:
```shell
<InstallDir>/devtools/DevOptions.sh
```

Uncomment the JAVA_OPTS variable and update it to include your options.  This is handy for debugging setup when working on an issue with the tools themselves.
The DevOptions.cmd/.sh script files will not be overwritten on upgrade.

Workbench options can be changed by editing the following file:

Windows:

```shell
<installDir>\devtools\workbench\eclipe.ini
```

OSX:
```shell
<installDir>/devtools/workbench/Eclipse.app/Contents/MacOS/eclipse.ini
```

Linux:
```shell
<installDir>/devtools/workbench/eclipe.ini
```

### Customizing Workbench JVM settings
An example eclipse.ini with a custom -agentlib param to enable Java debug on port 8052
```shell
-vmargs
-Dosgi.requiredJavaVersion=1.8
-Xms128m
-Xmx384m
-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8052
```
The -Xmx parameter indicates the maximum amount of memory that the Workbench can use.

### Specifying the Java Runtime that the Workbench uses

See the [eclipse wiki for more information](https://wiki.eclipse.org/Eclipse.ini) on the eclipse.ini options.

Expanding on the previous example, in the config below the vm used will be the jvm located at C:\Java\JDK\1.8\bin\javaw.exe

```shell
-vm
C:\Java\JDK\1.8\bin\javaw.exe
-Dosgi.requiredJavaVersion=1.8
-Xms128m
-Xmx384m
-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8052
```
This can be helpful to configure if security has mandated a particular location / version of the JRE on developer systems. 

## Inactive / Incomplete Development Tools
The following tools are present in code but are not currently targeted for inclusion in the Development Tools distribution for lack of maintainer / user interest.  If you have a need for these tools in your deployment please raise an issue on [GitHub](https://www.github.com/percussion/percussioncms-tools/issues), the [Percussion community](https://community.percussion.com), or if you are an active Percussion customer, via the [support portal] (https://support.percussion.com)

* Multi Server Manager - MSM
* Loader - Enterprise Content Connector
* htmlconverter / XSpLit
