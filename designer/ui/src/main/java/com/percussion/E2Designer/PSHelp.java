/*[ PSHelp.java ]**************************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.util.PSProperties;

import java.io.File;
import java.text.MessageFormat;
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
public class PSHelp
{

   /**
    * The key whose value is the location of the Docs directory, set by the
    * installer. For example: "d:\Rhythmyx\Docs".
   **/
   public static final String DOC_ROOT_PATH = "docRoot";

   /**
    * The id that will bring up the primary HTML help file for the workbench.
   **/
   public static final String MAIN_HELP_TOPIC_ID = "default";

   /**
    * @return The one and only instance for this object. If it doesn't exist, it will
    * be created.
   **/
   public static PSHelp getInstance()
   {
      if ( null == ms_theInstance )
         ms_theInstance = new PSHelp();
      return ms_theInstance;
   }

   /**
    * The standard method for all classes part of the E2Designer package. Classes
    * in other packages should use the 2 param version. See that description for
    * full details.
    *
    * @see #launchHelp( PSProperties, String )
   **/
   public static void launchHelp( String helpId )
   {
      launchHelp( null, helpId );
   }


   /**
    * Launches the default browser to display the HTML help file associated with
    * the supplied Id. If there is no mapping for the supplied Id, the main help
    * topic will be displayed. <p/>
    *
    * @param props The properties object that contains the DOC_ROOT_PATH key
    * that is used to find the help. If null, the properties file is obtained
    * from the E2Designer class.
    *
    * @param helpId A key that is used to retrieve the HTML file name from the
    * help mapping file. If empty or null, or the key is not present in the
    * property file, the main help is shown. If there is no main help mapping,
    * the method prints a message to the console, displays an error dlg and returns.
   **/
   public static void launchHelp( PSProperties props, String helpId )
   {
      PSHelp helpInst = getInstance();

      if ( null == helpInst.m_helpIdToFileMap )
      {
         // the end user has already been notified once
         System.out.println( "Help requested but no map available." );
         return;
      }
      try
      {
         String filename;
         if ( null == helpId || 0 == helpId.trim().length())
            filename = helpInst.m_helpIdToFileMap.getString( MAIN_HELP_TOPIC_ID );
         else
         {
            filename = helpInst.m_helpIdToFileMap.getString( helpId );
            if ( null == filename )
            {
               System.out.println( "Couldn't find help mapping for '" + helpId + "'. Trying default." );
               filename = helpInst.m_helpIdToFileMap.getString( MAIN_HELP_TOPIC_ID );
            }
         }

         if ( null == props )
         {
            props = E2Designer.getDesignerProperties();
         }
         String root = null;
         if ( null != props )
            root = props.getProperty( DOC_ROOT_PATH );
         if ( null != root )
            root += File.separator;
         else
         {
            root = "Docs";
            System.out.println( "Docs root path not set, trying pre-defined location (" + root + ")" );
            root+=File.separator;
         }

         String fullname = root + filename;
         File helpFile = new File( fullname );

         if ( !helpFile.exists())
         {
            Object[] params = { helpFile.getAbsolutePath() };
            String errMsg = MessageFormat.format(
                  E2Designer.getResources().getString( "missingHelpFile" ),
                  params );

            PSDlgUtil.showErrorDialog(
                  errMsg, E2Designer.getResources().getString( "noHelpTitle" ));
         }
         else
            UTBrowserControl.displayURL( root + filename );
      }
      catch ( MissingResourceException mre )
      {
         System.out.println( "Help requested for id '" + helpId + "', but key not found or no default entry" );
         PSDlgUtil.showErrorDialog(
               E2Designer.getResources().getString( "noHelp" ),
               E2Designer.getResources().getString( "noHelpTitle" ));
      }
   }

   /**
    * Private constructor to implement Singleton pattern. Use getInstance()
    * to get the single instance. <p/>
    * Attempts to load the resource that contains the help id mappings. If it
    * fails, a message is displayed to the user via a dialog.
   **/
   private PSHelp()
   {
      // base name of the helpId property file
      String mappingFileBase = "htmlmapping";
      try
      {
            m_helpIdToFileMap = ResourceBundle.getBundle("com.percussion.E2Designer."
               + mappingFileBase, Locale.getDefault());
      }
      catch( MissingResourceException mre )
      {
         Object[] params = { mappingFileBase + ".properties" };
         String text = MessageFormat.format(
               E2Designer.getResources().getString( "missingHelpMap" ), params );

         // we can't get the mainframe because it may not be created yet
         PSDlgUtil.showErrorDialog(text,
               E2Designer.getResources().getString( "missingHelpMapTitle" ));
      }
   }

   /**
    * The HTML help id mapping file. Kept by the singleton instance. This may be
    * null if the resource can't be found and someone tries to display help.
   **/
   private ResourceBundle m_helpIdToFileMap = null;

   /**
    * The single instance of this class. Use getInstance() to obtain it.
   **/
   private static PSHelp ms_theInstance = null;
}
