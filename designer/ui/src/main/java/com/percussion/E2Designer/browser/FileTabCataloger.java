/*[ FileTabCataloger.java ]****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.design.catalog.PSCatalogResultsWalker;
import com.percussion.design.catalog.PSCataloger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.Iterator;
import java.util.Properties;

/**
 * This is a common base class for all catalogers that are used in the FILE
 * tab. It is added for future extension, it currently provides no support
 * except as a place for the E2 server cataloger object.
 */
public abstract class FileTabCataloger extends Cataloger
{
   public FileTabCataloger( int Type )
   {
      super( Type );
   }

  public abstract String getBaseKey( );

  /**
    * Returns the text string that should be used as the internal name used when
    * communicating with the E2 server. The string may be empty or null.
    *
    * @returns text for internal name. If empty, the name is empty.
    */

   protected abstract String getInternalName( PSCatalogResultsWalker walker );

  /**
    * Returns the type of the cataloger. The type is determined by the derived class.
    *
    * @returns an integer corresponding to the Type of the cataloger.
    */
   protected abstract int getType( PSCatalogResultsWalker walker );


  /**
   * Returns an iterator that walks the list of cataloged objects returned
    * by this cataloger. The first time next() is called, the actual cataloging
    * takes place, so it may be slow.
    */
   public Iterator iterator( )
   {
      m_bError = false;
      return new CatalogIterator( );
   }


   /**
    * Implements the Iterator interface for the cataloger class.
    */
   private class CatalogIterator implements Iterator
   {
      // constructors
      /**
       * Initiates a catalog, and creates a walker for the outer class'
       * cataloger.
       *
       * @throws IllegalArgumentException if the outer class' properties object is
       * null.
       */
      public CatalogIterator( )
       {
         if (m_bError)
            return;
         if (null == m_Properties)
            throw new IllegalArgumentException( );

      }


      /**
       * Returns true if next node exists
       *
       * @throws IllegalArgumentException if the m_Walker object is null.
       */
      public boolean hasNext( )
      {
         System.out.println("In Has Next");
         if(m_bError)
            return false;
         
         System.out.println("After error");
         boolean bHasNext = false;
         PSCatalogResultsWalker walker = getWalker();
         if(walker == null)
            throw new IllegalArgumentException();

         System.out.println("after walker null");
         // Note: Since the PSCatalogResultsWalker does not have a method to
         // just determine if next element exists without advancing to the next element
         // we Save the  current node and then if we have advanced, we set it back to
         // where we were.  .....This may have to be reimplemented if we find it is slow.
         Node curNode = walker.getCurrent();
         System.out.println("after getCurrent");
         if(walker.nextResultObject(getBaseKey()) == true)
         {
            System.out.println("DoesHaveNext");
            bHasNext = true;
            walker.setCurrent(curNode);
         }

         return bHasNext;
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
         if(m_Walker.nextResultObject(getBaseKey()) == true)  // this also advances to the next
         {
           entry = new CatalogEntry();
           entry.setInternalName(getInternalName(m_Walker));
           entry.setType(getType(m_Walker));
         }
         return entry;
      }

      public void remove( )
       {
         throw new UnsupportedOperationException( );
      }

      private PSCatalogResultsWalker getWalker()
      {
         if(m_Walker != null)
            return m_Walker;
//      System.out.println("In FileTabCataloger ... creating Walker");

         BrowserFrame bf = null;
      bf = bf.getBrowser();
      PSCataloger cataloger = bf.getCataloger();
       try
        {
           Document   xmlDoc   = cataloger.catalog( m_Properties );
           m_Walker   = new PSCatalogResultsWalker( xmlDoc );
      }
        catch (Exception e)
        {
            m_bError = true;
            PSDlgUtil.showError(e);
        }
         return m_Walker;
      }

      // variables
      private PSCatalogResultsWalker m_Walker = null;
   }

   // variables
//   protected PSCataloger m_Cataloger = null;
  protected Properties m_Properties = null;
   private boolean m_bError = false;

}


