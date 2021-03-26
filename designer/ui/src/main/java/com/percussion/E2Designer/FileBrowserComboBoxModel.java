/*[ FileBrowserComboBoxModel.java ]********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import java.util.Vector;

/**
 * The model for dirctory listing combo boxes in FileBrowser.  This model extends
 * from DefaultComboBoxModel and thus inherits all its functionalities.  Additionally,
 * this model adds an indentation &quot;level&quot; for display of subdirectories.
 * The level value is stored with the object data in a ComboHierarchy object.
 * This class manages adding/removing elements with this additional layer.
 */

public class FileBrowserComboBoxModel extends javax.swing.DefaultComboBoxModel
{
   //
   // CONSTRUCTORS
   //

   /**
    * Constructs an empty object vector and an empty level vector.
    */
   public FileBrowserComboBoxModel( )
   {
      super( );
   }

   /**
    * Constructs a FileBrowserComboBoxModel object initialized with
    * a vector for objects and another vector for levels.
    *
    * @param obj  a Vector of objects...
    * @param levels  a Vector of ints...
    */
   public FileBrowserComboBoxModel( Vector obj, Vector levels )
   {
      super( createObjVector( obj, levels ));
   }

   /**
    * Constructs a FileBrowserComboBoxModel object initialized with
    * an array of objects and an array containing hierarchy levels.
    *
    * @param items  a vector containing all the items for the list
    *
    * @param levels a vector containing the same # of elements as items; each 
    * element must be an Integer containing the level of the corresponding element
    * in items
    *
    * @throws IllegalArgumentException if the # of elements in levels does not match
    * the # of elements in items or the object type of the elements in levels is
    * not of class Integer
    */
   private static Vector createObjVector( Vector items, Vector levels )
   {
      if ( items.size() != levels.size())
         throw new IllegalArgumentException( 
               E2Designer.getResources().getString( "SizeMismatch" ));

      try
      {
         int size = items.size();
         Vector v = new Vector( size );
         FileBrowserComboBoxModel dummy = new FileBrowserComboBoxModel( );
         for ( int index = 0; index < size; index++ )
         {
            v.add( dummy.new ComboHierarchy( items.get( index ), 
                  ((Integer) levels.get( index )).intValue( ) ) );
         }
         return v;
      } 
      catch ( ClassCastException e )
      {
         throw new IllegalArgumentException( 
               E2Designer.getResources().getString( "WrongObjectType" ));
      }
   }


   /**
    * Selects the item at index <code>anIndex</code>.
    *
    * @param anIndex an int specifying the list item to select, where 0 specifies
    *                the first item in the list
    */
   public void setSelectedIndex( int anIndex )
   {
      int size = getSize( );

      if ( anIndex == -1 )
      {
         setSelectedItem( null );
      }
      else if ( anIndex < -1 || anIndex >= size )
      {
         throw new IllegalArgumentException( "setSelectedIndex: " + anIndex + " out of bounds" );
      }
      else
      {
         setSelectedItem( getElementAt( anIndex ) );
      }
   }

   /**
    * Returns the index of the currently selected item in the list. The result is not
    * always defined if the JComboBox box allows selected items that are not in the
    * list.
    * Returns -1 if there is no selected item or if the user specified an item
    * which is not in the list.
    *
    * @returns an int specifying the currently selected list item, where 0 specifies
    *                the first item in the list, or -1 if no item is selected or if
    *                the currently selected item is not in the list
    */
   public int getSelectedIndex( )
   {
      return getIndexOf( getSelectedItem( ) );
   }

   /**
    * @returns The indentation level of selected object.
    */
   public int getSelectedLevel( )
   {
      return getLevelAt( getIndexOf( getSelectedItem( ) ) );
   }

   /** Gets the specified objects directory indentation level.
    *
    * @param index  the element of the m_level vector to be retrieved.
    * @returns the indentation level at the specified index.  -1 will be returned
    * if element at index does not exist.
    */
   public int getLevelAt( int index )
   {

      ComboHierarchy hier = (ComboHierarchy) super.getElementAt( index );
      int level = -1;
      if ( null != hier )
      {
         level = hier.getLevel( );
      }
      return level;
   }


   /**
    * @returns the data object at the supplied index
    */
   public Object getElementAt( int index )
   {
      ComboHierarchy hier = (ComboHierarchy) super.getElementAt( index );
      if ( null != hier )
         return hier.getData( );
      else
         return null;
   }

   /**
    * @returns the index of the element that contains value as its data, or -1
    * if no element contains the value. Value is checked using a logical compare,
    * (i.e. value.equals()).
    */
   public int getIndexOf( Object value )
   {
      if ( null == value )
         return -1;
      int size = getSize( );
      boolean bFound = false;
      int index;
      for ( index = 0; index < size && !bFound; index++ )
      {
         ComboHierarchy hier = (ComboHierarchy) super.getElementAt( index );
         if ( value.equals( hier.getData( ) ))
            bFound = true;
      }
      return bFound ? index-1 : -1;
   }

   /** 
    * Adds new element to the end of the list.
    */
   public void addElement( Object obj, int level )
   {
      super.addElement( new ComboHierarchy( obj, level ) );
   }

   /** Inserts new indentation level at index. */
   public void insertElementAt( Object obj, int level, int index )
   {
      insertElementAt( new ComboHierarchy( obj, level ), index );
   }

   /** 
    * Removes all elements that contain the supplied value as its data. The
    * comparison is a logical compare.
    */
   public void removeElement( Object value )
   {
      for ( int index = getSize( )-1; index >= 0; index-- )
      {
         ComboHierarchy hier = (ComboHierarchy) super.getElementAt( index );
         if ( value.equals( hier.getData()))
            removeElementAt( index );
      }
   }

   /**
    * Creates a string containing all elements in the vector, one element per
    * line.
    */
   public String toString( )
   {
      String strContents = new String( );
      for ( int index = 0; index < getSize( )-1; index++ )
      {
         strContents = strContents + "\n  " + getElementAt( index ).toString( ) + "@" + Integer.toString( getElementAt( index ).hashCode( ) );
      }
      return strContents;
   }

   /**
    * This class adds a level to an object stored in a combo box model.
    */
   private class ComboHierarchy
   {
      public ComboHierarchy( Object data, int level )
      {
         m_data = data;
         m_level = level;
      }

      public Object getData( )
      {
         return m_data;
      }

      public int getLevel( )
      {
         return m_level;
      }

      public String toString( )
      {
         return m_data.toString( );
      }

      private int m_level;
      private Object m_data;
   }
}




