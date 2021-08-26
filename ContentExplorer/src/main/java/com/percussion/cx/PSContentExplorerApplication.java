package com.percussion.cx;


import com.percussion.cx.javafx.PSWindowManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PSContentExplorerApplication extends Application {
   
   static Logger log = Logger.getLogger(PSContentExplorerApplication.class);
   
   private static File configDir;
   private static Dimension dimension = new Dimension(1180, 750);

   public  static int sessionExpired = 0;
   
   private static File logConfig;
   
   private static final String DEFAULT_CONFIG_FOLDER_NAME=".perc_config";
   
   public static void main(String[] args) {
       System.out.println(Arrays.toString(args));
       launch(args);
   }
   
 
   private static volatile PSContentExplorerFrame baseFrame = null;
   
   
   public static PSContentExplorerFrame getBaseFrame()
   {
      return baseFrame;
   }

   public static void setBaseFrame(PSContentExplorerFrame baseFrame)
   {
      PSContentExplorerApplication.baseFrame = baseFrame;
   }

   @Override
   public void start(Stage primaryStage) {
      Platform.setImplicitExit(false);
      try {
         UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName() );
      } catch (Exception e) {
                 e.printStackTrace();
      }
      
      String version = this.getClass().getPackage().getImplementationVersion();
      
      
      Parameters parameters = getParameters();

      Map<String, String> params = this.getParameters().getNamed();
      String codebase = params.get("codebase");
      if (codebase==null)
    	  codebase="http://localhost:9992";
      String protocol = null;
      String host = null;
      int port = -1;
      URI uri = null;
      String clientConfigDir = DEFAULT_CONFIG_FOLDER_NAME;
      try
      {
         if (StringUtils.isNotEmpty(codebase))
         {
            uri = new URI(codebase);
            protocol = uri.getScheme();
            host = uri.getHost();
            port = uri.getPort();
            if (port==-1)
               port = ("https".equals(protocol)) ? 443:80;
            
            clientConfigDir+= File.separator+host.replace(".","_");

            if (port<0)
               clientConfigDir+="_"+port;
         }
         
      }
      catch ( URISyntaxException e)
      {
         log.error("Codebase parameter is not a valid url "+ codebase);
      }
      
      
      
      configDir = new File(System.getProperty("user.home")
            + File.separator + clientConfigDir + File.separator );
      
      configDir.mkdirs();
      

      logConfig = new File(configDir,"log4j.properties");
      
      System.out.println("Setting log4j config to "+logConfig);
      System.setProperty("configDir", configDir.getAbsolutePath());
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      
      if (!logConfig.exists())
      {
         URL inputUrl = loader.getResource("dce_log4j.properties");
         if (inputUrl != null)
            {
               PropertyConfigurator.configure(inputUrl);
            
            try
            {
               FileUtils.copyURLToFile(inputUrl, logConfig);
            }
            catch (IOException e)
            {
               log.error("Cannot write user log config to "+logConfig.getAbsolutePath());
            }
         }
      } else {
         PropertyConfigurator.configure(logConfig.getAbsolutePath());
      }
      
      
      //BasicConfigurator.configure();
      System.setProperty("javax.xml.parsers.SAXParserFactory",
            "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
      System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
            "com.percussion.xml.PSDocumentBuilderFactoryImpl");
      System.setProperty("javax.xml.transform.TransformerFactory",
            "org.apache.xalan.xsltc.trax.SmartTransformerFactoryImpl");
      System.setProperty("javax.xml.xpath.XPathFactory", "com.sun.org.apache.xpath.internal.jaxp.XPathFactoryImpl");
      System.setProperty("javax.xml.datatype.DatatypeFactory",
              "com.sun.org.apache.xerces.internal.jaxp.datatype.DatatypeFactoryImpl");

    
      log.info("USING JAVA:   " + System.getProperty("java.version"));
      log.info("DesktopContentExplorer.jar version :   " + version);
      
      
      WebView web = new WebView();
      log.info(
              "Java Version:   " + System.getProperty("java.runtime.version")
      );
      log.info(
              "JavaFX Version: " + System.getProperty("javafx.runtime.version")
      );
      log.info(
              "OS:             " + System.getProperty("os.name") + ", " 
                                 + System.getProperty("os.arch")
      );
      log.info(
              "User Agent:     " + web.getEngine().getUserAgent()
      );
      
      
      Map<String, String> namedParameters = parameters.getNamed ();
      List<String> rawArguments = parameters.getRaw ();
      List<String> unnamedParameters = parameters.getUnnamed ();

      log.debug("\nnamedParameters -");
      for (Map.Entry<String, String> entry : namedParameters.entrySet ())
         log.debug(entry.getKey () + " : " + entry.getValue ());

     
      for (String raw : rawArguments)
         log.debug(raw);


      for (String unnamed : unnamedParameters)
         log.debug(unnamed);
      
      
      log.debug("params ="+ this.getParameters().getNamed());
      
      log.debug("protocol ="+ protocol);
      log.debug("host ="+ host);
      log.debug("port ="+ port);
      
      
      baseFrame = new PSContentExplorerFrame(uri);  
      
      PSWindowManager.getInstance().addRoot(baseFrame);
      
      log.info("Launching desktop content explorer");
   }
   
   public static PSContentExplorerApplet getApplet()
   {
      return baseFrame.getApplet();
   }
   
  
   
   public static File getConfigDir()
   {
      return configDir;
   }

   public static void setConfigDir(File configDir)
   {
      PSContentExplorerApplication.configDir = configDir;
   }
   public static void logout()
   {
      SwingUtilities.invokeLater(() ->
      {
         baseFrame.logout();
      });
   }
   
   @Override
   public void stop() throws Exception
   {
      if (baseFrame != null)
      {
         SwingUtilities.invokeLater(() ->
         {
            baseFrame.cleanup();
            Platform.exit();
            System.exit(0);
         });
      }
   }
   public static Dimension getMainFrameSize(){
    
      return getDimension();
      
   }

   public static Dimension getDimension()
   {
      return dimension;
   }

   public static void setDimension(Dimension dimension)
   {
      PSContentExplorerApplication.dimension = dimension;
   }
   
   
   
}
