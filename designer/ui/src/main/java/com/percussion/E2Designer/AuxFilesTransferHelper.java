/******************************************************************************
 *
 * [ AuxFilesTransferHelper.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/


package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSApplicationFile;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.util.IOTools;
import com.percussion.util.PSPurgableTempFile;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;


/**
 * Helper class to assist in copying auxillary files during DND/copy/paste to other
 * application. To use the class, it should be created and serialized when the
 * UIConnectableFigure is serialized, then getFile() should be called when the
 * uic is deserialized. This will result in the creation of a temp file that
 * has a copy of the contents of the application file belonging to the object
 * that was dragged or copied.
**/
public class AuxFilesTransferHelper implements Serializable
{
   /**
    * Constructor takes the UIConnectableFigure object that uses this helper class.
    * Gets the temp file path from the fig's data object. If null, then saves the source
    * application name, to retrieve the file from.
    *
    * @param fig - the UIConnectableFigure object that has associated auxillary files.
    *
   **/
   public AuxFilesTransferHelper(UIConnectableFigure fig)
   {
      if(! (fig.getData() instanceof IAuxFilesTransfer))
         throw new IllegalArgumentException("UIConnectableFigure's getData() object is not an instance of IAuxFilesTransfer");


      m_strTempFilePath = ((IAuxFilesTransfer) fig.getData()).getTempFilePath();
      if ( null != m_strTempFilePath )
         return;

      Object o = fig.getParent();
      while(o != null && !(o instanceof UIAppFrame))
      {
         o = ((Component)o).getParent();
      }
      if(o != null && o instanceof UIAppFrame)
      {
         UIAppFrame appFrame = (UIAppFrame)o;
         m_sourceAppName = appFrame.getApp().getName();
      }
   }

   public AuxFilesTransferHelper( String tempFilePath )
   {
      if ( null == tempFilePath || 0 == tempFilePath.trim().length() )
         throw new IllegalArgumentException( "path param can't be null or empty" );

      m_strTempFilePath = tempFilePath;
   }

   public AuxFilesTransferHelper( PSApplication app )
   {
      if ( null == app )
         throw new IllegalArgumentException( "App param can't be null" );

      m_sourceAppName = app.getName();
   }

   /**
    * Returns the purgable temp file object, which contains the content from
    * either the internal saved temporary file or the file from the
    * application's root directory.
    *
    * @param fig The figure that was created during de-serialization.
   **/
   public PSPurgableTempFile getFile(IAuxFilesTransfer data)
   {
      PSPurgableTempFile targetFile = null;
      final String strFileName = data.getServerFileName(); // strips off the path, just returns filename

      try
      {
         InputStream in = null;
         targetFile = new PSPurgableTempFile(generatePrefix(strFileName), ".tmp", null);
         if(m_strTempFilePath != null)
         {
            File sourceFile = null;
            sourceFile = new File(m_strTempFilePath);
            in = new FileInputStream(sourceFile);
         }
         else //m_sourceAppName not null      TODO: handle case when app is still null /empty
         {
            PSApplication app = getObjectStore().getApplication(m_sourceAppName, false);

            //load application file from server
            PSApplicationFile psfile = new PSApplicationFile(new File(strFileName));
            psfile = getObjectStore().loadApplicationFile(app, psfile);
            in = psfile.getContent().getContent();
         }

         if(in != null)
         {
            //write byte array of application file to file on disk
            FileOutputStream out = new FileOutputStream(targetFile);
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
      catch(IOException e)
      {
         e.printStackTrace();
      }
      catch(Exception e) // TODO: catch specific types
      {
         e.printStackTrace();
      }

      return targetFile;
   }

   /**
    * Generates file name prefix from provided file name
    */
   private String generatePrefix(final String strFileName)
   {
      int firstOccur = strFileName.indexOf( '.' );
      // prefix must be at least 3 chars
      return firstOccur <= 2 ? "psxfile" : strFileName.substring(0, firstOccur);  
   }

   /**
    * @return
    */
   private PSObjectStore getObjectStore()
   {
      return E2Designer.getApp().getMainFrame().getObjectStore();
   }

   /**
    * The name of the application that owns the application file that needs to
    * be copied. If not null, an application by this name will attempt to be
    * opened and the applcation file contents will be copied to a temporary file.
   **/
   private String m_sourceAppName = null;
   /**
    * If the data object has never been saved to the server, it will have a temporary
    * file. We will use this if present. If not present, this will be null and
    * m_sourceAppName should have a valid name. If present, then m_sourceAppName
    * should be null.
   **/
   private String m_strTempFilePath = null;
}

