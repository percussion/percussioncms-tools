/******************************************************************************
*
* [ PSMaxRowsSpinnerComposite.java ]
*
* COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.editors.common;

import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

import java.util.ArrayList;
import java.util.List;

public class PSMaxRowsSpinnerComposite extends Composite
   implements IPSUiConstants
{

   
   /**
    * Create the composite
    * @param parent
    */
   public PSMaxRowsSpinnerComposite(Composite parent)
   {
      super(parent, SWT.NONE);
      setLayout(new FormLayout());

      m_Label = new Label(this, SWT.WRAP);
      final FormData formData = new FormData();
      formData.top = new FormAttachment(0, 
         LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET);
      formData.left = new FormAttachment(0, 0);
      m_Label.setLayoutData(formData);
      m_Label.setText(PSMessages.getString(
         "PSMaxRowsSpinnerComposite.label.max.rows")); //$NON-NLS-1$

      m_Spinner = new Spinner(this, SWT.BORDER);
      m_Spinner.setMinimum(MIN);
      m_Spinner.setMaximum(MAX);
      final FormData formData_1 = new FormData();
      formData_1.top = new FormAttachment(m_Label,
         -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      formData_1.left = 
         new FormAttachment(m_Label, LABEL_HSPACE_OFFSET, SWT.RIGHT);
      formData_1.width = 25;
      m_Spinner.setLayoutData(formData_1);
      
      m_Spinner.addSelectionListener(new SelectionAdapter()
         {
            /* 
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
             * org.eclipse.swt.events.SelectionEvent)
             */
            @SuppressWarnings("synthetic-access") //$NON-NLS-1$
            @Override
            public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e)
            {
              fireSelectionEvent();
            }
            
         });
      m_Spinner.addFocusListener(new FocusListener()
         {

            public void focusGained(@SuppressWarnings("unused") FocusEvent e)
            {
               fireFocusEvent(true);               
            }

            public void focusLost(@SuppressWarnings("unused") FocusEvent e)
            {
               fireFocusEvent(false);
            }
         
         });

      m_Button = new Button(this, SWT.CHECK);
      final FormData formData_2 = new FormData();
      formData_2.top = new FormAttachment(m_Spinner, 0, SWT.TOP);
      formData_2.left = new FormAttachment(m_Spinner, 10, SWT.RIGHT);
      m_Button.setLayoutData(formData_2);
      m_Button.setText(PSMessages.getString(
         "PSMaxRowsSpinnerComposite.label.unlimited")); //$NON-NLS-1$
      
      m_Button.addSelectionListener(new SelectionAdapter()
         {
            /* 
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
             * org.eclipse.swt.events.SelectionEvent)
             */
            @SuppressWarnings("synthetic-access") //$NON-NLS-1$
            @Override
            public void widgetSelected(SelectionEvent e)
            {
               Button box = (Button)e.getSource();
               if(box.getSelection())
               {
                  m_Spinner.setEnabled(false);   
               }
               else
               {
                  m_Spinner.setEnabled(true);
               }
               fireSelectionEvent();
            }
            
         });
      //
   }
   
   /**
    * @return the value of the spinner control or -1 if the 
    * unlimited checkbox is selected.
    */
   public int getValue()
   {
      if(m_Button.getSelection())
         return -1;
      return m_Spinner.getSelection();
   }
   
   /**
    * Sets the value of the control. If -1 is passed in then the
    * spinner will be disabled and the unlimited checkbox will be 
    * selected.
    * @param value a value greater than -1 but less than 10,000 and 
    * cannot be zero.
    */
   public void setValue(int value)
   {
      if(value < -1)
         throw new IllegalArgumentException("value cannot be less than -1."); //$NON-NLS-1$
      if(value != -1 && value < MIN)
         throw new IllegalArgumentException(
            "value cannot be less than " + MIN + "."); //$NON-NLS-1$ //$NON-NLS-2$
      if(value > MAX)
         throw new IllegalArgumentException(
            "value cannot be more than " + MAX + "."); //$NON-NLS-1$ //$NON-NLS-2$
      if(value == -1)
      {
         m_Button.setSelection(true);
         m_Spinner.setEnabled(false);        
      }
      else
      {
         m_Button.setSelection(false);
         m_Spinner.setEnabled(true);
         m_Spinner.setSelection(value);
      }
   }
   
   /**
    * @return the labels text value
    */
   public String getLabelText()
   {
      return m_Label.getText();
   }
   
   /**
    * Register a selection listener with this control.
    * @param listener the selection listener, cannot be <code>null</code>.
    */
   public void addSelectionListener(SelectionListener listener)
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null."); //$NON-NLS-1$
      if(!m_selectionListeners.contains(listener))
      {
         m_selectionListeners.add(listener);
      }
   }
   
   /**
    * Removes a selection listener from this control.
    * @param listener the selection listener, cannot be <code>null</code>.
    */
   public void removeSelectionListener(SelectionListener listener)
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null."); //$NON-NLS-1$
      if(m_selectionListeners.contains(listener))
      {
         m_selectionListeners.remove(listener);
      }
   }
   
   /**
    * Fires a <code>SelectionEvent</code> for all registered 
    * <code>SelectionListeners</code>.
    */
   private void fireSelectionEvent()
   {
      Event e = new Event();
      e.item = this;
      e.widget = this;     
      SelectionEvent event = new SelectionEvent(e);
      for(SelectionListener listener : m_selectionListeners)
      {
        listener.widgetSelected(event);
      }
   }
   
   /**
    * Add a focus listener to be notified when a focus
    * event occurs.
    * @param listener cannot be <code>null</code>.
    */
   @Override
   public void addFocusListener(FocusListener listener)
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null"); //$NON-NLS-1$
      if(!m_focusListeners.contains(listener))
         m_focusListeners.add(listener);
   }
   
   /**
    * Removes the specified selection listener
    * @param listener cannot be <code>null</code>.
    */
   @Override
   public void removeFocusListener(FocusListener listener)
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null"); //$NON-NLS-1$
      if(m_focusListeners.contains(listener))
         m_focusListeners.remove(listener);
   }
   
   /**
    * Fires a <code>FocusEvent</code> for all registered 
    * <code>FocusListeners</code>.
    */
   @SuppressWarnings("unused")
   private void fireFocusEvent(boolean gained)
   {
      Event e = new Event();
      e.item = this;
      e.widget = this;     
      FocusEvent event = new FocusEvent(e);
      for(FocusListener listener : m_focusListeners)
      {
        if(gained)
           listener.focusGained(event);
        else
           listener.focusLost(event);
      }
   }

   @Override
   public void dispose()
   {
      super.dispose();
   }

   @Override
   protected void checkSubclass()
   {
      // Disable the check that prevents subclassing of SWT components
   }
   
   //Controls
   private Button m_Button;
   private Spinner m_Spinner;
   private Label m_Label;
   
   /**
    * List of all registered selection listeners
    */
   private List<SelectionListener> m_selectionListeners = 
      new ArrayList<SelectionListener>();
   
   /**
    * List of focus listeners
    */
   private java.util.List<FocusListener> m_focusListeners = 
      new ArrayList<FocusListener>();
   
   //Constants
   private static final int MIN = 1;
   private static final int MAX = 9999;

}
