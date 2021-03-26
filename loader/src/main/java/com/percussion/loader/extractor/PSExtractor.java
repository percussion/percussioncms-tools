/*[ PSExtractor.java ]*********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.extractor;

import com.percussion.error.PSException;
import com.percussion.loader.IPSItemExtractor;
import com.percussion.loader.PSConfigurationException;
import com.percussion.loader.PSItemContext;
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.PSLoaderUtils;
import com.percussion.loader.objectstore.PSExtensionDef;
import com.percussion.loader.objectstore.PSExtractorDef;
import com.percussion.loader.objectstore.PSFilter;
import com.percussion.loader.objectstore.PSMimeTypeDef;
import com.percussion.loader.ui.IPSUIPlugin;
import com.percussion.loader.ui.PSConfigPanel;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class can be used as the super class for all extractors. The derived
 * class must implement the <code>extractItems</code> method, which is defined
 * in <code>IPSItemExtractor</code>. The GUI configuration panel is empty,
 * which can be customized by overriding the {@link #getConfigurationUI()}
 * method.
 */
public abstract class PSExtractor
   implements IPSItemExtractor, IPSUIPlugin
{
   /**
    * Implemented an empty configuration panel. See
    * {@link com.percussion.loader.ui.IPSUIPlugin#getConfigurationUI()} for
    * detail description.
    */
   public PSConfigPanel getConfigurationUI()
   {
      return new PSConfigPanel();
   }

   // see {@link com.percussion.loader.IPSPlugin} for description
   public void configure(Element config) throws PSConfigurationException
   {
      try
      {
         m_extractorDef = new PSExtractorDef(config);
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

   // see {@link com.percussion.loader.IPSItemExtractor} for description
   public Element getConfigure()
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      return m_extractorDef.toXml(doc);
   }


   /**
    * It uses the filters and mimetype-extension properties to determine
    * whether the specified resource is the current extractor.
    *
    * @param resource The data to be analyzed, including its meta data. Never
    *    <code>null</code>.
    *
    * @return The number of items that would be created if the {@link
    *    #extractItems(PSItemContext, InputStream)} method was called with
    *    the supplied resource.
    *
    * @see
    *    com.percussion.loader.IPSItemExtractor#containsInstances(PSItemContext)
    */
   public int containsInstances( PSItemContext resource )
   {
      if (! matchFilter(resource.getResourceId()))
         return 0;

      return matchMimeType(resource) ? 1 : 0;
   }

   /**
    * Get extractor definition. The {@link #configure(Element)} needs to be
    * called first.
    *
    * @return the extractor definition, never <code>null</code>.
    *
    * @throws IllegalStateException if the extractor definition is not
    *    available.
    */
   public PSExtractorDef getExtractorDef()
   {
      if (m_extractorDef == null)
         throw new IllegalStateException("m_extractorDef cannot be null");

      return m_extractorDef;
   }

   /**
    * Determines whether the specified resource-id matches one of the
    * filters that is specified in the extractor definition.
    *
    * @param resourceId The to be analyzed resource-id, it may not be
    *    <code>null</code> or empty.
    *
    * @return <code>true</code> if found a match or there is no filter specified
    *    in the extractor definition; otherwise return <code>false</code>.
    */
   protected boolean matchFilter(String resourceId)
   {
      if (resourceId == null || resourceId.trim().length() == 0)
         throw new IllegalArgumentException(
            "resourceId must not be null or empty");
         
      if (m_extractorDef.getFiltersList().isEmpty())
         return true; // empty filter is the same as "*"

      Iterator filters = m_extractorDef.getFilters();
      while (filters.hasNext())
      {
         PSFilter filter = (PSFilter) filters.next();
         if (PSLoaderUtils.matchRegExpression(resourceId, filter.getValue()))
            return true;
      }
      return false;
   }

   /**
    * Determines whether the specified resouce matches one of the mime type (or
    * extension) that is specified in the extractor definition.
    *
    * @param resource The to be analyzed resource, it may not be
    *    <code>null</code>.
    *
    * @return <code>true</code> if found a match; otherwise return
    *    <code>false</code>.
    */
   protected boolean matchMimeType(PSItemContext resource)
   {
      if (resource == null)
         throw new IllegalStateException("resource must not be null");

      String mimeTypeName = resource.getResourceMimeType();
      String mimeExtension = resource.getResourceExtension();

      Iterator mimeTypes = m_extractorDef.getMimeTypeList().iterator();
      while (mimeTypes.hasNext())
      {
         // try to match mime-type first
         PSMimeTypeDef mimeType = (PSMimeTypeDef) mimeTypes.next();
         if (mimeType.getName().equals(mimeTypeName))
            return true;

         // then try to match the extension
         if (findExtension(mimeExtension, mimeType.getExtensions()))
            return true;
      }

      return false; // cannot find a match
   }

   /**
    * Looking for an extension from the specified list of extensions.
    *
    * @param mimeExtension The to be searched extension, assume not
    *    <code>null</code>
    * @param extensions A list of <code>PSExtensionDef</code> objects, assume
    *    not <code>null</code>.
    *
    * @return <code>true</code> if found the extension in the list;
    *    <code>false</code> otherwise.
    */
   private boolean findExtension(String mimeExtension,
      Iterator extensions)
   {
      while (extensions.hasNext())
      {
         PSExtensionDef extension = (PSExtensionDef) extensions.next();
         if (extension.getAttValue().equals(mimeExtension))
            return true;
      }

      return false;
   }

   /**
    * The definition of the extractor, set by <code>configure()</code>
    * only, never <code>null</code> after that.
    */
   private PSExtractorDef m_extractorDef = null;
}
