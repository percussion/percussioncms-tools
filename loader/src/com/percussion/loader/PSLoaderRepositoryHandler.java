/*[ PSLoaderRepositoryHandler.java ]*******************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader;

import com.percussion.error.PSException;
import com.percussion.loader.objectstore.PSLoaderDescriptor;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class handles I/O for all files in the directory of the loader
 * descriptor.
 */
public class PSLoaderRepositoryHandler
{
  /**
   * Constructs a handler from a path of the handler.
   *
   * @param descPath The path information to which all files for this descriptor
    *    will be saved to. The descriptor name is the last name in the
    *    pathname's name sequence. It may not be <code>null</code> or empty.
   */
   public PSLoaderRepositoryHandler(String descPath)
   {
      if (descPath == null || descPath.trim().length() == 0)
         throw new IllegalArgumentException(
            "descPath may not be null or empty");

      m_descPath = descPath;
      File file = new File(m_descPath);
      m_descName = file.getName();
   }

   /**
    * Get the descriptor in <code>Document</code> form, which is extracted from
    * the file system.
    *
    * @return The loader descriptor document, may be <code>null</code> if not
    *    exists
    *
   * @throws PSLoaderException if any error occurs.
    */
   public Document getDescriptorDoc() throws PSLoaderException
   {
      Document doc = null;
      File descFile = getDescriptorFile();

      PSLoaderDescriptor desc = null;

      if (descFile.exists())
      {
         FileInputStream in = null;
         try
         {
            in = new FileInputStream(descFile);
            doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
            Element descEl = doc.getDocumentElement();
            desc = new PSLoaderDescriptor(descEl);
         }
         catch (Exception ex)
         {
            throw new PSLoaderException(IPSLoaderErrors.UNEXPECTED_ERROR,
               ex.toString());
         }
         finally
         {
            if (in != null)
               try {in.close();} catch(IOException e){}
         }
      }

      return doc;
   }

   /**
    * Get the descriptor from the disk.
    *
    * @return The loader descriptor, may be <code>null</code> if not exists
    *
   * @throws PSLoaderException if any error occurs.
    */
   public PSLoaderDescriptor getDescriptor() throws PSLoaderException
   {
      PSLoaderDescriptor desc = null;
      Document doc = getDescriptorDoc();
      if (doc != null)
      {
         Element descEl = doc.getDocumentElement();
         try
         {
            desc = new PSLoaderDescriptor(descEl);
            desc.setPath(m_descPath);
         }
         catch (PSException ex)
         {
            throw new PSLoaderException(ex);
         }

         // handle the override server and port if specified from command line
         // argument.
         PSContentLoaderApp loaderApp = PSContentLoaderApp.getInstance();
         if ( loaderApp.getOverrideServer() != null )
            desc.getConnectionDef().setServerName(loaderApp.getOverrideServer());
         if ( loaderApp.getOverridePort() != -1 )
            desc.getConnectionDef().setPortInt(loaderApp.getOverridePort());
      }

      return desc;

   }

  /**
   * Save the given descriptor to the disk.
   *
   * @param desc The to be saved descriptor. It may not be <code>null</code>.
   *
   * @throws PSLoaderException if any error occurs.
   */
   public void saveDescriptor(PSLoaderDescriptor desc) throws PSLoaderException
   {
      if (desc == null )
         throw new IllegalArgumentException("desc may not be null");

      File dir = new File(m_descPath);
      if (! dir.exists())
         dir.mkdirs();
      File descFile = getDescriptorFile();
      FileOutputStream out = null;
      try
      {
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         Element descEl = desc.toXml(doc);
         PSXmlDocumentBuilder.replaceRoot(doc, descEl);
         out = new FileOutputStream(descFile);
         PSXmlDocumentBuilder.write(doc, out);
      }
      catch (Exception ex)
      {
         throw new PSLoaderException(IPSLoaderErrors.UNEXPECTED_ERROR,
            ex.toString());
      }
      finally
      {
         if (out != null)
            try {out.close();} catch(IOException e){}
      }
   }

   /**
    * Get the content status which was created and saved from previous session.
    *
    * @return The previous saved content status, may be <code>null</code> if
    *    not exists.
    *
    * @throws PSLoaderException if there is any error occurs
    */
   public PSContentStatus getStatus()
      throws PSLoaderException
   {
      PSContentStatus status = null;
      FileInputStream istream = null;
      ObjectInputStream in = null;
      File statusFile = getStatusFile();

      if (statusFile.exists())
      {
         try
         {
            // Open the file and set to read objects from it.
            istream = new FileInputStream(statusFile);
            
            Document doc = PSXmlDocumentBuilder.createXmlDocument(istream, false);
            Element statusEl = doc.getDocumentElement();
            status = new PSContentStatus(statusEl);
         }
         catch (Exception ex)
         {
            throw new PSLoaderException(IPSLoaderErrors.FAIL_TO_LOAD_STATUS,
               ex.getLocalizedMessage());
         }
         finally
         {
            if ( istream != null )
               try {istream.close();} catch (Exception e) {}
         }
      }

      return status;
   }

  /**
   * Save the content status to the file system.
   *
   * @param    status  The to be saved content status.
   *
    * @throws PSLoaderException if there is any error occurs
   */
   public void saveStatus(PSContentStatus status) throws PSLoaderException
   {
      FileOutputStream ostream = null;

      try
      {
         File dir = new File(m_descPath);
         if (! dir.exists())
            dir.mkdirs();
         File statusFile = getStatusFile();

         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         Element statusEl = status.toXml(doc);
         PSXmlDocumentBuilder.replaceRoot(doc, statusEl);

         ostream = new FileOutputStream(statusFile);
         PSXmlDocumentBuilder.write(doc, ostream);
      }
      catch (IOException ex)
      {
         throw new PSLoaderException(IPSLoaderErrors.UNEXPECTED_ERROR,
            ex.toString());
      }
      finally
      {
         if ( ostream != null )
            try {ostream.close();} catch (Exception e) {}
      }
   }

   /**
    * Get the descriptor file of the object.
    *
    * @return The descriptor file, never <code>null</code>.
    */
   private File getDescriptorFile()
   {
      return new File(m_descPath, m_descName + DESCRIPTOR_EXTESION);
   }

   /**
    * Get the status file of the object.
    *
    * @return The status file, never <code>null</code>.
    */
   private File getStatusFile()
   {
      return new File(m_descPath, m_descName + ".cls");
   }

   public static final String DESCRIPTOR_EXTESION = ".xml";

  /**
   * The name of the descriptor. Initialized by constructor, will never be
   * <code>null</code> or empty after that
   */
   private String m_descName;

  /**
   * The path of the descriptor, it includes the name of the descriptor, as the
   * last name of the path name sequence. Initialized by constructor, will
   * never be <code>null</code> or empty after that
   */
   private String m_descPath;


}