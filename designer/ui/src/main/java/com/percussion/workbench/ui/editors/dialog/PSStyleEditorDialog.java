/******************************************************************************
 *
 * [ PSStyleEditorDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.dialog;

import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PSStyleEditorDialog extends PSDialog implements IPSUiConstants
{
   
   
   /**
    * Create the dialog
    * @param parentShell
    * @param args [0] = target, [1] = targetStyle
    */
   public PSStyleEditorDialog(Shell parentShell, String[] args)
   {
      super(parentShell);
      if(args == null)
            throw new IllegalArgumentException("Arguments cannot be null.");
      m_args = args;      
   }

   /**
    * Create contents of the dialog
    * @param parent
    */
   @Override
   protected Control createDialogArea(Composite parent)
   {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new FillLayout());

      m_Composite = new Composite(container, SWT.NONE);
      m_Composite.setLayout(new FormLayout());

      m_targetNameLabel = new Label(m_Composite, SWT.WRAP);
      m_targetNameLabel.setAlignment(SWT.RIGHT);
      final FormData formData = new FormData();
      formData.right = new FormAttachment(25, 0);
      formData.top = new FormAttachment(0, 
         COMMON_BORDER_OFFSET + LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET);
      formData.left = new FormAttachment(0, 0);
      m_targetNameLabel.setLayoutData(formData);
      m_targetNameLabel.setText(
         PSMessages.getString("PSStyleEditorDialog.label.target.name")); //$NON-NLS-1$

      m_targetNameText = new Text(m_Composite, SWT.BORDER);
      m_targetNameText.setTextLimit(255);
      final FormData formData_1 = new FormData();
      formData_1.right = new FormAttachment(100, -5);
      formData_1.top = new FormAttachment(m_targetNameLabel, 
         -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      formData_1.left = new FormAttachment(m_targetNameLabel,
         LABEL_HSPACE_OFFSET, SWT.RIGHT);
      m_targetNameText.setLayoutData(formData_1);

      m_heightLabel = new Label(m_Composite, SWT.NONE);
      m_heightLabel.setAlignment(SWT.RIGHT);
      final FormData formData_2 = new FormData();
      formData_2.right = new FormAttachment(m_targetNameLabel, 0, SWT.RIGHT);
      formData_2.top = new FormAttachment(m_targetNameText, 
         30 + LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      formData_2.left = new FormAttachment(0, 0);
      m_heightLabel.setLayoutData(formData_2);
      m_heightLabel.setText(
         PSMessages.getString("PSStyleEditorDialog.label.height")); //$NON-NLS-1$

      m_heightSpinner = new Spinner(m_Composite, SWT.BORDER);
      m_heightSpinner.setSelection(100);
      m_heightSpinner.setMaximum(9999);
      m_heightSpinner.setMinimum(0);
      final FormData formData_3 = new FormData();
      formData_3.width = 100;
      formData_3.top = new FormAttachment(m_heightLabel,
         -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      formData_3.left = new FormAttachment(m_heightLabel,
         LABEL_HSPACE_OFFSET, SWT.RIGHT);
      m_heightSpinner.setLayoutData(formData_3);

      m_widthSpinner = new Spinner(m_Composite, SWT.BORDER);
      m_widthSpinner.setSelection(100);
      m_widthSpinner.setMaximum(9999);
      m_widthSpinner.setMinimum(0);
      final FormData formData_4 = new FormData();
      formData_4.width = 100;
      formData_4.right = new FormAttachment(100, -5);
      formData_4.top = new FormAttachment(m_heightSpinner, 0, SWT.TOP);
      m_widthSpinner.setLayoutData(formData_4);

      m_widthLabel = new Label(m_Composite, SWT.NONE);
      final FormData formData_5 = new FormData();
      formData_5.right = new FormAttachment(m_widthSpinner,
         -LABEL_HSPACE_OFFSET, SWT.LEFT);
      formData_5.top = new FormAttachment(m_heightLabel, 0, SWT.TOP);
      m_widthLabel.setLayoutData(formData_5);
      m_widthLabel.setText(
         PSMessages.getString("PSStyleEditorDialog.label.width")); //$NON-NLS-1$

      m_statusBarButton = new Button(m_Composite, SWT.CHECK);
      final FormData formData_6 = new FormData();
      formData_6.top = new FormAttachment(m_heightSpinner, 30, SWT.DEFAULT);
      formData_6.left = new FormAttachment(m_heightSpinner, 0, SWT.LEFT);
      m_statusBarButton.setLayoutData(formData_6);
      m_statusBarButton.setText(
         PSMessages.getString("PSStyleEditorDialog.label.status.bar")); //$NON-NLS-1$

      m_menuBarButton = new Button(m_Composite, SWT.CHECK);
      final FormData formData_7 = new FormData();
      formData_7.right = new FormAttachment(m_statusBarButton, 0, SWT.RIGHT);
      formData_7.top = new FormAttachment(m_statusBarButton, 5, SWT.BOTTOM);
      formData_7.left = new FormAttachment(m_statusBarButton, 0, SWT.LEFT);
      m_menuBarButton.setLayoutData(formData_7);
      m_menuBarButton.setText(
         PSMessages.getString("PSStyleEditorDialog.label.menu.bar")); //$NON-NLS-1$

      m_toolBarButton = new Button(m_Composite, SWT.CHECK);
      final FormData formData_8 = new FormData();
      formData_8.top = new FormAttachment(m_menuBarButton, 5, SWT.DEFAULT);
      formData_8.left = new FormAttachment(m_menuBarButton, 0, SWT.LEFT);
      m_toolBarButton.setLayoutData(formData_8);
      m_toolBarButton.setText(
         PSMessages.getString("PSStyleEditorDialog.label.tool.bar")); //$NON-NLS-1$

      m_scrollBarsButton = new Button(m_Composite, SWT.CHECK);
      final FormData formData_9 = new FormData();
      formData_9.bottom = new FormAttachment(m_statusBarButton, 0, SWT.BOTTOM);
      formData_9.top = new FormAttachment(m_statusBarButton, 0, SWT.TOP);
      formData_9.left = new FormAttachment(m_heightSpinner, 0, SWT.RIGHT);
      m_scrollBarsButton.setLayoutData(formData_9);
      m_scrollBarsButton.setText("Scroll bars");

      m_locationButton = new Button(m_Composite, SWT.CHECK);
      final FormData formData_10 = new FormData();
      formData_10.top = new FormAttachment(m_menuBarButton, 0, SWT.TOP);
      formData_10.left = new FormAttachment(m_scrollBarsButton, 0, SWT.LEFT);
      m_locationButton.setLayoutData(formData_10);
      m_locationButton.setText("Location");

      m_resizableButton = new Button(m_Composite, SWT.CHECK);
      final FormData formData_11 = new FormData();
      formData_11.top = new FormAttachment(m_toolBarButton, 0, SWT.TOP);
      formData_11.left = new FormAttachment(m_locationButton, 0, SWT.LEFT);
      m_resizableButton.setLayoutData(formData_11);
      m_resizableButton.setText("Resizable");
      //
      setTargetName(StringUtils.defaultString(m_args[0]));
      setStyleOptions(StringUtils.defaultString(m_args[1]));
      
      return container;
   }
   
   /**
    * @return the target name, never <code>null</code>, may be empty.
    */
   public String getTargetName()
   {
      return m_targetNameText.getText();
   }
   
   private void setTargetName(String target)
   {
      m_targetNameText.setText(StringUtils.defaultString(target));
   }
   
   public String getStyleOptions()
   {
      Map<String, String> opts = new HashMap<String, String>(m_extraOptions);
      opts.put("toolbar", m_toolBarButton.getSelection() ? "1" : "0");
      opts.put("menubar", m_menuBarButton.getSelection() ? "1" : "0");
      opts.put("status", m_statusBarButton.getSelection() ? "1" : "0");
      opts.put("resizable", m_resizableButton.getSelection() ? "1" : "0");
      opts.put("location", m_locationButton.getSelection() ? "1" : "0");
      opts.put("scrollbars", m_scrollBarsButton.getSelection() ? "1" : "0");
      if(m_widthSpinner.getSelection() > 0)
         opts.put("width", String.valueOf(m_widthSpinner.getSelection()));
      if(m_heightSpinner.getSelection() > 0)
         opts.put("height", String.valueOf(m_heightSpinner.getSelection()));
      StringBuilder sb = new StringBuilder();
      Iterator<String> it = opts.keySet().iterator();
      int count = 0;
      while(it.hasNext())
      {
         String name = it.next();
         String value = opts.get(name);
         sb.append(name);
         sb.append("=");
         sb.append(value);
         if(++count < opts.size())
            sb.append(", ");
      }
      return sb.toString();
   }
   
   private void setStyleOptions(String options)
   {
      options = StringUtils.defaultString(options);
      // reset all controls
      m_toolBarButton.setSelection(false);
      m_menuBarButton.setSelection(false);
      m_statusBarButton.setSelection(false);
      m_resizableButton.setSelection(false);
      m_locationButton.setSelection(false);
      m_scrollBarsButton.setSelection(false);
      m_widthSpinner.setSelection(0);
      m_heightSpinner.setSelection(0);
      Map<String, String> opts = parseStyleOptions(options);
      Iterator<String> it = opts.keySet().iterator();
      List<String> removalList = new ArrayList<String>();
      while(it.hasNext())
      {
         String rawName = it.next();
         String name = rawName.toLowerCase();
         String value = opts.get(rawName);
         if(name.equals("toolbar"))
         {
            m_toolBarButton.setSelection(value.equals("1"));      
         }
         else if(name.equals("menubar"))
         {
            m_menuBarButton.setSelection(value.equals("1"));
            removalList.add(rawName);
         }
         else if(name.equals("status"))
         {
            m_statusBarButton.setSelection(value.equals("1"));
            removalList.add(rawName);
         }
         else if(name.equals("resizable"))
         {
            m_resizableButton.setSelection(value.equals("1"));
            removalList.add(name);
         }
         else if(name.equals("location"))
         {
            m_locationButton.setSelection(value.equals("1"));
            removalList.add(rawName);
         }
         else if(name.equals("scrollbars"))
         {
            m_scrollBarsButton.setSelection(value.equals("1"));
            removalList.add(rawName);
         }
         else if(name.equals("width"))
         {
            m_widthSpinner.setSelection(Integer.parseInt(value));
            removalList.add(rawName);
         }
         else if(name.equals("height"))
         {
            m_heightSpinner.setSelection(Integer.parseInt(value));
            removalList.add(rawName);
         }
      }
      for(String key : removalList)
         opts.remove(key);
      
      m_extraOptions = opts;      
   }
   
   public static Map<String, String> parseStyleOptions(String options)
   {
      Map<String, String> results = new HashMap<String, String>();
      String[] pairs = options.split(",");
      for(String pair : pairs)
      {
         String[] nv = pair.trim().split("=");
         String name = nv[0].trim();
         String value = nv.length == 2 ? nv[1].trim() : null;
         String type = (String)ms_validStyleOptions.get(name.toLowerCase());
         if(type == null)
            continue; //Throw away invalid options
         if(type.equals("int") && value == null)
            continue; //Throw away integer value options with no value
         if(type.equals("bool"))
         {
           if(value == null || value.equalsIgnoreCase("yes"))
            {
               value = "1";
            }
           else if(value.equalsIgnoreCase("no") || value.equals("0"))
           {
              value = "0";
           }
           else
           {
              value = "1";
           }
         }
         if(type.equals("int"))
         {
            try
            {
               Integer.parseInt(value);
            }
            catch(NumberFormatException e)
            {
               continue; // Throw away options with invalid integer values
            }
         }
         results.put(name, value);
      }
      
      return results;
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
      m_args[0] = getTargetName();
      m_args[1] = getStyleOptions();
      super.okPressed();
   }

   /**
    * Return the initial size of the dialog
    */
   @Override
   protected Point getInitialSize()
   {
      return new Point(445, 235);
   }
   protected void configureShell(Shell newShell)
   {
      super.configureShell(newShell);
      newShell.setText(PSMessages.getString("PSStyleEditorDialog.label.window.title")); //$NON-NLS-1$
   }
   
   // Controls
   private Button m_toolBarButton;
   private Button m_menuBarButton;
   private Button m_statusBarButton;
   private Label m_widthLabel;
   private Spinner m_widthSpinner;
   private Spinner m_heightSpinner;
   private Label m_heightLabel;
   private Text m_targetNameText;
   private Label m_targetNameLabel;
   private Composite m_Composite;
   private Button m_resizableButton;
   private Button m_locationButton;
   private Button m_scrollBarsButton;
   
   private Map<String, String> m_extraOptions = new HashMap<String, String>();
   private String[] m_args;
   
   private static Map<String, String> ms_validStyleOptions = 
      new HashMap<String, String>();
   static
   {
      ms_validStyleOptions.put("alwayslowered", "bool"); 
      ms_validStyleOptions.put("alwaysraised", "bool");
      ms_validStyleOptions.put("dependent", "bool");
      ms_validStyleOptions.put("directories", "bool");
      ms_validStyleOptions.put("height", "int");
      ms_validStyleOptions.put("hotkeys", "bool");
      ms_validStyleOptions.put("innerheight", "int");
      ms_validStyleOptions.put("innerwidth", "int");  
      ms_validStyleOptions.put("location", "bool");
      ms_validStyleOptions.put("menubar", "bool");  
      ms_validStyleOptions.put("outerheight", "int");
      ms_validStyleOptions.put("outerwidth", "int");  
      ms_validStyleOptions.put("resizable", "bool");
      ms_validStyleOptions.put("screenx", "int");  
      ms_validStyleOptions.put("screeny", "int");
      ms_validStyleOptions.put("scrollbars", "bool");  
      ms_validStyleOptions.put("status", "bool");
      ms_validStyleOptions.put("titlebar", "bool");    
      ms_validStyleOptions.put("toolbar", "bool");
      ms_validStyleOptions.put("width", "int");    
      ms_validStyleOptions.put("z-lock", "bool");
   }

}
