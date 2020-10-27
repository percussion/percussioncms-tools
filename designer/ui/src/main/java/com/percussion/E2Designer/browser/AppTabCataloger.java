/*[ AppTabCataloger.java ]*****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.browser;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * This is a common base class for all catalogers that are used in the Application
 * tab. It is added for future extension, it currently provides no support
 * except as a place for the E2 server cataloger object.
 */
public abstract class AppTabCataloger extends Cataloger
{
   // constructors
   /**
   * Creates the object store by getting the connect info from the browser.
   */
   public AppTabCataloger( int Type )
   {
      super( Type );
   }

   /**
    * Returns the text string that should be used as the internal name used when
    * communicating with the E2 server. The string may be empty or null.
    *
    * @returns text for internal name. If empty, the name is empty.
    */
   protected abstract String getInternalName(Object obj );


 /**
   * Returns an iterator that walks the list of cataloged objects returned
   * by this cataloger. The first time next() is called, the actual cataloging
   * takes place, so it may be slow.
   */

   public Iterator iterator( )
   {
      return new CatalogIterator( );
   }

   /**
    * Implements the Iterator interface for the cataloger class.
    */
   protected class CatalogIterator implements Iterator
   {
      // constructors
      /**
       * Initiates a catalog, and creates a walker for the outer class'
       * cataloger. Look at the derived class implementation.
       *
       */
      public CatalogIterator( )
      {
//         System.out.println("In AppTabCataloger's CatalogIterator constructor");
      }


      /**
       * Returns true if next node exists
       *
       */
      public boolean hasNext( )
      {
         return false;
      }

      /**
       * Uses protected methods of the outer class to access specific
       * information from the derived class and then use this data to
       * build the returned object. The first time the
       * method is called, a connection is made to the E2 server to acquire the
       * catalog information. If any failures occur, an error message is placed
       * in the CatalogEntry object and returned. No additional elements will
       * be returned after this.
       *
       * @returns a CatalogEntry object
       */
      public Object next( )
      {
      CatalogEntry entry = null;

      return entry;
      }

      public void remove( )
      {
         throw new UnsupportedOperationException( );
      }

      // variables
    protected Enumeration m_enum = null;
   }

   // variables

}


