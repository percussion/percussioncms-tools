/******************************************************************************
 *
 * [ OSExternalInterface.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.content.IPSMimeContent;
import com.percussion.content.PSContentFactory;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.util.PSPurgableTempFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Properties;


/*
 * This class stores provides the functionality to store / load the GUI
 * information to the application.
 */
////////////////////////////////////////////////////////////////////////////////
public class OSExternalInterface implements Serializable,
                                     IGuiLink,
                                     IPersist,
                                     ICustomDropSourceData,
                                     IAuxFilesTransfer,
                                     IDataCataloger
{
   /**
    * Default constructor.
    */
   //////////////////////////////////////////////////////////////////////////////
   public OSExternalInterface()
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

   /**
    * Clear all connected figures.
    */
   //////////////////////////////////////////////////////////////////////////////
   public void clearConnectedFigures()
   {
      m_connectedFigures = new String("");
   }

   /**
   * Add a figure to the string of all figures this external interface is connected
   * with. The string has the format of:
   * figType1:figInstance1, figType2:figInstance2, ...
   *
   * @param typeId the figure ID itself, which tells us the figure type.
   * @param instanceId the ID of the figure instance (ID from its data object)
    */
   //////////////////////////////////////////////////////////////////////////////
   public void addConnectedFigure(int typeId, int instanceId)
   {
    String strTypeId = new Integer(typeId).toString();
    String strInstanceId = new Integer(instanceId).toString();

    if (m_connectedFigures.length() != 0)
      m_connectedFigures += NEXT_FIGURE;
      m_connectedFigures += strTypeId;
    m_connectedFigures += ID_DEVIDER;
    m_connectedFigures += strInstanceId;
   }

   /**
    * Set connected figures.
   *
   * @connectedFigures the connected figures string.
    */
   //////////////////////////////////////////////////////////////////////////////
   public void setConnectedFigures(String connectedFigures)
   {
      m_connectedFigures = connectedFigures;
   }

   /**
    * Get the number of connected figures.
   *
   * @return the number of connected figures.
    */
   //////////////////////////////////////////////////////////////////////////////
   public int getConnectedFigureCount()
   {
      int count = 0;

    if (m_connectedFigures == null || m_connectedFigures.length() == 0)
      return count;

    int pos = 0;
    while (pos != -1)
    {
      if (pos != 0)
        pos += NEXT_FIGURE.length();

      pos = m_connectedFigures.indexOf(NEXT_FIGURE, pos);
      count++;
    }

    return count;
   }

   /**
    * Get the figure element at provided index.
   *
   * @return int the figure type ID
    */
   //////////////////////////////////////////////////////////////////////////////
   private String getFigure(int index)
   {
    int pos = 0;

    if (index >= getConnectedFigureCount())
      return null;

    for (int i=0; i<index && pos!=-1; i++)
    {
      pos = m_connectedFigures.indexOf(NEXT_FIGURE, pos);
      pos += NEXT_FIGURE.length();
    }

    int nextPos = m_connectedFigures.indexOf(NEXT_FIGURE, pos);
    if (nextPos == -1)
      return m_connectedFigures.substring(pos, m_connectedFigures.length());

    return m_connectedFigures.substring(pos, nextPos);
   }

   /**
    * Get the figure type at provided index.
   *
   * @return int the figure type ID
    */
   //////////////////////////////////////////////////////////////////////////////
   public int getConnectedFigureType(int index)
   {
    String figure = getFigure(index);
    if (figure == null)
      return -1;

    int pos = figure.indexOf(ID_DEVIDER);
    figure = figure.substring(0, pos);

    return (new Integer(figure)).intValue();
   }

   /**
    * Get the figure instance at provided index.
   *
   * @return int the figure instance ID
    */
   //////////////////////////////////////////////////////////////////////////////
   public int getConnectedFigureInstance(int index)
   {
    String figure = getFigure(index);
    if (figure == null)
      return -1;

    int pos = figure.indexOf(ID_DEVIDER);
    figure = figure.substring(pos+1, figure.length());

    return (new Integer(figure)).intValue();
   }

   //////////////////////////////////////////////////////////////////////////////
   // implementation for IDataCataloger
   public void catalogData(ObjectType iObjType, CatalogReceiver container)
   {

     if (iObjType.equals(ObjectType.HTML_PARAM))
     {
        // get the splitter tag prefix
        String strPsxTag = Util.getXSplitDynamicPrefix();

        // used AuxFilesTransferHelper object to automatically get the
        // ExternalInterface File from either the server or the temp file
        AuxFilesTransferHelper auxFile = new AuxFilesTransferHelper((UIConnectableFigure)getFigure());

        //  htmlconvertor no longer supported

        m_bIsDataDirty = false;
     }

   }

  public boolean isDataDirty()
  {
    return m_bIsDataDirty;
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

      if (!(store instanceof OSExternalInterface))
      {
         Object[] astrParams =
         {
            "OSExternalInterface"
         };
         throw new IllegalArgumentException(MessageFormat.format(
               E2Designer.getResources().getString("IncorrectType"), astrParams));
      }

      // restore GUI information
      int currentId = this.getId();
      m_filePath = config.getProperty(KEY_EXTERNAL_INTERFACE_HTML + currentId);
      if (m_owner != null)
         m_owner.invalidateLabel();

      OSLoadSaveHelper.loadOwner(currentId, config, m_owner);
      return true;
   }

   //////////////////////////////////////////////////////////////////////////////
   // IPersist interface implementation
   public void save(PSApplication app, Object store, Properties config)
   {
      if (null == store || null == config)
         throw new IllegalArgumentException();

      if (!(store instanceof PSApplication))
      {
         throw new IllegalArgumentException(MessageFormat.format(
               E2Designer.getResources().getString("IncorrectType"),
               new Object[] {"PSApplication"}));
      }

      try
      {
         // store current id temporary and create a new unique id for this object
         final int currentId = this.getId();
         this.setId(Util.getUniqueId());
         
         if (app instanceof OSApplication)
         {
            saveToServer((OSApplication) app);
         }

         // then store all keys with the new ID created.
         final String strId = Integer.toString(this.getId());
         config.setProperty(KEY_EXTERNAL_INTERFACE_KEY + strId, strId);
         config.setProperty(KEY_EXTERNAL_INTERFACE_FIGURES + strId, m_connectedFigures);
         if (m_filePath != null)
         {
            config.setProperty(KEY_EXTERNAL_INTERFACE_HTML + strId, m_filePath);
         }

         // save GUI information
         OSLoadSaveHelper.saveOwner(currentId, this.getId(), config, m_owner);
    }
    catch (Exception e)
    {
       e.printStackTrace();
    }
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

   //////////////////////////////////////////////////////////////////////////////
  // ICustomDropSourceData interface implementation
   public boolean prepareSourceForDrop(DropAction action, String filePath, String rootName)
  {
    // just do this once
    if (m_bSourceIsPrepared)
      return true;

    // indicate this has been prepared
    m_bSourceIsPrepared = true;

    // indicate that cache is dirty, must re-catalog; so clear ALL caches
    m_bIsDataDirty = true;
    //CatalogHtmlParam.resetParamMap();
    //CatalogHtmlParam.setFocusedDataset( null );

    if (action.equals(DropAction.UPDATE) ||
        action.equals(DropAction.STATIC))
    {
      setFilePath(filePath);
      return true;
    }

    return true;
  }

  /**
   * Save the local file associated to the server. Marks for preserving if 
   * no local file is associated with the object. 
   * @param app the application object, must not be <code>null</code>.
   * @return <code>false</code> if the local file is associated with this 
   * object and save fails for any reason, <code>true</code> otherwise.
   */
   private boolean saveToServer(OSApplication app)
    {
      // this is not from the local file system
      if (getTempFilePath() == null)
      {
         if (m_filePath == null)
            return true;
            
         File file = new File(Util.stripPath(m_filePath));
         app.saveAppFile(file);
         return true;
      }

      try
      {
         File file = new File(Util.stripPath(m_filePath));
         File tempFile = new File(getTempFilePath());
         IPSMimeContent dtd = PSContentFactory.loadHtmlFile( tempFile );
         InputStream is = dtd.getContent();
         app.saveAppFile( file, is );

         // delete the file
         if(!(getTempFilePath().equals(getFilePath())))
         {
            if(tempFile != null)
               tempFile.delete();
         }
         setTempFilePath(null);
      }

      catch (FileNotFoundException e)
      {
         final Object[] astrParams =
         {
            m_filePath,
            e.toString()
         };
         String msg = MessageFormat.format(
            E2Designer.getResources().getString("FileNotFound"), astrParams );
         PSDlgUtil.showErrorDialog(
               msg, E2Designer.getResources().getString("OpErrorTitle"));
         return false;
      }
      catch (IOException e)
      {
         // this should never happen
         e.printStackTrace();
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
    * Implementation of the interface method. Mark for deletion if the 
    * associated file path is valid. 
    * @see IPersist
    */
   public void cleanup(OSApplication psApp)
   {
      if (m_filePath != null && m_filePath.length() > 0)
         psApp.removeAppFile(new File(Util.stripPath(m_filePath)));
   }

  /*
   * Get the label text.
   *
   * @return String the current label text 
   */
   public String getLabelText()
   {
    if (m_filePath == null)
      return m_filePath;

    File file = new File(Util.stripPath(m_filePath));
    return file.getName();
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
      setTempFilePath(filePath);
      if (m_owner != null)
         m_owner.invalidateLabel();
   }

   //////////////////////////////////////////////////////////////////////////////
  // ICustomDropSourceData
   public void setUsedLocalFileSystem()
   {
   // no op -  since now using m_tempFilePath to determine if we are using local file system
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
   **/
   private  void readObject(java.io.ObjectInputStream stream)
       throws IOException, ClassNotFoundException
   {
      stream.defaultReadObject();
      if ( hasFiles())
      {
         AuxFilesTransferHelper auxFilesHelper = (AuxFilesTransferHelper)stream.readObject();
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
   // no need to serialize, it will be replaced if deserialized
   transient private String m_tempFilePath = null;

  /**
   * The reference to the temp file object. This is to prevent G.C.'ed while it
   * is in use. It is <code>null</code> if has not set. 
   * No need to be serialized, used for runtime only.
   */
  transient private PSPurgableTempFile m_tempFile = null;
     
   private String m_filePath = null;

   private UIFigure m_owner = null;
   private int m_id = 0;
   private String m_connectedFigures = new String("");

   private static final String NEXT_FIGURE = ", ";
   private static final String ID_DEVIDER = ":";
   /*
    * status flag which indicates wether or not the source preparation from the
    * local file system has been done
    */
   private boolean m_bSourceIsPrepared = false;
   
   /*
    * The user configuration keys usedfor  external interfaces. the unique ID
    * will be added to distinguish between different instances.
    */
   public static final String KEY_EXTERNAL_INTERFACE_KEY = new String("externalInterfaceKey");
   public static final String KEY_EXTERNAL_INTERFACE_HTML = new String("externalInterfaceHtml");
   public static final String KEY_EXTERNAL_INTERFACE_FIGURES = new String("externalInterfaceConnectedFigures");
   public static final String KEY_EXTERNAL_INTERFACE_FILE_PATH = new String("externalInterfaceFilePath");

  /**
   * <CODE>true</CODE> by default; <CODE>true</CODE> if a new static page has
   * been dropped onto this object&apos;s figure, or first time cataloging data.
   */
  private boolean m_bIsDataDirty = true;
}
