/*[ ISelectable.java ]*********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

/**
 * A simple interface defining the accessing and mutating of a selected
 * property. Typically used in UIs that have have objects that can be operated
 * upon by the end user.
 */
interface ISelectable
{
   /**
    * Sets the selected property. If an object is selected, it will display
    * a visual indicator. 
    *
    * @param bSelect <code>true</code> to set the property, <code>false</code>
    * to clear the property
    *
    * @param bAppend used to indicate whether the user is doing a multi-select
    * operation; <code>true</code> if she is. This parameter may be useful in
    * certain situations. It is ignored if bSelect is <code>false</code>.
    * 
    * @returns the previous state of the selection property
    */
   public boolean setSelection( boolean bSelect, boolean bAppend );

   /**
    * @returns <code>true</code> if this object's selected property is set, otherwise
    * <code>false</code> is returned.
    */
   public boolean isSelected( );
}


