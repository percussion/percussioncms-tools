/******************************************************************************
 *
 * [ OSResultPage.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import com.percussion.conn.PSServerException;
import com.percussion.content.HTMLException;
import com.percussion.content.IPSMimeContent;
import com.percussion.content.IPSMimeContentTypes;
import com.percussion.content.PSContentFactory;
import com.percussion.content.PSMimeContentAdapter;
import com.percussion.error.PSCatalogException;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSHtmlParameter;
import com.percussion.error.PSNotLockedException;
import com.percussion.design.objectstore.PSResultPage;
import com.percussion.design.objectstore.PSResultPageSet;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.util.IOTools;
import com.percussion.util.PSCharSets;
import com.percussion.util.PSCollection;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.xml.PSDtdTree;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Properties;

/**
 * This class is result page data used in the app view window. Most objects
 * can use the PSObjectStore object directly, but in the case of the PSResultPage,
 * it also contains information about the stylesheet selector, which is a
 * different visual object.
 */
public class OSResultPage extends PSResultPage
implements IGuiLink, IPersist,   ICustomDropSourceData,  IAuxFilesTransfer,
IDataCataloger
{
   /**
    */
   public OSResultPage()
   {
      super(null);
   }

   /**
    * Copy constructors
    */
   public OSResultPage(OSResultPage page)
   {
      super(page.getStyleSheet());
      copyFrom(page);
   }

   public OSResultPage(PSResultPage page)
   {
      super(page.getStyleSheet());
      copyFrom(page);
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first. <p>
    * The owner is not copied by this method. If the caller wants to change
    * owners, they should call <code>setFigure( src.getFigure())</code> after
    * this method completes.
    *
    * @param   page a valid OSResultPage.
    *
    */
   public void copyFrom(OSResultPage page)
   {
      copyFrom((PSResultPage) page);
      m_content = page.m_content; // TODO: is this OK?
      setFilePath( page.m_filePath );
      m_bSourceIsPrepared = page.m_bSourceIsPrepared;
   }

   /**
    * Overridden to modify the figure label in case the data changed.
    **/
   @Override
   public void copyFrom(PSResultPage page)
   {
      super.copyFrom( page );
      if ( null != m_owner )
      m_owner.invalidateLabel();
   }

   /**
    * Indicates if the specified file is can be split.
    *
    * @return <code>true</code> if splitable, else <code>false</code>
    */
   public boolean isSplitable()
   {
      return m_filePath == null ? false : Util.isSplitable(m_filePath);
   }

   /**
    * @return  The name of the stylesheet associated with this result page (not
    * including any protocol that is part of the name). If no stylesheet is
    * currently set, a string indicating the default stylesheet is returned.
    *
    * @see  #getDefaultStylesheetName
    **/
   public String getStylesheetForDisplay()
   {
      String name = null;
      URL ss = getStyleSheet();
      if ( null != ss )
      name = ss.getFile();
      if ( null == name || 0 == name.trim().length())
      name =  getDefaultStylesheetName();
      return name;
   }


   /**
    * @return  A string to display to the user that indicates the default ss
    * will be used.
    **/
   public static String getDefaultStylesheetName()
   {
      return new String( E2Designer.getResources().getString( "default" ));
   }

   /**
    * Replace resets the flag which indicates that this has been created from
    * the local file system already.
    */
   //////////////////////////////////////////////////////////////////////////////
   public void startReplace()
   {
      m_bSourceIsPrepared = false;
   }

   /*
    * Read an XSL document from the provided file path. If successful the
    * created output stream is saved.
    *
    * @param   filePath the file path from where to read an XSL document
    * @return  boolean true if successful, false otherwise
    */
   private boolean readXslDocument(String filePath)
   {
      try
      {
         /*
         this has been replaced with the IPSMimeContent interface
         FileInputStream fileInputStream = new FileInputStream(dropedFile);

         ByteArrayOutputStream out = new ByteArrayOutputStream();
         int read;
         byte[] buf = new byte[1024];
         while ((read = fileInputStream.read(buf)) >= 0)
         {
         out.write(buf, 0, read);
         if (read < buf.length)
         break;
         }
         out.flush();
          */
         // create the XSL document from the local file system
         File droppedFile = new File(filePath);
         m_content = PSContentFactory.loadXslFile(droppedFile);
         String strFileName = Util.stripPath(filePath, ".xsl");
         setStyleSheet(new java.net.URL("file:" + strFileName));
         return true;
      }
      catch (FileNotFoundException e)
      {
         final Object[] astrParams =
         {
            filePath,
            e.toString()
         };

         /* invoke this later in case we are in the middle of a drop, which would
         cause a hang */
         SwingUtilities.invokeLater( new Runnable()
         {
            public void run()
            {
               PSDlgUtil.showErrorDialog(
                     MessageFormat.format(E2Designer.getResources().getString("FileNotFound"), astrParams),
                     E2Designer.getResources().getString("OpErrorTitle"));
            }
         });
         return false;
      }
      catch (IOException e)
      {
         // this should never happen
         e.printStackTrace();
         return false;
      }
   }

   /**
    * Sets the stylesheet location for this page. The filename part of the passed
    * in URL is possibly modified so it is XML compliant (and safer for URLs).
    **/
   @Override
   public void setStyleSheet(URL ss)
   {
      if ( null == ss )
      {
         super.setStyleSheet( ss );
         return;
      }

      try
      {
         // fix up the name so it is XML/URL safe
         String filename = Util.makeXmlName(ss.getFile());
         super.setStyleSheet( new URL( ss.getProtocol(), ss.getHost(), ss.getPort(),
         filename ));
         if ( null != m_owner )
         m_owner.invalidateLabel();
      }
      catch ( MalformedURLException e )
      {
         // since we are taking all the parts from an existing URL, this should never happen
         System.out.println( "Unexpected exception when creating URL: " + e.getLocalizedMessage());
      }
   }

   //////////////////////////////////////////////////////////////////////////////
   // IGuiLink interface implementation
   public void setFigure(UIFigure fig) {  m_owner = fig; }


   // see IGuiLink
   public void release()
   {
      m_owner = null;
      releasePurgableTempFile();
   }

   public UIFigure getFigure()   {  return m_owner;   }

   //////////////////////////////////////////////////////////////////////////////
   // implementation for IPersist
   public boolean load(PSApplication psApp, Object store, Properties config)
   {
      if (null == store || null == config)
      throw new IllegalArgumentException();

      if (!(store instanceof PSResultPage))
      {
         Object[] astrParams =
         {
            "PSResultPage"
         };
         throw new IllegalArgumentException(MessageFormat.format(
         E2Designer.getResources().getString("IncorrectType"), astrParams));
      }

      OSApplication app = null;
      if ( psApp instanceof OSApplication )
      app = (OSApplication) psApp;
      else
      {
         throw new IllegalArgumentException( "Expected OS Application" );
      }

      try
      {
         PSResultPage resultPage = (PSResultPage) store;
         copyFrom(resultPage);

         // restore GUI information
         int currentId = this.getId();
         m_filePath = config.getProperty(KEY_RESULT_PAGE_FILE_PATH + currentId);
         m_dtdName = config.getProperty( KEY_RESULT_PAGE_DTD_NAME + currentId );
         if (m_owner != null)
         m_owner.invalidateLabel();

         // restore dtdTree from dtd file name
         if ( null != m_dtdName )
         {
            String baseDtdName = Util.stripPath( m_dtdName, "" );
            try
            {
               m_dtdTree = new PSDtdTree( app.loadAppFile( new File( m_dtdName)),
               baseDtdName, PSCharSets.rxJavaEnc());
            }
            catch (PSCatalogException e)
            {
               PSDlgUtil.showErrorDialog(e.getLocalizedMessage(),
               E2Designer.getResources().getString("BadDTDTitle"));
               e.printStackTrace();
            }
            catch (PSAuthenticationFailedException e)
            {
               PSDlgUtil.showErrorDialog(e.getLocalizedMessage(),
               E2Designer.getResources().getString("ExceptionTitle"));
            }
            catch(PSValidationException e )
            {
               e.printStackTrace();
            }
            catch(PSNotLockedException e)
            {
               e.printStackTrace();
            }
            catch (PSServerException e)
            {
               // this should never happen
               e.printStackTrace();
            }
            catch (PSAuthorizationException e)
            {
               e.printStackTrace();
            }
         }

         OSLoadSaveHelper.loadOwner(this.getId(), config, m_owner);
         return true;
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      return false;
   }

   //////////////////////////////////////////////////////////////////////////////
   // implementation for IPersist
   public void save(PSApplication app, Object store, Properties config )
   {
      if (null == store || null == config)
      throw new IllegalArgumentException();

      if (!(store instanceof OSDataset))
      {
         System.out.println("...got class: " + store.getClass().toString());
         throw new IllegalArgumentException(MessageFormat.format(
         E2Designer.getResources().getString("IncorrectType"), new Object[] {"OSDataset"}));
      }

      try
      {
         // store current id temporary and create a new unique id for this object
         int currentId = this.getId();
         this.setId(Util.getUniqueId());

         // get result page set and create a new one if not existing
         OSDataset dataset = (OSDataset) store;
         PSResultPageSet resultPageSet = dataset.getOutputResultPages();
         if (resultPageSet == null)
         {
            resultPageSet = new PSResultPageSet();
            dataset.setOutputResultPages(resultPageSet);
         }

         // get collection of result pages, create one if not there yet
         PSCollection resultPages = resultPageSet.getResultPages();
         if (resultPages == null)
         {
            resultPages = new PSCollection("com.percussion.design.objectstore.PSResultPage");
            resultPageSet.setResultPages(resultPages);
         }

         // add this result page to the dataset
         resultPages.remove(this);
         resultPages.add(this);

         if (app instanceof OSApplication)
            saveToServer((OSApplication) app);

         // then store all keys with the new ID created.
         String strId = new Integer(this.getId()).toString();
         if (m_filePath != null)
            config.setProperty(KEY_RESULT_PAGE_FILE_PATH + strId, m_filePath);
         if ( null != m_dtdName )
            config.setProperty( this.KEY_RESULT_PAGE_DTD_NAME + strId, m_dtdName );

         // save GUI information
         OSLoadSaveHelper.saveOwner(currentId, this.getId(), config, m_owner);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   //////////////////////////////////////////////////////////////////////////////
   /**
    * ICustomDropSourceData interface implementation. For drop preparation of
    * query dataset result pages, a DTD tree object will be constructed from the
    * splitter-generated DTD (stored as a PSDTDTree object locally and as an
    * .rpg file on the server). An update dataset result page will not go through
    * those steps and only a xsl reference and figure will be created (no DTD).
    *
    * @see PSDtdTree
    * @todo Tidy is ran twice. Inefficient. See source code for explanations.
    */
   public boolean prepareSourceForDrop(DropAction action, String filePath, String rootName)
   {
      //System.out.println("OSResultPage.prepareSourceForDrop(" + action + "," + filePath + "," + rootName + ")");
      // just do this once
      if (m_bSourceIsPrepared)
      {
         return true;
      }

      if (filePath == null)
      {
         return false;
      }

      String fileName = Util.stripPath(filePath, "");
      validateFileNameCharacters(fileName);

       // we always want the baseName of the filePath passed in as the root for
      // the dtd tree; and make sure the name is ok
      fileName = Util.makeXmlName(fileName);

      rootName = rootName == null ? fileName : Util.makeXmlName(rootName);

      // indicate this has been prepared
      m_bSourceIsPrepared = true;

      // set m_filePath using setter, instead of directly, because side effect
      // is important (fix for bug #Rx-01-08-0018)
      setFilePath(filePath);

        if (action == ICustomDropSourceData.DropAction.XSL)
        {
            return readXslDocument(filePath);
        }


      m_bSourceIsPrepared = false;
      return false;
   }

   private void validateFileNameCharacters(String fileName)
   {
      boolean test = false;
      //Check the first character in a file name
      if(!(Character.isLetter(fileName.charAt(0))) && (
         fileName.charAt(0) != '_'))
         {
            PSDlgUtil.showWarningDialog(
               E2Designer.getResources().getString("changeFileName"),
               E2Designer.getResources().getString("errorFileName"));
            test = true;
         }

      //check all characters(other than the first)

      for(int i = 1; (i < fileName.length() && test == false); i++)
      {
         if(!(Character.isJavaIdentifierPart(fileName.charAt(i))) && (
            (fileName.charAt(i) != '-') && (fileName.charAt(i) != '.')) || (
            fileName.charAt(i) == '$'))
         {
            PSDlgUtil.showWarningDialog(
            E2Designer.getResources().getString("changeFileName"),
            E2Designer.getResources().getString("errorFileName"));
            test=true;
         }
      }
   }

   private boolean usedLocalFileSystem()
   {
      return null != m_tempFilePath || null != m_content;
   }

   /**
    * Saves the original source file that was dragged and dropped onto the
    * workbench. The source file will be saved in <rxroot>/<appRoot>/<src> dir.
    * If a source file is a part of the object created when  the
    * source file was dropped onto the workbench (Eg: Objects created by UPDATE
    * and STATIC actions) then the source file will not be saved here,
    * since it is already saved in <rxroot>/<appRoot>.
    *
    * This method is a cleaned-up copy from OSPageDatatank.
    *
    * @param app an application which this file belongs to, it is never
    * <code>null</code>
    * @return <CODE>true</CODE> if the file was saved successfully, if an
    * exception is caught during this process <CODE>false</CODE> is returned.
    */
   private boolean saveSourceFile(OSApplication app)
   {
      boolean isSourceSaved = true;
      /* If this source file is not an html file do not save it, as it is
         already saved as an application file */
      if (isSplitable())
      {
         try
         {
            /* If this file has been saved as a part of the object,
               do not save it in the application's source dir */
            if (m_tempFilePath == null)
            {
               return true;
            }
            
            final File tempFile = new File(this.m_tempFilePath);
            final InputStream source = PSContentFactory.loadHtmlFile(tempFile).getContent();

            String fileName = Util.stripPath(getFilePath());
            String newFilePath = SRC_DIR + File.separatorChar + fileName;
            File droppedFile = new File(newFilePath);
            app.saveAppFile(droppedFile, source);

            isSourceSaved = true;
         }
         catch (IOException e)
         {
            e.printStackTrace();
            isSourceSaved = false;
         }
         catch (HTMLException e)
         {
            e.printStackTrace();
            isSourceSaved = false;
         }
      }
      return isSourceSaved;
   }

   /**
    * Save the local stylesheet file/DTD file associated to the server. 
    * Marks for preserving if no local file(s) is/are associated with the 
    * object. 
    * @param app the application object, must not be <code>null</code>.
    * @return <code>false</code> if the local file is associated with this 
    * object and save fails for any reason, <code>true</code> otherwise.
    */
   private  boolean saveToServer(OSApplication app)
   {
      // this is not from the local file system
      //Markup the files to preserve
      if (!usedLocalFileSystem())
      {
         if (getStyleSheet() != null && getStyleSheet().getFile() != null)
         {
            File filename = new File(getStyleSheet().getFile());
            app.saveAppFile(filename);
         }

         // there may be a case that the xsl file is created directly from an
         // xsl file in the browser. In this case, no dtd (or rpg) file will be
         // associated with this result page. Thus no saving is required of this
         // dtd (no dtd to save anyways).
         if ( null != m_dtdName )
         {
            // saving dtd file to server
            File filename = new File(m_dtdName);
            app.saveAppFile( filename );
         }
         return true;
      }

      try
      {
         //         System.out.println( "Saving to Server!!!" );
         File tempFile = null;
         {
            String tempFilePath = getTempFilePath();
            if (tempFilePath != null)
            {
               tempFile = new File(tempFilePath);
            }
         }

         if (m_content == null)
         {
            InputStream is = new BufferedInputStream(
            new FileInputStream(tempFile));

            m_content = new PSMimeContentAdapter(
               is, IPSMimeContentTypes.MIME_TYPE_APPLICATION_XSL, null,
               PSCharSets.rxJavaEnc(), tempFile.length());
         }

         if(getStyleSheet() != null)
         {
            File filename = new File(getStyleSheet().getFile());
            app.saveAppFile( filename, m_content.getContent());
         }

         // there may be a case that the xsl file is created directly from an
         // xsl file in the browser. In this case, no dtd (or rpg) file will be
         // associated with this result page. Thus no saving is required of this
         // dtd (no dtd to save anyways).
         if ( null != m_dtdName )
         {
            // saving dtd file to server
            File filename = new File(m_dtdName);
            String baseName = Util.stripPath( filename.getName(), "" );

            boolean wrapInXml =
               !m_dtdTree.getRoot().getElement().getName().equals( baseName );

            app.saveAppFile( filename,
               new ByteArrayInputStream(
               m_dtdTree.toDTD( wrapInXml ).getBytes()));
         }

         // make sure that we save files dropped on result pages
         // fix for bug #Rx-01-08-0018
         saveSourceFile(app);  // don't check return value b/c we won't fail
                               // the whole operation if saving the source fails

         // clear local copies now that it resides on the server
         if ( null != tempFile && !tempFile.getPath().equals(m_filePath))
            tempFile.delete();
         clearContent();
         // OSLoadSaveHelper.logSaveApplicationFile(appFile.getFileName());

      }
      catch (IOException e)
      {
         e.printStackTrace();
         return false;
      }
      catch (IllegalStateException e)
      {
         // do we need to catch this, or can it propagate up?
         e.printStackTrace();
         return false;
      }

      return true;
   }

   /**
    * Clears up page content after it is not needed anymore.
    * @return existing content.
    */
   public IPSMimeContent clearContent()
   {
      final IPSMimeContent content = m_content;
      m_content = null;
      setTempFilePath(null);
      return content;
   }
   
   /**
    * Indicates whether the page has content loaded.
    */
   public boolean isContentLoaded()
   {
      return m_content != null;
   }

   /**
    * Implementation of the interface method. Mark the stylesheet and DTD files
    * for deletion.
    * 
    * @see IPersist
    */
   public void cleanup(OSApplication psApp)
   {
      if(getStyleSheet() != null)
      {
         File ssFile = new File(getStyleSheet().getFile());
         psApp.removeAppFile(ssFile);
      }
      
      if (m_dtdName != null)
      {
         File dtdFile = new File(m_dtdName);
         psApp.removeAppFile(dtdFile);
      }      
   }

   //////////////////////////////////////////////////////////////////////////////
   // ICustomDropSourceData
   public String getFilePath()
   {
      return m_filePath;
   }

   /**
    * Sets the file path to our source file.  As a side effect, also sets the
    * temporary file path.
    *
    * @param filePath the complete file path
    */
   public void setFilePath(String filePath)
   {
      m_filePath = filePath;
      setTempFilePath( m_filePath );
   }

   //////////////////////////////////////////////////////////////////////////////
   // ICustomDropSourceData
   public void setUsedLocalFileSystem()
   {
      // no-op, no longer used
   }


   //////////////////////////////////////////////////////////////////////////////
   // IAuxFilesTransfer
   public String getServerFileName()
   {
      URL ss = getStyleSheet();
      return null == ss ? null : ss.getFile();
   }

   //////////////////////////////////////////////////////////////////////////////
   // IAuxFilesTransfer
   public void setTempFilePath( String path )
   {
      m_tempFilePath = path;
      //    System.out.println(m_tempFilePath);
   }

   //////////////////////////////////////////////////////////////////////////////
   // IAuxFilesTransfer
   public String getTempFilePath()
   {
      return m_tempFilePath;
   }

   //////////////////////////////////////////////////////////////////////////////
   // IAuxFilesTransfer
   public boolean hasFiles()
   {
      return ( null == m_content && null != m_filePath );
   }


   /**
    * Clean up the temporary file if there is one.
    **/
   @Override
   protected void finalize() throws Throwable
   {
      if ( null != m_tempFilePath && !m_tempFilePath.equals(m_filePath))
      {
         File f = new File( m_tempFilePath );
         f.delete();
         m_tempFilePath = null;
      }
      super.finalize();
   }

   //////////////////////////////////////////////////////////////////////////////
   // Serializable interface optional implementation
   /**
    * Overide the serialization method to create and write the
    * AuxFilesTransferHelper object, if it is necessary. It is necessary if
    * there is data in a file to transfer.
    */
   private void writeObject(ObjectOutputStream stream) throws IOException
   {
      stream.defaultWriteObject();
      if (null != m_content)
      {
         ByteArrayOutputStream oStream = null;
         try
         {
            oStream = new ByteArrayOutputStream();
            InputStream inStream = m_content.getContent();
            IOTools.copyStream(inStream, oStream);

            if (oStream.size() > 0)
            {
               inStream.close();

               stream.writeBoolean(true);
               byte[] streamData = oStream.toByteArray();
               stream.writeObject(streamData);
               stream.writeObject(m_content.getMimeType());

               /*
                * Recreate original stream and original IPSMimeContentType object
                * for additional serialization of this object.
                */
               ByteArrayInputStream byteStream =
                  new ByteArrayInputStream(streamData);
               m_content = new PSMimeContentAdapter(byteStream,
                  m_content.getMimeType(), null, PSCharSets.rxJavaEnc(),
                  streamData.length);
            }
            else
               stream.writeBoolean(false);
         }
         finally
         {
            if (oStream != null)
               oStream.close();
         }
      }
      else
         stream.writeBoolean(false);

      if (hasFiles())
      {
         AuxFilesTransferHelper auxFilesHelper =
            new AuxFilesTransferHelper((UIConnectableFigure) m_owner);
         stream.writeObject(auxFilesHelper);
      }
   }

   /**
    * Overide the serialization method to read the AuxFilesTransferHelper
    * object.
    **/
   private  void readObject(java.io.ObjectInputStream stream)
   throws IOException, ClassNotFoundException
   {
      stream.defaultReadObject();

      boolean bDataExists = stream.readBoolean();

      if ( bDataExists )
      {
         byte[] streamData = (byte[])stream.readObject();

         if ( 0 < streamData.length )
         {
            String mimeType = (String)stream.readObject();
            InputStream dataStream = new ByteArrayInputStream( streamData );
            m_content = new PSMimeContentAdapter( dataStream, mimeType, null,
            PSCharSets.rxJavaEnc(),
            streamData.length );
         }
      }

      if ( hasFiles())
      {
         AuxFilesTransferHelper auxFilesHelper = (AuxFilesTransferHelper)stream.readObject();
         if(  getServerFileName() != null )
         {
            releasePurgableTempFile();
            m_tempFile = auxFilesHelper.getFile(this);
            setTempFilePath(m_tempFile.getPath());

            FileInputStream fileStream = new FileInputStream( m_tempFile );
            m_content = new PSMimeContentAdapter( fileStream,
            IPSMimeContentTypes.MIME_TYPE_APPLICATION_XSL,
            null,
            PSCharSets.rxJavaEnc(),
            m_tempFile.length() );
         }
      }
      stream.close();
   }

   /**
    * implementation for ICatalogDataHelper
    * @see IDataCataloger
    */
   public void catalogData(ObjectType iObjType, CatalogReceiver container)
   {
      if (iObjType == ObjectType.HTML_PARAM)
      {
         PSCollection conditionals = getConditionals();
         if (null == conditionals) // no where conditionals exist, return;
         return;

         // go thru all conditionals and append all variables/values that are
         // HtmlParameters to the paramList; sort later
         for (int i = 0; i < conditionals.size(); i++)
         {
            PSConditional aConditional = (PSConditional)conditionals.get(i);
            IPSReplacementValue param = aConditional.getVariable();
            if (param instanceof PSHtmlParameter)
            container.add( ((PSHtmlParameter)param).getName() );

            param = aConditional.getValue();
            if (param instanceof PSHtmlParameter)
            container.add( ((PSHtmlParameter)param).getName() );
         }
      }
      else if (iObjType == ObjectType.XML_DTD)
      {
         if ( null != m_dtdTree )
         container.add( m_dtdTree );
         //      else
         //        System.out.println( this.getStylesheetForDisplay()+": m_dtdTree is null" );
      }
   }

   /**
    * Release the purgable temp file if it is not <code>null</code>.
    */
   private void releasePurgableTempFile()
   {
      if (m_tempFile != null)
      {
         m_tempFile.release();
         m_tempFile = null;
      }
   }
   
   public String getLastSplitXSLContent()
   {
      return m_lastSplitXSLContent;
   }
   
   //////////////////////////////////////////////////////////////////////////////
   // private storage
   private UIFigure m_owner = null;

   /*
    * status flag which indicates wether or not the source preparation from the
    * local file system has been done
    */
   private boolean m_bSourceIsPrepared = false;
   private transient IPSMimeContent m_content;

   /**
    * The file path name for this styleSheet. The file path will be in
    * &quot;file:&lt;name&gt;.xsl&quote; format.
    */
   private String m_filePath;

   /**
    * The name of the Dtd file associated with this result page. It will be
    * be <CODE>null</CODE> if there is no Dtd file.
    */
   private String m_dtdName = null;

   /** The DTD associated with this xsl result page in PSDtdTree form. */
   private PSDtdTree m_dtdTree = null;

   /** no need to serialize, it will be replaced if deserialized. */
   transient private String m_tempFilePath = null;

  /**
   * The reference to the temp file object. This is to prevent G.C.'ed while it
   * is in use. It is <code>null</code> if has not set.
   * No need to be serialized, used for runtime only.
   */
  transient private PSPurgableTempFile m_tempFile = null;
  
  /**
   * XSL file content from the last split (workaround to make it work).
   */
  transient private String m_lastSplitXSLContent;

   /** the user property key for the location of the html file that created this
    * result page.
    */
   private static final String KEY_RESULT_PAGE_FILE_PATH = new String("resultPageFilePath");

   /**
    * A unique (within the app objects) extension for DTDs that are saved for
    * later use by this object, but not the server. The ext must be unique from
    * ext used by other objects saving DTDs.
    **/
   private static final String LOCAL_DTD_EXT = ".rpg";


   /** the user property key for the location of the dtd file that was created
    * with this result page.
    */
   private static final String KEY_RESULT_PAGE_DTD_NAME = new String("resultPageDtd");
}
