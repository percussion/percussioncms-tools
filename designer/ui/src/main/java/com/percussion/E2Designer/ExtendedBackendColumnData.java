/*[ ExtendedBackendColumnData.java ]*******************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;
import java.io.Serializable;

/**
* This class contains the column field type and column name.
*/
public class ExtendedBackendColumnData implements Serializable
{
  /**
  * default constructor of ExtendedBackendColumnData
  *
  */
  public ExtendedBackendColumnData()
  {
  }
  /**
  *constructor of ExtendedBackendColumnData
  *
  *@param columName the field name
  *
  *@param backendDataType the native field type
  *
  *@param backendJDBCType the JDBC field type
  *
  */
  public ExtendedBackendColumnData(String columName,String backedDataType,String backendJDBCType)
  {
    if( columName != null && backedDataType != null && backendJDBCType != null )
    {
      m_Column_name=columName;
      m_backEndDataType=backedDataType;
      m_jdbcDataType=backendJDBCType;
    }
  }
  /**
  * copy the passed ExtendedBackendColumnData into our data
  *
  * @param e data to be copied, throws IllegalArgumentException if null
  *
  * @throws IllegalArgumentException if e is null
  */
  public void copyFrom(ExtendedBackendColumnData e)
 {
   if( e == null )
     throw new IllegalArgumentException();

   m_Column_name=e.m_Column_name;
   m_backEndDataType=e.m_backEndDataType;
   m_jdbcDataType=e.m_jdbcDataType;
  }
  /**
  *@return the colum name
  */

  public String getColumName()
  {
     return(m_Column_name);
  }
  /**
  *@return native type as string
  */
  public String getBackendType()
  {
     return(m_backEndDataType);
  }

  /**
  *@return the jdbc type as string
  */
  public String getJDBCType()
  {
    return(m_jdbcDataType);
  }
  /**
  *the colum name
  */
  private String m_Column_name;
  /**
  *the native backend type
  */
  private String m_backEndDataType;
  /**
  *the JDBC type as string
  */
  private String m_jdbcDataType;


}
