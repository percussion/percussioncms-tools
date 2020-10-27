/*[ DTBackendColumn.java ]*****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.util.PSCollection;

import java.util.Enumeration;
import java.util.Vector;

public class DTBackendColumn extends AbstractDataTypeInfo
{
   /**
    * @param tank container of all the possible tables in a given context
    */
   public DTBackendColumn(OSBackendDatatank tank)
   {
      m_tank = tank;

      PSCollection tables = tank.getTables();
      if (tables.size() < 1)
         throw new IllegalArgumentException();
   }

   /**
    * @param a vector of backend tanks
    */
   public DTBackendColumn(Vector backendTanks)
   {
      if (backendTanks == null || backendTanks.size() == 0)
         throw new IllegalArgumentException();

      try
      {
         OSBackendDatatank mergedTank = null;
         for (int i = 0; i < backendTanks.size(); i++)
         {
            OSBackendDatatank backendTank = (OSBackendDatatank) backendTanks
               .get(i);
            if (mergedTank == null)
            {
               mergedTank = new OSBackendDatatank();
               mergedTank.copyFrom(backendTank);
            }
            else
            {
               PSCollection mergedTables = mergedTank.getTables();
               PSCollection tables = backendTank.getTables();
               for (int j = 0; j < tables.size(); j++)
               {
                  PSBackEndTable newTable = (PSBackEndTable) tables.get(j);
                  boolean exists = false;
                  for (int k = 0; k < mergedTables.size(); k++)
                  {
                     PSBackEndTable table = (PSBackEndTable) mergedTables
                        .get(k);

                     if (table.getTable().equals(newTable.getTable())
                        && table.getAlias().equals(newTable.getAlias())
                        && table.getDataSource().equals(
                           newTable.getDataSource()))
                     {
                        exists = true;
                        break;
                     }
                  }
                  
                  if (!exists)
                     mergedTables.add(tables.get(j));
               }
            }
         }

         m_tank = mergedTank;
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

   }

   /**
    * There are cases where columns need to be added that can't be cataloged if
    * a user entered SELECT statement is used.
    *
    * @returns <code>true</code> always
    */
   public boolean allowUncatalogedValues()
   {
      return true;
   }

   /**
    * Creates a PSBackEndColumn object from the passed in column name.
    *
    * @param strValue a name obtained from <code>catalog</code> of a form
    * known by <code>PSBackEndColumn</code>. Also allows just 'column_name'. 
    * In this case, it looks for the first table that contains this name.
    * If an empty value is passed, then the first column in the tank is used.
    */
   public Object create(String strValue)
   {
      PSBackEndColumn column = null;

      try
      {
         PSCollection tables = m_tank.getTables();
         try
         {
            if (strValue != null && strValue.trim().length() != 0)
            {
               column = PSBackEndColumn.findColumnFromDisplay(strValue, tables);
            }
            else
            {
               if (tables.size() > 0)
               {
                  OSBackendTable table = (OSBackendTable) tables.get(0);
                  Enumeration columns = table.getBackendColumns();
                  if (columns.hasMoreElements())
                  {
                     column = (PSBackEndColumn) columns.nextElement();
                  }
               }
            }
         }
         catch (IllegalArgumentException e)
         {
            column = null;
         }

         if (column == null || column.getColumn() == null)
         {
            if (tables.isEmpty())
            {
               throw new IllegalArgumentException("Tank may not be empty");
            }
            // Assume that the passed string value is just the column
            // name with a missing table. Use the first table in the
            // collection to create a back end column.
            PSBackEndTable table = (PSBackEndTable) tables.get(0);
            column = new PSBackEndColumn(table, strValue);
         }
      }
      catch (IllegalArgumentException e)
      {
         e.printStackTrace();
      }

      return column;
   }

   /**
    * @return a vector containing Strings of all the column names in the form
    * 'table_alias.column_name'. One of these may be passed as the param to the
    * <code>create</code> method, if it is called. Or any 'column_name' in this
    * list may also be passed to that method.
    */
   public Enumeration catalog()
   {
      if (null == m_cols)
      {
         m_cols = m_tank.getColumns();
         if (null == m_cols)
            m_cols = new Vector(0);
      }
      return m_cols.elements();
   }

   // storage
   private OSBackendDatatank m_tank = null;
   private Vector m_cols = null;
}
