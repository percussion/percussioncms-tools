/******************************************************************************
 *
 * [ PSDBMSMapsPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/


package com.percussion.deployer.ui;

import com.percussion.UTComponents.UTFixedButton;
import com.percussion.deployer.objectstore.PSDatasourceMap;
import com.percussion.deployer.objectstore.PSDbmsMapping;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

/**
* The panel which allows user to map the external dbms credentials.
*/
public class PSDBMSMapsPanel extends JPanel
{
  /**
   * Constructs the class with supplied mappings and initializes the panel with
   * data.
   * 
   * @param dbmsMappings the list of dbms mappings to be edited/viewed, may not
   * be <code>null</code>, can be empty.
   * @param editSource if <code>true</code> user will be allowed to 
   * add/edit/delete source credentials, shows 'Add', Edit', 'Delete' buttons 
   * and edit the mappings in the mappings table, otherwise not.
   * @param editMappings if <code>true</code> user will be allowed to edit the
   * mappings in the mappings table alone not add/edit/delete source 
   * credentials, otherwise not. Ignored if <code>editSource</code> is <code>
   * true</code>. 
   * @deprecated
   */
  public PSDBMSMapsPanel(Iterator dbmsMappings, boolean editSource, 
     boolean editMappings)
  {
     if ( true )   
        throw new IllegalStateException("Invalid initializer...");
     
     if(dbmsMappings == null)
        throw new IllegalArgumentException("dbmsMappings may not be null.");

     m_editType = EDIT_TYPE_NONE;
     if (editSource)
        m_editType = EDIT_TYPE_ALL;
     else if (editMappings)
        m_editType = EDIT_TYPE_MOD;
     
     init(dbmsMappings);
  }

    
  /**
   * Constructs the class with supplied mappings and initializes the panel with
   * data.
   * 
   * @param dbmsMappings the list of dbms mappings to be edited/viewed, may not
   * be <code>null</code>, can be empty.
   * @param editType One of the <code>EDIT_TYPE_XXX</code> constants to indicate
   * what level of editing should be enabled. 
   */
  public PSDBMSMapsPanel(Iterator dbmsMappings, int editType)
  {
     if(dbmsMappings == null)
        throw new IllegalArgumentException("dbmsMappings may not be null.");
     
     if (editType < 0)
        throw new IllegalArgumentException("invalid editType");
     
     m_editType = editType;
     init(dbmsMappings);
  }
  
  /**
   * Sets the data of this panel and refreshes the panel ui to represent new
   * data. Uses the supplied transforms handler to update the dbms map with
   * added/deleted mappings in this panel.
   * 
   * @param dbmsMappings the list of dbms mappings to set, may not
   * be <code>null</code>, can be empty.
   * @param handler the transforms handler, may not be <code>null</code>
   */
  public void setData(Iterator dbmsMappings, PSTransformsHandler handler)
  {
     if(dbmsMappings == null)
        throw new IllegalArgumentException("dbmsMappings may not be null.");
        
     if(handler == null)
        throw new IllegalArgumentException("handler may not be null.");         

     DataSourceModel model = 
        (DataSourceModel)m_sourceTable.getModel();
     model.setData(dbmsMappings);
     
     m_transformsHandler = handler;
     m_isDataModified = false;
  }
  
  /**
   * Sets the target database drivers. Must be called before displaying this
   * panel if the panel is constructed to edit mappings.
   * 
   * @param dataSrc the list of drivers, may not be <code>null</code> or empty.
   */
  public void setTargetDataSources(List dataSrc)
  {
     if(dataSrc == null || dataSrc.isEmpty())
     {
        throw new IllegalArgumentException(
           "drivers must be supplied to edit mappings.");
     }
     m_targetDatasources = dataSrc;
     Vector<String> data = new Vector<String>();         
     Iterator<String> iter = dataSrc.iterator();
     while(iter.hasNext())
        data.add(iter.next());
     m_targetDatasourceEditor = new JComboBox(data);    
  }
  
  /**
   * Sets the source datasources. Must be called before displaying this
   * panel if the panel is constructed to add new source credentials and add 
   * mappings for them.
   * 
   * @param dataSrc the list of drivers, may not be <code>null</code> or empty.
   */
  public void setSourceDatasources(List dataSrc)
  {
     if(dataSrc == null || dataSrc.isEmpty())
     {
        throw new IllegalArgumentException(
           "drivers must be supplied to edit mappings.");
     }
     m_sourceDatasources = dataSrc;
     
  }
  
  /**
   * Initializes the panel with the dbms mappings.
   * 
   * @param dbmsMappings the list of dbms mappings to be edited, assumed not 
   * <code>null</code>.
   */
  private void init(Iterator dbmsMappings)
  {
     try
     {
        if (null == ms_res)
           ms_res = ResourceBundle.getBundle(
                 getClass().getName() + "Resources", Locale.getDefault() );
     } 
     catch (MissingResourceException e)
     {
        e.printStackTrace();
     }
      
     if(ms_mapColumns.isEmpty())
     {                      
        ms_source = getResourceString("source");      
        ms_target = getResourceString("target");
        ms_mapColumns.add(ms_source);
        ms_mapColumns.add(ms_target);
     }

     setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));      
     setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder(
        getResourceString("mapSourceTarget")), 
        BorderFactory.createEmptyBorder(10,10,10,10)) );
             
     add(createSourcePanel(dbmsMappings));           
     add(Box.createVerticalGlue());            
  }
  
  /**
   * Enables or disables the buttons (Add, Edit, Delete) used for 
   * modifying the mappings list. Should be called only if this panel is 
   * constructed to modify the mappings list.
   * 
   * @param enable if <code>true</code> the buttons will be enabled, otherwise
   * disabled.
   * 
   * @throws IllegalStateException if the panel is not constructed to modify
   * the mappings list.
   */
  public void setPanelState(boolean enable)
  {
     if(m_editType < EDIT_TYPE_DEL)
        throw new IllegalStateException(
           "This method should not be called if you are not " + 
           "adding/deleting the mappings");
     
     if (m_editType == EDIT_TYPE_ALL)
     {
        m_addButton.setEnabled(enable);
        m_editButton.setEnabled(enable);        
     }
     else if ( m_editType == EDIT_TYPE_EDIT)
        m_editButton.setEnabled(enable);
     
     m_deleteButton.setEnabled(enable);                 
  }
  
  /**
   * Gets the resource string for the specified key.
   * 
   * @param key the resource key to check, assumed not <code>null</code> or
   * empty
   * 
   * @return the string if found, otherwise key itself, never <code>null</code>,
   * may be empty.
   */
  private String getResourceString(String key)
  {
     return PSDeploymentResources.getResourceString(ms_res, key);
  }
  
  /**
   * Creates panel with a table to display source credentials.
   * 
   * @param dbmsMappings the list of dbms mappings to be edited, assumed not 
   * <code>null</code>.
   * 
   * @return the created panel, never <code>null</code>
   */
  private JPanel createSourcePanel(Iterator dbmsMappings)
  {
     JPanel sourcePanel = new JPanel(new BorderLayout());
     JPanel centerPanel = new JPanel(new BorderLayout());

     m_sourceTable = new JTable(new DataSourceModel(dbmsMappings,true));
     m_sourceTable.getTableHeader().setReorderingAllowed(false);
               
     JScrollPane pane = new JScrollPane(m_sourceTable);
     pane.setPreferredSize(new Dimension(280, 100));
     pane.setAlignmentX(LEFT_ALIGNMENT);
     centerPanel.add(pane, BorderLayout.CENTER);
     centerPanel.setAlignmentY(TOP_ALIGNMENT);
     sourcePanel.add(centerPanel, BorderLayout.CENTER);
     
     if ( m_editType > EDIT_TYPE_NONE )
     {
        JPanel southBtnPanel = new JPanel();
        southBtnPanel.setLayout(new BoxLayout(southBtnPanel, BoxLayout.X_AXIS));
        southBtnPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 0));
        southBtnPanel.setAlignmentX(RIGHT_ALIGNMENT);         
        southBtnPanel.add(Box.createHorizontalStrut(10)); 
        
        switch (m_editType)
        {
           case EDIT_TYPE_ALL: 
              southBtnPanel.add(initAddButton());
              southBtnPanel.add(Box.createHorizontalStrut(10));
              southBtnPanel.add(initEditButton());
              southBtnPanel.add(Box.createHorizontalStrut(10));
              southBtnPanel.add(initDeleteButton()); 
              break;
           case EDIT_TYPE_DEL:
              southBtnPanel.add(initDeleteButton());   
              break;
           case EDIT_TYPE_EDIT:
              southBtnPanel.add(initEditButton());
              southBtnPanel.add(Box.createHorizontalStrut(10)); 
              break;
           case EDIT_TYPE_EDITDELETE:
              southBtnPanel.add(initEditButton());
              southBtnPanel.add(Box.createHorizontalStrut(10)); 
              southBtnPanel.add(initDeleteButton());
              southBtnPanel.add(Box.createHorizontalStrut(10)); 
              break;              
        }
        JPanel btnPanel = new JPanel(new BorderLayout());
        btnPanel.add(southBtnPanel, BorderLayout.EAST);
        sourcePanel.add(btnPanel, BorderLayout.SOUTH);
     }         
     return sourcePanel;
  }


   /**
    * A convenience method which initializes m_addButton.
    */
   private UTFixedButton initAddButton()
   {
      m_addButton = new UTFixedButton(getResourceString("add"));
      m_addButton.setMnemonic(getResourceString("add.mn").charAt(0));
      m_addButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            stopEditing();
            DataSourceModel model = (DataSourceModel) m_sourceTable.getModel();
            PSDataSourceSelectionDialog dlg = new PSDataSourceSelectionDialog(
                  (Dialog) getParentWindow(), null, null, m_sourceDatasources,
                  m_targetDatasources, model.getDbmsMappings());
            dlg.setVisible(true);
            if (dlg.isOk())
            {
               PSDbmsMapping mapping = new PSDbmsMapping(dlg.getDatasourceMap());
               model.addDbmsMapping(mapping);
               m_transformsHandler.addDbmsMapping(mapping);
            }
         }
      });
      return m_addButton;
   }


   /**
    * A convenience method which initializes m_deleteButton.
    */
   private UTFixedButton initDeleteButton()
   {
      m_deleteButton = new UTFixedButton(getResourceString("delete"));
      m_deleteButton.setMnemonic(getResourceString("delete.mn").charAt(0));
      m_deleteButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            stopEditing();
            DataSourceModel model = (DataSourceModel) m_sourceTable.getModel();
            int row = m_sourceTable.getSelectedRow();

            if (row >= 0 && row < model.getRowCount())
            {
               PSDbmsMapping mapping = model.getDbmsMapping(row);
               model.removeRow(row);
               m_transformsHandler.removeDbmsMapping(mapping);
            }
         }
      });
      return m_deleteButton;
   }


   /**
    * A convenience method which initializes m_editButton.
    */
   private UTFixedButton initEditButton()
   {
      m_editButton = new UTFixedButton(getResourceString("edit"));
      m_editButton.setMnemonic(getResourceString("edit.mn").charAt(0));
      m_editButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            stopEditing();
            int row = m_sourceTable.getSelectedRow();
            if (row >= 0)
            {
               DataSourceModel model = (DataSourceModel) m_sourceTable
                     .getModel();
               PSDbmsMapping mapping = model.getDbmsMapping(row);
               if (mapping != null)
               {
                  PSDatasourceMap eMap = model.getRowData(row);
                  PSDataSourceSelectionDialog dlg = new PSDataSourceSelectionDialog(
                        (Dialog) getParentWindow(), eMap.getSrc(), eMap
                              .getTarget(), m_sourceDatasources,
                        m_targetDatasources, model.getDbmsMappings());
                  dlg.setVisible(true);
                  if (dlg.isOk())
                  {
                     PSDatasourceMap dsMap = dlg.getDatasourceMap();
                     mapping.setSourceInfo(dsMap.getSrc());
                     mapping.setTargetInfo(dsMap.getTarget());
                     model.fireTableRowsUpdated(row, row);
                  }
               }
            }
         }
      });
      return m_editButton;
   }
    
  
  /**
   * Validates that all source credentials are mapped.
   * 
   * @return <code>true</code> if all credentials are mapped, otherwise <code>
   * false</code>
   */
  public boolean validateMappings()
  {
     stopEditing();
     
     DataSourceModel sourceModel = 
           (DataSourceModel)m_sourceTable.getModel();
     for (int i = 0; i < sourceModel.getRowCount(); i++) 
     {
        PSDbmsMapping mapping = sourceModel.getDbmsMapping(i);
        if(mapping != null && mapping.getTargetInfo() == null)
           return false;
     }
     return true;
  }
  
  /**
   * Stops editing of cells in the Mappings table and update the current target
   * information to the mapping.
   */
  public void stopEditing()
  {
     //Update the current selected mapping target information.
     int selRow = m_sourceTable.getSelectedRow();
     if(selRow >= 0 && selRow < m_sourceTable.getModel().getRowCount())
        updateMapping(m_sourceTable.getSelectedRow());
  }
  
  /**
   * Finds whether data is modified/updated by the user after data is loaded to
   * this panel.
   * 
   * @return <code>true</code> if it is modified, otherwise <code>false</code>
   */
  public boolean isDataModified()
  {
     return m_isDataModified;
  }
  
  /**
   * Sets the data represented by this panel is not modified by the user. 
   * Should be called if the container updates the data represented by this 
   * panel to the server.
   */
  public void dataNotModified()
  {
     m_isDataModified = false;
  }

  /**
   * Gets the list of dbms mappings represented by this panel
   * 
   * @return the list of <code>PSDbmsMapping</code>s, never <code>null</code>, 
   * may be empty.
   */
  public List getDBMSMappings()
  {
     List mappings = new ArrayList();
     DataSourceModel sourceModel = 
        (DataSourceModel)m_sourceTable.getModel();
     for (int i = 0; i < sourceModel.getRowCount(); i++) 
     {
        PSDbmsMapping mapping = sourceModel.getDbmsMapping(i);
        if(mapping != null)
           mappings.add(mapping);
     }
     
     return mappings;
  }

  /**
   * Inform the panel that data in its underlying model has been changed so the
   * tables may be updated to reflect the modified data.
   */
  public void dataChanged()
  {
     int sel = m_sourceTable.getSelectedRow();
     ((DataSourceModel)m_sourceTable.getModel()).fireTableDataChanged();
     // force row selection to update visible data
     if (sel >= 0 && sel < m_sourceTable.getRowCount())
        m_sourceTable.setRowSelectionInterval(sel, sel);
     else if (m_sourceTable.getRowCount() > 0)
        m_sourceTable.setRowSelectionInterval(0, 0);
  }

  /**
   * Adds a listener to be informed of any changes to any mappings.
   * The change event will have this panel instance as its source.
   * 
   * @param listener The listener, may not be <code>null</code>.
   */
  public void addChangeListener(ChangeListener listener)
  {
     if (listener == null)
        throw new IllegalArgumentException("listener may not be null");
     
     m_changeListeners.add(listener);
  }

  /**
   * Removes the supplied listener so it is no longer notified of changes. See
   * {@link #addChangeListener(ChangeListener)} for details.
   * 
   * @param listener The listener, may not be <code>null</code>.  If it has not
   * previously been added as a listener, the method simply returns.
   */
  public void removeChangeListener(ChangeListener listener)
  {
     if (listener == null)
        throw new IllegalArgumentException("listener may not be null");
     
     m_changeListeners.remove(listener);
  }
  
  /**
   * Notifies all change listeners. 
   */
  protected void notifyChangeListeners()
  {
     ChangeEvent e = new ChangeEvent(this);
     Iterator listeners = m_changeListeners.iterator();
     while (listeners.hasNext())
        ((ChangeListener)listeners.next()).stateChanged(e);
  }
  
  /**
   * Updates the mapping corresponding to selected index in source table with 
   * the current target info in the mapping table.
   * 
   * @param selIndex index of selected row in source table, assumed to be valid
   * source credential row.
   * @deprecated
   */
  private void updateMapping(int selIndex)
  {
     /**
      * This is not needed anymore since the the edit is not happening
      * inline, but a new dlg is opened and when the dlg is dismissed, the data
      * is sync-ed back. Leaving if for now - Vamsi
      */
     return;
  }
  
  /**
   * The parent container of this panel that is an instance of <code>Window
   * </code>. Recursively checks the parent tree until it finds a parent that 
   * is a window.
   * 
   * @return the parent window, may be <code>null</code> if it does not find.
   */
  private Window getParentWindow()
  {
     Container parent = getParent();
     while(parent != null && !(parent instanceof Window) )
     {
        parent = parent.getParent();
     }
     return (Window)parent;
  }

  /**
   * A datasource table model
   */
  private class DataSourceModel extends AbstractTableModel
  {
     public DataSourceModel(Iterator dataSrcMappings, boolean allowEdit)
     {
        m_allowEdit  = allowEdit;
        if(dataSrcMappings == null)
           return;
        setTableData(dataSrcMappings);         
     }

     /**
      * Sets the data of this model
      * 
      * @param dbmsMappings the list of dbms mappings to set on model, may not
      * be <code>null</code>, can be empty.
      */
     public void setData(Iterator dbmsMappings)
     {
        if(dbmsMappings == null)
           throw new IllegalArgumentException("dbmsMappings may not be null.");

        m_dbmsMappings.clear();
        while(dbmsMappings.hasNext())
        {
           PSDbmsMapping mapping = (PSDbmsMapping)dbmsMappings.next();
           m_dbmsMappings.add(mapping);                             
        }
        fireTableDataChanged();
     }
     
     /**
      * Removes the specified mapping row.
      * 
      * @param row the row index to remove, must be >= 0 and less than 
      * {@link #getRowCount() rowcount} of this model.
      * 
      * @throws IllegalStateException if the model is not allowed edit.
      */
     public void removeRow(int row)
     {
         if(!m_allowEdit)
           throw new IllegalStateException(
              "The model does not allow deleting rows");
              
        checkRow(row);
        if(row < m_dbmsMappings.size())
           m_dbmsMappings.remove(row);

        m_isDataModified = true;            
        fireTableDataChanged();
     }

     
     /**
      * Gets the id type mapping of the supplied row.
      * 
      * @param row the row index of mapping to get, must be >= 0 and less than 
      * {@link #getRowCount() rowcount} of this model.
      * 
      * @return the mapping, may be <code>null</code> if the row represents an
      * empty row.
      */
     private PSDbmsMapping getDbmsMapping(int row)
     {
        checkRow(row);
        if(row < m_dbmsMappings.size())
           return (PSDbmsMapping)m_dbmsMappings.get(row);
        else
           return null;
     }
     
     public ArrayList<PSDbmsMapping> getDbmsMappings()
     {
        return m_dbmsMappings;
     }
     /**
      * Adds the supplied mapping to this model data.
      * 
      * @param mapping the mapping to add, may not be <code>null</code>
      * 
      * @throws IllegalStateException if the model is not allowed edit.
      */
     public void addDbmsMapping(PSDbmsMapping mapping)
     {
        if(mapping == null)
           throw new IllegalArgumentException("mapping may not be null.");
           
        if(!m_allowEdit)
           throw new IllegalStateException(
              "The model does not allow adding new rows");
        
        m_dbmsMappings.add(mapping);
        m_isDataModified = true;         
        fireTableDataChanged();
     }

     
     private void clearTableData()
     {
        if ( m_dbmsMappings != null )
           m_dbmsMappings.clear();
     }
      
     private void addTableData(PSDbmsMapping mapping)
     {
        m_dbmsMappings.add(mapping);
     }
     
     public void setTableData(Iterator datasrcMapping)
     {
        if(datasrcMapping == null)
           throw new IllegalArgumentException("dbmsMappings may not be null.");

        clearTableData();
        
        while(datasrcMapping.hasNext())
        {
           PSDbmsMapping mapping = (PSDbmsMapping)datasrcMapping.next();
           addTableData(mapping);                             
        }
        fireTableDataChanged();
     }

     public int getRowCount()
     {
         if ( m_dbmsMappings.size() > MIN_ROWS )
            return m_dbmsMappings.size();
         else
            return MIN_ROWS;
     }
     
     /**
      * Checks that the supplied row exists in this model.
      * 
      * @param row the row index to check, must be >= 0 and less than 
      * {@link #getRowCount() rowcount} of this model.
      */
     private void checkRow(int row)
     {
        if(row < 0 || row >= getRowCount())
           throw new IllegalArgumentException("row must be between 0 and " + 
              (getRowCount()-1) + "inclusive");   
     }

    /**
     * Checks that the supplied column exists in this model.
     * 
     * @param col the column index to check, must be >= 0 and less than 
     * {@link #getRowCount() rowcount} of this model.
     */
    private void checkColumn(int col)
    {
       if(col < 0 || col >= getColumnCount())
          throw new IllegalArgumentException("col must be between 0 and " + 
             (getColumnCount()-1) + "inclusive");     
    }      

   
     /**
      * Gets the column name for the supplied column index.
      * 
      * @param col the column index of name to get, must be >= 0 and less than  
      * {@link #getColumnCount() columncount} of this model.
      * 
      * @return the column name, never <code>null</code> or empty.
      */
     public String getColumnName(int col)
     {
        checkColumn(col);       
        return (String)ms_mapColumns.get(col);
     }

     
     /**
      * Get the column count
      */
     public int getColumnCount()
     {
        return ms_mapColumns.size();
     }
   
     
     /**
      * 
      * @param row must be a valid number between 0 and datasize
      * @return PSDataSourceMapping if it exists else <code>null</code>.
      */
     private PSDatasourceMap getRowData(int row)
     {
        if ( row > -1 && row < m_dbmsMappings.size() )
        {
           PSDbmsMapping dbmsMap = (PSDbmsMapping) m_dbmsMappings.get(row);
           return dbmsMap.getDataSourceMap();
        }
        return null;
     }
     

     /**
      * Get a cell value, if the row and col indexes are in the range
      * else returns <code>null</code>
      */
     public Object getValueAt(int row, int col)
     {
        checkRow(row);
        checkColumn(col);
        
        PSDatasourceMap mapping = getRowData(row);
        if ( mapping != null && col > -1 && col < MAX_COLS)
           return (Object)mapping.getColumnData(col);
        return null;
     }
     
     /**
      * An array list of PSDbmsMappings
      */
     private ArrayList<PSDbmsMapping> m_dbmsMappings = 
                                      new ArrayList<PSDbmsMapping>();
     private boolean m_allowEdit = false;
     
     /**
      * The constant to indicate the minimum number of rows in model.
      */
     //private static final int SOURCE_INDEX = 0;
     //private static final int TARGET_INDEX = 1;
     private static final int MAX_COLS     = 2;
     private static final int MIN_ROWS     = 2;
  }
  
  
  /**
   * The flag to indicate whether mappings can be added/deleted or not, 
   * initialized in the ctor and is never modified after that.  One of the
   * <code>EDIT_TYPE_XXX</code> values. 
   */
  private int m_editType;
  
  /**
   * The flag to indicate whether target column of mapping is editable or not,
   * initialized in <code>createMappingsPanel(boolean)</code> and is never 
   * modified after that.
   */
  private boolean m_editTarget;
  
  /**
   * The flag to indicate whether data is modified in this panel by the user, 
   * after it is set with new data, initialized to <code>false</code> and 
   * updated to <code>true</code> whenever the data modifications occurs to the 
   * <code>m_sourceTable</code> or <code>m_mapTable</code> model. The flag 
   * might be reset by the container of this panel, whenever it updates the 
   * data to the server through a call to <code>dataNotModified()</code> or 
   * whenever it modifies data representing by this panel through a call to 
   * <code>setData(Iterator, PSTransformsHandler)</code>
   */
  private boolean m_isDataModified = false;
  
  /**
   * The table that represents the source credentials of mappings, initialized
   * in <code>createSourcePanel(Iterator, boolean)</code> and never <code>null
   * </code> after that.
   */
  private JTable m_sourceTable;
  
  /**
   * The combo box used for editing a datasource, initialized in the constructor
   * with the list of drivers to show,  may be <code>null</code> if the panel 
   * is not invoked to edit mappings. Never modified after initialization.
   */
  private JComboBox m_targetDatasourceEditor;

  /**
   * The list of source datasource to be presented to the user to add new source 
   * credentials, initialized to an empty list and gets updated with the list 
   * in <code>setSourceDrivers(List)</code>. 
   */
  private List m_sourceDatasources = new ArrayList();   
  private List m_targetDatasources = new ArrayList();
  
  
  /**
   * The handler that needs to be updated with the changes to the dbms mappings
   * list currently represented by this panel, initialized to <code>null</code>
   * and modified by calls to <code>setData(Iterator, PSTransformsHandler)
   * </code>
   */
  private PSTransformsHandler m_transformsHandler = null;
  
  /**
   * The button used to display a dialog to add a source credential, 
   * initialized in <code>createSourcePanel(Iterator, boolean)</code> and never
   * <code>null</code> or modified after that. The state may be modified by a 
   * call to {@link #setPanelState(boolean)}.
   */
  private UTFixedButton m_addButton;
  
  /**
   * The button used to display a dialog to edit the source credential, 
   * initialized in <code>createSourcePanel(Iterator, boolean)</code> and never
   * <code>null</code> or modified after that. The state may be modified 
   * by a call to {@link #setPanelState(boolean)}.
   */
  private UTFixedButton m_editButton;
  
  /**
   * The button used to delete the mapping, initialized in <code>
   * createSourcePanel(Iterator, boolean)</code> and never <code>null</code> or
   * modified after that. The state may be modified by a call to {@link 
   * #setPanelState(boolean)}.
   */
  private UTFixedButton m_deleteButton;
  
  /**
   * List of {@link ChangeListener} objects added and removed via 
   * {@link #addChangeListener(ChangeListener)} and 
   * {@link #removeChangeListener(ChangeListener)}.  Never <code>null</code>,
   * may be empty.
   */
  private List m_changeListeners = new ArrayList();
  
  
  /**
   * The list of column names for a source to target map table, initialized to 
   * an empty list and elements are added while initializing this panel in 
   * <code>createMappingPanel(boolean)</code>, never <code>null</code> or 
   * modified after that.
   */   
  private static Vector<String> ms_mapColumns = new Vector<String>();
  
  
  /**
   * The resource string to represent the source column name in map table, 
   * initialized in <code>createMappingPanel(boolean)</code>, never <code>null
   * </code> or modified after that.
   */
  private static String ms_source;
  
  /**
   * The resource string to represent the target column name in map table, 
   * initialized in <code>createMappingPanel(boolean)</code>, never <code>null
   * </code> or modified after that.
   */
  private static String ms_target;
  
  /**
   * The resource bundle used to get the resource strings of this panel, 
   * initialized in <code>init()</code>, may be <code>null</code> if it could
   * not load, never modified after that.
   */
  private static ResourceBundle ms_res;
  
  /**
   * The constant to indicate the row index of the 'driver' information in map 
   * table.
   */
  //private static final int SOURCE_INDEX = 0;
  
  /**
   * The constant to indicate the row index of the 'server' information in map 
   * table.
   */
  //private static final int TARGET_INDEX = 1;
    
  /**
   * Constant to indicate the mappings are read only.
   */
  public static final int EDIT_TYPE_NONE = 0;

  /**
   * Constant to indicate the mappings are editable, but the add/edit/delete
   * buttons are unavailable.
   */
  public static final int EDIT_TYPE_MOD = 1;
  
  /**
   * Constant to indicate the mappings are editable, but add and edit
   * buttons are unavailable.
   */
  public static final int EDIT_TYPE_DEL = 2;
  
  /**
   * Constant to indicate the mappings are editable, and the add/edit/delete
   * buttons are available. 
   */
  public static final int EDIT_TYPE_ALL = 3;
  
  /**
   * Constant to indicate the mapping panel to have edit button
   */
  public static final int EDIT_TYPE_EDIT = 4;
  
  /**
   * Constant to indicate the mapping panel to have edit/delete buttons. Will be
   * used by PSIDMapsDialog
   */
  public static final int EDIT_TYPE_EDITDELETE = 5;
}
