/*[ ExtendedTypesUniqueKeys.java ]*********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;
import java.util.Vector;
/**
* this class contains the unique keys data
*/
public class ExtendedTypesUniqueKeys
{
  /**
  * constructor of ExtendedTypesUniqueKeys
  *
  */
  public ExtendedTypesUniqueKeys()
  {
     InitializeVector();
     m_tableName="";

  }
  /**
  * constructor of ExtendedTypesUniqueKeys
  *
  *@param tableName from where the unique keys belong
  *
  *@param vc contains the unique keys
  */

  public ExtendedTypesUniqueKeys(String tableName,Vector vc)
  {
   m_tableName=tableName;
   InitializeVector();
   m_Objects.addAll(vc);
  }
  /**
  * copies the unique keys
  *
  *@param e data source, throws IllegalArgumentException if invalid
  *
  *@throws IllegalArgumentException if e is null
  */
  public void copyFrom(ExtendedTypesUniqueKeys e)
  {
    if( e == null )
       throw new IllegalArgumentException();

    m_tableName=e.m_tableName;
    InitializeVector();
    m_Objects.addAll(e.getUpdateKeyName());
  }
  /**
  *@return update keys name
  */
  public  Vector getUpdateKeyName()
  {
    return(m_Objects);
  }
  /**
  *@return the table name to which this update name belongs
  */
  public String getTableName()
  {
     return(m_tableName);
  }
  /**
  * initializes the class to defauls
  *
  */
  private void InitializeVector()
  {
    if(m_Objects != null )
    {
       m_Objects.removeAllElements();
    }
    if(m_Objects == null )
    {
      m_Objects= new Vector(2);
    }
  }
  /**
  *the update keys
  */
  private Vector m_Objects=null;
  /**
  * the associated table name
  */
  private String m_tableName="";
}
