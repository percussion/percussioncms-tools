/******************************************************************************
 *
 * [ PSContentTypesNodeHandler.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.handlers;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelChangedEvent;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSModelListener;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.editors.dialog.PSCTypeCopyNamesDialog;
import com.percussion.workbench.ui.model.IPSDropHandler;
import com.percussion.workbench.ui.model.PSDesignObjectHierarchy;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Very thin class that just supplies the proper path to the base class.
 *
 * @author paulhoward
 */
public class PSContentTypesNodeHandler extends PSDropNodeHandler
{
   /**
    * Required by framework. See base for param description.
    */
   public PSContentTypesNodeHandler(Properties props, String iconPath,
         PSObjectType[] allowedTypes)
   {
      super(props, iconPath, allowedTypes);
      
      try
      {
         IPSCmsModel configurationsModel = PSCoreFactory.getInstance()
               .getModel(PSObjectTypes.CONFIGURATION_FILE);
         
         //todo - OK for release- decouple from name
         configurationsModel.addListener(new NavPropertiesListener("/Content Types"),
               PSModelChangedEvent.ModelEvents.MODIFIED.getFlag());
         ms_dropHandler = (new PSNavigationObjectNodeHandler(props, iconPath,
               allowedTypes)).new NavObjectDropHandler();
      }
      catch (PSModelException e)
      {
         // should never happen
         throw new RuntimeException(e);
      }
   }

   //see base class method for details
   @Override
   public void handlePaste(PSUiReference parent, Map<Transfer, Object> cbData)
   {
      super.handlePaste(parent, cbData);
      //need to clear all apps so dependent code will see the app for the new 
      // ctype
      getModel(PSObjectTypes.XML_APPLICATION).flush(null);
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
      return queryCloneNames(clones, resultNames);
   }
   
   /**
    * Does the work for {@link #getCloneNames(List, List)} so it can be shared.
    * See that method for param/return descriptions.
    */
   static List<IPSReference> queryCloneNames(List<IPSReference> clones,
         List<String> resultNames)
   {
      Collection<String> notAllowedNames = new ArrayList<String>();
      try
      {
         for (IPSReference ref : getModel(PSObjectTypes.CONTENT_TYPE).catalog())
         {
            notAllowedNames.add(ref.getName());
         }
      }
      catch (PSModelException e)
      {
         // ignore, hope for the best; any dupes will get caught later
      }
      
      String[] existingNames = new String[clones.size()];
      for (int i = 0; i < clones.size(); i++)
      {
         existingNames[i] = clones.get(i).getName();
      }
      
      PSCTypeCopyNamesDialog dialog = 
         new PSCTypeCopyNamesDialog(PSUiUtils.getShell(), existingNames, 
               notAllowedNames);
      int status = dialog.open();
      if(status == Dialog.OK)
      {
         String[] userNames = dialog.getCopyNames();
         assert (userNames.length == clones.size());
         //go from end to beginning so removals work correctly
         for(int i = userNames.length-1; i >= 0; i--)
         {
            if (userNames[i] == null)
               clones.remove(i);
            else
               resultNames.add(userNames[i]);
         }
         assert (resultNames.size() == clones.size());
      }
      else
         clones.clear();
      return clones;
   }

   @Override
   public IPSDropHandler getDropHandler()
   {
      return ms_dropHandler;
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

   /**
    * This class causes a specified node to be refreshed whenever changes to
    * navigation.properties are seen.
    *
    * @author paulhoward
    */
   protected class NavPropertiesListener implements IPSModelListener
   {
      /**
       * Only ctor.
       * 
       * @param path When an event is received, the node identified by the
       * supplied path is requested and updated. Never <code>null</code> or
       * empty. If a node is not found at this location, that is logged and the
       * method returns without refreshing anything. 
       */
      public NavPropertiesListener(String path)
      {
         if (null == path)
         {
            throw new IllegalArgumentException("path cannot be null");  
         }
         m_path = path;
      }
      
      //see base class method for details
      public void modelChanged(PSModelChangedEvent event)
      {
         try
         {
            for (IPSReference ref : event.getSource())
            {
               if (ref.getObjectType().equals(PSObjectTypeFactory.getType(
                  PSObjectTypes.CONFIGURATION_FILE,
                  PSObjectTypes.ConfigurationFileSubTypes.NAVIGATION_PROPERTIES)))
               {
                  PSDesignObjectHierarchy viewModel = 
                     PSDesignObjectHierarchy.getInstance();
                  PSUiReference node = viewModel.getNode(m_path);
                  if (node != null)
                     viewModel.refresh(node);
                  else
                  {
                     ms_logger.info(MessageFormat.format(
                           "Couldn't find node for path ''{0}''", m_path));
                  }
                  break;
               }
            }
         }
         catch (PSModelException e)
         {
            // should never happen as we are asking for a fixed node
            throw new RuntimeException(e);
         }
      }
      
      /**
       * Used to find the node to refresh when a change in the
       * navigation.properties file is received. Never <code>null</code> or
       * empty after ctor.
       */
      private final String m_path;      
   }

   /**
    * The handler that manages the drop operations. Created lazily by the
    * {@link #getDropHandler()} method, then never modified.
    */
   private static IPSDropHandler ms_dropHandler;
   
   /**
    * The logging target for all instances of this class. Never
    * <code>null</code>.
    */
   private static Logger ms_logger = LogManager
         .getLogger(PSContentTypesNodeHandler.class);
}
