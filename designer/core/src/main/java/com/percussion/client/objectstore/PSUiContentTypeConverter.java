/******************************************************************************
 *
 * [ PSUiContentTypeConverter.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
/**
 * 
 */
package com.percussion.client.objectstore;

import com.percussion.cms.objectstore.PSContentType;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.webservices.transformation.converter.PSContentTypeConverter;
import org.apache.commons.beanutils.BeanUtilsBean;

/**
 * Overrides the Content type converter so that it creates
 * a <code>PSUiItemDefinition</code> object to give to the ui
 * client.
 */
public class PSUiContentTypeConverter extends PSContentTypeConverter
{
   
   /**
    * @param beanUtils
    */
   public PSUiContentTypeConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
   }

   @Override
   protected Object createDefinition(String appName, PSContentType typeDef,
      PSContentEditor ce)
   {
       return new PSUiItemDefinition(appName, typeDef, ce);
   }
   
   

}
