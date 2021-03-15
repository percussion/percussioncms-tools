/******************************************************************************
 *
 * [ PSCopyAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.actions;

import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.model.IPSDeclarativeNodeHandler;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Standard action for copying the currently selected Rhythmyx design objects to
 * the m_clipboard.
 */
final class PSCopyAction extends PSBaseSelectionListenerAction
{
   /**
    * The id of this action.
    */
   public static final String ID = PSWorkbenchPlugin.getPluginId()
         + ".CopyAction"; //$NON-NLS-1$

   /**
    * Creates a new action.
    *
    * @param provider Supplied to super ctor. Never <code>null</code>.
    * @param clipboard A platform clipboard, never <code>null</code>.
    * @param pasteAction This is the action that will perform the paste of the
    * material copied by this action. Its state is updated after a copy is
    * performed. Never <code>null</code>.
    */
   public PSCopyAction(ISelectionProvider provider, Clipboard clipboard,
         PSPasteAction pasteAction)
   {
      super(PSMessages.getString("PSCopyAction.action.label"), provider);
      if ( null == clipboard)
      {
         throw new IllegalArgumentException("clipboard cannot be null");  
      }
      if ( null == pasteAction)
      {
         throw new IllegalArgumentException("pasteAction cannot be null");  
      }
      
      m_clipboard = clipboard;
      m_pasteAction = pasteAction;
      setToolTipText(PSMessages.getString("PSCopyAction.action.tooltip"));
      //for global action support
      setActionDefinitionId("org.eclipse.ui.edit.copy");
      setId(PSCopyAction.ID);
   }

   /**
    * Performs the actual copy to the clipboard. The elements in the selection
    * are not copied, rather paths of each node are copied to the clipboard.
    * On paste, the nodes are retrieved from the model.
    */
   @Override
   public void run()
   {
      if (!isEnabled())
         return;

      IStructuredSelection ssel = getStructuredSelection();
      Collection<PSUiReference> toCopy = validForCopy(ssel);
      if (toCopy == null)
      {
         PSWorkbenchPlugin.getDefault().log(
            "Selection not valid for copy.");
         return;
      }

      if (ms_logger.isDebugEnabled())
      {
         ms_logger.debug(MessageFormat.format(
               "Copying {0} items to clipboard.", toCopy.size()));
      }
      IPSDeclarativeNodeHandler handler = toCopy.iterator().next().getHandler();
      
      Map<Transfer, Object> cbData = handler.handleCopy(toCopy);
      if (cbData == null)
         return;
      Object[] data = new Object[cbData.size()];
      Transfer[] transfers = new Transfer[cbData.size()];
      int i = 0;
      for (Map.Entry<Transfer, Object> entry : cbData.entrySet())
      {
         data[i] = entry.getValue();
         transfers[i++] = entry.getKey();
      }
      
      boolean success = setClip(data, transfers, m_clipboard);
      
      //cause the paste action to update based on the new clipboard content
      if (success && m_pasteAction != null
            && m_pasteAction.getStructuredSelection() != null)
      {
         m_pasteAction.selectionChanged(m_pasteAction
               .getStructuredSelection());
      }
   }
   
   /* 
    * @see org.eclipse.ui.actions.BaseSelectionListenerAction#runWithEvent(
    * org.eclipse.swt.widgets.Event)
    * This method is overriden to get around a bug with the tree capturing
    * copy/paste events on tree editor text objects. We now force the
    * calling widget to handle the copy/paste event itself as it should.
    */
   @Override
   public void runWithEvent(Event event)
   {
      if(event.widget instanceof Text)
      {
         ((Text)event.widget).copy();
      }
      else
      {
         super.runWithEvent(event);
      }
      
   }

   /**
    * Puts the data on the clipboard. May display a dialog to the user if the
    * clipboard is busy. In this case, it will try one more time.
    * 
    * @param data Assumed not <code>null</code>.
    * @param transfers Assumed not <code>null</code> and len = data.length.
    * @param cb The data is put on this clipboard. Assumed not <code>null</code>.
    * 
    * @return A flag to indicate whether the data was put on the clipboard.
    * <code>true</code> indicates success.
    */
   private boolean setClip(Object[] data, Transfer[] transfers, Clipboard cb)
   {
      assert(data.length == transfers.length);
      boolean success = false;
      try
      {
         cb.setContents(data, transfers);
         success = true;
      }
      catch (SWTError e)
      {
         if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD)
            throw e;
         if (MessageDialog
               .openQuestion(
                     PSUiUtils.getShell(),
                     PSMessages.getString(
                           "PSCopyAction.error.clipboardUnavailable.title"),
                     PSMessages.getString(
                           "PSCopyAction.error.clipboardUnavailable.message")))
         {
            setClip(data, transfers, cb);
            success = true;
         }
      }
      return success;
   }

   /**
    * There must be at least 1 object selected, and the object(s) must be 
    * design objects and they must have the same parent.
    */
   @Override
   protected boolean updateSelection(IStructuredSelection selection)
   {
      if (!super.updateSelection(selection))
         return false;

      return validForCopy(selection) != null;
   }

   /**
    * Checks whether the supplied selection meets all criteria for copying.
    * 
    * @param ssel May be <code>null</code>.
    * 
    * @return <code>null</code> if it doesn't, or the selection cast to
    * nodes if it does.
    */
   private Collection<PSUiReference> validForCopy(IStructuredSelection ssel)
   {
      if (ssel == null)
         return null;
      
      List objects = ssel.toList();
      if (objects.size() == 0)
         return null;
      
      IPSDeclarativeNodeHandler handler = null;      
      Collection<PSUiReference> toCopy = new ArrayList<PSUiReference>();
      Enum primaryType = null;
      for (Object o : objects)
      {
         if (!(o instanceof PSUiReference))
         {
            return null;
         }
         PSUiReference node = (PSUiReference) o;
         if (handler == null)
         {
            handler = node.getHandler();
            if (handler == null)
               return null;
         }
         else if (node.getHandler() == null)
         {
            return null;
         }
         else if (!handler.equals(node.getHandler()))
         {
            return null;
         }
         
         if (!handler.supportsCopy(node))
            return null;
         if (node.getObjectType() != null)
         {
            if (!node.isFolder())
            {
               if (primaryType == null)
                  primaryType = node.getObjectType().getPrimaryType();
               else
               {
                  if (primaryType != node.getObjectType().getPrimaryType())
                     return null;
               }
            }
         }
         toCopy.add(node);
      }
      return toCopy;
   }

   /**
    * System clipboard supplied in the ctor.
    */
   private final Clipboard m_clipboard;

   /**
    * Associated paste action. May be <code>null</code>. Used to update the
    * enabled state after a copy.
    */
   private final PSPasteAction m_pasteAction;
   
   /**
    * The logging target for all instances of this class. Never
    * <code>null</code>.
    */
   private static Logger ms_logger = LogManager.getLogger(PSCopyAction.class);
}
