/******************************************************************************
 *
 * [ PSJexlExtensionContextFilter.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.common;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * A viewer filter for the extension combo viewer that when apply
 * filtering is <code>true</code> will only show contexts that can be
 * used with a Jexl Expression extension. Apply filtering
 * is <code>false</code> by default.
 */
public class PSJexlExtensionContextFilter extends ViewerFilter
{
   
   @Override
   public boolean select(@SuppressWarnings("unused") Viewer viewer,
      @SuppressWarnings("unused") Object parentElement, Object element)
   {
      if(!m_applyFiltering)
         return true;
      String context = (String)element;
      if(context.equals("global/percussion/system/") || 
         context.equals("global/percussion/user/"))
         return true;
      return false;
   }
   
   /**
    * Turn filtering on or off
    * @param apply if <code>true</code> then filtering
    * will occur.
    */
   public void setApplyFiltering(boolean apply)
   {
      m_applyFiltering = apply;
   }
   
   /**
    * @return if <code>true</code> then filtering is turned on.
    */
   public boolean isFilteringOn()
   {
      return m_applyFiltering;
   }
   
   private boolean m_applyFiltering = false;

}
