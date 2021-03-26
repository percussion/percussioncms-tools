/******************************************************************************
 *
 * [ LuceneExtensionPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.CatalogExtensionCatalogHandler;
import com.percussion.E2Designer.CatalogLocales;
import com.percussion.E2Designer.CatalogMimeTypes;
import com.percussion.E2Designer.CatalogServerExits;
import com.percussion.conn.PSDesignerConnection;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSSearchConfig;
import com.percussion.extension.PSExtensionDef;
import com.percussion.extension.PSExtensionRef;
import com.percussion.util.PSStringComparator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * Lucene extension panel.  This panel includes a two column table used to edit
 * lucene extension information, including analyzers and text converters.
 */
public class LuceneExtensionPanel extends JPanel implements ITabDataHelper
{
   /**
    * Ctor.
    * @param config search configuration, never <code>null</code>.
    * @param iface the interface implemented by the set of extensions which will
    * be edited, must be either {@link #ANALYZER_INTERFACE_NAME} or
    * {@link #EXTRACTOR_INTERFACE_NAME}.
    * @param col1Title title to be used for the first column of the table
    * control, may not be <code>null</code> or empty.
    * @param col2Title title to be used for the second column of the table
    * control, may not be <code>null</code> or empty.
    */
   @SuppressWarnings("unchecked")
   public LuceneExtensionPanel(PSSearchConfig config, String iface, 
         String col1Title, String col2Title)
   {
      if (config == null)
         throw new IllegalArgumentException("config may not be null");
      
      if (iface == null)
         throw new IllegalArgumentException("iface may not be null");
      
      if (col1Title == null || col1Title.trim().length() == 0)
         throw new IllegalArgumentException(
               "col1Title may not be null or empty");

      if (col2Title == null || col2Title.trim().length() == 0)
         throw new IllegalArgumentException(
               "col2Title may not be null or empty");
      
      this.setBorder(new EmptyBorder(5, 5, 5, 5));
     
      //receive designer connection to the server
      ms_conn = AppletMainDialog.getServerConnection().getConnection();
      
      if (iface.equals(ANALYZER_INTERFACE_NAME))
      {
         m_extensions = new HashMap<String, PSExtensionCall>(config.
               getAnalyzers());
         m_properties = catalogLocaleNames();
         m_interface = ANALYZER_INTERFACE_NAME;  
      }
      else if (iface.equals(EXTRACTOR_INTERFACE_NAME))
      {
         m_extensions = new HashMap<String, PSExtensionCall>(config.
               getTextConverters());
         m_properties = catalogMimeTypeNames();
         m_interface = EXTRACTOR_INTERFACE_NAME; 
      }
      else
         throw new IllegalArgumentException("invalid interface " + iface);
   
      m_col1Title = col1Title;
      m_col2Title = col2Title; 
           
      initPanel();
   }

   /**
    * See ITabDataHelper iface.
    * @return If the data within this tab has changed, so we updated the data
    * and let whoever is interested in this information know by returning
    * <CODE>true</CODE>. <CODE>false</CODE> is returned if nothing has changed.
    */
   public boolean saveTabData()
   {
      boolean hasChanged = m_tableChanged;
      m_tableChanged = false;

      if (hasChanged)
      {
         //save table data
         int rows = m_tableModel.getRowCount();
         m_extensions.clear();

         for (int row = 0; row < rows; row++)
         {
            String prop = (String) m_tableModel.getValueAt(row, COL_PROPERTY);
            if (prop == null || prop.trim().length() < 1)
               continue;

            String extension = (String) m_tableModel.getValueAt(row,
                  COL_EXTENSION);
            if (extension == null || extension.trim().length() < 1)
               continue;

            PSExtensionCall extCall = new PSExtensionCall(
                  m_extRefs.get(extension), null);
            m_extensions.put(prop, extCall);
         }
      }

      return hasChanged;
   }

   /**
    * See ITabDataHelper iface.
    * @return <CODE>false</CODE> if validation of the tab data has failed,
    * <CODE>true</CODE> otherwise.
    */
   public boolean validateTabData()
   {
      if (m_table.isEditing())
      {
         int row = m_table.getEditingRow();
         int col = m_table.getEditingColumn();

         if (!validateCellValue(row, col))
         {
            JOptionPane.showMessageDialog( AppletMainDialog.getMainframe(),
                ms_res.getString("error.pluginPropertiesValidationFailed"),
                ms_res.getString( "error" ),
                JOptionPane.ERROR_MESSAGE );

            // if cell validation fails user won't be able to end edit
            return false; 
         }

         m_table.getCellEditor(row, col).stopCellEditing();
      }

      return true;
   }

   /**
    * The map of lucene extensions can represent analyzers or text extractors.
    * 
    * @return the current, in-memory map of lucene extensions.  Never
    * <code>null</code>, may be empty.
    */
   public Map<String, PSExtensionCall> getExtensions()
   {
      return m_extensions;
   }
   
   /**
    * Validates value in a given table cell.
    * @param row table row index, ignored if out of bounds.
    * @param col table column index, must be > 0  and < NUM_COLUMNS.
    * @return <code>true</code> if value is valid, <code>false</code> otherwise.
    */
   private boolean validateCellValue(int row, int col)
   {
      if (m_tableModel.getRowCount() < row || row < 0)
         return true;

      if (col < 0 || col >= NUM_COLUMNS)
         throw new IllegalArgumentException("column index out of range");

      boolean isValid = true;

      return isValid;
   }

   /**
    * Table that handles lucene extension manipulations.
    */
   private class LuceneExtensionsTable extends JTable implements FocusListener,
      KeyListener
   {
      /**
       * Ctor. Initializes JTable. Sets up cell editors as ComboBoxes,
       * also sets up key and focus listeners.
       * @param model table model.
       */
      public LuceneExtensionsTable(TableModel model)
      {
         super(model);

         init();
      }

      /**
       * Initializes JTable. Sets up cell editors as ComboBoxes,
       * also sets up key and focus listeners.
       */
      private void init()
      {
         setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
         setCellSelectionEnabled(false);
         setRowSelectionAllowed(true);
         setColumnSelectionAllowed(false);
         getTableHeader().setReorderingAllowed(false);

         TableColumn col0 = getColumnModel().getColumn(0);
         createComboBoxColumn(col0, m_properties);
            
         TableColumn col1 = getColumnModel().getColumn(1);
         createComboBoxColumn(col1, getExtensionNames());
                 
         //del key removes selected row(s)
         addKeyListener(this);

         KeyStroke ksEnterRelease =
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true);

         String stopEditActionName = "stopedit";
         getInputMap().put(ksEnterRelease, stopEditActionName);

         AbstractAction stopEditing = new AbstractAction()
         {
            /**
             * This action does 2 things. First, it stops any cell editing when
             * the Enter key is pressed. Secondly, it prevents the enter key
             * press from activating the default button of the main dialog,
             * which is extremely annoying when in a cell editor and enter
             * dismisses the whole dialog rather than just stopping editing.
             *
             * @param e unused
             */
            public void actionPerformed(
                  @SuppressWarnings("unused") ActionEvent e)
            {
               if (isEditing())
                  stopTableEditing();
               else if (m_lastRow!=-1 && m_lastCol!=-1)
               {
                  if (!validateCellValue(m_lastRow, m_lastCol))
                  {
                     editCellAt(m_lastRow, m_lastCol);

                     Component c = getEditorComponent();

                     if (c instanceof JComboBox) {
                        JComboBox combo = (JComboBox)c;
                        Component ce = combo.getEditor().getEditorComponent();
                        ce.setBackground(Color.red);
                        combo.getEditor().selectAll();
                     }

                     c.requestFocus();
                  }
               }
            }
         };

         getActionMap().put(stopEditActionName, stopEditing);

         appendEmptyRow();
      }

      /**
       * Creates a table column with combo box cell controls.  The combo box
       * will not be editable via typing in values.  Cell editing will be
       * activated on doubleclick.
       * 
       * @param col The table column to be modified, assumed not
       * <code>null</code>.
       * @param choices The list of choice values with which the combo box will
       * be populated, assumed not <code>null</code>. 
       */
      @SuppressWarnings("unchecked")
      private void createComboBoxColumn(TableColumn col, List<String> choices)
      {
         JComboBox comboEditor = new JComboBox();
         comboEditor.setEditable(false);
                
         for (String choice : choices)
             comboEditor.addItem(choice);
         
         DefaultCellEditor cellEdit = new DefaultCellEditor(comboEditor);
         cellEdit.setClickCountToStart(2);
         col.setCellEditor(cellEdit);
      }
      
      /**
       * Overridden JTable method that serves two purposes: makes JTable
       * to ignore VK_DELETE; looks for VK_ENTER and stops editing. See JTable
       * documentation for supplied params.
       */
      @Override
      protected boolean processKeyBinding(KeyStroke ks, KeyEvent e,
         int condition, boolean pressed)
      {
         if (e.getKeyCode()==KeyEvent.VK_DELETE)
            return true; //ignore

         boolean res = super.processKeyBinding(ks, e, condition, pressed);

         if (isEditing())
         {
            if (e.getKeyCode()==KeyEvent.VK_ESCAPE)
            {
               int row = m_table.getEditingRow();
               int col = m_table.getEditingColumn();

               m_table.getCellEditor(row, col).cancelCellEditing();
            }
            else if (e.getKeyCode()==KeyEvent.VK_ENTER)
               stopTableEditing();           
         }

         return res;
      }

      /**
       * Overridden JTable method. Remembers last edited row and col as well
       * as resets combo editor background to white.  Also filters out
       * currently selected properties from combobox items.  See JTable javadoc
       * for more info.
       */
      @Override
      public Component prepareEditor(TableCellEditor ed, int row, int col)
      {
         m_lastRow = row;
         m_lastCol = col;

         JComponent c = (JComponent) super.prepareEditor(ed, row, col);

         if (c instanceof JComboBox)
         {
            JComboBox combo = (JComboBox) c;
            int selIdx = combo.getSelectedIndex();
            String selItm = (String) combo.getSelectedItem();
            List<String> filteredProps = new ArrayList<String>(m_properties);
            
            if (col == COL_PROPERTY)
            {
               // filter out properties which have already been set
               for (int i = 0; i < m_tableModel.getRowCount(); i++)
               {
                  String prop = (String) m_tableModel.getValueAt(i,
                        COL_PROPERTY);
                  if (prop == null || prop.trim().length() < 1)
                     continue;

                  filteredProps.remove(prop);
               }
               
               combo.removeAllItems();
               for (String item : filteredProps)
                  combo.addItem(item);
            }
            
            if (selIdx != -1)
               combo.setSelectedItem(selItm);
            
            ComboBoxEditor comboEditor = combo.getEditor();
            comboEditor.getEditorComponent().setBackground(Color.white);
         }

         c.addFocusListener(this);

         return c;
      }

      /**
       * Overridden JTable method. Prevents user from editing a new cell if the
       * previously edited one ended up with invalid data. See JTable javadoc
       * for more info.
       */
      @Override
      public boolean isCellEditable(int row, int col)
      {
         if (m_lastRow==row && m_lastCol==col)
            return true;
         else if (m_lastRow!=-1 && m_lastCol!=-1)
         {
            if (!validateCellValue(m_lastRow, m_lastCol))
            {
               editCellAt(m_lastRow, m_lastCol);

               Component c = getEditorComponent();

               if (c instanceof JComboBox) {
                  JComboBox combo = (JComboBox)c;
                  Component ce = combo.getEditor().getEditorComponent();
                  ce.setBackground(Color.red);
                  combo.getEditor().selectAll();
               }

               c.requestFocus();

               return false;
            }
            else
            {
               m_lastRow = m_lastCol = -1;
               return true;
            }
         }

         return true;
      }

      //see FocusListener interface
      public void focusGained(FocusEvent e)
      {
         Component c = e.getComponent();

         c.setBackground(Color.white);
      }

       /**
       * If someone clicks outside the cell with the mouse, this will catch it
       * and stop editing for the cell. See FocusListener interface.
       * @param e unused
       */
      public void focusLost(@SuppressWarnings("unused") FocusEvent e)
      {
         
         if (m_table.isEditing())
            stopTableEditing();
      }

      /**
       * stops table cell editing and validates cell data.
      */
      void stopTableEditing()
      {
         if (m_table.isEditing())
         {
            int row = m_table.getEditingRow();
            int col = m_table.getEditingColumn();
            Component c = m_table.getEditorComponent();
            
            m_table.getCellEditor(row, col).stopCellEditing();

            if (!validateCellValue(row, col))
            {
               m_table.editCellAt(row, col);               

               if (c instanceof JComboBox) {
                  JComboBox combo = (JComboBox)c;
                  Component ce = combo.getEditor().getEditorComponent();
                  ce.setBackground(Color.red);
                  combo.getEditor().selectAll();
               }
               else if (c instanceof JTextField) {
                   JTextField text = (JTextField)c;
                   text.setBackground(Color.red);
                   text.selectAll();
               }              
            }
         }
         appendEmptyRow();
        
      }

      /**
       * Appends one empty row if all rows are used up.
       */
      private void appendEmptyRow()
      {
         TableModel model = getModel();

         int rows = model.getRowCount();

         int emptyCount = 0;

         for (int r = 0; r < rows; r++)
         {
            Object o = model.getValueAt(r, COL_PROPERTY);
            if (null == o || o.toString().trim().length() == 0)
               emptyCount++;
         }

         if (emptyCount < 1)
            addRow(); //add empty row
      }


       //KeyListener implementation
       /**
        * Invoked when a key has been typed.
        * This event occurs when a key press is followed by a key release.
        */
       public void keyTyped(@SuppressWarnings("unused") KeyEvent e)
       {
         //noop
       }

       /**
        * Invoked when a key has been released.
        */
       public void keyReleased(KeyEvent e)
       {
         int code = e.getKeyCode();

         if (code == KeyEvent.VK_DELETE)
         {
            removeRows();
         }
       }

       /**
        * Invoked when a key has been pressed.
        */
       public void keyPressed(@SuppressWarnings("unused") KeyEvent e)
       {        
       }

      /**
       * removes all selected rows.
       */
      public void removeRows()
      {
         DefaultTableModel dtm = (DefaultTableModel) getModel();
         ListSelectionModel lsm = getSelectionModel();
         Vector data = dtm.getDataVector();
         int minIndex = lsm.getMinSelectionIndex();
         int maxIndex = lsm.getMaxSelectionIndex();
         int k = 0;
         for (int i = minIndex; i <= maxIndex; i++)
         {
            if (lsm.isSelectedIndex(i - k))
            {
               if (m_lastRow==i)
               {
                  m_lastRow = -1;
                  m_lastCol = -1;
               }

               data.removeElementAt(i - k);
               k++;
            }
         }

         dtm.fireTableDataChanged();

         m_tableChanged = true;
         appendEmptyRow();
      }

      /**
       * adds one empty row to a table.
       */
      public void addRow()
      {
         int cols = getModel().getColumnCount();
         DefaultTableModel dtm = (DefaultTableModel)getModel();

         dtm.addRow(new Object[cols]);
         dtm.fireTableDataChanged();

         m_tableChanged = true;
      }

      /**
       * Override super.valueChanged(ListSelectionEvent).  Add a new row when
       * selecting the last row.
       * 
       * @see javax.swing.JTable#valueChanged(
       * javax.swing.event.ListSelectionEvent)
       */ 
      public void valueChanged(ListSelectionEvent e)
      {
         if (getSelectedRow() != -1 && getSelectedRow() == getRowCount() - 1)
         {
            addRow();
         }
         
         super.valueChanged(e);
      }
      
      /**
       * tracks last edited row.
       */
      private int m_lastRow = -1;

      /**
       * tracks last edited col.
       */
      private int m_lastCol = -1;
   }


   /**
    * Performs basic initialization of this panel.
    */
   @SuppressWarnings("unchecked")
   private void initPanel()
   {
      Vector vTableHeaders = new Vector();
      vTableHeaders.add(m_col1Title);
      vTableHeaders.add(m_col2Title);
      
      m_tableModel = new DefaultTableModel(getInitTableData(), vTableHeaders)
      {
         private boolean m_updatingData = false;
         
         @Override
         public void setValueAt(Object aValue, int row, int column)
         {
            super.setValueAt(aValue, row, column);
            m_tableChanged = true;
            
            synchronizeCellData(row, column);
         }

         /**
          * Updates the corresponding cell data when a change is made.  If they
          * are found in the list the other is updated to the appropriate value.
          */
         private void synchronizeCellData(int row, int col)
         {
            if (!m_updatingData)
            {
               m_updatingData = true;
               
               String[] props = new String[m_properties.size()];
               m_properties.toArray(props);
               
               List<String> extNames = getExtensionNames();
               String[] extensions = new String[extNames.size()];
               extNames.toArray(extensions);
              
               if (col == COL_PROPERTY)
               {
                  String tmp = (String)m_table.getValueAt(row, col);
                  int idx =
                     findListIndex(tmp, props);
                  // if found set the appropriate property
                  if (idx >= 0)
                  {
                     m_table.setValueAt(
                        props[idx],
                        row,
                        COL_PROPERTY);
                  }
               }
               else if (col == COL_EXTENSION)
               {
                  String tmp = (String)m_table.getValueAt(row, col);
                  int idx =
                     findListIndex(
                        tmp,
                        extensions);
                  // if found set the appropriate extension
                  if (idx >= 0)
                  {
                     m_table.setValueAt(
                        extensions[idx],
                        row,
                        COL_EXTENSION);
                  }
               }
               m_updatingData = false;
            }
         }

         /**
          * Finds the index of the specified string, if not found returns -1.
          * 
          * @param strFind the string to find within the list, must not be 
          *    <code>null</code> or empty
          * @param strList the list of strings to traverse looking for the
          *    specified
          *    string, must not be <code>null</code>
          * @return the index of the found item, if not found -1
          */
         private int findListIndex(String strFind, String[] strList)
         {
            for (int i = 0; i < strList.length; i++)
            {
               String strCheck = strList[i];
               if (strCheck.equals(strFind))
                  return i;
            }
            return -1;
         }
      };

      //must catalog extensions before creating table
      catalogExtensions();
           
      m_table = new LuceneExtensionsTable(m_tableModel);

      JScrollPane scrollPane = new JScrollPane(m_table);

      JPanel basePane = new JPanel(new BorderLayout());

      basePane.add(scrollPane, BorderLayout.CENTER);
      basePane.setBackground(Color.white);

      setLayout(new BorderLayout());
      add(basePane, BorderLayout.CENTER);
   }


   /**
    * Returns a Vector of Vector of Strings that are used to initialize the
    * Table model.
    * @return Vector of Vectors of Strings, never <code>null</code>, may be
    * <code>empty</code>.
    */
   @SuppressWarnings("unchecked")
   private Vector getInitTableData()
   {
      Vector data = new Vector();

      Iterator<String> itProps = m_extensions.keySet().iterator();

      while (itProps.hasNext())
      {
          String prop = itProps.next();

          Vector row = new Vector();
          row.add(prop);
          row.add(m_extensions.get(prop).getName());
                    
          data.add(row);
      }

      return data;
   }

   /**
    * Utilizes {@link CatalogLocales#getCatalog(PSDesignerConnection)} to
    * catalog the locales from the server.  
    * 
    * @return a list of locale names (language strings) sorted in ascending
    * order, case-insensitive. 
    */
   @SuppressWarnings("unchecked")
   private List<String> catalogLocaleNames()
   {
      // Catalog locales
      List<String> locales = CatalogLocales.getCatalog(ms_conn);
      
      Collections.sort(locales, new PSStringComparator(
            PSStringComparator.SORT_CASE_INSENSITIVE_ASC));
      
      return locales;
   }
   
   /**
    * Utilizes {@link CatalogMimeTypes#getCatalog(PSDesignerConnection)} to
    * catalog the supported mime types from the server.
    * 
    * @return a list of mime type names sorted in ascending order, case-
    * insensitive. 
    */
   @SuppressWarnings("unchecked")
   private List<String> catalogMimeTypeNames()
   {
      // Catalog mime types
      List<String> mimetypes = CatalogMimeTypes.getCatalog(ms_conn);
      
      Collections.sort(mimetypes, new PSStringComparator(
            PSStringComparator.SORT_CASE_INSENSITIVE_ASC));
      
      return mimetypes;
   }
   
   /**
    * Catalogs the current set of server extensions.  Uses
    * {@link CatalogServerExits#getCatalog(PSDesignerConnection, String, String,
    *  String, boolean, boolean)} to perform the catalog.
    */
   @SuppressWarnings("unchecked")
   private void catalogExtensions()
   {
      if (m_extRefs == null)
      {
         m_extRefs = new HashMap<String, PSExtensionRef>();
         
         Vector exits = new Vector();
         Vector javaExits = CatalogServerExits.getCatalog(
               ms_conn, 
               CatalogExtensionCatalogHandler.JAVA_EXTENSION_HANDLER_NAME,
               null,
               m_interface,
               true,
               true);
         
         Vector javaScriptExits = CatalogServerExits.getCatalog(
               ms_conn, 
               CatalogExtensionCatalogHandler.
               JAVA_SCRIPT_EXTENSION_HANDLER_NAME,
               null,
               m_interface,
               true,
               true);
           
         exits.addAll(javaExits);
         exits.addAll(javaScriptExits);
         
         for (int i = 0; i < exits.size(); i++)
         {
            PSExtensionDef extDef = (PSExtensionDef) exits.elementAt(i);
            m_extRefs.put(extDef.toString(), extDef.getRef());
         }
      }
   }
   
   /**
    * The current set of server extensions are stored in a map where the key is
    * the name of the extension.
    * 
    * @return a list of current extension names sorted in ascending order, 
    * case-insensitive, never <code>null</code>, may be empty.
    */
   @SuppressWarnings("unchecked")
   private List<String> getExtensionNames()
   {
      List<String> extNames = new ArrayList<String>();
      Iterator<String> iter = m_extRefs.keySet().iterator();
      while (iter.hasNext())
      {
         extNames.add(iter.next());
      }
      
      Collections.sort(extNames, new PSStringComparator(
            PSStringComparator.SORT_CASE_INSENSITIVE_ASC));
      
      return extNames;
   }
   
   /**
    * Table for the lucene extensions. Initialized in {@link #initPanel()}
    */
   private JTable m_table = null;

   /** The model of the display table. <code>DefaultTableModel</code> is used.*/
   private TableModel m_tableModel = null;

   /** set to <code>true</code> whenever user modifies table data. */
   private boolean m_tableChanged = false;

   /**
    * Property col., i.e., item locale (analyzers) or mime type (converters).
    */
   private static final int     COL_PROPERTY = 0;
   
   /**
    * Extension col, i.e., analyzer or extractor.
    */
   private static final int     COL_EXTENSION = 1;
  
   /** number of table columns. */
   private static int NUM_COLUMNS = 2;

   /**
    * Local reference to the set of lucene extension mappings, set in ctor,
    * never <code>null</code> after that.
    */
   private Map<String, PSExtensionCall> m_extensions = null;
   
   /**
    * Local reference to the set of available property values for column one
    * of the table, set in ctor, never <code>null</code> after that.
    */
   private List<String> m_properties = null;
   
   /**
    * Map consisting of extension name - {@link PSExtensionRef} pairs.  Values
    * are loaded in {@link #catalogExtensions()}.
    */
   private Map<String, PSExtensionRef> m_extRefs = null;
   
   /**
    * The interface implemented by the set of extensions.  Will be either
    * {@link #ANALYZER_INTERFACE_NAME} or {@link #EXTRACTOR_INTERFACE_NAME}.
    */
   private String m_interface = null;
   
   /**
    * Column one title.  See ctor for details.
    */
   private String m_col1Title = null;
   
   /**
    * Column two title.  See ctor for details.
    */
   private String m_col2Title = null;
   
   /** 
    * A local reference of the connection this admin client has to the server.
    */
   private static PSDesignerConnection ms_conn = null;
   
   /**
    * All lucene analyzer extensions will implement this interface.
    */
   public static String ANALYZER_INTERFACE_NAME = 
      "com.percussion.search.lucene.analyzer.IPSLuceneAnalyzer";
   
   /**
    * All lucene text converters will implement this interface.
    */
   public static String EXTRACTOR_INTERFACE_NAME =
      "com.percussion.search.lucene.textconverter.IPSLuceneTextConverter";
   
   /** The resource reference for all display text. */
   private static ResourceBundle ms_res = PSServerAdminApplet.getResources();
}
