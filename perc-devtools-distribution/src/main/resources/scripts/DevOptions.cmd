@echo off

rem Add your own options here and the scripts will include them when launching tools.
rem useful for setting -D or -xmx settings etc.
rem SET PATH=C:\Program Files\java\jre1.8.0_271\bin;%PATH%
rem SET JRE_HOME=C:\Program Files\java\jre1.8.0_271
SET JAVA_OPTS=-Djava.net.preferIPv4Stack=true -Djava.net.preferIPv4Addresses=true -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Dhttps.protocols=TLSv1.2 -Djavax.xml.transform.TransformerFactory=com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl -Djavax.xml.parsers.SAXParserFactory=com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl -Djavax.xml.datatype.DatatypeFactory=com.sun.org.apache.xerces.internal.jaxp.datatype.DatatypeFactoryImpl -Djavax.xml.parsers.DocumentBuilderFactory=com.percussion.xml.PSDocumentBuilderFactoryImpl -Dxml.catalog.files=/PercussionXMLCatalog.xml
