/******************************************************************************
 *
 * [ PSOpenAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.actions;

import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.workbench.ui.IPSEditorFactory;
import com.percussion.workbench.ui.PSEditorRegistry;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.model.IPSDeclarativeNodeHandler;
import com.percussion.workbench.ui.model.PSFileEditorTracker;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PartInitException;

/**
 * This action opens a an editor on a Rhythmyx design object. The editor may
 * be Eclipse 'editor part' based or dialog based. 
 * <p>
 * The action is applicable to selections containing elements of type
 * <code>PSUiReference</code> that refer to design objects. 
 * 
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 6.0
 */
public final class PSOpenAction extends PSBaseSelectionListenerAction
{
   /**
    * Creates a new <code>PSOpenAction</code>. The action requires that the
    * selection provided by the site's selection provider is of type <code>
    * org.eclipse.jface.viewers.IStructuredSelection</code>.
    * 
    * @param site the site providing context information for this action.
    * Never <code>null</code>.
    * @param provider Supplied to base class ctor.
    */
   public PSOpenAction(IWorkbenchSite site, ISelectionProvider provider)
   {
      super(PSMessages.getString("PSOpenAction.action.label"), provider);
      if (site == null)
      {
         throw new IllegalArgumentException("Site should not be null.");
      }
      setToolTipText(PSMessages.getString("PSOpenAction.action.tooltip"));
      setDescription(PSMessages.getString("PSOpenAction.action.tooltip"));
      m_site = site;
   }

   /**
    * @inheritDoc
    * Gets the selection using the site supplied in the ctor. If this is an
    * <code>IStructuredSelection</code>, each element is processed by opening
    * the appropriate editor using the {@link PSEditorRegistry} to open
    * the proper one
    */
   @Override
   public void run()
   {
      if (!isEnabled())
         return;
      IStructuredSelection ssel = getStructuredSelection();
      
      for (Object o : ssel.toArray())
      {
         if (!(o instanceof PSUiReference))
         {
            PSWorkbenchPlugin.getDefault().log(
                  "Found non-PSUiReference in selection: "
                        + o.getClass().getName());
            continue;
         }
         final PSUiReference node = (PSUiReference) o;
         try
         {
            final IPSDeclarativeNodeHandler handler = node.getHandler();
            if (handler != null && handler.supportsOpen(node))
            {
               handler.handleOpen(m_site, node);
            }
            else if (node.getObjectType() != null)
            {
               IPSEditorFactory factory = PSEditorRegistry.getInstance()
                     .findEditorFactory(node.getObjectType());
               factory.openEditor(m_site.getPage(), node.getReference());
            }
            else
            {
               // the node does not open
            }
         }
         catch (PartInitException e)
         {
            String title = PSMessages.getString(
                  "PSOpenAction.error.openEditorFailed.title", 
                  new Object[] {node.getPath()});
            String msg = PSMessages.getString(
                  "PSOpenAction.error.openEditorFailed.message");
            PSUiUtils.handleExceptionSync("Open object", title, msg, e);
            e.printStackTrace();
         }
      }
   }

   /**
    * @inheritDoc The selection must contain 1 or more Eclipse 'editor part'
    * types to be enabled.
    */
   @Override
   protected boolean updateSelection(IStructuredSelection sel)
   {
      //as documented by super class
      if (!super.updateSelection(sel))
         return false;
      
      if (sel.size() == 0)
         return false;
      
      for (Object o : sel.toArray())
      {
         if (!(o instanceof PSUiReference))
         {
            return false;
         }
         PSUiReference node = (PSUiReference) o;
         IPSDeclarativeNodeHandler handler = node.getHandler();
         if (handler != null && handler.supportsOpen(node))
         {
            return true;
         }
         if (node.getObjectType() == null)
            return false;

         IPSEditorFactory factory = PSEditorRegistry.getInstance()
               .findEditorFactory(node.getObjectType());
         if (factory == null)
            return false;
         
         if (((IPSPrimaryObjectType) node.getObjectType().getPrimaryType())
               .isFileType())
         {
            if (PSFileEditorTracker.getInstance().isRegisteredForEdit(
                        node.getReference(), true))
            {
               return false;
            }
         }
      }

      return true;
   }

   /**
    * Used to obtain the selection when the action is run. Never
    * <code>null</code> or modified after ctor.
    */
   private final IWorkbenchSite m_site;
}
