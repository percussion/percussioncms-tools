/******************************************************************************
 *
 * [ PSAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
/**
 * 
 */
package com.percussion.workbench.ui.actions;

import com.percussion.workbench.ui.help.IPSHelpProvider;
import com.percussion.workbench.ui.help.PSHelpManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Control;

/**
 * BaseSelectionListenerAction extended to implement the
 * help manager.
 */
public class PSAction extends Action
   implements
      IPSHelpProvider
{
   
   
   public PSAction()
   {
      super();
      m_helpManager = new PSHelpManager(this, this);
   }

   public PSAction(String text, ImageDescriptor image)
   {
      super(text, image);
      m_helpManager = new PSHelpManager(this, this);
   }

   public PSAction(String text, int style)
   {
      super(text, style);
      m_helpManager = new PSHelpManager(this, this);
   }

   public PSAction(String text)
   {
      super(text);
      m_helpManager = new PSHelpManager(this, this);
   }
      

   /* 
    * @see com.percussion.workbench.ui.help.IPSHelpProvider#
    * getHelpKey(org.eclipse.swt.widgets.Control)
    */
   public String getHelpKey(@SuppressWarnings("unused") Control control)
   {      
      return getClass().getName();
   }
   
   /**
    * The help manager for the action
    */
   protected PSHelpManager m_helpManager;

}
