/******************************************************************************
*
* [ PSAbstractLabelProvider.java ]
*
* COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
/**
 * 
 */
package com.percussion.workbench.ui.controls;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

/**
 * A convenience class to make it easier to implement <code>ILabelProvider</code>
 *  classes without having to implement all the methods.
 *  {@link #getText(Object)} is the only method that must
 *  be implemented.
 */
public abstract class PSAbstractLabelProvider implements ILabelProvider
{
   

   /* 
    * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
    */
   public Image getImage(Object element)
   {
      return null;
   }

   /* 
    * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(
    * org.eclipse.jface.viewers.ILabelProviderListener)
    */
   public void addListener(ILabelProviderListener listener)
   {
      // no-op
   }

   /* 
    * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
    */
   public void dispose()
   {
      // no-op
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
      // no-op
   }

}
