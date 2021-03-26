/*[ ExtendedTypesForeignKey.java ]*********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

/**
*this class contains the foreign key information
*/
public class ExtendedTypesForeignKey
{
  /**
  * default constructor of ExtendedTypesForeignKey
  *
  */
  public  ExtendedTypesForeignKey()
  {

  }

  /**
  * constructor of ExtendedTypesForeignKey
  *
  *
  *@param  primaryKeyColumName column name
  *
  *@param  foreignTableSchema foreign table schema ( can be <code> null </code>)
  *
  *@param  foreignKeyTableName the foreign table
  *
  *@param  foreignKeyColumName the foreing field name
  */
  public ExtendedTypesForeignKey(String primaryKeyColumnName,
                                 String foreignTableSchema,
                                 String foreignKeyTableName,
                                 String foreignKeyColumnName)

  {
    m_primaryKeyColumnName=primaryKeyColumnName;
    m_foreignTableSchema=foreignTableSchema;
    m_foreignKeyTableName=foreignKeyTableName;
    m_foreignKeyColumnName=foreignKeyColumnName;
  }

  /**
  * copy the data into our
  *
  *@param e ExtendedTypesForeignKey to be copied, throws IllegalArgumentException
  *   if invalid
  *
  *@throws IllegalArgumentException if e is null
  */
  public void copyFrom(ExtendedTypesForeignKey e)
  {
    if( e == null )
      throw  new IllegalArgumentException();

    m_primaryKeyColumnName=e.m_primaryKeyColumnName;
    m_foreignTableSchema=e.m_foreignTableSchema;
    m_foreignKeyTableName=e.m_foreignKeyTableName;
    m_foreignKeyColumnName=e.m_foreignKeyColumnName;
  }

  /**
  *@return the column Name
  */

  public String getColumnName()
  {
    return(m_primaryKeyColumnName);
  }

  /**
  *@return the foreign table schema
  */
  public String getForeignTableSchema()
  {
    return(m_foreignTableSchema);
  }

 /**
  *@return the foreign table Name
  */
  public String getForeignKeyTableName()
  {
    return(m_foreignKeyTableName);
  }

  /**
  *@return the foreign column Name
  */

  public String getForeignKeyColumnName()
  {
    return(m_foreignKeyColumnName);
  }
  /**
  *column name
  */
   private  String m_primaryKeyColumnName="";
  /**
  * the foreign table schema
  */
  private  String m_foreignTableSchema="";
  /**
  *the foreign table name
  */
  private  String m_foreignKeyTableName="";

  /**
  *the foreign column name
  */
  private  String m_foreignKeyColumnName="";

}
