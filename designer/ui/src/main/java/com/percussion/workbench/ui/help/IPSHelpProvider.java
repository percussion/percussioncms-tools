/******************************************************************************
*
* [ IPSHelpProvider.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.help;

import org.eclipse.swt.widgets.Control;

/**
 * This interface must be implemented by any View, Editor or
 * WizardPage that will need to display contextual help.
 */
public interface IPSHelpProvider
{
   
  /**
   * Returns the help key for the current context. In general
   * this will just be the fully qualified classname. There may
   * be cases where more detail will be needed such as on a
   * multi page wizard or editor. In this case the page name
   * would be appended to the classname with an underscore 
   * used to separate it.
   * @param control the control who had focus when help
   * was called. Never <code>null</code>.
   * @return the key, may be <code>null</code> or
   * empty in which case no help will be shown.
   */
   public String getHelpKey(Control control);
   
}
