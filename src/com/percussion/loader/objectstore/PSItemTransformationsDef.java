/*[ PSItemTransformationsDef.java ]********************************************
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
 * Encapsulates a container for Transformations with a specific 
 * xml node name.
 */
public class PSItemTransformationsDef extends PSTransformationBucket
   implements java.io.Serializable
{
   /**
    * Default Constructor
    */
   public PSItemTransformationsDef()
   {
      super();
   }

   /**
    * see {@link #PSTransformationBucket} for details
    */
   public PSItemTransformationsDef(Element el)
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
    * XML node name or tag for a ItemTransformations element
    */
   final static public String XML_NODE_NAME = "PSXItemTransformationsDef";
}
