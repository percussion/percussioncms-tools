/******************************************************************************
 *
 * [ PSTemplateModel.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.models.impl.test;

import com.percussion.client.IPSPrimaryObjectType;

/**
 * Directly extends the real version because there are no methods.
 *
 * @author Andriy Palamarchuk
 */
public class PSTemplateModel
      extends com.percussion.client.models.impl.PSTemplateModel
{

   /**
    * See real version ctor for details.
    */
   public PSTemplateModel(String name, String description,
         IPSPrimaryObjectType supportedType)
   {
      super(name, description, supportedType);
   }
}
