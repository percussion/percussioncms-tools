/*[ ICatalogEntry.java ]*******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.browser;

import javax.swing.*;

/**
 * A catalog entry is an object that is to be displayed as a node in a browser
 * tree. It is designed to be used with the IHierarchyConstraints object.
 * An entry always has a non-empty display name, and possibly a different
 * internal name. It may optionally have an associated data object; and it always
 * has a type.
 *
 * @see IHierarchyConstraints
 */
public interface ICatalogEntry
{
   /**
    * The display name is the text that is displayed in the browser. It must
    * not be empty.
    */
   public String getDisplayName( );

   /**
    * Sets the display text. 
    *
    * @param strName a non-empty text string, certain characters are excluded
    *
    * @throws IllegalArgumentException if strName is null, empty or contains
    * any non-valid character
    */
   public void setDisplayName( String strName );

   /**
    * If no internal name is supplied, it is assumed to be the same as the display
    * name. Use hasInternalName() to determine if this object has a different
    * internal name. If setInternalName is not supported, this method will
    * return the display name.
    */
   public String getInternalName( );

   /**
    * Sets the internal name of the object. If strName matches the display name
    * identically, (including case), the internal name is set to null
    * (hasInternalName() will return false in this case).
    *
    * @param strName may be null, which means the internal name matches the
    * display name, or empty, but it cannot contain invalid characters
    *
    * @throws IllegalArgumentException if strName contains invalid chars
    * @throws UnsupportedOperationException if the implementation doesn't support an
    * internal name
    */
   public void setInternalName( String strName );

   /**
    * Returns the data object associated with this entry. May be null. This
    * object does not use the data object, it is only stored for use by users
    * of this object. If setData is not supported, this method always returns
    * null.
    */
   public Object getData( );

   /**
    * Sets the data object. The current data object is no longer referenced.
    *
    * @param Data may be null
    *
    * @throws UnsupportedOperationException if this object can't store a data member
    */
   public void setData( Object Data );

   /**
    * Returns the type of this object. The type is interpreted by the user
    * of the entry.
    */
   public int getType( );

   /**
    * Sets the type of this object. This object does not use the passed in type,
    * it stores the type for others to use and interpret.
    */
   public void setType( int Type );

   /**
    * Returns true if this object has a display name name different from the internal
    * name. This is a case sensitive compare.
    */
   public boolean hasDisplayNameDifferentThanInternalName( );

   public ImageIcon getIcon();

   public void setIcon(ImageIcon icon);

}



