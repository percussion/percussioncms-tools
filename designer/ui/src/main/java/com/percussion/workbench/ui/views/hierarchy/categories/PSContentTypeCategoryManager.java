/******************************************************************************
 *
 * [ PSContentTypeCategoryManager.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.views.hierarchy.categories;

import com.percussion.client.PSModelException;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.workbench.ui.model.IPSHomeNodeManager;

import java.util.Map;

/**
 * todo
 * Class description
 *
 * @author paulhoward
 */
public class PSContentTypeCategoryManager implements IPSHomeNodeManager
{
   //see interface
   public boolean isHomeNode(Map<String,String> props, Object data)
   {
      // TODO Auto-generated method stub
      if ( null == data)
      {
         throw new IllegalArgumentException("data cannot be null");  
      }
      PSItemDefinition def = (PSItemDefinition) data;
      
      String cat = props.get("category");
      if (cat == null)
         cat = "";
      
      if (cat.equals("navigation"))
      {
         //todo
         return false;
      }
      else if (cat.equals("system"))
      {
         //todo
         return false;
      }
      
      return true;
   }

   //see interface
   public void modifyForHomeNode(Map<String,String> props, Object data) throws PSModelException
   {
      // TODO Auto-generated method stub
      
   }
}
