/******************************************************************************
 *
 * [ PSNavigationObjectNodeHandler.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.handlers;

import com.percussion.client.IPSReference;
import com.percussion.client.PSObjectType;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.model.IPSDropHandler;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.dnd.DND;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * This class is used with folders in hierarchies that contain objects that may
 * be kept in a special 'Navigation' node due to their presence in the
 * navigation.properties file. If such an object is dropped on a node with this
 * handler, a message will be displayed to the user that the object can't be
 * moved directly. To move it, the navigation configuration must be modified.
 * 
 * @author paulhoward
 */
public class PSNavigationObjectNodeHandler extends PSDeclarativeNodeHandler
{
   /**
    * Required by framework. See base class for param description.
    */
   public PSNavigationObjectNodeHandler(Properties props, String iconPath,
         PSObjectType[] allowedTypes)
   {
      super(props, iconPath, allowedTypes);
   }

   /**
    * Asks the user for new names. 
    * 
    * @return Only those clones for which the user supplies a new name.
    */
   @Override
   protected List<IPSReference> getCloneNames(List<IPSReference> clones,
         List<String> resultNames)
   {
      return PSContentTypesNodeHandler.queryCloneNames(clones, resultNames);
   }
   
   /**
    * If any of the dropped nodes are navigation objects, they are skipped and
    * a message is displayed to the user. 
    * <p>
    * This class does not depend on the outer class.
    *
    * @author paulhoward
    */
   protected class NavObjectDropHandler extends NodeDropHandler
   {
      /**
       * If the dropped object comes from the Navigation node, then a message
       * is displayed, otherwise it is passed to the base class to handle.
       * See that method for param descriptions.
       */
      @SuppressWarnings("unchecked") //data param
      @Override
      public boolean performDrop(PSUiReference target, int op, Object data)
      {
         if (!(data instanceof Collection))
            return false;
         Collection<PSUiReference> nodes = new ArrayList<PSUiReference>();
         nodes.addAll((Collection<PSUiReference>) data);
         boolean found = false;
         if (op == DND.DROP_MOVE)
         {
            for (Iterator<PSUiReference> iter = nodes.iterator(); iter.hasNext();)
            {
               PSUiReference node = iter.next();
               PSUiReference parent = node.getParentNode();
               //todo - OK for release - remove dependency on folder name
               if (parent != null && parent.getReference() == null
                     && parent.getName().equalsIgnoreCase("Navigation"))
               {
                  iter.remove();
                  found = true;
               }
            }
         }
         if (found)
         {
            String title = PSMessages
                  .getString("PSNavigationObjectNodeHandler.cantMoveNavNodes.title");
            String msg = PSMessages
                  .getString("PSNavigationObjectNodeHandler.cantMoveNavNodes.message");
            MessageDialog.openWarning(PSUiUtils.getShell(), title, msg);
         }
         if (!nodes.isEmpty())
            return super.performDrop(target, op, nodes);
         return false;
      }
   }

   @Override
   public IPSDropHandler getDropHandler()
   {
      if (ms_dropHandler == null)
         ms_dropHandler = new NavObjectDropHandler(); 
      return ms_dropHandler;
   }

   /**
    * The handler that manages the drop operations. Created lazily by the
    * {@link #getDropHandler()} method, then never modified.
    */
   private static IPSDropHandler ms_dropHandler;
}
