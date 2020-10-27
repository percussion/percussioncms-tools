/*[ IDataTypeInfo.java ]*******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import java.util.Enumeration;

/**
 * This interface is used with the Value Selector dialog. The calling class
 * creates objects that implement this interface and are appropriate to the
 * current context. The Value Selector dialog then uses these values to query
 * the end user for a new value, creating the matching PS<DataType> object
 * to return. Objects implementing this interface generally use DT<DataType>
 * as their class name. Exampls of the PS<DataType> are PSBackEndColumn, PSCookie,
 * PSCgiVariable, etc.
 */
public interface IDataTypeInfo
{
   /**
    * @returns <code>true</code> if the value field should be editable by the
    * user.
    */
   public boolean allowUncatalogedValues();
   
   /**
    * @returns the displayable name for this type
    */
   public String getDisplayName();

   /**
    * Creates a PS<DataType> object appropriate for this data type object,
    * using the supplied text as the value for the object.
    *
    * @param value The text used to create the underlying object. Typically
    * <code>null</code> is not allowed, but see the implementing class for
    * requirements of this parameter.
    *
    * @throws IllegalArgumentException if value must be an entry from the
    * cataloged values but it isn't or value doesn't conform to the
    * allowed values for the underlying server object.
    */
   public Object create( String value );

   /**
    * @returns a vector that contains String objects that are possible values 
    * to use when calling <code>create</code>. If allowUncatalogedValues() 
    * returns <code>false</code>, then create must be called with one of these
    * values.
    */
   public Enumeration catalog();
}   
