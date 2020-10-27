/*[ AppCataloger.java ]********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.E2Designer;
import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.security.PSAuthenticationFailedException;

import javax.swing.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.ResourceBundle;


/**
 * This class catalogs the application names in the server.
 */
public class AppCataloger extends AppTabCataloger
{
   // constructors
   /**
    *
    */
   public AppCataloger( )
   {
      super( ApplicationHierarchyConstraints.NT_APPLICATION );

    // set the Properties object that will be used by the base class to
    // get enumerated entries
    m_Properties = new Properties( );

      m_Properties.put( "name",  ""      );
      m_Properties.put( "isActive", "");
   }

   /**
    * Creates an iterator for the catalog of application names.
    *
    * @throws   IllegalArgumentException if m_Properties is <CODE>null</CODE> 
    *           RuntimeException if a
    *           <CODE>PSServerException<\CODE> or a
    *           <CODE>PSAuthenticationFailedException<\CODE> is caught
    *           during initialization while getting the application
    *           summaries
    */   
   public Iterator iterator( )
   {
      m_bErrorOccurred = false;
      return new CatalogIterator( );
   }

   protected String getInternalName(Object obj)
   {
      Properties props = (Properties)obj;
      return props.getProperty("name");
   }

   private boolean isActive(Properties props)
   {
      String strActive = props.getProperty("isActive");
      if(strActive.equals("yes"))
         return true;
      return false;
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
       * @throws   IllegalArgumentException if the outer class' properties
       *           object is null.
       *           RuntimeException if a
       *           <CODE>PSServerException<\CODE> or a
       *           <CODE>PSAuthenticationFailedException<\CODE> is caught
       *           during Designer initialization while getting the
       *           application summaries
       */
      public CatalogIterator( )
      {
         if (m_bErrorOccurred)
            return;
         if (null == m_Properties)
            throw new IllegalArgumentException( );

         BrowserFrame bf = null;
         bf = bf.getBrowser();
         PSObjectStore store = bf.getObjectStore();
         try
         {
            m_enum   = store.getApplicationSummaries( m_Properties );
         }
         catch (PSServerException e)
         {
            m_bErrorOccurred = true;
            PSDlgUtil.showError(e, false,
                  E2Designer.getResources().getString("OpErrorTitle"));
         }
         catch (PSAuthenticationFailedException e)
         {
            if (E2Designer.getApp() != null)
            {
               PSDlgUtil.showError(
                  e,
                  false,
                  E2Designer.getResources().
                  getString("ExceptionTitle"));
            }
            else
            {
               throw new RuntimeException(
                  E2Designer.getResources().getString("ExceptionTitle") +
                  ": " + e.getLocalizedMessage());
            }
         }
      }


      /**
       * Returns true if next node exists
       *
       * @throws IllegalArgumentException if the m_enum object is null.
       */
      public boolean hasNext( )
      {
      if(m_bErrorOccurred)
            return false;
      if(m_enum == null)
        throw new IllegalArgumentException();
//         System.out.println("hasNext() ="+m_enum.hasMoreElements());
          return m_enum.hasMoreElements();
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

      if(m_enum.hasMoreElements())
      {
//        System.out.println("created one CatalogEntry object");
        entry = new CatalogEntry();
        Properties props = (Properties)m_enum.nextElement();
        entry.setInternalName(getInternalName(props));

        // note member m_Type is set by calling the constructor for superclass by the
        // appropriate Type of the derived class. Use this to set the Type of
        // the CatalogEntry object
        entry.setType(m_Type);

            ResourceBundle res = BrowserFrame.getBrowser().getResources();
            if(isActive(props))
               entry.setIcon(new ImageIcon(getClass().getResource(res.getString("gif_app_active"))));
            else
               entry.setIcon(new ImageIcon(getClass().getResource(res.getString("gif_app"))));

      }
      return entry;
      }

      public void remove( )
      {
         throw new UnsupportedOperationException( );
      }

      // variables
    private Enumeration m_enum = null;
   }
    private Properties m_Properties = null;
      private boolean m_bErrorOccurred = false;

}


