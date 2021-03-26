/******************************************************************************
 *
 * [ OSBackendTable.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSBackEndTable;

import java.awt.*;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

public class OSBackendTable extends PSBackEndTable
      implements IGuiLink, IPersist
{

   /*
    */
   public OSBackendTable( )
   {
      this( E2Designer.getResources().getString( "Dummy" ));
   }

   /**
    * Creates a new table with the specified alias.
    *
    * @param alias A unique name w/in the context of a datatank. If this table is
    * added to a datatank and a table with the same alias already exists, an
    * exception will be thrown.
   **/
   public OSBackendTable( String alias )
   {
      super( alias );
   }

   /*
    * Creates a new table, taking its properties from the supplied table.
    *
    * @throws NullPointerException if table is null
    */
   public OSBackendTable( PSBackEndTable table )
   {
      super(table.getAlias());
      copyFrom(table);
   }

   /*
    * Creates a new table, taking its properties from the supplied table.
    *
    * @throws NullPointerException if table is null
    */
   public OSBackendTable(OSBackendTable table)
   {
      super(table.getAlias());
      copyFrom(table);
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    * @param table a valid OSBackendTable. 
    *
    */
   public void copyFrom( OSBackendTable table )
   {
      copyFrom((PSBackEndTable) table );

      // do not copy the owner key because this belongs to the figure that created
      // it. use setFigure/getFigure instead
      //m_owner = table.getFigure();

      m_readOnly = table.m_readOnly;
      m_ptLocation = table.m_ptLocation;
      m_dimSize = table.m_dimSize;
      m_columnObjs = table.m_columnObjs;

   }
  /**
   * Sees if the passed table matches our table
   * 
   * @param table to check, may not be <code>null</code>.
   * 
   * @return <code> true </code> if my table and the table are the same
   * <code> false </code> if not
   */
   public boolean isEqual(OSBackendTable table)
   {
      if (table == null)
         throw new IllegalArgumentException("table may not be null");

      boolean bRet = false;

      String srcTable = table.getTable();

      String trgTable = getTable();
      bRet = compare(srcTable, trgTable) && isSameDatasource(table);

      return (bRet);
   }


   /**
    * Clears all data objects that have an cached data. Should be called when
    * any table data changes.
    */
   private void invalidateCache()
   {
      m_columnObjs = null;
   }

   public void setDataSource( String datasource )
   {
      super.setDataSource( datasource );
      invalidateCache();
   }
   

   public void setTable( String table )
   {
      super.setTable( table );
      invalidateCache();
   }

  /*
   * Set the object to the provided read only state.
   *
   * @param readOnly true for read only
   */
  //////////////////////////////////////////////////////////////////////////////
   public void setReadOnly(boolean readOnly)
   {
    m_readOnly = readOnly;
   }

  /*
   * Check the read only state the first time this is used only and return the
   * result.
   *
   * @return boolean   the read only status
   */
  //////////////////////////////////////////////////////////////////////////////
   public boolean isReadOnly()
  {
    return m_readOnly;
  }

   /*************
      IGuiLink interface implementation
   *************/
   public void setFigure( UIFigure fig )
   {
      m_owner = fig;
   }

   public void release()
   {
      m_owner = null;
   }

   public UIFigure getFigure()
   {
      return m_owner;
   }

   //////////////////////////////////////////////////////////////////////////////
  // IPersist interface implementation
   public boolean load(PSApplication app, Object store, Properties config)
   {
      if (null == store || null == config)
         throw new IllegalArgumentException();

      if (!(store instanceof OSBackendDatatank))
      {
         String [] astrParams =
         {
            "OSBackendDatatank"
         };
         throw new IllegalArgumentException(MessageFormat.format(
            E2Designer.getResources().getString("IncorrectType"), astrParams));
      }

    try
    {
         OSBackendDatatank pipe = (OSBackendDatatank) store;


         /*   TODOph: this should not be in the data object, it should be in the
            gui object, that's what m_owner is for */
         // restore the GUI stuff
         String strID = new Integer(getId()).toString();
         String strLocationx = strID + m_locationx;
         String strLocationy = strID + m_locationy;
         String strSizeWidth = strID + m_sizewidth;
         String strSizeHeight = strID + m_sizeheight;
         String strReadonly = strID + m_strReadonly;

         if(config.getProperty(strLocationx) != null)
            m_ptLocation.x = new Integer(config.getProperty(strLocationx)).intValue();

         if(config.getProperty(strLocationy) != null)
            m_ptLocation.y = new Integer(config.getProperty(strLocationy)).intValue();

         if(config.getProperty(strSizeWidth) != null)
            m_dimSize.width = new Integer(config.getProperty(strSizeWidth)).intValue();

         if(config.getProperty(strSizeHeight) != null)
            m_dimSize.height = new Integer(config.getProperty(strSizeHeight)).intValue();

         if(config.getProperty(strReadonly) != null)
            m_readOnly = Boolean.parseBoolean(config.getProperty(strReadonly));

      return true;
    }
    catch (Exception e)
    {
       e.printStackTrace();
    }

      return false;
   }

   /**
    * Interface method unimplemented.
    * @see com.percussion.E2Designer.IPersist#cleanup(com.percussion.
    * E2Designer.OSApplication)
    */
   public void cleanup(OSApplication app)
   {
   }

   //////////////////////////////////////////////////////////////////////////////
  // IPersist interface implementation
   public void save(PSApplication app, Object store, Properties config)
   {
      if (null == store || null == config)
         throw new IllegalArgumentException();

      if (!(store instanceof OSBackendDatatank))
      {
         String [] astrParams =
         {
            "OSBackendDatatank"
         };
         throw new IllegalArgumentException(MessageFormat.format(
            E2Designer.getResources().getString("IncorrectType"), astrParams));
      }

      try
   {
         // store current id temporary and create a new unique id for this object
         int currentId = this.getId();
      this.setId(Util.getUniqueId());

         // save this backendtank into the provided pipe
         OSBackendDatatank tank = (OSBackendDatatank) store;

         //save locations in properties
         String strID = new Integer(getId()).toString();
         String strLocationx = strID + m_locationx;
         String strLocationy = strID + m_locationy;
         String strSizeWidth = strID + m_sizewidth;
         String strSizeHeight = strID + m_sizeheight;
         String strReadonly = strID + m_strReadonly;

         if(m_ptLocation != null)
         {
            config.setProperty(strLocationx, new Integer(m_ptLocation.x).toString());
            config.setProperty(strLocationy, new Integer(m_ptLocation.y).toString());
         }

         if(m_dimSize != null)
         {
            config.setProperty(strSizeWidth, new Integer((int)m_dimSize.getWidth()).toString());
            config.setProperty(strSizeHeight, new Integer((int)m_dimSize.getHeight()).toString());
         }

         String readOnly = Boolean.toString(m_readOnly);
         config.setProperty(strReadonly, readOnly);
   }
   catch (Exception e)
   {
      e.printStackTrace();
   }
   }

   public Point getLocation()
   {
      return m_ptLocation;
   }

   public void setLocation(Point pt)
   {
      m_ptLocation = pt;
   }

   public Dimension getSize()
   {
      return m_dimSize;
   }

   public void setSize(Dimension size)
   {
      m_dimSize = size;
   }
  /**
  *search our table for a column that match strName
  *
  *@param strName column name to match
  *
  *@return <code> true </code> if found <code> false </code> if not found
  *
  */
  public boolean hasFieldName(String strName)
  {
       boolean bRet=false;
        Vector vc=getColumns();
       if( vc != null )
       {
          int limit=vc.size();
          if( limit > 0 )
          {
              String strFieldName;
              // scan the list
              for( int count=0; count<limit;count++)
              {
                  // see if they match
                  strFieldName=(String)vc.elementAt(count);
                  if( strFieldName.equals(strName) )
                  {
                       bRet=true;    // set our flag and break
                       break;
                  }
              }
          }
       }
     return(bRet); // return the flag
  }

  /**
  *creates an CatalogExtendedBackendcolumns, and returns the field type
  *
  *@param strName which column to return the extended data
  *
  *@return ExtendedBackendColumnData containg the extended column data or
  *        null if not found
  *
  */

  public ExtendedBackendColumnData hasColumn(String strName)
  {
      ExtendedBackendColumnData pRet=null; // the return type
      if( m_ExtendedCataloger == null )
      {
          m_ExtendedCataloger=new Vector(10);
          m_ExtendedCataloger=ExtendedCat.getCatalog(this,true);
      }

      int limit=m_ExtendedCataloger.size();
      if( limit >=1 )
      {
        // walk trough the elements
         for( int count=0; count< limit; count++)
         {
             // see if is our
             ExtendedBackendColumnData pCol=(ExtendedBackendColumnData)m_ExtendedCataloger.get(count);
             if( pCol.getColumName().equals(strName))
             {
                  pRet=pCol; // it is set the return
                  break;
             }
         }
      }
     return(pRet);
}

   /**
    * If the table has never been cataloged, or it has been changed, cataloging
    * of the columns is performed. If it has
    * been previously cataloged and no changes have occurred, a cached copy of
    * the object is returned.
    * @return an enumeration containing PSBackEndColumn objects for all columns
    * in this table, or an empty one if there are no columns.
    */
   public Enumeration getBackendColumns()
   {
      try
      {
         if(m_columnObjs != null && m_columnObjs.size() != 0)
             return m_columnObjs.elements();

         m_columnObjs = new Vector(20);
         //catalog the columns
         Vector columns = CatalogBackendColumns.getCatalog(this, true);

         if(columns != null)
         {
            for ( int i = 0; i < columns.size(); ++i )
            {
               /*for each column create PSBackEndColumn and each
                 PSBackEndColumn add to the list. */
               String colName = (String) columns.get(i);
               m_columnObjs.add(new PSBackEndColumn( this, colName ));
            }
         }
         //sort the columns
         if (m_columnObjs.size() > 1)
         {
            Collator c = Collator.getInstance();
            c.setStrength( Collator.PRIMARY );
            ObjectCollator oc = new ObjectCollator( c );
            Collections.sort( m_columnObjs, oc );
         }
      }

      catch(IllegalArgumentException e )
      {
         e.printStackTrace();
      }

      return m_columnObjs.elements();
   }

   /**
    * Gets all the columns for this table as simple column names and returns
    * a vector containing a sorted list.
    * The first time this method is called, a catalog is performed, successive
    * calls to this method will return cached values, unless the object has
    * changed.
    */
   public Vector getColumns()
   {
      Vector columns = new Vector(10);
      Enumeration cols = getBackendColumns();

      while ( cols.hasMoreElements())
         columns.add(((PSBackEndColumn) cols.nextElement()).getColumn());
      return columns;
   }

  //////////////////////////////////////////////////////////////////////////////
   // private storage
   transient private UIFigure m_owner = null;
   // reasonable defaults for size of table window in the join editor
   private Point m_ptLocation = new Point( 20, 20);
   private Dimension m_dimSize = new Dimension( 120, 200 );
   private Vector m_columnObjs = null;

  //////////////////////////////////////////////////////////////////////////////
  /*
   * the read only flag
   */
   private boolean m_readOnly = false;

   private String m_locationx = new String("locationx");
   private String m_locationy = new String("locationy");
   private String m_sizewidth = new String("sizewidth");
   private String m_sizeheight = new String("sizeheight");
   private String m_strReadonly = new String("readonly");
   private Vector m_ExtendedCataloger=null;
   private CatalogExtendedBackendcolumns ExtendedCat=null;

  }


