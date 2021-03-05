/*[ PSContentLoaderPreview.java ]**********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSException;
import com.percussion.loader.objectstore.PSExtractorDef;
import com.percussion.loader.objectstore.PSLoaderPreviewDef;
import com.percussion.loader.objectstore.PSStaticItemExtractorDef;
import com.percussion.loader.ui.IPSUIPlugin;
import com.percussion.loader.ui.PSConfigPanel;
import com.percussion.loader.ui.PSListContentEditorPanel;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * See base interface {@link IPSContentLoader}for a more generic description.
 * Plugins implementing this interface fit into the migration/loader model as
 * the loader of content. Each plugin is responsible for uploading content to
 * the configured target system.
 */
public class PSContentLoaderPreview implements IPSContentLoader, IPSUIPlugin
{
   /**
    * @see {@link IPSContentLoader#loadContentItem(PSItemContext item)} for
    *      detail.
    */
   public void loadContentItem(PSItemContext itemContext)
      throws PSLoaderException
   {

      Document itemDoc = itemContext.getStandardItemDoc();
      File file = getItemFile(itemContext);

      writeDocToFile(file, itemDoc);

      if (itemContext.getLocator() == null) // update a new content
         itemContext.setLocator(new PSLocator(0, -1)); // create a fake locator
   }

   /**
    * Get a <code>File</code> object for a uploaded item.
    *
    * @param itemContext The to be uploaded item, assume not <code>null</code>.
    *
    * @return The <code>File</code>, never <code>null</code>.
    */
   private File getItemFile(PSItemContext itemContext) throws PSLoaderException
   {
      IPSContentTreeNode node = itemContext.getContentTreeNode();
      String resId = itemContext.getResourceId();
      String filePath;
      File hostFile;

      try
      {
         String host = PSLoaderUtils.getHostFromURL(resId);
         filePath = PSLoaderUtils.getFilePathFromURL(resId);

         // look for "C:/", then skip it if exist
         int i = filePath.indexOf(":/");
         if (i != -1)
            filePath = filePath.substring(i + 2); // skip ":/", get the rest

         hostFile = new File(m_prevPathFile, host);
      }
      catch (Exception ex)
      {
         throw new PSLoaderException(IPSLoaderErrors.UNEXPECTED_ERROR,
            ex.toString());
      }

      return new File(hostFile, filePath);
   }

   /**
    * Write a uploaded document a specified file.
    *
    * @param xmlFile The file to write to, assume not <code>null</code>.
    *
    * @param itemDoc The to be uploaded item document, assume not 
    *    <code>null</code>.
    *
    * @throws PSLoaderException if any error occurs.
    */
   private void writeDocToFile(File xmlFile, Document itemDoc)
      throws PSLoaderException
   {
      //System.out.println("writeToFile: " + xmlFile.getAbsolutePath());

      File dir = xmlFile.getParentFile();
      if (! dir.exists())
         dir.mkdirs();
      FileOutputStream out = null;
      try
      {
         out = new FileOutputStream(xmlFile);
         PSXmlDocumentBuilder.write(itemDoc, out);
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
    * @see {@link IPSContentLoader#loadStaticItem(PSItemContext item)} for
    *      detail.
    */
   public void loadStaticItem(PSItemContext item) throws PSLoaderException
   {
      ByteArrayInputStream in = null;

      try
      {

         File file = getStaticFile(item);
         byte[] data = item.getStaticData();

         writeToStaticFile(file, data);
      }
      catch (Exception e)
      {
         throw new PSLoaderException(
            IPSLoaderErrors.UNEXPECTED_ERROR, e.toString());
      }
      finally
      {
         if (in != null)
         {
            try { in.close(); } catch (Exception e){};
         }
      }
   }

   /**
    * Get an static <code>File</code> object for a to be uploaded item.
    *
    * @param item The to be uploaded item, assume not <code>null</code>.
    *
    * @return The <code>File</code> object for the uploaded <code>item</code>.
    *
    * @throws Exception if any error occurs.
    */
   private File getStaticFile(PSItemContext item) throws Exception
   {
      PSExtractorDef def = item.getExtractorDef();
      PSStaticItemExtractorDef staticDef;
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      staticDef = new PSStaticItemExtractorDef(def.toXml(doc));

      String targetLoc = staticDef.getTargetLocation();
      String fileName = new File(item.getResourceId()).getName();

      File targetFile = new File(m_staticRootFile, targetLoc);

      return new File(targetFile, fileName);
   }

   /**
    * Write the data to a file.
    *
    * @param dataFile The file that will write the data to, assume not
    *    <code>null</code>.
    *
    * @param data The data that is going to write to, assume not
    *    <code>null</code>
    *
    * @throws PSLoaderException if any error occurs.
    */
   private void writeToStaticFile(File dataFile, byte[] data)
      throws PSLoaderException
   {
      //System.out.println("writeToStaticFile: " + dataFile.getAbsolutePath());

      File dir = dataFile.getParentFile();
      if (! dir.exists())
         dir.mkdirs();
      FileOutputStream out = null;
      try
      {
         out = new FileOutputStream(dataFile);
         out.write(data);
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
    * @see {@link IPSPlugin#configure(Element)} for detail.
    */
   public void configure(Element config) throws PSConfigurationException
   {
      try
      {
         m_loaderDef = new PSLoaderPreviewDef(config);

         m_prevPathFile = new File(m_loaderDef.getPreviewPath());
         m_staticRootFile = new File(m_loaderDef.getPreviewPath(),
            m_loaderDef.getStaticRoot());
      }
      catch (PSLoaderException e)
      {
         PSConfigurationException configEx = new PSConfigurationException(e);
         throw configEx;
      }
      catch (PSException e)
      {
         PSConfigurationException configEx = new PSConfigurationException(e);
         throw configEx;
      }
   }

   /**
    * Implements {@link IPSUIPlugin} interface
    */
   public PSConfigPanel getConfigurationUI()
   {
      return new PSListContentEditorPanel(true);
   }

   /**
    * The loader definition, set by <code>configure</code> only, never
    * <code>null</code> after that.
    */
   private PSLoaderPreviewDef m_loaderDef = null;

   /**
    * The (fully qualified) preview directory for to be uploaded contents.
    * Initialized by <code>configure()</code>, never <code>null</code> after
    * that.
    */
   private File m_prevPathFile = null;

   /**
    * The (fully qualified) directory for to be uploaded static files or
    * contents. This directory is directly under the <code>m_prevPath</code>.
    * Initialized by <code>configure()</code>, never <code>null</code> after
    * that.
    */
   private File m_staticRootFile = null;
}