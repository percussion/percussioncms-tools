/******************************************************************************
 *
 * [ IPSPropertyDescriptor.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.views.properties;

import org.eclipse.ui.views.properties.IPropertyDescriptor;

/**
 * Adds the concept of a data type to the descriptor. This is provided to allow
 * proper sorting when rendering multiple properties.
 * 
 * @version 6.0
 * @author paulhoward
 */
public interface IPSPropertyDescriptor extends IPropertyDescriptor
{
   public enum DataTypes
   {
      NUMBER,
      DATE,
      TEXT
   }
   /**
    * @return The basic data type of this object, such as number or text. One of
    * the {@link DataTypes} values.
    */
   public DataTypes getPrimitiveDataType();

}
