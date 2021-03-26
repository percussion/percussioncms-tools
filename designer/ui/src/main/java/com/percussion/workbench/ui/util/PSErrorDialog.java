/******************************************************************************
 *
 * [ PSErrorDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.util;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.PSAbstractLabelProvider;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A Error dialog that will display an error message and can 
 * display a list of all passed in exceptions. If there is only 
 * one exception then just the detail message will show. If this
 * is a multi exception then the dialog will show a list of errors
 * with a detail box to show the detail message.
 */
public class PSErrorDialog extends Dialog
{   
   
   /**
    * Ctor
    * @param parentShell the parent shell for this dialog, cannot be 
    * <code>null</code>.
    * @param title the title to be displayed, may be <code>null</code> or
    * empty in which case the default title will appear.
    * @param msg the error message to be displayed may be <code>null</code>
    * or empty.
    * @param t a single error, May be <code>null</code>.
    */
   public PSErrorDialog(Shell parentShell, String title, String msg, Throwable t)
   {      
      this(parentShell, title, msg, getThrowableCollection(t)); 
   }
   
   /**
    * Ctor
    * @param parentShell the parent shell for this dialog, cannot be 
    * <code>null</code>.
    * @param title the title to be displayed, may be <code>null</code> or
    * empty in which case the default title will appear.
    * @param msg the error message to be displayed may be <code>null</code>
    * or empty.
    */
   public PSErrorDialog(Shell parentShell, String title, String msg)
   {
      this(parentShell, title, msg, (Collection<Throwable>)null);    
   }
   
   /**
    * Ctor
    * @param parentShell the parent shell for this dialog, cannot be 
    * <code>null</code>.
    * @param title the title to be displayed, may be <code>null</code> or
    * empty in which case the default title will appear.
    * @param msg the error message to be displayed may be <code>null</code>
    * or empty.
    * @param errors collection of errors, may be <code>null</code> or empty.
    * In which case the dialog will not display the exceptions section, only
    * the message will be shown.
    */
   public PSErrorDialog(Shell parentShell, String title, String msg,
      Collection<Throwable> errors)
   {
      this(parentShell, title, msg, errors, false);
   }
   
   /**
    * Ctor
    * @param parentShell the parent shell for this dialog, cannot be 
    * <code>null</code>.
    * @param title the title to be displayed, may be <code>null</code> or
    * empty in which case the default title will appear.
    * @param msg the error message to be displayed may be <code>null</code>
    * or empty.
    * @param errors collection of errors, may be <code>null</code> or empty.
    * In which case the dialog will not display the exceptions section, only
    * the message will be shown.
    * @param isWarning if <code>false</code> then a warning icon and title
    * will be displayed.
    */
   public PSErrorDialog(Shell parentShell, String title, String msg,
      Collection<Throwable> errors, boolean isWarning)
   {
      super(parentShell);
      setShellStyle(getShellStyle() | SWT.RESIZE);
      m_msg = msg;
      if(errors != null)
      {
         processErrors(errors);
      }
      m_isWarning = isWarning;
      if(isWarning)
         m_title = PSMessages.getString("common.error.warning_title"); //$NON-NLS-1$
      else if(!StringUtils.isBlank(title))
         m_title = title;
      else
         m_title = PSMessages.getString("common.error.title"); //$NON-NLS-1$
      
      
   }
   
   /**
    * Ctor
    * @param parentShell the parent shell for this dialog, cannot be 
    * <code>null</code>.
    * @param title the title to be displayed, may be <code>null</code> or
    * empty in which case the default title will appear.
    * @param msg the error message to be displayed cannot be <code>null</code>
    * or empty.
    * @param errors collection of errors in a <code>MultiStatus</code> object,
    * may be <code>null</code>.In which case the dialog will not display the
    * exceptions section, only the message will be shown.
    */
   public PSErrorDialog(Shell parentShell, String title, String msg,
      MultiStatus errors)
   {
      this(parentShell, title, msg, getMultiStatusErrors(errors));
   }
   
   /**
    * Ctor
    * @param parentShell the parent shell for this dialog, cannot be 
    * <code>null</code>.
    * @param msg the error message to be displayed may be <code>null</code>
    * or empty.
    * @param t a single error, May be <code>null</code>.
    */
   public PSErrorDialog(Shell parentShell, String msg, Throwable t)
   {      
      this(parentShell, null, msg, t); 
   }
   
   /**
    * Ctor
    * @param parentShell the parent shell for this dialog, cannot be 
    * <code>null</code>.
    * @param msg the error message to be displayed cannot be <code>null</code>
    * or empty.
    */
   public PSErrorDialog(Shell parentShell, String msg)
   {
      this(parentShell, null, msg);    
   }
   
   /**
    * Ctor
    * @param parentShell the parent shell for this dialog, cannot be 
    * <code>null</code>.
    * @param msg the error message to be displayed cannot be <code>null</code>
    * or empty.
    * @param errors collection of errors, may be <code>null</code> or empty.
    * In which case the dialog will not display the exceptions section, only
    * the message will be shown.
    */
   public PSErrorDialog(Shell parentShell, String msg,
      Collection<Throwable> errors)
   {
      this(parentShell, null, msg, errors);
   }
   
   /**
    * Ctor
    * @param parentShell the parent shell for this dialog, cannot be 
    * <code>null</code>.
    * @param msg the error message to be displayed cannot be <code>null</code>
    * or empty.
    *@param errors collection of errors in a <code>MultiStatus</code> object,
    * may be <code>null</code>.In which case the dialog will not display the
    * exceptions section, only the message will be shown.
    */
   public PSErrorDialog(Shell parentShell, String msg,
      MultiStatus errors)
   {
      this(parentShell, null, msg, errors);
   }
   
   /**
    * Process the errors passed into the dialog, expanding
    * the multi operation exceptions and capturing their details.
    * @param errors assumed not <code>null</code>.
    */
   private void processErrors(final Collection<Throwable> errors)
   {
      for(Throwable t : errors)
      {
         if(t instanceof PSMultiOperationException)
         {
            processMultiOperationException((PSMultiOperationException)t);
         }
         else
         {
            processException(t, null);
         }
      }
   }
   
   /**
    * Helper method to handle the processing of a <code>PSMultiOperationException<code>.
    * We get all the exceptions in the results and add those to the list of
    * errors. 
    * @param ex assumed not <code>null</code>.
    */
   private void processMultiOperationException(PSMultiOperationException ex)
   {
      Object[] results = ex.getResults();
      Object[] details = ex.getDetails();
      for(int i = 0; i < results.length; i++)
      {
         if(results[i] instanceof Throwable)
         {
            processException((Throwable)results[i],
               details == null ? null : details[i]);
         }
      }
   }
   
   /**
    * Processes an exception by adding it and its details (if any) to
    * the list of errors.
    * @param ex assumed not <code>null</code>.
    * @param detail may be <code>null</code>.
    */
   private void processException(Throwable ex, Object detail)
   {
      if(ex instanceof PSMultiOperationException)
      {
         processMultiOperationException((PSMultiOperationException)ex);
      }
      else if(ex.getCause() != null && 
         ex.getCause() instanceof PSMultiOperationException)
      {
         processMultiOperationException((PSMultiOperationException)ex.getCause());
      }
      else
      {
         m_errors.add(ex);
         if(ex instanceof PSModelException)
         {
            PSModelException mex = (PSModelException)ex;
            if(mex.getDetail() != null)
               detail = mex.getDetail();
         }
            
         if(detail != null)
            m_details.put(ex, detail);
         
         if (ex.getCause() != null)
            processException(ex.getCause(), null);
      }
   }
   
   /* 
    * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(
    * org.eclipse.swt.widgets.Composite)
    */
   @Override
   protected Control createDialogArea(Composite parent)
   {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new FormLayout());      
      
      Composite msgComp = createMessageComposite(container);
      
      final FormData formData_6 = new FormData();
      formData_6.top = new FormAttachment(0, 15);
      formData_6.left = new FormAttachment(0, 10);
      formData_6.right = new FormAttachment(100, -10);
      msgComp.setLayoutData(formData_6);
      Composite lastComp = msgComp;
      
      Composite exceptionsComp = null;
      if(m_errors.size() > 1)
      {
         exceptionsComp = createMultiExceptionComposite(container);
         final FormData formData_2 = new FormData();
         formData_2.right = new FormAttachment(100, -10);
         formData_2.top = new FormAttachment(lastComp, 20, SWT.BOTTOM);
         formData_2.left = new FormAttachment(0, 10);
         formData_2.height = 140;
         exceptionsComp.setLayoutData(formData_2);
         lastComp = exceptionsComp;
         initExceptionsTree();
         
      }   
      if(m_errors.size() > 1 || 
         (m_errors.size() == 1 && StringUtils.isNotBlank(m_msg)))
      {
         Composite detailsComp = createDetailsComposite(container);
         final FormData formData_5 = new FormData();
         formData_5.right = new FormAttachment(100, -10);
         formData_5.top = new FormAttachment(lastComp, 15);
         formData_5.left = new FormAttachment(lastComp, 0, SWT.LEFT);
         formData_5.height = 140;
         detailsComp.setLayoutData(formData_5);         
      }  
      if(!m_errors.isEmpty())
      {
         if(m_errors.size() == 1)   
            displaySingleError();
         else
         {
            m_exceptionsTree.setSelection(
               new TreeItem[]{m_exceptionsTree.getItem(0)});
            updateDetailsText();
            m_exceptionsTree.setFocus();
         }
      }
      addKeyListeners(container);
      return container;
   }
   
   /**
    * Handles displaying an error message when there is only a single error.
    */
   private void displaySingleError()
   {
      // Format the message
      StringBuilder sb = new StringBuilder();
      Throwable t = m_errors.iterator().next();
      Object detail = m_details.get(t);
      String name = null;
      if(detail != null)
         name = getFormattedDetail(detail);
      if(StringUtils.isNotBlank(name))
      {
         Object[] args = new Object[]{name};
         sb.append(PSMessages.getString("PSErrorDialog.error.prefix", args)); //$NON-NLS-1$
      }
      // Get the exception message
      sb.append(getExceptionMessage(t));
      if(StringUtils.isBlank(sb.toString()))
      {
         String unknown = PSMessages.getString("PSErrorDialog.error.unknown"); //$NON-NLS-1$
         sb.setLength(unknown.length());
         sb.append(unknown);
      }
      if(StringUtils.isBlank(m_msg))
      {
         // Put details in message field if no message was passed
         // into the ctor
         m_messageLabel.setText(sb.toString());
      }
      else
      {
         // Use detail text field if a message was passed in
         m_detailText.setText(sb.toString());
      }
      
   }

   /**
    * The message for the provided exception. Analyzes throwable
    * localized message, message, nested throwable for valid information
    * to show to the user.
    * @param t the exception to return message for.
    * Assumed not <code>null</code>.
    * @return message extracted from the exception to show to the user.
    * Never <code>null</code> or empty. 
    */
   private String getExceptionMessage(Throwable t)
   {           
      String msg = StringUtils.isBlank(t.getLocalizedMessage())
      ? t.getMessage()
         : t.getLocalizedMessage();
      
      if (StringUtils.isBlank(msg))
      {
         if (t.getCause() != null && !t.getCause().equals(t))
         {
            return getExceptionMessage(t.getCause());
         }
         return t.getClass().getName();
      }
      return msg;      
   }
   
   

   /**
    * Helper method to encapsulate creation of the message composite
    * @param parent assumed not <code>null</code>.
    * @return the new composite, never <code>null</code>.
    */
   private Composite createMessageComposite(Composite parent)
   {
      Composite comp = new Composite(parent, SWT.NONE);
      comp.setLayout(new FormLayout());
      m_iconLabel = new Label(comp, SWT.NONE);
      final FormData formData_6 = new FormData();
      formData_6.top = new FormAttachment(0, 0);
      formData_6.left = new FormAttachment(0, 0);
      m_iconLabel.setLayoutData(formData_6);
      m_iconLabel.setImage(m_isWarning ? 
         PSUiUtils.getWarningImage(getShell()):
            PSUiUtils.getErrorImage(getShell()));
      
      m_messageLabel = new Label(comp, SWT.WRAP);
      final FormData formData = new FormData();
      //formData.bottom = new FormAttachment(100, 0);
      formData.right = new FormAttachment(100, 0);
      formData.top = new FormAttachment(m_iconLabel, 8, SWT.TOP);
      formData.left = new FormAttachment(m_iconLabel, 5);
      m_messageLabel.setLayoutData(formData);
      if(m_errors.size() > 1 && StringUtils.isBlank(m_msg))
         m_msg = PSMessages.getString("PSErrorDialog.error.defaultMultiErrorMessage"); //$NON-NLS-1$
      if(StringUtils.isNotBlank(m_msg))
         m_messageLabel.setText(m_msg);
      
      return comp;
   }
   
   /**
    * Helper method to encapsulate creation of the multi exception composite
    * @param parent assumed not <code>null</code>.
    * @return the new composite, never <code>null</code>.
    */
   private Composite createMultiExceptionComposite(Composite parent)
   {
      Composite comp = new Composite(parent, SWT.NONE);
      comp.setLayout(new FormLayout());
      
      m_prevButton = new Button(comp, SWT.ARROW);
      m_prevButton.setToolTipText(PSMessages.getString("PSErrorDialog.tooltip.previous")); //$NON-NLS-1$
      m_prevButton.setText(PSMessages.getString("PSErrorDialog.button.previous")); //$NON-NLS-1$
      m_prevButton.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(
            @SuppressWarnings("unused") SelectionEvent e) //$NON-NLS-1$
         {
            moveUpDown(true);
         }
      });
      
      m_nextButton = new Button(comp, SWT.ARROW | SWT.DOWN);
      m_nextButton.setToolTipText(PSMessages.getString("PSErrorDialog.tooltip.next")); //$NON-NLS-1$
      m_nextButton.setText(PSMessages.getString("PSErrorDialog.button.next")); //$NON-NLS-1$
      m_nextButton.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(
            @SuppressWarnings("unused") SelectionEvent e) //$NON-NLS-1$
         {
            moveUpDown(false);
         }
      });
      
      m_exceptionsLabel = new Label(comp, SWT.NONE);
      final FormData formData_1 = new FormData();
      formData_1.top = new FormAttachment(0, 0);
      formData_1.left = new FormAttachment(0, 0);
      m_exceptionsLabel.setLayoutData(formData_1);
      m_exceptionsLabel.setText(PSMessages.getString("PSErrorDialog.label.failures")); //$NON-NLS-1$
      
      m_exceptionsViewer = new TreeViewer(
         comp, SWT.V_SCROLL | SWT.BORDER | SWT.H_SCROLL | SWT.SINGLE);
      m_exceptionsViewer.setContentProvider(new ErrorTreeContentProvider());
      m_exceptionsViewer.setLabelProvider(new PSAbstractLabelProvider()
         {
         public String getText(Object element)
         {
            ErrorNode error = (ErrorNode)element;
            if(error == null)
               return ""; //$NON-NLS-1$
            Throwable t = error.getError();
            String cName = t.getClass().getName();               
            Object detail = m_details.get(t);
            if(detail != null)
            {
               return getFormattedDetail(detail);
            }
            // Return the exception object name in the event that there
            // is no detail
            return cName.substring(cName.lastIndexOf(".") + 1); //$NON-NLS-1$
         }         
         });
      m_exceptionsTree = m_exceptionsViewer.getTree();
      m_exceptionsTree.addSelectionListener(new SelectionAdapter()
         {
         /* 
          * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
          * org.eclipse.swt.events.SelectionEvent)
          */
         @Override
         public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e) //$NON-NLS-1$
         {
            updateDetailsText();               
         }
         
         });
      final FormData formData_2 = new FormData();
      formData_2.bottom = new FormAttachment(100, 0);
      formData_2.right = new FormAttachment(m_prevButton, -5, SWT.LEFT);
      formData_2.top = new FormAttachment(m_exceptionsLabel, 0, SWT.BOTTOM);
      formData_2.left = new FormAttachment(m_exceptionsLabel, 0, SWT.LEFT);
      m_exceptionsTree.setLayoutData(formData_2);
      
      // Layout prev button
      final FormData formData_3 = new FormData();
      formData_3.right = new FormAttachment(100, 0);
      formData_3.top = new FormAttachment(m_exceptionsTree, 0, SWT.TOP);
      formData_3.width = 50;
      m_prevButton.setLayoutData(formData_3);      
      
      // Layout next button
      final FormData formData_4 = new FormData();
      formData_4.left = new FormAttachment(m_prevButton, 0, SWT.LEFT);
      formData_4.right = new FormAttachment(m_prevButton, 0, SWT.RIGHT);
      formData_4.top = new FormAttachment(m_prevButton, 5, SWT.BOTTOM);
      m_nextButton.setLayoutData(formData_4);            
      
      Control[] tabList = new Control[]{
         m_exceptionsTree, m_prevButton, m_nextButton};
      comp.setTabList(tabList);      
      
      return comp;
   }
   
   /**
    * Adds key listeners recursively to all the control passed in
    * and all of its decendent controls.
    * @param control assumed not <code>null</code>.
    */
   private void addKeyListeners(Control control)
   {
      if(control instanceof Composite)
      {
         for(Control child : ((Composite)control).getChildren())
            addKeyListeners(child);
      }
      control.addKeyListener(new KeyAdapter()
         {
         
         /* 
          * @see org.eclipse.swt.events.KeyAdapter#keyPressed(
          * org.eclipse.swt.events.KeyEvent)
          */
         @Override
         public void keyPressed(KeyEvent e)
         {
            if(e.character == 's' && e.stateMask == SWT.ALT)
            {
               showStackTrace();
            }
         }
         
         });
   }
   
   /**
    * Helper method to format the detail object
    * @param detail the detail, may be <code>null</code>
    * @return the formatted detail or an empty string.
    */
   private String getFormattedDetail(Object detail)
   {
      if(detail instanceof IPSReference)
      {
         // Format as:
         // NAME <TYPE> [READABLE ID(ID)]
         IPSReference ref = (IPSReference)detail;
         StringBuilder sb = new StringBuilder();
         sb.append(ref.getName());
         String type = StringUtils.defaultString(
            PSMessages.getString("common.objecttype." +  //$NON-NLS-1$
               ref.getObjectType().toString()));
         if(StringUtils.isNotBlank(type))
         {
            sb.append(" <");
            sb.append(type);
            sb.append(">");
         }
         if(ref.getId() != null)
         {
            PSDesignGuid guid = new PSDesignGuid(ref.getId());
            sb.append(" ["); //$NON-NLS-1$
            sb.append(guid.toString());            
            sb.append(" ("); //$NON-NLS-1$
            sb.append(guid.getValue());
            sb.append(")]"); //$NON-NLS-1$
         }
         return sb.toString();
      }
      else if (detail instanceof String)
      {
         return (String)detail;
      }    
      return ""; //$NON-NLS-1$
   }
   
   
   /**
    * Helper method to encapsulate creation of the details composite
    * @param parent assumed not <code>null</code>.
    * @return the new composite, never <code>null</code>.
    */
   private Composite createDetailsComposite(Composite parent)
   {
      Composite comp = new Composite(parent, SWT.NONE);
      comp.setLayout(new FormLayout());
      Label detailLabel = new Label(comp, SWT.WRAP);
      detailLabel.setText(PSMessages.getString("PSErrorDialog.label.detail")); //$NON-NLS-1$
      final FormData formData_1 = new FormData();
      formData_1.left = new FormAttachment(0, 0);
      formData_1.top = new FormAttachment(0, 0);
      detailLabel.setLayoutData(formData_1);
      m_detailText = new StyledText(comp, SWT.V_SCROLL | SWT.READ_ONLY | SWT.BORDER | SWT.WRAP);
      m_detailText.setBackground(Display.getCurrent().getSystemColor(
         SWT.COLOR_WIDGET_BACKGROUND));
      final FormData formData_2 = new FormData();
      formData_2.bottom = new FormAttachment(100, 0);
      formData_2.right = new FormAttachment(100, 0);
      formData_2.top = new FormAttachment(detailLabel, 0);
      formData_2.left = new FormAttachment(0, 0);
      m_detailText.setLayoutData(formData_2);
      return comp;
   }
    
   
   /* 
    * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(
    * org.eclipse.swt.widgets.Composite)
    */
   @Override
   protected void createButtonsForButtonBar(Composite parent)
   {
      createButton(parent, IDialogConstants.CANCEL_ID,
         PSMessages.getString("PSErrorDialog.button.close"), false); //$NON-NLS-1$
   }
   
   /**
    * Displays stack trace information about the selected error
    * in a separate dialog that allows clipboard copy and print.
    */
   protected void showStackTrace()
   {
      int errorCount = m_errors.size();
      if(errorCount == 0)
         return;
      Throwable t = null;
      if(errorCount == 1)
      {
         t = m_errors.iterator().next();
      }
      else
      {
         TreeItem[] item = m_exceptionsTree.getSelection();
         if(item.length == 0)
            return;
         ErrorNode error = (ErrorNode)item[0].getData();
         t = error.getError();
      }
      new StackTraceDialog(getShell(), t).open();
   }
   
   /**
    * Moves the selection up or down from the current selection.
    * @param moveUp if <code>true</code> then we try to move the
    * selection up, else go down.
    */
   protected void moveUpDown(boolean moveUp)
   {
      m_exceptionsViewer.expandAll();
      TreeItem[] item = m_exceptionsTree.getSelection();
      TreeItem[] rootItems = m_exceptionsTree.getItems();
      
      if(item.length == 1)
      {
         TreeItem parent = item[0].getParentItem();
         TreeItem newSelection = item[0];
         
         if(moveUp)
         {
            if(parent == null && item[0] == rootItems[0])
            {
               m_exceptionsTree.setFocus();
               return; // nothing to do this is root
            }
            if(parent == null)
            {
               int idx = ArrayUtils.indexOf(rootItems, item[0]);
               newSelection = getLastItem(rootItems[idx - 1]);
            }
            else
            {
               
               TreeItem[] children = parent.getItems();
               int idx = ArrayUtils.indexOf(children, item[0]);
               
               if(idx == 0)
               {
                  newSelection = parent;                     
               }
               else
               {
                  newSelection = children[idx - 1];
               } 
            }
            
         }
         else
         {
            if(item[0].getItemCount() > 0)
            {
               newSelection = item[0].getItems()[0];
            }
            else 
            {
               TreeItem next = getNextItem(item[0]);
               if(next != null)
               {
                  newSelection = next;
               }
               else
               {
                  TreeItem topNode = getTopNode(item[0]);
                  int idx = ArrayUtils.indexOf(rootItems, topNode);
                  if(idx < (rootItems.length - 1))
                     newSelection = rootItems[idx + 1];                  
               }
            }
            
         }
         
         m_exceptionsTree.setSelection(new TreeItem[]{newSelection});
         updateDetailsText();
         m_exceptionsTree.setFocus();
      }
   }
   
   /**
    * Helper method to get the very last item from the
    * passed in tree item.
    * @param parent assumed not <code>null</code>.
    * @return the last item or itself if it is the last item
    */
   private TreeItem getLastItem(TreeItem parent)
   {
      TreeItem[] items = parent.getItems();
      if(items.length == 0)
         return parent;
      return getLastItem(items[items.length - 1]);
   }
   
   /**
    * Helper method to get the next item in the tree as
    * expected when pressing the next button.
    * @param node assumed not <code>null</code>.
    * @return the next item or <code>null</code> if it could
    * not be found
    */
   private TreeItem getNextItem(TreeItem node)
   {
      TreeItem parent = node.getParentItem();
      if(parent == null)
         return null;
      int idx = ArrayUtils.indexOf(parent.getItems(), node);
      if(idx < (parent.getItemCount() - 1))
         return parent.getItems()[idx + 1];
      return getNextItem(parent);
   }
   
   /**
    * Helper method to retrieve the top most parent node
    * (or root) that the passed in node exists in. 
    * @param node assumed not <code>null</code>.
    * @return the root node.
    */
   private TreeItem getTopNode(TreeItem node)
   {
      TreeItem parent = node.getParentItem();
      if(parent == null)
         return node;
      return getTopNode(parent);
   }
   
   /**
    * Updates the exception text from the selected exception.
    */
   protected void updateDetailsText()
   {
      TreeItem[] selection = m_exceptionsTree.getSelection();
      if(selection.length == 1)
      {
         ErrorNode node = (ErrorNode)selection[0].getData();
         m_detailText.setText(getExceptionMessage(node.getError()));
      }
      else
      {
         m_detailText.setText(""); //$NON-NLS-1$
      }
   }     
   
   /* 
    * @see org.eclipse.jface.window.Window#getInitialSize()
    */
   @Override
   protected Point getInitialSize()
   {
      
      final int width = 500;
      int height = (m_errors.size() > 1)
      ? 450 : 290;
      // Must compute shell size if only the message is showing
      if(m_errors.isEmpty() || 
         (m_errors.size() == 1 && StringUtils.isBlank(m_msg)))
      {       
         height = getShell().computeSize(width, SWT.DEFAULT).y + 20;         
      }
      return new Point(width, Math.max(height, 180));
   }
   
   /* 
    * @see org.eclipse.jface.window.Window#configureShell(
    * org.eclipse.swt.widgets.Shell)
    */
   @Override
   protected void configureShell(Shell newShell)
   {
      super.configureShell(newShell);
      newShell.setText(m_title);
   }
   
   /**
    * The dialog title.
    */
   public String getTitle()
   {
      return m_title;
   }
   
   public void setTitle(String title)
   {
      m_title = title;
   }
   
   /**
    * Initializes the exceptions list control.
    */
   private void initExceptionsTree()
   {
      ErrorNode root = new ErrorNode(null);
      for(Throwable error : m_errors)
      {
         root.addChild(new ErrorNode(root, error));
      }
      m_exceptionsViewer.setInput(root);
      m_exceptionsViewer.expandAll();
   }
   
   
   /**
    * Get a collection of throwables from a <code>MultiStatus</code>
    * object.
    * @param status assumed not <code>null</code>.
    * @return collection, never <code>null</code>.
    */
   private static Collection<Throwable> getMultiStatusErrors(
      MultiStatus status)
      {
      Collection<Throwable> errors = new ArrayList<Throwable>();
      for(IStatus s : status.getChildren())
      {
         Throwable t = s.getException();
         if(t != null)
            errors.add(t);
      }
      return errors;
      
      }
   
   /**
    * Create a Collection from a single throwable
    * @param t assumed not <code>null</code>.
    * @return collection, never <code>null</code>.
    */
   private static Collection<Throwable> getThrowableCollection(Throwable t)
   {
      Collection<Throwable> errors = new ArrayList<Throwable>();
      errors.add(t);
      return errors;
   }
   
   /**
    * Simple Wrapper around a throwable to make it easier to maintain
    * in a tree.
    */
   class ErrorNode
   {
      
      ErrorNode(ErrorNode parent, Throwable error)
      {
         this(error);
         mi_parent = parent;
         
      }
      
      ErrorNode(Throwable error)
      {
         mi_error = error;
         if(error != null && error.getCause() != null)
         {
            addChild(new ErrorNode(this, error.getCause()));
         }
      }
      
      Throwable getError()
      {
         return mi_error;
      }
      
      ErrorNode getParent()
      {
         return mi_parent;
      }
      
      Collection<ErrorNode> getChildren()
      {
         return mi_children;
      }
      
      boolean hasChildren()
      {
         return !mi_children.isEmpty();
      }
      
      void addChild(ErrorNode child)
      {
         if(child == null)
            throw new IllegalArgumentException("child cannot be null."); //$NON-NLS-1$
         mi_children.add(child);
         
      }      
      
      private Throwable mi_error;
      private ErrorNode mi_parent;
      private Collection<ErrorNode> mi_children = 
         new ArrayList<ErrorNode>();
   }
   
   /**
    * Content provider for the tree control
    */
   class ErrorTreeContentProvider extends ArrayContentProvider
   implements ITreeContentProvider
   {
      
      public Object[] getChildren(Object parentElement)
      {
         if(!m_developMode)
            return new Object[0];
         ErrorNode node = (ErrorNode)parentElement;
         return node.getChildren().toArray();         
      }
      
      public Object getParent(Object element)
      {
         ErrorNode node = (ErrorNode)element;
         return node.getParent();
      }
      
      public boolean hasChildren(Object element)
      {
         if(!m_developMode)
            return false;
         ErrorNode node = (ErrorNode)element;
         return node.hasChildren();
      }
      
      /* 
       * @see org.eclipse.jface.viewers.ArrayContentProvider#getElements(java.lang.Object)
       */
      @Override
      public Object[] getElements(Object inputElement)
      {
         
         ErrorNode node = (ErrorNode)inputElement;
         if(node.hasChildren())
            return node.getChildren().toArray();
         return new Object[0];
      }     
      
   }
   
   class StackTraceDialog extends Dialog
   {
      
      protected StackTraceDialog(Shell parentShell, Throwable t)
      {
         super(parentShell);
         setShellStyle(getShellStyle() | SWT.RESIZE);
         mi_error = t;         
      }      
      
      /**
       * Create contents of the dialog
       * @param parent
       */
      @Override
      protected Control createDialogArea(Composite parent)
      {
         Composite container = (Composite) super.createDialogArea(parent);
         
         mi_text = new Text(container,
            SWT.V_SCROLL | SWT.READ_ONLY | SWT.BORDER | SWT.WRAP);
         mi_text.setBackground(Display.getCurrent().getSystemColor(
            SWT.COLOR_WHITE));
         final GridData gridData = new GridData(
            GridData.FILL, GridData.FILL, true, true);
         gridData.heightHint = 285;
         mi_text.setLayoutData(gridData);
         StringBuilder sb = new StringBuilder();
         sb.append("Exception name: "); //$NON-NLS-1$
         sb.append(mi_error.getClass().getName());
         sb.append("\nException Message: "); //$NON-NLS-1$
         sb.append(getExceptionMessage(mi_error));
         sb.append("\n"); //$NON-NLS-1$
         appendStacks(sb, mi_error);
         if (mi_error.getCause() != null)
         {
            final Throwable cause = mi_error.getCause();
            sb.append("Cause of the exception: "); //$NON-NLS-1$
            sb.append(cause.getClass().getName());
            sb.append("\nCause Message: "); //$NON-NLS-1$
            sb.append(getExceptionMessage(cause));
            sb.append("\n"); //$NON-NLS-1$
            appendStacks(sb, cause);
         }
         mi_text.setText(sb.toString());
         return container;
      }
      
      /**
       * Helper method to append the stack trace to a string
       * builder.
       * @param sb assumed not <code>null</code>.
       * @param t assumed not <code>null</code>.
       */
      private void appendStacks(StringBuilder sb, Throwable t)
      {
         StackTraceElement[] stacks = t.getStackTrace();
         for(StackTraceElement trace: stacks)
         {
            sb.append(trace.toString());
            sb.append("\n"); //$NON-NLS-1$
         }
      }
      
      /**
       * Create contents of the button bar
       * @param parent
       */
      @Override
      protected void createButtonsForButtonBar(Composite parent)
      {
         Button copyButton = createButton(
            parent, IDialogConstants.CLIENT_ID + 1, PSMessages.getString(
               "PSErrorDialog.button.copy"), false); //$NON-NLS-1$
         copyButton.setToolTipText(PSMessages.getString(
            "PSErrorDialog.button.copy.tooltip")); //$NON-NLS-1$
         copyButton.addSelectionListener(new SelectionAdapter()
            {

               /* 
                * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
                * org.eclipse.swt.events.SelectionEvent)
                */
               @Override
               public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e) //$NON-NLS-1$
               {
                  mi_text.selectAll();
                  mi_text.copy();
                  mi_text.clearSelection();
               }
               
            });
         Button printButton = createButton(
            parent, IDialogConstants.CLIENT_ID + 2, PSMessages.getString(
               "PSErrorDialog.button.print"), false);  //$NON-NLS-1$
         printButton.setToolTipText(PSMessages.getString(
            "PSErrorDialog.button.print.tooltip"));  //$NON-NLS-1$
         printButton.addSelectionListener(new SelectionAdapter()
            {

               /* 
                * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
                * org.eclipse.swt.events.SelectionEvent)
                */
               @Override
               public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e) //$NON-NLS-1$
               {
                  PrintDialog dlg = new PrintDialog(getShell());
                  PrinterData pData = dlg.open();
                  if(pData != null)
                  {
                     
                     Printer printer = new Printer(pData);
                     try
                     {                        
                        new PSWrappingPrinter(printer, PSMessages.getString(
                           "PSErrorDialog.message.printStackTrace"), //$NON-NLS-1$
                           mi_text.getText()).print();                        
                     }
                     finally
                     {
                        printer.dispose();
                     }
                  }
               }
               
            });
         createButton(parent, IDialogConstants.CANCEL_ID,
            PSMessages.getString("PSErrorDialog.button.close"), false); //$NON-NLS-1$
         
      }      
      
      /**
       * Return the initial size of the dialog
       */
      @Override
      protected Point getInitialSize()
      {
         return new Point(400, 450);
      }
      
      @Override
      protected void configureShell(Shell newShell)
      {
         super.configureShell(newShell);
         newShell.setText(PSMessages.getString("PSErrorDialog.stackTrace.title")); //$NON-NLS-1$
      }
      
      private Throwable mi_error;
      private Text mi_text;
      
      
   }
   
   private StyledText m_detailText;
   private Button m_nextButton;
   private Button m_prevButton;
   private Tree m_exceptionsTree;
   private TreeViewer m_exceptionsViewer;
   private Label m_exceptionsLabel;
   private Label m_messageLabel;
   private Label m_iconLabel;
   
   private String m_msg;
   private String m_title;
   private Collection<Throwable> m_errors = new ArrayList<Throwable>();
   private Map<Throwable, Object> m_details = new HashMap<Throwable, Object>();
   private boolean m_isWarning;
   private boolean m_developMode = false;
}
