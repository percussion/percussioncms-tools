/*[ PSFieldTransformationDef.java ]********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.loader.PSLoaderException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Encapsulates a transformation definition.
 */
public class PSFieldTransformationDef extends PSTransformationDef
   implements java.io.Serializable
{
   /**
    * Constructs the object from a given name and its class name.
    *
    * @param name The name of the object, may not be <code>null</code>
    *    or empty.
    * @param pluginClass A fully qualified class name of the specified
    *    plugin selector, may not be <code>null</code> or empty.
    * @param targetField The target field of the object, may not be
    *    <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSFieldTransformationDef(String name, String pluginClass,
      String targetField)
   {
      super(name, pluginClass);

      if (targetField == null || targetField.trim().length() == 0)
         throw new IllegalArgumentException(
            "targetField may not be null or empty");

      m_targetField = targetField;
   }

   /**
    * Create this object from its XML representation
    *
    * @param source The source element.  See {@link #toXml(Document)} for
    * the expected format.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>source</code> is
    * <code>null</code>.
    * @throws PSUnknownNodeTypeException <code>source</code> is malformed.
    */
   public PSFieldTransformationDef(Element source)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      super(source);
   }
   
   /**
    * Get the target field.
    * 
    * @return The target field, never <code>null</code> or empty.
    */
   public String getTargetField()
   {
      return m_targetField;
   }
   
   /**
    * Set the target field.
    * 
    * @param targetField The to be set target field, may not be 
    *    <code>null</code> or empty.
    */
   public void setTargetField(String targetField)
   {
      if (targetField == null || targetField.trim().length() == 0)
         throw new IllegalArgumentException(
            "targetField may not be null or empty");
            
      m_targetField = targetField;
   }

   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * &lt;!ELEMENT PSTransformationDef (PSXParamDefs*)>
    * &lt;!ATTLIST PSTransformationDef
    *     name CDATA #REQUIRED
    *     class CDATA #REQUIRED
    *     targetField CDATA #REQUIRED
    * >
    * &lt;!ELEMENT PSXParamDef (Description)>
    * &lt;!ATTLIST PSXParamDef
    *     name CDATA #REQUIRED
    *     type CDATA #REQUIRED
    * >
    * &lt;!ELEMENT Description (#PCDATA)>
    * </code></pre>
    *
    * @throws IllegalArgumentException If <code>doc</code> is <code>null</code>
    *
    * See {@link PSLoaderComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element root = doc.createElement(getXmlNode());
      root.setAttribute(XML_ATTR_NAME, m_strName);
      root.setAttribute(XML_ATTR_CLASS, m_strPluginClass);
      root.setAttribute(XML_ATTR_TARGETFIELD, m_targetField);

      if (!m_parameters.isEmpty())
         root.appendChild(m_parameters.toXml(doc));

      return root;
   }

   /**
    * @see PSLoaderComponent#fromXml(Element)
    */
   public void fromXml(Element sourceNode)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      super.fromXml(sourceNode);

      m_targetField = getRequiredAttribute(sourceNode, XML_ATTR_TARGETFIELD);

   }

   /**
    * PSLoaderComponent#hashCode()
    */
   public int hashCode()
   {
      return super.hashCode() + m_targetField.hashCode();
   }

   /**
    * @see PSLoaderComponent#equals(Object)
    */
   public boolean equals(Object obj)
   {
      if ((obj != null) && (obj instanceof PSFieldTransformationDef))
      {
         PSFieldTransformationDef obj2 = (PSFieldTransformationDef) obj;

         return m_targetField.equals(obj2.m_targetField) && super.equals(obj2);
      }
      else
      {
         return false;
      }
   }

   /**
    * Get the XML node name for the current object.
    * 
    * @return XML node name, never <code>null</code> or empty.
    */
   protected String getXmlNode()
   {
      return XML_NODE_NAME;
   }
      
   /**
    * The XML node name of the component list, initialized by constructor,
    * never <code>null</code> or empty after that.
    */
   final public static String XML_NODE_NAME = "PSXFieldTransformationDef";
   
   /**
    * The target field, initialized by constructor, never <code>null</code>
    * or empty after that.
    */
   private String m_targetField;
   
   // Private constants for XML attribute and element name
   final static protected String XML_ATTR_TARGETFIELD = "targetField";
}
