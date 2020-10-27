/*[ ConsoleCommandHelper.java ]************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/


package com.percussion.E2Designer.admin;


public class ConsoleCommandHelper
{

   public static String[] getSupportedCommands()
   {
      if(m_straCommands == null)
      {
         m_straCommands = new String[11];
         m_straCommands[0] = START_SERVER;
         m_straCommands[1] = START_APPLICATION;
         m_straCommands[2] = RESTART_SERVER;
         m_straCommands[3] = RESTART_APPLICATION;
         m_straCommands[4] = STOP_SERVER;
         m_straCommands[5] = STOP_APPLICATION;
         m_straCommands[6] = SHOW_STATUS_SERVER;
         m_straCommands[7] = SHOW_STATUS_APPLICATION;
         m_straCommands[8] = SHOW_APPLICATIONS;
         m_straCommands[9] = LOG_FLUSH;
         m_straCommands[10] = LOG_DUMP;
      }

      return m_straCommands;
   }

   public static String getBaseCommand(String fullCommand)
   {
      if(fullCommand == null || fullCommand.equals(""))
         return "";

      String command = fullCommand.toLowerCase();

      String[] straCommands = getSupportedCommands();
      String strBaseCommand = null;
      for(int i=0; i<straCommands.length; i++)
      {
         strBaseCommand = straCommands[i];
         if(command.indexOf(strBaseCommand) == 0)
            return strBaseCommand;
      }
      return "";
   }

   public static String removeExtraWhitespace(String strCommand)
   {
      if(strCommand == null)
         return null;

      if(strCommand.trim().equals(""))
         return strCommand.trim();

      char[] charArray = strCommand.trim().toCharArray();
      StringBuffer buf = new StringBuffer();
      boolean bPrevWhitespace = false;
      for(int i=0; i<charArray.length; i++)
      {
         char ch = charArray[i];
         if(Character.isWhitespace(ch) && bPrevWhitespace)
            continue;
         else
            bPrevWhitespace = false;

         buf.append(ch);
         if(Character.isWhitespace(ch) && !bPrevWhitespace)
            bPrevWhitespace = true;
      }

      return buf.toString();

   }


   private static String [] m_straCommands = null;

   public final static String   START_SERVER = "start server";
   public final static String   START_APPLICATION = "start application ";  // appName

   public final static String   RESTART_SERVER = "restart server";
   public final static String   RESTART_APPLICATION = "restart application ";   // appName

   public final static String   STOP_SERVER = "stop server";
   public final static String   STOP_APPLICATION = "stop application ";   // appName

   public final static String   SHOW_STATUS_SERVER = "show status server";
   public final static String   SHOW_STATUS_APPLICATION = "show status application ";   // appName
   public final static String   SHOW_APPLICATIONS = "show applications "; // active   or   all   or  disabled

   public final static String   LOG_FLUSH = "log flush";
   public final static String   LOG_DUMP = "log dump";
}

