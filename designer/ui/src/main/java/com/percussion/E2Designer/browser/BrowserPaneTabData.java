/*[ BrowserPaneTabData.java ]**************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/


package com.percussion.E2Designer.browser;

/**
 * Class to store information associated with each of the Browser Pane Tabs.
 * Contains GroupID and GroupNumber associated with each tab.
 * <p>
 * The GroupID is a unique <code>int</code> that identifies a Tab's association
 * to a group.
 * <p>
 * The GroupNumber is a 0 based index ranging from 0 to N-1 where N is the
 * total number of Groups present in the BrowserPane.
 */

public class BrowserPaneTabData
{
    /**
    * Sets the GroupID.
     */
   public void setGroupID(int groupID)
   {
      m_iGroupID = groupID;
   }

    /**
    * Sets the GroupNumber
     */
   public void setGroupNumber( int groupNumber)
   {
      m_iGroupNumber = groupNumber;
   }

   public int getGroupID()
   {
      return m_iGroupID;
   }

   public int getGroupNumber()
   {
      return m_iGroupNumber;
   }

    /**
    * Increments the GroupNumber by 1
     */
  public void incrementGroupNumber()
   {
      m_iGroupNumber++;
   }

    /**
    * Decrements the GroupNumber by 1
     */
  public void decrementGroupNumber()
   {
      m_iGroupNumber--;
   }

  public void setDisplayName(String strDisplayName)
  {
    m_strDisplayName = strDisplayName;
  }

  public String getDisplayName()
  {
    if(m_strDisplayName == null || m_strDisplayName.equals(""))
         return m_strInternalName;
      else
         return m_strDisplayName;
  }

  public void setInternalName(String strInternalName)
  {
    m_strInternalName = strInternalName;
  }

  public String getInternalName()
  {
    return m_strInternalName;
  }


   //the GroupIDs
  public static final int   APPLICATION = 0;
  public static final int   XML = 1;
  public static final int   DATASOURCES = 2;
  public static final int   SECURITY = 3;
  public static final int   SERVER_OBJECTS=4;

  /**
    * The ID of Group this tab falls under
   * The Group IDs can be any unique int value, for example
    * Application   = 0
    * Driver = 1
     */
   private int m_iGroupID;

  /**
    * The 0 based group index within the tabs depending on the number of
   * groups present. This may get incremented or decremented when a
    * tab is added or removed
     */
   private int m_iGroupNumber;

  /**
    * The name to be displayed on the tab.
     */
  private String m_strDisplayName = "";



  /**
    * The name to be used when communicationg with the E2 Server
     */
  private String m_strInternalName= "";
   
}
