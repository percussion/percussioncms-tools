/******************************************************************************
 *
 * [ PSEclHelpManager.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.packagerhelp;

import org.eclipse.help.standalone.Help;

import java.io.File;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * This is a helper class to launch regular and context sensitive help. This is
 * a singleton object. To prevent the class from being unloaded, a single reference
 * should be kept by a class that is always loaded. This will prevent the help
 * mapping file from being loaded more than once. <p/>
 * The mapping file used is htmlmapping.properties. Entries of the following
 * form are expected:<p/>
 * <DEFAULT_HELP_KEY>=<HTML file to display if no id is supplied><p/>
 * <helpId>=<HTML file to display for a specific id><p/>
 * <DEFAULT_HELP_KEY> should be the value as specified below and must be present.<p/>
 * The HTML file names should be relative to the installed 'Docs' directory, as
 * set by the installer.<p/>
 * Typically, there will be a <helpId> for every dialog and tab. The class name
 * should be relative to this package. For example,
 * com.percussion.E2Designer.AppSecDialog should pass
 * in an id of 'AppSecDialog', while com.percussion.E2Designer.admin.MainDialog
 * should pass in an id of 'admin.MainDialog'. This will prevent any name collisions.<p/>
 *
 * This class may be used when workbench is not running, so don't make assumptions
 * about the presence of the workbench.
**/
public class PSEclHelpManager
{   
   /**
    * @return The one and only instance for this object. If it doesn't exist, it will
    * be created.
   **/
   public static PSEclHelpManager getInstance()
   {
      if ( null == ms_theInstance )
         ms_theInstance = new PSEclHelpManager();
      return ms_theInstance;
   }

   /**
    * Launches the default browser to display the HTML help file associated with
    * the supplied Id. If there is no mapping for the supplied Id, the main help
    * topic will be displayed. <p/>
    *
    * @param helpId A key that is used to retrieve the HTML file name from the
    * help mapping file. If empty or null, or the key is not present in the
    * property file, the main help is shown. If there is no main help mapping,
    * the method prints a message to the console, displays an error dlg and returns.
   **/
   public static void launchHelp( String helpId )
   {
      ResourceBundle helpIdToFileMap = null;
      PSEclHelpManager helpInst = getInstance();


      
      if ( null == m_pkgHelpIdToFileMap || null == m_insHelpIdToFileMap )
      {
         // the end user has already been notified once
         System.out.println( "Help requested but no map available." );
         return;
      }
      
      if ( helpId.contains("com.percussion.packager.ui") )
      {
         helpIdToFileMap = m_pkgHelpIdToFileMap;
      }
      else
      {
         helpIdToFileMap = m_insHelpIdToFileMap;
      }

      try
      {
         String filename;
         if ( null == helpId || 0 == helpId.trim().length())
            filename = helpIdToFileMap.getString( MAIN_HELP_TOPIC_ID );
         else
         {
            filename = helpIdToFileMap.getString( helpId );
            if ( null == filename )
            {
               System.out.println( "Couldn't find help mapping for '" + helpId + "'. Trying default." );
               filename = helpIdToFileMap.getString( MAIN_HELP_TOPIC_ID );
            }
         }
         System.out.print("Help ID - " + helpId);
         System.out.println();
         System.out.print("Filename - " + filename);
         System.out.println();
         
         String[] opt = {"-eclipsehome", DOC_ROOT_PATH, "-product", "com.percussion.doc.help.packageinstaller.Installer"};
         Help h = new Help(opt);
         h.start();
         h.displayHelp(HELP_TOPIC_ROOT + filename);
         
      }
      catch ( MissingResourceException mre )
      {
         System.out.println( "Help requested for id '" + helpId + "', but key not found or no default entry" );
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Private constructor to implement Singleton pattern. Use getInstance()
    * to get the single instance. <p/>
    * Attempts to load the resource that contains the help id mappings. If it
    * fails, a message is displayed to the user via a dialog.
   **/
   private PSEclHelpManager()
   {
      // base name of the helpId property file
      String mappingFileBase = "htmlmapping";
      try
      {
            m_pkgHelpIdToFileMap = ResourceBundle.getBundle("com.percussion.packager.ui."
               + mappingFileBase, Locale.getDefault());
            m_insHelpIdToFileMap = ResourceBundle.getBundle("com.percussion.packageinstaller.ui."
                  + mappingFileBase, Locale.getDefault());
      }
      catch( MissingResourceException mre )
      {
         System.out.println( "Could not find help mapping file." );
      }
   }

   /**
    * Finds eclipse dir.
    * 
    * @return path to eclipse dir
    */
   private static String getDocPath()
   {
      File f = new File(".");
      String filePath = f.getAbsolutePath();
      String docPath = filePath.substring(0, filePath.length()-1);
      docPath = docPath + "eclipse";
      return docPath;
   }
   
   /**
    * The key whose value is the location of the Docs directory, set by the
    * installer. For example: "d:\Rhythmyx\Docs".
   **/
   public static final String DOC_ROOT_PATH = getDocPath();

   /**
    * The id that will bring up the primary HTML help file for the workbench.
   **/
   public static final String MAIN_HELP_TOPIC_ID = "default";
   
   /**
    * Root location of files
   **/
   public static final String HELP_TOPIC_ROOT = "/com.percussion.doc.help.";

   /**
    * The HTML help id mapping file. Kept by the singleton instance. This may be
    * null if the resource can't be found and someone tries to display help.
   **/
   private static ResourceBundle m_pkgHelpIdToFileMap = null;
   
   /**
    * The HTML help id mapping file. Kept by the singleton instance. This may be
    * null if the resource can't be found and someone tries to display help.
   **/
   private static ResourceBundle m_insHelpIdToFileMap = null;
   
   /**
    * The single instance of this class. Use getInstance() to obtain it.
   **/
   private static PSEclHelpManager ms_theInstance = null;
}
