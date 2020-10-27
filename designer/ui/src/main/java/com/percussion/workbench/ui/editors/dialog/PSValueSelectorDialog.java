/******************************************************************************
*
* [ PSValueSelectorDialog.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.editors.dialog;

import com.percussion.E2Designer.DTHtmlParameter;
import com.percussion.E2Designer.DTSingleHtmlParameter;
import com.percussion.E2Designer.DTTextLiteral;
import com.percussion.E2Designer.E2Designer;
import com.percussion.E2Designer.IDataTypeInfo;
import com.percussion.E2Designer.ValueSelectorDialogHelper;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.PSAbstractLabelProvider;
import com.percussion.workbench.ui.controls.PSDefaultContentProvider;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class PSValueSelectorDialog extends PSDialog
{

   
   
   /**
    * Create the dialog
    * @param parentShell
    */
   public PSValueSelectorDialog(Shell parentShell, java.util.List<IDataTypeInfo> vIDataType,
      IDataTypeInfo defaultType, Object value)
   {
      super(parentShell);
      
      m_value = value;
      init(vIDataType, defaultType);
   }
   
   private void init(java.util.List<IDataTypeInfo> vIDataType, IDataTypeInfo defaultType)
   {
      if(vIDataType == null || vIDataType.size() <= 0)
         throw new IllegalArgumentException(
            "Passed in list of IDataTypeInfo is null or empty");
      m_dataTypes = vIDataType;
      m_defaultType = defaultType;      
   }
   
   private void onTypeChange()
   {
      IStructuredSelection selection = 
         (IStructuredSelection)m_comboViewer.getSelection(); 
      //Clear list
      m_listViewer.setInput(new ArrayList());
      IDataTypeInfo info = (IDataTypeInfo)selection.getFirstElement();
      if(info != null)
      {
         Enumeration items = info.catalog();
         java.util.List<String> values = new ArrayList<String>();
         while(items.hasMoreElements())
         {
            values.add((String)items.nextElement());
         }
         // Pre-populate the Html params if an Html data type
         if(info instanceof DTHtmlParameter 
            || info instanceof DTSingleHtmlParameter)
         {
            for(int i = 0; i < ms_prePopulateHtmlParamValues.length; i++)
               values.add(ms_prePopulateHtmlParamValues[i]);
         }
         if(!values.isEmpty())
            m_listViewer.setInput(values);
      }
      
   }
   
   private void onValueSelected()
   {
      IStructuredSelection selection = 
         (IStructuredSelection)m_listViewer.getSelection(); 
      String value = (String)selection.getFirstElement();
      m_text.setText(StringUtils.defaultString(value));
   }

   /**
    * Create contents of the dialog
    * @param parent
    */
   @Override
   protected Control createDialogArea(Composite parent)
   {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new FormLayout());
     
      //Initialize controls
      m_typeLabel = new Label(container, SWT.NONE);
      
      m_comboViewer = new ComboViewer(container, SWT.READ_ONLY);
      m_comboViewer.setContentProvider(new PSDefaultContentProvider());
      m_comboViewer.setLabelProvider(new DataTypeInfoLabelProvider());
      m_comboViewer.setSorter(new ViewerSorter());
      m_combo = m_comboViewer.getCombo();
      m_combo.addSelectionListener(new SelectionAdapter()
         {
            /* 
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected
             * (org.eclipse.swt.events.SelectionEvent)
             */
            @SuppressWarnings("synthetic-access")
            @Override
            public void widgetSelected(SelectionEvent e)
            {
               onTypeChange();
            }
            
         });
      m_valueLabel = new Label(container, SWT.NONE);
      m_text = new Text(container, SWT.BORDER);
      m_choicesLabel = new Label(container, SWT.NONE);
      m_listViewer = new ListViewer(container, SWT.V_SCROLL | SWT.BORDER);
      m_listViewer.setContentProvider(new PSDefaultContentProvider());
      m_listViewer.setSorter(new ViewerSorter());
      m_list = m_listViewer.getList();
      m_list.addSelectionListener(new SelectionAdapter()
         {
            /* 
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
             * org.eclipse.swt.events.SelectionEvent)
             */
            @SuppressWarnings("synthetic-access")
            @Override
            public void widgetSelected(SelectionEvent e)
            {
               onValueSelected();
            }
            
         });
      
      m_typeLabel.setAlignment(SWT.RIGHT);
      final FormData formData = new FormData();
      formData.top = new FormAttachment(0, 13);
      formData.left = new FormAttachment(0, 0);
      formData.right = new FormAttachment(15, 0);
      m_typeLabel.setLayoutData(formData);
      m_typeLabel.setText("T&ype:");
      
      final FormData formData_1 = new FormData();
      formData_1.right = new FormAttachment(100, -10);
      formData_1.top = new FormAttachment(m_typeLabel, -3, SWT.TOP);
      formData_1.left = new FormAttachment(m_typeLabel, 5, SWT.RIGHT);
      m_combo.setLayoutData(formData_1);
      
     
      m_valueLabel.setAlignment(SWT.RIGHT);
      final FormData formData_2 = new FormData();
      formData_2.right = new FormAttachment(m_typeLabel, 0, SWT.RIGHT);
      formData_2.left = new FormAttachment(m_typeLabel, 0, SWT.LEFT);
      formData_2.top = new FormAttachment(m_combo, 13);
      m_valueLabel.setLayoutData(formData_2);
      m_valueLabel.setText("&Value:");
      
      final FormData formData_3 = new FormData();
      formData_3.right = new FormAttachment(m_combo, 0, SWT.RIGHT);
      formData_3.top = new FormAttachment(m_valueLabel, -3, SWT.TOP);
      formData_3.left = new FormAttachment(m_combo, 0, SWT.LEFT);
      m_text.setLayoutData(formData_3);
      
      m_choicesLabel.setAlignment(SWT.RIGHT);
      final FormData formData_4 = new FormData();
      formData_4.right = new FormAttachment(m_valueLabel, 0, SWT.RIGHT);
      formData_4.top = new FormAttachment(m_list, 0, SWT.CENTER);
      formData_4.left = new FormAttachment(m_valueLabel, 0, SWT.LEFT);
      m_choicesLabel.setLayoutData(formData_4);
      m_choicesLabel.setText("&Choices:");
      
      
      final FormData formData_5 = new FormData();
      formData_5.bottom = new FormAttachment(100, 0);
      formData_5.right = new FormAttachment(m_text, 0, SWT.RIGHT);
      formData_5.top = new FormAttachment(m_text, 10, SWT.BOTTOM);
      formData_5.left = new FormAttachment(m_text, 0, SWT.LEFT);
      m_list.setLayoutData(formData_5);      
      //
      
      m_comboViewer.setInput(m_dataTypes);
      if(m_defaultType == null)
         m_defaultType = new DTTextLiteral();
      selectByDataTypeName(getClassName(m_defaultType));
      setValue(m_value);
      
      return container;
   }

   /**
    * Create contents of the button bar
    * @param parent
    */
   @Override
   protected void createButtonsForButtonBar(Composite parent)
   {
      createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
         true);
      createButton(parent, IDialogConstants.CANCEL_ID,
         IDialogConstants.CANCEL_LABEL, false);
   }   
   
   /* 
    * @see org.eclipse.jface.dialogs.Dialog#okPressed()
    */
   @Override
   protected void okPressed()
   {
      setResult();
      super.okPressed();
   }

   /**
   *
   * Creates the object for external use when the user clicks OK.
   */
  private void setResult()
  {
     IStructuredSelection selection = 
        (IStructuredSelection)m_comboViewer.getSelection(); 
     IDataTypeInfo data = (IDataTypeInfo)selection.getFirstElement();
     String strValue = m_text.getText();

     if (!StringUtils.isBlank(strValue))
        m_result = data.create(strValue);
     else
        m_result = null;
  }

   /**
    * Return the initial size of the dialog
    */
   @Override
   protected Point getInitialSize()
   {
      return new Point(500, 375);
   }
   
   protected void configureShell(Shell newShell)
   {
      super.configureShell(newShell);
      newShell.setText("Value Selector");
   }
   
   /**
    * @param obj assumed not <code>null</code>.
    * @return the class name, without the package prefix
    * for the class passed in.
    */
   private static String getClassName(Object obj)
   {
      String strClass = obj.getClass().getName();
      return strClass.substring( strClass.lastIndexOf('.')+1);
   }
   
   /** 
    * @param obj the data type object, assumed not <code>null</code>.
    * @return the display name for the data type passed in. May
    * be <code>null</code> if not found.
    */
   private static String getDisplayName(Object obj)
   {
      return ms_dataTypeNames.get(getClassName(obj));
   }
   
   /**
    * Initialize the "value" object with the provided data.
    *
    * @param value the value text
    */
   protected void setValue(Object value)
   {
      
      if (value == null)
         return;
      if (value instanceof IPSReplacementValue)
         m_text.setText(
            ((IPSReplacementValue) value).getValueText());
      else
         m_text.setText( parseValue(value.toString(), true) );
      
      updateTypeCombo( value );
      
   }
   
   /**
    * Parse the provided value into its components (type, value) and return the
    * value.
    *
    * @param value the value in the format type/value
    * @param returnValue flag indicating the value should be returned
    * or if <code>false</code> then the type wil be returned.
    */
   private static String parseValue(String value, boolean returnValue)
   {
      int index = value.indexOf("/");
      if(returnValue)
      {
         if (index >= 0)
            return value.substring(index + 1, value.length());         
      }
      else if(index != -1)
      {
         return value.substring(0, index);
      }
      return value;
   }
   
   /**
    * Sets the selected item of the data type combo box based on the type of
    * the object supplied as <code>value</code>.
    *
    * @param value the value that is an instance of IDataTypeInfo
    * If <code>null</code>, this method does nothing.
    */
   private void updateTypeCombo(Object value)
   {
      if (value == null)
         return;

      if (value instanceof IDataTypeInfo)
      {
         selectByDataTypeName(getClassName(value));
      }
      else if (value instanceof IPSReplacementValue)
      {
         String type = "DT" + ((IPSReplacementValue) value).getValueType();
         selectByDataTypeName(type);
      }
      else if(value instanceof String && !StringUtils.isBlank((String)value))
      {
         String type = parseValue((String)value, false);
         String DT = null;
         if(type.toLowerCase().startsWith("psx"))
         {
            DT = "DT" + type.substring(3);
            if(!ms_dataTypeNames.containsKey(DT))
               DT = null;              
         }
         if(DT == null)
            DT = "DTTextLiteral";
         selectByDataTypeName(DT);
         
      }
      else
      {         
         // set the default if it exists
         if (m_defaultType != null)
         {
           selectByDataTypeName(getClassName(m_defaultType));
         }
      }
   }
   
   /**
    * Set the selection of the data type combo viewer to the data type
    * of the data type name specified.
    * @param name assumed not <code>null</code> or empty.
    */
   private void selectByDataTypeName(String name)
   {
      java.util.List all = (java.util.List)m_comboViewer.getInput();
      for(Object obj : all)
      {
         if(name.equals(getClassName(obj)))
         {
            m_comboViewer.setSelection(
               new StructuredSelection(new Object[]{obj}));
            onTypeChange();
            break;
         }
            
      }
   }
   
   /**
    * Determines if <code>model</code> is an object that can be edited by
    * this dialog.  An object is editable if it is an instance of either
    * <code>IDataTypeInfo</code> or <code>IPSReplacementValue</code> and if
    * its class has been registered in the designer resources.
    *
    * @param model object to check for editability.  If <code>null</code>,
    * this method will return <code>false</code>.
    *
    * @return <code>true</code> if the object is editable by this dialog;
    * <code>false</code> otherwise.
    */
   public boolean isValidModel(Object model)
   {
      return myIsValidModel( model );
   }

   /**
    * Creates a new replacement value based on the supplied string.
    * 
    * @return <code> null </code> if object type was not found, else a new
    *         object based on original object
    */
   public static IPSReplacementValue createReplacementValue(String value)
   {
      IPSReplacementValue retObject = null;
      if (!StringUtils.isBlank(value))
      {

         String valText = parseValue(value, true);
         // get the object type
         String strname = parseValue(value, false);
         if (strname.toLowerCase().startsWith("psx"))
         {
            strname = strname.substring(3);
         }
         // and get the class text

         String classKey = E2Designer.getResources().getString(strname);
         // get the display name, this is the same name that we use
         // to create the object
         if (classKey.startsWith("DT") || classKey.startsWith("PS"))
         {
            // convert datatype to display string
            String key = classKey.substring(2, classKey.length());
            classKey = E2Designer.getResources().getString(
                  E2Designer.getResources().getString(key));
         }
         if (classKey == null || classKey.trim().length() < 1)
            return null;
         // now walk trought the list and get the proper object type
         ValueSelectorDialogHelper helper = new ValueSelectorDialogHelper();
         Object item = null;
         for (int i = 0; i < helper.getDataTypes().size(); i++)
         {
            // get the object
            Object obj = helper.getDataTypes().get(i);
            if (!(obj instanceof IDataTypeInfo))
               continue;
            IDataTypeInfo data = (IDataTypeInfo) obj;
            // it matches?
            if (data.getDisplayName().equals(classKey))
            {
               item = data; // yes return the object
            }
         }

         if (item != null && valText.length() > 0)
         {
            // cast to IDataTypeInfo
            IDataTypeInfo data = (IDataTypeInfo) item;
            // construct it
            try
            {
               retObject = (IPSReplacementValue) data.create(valText);
            }
            catch (IllegalArgumentException e)
            {
               String msg = "Couldn't convert the input string into the specified type.";
               String title = "Type creation error";
               String context = "Default value control";
               PSWorkbenchPlugin.handleException(context, title, msg, e);
               retObject = null;
            }
         }
      }
      // return the new object or null
      return (retObject);
   }


   /**
    * Determines if <code>model</code> is an object that can be edited by
    * this dialog.  An object is editable if it is an instance of either
    * <code>IDataTypeInfo</code> or <code>IPSReplacementValue</code> and if
    * its class has been registered in the designer resources.
    * This method is used by <code>setData</code> because it cannot be overriden.
    * (If <code>setData</code> called <code>isValidModel</code>, then if
    * a derived class called <code>super.setData</code>, the super class might
    * use the derived class' <code>isValidModel</code> instead -- which would
    * cause a false result if the derived class had different requirements than
    * the super class.)
    *
    * @param model object to check for editability.  If <code>null</code>,
    * this method will return <code>false</code>.
    *
    * @return <code>true</code> if the object is editable by this dialog;
    * <code>false</code> otherwise.
    */
   private boolean myIsValidModel(Object model)
   {      
      if (model instanceof IDataTypeInfo)
      {
         if(getDisplayName(model) != null)               
            return true;
      }
      else if(model instanceof IPSReplacementValue)
      {
         String name = "DT" + ((IPSReplacementValue)model).getValueType();
         if(ms_dataTypeNames.containsKey(name))
            return true;
      }
         
      return false;
   }
   
   /**
    * Label provider for data type info objects
    */
   public class DataTypeInfoLabelProvider extends PSAbstractLabelProvider
   {

      /* 
       * @see org.eclipse.jface.viewers.ILabelProvider#
       * getText(java.lang.Object)
       */
      @SuppressWarnings("synthetic-access")
      public String getText(Object element)
      {
         return StringUtils.defaultString(getDisplayName(element));         
      }

   }
   
   /**
    * Returns the value selected by the user.
    * @return may be <code>null</code>.
    */
   public Object getValue()
   {
      return m_result;
   }
   
   private List m_list;
   private ListViewer m_listViewer;
   private Label m_choicesLabel;
   private Text m_text;
   private Label m_valueLabel;
   private Combo m_combo;
   private ComboViewer m_comboViewer;
   private Label m_typeLabel;
   private java.util.List<IDataTypeInfo> m_dataTypes;
   private IDataTypeInfo m_defaultType;
   private Object m_result;
   private Object m_value;
   
   /**
    * Values that will be pre-populated in the choice field for
    * HTML param types.
    */
   private static String[] ms_prePopulateHtmlParamValues = 
      {
      "sys_authtype",
      "sys_contentid",
      "sys_contenttypeid",
      "sys_context",
      "sys_communityid",
      "sys_folderid",
      "sys_revision",
      "sys_siteid",
      "sys_slotid",
      "sys_slotname",
      "sys_stateid",
      "sys_variantid",
      "sys_workflowid"
      };
   
   private static Map<String, String> ms_dataTypeNames = 
      new HashMap<String, String>();
   static
   {    
      ms_dataTypeNames.put("DTCgiVariable", 
         "CGI Variable");
      ms_dataTypeNames.put("DTSingleHtmlParameter", 
         "Single HTML Parameter");
      ms_dataTypeNames.put("DTHtmlParameter", 
         "HTML Parameter");
      ms_dataTypeNames.put("DTCookie", 
         "Cookie");
      ms_dataTypeNames.put("DTXMLField", 
         "XML Element");
      ms_dataTypeNames.put("DTBackendColumn", 
         "Backend Column");
      ms_dataTypeNames.put("DTTextLiteral", 
         "Literal");
      ms_dataTypeNames.put("DTNumericLiteral", 
         "Number");
      ms_dataTypeNames.put("DTDateLiteral", 
         "Date");
      ms_dataTypeNames.put("DTUserContext", 
         "User Context");
      ms_dataTypeNames.put("DTContentItemStatus", 
         "Content Item Status");
      ms_dataTypeNames.put("DTContentItemData", 
         "Content Item Data");
      ms_dataTypeNames.put("DTRelationshipProperty", 
         "Current Relationship Property");
      ms_dataTypeNames.put("DTOriginatingRelationshipProperty", 
         "Originating Relationship Property");
      ms_dataTypeNames.put("DTMacro", "Macro");
   }

}
