/******************************************************************************
 *
 * [ PSResourceLoader.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.util;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

/**
 * Utility class to load workbench resources.
 * Adapted existing Workbench logic.
 *
 * @author Andriy Palamarchuk
 */
public class PSResourceLoader
{
   /**
    * Loads character sets from the configuration file.
    */
   @SuppressWarnings("unchecked")
   public static List<String> loadCharacterSets() throws IOException, FileNotFoundException
   {
      Properties charsets = new Properties();
      charsets.load(new FileInputStream(
            new File(ms_resourceHome, CHARSET_PROPS_FILENAME)));
      List<String> charsetNames = new ArrayList<String>();
      for (Object name : charsets.keySet())
      {
         charsetNames.add((String) name);
      }
      final Collator coll = Collator.getInstance();
      coll.setStrength(Collator.PRIMARY);
      Collections.sort(charsetNames, coll);
      return charsetNames;
   }
   
   /**
    * Loads mime types.
    * Returned list is sorted, contains no dups.
    */
   @SuppressWarnings("unchecked")
   public static List<String> loadMimeTypes() throws IOException, FileNotFoundException
   {
      final HashSet<String> mimeTypesSet = new HashSet<String>();
      for (Object mimeType : loadMimeExtMap().values())
      {
         mimeTypesSet.add((String) mimeType);
      }
      List<String> mimeTypes = new ArrayList<String>(mimeTypesSet);
      Collator coll = Collator.getInstance();
      coll.setStrength(Collator.PRIMARY);
      Collections.sort(mimeTypes, coll);
      return mimeTypes;
   }

   /**
    * Maps extensions to mime types. Keys - extensions, values - types.
    */
   public static Properties loadMimeExtMap() throws IOException, FileNotFoundException
   {
      Properties mimeExtMap = new Properties();
      mimeExtMap.load(new FileInputStream(
            new File(ms_resourceHome, PSResourceLoader.EXTMIME_PROPS_FILENAME)));
      return mimeExtMap;
   }

   /**
    * Returns the last port opened property from the designer properties.  This
    * will be the Rhythmyx server port following an installation of both the 
    * server and development tools.  Defaults to 9992.
    */
   public static int getLastPortOpened() throws IOException, FileNotFoundException
   {
      Properties designerProps = new Properties();
      designerProps.load(new FileInputStream(
            new File(ms_resourceHome, DESIGNER_PROPS_FILENAME)));
      
      int port = 9992;
      String strPort = designerProps.getProperty(LAST_PORT_OPENED_PROP);
           
      if (strPort != null)
      {
         if (strPort.trim().length() > 0)
            port = Integer.parseInt(strPort);
      }
      return port;
   }
   
   /**
    * Returns the last server opened property from the designer properties.
    * Defaults to localhost.  
    */
   public static String getLastServerOpened() throws IOException, FileNotFoundException
   {
      Properties designerProps = new Properties();
      designerProps.load(new FileInputStream(
            new File(ms_resourceHome, DESIGNER_PROPS_FILENAME)));
      
      String server = designerProps.getProperty(LAST_SERVER_OPENED_PROP);
      
      if (server != null)
      {
         if (server.trim().length() == 0)
            server = "localhost";
      }
      
      return server;
   }
   
   /**
    * Returns the last user name property from the designer properties.
    * Defaults to admin1.  
    */
   public static String getLastUserName() throws IOException, FileNotFoundException
   {
      Properties designerProps = new Properties();
      designerProps.load(new FileInputStream(
            new File(ms_resourceHome, DESIGNER_PROPS_FILENAME)));
      
      String user = designerProps.getProperty(LAST_USER_NAME_PROP);
      
      if (user != null)
      {
         if (user.trim().length() == 0)
            user = "Admin";
      }
      
      return user;
   }
   
   /**
    * Sets value for the root directory to use for file path resolution, may be
    * <code>null</code> to use the current directory.
    */
   public static void setRootDir(File resourceHome)
   {
      ms_resourceHome = resourceHome;
   }
   
   /**
    * Check if upgrade resource file exists, if present <b>delete</b> after 
    * checking.
    * 
    * @param fileName the file to check if it exists assumed not 
    * <code>null</code> or empty
    * @return <code>true</code> if present <code>false</code> otherwise.
    */
   
   public static boolean checkUpgradeResourceFileExists(String fileName)
   {
      boolean exists = false;
      if ( StringUtils.isNotBlank(fileName) )
      {
         File upgFile = new File(ms_resourceHome, ms_configDir+fileName);
         if (upgFile.exists())
         {
            exists = true;
            upgFile.delete();
         }
      }
      return exists;
   }
   
   /**
    * This is the filename (relative to installation dir) of the properties
    * file that contains the list of character sets to be shown in the
    * default charsets combo boxes. The file contains entries
    * of the form:
    * <p>charset=</p>
    * where charset is an IANA registered name for a character encoding.
    * <p>
    * The variable is package access so other dialogs can use the same file.
    */
   private static final String CHARSET_PROPS_FILENAME = 
         "rxconfig/Workbench/charsets.properties";
   /**
    * This is the filename (relative to installation dir) of the properties
    * file that contains the extension to mime type mappings. The keys from
    * this file are placed in a combo box used in the mime/ext map table and
    * the values are placed in a combo box used in the mime/ext map table.
    * Entries should be of the form:
    * <p>ext=mimetype</p>
    * where <i>ext</i> is the file extension, w/o the leading period and <i>
    * mimetype</i> is a standard mime type descriptor (e.g. <i>text/html</i>).
    * <p>
    * Each extension should be unique w/in the file, but a given mime type
    * may appear more than once.
    * <p>
    * The variable is package access so other dialogs can use the same file.
    */
   public static final String EXTMIME_PROPS_FILENAME = 
      "rxconfig/Workbench/mimemapwb.properties";

   /**
    * This file exists during upgrade and is deleted upon starting WB. The 
    * consumer is Preferences which will modify settings during upgrade.
    */
   private static final String ms_configDir="rxconfig/Workbench/";
   
   /**
    * This is the filename (relative to installation dir) of the properties file
    * that contains the workbench user connection info used in the initial
    * startup after new installs and 5.x upgrades.
    * 
    */
   private static final String DESIGNER_PROPS_FILENAME =
      "rxconfig/Workbench/designer.properties";
   
   /**
    * @see #getResourceHome()
    */
   private static File ms_resourceHome = new File("./default-config");
   
   /**
    * This is the last port opened property name from designer.properties.
    */
   private static final String LAST_PORT_OPENED_PROP = "last_port_opened";
   
   /**
    * This is the last server opened property name from designer.properties. 
    */
   private static final String LAST_SERVER_OPENED_PROP = "last_server_opened";
   
   /**
    * This is the last user name property name from designer.properties.
    */
   private static final String LAST_USER_NAME_PROP = "last_user_name";
}
