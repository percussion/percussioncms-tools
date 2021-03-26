/******************************************************************************
*
* [ PSHelpView.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
/**
 * 
 */
package com.percussion.workbench.ui.views;

import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.editors.common.PSEditorHelpHints;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;

/**
 * View that will display field level help for Editors and
 * wizards.
 */
public class PSHelpView extends ViewPart
{
   /**
    * The view id as specified in the plugin.xml.
    */
   public static final String ID = PSHelpView.class.getName();

   /* 
    * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(
    * org.eclipse.swt.widgets.Composite)
    */
   @Override
   public void createPartControl(Composite parent)
   {
      Display display = PSWorkbenchPlugin.getDefault().getWorkbench().getDisplay();
      Composite comp = new Composite(parent, SWT.NONE);
      comp.setLayout(new FormLayout());
      comp.setBackground(display.getSystemColor(SWT.COLOR_WHITE));      
      
      m_browser = new Browser(comp, SWT.NONE);
      final FormData formData = new FormData();
      formData.bottom = new FormAttachment(100, 0);
      formData.top = new FormAttachment(0, 0);
      formData.right = new FormAttachment(100, 0);
      formData.left = new FormAttachment(0, 0);
      m_browser.setLayoutData(formData);      

   }

   /* 
    * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
    */
   @Override
   public void setFocus()
   {
      m_browser.setFocus();
   }
   
   /**
    * Display help specified by the passed in key in this
    * view.
    * @param key may be <code>null</code> or empty.
    */
   @SuppressWarnings("unused") 
   public void displayHelp(String key)
   {
      if(m_lastKey.equals(key))
         return;
      System.out.println(key); // fixme: remove after debugging done
      if(StringUtils.isBlank(key))
      {
         m_browser.setText("");
         m_lastKey = "";
      }
      else
      {
        String message = PSEditorHelpHints.getMessage(key);
        
        if(message == null)
           message = "";
        m_browser.setText(message);
        m_lastKey = key;
      }
   }
     
   // Controls
   private Browser m_browser;
   private String m_lastKey = "";
  

}
