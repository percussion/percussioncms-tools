/******************************************************************************
 *
 * [ ApplicationImportExport.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.conn.PSServerException;
import com.percussion.content.IPSMimeContent;
import com.percussion.design.objectstore.*;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.error.PSIllegalStateException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.util.IOTools;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


////////////////////////////////////////////////////////////////////////////////
public class ApplicationImportExport
{
   /**
    * The constructor takes an existing file chooser object
    *
    * @param chooser the file chooser
    */
   //////////////////////////////////////////////////////////////////////////////
   public ApplicationImportExport(JFileChooser chooser)
   {
      try
      {
         m_chooser = chooser;
         m_chooser.setFileFilter(new ApplicationFileFilter());
         m_chooser.setMultiSelectionEnabled(false);
         m_chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      }
      catch (Exception e)
      {
          PSDlgUtil.showError(e);
      }
   }

   /**
    * Imports the choosen XML document into an application.
    * @param ref the application to import to. Should not be <code>null</code>.
    * @return <code>true</code> if data was successfully imported.
    */
   public boolean importApplication(final IPSReference ref) throws Exception
   {
      assert ref != null;
      final int option = m_chooser.showOpenDialog(null);

      if (option == JFileChooser.APPROVE_OPTION)
      {
         final File file = m_chooser.getSelectedFile();
         final File directory = m_chooser.getCurrentDirectory();

         // save for others to use
         m_selectedFile = file;
         m_selectedDir = directory;

         final OSApplication app =
               (OSApplication) getAppModel().load(ref, true, false);

         // keep these data
         final PSApplicationType appType = app.getApplicationType();
         final PSRevisionHistory hist = app.getRevisionHistory();
         final int id = app.getId();
         final String requestRoot = app.getRequestRoot();
         
         app.fromXml(parseXml(file));
         
         // restore data
         app.setId(id);
         app.setName(ref.getName());
         app.setDescription("Imported from " + getAppFile().getPath() + ". "
               + StringUtils.defaultString(app.getDescription()));
         if (app.getApplicationType().equals(PSApplicationType.CONTENT_EDITOR))
         {
            throw new IllegalArgumentException(
                  "Content type application import is prohibited.");
         }
         // we keep the old type because we do not want users to create
         // system apps through import
         app.setApplicationType(appType);
         // have to copy history because importing in the existing app
         // otherwise there will be version validation error
         // when saving the app
         assignRevisionHistory(app, hist);

         // make sure the application is not running
         app.setEnabled(false);
         
         if (ref instanceof PSReference)
         {
            ((PSReference) ref).setDescription(app.getDescription());
         }

         //now get the app files
         m_appDir = new File(directory, app.getRequestRoot());
         importAppFilesFromDir(app, m_appDir);

         app.setRequestRoot(requestRoot);
         return true;
      }
      return false;
   }

   /**
    * Assigns the provided revision history to the app.
    * @param app application to set revision history for.
    * Must not be <code>null</code>.
    * @param hist history to assign to the application.
    * Must not be <code>null</code>.
    */
   private void assignRevisionHistory(final OSApplication app,
         final PSRevisionHistory hist) throws PSUnknownNodeTypeException
   {
      app.clearRevisionHistory();
      app.setRevision(hist.getInitialRevision());
      final Document doc = PSXmlDocumentBuilder.createXmlDocument();
      final Element el = hist.toXml(doc);
      doc.appendChild(el);
      app.getRevisionHistory().fromXml(doc.getDocumentElement(), null, null);
   }

   /**
    * Loads XML from the provided file. Returns the loaded document.
    * @param file the existing valid readable file to parse.
    * Assumed not <code>null</code>.
    * @throws FileNotFoundException if the provided file does not exist.
    * @throws IOException if file reading fails.
    * @throws SAXException if file parsing fails.
    */
   private Document parseXml(final File file)
         throws FileNotFoundException, IOException, SAXException
   {
      FileInputStream input = new FileInputStream(file);
      try
      {
         return new PSXmlDocumentBuilder().createXmlDocument(input, false);
      }
      finally
      {
         input.close();
      }
   }

   /**
    * Convenience method to access application model.
    * @return Never <code>null</code>.
    */
   private IPSCmsModel getAppModel() throws PSModelException
   {
      return PSCoreFactory.getInstance().getModel(PSObjectTypes.XML_APPLICATION);
   }

   /**
    * Imports application files from the given directory.
    * Recursively calls itself to import files from subdirectories.
    * @param application application to import files to
    * @param directory directory to import from
    */
   private void importAppFilesFromDir(final OSApplication application, File directory)
   {
      File[] files = directory.listFiles();
      if ( null != files )
      {
         for (final File file : files)
         {
            if (file.isDirectory())
            {
               importAppFilesFromDir(application, file);
               continue;
            }
            try
            {
               FileInputStream instream = new FileInputStream(file);
               if (file.getName().equals(APP_EXT_EXPORT_FILE_NAME))
               {
                  application.loadUdfs(file);
               }
               else
               {
                  final File filename = new File(file.getName());
                  application.addAppFile(new PSApplicationFile(instream, filename));
               }
            }
            catch (Exception e)
            {
               PSDlgUtil.showError(e);
            }
         }
      }
   }

   /**
    * Exports the passed application to the choosen location an disk.
    *
    * @param app the application to export
    */
   //////////////////////////////////////////////////////////////////////////////
   public void exportApplication(OSApplication app)
   {
      try
      {
         m_chooser.setDialogTitle(
                   E2Designer.getResources().getString("ExportDlgTitle") +
                   " " + app.getName());

         int option = m_chooser.showSaveDialog(
                                            E2Designer.getApp().getMainFrame());
         if (option == JFileChooser.APPROVE_OPTION)
         {
            File selectedFile = m_chooser.getSelectedFile();
            if (selectedFile.exists())
            {
               if (JOptionPane.CANCEL_OPTION == PSDlgUtil.showConfirmDialog(
                     E2Designer.getResources().getString("OverwriteExitstingFile"),
                     E2Designer.getResources().getString("ConfirmOperation"),
                     JOptionPane.OK_CANCEL_OPTION,
                     JOptionPane.QUESTION_MESSAGE) )
               {
                  // the user canceled the export
                  return;
               }
            }
            else if (!selectedFile.getName().endsWith(".xml"))
            {
               selectedFile = new File(selectedFile.getPath()  + ".xml");
            }

            // save for others to use
            m_selectedFile = selectedFile;
            m_selectedDir = m_chooser.getCurrentDirectory();

            FileOutputStream output = new FileOutputStream(selectedFile);
            PSXmlDocumentBuilder xmlFactory = new PSXmlDocumentBuilder();
            xmlFactory.write(app.toXml(), output);
              output.close();

            // create export subdirectory
            final String strDir = m_chooser.getCurrentDirectory().getPath()
                  + File.separator + app.getRequestRoot();
            // export app udf extensions first; write to xml file only if there
            // are app udf extensions
            File fAppExport = new File(new File(strDir), APP_EXT_EXPORT_FILE_NAME);
            app.saveUdfs( fAppExport );

            //now save application files locally
            for (final String file : Util.getApplicationFiles(app.getRequestRoot()))
            {
               //create file on disk
               final File appfile = new File(strDir + File.separator + file);
               appfile.getParentFile().mkdirs();
               appfile.createNewFile();

              final IPSMimeContent content = loadAppFileFromServer(app, file);
              final InputStream in = content.getContent();

              //write byte array of application file to file on disk
              final FileOutputStream out = new FileOutputStream(appfile);
              try
              {
                 IOTools.copyStream(in, out);
              }
              finally
              {
                 try { out.close(); } catch (IOException e) { /* ignore */ }
                 try { in.close(); } catch (IOException e) { /* ignore */ }
              }
            }
         }
      }
      catch (Exception e)
      {
         PSDlgUtil.showError(e);
      }
   }

   /**
    * Loads application file from server and returns its content.
    */
   private IPSMimeContent loadAppFileFromServer(OSApplication app, final String file)
           throws PSServerException, PSAuthorizationException,
           PSAuthenticationFailedException, PSNotLockedException,
            PSIllegalStateException, PSSystemValidationException {
      final PSApplicationFile psfile = new PSApplicationFile(new File(file));
      return getObjectStore().loadApplicationFile(app, psfile).getContent();
   }

   /**
    * Convenience method to access object store.
    * @return Never <code>null</code>.
    */
   private PSObjectStore getObjectStore()
   {
      return E2Designer.getApp().getMainFrame().getObjectStore();
   }


   /**
    * @return The file selected by the user via the File chooser or passed in. Not useful
    * until {@link #importApplication(IPSReference) importApplication }
    * or {@link #exportApplication(OSApplication) exportApplication} has been
    * called. If called before one of these methods is called, null is returned.
    */
   public File getAppFile()
   {
      return m_selectedFile;
   }


   /**
    * @return The directory chosen by the user via the File chooser.
    * Not useful until {@link #importApplication(IPSReference) importApplication}
    * or {@link #exportApplication(OSApplication) exportApplication} has been
    * called. If called before one of these methods is called, null is returned.
    */
   public File getAppFileDir()
   {
      return m_selectedDir;
   }
   
   /**
    * @return The directory storing the application files.
    * Not useful until {@link #importApplication(IPSReference) importApplication}
    * or {@link #exportApplication(OSApplication) exportApplication} has been
    * called. If called before one of these methods is called, null is returned.
    */
   public File getAppDir()
   {
      return m_appDir;
   }

   //////////////////////////////////////////////////////////////////////////////
   /**
    * the file chooser
    */
   private JFileChooser m_chooser;

   /**
    * The name of the application file.
    */
   private File m_selectedFile;

   /**
    * The directory where the application is read/written.
    */
   private File m_selectedDir;
   
   /**
    * The directory to read the application files from. 
    */
   private File m_appDir;

   /** The name of the application extensions export file. */
   public static final String APP_EXT_EXPORT_FILE_NAME = "AppExtensions.xml";
}
