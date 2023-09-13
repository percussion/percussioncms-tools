/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.htmlConverter;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

/**
 * This class behaves similar to JTabbedPane, except it contains visible and
 * non-visible tabs. A user interface is provided that allows the user to show/
 * hide any tabs that were added as being hidable. <p/>
 * When using the class, you must be careful to use the correct index. When a
 * method requires an index, one of 2 possibilities exist: the index considers
 * the visible tabs only, or the index considers all tabs, visible or not. All
 * super class methods use the visible index unless noted otherwise. The description
 * for the index param will indicate what type is required for that method.
 * Hidable tabs can be added by using any of the addTab/insertTab methods. Any tab added
 * using any method to add a tab that doesn't have the hidable flag will result
 * in adding a tab that is not hidable. <p.>
 * This class does not currently support multi-threaded access.
**/
public class JOptionalTabbedPane extends JTabbedPane
{
   public JOptionalTabbedPane()
   {
      super();
   }

   public JOptionalTabbedPane( int tabPlacement )
   {
      super( tabPlacement );
   }

   /**
    * Fix bug in JTabbedPane for drag and drop.
    */
   public Component findComponentAt(Point p)
   {
      return findComponentAt(p.x, p.y);
   }

   /**
    * Fix bug in JTabbedPane for drag and drop.
    */
   public Component findComponentAt(int x, int y)
   {
      Component comp = getSelectedComponent();
      if (comp instanceof JScrollPane)
      {
         JScrollPane scroller = (JScrollPane) comp;
         return scroller.findComponentAt(x, y);
      }

      return null;
   }

   /**
    * When called, if this tabbed pane has any hidable tabs, a dialog is presented
    * to the end user. This dialog lists all hidable tabs, allowing the user to
    * check/uncheck each one to indicate whether is should be shown. <p/>
    * Tabs can also be shown/hidden programatically by calling showTab/hideTab.
   **/
   public void queryVisibleTabs()
   {
      // todo
      throw new UnsupportedOperationException();
   }

   /**
    * @return <code>true</code> if this tabbed pane has any tabs that are hidable.
   **/
   public boolean hasHidableTabs()
   {
      // walk thru list of all tabs, and see if any of them are hidable
      Enumeration e = getAllTabs().elements();
      boolean bFound = false;
      while ( e.hasMoreElements() && !bFound )
      {
         TabInfo info = (TabInfo) e.nextElement();
         bFound = info.m_hidable;
      }
      return bFound;
   }


   /**
    * If the tab with the supplied title is a hidable tab, it is added to the
    * visible tab list. If it is already visible, nothing is done. If it does not
    * reference a tab in this pane, an exception is thrown.
    *
    * @throws IllegalArgumentException if title is not the title of a tab in this
    * control.
   **/
   public void showTab( String title )
   {
      showTab( getTabInfo( title ));
   }

   /**
    * Like the method it overloads, except takes an index rather than a tab title.
   **/
   public void showTab( int index )
   {
      showTab( getTabInfo( index ));
   }

   /**
    * See the public methods for a description.
   **/
   private void showTab( TabInfo info )
   {
      if ( info.m_hidable && !isTabVisible( info.m_index ))
      {
         // figure out where to add it in the visible list
         int insertPos = info.m_index;
         for ( int tabsToCheck = insertPos-1; tabsToCheck >= 0; --tabsToCheck )
            if ( !isTabVisible( tabsToCheck ))
               --insertPos;

         TabPane page = getHiddenTab( info );
         // sanity check
         if ( null == page )
            throw new IllegalStateException( "Internal state not synchronized" );

         // remove from hidden list before adding to visible list
         getHiddenTabs().remove( page );

         // add to visible list
         super.insertTab( page.m_title, null, page.m_component, null, insertPos );
         page.transferProperties( insertPos );
      }
   }


   /**
    * If the tab with the supplied title is a hidable tab, it is removed from the
    * visible tab list. If it is already hidden, nothing is done. If it does not
    * reference a tab in this pane, an exception is thrown. If the tab is not
    * hidable, an exception is thrown.
    *
    * @throws IllegalArgumentException if title is not the title of a tab in this
    * control or the referenced tab is not hidable.
   **/
   public void hideTab( String title )
   {
      hideTab( getTabInfo( title ));
   }

   /**
    * Like the method it overloads, except takes an index rather than a tab title.
   **/
   public void hideTab( int index )
   {
      hideTab( getTabInfo( index ));
   }

   /**
    * See the public method for a description.
   **/
   private void hideTab( TabInfo info )
   {
      if ( !info.m_hidable )
         throw new IllegalArgumentException( "Tried to hide a tab that is not hidable." );

      if ( isTabVisible( info.m_index ))
      {
         int visibleIndex = indexAllToVisible( info.m_index );
         TabPane page = new TabPane( visibleIndex );
         // remove from visible tab list before adding to hidden list
         super.removeTabAt( visibleIndex );

         // now add to hidden list
         getHiddenTabs().add( page );
      }
   }

   /**
    * A tab is hidable if it was added with the hidable flag set to <code>true
    * </code>. A hidable tab can be shown/hidden by the end user.
    *
    * @return <code>true</code> if the tab with the supplied title is a hidable
    * tab, <code>false</code> otherwise.
    *
    * @throws IllegalArgumentException if title is not the title of a tab in this
    * control.
   **/
   public boolean isHidable( String title )
   {
      return isHidable( getTabInfo( title ));
   }

   /**
    * Just like the overloaded version that takes a String, except this one uses
    * indexes instead of titles.
    *
    * @param index The index of the tab to change. Should be between 0 and
    * one less than getTabCount(), inclusive.
   **/
   public boolean isHidable( int index )
   {
      return isHidable( getTabInfo( index ));
   }


   /**
    * See the public method for a description.
   **/
   private boolean isHidable( TabInfo info )
   {
      return info.m_hidable;
   }

   /**
    * @return <code>true</code> if the tab with the supplied title is currently
    * visible. <code>false</code> is returned if the tab is not visible. If the
    * tab is not a member of this control, an exception is thrown.
    *
    * @throws IllegalArgumentException if title is not the title of a tab in this
    * control.
   **/
   public boolean isTabVisible( String title )
   {
      return isTabVisible( getTabInfo( title ));
   }

   /**
    * Just like the overloaded version that takes a String, except this one uses
    * indexes instead of titles.
    *
    * @param index The index of the tab to change. Should be between 0 and
    * one less than getTabCount(), inclusive.
   **/
   public boolean isTabVisible( int index )
   {
      return isTabVisible( getTabInfo( index ));
   }


   /**
    * See the public method for a description.
    *
    * @param info The info for the tab to check. Should not be null.
   **/
   private boolean isTabVisible( TabInfo info )
   {
      TabPane page = getHiddenTab( info );
      return null == page;
   }

   /**
    * @return The index of the currently selected tab. If allTabs is <code>true
    * </code>, this index includes hidden tabs, otherwise it only includes
    * visible tabs.
   **/
   public int getSelectedIndex( boolean allTabs )
   {
      int index = super.getSelectedIndex();
      return allTabs ? indexVisibleToAll(index) : index;
   }

   public void setSelectedTab( String title )
   {
      if ( isTabVisible( title ))
      {
         TabInfo info = getTabInfo( title );
         super.setSelectedIndex( indexAllToVisible( info.m_index ));
      }
   }

   /**
    * Identical to the addTab method without the hidable parameter, except if
    * <code>hidable</code> is <code>true</code>, the user may show and hide this
    * tab. If <code>false</code>, this method is identical.
    *
    * @param hidable If <code>true</code>, the tab can be shown/hidden either by
    * the user or programatically. Use <code>queryVisibleTabs</code> to present
    * a dialog to the user that allows showing/hiding of hidable tabs.
   **/
   public void addTab( String title, Component component, boolean hidable )
   {
      insertTab( title, null, component, null, getTabCount(), hidable );
   }

   /**
    * Identical to the addTab method without the hidable parameter, except if
    * <code>hidable</code> is <code>true</code>, the user may show and hide this
    * tab. If <code>false</code>, this method is identical.
    *
    * @param hidable If <code>true</code>, the tab can be shown/hidden either by
    * the user or programatically. Use <code>queryVisibleTabs</code> to present
    * a dialog to the user that allows showing/hiding of hidable tabs.
   **/
   public void addTab( String title, Icon icon, Component component,
         boolean hidable )
   {
      insertTab( title, icon, component, null, getTabCount(), hidable );
   }

   /**
    * Identical to the addTab method without the hidable parameter, except if
    * <code>hidable</code> is <code>true</code>, the user may show and hide this
    * tab. If <code>false</code>, this method is identical.
    *
    * @param hidable If <code>true</code>, the tab can be shown/hidden either by
    * the user or programatically. Use <code>queryVisibleTabs</code> to present
    * a dialog to the user that allows showing/hiding of hidable tabs.
   **/
   public void addTab( String title, Icon icon, Component component, String tip,
         boolean hidable )
   {
      insertTab( title, icon, component, tip, getTabCount(), hidable );
   }

   /**
    * @return The total number of visible tabs. Tabs that are currently
    * hidden are not included in this count.
    *
    * @see #getTabCount
   **/
   public int getAllTabCount()
   {
      return super.getTabCount() + getHiddenTabs().size();
   }

   /**
    * Redirects the call to our override of this method.
   **/
   public void insertTab(String title, Icon icon, Component component, String tip,
         int index )
   {
      insertTab( title, icon, component, tip, indexVisibleToAll(index), false );
   }

   /**
    * Identical to the insertTab method without the hidable parameter, except if
    * <code>hidable</code> is <code>true</code>, the user may show and hide this
    * tab. If <code>false</code>, this method is identical.
    *
    * @param hidable If <code>true</code>, the tab can be shown/hidden either by
    * the user or programatically. Use <code>queryVisibleTabs</code> to present
    * a dialog to the user that allows showing/hiding of hidable tabs.
   **/
   public void insertTab(String title, Icon icon, Component component, String tip,
         int index, boolean hidable )
   {
      /* This method acts like the addImpl method for other containers. */
      TabInfo info = new TabInfo( title, hidable );
      getAllTabs().insertElementAt( info, index );
      try
      {
         super.insertTab( title, icon, component, tip, indexAllToVisible( index ));
      }
      catch ( RuntimeException e )
      {
         getAllTabs().remove( info );
         throw e;
      }
   }

   public void removeAll()
   {
      getHiddenTabs().clear();
      getAllTabs().clear();
      super.removeAll();
   }

   /**
    * Overridden to perform internal cleanup. To remove a hidden tab, use the
    * 2 param version of this method.
    *
    * @param index The index of the tab to change. Should be between 0 and
    * one less than getTabCount(), inclusive.
   **/
   public void removeTabAt(int index)
   {
      super.removeTabAt( index );
      getAllTabs().remove( indexVisibleToAll(index));
   }

   /**
    * Use this method to remove a tab, whether hidden or not.
    *
    * @param Index of the tab to remove. If the includeHidden flag is <code>true
    * </code>, then this index will be assumed to be relative to all tabs, otherwise
    * it will be interpreted relative to visible tabs only.
   **/
   public void removeTabAt( int index, boolean includeHidden )
   {
      throw new UnsupportedOperationException();
   }

   /**
    *
   **/
   public void setSelectedComponent(Component c)
   {
      // todo
      throw new UnsupportedOperationException();
   }

   /**
    * Translates the supplied index that references any tab to the
    * index value of that tab within the visible tab list. The converted value
    * can be passed directly to any super class method that requires an index.
    *
    * @return If the tab is visible, the adjusted index (between 0 and
    * getVisibleTabCount()-1, inclusive). If the tab is not visible, -1 is returned.
    * If the index does not refer to any tab, an exception is thrown.
    *
    * @see #indexVisibleToAll
   **/
   private int indexAllToVisible( int index )
   {
      // deal with special cases
      boolean isVisible = isTabVisible( index );
      if ( 0 == index || !isVisible )
         return isVisible ? index : -1;

      /* count how many tabs with a lower index are not visible, then use this
         value to adjust the index */
      int hiddenTabs = 0;
      for ( int i = index-1; i >= 0; --i )
         if ( !isTabVisible(i))
            ++hiddenTabs;
      return index - hiddenTabs;
   }

   /**
    * Translates the supplied index that references a tab in the visible tab
    * array to the index value of that tab within the complete tab list. This
    * method undoes the conversion done by <code>indexAllToVisible</code>.
    *
    * @return If the tab is visible, the adjusted index (between 0 and
    * getTabCount()-1, inclusive). If the index does not refer to any tab,
    * an exception is thrown.
    *
    * @see #indexAllToVisible
   **/
   private int indexVisibleToAll( int index )
   {
      /* count how many tabs with a lower index are not visible, then use this
         value to adjust the index */
      int hiddenTabs = 0;
      for ( int i = index-1; i >= 0; --i )
         if ( !isTabVisible(i))
            ++hiddenTabs;
      return index + hiddenTabs;
   }


   /**
    * @return The TabInfo object for the tab that has the specified title.
    *
    * @throws IllegalArgumentException if there is no tab containing the
    * specified title
   **/
   private TabInfo getTabInfo( String title )
   {
      boolean bFound = false;
      TabInfo info = null;
      int count = getAllTabs().size();
      int i = 0;
      for ( ; i < count && !bFound; ++i )
      {
         info = getTabInfo(i);
         if ( info.m_title.equals( title ))
            bFound = true;
      }

      if ( null == info || !bFound )
         throw new IllegalArgumentException( "No tab named \"" + title + "\" could be found" );

      return info;
   }

   /**
    * @return The TabInfo object for the tab that has the specified index.
    *
    * @throws IndexOutOfBoundsException if index is not between 0 and
    * getAllTabCount() inclusive.
   **/
   private TabInfo getTabInfo( int index )
   {
      validateIndex( index );
      TabInfo info = (TabInfo) getAllTabs().get( index );
      info.m_index = index;
      if ( null == info )
         throw new IllegalStateException( "Internal error: TabInfo unexpectedly null." );
      return info;
   }

   /**
    * Verifies that the supplied index is valid on this object. If it is, the
    * method just returns, otherwise, an exception is thrown.
    *
    * @throws IndexOutOfBoundsExceptino if index does not refer to a valid tab.
   **/
   private void validateIndex( int index )
   {
      if ( index < 0 || index > getAllTabCount())
         throw new IndexOutOfBoundsException( "Index = " + index + ": should be between 0 and "
            + getAllTabCount() + "." );
   }

   /**
    * @return If the page described by the supplied info is currently hidden, it is returned.
    * Otherwise, null is returned.
    *
    * @throws NullPointerException if info is null
   **/
   private TabPane getHiddenTab( TabInfo info )
   {
      int count = getHiddenTabs().size();
      TabPane page = null;
      boolean bFound = false;
      for ( int i = 0; i < count && !bFound; ++i )
      {
         page = (TabPane) getHiddenTabs().get( i );
         if ( page.m_title.equals( info.m_title ))
            bFound = true;
      }
      return bFound ? page : null;
   }

   /**
    * A helper class to maintain page properties of a hidden page. Hidden pages
    * are removed from the tabbed pane, with their properties stored in this
    * object. When the object is made visible again, a new tab is added and the
    * properties are transferred from this object to the newly created pane.
    * If the properties of a hidden pane are changed, they are changed inside
    * this object and will "take effect" when the page is made visible again.
   **/
   private class TabPane
   {
      // this is a quick and dirty class, so I'm not adding assessors/mutators
      public Component m_component;
      public String m_title;
      public Color m_foreground;
      public Color m_background;
      public Icon m_icon;
      public Icon m_disabledIcon;
      public String m_tip;
      public boolean m_enabled;

      /**
       * Extracts all needed properties of the page located at the tab index
       * in the tabbed pane.
       *
       * @param index The visible tab index of the page that will be the source of
       * these properties.
      **/
      public TabPane( int index )
      {
         JTabbedPane owner = JOptionalTabbedPane.this;

         m_component = owner.getComponentAt( index );
         m_title = owner.getTitleAt( index );
         m_foreground = owner.getForegroundAt( index );
         m_background = owner.getBackgroundAt( index );
         m_icon = owner.getIconAt( index );
         m_disabledIcon = owner.getDisabledIconAt( index );
//todo         m_tip = owner.getTip( index );
         m_enabled = owner.isEnabledAt( index );
      }

      /**
       * Transfers all the properties of this object to the pane located at the
       * tab index in the supplied owner, except m_component and m_title and m_tip
       * because these are always set in the addTab() methods.
       *
       * @param index The visible index of the pane to retrieve these
       * properties.
      **/
      public void transferProperties( int index )
      {
         JTabbedPane owner = JOptionalTabbedPane.this;

         owner.setForegroundAt( index, m_foreground );
         owner.setBackgroundAt( index, m_background );
         owner.setIconAt( index, m_icon );
         owner.setDisabledIconAt( index, m_disabledIcon );
         owner.setEnabledAt( index, m_enabled );
      }
   }

   /**
    * A small helper class that contains the tab index and hidable property for
    * each tab.
   **/
   private class TabInfo
   {
      /**
       * The index of this tab among all tabs, visible or hidden.
      **/
      public int m_index = 0;

      /**
       * Is this tab hidable. In other words, can the end user/programmer show/hide
       * this tab w/o removing/adding it to the control
      **/
      public boolean m_hidable = false;

      public String m_title;

      /**
       * Like the 3 arg ctor, except the hidable flag is defaulted to <code>
       * false</code>.
      **/
      public TabInfo( String title )
      {
         this( title, false );
      }

      /**
       * All properties of this object are set by the passed in params.
      **/
      public TabInfo( String title, boolean hidable )
      {
         m_title = title;
         m_index = -1;
         m_hidable = hidable;
      }
   }

   // Variables
   /**
    * This list contains a TabInfo obj for every tab that is currently owned by this
    * pane. The number of entries in this list should equal hiddenTabs.size() +
    * getVisibleTabCount(). The m_index property of each TabInfo object should not
    * be used unless the object was obtained via a call to getTabInfo. The objects
    * in the vector will move around w/o the internal indexes being updated. The
    * real index is always known by the position of the object within the vector.
   **/
   private Vector getAllTabs()
   {
      /* We have this method instead of initing the variable where it is defined
         because methods in this class that override the superclass may be called
         before static initialization is complete (if these methods are called
         in the ctor of the super) */
      if ( null == m_allTabs )
         m_allTabs = new Vector(10);
      return m_allTabs;
   }
   private Vector m_allTabs;// = new Vector(10);

   /**
    * Contains the list of all hidable tabs that are currently hidden. This list
    * plus the list of tabs contained by the super class contain all the tabs that
    * are currently part of this tabbed pane. The objects in this list must be
    * TabPane objects.
   **/
   private Vector getHiddenTabs()
   {
      if ( null == m_hiddenTabs )
         m_hiddenTabs = new Vector(5);
      return m_hiddenTabs;
   }
   private Vector m_hiddenTabs;
}

