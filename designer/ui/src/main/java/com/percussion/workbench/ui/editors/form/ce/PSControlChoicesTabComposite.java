/******************************************************************************
 *
 * [ PSControlChoicesTabComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.E2Designer.LookupRequestDialog;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.catalogers.PSCatalogDatabaseTables;
import com.percussion.client.catalogers.PSCatalogDatasources;
import com.percussion.client.catalogers.PSSqlCataloger;
import com.percussion.conn.PSDesignerConnection;
import com.percussion.design.objectstore.PSChoiceTableInfo;
import com.percussion.design.objectstore.PSChoices;
import com.percussion.design.objectstore.PSDefaultSelected;
import com.percussion.design.objectstore.PSDisplayText;
import com.percussion.design.objectstore.PSEntry;
import com.percussion.design.objectstore.PSNullEntry;
import com.percussion.design.objectstore.PSUISet;
import com.percussion.design.objectstore.PSUrlRequest;
import com.percussion.services.content.data.PSKeyword;
import com.percussion.services.content.data.PSKeywordChoice;
import com.percussion.util.PSCollection;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSModelTracker;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.IPSNewRowObjectProvider;
import com.percussion.workbench.ui.controls.PSAbstractTableLabelProvider;
import com.percussion.workbench.ui.controls.PSDefaultContentProvider;
import com.percussion.workbench.ui.controls.PSRadioAndCheckBoxes;
import com.percussion.workbench.ui.controls.PSSortableTable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import static com.percussion.design.objectstore.PSChoices.SORT_ORDER_ASCENDING;
import static com.percussion.design.objectstore.PSChoices.SORT_ORDER_DESCENDING;
import static com.percussion.design.objectstore.PSChoices.SORT_ORDER_USER;
import static com.percussion.design.objectstore.PSDefaultSelected.TYPE_SEQUENCE;


public class PSControlChoicesTabComposite extends Composite
      implements
         IPSUiConstants,
         ISelectionChangedListener,
         SelectionListener,
         ModifyListener
{
   /**
    * The comparator used to sort list of string in ascending order.
    */
   private static class ComparatorAscending implements Comparator<String>
   {
      /**
       * Compares 2 strings lexicographically.
       * 
       * @param left left string, assumed not <code>null</code>.
       * @param right right string, assumed not <code>null</code>.
       */
      public int compare(String left, String right)
      {
         return left.compareTo(right);
      }
   }

   /**
    * The comparator used to sort list of string in descending order.
    */
   public static class ComparatorDeScending implements Comparator<String>
   {
      /**
       * Compares 2 strings lexicographically.
       * 
       * @param left left string, assumed not <code>null</code>.
       * @param right right string, assumed not <code>null</code>.
       */
      public int compare(String left, String right)
      {
         return right.compareTo(left);
      }
   }



   public PSControlChoicesTabComposite(Composite parent, int style,
         PSFieldTableRowDataObject rowData) 
   {
      super(parent, style);
      if (rowData == null)
      {
         throw new IllegalArgumentException("rowData must not be null");
      }
      setLayout(new FormLayout());
      m_rowData = rowData;
      createControls();
      loadControlValues();
   }
   private void createControls()
   {
      PSUISet uiSet = m_rowData.getDisplayMapping().getUISet();
      
      m_mainRadioComp = new PSRadioAndCheckBoxes(
            this, "", SWT.VERTICAL | SWT.RADIO);
      final FormData formData_1 = new FormData();
      formData_1.right = new FormAttachment(100,-10);
      formData_1.left = new FormAttachment(0, 10);
      formData_1.top = new FormAttachment(0, 20);
      m_mainRadioComp.setLayoutData(formData_1);
      m_mainRadioComp.addSelectionListener(this);
      
      //Create the composites for different radio boxes.
      Composite kwComp = new Composite(m_mainRadioComp,SWT.NONE);
      kwComp.setLayout(new FormLayout());
      
      final Label kwLbl = new Label(kwComp,SWT.NONE);
      kwLbl.setAlignment(SWT.RIGHT);
      final FormData formData_2 = new FormData();
      formData_2.right = new FormAttachment(RADIO_CHILD_LABEL_NUMERATOR,0);
      formData_2.left = new FormAttachment(0, 0);
      formData_2.top = new FormAttachment(0, LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET);
      kwLbl.setLayoutData(formData_2);
      kwLbl.setText("Name:");
      
      final ComboViewer kwCV = new ComboViewer(kwComp,SWT.NONE | SWT.READ_ONLY);
      m_keyWordCombo = kwCV.getCombo();
      m_keyWordCombo.addSelectionListener(this);
      final FormData formData_3 = new FormData();
      formData_3.left = new FormAttachment(kwLbl,LABEL_HSPACE_OFFSET,SWT.RIGHT);
      formData_3.right = new FormAttachment(100, 0);
      formData_3.top = new FormAttachment(kwLbl, -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      kwCV.setLabelProvider(new PSCECommonLabelProvider());
      kwCV.setContentProvider(new PSDefaultContentProvider());
      m_keyWordCombo.setLayoutData(formData_3);
      try
      {
         m_kwList = PSCoreUtils.catalog(PSObjectTypes.KEYWORD,true);
         kwCV.setInput(m_kwList);
      }
      catch (PSModelException e1)
      {
         String title = "Failed to get the keyword list";
         String msg = "The following error occured while catalogging keyword list";
         PSWorkbenchPlugin.handleException("Control Choices Dialog",title, msg,e1);
      }
      
      Composite dsComp = new Composite(m_mainRadioComp,SWT.NONE);
      dsComp.setLayout(new FormLayout());
      
      final Label dsLbl = new Label(dsComp,SWT.NONE);
      dsLbl.setAlignment(SWT.RIGHT);
      final FormData formData_4 = new FormData();
      formData_4.right = new FormAttachment(RADIO_CHILD_LABEL_NUMERATOR,0);
      formData_4.left = new FormAttachment(0, 0);
      formData_4.top = new FormAttachment(0, LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET);
      dsLbl.setLayoutData(formData_4);
      dsLbl.setText("Data source:");
      
      final ComboViewer dataSourceCV = new ComboViewer(dsComp,SWT.NONE | SWT.READ_ONLY);
      m_dataSourceCombo = dataSourceCV.getCombo();
      m_dataSourceCombo.addSelectionListener(this);
      final FormData formData_5 = new FormData();
      formData_5.left = new FormAttachment(dsLbl,LABEL_HSPACE_OFFSET,SWT.RIGHT);
      formData_5.right = new FormAttachment(100, 0);
      formData_5.top = new FormAttachment(dsLbl, -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      m_dataSourceCombo.setLayoutData(formData_5);
      m_dataSourceCombo.setItems(getDataSources());
      
      final Label tbLbl = new Label(dsComp,SWT.NONE);
      tbLbl.setAlignment(SWT.RIGHT);
      final FormData formData_6 = new FormData();
      formData_6.right = new FormAttachment(RADIO_CHILD_LABEL_NUMERATOR,0);
      formData_6.left = new FormAttachment(0, 0);
      formData_6.top = new FormAttachment(dsLbl, LABEL_VSPACE_OFFSET,SWT.BOTTOM);
      tbLbl.setLayoutData(formData_6);
      tbLbl.setText("Table:");
      
      final ComboViewer tableCV = new ComboViewer(dsComp,SWT.NONE | SWT.READ_ONLY);
      m_tableCombo = tableCV.getCombo();      
      final FormData formData_7 = new FormData();
      formData_7.left = new FormAttachment(tbLbl,LABEL_HSPACE_OFFSET,SWT.RIGHT);
      formData_7.right = new FormAttachment(100, 0);
      formData_7.top = new FormAttachment(tbLbl, -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      m_tableCombo.setLayoutData(formData_7);
      m_tableCombo.addSelectionListener(this);
            
      final Label lcLbl = new Label(dsComp,SWT.NONE);
      lcLbl.setAlignment(SWT.RIGHT);
      final FormData formData_8 = new FormData();
      formData_8.right = new FormAttachment(RADIO_CHILD_LABEL_NUMERATOR,0);
      formData_8.left = new FormAttachment(0, 0);
      formData_8.top = new FormAttachment(tbLbl, LABEL_VSPACE_OFFSET,SWT.BOTTOM);
      lcLbl.setLayoutData(formData_8);
      lcLbl.setText("Label column:");
      
      final ComboViewer labColCV = new ComboViewer(dsComp,SWT.NONE | SWT.READ_ONLY);
      m_labelColumnCombo = labColCV.getCombo();
      final FormData formData_9 = new FormData();
      formData_9.left = new FormAttachment(lcLbl,LABEL_HSPACE_OFFSET,SWT.RIGHT);
      formData_9.right = new FormAttachment(100, 0);
      formData_9.top = new FormAttachment(lcLbl, -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      m_labelColumnCombo.setLayoutData(formData_9);

      final Label vcLbl = new Label(dsComp,SWT.NONE);
      vcLbl.setAlignment(SWT.RIGHT);
      final FormData formData_10 = new FormData();
      formData_10.right = new FormAttachment(RADIO_CHILD_LABEL_NUMERATOR,0);
      formData_10.left = new FormAttachment(0, 0);
      formData_10.top = new FormAttachment(lcLbl, LABEL_VSPACE_OFFSET,SWT.BOTTOM);
      vcLbl.setLayoutData(formData_10);
      vcLbl.setText("Value column:");
      
      final ComboViewer valColCV = new ComboViewer(dsComp,SWT.NONE | SWT.READ_ONLY);
      m_valueColumnCombo = valColCV.getCombo();
      final FormData formData_11 = new FormData();
      formData_11.left = new FormAttachment(vcLbl,LABEL_HSPACE_OFFSET,SWT.RIGHT);
      formData_11.right = new FormAttachment(100, 0);
      formData_11.top = new FormAttachment(vcLbl, -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      m_valueColumnCombo.setLayoutData(formData_11);

      //Add the input to the datatype info controls
      
      Composite urlComp = new Composite(m_mainRadioComp,SWT.NONE);
      urlComp.setLayout(new FormLayout());

      final Label urlLbl = new Label(urlComp,SWT.NONE);
      urlLbl.setAlignment(SWT.RIGHT);
      final FormData formData_12 = new FormData();
      formData_12.right = new FormAttachment(RADIO_CHILD_LABEL_NUMERATOR,0);
      formData_12.left = new FormAttachment(0, 0);
      formData_12.top = new FormAttachment(0, LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET);
      urlLbl.setLayoutData(formData_12);
      urlLbl.setText("URL:");
      
      
      m_urlButton = new Button(urlComp,SWT.BORDER);
      final FormData formData_12b = new FormData();
      formData_12b.height = 21;
      formData_12b.width = 21;
      formData_12b.right = new FormAttachment(100, 0);
      formData_12b.top = new FormAttachment(0, 0);
      m_urlButton.setLayoutData(formData_12b);
      m_urlButton.setText("...");
      m_urlButton.addSelectionListener(this);
      
      m_urlText = new Text(urlComp, SWT.V_SCROLL | SWT.BORDER | SWT.WRAP);
      final FormData formData_13 = new FormData();
      formData_13.left = new FormAttachment(urlLbl,LABEL_HSPACE_OFFSET,SWT.RIGHT);
      formData_13.right = new FormAttachment(m_urlButton, -LABEL_HSPACE_OFFSET,SWT.LEFT);
      formData_13.top = new FormAttachment(urlLbl, -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      formData_13.height = TEXTAREA_BOX_HEIGHT;
      m_urlText.setLayoutData(formData_13);
      
      
      Composite chComp = new Composite(m_mainRadioComp,SWT.NONE);
      chComp.setLayout(new FormLayout());

      // Create the label provider for this table
      ITableLabelProvider labelProvider = new PSAbstractTableLabelProvider()
      {

         public String getColumnText(Object element, int columnIndex)
         {
            Choice choice = (Choice) element;
            switch (columnIndex)
            {
               case 0 :
                  return StringUtils.defaultString(choice.label);
               case 1 :
                  return StringUtils.defaultString(choice.value);
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
            if (!(obj instanceof Choice))
               throw new IllegalArgumentException(
                     "The passed in object must be an instance of Choice.");
            Choice choice = (Choice) obj;
            return choice.isEmpty();

         }

      };   
      
      m_choicesTable = new PSSortableTable(
            chComp, labelProvider, newRowProvider, SWT.NONE,
         PSSortableTable.NONE 
           | PSSortableTable.INSERT_ALLOWED
           | PSSortableTable.DELETE_ALLOWED);
      m_choicesTable.setCellModifier(new CellModifier(m_choicesTable));
      final FormData formData_14 = new FormData();
      formData_14.left = new FormAttachment(0, 0);
      formData_14.top = new FormAttachment(0, 0);
      formData_14.right = new FormAttachment(100, 0);
      formData_14.height = 100;
      m_choicesTable.setLayoutData(formData_14);
      
      // Add listener to fill the value box upon row selection
      final Table table = m_choicesTable.getTable();
      m_choicesTable.addSelectionListener(new SelectionAdapter()
      {

         /*
          * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
          *      org.eclipse.swt.events.SelectionEvent)
          */
         @SuppressWarnings("synthetic-access")//$NON-NLS-1$
         @Override
         public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e)
         {
            // validateSelectedTableRow();
            // if(table.getSelectionIndices().length < 0 ||
            // table.getSelectionIndices().length > 1)
            // {
            // m_valueText.setText(""); //$NON-NLS-1$
            // }
            // else
            // {
            // TableItem item =
            // table.getItem(table.getSelectionIndices()[0]);
            // Choice choice = (Choice)item.getData();
            // String value = choice.value;
            // m_valueText.setText(value == null ? "" : value); //$NON-NLS-1$
            //                  
            //               }
         }

      });            

      CellEditor cEditor = new TextCellEditor(m_choicesTable.getTable(),
            SWT.NONE);
      Text labelText = (Text) cEditor.getControl();
      labelText.addFocusListener(new FocusAdapter()
      {
         @Override
         public void focusLost(@SuppressWarnings("unused") FocusEvent e)
         {
            int sel = m_choicesTable.getTable().getSelectionIndex();
            TableItem item = table.getItem(sel);
            Choice choice = (Choice)item.getData();
            if(!StringUtils.isBlank(choice.label) && StringUtils.isBlank(choice.value))
            {
               choice.value = new String(choice.label);
            }
            refreshDefaultList();
         }
      });

      m_choicesTable.addColumn("Label", 0, new ColumnWeightData(10, 100),
            cEditor, SWT.LEFT); 
      
      cEditor = new TextCellEditor(m_choicesTable.getTable(), SWT.NONE);     
      // Add listener to update value text box on each key stroke
      Text valueText = (Text)cEditor.getControl();
      
      valueText.addFocusListener(new FocusAdapter()
      {
         @Override
         @SuppressWarnings("synthetic-access")
         public void focusLost(@SuppressWarnings("unused") FocusEvent e)
         {
            refreshDefaultList();
         }
      });


      m_choicesTable.addColumn("Value", 0, new ColumnWeightData(10, 100),
            cEditor, SWT.LEFT); 
      

      m_mainRadioComp.addEntry("Use a keyword",true,"",kwComp);
      m_mainRadioComp.addEntry("Retrieve from table",true,"",dsComp);
      m_mainRadioComp.addEntry("Retrieve from xml application",true,"",urlComp);
      m_mainRadioComp.addEntry("Define choices for this control only",true,"",chComp);
      
      m_mainRadioComp.layoutControls();
      
      Composite defVComp = new Composite(this,SWT.NONE);
      defVComp.setLayout(new FormLayout());
      
      Label defVLbl = new Label(defVComp,SWT.NONE);
      defVLbl.setText("Default values:");
      final FormData formData_15 = new FormData();
      formData_15.right = new FormAttachment(60,0);
      formData_15.left = new FormAttachment(0, 0);
      formData_15.top = new FormAttachment(0, LABEL_VSPACE_OFFSET);
      defVLbl.setLayoutData(formData_15);      
      
      Label soLbl = new Label(defVComp,SWT.WRAP);
      soLbl.setText("Sort order");
      final FormData formData_16 = new FormData();
      formData_16.left = new FormAttachment(defVLbl, LABEL_HSPACE_OFFSET, SWT.RIGHT);
      formData_16.top = new FormAttachment(defVLbl, 0, SWT.TOP);
      soLbl.setLayoutData(formData_16);      
      
      Label separator = 
         new Label(defVComp, SWT.HORIZONTAL | SWT.SEPARATOR);
      FormData formData_17 = new FormData();
      formData_17.left = 
         new FormAttachment(soLbl, LABEL_HSPACE_OFFSET, SWT.RIGHT);
      formData_17.top = new FormAttachment(soLbl, 0, SWT.CENTER);
      formData_17.right = new FormAttachment(100, 0);
      separator.setLayoutData(formData_17);            
      
      // create "Default List" control
      String controlName = uiSet.getControl().getName();
      int listStyle = SWT.V_SCROLL | SWT.BORDER | SWT.WRAP;
      if (controlName.equals("sys_DropDownMultiple")
            || controlName.equals("sys_CheckBoxTree")
            || controlName.equals("sys_CheckBoxGroup"))
      {
         listStyle |= SWT.MULTI;
      }
      m_defValueList = new List(defVComp, listStyle);
      final FormData formData_18 = new FormData();
      formData_18.left = new FormAttachment(0,0);
      formData_18.right = new FormAttachment(50, 0);
      formData_18.top = new FormAttachment(defVLbl, COMBO_VSPACE_OFFSET, SWT.BOTTOM);
      formData_18.height = DESCRIPTION_FIELD_HEIGHT;
      m_defValueList.setLayoutData(formData_18);

      Composite sorComp = new Composite(defVComp,SWT.None);
      sorComp.setLayout(new FormLayout());
      m_sortOrderRadio[0] = new Button(sorComp,SWT.RADIO);
      m_sortOrderRadio[0].setText("Ascending");
      m_sortOrderRadio[1] = new Button(sorComp,SWT.RADIO);
      m_sortOrderRadio[1].setText("Descending");
      m_sortOrderRadio[2] = new Button(sorComp,SWT.RADIO);
      m_sortOrderRadio[2].setText("User specified");

      for (Button b : m_sortOrderRadio)
      {
         b.addSelectionListener(this);
      }
      final FormData formData_19 = new FormData();
      formData_19.left = new FormAttachment(soLbl,2 * LABEL_HSPACE_OFFSET,SWT.LEFT);
      formData_19.right = new FormAttachment(100, -10);
      formData_19.top = new FormAttachment(m_defValueList, 0, SWT.TOP);
      m_sortOrderRadio[0].setLayoutData(formData_19);
      
      final FormData formData_20 = new FormData();
      formData_20.left = new FormAttachment(m_sortOrderRadio[0],0,SWT.LEFT);
      formData_20.right = new FormAttachment(100, 0);
      formData_20.top = new FormAttachment(m_sortOrderRadio[0], 0, SWT.BOTTOM);
      m_sortOrderRadio[1].setLayoutData(formData_20);

      final FormData formData_21 = new FormData();
      formData_21.left = new FormAttachment(m_sortOrderRadio[0],0,SWT.LEFT);
      formData_21.right = new FormAttachment(100, 0);
      formData_21.top = new FormAttachment(m_sortOrderRadio[1], 0, SWT.BOTTOM);
      m_sortOrderRadio[2].setLayoutData(formData_21);

      final FormData formData_22 = new FormData();
      formData_22.left = new FormAttachment(60,0);
      formData_22.right = new FormAttachment(100, 0);
      formData_22.top = new FormAttachment(m_defValueList, 0, SWT.TOP);
      sorComp.setLayoutData(formData_22);
      
      
      FormData formData_23 = new FormData();
      formData_23.left = 
         new FormAttachment(0, 20);
      formData_23.top = new FormAttachment(m_mainRadioComp, LABEL_VSPACE_OFFSET, SWT.BOTTOM);
      formData_23.right = new FormAttachment(100, -10);
      defVComp.setLayoutData(formData_23);            

      m_emptyEntryComp = new PSRadioAndCheckBoxes(
            this, "", SWT.VERTICAL | SWT.CHECK);

      FormData formData_24 = new FormData();
      formData_24.right = new FormAttachment(100,-10);
      formData_24.left = new FormAttachment(0, 10);
      formData_24.top = new FormAttachment(defVComp, LABEL_VSPACE_OFFSET, SWT.BOTTOM);
      m_emptyEntryComp.setLayoutData(formData_24);

      Composite eeComp = new Composite(m_emptyEntryComp,SWT.NONE);
      eeComp.setLayout(new FormLayout());
      
      final Label eelLbl = new Label(eeComp,SWT.NONE);
      eelLbl.setAlignment(SWT.RIGHT);
      final FormData formData_25 = new FormData();
      formData_25.right = new FormAttachment(RADIO_CHILD_LABEL_NUMERATOR,0);
      formData_25.left = new FormAttachment(0, 0);
      formData_25.top = new FormAttachment(0, LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET);
      eelLbl.setLayoutData(formData_25);
      eelLbl.setText("Label:");
      
      m_emptyEntryLabelText = new Text(eeComp,SWT.BORDER | SWT.WRAP);
      final FormData formData_26 = new FormData();
      formData_26.left = new FormAttachment(eelLbl,LABEL_HSPACE_OFFSET,SWT.RIGHT);
      formData_26.right = new FormAttachment(100, 0);
      formData_26.top = new FormAttachment(eelLbl, -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      m_emptyEntryLabelText.setLayoutData(formData_26);

      final Label eevLbl = new Label(eeComp,SWT.NONE);
      eevLbl.setAlignment(SWT.RIGHT);
      final FormData formData_27 = new FormData();
      formData_27.right = new FormAttachment(RADIO_CHILD_LABEL_NUMERATOR,0);
      formData_27.left = new FormAttachment(0, 0);
      formData_27.top = new FormAttachment(eelLbl, LABEL_VSPACE_OFFSET,SWT.BOTTOM);
      eevLbl.setLayoutData(formData_27);
      eevLbl.setText("Value:");
      
      m_emptyEntryValueText = new Text(eeComp,SWT.BORDER | SWT.WRAP);
      final FormData formData_28 = new FormData();
      formData_28.left = new FormAttachment(eevLbl,LABEL_HSPACE_OFFSET,SWT.RIGHT);
      formData_28.right = new FormAttachment(100, 0);
      formData_28.top = new FormAttachment(eevLbl, -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      m_emptyEntryValueText.setLayoutData(formData_28);

      final Label includeLabel = new Label(eeComp,SWT.NONE);
      includeLabel.setAlignment(SWT.RIGHT);
      final FormData formData_29 = new FormData();
      formData_29.right = new FormAttachment(RADIO_CHILD_LABEL_NUMERATOR,0);
      formData_29.left = new FormAttachment(0, 0);
      formData_29.top = new FormAttachment(eevLbl, LABEL_VSPACE_OFFSET, SWT.BOTTOM);
      includeLabel.setLayoutData(formData_29);
      includeLabel.setText("Include:");
      
      final ComboViewer eeIncludeCV = new ComboViewer(eeComp,SWT.NONE | SWT.READ_ONLY);
      m_eeIncludeCombo = eeIncludeCV.getCombo();
      final FormData formData_30 = new FormData();
      formData_30.right = new FormAttachment(50,0);
      formData_30.left = new FormAttachment(includeLabel, LABEL_HSPACE_OFFSET, SWT.RIGHT);
      formData_30.top = new FormAttachment(includeLabel, -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      m_eeIncludeCombo.setLayoutData(formData_30);
      m_eeIncludeCombo.setItems(INCLUDE_WHEN_LIST);
      
      final Label sortOrderLabel = new Label(eeComp,SWT.NONE);
      sortOrderLabel.setAlignment(SWT.RIGHT);
      final FormData formData_31 = new FormData();
      formData_31.right = new FormAttachment(75,0);
      formData_31.left = new FormAttachment(m_eeIncludeCombo,0,SWT.RIGHT);
      formData_31.top = new FormAttachment(includeLabel, 0, SWT.TOP);
      sortOrderLabel.setLayoutData(formData_31);
      sortOrderLabel.setText("Sort order:");
      
      final ComboViewer eeSortOrderCV = new ComboViewer(eeComp,SWT.NONE | SWT.READ_ONLY);
      m_eeSortOrderCombo = eeSortOrderCV.getCombo();
      final FormData formData_32 = new FormData();
      formData_32.right = new FormAttachment(100,0);
      formData_32.left = new FormAttachment(sortOrderLabel, LABEL_HSPACE_OFFSET, SWT.RIGHT);
      formData_32.top = new FormAttachment(sortOrderLabel, -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      m_eeSortOrderCombo.setLayoutData(formData_32);
      m_eeSortOrderCombo.setItems(SORT_ORDER_LIST);
      
      //
//      final FormData formData_29 = new FormData();
//      formData_29.left = new FormAttachment(soLbl,2 * LABEL_HSPACE_OFFSET,SWT.LEFT);
//      formData_29.right = new FormAttachment(100, -10);
//      formData_29.top = new FormAttachment(m_defVauleList, 0, SWT.TOP);
//      m_eeSortOrderRadio[0].setLayoutData(formData_29);
//      
//
//      final FormData formData_32 = new FormData();
//      formData_32.left = new FormAttachment(60,0);
//      formData_32.right = new FormAttachment(100, 0);
//      formData_32.top = new FormAttachment(m_defVauleList, 0, SWT.TOP);
//      eesorComp.setLayoutData(formData_32);
      
      m_emptyEntryComp.addEntry("Display text for empty entry",true,"",eeComp);
      m_emptyEntryComp.layoutControls();
   }

   /**
    * Sets the selection for the "Default values" list from the given "choices".
    *  
    * @param choices the object which may contain predefined selections,
    * it may be <code>null</code>. Do nothing if it is <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private void setSelectedDefaultList(PSChoices choices)
   {
      if (choices == null)
         return;
      
      ArrayList<Integer> selections = new ArrayList<Integer>();
      Iterator selects = choices.getDefaultSelected();
      while (selects.hasNext())
      {
         PSDefaultSelected selected = (PSDefaultSelected) selects.next();
         if (selected.getType() == TYPE_SEQUENCE && selected.getSequence() > 0)
         {
            // the sequence is 1 based, see PSChoiceBuilder
            selections.add(selected.getSequence() - 1);
         }
      }
      if (!selections.isEmpty())
      {
         int[] indices = new int[selections.size()];
         for (int i=0; i<selections.size(); i++)
            indices[i] = selections.get(i).intValue();
            
         m_defValueList.select(indices);
      }
   }
   
   /**
    * Sets the input for the controls
    */
   private void loadControlValues()
   {
      PSUISet uiSet = m_rowData.getDisplayMapping().getUISet();
      PSChoices choices = uiSet.getChoices();
      if(choices == null)
      {
         m_mainRadioComp.setSelection(0);
         m_keyWordCombo.select(0);
         setSortOrderRadio(SORT_ORDER_ASCENDING);
         IPSReference kwRef = m_kwList.get(m_keyWordCombo
               .getSelectionIndex());
         refreshDefaultList(getKWLabels(kwRef), SORT_ORDER_ASCENDING);
         return;
      }
      setSortOrderRadio(choices.getSortOrder());
      int type = choices.getType();
      switch(type)
      {
         case PSChoices.TYPE_GLOBAL:
            m_mainRadioComp.setSelection(0);
            int kwvalue = choices.getGlobal();
            IPSReference kwRef = null;
            try
            {
               kwRef = getReference(m_kwList,kwvalue);
            }
            catch (Exception e)
            {
               // This should not happen as the references are just catalogged.
               String title = "Failed to load the keywords";
               PSWorkbenchPlugin.handleException("Control Choices Dialog",
                     title, null, e);
            }
            m_keyWordCombo.select(m_kwList.indexOf(kwRef));
            refreshDefaultList(getKWLabels(kwRef), getSortOrder());
            setSelectedDefaultList(choices);
            break;
         case PSChoices.TYPE_TABLE_INFO:
            m_mainRadioComp.setSelection(1);
            PSChoiceTableInfo tinfo = choices.getTableInfo();
            String ds = tinfo.getDataSource();
            if(StringUtils.isBlank(ds))
            {
               m_dataSourceCombo.select(0);
            }
            else
            {
               m_dataSourceCombo.select(PSContentEditorDefinition.getItemIndex(
                     m_dataSourceCombo.getItems(), ds));
            }
            
            //Set items of table based on the selected datasource
            m_tableCombo.setItems(getTableNames(ds));
            String table = tinfo.getTableName();
            m_tableCombo.select(PSContentEditorDefinition.getItemIndex(
                  m_tableCombo.getItems(), table));
            //Set items of label column
            m_labelColumnCombo.setItems(getColumnNames(ds,table));
            m_labelColumnCombo.select(PSContentEditorDefinition.getItemIndex(
                  m_labelColumnCombo.getItems(), tinfo.getLableColumn()));
           //Set items of value column
            m_valueColumnCombo.setItems(getColumnNames(ds,table));
            m_valueColumnCombo.select(PSContentEditorDefinition.getItemIndex(
                  m_valueColumnCombo.getItems(), tinfo.getValueColumn()));
            break;
         case PSChoices.TYPE_INTERNAL_LOOKUP:
            m_mainRadioComp.setSelection(2);
            m_urlRequest = choices.getLookup();
            m_urlText.setText(m_urlRequest.toString());
            break;
         case PSChoices.TYPE_LOCAL:
            m_mainRadioComp.setSelection(3);
            m_localChoices = new ArrayList<Choice>();
            Iterator iter = choices.getLocal();
            while(iter.hasNext())
            {
               PSEntry entry = (PSEntry)iter.next();
               m_localChoices.add(new Choice(entry));
            }
            m_choicesTable.setValues(m_localChoices);
            java.util.List<String> dvList = new ArrayList<String>();
            for(Choice choice : m_localChoices)
            {
               PSEntry en = choice.toEntryChoice();
               dvList.add(en.getValue());
            }
            refreshDefaultList(dvList.toArray(new String[dvList.size()]), 
                  getSortOrder());
            setSelectedDefaultList(choices);
            break;
         case PSChoices.TYPE_LOOKUP:
            break;
         default:
            m_mainRadioComp.setSelection(0);
            m_keyWordCombo.select(0);
      }
      //Set the null entry
      PSNullEntry ne = choices.getNullEntry();
      if(ne!=null)
      {
         int[] tmp = {0};
         m_emptyEntryComp.setSelection(tmp);
         m_emptyEntryLabelText.setText(ne.getLabel().getText());
         m_emptyEntryValueText.setText(ne.getValue());
         if(ne.getSortOrder() < SORT_ORDER_LIST.length)
            m_eeSortOrderCombo.select(ne.getSortOrder());
         if(ne.getIncludeWhen() < INCLUDE_WHEN_LIST.length)
            m_eeIncludeCombo.select(ne.getIncludeWhen());
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
      @SuppressWarnings("unused")
      public boolean canModify(Object element, String property)
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
         }
         mi_tableComp.refreshTable();
      }
      
      private PSSortableTable mi_tableComp;
      
   }
   
   /**
    * A little convenience class must to hold the values for a row
    * in the choices table.
    */
   public class Choice
   {      
      public Choice(){}
      
      /**
       * 
       * @param label
       * @param value
       */
      public Choice(String label, String value)
      {
         this.label = label;
         this.value = value;
      }
      
      /**
       * 
       * @param eChoice
       */
      public Choice(PSEntry eChoice)
      {
         if(eChoice == null)
            throw new IllegalArgumentException("kChoice cannot be null.");
         label = eChoice.getLabel().getText();
         value = eChoice.getValue();
         m_entryChoice = eChoice;
      }
      
       public boolean isEmpty()
      {         
        return (StringUtils.isBlank(label) || StringUtils.isBlank(value)) ; 
      }
      
      public PSEntry toEntryChoice()
      {
         if(isEmpty())
            throw new IllegalStateException("Label and value are both required.");
         if(m_entryChoice == null)
            m_entryChoice = new PSEntry(label,value);
         
         return m_entryChoice;
      }
      
      public String label;
      public String value;
      private PSEntry m_entryChoice;
   }

   @SuppressWarnings("unused")
   public void selectionChanged(SelectionChangedEvent event)
   {
   }

   @SuppressWarnings("unchecked")
   public void widgetSelected(SelectionEvent e)
   {
      if(e.getSource() == m_mainRadioComp)
      {
         int sel = m_mainRadioComp.getSelectedIndex();
         java.util.List<String> dvList = new ArrayList<String>();
         switch(sel)
         {
            case 0 :
               int index = m_keyWordCombo.getSelectionIndex();
               if(index==-1)
               {
                  refreshDefaultList(new String[0], getSortOrder());
               }
               else
               {
                  IPSReference kwRef = m_kwList.get(index);
                  refreshDefaultList(getKWLabels(kwRef), getSortOrder());
               }
               break;
            case 1 :
               refreshDefaultList(null, getSortOrder());
               break;
            case 2:
               refreshDefaultList(null, getSortOrder());
               break;
            case 3:
               java.util.List<Choice> values =
                  (java.util.List<Choice>) m_choicesTable.getValues();
               for(Choice choice : values)
               {
                  PSEntry en = choice.toEntryChoice();
                  dvList.add(en.getValue());
               }
               refreshDefaultList(dvList.toArray(new String[0]), getSortOrder());
               break;
            default:
               break; 
         }
      }
      else if(e.getSource() == m_keyWordCombo)
      {
         int index = m_keyWordCombo.getSelectionIndex();
         if(index==-1)
         {
            refreshDefaultList(new String[0], getSortOrder());
         }
         else
         {
            IPSReference kwRef = m_kwList.get(index);
            refreshDefaultList(getKWLabels(kwRef), getSortOrder());
         }
      }
      else if(e.getSource() == m_dataSourceCombo)
      {
         //Populate the tables
         m_tableCombo.setItems(getTableNames(m_dataSourceCombo.getText()));
         m_labelColumnCombo.setItems(new String[0]);
         m_valueColumnCombo.setItems(new String[0]);
      }
      else if (e.getSource() == m_tableCombo)
      {
         m_labelColumnCombo.setItems(getColumnNames(
               m_dataSourceCombo.getText(), m_tableCombo.getText()));
         m_valueColumnCombo.setItems(getColumnNames(
               m_dataSourceCombo.getText(), m_tableCombo.getText()));
      }
      else if (e.getSource() == m_sortOrderRadio[0])
      {
         refreshDefaultList(SORT_ORDER_ASCENDING);
      }
      else if (e.getSource() == m_sortOrderRadio[1])
      {
         refreshDefaultList(SORT_ORDER_DESCENDING);
      }
      else if (e.getSource() == m_sortOrderRadio[2])
      {
         refreshDefaultList(SORT_ORDER_USER);
      }
      else if (e.getSource() == m_urlButton)
      {
         final Display display = getShell().getDisplay();
         m_urlButton.setEnabled(false);
         SwingUtilities.invokeLater(new Runnable()
            {
            @SuppressWarnings("synthetic-access")
            public void run()
            {
               try
               {
                  LookupRequestDialog dlg = new LookupRequestDialog();
                  if (m_urlRequest != null)
                     dlg.setData( m_urlRequest);
                  dlg.setVisible(true);
                  PSUrlRequest result = (PSUrlRequest) dlg.getData();
                  dlg.dispose();
                  if (result != null)
                  {
                     m_urlRequest = result; // save the dialog result
                     display.asyncExec(new Runnable()
                        {
                           public void run()
                           {
                              if(m_urlRequest!=null)
                                 m_urlText.setText(m_urlRequest.toString());                           
                           }
                        });
                  }
               }
               catch (Exception ex)
               {
                  PSWorkbenchPlugin.handleException(
                        "Opening Lookup Dialog", null, null, ex);
               }
               finally
               {
                  display.asyncExec(new Runnable()
                  {
                     public void run()
                     {
                        m_urlButton.setEnabled(true);
                     }
                  });
               }
            }
            });
      }
   }
   private String[] getKWLabels(IPSReference kwRef)
   {
      String[] labels = null;
      PSKeyword kw = null;
      if (kwRef != null)
      {
         try
         {
            kw = (PSKeyword) PSModelTracker.getInstance().load(kwRef,false);
            java.util.List<PSKeywordChoice> kwch = kw.getChoices();
            labels = new String[kwch.size()];
            for(int i=0;i<kwch.size();i++)
               labels[i] = kwch.get(i).getLabel();
         }
         catch (Exception e1)
         {
            e1.printStackTrace();
         }
      }
      return labels;
   }
   
   /**
    * Refreshes the default value list according to the given items and sort
    * order.
    * 
    * @param items the new set of default value list, it may be 
    * <code>null</code> or empty.
    * 
    * @param sortOrder the sort order for the default value list. It should be
    * one of the SORT_ORDER_XXX values.
    */
   private void refreshDefaultList(String[] items, int sortOrder)
   {
      m_origDefaultValues.clear();
      if(items != null && items.length > 0)
      {
         CollectionUtils.addAll(m_origDefaultValues, items);
      }
      refreshDefaultList(sortOrder);
   }

   /**
    * Refreshes the default value list control according to the
    * {@link #m_origDefaultValues} and the given sort order.
    * 
    * @param sortOrder the sort order for the default value list. It should be
    * one of the SORT_ORDER_XXX values.
    */
   private void refreshDefaultList(int sortOrder)
   {
      ArrayList<String> origDefaultValues = m_origDefaultValues;
      if(!origDefaultValues.isEmpty())
      {
         ArrayList<String> sortValues = new ArrayList<String>(origDefaultValues);
         String[] sortedItems = new String[origDefaultValues.size()];
         if (sortOrder == SORT_ORDER_ASCENDING)
         {
            Collections.sort(sortValues, new ComparatorAscending());
         }
         else if (sortOrder == SORT_ORDER_DESCENDING)
         {
            Collections.sort(sortValues, new ComparatorDeScending());
         }
         sortValues.toArray(sortedItems);
         
         refreshDefaultListControl(sortedItems);
      }
      else
      {
         refreshDefaultListControl(null);
      }
   }


   /**
    * Refreshes the default value list control according to the given items.
    * 
    * @param items the new set of default values, it may be <code>null</code>
    * or empty.
    */
   private void refreshDefaultListControl(String[] items)
   {
      m_defValueList.removeAll();
      if(items != null && items.length > 0)
      {
         m_defValueList.setItems(items);
      }
   }
   
   @SuppressWarnings("unchecked")
   private void refreshDefaultList()
   {
      java.util.List<String> dvList = new ArrayList<String>();
      java.util.List<Choice> values = 
         (java.util.List<Choice>) m_choicesTable.getValues();
      for(Choice choice : values)
      {
         PSEntry en = choice.toEntryChoice();
         dvList.add(en.getValue());
      }
      refreshDefaultList(dvList.toArray(new String[0]), getSortOrder());
   }

   @SuppressWarnings("unused")
   public void widgetDefaultSelected(SelectionEvent e)
   {
   }

   @SuppressWarnings("unused")
   public void modifyText(ModifyEvent e)
   {
   }
   
   public void updateData()
   {
      PSUISet uiSet = m_rowData.getDisplayMapping().getUISet();
      PSChoices choice = null;
      //Check the selected type.
      int sel = m_mainRadioComp.getSelectedIndex();
      switch(sel)
      {
         case 0:
            int index = m_keyWordCombo.getSelectionIndex();
            IPSReference kwRef = m_kwList.get(index);
            try
            {
               PSKeyword kw = (PSKeyword) PSModelTracker.getInstance().load(kwRef,false);
               choice = new PSChoices(Integer.parseInt(kw.getValue()));
            }
            catch (Exception e)
            {
               String title = "Failed to load the keyword";
               PSWorkbenchPlugin.handleException("Control Choices Dialog",
                     title, null, e);
            }
            break;
         case 1:
            //If the selected datasource is CMS_REPOSITORY which will be always the
            //first choice, set the datasource to blank.
            String ds = m_dataSourceCombo.getText();
            if(ds.equals(CMS_REPOSITORY))
               ds = StringUtils.EMPTY;
            PSChoiceTableInfo tinfo = new PSChoiceTableInfo(ds, m_tableCombo
                  .getText(), m_labelColumnCombo.getText(), m_valueColumnCombo
                  .getText());
            choice =  new PSChoices(tinfo);
            break;
         case 2:
            choice = new PSChoices(m_urlRequest,PSChoices.TYPE_INTERNAL_LOOKUP);
            break;
         case 3:
            java.util.List choices = m_choicesTable.getValues();
            PSCollection col;
            try
            {
               col = new PSCollection("com.percussion.design.objectstore.PSEntry");
               for(Object obj : choices)
               {
                  Choice ch = (Choice)obj;
                  if(ch.isEmpty())
                     continue;
                  col.add(new PSEntry(ch.value,ch.label));
               }
               choice = new PSChoices(col);
            }
            catch (ClassNotFoundException e)
            {
               e.printStackTrace();
            }
            break;
      }
      if(choice != null)
      {
         int so = getSortOrder();
         choice.setSortOrder(so);
         
         // set the default values if there is any
         int[] indices = m_defValueList.getSelectionIndices();
         if (indices != null && indices.length > 0)
         {
            PSCollection defaults = new PSCollection(PSDefaultSelected.class);
            for (int index : indices)
            {
               // default value selection is 1 based, see PSChoiceBuilder 
               PSDefaultSelected defSelect = new PSDefaultSelected(index+1);
               defaults.add(defSelect);
            }
            choice.setDefaultSelected(defaults);
         }
         
         if(m_emptyEntryComp.getSelectedIndex()==0 && 
               !StringUtils.isBlank(m_emptyEntryLabelText.getText()))
         {
            PSNullEntry ne = new PSNullEntry(m_emptyEntryValueText.getText(),
                  new PSDisplayText(m_emptyEntryLabelText.getText()));
            if(m_eeIncludeCombo.getSelectionIndex() != -1)
               ne.setIncludeWhen(m_eeIncludeCombo.getSelectionIndex());
            if(m_eeSortOrderCombo.getSelectionIndex() != -1)
               ne.setSortOrder(m_eeSortOrderCombo.getSelectionIndex());
            choice.setNullEntry(ne);
         }
         else
            choice.setNullEntry(null);
      }
      uiSet.setChoices(choice);
      m_rowData.getDisplayMapping().setUISet(uiSet);
   }

   /**
    * Sets the sort order radio buttons, <code>m_sortOrderRadio[]</code>, 
    * according to the given sort order ID.
    *  
    * @param sortOrder sort order ID.
    */
   private void setSortOrderRadio(int sortOrder)
   {
      switch(sortOrder)
      {
         case SORT_ORDER_DESCENDING:
            m_sortOrderRadio[1].setSelection(true);
            break;
         case SORT_ORDER_USER:
            m_sortOrderRadio[2].setSelection(true);
            break;
         default:
            m_sortOrderRadio[0].setSelection(true);
      }
   }

   /**
    * Gets the sort order from current selected "sort order radio buttons".
    * 
    * @return the related sort order constant, SORT_ORDER_XXX.
    */
   private int getSortOrder()
   {
      int so = SORT_ORDER_ASCENDING;
      if(m_sortOrderRadio[1].getSelection())
         so = SORT_ORDER_DESCENDING;
      else if(m_sortOrderRadio[2].getSelection())
         so = SORT_ORDER_USER;

      return so;
   }
   
   /**
    * Convenient method to get a reference from a list of references
    * corresponding to the supplied keyword value.
    * 
    * @param refs list of IPSReference objects.
    * @param kwvalue the value of keyword for which the reference needs to be
    *           returned.
    * @return IPSReference of the supplied id or <code>null</code> if not
    *         found or supplied list is <code>null</code> or empty.
    * @throws Exception if loading of any keyword from its reference fails. 
    */
   private IPSReference getReference(java.util.List<IPSReference> refs,
         int kwvalue) throws Exception
   {
      if (refs == null || refs.isEmpty())
         return null;
      for (IPSReference ref : refs)
      {
         PSKeyword kw = (PSKeyword) PSModelTracker.getInstance().load(ref,
               false);
         if (Integer.parseInt(kw.getValue()) == kwvalue)
         {
            return ref;
         }
      }
      return null;
   }

   private String[] getDataSources()
   {
      //Catalog the datasources and move the default data source to the top
      //Name it as <CMS Repository>
      java.util.List<String> ds = PSCatalogDatasources.getCatalog(false);
      //Remove the repository data source from the list and add at the begining
      for(int i=0; i<ds.size();i++)
      {
         if(PSCatalogDatasources.isRepository(ds.get(i)))
         {
            m_repDataSource = ds.get(i);
            ds.remove(i);
         }
      }
      ds.add(0,PSCatalogDatasources.getDisplayName(m_repDataSource));
      return ds.toArray(new String[ds.size()]);
   }

   private String[] getTableNames(String ds)
   {
      if(ds.equals(PSCatalogDatasources.REPOSITORY_LABEL))
         ds = m_repDataSource;
      java.util.List<String> tables = PSCatalogDatabaseTables.getCatalog(ds,"TABLE",false);
      return tables.toArray(new String[tables.size()]);
   }

   private String[] getColumnNames(String ds, String tn)
   {
      if(ds.equals(PSCatalogDatasources.REPOSITORY_LABEL))
         ds = m_repDataSource;
      PSSqlCataloger cat = new PSSqlCataloger(ds,tn);
      PSDesignerConnection conn = PSCoreFactory.getInstance().getDesignerConnection();
      cat.setConnectionInfo(conn);
      try
      {
         Collection<String> cols = cat.getCatalog();
         return cols.toArray(new String[cols.size()]);
      }
      catch (Exception e)
      {
         String title = "Failed to catalog the table columns";
         String msg = "The following error occured while updating the data";
         PSWorkbenchPlugin.handleException("Control Choices Dialog",title, msg,e);
      }
      return new String[0];
   }

   /**
    * This is used to store the original default values, which will be "feet"
    * into "m_defValueList" control.
    */
   ArrayList<String> m_origDefaultValues = new ArrayList<String>();

   /**
    * Controls
    */
   private Combo m_keyWordCombo;
   private Combo m_dataSourceCombo;
   private Combo m_tableCombo;
   private Combo m_labelColumnCombo;
   private Combo m_valueColumnCombo;
   private Text m_urlText;
   private Text m_emptyEntryLabelText;
   private Text m_emptyEntryValueText;
   private Button[] m_sortOrderRadio = new Button[3];
   private List m_defValueList;
   private PSSortableTable m_choicesTable;
   private PSRadioAndCheckBoxes m_mainRadioComp;
   private PSRadioAndCheckBoxes m_emptyEntryComp;
   private int RADIO_CHILD_LABEL_NUMERATOR = 20;
   private int TEXTAREA_BOX_HEIGHT = 30;
   private PSFieldTableRowDataObject m_rowData;
   private java.util.List<IPSReference> m_kwList;
   private java.util.List<Choice> m_localChoices;
   private PSUrlRequest m_urlRequest;
   private Button m_urlButton;
   private Combo m_eeSortOrderCombo;
   private Combo m_eeIncludeCombo;
   private String m_repDataSource;
   private static final String CMS_REPOSITORY = "<CMS Repository>";

   /**
    * An array of XML attribute values for the includeWhen. They are
    * specified at the index of the specifier.
    */
   private static final String[] INCLUDE_WHEN_LIST =
   {
      "Always", "Only if null"
   };
   /**
    * An array of XML attribute values for the sortOrder. They are
    * specified at the index of the specifier.
    */
   private static final String[] SORT_ORDER_LIST =
   {
      "First", "Last", "Sorted"
   };

}
