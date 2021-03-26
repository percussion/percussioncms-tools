/*[ OSBackendDatatank.java ]***************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSBackEndDataTank;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSPipe;
import com.percussion.util.PSCollection;

import java.net.MalformedURLException;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
////////////////////////////////////////////////////////////////////////////////
public class OSBackendDatatank extends PSBackEndDataTank implements IGuiLink,
                                                                    IPersist,
                                                                    ICustomDropSourceData
{
   /*
    * Creates a new datatank
    */
   public OSBackendDatatank( )
   {
      super();
   }

   /*
    * Creates a new datatank, taking its properties from the supplied tank.
    *
    * @throws NullPointerException if tank is null
    */
   public OSBackendDatatank( PSBackEndDataTank tank )
   {
      super();
      copyFrom(tank);
   }


   /**
    * Clears all data objects that have an cached data. Should be called when
    * any tables change.
    */
   private void invalidateCache()
   {
      m_columnObjs = null;
      extCatalog=null;
   }

   /**
    *
    * @param  tables  list of table to be added to datatank, it checks that the
    * tables being added are not pressent on the list.  Those tables will not be
    * added to to datatank.  Matching tables from the original list of tables
    * will be replaced with the matching ones from the list of tables passed in.
    *
    * also if bGenerateJoins
    * is set try to generate the joins
    *
    *@param  bGenerateJoins if <code> true </code> try to auto join
    *   <code>false </code> do not try to autojoin
    *
    */
   public void addTables(PSCollection tables,boolean bGenerateJoins)
   {
      PSCollection myTables=getTables();
      PSCollection newTables=null;
      PSCollection tablesPresent = null;
      try
      {
         newTables = new PSCollection( "com.percussion.design.objectstore.PSBackEndTable" );
         tablesPresent = new PSCollection ("com.percussion.design.objectstore.PSBackEndTable");
      }
      catch ( ClassNotFoundException e )
      {
         // design time error, should never happen
         e.printStackTrace();
      }
      OSBackendTable masterTable=null;

      OSBackendTable mytableData=null;
      OSBackendTable tableData=null;

      // try to see if this tables are not already present on the list
      for(int iTables=0; iTables<tables.size(); iTables++)
      {
         // get the added table
         tableData = (OSBackendTable)tables.get(iTables);
         boolean found=false;
         // walk trough all the tables
         for(int  iMyTables=0; iMyTables< myTables.size(); iMyTables++)
         {
            mytableData = (OSBackendTable)myTables.get(iMyTables);
            // if both tables exists
            if (tableData != null && mytableData != null )
            {
               // see if they match
               if( mytableData.isEqual(tableData)== true )
               {
                  /*fill a collection with passed in tables that match existing
                    tables. Later we will use it to replace the matching original
                    tables.
                  */
                  tablesPresent.add(tableData);
                  found=true;  // they do, break
                  break;
               }
            }

         }
         // was not found, add it
         if( found == false && tableData != null )
         {
            newTables.add(tableData);
         }
      }
      // if generate joins
      if( bGenerateJoins )
      {
         // and we added at least a table
         if( newTables.size() > 0  )
         {
            if( m_joins != null )
            {
               m_joins.clear();
            }
            autoGenerateJoins(newTables);
         }
      }
      else // add the passed tables into the list
      {
         int MasterSize=tables.size();
         if( MasterSize > 0 )
         {
            for( int MasterCount=0; MasterCount< MasterSize; MasterCount++)
            {
               masterTable=(OSBackendTable)tables.get(MasterCount);
               if( masterTable !=null )
                  myTables.add(masterTable);
            }
            try
            {
               // set the tables
               setTables(myTables);
            }
            catch ( IllegalArgumentException iae )
            {
               // should never happen, we are using theoretically valid objects
               //w/o modification
               iae.printStackTrace();
            }
         }
      }
      //Walk through collection of the existing tables and the collection of the
      //tables passed in that matched existing tables
      OSBackendTable myTable = null;
      OSBackendTable table = null;
      for(int i = 0; i < tablesPresent.size(); i++)
      {
         table = (OSBackendTable)tablesPresent.get(i);
         for(int k = 0; k < myTables.size(); k++)
         {
            myTable = (OSBackendTable)myTables.get(k);
            if(table != null && myTable != null)
            {
               //see if they match
               if(myTable.isEqual(table))
               {
                  //if so replace the existing table with the matching table
                  //that was passed in
                  myTables.remove(myTable);
                  myTables.add(table);
               }
            }
         }
      }
      try
      {
         setTables(myTables);
      }
      catch(IllegalArgumentException ae)
      {
         ae.printStackTrace();
      }

   }


  /**
  *try to guess the joins between the passed tables and the tables currently
  *on list
  *
  *@param tables, list of tables that were added
  *
  */

  public void autoGenerateJoins(PSCollection tables)
  {
       try
      {
       PSCollection myTables=getTables();

       if( myTables == null || tables == null)
         return;

       OSBackendTable masterTable=null;
       OSBackendTable addedTable=null;

       int MasterSize=myTables.size();
       int AddedSize=tables.size();
         // walk trough all the added tables

       for( int AddedCount=0; AddedCount< AddedSize; AddedCount++)
       {
          // get it
          addedTable=(OSBackendTable)tables.get(AddedCount);
          // walk trough all the current tables
          for( int MasterCount=0; MasterCount< MasterSize; MasterCount++)
          {
             // get the table
             masterTable=(OSBackendTable)myTables.get(MasterCount);
             if( masterTable != null  && addedTable != null )
             {
                // check for primary keys
                CheckJoin(masterTable,addedTable,CatalogExtendedTypes.PRIMARY_KEY);
                // check for a foreign key in the master tbl.
                CheckJoin(masterTable,addedTable,CatalogExtendedTypes.FOREIGN_KEY);
                CheckJoin(addedTable,masterTable,CatalogExtendedTypes.FOREIGN_KEY);
                // do the same for unique keys
                CheckJoin(masterTable,addedTable,CatalogExtendedTypes.UNIQUE_KEY);
                CheckJoin(addedTable,masterTable,CatalogExtendedTypes.UNIQUE_KEY);
             }
         }
       }
       // now add the tables into our list
       for( int AddedCount=0; AddedCount< AddedSize; AddedCount++)
       {
           masterTable=(OSBackendTable)tables.get(AddedCount);
           if( masterTable != null )
               myTables.add(masterTable);
       }

       // set the tables
       if( myTables != null )
       {
          setTables(myTables);
       }
       PSCollection myJoins=getJoins();
       if( myJoins == null && m_joins != null )
       {
           try
           {
               myJoins = new PSCollection("com.percussion.design.objectstore.PSBackEndJoin");
           }
           catch ( ClassNotFoundException e )
              {
                   // design time error, should never happen
                  e.printStackTrace();
              }
       }
       if( m_joins != null )
       {
         int limit=m_joins.size();
         if( limit >=1 )
         {
            for(int count=0;count<limit;count++)
            {
              myJoins.add(m_joins.get(count));
            }
         }
         // and joins
         if( myJoins != null )
         {
            setJoins(myJoins);
         }
       }
   }
   catch ( IllegalArgumentException iae )
    {
         // should never happen, we are using theoretically valid objects w/o modification
         iae.printStackTrace();
    }

  }
  /**
  * adds a new join into the join list, it makes sure that the same join do not
  * exists on the join list.
  *
  *@param srcTable the LEFT table
  *
  *@param trgTable the RIGHT table
  *
  *@param srcFieldName the field name to be used in the join
  *
  */

  void AddJoin(OSBackendTable srcTable,String srcFieldName,OSBackendTable trgTable)
  {
    if( srcTable == null || srcFieldName == null || trgTable== null )
         return;

    try
    {
      if( m_joins == null )
      {
        m_joins = new PSCollection("com.percussion.design.objectstore.PSBackEndJoin");
      }
    }
    catch ( ClassNotFoundException e )
      {
         // design time error, should never happen
         e.printStackTrace();
      }

      //need to set left and right columns
       PSBackEndColumn leftColumnData = null;
       PSBackEndColumn rightColumnData = null;
    try
    {
      // construct the join
      leftColumnData = new PSBackEndColumn(srcTable,srcFieldName);
        rightColumnData = new PSBackEndColumn(trgTable,srcFieldName);
      OSBackendJoin joinData=new OSBackendJoin();
        joinData.setLeftColumn(leftColumnData);
        joinData.setRightColumn(rightColumnData);
      // see if join is not present
      if( isJoinNotOnList(joinData) )
      {
          m_joins.add(joinData);  // add it
      }
    }
    catch ( IllegalArgumentException iae )
      {
         // should never happen, we are using theoretically valid objects w/o modification
         iae.printStackTrace();
      }

   }
  /**
  * used to check if join is not on the join list,
  *
  *@param join to be checked
  *
  *@return <code> true </code> is not on list, <code> false </code> if is
  *
  */
  boolean isJoinNotOnList(OSBackendJoin join)
  {
    boolean bRet=true;
    if( join == null )
         return(bRet);

    PSBackEndColumn Column = null;
    PSBackEndColumn otherColumn = null;

    PSBackEndColumn srcLeftColumn=join.getLeftColumn();
    PSBackEndColumn srcRightColumn=join.getRightColumn();

    if(m_joins != null && srcLeftColumn != null && srcRightColumn != null )
    {
      // if the join list is not empty

       int limit=m_joins.size();
       if( limit >= 1 )
       {
        // scan the joins list
        for( int count=0; count <limit; count++)
        {
            OSBackendJoin joinData=(OSBackendJoin)m_joins.get(count);
            if( joinData != null )
            {
               Column=joinData.getLeftColumn(); // get the left colum
               if( Column != null )
               {
                 // it matches the left table and field?
                  if( Column.getTable().equals(srcLeftColumn.getTable()) &&
                      Column.getColumn().equals(srcLeftColumn.getColumn()) )
                  {
                   // yes look at the right side
                     otherColumn=joinData.getRightColumn();
                     if( otherColumn != null )
                     {
                       // see if they matches
                       if( otherColumn.getTable().equals(srcRightColumn.getTable()) &&
                           otherColumn.getColumn().equals(srcRightColumn.getColumn()) )
                       {
                          bRet=false;
                          break;    // they match return
                       }
                     }
                   }
                   // now see if the right column equals to our left column
                   if( Column.getTable().equals(srcRightColumn.getTable()) &&
                       Column.getColumn().equals(srcRightColumn.getColumn()) )
                   {
                     otherColumn=joinData.getLeftColumn();
                     if( otherColumn != null )
                     {
                       // it matches then look at the left side
                       if( otherColumn.getTable().equals(srcLeftColumn.getTable()) &&
                           otherColumn.getColumn().equals(srcLeftColumn.getColumn()) )
                       {
                           bRet=false; // they match return
                           break;
                       }
                     }
                   }
               } // if Column ! null
               // now get the right side
               Column=joinData.getRightColumn();
               if( Column != null )
               {
                  // it matches the left
                  if( Column.getTable().equals(srcLeftColumn.getTable()) &&
                      Column.getColumn().equals(srcLeftColumn.getColumn()) )
                  {
                    otherColumn=joinData.getLeftColumn();
                    if(otherColumn != null )
                    {
                       if( otherColumn.getTable().equals(srcRightColumn.getTable()) &&
                           otherColumn.getColumn().equals(srcRightColumn.getColumn()) )
                       {
                           bRet=false;
                           break;
                       }
                    }
                  }
                  // see if matches our right
                  if( Column.getTable().equals(srcRightColumn.getTable()) &&
                      Column.getColumn().equals(srcRightColumn.getColumn()) )
                  {
                     otherColumn=joinData.getRightColumn();
                     if(otherColumn != null )
                     {
                         if( otherColumn.getTable().equals(srcLeftColumn.getTable()) &&
                             otherColumn.getColumn().equals(srcLeftColumn.getColumn()) )
                         {
                             bRet=false;
                             break;
                         }
                     }
                  }
               } // if column ! = null
            } // if join data != null
         }// for
      } // if limit
    } // if valid data
    return(bRet);
  }

  /**
  * catalogs the table and try to guess the joins based on the type
  *
  *@param  masterTable the LEFT table
  *
  *@param  addedTable  the RIGHT table
  *
  *@param type correspond to one of the defined  CatalogExtendedTypes
  *
  *  FOREIGN_KEY
  *  PRIMARY_KEY
  *  UNIQUE_KEY
  */
  void CheckJoin(OSBackendTable masterTable,OSBackendTable addedTable,int type)
  {
     ExtendedBackendColumnData pColumn;
     ExtendedTypes mT;

     String columnName;
     String scFieldName;

     if( masterTable != null && addedTable != null &&
        ( type == CatalogExtendedTypes.FOREIGN_KEY  ||
          type == CatalogExtendedTypes.PRIMARY_KEY  ||
          type == CatalogExtendedTypes.UNIQUE_KEY    ) )
     {

       // if foreign key switch the table
       if( type==CatalogExtendedTypes.FOREIGN_KEY )
       {
          mT=extCatalog.getCatalog(addedTable,type,false);
       }
       else
       {
          mT=extCatalog.getCatalog(masterTable,type,false);
       }
       // if success
       if( mT != null )
       {
         // get the extended types
         Vector vc=mT.getExtendedTypes(type);
         if( vc != null ) //
         {
            // walk troug the extended types
           for( int iCount=0; iCount<vc.size(); iCount++)
           {
               switch(type)
               {
                   case CatalogExtendedTypes.FOREIGN_KEY:
                         // get the foreign key
                      ExtendedTypesForeignKey key=(ExtendedTypesForeignKey)vc.elementAt(iCount);
                      if( key != null )
                      {
                          // if the foreign table name matches the right side table
                          if( key.getForeignKeyTableName().equals(masterTable.getTable()))
                          {
                               columnName=key.getColumnName(); // get the column name
                               if( columnName != null )
                               {
                                  // get the type, and see if matches
                                 scFieldName=isMatchingBackEndColumn(columnName,masterTable);
                                 if( scFieldName != null )
                                 {
                                    // it matches add the join
                                    AddJoin(masterTable,scFieldName,addedTable);
                                  }
                               }
                           }
                      }
                   break;
                   case CatalogExtendedTypes.PRIMARY_KEY:
                        // get the primary key
                         ExtendedTypesPrimaryKey primaryKey=(ExtendedTypesPrimaryKey)vc.elementAt(iCount);
                         if( primaryKey != null )
                         {
                            // get the column name
                            columnName=primaryKey.getColumnName();
                            if( columnName != null )
                            {
                              // see if the right side table has a field of the same name
                               pColumn=addedTable.hasColumn(columnName);
                               if( pColumn != null )
                               {
                                 // get the type of the column
                                  ExtendedBackendColumnData pcol=masterTable.hasColumn(columnName);
                                  if( pcol != null )
                                  {
                                     // see if the match
                                     if( pcol.getBackendType().equals(pColumn.getBackendType()) )
                                     {
                                         if( pcol.getJDBCType().equals(pColumn.getJDBCType()) )
                                         {
                                            // the do add the join
                                            AddJoin(masterTable,columnName,addedTable);
                                          }
                                      }
                                   }
                                }
                             }
                          } // if primaryKey != null

               break;
               case CatalogExtendedTypes.UNIQUE_KEY:
                        // get the unique keys
                        ExtendedTypesUniqueKeys unique=(ExtendedTypesUniqueKeys)vc.elementAt(iCount);
                        if( unique != null )
                        {
                           Vector  uniqueKeys=unique.getUpdateKeyName();
                           if( uniqueKeys != null )
                           {
                               int limit=uniqueKeys.size();
                               // walk trough the unique keys
                               for( int loop=0; loop < limit; loop++)
                               {
                                  // get the column name
                                   columnName=(String)uniqueKeys.get(loop);
                                   if( columnName != null )
                                   {
                                     // see if LEFT side table has the same field name
                                     pColumn=addedTable.hasColumn(columnName);
                                     if( pColumn != null )
                                     {
                                       // get the column type
                                        ExtendedBackendColumnData pMasterColumn=masterTable.hasColumn(columnName);
                                        if( pMasterColumn != null && pMasterColumn.getBackendType().equals(pColumn.getBackendType()))
                                        {
                                          // see if the match
                                          if(pMasterColumn.getJDBCType().equals(pColumn.getJDBCType()) )
                                          {
                                             // they do add the join
                                             AddJoin(masterTable,columnName,addedTable);
                                          }
                                        }
                                     }
                                   }
                               }
                            }//   if( uniqueKeys != null )
                         }// if( unique != null )
                break;
            }
         }// for
        }// vc != null
     }// mT != null
    }
  }

  /**
  *searchs in the table for a matching field
  *
  *@param scField column name to search on the table
  *
  *@param tableData table to be searched
  *
  *@return <code> null </code> if not found, the column name if found
  *
  */
  String isMatchingBackEndColumn(String scField,OSBackendTable tableData)
  {
      String scRet=null;

      if( scField != null && tableData != null )
      {
           // get the columns
         Vector cols=tableData.getColumns();
         if( cols != null )
         {
            int limit=cols.size();
            // search the list
           for(int count=0; count<limit; count++)
           {
             String src=(String)cols.elementAt(count);
             // they match?
             if(src!= null && scField.equals(src) )
             {
                scRet=src; // set the field and break
                break;
             }
           }
         }
      }
      return(scRet);
  }

    /**
    * Overridden to convert all PSBackEndTable objects to OSBackEndTable objects.
   **/
   public PSCollection getTables()
   {
      PSCollection tables = super.getTables();
      if ( null == tables || 0 == tables.size()
            || tables.get(0) instanceof OSBackendTable )
      {
         return tables;
      }

      try
      {
         // convert all tables to our type
         PSCollection newTables = new PSCollection( "com.percussion.design.objectstore.PSBackEndTable" );
         int size = tables.size();
         for ( int i = 0; i < size; ++i )
         {
            PSBackEndTable oldTable = (PSBackEndTable) tables.get(0);
            OSBackendTable newTable = new OSBackendTable(oldTable);
            tables.remove(oldTable);
            newTables.add(newTable);
         }
         super.setTables( newTables );
         return newTables;
      }
      catch ( ClassNotFoundException e )
      {
         // design time error, should never happen
         e.printStackTrace();
      }
      catch ( IllegalArgumentException iae )
      {
         // should never happen, we are using theoretically valid objects w/o modification
         iae.printStackTrace();
      }

      return tables;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param tank a valid OSBackendDatatank. 
    */
   public void copyFrom( OSBackendDatatank tank )
   {
      copyFrom((PSBackEndDataTank) tank );
      m_filePath = tank.m_filePath;
      m_bUsedLocalFileSystem = tank.m_bUsedLocalFileSystem;
      m_bSourceIsPrepared = tank.m_bSourceIsPrepared;
    }

   public void setTables( PSCollection tables )
   {
      super.setTables( tables );
      invalidateCache();
      if ( null != m_owner )
         m_owner.invalidateLabel();
   }


   /**
    * Creates a new vector and fills it with a PSBackEndColumn object for each
    * column in each table that is part of this tank. The returned enumeration
    * will serve up columns in alphabetical order (based on the column name
    * <table alias>.<column name>).
    *
    * @return A vector containing PSBackEndColumn objects for all columns in this
    * tank, or an empty vector if there are no columns.
   **/
   public Enumeration getBackendColumns()
   {
      if(m_columnObjs != null && 0 != m_columnObjs.size())
         return m_columnObjs.elements();

      else
      {
         try
         {
            m_columnObjs = new Vector( 30 );
            if (getTables() != null && getTables().size() > 0)
            {
               PSCollection tables = getTables();
               int size = tables.size();
               // put it in a vector so we can sort it
               Vector tableVec = new Vector( size );
               for ( int i = 0; i < size; ++i )
                  tableVec.add( tables.get(i));
               if ( size > 1 )
               {
                  /* sort the tables, the columns from each table are
                     already sorted. */
                  Collator c = Collator.getInstance();
                  c.setStrength( Collator.PRIMARY );
                  ObjectCollator oc = new ObjectCollator( c );
                  Collections.sort( tableVec, oc );
               }
               for (int iTable=0; iTable < size; ++iTable)
               {
                  OSBackendTable tableData = null;
                  if (tableVec.get(iTable) instanceof OSBackendTable)
                     tableData = (OSBackendTable) tableVec.get(iTable);
                  else if (tableVec.get(iTable) instanceof PSBackEndTable)
                     tableData = new OSBackendTable((PSBackEndTable) tableVec.get(iTable));
                  if ( tableData == null )
                     continue;

                  Enumeration cols = tableData.getBackendColumns();
                  while ( cols.hasMoreElements())
                     m_columnObjs.add( cols.nextElement());
               }
            }
         }
         catch ( IllegalArgumentException e )
         {
            // not much we can do
            e.printStackTrace();
         }
      }
      return m_columnObjs.elements();
   }


   /**
    * Catalog function to get column names from the tables. Returns a vector
    * containing all of the columns from all of the tables in this tank. The
    * names are of the form <table alias>.<column name> are are sorted in
    * ascending order according to the local collation rules.
    * TODOph: Should return an enumeration so it can't be modified.
    */
   //////////////////////////////////////////////////////////////////////////////
   public Vector getColumns()
   {
         Vector columns = new Vector(30);
         Enumeration cols = getBackendColumns();
         while ( cols.hasMoreElements())
            columns.add( cols.nextElement().toString());

      System.out.println("\n"+"BackendDatatank getColumns returning "+ columns.toString());
      return columns;
   }


   /**
    * Returns the PSBackEndTable object that has the passed in alias.
    *
    * @param alias - the alias of the table requested
    */
   public PSBackEndTable getBackEndTable(String alias)
   {
      if(alias == null || alias.equals(""))
         return null;

      PSBackEndTable beTable = null;

     if (getTables() != null && getTables().size() > 0)
    {
       for (int i=0; i<getTables().size(); i++)
        {
            if(getTables().get(i) instanceof PSBackEndTable)
            {
               beTable = (PSBackEndTable)getTables().get(i);
               String strAlias = beTable.getAlias();
               if(strAlias != null)
               {
                  if(alias.equals(strAlias))
                     return beTable;
               }
            }
         }
      }
      return null;

   }

  //////////////////////////////////////////////////////////////////////////////
   // IGuiLink interface implementation
   public void setFigure(UIFigure fig)   {   m_owner = fig; }
   public void release()   {   m_owner = null;   }
   public UIFigure getFigure()   {   return m_owner;   }

   //////////////////////////////////////////////////////////////////////////////
  // IPersist interface implementation
   public boolean load(PSApplication app, Object store, Properties config)
   {
      if (null == store || null == config)
         throw new IllegalArgumentException();

      if (!(store instanceof PSPipe))
      {
         String [] astrParams =
         {
            "PSPipe"
         };
         throw new IllegalArgumentException(MessageFormat.format(
            E2Designer.getResources().getString("IncorrectType"), astrParams));
      }

     try
     {
         PSPipe pipe = (PSPipe) store;
       this.copyFrom(pipe.getBackEndDataTank());

       // restore the GUI stuff
         String strId = new Integer(this.getId()).toString();

       // restore GUI information
       OSLoadSaveHelper.loadOwner(this.getId(), config, m_owner);
       if (null != m_owner)
          m_owner.invalidateLabel();

     // load all tables
      PSCollection tables = this.getTables();
      for (int i=0; i<tables.size(); i++)
      {
      OSBackendTable osTable = (OSBackendTable) tables.get(i);
        osTable.load(app, this, config);
      }

       return true;
     }
     catch (Exception e)
     {
        e.printStackTrace();
     }

      return false;
   }

   //////////////////////////////////////////////////////////////////////////////
  // IPersist interface implementation
   public void save(PSApplication app, Object store, Properties config)
   {
      if (null == store || null == config)
         throw new IllegalArgumentException();

      if (!(store instanceof PSPipe))
      {
         String [] astrParams =
         {
            "PSPipe"
         };
         throw new IllegalArgumentException(MessageFormat.format(
            E2Designer.getResources().getString("IncorrectType"), astrParams));
      }

      try
    {
      // save all tables
      PSCollection tables = this.getTables();
      for (int i=0; i<tables.size(); i++)
      {
        OSBackendTable osTable = (OSBackendTable) tables.get(i);
        osTable.save(app, this, config);
      }

         // store current id temporary and create a new unique id for this object
         int currentId = this.getId();
         this.setId(Util.getUniqueId());

         // save this backendtank into the provided pipe
         PSPipe pipe = (PSPipe) store;
      pipe.setBackEndDataTank(this);

      // save GUI information
      OSLoadSaveHelper.saveOwner(currentId, this.getId(), config, m_owner);
    }
    catch (Exception e)
    {
       e.printStackTrace();
    }
   }

   //////////////////////////////////////////////////////////////////////////////
  // ICustomDropSourceData interface implementation
   public boolean prepareSourceForDrop(DropAction action, String filePath, String rootName)
  {
    // just do this once
    if (m_bSourceIsPrepared)
      return true;

    // indicate this has been prepared
    m_bSourceIsPrepared = true;

    // get page datatank
    UTPipeNavigator navigator = new UTPipeNavigator();
    UIFigure page = navigator.getPageTank(getFigure());
     if (page != null)
    {
      OSPageDatatank pageTank = (OSPageDatatank) page.getData();
      if (pageTank.isSchemaSourceReadOnly())
      {
        try
        {
          pageTank.autoCreatePageDatatank(this);
        }
        catch (IllegalArgumentException e)
        {
          e.printStackTrace();
          return false;
        }
        catch (MalformedURLException e)
        {
          e.printStackTrace();
          return false;
        }
      }
    }

    return true;
  }

   //////////////////////////////////////////////////////////////////////////////
  // ICustomDropSourceData interface implementation
  /**
   * Interface method unimplemented.
   * @see com.percussion.E2Designer.IPersist#cleanup(com.percussion.
   * E2Designer.OSApplication)
   */
  public void cleanup(OSApplication app)
  {
  }
   
   //////////////////////////////////////////////////////////////////////////////
  // ICustomDropSourceData
   public String getFilePath()
   {
    return m_filePath;
   }

   //////////////////////////////////////////////////////////////////////////////
  // ICustomDropSourceData
   public void setFilePath(String filePath)
   {
    m_filePath = filePath;
   }

   //////////////////////////////////////////////////////////////////////////////
  // ICustomDropSourceData
   public void setUsedLocalFileSystem()
   {
    m_bUsedLocalFileSystem = true;
   }

   //////////////////////////////////////////////////////////////////////////////
   // private storage
   private UIFigure m_owner = null;

   //private Vector m_columns = null;
   /**
    * Contains cached
   **/
   private Vector m_columnObjs = null;

  /*
   * status flag to save wether or not we have to store files from the local
   * file system
   */
   private boolean m_bUsedLocalFileSystem = false;
  /*
   * status flag which indicates wether or not the source preparation from the
   * local file system has been done
   */
   private boolean m_bSourceIsPrepared = false;
  private String m_filePath = null;
  private CatalogExtendedTypes extCatalog=null;
  private PSCollection m_joins=null;


}
