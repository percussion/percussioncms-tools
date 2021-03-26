/*[ OSCustomError.java ]*******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSApplicationFile;
import com.percussion.design.objectstore.PSCustomError;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.util.PSCharSets;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;

/*
 * Wrapper for PSErrorPage
 */
public class OSCustomError extends PSCustomError implements ICustomDropSourceData
{
   /**
    * Constructor
    */
   public OSCustomError(String error, URL url) throws PSIllegalArgumentException
   {
      super(error, url);
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param page a valid OSErrorWebpages. If null, a PSIllegalArgumentException is
    * thrown.
    *
    * @throws PSIllegalArgumentException if page is null
    */
   public void copyFrom(OSCustomError error) throws PSIllegalArgumentException
   {
      copyFrom((PSCustomError) error);

      // assume object is valid
      m_bUsedLocalFileSystem = error.m_bUsedLocalFileSystem;
      m_bSourceIsPrepared = error.m_bSourceIsPrepared;
    m_filePath = error.m_filePath;
    m_outputStream = error.m_outputStream;
   }

  /*
   * Create an HTM document using the file path. If successful, the created
   * output stream is stored in m_outputStream, so it can be saved in
   * saveToServer.
   *
   * @return boolean true if successful, false otherwise
   */
   private boolean createHtmDocument()
   {
    String filePath = getFilePath();
    try
    {
      FileInputStream in = new FileInputStream(filePath);
      BufferedInputStream bufIn = new BufferedInputStream(in);
      m_outputStream = new ByteArrayOutputStream();
      int read;
      byte[] buf = new byte[1024];
      while ((read = bufIn.read(buf)) >= 0)
      {
      // convert to standard bytes from local encoding and rewrite
      String str = new String(buf, 0, read);
        m_outputStream.write(str.getBytes(PSCharSets.rxJavaEnc()));
        if (read < buf.length)
          break;
      }
      m_outputStream.flush();
      m_outputStream.close();

      m_bUsedLocalFileSystem = true;
      return true;
    }
    catch (FileNotFoundException e)
    {
         final Object[] astrParams =
         {
        filePath,
            e.toString()
         };
         PSDlgUtil.showErrorDialog(
               MessageFormat.format(E2Designer.getResources().getString("FileNotFound"), astrParams),
               E2Designer.getResources().getString("OpErrorTitle"));
      return false;
    }
    catch (IOException e)
    {
      // this should never happen
      e.printStackTrace();
      return false;
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  // implementaion for ICustomDropSourceData
   public boolean prepareSourceForDrop(DropAction action, String filePath, String rootName)
  {
    // just do this once
    if (m_bSourceIsPrepared)
      return true;

    // indicate this has been prepared
    m_bSourceIsPrepared = true;

    if (action == DropAction.STATIC)
    {
      setFilePath(filePath);
      return createHtmDocument();
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
   protected boolean saveToServer(OSApplication app)
   {
      if (app == null)
         throw new IllegalArgumentException("app must not be null");
      // this is not from the local file system
      if (!m_bUsedLocalFileSystem)
      {
         if (m_filePath == null)
            return true;

         File file = new File(Util.stripPath(m_filePath));
         app.saveAppFile(file);
         return true;
      }

      try
      {
         byte[] buf = m_outputStream.toByteArray();
         ByteArrayInputStream is = new ByteArrayInputStream(buf);

         File file = new File(Util.stripPath(m_filePath));
         PSApplicationFile appFile = new PSApplicationFile(is, file);

         PSObjectStore os = E2Designer.getApp().getMainFrame().getObjectStore();
         os.saveApplicationFile(app, appFile, true, true);
         OSLoadSaveHelper.logSaveApplicationFile(appFile.getFileName());
      }
      catch (Exception e)
      {
         e.printStackTrace();
         return false;
      }

      return true;
   }

  //////////////////////////////////////////////////////////////////////////////
  // implementaion for ICustomDropSourceData
  public void setFilePath(String filePath)
  {
    m_filePath = filePath;
  }

  //////////////////////////////////////////////////////////////////////////////
  // implementaion for ICustomDropSourceData
  public String getFilePath()
  {
    return m_filePath;
  }

  //////////////////////////////////////////////////////////////////////////////
  // implementaion for ICustomDropSourceData
  public void setUsedLocalFileSystem()
  {
    m_bUsedLocalFileSystem = true;
  }

  /*
   * Returns whether or not the local file system is used.
   *
   * @return boolean the local file system flag
   */
  //////////////////////////////////////////////////////////////////////////////
  public boolean localFileSystemUsed()
  {
    return m_bUsedLocalFileSystem;
  }

  //////////////////////////////////////////////////////////////////////////////
   // private storage
  /*
   * status flag to save wether or not we have to store files from the local
   * file system
   */
   private boolean m_bUsedLocalFileSystem = false;
  /*
   * status flag which indicates wether or not the source preparation from the
   * local file system has been done
   */
   private boolean m_bSourceIsPrepared = false;
  /*
   * Storage for the local file path
   */
  private String m_filePath = null;
  /*
   * Storage for the HTML file to be saved to the server
   */
  private ByteArrayOutputStream  m_outputStream = null;
}
