/*[ SQLTabCataloger.java ]*****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.E2Designer;
import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.E2Designer.Util;
import com.percussion.conn.PSServerException;
import com.percussion.design.catalog.PSCatalogResultsWalker;
import com.percussion.design.catalog.PSCataloger;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.swing.*;
import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Properties;

/**
 * This is a common base class for all catalogers that are used in the ODBC
 * tab. It makes calls to the the derived class to get the pieces needed to
 * build the returned object.
 * <p>
 * Derived classes should set the properties of the cataloger in their constructors.
 */
public abstract class SQLTabCataloger extends Cataloger
{
   // constructors
   /**
    * Creates a cataloger by asking the browser for the server connect
    * information. The derived class is then responsible for filling in
    * the catalog specific properties. The connection is not actually
    * initiated at this point, only the connect info is stored in the
    * cataloger object. The connect will occur when the first iteration
    * occurs. <p>
    * Creates a new properties object for this instance and fills in the driver,
    * server, request category and uid/pw (if needed) properties. Derived classes
    * need to fill in their own specific properties needed for cataloging in
    * addition to these required properties.
    *
    * @param strDatasource The internal name of the datasource to be cataloged,
    * may be <code>null</code> or empty to query the default datasource.
    *
    * @throws IllegalArgumentException If strDriver or strServer is null or empty.
    */
   public SQLTabCataloger( int Type, String strDatasource)
   {
      super( Type );

      m_Properties = new Properties( );
      m_Properties.put( "RequestCategory", "data" );
      if (!StringUtils.isBlank(strDatasource))
         m_Properties.put( "Datasource", strDatasource);
   }


   public abstract String getBaseKey( );


   /**
    * The default entry mechanism allows the SQL hierarchy to work even if
    * certain levels are not present on a particular DBMS. When the cataloger
    * performs the catalog, if no elements are returned and no errors occurred,
    * the the default entry will be returned, if there is one.
    *
    * @return A catalog entry to return if a certain level in the hierarchy
    * has no elements. By default, null is returned.
   **/
   public CatalogEntryEx getDefaultEntry()
   {
      return null;
   }


   /**
   * Returns an iterator that walks the list of cataloged objects returned
   * by this cataloger. The first time hasNext() is called, the actual cataloging
   * takes place, so it may be slow.
   */
   public Iterator iterator( )
   {
      //m_bError = false;
      if ( !m_bError )
        m_cIterator = new CatalogIterator();

      return m_cIterator;
   }


   /**
   * Implements the Iterator interface for the PSCatalogResultsWalker class.
   */
   protected class CatalogIterator implements Iterator, Serializable
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
         m_bError = false;

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
         if(m_bError)
            return false;

         boolean bHasNext = false;
         try
         {
            PSCatalogResultsWalker walker = getWalker();
            if(walker == null)
               throw new IllegalArgumentException();

            // Note: Since the PSCatalogResultsWalker does not have a method to
            // just determine if next element exists without advancing to the next element
            // we Save the  current node and then if we have advanced, we set it back to
            // where we were.  .....This may have to be reimplemented if we find it is slow.
            Node curNode = walker.getCurrent();

            if ( null != m_defaultEntry )
               bHasNext = true;
            else if (walker.nextResultObject(getBaseKey()) == true)
            {
               bHasNext = true;
               walker.setCurrent(curNode);
            }
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
         catch (PSAuthorizationException e)
         {
            final Object[] params =
            {
               e.toString(),
            };
            m_bError = true;
            PSDlgUtil.showErrorDialog(
                  Util.cropErrorMessage(MessageFormat.format(
                        E2Designer.getResources().getString("AuthException"), params)),
                  E2Designer.getResources().getString("AuthErr"));
         }
         catch (PSAuthenticationFailedException e)
         {
            e.printStackTrace();
         }
         catch (PSServerException e)
         {
            final Object[] params =
            {
               e.toString(),
            };
            m_bError = true;
            PSDlgUtil.showErrorDialog(
                  Util.cropErrorMessage(
                        MessageFormat.format(E2Designer.getResources().getString("ServerConnException"), params)),
                  E2Designer.getResources().getString("ServerConnErr"));
         }
         catch (PSIllegalArgumentException e)
         {
            e.printStackTrace();
         }
         m_bError = false;
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
       * <p> If no elements are returned when the initial catalog is performed,
       * and no errors occurred, the derived class can return a default entry
       * that will be returned. This allows for different DBMS' that don't
       * support various levels in the hierarchy, such as database or schema.
       *
       * @returns a CatalogEntry object
       */
      public Object next( )
      {
         CatalogEntryEx entry = null;

         if ( null != m_defaultEntry )
         {
            entry = m_defaultEntry;
            m_defaultEntry = null;
         }
         else if(m_Walker.nextResultObject(getBaseKey()) == true)  // this also advances to the next
         {
            entry = new CatalogEntryEx();
            entry.setInternalName(getInternalName(m_Walker));
            entry.setDisplayName(getDisplayName(m_Walker));
            entry.setData(getData(m_Walker));
            entry.setIcon(getIcon());
            // note member m_Type is set by calling the constructor for superclass by the
            // appropriate Type of the derived class. Use this to set the Type of
            // the CatalogEntryEx object
            entry.setType(m_Type);
         }
         return entry;
      }

      public void remove( )
      {
         throw new UnsupportedOperationException( );
      }

      private PSCatalogResultsWalker getWalker() throws IOException,
                                          PSAuthorizationException,
                                          PSAuthenticationFailedException,
                                          PSServerException,
                                          PSIllegalArgumentException
      {
         if(m_Walker != null)
            return m_Walker;
//      System.out.println("In SQLTabCataloger ... creating Walker");
         initIterator();

         return m_Walker;
      }

      /**
       * Performs various actions required before any catalog entries can
       * be returned.
      **/
      private void initIterator() throws IOException,
                                 PSAuthorizationException,
                                 PSAuthenticationFailedException,
                                 PSServerException,
                                 PSIllegalArgumentException
      {
         if (m_bError)
           return;

         BrowserFrame bf = null;
         bf = bf.getBrowser();
         PSCataloger cataloger = bf.getCataloger();
         Document xmlDoc = cataloger.catalog( m_Properties );
         m_Walker = new PSCatalogResultsWalker( xmlDoc );

         if ( !hasNext())
         {
            /* There are no elements returned. Check if the derived class
               has a default entry */
            m_defaultEntry = getDefaultEntry();
         }

      }

      // variables
      protected PSCatalogResultsWalker m_Walker = null;
      private CatalogEntryEx m_defaultEntry = null;
   }

   /**
    * Returns the text string that should be displayed to the end user. If null
    * is returned, this means the internal name is the same as the display
    * name.
    */
   protected abstract String getDisplayName( PSCatalogResultsWalker walker );

   /**
    * Returns the text string that should be used as the internal name used when
    * communicating with the E2 server. The string may be empty or null.
    *
    * @returns text for internal name. If empty, the name is empty.
    */
   protected abstract String getInternalName( PSCatalogResultsWalker walker );

   /**
    * Returns a context sensitive data object. See the derived class for the
    * actual object type and usage.
    *
    * @returns a data object specific to the type of the derived class. May be
    * null.
    */
   protected abstract Object getData(  PSCatalogResultsWalker walker );

   protected abstract ImageIcon getIcon( );

   // variables
//   protected PSCataloger m_Cataloger = null;
   protected Properties m_Properties = null;
   protected static boolean m_bError = false;
   private static CatalogIterator m_cIterator = null;
}


