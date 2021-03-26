/******************************************************************************
 *
 * [ BasePanel.java ]
 * 
 * COPYRIGHT (c) 1999 - 2004 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.UTComponents.UTFixedButton;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * A panel that supplies base functionality used for all directory service UI 
 * panels of the server administrator.
 */
public abstract class DirectoryServiceBasePanel extends JPanel 
   implements KeyListener, ListSelectionListener, ChangeListener, ITabDataHelper
{
   /**
    * Constructs the new base panel for the supplied parameters.
    * 
    * @param parent the parent frame, not <code>null</code>.
    * @param data the directory service data to initialize the panel with,
    *    not <code>null</code>. This object is used to store the
    *    data before it is saved to the server configuration and to transfer
    *    information between the various directory service panels.
    * @param config the server configuration to which the UI component is
    *    saved, not <code>null</code>.
    */
   public DirectoryServiceBasePanel(Frame parent, DirectoryServiceData data,
      ServerConfiguration config)
   {
      if (parent == null)
         throw new IllegalArgumentException("parent cannot be null");
      
      if (data == null)
         throw new IllegalArgumentException("data cannot be null");
         
      if (config == null)
         throw new IllegalArgumentException("config cannot be null");
         
      m_parent = parent;
      m_data = data;
      m_config = config;
   }
   
   /**
    * Constructs a new base panel. This constructor requires that the shared
    * resources among different instances are initialized. Shared resources 
    * can be initialized through the {@link BasePanel(Frame, 
    * DirectoryServiceData, ServerConfiguration) constructor}.
    * 
    * @param config the server configuration to which the UI component is
    *    saved, not <code>null</code>.
    */
   public DirectoryServiceBasePanel(Frame parent)
   {
      if (parent == null)
         throw new IllegalArgumentException("parent cannot be null");
      
      if (m_config == null || m_data == null)
         throw new IllegalStateException(
            "initialize m_config and m_data before using this constructor");

      m_parent = parent;
   }
   
   /**
    * @see KeyListener#keyPressed(KeyEvent)
    */
   public void keyPressed(KeyEvent event)
   {
      // noop
   }

   /**
    * @see KeyListener#keyReleased(KeyEvent)
    */
   public void keyReleased(KeyEvent event)
   {
      switch (event.getKeyCode())
      {
         case KeyEvent.VK_ESCAPE:
            m_table.clearSelection();
            break;
            
         case KeyEvent.VK_INSERT:
            onAdd();
            break;
            
         case KeyEvent.VK_DELETE:
            onDelete();
            break;
      }
   }

   /**
    * @see KeyListener#keyTyped(KeyEvent)
    */
   public void keyTyped(KeyEvent event)
   {
      // noop
   }
   
   /**
    * @see ListSelectionListener#valueChanged(ListSelectionEvent)
    */
   public void valueChanged(ListSelectionEvent event)
   {
      m_editButton.setEnabled(m_table.getSelectedRowCount() == 1);
      m_deleteButton.setEnabled(m_table.getSelectedRowCount() > 0);
   }
   
   /**
    * Creates a command panel with an Add, Edit and Delete button. The default
    * action implementation will do nothing and must be overridden if the
    * command panel is used.
    * 
    * @return the command panel, never <code>null</code>.
    */
   protected JPanel createCommandPanel()
   {
      JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      
      m_addButton = new UTFixedButton(getResources().getString("dir.add"));
      m_addButton.setMnemonic(getResources().getString("dir.add.mn").charAt(0));
      m_addButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
            onAdd();
         }
      });
      m_addButton.setDefaultCapable(true);
      panel.add(m_addButton);
      
      m_editButton = new UTFixedButton(getResources().getString("dir.edit"));
      m_editButton.setMnemonic(
              getResources().getString("dir.edit.mn").charAt(0));
      m_editButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
            onEdit();
         }
      });
      m_editButton.setEnabled(false);
      panel.add(m_editButton);
      
      m_deleteButton = new UTFixedButton(getResources().getString("dir.delete"));
      m_deleteButton.setMnemonic(
              getResources().getString("dir.delete.mn").charAt(0));
      m_deleteButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
            onDelete();
         }
      });
      m_deleteButton.setEnabled(false);
      panel.add(m_deleteButton);
      
      return panel;
   }
   
   /**
    * The method called through actions from the Add button.
    */
   protected abstract void onAdd();
   
   /**
    * The method called through actions from the Edit button.
    */
   protected abstract void onEdit();
   
   /**
    * The method called through actions from the Delete button.
    */
   protected abstract void onDelete();
   
   /**
    * Creates a panel with a table view to show a summary of all configured 
    * pieces for the current tab.
    * 
    * @param columnNames an array of <code>String</code> objects with all
    *    column names in the order from left to right, not <code>null</code>
    *    or empty.
    * @param rows the number of empty rows to be initialized.
    * @return the table panel, never <code>null</code>.
    */
   protected JPanel createTablePanel(Object[] columnNames, int rows)
   {
      if (columnNames == null)
         throw new IllegalArgumentException("columnNames cannot be null");
         
      if (columnNames.length == 0)
         throw new IllegalArgumentException("columnNames cannot be empty");
                  
      m_model = new DefaultTableModel(columnNames, rows);
      
      m_table = new JTable(m_model);
      m_table.setCellSelectionEnabled(false);
      m_table.setColumnSelectionAllowed(false);
      m_table.setRowSelectionAllowed(true);
      m_table.addKeyListener(this);
      m_table.getSelectionModel().addListSelectionListener(this);
      
      // all table cells are read-only
      m_table.setDefaultEditor(m_table.getColumnClass(0), null);
      
      JPanel panel = new JPanel(new BorderLayout());
      panel.add(new JScrollPane(m_table), BorderLayout.CENTER);
      
      return panel;
   }
   
   /**
    * Get the server configuration.
    * 
    * @return the server configuration, never <code>null</code>.
    */
   protected PSServerConfiguration getConfig()
   {
      return m_config.getServerConfiguration();
   }
   
   /**
    * Get the resource bundle used with this dialog.
    * 
    * @return returns the resources used with this dialog, never 
    *    <code>null</code>.
    */
   protected static ResourceBundle getResources()
   {
      return PSServerAdminApplet.getResources();
   }
   
   /**
    * Was this panel modified since the last save?
    * 
    * @return <code>true</code> if the current panel was modified since the
    *    last save operation, <code>false</code> otherwise.
    */
   protected boolean isModified()
   {
      return m_modified;
   }
   
   /**
    * Set the modified state flag.
    * 
    * @param modified the new modified state, <code>true</code> to indicate
    *    that this panel was modified, <code>false</code> otherwise.
    */
   protected void setModified(boolean modified)
   {
      m_modified = modified;
   }
   
   /**
    * Open the appropriate editor on double click.
    */
   protected void initPanel()
   {
      m_table.addMouseListener(new MouseAdapter()
      {
         public void mouseClicked(MouseEvent e)
         {
            if (m_editButton.isEnabled() && e.getClickCount() == 2)
            {
               onEdit();
            }
         }
      });
   }
   
   /**
    * Remove all rows from the table.
    */
   protected void removeAllRows()
   {
      for (int i=m_model.getRowCount()-1; i>=0; i--)
         m_model.removeRow(i);
   }
   
   /**
    * Initializes the tab data from the data stored in the tab local storage.
    */
   protected void initData()
   {
      removeAllRows();
   }
   
   /**
    * Add a new change listener to receive events when this object changes.
    * 
    * @param listener the new change listener to be added, not 
    *    <code>null</code>.
    */
   public void addChangeListener(ChangeListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener cannot be null");
         
      m_changeListeners.add(listener);
   }
   
   /**
    * Stop listening for change events of this object.
    * 
    * @param listener the listener to be removed, not <code>null</code>.
    */
   public void removeChangeListener(ChangeListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener cannot be null");
         
      m_changeListeners.remove(listener);
   }
   
   /**
    * Inform all registered listeners of a change of this object.
    * 
    * @param data the data object that changed, may be <code>null</code>.
    */
   protected void dataChanged(Object data)
   {
      if (data != null)
      {
         ChangeEvent event = new ChangeEvent(data);
         for (int i=0; i<m_changeListeners.size(); i++)
           ((ChangeListener) m_changeListeners.get(i)).stateChanged(event);
      }
   }

   /* (non-Javadoc)
    * @see ChangeListener#stateChanged(ChangeEvent)
    */
   public void stateChanged(ChangeEvent event)
   {
   }
   
   /**
    * Get the ccontainer panel of this panel.
    * 
    * @return the container panel of this panel, may be <code>null</code>.
    */
   protected DirectoryServicePanel getContainerPanel()
   {
      return m_containerPanel;
   }
   
   /**
    * Set the supplied container panel.
    * 
    * @param containerPanel the new container panel for this panel, may be
    *    <code>null</code>.
    */
   protected void setContainerPanel(DirectoryServicePanel containerPanel)
   {
      m_containerPanel = containerPanel;
   }

   /**
    * The list of all registered change listeners, never <code>null</code>, may
    * be empty. Use {@link addChangeListener(ChangeListener)} to register a
    * listener and {@link removeChangeListener(ChangeListener) to de-register
    * a listener.
    */
   private List m_changeListeners = new ArrayList();
   
   /**
    * The directory service data, initialized while constructed, updated
    * through various directory service editors, never <code>null</code>. This
    * is a shared resource among all directory service panels. Is is used to 
    * store data locally before it is saved to the server configuration and
    * to transfer data between the various directory service panels.
    */
   protected static DirectoryServiceData m_data = null;
   
   /**
    * The parent frame for this panel, initialized in constructor, never 
    * <code>null</code> or changed after that.
    */
   protected Frame m_parent = null;
   
   /**
    * The panel which is containing this panel. Set through 
    * {@link setContainerPanel(DirectoryServicePanel)}, may be 
    * <code>null</code>.
    */
   protected DirectoryServicePanel m_containerPanel = null;
   
   /**
    * The server configuration for which the directory services are configured,
    * initialized in constructor, never <code>null</code> after that. This is
    * shared among all directory service panels and used to save directory
    * service data to the server configuration.
    */
   protected static ServerConfiguration m_config = null;
   
   /**
    * The Add button for the command panel, initialized in 
    * {@link JPanel createCommandPanel()}, never <code>null</code> after that.
    */
   protected UTFixedButton m_addButton = null;
   
   /**
    * The Edit button for the command panel, initialized in 
    * {@link JPanel createCommandPanel()}, never <code>null</code> after that.
    */
   protected UTFixedButton m_editButton = null;
   
   /**
    * The Delete button for the command panel, initialized in 
    * {@link JPanel createCommandPanel()}, never <code>null</code> after that.
    */
   protected UTFixedButton m_deleteButton = null;
   
   /**
    * The table to show an overview of all configured pieces for the current 
    * tab. Initialized in {@link createTablePanel(Object[], int)}, never 
    * <code>null</code> after that.
    */
   protected JTable m_table = null;
   
   /**
    * The table model used to stored the data used with the table view. 
    * Initialized in {@link createTablePanel(Object[], int)), never 
    * <code>null</code> after that.
    */
   protected DefaultTableModel m_model = null;
   
   /**
    * A flag indicating whether or not the current panel was modified.
    */
   private boolean m_modified = false;
}
