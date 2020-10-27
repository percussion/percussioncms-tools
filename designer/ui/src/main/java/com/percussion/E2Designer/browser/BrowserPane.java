/*[ BrowserPane.java ]*********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.browser;


import javax.swing.*;
import java.awt.*;
import java.text.Collator;
import java.util.Enumeration;
import java.util.Vector;


/**
 * Extends a JTabbedPane by adding the concept of tab groups, context sensitive
 * menus for nodes and DnD for nodes. Tabs with same GroupID's are grouped together.
 * GroupID's should be <code>int</code> values. Tabs are placed alphabetically
 * within a tab group.
 * <p>
 * A vector maintains the BrowserPaneTabData which contains the GroupID and
 * a GroupNumber for each tab. GroupNumber is a 0 based index ranging from 0 to
 * N-1 where N is the total number of Groups present. After addition of a new group
 * with new GroupID or after removal of a Group with a specified GroupID, the
 * GroupNumber is rearranged.
 */
public class BrowserPane extends JTabbedPane
{
   // constructors
   /**
    * Creates the BrowserPane object and creates a vector for storing the
    * BrowserPaneTabData (Group ID's and Group Numbers) for the BrowserPane Tabs.
    */
  BrowserPane()
  {
    super();
    // create a vector with initial size of 5, and size increments of 1
    m_vTabs = new Vector(5, 1);
    m_vTabs.removeAllElements( );
  }

   /**
    * Adds a tab to the specified GroupID. The tab is added in ascending alpha order
    * within the group. If a group by the supplied ID doesn't exist, a new group is
   * created and added at specified GroupNumber. Groups are sequenced left to right,
   * starting with lowest GroupNumber. Group numbers range from 0 to N-1. When adding a grouped
    * tab, iGroupNumber must be in the range from 0 to N.
    *
    * @throws GroupIndexOutOfBoundsException if iGroupNumber is not in the range
    * 0 to N inclusive, where N is the number of groups.
    */
   public void insertGroupedTab( String strTitle, Icon icon,
         Component Comp, String strTip, int iGroupID, int iGroupNumber )
   throws GroupIndexOutOfBoundsException
   {
    BrowserPaneTabData data = new BrowserPaneTabData();

    if (m_vTabs.isEmpty())
    {
      if (iGroupNumber != 0)
      {
        throw new GroupIndexOutOfBoundsException("Group Number out of bounds");
      }
      else    // iGroupNumber == 0
      {       // add to beginning
        insertTab( strTitle, icon, Comp, strTip, iGroupNumber );
        data.setGroupNumber( iGroupNumber);
        data.setGroupID(iGroupID);
        data.setInternalName(strTitle);
        m_vTabs.addElement(data);
      }
    }
    else
    {
      // if iGroupID already exists, then iGroupNumber is immaterial
      // find the Group ID traverse it and insert in alpha order
      if (isGroupIDPresent(iGroupID))
      {
        int i, iFirst , iTabCount = this.getTabCount(iGroupID);
        iFirst = getTabIndexOfGroupID(iGroupID);
        int iGroupNum = getGroupNumberAtTabIndex(iFirst);
        i = iFirst;
        if(iTabCount > 0)
        {  // Compare the strings using the default locale
          Collator c = Collator.getInstance();
          c.setStrength(Collator.SECONDARY);     // for case insensitive comparison
          for ( i=iFirst ; i < iFirst+iTabCount ; i++)
          {
            String s = this.getTitleAt(i);
            if( c.compare(strTitle, s) < 0 )
            break;
          }
        }
        // we only need to insert tab and set color no need to increment group numbers
        insertTab( strTitle, icon, Comp, strTip, i );
        setBackgroundAt(i, getGroupBackgroundAt(iGroupID)); // call to superclass method
        data.setGroupID(iGroupID);
        data.setGroupNumber(iGroupNum);
        data.setInternalName(strTitle);
        m_vTabs.insertElementAt(data, i);

      }
      else // New Group ID. Add at the specified Group Number
      {
        if (iGroupNumber > getGroupCount())
        {
          throw  new GroupIndexOutOfBoundsException("Group Number out of bounds.");
        }
        else
        {
          if (iGroupNumber < getGroupCount() && getGroupCount() > 0)
          {
            int insertIndex = getTabIndexOfGroupNumber(iGroupNumber);
            insertTab( strTitle, icon, Comp, strTip, insertIndex );
            data.setGroupNumber( iGroupNumber);
            data.setGroupID(iGroupID);
            data.setInternalName(strTitle);
            m_vTabs.insertElementAt(data, insertIndex);

            if(m_vTabs.size() > insertIndex+1)
            for( int i = insertIndex+1; i < m_vTabs.size(); i++)
            {
              data = (BrowserPaneTabData)m_vTabs.get( i );
              data.incrementGroupNumber();
            }
          }
          else
          {
            if (iGroupNumber == getGroupCount())  // this is last group
            {                                     // add at end
              addTab( strTitle, icon, Comp, strTip);
              data.setGroupNumber( iGroupNumber);
              data.setGroupID(iGroupID);
              data.setInternalName(strTitle);
              m_vTabs.addElement(data);
            }
          }
        }
      }

    }
   //For Debugging
   //printVector();
  }

  /**
    * Adds a tab to the specified GroupID. The tab is added in ascending alpha order
    * within the group. If a group by the supplied ID doesn't exist, a new group is
   * created and added at the end.
    */
   public void insertGroupedTab( String strTitle, Icon icon,
         Component Comp, String strTip, int iGroupID )
  {
    if (isGroupIDPresent(iGroupID))      // the iGroupNumber is immaterial
    {
      insertGroupedTab( strTitle, icon, Comp, strTip, iGroupID, 0);
    }
    else                                // create a new group and add at end
    {
      int iGroupNum = getGroupCount();
      // add at iGroupNum because 0 based count, iGroupNum will be a new group
      insertGroupedTab( strTitle, icon, Comp, strTip, iGroupID, iGroupNum);
    }
  }



   /**
    * Removes the tab for the specified GroupID and index values.
    *
    * @throws GroupIDNotFoundException if iGroupID is not found.
    * @throws GroupIndexOutOfBoundsException if iIndex is not in the range
    * 0 to I-1 inclusive, where I is the number of tabs for this group.
    */
   public void removeTabAt( int iGroupID, int iIndex )
   throws GroupIndexOutOfBoundsException,
  GroupIDNotFoundException
   {

    // First find if the group with the iGroupID exists. If it does then get the
    // index, remove the requested Tab. After removing the Tab we will need to
    // check if other elements of iGroupID still exist. If they exist we are
    // done, otherwise we need to reduce subsequent group numbers (if any) by 1.

       // get the index of the first tab in this Group
    int indexGroup = getTabIndexOfGroupID(iGroupID);

    if (indexGroup < 0)
    {
      throw new GroupIDNotFoundException("Group ID not found - " + iGroupID);
    }
    else
    {
      if (iIndex > getTabCount(iGroupID)-1)
      {
        throw new GroupIndexOutOfBoundsException("Group Index out of bounds.");
      }
      else
      {
        int iTabIndex =  indexGroup+iIndex;         // overall tab index of Tab
        this.removeTabAt( iTabIndex );      // remove the tab
        m_vTabs.removeElementAt(iTabIndex); // remove the Data, call to super

        // if iGroupID still exists we need to do nothing otherwise decrement
        // group numbers for subsequent Tabs by 1
        if ( getTabCount(iGroupID) <= 0)   // does not exist anymore
        {
          if(m_vTabs.size() > iTabIndex)
          for(int i = iTabIndex; i < m_vTabs.size(); i++)
          {
            BrowserPaneTabData data = (BrowserPaneTabData)m_vTabs.get( i );
            data.decrementGroupNumber();
          }
        }
      }
    }

   }

   /**
    * Returns the lowest group number of the tab that has the supplied name,
    * or -1 if no tab has this name.
    */
   public int getGroupNumberOfTab( String strTabName )
   {
    // first get the overall index of the tab in the Browser Pane
    int iTabIndex = this.indexOfTab( strTabName );

    if (iTabIndex == -1)     // tab not found
    {
        return -1;
    }
    else
    {   // get the Group Number stored at the iTabIndex location
        // in the Vector
      BrowserPaneTabData data = (BrowserPaneTabData)m_vTabs.get(iTabIndex);
      return data.getGroupNumber();
    }
   }

     /**
    * Returns the first group ID of the tab that has the supplied name,
    * or -1 if no tab has this name.
    */
   public int getGroupIDOfTab( String strTabName )
   {
    // first get the overall index of the tab in the Browser Pane
    int iTabIndex = this.indexOfTab( strTabName );

    if (iTabIndex == -1)     // tab not found
    {
        return -1;
    }
    else
    {   // get the Group ID stored at the iTabIndex location
        // in the Vector
      BrowserPaneTabData data = (BrowserPaneTabData)m_vTabs.get(iTabIndex);
      return data.getGroupID();
    }
   }

   /**
    * Removes all tabs with the supplied GroupID. The group number for all
   * subsequent groups (if any) are reduced by 1.
    */
   public void removeAll( int iGroupID )
   {
    int iIndex = getTabIndexOfGroupID(iGroupID);
    if (m_vTabs.isEmpty() || iIndex < 0)      // nothing to remove
    {
      return;
    }
    else
    {
      while(iIndex >= 0)
      {
        removeTabAt(iGroupID, 0);
        iIndex = getTabIndexOfGroupID(iGroupID);
      }
    }

    return ;
    }

   /**
    * Returns the number of tabs in the supplied group.
    */
   public int getTabCount( int iGroupID )
   {
    int count = 0;
    if (!isGroupIDPresent(iGroupID))
    {
      return 0;
    }
    else
    {
      BrowserPaneTabData data = null;
      Enumeration e = m_vTabs.elements();
      while(e.hasMoreElements())
      {
        data = (BrowserPaneTabData)e.nextElement();
        if (data.getGroupID() == iGroupID)
           count++;
      }
    }
    return count;
    }

   /**
    * Returns the number of groups. If no tabs are present, then 0 is returned,
    * otherwise, a value in the range 1 to N is returned, where N is the number
    * of Groups of Tabs.
    */
   public int getGroupCount( )
   {
    int count;
    if (m_vTabs.isEmpty())
    {
      count = 0;
    }
    else
    {
      int currentGroupNumber = 0;
      count = 1;
      BrowserPaneTabData data = null;
      Enumeration e = m_vTabs.elements();
      while(e.hasMoreElements())
      {
        data = (BrowserPaneTabData)e.nextElement();
        if (data.getGroupNumber() > currentGroupNumber)
        {
          currentGroupNumber++;
          count++;
        }
      }
    }

    return count;
    }

   /**
    * Just like getBackgroundAt, except it applies to all tabs in the group
    * with the specified GroupID.
    *
    * @throws GroupIDNotFoundException if iGroupID is not found.
    */
   public Color getGroupBackgroundAt( int iGroupID )
   throws GroupIDNotFoundException
   {
    int iIndex = getTabIndexOfGroupID(iGroupID);
    if (m_vTabs.isEmpty() || iIndex < 0)      // GroupID not present
    {
      throw new GroupIDNotFoundException("GroupID "+ iGroupID +" not found");
    }
    else
    {
      return getBackgroundAt(iIndex);
    }
    //   return Color.blue;
   }

   /**
    * Just like setBackgroundAt, except it applies to all tabs in the group
    * with the specified GroupID.
    */
   public void setGroupBackgroundAt( int iGroupID, Color c )
   {
    int iIndex = getTabIndexOfGroupID(iGroupID);
    if (m_vTabs.isEmpty() || iIndex < 0)
    {
      System.out.println("GroupID "+ iGroupID +" doesn't exist");
    }
    else
    {
      int iGroupCount = getTabCount(iGroupID);
      for(int i = iIndex; i < iIndex+iGroupCount; i++)
      {
        setBackgroundAt(i, c); // call to superclass method
      }
    }
    return;
  }

    /**
    * Returns <code>true</code> if a group with the specified GroupID exists.
    * Otherwise returns <code>false</code>.
    */

  public boolean isGroupIDPresent( int iGroupID)
  {
    boolean bFoundGroupID = false;

    if(!m_vTabs.isEmpty())
    {
      Enumeration e = m_vTabs.elements();
      while(e.hasMoreElements())
      {
        BrowserPaneTabData data = (BrowserPaneTabData)e.nextElement();
        if (data.getGroupID() == iGroupID)
        {
          bFoundGroupID = true;
          break;
        }
      }
    }
    return bFoundGroupID;
  }

    /**
    * Returns the zero based index of the first tab in the group with the
   * specified GroupID. Returns -1 if the group is not found.
    */
  public int getTabIndexOfGroupID( int iGroupID)
  {
    int indexOfTab = -1;
    if (!isGroupIDPresent(iGroupID))
    {
      return -1;
    }
    else
    {
      BrowserPaneTabData data = null;
      Enumeration e = m_vTabs.elements();
      while(e.hasMoreElements())
      {
        indexOfTab++;
        data = (BrowserPaneTabData)e.nextElement();
        if (data.getGroupID() == iGroupID)
        {
          break;
        }
      }
    }
    return indexOfTab;
  }

    /**
    * Returns the zero based index of the first tab in the group with the
   * specified GroupNumber. Returns -1 if the group is not found.
    */
  public int getTabIndexOfGroupNumber( int iGroupNumber)
  {
    int indexOfTab = -1;
    if (m_vTabs.isEmpty())
    {
      return -1;
    }
    else
    {
      BrowserPaneTabData data = null;
      Enumeration e = m_vTabs.elements();
      while(e.hasMoreElements())
      {
        indexOfTab++;
        data = (BrowserPaneTabData)e.nextElement();
        if (data.getGroupNumber() == iGroupNumber)
        {
          break;
        }
      }
    }
    return indexOfTab;
  }

    /**
    * Returns the GroupID of the tab at the specified index
     */
  public int getGroupIDAtTabIndex(int iTabIndex)
  {
    BrowserPaneTabData data = (BrowserPaneTabData)m_vTabs.get(iTabIndex);
    return data.getGroupID();
  }

    /**
    * Returns the GroupNumber of the tab at the specified index
     */
  public int getGroupNumberAtTabIndex(int iTabIndex)
  {
    BrowserPaneTabData data = (BrowserPaneTabData)m_vTabs.get(iTabIndex);
    return data.getGroupNumber();
  }

    /**
    * Prints the <code>Vector</code> data. Used for Debugging only.
     */
  public void printVector()
  {
    if (m_vTabs.isEmpty())
    {
      System.out.println("Vector is empty ");
    }
    else
    { int index = 0;
      BrowserPaneTabData data = null;
      Enumeration e = m_vTabs.elements();
      System.out.println("Vector data :");
      while(e.hasMoreElements())
      {
        data = (BrowserPaneTabData)e.nextElement();
        System.out.println("index= " + index);
        System.out.print("Group Number= " + data.getGroupNumber());
        System.out.print(", Group ID= " + data.getGroupID()+"\n");
        index++;
      }
      System.out.println("");
    }
  }

  public Vector getTabData()
  {
    return m_vTabs;
  }


   /**
    * Stores the BrowserPaneTabData corresponding to each Tab in the BrowserPane.
    * When a Tab Group is added or inserted, the subsequent Tab Group Numbers
   * (if any) are incremented by 1. Similarly, when a Tab Group is removed
   * the subsequent Tab Group Numbers (if any) are decremented by 1. Therefore,
   * the Tab Group Numbers must always be in ascending order starting from 0.
   * to N-1 where N is the total number of groups.
     */
   private Vector m_vTabs;

}


