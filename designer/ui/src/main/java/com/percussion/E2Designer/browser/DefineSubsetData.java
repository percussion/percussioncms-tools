/*[ DefineSubsetData.java ]****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.browser;

//import java.util.Vector;

public class DefineSubsetData {


  public DefineSubsetData(String strDriver, String strServer, String strDatabase, String strSubsetName, String strFilter, String [] straObjTypes)
  {
    m_strDriver = strDriver;
    m_strServer = strServer;
      m_strDatabase = strDatabase;
      m_strSubsetName = strSubsetName;
      m_strFilterString = strFilter;
      m_straFilterObjectTypes = straObjTypes;
  }

  public String getDriver()
  {
    return m_strDriver;
  }


  public String getServer()
  {
    return m_strServer;
  }


  public String getDatabase()
  {
    return m_strDatabase;
  }

  public void setDatabase(String strDatabase)
  {
    m_strDatabase = strDatabase;
  }

  public String getSubsetName()
  {
    return m_strSubsetName;
  }

  public void setSubsetName(String strSubsetName)
  {
    m_strSubsetName = strSubsetName;
  }

  public String getFilterString()
  {
    return m_strFilterString;
  }

  public void setFilterString(String strFilterString)
  {
    m_strFilterString = strFilterString;
  }

  public String [] getFilterObjectTypes()
  {
    return m_straFilterObjectTypes;
  }

  public void setFilterObjectTypes(String [] straFilterObjectTypes)
  {
    m_straFilterObjectTypes = straFilterObjectTypes;
  }

//  static final String TABLES = "Tables";
//  static final String VIEWS = "Views";
//  static final String SYSTEM_TABLES = "System tables";

  private   String    m_strDriver           = "";
  private   String    m_strServer           = "";
  private   String    m_strDatabase         = "";
  private   String    m_strSubsetName       = null;     // contains subset name
  private   String    m_strFilterString     = null;     // contains Filter string corresponding to subset name
  private   String [] m_straFilterObjectTypes  = null;     // contains Filter Object Types corresponding to subset name
}
