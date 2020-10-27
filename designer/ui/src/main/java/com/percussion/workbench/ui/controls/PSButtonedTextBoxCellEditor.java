/******************************************************************************
 *
 * [ PSButtonedTextBoxCellEditor.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.controls;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import java.text.MessageFormat;

/**
 * A cell editor that is comprised of a <code>Text</code> control with a
 * button on the right side of it.
 */
public class PSButtonedTextBoxCellEditor extends CellEditor
{
   public PSButtonedTextBoxCellEditor(Composite parent, int style)
   {
      super(parent, style);
   }   

   /* 
    * @see org.eclipse.jface.viewers.CellEditor#createControl(
    * org.eclipse.swt.widgets.Composite)
    */
   protected Control createControl(Composite parent)
   {
      Composite comp = new Composite(parent, SWT.NONE);
      comp.setLayout(new FormLayout());

      m_button = new Button(comp, SWT.NONE);
      final FormData formData_1 = new FormData();
      formData_1.bottom = new FormAttachment(100, 0);
      formData_1.top = new FormAttachment(0, 0);
      formData_1.right = new FormAttachment(100, 0);
      m_button.setLayoutData(formData_1);
      m_button.setText("...");

      m_text = new Text(comp, getStyle());
      final FormData formData = new FormData();
      formData.bottom = new FormAttachment(100, 0);
      formData.top = new FormAttachment(0, 0);
      formData.right = new FormAttachment(m_button, 0, SWT.LEFT);
      formData.left = new FormAttachment(0, 0);
      m_text.setLayoutData(formData);
      
      m_text.addKeyListener(new KeyAdapter()
      {
         // hook key pressed - see PR 14201
         @Override
         public void keyPressed(KeyEvent e)
         {
            keyReleaseOccured(e);
         }
      });
          
      
      m_text.addTraverseListener(new TraverseListener()
      {
         public void keyTraversed(TraverseEvent e)
         {
            if (e.detail == SWT.TRAVERSE_ESCAPE
                  || e.detail == SWT.TRAVERSE_RETURN)
            {
               e.doit = false;
            }
         }
      });
      
      m_text.addFocusListener(new FocusAdapter()
      {
         @Override
         @SuppressWarnings("unused")
         public void focusLost(FocusEvent e)
         {
            if (m_inControl)
               PSButtonedTextBoxCellEditor.this.focusLost();
            m_inControl = false;
         }
      });
      
      m_text.addMouseListener(new MouseAdapter()
      {
         
         /* 
          * @see org.eclipse.swt.events.MouseAdapter#mouseDown(
          * org.eclipse.swt.events.MouseEvent)
          */
         @SuppressWarnings("unused")
         @Override
         public void mouseDown(MouseEvent e)
         {
            m_inControl = true;
         }
         
      });
      
      m_button.addFocusListener(new FocusAdapter()
      {
         @Override
         @SuppressWarnings("unused")
         public void focusLost(FocusEvent e)
         {
            if (m_inControl)
               PSButtonedTextBoxCellEditor.this.focusLost();
            m_inControl = false;
         }
      });
      m_button.addMouseListener(new MouseAdapter()
      {
         /* 
          * @see org.eclipse.swt.events.MouseAdapter#mouseDown(
          * org.eclipse.swt.events.MouseEvent)
          */
         @SuppressWarnings("unused")
         @Override
         public void mouseDown(MouseEvent e)
         {
            m_inControl = true;
         }
         
      });

      return comp;
   }

   /* 
    * @see org.eclipse.jface.viewers.CellEditor#doGetValue()
    */
   public Object doGetValue()
   {
     return m_text.getText();
   }

   
   /* 
    * @see org.eclipse.jface.viewers.CellEditor#doSetFocus()
    */
   protected void doSetFocus()
   {
      m_text.setFocus();      
   }

   
   /* 
    * @see org.eclipse.jface.viewers.CellEditor#doSetValue(java.lang.Object)
    */
   public void doSetValue(Object value)
   {
      if(value == null)
         return;
      if(!(value instanceof String))
         throw new IllegalArgumentException("value must be a string.");
      m_text.setText((String)value);
      
   }
   
   /**
    *  @return the <code>Button</code> control contained in this
    *  cell editor. Never <code>null</code>.
    */
   public Button getButton()
   {
      return m_button;
   }
   
   /**
    *  @return the <code>Text</code> control contained in this
    *  cell editor. Never <code>null</code>.
    */
   public Text getTextControl()
   {
      return m_text;
   }
   
   /**
    * The <code>PSButtonedTextBoxCellEditor</code> implementation of
    * this <code>CellEditor</code> framework method sets the 
    * minimum width of the cell.  The minimum width is 10 characters
    * if <code>Text</code> is not <code>null</code> or <code>disposed</code>
    * eles it is 60 pixels to make sure the arrow button and some text is visible.
    */
   @Override
   public LayoutData getLayoutData() {
       LayoutData layoutData = super.getLayoutData();
       if ((m_text == null) || m_text.isDisposed())
           layoutData.minimumWidth = 60;
       else {
           // make the comboBox 10 characters wide
           GC gc = new GC(m_text);
           layoutData.minimumWidth = (gc.getFontMetrics()
                   .getAverageCharWidth() * 10) + 10;
           gc.dispose();
       }
       return layoutData;
   }
   
   /**
    * Applies the currently selected value and deactiavates the cell editor
    */
   @SuppressWarnings("unchecked")
   void applyEditorValueAndDeactivate()
   {      
      
      Object newValue = doGetValue();
      markDirty();
      boolean isValid = isCorrect(newValue);
      setValueValid(isValid);
      if (!isValid)
      {
         // try to insert the current value into the error message.
         setErrorMessage(MessageFormat.format(getErrorMessage(),
               new Object[] { newValue }));
      }
      fireApplyEditorValue();
      deactivate();
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.jface.viewers.CellEditor#focusLost()
    */
   @Override
   protected void focusLost()
   {
      if (isActivated())
      {
         applyEditorValueAndDeactivate();
      }
   }

   /*
    *  (non-Javadoc)
    * @see org.eclipse.jface.viewers.CellEditor#keyReleaseOccured(org.eclipse.swt.events.KeyEvent)
    */
   @Override
   protected void keyReleaseOccured(KeyEvent keyEvent)
   {
      if (keyEvent.character == '\u001b')
      { // Escape character
         fireCancelEditor();
      }
      else if (keyEvent.character == '\t' || keyEvent.keyCode == 13)
      { // tab key
         applyEditorValueAndDeactivate();
      }
   }

   
   /**
    * The text control contained in this control.
    * Initialized in {@link #createControl(Composite)}
    * never <code>null</code> after that.
    */
   private Text m_text;
   
   /**
    * The button contained in this control.
    * Initialized in {@link #createControl(Composite)}
    * never <code>null</code> after that.
    */
   private Button m_button;
   
   /**
    * Flag indicating that our mouse is still within this
    * cell editor control.
    */
   private boolean m_inControl;
   

}
