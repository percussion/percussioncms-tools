/******************************************************************************
 *
 * [ PSPackageSelectionPanel.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packager.ui;

import com.percussion.deployer.error.PSDeployException;
import com.percussion.packager.ui.data.PSPackageDescriptorMetaInfo;
import com.percussion.packager.ui.managers.PSServerConnectionManager;
import com.percussion.packager.ui.model.PSPackagerClientModel;
import com.percussion.guitools.PSTableSorter;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author erikserating
 *
 */
public class PSPackageSelectionPanel extends JPanel implements ActionListener
{
   
   public PSPackageSelectionPanel()
   {
      init();
   }
   
   /**
    * Layout the panel and add listeners.
    */
   private void init()
   {
      MigLayout layout = new MigLayout(
         "fill, ins 10 10 0 10",
         "[grow]",
         "[top][grow, top]");
      setLayout(layout);
      
      URL logoImage = getClass().getResource(getResourceString("image.logo"));
      if (logoImage != null)
      {
         ImageIcon logo = new ImageIcon(logoImage);
         JLabel logoLabel = new JLabel(logo);
         logoLabel.setHorizontalAlignment(SwingConstants.LEFT);
         add(logoLabel, "growx, left, split");
      }
      
      URL iconImage = getClass().getResource(getResourceString("image.icon"));
      if (iconImage != null)
      {
         ImageIcon icon = new ImageIcon(iconImage);
         JLabel iconLabel = new JLabel(icon);
         add(iconLabel, "wrap 1");
      }     
      
      JLabel packagesLabel = new JLabel(
         getResourceString("label.package.descriptors"));
      add(packagesLabel, "wrap 1");
      m_model = new DescriptorTableModel();
      PSTableSorter sorter = new PSTableSorter(m_model);
      sorter.setIsSortingEnabled(false);
      m_packageTable = new JTable(sorter);
      m_packageTable.setColumnSelectionAllowed(false);
    
      m_packageTable.setDefaultRenderer(
         String.class, new PSCellRenderer());
      m_packageTable.setDefaultRenderer(
        Date.class, new PSCellRenderer());
      
      m_packageTable.addMouseListener(new MouseAdapter()
      {
         @Override
         public void mouseClicked(MouseEvent e)
         {
            if (e.getClickCount() == 2)
            {
               JTable table = (JTable)e.getSource();
               if(table.getSelectedRow() != -1)
               {
                  final PSPackagerClientModel model = 
                     PSPackagerClient.getFrame().getModel();
                  final PSPackageDescriptorMetaInfo info = 
                     getSelectedDescriptorInfo();
                  if(info != null)
                  {
                     Runnable r = new Runnable()
                     {
                        public void run()
                        {
                           try
                           {
                              model.editDescriptor(info);
                           }
                           catch (final PSDeployException ex)
                           {
                              SwingUtilities.invokeLater(new Runnable()
                              {
                                 public void run()
                                 {
                                    PSPackagerClient.getErrorDialog().showError(ex, true,
                                       PSResourceUtils.getCommonResourceString("errorTitle"));                           
                                 }
                              });
                           }                  
                        }
                     };
                     Thread t = new Thread(r);
                     t.start();
                  }
               }
            }
         }
      });

      
      sorter.addMouseListenerToHeaderInTable(m_packageTable);
      m_packageTable.getSelectionModel().setSelectionMode(
         ListSelectionModel.SINGLE_SELECTION);
      m_packageTable.getSelectionModel().addListSelectionListener(
         new ListSelectionListener()
         {

            public void valueChanged(@SuppressWarnings("unused")
               ListSelectionEvent event)
            {               
               handleButtonState();
            }
            
         });
      JScrollPane scrollPane = new JScrollPane(m_packageTable);      
      add(scrollPane, "grow");
      JPanel commandPanel = createCommandPanel();
      add(commandPanel, "dock south, align left");      
      
      handleButtonState();
   }
   
   /**
    * Add a list selection listener to the selection table.
    * @param listener
    */
   void addListSelectionListener(ListSelectionListener listener)
   {
      m_packageTable.getSelectionModel().addListSelectionListener(listener);
   }
   
   private JPanel createCommandPanel()
   {
      JPanel panel = new JPanel();
      MigLayout layout = new MigLayout(
         "left",
         "[][][][]",
         "[]");
      panel.setLayout(layout);
      m_newButton = createButton("button.new");
      
      panel.add(m_newButton, "sg 1");
      m_editButton = createButton("button.edit");
      
      panel.add(m_editButton, "sg 1");
      m_deleteButton = createButton("button.delete");
      
      panel.add(m_deleteButton, "sg 1");
      m_buildButton = createButton("button.build");
      
      panel.add(m_buildButton, "sg 1");
      m_exportButton = createButton("button.export");
     
      panel.add(m_exportButton, "sg 1");
      
      return panel;
   }
   
   /**
    * Refresh the table to keep it up to date.
    */
   public void refreshTable()
   {
      try
      {
         List<PSPackageDescriptorMetaInfo> data = 
            PSPackagerClient.getFrame().getModel().getDescriptors(true);
         m_model.setData(data);
      }
      catch (PSDeployException e)
      {
         PSPackagerClient.getErrorDialog().showError(e, true,
            PSResourceUtils.getCommonResourceString("errorTitle"));
      }
      
   }
   
   /**
    * Selects the row containing the specified meta info if
    * it exists.
    * @param info
    */
   public void select(PSPackageDescriptorMetaInfo info)
   {
      int idx = m_model.getItemIndex(info);
      if(idx != -1)
      { 
         ListSelectionModel selectionModel = 
            m_packageTable.getSelectionModel();
         selectionModel.setSelectionInterval(idx, idx);

      }
   }
   
   /**
    * Clear all table entries.
    */
   public void clearTable()
   {
      m_model.setData(
         new ArrayList<PSPackageDescriptorMetaInfo>());
   }
   
   /**
    * Handle the enable/disable state for the buttons. 
    */
   public void handleButtonState()
   {
      boolean hasSelection = m_packageTable.getSelectedRowCount() > 0;
      PSServerConnectionManager connMgr = 
         PSServerConnectionManager.getInstance();
      boolean isConnected = connMgr.getConnection() != null &&
         connMgr.getConnection().isConnected();
      m_newButton.setEnabled(isConnected);
      m_editButton.setEnabled(hasSelection);
      m_deleteButton.setEnabled(hasSelection);
      m_buildButton.setEnabled(hasSelection);
      m_exportButton.setEnabled(hasSelection);
   }
   
   /* (non-Javadoc)
    * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
    */
   public void actionPerformed(ActionEvent event)
   {
      Object source = event.getSource();
      final PSPackagerClientModel model = PSPackagerClient.getFrame().getModel();
      final PSPackageDescriptorMetaInfo info = getSelectedDescriptorInfo();
      try
      {
         if(source == m_newButton)
         {
            onNew(model);
         }
         if(source == m_deleteButton && info != null)
         {
            onDelete(info, model);
         }
         if(source == m_exportButton && info != null)
         {          
           onExport(info, model);
         }
         if(source == m_editButton && info != null)
         {
            
            onEdit(info, model);
         }
         if(source == m_buildButton)
         {            
           onBuild(info, model);
         }
      }
      catch (PSDeployException e)
      {
         PSPackagerClient.getErrorDialog().showError(e, true,
            PSResourceUtils.getCommonResourceString("errorTitle"));
      }
      
   }
   
   /**
    * Action that occurs when new is clicked.
    * @param model cannot be <code>null</code>.
    */
   void onNew(PSPackagerClientModel model) throws PSDeployException
   {
      if(model == null)
         throw new IllegalArgumentException("model cannot be null.");
      model.newDescriptor();
   }
   
   /**
    * Action that occurs when edit is clicked.
    * @param info cannot be <code>null</code>.
    * @param model cannot be <code>null</code>.
    */
   void onEdit(final PSPackageDescriptorMetaInfo info,
      final PSPackagerClientModel model)
   {
      if(info == null)
         throw new IllegalArgumentException("info cannot be null.");
      if(model == null)
         throw new IllegalArgumentException("model cannot be null.");
      Runnable r = new Runnable()
      {
         public void run()
         {
            try
            {
               model.editDescriptor(info);
            }
            catch (final PSDeployException e)
            {
               SwingUtilities.invokeLater(new Runnable()
               {
                  public void run()
                  {
                     PSPackagerClient.getErrorDialog().showError(e, true,
                        PSResourceUtils.getCommonResourceString("errorTitle"));                           
                  }
                  
               });
            }                  
         }
      };
      Thread t = new Thread(r);
      t.start();
   }
   
   /**
    * Action that occurs when delete is clicked.
    * @param info cannot be <code>null</code>.
    * @param model cannot be <code>null</code>.
    */
   void onDelete(PSPackageDescriptorMetaInfo info, PSPackagerClientModel model) 
      throws PSDeployException
   {
      if(info == null)
         throw new IllegalArgumentException("info cannot be null.");
      if(model == null)
         throw new IllegalArgumentException("model cannot be null.");
      String msg = MessageFormat.format(
         getResourceString("msg.warning.delete.descriptor"),
         new Object[]{info.getName()});
      int confirm = JOptionPane.showConfirmDialog(this,
               msg,
               PSResourceUtils.getCommonResourceString("warningTitle"),
               JOptionPane.YES_NO_OPTION,
               JOptionPane.WARNING_MESSAGE);
            if(confirm == JOptionPane.YES_OPTION)
               model.deleteDescriptor(info);
   }
   
   /**
    * Action that occurs when build is clicked.
    * @param info cannot be <code>null</code>.
    * @param model cannot be <code>null</code>.
    */
   void onBuild(final PSPackageDescriptorMetaInfo info,
      final PSPackagerClientModel model)
   {
      if(info == null)
         throw new IllegalArgumentException("info cannot be null.");
      if(model == null)
         throw new IllegalArgumentException("model cannot be null.");
    //Ask for target dir, quit if cancel
      PSTargetDirectoryDialog dialog = 
         new PSTargetDirectoryDialog(PSPackagerClient.getFrame(), null, false);
      if(dialog.showTargetDirectoryDialog())
      {
         model.setLocalPkgDir(dialog.getPath());
      }
      else
      {
         return;
      }
      Runnable r = new Runnable()
      {
         public void run()
         {
            try
            {                 
               if(info != null)
                  model.build(info);                   
            }
            catch (final Exception e)
            {
               SwingUtilities.invokeLater(new Runnable()
               {
                  public void run()
                  {
                     PSPackagerClient.getErrorDialog().showError(
                        e, true,
                        PSResourceUtils.getCommonResourceString("errorTitle"));                              
                  }                     
               });
            }           
         }
         
      };
      Thread t = new Thread(r);
      t.start();
   }
   
   /**
    * Action that occurs when export is clicked.
    * @param info cannot be <code>null</code>.
    * @param model cannot be <code>null</code>.
    */
   void onExport(final PSPackageDescriptorMetaInfo info,
      final PSPackagerClientModel model)
   {
      if(info == null)
         throw new IllegalArgumentException("info cannot be null.");
      if(model == null)
         throw new IllegalArgumentException("model cannot be null.");
    //Ask for target dir, quit if cancel
      PSTargetDirectoryDialog dialog = 
         new PSTargetDirectoryDialog(PSPackagerClient.getFrame(), 
               getSelectedDescriptorInfo(),true);
      if(dialog.showTargetDirectoryDialog())
      {
         Runnable r = null;
         model.setLocalExportDir(dialog.getPath());
         if(dialog.getOperation() == PSTargetDirectoryDialog.TYPE_CONFIGDEF)
         {
            r = new Runnable()
            {
               public void run()
               {
                  try
                  {
                     model.createConfigDef(info);
                  }
                  catch (final PSDeployException e)
                  {
                     SwingUtilities.invokeLater(new Runnable()
                     {
                        public void run()
                        {
                           PSPackagerClient.getErrorDialog().showError(e, true,
                              PSResourceUtils.getCommonResourceString("errorTitle"));                           
                        }
                     });
                  }                        
               }
            };
         }
         else if(dialog.getOperation() == PSTargetDirectoryDialog.TYPE_DEFAULTCONFIG)
         {
            r = new Runnable()
            {
               public void run()
               {
                  try
                  {
                     model.createDefaultConfig(info);
                  }
                  catch (final PSDeployException e)
                  {
                     SwingUtilities.invokeLater(new Runnable()
                     {
                        public void run()
                        {
                           PSPackagerClient.getErrorDialog().showError(e, true,
                              PSResourceUtils.getCommonResourceString("errorTitle"));                           
                        }
                     });
                  }                        
               }
            };
         }
         else
         {
            r = new Runnable()
            {
               public void run()
               {
                  try
                  {
                     model.createSummary(info);
                  }
                  catch (PSDeployException e)
                  {
                     PSPackagerClient.getErrorDialog().showError(e, true,
                        PSResourceUtils.getCommonResourceString("errorTitle"));
                  }                        
               }                     
            };
         }
         Thread t = new Thread(r);
         t.start();
      }
   }
   
   /**
    * Retrieves selected package descriptor info object.
    * @return the selected info object or <code>null</code> if
    * no selection.
    */
   public PSPackageDescriptorMetaInfo getSelectedDescriptorInfo()
   {
      int row = m_packageTable.getSelectedRow();
      if(row == -1)
         return null;
      return m_model.getFromIndex(row);
   }
   
   /**
    * Helper method to create a button based on resource key and
    * add this class as actionlistener.
    * @param key assumed not <code>null</code> or
    * empty.
    * @return the button, never <code>null</code>.
    */
   private JButton createButton(String key)
   {
      JButton button = new JButton(getResourceString(key));
      button.setToolTipText(getResourceString(key + ".tt"));
      button.setMnemonic(
         getResourceString(key + ".m").charAt(0));
      button.addActionListener(this);
      return button;
   }
   
   /**
    * 
    * @param key
    * @return
    */
   private String getResourceString(String key)
   {
      return PSResourceUtils.getResourceString(getClass(), key);
   }
      
   
   /**
    * Date renderer that overrides set value to provide a date/time format
    */
   class PSCellRenderer extends DefaultTableCellRenderer
   {
      // see base class
      @Override
      public void setValue(Object value)
      {
         if (mi_formatter == null)
         {
            mi_formatter = DateFormat.getDateTimeInstance();
         }
         if(value == null)
         {
            setText("");
            return;
         }
         else if(value instanceof Date)
         {
            setText((value == null) ? "" : mi_formatter.format(value));
         }
         else
         {
            super.setValue(value);
         }
         
      }      

      /* (non-Javadoc)
       * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(
       * javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
       */
      @Override
      public Component getTableCellRendererComponent(JTable table,
               Object value, boolean isSelected, boolean hasFocus, int row,
               int column)
      {
         Component cell = super.getTableCellRendererComponent(
            table, value, isSelected, hasFocus, row, column);
         if (isSelected) 
         {
            cell.setBackground(SELECTED_ROW_COLOR);
         } 
          else 
         {
            if (row % 2 == 0)
            {
              cell.setBackground(ODD_ROW_COLOR);
            }
            else 
            {
              cell.setBackground(EVEN_ROW_COLOR);
            }
         }
         
         return cell;
      }



      /**
       * Formatter used to format date time values, <code>null</code> until
       * first call to {@link #setValue(Object)}, never <code>null</code> or
       * modified after that.
       */
      private DateFormat mi_formatter;      
   }
   
   /**
    * Custom table model for the descriptor table.
    * @author erikserating
    *
    */
   class DescriptorTableModel extends DefaultTableModel
   {
      /**
       * Ctor
       */
      public DescriptorTableModel()
      {
         addColumn(getResourceString("column.name"));
         addColumn(getResourceString("column.desc"));
         addColumn(getResourceString("column.last.modified"));
      }

      /* (non-Javadoc)
       * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
       */
      @Override
      public Object getValueAt(int row, int column)
      {
         PSPackageDescriptorMetaInfo descriptor = mi_data.get(row);
         switch(column)
         {
            case 0:
               return descriptor.getName();
            case 1:
               return descriptor.getDescription();
            case 2:
               return descriptor.getLastModified();
         }
         throw new ArrayIndexOutOfBoundsException("Column does not exist.");
         
      }
      
      /* (non-Javadoc)
       * @see javax.swing.table.DefaultTableModel#getColumnClass(int)
       */
      @SuppressWarnings("unchecked")
      @Override
      public Class getColumnClass(int c)
      {
         switch(c)
         {
            case 0:
               return String.class;
            case 1:
               return String.class;
            case 2:
               return Date.class;
         }
         throw new ArrayIndexOutOfBoundsException("Column does not exist.");

     }


      /* (non-Javadoc)
       * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
       */
      @Override
      public boolean isCellEditable(@SuppressWarnings("unused")
      int row, @SuppressWarnings("unused")
      int column)
      {
         return false;
      }       
      
      /* (non-Javadoc)
       * @see javax.swing.table.DefaultTableModel#getRowCount()
       */
      @Override
      public int getRowCount()
      {
        if(mi_data == null)
           return 0;
        return mi_data.size();
      }

      /**
       * Set the table's data.
       * @param data cannot be <code>null</code>, may be empty.
       */
      public void setData(List<PSPackageDescriptorMetaInfo> data)
      {
         if(data == null)
            throw new IllegalArgumentException("data cannot be null.");
         mi_data = data;
         fireTableDataChanged();
      }
      
      /**
       * Get meta info from specified row index
       * @param idx
       * @return the package descriptor meta info object.
       */
      public PSPackageDescriptorMetaInfo getFromIndex(int idx)
      {
         if(idx >= mi_data.size())
            throw new ArrayIndexOutOfBoundsException();
         return mi_data.get(idx);
      }
      
      /**
       * Return index of meta info object in data. 
       * @param info may be <code>null</code>.
       * @return the index or -1 if not found.
       */
      public int getItemIndex(PSPackageDescriptorMetaInfo info)
      {
         if(info == null)
            return -1;
         return mi_data.indexOf(info);
      }
      
      /**
       * The data of the table. Never <code>null</code>, may
       * be empty.
       */
      private List<PSPackageDescriptorMetaInfo> mi_data = 
         new ArrayList<PSPackageDescriptorMetaInfo>();
   }
   
   /**
    * 
    */
   private DescriptorTableModel m_model;
   
   /**
    * 
    */
   private JTable m_packageTable;
   
   /**
    * 
    */
   private JButton m_editButton;
   
   /**
    * 
    */
   private JButton m_newButton;
   
   /**
    * 
    */
   private JButton m_deleteButton;
   
   /**
    * 
    */
   private JButton m_buildButton;
   
   /**
    * 
    */
   private JButton m_exportButton;
   
   /**
    * Constant for odd table row color.
    */
   private static final Color ODD_ROW_COLOR = new Color(240, 245, 250);
   
   /**
    * Constant for even table row color.
    */
   private static final Color EVEN_ROW_COLOR = new Color(230, 235, 240);
   
   /**
    * Constant for selected table row color.
    */
   private static final Color SELECTED_ROW_COLOR = new Color(242, 214, 146);
      
      

   

   
}
