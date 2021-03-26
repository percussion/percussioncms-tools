/******************************************************************************
 *
 * [ PSRefNodeHandler.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.handlers;

import com.percussion.client.PSObjectType;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.model.IPSDeclarativeNodeHandler;

import java.util.Collection;
import java.util.Properties;

/**
 * This handler is designed for nodes that represent references to design 
 * objects. It handles deletes by delegating to the first ancestor that is an
 * instance of {@link com.percussion.workbench.ui.handlers.PSLinkNodeHandler}.
 * Other operations are handled as if it was an actual instance of the object.  
 *
 * @author paulhoward
 */
public class PSRefNodeHandler extends PSDeclarativeNodeHandler
{

   public PSRefNodeHandler(Properties props, String iconPath,
         PSObjectType[] allowedTypes)
   {
      super(props, iconPath, allowedTypes);
   }

   /**
    * Delegate to closest ancestor that is of a matching type.
    */
   @Override
   public void handleDelete(Collection<PSUiReference> nodes)
   {
      IPSDeclarativeNodeHandler handler = getAncestorHandler(nodes.iterator()
            .next());
      if (handler == null)
         return;
      handler.handleDelete(nodes);
   }

   //see base class method for details
   @Override
   public boolean supportsDelete(PSUiReference node)
   {
      IPSDeclarativeNodeHandler handler = getAncestorHandler(node);
      return handler == null ? false : handler.supportsDelete(node);
   }

   /**
    * Checks the supplied node and its ancestors to find the closest one that 
    * has a handler that implements {@link PSLinkNodeHandler}.
    * 
    * @param node Assumed not <code>null</code>.
    * 
    * @return May be <code>null</code> if no ancestor node matches the criteria.
    */
   private IPSDeclarativeNodeHandler getAncestorHandler(PSUiReference node)
   {
      IPSDeclarativeNodeHandler handler = null;
      while (node.getParentNode() != null
            && !(node.getHandler() instanceof PSLinkNodeHandler))
      {
         node = node.getParentNode();
      }
      
      if (node != null)
         handler = node.getHandler();
      return handler;
   }
}
