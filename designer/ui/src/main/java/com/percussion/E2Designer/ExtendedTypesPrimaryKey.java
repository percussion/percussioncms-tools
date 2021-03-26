/*[ ExtendedTypesPrimaryKey.java ]*********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;
/**
* this class contains the primary key data
*/

public class ExtendedTypesPrimaryKey
{
  /**
  * default  constructor
  */
  public ExtendedTypesPrimaryKey()
  {

  }

  /**
  * constructor of ExtendedTypePrimaryKey
  *
  *@param tableName the table name
  *
  *@param columnName the column name ( primary key )
  *
  */
  public ExtendedTypesPrimaryKey(String tableName,String columnName)
  {
      m_columnName=columnName;
      m_tableName=tableName;
  }
  /**
  * copies the passed ExtendedTypesPrimaryKey
  *
  *@param e data to be copied, if invalid throw IllegalArgumentException
  *
  *@throws IllegalArgumentException if e is null
  *
  */
  public void copyFrom(ExtendedTypesPrimaryKey e)
  {
     if( e == null )
        throw new   IllegalArgumentException();

      m_columnName=e.m_columnName;
      m_tableName=e.m_tableName;
  }

  /**
  *@return colum name
  */
  public String getColumnName()
  {
      return(m_columnName);
  }
  /**
  *@return table name
  */
  public String getTableName()
  {
    return(m_tableName);
  }
  /**
  *table name
  */
  private  String m_tableName;
  /**
  * column name
  */
  private  String m_columnName;
}
