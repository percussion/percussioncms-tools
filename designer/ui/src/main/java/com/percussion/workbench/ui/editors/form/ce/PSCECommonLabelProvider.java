/******************************************************************************
*
* [ PSCECommonLabelProvider.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.client.IPSReference;
import com.percussion.design.objectstore.PSControlMeta;
import com.percussion.design.objectstore.PSField;
import com.percussion.services.content.data.PSKeyword;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

/**
 * A common label provider for content type editor objects.
 */
public class PSCECommonLabelProvider implements ILabelProvider
{
   /**
    * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
    * @return always returns <code>null</code>.
    */   
   public Image getImage(Object element)
   {
      return null;
   }
   
   /** 
    * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
    * @return the name of the object by calling the appropriate method of
    * the passed in object.
    */
   public String getText(Object element)
   {
      String text = "";
      if(element == null)
         text = "";
      else if(element instanceof PSControlMeta)
         text = ((PSControlMeta)element).getName();
      else if(element instanceof PSKeyword)
         text = ((PSKeyword)element).getName();
      else if(element instanceof PSField)
         text = ((PSField)element).getSubmitName();
      else if(element instanceof IPSReference)
         text = ((IPSReference)element).getName();
      return text;
   }

   /* 
    * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(
    * org.eclipse.jface.viewers.ILabelProviderListener)
    */
   public void addListener(ILabelProviderListener listener)
   {
      // TODO Auto-generated method stub

   }

   /* 
    * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
    */
   public void dispose()
   {
      // TODO Auto-generated method stub

   }

   /* 
    * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(
    * java.lang.Object, java.lang.String)
    */
  public boolean isLabelProperty(Object element, String property)
   {
      // TODO Auto-generated method stub
      return false;
   }

  /* 
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(
   * org.eclipse.jface.viewers.ILabelProviderListener)
   */
   public void removeListener(ILabelProviderListener listener)
   {
      // TODO Auto-generated method stub

   }

}
