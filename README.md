# Percussion CMS Unified Development Tools

This project contains the various desktop tools that can be used for working with Percussion CM1 5.4 and Rhythmyx 7.3.2.  Please log any issues with the main Percussion CMS issue tracker: https://github.com/percussion/percussioncms/issues. 

## Maven Toolchain Configuration 
These projects have been updated to compile at the java 1.8 target.  Currently, they cannot be run without an Oracle 1.8 JDK as OpenJDK 1.8 does not include JavaFX which many of the projects depend on.  

You will need to add an Oracle 1.8 to your ~/.m2/toolchains.xml file (C:\Users\<username>\.m2\toolchains.xml on Windows). For example:

```xml
<toolchain>
    <type>jdk</type>
    <provides>
      <id>JavaSE-1.8</id>
      <version>1.8</version>
      <vendor>oracle</vendor>
    </provides>
    <configuration>
        <jdkHome>/Library/Java/JavaVirtualMachines/jdk1.8.jdk/Contents/Home/</jdkHome>
        <bootClassPath>
            <includes>
                <include>jre/lib/*.jar</include>
            </includes>
        </bootClassPath>
    </configuration>
  </toolchain>
```
## Importing into IntelliJ
From the main IntelliJ menu pick import from source control and provide https://github.com/percussion/percussioncms-tools.git as the URL for the repository.


## Building the projects
```shell script
mvn clean install
````
