/******************************************************************************
 *
 * [ PSValueSelectorCellEditor.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.controls;

import com.percussion.E2Designer.DTTextLiteral;
import com.percussion.E2Designer.IDataTypeInfo;
import com.percussion.E2Designer.OSBackendDatatank;
import com.percussion.E2Designer.ValueSelectorDialogHelper;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.editors.dialog.PSValueSelectorDialog;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * A cell editor that is comprised of a <code>Text</code> control with a
 * button on the right side of it.
 * <p>
 * The <code>getValue</code> method of the calling class's
 * {@link org.eclipse.jface.viewers.ICellModifier ICellModifier} implementation
 * must return an {@link IPSReplacementValue}.
 */
public class PSValueSelectorCellEditor extends DialogCellEditor
{
   /**
    * The only ctor.
    * 
    * @param parent The table that will own this editor. Never
    * <code>null</code>.
    * 
    * @param onlyLiterals If <code>true</code>, then only the 'Literal'
    * replacement value type will be shown to the user, otherwise, all types
    * will be shown.
    */
   public PSValueSelectorCellEditor(Table parent, boolean onlyLiterals)
   {
      super(parent);
      if (null == parent)
      {
         // it's not clear if the super classes allow null or check it
         throw new IllegalArgumentException("parent cannot be null");  
      }
      m_table = parent;
      m_allowOnlyLiteralDataTypes = onlyLiterals;
   }   

   /* 
    * @see org.eclipse.jface.viewers.CellEditor#createControl(
    * org.eclipse.swt.widgets.Composite)
    */
   @SuppressWarnings("synthetic-access")
   protected Control createContents(Composite parent)
   {            
      m_text = new Text(parent, getStyle());      
      m_text.addKeyListener(new KeyAdapter() {
         // hook key pressed - see PR 14201  
         
         public void keyPressed(KeyEvent e) {
            keyReleaseOccured(e);
            
            // as a result of processing the above call, clients may have
            // disposed this cell editor
            if ((getControl() == null) || getControl().isDisposed())
               return;
            checkSelection(); // see explaination below
            checkDeleteable();
            checkSelectable();
         }
      });
      m_text.addTraverseListener(new TraverseListener() {
         public void keyTraversed(TraverseEvent e) {
            if (e.detail == SWT.TRAVERSE_ESCAPE
               || e.detail == SWT.TRAVERSE_RETURN) {
               e.doit = false;
            }
         }
      });
      // We really want a selection listener but it is not supported so we
      // use a key listener and a mouse listener to know when selection changes
      // may have occurred
      m_text.addMouseListener(new MouseAdapter() {
         public void mouseUp(@SuppressWarnings("unused") MouseEvent e) {
            checkSelection();
            checkDeleteable();
            checkSelectable();
         }
      });
      m_text.addFocusListener(new FocusAdapter()
         {
         public void focusLost(@SuppressWarnings("unused") FocusEvent e)
         {            
            if(!m_inButton)
            PSValueSelectorCellEditor.this.focusLost();
         }
         });
      m_text.setFont(parent.getFont());
      m_text.setBackground(parent.getBackground());
      m_text.setText("");//$NON-NLS-1$
      m_text.addModifyListener(getModifyListener());
            
      return m_text;
   }

   /* 
    * @see org.eclipse.jface.viewers.CellEditor#doGetValue()
    */
   public Object doGetValue()
   {
     return m_value;
   }
   
   /* 
    * @see org.eclipse.jface.viewers.CellEditor#fireApplyEditorValue()
    */
   @Override
   protected void fireApplyEditorValue()
   {
      super.fireApplyEditorValue();
      //doSetValue("");
   }

   /* 
    * @see org.eclipse.jface.viewers.CellEditor#doSetFocus()
    */
   protected void doSetFocus()
   {
      if (m_text != null) {
         m_text.selectAll();
         m_text.setFocus();
         checkSelection();
         checkDeleteable();
         checkSelectable();
     }      
   }

   
   /* 
    * @see org.eclipse.jface.viewers.CellEditor#doSetValue(java.lang.Object)
    */
   public void doSetValue(Object value)
   {
      setDataTypeValue(value);
      m_text.removeModifyListener(getModifyListener());
      m_text.setText(m_value == null ? "" : m_value.getValueText());      
      m_text.addModifyListener(getModifyListener());
   }
   
   /**
    * Sets the value of the underlying data type object, i.e the replacement
    * value.
    * 
    * @param value Expected to be a <code>String</code> or
    * <code>IPSReplacementValue</code>. May be <code>null</code>.
    */
   private void setDataTypeValue(Object value)
   {
      Object dt = null;
      if(value instanceof String)
      {
         if(m_value == null || (StringUtils.isBlank((String)value)))
            m_value = (IPSReplacementValue) new DTTextLiteral().create("");
         IDataTypeInfo temp = convertToDTObject(m_value);
         dt = temp.create((String)value);
      }
      else
      {
         dt = value;
      }
      m_value = (IPSReplacementValue) dt;
   }
   
   /**
    * Helper method to get the appropriate designer data type
    * object from the specified replacement value server
    * object
    * @param obj assumed not <code>null</code>.
    * @return the data type object, Never <code>null</code>
    * if not found.
    */
   private IDataTypeInfo convertToDTObject(Object obj)
   {
      String thePackage = "com.percussion.E2Designer.";
      String oldclassname = obj.getClass().getName();
      oldclassname = oldclassname.substring(oldclassname.lastIndexOf('.') + 1);
      if(!oldclassname.startsWith("PS"))
         return null;
      String newclassname = thePackage + "DT" + oldclassname.substring(2);
      try
      {
         Class dtClass = Class.forName(newclassname);
         return (IDataTypeInfo)dtClass.newInstance();
      }
      catch (Exception e)
      {
         PSWorkbenchPlugin.handleException(null, null, null, e);
         throw new RuntimeException(e);
         
      }     
   }
   
   /* 
    * @see org.eclipse.jface.viewers.DialogCellEditor#createButton(
    * org.eclipse.swt.widgets.Composite)
    */
   protected Button createButton(Composite parent)
   {
      Button result = new Button(parent, SWT.DOWN);
      result.setText("..."); //$NON-NLS-1$
      result.addMouseTrackListener(new MouseTrackAdapter()
         {

            @SuppressWarnings("synthetic-access")
            public void mouseEnter(@SuppressWarnings("unused") MouseEvent e)
            {
               m_inButton = true;
               
            }

            @SuppressWarnings("synthetic-access")
            public void mouseExit(@SuppressWarnings("unused") MouseEvent e)
            {
               m_inButton = false; 
            }
         
         });
      return result;
   }
   
   /**
    * Return the modify listener.
    */
   private ModifyListener getModifyListener() {
       if (m_modifyListener == null) {
           m_modifyListener = new ModifyListener() {
               public void modifyText(ModifyEvent e) {
                   editOccured(e);
               }
           };
       }
       return m_modifyListener;
   }
   
      
   /**
    * Processes a modify event that occurred in this text cell editor. This
    * framework method performs validation and sets the error message
    * accordingly, and then reports a change via
    * <code>fireEditorValueChanged</code>. Subclasses should call this method
    * at appropriate times. Subclasses may extend or reimplement.
    * 
    * @param e the SWT modify event
    */
   protected void editOccured(@SuppressWarnings("unused") ModifyEvent e) {
       String value = m_text.getText();
       if (value == null)
           value = "";//$NON-NLS-1$
       Object typedValue = value;
       boolean oldValidState = isValueValid();
       boolean newValidState = isCorrect(typedValue);
       if (typedValue == null && newValidState)
           Assert.isTrue(false,
                   "Validator isn't limiting the cell editor's type range");//$NON-NLS-1$
       if (!newValidState) {
           // try to insert the current value into the error message.
           setErrorMessage(MessageFormat.format(getErrorMessage(),
                   new Object[] { value }));
       }
       setDataTypeValue(value);
       valueChanged(oldValidState, newValidState);
   }

   /**
    * Handles a default selection event from the text control by applying the editor
    * value and deactivating this cell editor.
    * 
    * @param event the selection event
    * 
    * @since 3.0
    */
   protected void handleDefaultSelection(
         @SuppressWarnings("unused") SelectionEvent event) 
   {
       // same with enter-key handling code in keyReleaseOccured(e);
       fireApplyEditorValue();
       deactivate();
   }
   
   /**
    * The <code>TextCellEditor</code> implementation of this
    * <code>CellEditor</code> method copies the
    * current selection to the clipboard. 
    */
   public void performCopy() {
       m_text.copy();
   }

   /**
    * The <code>TextCellEditor</code> implementation of this
    * <code>CellEditor</code> method cuts the
    * current selection to the clipboard. 
    */
   public void performCut() {
       m_text.cut();
       checkSelection();
       checkDeleteable();
       checkSelectable();
   }

   /**
    * The <code>TextCellEditor</code> implementation of this
    * <code>CellEditor</code> method deletes the
    * current selection or, if there is no selection,
    * the character next character from the current position. 
    */
   public void performDelete() {
       if (m_text.getSelectionCount() > 0)
           // remove the contents of the current selection
           m_text.insert(""); //$NON-NLS-1$
       else {
           // remove the next character
           int pos = m_text.getCaretPosition();
           if (pos < m_text.getCharCount()) {
               m_text.setSelection(pos, pos + 1);
               m_text.insert(""); //$NON-NLS-1$
           }
       }
       checkSelection();
       checkDeleteable();
       checkSelectable();
   }

   /**
    * The <code>TextCellEditor</code> implementation of this
    * <code>CellEditor</code> method pastes the
    * the clipboard contents over the current selection. 
    */
   public void performPaste() {
       m_text.paste();
       checkSelection();
       checkDeleteable();
       checkSelectable();
   }

   /**
    * The <code>TextCellEditor</code> implementation of this
    * <code>CellEditor</code> method selects all of the
    * current text. 
    */
   public void performSelectAll() {
       m_text.selectAll();
       checkSelection();
       checkDeleteable();
   }
   
   /**
    * The <code>TextCellEditor</code>  implementation of this 
    * <code>CellEditor</code> method returns <code>true</code> if 
    * the current selection is not empty.
    */
   public boolean isCopyEnabled() {
       if (m_text == null || m_text.isDisposed())
           return false;
       return m_text.getSelectionCount() > 0;
   }

   /**
    * The <code>TextCellEditor</code>  implementation of this 
    * <code>CellEditor</code> method returns <code>true</code> if 
    * the current selection is not empty.
    */
   public boolean isCutEnabled() {
       if (m_text == null || m_text.isDisposed())
           return false;
       return m_text.getSelectionCount() > 0;
   }

   /**
    * The <code>TextCellEditor</code>  implementation of this 
    * <code>CellEditor</code> method returns <code>true</code>
    * if there is a selection or if the caret is not positioned 
    * at the end of the m_text.
    */
   public boolean isDeleteEnabled() {
       if (m_text == null || m_text.isDisposed())
           return false;
       return m_text.getSelectionCount() > 0
               || m_text.getCaretPosition() < m_text.getCharCount();
   }

   /**
    * The <code>TextCellEditor</code>  implementation of this 
    * <code>CellEditor</code> method always returns <code>true</code>.
    */
   public boolean isPasteEnabled() {
       if (m_text == null || m_text.isDisposed())
           return false;
       return true;
   }

   /**
    * The <code>TextCellEditor</code>  implementation of this 
    * <code>CellEditor</code> method always returns <code>true</code>.
    */
   public boolean isSaveAllEnabled() {
       if (m_text == null || m_text.isDisposed())
           return false;
       return true;
   }
   
   /**
    * Returns <code>true</code> if this cell editor is
    * able to perform the select all action.
    * <p>
    * This default implementation always returns 
    * <code>false</code>.
    * </p>
    * <p>
    * Subclasses may override
    * </p>
    * @return <code>true</code> if select all is possible,
    *  <code>false</code> otherwise
    */
   public boolean isSelectAllEnabled() {
       if (m_text == null || m_text.isDisposed())
           return false;
       return m_text.getCharCount() > 0;
   }
   
   /* 
    * @see org.eclipse.jface.viewers.DialogCellEditor#openDialogBox(
    * org.eclipse.swt.widgets.Control)
    */
   @SuppressWarnings({"unchecked"})
   @Override
   protected Object openDialogBox(
         @SuppressWarnings("unused") Control cellEditorWindow)
   {
      //    Open value selector dialog
      final ValueSelectorDialogHelper helper = 
         new ValueSelectorDialogHelper((OSBackendDatatank)null, null);

      IPSReplacementValue val = m_value;
      List<IDataTypeInfo> types = new ArrayList();
      Enumeration e = helper.getDataTypes().elements();
      while(e.hasMoreElements())
      {
         IDataTypeInfo info = (IDataTypeInfo)e.nextElement();
         if(m_allowOnlyLiteralDataTypes &&
            info.getClass().getName().toLowerCase().indexOf("literal") == -1)
            continue;
         types.add(info);
      }
      PSValueSelectorDialog dialog = 
         new PSValueSelectorDialog(m_table.getShell(),
            types, null, val);
      
      int status = dialog.open();               
      if(status == Dialog.OK)
      {
         return dialog.getValue();
      }
      return null;
   }
     
   /**
    * Checks to see if the "deleteable" state (can delete/
    * nothing to delete) has changed and if so fire an
    * enablement changed notification.
    */
   private void checkDeleteable() {
       boolean oldIsDeleteable = m_isDeleteable;
       m_isDeleteable = isDeleteEnabled();
       if (oldIsDeleteable != m_isDeleteable) {
           fireEnablementChanged(DELETE);
       }
   }

   /**
    * Checks to see if the "selectable" state (can select)
    * has changed and if so fire an enablement changed notification.
    */
   private void checkSelectable() {
       boolean oldIsSelectable = m_isSelectable;
       m_isSelectable = isSelectAllEnabled();
       if (oldIsSelectable != m_isSelectable) {
           fireEnablementChanged(SELECT_ALL);
       }
   }

   /**
    * Checks to see if the selection state (selection /
    * no selection) has changed and if so fire an
    * enablement changed notification.
    */
   private void checkSelection() {
       boolean oldIsSelection = m_isSelection;
       m_isSelection = m_text.getSelectionCount() > 0;
       if (oldIsSelection != m_isSelection) {
           fireEnablementChanged(COPY);
           fireEnablementChanged(CUT);
       }
   }

   
   /**
    * The text control contained in this control. Initialized in
    * {@link #createControl(Composite)} never <code>null</code> after that.
    */
   private Text m_text;
   
   /**
    * May be <code>null</code>.
    */
   private IPSReplacementValue m_value;
      
   
   /**
    * State information for updating action enablement
    */
   private boolean m_isSelection = false;

   private boolean m_isDeleteable = false;

   private boolean m_isSelectable = false;
   
   private ModifyListener m_modifyListener;
   
   private Table m_table;
   
   private boolean m_inButton;
   
   private boolean m_allowOnlyLiteralDataTypes;
}
