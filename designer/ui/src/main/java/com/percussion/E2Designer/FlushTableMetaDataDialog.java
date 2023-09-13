/******************************************************************************
 *
 * [ FlushTableMetaDataDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import com.percussion.conn.PSDesignerConnection;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.server.PSRemoteConsole;
import com.percussion.util.PSCollection;
import com.percussion.util.PSStringComparator;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

public class FlushTableMetaDataDialog extends PSEditorDialog
{
   /**
    * Parameterless constructor for this class.  Editor must then be invoked
    * thru onEdit().
    *
    * @see #onEdit(UIFigure, Object) onEdit
    */
   public FlushTableMetaDataDialog()
   {
      super();
   }

   public FlushTableMetaDataDialog(Window parent)
   {
      super(parent);
   }

   // IEditor implementation
   public boolean onEdit(UIFigure figure, final Object data)
   {
      if ((figure == null) || (data == null))
         throw new IllegalArgumentException("figure or data may not be null");

      init(figure);

      return true;
   }


   /**
    * Causes all items in the table list box to be selected.
    */
   public void onSelectAll()
   {
      m_tableList.setSelectionInterval(0, m_tableList.getModel().getSize() - 1);
   }

   /**
    * Sends a remote console command to the server to flush any tables that
    * have been selected.
    */
   public void onFlush()
   {
      // Switch cursor to an hourglass
      this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      try
      {
         Object[] tables = m_tableList.getSelectedValues();
         for (int i=0; i < tables.length; i++)
         {
            PSBackEndTable table = (PSBackEndTable)m_beTables.get(tables[i]);
            sendConsoleCmd(getFlushConsoleCmd(table));
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         String [] args =
         {
            e.getLocalizedMessage(),
         };

         JOptionPane.showMessageDialog( this,
               MessageFormat.format( E2Designer.getResources( ).getString(
                   "ServerConnException" ), args ),
               E2Designer.getResources( ).getString( "ServerConnErr" ),
               JOptionPane.ERROR_MESSAGE );
      }
      finally
      {
         // Switch cursor back
         this.setCursor(Cursor.getDefaultCursor());
         dispose();
      }

   }


   /**
    * Store all necessary data, and make call to initialize UI.
    *
    * @param figure The figure of the dataset whose tables will be flushed.
    * May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if figure is <code>null</code> or does
    * not contain a PSDataSet as its data.
    */
   private void init(UIFigure figure)
   {
      // be sure the app is not null
      if (figure == null)
         throw new IllegalArgumentException("figure cannot be null");

      // store the data
      Object o = figure.getData();
      if (o instanceof PSDataSet)
         m_data = (PSDataSet)o;
      else
         throw new IllegalArgumentException(
            "figure must contain a PSDataSet for its data");

      // store the tables in a sorted map, keyed by the tablename
      PSCollection beTables = null;
      if (m_data.getPipe() != null && 
         m_data.getPipe().getBackEndDataTank() != null)
      {
         beTables = m_data.getPipe().getBackEndDataTank().getTables();
      }
         
      
      m_beTables = new TreeMap(new PSStringComparator(
         PSStringComparator.SORT_CASE_SENSITIVE_ASC));
      
      if (beTables != null)
      {
         Iterator tables = beTables.iterator();
         while (tables.hasNext())
         {
            PSBackEndTable table = (PSBackEndTable)tables.next();
            String tableName = table.getTable();

            // same table may be in tank more than once with different alias
            if (m_beTables.containsKey(tableName))
               continue;

            m_beTables.put(tableName, table);
         }
      }


      // get the resource bundle
      m_res = getResources();

      initUI();
       setVisible(true);
   }


   /**
    * Initializes all panels and puts everything together.
    */
   private void initUI()
   {
      // create main panel to hold everything else
      JPanel contentPane = new JPanel();
        getContentPane().add(contentPane);

       setResizable(true);

      final int BORDER_WIDTH = 3;
      contentPane.setBorder(new EmptyBorder(2*BORDER_WIDTH, BORDER_WIDTH,
         BORDER_WIDTH, BORDER_WIDTH ));

      contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

      // create top panel to hold tables panel and command buttons
      JPanel top = createTopPanel();
      contentPane.add(Box.createVerticalGlue());
      contentPane.add(top);

      setSize(DIALOG_SIZE);
      center();

      // set the title
      setTitle(getResources().getString("title"));
      pack();
   }



   /**
    * Creates the panel containing the table and command panels for the dialog.
    * @return the panel, never <code>null</code>.
    */
   private JPanel createTopPanel()
   {
      JPanel top = new JPanel();
      top.setLayout(new BorderLayout());
      top.setBorder(new EmptyBorder(5,5,5,5));
      top.add(createTablesPanel(), BorderLayout.CENTER);
      top.add(createCommandPanel(), BorderLayout.SOUTH);

      return top;
   }


   /**
    * Creates the panel containing the command buttons for the dialog.
    * @return the panel, never <code>null</code>.
    */
   private JPanel createCommandPanel()
   {
      // set up the options panel
      JPanel cmdPanel = new JPanel(new BorderLayout());
      cmdPanel.setBorder(new EmptyBorder(5,5,5,5));

      // add the command panel
      // TODO: GET HELP URL
      UTStandardCommandPanel cmd = new UTStandardCommandPanel(this, "",
                                       javax.swing.SwingConstants.HORIZONTAL)
      {
         public void onOk()
         {
            FlushTableMetaDataDialog.this.onFlush();
         }
      };
      JPanel bottomPanel = new JPanel(new BorderLayout());
      bottomPanel.add(cmd, BorderLayout.EAST);

      // add the flush buttons
      JPanel panel = createFlushButtonPanel();
      cmdPanel.add(bottomPanel, BorderLayout.SOUTH);
      cmdPanel.add(panel, BorderLayout.NORTH);
      cmdPanel.setAlignmentX(CENTER_ALIGNMENT);

      // add filler to take up all space in the bottom
      cmdPanel.add(Box.createVerticalGlue());

      return cmdPanel;
   }

   /**
    * Creates the panel containing the list box of tables.
    *
    * @return the panel, never <code>null</code>.
    */
   private JPanel createTablesPanel()
   {
      JPanel tablePanel = new JPanel();
      tablePanel.setLayout(new BorderLayout());

      // add instructions
      JTextArea instructionText = new JTextArea(m_res.getString(
         "instructions"));
      instructionText.setRows(4);
      instructionText.setLineWrap(true);
      instructionText.setWrapStyleWord(true);
      instructionText.setBackground(getBackground());
      instructionText.setEditable(false);
      instructionText.setAlignmentX(LEFT_ALIGNMENT);
      tablePanel.add(instructionText, BorderLayout.NORTH);

      // setup listbox with list of tables
      m_tableList = new JList(m_beTables.keySet().toArray());
      JScrollPane scrollPane = new JScrollPane(m_tableList);
      tablePanel.add(scrollPane, BorderLayout.CENTER);

      return tablePanel;
   }


   /**
    * Creates the panel containing the refresh and flush buttons.
    *
    * @return the panel, never <code>null</code>.
    */
   private JPanel createFlushButtonPanel()
   {
      JPanel flushPanel = new JPanel(new BorderLayout());
      m_selectAllButton = new UTFixedButton(m_res.getString("selectAll"));
        
      m_selectAllButton.setMnemonic(m_res.getString("selectAll.mn").charAt(0));
        m_selectAllButton.addActionListener(
         new ActionListener()
         {
            public void actionPerformed(ActionEvent e)
            {
               FlushTableMetaDataDialog.this.onSelectAll();
            }
         }
      );
        flushPanel.setBorder(new EmptyBorder(5,5,5,5));
      flushPanel.setLayout(new BorderLayout());
      flushPanel.add(m_selectAllButton, BorderLayout.EAST);
      return flushPanel;
   }

   /**
    * Sends the given command string to the server using PSRemoteConsole.
    *
    * @param cmd The console command to send. May not be <code>null</code>.
    *
    * @return The result xml document from the server.  May be
    * <code>null</code>.
    *
    * @throws Exception if anything goes wrong
    */
   private Document sendConsoleCmd(String cmd) throws Exception
   {
      if (cmd == null || cmd.length() == 0)
         throw new IllegalArgumentException("cmd may not be null");

      Document   xmlDoc   = null;

      // Send command to the server
      PSDesignerConnection conn = E2Designer.getDesignerConnection();
      PSRemoteConsole console = new PSRemoteConsole(conn);
      xmlDoc = console.execute(cmd);

      return xmlDoc;
   }


   /**
    * Creates a flush console command string.
    *
    * @param table The table to flush.  Assumed not <code>null</code>.
    *
    * @return The string, never <code>null</code> or empty.
    */
   private String getFlushConsoleCmd(PSBackEndTable table)
   {
      StringBuffer cmd = new StringBuffer();
      cmd.append("flush dbmd");

      // add datasource if we have one
      if (StringUtils.isNotBlank(table.getDataSource()))
         cmd.append(" -d " + table.getDataSource());

      // add table
      cmd.append(" -t " + table.getTable());

      return cmd.toString();

   }

   /**
    * The dataset that we will flush table for.  Set in {@link #init(UIFigure)},
    * never <code>null</code> or modified after that.
    */
   private PSDataSet m_data = null;

   /**
    * Map of PSBackEndTable objects that are used by this dialog's
    * dataset.  Key is the table name as a String, value is the PSBackEndTable
    * object.  Set in {@link #init(UIFigure)}, never <code>null</code> or
    * modified after that.
    */
   private Map m_beTables = null;

   /**
    * The resource bundle for this dialog.  Always set by init method.
    */
   private ResourceBundle m_res = null;

   /**
    * List box of table names, used to select tables that will be flushed.
    * Set in {@link #initUI()}, never <code>null</code> after that.
    */
   private JList m_tableList = null;

   /**
    * The Select All button. Causes all items in the table list to be selected.
    * Initialized during construction, never <code>null</code> after that.
    */
   private JButton m_selectAllButton = null;

   /**
    * The Flush button. Causes all selected tables to be flush from the server's
    * meta data cache.
    * Initialized during construction, never <code>null</code> after that.
    */
   private JButton m_flushButton = null;



   /**
    * This dialogs preferred size.
    */
   private final static Dimension DIALOG_SIZE = new Dimension(300, 300);

}
