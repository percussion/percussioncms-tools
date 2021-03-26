# perc-eclipse
Contains the build for the eclipse workbench and plugins.


## Building the projects
```shell script
mvn clean install
````

## Troubleshooting

On OSX - Workbench eclipse fails to start with a JVM error:

"JavaVM: Failed to load JVM: /Library/Java/JavaVirtualMachines/jdk<version>.jdk/Contents/Home/lib/libserver.dylib
JavaVM FATAL: Failed to load the jvm library."

To work around this, in a Terminal change to the 

cd /Library/Java/JavaVirtualMachines/jdk<version>.jdk/Contents/Home/lib folder and create a symbolic link.
sudo ln -s ../jre/lib/server/libjvm.dylib libserver.dylib

Eclipse should start after this change. 