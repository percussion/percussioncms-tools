/*[ PSLoaderComponent.java ]***************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.loader.objectstore;

import com.percussion.cms.IPSConstants;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.loader.PSLoaderException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.lang.reflect.Constructor;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * The PSLoaderComponent (abstract) class defines required public methods for
 * all component level object store objects. It also contains helper methods.
 */
public abstract class PSLoaderComponent implements Cloneable
{
   /**
    * This method is called to create an XML element node with the
    * appropriate format for the given object. An element node may contain a
    * hierarchical structure, including child objects. The element node can
    * also be a child of another element node.
    *
    * @param doc The document to use to create the element, may not be
    * <code>null</code>.
    *
    * @return the newly created XML element node, never <code>null</code>
    *
    * @throws IllegalArgumentException if <code>doc</code> is <code>null</code>.
    */
   public abstract Element toXml(Document doc);

   /**
    * This method is called to populate an object from its XML representation.
    * An element node may contain a hierarchical structure, including child
    * objects. The element node can also be a child of another element node.
    *
    * @param sourceNode the XML element node to populate from not
    * <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException if the XML element node does not
    *    represent a type supported by the class.
    * @throws PSLoaderException if any other error occurs.
    */
   public abstract void fromXml(Element sourceNode)
      throws PSUnknownNodeTypeException, PSLoaderException;

   /**
    * Returns a hash code value for the object. See
    * {@link java.lang.Object#hashCode() Object.hashCode()} for more info.
    */
   public abstract int hashCode();

   /**
    * Determines if this object is equal to another.  See
    * {@link java.lang.Object#equals(Object) Object.equals()} for more info.
    */
   public abstract boolean equals(Object obj);

   /**
    * Copy or clone the object from another object. This is not a supported
    * operation unless override by derived classes. This signature made it
    * possible to support the <code>copyFrom</code> method in the future.
    *
    * @param obj The to be copied or cloned object.
    */
   public void copyFrom(Object obj)
   {
      throw new IllegalStateException("copyFrom() is not supported operation");
   }

   /**
    * Implements <code>Cloneable</code> interface.
    * 
    * @return Deep cloned object, never <code>null</code>.
    */
   public Object clone()
   {
      try
      {
         Constructor compCtor = this.getClass().getConstructor( new Class[]
            { Element.class });
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         Element element = toXml(doc);
         Object comp = compCtor.newInstance(
            new Object[] {element} );
         
         return comp;
      }
      catch (Exception e)
      {
         throw new RuntimeException("Unexpected exception: " + e.toString());
      }
   }
   
   /**
    * Utility method to get a required attibute value, validating that it is
    * not <code>null</code> or empty.
    *
    * @param source Element to get the attribute from, may not be
    * <code>null</code>.
    * @param attName The name of the attribute to get, may not be
    * <code>null</code> or empty
    *
    * @return The attribute value, never <code>null</code> or empty.
    *
    * @throws PSUnknownNodeTypeException If the specified attribute cannot be
    * found with a non-empty value.
    */
   public static String getRequiredAttribute(Element source, String attName)
      throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      if (attName == null || attName.trim().length() == 0)
         throw new IllegalArgumentException("attName may not be null or empty");

      String val = source.getAttribute(attName);
      if (val == null || val.trim().length() == 0)
      {
         Object[] args = {source.getTagName(), attName, "null or empty"};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }

      return val;
   }

   /**
    * Get next element from the <code>tree</code> and <code>flags</code>,
    * validating the parameters and the retrieved element that it is not
    * <code>null</code>.
    *
    * @param tree The tree to get the next element from, may not be
    *    <code>null</code>.
    * @param nodeName The expected XML node name, may not be <code>null</code>
    *    or empty.
    * @param flags The appropriate <code>PSXmlTreeWalker.GET_NEXT_xxx</code>
    *    flags.
    *
    * @return The retrieved and validated element, never <code>null</code>.
    *
    * @throws IllegalArgumentException if there is an invalid parameters.
    * @throws PSUnknownNodeTypeException if fail to retrieve next element from
    * the <code>tree</code>.
    */
   public static Element getNextRequiredElement(PSXmlTreeWalker tree,
      String nodeName, int flags) throws PSUnknownNodeTypeException
   {
      if (tree == null)
         throw new IllegalArgumentException("tree may not be null");

      if (nodeName == null || nodeName.trim().length() == 0)
         throw new IllegalArgumentException(
            "nodeName may not be null or empty");

      Element element = tree.getNextElement(nodeName, flags);

      if ( element == null)
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, nodeName);
      }

      if (! element.getNodeName().equals(nodeName))
      {
         Object[] args = { nodeName, element.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      return element;
   }

   /**
    * Validating a XML element.
    *
    * @param sourceNode The to be validated XML element, may not be
    *    <code>null</code>
    * @param xmlNodeName The expected XML node name, may not be
    *    <code>null</code> or empty
    *
    * @throws PSUnknownNodeTypeException if the XML node of element is not
    *    <code>xmlNodeName</code>.
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   protected void validateElement(Element sourceNode, String xmlNodeName)
      throws PSUnknownNodeTypeException
   {
      if ( sourceNode == null)
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, xmlNodeName);
      }

      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode should not be null");
      if (xmlNodeName == null || xmlNodeName.trim().length() == 0)
         throw new IllegalArgumentException(
            "xmlNodeName may not be null or empty");

      if (!xmlNodeName.equals(sourceNode.getNodeName()))
      {
         Object[] args = { xmlNodeName, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
   }

   /**
    * XML node name or tag for a properties element
    */
   final static public String XML_NODE_PROPERTIES = "Properties";

   /**
    * XML node name or tag for a field properties element
    */
   final static public String XML_NODE_FIELDPROPERTIES = "FieldProperties";

   /**
    * XML node name or tag for a filters element
    */
   final static public String XML_NODE_FILTERS = "Filters";

   /**
    * XML node name or tag for a mimetypes element
    */
   final static public String XML_NODE_MIMETYPES = "MimeTypes";

   /**
    * XML node name or tag for a Actions element
    */
   final static public String XML_NODE_ACTIONS = "Actions";

   /**
    * XML string to represent a <code>true boolean</code> value
    */
   final static public String XML_TRUE = IPSConstants.BOOLEAN_TRUE;

   /**
    * XML string to represent a <code>false boolean</code> value
    */
   final static public String XML_FALSE = IPSConstants.BOOLEAN_FALSE;

   /**
    * XML string to represent "yes"
    */
   final static public String YES_STRING = IPSConstants.BOOLEAN_TRUE;

   /**
    * XML string to represent "no"
    */
   final static public String NO_STRING = IPSConstants.BOOLEAN_FALSE;
}
