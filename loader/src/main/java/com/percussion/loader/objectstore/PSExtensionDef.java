/*[ PSExtensionDef.java ]******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.loader.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;

import org.w3c.dom.Element;

/**
 * @see {@link #PSSingleAttElem} for description
 */
public class PSExtensionDef extends PSSingleAttElem 
   implements java.io.Serializable
{
   /**
    * @see {@link #PSSingleAttElem} for description
    */
   public PSExtensionDef(String strName)
   {
      super(strName);
   }
   
   /**
    * @see {@link #PSSingleAttElem} for description
    */
   public PSExtensionDef(Element elem)
      throws PSUnknownNodeTypeException
   {
      super(elem);
   }

   /**
    * @see {@link #PSSingleAttElem} for description
    */
   protected String getXmlNodeName()
   {
      return this.XML_NODE_NAME;
   }
   
   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXExtensionDef";
}
