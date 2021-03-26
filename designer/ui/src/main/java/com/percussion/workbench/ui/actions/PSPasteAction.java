/******************************************************************************
 *
 * [ PSPasteAction.java ]
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
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Standard action for performing some form of pasting of Rhythmyx design
 * objects on the clipboard to the selected node's location. The object on the
 * clipboard will be an
 * {@link com.percussion.client.IPSReference IPSReference[]}. The paste action
 * will differ depending on the target node. The target node is queried for a
 * node handler, which is used to perform the actual action.
 */
final class PSPasteAction extends PSBaseSelectionListenerAction
{
   /**
    * The id of this action.
    */
   public static final String ID = PSWorkbenchPlugin.getPluginId() + ".PasteAction";//$NON-NLS-1$

   /**
    * Creates a new action.
    * 
    * @param provider Supplied to super ctor. Never <code>null</code>.
    * 
    * @param clipboard The clipboard from which to paste from. Never
    * <code>null</code>.
    */
   public PSPasteAction(ISelectionProvider provider, Clipboard clipboard)
   {
      super(PSMessages.getString("PSPasteAction.action.label"), provider);
      if ( null == clipboard)
      {
         throw new IllegalArgumentException("clipboard cannot be null");  
      }
      
      m_clipboard = clipboard;
      setToolTipText(PSMessages.getString("PSPasteAction.action.tooltip"));
      setActionDefinitionId("org.eclipse.ui.edit.paste");
      setId(PSPasteAction.ID);
      
   }

   /**
    * Groups the objects on the clipboard by transfer type and calls the node
    * handler for the selected node for each group.
    */
   @Override
   public void run()
   {
      IStructuredSelection ssel = getStructuredSelection();
      List objects = ssel.toList();
      if (objects.size() != 1)
      {
         PSWorkbenchPlugin.getDefault().log(
               "Must have exactly 1 object selected for paste: " 
               + objects.size() 
               + " were checked.");
         return;
      }
      
      Object o = objects.get(0);
      if (!(o instanceof PSUiReference))
      {
         PSWorkbenchPlugin.getDefault().log(
               "Non PSUiReference as selection during paste. Actual class: "
                     + o.getClass().getName());
         return;
      }
      final PSUiReference node = (PSUiReference) o;
      
      Transfer[] acceptedTransfers = node.getHandler().getAcceptedTransfers();
      final Map<Transfer, Object> cbData = new HashMap<Transfer, Object>();
      for (Transfer transfer : acceptedTransfers)
      {
         Object tmp = m_clipboard.getContents(transfer);
         if (tmp != null)
         {
            cbData.put(transfer, tmp);
         }
      }
      
      if (cbData.size() == 0)
      {
         PSWorkbenchPlugin.getDefault().log(
               "No matching data found on clipboard for node: " + node.getPath());
         return;
      }
      
      BusyIndicator.showWhile(Display.getCurrent(), new Runnable()
      {
         public void run()
         {
            node.getHandler().handlePaste(node, cbData);
         }
      });
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
         ((Text)event.widget).paste();
      }
      else
      {
         super.runWithEvent(event);
      }
      
   }

   /**
    * The following must be <code>true</code> for a paste to be allowed:
    * <ol>
    *    <li>Selection size must be 1</li>
    *    <li>Selection type must be instanceof {@link PSUiReference}</li>
    *    <li>There must be data on the clipboard that matches at least one type
    *    returned by the getAcceptedTransfers method of the selection's node
    *    handler</li>
    * <ol>
    */
   @Override
   protected boolean updateSelection(IStructuredSelection selection)
   {
      if (!super.updateSelection(selection))
         return false;
      if (selection == null || selection.size() != 1)
         return false;
      if (!(selection.getFirstElement() instanceof PSUiReference))
         return false;
      
      TransferData[] availableData = m_clipboard.getAvailableTypes();
      if (availableData == null)
         return false;
      
      PSUiReference node = (PSUiReference) selection.getFirstElement();
      if (node.getHandler() == null)
         return false;
      Transfer[] acceptedTypes = node.getHandler().getAcceptedTransfers();

      //see if there is at least 1 match between available and accepted types
      for (TransferData td : availableData)
      {
         for (Transfer acceptedType : acceptedTypes)
         {
            if (acceptedType.isSupportedType(td))
               return true;
         }
      }

      return false;
   }

   /**
    * System clipboard, provided in the ctor. Never <code>null</code> after ctor.
    */
   private final Clipboard m_clipboard;
}
