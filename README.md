# Percussion CMS Unified Development Tools

This project contains the various desktop tools that can be used for working with Percussion CM1 5.4 and Rhythmyx 7.3.2.

## Maven Toolchain Configuration 
These projects have been updated to compile at the java 1.8 target.  Currently they cannot be run without an Oracle 1.8 JDK as OpenJDK 1.8 does not include JavaFX wich many of the projects depend on.  

You will need to add an Oracle 1.8 to your ~/.m2/toolchains.xml file. For example:

```xml
<toolchain>
    <type>jdk</type>
    <provides>
      <version>8</version>
      <vendor>oracle</vendor>
    </provides>
    <configuration>
      <jdkHome>/Library/Java/JavaVirtualMachines/jdk1.8.0_261.jdk/Contents/Home/</jdkHome>
    </configuration>
  </toolchain>
```
## Importing into IntelliJ
From the main IntelliJ menu pick import from source control and provide https://github.com/percussion/percussioncms-tools.git as the URL for the repository.

## Sub Modules
The tools are in the repository as separate sub modules.  The following commands should run before you start to work on code changes.

```shell script
git submodule foreach --recursive git fetch
git submodule foreach --recursive git checkout development
git submodule foreach --recursive git pull
```

## Building the projects
```shell script
mvn clean install
````
