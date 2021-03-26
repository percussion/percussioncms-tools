/******************************************************************************
 *
 * [ PSPropertyDescriptor.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.views.properties.impl;

import com.percussion.workbench.ui.views.properties.IPSPropertyDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

/**
 * Provides the meta data about a property needed to render it.
 * 
 * @version 6.0
 * @author paulhoward
 */
public class PSPropertyDescriptor extends PropertyDescriptor implements
   IPSPropertyDescriptor
{

   public PSPropertyDescriptor() throws IntrospectionException
   {
      super(null, null);
   }

   //see interface
   public DataTypes getPrimitiveDataType()
   {
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.views.properties.IPropertyDescriptor#createPropertyEditor(org.eclipse.swt.widgets.Composite)
    */
   public CellEditor createPropertyEditor(Composite arg0)
   {
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.views.properties.IPropertyDescriptor#getCategory()
    */
   public String getCategory()
   {
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.views.properties.IPropertyDescriptor#getDescription()
    */
   public String getDescription()
   {
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.views.properties.IPropertyDescriptor#getFilterFlags()
    */
   public String[] getFilterFlags()
   {
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.views.properties.IPropertyDescriptor#getHelpContextIds()
    */
   public Object getHelpContextIds()
   {
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.views.properties.IPropertyDescriptor#getId()
    */
   public Object getId()
   {
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.views.properties.IPropertyDescriptor#getLabelProvider()
    */
   public ILabelProvider getLabelProvider()
   {
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.views.properties.IPropertyDescriptor#isCompatibleWith(org.eclipse.ui.views.properties.IPropertyDescriptor)
    */
   public boolean isCompatibleWith(IPropertyDescriptor arg0)
   {
      return false;
   }

}
