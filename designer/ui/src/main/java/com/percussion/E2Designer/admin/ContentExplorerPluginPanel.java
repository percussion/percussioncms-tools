/******************************************************************************
 *
 * [ ContentExplorerPluginPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.design.objectstore.IPSJavaPlugin;
import com.percussion.design.objectstore.IPSJavaPluginConfig;
import com.percussion.design.objectstore.PSJavaPlugin;
import com.percussion.design.objectstore.PSJavaPluginConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Content Explorer Properties Panel.
 */
public class ContentExplorerPluginPanel extends JPanel implements ITabDataHelper
{
   /**
    * Ctor.
    * @param config server configuration, never <code>null</code>.
    */
   public ContentExplorerPluginPanel(ServerConfiguration config)
   {
      if (config==null)
         throw new IllegalArgumentException("config may not be null");

      this.setBorder(new EmptyBorder(5, 5, 5, 5));
      m_serverConfig = config;
      m_pluginConfig = config.getJavaPluginConfig();

      if (m_pluginConfig==null)
         throw new IllegalStateException("m_pluginConfig may not be null");

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
         PSJavaPluginConfig pluginConfig = new PSJavaPluginConfig();

         int rows = m_tableModel.getRowCount();

         for (int row = 0; row < rows; row++)
         {
            String os = (String)m_tableModel.getValueAt(row, COL_CLIENT_OS);
            if (os==null || os.trim().length() < 1)
               continue;

            String browser = (String)m_tableModel.getValueAt(row, COL_BROWSER);
            if (browser==null || browser.trim().length() < 1)
               continue;

            String ver = (String)m_tableModel.getValueAt(row, COL_PLUGIN_VERSION);
            if (ver==null || ver.trim().length() < 1)
               continue;

            String verType = (String)m_tableModel.getValueAt(row, COL_VERSIONING);
            if (verType==null || verType.trim().length() < 1)
               continue;

            boolean isStatic = true;
            if (verType.trim().
                equalsIgnoreCase(IPSJavaPlugin.VERSIONING_TYPE_DYNAMIC))
               isStatic = false;

            String loc = (String)m_tableModel.getValueAt(row, COL_PLUGIN_LOCATION);
            if (loc==null || loc.trim().length() < 1)
               continue;

            IPSJavaPlugin plugin = new PSJavaPlugin(os.trim(), browser.trim(),
               ver.trim(), isStatic, loc.trim());

            pluginConfig.addPlugin(plugin);
         }

         m_serverConfig.setJavaPluginConfig(pluginConfig);
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

            return false; //if cell validation fails user won't be able to end edit
         }

         m_table.getCellEditor(row, col).stopCellEditing();
      }

      return true;
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

      switch (col) {
         case COL_PLUGIN_VERSION:
            isValid = validatePluginVersion(m_table.getValueAt(row, col));
            break;
         case COL_PLUGIN_LOCATION:
            isValid = validatePluginLocation(m_table.getValueAt(row, col));
            break;
      }

      return isValid;
   }


   /**
    * Validates PluginVesion value. It is expected to be in the following format:
    * number1.number2.number3_number4, where number1, 2 and 3 must be present
    * the last one however is optional; also the placement of dots and
    * underscore are expected to be in right places; ie: 1.3.4_01
    * @param val value to validate, may be <code>null</code> or <code>empty</code>.
    * @return <code>true</code> if value is valid, <code>false</code> otherwise.
    */
   private boolean validatePluginVersion(Object val)
   {
      if (val==null || val.toString().trim().length() < 1)
         return false;

      String valStr = val.toString().trim();

      int count = 0;
      String strTmp = "";

      /*
        The version number is expected to be in the following format:
        number1.number2.number3_number4, where number1, 2 and 3 must be present
        and the last token number4 is optional.
      */
      StringTokenizer st = new StringTokenizer(valStr.toString(), "._");
      String versionNumber = "";
      while (st.hasMoreTokens())
      {
          String token = st.nextToken();
          int intVal = 0;

          //must be a positive number
          try
          {
            intVal = Integer.parseInt(token);

            if (intVal < 0)
               return false;
          }
          catch(NumberFormatException ex)
          {
            return false;
          }

          if (count==0)
            strTmp += "" + token;
          else if(count==3)
          {
            strTmp += "_" + token;
            if(token.length()>2)
               return false;
          }
          else
            strTmp += "." + token;
          versionNumber += token;
          count++;
      }

      //compare format of a given one with a constructed one
      if (count < 3 || !strTmp.equals(valStr))
         return false;
      try
      {
         /*If the version has less than three numbers we return in the above
         loop. If we just compare a version like 1.4.1 with minimum supported
         version (141 < 13106) returns false. To take care of such versions we
         need to append the appropriate zeros to the version before comparing.*/
         if(versionNumber.length()==3) versionNumber += "00";
         else if(versionNumber.length()==4) versionNumber += "0";

         int intVersion = Integer.parseInt(versionNumber);

         if (intVersion < MIN_SUPPORTED_VERSION || 
            intVersion > MAX_SUPPORTED_VERSION)
         {
            return false;
         }
      }
      catch(NumberFormatException ex)
      {
         return false;
      }
      return true;
   }

   /**
    * Validates PluginLocation value, for now only check for null or empty.
    * @param val value to validate, may be <code>null</code> or <code>empty</code>.
    * @return <code>true</code> if value is valid, <code>false</code> otherwise.
    */
   private boolean validatePluginLocation(Object val)
   {
      if (val==null || val.toString().trim().length() < 1)
         return false;

      return true;
   }

   /**
    * Table that handles Java Plug-in properties manipulations.
    */
   private class PluginPropertiesTable extends JTable implements FocusListener,
      KeyListener
   {
      /**
       * Ctor. Initializes JTable. Sets up cell editors as ComboBoxes,
       * also sets up key and focus listeners.
       * @param model table model.
       */
      public PluginPropertiesTable(TableModel model)
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

         for(int i=0; i < getColumnCount(); i++)
         {
            boolean isEditable = COL_EDITABLE[i];
            TableColumn col = getColumnModel().getColumn(i);

            JComboBox comboEditor = new JComboBox();
            comboEditor.setEditable(isEditable);

            String[] comboChoices = getColumnComboChoices(i);
            for (int chi = 0; chi < comboChoices.length; chi++)
               comboEditor.addItem(comboChoices[chi]);

            DefaultCellEditor cellEdit = new DefaultCellEditor(comboEditor);
            cellEdit.setClickCountToStart(2); //doubleclick
            col.setCellEditor(cellEdit);
         }

         //right click on the header adds a new row
         MouseListener ml = new MouseAdapter()
         {
            @Override
            public void mousePressed(MouseEvent e)
            {
               if (SwingUtilities.isRightMouseButton(e))
               {
                  addRow();
               }
            }
         };

         getTableHeader().addMouseListener(ml);

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
             * press from activating the default button of the main dialog, which
             * is extremely annoying when in a cell editor and enter dismisses
             * the whole dialog rather than just stopping editing.
             *
             * @param e unused
             */
            public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
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
       * Overridden JTable method that serves two purposes: makes JTable
       * to ignore VK_DELETE; looks for VK_ENTER and stops editing. See JTable
       * documentation for supplied params.
       * @param ks
       * @param e
       * @param condition
       * @param pressed
       * @return
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
       * as resets combo editor background to white. See JTable javadoc
       * for more info.
       * @param ed
       * @param row
       * @param col
       * @return
       */
      @Override
      public Component prepareEditor(TableCellEditor ed, int row, int col)
      {
         m_lastRow = row;
         m_lastCol = col;

         JComponent c = (JComponent) super.prepareEditor(ed, row, col);

         if (c instanceof JComboBox)
         {
            JComboBox combo = (JComboBox)c;
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
       * @param row see JTable.
       * @param col see JTable.
       * @return see JTable.
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
            Object o = model.getValueAt(r, COL_CLIENT_OS);
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
      vTableHeaders.add(ms_res.getString("clientOS"));
      vTableHeaders.add(ms_res.getString("browser"));
      vTableHeaders.add(ms_res.getString("pluginVersion"));
      vTableHeaders.add(ms_res.getString("versioning"));
      vTableHeaders.add(ms_res.getString("pluginLocation"));

      m_tableModel = new DefaultTableModel( getInitTableData(), vTableHeaders)
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
          * Updates the corresponding cell data when a change is made to either
          * the version or location data. If they are found in the list the
          * other is updated to the appropriate value.
          */
         private void synchronizeCellData(int row, int col)
         {
            if (!m_updatingData)
            {
               m_updatingData = true;

               if (col == COL_PLUGIN_VERSION)
               {
                  String tmp = (String)m_table.getValueAt(row, col);
                  int idx =
                     findListIndex(tmp, IPSJavaPluginConfig.VERSION_LIST);
                  // if found set the appropriate location string
                  if (idx >= 0)
                  {
                     m_table.setValueAt(
                        IPSJavaPluginConfig.PLUGIN_LOCATION_LIST[idx],
                        row,
                        COL_PLUGIN_LOCATION);
                  }
               }
               else if (col == COL_PLUGIN_LOCATION)
               {
                  String tmp = (String)m_table.getValueAt(row, col);
                  int idx =
                     findListIndex(
                        tmp,
                        IPSJavaPluginConfig.PLUGIN_LOCATION_LIST);
                  // if found set the appropriate location string
                  if (idx >= 0)
                  {
                     m_table.setValueAt(
                        IPSJavaPluginConfig.VERSION_LIST[idx],
                        row,
                        COL_PLUGIN_VERSION);
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
          * @param strList the list of strings to traverse looking for the specified
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

      m_table = new PluginPropertiesTable(m_tableModel);

      JScrollPane scrollPane = new JScrollPane(m_table);

      JPanel basePane = new JPanel(new BorderLayout());

      basePane.add(scrollPane, BorderLayout.CENTER);
      basePane.setBackground(Color.white);

      setBorder(new TitledBorder(new EtchedBorder(),
         ms_res.getString("javaPluginConfiguration")));

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

      Iterator itPugins = m_pluginConfig.getAllPlugins().iterator();

      while (itPugins.hasNext())
      {
          IPSJavaPlugin plugin = (IPSJavaPlugin)itPugins.next();

          Vector row = new Vector();
          row.add(plugin.getOsKey());
          row.add(plugin.getBrowserKey());
          row.add(plugin.getVersionToUse());

          boolean isStatic = plugin.isStaticVersioning();
          if (isStatic)
            row.add(ms_res.getString("static"));
          else
            row.add(ms_res.getString("dynamic"));

          row.add(plugin.getDownloadLocation());
          data.add(row);
      }

      return data;
   }

   /**
    * Returns an array of choices that are appropriate for a given table column.
    * @param col expected col >= COL_CLIENT_OS && col <= COL_PLUGIN_LOCATION,
    * IllegalArgumentException is thrown if column ind is not in range.
    * @return array of Strings that are used to load ComboBoxes,
    * never <code>null</code>.
    */
   private String[] getColumnComboChoices(int col)
   {
      switch (col)
      {
         case COL_CLIENT_OS:
            return IPSJavaPluginConfig.OS_LIST;
         case COL_BROWSER:
            return IPSJavaPluginConfig.BROWSER_LIST;
         case COL_PLUGIN_VERSION:
            return IPSJavaPluginConfig.VERSION_LIST;
         case COL_VERSIONING:
            return IPSJavaPluginConfig.VERSIONING_LIST;
         case COL_PLUGIN_LOCATION:
            return IPSJavaPluginConfig.PLUGIN_LOCATION_LIST;
         default:
            throw new IllegalArgumentException("col index is not in the range");
      }
   }

   /**
    * Table for the java plug-in properties. Initialized in {@link #initPanel()}
    */
   private JTable m_table = null;

   /** The model of the display table. <code>DefaultTableModel</code> is used.*/
   private TableModel m_tableModel = null;

   /** set to <code>true</code> whenever user modifies table data. */
   private boolean m_tableChanged = false;

   /** indicates which column is editable. */
   private static final boolean COL_EDITABLE[] = {false, false, true, false, true};

   /**
    * CLIENT_OS col.
    */
   private static final int     COL_CLIENT_OS = 0;
   /**
    * BROWSER col.
    */
   private static final int     COL_BROWSER = 1;
   /**
    * PLUGIN_VERSION col.
    */
   private static final int     COL_PLUGIN_VERSION = 2;
   /**
    * VERSIONING col.
    */
   private static final int     COL_VERSIONING = 3;
   /**
    * PLUGIN_LOCATION col.
    */
   private static final int     COL_PLUGIN_LOCATION = 4;

   /** number of table columns. */
   private static int NUM_COLUMNS = 5;

   /**
    * Minimum supported version (lowest version of 1.5)
    */
   private static final int     MIN_SUPPORTED_VERSION = 15000;
   
   /**
    * Maximum supported version (highest level of 1.6 we'll encounter)
    */
   private static final int MAX_SUPPORTED_VERSION = 16999;

   /**
    *  A local reference to the ServerConfiguration
    */
   private ServerConfiguration m_serverConfig = null;
   /**
    * Plugin config. Initialized in ctor., never <code>null</code> after that.
    */
   private IPSJavaPluginConfig m_pluginConfig = null;

   /** The resource reference for all display text. */
   private static ResourceBundle ms_res = PSServerAdminApplet.getResources();
}
