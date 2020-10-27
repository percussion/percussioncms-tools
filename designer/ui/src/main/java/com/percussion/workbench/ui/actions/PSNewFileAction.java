/******************************************************************************
 *
 * [ PSNewFileAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.actions;

import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSDuplicateNameException;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.workbench.ui.IPSEditorFactory;
import com.percussion.workbench.ui.PSEditorRegistry;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.model.PSDesignObjectHierarchy;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PSNewFileAction extends PSBaseSelectionListenerAction
{
   /**
    * The id of this action.
    */
   public static final String ID = PSWorkbenchPlugin.getPluginId()
         + ".NewRxFileAction"; //$NON-NLS-1$

   /**
    * Ctor
    * @param page Context for creating new files. If <code>null</code>, an 
    * attempt is made to find a page when the action is first activated. This
    * could fail.
    * @param provider Supplied to super ctor. Never <code>null</code>.
    */
   public PSNewFileAction(IWorkbenchPage page, ISelectionProvider provider)
   {
      super(PSMessages.getString("PSNewFileAction.action.label"), provider); //$NON-NLS-1$
      m_page = page;
      setImageDescriptor(PSUiUtils.getImageDescriptor("icons/newfile16.gif"));
      String msg = PSMessages.getString("PSNewFileAction.action.tooltip"); //$NON-NLS-1$
      setToolTipText(msg);
      setDescription(msg);
   }

   /**
    * @inheritDoc
    * Gets the selection using the window supplied in the ctor. If this is an
    * <code>IStructuredSelection</code> with 1 entry, the branch is traversed
    * upwards until a node that is a home node is found. The object type for
    * this home node is used as the type of this action.
    */
   public void run()
   {
      ms_logger.debug("PSNewFileAction ran: " + isEnabled());
      if (!isEnabled())
         return;
      PSUiReference node = getSelectedNode();
      if (node == null)
         return;
      
      final Collection<String> invalidNames = new ArrayList<String>();
      try
      {
         List<PSUiReference> children = 
            PSDesignObjectHierarchy.getInstance().getChildren(node);
         for (PSUiReference tmpNode : children)
         {
            invalidNames.add(tmpNode.getName().toLowerCase());
         }
      }
      catch (PSModelException e)
      {
         //ignore, let server fail it
      }
      
      boolean done = false;
      String filename = null;
      do
      {
         try
         {
            InputDialog dlg = new InputDialog(PSUiUtils.getShell(), 
                  PSMessages.getString("PSNewFileAction.newFileDialog.title"), 
                  PSMessages.getString("PSNewFileAction.newFileDialog.message",
                        new Object[] { node.getPath() }), filename, new IInputValidator()
            {
               //see interface
               public String isValid(String newText)
               {
                  if (PSCoreUtils.isValidHierarchyName(newText))
                  {
                     if (invalidNames.contains(newText.toLowerCase()))
                     {
                        return PSMessages.getString(
                           "PSNewFileAction.newFileDialog.validator.nameAlreadyExists");
                     }
                     return null;
                  }
                  return PSMessages.getString(
                        "PSNewFileAction.newFileDialog.validator.invalidCharacters");
               }
               
            });
            dlg.open();
            filename = dlg.getValue();
            if (StringUtils.isBlank(filename))
               return;
            
            //fixme - remove debug code
            System.out.println(dlg.getValue());
            
            PSUiReference fileNode = PSDesignObjectHierarchy.getInstance()
                  .createFile(node, filename);
            
            done = true;
            IPSEditorFactory factory = PSEditorRegistry.getInstance()
                  .findEditorFactory(fileNode.getObjectType());
            if (factory != null)
            {
               factory.openEditor(getPage(), fileNode.getReference());
            }
         }
         catch (PartInitException e)
         {
            // fixme  show message
            e.printStackTrace();
         }
         catch (PSModelException e)
         {
            // fixme Auto-generated catch block
            e.printStackTrace();
         }
         catch (PSMultiOperationException e)
         {
            // fixme Auto-generated catch block
            if (e.getResults()[0] instanceof PSDuplicateNameException)
            {
               invalidNames.add(filename.toLowerCase());
            }
            else
            {
               e.printStackTrace();
            }
         }
         catch (Exception e)
         {
            // fixme Auto-generated catch block
            e.printStackTrace();
         }
      }
      while(!done);
   }

   //fixme
   private IWorkbenchPage getPage()
      throws PartInitException
   {
      // fixme Auto-generated method stub
      if (m_page != null)
         return m_page;
      
      IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
      if (win != null)
      {
         m_page = win.getActivePage();
         if (m_page != null)
            return m_page;
      }
      throw new PartInitException("Could not find workbench page.");
   }
   
   /**
    * @inheritDoc
    * The selection must contain 1 item and it must be a {@link PSUiReference}
    * node that is a home node, or has an ancestor that is a home node to be
    * enabled.
    */
   @Override
   protected boolean updateSelection(IStructuredSelection sel)
   {
      if (sel == null)
         return false;

      //as documented by super class
      if (!super.updateSelection(sel))
         return false;
      
      //element must be a design object
      if (!(sel.getFirstElement() instanceof PSUiReference))
         return false;
      
      PSUiReference node = (PSUiReference) sel.getFirstElement();
      PSObjectType type = node.getObjectType(); 
      if (type == null || type.getSecondaryType() == null)
         return false;
      
      /* We don't allow creating new files on the local file system. The
       * local file system view is only present as a target for copying files
       * from the CMS file system on the Rx server.
       */
      return type.getSecondaryType() == PSObjectTypes.FileSubTypes.FOLDER
         && type.getPrimaryType() != PSObjectTypes.LOCAL_FILE;
   }
   
   /**
    * The logging target for all instances of this class. Never
    * <code>null</code>.
    */
   private static Log ms_logger = LogFactory.getLog(PSNewFileAction.class);

   /**
    * Context for creating new files. May be <code>null</code>.
    */
   private IWorkbenchPage m_page;
}
