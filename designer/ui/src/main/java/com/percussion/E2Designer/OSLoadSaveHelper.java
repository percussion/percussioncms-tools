/*[ OSLoadSaveHelper.java ]****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

////////////////////////////////////////////////////////////////////////////////
public class OSLoadSaveHelper
{
   /**
    * Create the file to which we log all debug messages.
    */
   public static synchronized void createSaveLog()
   {
      try
      {
         if (DEBUG)
         {
            if (m_logFile != null)
            {
               throw new IllegalStateException(
                     "...the log has ALREADY been created!");
            }
            
            String filePath = System.getProperty("user.home");
            if ( !filePath.endsWith( File.separator ))
               filePath += File.separator; 
            filePath += "save.log";
            m_logFile = new File(filePath);
            m_outStream = new FileOutputStream(m_logFile);
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Create the file to which we log all debug messages.
    */
   public static synchronized void closeSaveLog()
   {
      try
      {
         if (DEBUG)
         {
            if (m_logFile == null || m_outStream == null)
            {
               throw new IllegalStateException(
                     "...the log has NOT been created yet!");
            }

            m_outStream.flush();
            m_outStream.close();
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Start logging a new application.
    *
    * @param appName the name of the new application
    */
   public static synchronized void startLoggingApplication(String appName)
   {
      try
      {
         if (DEBUG)
         {
            if (m_logFile == null || m_outStream == null)
            {
               throw new IllegalStateException(
                     "...the log has NOT been created yet!");
            }

            String log = "--- start application log: " + appName 
                  + " -----------------------------------------------" + ENDL;
            m_outStream.write(log.getBytes());
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Finish logging an application.
    *
    * @param appName the name of the new application
    */
   public static synchronized void finishLoggingApplication(String appName)
   {
      try
      {
         if (DEBUG)
         {
            if (m_logFile == null || m_outStream == null)
            {
               throw new IllegalStateException(
                     "...the log has NOT been created yet!");
            }

            String log = "--- stop application log: " + appName 
                  + " ------------------------------------------------" + ENDL;
            m_outStream.write(log.getBytes());
            m_outStream.write(ENDL.getBytes());
            m_outStream.flush();
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Logging save appication file.
    *
    * @param file the file saved to the application
    */
   public static synchronized void logSaveApplicationFile(File file)
   {
      try
      {
         if (DEBUG)
         {
            if (m_logFile == null || m_outStream == null)
            {
               throw new IllegalStateException(
                     "...the log has NOT been created yet!");
            }

            String fileName = file.getCanonicalPath();
            String log = "- saved application file: " + fileName + ENDL;
            m_outStream.write(log.getBytes());
            m_outStream.flush();
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Logging copy appication file.
    *
    * @param file the file copied to the application
    */
   public static synchronized void logCopyApplicationFile(File file)
   {
      try
      {
         if (DEBUG)
         {
            if (m_logFile == null || m_outStream == null)
            {
               throw new IllegalStateException(
                     "...the log has NOT been created yet!");
            }

            String fileName = file.getCanonicalPath();
            String log = "- copied application file: " + fileName + ENDL;
            m_outStream.write(log.getBytes());
            m_outStream.flush();
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Load a figures position from the user configuration into the provided
    * owner.
    *
    * @param the figures unique id
    * @param the application configuration
    * @param the owner for whom to restore the location
    */
   public static void loadOwner(int id, Properties config, UIFigure owner)
   {
      if (config != null && owner != null)
      {
         String strId = new Integer(id).toString();

         String strX = config.getProperty(LOCATION_X + strId);
         String strY = config.getProperty(LOCATION_Y + strId);
         if ((strX != null) && (strY != null))
         {
            owner.setLocation(new Integer(strX).intValue(),
                  new Integer(strY).intValue());
         }
      }
   }

   /**
    * Save a figures position to the user configuration from the provided
    * owner.
    *
    * @param the figures current unique id
    * @param the figures new unique id
    * @param the application configuration
    * @param the owner for whom to restore the location
    */
   public static void saveOwner(int currentId, int newId,
         Properties config, UIFigure owner)
   {
      try
      {
         if (config != null && owner != null)
         {
            String strCurrentId = new Integer(currentId).toString();
            String strNewId = new Integer(newId).toString();

            // add all entries for the new key
            config.setProperty(LOCATION_X + strNewId, 
                  new Integer(owner.getLocation().x).toString());
            config.setProperty(LOCATION_Y + strNewId, 
                  new Integer(owner.getLocation().y).toString());

            if (DEBUG)
            {
               if (m_logFile == null || m_outStream == null)
               {
                  throw new IllegalStateException(
                        "...the log has NOT been created yet!");
               }

               String log = "- saved figure: " + owner.getName() 
                     + "\tcurrent/new ID= " + strCurrentId + "/" + strNewId 
                     + "\tposition X/Y= " 
                     + owner.getLocation().x + "/" + owner.getLocation().y 
                     + ENDL;
               m_outStream.write(log.getBytes());
               m_outStream.flush();
            }
         }
         else
         {
            if (DEBUG)
            {
               if (m_logFile == null || m_outStream == null)
               {
                  throw new IllegalStateException(
                        "...the log has NOT been created yet!");
               }

               String log = "";
               if (config == null && owner != null)
               {
                  log += new String("- saved owner: ") +
                            owner.getName() +
                            "\tthe user config is null!" + ENDL;
               }
               else if (config != null && owner == null)
                  log += "- saved owner: the owner is null" + ENDL;
               else
               {
                  log += "- saved owner: the user config and the owner are null!"
                        + ENDL;
               }
               m_outStream.write(log.getBytes());
               m_outStream.flush();
            }
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Initialize the default locations.
    *
    * @param the application configuration
    */
   public static synchronized void initDefault(Properties config)
   {
      if (config != null)
      {
         config.setProperty(DEF_UNATTACHED_X, START_X.toString());
         config.setProperty(DEF_UNATTACHED_Y, START_UNATTACHED_Y.toString());
         config.setProperty(DEF_ATTACHED_X, START_X.toString());
         config.setProperty(DEF_ATTACHED_Y, START_ATTACHED_Y.toString());
      }
   }

   /**
    * Set default location for next attached figure.
    *
    * @param config the application configuration
    * @param fig the figure using the current location
    */
   public static synchronized void nextAttached(Properties config, UIFigure fig)
   {
      if (config != null && fig != null)
      {
         if (config.getProperty(UIAppFrame.SAVED_FROM_E2) == null &&
               config.getProperty(UIAppFrame.SAVED_FROM_DESIGNER) == null)
         {
            // get current location
            Integer x = new Integer(config.getProperty(DEF_ATTACHED_X));
            Integer y = new Integer(config.getProperty(DEF_ATTACHED_Y));

            // set next location
            Integer newX = new Integer(x.intValue() + OFFSET);
            config.setProperty(DEF_ATTACHED_X, newX.toString());

            // set figures location
            if ((x != null) && (y != null))
               fig.setLocation(x.intValue(), y.intValue());
         }
      }
   }

   /**
    * Set default location for next unattached figure.
    *
    * @param config the application configuration
    * @param fig the figure using the current location
    */
   public static synchronized void nextUnattached(Properties config, 
         UIFigure fig)
   {
      if (config != null && fig != null)
      {
         //if (((IGuiLink) fig.getData()).getFigure() == null)
         if (config.getProperty(UIAppFrame.SAVED_FROM_E2) == null &&
               config.getProperty(UIAppFrame.SAVED_FROM_DESIGNER) == null)
         {
            Integer x = new Integer(config.getProperty(DEF_UNATTACHED_X));
            Integer y = new Integer(config.getProperty(DEF_UNATTACHED_Y));

            Integer newX = new Integer(x.intValue() + OFFSET);
            config.setProperty(DEF_UNATTACHED_X, newX.toString());

            // set figures location
            if ((x != null) && (y != null))
               fig.setLocation(x.intValue(), y.intValue());
         }
      }
   }

   ///////////////////////////////////////////////////////////////////////////
   // private storage
   private static final boolean DEBUG = true;
   private static File m_logFile = null;
   private static FileOutputStream m_outStream = null;
   private static String ENDL = "\r\n";

   private static final String LOCATION_X = new String("locationX");
   private static final String LOCATION_Y = new String("locationY");

   // keys used for default locations
   private static final String DEF_ATTACHED_X = new String("defaultAttachedX");
   private static final String DEF_ATTACHED_Y = new String("defaultAttachedY");
   private static final String DEF_UNATTACHED_X = 
         new String("defaultUnattachedX");
   private static final String DEF_UNATTACHED_Y = 
         new String("defaultUnattachedY");

   private static final int OFFSET = 60;
   private static final Integer START_X = new Integer(20);
   private static final Integer START_UNATTACHED_Y = new Integer(20);
   private static final Integer START_ATTACHED_Y = new Integer(120);
}
