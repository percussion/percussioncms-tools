/******************************************************************************
 *
 * [ PSDeploymentClient.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import com.percussion.deployer.client.IPSDeployConstants;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownDocTypeException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSDeployException;
import com.percussion.guitools.BitmapManager;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSResources;
import com.percussion.tools.help.PSJavaHelp;
import com.percussion.util.PSProperties;
import com.percussion.util.PSStringComparator;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TreeMap;


/**
 * The class to start the deployment client and supports methods to load and
 * save the server registrations.
 */
public class PSDeploymentClient
{
   /**
    * Loads all registered servers and initializes main frame with this list of
    * servers and displays the frame.
    */
   public static void main(String[] args)
   {
      if ((args != null) && (args.length > 0))
      {
         List argsList = Arrays.asList(args);
         if (argsList.contains("-t") || argsList.contains("-T"))
            ms_supportMode = true;
         else if (argsList.contains("/t") || argsList.contains("/T"))
            ms_supportMode = true;
      }
      try {
         if (ms_supportMode && isSampleMode())
         {
            throw new IllegalArgumentException(
               "Invalid arguments specified - " +
               "specify only one of support or sample switch");
         }

         //load the resources required by the application.
         getResources();
         loadUserProperties();

         Map servers = loadServers();

         String strLnFClass = UIManager.getSystemLookAndFeelClassName();
         LookAndFeel lnf =
            (LookAndFeel) Class.forName(strLnFClass).newInstance();
         UIManager.setLookAndFeel( lnf );

         ms_imageLoader = BitmapManager.getBitmapManager(
            PSDeploymentClient.class);
         ms_frame = new PSMainFrame(servers);
         ms_errDlg = new ErrorDialogs(ms_frame);
         ms_help = PSJavaHelp.getInstance();
         
         ms_help.setHelpSet(
            PSJavaHelp.getHelpSetURL(IPSDeployConstants.HELPSET_FILE),
            "com.percussion.deployer.ui.helptopicmapping");
         
         ms_frame.setVisible(true);
      }
      catch (Throwable e)
      {
         e.printStackTrace();
         // we're on our way out, so notify user
         System.out.println( "Unexpected exception in main" );
         ErrorDialogs.FatalError(e.getLocalizedMessage());

      }
   }

   /**
    * Loads all registered servers from the 'ServerRegistration.xml' file and
    * creates a map of servers with repository info(<code>OSDbmsInfo</code>) as
    * key and the list of <code>PSDeploymentServer</code> objects that uses
    * the repository as values. Displays an error message to the user if an
    * exception happens loading the servers. The expected format of xml is
    * <pre><code>
    * &lt;!ELEMENT RepositoryGroups (Repository+)>
    * &lt;!ELEMENT Repository (OSXDbmsInfo, Servers)>
    * &lt;!ELEMENT Servers (PSXServerRegistration+)>
    * </code></pre>
    *
    * @return the map, never <code>null</code> may be empty.
    */
   public static Map loadServers()
   {
      Map servers = new TreeMap(new PSStringComparator(
               PSStringComparator.SORT_CASE_SENSITIVE_ASC));

      File registrationFile = new File(IPSDeployConstants.CLIENT_DIR,
         REGISTER_FILE);
      if(registrationFile.exists())
      {
         FileInputStream in = null;
         try {
            in = new FileInputStream(registrationFile);
            Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
            if(doc != null)
               loadServers(doc, servers);
         }
         catch(IOException e)
         {
            System.out.println(
               "Error reading the server registrations file <" +
               registrationFile.getAbsolutePath() + ">");
            ErrorDialogs.showErrorMessage(null, MessageFormat.format(
               getResources().getString("ioReadError"),
               new String[]{registrationFile.getAbsolutePath(),
               e.getLocalizedMessage()}), null);
         }
         catch(SAXException e)
         {
            System.out.println(
               "Exception parsing the xml file to load the server registrations"
               );
            System.out.println(e.getLocalizedMessage());

            ms_errDlg.showErrorMessage(MessageFormat.format(
               getResources().getString("ioParseError"),
               new String[]{registrationFile.getAbsolutePath(),
               e.getLocalizedMessage()}), null);
         }
         catch(PSUnknownDocTypeException e)
         {
            System.out.println(
               "Exception loading server registrations defined in the file. "
               + e.getLocalizedMessage());

            ErrorDialogs.showErrorMessage(null, MessageFormat.format(
               getResources().getString("ioDocError"),
               new String[]{registrationFile.getAbsolutePath(),
               e.getLocalizedMessage()}), null);

         }
         catch(PSUnknownNodeTypeException | PSDeployException e)
         {
            System.out.println(
               "Exception loading server registrations defined in the file."
               + e.getLocalizedMessage());

            ErrorDialogs.showErrorMessage(null, MessageFormat.format(
               getResources().getString("ioDocError"),
               new String[]{registrationFile.getAbsolutePath(),
               e.getLocalizedMessage()}), null);

         }
         finally
         {
            try {
               if(in != null)
                  in.close();
            } catch(IOException ie){}
         }

      }
      return servers;
   }

   /**
    * Loads the server registrations from the document and stores them in the
    * supplied map with repository/dbms info as the key and the list of servers
    * using that repository as values.
    *
    * @param doc the document, assumed not to be <code>null</code>
    * @param servers the map of servers, assumed not to be <code>null</code>
    *
    * @throws PSUnknownDocTypeException if the document element is not a
    * supported element of this class.
    * @throws PSUnknownNodeTypeException if any element or attribute is not
    * supported.
    */
   private static void loadServers(Document doc, Map servers)
           throws PSUnknownDocTypeException, PSUnknownNodeTypeException, PSDeployException {
      if(doc.getDocumentElement() == null)
      {
         throw new PSUnknownDocTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_ROOT_NODE);
      }

      if(!doc.getDocumentElement().getTagName().equals(XML_ROOT_NODE))
      {
         throw new PSUnknownDocTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, XML_ROOT_NODE);
      }
      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      PSXmlTreeWalker tree = new PSXmlTreeWalker(doc);

      Element repository = tree.getNextElement(XML_REPOSITORY_NODE,
         firstFlags);
      while(repository != null)
      {
         loadServers(repository, servers);
         repository = tree.getNextElement(XML_REPOSITORY_NODE,
            nextFlags);
      }
   }

   /**
    * Loads the dbms info and the server registrations using that from the
    * supplied repository element.
    *
    * @param repository the repsitory element, assumed not to be <code>null
    * </code>
    * @param servers the map of repository to servers, assumed not to be
    * <code>null</code>
    */
   private static void loadServers(Element repository, Map servers)
           throws PSUnknownNodeTypeException, PSDeployException {
      List registrations = new ArrayList();

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      PSXmlTreeWalker tree = new PSXmlTreeWalker(repository);

      Element dbmsInfoEl = tree.getNextElement(OSDbmsInfo.XML_NODE_NAME,
            firstFlags);
      if (dbmsInfoEl == null)
      {
         Object[] args =
            { XML_REPOSITORY_NODE, OSDbmsInfo.XML_NODE_NAME, "null" };
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      OSDbmsInfo dbmsInfo = new OSDbmsInfo(dbmsInfoEl);

      Element serversEl = tree.getNextElement(XML_SERVERS_NODE,
            nextFlags);
      if(serversEl == null)
      {
         Object[] args =
            { XML_REPOSITORY_NODE, XML_SERVERS_NODE, "null" };
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      tree.setCurrent(serversEl);

      Element registrationsEl = tree.getNextElement(
         PSServerRegistration.XML_NODE_NAME, firstFlags);
      if(registrationsEl == null)
      {
         Object[] args =
            { XML_SERVERS_NODE, PSServerRegistration.XML_NODE_NAME, "null" };
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      while (registrationsEl != null)
      {
         registrations.add( new PSDeploymentServer(
            new PSServerRegistration(registrationsEl), dbmsInfo) );
         registrationsEl = tree.getNextElement(PSServerRegistration.XML_NODE_NAME,
            nextFlags);
      }
      servers.put(dbmsInfo, registrations);
   }

   /**
    * Saves the registered servers to 'ServerRegistration.xml' file. If the file
    * exists and there are no registered servers, then it deletes the file,
    * otherwise it overwrites the file. The format of xml is
    * <pre><code>
    * &lt;!ELEMENT RepositoryGroups (Repository+)>
    * &lt;!ELEMENT Repository (OSXDbmsInfo, Servers)>
    * &lt;!ELEMENT Servers (PSXServerRegistration+)>
    * </code></pre>
    *
    * @param servers The map of servers with repository info as key and the list
    * of <code>PSDeploymentServer</code> objects as values, may not be <code>
    * null</code>, can be empty.
    */
   public static void saveServers(Map servers)
   {
      if(servers == null)
         throw new IllegalArgumentException("servers may not be null.");

      File registrationFile = new File(IPSDeployConstants.CLIENT_DIR,
         REGISTER_FILE);
      if(servers.isEmpty())
      {
         if(registrationFile.exists())
            registrationFile.delete();
      }
      else
      {
         new File(IPSDeployConstants.CLIENT_DIR).mkdirs();
         FileOutputStream out = null;
         try {
            out = new FileOutputStream(registrationFile);
            saveServers(servers, out);
         }
         catch(IOException e)
         {
            System.out.println(
               "Error saving to the server registrations file <" +
               registrationFile.getAbsolutePath() + ">");
            ms_errDlg.showErrorMessage(MessageFormat.format(
               getResources().getString("ioWriteError"),
               new String[]{registrationFile.getAbsolutePath(),
               e.getLocalizedMessage()}), null);
         }
         finally
         {
            try {
               if(out != null)
                  out.close();
            } catch(IOException ie){}
         }
      }
   }

   /**
    * Creates the xml document from the supplied map according to the following
    * dtd and writes to the supplied output stream.
    *
    * @param servers The map of servers with repository info(<code>OSDbmsInfo
    * </code>) as key and the list of <code>PSDeploymentServer</code> objects as
    * values, assumed not to be <code>null</code> and empty.
    * @param out the output stream to write to, assumed not to be <code>null
    * </code>
    *
    * @throws IllegalArgumentException if the keys and values in the <code>
    * servers</code> map is not of expected type.
    * @throws IOException if exception happens writting the document to output
    * stream.
    */
   private static void saveServers(Map servers, FileOutputStream out)
      throws IOException
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, XML_ROOT_NODE);

      Iterator entries = servers.entrySet().iterator();
      while(entries.hasNext())
      {
         Element repositoryEl = PSXmlDocumentBuilder.addElement(
            doc, root, XML_REPOSITORY_NODE, null);
         Map.Entry entry = (Map.Entry)entries.next();

         Object key = entry.getKey();
         if( !(key instanceof OSDbmsInfo) )
            throw new IllegalArgumentException(
               "The key of the 'servers' map must be an instance of OSDbmsInfo"
               );
         OSDbmsInfo dbmsInfo = (OSDbmsInfo)key;

         Object value = entry.getValue();
         if( !(value instanceof List) )
            throw new IllegalArgumentException(
               "The value of the 'servers' map must be an instance of List that"
               + " holds the PSDeploymentServer objects");

         List registrations = (List)entry.getValue();
         if(registrations.isEmpty())
            throw new IllegalArgumentException(
            "The repository entry should have at least one registered server.");

         try {
            PSDeploymentServer[] depServers =
               (PSDeploymentServer[])registrations.toArray(
               new PSDeploymentServer[0]);

            repositoryEl.appendChild(dbmsInfo.toXml(doc));
            Element serversEl = PSXmlDocumentBuilder.addElement(
               doc, repositoryEl, XML_SERVERS_NODE, null);
            for(int i=0; i<depServers.length; i++)
            {
               PSDeploymentServer server = depServers[i];
               serversEl.appendChild(server.getServerRegistration().toXml(doc));
            }
         }
         catch(ArrayStoreException e)
         {
            throw new IllegalArgumentException(
               "The list of servers must be instances of PSDeploymentServer");
         }
      }

      PSXmlDocumentBuilder.write(doc, out);
   }

   /**
    * Loads the user properties from 'user.properties' file in
    * 'sys_deployment\client' directory if it exists, otherwise it creates empty
    * user properties. Displays an error message if an error happens loading the
    * file.
    */
   private static void loadUserProperties()
   {
      File userPropsFile = new File(IPSDeployConstants.CLIENT_DIR,
            USER_PROPS_FILE);
      try {
         if(userPropsFile.exists())
            ms_userProps = new PSProperties(userPropsFile.getPath());
         else
            ms_userProps = new PSProperties();
      }
      catch(IOException e)
      {
         ms_errDlg.showErrorMessage( MessageFormat.format(
            getResources().getString("unableToLoadUserProps"),
            new String[] {userPropsFile.getAbsolutePath(),
            e.getLocalizedMessage()}),
            getResources().getString("errorTitle") );
      }
   }

   /**
    * Saves the user properties file if there are user defined properties,
    * otherwise it deletes the file if it exists. Please see <code>
    * loadUserProperties()</code> for file location and name. Displays an error
    * message if an error happens saving the file.
    */
   public static void saveUserProperties()
   {
      File userPropsFile = new File(IPSDeployConstants.CLIENT_DIR,
         USER_PROPS_FILE);

      FileOutputStream out = null;
      try {

         if(ms_userProps.isEmpty())
         {
            if(userPropsFile.exists())
               userPropsFile.delete();
         }
         else
         {
            userPropsFile.getParentFile().mkdirs();
            out = new FileOutputStream(userPropsFile);
            ms_userProps.store(out, "");
         }
      }
      catch(IOException e)
      {
          ms_errDlg.showErrorMessage( MessageFormat.format(
            getResources().getString("unableToSaveUserProps"),
            new String[] {userPropsFile.getAbsolutePath(),
            e.getLocalizedMessage()}),
            getResources().getString("errorTitle") );
      }
      finally
      {
         try {
            if(out != null)
               out.close();
         } catch(IOException ie){}
      }
   }

   /**
    * Displays file chooser dialog set with supplied parameters. Allows only
    * a single file selection. Remembers the last working directory and offers
    * that as starting directory to browse if the supplied file to select is
    * <code>null</code>.
    *
    * @param parent the parent window of the dialog, may not be <code>null
    * </code>
    * @param fileToSelect the file to select, may be <code>null</code>
    * @param extension the file extension to filter, may not be <code>null
    * </code> or empty and should not start with '.'
    * @param description the description for the file,  may not be <code>null
    * </code> or empty.
    * @param dialogType the dialog type to use, must be one of the <code>
    * JFileChooser.OPEN_DIALOG</code> or <code>JFileChooser.SAVE_DIALOG</code>
    *
    * @return the selected file, may be <code>null</code> if user cancelled out
    * of the dialog.
    */
   public static File showFileDialog(Window parent, File fileToSelect,
      final String extension, final String description, int dialogType)
   {
      if(parent == null)
         throw new IllegalArgumentException("parent may not be null.");

      if(extension == null || extension.trim().length() == 0)
         throw new IllegalArgumentException(
            "extension may not be null or empty.");

      if(extension.startsWith("."))
         throw new IllegalArgumentException(
            "invalid extension should not start with '.'");

      if(description == null || description.trim().length() == 0)
         throw new IllegalArgumentException(
            "description may not be null or empty.");

      if(dialogType != JFileChooser.OPEN_DIALOG &&
         dialogType != JFileChooser.SAVE_DIALOG)
      {
         throw new IllegalArgumentException("invalid dialog type");
      }

      String workingDir = ms_userProps.getProperty(LAST_WORKING_DIR);

      if(workingDir == null || !(new File(workingDir)).exists())
         workingDir = System.getProperty("user.dir");

      JFileChooser chooser = new JFileChooser(workingDir);
      chooser.setMultiSelectionEnabled(false);
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      chooser.setFileFilter( new FileFilter()
      {
         //accept any directory or files ending with specified extension.
         public boolean accept(File path)
         {
            if(path.isFile() && path.getAbsolutePath().endsWith("." + extension)
               || path.isDirectory())
            {
               return true;
            }
            return false;
         }

         public String getDescription()
         {
            return description;
         }
      });

      if(fileToSelect != null)
         chooser.setSelectedFile(fileToSelect);

      int returnVal;
      if(dialogType == JFileChooser.OPEN_DIALOG)
         returnVal = chooser.showOpenDialog(parent);
      else
         returnVal = chooser.showSaveDialog(parent);

      File selectedFile = null;
      if (returnVal == JFileChooser.APPROVE_OPTION)
      {
         selectedFile = chooser.getSelectedFile();
         workingDir = selectedFile.getParent();
         ms_userProps.put(LAST_WORKING_DIR, workingDir);

         // if saving a new file, ensure we have an extension, add ours if not
         if(dialogType == JFileChooser.SAVE_DIALOG &&
            !selectedFile.exists() &&
            selectedFile.getName().indexOf(".") == -1)
         {
            selectedFile = new File(selectedFile.toString() + "." + extension);
         }
      }

      return selectedFile;
   }

   /**
    * Displays file chooser dialog for selecting deployer package files.  Calls
    * {@link #showFileDialog(Window, File, String, String, int).
    *
    * @param parent the parent window of the dialog, may not be <code>null
    * </code>
    * @param fileToSelect the file to select, may be <code>null</code>
    * @param dialogType the dialog type to use, must be one of the <code>
    * JFileChooser.OPEN_DIALOG</code> or <code>JFileChooser.SAVE_DIALOG</code>
    *
    * @return the selected file, may be <code>null</code> if user cancelled out
    * of the dialog.
    */
   public static File showPackageFileDialog(Window parent, File fileToSelect,
      int dialogType)
   {
      final String packageExt = IPSDeployConstants.ARCHIVE_EXTENSION;
      final String pkgExtMod = packageExt.substring(1);
      
      return showFileDialog(parent, fileToSelect, pkgExtMod,
            pkgExtMod.toUpperCase() + " Files (*" + packageExt + ")",
            dialogType);
   }
   
   /**
    * Gets and caches the resource bundle used by this application for menus,
    * generic labels and error messages. Displays the error message to the user
    * in case of missing resource bundle and terminates the application.
    *
    * @return the resources never <code>null</code>
    */
   public static PSResources getResources()
   {
      if (null == ms_res)
      {
         final String strResBaseName =
            "com.percussion.deployer.ui.PSDeploymentResources";
         try
         {
            // load the string resources for the application
            ms_res = (PSDeploymentResources)
               ResourceBundle.getBundle(strResBaseName);
         }
         catch(MissingResourceException e)
         {
            ErrorDialogs.FatalError(
               "Missing resource bundle for deployment client ui");
         }
      }
      return ms_res;
   }

   /**
    * Determines if the Multi-Server Manager client is
    * running in support mode or not. If running in support mode, creation of
    * archives (except sample archives) is allowed even if the Rx server is not
    * licensed for Multi-Server Manager.
    *
    * @return <code>true</code> if the Multi-Server Manager client is running
    * in support mode, <code>false</code> otherwise.
    */
   public static boolean isSupportMode()
   {
      return ms_supportMode;
   }

   /**
    * Determines if the Multi-Server Manager client has been
    * started in sample applications package creation mode. When running in
    * this mode, archive creation will be allowed only if the server is
    * licensed for Multi-Server Manager and the archives created in this mode
    * will have archiveType (attribute for PSXExportDescriptor element) set to
    * <code>PSExportDescriptor.ARCHIVE_TYPE_SAMPLE</code>. To run in this mode,
    * system property <code>IPSDeployConstants.PROP_CREATE_SAMPLE_ARCHIVE</code>
    * must be specified when starting the client.
    *
    * @return <code>true</code> if the Multi-Server Manager client has been
    * started in sample applications package creation mode, otherwise
    * <code>false</code>
    */
   public static boolean isSampleMode()
   {
      String mode = System.getProperty(
         IPSDeployConstants.PROP_CREATE_SAMPLE_ARCHIVE);
      // property defined is all that matters
      return mode != null ? true : false;
   }

   /**
    * Determine if the supplied string is valid as a name of an object stored
    * on the server.  Checks that the string is not <code>null</code>, empty,
    * and contains only alphanumeric, period, hyphen, and underscore characters.
    *
    * @param name The name to check, may be <code>null</code> or empty.
    *
    * @return <code>true</code> if the name is valid, <code>false</code>
    * otherwise.
    */
   public static boolean isValidServerObjectName(String name)
   {
      boolean isValid = true;

      if (name == null || name.trim().length() == 0)
         isValid = false;
      else
      {
         for (int i = 0; i < name.length(); i++)
         {
            if (!isValidServerNameChar(name.charAt(i)))
            {
               isValid = false;
               break;
            }
         }
      }

      return isValid;
   }

   /**
    * Fix the supplied string so it is valid as the name of an object stored
    * on the server.  Substitutes an underscore for any characaters other than
    * alphanumeric, period, hyphen, and underscore characters.
    *
    * @param name The name to check, may not be <code>null</code> or empty.
    *
    * @return The fixed name, never <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if <code>name</code> is invalid.
    */
   public static String fixServerObjectName(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      StringBuffer buf = new StringBuffer(name.length());

      for (int i = 0; i < name.length(); i++)
      {
         char next = name.charAt(i);
         if (isValidServerNameChar(next))
            buf.append(next);
         else
            buf.append('_');
      }

      return buf.toString();
   }

   /**
    * Determines if the supplied character is valid as part of the name of a
    * server object.  See {@link #isValidServerObjectName(String)} for details
    * on valid characters.
    *
    * @param test The character to check.
    *
    * @return <code>true</code> if it is valid, <code>false</code> otherwise.
    */
   private static boolean isValidServerNameChar(char test)
   {
      boolean isValid = true;
      if (!Character.isLetterOrDigit(test) && test != '-' && test != '_' &&
         test != '.')
      {
         isValid = false;
      }

      return isValid;
   }

   /**
    * Gets the main frame of this application.
    *
    * @return the frame may be <code>null</code> if this method is called before
    * the main frame is constructed.
    */
   public static PSMainFrame getFrame()
   {
      return ms_frame;
   }
   
   /**
    * Gets the connection handler of this application.
    *
    * @return the handler, never <code>null</code> 
    * 
    * @throws IllegalStateException if this method is called before
    * the main frame is constructed and the handler is initialized.
    */
   public static PSConnectionHandler getConnectionHandler()
   {
      if (ms_frame == null)
         throw new IllegalStateException(
            "Connection handler not yet initialized");
      
      return ms_frame.getConnectionHandler();
   }

   /**
    * Gets the object that can be used to display different kind of error
    * messages to the user with the main frame of this application as parent of
    * the error dialog.
    *
    * @return the error dialogs object, may be <code>null</code> if it is called
    * before it was constructed in <code>main(String[])</code> method.
    */
   public static ErrorDialogs getErrorDialog()
   {
      return ms_errDlg;
   }

   /**
    * Gets the instance of the image loader to load the images for the classes
    * in this package.
    *
    * @return the image loader,  may be <code>null</code> if it is called
    * before it was constructed in <code>main(String[])</code> method.
    */
   public static BitmapManager getImageLoader()
   {
      return ms_imageLoader;
   }

   /**
    * The object that can be used to display the error dialogs with in this
    * application. Initialized in <code>main()</code> method after constructing
    * the main frame and never <code>null</code> or modified after that.
    */
   private static ErrorDialogs ms_errDlg;

   /**
    * The main frame of the client. Initialized in <code>main()</code> method
    * and never <code>null</code> or modified after that.
    */
   private static PSMainFrame ms_frame;

   /**
    * The single instance of the image loader that can be used through out this
    * package classes to load the images required by them, initialized in
    * <code>main()</code> method and never <code>null</code> or modified after
    * that.
    */
   private static BitmapManager ms_imageLoader;

   /**
    * The singleton instance of javahelp used to launch help, initialized in
    * <code>main()</code> method and never <code>null</code> or modified after
    * that.
    */
   private static PSJavaHelp ms_help;

   /**
    * The resource bundle for this client. Initialized the first time <code>
    * getResources()</code> is called and never <code>null</code> or modified
    * after that.
    */
   private static PSResources ms_res;

   /**
    * The user properties of this application, initialized the first time <code>
    * loadUserProperties()</code> is called and never <code>null</code> after
    * that. Currently it supports only <code>LAST_WORKING_DIR</code> property,
    * gets modified whenever <code>showFileDialog(Window, File, String, String,
    * int)</code> is called.
    */
   private static PSProperties ms_userProps;

   /**
    * Stores whether the Multi-Server Manager client is running in support mode
    * or not. If the client is started with "-t" or "/t" option specified, then
    * it runs in support mode. In this mode, creation of archives (except
    * sample archives) is allowed even if the Rx server is not licensed for
    * Multi-Server Manager.
    */
   private static boolean ms_supportMode = false;

   /**
    * The name of the server registration xml file.
    */
   private static final String REGISTER_FILE = "ServerRegistration.xml";

   /**
    * The name of the user properties file.
    */
   private static final String USER_PROPS_FILE = "user.properties";

   /**
    * The name of the last working directory property for the file chooser
    * dialogs. The value of the property is the recent directory chosen by user
    * in any of the file chooser dialogs of the application.
    */
   private static final String LAST_WORKING_DIR = "lastWorkingDir";

   /**
    * The properties file that relates the archive file created with the
    * export descriptor, with each property in the format
    * 'server:port/descriptor=archiveFileLocation'. Useful to create an archive
    * from selected descriptor.
    */
   public static final String DESCRIPTOR_ARCHIVE_FILE = "archives.properties";

   /**
    * The separator to use with the key value of the {@link
    * #DESCRIPTOR_ARCHIVE_FILE} properties to separate the server from
    * descriptor.
    */
   public static final String SERVER_DESCRIPTOR_SEPARATOR = "/";

   /**
    * The constants that define the xml element and attribute names in the
    * server registration xml file.
    */
   private static final String XML_ROOT_NODE = "RepositoryGroups";
   private static final String XML_REPOSITORY_NODE = "Repository";
   private static final String XML_SERVERS_NODE = "Servers";
}
