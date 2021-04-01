@echo off

rem Add your own options here and the scripts will include them when launching tools.
rem useful for setting -D or -xmx settings etc.
rem SET PATH=C:\Program Files\java\jre1.8.0_271\bin;%PATH%
rem SET JRE_HOME=C:\Program Files\java\jre1.8.0_271
SET JAVA_OPTS=-Djavax.xml.parsers.DocumentBuilderFactory=com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl -Djavax.xml.parsers.SAXParserFactory=com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl
