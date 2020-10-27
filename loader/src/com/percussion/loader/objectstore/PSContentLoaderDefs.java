/**[ PSContentLoaderDefs ]*****************************************************
 *
 * COPYRIGHT (c) 2002 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 ******************************************************************************/
package com.percussion.loader.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.loader.PSLoaderException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * This is a utility class, which contains common methods and data member to be
 * used by both configuration and descriptor definitions
 */
public abstract class PSContentLoaderDefs extends PSLoaderComponent
   implements java.io.Serializable
{

   /**
    * Loads the list of extractor definitions.
    *
    * @param extractorsEl The element of the extractor definitions, may not
    *    <code>null</code>
    *
    * @throws PSUnknownNodeTypeException if the XML is malformed
    * @throws PSLoaderException if any other error occurs.
    */
   protected void loadExtractorDefs(Element extractorsEl)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      if (extractorsEl == null)
         throw new IllegalArgumentException("extractorsEl may not be null");

      m_staticExtractorDefs.clear();
      m_itemExtractorDefs.clear();
      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      PSXmlTreeWalker tree = new PSXmlTreeWalker(extractorsEl);
      Element defEl = getNextRequiredElement(tree,
         PSExtractorDef.XML_NODE_NAME,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);

      while (defEl != null)
      {
         PSExtractorDef extractorDef = new PSExtractorDef(defEl);

         // NO NEED FOR THIS: get the correct definition before add to the list
         //String pluginClass = extractorDef.getPlugInClass();
         //if (pluginClass.equals(PSImageExtractorDef.PLUGIN_CLASS))
         //   extractorDef = new PSImageExtractorDef(extractorDef.toXml(doc));
         //else if (pluginClass.equals(PSFileExtractorDef.PLUGIN_CLASS))
         //   extractorDef = new PSFileExtractorDef(extractorDef.toXml(doc));
         //else if (pluginClass.equals(PSPageExtractorDef.PLUGIN_CLASS))
         //   extractorDef = new PSPageExtractorDef(extractorDef.toXml(doc));
         //else if (pluginClass.equals(PSStaticItemExtractorDef.PLUGIN_CLASS))
         //   extractorDef = new PSStaticItemExtractorDef(
         //      extractorDef.toXml(doc));

         if ( extractorDef.isStaticType() )
            m_staticExtractorDefs.addComponent(extractorDef);
         else
            m_itemExtractorDefs.addComponent(extractorDef);

         defEl = tree.getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }
   }

   /**
    * Loads both field & item transformers if there is any from the given XML
    * tree.
    *
    * @param tree The current XML tree, its current location may contains
    *    a <code>PSFieldTransformationsDef</code> and/or
    *    <code>PSItemTransformationsDef</code> objects. It may not be
    *    <code>null</code>.
    *
    * @return The current or next element from the <code>tree</code>. The
    *    returned element is not any of the above transformation elements. It
    *    may be <code>null</code> if there is no element from current location
    *    of <code>tree</code> or after the transformation defs.
    */
   protected Element loadTransformations(PSXmlTreeWalker tree)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      if (tree == null)
         throw new IllegalArgumentException("tree may not be null");

      Element listEl;

      listEl = tree.getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      if (listEl == null)
         return listEl;   // no more element available

      if (listEl.getNodeName().equals(PSFieldTransformationsDef.XML_NODE_NAME))
      {
         m_fieldTransDef = new PSFieldTransformationsDef(listEl);
         listEl = tree.getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }

      if (listEl.getNodeName().equals(PSItemTransformationsDef.XML_NODE_NAME))
      {
         m_itemTransDef = new PSItemTransformationsDef(listEl);
         listEl = tree.getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }

      return listEl;
   }

   /**
    * Get all (both static and item) extractors.
    *
    * @return An <code>PSComponentList</code> contains both static and item
    *    extractors.
    */
   public PSComponentList getAllExtractorDefs()
   {
      PSComponentList extractorDefs = new PSComponentList(XML_EXTRACTORS,
         PSExtractorDef.XML_NODE_NAME, PSExtractorDef.class, false);

      extractorDefs.addComponents(m_staticExtractorDefs.getComponents());
      extractorDefs.addComponents(m_itemExtractorDefs.getComponents());

      return extractorDefs;
   }

   /**
    * Get a list of static extractor definitions.
    *
    * @return An iterator over zero or more <code>PSExtractorDef</code> objects,
    *    never <code>null</code>, may be empty. The type of each object is
    *    static and the class of it may be derived from the
    *    <code>PSExtractorDef</code>.
    */
   public Iterator getStaticExtractorDefs()
   {
      return m_staticExtractorDefs.getComponents();
   }

   /**
    * Get a list of item extractor definitions.
    *
    * @return An iterator over zero or more <code>PSExtractorDef</code> objects,
    *    never <code>null</code>, may be empty. The type of each object is
    *    item (not static) and the class of it may be derived from the
    *    <code>PSExtractorDef</code>.
    */
   public Iterator getItemExtractorDefs()
   {
      return m_itemExtractorDefs.getComponents();
   }

   /**
    * Reset the list of static extractor definitions.
    *
    * @param extractorDefs The new set of static extractor definitions,
    *    may not <code>null</code>, but may be empty. The type of each object is
    *    static and the class of it may be derived from the
    *    <code>PSExtractorDef</code>.
    */
   public void setStaticExtractorDefs(Iterator extractorDefs)
   {
      if (extractorDefs == null)
         throw new IllegalArgumentException("extractorDefs may not be null");

      m_staticExtractorDefs.clear();

      while (extractorDefs.hasNext())
      {
         PSExtractorDef def = (PSExtractorDef) extractorDefs.next();

         if (! def.isStaticType())
            throw new IllegalArgumentException(
               "one of the extractorDefs is not static type");

         m_staticExtractorDefs.addComponent(def);
      }
   }

   /**
    * Reset the list of item (non-static) extractor definitions.
    *
    * @param extractorDefs The new set of item extractor definitions,
    *    may not <code>null</code>, but may be empty. The type of each object is
    *    item (non-static) and the class of it may be derived from the
    *    <code>PSExtractorDef</code>.
    */
   public void setItemExtractorDefs(Iterator extractorDefs)
   {
      if (extractorDefs == null)
         throw new IllegalArgumentException("extractorDefs may not be null");

      m_itemExtractorDefs.clear();

      while (extractorDefs.hasNext())
      {
         PSExtractorDef def = (PSExtractorDef) extractorDefs.next();

         if (def.isStaticType())
            throw new IllegalArgumentException(
               "one of the extractorDefs is static type");

         m_itemExtractorDefs.addComponent(def);
      }
   }

   /**
    * Get field transformations definition.
    *
    * @return The field transformations definition, may by <code>null</code> if
    *    not contain one.
    */
   public PSFieldTransformationsDef getFieldTransDef()
   {
      return m_fieldTransDef;
   }

   /**
    * Set field transformations definition.
    *
    * @param fieldTransDef The to be set field transformations definition, may
    *    be <code>null</code> if want to remove the existing one.
    */
   public void setFieldTransDef(PSFieldTransformationsDef fieldTransDef)
   {
      m_fieldTransDef = fieldTransDef;
   }

   /**
    * Get item transformations definition.
    *
    * @return The item transformations definition, may by <code>null</code> if
    *    not contain one.
    */
   public PSItemTransformationsDef getItemTransDef()
   {
      return m_itemTransDef;
   }

   /**
    * Set item transformations definition.
    *
    * @param itemTransDef The to be set item transformations definition, may
    *    be <code>null</code> if want to remove the existing one.
    */
   public void setItemTransDef(PSItemTransformationsDef itemTransDef)
   {
      m_itemTransDef = itemTransDef;
   }

   /**
    * Get the log definition.
    *
    * @return The log definition, never <code>null</code>.
    */
   public PSLogDef getLogDef()
   {
      if (m_logDef == null)
         throw new IllegalStateException("m_logDef must not be null");

      return m_logDef;
   }

   /**
    * Sets the log definition.
    *
    * @param logDef, The log definition, never <code>null</code>.
    */
   public void setLogDef(PSLogDef logDef)
   {
      if (logDef == null)
         throw new IllegalStateException("m_logDef must not be null");
      m_logDef = logDef;
   }

   /**
    * Get the connection definition.
    *
    * @return The connection definition, never <code>null</code>.
    */
   public PSConnectionDef getConnectionDef()
   {
      if (m_conDef == null)
         throw new IllegalStateException("m_conDef must not be null");

      return m_conDef;
   }

   /**
    * Sets the connection definition.
    *
    * @param conDef, The connection definition, never <code>null</code>.
    */
   public void setConnectionDef(PSConnectionDef conDef)
   {
      if (conDef == null)
         throw new IllegalStateException("m_logDef must not be null");
      m_conDef = conDef;
   }



   /**
    * Get the error handling definition.
    *
    * @return The error handling definition, never <code>null</code>.
    */
   public PSErrorHandlingDef getErrorHandlingDef()
   {
      if (m_errorHandlingDef == null)
         throw new IllegalStateException("m_errorHandlingDef must not be null");

      return m_errorHandlingDef;
   }

   /**
    * Sets the error handling definition.
    *
    * @param errorDef, The error handling definition, never <code>null</code>.
    */
   public void setErrorHandlingDef(PSErrorHandlingDef errorDef)
   {
      if (errorDef == null)
         throw new IllegalStateException("errorDef must not be null");
      m_errorHandlingDef = errorDef;
   }

   /**
    * The XML node for a list of extractor definitions
    */
   final static protected String XML_EXTRACTORS = "Extractors";

   /**
    * A list of static extractors, zero or more
    * <code>PSStaticItemExtractorDef</code> objects, never <code>null</code>,
    * may be empty.
    */
   protected PSComponentList m_staticExtractorDefs = new PSComponentList(
      XML_EXTRACTORS, PSExtractorDef.XML_NODE_NAME, PSStaticItemExtractorDef.class, false);

   /**
    * A list of item extractors, zero or more <code>PSExtractorDef</code> or
    * its derived class objects, never <code>null</code>, may be empty.
    */
   protected PSComponentList m_itemExtractorDefs = new PSComponentList(
      XML_EXTRACTORS, PSExtractorDef.XML_NODE_NAME, PSItemExtractorDef.class, false);

   /**
    * The list of field transformation definitions, may be <code>null</code>
    * if not contain one.
    */
   protected PSFieldTransformationsDef m_fieldTransDef = null;

   /**
    * The list of item transformation definitions, may be <code>null</code>
    * if not contain one.
    */
   protected PSItemTransformationsDef m_itemTransDef = null;

   /**
    * The default log definition, initialized by the constructor, never
    * <code>null</code> or modified after that.
    * 
    * NOTE: there is a problem to serialize the PSLogDef (or the Element, got
    *       the following error when serialize PSLogDef: 
    *       java.io.NotSerializableException: 
    *          org.apache.xerces.util.DOMErrorHandlerWrapper
    *          
    *       make it transient for now.
    */
   protected transient PSLogDef m_logDef = null;

   /**
    * The connection definition, initialized by the constructor, never
    * <code>null</code> or modified after that.
    */
   protected PSConnectionDef m_conDef = null;

   /**
    * The error handling definition, initialized by the constructor, never
    * <code>null</code> or modified after that.
    */
   protected PSErrorHandlingDef m_errorHandlingDef = null;
}