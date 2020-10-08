/*[ PSPluginFactory.java ]*****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader;

import com.percussion.loader.objectstore.PSContentSelectorDef;
import com.percussion.loader.objectstore.PSExtractorDef;
import com.percussion.loader.objectstore.PSLoaderComponent;
import com.percussion.loader.objectstore.PSLoaderDef;
import com.percussion.loader.objectstore.PSTransformationDef;
import com.percussion.xml.PSXmlDocumentBuilder;

import org.w3c.dom.Document;

/**
 * A singlton class, creates plugin objects from its corresponding definition.
 */
public class PSPluginFactory
{
   /**
    * Private constructor to enforce singleton pattern.
    */
   private PSPluginFactory()
   {
   }

   /**
    * Gets the single instance of this class.
    *
    * @return The instance, never <code>null</code>.
    */
   public static PSPluginFactory getInstance()
   {
      if (m_instance == null)
         m_instance = new PSPluginFactory();

      return m_instance;
   }

   /**
    * Creates an instance of content selector from a given definition
    *
    * @param loaderComp The definition of the content selector, may not
    *    <code>null</code>
    *
    * @return The created content selector, never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>loaderComp</code> is
    *    <code>null</code>.
    * @throws PSLoaderException if any error occures.
    */
   public IPSContentSelector newContentSelector(PSLoaderComponent loaderComp)
      throws PSLoaderException
   {
      IPSContentSelector selector = null;
      PSContentSelectorDef selectDef = (PSContentSelectorDef)loaderComp;
      // just for testing
      if (selectDef == null)
         throw new IllegalArgumentException("selectDef may not be null");
      String className = selectDef.getPlugInClass();
      try
      {
         Class pluginClass = Class.forName(className);
         selector = (IPSContentSelector) pluginClass.newInstance();
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         selector.configure(selectDef.toXml(doc));
      }
      catch (Exception e)
      {
         Object[] args = {className, e.getLocalizedMessage()};
         throw new PSLoaderException(
            IPSLoaderErrors.INIT_PLUGIN_ERROR, args);
      }
      return selector;
   }

   /**
    * Creates an instance of item extractor from a given definition
    *
    * @param extractorDef The definition of the item extractor, may not
    *    <code>null</code>
    *
    * @return The created item extractor, never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>extractorDef</code> is
    *    <code>null</code>.
    *
    * @throws PSLoaderException if any error occures.
    */
   public IPSItemExtractor newItemExtractor(PSExtractorDef extractorDef)
      throws PSLoaderException
   {
      if (extractorDef == null)
         throw new IllegalArgumentException("extractorDef may not be null");

      String className = extractorDef.getPlugInClass();
      IPSItemExtractor extractor = null;

      try
      {
         Class pluginClass = Class.forName(className);
         extractor = (IPSItemExtractor) pluginClass.newInstance();
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         extractor.configure(extractorDef.toXml(doc));
      }
      catch (Exception e)
      {
         Object[] args = {className, e.getLocalizedMessage()};
         throw new PSLoaderException(
            IPSLoaderErrors.INIT_PLUGIN_ERROR, args);
      }

      return extractor;
   }

   /**
    * Creates an instance of content loader from a given definition
    *
    * @param loaderDef The definition of the content loader, may not
    *    <code>null</code>
    *
    * @return The created content loader object, never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>loaderDef</code> is
    *    <code>null</code>.
    *
    * @throws PSLoaderException if any error occures.
    */
   public IPSContentLoader newContentLoader(PSLoaderDef loaderDef)
      throws PSLoaderException
   {
      if (loaderDef == null)
         throw new IllegalArgumentException("loaderDef may not be null");

      String className = loaderDef.getPlugInClass();
      IPSContentLoader loader = null;

      try
      {
         Class pluginClass = Class.forName(className);
         loader = (IPSContentLoader) pluginClass.newInstance();
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         loader.configure(loaderDef.toXml(doc));
      }
      catch (Exception e)
      {
         Object[] args = {className, e.getLocalizedMessage()};
         throw new PSLoaderException(
            IPSLoaderErrors.INIT_PLUGIN_ERROR, args);
      }

      return loader;
   }

   /**
    * Creates an instance of field transformer for a given definition.
    *
    * @param loaderComp, base class for all the objectstore classes, actually an
    * instanceof of {@link com.percussion.loader.objectstore.PSTransformationDef}
    * Never <code>null</code>.
    *
    * @return The created field transformer object, never <code>null</code>.
    *
    * @throws PSLoaderException if any error occures.
    */
   public IPSFieldTransformer newFieldTransformer(PSLoaderComponent loaderComp)
      throws PSLoaderException
   {
      if (loaderComp == null)
         throw new IllegalArgumentException("transformerDef may not be null");

      PSTransformationDef tranDef = (PSTransformationDef)loaderComp;
      String className = tranDef.getPlugInClass();
      IPSFieldTransformer fTrans = null;

      try
      {
         Class pluginClass = Class.forName(className);
         fTrans = (IPSFieldTransformer) pluginClass.newInstance();
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         fTrans.configure(loaderComp.toXml(doc));
      }
      catch (Exception e)
      {
         Object[] args = {className, e.getLocalizedMessage()};
         throw new PSLoaderException(
            IPSLoaderErrors.INIT_PLUGIN_ERROR, args);
      }
      return fTrans;
   }

   /**
    * Creates an instance of item transformer for a given definition.
    *
    * @param loaderComp, base class for all the objectstore classes, actually an
    * instanceof of {@link com.percussion.loader.objectstore.PSTransformationDef}
    * Never <code>null</code>.
    *
    * @return The created item transformer object, never <code>null</code>.
    *
    * @throws PSLoaderException if any error occures.
    */
   public IPSItemTransformer newItemTransformer(PSLoaderComponent loaderComp)
      throws PSLoaderException
   {
      if (loaderComp == null)
         throw new IllegalArgumentException("loaderDef may not be null");

      PSTransformationDef tranDef = (PSTransformationDef)loaderComp;
      String className = tranDef.getPlugInClass();
      IPSItemTransformer iTrans = null;
      try
      {
         Class pluginClass = Class.forName(className);
         iTrans = (IPSItemTransformer) pluginClass.newInstance();
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         iTrans.configure(loaderComp.toXml(doc));
      }
      catch (Exception e)
      {
         Object[] args = {className, e.getLocalizedMessage()};
         throw new PSLoaderException(
            IPSLoaderErrors.INIT_PLUGIN_ERROR, args);
      }
      return iTrans;
   }

   /**
    * Singleton instance of this class, set by first call to
    * {@link #getInstance()}, never <code>null</code> or modified after that.
    */
   private static PSPluginFactory m_instance = null;
}
