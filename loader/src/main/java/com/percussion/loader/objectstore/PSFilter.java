/******************************************************************************
 *
 * [ PSFilter.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.loader.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;

import org.w3c.dom.Element;

/**
 * The class to encapsulate filter definition
 */
public class PSFilter extends PSNameValuePair implements java.io.Serializable
{
   /**
    * Constructs the object from a given name and value.
    *
    * @see  PSNameValuePair#(String name, String value)
    */
   public PSFilter(String name, String value)
   {
      super(name, value);
   }

   /**
    * Create this object from its XML representation
    *
    * @see  PSNameValuePair#(Element source)
    */
   public PSFilter(Element source) throws PSUnknownNodeTypeException
   {
      super(source);
   }

   /**
    * Get the XML node name
    * 
    * @return The XML node name, never <code>null</code> or empty
    */
   protected String getXmlNodeName()
   {
      return XML_NODE_NAME;
   }
   
   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXFilter";

   
}
