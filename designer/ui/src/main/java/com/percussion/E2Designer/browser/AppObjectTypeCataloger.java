/*[ AppObjectTypeCataloger.java ]**********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/


package com.percussion.E2Designer.browser;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;


/**
 * This class catalogs the application objects for the specified application
 * name.
 *
 */
public class AppObjectTypeCataloger extends AppTabCataloger
{
    //create static resource bundle object
    static ResourceBundle res = null;
    static
    {
      try
      {
        res = ResourceBundle.getBundle( "com.percussion.E2Designer.browser.AppObjectTypeCatalogerResources", Locale.getDefault( ) );
      }catch(MissingResourceException mre)
      {
        System.out.println( mre );
      }
    }

    // constructors
    /**
    * @throws IllegalArgumentException if the application name is null or empty.
    */
    public AppObjectTypeCataloger()
    {

        super( ApplicationHierarchyConstraints.NT_APPOBJ_TYPE );

    }

    public String getInternalName(Object obj )
    {

        String strName = null;

        strName =(String)obj;

        return strName;
    }


    public Iterator iterator( )
    {
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

//         System.out.println("in AppObjectTypeCataloger's CatalogIterator construcor");
         m_vObjTypes = new Vector(3);
         String strObjType = new String(DATASET);
         m_vObjTypes.add(strObjType);
         strObjType = new String(ROLE);
         m_vObjTypes.add(strObjType);

         m_enum =m_vObjTypes.elements();
      }


      /**
       * Returns true if next node exists
       *
       * @throws IllegalArgumentException if the m_enum object is null.
       */
      public boolean hasNext( )
      {
      if(m_enum == null)
        throw new IllegalArgumentException();
//         System.out.println("hasNext() ="+m_enum.hasMoreElements());
          return m_enum.hasMoreElements();
      }

      /**
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
        Object obj = m_enum.nextElement();
        entry.setInternalName(getInternalName(obj));
        // note member m_Type is set by calling the constructor for superclass by the
        // appropriate Type of the derived class. Use this to set the Type of
        // the CatalogEntry object
        entry.setType(m_Type);

      }
      return entry;
      }

      public void remove( )
      {
         throw new UnsupportedOperationException( );
      }

      // variables
    private Enumeration m_enum = null;
      private Vector m_vObjTypes = null;
   }

public static final String DATASET      = res.getString("DATASET");
public static final String ROLE            = res.getString("ROLE");
   
}


