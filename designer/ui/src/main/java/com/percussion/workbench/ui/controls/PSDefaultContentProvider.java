/******************************************************************************
*
* [ PSDefaultContentProvider.java ]
*
* COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.controls;

import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import java.util.Collection;

/**
 * The <code>PSDefaultContentProvider</code> can be used for the 
 * <code>ComboViewer</code> and <code>ListViewer</code>. It expects
 * that a <code>Collection</code> will be used
 * @author erikserating
 *
 */
public class PSDefaultContentProvider implements IStructuredContentProvider
{

   /**
    * Returns the contents of the input element as an array of objects.
    * @param inputElement expected to be a <code>Collection</code>,
    * must not be <code>null</code>.
    */
   public Object[] getElements(Object inputElement)
   {
      Assert.isNotNull(inputElement);
      Assert.isTrue(inputElement instanceof Collection);
      Collection coll = (Collection)inputElement;
      return coll.toArray();
   }

   /* 
    * @see org.eclipse.jface.viewers.IContentProvider#dispose()
    */
   public void dispose()
   {      
      // no-op
   }

   /* 
    * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(
    * org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
    */
   public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
   {
      // no-op      
   }

   

}
