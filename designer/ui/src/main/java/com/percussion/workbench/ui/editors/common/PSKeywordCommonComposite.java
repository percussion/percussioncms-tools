/******************************************************************************
 *
 * [ PSKeywordCommonComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.common;

import com.percussion.services.content.data.PSKeywordChoice;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.IPSNewRowObjectProvider;
import com.percussion.workbench.ui.controls.PSAbstractTableLabelProvider;
import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import com.percussion.workbench.ui.controls.PSSortableTable;
import com.percussion.workbench.ui.editors.wizards.PSWizardPageBase;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * This is a common composite for the keyword wizard coice list page and the keyword editor.
 * A flag is set in the ctor which indicates if this is a wizard or an editor
 * and the appropriate fields are shown.
 */
public class PSKeywordCommonComposite extends Composite 
   implements IPSUiConstants
{

   /**
    * Create the composite
    * @param parent the parent control, acannot be <code>null</code>.
    * @param style the style options for this composite
    * @param editor the wizard or editor that this composite resides in
    *  
    */
   public PSKeywordCommonComposite(Composite parent, int style,
      IPSDesignerObjectUpdater editor)
   {
      super(parent, style);
      setLayout(new FormLayout());
      if(editor == null)
         throw new IllegalArgumentException("editor cannot be null.");
      m_isWizard = (editor instanceof PSWizardPageBase);
      if(!m_isWizard)
      {
         m_commonComp = new PSNameLabelDesc(this, SWT.NONE, 
            "Keyword", //$NON-NLS-1$
            -1, 
            PSNameLabelDesc.SHOW_DESC | 
            PSNameLabelDesc.SHOW_NAME |
            PSNameLabelDesc.NAME_READ_ONLY |
            PSNameLabelDesc.LAYOUT_SIDE);
         final FormData formData_11 = new FormData();
         formData_11.left = new FormAttachment(0, 0);
         formData_11.right = 
            new FormAttachment(100, 0);
         formData_11.top = new FormAttachment(0, 0);
         m_commonComp.setLayoutData(formData_11);
         m_commonComp.getDescriptionText().setTextLimit(255);
      }
      
      m_choicesLabel = new Label(this, SWT.WRAP);
      final FormData formData = new FormData();
      
      
      if(m_isWizard)
      {
         formData.top = new FormAttachment(0, WIZARD_TOP_OFFSET);
         formData.left = new FormAttachment(0, COMMON_BORDER_OFFSET);
      }
      else
      {
         formData.top = new FormAttachment(m_commonComp, 15, SWT.BOTTOM);
         formData.left = new FormAttachment(10, 0);
         
      }
      m_choicesLabel.setLayoutData(formData);
      m_choicesLabel.setText(PSMessages.getString(
         "PSKeywordCommonComposite.label.choices"));       //$NON-NLS-1$
      
      // Create the label provider for this table
      ITableLabelProvider labelProvider = new PSAbstractTableLabelProvider()
         {
            
            public String getColumnText(Object element, int columnIndex)
            {
               Choice choice = (Choice)element;
               switch(columnIndex)
               {
                  case 0:
                     return StringUtils.defaultString(choice.label);
                  case 1:
                     return StringUtils.defaultString(choice.value);
                  case 2:
                     return StringUtils.defaultString(choice.description);
               }
               return ""; // should never get here
            }
         
         };
         
      // Create the new row object provider for this table
      IPSNewRowObjectProvider newRowProvider = new IPSNewRowObjectProvider()
         {

            public Object newInstance()
            {
               return new Choice();
            }

            public boolean isEmpty(Object obj)
            {
               if(!(obj instanceof Choice))
                  throw new IllegalArgumentException(
                     "The passed in object must be an instance of Choice.");
               Choice choice = (Choice)obj;
               return choice.isEmpty();
               
            }
         
         };   
      
      m_choicesTable = new PSSortableTable(
         this, labelProvider, newRowProvider, SWT.NONE,
         PSSortableTable.SHOW_ALL 
           | PSSortableTable.INSERT_ALLOWED
           | PSSortableTable.DELETE_ALLOWED);
      m_choicesTable.setCellModifier(new CellModifier(m_choicesTable));
      final FormData formData_14 = new FormData();
      if(m_isWizard)
      {
         formData_14.left = new FormAttachment(0, COMMON_BORDER_OFFSET);
         formData_14.right = new FormAttachment(100, -COMMON_BORDER_OFFSET);
      }
      else
      {
         formData_14.left = new FormAttachment(m_choicesLabel, 0, SWT.LEFT);
         formData_14.right = new FormAttachment(90, 0);
      }
      formData_14.top = new FormAttachment(m_choicesLabel, 0, SWT.BOTTOM);
      formData_14.height = 150;
      m_choicesTable.setLayoutData(formData_14);
      
      // Add listener to fill the value box upon row selection
      final Table table = m_choicesTable.getTable();
      m_choicesTable.addSelectionListener(new SelectionAdapter()
         {

            /* 
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
             * org.eclipse.swt.events.SelectionEvent)
             */
            @SuppressWarnings("synthetic-access") //$NON-NLS-1$
            @Override
            public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e)
            {
               // fixme: add row validation here
               if (table.getSelectionCount() == 0 ||
                    table.getSelectionCount() > 1)
               {
                  m_valueText.setText(""); //$NON-NLS-1$                  
               }
               else
               {
                  TableItem item = 
                     table.getItem(table.getSelectionIndices()[0]);
                  Choice choice = (Choice)item.getData();
                  String value = choice.value;
                  m_valueText.setText(value == null ? "" : value); //$NON-NLS-1$
                  
               }
            }
         
         });            
      
      final Label valueLabel = new Label(this, SWT.WRAP);
      final FormData formData_2 = new FormData();
      formData_2.left = new FormAttachment(m_choicesTable, 0, SWT.LEFT);
      formData_2.top = new FormAttachment(m_choicesTable, 20, SWT.BOTTOM);
      valueLabel.setLayoutData(formData_2);
      valueLabel.setText(PSMessages.getString("PSKeywordCommonComposite.label.value")); //$NON-NLS-1$
      
      m_valueText = new Text(this, SWT.V_SCROLL | SWT.BORDER | SWT.WRAP);
      m_valueText.setTextLimit(VALUE_MAX);
      m_valueText.setForeground(
         getDisplay().getSystemColor(SWT.COLOR_BLACK));
      m_valueText.setBackground(
         getDisplay().getSystemColor(SWT.COLOR_WHITE));
      
      m_valueText.setEnabled(false);
      // Add a modify listener to keep the cell editor in sync
      m_valueText.addModifyListener(new ModifyListener()
         {
            @SuppressWarnings("synthetic-access")
            public void modifyText(@SuppressWarnings("unused") ModifyEvent e)
            {               
               if(m_choicesTable.getTable()
                  .getSelectionIndices().length == 0)
                  return;
               TableItem item = 
                  m_choicesTable.getTable().getItem(
                     m_choicesTable.getTable().getSelectionIndices()[0]);
               Choice choice = (Choice)item.getData();
               if(!StringUtils.isBlank(m_valueText.getText()))
                  choice.value = m_valueText.getText();
               item.setText(1, m_valueText.getText());                           
            }         
         });
      // Use a focus listener to disable this control when focus
      // is lost.
      m_valueText.addFocusListener(new FocusAdapter()
         {
                     
            @Override
            @SuppressWarnings("synthetic-access")
            public void focusLost(@SuppressWarnings("unused") FocusEvent e)
            {               
               m_valueText.setEnabled(false);
            }            
          
         });
      // We need a listener to let us know when the mouse is in the 
      // value text box so that we know not to disable this control 
      // when focus is lost in the value cell editor and gained in the
      // value text box
      m_valueText.addMouseTrackListener(new MouseTrackAdapter()
         {

            @Override
            @SuppressWarnings("synthetic-access")
            public void mouseEnter(@SuppressWarnings("unused") MouseEvent e)
            {
               m_inValueText = true;               
            }

            @Override
            @SuppressWarnings("synthetic-access")
            public void mouseExit(@SuppressWarnings("unused") MouseEvent e)
            {
               m_inValueText = false;               
            }         
            
         });      
      
      
      final FormData formData_1 = new FormData();
      formData_1.right = new FormAttachment(m_choicesTable, 0, SWT.RIGHT);
      formData_1.top = new FormAttachment(valueLabel, 0, SWT.BOTTOM);
      formData_1.left = new FormAttachment(valueLabel, 0, SWT.LEFT);
      formData_1.height = DESCRIPTION_FIELD_HEIGHT;
      m_valueText.setLayoutData(formData_1);
      
      
      int sortable = PSSortableTable.IS_SORTABLE;
      CellEditor cEditor = new TextCellEditor(m_choicesTable.getTable(), SWT.NONE);
      Text labelText = (Text)cEditor.getControl();
      labelText.setTextLimit(LABEL_MAX);
      labelText.addModifyListener(new ModifyListener()
         {

            @SuppressWarnings("synthetic-access") //$NON-NLS-1$
            public void modifyText(@SuppressWarnings("unused") ModifyEvent e)
            {                 
               // fixme: add row validation here
            }
         
         });
      m_choicesTable.addColumn("PSKeywordCommonComposite.column.name.label",
         sortable, new ColumnWeightData(10, 100), cEditor,  SWT.LEFT); //$NON-NLS-1$
      
      m_valueCellEditor = new TextCellEditor(m_choicesTable.getTable(), SWT.NONE);     
      // Add listener to update value text box on each key stroke
      m_valueCellText = (Text)m_valueCellEditor.getControl();
      m_valueCellText.setTextLimit(VALUE_MAX);
      
      m_valueCellText.addModifyListener(new ModifyListener()
      {

         @SuppressWarnings("synthetic-access") //$NON-NLS-1$
         public void modifyText(ModifyEvent e)
         {               
            Text text = (Text)e.getSource();
            m_valueText.setText(text.getText());
            // fixme: Add row validation here
         }
      
      });
      // Use focus listener on cell editor to enable and disable
      // the value text box editor as needed
      m_valueCellText.addFocusListener(new FocusAdapter()
         {
            @Override
            @SuppressWarnings("synthetic-access")
            public void focusGained(@SuppressWarnings("unused") FocusEvent e)
            {               
               m_valueText.setEnabled(true);
               // fixme: Add row validation here
            }   
            
            @Override
            @SuppressWarnings("synthetic-access")
            public void focusLost(@SuppressWarnings("unused") FocusEvent e)
            {               
               
               if(!m_inValueText)
                  m_valueText.setEnabled(false);
            } 
         });
      m_choicesTable.addColumn("PSKeywordCommonComposite.column.name.value",
         sortable, new ColumnWeightData(8, 80), m_valueCellEditor, SWT.LEFT); //$NON-NLS-1$
      
      cEditor = new TextCellEditor(m_choicesTable.getTable(), SWT.NONE);
      ((Text)cEditor.getControl()).setTextLimit(DESC_MAX);
      m_choicesTable.addColumn("PSKeywordCommonComposite.column.name.desc",
         PSSortableTable.NONE,  //$NON-NLS-1$
         new ColumnWeightData(10, 100), cEditor, SWT.LEFT);
      
      
      //
   }
   
   /** 
    * @return the choices label text, never <code>null</code>.
    */
   public String getChoicesLabelText()
   {
      return m_choicesLabel.getText();
   }
   
   /** 
    * @return the choices table control, never <code>null</code>.
    */
   public PSSortableTable getChoicesTable()
   {
      return m_choicesTable;
   }
   
   /** 
    * @return the description label text, may be <code> null</code>.
    */
   public String getDescriptionLabelText()
   {
      if(m_commonComp == null)
         return null;
      return m_commonComp.getDescriptionLabelValue();
   }
   
   /** 
    * @return the description <code>Text</code> control, may be
    * <code>null</code>.
    */
   public Text getDescriptionControl()
   {
      if(m_commonComp == null)
         return null;
      return m_commonComp.getDescriptionText();
   }
   
   /**
    * @return the value box control, never <code>null</code>.
    */
   public Text getValueControl()
   {
      return m_valueText;
   }
   
   /**
    * @return the name control, may be <code>null</code>.
    */
   public Control getNameControl()
   {
      return m_commonComp.getNameText();
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
      
   
   /**
    * Cell modifier for the choice list table
    */
   class CellModifier implements ICellModifier
   {

      CellModifier(PSSortableTable comp)
      {
         mi_tableComp = comp;
      }
      /* 
       * @see org.eclipse.jface.viewers.ICellModifier#canModify(
       * java.lang.Object, java.lang.String)
       */
      public boolean canModify(@SuppressWarnings("unused") Object element,
         @SuppressWarnings("unused") String property)
      {
         return true;
      }

      /* 
       * @see org.eclipse.jface.viewers.ICellModifier#getValue(
       * java.lang.Object, java.lang.String)
       */
      @SuppressWarnings("synthetic-access") //$NON-NLS-1$
      public Object getValue(Object element, String property)
      {
         int col = m_choicesTable.getColumnIndex(property);
         Choice choice = (Choice)element;
         switch(col)
         {
            case 0:
               return StringUtils.defaultString(choice.label);
            case 1:
               return StringUtils.defaultString(choice.value);
            case 2:
               return StringUtils.defaultString(choice.description);
         }
         return "";
      }

      /* 
       * @see org.eclipse.jface.viewers.ICellModifier#modify(
       * java.lang.Object, java.lang.String, java.lang.Object)
       */
      @SuppressWarnings("synthetic-access") //$NON-NLS-1$
      public void modify(Object element, String property, Object value)
      {
         int col = m_choicesTable.getColumnIndex(property);
         TableItem item = (TableItem)element;
         Choice choice = (Choice)item.getData();
         switch(col)
         {
            case 0:
               choice.label = (String)value;
               break;
            case 1:               
               choice.value = (String)value;                  
               break;
            case 2:
               choice.description = (String)value;
               break;
         }
         mi_tableComp.refreshTable();
      }
      
      private PSSortableTable mi_tableComp;
      
   }
   
   /**
    * A little convenience class to hold the values for a row in the choices
    * table.
    */
   public class Choice
   {      
      public Choice(){}
      
      /**
       * Constructs a new <code>Choice</code> object representing a row in the
       * choices table.
       * 
       * @param label see {@link #label}
       * @param value see {@link #value}
       * @param desc see {@link #description}
       */
      public Choice(String label, String value, String desc)
      {
         this.label = label;
         this.value = value;
         this.description = desc;
      }
      
      /**
       * Constructs a new <code>Choice</code> object from a keyword choice
       * object.
       * 
       * @param kChoice the keyword choice object to construct from, not
       * <code>null</code>.
       */
      public Choice(PSKeywordChoice kChoice)
      {
         if(kChoice == null)
            throw new IllegalArgumentException("kChoice cannot be null.");
         label = kChoice.getLabel();
         value = kChoice.getValue();
         description = kChoice.getDescription();
         m_keywordChoice = kChoice;
      }
      
      /**
       * Determines whether a choice object is empty.  A choice object is empty
       * if its label is <code>null</code> or empty, or its value is
       * <code>null</code>.
       * 
       * @return <code>true</code> if the choice is empty, <code>false</code>
       * otherwise.
       */
      public boolean isEmpty()
      {         
        return (StringUtils.isBlank(label) || value == null) ; 
      }
      
      /**
       * Converts the choice object to a keyword choice.
       * 
       * @return the choice object as a keyword choice, never <code>null</code>.
       */
      public PSKeywordChoice toKeywordChoice()
      {
         if(isEmpty())
            throw new IllegalStateException("Label cannot be null or empty " +
                  "and value cannot be null.");
         if(m_keywordChoice == null)
            m_keywordChoice = new PSKeywordChoice();
         m_keywordChoice.setLabel(label);
         m_keywordChoice.setValue(value);
         m_keywordChoice.setDescription(description);
         
         return m_keywordChoice;
      }
      
      /**
       * The text that is shown to the end user, may be <code>null</code> or
       * empty. 
       */
      public String label;
      
      /**
       * The text that is stored when a choice value is persisted as the value
       * of a field, may be <code>null</code> or empty.
       */
      public String value;
      
      /**
       * The text that describes the choice to the end user, may be
       * <code>null</code> or empty.
       */
      public String description;
      
      /**
       * Used when constructing from and converting to a keyword choice object,
       * may be <code>null</code>.
       */
      private PSKeywordChoice m_keywordChoice;
   }
   
   /**
    * The common composite used to display the description, 
    * initialized in the ctor, will be <code>null</code> if
    * <code>isWizard</code> was <code>true</code>.
    */
   private PSNameLabelDesc m_commonComp;
   
   /**
    * The choice list table, initialized in the ctor, never <code>null</code>
    * after that.
    */
   private PSSortableTable m_choicesTable;
   
   /**
    * The choice label, initialized in the ctor, never <code>null</code>
    * after that.
    */
   private Label m_choicesLabel;
   
   /**
    * The value text box, initialized in the ctor, never <code>null</code>
    * after that.
    */
   private Text m_valueText;
   
   /**
    * The value text box for the value cell editor. Initialized in the ctor, 
    * never <code>null</code> after that.
    */
   private Text m_valueCellText;  
   
   /**
    * The value cell editor. Initialized in the ctor, 
    * never <code>null</code> after that.
    */
   private TextCellEditor m_valueCellEditor;  
   
   /**
    * Flag indicating that the mouse is in the value text box
    */
   private boolean m_inValueText;
      
   
   /**
    * Flag indicating that this composite is within a wizard. Set in ctor.
    */
   private boolean m_isWizard;
   
   /**
    * The maximum allowed text size for the value field
    */
   private static final int VALUE_MAX = 2100;
   
   /**
    * The maximum allowed text size for the label field
    */
   private static final int LABEL_MAX = 50;
   
   /**
    * The maximum allowed text size for the description field
    */
   private static final int DESC_MAX = 255;
   
   
  

}
