/******************************************************************************
*
* [ PSDefaultValueControl.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.controls;

import com.percussion.E2Designer.E2Designer;
import com.percussion.E2Designer.FormulaPropertyDialog;
import com.percussion.E2Designer.IDataTypeInfo;
import com.percussion.E2Designer.OSExtensionCall;
import com.percussion.E2Designer.PSUdfSet;
import com.percussion.E2Designer.ValueSelectorDialogHelper;
import com.percussion.client.PSCoreFactory;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSExtensionParamValue;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.editors.dialog.PSValueSelectorDialog;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * A text box control with eclipse button associated with it to represent the
 * default value of a PSField object. 
 */
public class PSDefaultValueControl extends Composite
      implements
         IPSUiConstants,
         SelectionListener
{
   /**
    * Constructor
    * @param parent Parent composite must not be <code>null</code>.
    * @param style SWT style options for this composite
    * @param options option bits to show the appropriate menus, valid 
    * values are FUNCTION_SELECTOR,OTHERVALUE_SELECTOR and LINK_SELECTOR
    * 
    */
   public PSDefaultValueControl(Composite parent, int style, int options) 
   {
      super(parent, style);
      if (parent == null)
      {
         throw new IllegalArgumentException("parent must not be null");
      }
      m_options = options;
      setLayout(new FormLayout());
      m_valueText = new Text(this,SWT.BORDER);
      m_button = new Button(this,SWT.NONE);
      m_repValue = new PSTextLiteral(StringUtils.EMPTY);
      
      final FormData formData_1 = new FormData();
      formData_1.height = 21;
      formData_1.width = 21;
      formData_1.right = new FormAttachment(100, 0);
      formData_1.top = new FormAttachment(0, 0);
      m_button.setLayoutData(formData_1);
      m_button.setText("...");
      m_button.addSelectionListener(this);
      
      final FormData formData = new FormData();
      formData.top = new FormAttachment(0, 0);
      formData.left = new FormAttachment(0, 0);
      formData.right = 
         new FormAttachment(m_button, -BUTTON_HSPACE_OFFSET, SWT.LEFT);
      m_valueText.setLayoutData(formData);    
      m_valueText.addFocusListener(new FocusListener(){

         @SuppressWarnings("synthetic-access")
         public void focusGained(FocusEvent e)
         {
            if(m_repValue != null)
               m_valueText.setText(m_repValue.getValueText());
         }

         @SuppressWarnings("synthetic-access")
         public void focusLost(FocusEvent e)
         {
            m_repValue = (IPSReplacementValue) createObject();
            String dt = StringUtils.EMPTY;
            if(m_repValue != null)
               dt = StringUtils.defaultString(m_repValue.getValueDisplayText());
            m_valueText.setText(dt);
            m_field.setDefault(m_repValue);
         }
         
      });
      setTabList(new Control[]{m_valueText,m_button});
   }

   /* 
    * @see org.eclipse.swt.events.SelectionListener#widgetSelected(
    * org.eclipse.swt.events.SelectionEvent)
    */
   public void widgetSelected(SelectionEvent e)
   {
      if (e.getSource() == m_button)
      {
         Menu menu = new Menu(getShell(), SWT.POP_UP);
         if ((m_options & LINK_SELECTOR) != 0)
         {
            MenuItem link = new MenuItem(menu, SWT.NONE);
            link.setText("Link...");
            link.addSelectionListener(new SelectionAdapter()
            {
               // TODO Open link dialog
                  // Open the old dialog with the replacement value
                  // Update upon OK of the dialog box
               });
         }
         if ((m_options & FUNCTION_SELECTOR) != 0)
         {
            MenuItem udf = new MenuItem(menu, SWT.NONE);
            udf.setText("User defined function...");
            udf.addSelectionListener(new SelectionAdapter()
            {
               @Override
               public void widgetSelected(SelectionEvent e1)
               {
                  final Display display = getShell().getDisplay();
                  SwingUtilities.invokeLater( new Runnable()
                     {
                     @SuppressWarnings("synthetic-access")
                     public void run()
                     {            
                        PSUdfSet udfSet =  new PSUdfSet("ALL",
                              PSCoreFactory.getInstance().
                                 getDesignerConnection());
                        FormulaPropertyDialog dlg =
                           new FormulaPropertyDialog(
                                 (Frame)null,udfSet,null,null);
                        if (m_repValue != null && m_repValue instanceof 
                              PSExtensionCall)
                        {
                           OSExtensionCall osCall = new OSExtensionCall(
                                 (PSExtensionCall)m_repValue);
                           dlg.setData( osCall);
                        }
                        dlg.setVisible(true);
                        OSExtensionCall result = (OSExtensionCall) dlg.getData();
                        dlg.dispose();
                        if (result != null)
                        {
                           //save the dialog result
                           m_repValue = (PSExtensionCall)result; 
                           display.asyncExec(new Runnable()
                              {
                                 public void run()
                                 {
                                    if (m_repValue!=null)
                                    {
                                       m_field.setDefault(m_repValue);
                                       m_valueText.setText(createUdfDisplayText(
                                             (PSExtensionCall) m_repValue));   
                                    }
                                    m_valueText.setEditable(false);
                                    setTabOrder();
                                 }
                              });
                        }
                     }
                     });

                  super.widgetSelected(e1);
               }
            });
         }
         if ((m_options & OTHERVALUE_SELECTOR) != 0)
         {
            MenuItem ov = new MenuItem(menu, SWT.NONE);
            ov.setText("Other value...");
            ov.addSelectionListener(new SelectionAdapter()
            {
               @SuppressWarnings("synthetic-access")
               @Override
               public void widgetSelected(SelectionEvent e2)
               {
                  String val = "";
                  if (!(m_repValue == null || m_repValue instanceof 
                        PSExtensionCall))
                     val = m_repValue.getValueDisplayText();
                  PSValueSelectorDialog dialog = 
                     new PSValueSelectorDialog(getShell(),
                        new ArrayList<IDataTypeInfo>(
                              m_valSelDlgHelper.getDataTypes()),
                        null, StringUtils.defaultString(val));
                  int status = dialog.open();               
                  if(status == Dialog.OK)
                  {
                     m_repValue = (IPSReplacementValue) dialog.getValue();
                     m_field.setDefault(m_repValue);
                     if(m_repValue != null)
                        m_valueText.setText(m_repValue.getValueDisplayText());
                     m_valueText.setEditable(true);
                     setTabOrder();
                  }
                  super.widgetSelected(e2);
               }
            });
         }
         menu.setVisible(true);
      }
   }

   /* 
    * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(
    * org.eclipse.swt.events.SelectionEvent)
    */
   public void widgetDefaultSelected(SelectionEvent e)
   {
   }

   /**
    * Sets the values of for this control.
    * @param field The PSField object, whose default value is represented by
    *           this control. Must not be <code>null</code>.
    */
   public void setValue(PSField field)
   {
      if (field == null)
      {
         throw new IllegalArgumentException("field must not be null");
      }
      m_field = field;
      m_repValue = field.getDefault();
      if (m_repValue == null)
         m_repValue = new PSTextLiteral(StringUtils.EMPTY);
      
      String displayText = "";
      if(m_repValue instanceof PSExtensionCall)
      {
         displayText = createUdfDisplayText((PSExtensionCall)m_repValue);
         m_valueText.setEditable(false);
      }
      else
      {
         displayText = m_repValue.getValueDisplayText();
         m_valueText.setEditable(true);
      }
      m_valueText.setText(displayText);
      setTabOrder();
   }

   /**
    * Sets the tab order based on whether the value text is
    * enabled or not.
    */
   private void setTabOrder()
   {
      if(m_valueText.getEditable())
         setTabList(new Control[]{m_valueText,m_button});
      else
         setTabList(new Control[]{m_button});
   }
   
   /**
    * Gets IPSReplacement value of this object.
    * 
    * @return Default value. May be <code>null</code>.
    */
   public IPSReplacementValue getValue()
   {
      return m_repValue;
   }

   /**
    * Get the UDF display text.
    *
    * @param call the call to create the display text from
    * @return String the UDF display text
    *    never <code>null</code>, might be empty
    * @throws IllegalArgumentException If param is null.
    */
    /////////////////////////////////////////////////////////////////////////////
    public static String createUdfDisplayText(PSExtensionCall call)
    {
       if (call == null)
          throw new IllegalArgumentException("call cannot be null");

       PSExtensionParamValue[] params = call.getParamValues();
       String function = call.getExtensionRef().getExtensionName();
       function += "(";
       for (int i=0, n=params.length; i<n; i++)
       {
          if (params[i] == null)
             function += "";
          else
             function += params[i].getValue().getValueDisplayText();
          if (i<n-1)
           function += ", ";
       }
       function += ")";

       return function;
    }

   /**
    * creates a new object based on the original object
    * 
    * @return <code> null </code> if object type was not found, else a new
    *         object based on original object
    */
   private Object createObject()
   {
      Object retObject = null;
      if (m_repValue != null)
      {

         String valText = m_valueText.getText().trim();
         // get the object type
         String strname = m_repValue.getValueType();
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
         Object item = null;
         for (int i = 0; i < m_valSelDlgHelper.getDataTypes().size(); i++)
         {
            // get the object
            Object obj = m_valSelDlgHelper.getDataTypes().get(i);
            if (!(obj instanceof IDataTypeInfo))
               continue;
            IDataTypeInfo data = (IDataTypeInfo) obj;
            // it matches?
            if (data.getDisplayName().equals(classKey))
            {
               item = data; // yes return the object
               break;
            }
         }

         if (item != null && valText.length() > 0)
         {
            // cast to IDataTypeInfo
            IDataTypeInfo data = (IDataTypeInfo) item;
            // construct it
            try
            {
               retObject = data.create(valText);
            }
            catch (IllegalArgumentException e)
            {
               String msg = PSMessages.getString(
                     "PSDefaultValueControl.error.invalidDefaultValue.message");
               String title = PSMessages.getString(
                     "PSDefaultValueControl.error.invalidDefaultValue.title");
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
    * instance of Value selector dialog helper
    */
   private ValueSelectorDialogHelper m_valSelDlgHelper = 
      new ValueSelectorDialogHelper();
   
   /**
    * This controls field data
    */
   private PSField m_field;

   /**
    * The data for this control IPSReplacement value
    */
   private IPSReplacementValue m_repValue;
   
   /**
    * Eclipse Button control
    */
   private Button m_button;
   
   /**
    * Default value text box
    */
   private Text m_valueText;
   
   /**
    * Options for this class
    */
   private int m_options;
   
   /**
    * Option to display User defined function menu item.
    */
   public static final int FUNCTION_SELECTOR = 1 << 1;
   
   /**
    * Option to display User defined function menu item.
    */
   public static final int OTHERVALUE_SELECTOR = 1 << 2;

   /**
    * Option to display User defined function menu item.
    */
   public static final int LINK_SELECTOR = 1 << 3;
   
}
