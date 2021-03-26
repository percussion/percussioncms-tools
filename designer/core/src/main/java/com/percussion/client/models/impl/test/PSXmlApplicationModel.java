/******************************************************************************
 *
 * [ PSXmlApplicationModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.models.impl.test;

import com.percussion.client.IPSPrimaryObjectType;

/**
 * Test application model.
 *
 * @author Andriy Palamarchuk
 */
public class PSXmlApplicationModel
      extends com.percussion.client.models.impl.PSXmlApplicationModel
{
   public PSXmlApplicationModel(String name, String description,
         IPSPrimaryObjectType supportedType)
   {
      super(name, description, supportedType);
   }
}
