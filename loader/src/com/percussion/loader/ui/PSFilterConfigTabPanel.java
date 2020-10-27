/*[ PSFilterConfigTabPanel.java ]********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.loader.objectstore.PSExtensionDef;
import com.percussion.loader.objectstore.PSExtractorDef;
import com.percussion.loader.objectstore.PSFilter;
import com.percussion.loader.objectstore.PSMimeTypeDef;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;

/**
 * This class displays and edits the filter definition for an extractor. It is
 * typically used as one of the sub tab panel within the main configuration 
 * panel of an extractor.
 */
public class PSFilterConfigTabPanel extends PSAbstractExtractorConfigTabPanel
{


   /**
    * Constructs a new <code>PSFilterConfigTabPanel</code> object
    */
   public PSFilterConfigTabPanel()
   {
      init();
   }


   /**
    * Initializes the gui components for this panel
    */
   private void init()
   {
      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(
               getClass().getName() + "Resources", Locale.getDefault() );

      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

      // Create borders
      Border b1 = BorderFactory.createEmptyBorder(10, 10, 10, 10 );
      Border b3 = BorderFactory.createEtchedBorder();
      Border b2 = BorderFactory.createCompoundBorder(b3, b1);

      // Filter table panel
      JPanel tablePanel = new JPanel(new BorderLayout());
      tablePanel.setBorder(BorderFactory.createTitledBorder(b2,
            ms_res.getString("field.label.filters")));
      tablePanel.setPreferredSize(new Dimension(100, 100));
      tablePanel.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
      m_fieldsTableModel = new FiltersTableModel(TYPE_FIELDS);
      m_fieldsTable = new JTable(m_fieldsTableModel);
      m_fieldsTable.addKeyListener( new KeyAdapter()
      {
         // Removes selected row if delete key is hit
         public void keyReleased(KeyEvent event)
         {
           if(event.getKeyCode() == event.VK_DELETE)
           {
             m_fieldsTableModel.removeRow(m_fieldsTable);
           }
         }
      });
      JScrollPane scrollPane = new JScrollPane(m_fieldsTable);
      tablePanel.add(scrollPane, BorderLayout.CENTER);

      // Mime type table panel
      JPanel tablePanel2 = new JPanel(new BorderLayout());
      tablePanel2.setBorder(BorderFactory.createTitledBorder(b2,
            ms_res.getString("field.label.mimeTypeMap")));
      tablePanel2.setPreferredSize(new Dimension(100, 100));
      tablePanel2.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
      m_mimeTypeTableModel = new FiltersTableModel(TYPE_MIME);
      m_mimeTypeTable = new JTable(m_mimeTypeTableModel);
      m_mimeTypeTable.addKeyListener( new KeyAdapter()
      {
         // Removes selected row if delete key is hit
         public void keyReleased(KeyEvent event)
         {
           if(event.getKeyCode() == event.VK_DELETE)
           {
             m_mimeTypeTableModel.removeRow(m_mimeTypeTable);
           }
         }
      });
      JScrollPane scrollPane2 = new JScrollPane(m_mimeTypeTable);
      tablePanel2.add(scrollPane2, BorderLayout.CENTER);

      add(tablePanel);
      add(tablePanel2);

   }

   // implements IPSExtractorConfigTabPanel interface method
   public void load(PSExtractorConfigContext configCtx)
   {
      if(null == configCtx)
         throw new IllegalArgumentException(
            "Extractor Configuration context cannot be null");

      PSExtractorDef extractorDef = configCtx.getExtractorDef();
      m_fieldsTableModel.setData(
         extractorDef.getFiltersList().getComponentList());
      m_mimeTypeTableModel.setData(extractorDef.getMimeTypeList());


   }

   // implements IPSExtractorConfigTabPanel interface method
   public void save(PSExtractorConfigContext configCtx)
   {
      if(null == configCtx)
         throw new IllegalArgumentException(
            "Extractor Configuration context cannot be null");

      stopTableEditing(m_fieldsTable);
      stopTableEditing(m_mimeTypeTable);

      if(!validateContent())
         return;

      PSExtractorDef extractorDef = configCtx.getExtractorDef();
      // Update the filter list
      List filterList =
         extractorDef.getFiltersList().getComponentList();
      filterList.clear();
      Iterator filters = m_fieldsTableModel.getData();
      while(filters.hasNext())
         filterList.add(filters.next());

      // Update the mime type list
      List mimeTypeList = extractorDef.getMimeTypeList();
      mimeTypeList.clear();
      Iterator mimeTypes = m_mimeTypeTableModel.getData();
      while(mimeTypes.hasNext())
         mimeTypeList.add(mimeTypes.next());
   }


   /**
    * Table model that will represent both the the filter
    * field and mime type tables in the filter tab panel
    */
   private class FiltersTableModel extends AbstractTableModel
   {

      /**
       * Constructs a new <code>FiltersTableModel</code> object
       * that is loaded with the data passed in.
       *
       * @param type the table type:
       * <ul>
       *    <li>TYPE_FIELDS</li>
       *    <li>TYPE_MIME</li>
       * </ul>
       *
       * @param data the list of objects to be loaded in the model.
       * The object's type depends on the table model's type:
       * <table cols=2>
       *    <thead>
       *     <tr>
       *        <th>Table Type</th><th>Object Type Expected</th>
       *     </tr>
       *    </thead>
       *    <tbody>
       *    <tr>
       *       <td>TYPE_FIELDS</td><td>PSFilter</td>
       *    </tr>
       *    <tr>
       *       <td>TYPE_MIME</td><td>PSMimeTypeDef</td>
       *    </tr>
       *    </tbody>
       * </table>
       */
      public FiltersTableModel(int type, List data)
      {
         m_type = type;
         if(null != data)
            setData(data);
      }

      /**
       * Constructs a new <code>FiltersTableModel</code> object
       *
       * @param type the table type:
       * <ul>
       *    <li>TYPE_FIELDS</li>
       *    <li>TYPE_MIME</li>
       * </ul>
       */
      public FiltersTableModel(int type)
      {
         this(type, null);
      }

      // implements TableModel interface method
      public int getRowCount()
      {
         return m_data == null ? 0 : m_data.size();
      }

      // implements TableModel interface method
      public int getColumnCount()
      {
         if(m_type == TYPE_FIELDS)
         {
            return 1;
         }
         else if(m_type == TYPE_MIME)
         {
            return 2;
         }
         return 0;
      }

      // implements TableModel interface method
      public Object getValueAt(int row, int col)
      {
          if(row < 0 || row >= getRowCount())
             return null;
          FilterRow theRow = (FilterRow)m_data.get(row);
          return theRow.getColumnValue(col);
      }

      // implements TableModel interface method
      public void setValueAt(Object value, int row, int col)
      {
         if(null == value)
            throw new IllegalArgumentException("Value cannot be null");

         if(isCellEditable(row, col))
         {
          if(row < 0 || row >= getRowCount())
             return;
          FilterRow theRow = (FilterRow)m_data.get(row);
          theRow.setColumnValue((String)value, col);

         }
      }

      // implements TableModel interface method
      public boolean isCellEditable(int row, int col)
      {
         return true;
      }

      /**
       * Removes row in the table
       *
       * @param row the row index of the row to be removed
       */
      public void removeRow(JTable table)
      {
         int row = table.getSelectedRow();
         int col = table.getSelectedColumn();
         if(row < 0 || row >= getRowCount()
            || col < 0 || col >= getColumnCount())
            return;
         table.getCellEditor(row, col).stopCellEditing();

         m_data.remove(row);
         addMinimumRows();
         fireTableDataChanged();
      }

      /**
       * Returns the table model's data into the
       * @return
       */
      public Iterator getData()
      {
         List data = new ArrayList();
         Iterator it = m_data.iterator();
         if(m_type == TYPE_FIELDS)
            determineNextSequenceNumber(m_data);
         while(it.hasNext())
         {
            FilterRow row = (FilterRow)it.next();
            if(m_type == TYPE_FIELDS)
            {
               if(row.getColumnValue(COL_FIELDS).trim().length() > 0)
               {
                  String name = row.getName();
                  if(name == null || name.trim().length() == 0)
                     name = DEFAULT_FILTER_NAME + (m_sequence++);
                  data.add(new PSFilter(name, row.getColumnValue(COL_FIELDS)));
               }
            }
            else if(m_type == TYPE_MIME)
            {
               if(row.getColumnValue(COL_NAME).trim().length() > 0)
               {
                  data.add(new PSMimeTypeDef(row.getColumnValue(COL_NAME).trim(),
                     row.stringToList(row.getColumnValue(COL_EXTENSION))));
               }
            }
         }

         return data.iterator();
      }

      /**
       * Sets the data for this table model and add the
       * empty minimum required rows
       *
       * @param data the list of objects to be loaded in the model.
       * The object's type depends on the table model's type:
       * <table cols=2>
       *    <thead>
       *     <tr>
       *        <th>Table Type</th><th>Object Type Expected</th>
       *     </tr>
       *    </thead>
       *    <tbody>
       *    <tr>
       *       <td>TYPE_FIELDS</td><td>PSFilter</td>
       *    </tr>
       *    <tr>
       *       <td>TYPE_MIME</td><td>PSMimeTypeDef</td>
       *    </tr>
       *    </tbody>
       * </table>
       */
      public void setData(List data)
      {
         m_data.clear();
         Iterator it = data.iterator();
         while(it.hasNext())
         {
            Object obj = it.next();
            if(m_type == TYPE_FIELDS && obj instanceof PSFilter)
               m_data.add(new FilterRow((PSFilter)obj));
            else if(m_type == TYPE_MIME && obj instanceof PSMimeTypeDef)
            {
               m_data.add(new FilterRow((PSMimeTypeDef)obj));
            }
         }
         addMinimumRows();
         fireTableDataChanged();
      }

      /**
       * Adds empty rows so table contains the minimum rows
       * required as defined in MIN_ROWS.
       */
      private void addMinimumRows()
      {
         for(int i = m_data.size(); i < MIN_ROWS; i++)
            m_data.add(new FilterRow(m_type));
      }

      // implements TableModel interface method
      public String getColumnName(int col)
      {
        if(m_type == TYPE_FIELDS)
        {
           if(col == COL_FIELDS)
              return ms_res.getString("field.label.fields");
        }
        else if(m_type == TYPE_MIME)
        {
            if(col == COL_NAME)
               return ms_res.getString("field.label.name");
            if(col == COL_EXTENSION)
               return ms_res.getString("field.label.extensions");
        }
        return "";
      }

      /**
       * Determines next filter name sequence number from data list
       * @param data list, May be <code>null</code>.
       */
      private void determineNextSequenceNumber(List data)
      {
         if(null == data || m_type != TYPE_FIELDS)
            return;
         int seq = m_sequence;
         Iterator it = data.iterator();
         while(it.hasNext())
         {
            String name = ((FilterRow)it.next()).getName();
            if(name != null && name.startsWith(DEFAULT_FILTER_NAME))
            {
               int tempSeq = 0;
               String possibleSequence =
                  name.substring(DEFAULT_FILTER_NAME.length());
               try
               {
                  tempSeq = Integer.parseInt(possibleSequence);
                  if(tempSeq > seq)
                     seq = tempSeq + 1;
               }
               catch(NumberFormatException ignore){}

            }
         }
         m_sequence = seq;

      }

      /**
       * The model's data. Never <code>null</code>.
       */
      private List m_data = new ArrayList();

      /**
       * The model's type. Either <code>TYPE_FIELDS<code> or
       * <code>TYPE_MIME</code>.
       */
      private int m_type;

      /**
       * Next filter name sequence number
       */
      private int m_sequence = 1;

   }

   /**
    * Inner Class to represent a row in the filter tables
    */
   private class FilterRow
   {

       /**
        * Constructs an empty filter row of the specified type
        * @param type the table type:
        * <ul>
        *    <li>TYPE_FIELDS</li>
        *    <li>TYPE_MIME</li>
        * </ul>
        */
       public FilterRow(int type)
       {
          m_type = type;
          if(m_type == TYPE_FIELDS)
             m_cols = new String[1];
          else if(m_type == TYPE_MIME)
             m_cols = new String[2];
       }

       /**
        * Contructs a new filter row of <code>TYPE_MIME</code>
        * @param def the <code>PSMimeTypeDef</code>. Must not be
        * <code>null</code>.
        */
       public FilterRow(PSMimeTypeDef def)
       {
         this(TYPE_MIME);
         if(null == def)
            throw new IllegalArgumentException(
               "Mime type definition cannot be null.");

         setColumnValue(def.getName(), COL_NAME);
         setColumnValue(extsToString(def.getExtensions()), COL_EXTENSION);
       }

       /**
        * Contructs a new filter row of <code>TYPE_FIELDS</code>
        *
        * @param filter the <code>PSFilter<code> object. Must not be
        * <code>null</code>.
        */
       public FilterRow(PSFilter filter)
       {
         this(TYPE_FIELDS);
         if(null == filter)
            throw new IllegalArgumentException(
               "Filter cannot be null.");

         setColumnValue(filter.getValue(), COL_FIELDS);
         m_name = filter.getName();
       }

       /**
        * Returns the value of the specified column
        * @param col the column to return
        * @return a string value. Never <code>null</code>, may be empty.
        */
       public String getColumnValue(int col)
       {
          if(col >= 0 && col < m_cols.length)
          {
             return m_cols[col] == null ? "" : m_cols[col];
          }
          return "";
       }

       /**
        * Sets the value of the specified column to the string passed in
        * @param value the string value to set the column to, may be
        * <code>null</code>
        * @param col
        */
       public void setColumnValue(String value, int col)
       {
          if(col >= 0 && col < m_cols.length)
          {
             m_cols[col] = value;
          }
       }

       /**
        * Returns the type for this row
        * @return the model's type
        */
       public int getType()
       {
         return m_type;
       }

       /**
        * Returns the filter field's name
        * @return the name, may be <code>null</code> or empty.
        */
       public String getName()
       {
          return m_name;
       }

       /**
        * Converts an iterator of <code>PSExtensionDef</code> objects
        * into a comma delimited string.
        *
        * @param exts iterator of extension defs. May not be <code>null</code>.
        *
        * @return comma delimited string, never <code>null</code>, may be
        * empty.
        */
       public String extsToString(Iterator exts)
       {
          if(exts == null)
             return "";
          StringBuffer sb = new StringBuffer();
          while(exts.hasNext())
          {
            sb.append(((PSExtensionDef)exts.next()).getAttValue());
            if(exts.hasNext())
               sb.append(", ");
          }
          return sb.toString();
       }

       /**
        * Converts a comma delimited string to a list of
        * individual string objects.
        * @param exts comma delimited string, may be <code>null</code>.
        * @return list of String objects, never
        * <code>null</code>.
        */
       public List stringToList(String exts)
       {
          List results = new ArrayList();
          if(null == exts)
             return results;
          StringTokenizer st = new StringTokenizer(exts, ",");
          while(st.hasMoreTokens())
          {
             results.add(st.nextToken().trim());
          }

          return results;
       }

       /**
        * Array representing columns in this row. Intialized in ctor. Never
        * <code>null</code> after that.
        */
       private String[] m_cols;

       /**
        * Used to store the filter fields name if it exists.
        * May be <code>null<code> or empty.
        */
       private String m_name;

       /**
        * The row's model type
        */
       private int m_type;

   }

   /**
    * Fields table. Initialized in {@link #init()}, never
    * <code>null</code> after that.
    */
   private JTable m_fieldsTable;

   /**
    * Mime type map table. Initialized in {@link #init()}, never
    * <code>null</code> after that.
    */
   private JTable m_mimeTypeTable;

   /**
    * Table model for fields. Initialized in {@link #init()} never
    * <code>null</code> after that.
    */
   private FiltersTableModel m_fieldsTableModel;

   /**
    * Table model for mime type. Initialized in {@link #init()} never
    * <code>null</code> after that.
    */
   private FiltersTableModel m_mimeTypeTableModel;

   /**
    * Resource bundle for this class. Initialized once in {@link #init()},
    * never <code>null</code> after that.
    */
   protected static ResourceBundle ms_res;

   /**
    * Minimum table rows
    */
   protected static final int MIN_ROWS = 20;

   /**
    * Fields table model type
    */
   protected static final int TYPE_FIELDS = 1;

   /**
    * Mime type table model type
    */
   protected static final int TYPE_MIME = 2;

   /**
    * Fields table column index
    */
   protected static final int COL_FIELDS = 0;

   /**
    * Name table column index
    */
   protected static final int COL_NAME = 0;

   /**
    * Extension table column index
    */
   protected static final int COL_EXTENSION = 1;

   /**
    * Default filter name
    */
   protected static final String DEFAULT_FILTER_NAME = "Filter";
}