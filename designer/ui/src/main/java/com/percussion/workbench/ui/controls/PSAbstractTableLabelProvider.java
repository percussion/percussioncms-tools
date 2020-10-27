/******************************************************************************
*
* [ PSAbstractTableLabelProvider.java ]
*
* COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.controls;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import java.util.ArrayList;
import java.util.Collection;

/**
 *  A convenience class to make it easier to implement <code>ITableLabelProvider</code>
 *  classes without having to implement all the methods.
 *  {@link #getColumnText(Object, int)} is the only method that must
 *  be implemented.
 */
public abstract class PSAbstractTableLabelProvider implements ITableLabelProvider
{

   /* 
    * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(
    * java.lang.Object, int)
    */
   public Image getColumnImage(Object element, int columnIndex)
   {
      return null;
   }

   /* 
    * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(
    * org.eclipse.jface.viewers.ILabelProviderListener)
    */
   public void addListener(ILabelProviderListener listener)
   {
      if(!m_listeners.contains(listener))
         m_listeners.add(listener);
   }

   /* 
    * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
    */
   public void dispose()
   {
            
   }

   /* 
    * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(
    * java.lang.Object, java.lang.String)
    */
   public boolean isLabelProperty(Object element, String property)
   {
      return false;
   }

   /* 
    * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(
    * org.eclipse.jface.viewers.ILabelProviderListener)
    */
   public void removeListener(ILabelProviderListener listener)
   {
      if(m_listeners.contains(listener))
         m_listeners.remove(listener);            
   }
   
   /**
    * All listeners registered to this label provider, never <code>null</code>,
    * may be empty.
    */
   protected Collection<ILabelProviderListener> m_listeners = 
      new ArrayList<ILabelProviderListener>(); 

   

}
