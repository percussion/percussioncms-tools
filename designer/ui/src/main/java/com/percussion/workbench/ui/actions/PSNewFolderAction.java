/******************************************************************************
 *
 * [ PSNewFolderAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.actions;

import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.model.PSDesignObjectHierarchy;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * This class implements the behavior of creating a virtual folder in a
 * declarative view. It is enabled if a single node is selected and that node
 * supports USER_FILE:WORKBENCH_FOLDER types for children.
 *
 * @author paulhoward
 */
public class PSNewFolderAction extends PSBaseSelectionListenerAction
{
   /**
    * The id of this action.
    */
   public static final String ID = PSWorkbenchPlugin.getPluginId()
         + ".NewFolderAction"; //$NON-NLS-1$

   /**
    * Ctor
    * @param provider Supplied to super ctor. Never <code>null</code>.
    */
   public PSNewFolderAction(ISelectionProvider provider)
   {
      super(PSMessages.getString("PSNewFolderAction.action.label"), provider); //$NON-NLS-1$
      setImageDescriptor(PSUiUtils.getImageDescriptor("icons/newfolder16.gif"));
      String msg = PSMessages.getString("PSNewFolderAction.action.tooltip"); //$NON-NLS-1$
      setToolTipText(msg);
      setDescription(msg);
   }

   /**
    * @inheritDoc
    * Gets the selection using the site supplied in the ctor. If this is an
    * <code>IStructuredSelection</code>, a new folder is added to the current
    * node.
    */
   @Override
   public void run()
   {
      try
      {
         ms_logger.debug("PSNewFolderAction ran: " + isEnabled());
         if (!isEnabled())
            return;
         IStructuredSelection ss = getStructuredSelection();

         PSUiReference node = (PSUiReference) ss.getFirstElement();
         PSDesignObjectHierarchy hierarchyModel = PSDesignObjectHierarchy
               .getInstance();
         hierarchyModel.createFolder(node, null);
         //todo - OK for release - put into edit mode
      }
      catch (Exception e)
      {
         String title = PSMessages
               .getString("PSNewFolderAction.error.folderCreate.title");
         String msg = PSMessages
               .getString("PSNewFolderAction.error.folderCreate.message");
         PSWorkbenchPlugin.handleException("Folder create", title, msg, e);
      }
   }

   /**
    * @inheritDoc
    * The selection must contain 1 item and it must be a {@link PSUiReference}
    * node that supports folders for children.
    */
   @Override
   protected boolean updateSelection(IStructuredSelection sel)
   {
      //as documented by super class
      if (!super.updateSelection(sel))
         return false;
      
      if (sel.size() != 1
            || (sel.size() == 1 
                  && !(sel.getFirstElement() instanceof PSUiReference)))
      {
         return false;
      }
      
      PSUiReference node = (PSUiReference) sel.getFirstElement();

      /* We don't allow creating new folders on the local file system. The
       * local file system view is only present as a target for copying files
       * from the CMS file system on the Rx server.
       */
      boolean isLocalFolderType = node.getObjectType() != null 
            && node.getObjectType().equals(
               new PSObjectType(PSObjectTypes.LOCAL_FILE, 
                     PSObjectTypes.FileSubTypes.FOLDER)); 
      return (PSDesignObjectHierarchy.isUserFileHierarchy(node) 
            || PSDesignObjectHierarchy.isHierarchyModel(node))
            && !isLocalFolderType;
   }
   
   /**
    * The logging target for all instances of this class. Never
    * <code>null</code>.
    */
   private static Logger ms_logger = LogManager.getLogger(PSNewFolderAction.class);
}
