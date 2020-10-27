/*[ OSApplicationFile.java ]***************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSApplicationFile;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.util.PSPurgableTempFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Properties;

/*
 * This class stores provides the functionality to store / load the GUI
 * information to the application.
 */
////////////////////////////////////////////////////////////////////////////////
public class OSApplicationFile implements Serializable,
                                     IGuiLink,
                                     IPersist,
                                                       ICustomDropSourceData,
                                                       IAuxFilesTransfer
{
   OSApplicationFile()
   {
      
   }
   
      /**
    * Get unique object identifier.
    */
   //////////////////////////////////////////////////////////////////////////////
   public int getId()
   {
      return m_id;
   }

   /**
    * Set unique object identifier.
    */
   //////////////////////////////////////////////////////////////////////////////
   public void setId(int id)
   {
      m_id = id;
   }


   //////////////////////////////////////////////////////////////////////////////
   // implementation for IGuiLink
   public void setFigure(UIFigure fig)   { m_owner = fig; }

   // see IGuiLink
   public void release() 
   { 
      m_owner = null; 
      releasePurgableTempFile();
   }
   
   public UIFigure getFigure() { return m_owner; }

   //////////////////////////////////////////////////////////////////////////////
  // IPersist interface implementation
   public boolean load(PSApplication app, Object store, Properties config)
   {
      if (null == store || null == config)
         throw new IllegalArgumentException();

      if (!(store instanceof OSApplicationFile))
      {
         Object[] astrParams =
         {
            "OSApplicationFile"
         };
         throw new IllegalArgumentException(MessageFormat.format(
            E2Designer.getResources().getString("IncorrectType"), astrParams));
      }

    try
    {
      // restore GUI information
      int currentId = this.getId();
       setInternalName(config.getProperty(KEY_APPLICATION_FILE_NAME + currentId));
         m_filePath = config.getProperty(KEY_APPLICATION_FILE_FILE + currentId);
         if (m_owner != null)
            m_owner.invalidateLabel();

      OSLoadSaveHelper.loadOwner(currentId, config, m_owner);
      return true;
    }
    catch (Exception e)
    {
       e.printStackTrace();
    }

      return false;
   }

   //////////////////////////////////////////////////////////////////////////////
  // IPersist interface implementation
   public void save(PSApplication app, Object store, Properties config)
   {
      if (null == store || null == config)
         throw new IllegalArgumentException();

      if (!(store instanceof PSApplication))
      {
         Object[] astrParams =
         {
            "PSApplication"
         };
         throw new IllegalArgumentException(MessageFormat.format(
            E2Designer.getResources().getString("IncorrectType"), astrParams));
      }

      try
    {
        // store current id temporary and create a new unique id for this object
         int currentId = this.getId();
        this.setId(Util.getUniqueId());

      if (app instanceof OSApplication)
         saveToServer((OSApplication) app);

          // then store all keys with the new ID created.
        String strId = new Integer(this.getId()).toString();
        config.setProperty(KEY_APPLICATION_FILE_KEY + strId, strId);
      config.setProperty(KEY_APPLICATION_FILE_NAME + strId, m_strInternalName);
      if (m_filePath != null)
          config.setProperty(KEY_APPLICATION_FILE_FILE + strId, m_filePath);

        // save GUI information
      OSLoadSaveHelper.saveOwner(currentId, this.getId(), config, m_owner);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /*
    * Create a document using the file path. If successful, the created
    * output stream is stored in m_outputStream, so it can be saved in
    * saveToServer.
    *
    * @return boolean true if successful, false otherwise
    */
/*
   private boolean createDocument()
   {
      String filePath = getFilePath();
      m_appFile = new File(filePath);

      return true;
   }
*/
   //////////////////////////////////////////////////////////////////////////////
   // ICustomDropSourceData interface implementation
   public boolean prepareSourceForDrop(DropAction action, String filePath, String rootName)
   {
      // just do this once
      if (m_bSourceIsPrepared)
         return true;

      // indicate this has been prepared
      m_bSourceIsPrepared = true;

       setFilePath(filePath);
       return true; // createDocument();
   }

   /**
    * Save associated local file to the server. If no local file exists save 
    * to files to be preserved.
    * @param app application to save, must not be <code>null</code>.
    * @return <code>false</code> if the local file associated is not 
    * <code>null</code> and save fails for any reason, 
    * <code>true</code> otherwise. 
    */
   private boolean saveToServer(OSApplication app)
   {
      if (app == null)
         throw new IllegalArgumentException("app must not be null");
         
      // this is not from the local file system
      if ( m_tempFilePath == null )
      {
         if(getInternalName() == null)
            return true;

         File file = new File(getInternalName());
         app.saveAppFile(file);
         return true;
      }

      try
      {
         File srcFile = new File( m_tempFilePath );
         FileInputStream instream = new FileInputStream(srcFile);
         File file = new File(getInternalName());
         PSApplicationFile appFile = new PSApplicationFile(instream, file);

         PSObjectStore os = E2Designer.getApp().getMainFrame().getObjectStore();
         os.saveApplicationFile(app, appFile, true, true);
         OSLoadSaveHelper.logSaveApplicationFile(appFile.getFileName());


         if ( !m_tempFilePath.equals(m_filePath))
            srcFile.delete();
         m_tempFilePath = null;
      }
      catch (FileNotFoundException e)
      {
         final Object[] astrParams =
         {
            getInternalName(),
            e.toString()
         };
         PSDlgUtil.showErrorDialog(
               MessageFormat.format(E2Designer.getResources().getString("FileNotFound"), astrParams),
               E2Designer.getResources().getString("OpErrorTitle"));
         return false;
      }
      catch(Exception e)
      {
         e.printStackTrace ();
         return false;
      }

      return true;
   }

   /**
    * Implement the method from the interface. Remove the associated file 
    * obtained by {@link OSApplicationFile#getInternalName()} if it returns 
    * non-<code>null</code> and non-empty internal name.
    * @see IPersist#removeFromServer(OSApplication)
    */
   public void cleanup(OSApplication psApp)
   {
      String internalName = getInternalName();
      if (internalName == null || internalName.length() < 1)
         return;
      psApp.removeAppFile(new File(internalName));
   }

   //////////////////////////////////////////////////////////////////////////////
   // ICustomDropSourceData
   public String getFilePath()
   {
      return m_filePath;
   }

   //////////////////////////////////////////////////////////////////////////////
   // ICustomDropSourceData
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

   public void setInternalName(String strName)
   {
      m_strInternalName = strName;
      if ( null != m_owner )
         m_owner.invalidateLabel();
   }

   public String getInternalName()
   {
      return(m_strInternalName);
   }

   //////////////////////////////////////////////////////////////////////////////
   // IAuxFilesTransfer
   public String getServerFileName()
   {
      return Util.stripPath(m_filePath);
   }

   //////////////////////////////////////////////////////////////////////////////
   // IAuxFilesTransfer
   public void setTempFilePath( String path )
   {
      m_tempFilePath = path;
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
      return (m_filePath != null);
   }


   /**
    * Clean up the temporary file if there is one.
   **/
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
    * Overide the serialization method to create and write the AuxFilesTransferHelper
    * object, if it is necessary. It is necessary if there is data in a file to
    * transfer.
   **/
   private  void writeObject(java.io.ObjectOutputStream stream)
       throws IOException
   {
      stream.defaultWriteObject();
      if( hasFiles())
      {
            AuxFilesTransferHelper auxFilesHelper =
                  new AuxFilesTransferHelper((UIConnectableFigure)m_owner);
            stream.writeObject(auxFilesHelper);
      }

   }

   /**
    * Overide the serialization method to read the AuxFilesTransferHelper
    * object.
    */
   private  void readObject(java.io.ObjectInputStream stream)
       throws IOException, ClassNotFoundException
   {
      stream.defaultReadObject();
      if ( hasFiles())
      {
         AuxFilesTransferHelper auxFilesHelper = 
            (AuxFilesTransferHelper)stream.readObject();
         releasePurgableTempFile();
         m_tempFile = auxFilesHelper.getFile(this);
         setTempFilePath(m_tempFile.getPath());
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

   //////////////////////////////////////////////////////////////////////////////
   // private storage
   private String m_filePath = null;
   // no need to serialize, it will be replaced if deserialized
   transient private String m_tempFilePath = null;
  private String m_strInternalName = new String("");

   private UIFigure m_owner = null;
  private int m_id = 0;

  /**
   * The reference to the temp file object. This is to prevent G.C.'ed while it
   * is in use. It is <code>null</code> if has not set. 
   * No need to be serialized, used for runtime only.
   */
  transient private PSPurgableTempFile m_tempFile = null;
     
  
   /*
    * status flag which indicates wether or not the source preparation from the
   * local file system has been done
   */
   private boolean m_bSourceIsPrepared = false;
   public static final String KEY_APPLICATION_FILE_KEY = new String("applicationFileKey");
   public static final String KEY_APPLICATION_FILE_FILE = new String("applicationFileFile");
  public static final String KEY_APPLICATION_FILE_NAME = new String("applicationFileName");
}
