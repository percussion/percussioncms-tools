/*[ PSFieldTransformationsDef.java ]*******************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.loader.PSLoaderException;

import org.w3c.dom.Element;

/**
 * Encapsulates a container for Transformations as a specific
 * named xml node.
 * 
 * @see {@link #PSTransformationBucket} for details.
 */
public class PSFieldTransformationsDef extends PSTransformationBucket
   implements java.io.Serializable
{
   /**
    * Default Constructor
    */
   public PSFieldTransformationsDef()
   {
      super();
   }

   /**
    * see {@link #PSTransformationBucket} for details
    */
   public PSFieldTransformationsDef(Element el)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      super(el);
   }

   /**
    * Overriden from base class to provide an xml node 
    * name.
    */
   public String getXmlNodeName()
   {
      return XML_NODE_NAME;
   }

   /**
    * Set the component list, may be override by derived classes if needed
    */
   protected void setComponentList()
   {
      m_transformations = new
         PSComponentList(
         getXmlNodeName(),
         PSFieldTransformationDef.XML_NODE_NAME,
         PSFieldTransformationDef.class);
   }
   
   /**
    * XML node name or tag for a FieldTransformations element
    */
   final static public String XML_NODE_NAME = "PSXFieldTransformationsDef";
}
