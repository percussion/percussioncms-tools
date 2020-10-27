/******************************************************************************
*
* [ PSBaseSelectionListenerAction.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
/**
 * 
 */
package com.percussion.workbench.ui.actions;

import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.help.IPSHelpProvider;
import com.percussion.workbench.ui.help.PSHelpManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

/**
 * BaseSelectionListenerAction extended to implement the
 * help manager.
 */
public class PSBaseSelectionListenerAction extends BaseSelectionListenerAction
   implements IPSHelpProvider
{
   /**
    * @param text Passed to super ctor.
    * @param provider If a provider is supplied, this action will be added as
    * a listener to track selection changes. In this case, the {@link #dispose()}
    * method must be called when the action is no longer needed. May be 
    * <code>null</code>.
    */
   public PSBaseSelectionListenerAction(String text, ISelectionProvider provider)
   {
      super(text);
      m_helpManager = new PSHelpManager(this, this);
      m_provider = provider;
      if (m_provider != null)
         m_provider.addSelectionChangedListener(this);
   }

   /**
    * Convenience ctor that calls
    * {@link #PSBaseSelectionListenerAction(String, ISelectionProvider) 
    * this(text, <code>null</code>)}.
    */
   public PSBaseSelectionListenerAction(String text)
   {
      this(text, null);
   }
   
   /* 
    * @see com.percussion.workbench.ui.help.IPSHelpProvider#
    * getHelpKey(org.eclipse.swt.widgets.Control)
    */
   @SuppressWarnings("unused")
   public String getHelpKey(Control control)
   {      
      return getClass().getName();
   }
   
   /**
    * Remove this action as a listener on the provider supplied in the ctor.
    * Must be called by the creator of this action when they are finished with
    * it.
    */
   public void dispose()
   {
      if (m_provider != null)
      {
         m_provider.removeSelectionChangedListener(this);
         m_provider = null;
      }
   }

   /**
    * Retrieves the first element in the current selection.
    *  
    * @return A valid node, or <code>null</code> if there is no selection or
    * the selection does not contain <code>PSUiReference</code> objects.
    */
   protected PSUiReference getSelectedNode()
   {
      IStructuredSelection ss = getStructuredSelection();
      if (ss == null || !(ss.getFirstElement() instanceof PSUiReference))
         return null;
      
      return (PSUiReference) ss.getFirstElement();
   }

   //see base class method for details
   @Override
   protected void finalize() throws Throwable
   {
      // playing it safe
      dispose();
      super.finalize();
   }
   
   /**
    * The help manager for the action
    */
   protected PSHelpManager m_helpManager;

   /**
    * Used to obtain the selection to determine enablement and perform the
    * action. May be <code>null</code>.
    */
   private ISelectionProvider m_provider;
}
