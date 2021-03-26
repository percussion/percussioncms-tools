/*[ ServerConsole.java ]*******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.conn.PSServerException;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRemoteConsole;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * ServerConsole wraps the servers remote console.
 */
////////////////////////////////////////////////////////////////////////////////
public class ServerConsole
{
   /**
    * Create a new PSRemoteConsole using the passed connection.
    *
    * @param   connection      a valid server connection
    */
   //////////////////////////////////////////////////////////////////////////////
   public ServerConsole(ServerConnection connection)
   {
      try
      {
         m_connection = connection;
         m_console = new PSRemoteConsole(m_connection.getConnection());
      }
      catch (PSIllegalArgumentException iae )
      {
         handleException( iae );
      }
   }

   /**
    * Start the passed application.
    *
    * @param   appName      name of application to start
    */
   //////////////////////////////////////////////////////////////////////////////
   public void startApplication(String appName)
   {
      try
      {
         Document doc   = execute("start application " + appName);
         if ( null != doc )
            processReturn( doc, IPSServerErrors.RCONSOLE_APP_STARTED );
      }
      catch ( PSIllegalArgumentException iae )
      {
         handleException( iae );
      }
      catch (PSAuthorizationException ae )
      {
         handleException( ae );
      }
      catch ( PSServerException se )
      {
         handleException( se );
      }
      catch ( PSAuthenticationFailedException afe )
      {
         handleException( afe );
      }
      catch ( IOException ioe )
      {
         handleException( ioe );
      }
/*
      catch (Exception e)
      {
         e.printStackTrace();
      }
*/
   }

   /**
    * Stop the passed application.
    *
    * @param   appName      name of application to stop
    */
   //////////////////////////////////////////////////////////////////////////////
   public void stopApplication(String appName)
   {
      try
      {
         Document doc   = execute("stop application " + appName);
         if ( null != doc )
            processReturn( doc, IPSServerErrors.RCONSOLE_APP_SHUTDOWN );
      }
      catch ( PSIllegalArgumentException iae )
      {
         handleException( iae );
      }
      catch (PSAuthorizationException ae )
      {
         handleException( ae );
      }
      catch ( PSServerException se )
      {
         handleException( se );
      }
      catch ( PSAuthenticationFailedException afe )
      {
         handleException( afe );
      }
      catch ( IOException ioe )
      {
         handleException( ioe );
      }
/*
      catch (Exception e)
      {
         e.printStackTrace();
      }*/
   }

   /**
    * Restart the passed application.
    *
    * @param   appName      name of application to restart
    */
   //////////////////////////////////////////////////////////////////////////////
   public void restartApplication(String appName)
   {
      try
      {
         Document doc   = execute("restart application " + appName);
         if ( null != doc )
            processReturn( doc, IPSServerErrors.RCONSOLE_APP_RESTARTED );
      }
      catch ( PSIllegalArgumentException iae )
      {
         handleException( iae );
      }
      catch (PSAuthorizationException ae )
      {
         handleException( ae );
      }
      catch ( PSServerException se )
      {
         handleException( se );
      }
      catch ( PSAuthenticationFailedException afe )
      {
         handleException( afe );
      }
      catch ( IOException ioe )
      {
         handleException( ioe );
      }
   }

   /**
    * Reads the supplied response docuement that was returned from an execute
    * command. It parses the doc to find the result code and compares it to the
    * supplied expectedResult. If it matches, <code>true</code> is returned.
    * If it doesn't, a message is displayed to the user and <code>false</code>
    * is returned.
    *
    * @param response The document returned from executing a command on the
    * server's remote console.
    *
    * @param expectedResult The result code expected on successful execution of
    * the command.
    *
    * @return <code>true</code> if the returned code matches the expectedResult,
    * <code>false</code> otherwise.
   **/
   private boolean processReturn( Document response, int expectedResult )
   {
      PSXmlTreeWalker   walker   = new PSXmlTreeWalker( response );
      int result = -1;
      if(walker.getNextElement("resultCode", true, true) != null)
         result = Integer.parseInt(walker.getElementData("resultCode", false));
      if ( result == expectedResult )
         return true;

      String error = null;
      if(walker.getNextElement("resultText", true, true) != null)
         error = walker.getElementData("resultText", false);

      if ( null == error )
      {
         String[] astrParams =
         {
            String.valueOf( result )
         };

         error = MessageFormat.format( m_res.getString( "ErrorCode" ), astrParams);
      }
      showExceptionMessage( "CommandFailed", error );
      return false;
   }

   /**
    * Execute the passed command and return the XML document. processReturn() can
    * be used to analyze the results. If an exception is thrown, handleException()
    * can be called to display the error to the end user, if desired.
    *
    * @param   command      command defining which document should be retrieved
    *
    * @return The result document from executing the command.
    *
    * @throws PSIllegalArgumentException If command is not a valid command
    * @throws PSAuthorizationException If user is not authorized to perform the
    * requested command
    * @throws PSServerException A general purpose exception for most failures
    * @throws IOException If an IO exception occurs
    *
    * @see #processReturn
    * @see #handleException
    */
   //////////////////////////////////////////////////////////////////////////////
   public Document execute(String command)
         throws PSIllegalArgumentException, PSAuthorizationException,
            PSServerException, IOException, PSAuthenticationFailedException
   {
      return m_console.execute(command);
   }

   /**
    * Use these exception handlers to display an exception and its proper text
    * if user interaction is desired.
   **/
   public static void handleException( PSIllegalArgumentException iae )
   {
      showExceptionMessage( "IllegalArg", iae.getLocalizedMessage());
   }

   public static void handleException( PSServerException se )
   {
      showExceptionMessage( "ServerException", se.getLocalizedMessage());
   }

   public static void handleException( PSAuthorizationException ae )
   {
      showExceptionMessage( "AuthException", ae.getLocalizedMessage());
   }

   public static void handleException( IOException ioe )
   {
      showExceptionMessage( "IOException", ioe.getLocalizedMessage());
   }

   public static void handleException( PSAuthenticationFailedException afe )
   {
      showExceptionMessage( "AuthFailed", afe.getLocalizedMessage());
   }

   /**
    * Pops up a modal message dialog w/ an OK button. The message text is obtained from
    * the resource bundle using the supplied message key. The message is expected
    * to accept a single parameter. The exceptionText will be passed in as this
    * parameter. If the exception text is null, a string indicating that it was
    * empty will be passed in.
    *
    * @param messageKey The key to use to find the string in the resource bundle
    * for this class.
    *
    * @param exceptionText The detail message of the exception.
   **/
   private static void showExceptionMessage( String messageKey, String exceptionText )
   {
      try
      {
         if ( null == exceptionText || 0 == exceptionText.trim().length())
            exceptionText = m_res.getString( "NoTextAvailable" );
         String[] astrParams =
         {
            exceptionText
         };

         JTextArea textBox = new JTextArea(
               MessageFormat.format( m_res.getString( messageKey ), astrParams));//, 8, 20);
         textBox.setWrapStyleWord( true );
         textBox.setLineWrap( true );
         textBox.setEditable( false );
         JScrollPane pane = new JScrollPane( textBox );
         pane.setPreferredSize(new Dimension( 400, 125));
         JOptionPane.showMessageDialog( AppletMainDialog.getMainframe(),
               pane,
               m_res.getString( "RemoteConsoleExceptionTitle" ),
               JOptionPane.ERROR_MESSAGE );
      }
      catch ( MissingResourceException mre )
      {
         JOptionPane.showMessageDialog( AppletMainDialog.getMainframe(),
               "An exception occurred but the text resource could not be found (key=" + messageKey + ").",
               "MissingResourceException",
               JOptionPane.ERROR_MESSAGE);
      }
   }


   /**
    * Get the server connection of this console
    *
    */
  //////////////////////////////////////////////////////////////////////////////
  public ServerConnection getConnection()
  {
     return m_connection;
  }

  //////////////////////////////////////////////////////////////////////////////
   /**
    * the server connection used for this remote console
    */
   private ServerConnection m_connection = null;
   /**
    * the servers remote console
    */
  private PSRemoteConsole m_console = null;
   /**
   * Resources
   */
  private static ResourceBundle m_res = PSServerAdminApplet.getResources();
}
