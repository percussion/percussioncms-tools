/******************************************************************************
*
* [ PSMuttBoxControl.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.controls;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

import java.io.File;
import java.util.ArrayList;

/**
 * A control comprised of a multi select list with a control
 * on top to allow additions to be made to the list as well as
 * a delete button to remove items.
 * 
 * So why is it called a Mutt box?? I don't know. I could not
 * think of another name and this just sounded catchy.
 */
public class PSMuttBoxControl extends Composite
{  
   

   /**
    * Create the composite
    * @param parent
    * @param labelText the label that will appear at the top
    * of the control. May be <code>null</code> or empty.
    * @param type one of the TYPE_XXX options
    */
   public PSMuttBoxControl(Composite parent, String labelText, int type)
   {
      super(parent, SWT.NONE);      
      m_type = type;
      setLayout(new FormLayout());
            
      m_label = new Label(this, SWT.NONE);
      m_label.setText(StringUtils.defaultString(labelText));
      
      m_addButton = PSButtonFactory.createAddButton(this);
      m_addButton.setToolTipText("Add item to list");
      m_addButton.setEnabled(false);      
      m_addButton.addSelectionListener(new SelectionAdapter()
         {
             /* 
              * @see org.eclipse.swt.events.SelectionAdapter#
              * widgetSelected(org.eclipse.swt.events.SelectionEvent)
              */
             @SuppressWarnings({"synthetic-access","unchecked"})
            @Override
             public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e)
             {
                java.util.List list = 
                   (java.util.List)m_listViewer.getInput();
                if(m_type == TYPE_FILE_CHOOSER)
               {
                  String path = m_textBox.getTextControl().getText();
                  if(StringUtils.isNotBlank(path))
                  {
                     if(!preAdd())
                        return;
                     if(!list.contains(path))
                     {
                        list.add(path);
                        m_listViewer.setInput(list);
                        fireSelectionEvent();
                     }
                  }
               }
               else if(m_type == TYPE_DROP && 
                  m_combo.getSelectionIndex() != -1)
               {
                  java.util.List combolist = 
                     (java.util.List)m_comboViewer.getInput();
                  StructuredSelection selection = 
                     (StructuredSelection)m_comboViewer.getSelection();
                  if(!list.contains(selection.getFirstElement()))
                  {
                     if(!preAdd())
                        return;
                     list.add(selection.getFirstElement());
                     m_listViewer.setInput(list);
                     // Remove from combo as this can no longer be selected
                     combolist.remove(selection.getFirstElement());
                     m_comboViewer.setInput(combolist);
                     fireSelectionEvent();
                  }
               }
             }
          });
      
      m_deleteButton = PSButtonFactory.createDeleteButton(this);
      m_deleteButton.setToolTipText("Remove selected item(s)");
      m_deleteButton.setEnabled(false);
      m_deleteButton.addSelectionListener(new SelectionAdapter()
        {
            /* 
             * @see org.eclipse.swt.events.SelectionAdapter#
             * widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @SuppressWarnings({"synthetic-access","unchecked"})
            @Override
            public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e)
            {
               java.util.List list = 
                  (java.util.List)m_listViewer.getInput();
               boolean selectionChanged = false;
               if(m_list.getSelectionCount() > 0)
               {
                  if(!preDelete())
                     return;
                  StructuredSelection selection = 
                     (StructuredSelection)m_listViewer.getSelection();
                  for(Object obj : selection.toList())
                  {
                     //Remove from list
                     list.remove(obj);
                     m_listViewer.setInput(list);
                     selectionChanged = true;
                     if(m_type == TYPE_DROP)
                     {
                        // Add back to combo
                        java.util.List combolist = 
                           (java.util.List)m_comboViewer.getInput();
                        combolist.add(obj);
                        m_comboViewer.setInput(combolist);
                     }
                  }
               }
               if(selectionChanged)
               {
                  handleEnableButtons();
                  fireSelectionEvent();
               }
            }
         });
      
      Control control = null;
      if(type == TYPE_DROP)
      {
         m_comboViewer = new ComboViewer(this, SWT.READ_ONLY);
         m_comboViewer.setContentProvider(new PSDefaultContentProvider());
         m_comboViewer.setSorter(new ViewerSorter());
         m_comboViewer.setInput(new ArrayList());
         m_combo = m_comboViewer.getCombo();
         m_combo.addFocusListener(new InternalFocusListener());
         m_combo.addSelectionListener(new SelectionAdapter() {
            @Override
            @SuppressWarnings("synthetic-access")
            public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e)
            {
               handleEnableButtons();
               int index = m_combo.getSelectionIndex();
               m_combo.setToolTipText(index == -1 ? "" : m_combo.getItem(index));               
            }
         });
         control = m_combo;
      }
      else if(type == TYPE_FILE_CHOOSER)
      {
         m_textBox = new PSElipseButtonTextComposite(this);
         m_textBox.getButton().setToolTipText("Browse for a file");
         m_textBox.getButton().addSelectionListener(new SelectionAdapter()
            {

               /* 
                * @see org.eclipse.swt.events.SelectionAdapter#
                * widgetSelected(org.eclipse.swt.events.SelectionEvent)
                */
               @SuppressWarnings({"synthetic-access","unchecked"})
               @Override
               public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e)
               {
                  FileDialog dialog = 
                     new FileDialog(getShell(), SWT.MULTI);
                  if(m_fileFilter != null)
                     dialog.setFilterExtensions(m_fileFilter);
                  String filename = m_textBox.getTextControl().getText();
                  if(StringUtils.isNotBlank(filename))
                     dialog.setFileName(filename);
                  String status = dialog.open();
                  if(status != null)
                  {
                     m_textBox.getTextControl().setText(status);
                     // Send all selections to multi-select
                     java.util.List list = 
                        (java.util.List)m_listViewer.getInput();
                     
                     boolean selectionChanged = false; 
                     String folder = dialog.getFilterPath();
                     for(String path : dialog.getFileNames())
                      {
                          path = folder + File.separator + path;
                          if(!list.contains(path))
                          {
                             list.add(path);
                             selectionChanged = true;      
                             
                          }
                       }
                      if(selectionChanged)
                      {
                         m_listViewer.setInput(list);
                         fireSelectionEvent();
                      }
                    
                  }
               }
               
            });
         m_textBox.getTextControl().addFocusListener(
            new InternalFocusListener());
         m_textBox.getTextControl().addModifyListener(new ModifyListener()
            {

               @SuppressWarnings("synthetic-access")
               public void modifyText(ModifyEvent e)
               {
                  Text tcontrol = (Text)e.getSource();
                  tcontrol.setToolTipText(tcontrol.getText());
                  if(StringUtils.isNotBlank(tcontrol.getText()))
                     m_addButton.setEnabled(m_combo != null);
               }
            
            });
         control = m_textBox;
      }
      
      m_listViewer = new ListViewer(this, 
         SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL);
      m_listViewer.setContentProvider(new PSDefaultContentProvider());
      m_listViewer.setSorter(new ViewerSorter());
      m_listViewer.setInput(new ArrayList());
      m_list = m_listViewer.getList();
      m_list.addFocusListener(new InternalFocusListener());
      m_list.addSelectionListener(new SelectionAdapter()
         {

            /* 
             * @see org.eclipse.swt.events.SelectionAdapter#
             * widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @SuppressWarnings("synthetic-access")
            @Override
            public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e)
            {
               handleEnableButtons();
            }
            
         });
      
      final FormData formData = new FormData(); 
      formData.top = new FormAttachment(0, 0);
      formData.left = new FormAttachment(0, 0);
      if(StringUtils.isBlank(labelText))
         formData.bottom = new FormAttachment(0, 0);
      m_label.setLayoutData(formData);
     
      final FormData formData_1 = new FormData();
      formData_1.right = new FormAttachment(m_addButton, -5, SWT.LEFT);
      formData_1.top = new FormAttachment(m_label, 0, SWT.BOTTOM);
      formData_1.left = new FormAttachment(m_label, 0, SWT.LEFT);
      control.setLayoutData(formData_1);
      
      final FormData formData_4 = new FormData();
      formData_4.top = new FormAttachment(m_label, 0, SWT.BOTTOM);
      formData_4.right = new FormAttachment(m_deleteButton, -5, SWT.LEFT);
      formData_4.height = 21;
      formData_4.width = 21;
      m_addButton.setLayoutData(formData_4);
      
      final FormData formData_2 = new FormData();
      formData_2.top = new FormAttachment(m_label, 0, SWT.BOTTOM);
      formData_2.right = new FormAttachment(100, 0);
      formData_2.height = 21;
      formData_2.width = 21;
      m_deleteButton.setLayoutData(formData_2);
      
      
      final FormData formData_3 = new FormData();
      formData_3.bottom = new FormAttachment(100, 0);
      formData_3.right = new FormAttachment(m_deleteButton, 0, SWT.RIGHT);
      formData_3.top = new FormAttachment(m_deleteButton, 5, SWT.BOTTOM);
      formData_3.left = new FormAttachment(m_combo, 0, SWT.LEFT);
      m_list.setLayoutData(formData_3);
      
      java.util.List<Control> tabList = new ArrayList<Control>();
      if(type == TYPE_DROP)
         tabList.add(m_combo);
      else
         tabList.add(m_textBox);
      tabList.add(m_addButton);
      tabList.add(m_deleteButton);
      tabList.add(m_list);
      setTabList(tabList.toArray(new Control[tabList.size()]));
      //
   }
   
   /**
    * Handles enableing and disabling the add and
    * delete buttons.
    */
   private void handleEnableButtons()
   {
      m_deleteButton.setEnabled(m_list.getSelectionCount() > 0);
      m_addButton.setEnabled(m_combo != null && m_combo.getSelectionIndex() != -1);
   }
   
   /**
    * @return the selections currently appearing in the multi
    * select list for this control. Never <code>null</code>,
    * may be empty.
    */
   public java.util.List getSelections()
   {
      return (java.util.List)m_listViewer.getInput();
   }
   
   /**
    * Sets the selections that are showing in the multi
    * select list for this control. This method will also
    * automatically remove any selections from the choice combo
    * if this control is of TYPE_DROP.
    * @param selections may be <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   public void setSelections(java.util.List selections)
   {
      if(selections == null)
         selections = new ArrayList();
      java.util.List choices = null;
      if(m_type != TYPE_FILE_CHOOSER)
         choices = (java.util.List)m_comboViewer.getInput();
      for(Object selection : selections)
      {
         if(choices != null && choices.contains(selection))
            choices.remove(selection);
      }
      if(choices != null)
         m_comboViewer.setInput(new ArrayList(choices));
      m_listViewer.setInput(new ArrayList(selections));
   }
   
   /**
    * Method to allow a hook to do something before the
    * add action occurs.
    * @return if <code>true</code> then add action will be performed.
    */
   protected boolean preAdd()
   {
      return true;
   }
   
   /**
    * Method to allow a hook to do something before the
    * delete action occurs.
    * @return if <code>true</code> then delete action will be performed.
    */
   protected boolean preDelete()
   {
      return true;
   }
   
   /**
    * Adds choices to the dop list if this control is of
    * TYPE_DROP. Will not add selections that already appear
    * in the multi select list.
    * @param choices may be <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   public void setChoices(java.util.List choices)
   {
      if(m_type == TYPE_FILE_CHOOSER)
         return;
      java.util.List choiceList = new ArrayList();
      if(choices != null && !choices.isEmpty())
      {
         java.util.List selected = 
            (java.util.List)m_listViewer.getInput();
         for(Object choice : choices)
         {
            if(selected != null && selected.contains(choice))
               continue;
            choiceList.add(choice);
         }
      }
      
      m_comboViewer.setInput(choiceList);
   }
   
   /**
    * Set the allowed extensions for the file chooser.
    * Only useful if the control is of type
    * TYPE_FILE_CHOOSER else ignored.
    * @param filters array of extension filter strings, 
    * may be <code>null</code>.
    */    
   public void setFileExtFilter(String[] filters)
   {
      m_fileFilter = filters;
   }
   
   /**
    * The label provider that is used with the multi select
    * list and the drop down control if the control is of 
    * type TYPE_DROP. Generally no label provider is needed if
    * the underlying objects will be strings which happens to 
    * be the case for TYPE_FILE_CHOOSER.
    * 
    * @param provider cannot be <code>null</code>.
    */
   public void setLabelProvider(ILabelProvider provider)
   {
      if(provider == null)
         throw new IllegalArgumentException("provider cannot be null.");
      if(m_type != TYPE_FILE_CHOOSER)
         m_comboViewer.setLabelProvider(provider);
      m_listViewer.setLabelProvider(provider);
   }
   
   /**
    * Add a selection listener to be notified when a selection
    * event occurs.
    * @param listener cannot be <code>null</code>.
    */
   public void addSelectionListener(SelectionListener listener)
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null"); //$NON-NLS-1$
      if(!m_selectionListeners.contains(listener))
         m_selectionListeners.add(listener);
   }
   
   /**
    * Removes the specified selection listener
    * @param listener cannot be <code>null</code>.
    */
   public void removeSelectionListener(SelectionListener listener)
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null"); //$NON-NLS-1$
      if(m_selectionListeners.contains(listener))
         m_selectionListeners.remove(listener);
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

   /* 
    * @see org.eclipse.swt.widgets.Composite#setFocus()
    */
   @Override
   public boolean setFocus()
   {
      if(m_type == TYPE_DROP)
         return m_combo.setFocus();
      return m_textBox.setFocus();
   }

   /* 
    * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
    */
   @Override
   public void setEnabled(boolean enabled)
   {
      m_enabled = enabled;
      for(Control control : getChildren())
      {
         control.setEnabled(enabled);
      }
      Color color = enabled 
         ? getDisplay().getSystemColor(SWT.COLOR_WHITE)
            : getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
      m_list.setBackground(color);
      if(enabled)
         handleEnableButtons();
   }   

   /* 
    * @see org.eclipse.swt.widgets.Control#isEnabled()
    */
   @Override
   public boolean isEnabled()
   {
      return m_enabled;
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
   
   class InternalFocusListener implements FocusListener
   {

      @SuppressWarnings("synthetic-access")
      public void focusGained(@SuppressWarnings("unused") FocusEvent e)
      {
         fireFocusEvent(true);
         
      }

      @SuppressWarnings("synthetic-access")
      public void focusLost(@SuppressWarnings("unused") FocusEvent e)
      {
         fireFocusEvent(false);         
      }
      
   }
   
   //controls
   private List m_list;
   private ListViewer m_listViewer;
   private Button m_addButton;
   private Button m_deleteButton;
   private Combo m_combo;
   private ComboViewer m_comboViewer;
   private Label m_label;
   private PSElipseButtonTextComposite m_textBox;
   
   /**
    * The control type, one of the TYPE_XXX constants, set
    * in the ctor.
    */
   private int m_type;
   
   /**
    * List of selection listeners
    */
   private java.util.List<SelectionListener> m_selectionListeners = 
      new ArrayList<SelectionListener>();
   
   /**
    * List of focus listeners
    */
   private java.util.List<FocusListener> m_focusListeners = 
      new ArrayList<FocusListener>();
   
   /**
    * Extension file filter string array, set in
    * {@link #setFileExtFilter(String[])}. May be <code>null</code>
    * or empty.
    */
   private String[] m_fileFilter;
   
   /**
    * Flag indicating that this control is enabled.
    */
   private boolean m_enabled = true;
   
   /**
    * Type constant that indicates the the new item entry
    * control will be a single choice drop down list.
    */
   public static final int TYPE_DROP = 0;
   
   /**
    * Type constant that indicates the the new item entry
    * control will be a text box and a browse button that
    * when pressed will launch a file chooser dialog.
    */
   public static final int TYPE_FILE_CHOOSER = 1;

}
