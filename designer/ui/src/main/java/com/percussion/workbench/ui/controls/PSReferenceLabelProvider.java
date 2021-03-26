/******************************************************************************
 *
 * [ PSReferenceLabelProvider.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.controls;

import com.percussion.client.IPSReference;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import java.util.List;

/**
 * A Label provider that is meant to be used for lists, combo boxes and tables
 * that contain all <code>IPSReference</code> objects for values. This provider
 * will return the name when <code>getText()</code> is called.
 *  
 * @author erikserating
 *
 */
public class PSReferenceLabelProvider implements ILabelProvider, 
   ITableLabelProvider
{
   
   /**
    * Ctor that creates a label provider that returns the reference
    * name when getText() is called. 
    */
   public PSReferenceLabelProvider()
   {
      this(false);
   }
   
   /**
    * Ctor that creates a label provider that returns the reference
    * name or label when getText() is called. 
    * @param useLabel if <code>true</code> then the reference label is returned
    * by getText() or if <code>false</code> then the name is returned.
    */
   public PSReferenceLabelProvider(boolean useLabel)
   {
      m_useLabel = useLabel;
   }
   
   /**
    * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
    * @return always returns <code>null</code>.
    */   
   public Image getImage(@SuppressWarnings("unused") Object element)
   {
      // Always return null
      return null;
   }

   /** 
    * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
    * @return calls <code>getName()</code> for the <code>IPSReference</code>.
    */
   public String getText(Object element)
   {
      if(!(element instanceof IPSReference))
         throw new IllegalArgumentException("IPSReference was expected.");
      if(element == null)
         return "";
      String text = m_useLabel 
      ? getRefLabel((IPSReference)element) 
         : getRefName((IPSReference)element);
      return StringUtils.defaultString(text);
   }
   
   /**
    * Attempts to get local cached object name first and
    * if fails then returns the refs stored name.
    * @param ref assumed not <code>null</code>
    * @return never <code>null</code> or empty.
    */
   private String getRefName(IPSReference ref)
   {      
      try
      {
         return ref.getLocalName();
      }
      catch (Exception e)
      {
         return ref.getName();         
      }
   }
   
   /**
    * Attempts to get local cached object label first and
    * if fails then returns the refs stored label.
    * @param ref assumed not <code>null</code>
    * @return never <code>null</code> or empty.
    */
   private String getRefLabel(IPSReference ref)
   {
      try
      {
         return ref.getLocalLabelKey();
      }
      catch (Exception e)
      {
         return ref.getLabelKey();         
      }
   }
   
   /**
    * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(
    * java.lang.Object, int)
    * @return always returns <code>null</code>.
    */
   @SuppressWarnings("unused")
   public Image getColumnImage(Object element, int columnIndex)
   {
      return null;
   }

   /** 
    * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(
    * java.lang.Object, int)
    * @return calls <code>getName()</code> for the <code>IPSReference</code>.
    */
   public String getColumnText(Object element, int columnIndex)
   {
      if(!(element instanceof List))
         throw new IllegalArgumentException("A List was expected.");
      Object obj = ((List)element).get(columnIndex);
      if(!(obj instanceof IPSReference))
         throw new IllegalArgumentException("IPSReference was expected.");
      String text = m_useLabel 
      ? ((IPSReference)obj).getLabelKey() 
         : ((IPSReference)obj).getName();
      return StringUtils.defaultString(text);
   }

   /* 
    * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(
    * org.eclipse.jface.viewers.ILabelProviderListener)
    */
   public void addListener(
      @SuppressWarnings("unused") ILabelProviderListener listener)
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
   @SuppressWarnings("unused")
   public boolean isLabelProperty(Object element, String property)
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* 
    * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(
    * org.eclipse.jface.viewers.ILabelProviderListener)
    */
   public void removeListener(
      @SuppressWarnings("unused") ILabelProviderListener listener)
   {
      // no-op      
   } 

   private boolean m_useLabel;
}
