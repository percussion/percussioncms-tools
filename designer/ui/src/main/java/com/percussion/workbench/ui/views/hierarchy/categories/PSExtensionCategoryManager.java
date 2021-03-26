/******************************************************************************
 *
 * [ PSExtensionCategoryManager.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.views.hierarchy.categories;

import com.percussion.client.PSModelException;
import com.percussion.extension.PSExtensionDef;
import com.percussion.workbench.ui.model.IPSHomeNodeManager;
import org.apache.commons.lang.StringUtils;

import java.util.Iterator;
import java.util.Map;

/**
 * This class knows which sub-category to place an extension into. It expects
 * that the /Extensions tree has a child node for every interface type.
 * 
 * @author paulhoward
 */
public class PSExtensionCategoryManager implements IPSHomeNodeManager
{
   // see base class method for details
   public boolean isHomeNode(Map<String, String> props, Object data)
   {
      String subType = props.get("extensionSubType");
      if (StringUtils.isEmpty(subType))
      {
         throw new RuntimeException("extensionSubType must have been set "
            + "in the declarative hierarchy definition");
      }
      PSExtensionDef def = (PSExtensionDef) data;
      String handlerName = def.getRef().getHandlerName();
      boolean isJavaScript = handlerName
         .equalsIgnoreCase(HANDLERNAME_JAVASCRIPT);
      if (isJavaScript && subType.equalsIgnoreCase(HANDLERNAME_JAVASCRIPT))
         return true;

      // Must be non javascript ones
      Iterator<String> iterfaces = def.getInterfaces();
      while (iterfaces.hasNext())
      {
         String iface = iterfaces.next();
         if (iface.equals(subType))
            return true;
      }
      return false;
   }

   // see base class method for details
   public void modifyForHomeNode(Map<String, String> props, Object data)
      throws PSModelException
   {
      // fixme Auto-generated method stub

   }

   /**
    * Name of the javascript handler for case insensitive comparison.
    */
   private static final String HANDLERNAME_JAVASCRIPT = "javascript";
}
