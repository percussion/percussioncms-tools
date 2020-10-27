/******************************************************************************
 *
 * [ PSRelTypeEffectsExecutionContextDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.design.objectstore.PSConditionalEffect;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.PSSlushBucket;
import com.percussion.workbench.ui.editors.dialog.PSDialog;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import java.util.ArrayList;
import java.util.List;

import static com.percussion.relationship.IPSExecutionContext.RS_POST_CHECKOUT;
import static com.percussion.relationship.IPSExecutionContext.RS_POST_WORKFLOW;
import static com.percussion.relationship.IPSExecutionContext.RS_PRE_CHECKIN;
import static com.percussion.relationship.IPSExecutionContext.RS_PRE_CLONE;
import static com.percussion.relationship.IPSExecutionContext.RS_PRE_CONSTRUCTION;
import static com.percussion.relationship.IPSExecutionContext.RS_PRE_DESTRUCTION;
import static com.percussion.relationship.IPSExecutionContext.RS_PRE_UPDATE;
import static com.percussion.relationship.IPSExecutionContext.RS_PRE_WORKFLOW;
import static com.percussion.relationship.IPSExecutionContext.VALIDATION_MAX;
import static com.percussion.relationship.IPSExecutionContext.VALIDATION_MIN;

/**
 * Allows user to specify relationship type effect execution context.
 * @see PSConditionalEffect#getExecutionContexts()
 *
 * @author Andriy Palamarchuk
 */
public class PSRelTypeEffectsExecutionContextDialog extends PSDialog
{

   public PSRelTypeEffectsExecutionContextDialog(final Shell parentShell,
         final List<Integer> executionContexts)
   {
      super(parentShell);
      assert executionContexts != null;
      m_executionContexts = executionContexts; 
   }
   
   /* 
    * @see org.eclipse.jface.window.Window#configureShell(
    * org.eclipse.swt.widgets.Shell)
    */
   @Override
   protected void configureShell(Shell newShell)
   {
      super.configureShell(newShell);
      newShell.setText(PSMessages.getString(
            "PSRelTypeEffectsExecutionContextDialog.title")); //$NON-NLS-1$
   }

   @Override
   protected Control createDialogArea(Composite parent)
   {
      final Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new FormLayout());
      
      m_selectionControl = createSelectionControl(container);
      m_selectionControl.setValues(
            getExecutionContextIds(),
            m_executionContexts);
      return container;
   }

   /**
    * Creates the control which allows user to make selections.
    */
   private PSSlushBucket createSelectionControl(Composite container)
   {
      final PSSlushBucket selectionControl =
            new PSSlushBucket(container, SWT.NONE,
                  AVAILABLE_LABEL + ':', SELECTED_LABEL + ':', 
                  new PSContextIdLabelProvider());
      {
         final FormData formData = new FormData();
         formData.left = new FormAttachment(0, 0);
         formData.right = new FormAttachment(100, 0);
         formData.top = new FormAttachment(0, 0);
         formData.bottom = new FormAttachment(100, 0);
         selectionControl.setLayoutData(formData);
      }
      return selectionControl;
   }
   /**
    * Converts execution context ID to human-readable label.
    * Throws {@link IllegalArgumentException} if can't identify the ID.
    * @param contextId context id
    */
   public static String executionContextToLabel(final int contextId)
   {
      switch (contextId)
      {
         case RS_PRE_CONSTRUCTION:
            return getMessage("context.preConstruction"); //$NON-NLS-1$
         case RS_PRE_DESTRUCTION:
            return getMessage("context.preDestruction"); //$NON-NLS-1$
         case RS_PRE_WORKFLOW:
            return getMessage("context.preWorkflow"); //$NON-NLS-1$
         case RS_POST_WORKFLOW:
            return getMessage("context.postWorkflow"); //$NON-NLS-1$
         case RS_PRE_CHECKIN:
            return getMessage("context.preCheckin"); //$NON-NLS-1$
         case RS_POST_CHECKOUT:
            return getMessage("context.postCheckout"); //$NON-NLS-1$
         case RS_PRE_UPDATE:
            return getMessage("context.preUpdate"); //$NON-NLS-1$
         case RS_PRE_CLONE:
            return getMessage("context.preClone"); //$NON-NLS-1$

         default:
            throw new IllegalArgumentException("Unrecognized id:" + contextId); //$NON-NLS-1$
      }
   }
   
   private List<Integer> getExecutionContextIds()
   {
      final List<Integer> ids = new ArrayList<Integer>();
      for (int i = VALIDATION_MIN; i <= VALIDATION_MAX; i++)
      {
         ids.add(i);
      }
      return ids;
   }
   
   /**
    * Convenience method to get message.
    */
   private static String getMessage(final String key)
   {
      return PSMessages.getString(
            "PSRelTypeEffectsExecutionContextDialog." + key);  //$NON-NLS-1$
   }

   /**
    * Provides execution context label based on id. 
    */
   private class PSContextIdLabelProvider extends LabelProvider
   {
      @Override
      public String getText(Object element)
      {
         final int id = (Integer) element;
         return executionContextToLabel(id);
      }
   }
   
   /**
    * Selected items.
    */
   public List<Integer> getSelections()
   {
      final List<Integer> selections = new ArrayList<Integer>();
      for (final Object reference : m_selectionControl.getSelections())
      {
         selections.add((Integer) reference);
      }
      return selections;
   }

   /**
    * Label over available items list.
    */
   private static String AVAILABLE_LABEL = getMessage("label.available"); //$NON-NLS-1$
   
   /**
    * Label over selected items list.
    */
   private static String SELECTED_LABEL  = getMessage("label.selected"); //$NON-NLS-1$


   /**
    * List of execution context ids.
    */
   private final List<Integer> m_executionContexts;

   /**
    * Component to select execution contexts.
    */
   private PSSlushBucket m_selectionControl;
}
