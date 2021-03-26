/*[ ConnectionPropsPanel.java ]*************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.DatasourceComboBox;
import com.percussion.E2Designer.IConnectionSource;
import com.percussion.E2Designer.SqlCataloger;
import com.percussion.E2Designer.StringConstraint;
import com.percussion.conn.PSServerException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.UTComponents.UTFixedLabel;
import com.percussion.guitools.PSPropertyPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * Implements a panel that contains controls for editing all the information
 * needed to fully define a table, including driver, server, database, owner,
 * table name, user Id and password. All controls have cataloging built in.
 * The panel is designed to part of a larger dialog.
 */
public class ConnectionPropsPanel extends PSPropertyPanel
{
   /**
    * Creates a new panel that contains all fields necessary to connect to a
    * DBMS table, including optional user id and password fields. These fields
    * are optionally surrounded by a group-box (titled border).
    *
    * @param title The text of the group box. If <code>null</code>, no group
    *    box is shown.
    * @param connSrc An object that can return a Rx Designer connection. Used
    *    for cataloging drop-down lists. If <code>null</code>, cataloging is 
    *    disabled.
    * @param resources A valid resource that contains the strings needed by
    *    this panel.
    */
   public ConnectionPropsPanel(String title, 
      IConnectionSource connSrc, ResourceBundle resources)
   {
      if (null == resources)
         throw new IllegalArgumentException("must supply a resource bundle");
         
      m_res = resources;
      m_connectionSource = connSrc;
      initUI(title);
      initCataloging();
   }

   /**
    * Adds all of the validation objects to the supplied lists for all
    * components in this class that need validation. The new entries are added
    * to the end of the list. Each constraint results in 1 entry in comps and
    * its corresponding entry in validators.
    *
    * @param comps The list of components to modify.
    *
    * @param validators The list of validation constrain objects to modify.
    */
   public void getValidationInfo(Collection comps, Collection validators)
   {
      StringConstraint nonEmptyString = new StringConstraint();

      comps.add(m_tables);
      validators.add(nonEmptyString);
   }


   /**
    * Catalogs for column names based on all the info entered in the other
    * fields.
    *
    * @param force If <code>true</code>, will not use cached values. Otherwise,
    * if nothing has changed, will return the same vector that was previously
    * returned if no changes have been detected.
    *
    * @return A vector w/ 1 or more elements. If a problem occurs, the vector
    * will have 2 elements, the first one an empty string and the 2nd an
    * error msg describing the problem. If successful, no elements will be
    * empty strings.
    */
   public Vector catalogColumns( boolean force )
   {
      String datasource = getDatasourceChoice();
      String table = getTableChoice();

      String [] keys = { DATASOURCE_KEY, TABLE_KEY};
      String [] values = { datasource, table };
      if (!isCacheDirty(keys, values, m_columnCatCache ) && !force)
         return m_columnCatalog;
      SqlCataloger cat = new SqlCataloger(datasource, table);
      cat.setConnectionInfo(
         m_connectionSource.getDesignerConnection( false ));
      m_columnCatalog = getCatalog( cat, null );
      for ( int i = 0; i < keys.length; ++i )
         m_columnCatCache.put( keys[i], values[i] );
      
      return m_columnCatalog;
   }


   /**
    * Clears all controls. The dialog behaves as if it was just created after
    * this call.
    */
   public void reset()
   {
      /* We don't need to clear all the drop-down lists because that will
         happen automatically the next time the user tries to show the list */
      setDatasourceChoice(null);
   }


   /**
    * Create a panel using a 2x5 grid array with labels in col 1 and combo
    * boxes in column 2.
    *
    * @param title The title for this group of controls. If not <code>null
    * </code>, a border is placed around the panel, with this string in the
    * top part of the border. If <code>null</code>, no border is created.
    */
   private void initUI(String title)
   {
      if (title != null)
         setBorder(new TitledBorder(
            new EtchedBorder(EtchedBorder.LOWERED), title));
      else
         setBorder(new EmptyBorder(10, 10, 10, 10));

      m_datasources = new DatasourceComboBox(null,
         m_connectionSource.getDesignerConnection(false));
      
      addPropertyRow("Datasource:", new JComponent[] { m_datasources },
                      m_datasources, 'D', "");
      m_tables.setEditable(true);
      addPropertyRow("Table:", new JComponent[] { m_tables },
                      m_tables, 'l', "");
   }
   
   /**
    * Creates a box set up with a label that is right justified, with glue
    * on the left side. The supplied component is used as the component that
    * this label is being used for.
    *
    * @param text The text for the label.
    *
    * @param labelFor The control that this label describes.
    */
   public static Box createLabel(String text, JComponent labelFor)
   {
      UTFixedLabel label = new UTFixedLabel(text, SwingConstants.RIGHT);
      if (null != labelFor)
         label.setLabelFor(labelFor);
         
      Box panel = Box.createHorizontalBox();
      panel.add(Box.createHorizontalGlue());
      panel.add(label);
      
      return panel;
   }

   /**
    * Adds listeners to all controls so that cataloging will be performed as
    * needed. This method should only be called if m_connectionSource is valid.
    *
    * @throws IllegalStateException If m_connectionSource is <code>null</code>.
    */
   private void initCataloging()
   {
      if (null == m_connectionSource)
         throw new IllegalStateException(
            "to init cataloging, connection can't be null");

      m_tables.getEditor().getEditorComponent().addFocusListener(new FocusAdapter()
      {
         public void focusGained( FocusEvent e )
         {
            ConnectionPropsPanel.this.catalogTables( false );
         }
      });
   }
   

   /**
    * Accessor method for the datasource property.
    *
    * @return The user selected/entered value for the datasource if there is one
    * or the empty string.
    */
   public String getDatasourceChoice()
   {
      return m_datasources.getSelectedDatasource();
   }

   /**
    * Presets the name of the datasource in the combo box. If not called, the 
    * first entry in the cataloged list is displayed.
    *
    * @param name The name of the datasource to use. If <code>null</code>, 
    * clears the field.
    */
   public void setDatasourceChoice( String name )
   {
      m_datasources.setSelectedDatasource(name);
   }

   /**
    * Accessor method for the table property.
    *
    * @return The user selected/entered value for the table, if there is one,
    * or the empty string.
    */
   public String getTableChoice()
   {
      return getChoice( m_tables );
   }

   /**
    * Sets the text in the table name editor control. If <code>null</code>, the
    * field is cleared.
    *
    * @param name The text to set. Use <code>null</code> to clear the field.
    */
   public void setTableChoice( String name )
   {
      setChoice( m_tables, name );
   }

   /**
    * Gets the selected item from the supplied combobox. If no item is selected,
    * an empty string is returned.
    *
    * @param cb A valid combo box that contains 0 or more objects.
    *
    * @return The currently selected item, or "" if no item is selected.
    *
    * @throws IllegalArgumentException if cb is <code>null</code>
    */
   private String getChoice( JComboBox cb )
   {
      if ( null == cb )
         throw new IllegalArgumentException( "combo box can't be null" );
      String item = (String) cb.getSelectedItem();
      return null != item ? item.toString() : "";
   }

   /**
    * Gets the selected item from the supplied textfield.
    *
    * @param tf A valid text field to extract text from.
    *
    * @return The text in the field, or "" if no text is present.
    *
    * @throws IllegalArgumentException if tf is <code>null</code>
    */
   private String getChoice( JTextField tf )
   {
      if ( null == tf )
         throw new IllegalArgumentException( "text field can't be null" );
      String text = tf.getText();
      return null != text ? text : "";
   }

   /**
    * If name is in the combobox, it is made the current item, otherwise name
    * becomes the new editable text. If name is <code>null</code>, the current
    * item is cleared.
    *
    * @param cb A valid combo box.
    *
    * @param name The string to select into the combo box. If <code>null</code>,
    * returns immediately with no action taken.
    *
    * @throws IllegalArgumentException If cb is <code>null</code>;
    */
   private void setChoice( JComboBox cb, String name )
   {
      if ( null == cb )
         throw new IllegalArgumentException( "combo box can't be null" );

      if ( null == name )
         name = "";
         
      cb.setSelectedItem(name);
   }

   /**
    * If name is not <code>null</code>, it becomes the text fields data. If
    * <code>null</code>, the empty string is made the current data.
    *
    * @param tf A valid text control.
    *
    * @param name The string to select into the control. If <code>null</code>,
    * returns immediately with no action taken.
    *
    * @throws IllegalArgumentException If tf is <code>null</code>;
    */
   private void setChoice( JTextField tf, String name )
   {
      if ( null == tf )
         throw new IllegalArgumentException( "text field can't be null" );

      if ( null == name )
         name = "";

      tf.setText( name );
   }

   /* Cataloging methods. There is a catalog method for each combo box. Each
      time a list box is dropped, the method is called and it checks if any
      information has changed. The method reads needed info from other
      controls, then, if sufficient info is available, performs the catalog,
      and sets all of the values into the appropriate list box.  */

   /**
    * Removes all entries from the list associated w/ control, then adds all
    * the entries in listItems.
    *
    * @param listItems The list of values to add. If <code>null</code>, no
    * entries are added.
    *
    * @param control A valid combo box whose drop list will be filled w/ the
    * values in listItems.
    *
    * @throws IllegalArgumentException if control is <code>null</code>.
    */
   private void updateList( Iterator listItems, JComboBox control )
   {
      if ( null == control )
         throw new IllegalArgumentException( "control can't be null" );

      /* We need the following check because of a bug in 1.2. If no items in
         list, a NullPointerException will result */
      if ( control.getItemCount() > 0 )
         control.removeAllItems();
      if ( null != listItems )
      {
         while ( listItems.hasNext())
            control.addItem( listItems.next());
      }
   }


   /**
    * Checks the supplied cache to determine if the supplied key/value pairs
    * match the supplied cache. The algorithm is as follows: for each key,
    * if the key exists in the map and doesn't match the supplied value, or
    * the key is not in the map, the cache is considered dirty.
    *
    * @param keys A non-<code>null</code>, array of hash keys, checked against
    * keys in cache.
    *
    * @param values A non-<code>null</code>, array of hash values, check against
    * the values in cache. The size of this array must match the size of the
    * keys array.
    *
    * @param cache A valid map containing 0 or more entries. The entries are checked
    * against the supplied keys and values.
    *
    * @return <code>true</code> if the supplied keys and values don't match
    * the key/value pairs in cache.
    *
    * @throws IllegalArgumentException If keys, values or cache is <code>null
    * </code> or if the length of keys does not match the length of values.
    */
   private boolean isCacheDirty( String [] keys, String [] values,
      Map cache )
   {
      if ( null == keys || null == values || null == cache )
         throw new IllegalArgumentException( "a supplied param is null" );
      if ( keys.length != values.length )
         throw new IllegalArgumentException( "mismatch in key/value array size" );

      boolean dirty = false;
      for ( int i = 0; i < keys.length && !dirty; ++i )
      {
         Object o = cache.get( keys[i] );
         if ( null != o )
            dirty = !o.equals( values[i] );
         else
            dirty = true;
      }
      return dirty;
   }


   /**
    * Rather than having each catalog method call the SqlCataloger directly
    * and handle all the errors independently, we centralize the error
    * handling here. Always returns a vector of at least 1 element. The entries
    * are either a valid catalog, or an error message. If an error msg is
    * being returned, an empty string is placed before the error msg. We do
    * this because the behavior of the combo-box is to automatically choose
    * the first entry in the list, which we don't want it to do if it is an
    * error. This can also be used to determine if the cataloging was successful.
    *
    * @param cat A valid cataloger ready to catalog.
    *
    * @param type one of the allowed types for cataloging. See {@link
    * SqlCataloger#getCatalog( String, boolean) getCatalog} for details.
    *
    * @return A vector of 1 or more entries.
    */
   private Vector getCatalog( SqlCataloger cat, String type )
   {
      Vector entries = null;
      String errMsg = null;
      try
      {
         if ( null != type )
            entries = cat.getCatalog( type, true );
         else
            entries = cat.getCatalog();
         if ( null == entries )
         {
            errMsg = getString( "catalogFailed" );
         }
         else if ( entries.size() == 0 )
            errMsg = getString( "noEntries" );
      }
      catch ( IOException e )
      {
         errMsg = e.getLocalizedMessage();
      }
      catch ( PSAuthorizationException e )
      {
         errMsg = e.getLocalizedMessage();
      }
      catch ( PSAuthenticationFailedException e )
      {
         errMsg = e.getLocalizedMessage();
      }
      catch ( PSServerException e )
      {
         errMsg = e.getLocalizedMessage();
      }
      if ( null != errMsg )
      {
         entries = new Vector(2);
         entries.add( "" );
         entries.add( errMsg );
      }
      return entries;
   }

   /**
    * Catalogs all tables based on information the user has entered in other
    * fields and fills in the drop list of the tables combo box.
    * <p>If any errors occur, the error text will appear in the drop list.
    *
    * @param force If <code>false</code>, if no changes are detected, then
    * the drop list is not modified in any way. If <code>true</code>, a
    * catalog is performed and the drop list is modified.
    */
   private void catalogTables( boolean force )
   {
      String datasource = getDatasourceChoice();

      String [] keys = { DATASOURCE_KEY};
      String [] values = { datasource };
      if ( !isCacheDirty( keys, values, m_tableCatCache ) && !force)
         return;
      SqlCataloger cat = new SqlCataloger( datasource );
      cat.setConnectionInfo( m_connectionSource.getDesignerConnection(false) );
      Vector tables = getCatalog( cat, null );
      for ( int i = 0; i < keys.length; ++i )
         m_tableCatCache.put( keys[i], values[i] );
      
      updateList( tables.iterator(), m_tables );
   }

   /**
    * Finds a string in this dialog&apos;s resource bundle based on the supplied
    * key and returns it. If it can't be found, an exception is thrown.
    *
    * @param key The key of the string to return. Must not be <code>null</code>
    * or empty.
    *
    * @throws IllegalArgumentException if key is <code>null</code> or empty.
    *
    * @throws MissingResourceException if key can't be found in the resource
    * bundle
    */
   private String getString( String key )
   {
      if ( null == key || key.trim().length() == 0 )
         throw new IllegalArgumentException( "key can't be null or empty" );

      return m_res.getString( key );
   }

   /**
    * The source for all Strings used by this panel. Set at beginning of ctor.
    * Always valid. Should be accessed via the <code>getString</code> method.
    */
   private ResourceBundle m_res;

   /** The edit control for the datasource name entry. */
   private DatasourceComboBox m_datasources;
   
   /**
    * The parameters used in the last attempted catalog of tables are stored
    * here. The entries use the ..._KEY values as keys and the actual value
    * used to catalog as the value. This is always valid, but may be empty.
    */
   private HashMap m_tableCatCache = new HashMap();
   /** The edit control for the table name entry. */
   private JComboBox m_tables = new JComboBox();

   /**
    * An object that can provider a connection to an Rx server, or <code>null
    * </code> if cataloging is not supported.
    */
   private IConnectionSource m_connectionSource = null;
   /**
    * The parameters used in the last attempted catalog of columns are stored
    * here. The entries use the ..._KEY values as keys and the actual value
    * used to catalog as the value. This is always valid, but may be empty.
    */
   private HashMap m_columnCatCache = new HashMap();
   /**
    * The columns from the last column catalog. If no change is found in any
    * of the params used to catalog the last time, this vector is returned.
    */
   private Vector m_columnCatalog = null;

   /**
    * The key in the catalog cache hash maps that contains the driver used
    * for the last catalog. The entry is a non-<code>null</code>, non-empty
    * String, if present.
    */
   private static final String DATASOURCE_KEY = "ds";

   /**
    * The key in the catalog cache hash maps that contains the table used
    * for the last catalog. The entry is a non-<code>null</code> String, if
    * present.
    */
   private static final String TABLE_KEY = "tbl";
}
