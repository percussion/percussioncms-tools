/******************************************************************************
*
* [ PSTitleAreaDialog.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
/**
 * 
 */
package com.percussion.workbench.ui.editors.dialog;

import com.percussion.workbench.ui.help.IPSHelpProvider;
import com.percussion.workbench.ui.help.PSHelpManager;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Title Area Dialog class with help provider implementation
 */
public class PSTitleAreaDialog extends TitleAreaDialog
   implements
      IPSHelpProvider
{

   /**
    * @param parentShell
    */
   public PSTitleAreaDialog(Shell parentShell)
   {
      super(parentShell);
   }

   /* 
    * @see com.percussion.workbench.ui.help.IPSHelpProvider#getHelpKey(
    * org.eclipse.swt.widgets.Control)
    */
   public String getHelpKey(Control control)
   {
      return getClass().getName();
   }

   /**
    * This is a special case, normally this method should not be
    * overriden, but we need a place were we can be sure the
    * controls have been created so we can initialize the help
    * manager. 
    * @see org.eclipse.jface.dialogs.Dialog#createContents(
    * org.eclipse.swt.widgets.Composite)
    */
   @Override
   protected Control createContents(Composite parent)
   {
     Control control = super.createContents(parent);
     m_helpManager = new PSHelpManager(this, control);
     return control;
   }
   
   /**
    * The help manager for the dialog
    */
   protected PSHelpManager m_helpManager;
   
   

}
