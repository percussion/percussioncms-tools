/*[ PSXmlDomContext.java ]*****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.loader.util;

import com.percussion.loader.IPSLoaderErrors;
import com.percussion.loader.PSLoaderException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSServer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

/**
 * This class provides centralized parameter passing, exception handling
 * and trace message support for the xmldom package.
 * This class is intended for use only within the com.percussion.xmldom package
 */
public class PSXmlDomContext
{
   /**
    * The originating request for the extension
    */
   private IPSRequestContext m_req = null;

   /**
    * The Tidy Properties for this request
    **/
   private Properties m_tidyProperties = new Properties();

   /**
    * ServerPageTags file is used to parse the "ASP/JSP" tags in a file.
    **/
   private String m_serverPageTagsFile = null;

   /**
    * are we logging output to files? used for debugging
    **/
   private boolean m_logging = false;

   /**
    * the extension name determines which function we are performing. This is
    * used for error handling.
    **/
   private String m_function;

   /**
    * determine whether to use the Validating parser or not
    */
   private boolean m_validate = false;

   /**
    * Flag to determine whether to use tidy pretty print or not,
    * default is <code>true</code>.
    */
   private boolean m_pprint = true;

   /**
    * create a context where an IPSRequestContext is not available. This is
    * primarily used when debugging.
    **/
   public PSXmlDomContext(String functionName)
   {
      m_function = functionName;
      m_logging = false;
   }


   /**
    * create a context for the extension.  The context should be a be a member
    * variable of the Process method in each extension.
    **/
   public PSXmlDomContext(String functionName, IPSRequestContext req)
   {
      m_req = req;
      m_logging = req.isTraceEnabled();
      m_function = functionName;
   }


   /**
    * Allows support routines to print trace messages without knowing about
    * the IPSRequestContext
    */
   public void printTraceMessage(String msg)
   {
      if (!m_logging) return;

      if (m_req != null)
         m_req.printTraceMessage(msg);
      else
         System.out.println(msg);
   }


   /**
    * Gets the server's root value from the request.
    *
    * @return server root string in the format:
    *         127.0.0.1:&lt;port_nr>/&lt;rootname>
    */
   public String getServerRoot()
   {
      if (null == m_req)
         return "127.0.0.1:9992/Rhythmyx"; // defaults
      else
         return "127.0.0.1:" + m_req.getServerListenerPort()
               + PSServer.getRequestRoot();
   }


   /**
    * get the tidy properties for this operation context
    **/
   public Properties getTidyProperties()
   {
      return m_tidyProperties;
   }


   /**
    * set the tidy properties for this operation context
    **/
   public void setTidyProperties(Properties props)
   {
      m_tidyProperties = props;
   }


   /**
    * set the tidy properties from a file
    */
   public void setTidyProperties(String FileName) throws IOException
   {

      m_tidyProperties.load(new FileInputStream(FileName));
   }


   /**
    * determine if Tidy processing is enabled for this context
    **/
   public boolean isTidyEnabled()
   {
      return (null != m_tidyProperties && !m_tidyProperties.isEmpty());
   }


   /**
    * Sets the Server page tags file name for this context
    *
    * @param filename the name of the server page tags file
    */
   public void setServerPageTags(String filename)
   {
      m_serverPageTagsFile = filename;
   }


   /**
    * Gets the current ServerPageTags file name
    *
    * @return the name of the server page tags file, or <code>null</code> if
    *         one hasn't been assigned
    **/
   public String getServerPageTags()
   {
      return m_serverPageTagsFile;
   }


   /**
    * get the state of serverPageTags.
    *
    * @return true if a ServerPageTags.xml file has been defined
    **/
   public boolean isServerPageTags()
   {
      return (null != m_serverPageTagsFile && m_serverPageTagsFile.length() > 0);
   }


   /**
    * set the state of the logging flag
    *
    * @param logFlag a boolean determining if logging is set.  The logging flag
    *      will be automatically enabled by the constructor if the
    *      IPSRequestContext.isTraceEnabled method returns <code>true</code>
    **/
   public void setLogging(boolean logFlag)
   {
      m_logging = logFlag;
   }


   /**
    * read the logging flag
    *
    * @return <code>true</code> if logging is enabled; <code>false</code>
    *         otherwise
    */
   public boolean isLogging()
   {
      return m_logging;
   }

   /**
    * Sets the Validate flag. If this flag is <code>true</code>, use the
    * validating parser.
    */
   public void setValidate(boolean validate)
   {
      m_validate = validate;
   }

   /**
    * @return <code>true</code> when the validating parser should be used;
    * <code>false</code> when the non-validating parser should be used.
    */
   public boolean isValidate()
   {
      return m_validate;
   }

   /**
    * Sets the use tidy pprint flag. If this flag is <code>true</code>, use the
    * tidy pretty print.
    */
   public void setUsePrettyPrint(boolean pprint)
   {
      m_pprint = pprint;
   }

   /**
    * @return tidy pretty print flag.
    */
   public boolean getUsePrettyPrint()
   {
      return m_pprint;
   }

   /**
    * Prints exception context to the trace file, and optionally throws
    * a new PSLoaderException.
    * This standard exception handler should be used at the "main" level of
    * each extension to catch all unexpected exceptions.
    *
    * @param e the exception to log
    * @param throwException flag that if <code>true</code> will cause an
    *        exception to be thrown
    * @throws PSLoaderException if throwException is
    *         <code>true</code>
    */
   public void handleException(Exception e, boolean throwException)
         throws PSLoaderException
   {
      StringBuffer estr = new StringBuffer("Unexpected exception in ");
      estr.append(m_function).append("\n");
      estr.append(e.toString()).append("\n");
      estr.append(e.getMessage().toString()).append("\n");

      //print the stack trace into the tracing log.
      StringWriter stackWriter = new StringWriter();
      e.printStackTrace(new PrintWriter((Writer) stackWriter, true));
      estr.append(stackWriter.toString());

      printTraceMessage(estr.toString());

      if (throwException)
         throw new PSLoaderException(IPSLoaderErrors.UNEXPECTED_ERROR,
            e.toString());
   }


   /**
    * Prints exception context to the trace file and throws a new exception.
    * This standard exception handler should be used at the "main" level of
    * each extension to catch all unexpected exceptions.
    *
    * @param e the exception to log
    * @throws PSLoaderException always
    */
   public void handleException(Exception e)
         throws PSLoaderException
   {
      handleException(e, true);
   }
}
