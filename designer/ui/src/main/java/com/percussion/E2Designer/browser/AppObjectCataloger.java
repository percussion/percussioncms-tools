/******************************************************************************
 *
 * [ AppObjectCataloger.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSBackEndCredential;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.design.objectstore.PSRole;
import com.percussion.util.PSCollection;
import org.apache.commons.lang.StringUtils;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

/**
 * This class catalogs the application objects for the specified application
 * name.
 *
 */
public class AppObjectCataloger extends AppTabCataloger
{
    // constructors
   /**
    * @throws IllegalArgumentException if the application name is null or empty.
    */
    public AppObjectCataloger(String strAppName)
    {
        super( ApplicationHierarchyConstraints.NT_APPOBJECTS );

        if (StringUtils.isEmpty(strAppName))
        {
           throw new IllegalArgumentException();
        }
        m_strAppName = strAppName;

        // we need to catalog all appobjects here

    }

    public String getInternalName(Object obj )
    {

        if(obj == null)
           return null;
        String strName = null;

        if(obj instanceof PSDataSet)
        {
           PSDataSet dataset = (PSDataSet)obj;
           strName = dataset.getName();
           // System.out.println("instance of DataSet,  name = "+strName);
        }
        else if(obj instanceof PSBackEndCredential)
        {
           PSBackEndCredential beCred = (PSBackEndCredential)obj;
           strName = beCred.getAlias();
           //System.out.println("instance of PSBackEndCredential,  alias = "+strName);
        }
        else if(obj instanceof PSRole)
        {
           PSRole role = (PSRole)obj;
           strName = role.getName();
           //System.out.println("instance of PSRole,  name = "+strName);
        }

   return strName;
    }


   public int getType(Object obj )
   {

      if(obj == null)
         return -1;
      if(obj instanceof PSDataSet)
      {
         return ApplicationHierarchyConstraints.NT_APPOBJECT_DATASET;
      }
      else if(obj instanceof PSBackEndCredential)
      {
         return ApplicationHierarchyConstraints.NT_APPOBJECT_CREDALIAS;
      }
      else if(obj instanceof PSRole)
      {
         return ApplicationHierarchyConstraints.NT_APPOBJECT_ROLE;
      }
      //if none of the above match return -1
      return -1;
   }

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
      BrowserFrame bf = null;
      bf = bf.getBrowser();
      PSObjectStore store = bf.getObjectStore();
         m_vAppObjects = new Vector();

         PSCollection cDataSets = null;
         PSCollection cRoles = null; 
         
       try
        {
//            System.out.println("getting application " +m_strAppName);
            m_Application = store.getApplication(m_strAppName, false);
      }
        catch (Exception e)
        {
            m_bError = true;
            PSDlgUtil.showError(e);
            return;
        }

         if(m_Application == null)
            throw new RuntimeException("Retrieved application is null");

         cDataSets = m_Application.getDataSets();
         addObjects(cDataSets);
         cRoles = m_Application.getRoles();
         addObjects(cRoles);

         m_enum = m_vAppObjects.elements();
      }


      /**
       * Returns true if next node exists
       *
       * @throws IllegalArgumentException if the m_enum object is null.
       */
      public boolean hasNext( )
      {
      if(m_bError)
            return false;
         if(m_enum == null)
        throw new IllegalArgumentException("Enum object is null");
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
        Object obj = m_enum.nextElement();
        entry.setInternalName(getInternalName(obj));
        // note member m_Type is set by calling the constructor for superclass by the
        // appropriate Type of the derived class. Use this to set the Type of
        // the CatalogEntry object
        entry.setType(getType(obj));

      }
      return entry;
      }

      public void remove( )
      {
         throw new UnsupportedOperationException( );
      }

      private void addObjects(PSCollection coll)
      {
         //todo: sort before adding
         if(coll == null || coll.isEmpty() || coll.size() <= 0)
         {
//            System.out.println("Collection is null or empty");
            return;
         }
         for(int i=0; i<coll.size(); i++)
         {
//            System.out.println(coll.get(i)); // print out as we are adding   - for debugging
            m_vAppObjects.add(coll.get(i));
         }

      }

      // variables
    private Enumeration m_enum = null;
      private Vector m_vAppObjects = null;
   }

   private boolean m_bError = false;
   private PSObjectStore m_Store;
  private String m_strAppName;
   private PSApplication m_Application;

   
}


