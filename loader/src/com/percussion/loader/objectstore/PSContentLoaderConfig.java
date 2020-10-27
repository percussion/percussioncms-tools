/**[ PSContentLoaderConfig ]***************************************************
 *
 * COPYRIGHT (c) 2002 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.

******************************************************************************/
package com.percussion.loader.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.loader.PSLoaderException;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Encapsulates configuration information for the content loader
 */
public class PSContentLoaderConfig extends PSContentLoaderDefs
   implements java.io.Serializable
{
   /**
    * Create this object from its XML representation
    *
    * @param source The source element.  See {@link #toXml(Document)} for
    * the expected format.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>source</code> is
    *    <code>null</code>.
    * @throws PSUnknownNodeTypeException <code>source</code> is malformed.
    * @throws PSLoaderException if any other error occurs.
    */
   public PSContentLoaderConfig(Element sourceNode)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      fromXml(sourceNode);
   }


   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * &lt;!ELEMENT PSXContentLoaderConfig (Selectors, Extractors,
    *    FieldTransformations?, ItemTransformations?, Loaders)>
    *
    * &lt;!ELEMENT Selectors (PSXContentSelectorDef+)>
    * </code></pre>
    *
    * See {@link PSLoaderComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      // validate all required elements
      if (! m_selectorDefs.isEmpty())
         throw new IllegalStateException("m_selectors must not be empty");

      // create the XML element for this object
      Element root = doc.createElement(XML_NODE_NAME);

      root.appendChild(m_conDef.toXml(doc));

      root.appendChild(m_selectorDefs.toXml(doc));

      root.appendChild(getAllExtractorDefs().toXml(doc));

      if ( m_fieldTransDef != null )
         root.appendChild(m_fieldTransDef.toXml(doc));

      if ( m_itemTransDef != null )
         root.appendChild(m_itemTransDef.toXml(doc));

      root.appendChild(m_loaderDefs.toXml(doc));

      root.appendChild(m_logDef.toXml(doc));

      root.appendChild(m_errorHandlingDef.toXml(doc));

      return root;
   }

   // see PSLoaderComponent#fromXml(Element)
   public void fromXml(Element sourceNode)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      validateElement(sourceNode, XML_NODE_NAME);


      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      Element conEl = getNextRequiredElement(tree, PSConnectionDef.XML_NODE_NAME,
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      m_conDef = new PSConnectionDef(conEl);

      // load the selectors
      tree.setCurrent(sourceNode);
      Element listEl = getNextRequiredElement(tree, XML_SELECTORS,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      loadSelectorDefs(listEl);

      // load the extractors
      listEl = getNextRequiredElement(tree, XML_EXTRACTORS,
         PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      loadExtractorDefs(listEl);

      // load both field & item transformers if there is any
      listEl = loadTransformations(tree);

      validateElement(listEl, XML_LOADERS);
      m_loaderDefs.fromXml(listEl);

      // load the log definition
      Element logEl = getNextRequiredElement(tree, PSLogDef.XML_NODE_NAME,
         PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      m_logDef = new PSLogDef(logEl);

      // load the error handling definition
      Element errHandleDefEl = getNextRequiredElement(tree,
         PSErrorHandlingDef.XML_NODE_NAME,
         PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      m_errorHandlingDef = new PSErrorHandlingDef(errHandleDefEl);
   }

   /**
    * Loads the list of selector definitions.
    *
    * @param selectorsEl The element of the selector definitions, assume not
    *    <code>null</code>
    *
    * @throws PSUnknownNodeTypeException if the XML is malformed
    * @throws PSLoaderException if any other error occurs.
    */
   private void loadSelectorDefs(Element selectorsEl)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      m_selectorDefs.clear();

      PSXmlTreeWalker tree = new PSXmlTreeWalker(selectorsEl);
      Element slctEl = getNextRequiredElement(tree,
         PSContentSelectorDef.XML_NODE_NAME,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);

      while (slctEl != null)
      {
         PSContentSelectorDef selectorDef = new PSContentSelectorDef(slctEl);

         // NO NEED FOR THIS: get the correct definition before add to the list
         //String pluginClass = selectorDef.getPlugInClass();
         //if (pluginClass.equals(PSFileSelectorDef.PLUGIN_CLASS))
         //   selectorDef = new PSFileSelectorDef(selectorDef);
         //else if (pluginClass.equals(PSListSelectorDef.PLUGIN_CLASS))
         //   selectorDef = new PSListSelectorDef(selectorDef);

         m_selectorDefs.addComponent(selectorDef);
         slctEl = tree.getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }
   }

   // see PSLoaderComponent#hashCode()
   public int hashCode()
   {
      return m_selectorDefs.hashCode() + m_staticExtractorDefs.hashCode() +
         m_itemExtractorDefs.hashCode() + m_fieldTransDef.hashCode() +
         m_itemTransDef.hashCode() + m_loaderDefs.hashCode() +
         m_logDef.hashCode() + m_errorHandlingDef.hashCode();
   }

   // see PSLoaderComponent#equals(Object)
   public boolean equals(Object obj)
   {
      if ( obj == null || (! (obj instanceof PSContentLoaderConfig)) )
      {
         return false;
      }
      else
      {
         PSContentLoaderConfig obj2 = (PSContentLoaderConfig) obj;

         return m_selectorDefs.equals(obj2.m_selectorDefs) &&
                m_staticExtractorDefs.equals(obj2.m_staticExtractorDefs) &&
                m_itemExtractorDefs.equals(obj2.m_itemExtractorDefs) &&
                m_fieldTransDef.equals(obj2.m_fieldTransDef) &&
                m_itemTransDef.equals(obj2.m_itemTransDef) &&
                m_loaderDefs.equals(obj2.m_loaderDefs) &&
                m_logDef.equals(obj2.m_logDef) &&
                m_errorHandlingDef.equals(obj2.m_errorHandlingDef);
      }
   }

   /**
    * Get the list of selector definitions that are defined in the configuration
    *
    * @return An iterator over zero or more <code>PSContentSelectorDef</code>
    *    objects. It may also contains objects that are derived from
    *    <code>PSContentSelectorDef</code>.
    */
   public Iterator getSelectorDefs()
   {
      return m_selectorDefs.getComponents();
   }

   /**
    * Get the list of content loader definitions which are defined in the config
    *
    * @return An iterator over zero or more <code>PSLoaderDef</code> objects.
    */
   public Iterator getLoaderDefs()
   {
      return m_loaderDefs.getComponents();
   }

   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXContentLoaderConfig";

   // Private constants for XML element name
   final private String XML_SELECTORS = "Selectors";
   final protected String XML_LOADERS = "Loaders";

   /**
    * A list of selectors, zero or more <code>PSContentSelectorDef</code>
    * objects, never <code>null</code>, may be empty.
    */
   protected PSComponentList m_selectorDefs = new PSComponentList(XML_SELECTORS,
      PSContentSelectorDef.XML_NODE_NAME, PSContentSelectorDef.class, false);

   /**
    * A list of loaders, zero or more <code>PSLoaderDef</code>
    * objects, never <code>null</code>, may be empty.
    */
   protected PSComponentList m_loaderDefs = new PSComponentList(XML_LOADERS,
      PSLoaderDef.XML_NODE_NAME, PSLoaderDef.class);
}