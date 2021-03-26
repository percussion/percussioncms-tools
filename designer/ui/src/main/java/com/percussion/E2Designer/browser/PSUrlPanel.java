/*[ PSUrlPanel.java ]***********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.cms.objectstore.PSSearch;
import com.percussion.guitools.PropertyTablePanel;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

/**
 * Creates a panel with a text area to edit the url body and a table to edit
 * the url query.
 */
public class PSUrlPanel extends JPanel implements CellEditorListener
{
   /**
    * Constructs a new PSUrlPanel using the provided context parameters list.
    *
    * @param automaticParameters Iterator of automatic parameters for the value
    *    drop down as <code>String</code>. Must not be <code>null</code>,
    *    may be empty.
    * @param emptyUrlAllowed if set to <code>true</code> then we will allow
    * zero length url string
    */
   public PSUrlPanel(Iterator automaticParameters, boolean emptyUrlAllowed)
   {
      if (automaticParameters == null)
         throw new IllegalArgumentException("automaticParameters cannot be null");

      m_automaticParameters = new ArrayList();

      while (automaticParameters.hasNext())
         m_automaticParameters.add(automaticParameters.next());
      m_emptyUrlAllowed = emptyUrlAllowed;

      initPanel();
   }

   /**
    * Constructs a new PSUrlPanel using the provided url string, data and
    * context parameters list.
    *
    * @param url the url that this panel represents. May be <code>null</code>
    *    or empty.
    * @param data map of parameters as name/value pairs of <code>String</code>
    *    objects. May be <code>null</code> or empty.
    * @param automaticParameters Iterator of context parameters for the value
    *    drop down. Must not be <code>null</code>, may be empty.
    * @param emptyUrlAllowed if set to <code>true</code> then we will allow
    * zero length url string
    */
   public PSUrlPanel(String url, Map data, Iterator automaticParameters,
       boolean emptyUrlAllowed)
   {
      this(automaticParameters, emptyUrlAllowed);

      setUrl(url, data);
   }

   /**
    * Creates the panel's gui.
    */
   private void initPanel()
   {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      setBorder(BorderFactory.createTitledBorder(
         ms_res.getString("border.url")));

      add(createUrlPanel());
      add(createParametersPanel());
      initCellEditors();
   }

   /**
    * Create the url panel UI.
    *
    * @return the url panel, never <code>null</code>.
    */
   private JPanel createUrlPanel()
   {
      JPanel panel = new JPanel();
      panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

      m_url = new JTextArea();
      m_url.setLineWrap(true);
      m_url.setWrapStyleWord(true);
      m_url.setEditable(true);

      JScrollPane scrollPane = new JScrollPane(m_url);
      scrollPane.setPreferredSize(new Dimension(400, 50));

      panel.add(scrollPane);

      return panel;
   }

   /**
    * Create the parameter panel UI.
    *
    * @return the parameters panel, never <code>null</code>.
    */
   private JPanel createParametersPanel()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
      panel.setBorder(BorderFactory.createTitledBorder(
         ms_res.getString("border.parameters")));

      String[] headers =
      {
         ms_res.getString("col.name"),
         ms_res.getString("col.value")
      };
      m_parameters = new TablePanel(headers, 0, true);
      m_parameters.setMinimumSize(new Dimension(0, 120));

      JTable table = m_parameters.getTable();

      JComboBox cellEditor = new JComboBox(m_automaticParameters.toArray());
      cellEditor.setEditable(true);
      table.getColumnModel().getColumn(PropertyTablePanel.VALUE_COLUMN)
            .setCellEditor(new DefaultCellEditor(cellEditor));

      panel.add(m_parameters);

      return panel;
   }

   private void initCellEditors()
   {
      JTable table = m_parameters.getTable();
      for (int i=0; i<table.getColumnCount(); i++)
      {
         TableCellEditor editor = table.getCellEditor(0, i);
         editor.addCellEditorListener(this);
      }
   }

   // implements CellEditorListener
   public void editingCanceled(ChangeEvent e)
   {
      if (e.getSource() instanceof DefaultCellEditor)
      {
         DefaultCellEditor editor = (DefaultCellEditor) e.getSource();
         editor.cancelCellEditing();
      }
   }

   // implements CellEditorListener
   public void editingStopped(ChangeEvent e)
   {
      // no-op
   }

   /**
    * Get the url file string.
    *
    * @return the url file, never <code>null</code>, may be empty.
    */
   public String getUrlFile()
   {
      return m_url.getText();
   }

   /**
    * Get the url query string.
    *
    * @return the url query string, never <code>null</code>, may be empty.
    */
   public String getQuery()
   {
      String query = "";

      Map params = getParameters();

      Iterator parameters = params.keySet().iterator();
      while (parameters.hasNext())
      {
         String name = (String) parameters.next();
         String value = (String) params.get(name);
         query += name + "=" + value;

         if (parameters.hasNext())
            query += "&";
      }

      return query;
   }

   /**
    * Get the full url string.
    *
    * @return the url string, never <code>null</code>, may be empty.
    */
   public String getUrl()
   {
      String url = getUrlFile();
      String query = getQuery();
      if (query.length() == 0)
         return url;

      return url + "?" + query;
   }

   /**
    * Set the url panel for the supplied url and parameters. If the supplied
    * url string has parameters attached, the will be removed and added to the
    * parameters map.
    *
    * @param url the url to set, may be <code>null</code> or empty.
    * @param params the parameters to set, may be <code>null</code> or empty.
    */
   public void setUrl(String url, Map params)
   {
      if (url == null)
         url = "";

      if (params == null)
         params = new TreeMap();

      // make sure that they supplied a well formed url
      if (!isValid(url, -1))
      {
         Object[] args =
         {
            url
         };
         String message = MessageFormat.format(
            ms_res.getString("error.msg.invalidurl"), args);
         throw new RuntimeException(message);
      }

      // separate url file and query
      PSSearch.parseParameters(url, params);
      int pos = url.indexOf('?');
      if (pos >= 0)
         url = url.substring(0, pos);

      m_url.setText(url);
      m_parameters.setData(params);
   }

   /**
    * Returns the map of parameter name/value pairs.
    *
    * @return map of parameters. Never <code>null</code>, may be empty.
    */
   public Map getParameters()
   {
      return m_parameters.getData();
   }

   /**
    * Sets the supplied url parameters.
    *
    * @param params a map of parameter name/value pairs. May be
    *    <code>null</code> or empty.
    */
   public void setParameters(Map params)
   {
      m_parameters.setData(params);
   }

   /**
    * Test if the url is well formed, not longer then the supplied maximum
    * length and not empty.
    *
    * @param url the url to validate, not <code>null</code>, may be empty.
    * @param max the maximum number of characters allowed, -1 if unlimited.
    * @return <code>true</code> if it isvalid, <code>false</code> otherwise.
    */
   public boolean isValid(String url, int max)
   {
      if (url == null)
         throw new IllegalArgumentException("url cannot be null");

      // the url cannot be empty if empty is not allowed
      if (!m_emptyUrlAllowed && url.trim().length() == 0)
         return false;

      // the url must be smaller then the maximal defined length
      if (max != -1 && url.length() > max)
         return false;

      // the url must be well formed
      try
      {
         /**
          * If a protocol is supplied we use it, otherwise we add the file
          * protocol for our test only.
          */
         if (url.indexOf(':') >= 0)
            new URL(url);
         else
            new URL("file:" + url);

         return true;
      }
      catch (MalformedURLException e)
      {
         return false;
      }
   }

   /**
    * An inner class to override the <code>PropertyTablePanel</code> classes
    * methods <code>getData</code> and <code>setData</code>. See base class
    * for additional info.
    */
   private class TablePanel extends PropertyTablePanel
   {
      /**
       * Constructs a new table panel for the supplied headers and number of
       * rows.
       *
       * @param headers the table headers, not <code>null</code>, must be of
       *    length 2.
       * @param rows the number of rows to create initially.
       * @param isEditable <code>true</code> to make the table editable,
       *    <code>false</code> otherwise.
       */
      public TablePanel(String[] headers, int rows, boolean isEditable)
      {
         super(headers, rows, isEditable);
         setScrollPaneSize(new Dimension(0, 100));
      }
   }

   /**
    * The url represented by this panel. Initialized in the ctor.
    * May be <code>null</code>.
    */
   private JTextArea m_url = null;

   /**
    * The list of automatic parameters for the value's drop down. Initialized in
    * the ctor. Never <code>null</code> or changed after that. May be empty.
    */
   private List m_automaticParameters = null;

   /**
    *  The parameters table panel. Initialized in {@link #initPanel()},
    *  never <code>null</code> after that.
    */
   private TablePanel m_parameters = null;

   /**
    * Flag indicating that url must not be empty to be considered valid.
    * Defaults to <code>false</code>.
    */
   private boolean m_emptyUrlAllowed = false;

   /**
    * Resource bundle for this class. Never <code>null</code>, equivalent to a
    * variable declared final.
    */
   private static ResourceBundle ms_res = null;
   static
   {
      ms_res = ResourceBundle.getBundle(PSUrlPanel.class.getName() + "Resources",
         Locale.getDefault());
   }
}