/******************************************************************************
*
* [ PSUiContentEditorDefinitionConverter.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.client.objectstore;

import com.percussion.design.objectstore.PSUnknownDocTypeException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.webservices.transformation.converter.PSContentEditorDefinitionConverter;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.w3c.dom.Document;

/**
 * Overrides the Content editor def converter so that it creates
 * a <code>PSUiContentEditorSharedDef</code> object to give to the ui
 * client.
 */
public class PSUiContentEditorDefinitionConverter
   extends
      PSContentEditorDefinitionConverter
{

   /*
    * @see com.percussion.webservices.transformation.converter.
    * PSContentEditorDefinitionConverter#PSContentEditorDefinitionConverter
    * (BeanUtilsBean)
    */
   public PSUiContentEditorDefinitionConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
   }

   /* 
    * @see com.percussion.webservices.transformation.converter.
    * PSContentEditorDefinitionConverter#createSharedDefinition(
    * org.w3c.dom.Document)
    */
   @Override
   protected Object createSharedDefinition(Document doc) 
      throws PSUnknownDocTypeException, PSUnknownNodeTypeException
   {
      return new PSUiContentEditorSharedDef(doc);      
   }
   
   

}
