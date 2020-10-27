/******************************************************************************
 *
 * [ PSPropertyCatalogerFactory.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.catalogs.property;

import com.percussion.workbench.ui.views.properties.IPSMultiPropertySource;

/**
 * @version 1.0
 * @created 03-Sep-2005 4:43:57 PM
 */
public class PSPropertyCatalogerFactory {

   public IPSMultiPropertySource m_IPSMultiPropertySource;

   public PSPropertyCatalogerFactory(){

   }

   /**
    * 
    * @param datatype
    */
   public IPSMultiPropertySource getCataloger(String datatype){
      return null;
   }

}
