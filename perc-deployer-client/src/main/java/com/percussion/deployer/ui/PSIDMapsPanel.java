/******************************************************************************
 *
 * [ PSIDMapsPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.deployer.ui;

import com.percussion.UTComponents.UTFixedButton;
import com.percussion.deployer.catalog.PSCatalogResult;
import com.percussion.deployer.catalog.PSCatalogResultSet;
import com.percussion.deployer.objectstore.PSIdMapping;
import com.percussion.error.PSDeployException;
import com.percussion.guitools.ErrorDialogs;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;

/**
 * The panel which allows user to map the elements.
 */
public class PSIDMapsPanel extends JPanel implements ListSelectionListener
{
   /**
    * Constructs the class with supplied mappings and initializes the panel with
    * data.
    * 
    * @param idMappings the list of id mappings to be edited, may not be 
    * <code>null</code>, can be empty.
    * @param editSource if <code>true</code> user will be allowed to 
    * add/edit/delete the mappings and shows 'Add', 'Edit','Delete' buttons, 
    * otherwise not. 
    * @param editMappings if <code>true</code> user will be allowed to edit the
    * mappings only not the add/delete, otherwise not. Ignored if <code>
    * editSource</code> is <code>true</code>. If <code>editSource</code> is 
    * <code>false</code> and this is <code>true</code> it allows the editing of
    * the mappings in the table, not through the 'Edit' button which leads to a
    * separate dialog to edit the mapping, and will also add the 'Guess All' 
    * button to the panel.
    * @param source the server from which the source elements need to be 
    * cataloged for each element type (id type), may be <code>null</code> if
    * <code>editSource</code> is <code>false</code>, or to
    * allow editing and deleting but not adding if <code>editSource</code> is
    * <code>true</code>.  Must be connected if supplied and 
    * <code>editSource</code> is <code>true</code>.
    * @param types the list of all possible element types, may not be <code>
    * null</code> or empty.
    * @param showAll if <code>true</code> shows all types in the 'Element Types' 
    * list box, otherwise shows only types that have mappings.
    */
   public PSIDMapsPanel(Iterator idMappings, boolean editSource, 
      boolean editMappings, PSDeploymentServer source, PSCatalogResultSet types,
      boolean showAll)
   {      
      if(idMappings == null)
         throw new IllegalArgumentException("idMappings may not be null.");
         
      if( types == null || !types.getResults().hasNext() )
      {
         throw new IllegalArgumentException(
            "types may not be null or empty.");
      }
      
      if(editSource)
      { 
         if(source != null && !source.isConnected())
            throw new IllegalArgumentException(
               "if source is not null it must be connected " + 
               "in case of adding element mappings");
               
         m_source = source;
      }      
      m_isShowAll = showAll;
      
      //If any of the types don't have mappings, put an empty list of mappings
      //Update the list of types to show in list box.
      m_types = new Vector();
      Iterator iter = types.getResults();
      while(iter.hasNext())
      {
         PSCatalogResult type = (PSCatalogResult)iter.next();
         m_types.add(type);         
      }      
      updateMappingsAndTypes(idMappings);
      init(editSource, editMappings);
   }
   
   /**
    * Sets the data of this panel and refreshes the panel ui to represent new
    * data. Uses the supplied transforms handler to get or guess the target
    * elements for mappings or add the new mappings added in the panel. Should
    * be called after constructing the panel if the panel is constructed to 
    * support modifying mappings.
    * 
    * @param idMappings the list of id mappings to set, may not
    * be <code>null</code>, can be empty.
    * @param handler the transforms handler, may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if an error happens checking parent type.
    */
   public void setData(Iterator idMappings, PSTransformsHandler handler) 
      throws PSDeployException
   {
      if(idMappings == null)
         throw new IllegalArgumentException("idMappings may not be null.");
         
      if(handler == null)
         throw new IllegalArgumentException("handler may not be null.");
         
      m_transformsHandler = handler;
      m_isDataModified = false;
      
      updateMappingsAndTypes(idMappings);
      
      //update the list box with the types, add all types that has parent at the
      //end.
      m_typesList.clearSelection();      
      DefaultListModel listModel = (DefaultListModel)m_typesList.getModel();
      listModel.removeAllElements();      

      Iterator types = orderTypes(m_showTypes, handler);
      while(types.hasNext())
         listModel.addElement(types.next());       
         
      if(listModel.size() > 0)
         m_typesList.setSelectedIndex(0);      
   }
   
   /**
    * Orders the supplied element types with all the types that supports parent
    * types to come after the parent types. If a type has a parent type and the
    * parent type does not exist in the supplied list, then that type will be 
    * added to the end of the list.
    * 
    * @param elementTypes the list of types to order, assumed not <code>null
    * </code> or empty.
    * @param handler the handler to use for checking parent types, assumed not 
    * <code>null</code>
    * 
    * @return the ordered types, never <code>null</code> or empty.
    * 
    * @throws PSDeployException If there are any errors.
    */
   private Iterator orderTypes(List elementTypes, PSTransformsHandler handler)
      throws PSDeployException
   {
      List orderedTypes = new ArrayList();
      List childTypes = new ArrayList();
      Iterator types = elementTypes.iterator();
      //First add all types that don't have parent types.
      while(types.hasNext())
      {
         PSCatalogResult type = (PSCatalogResult)types.next();
         if(handler.hasParentType(type.getID()))
         {
            childTypes.add(type);
         }
         else {
            orderedTypes.add(type);
         }
      }
      
      Iterator children = childTypes.iterator();
      while(children.hasNext())
      {
         PSCatalogResult type = (PSCatalogResult)children.next();
         if(getIndex(orderedTypes, type.getID()) != -1)
            continue;
            
         //recursively get parent types
         List childParents = new ArrayList();         
         childParents.add(type);         
         int parentIndex;
         do {
            String parentType = handler.getTarget().getParentType(type.getID());
            parentIndex = getIndex(orderedTypes, parentType);         
            if(parentIndex == -1)
            {
               type = getType(elementTypes, parentType);                        
               if(type != null)
                  childParents.add(type);               
            }
         }while(parentIndex == -1 && type != null);
            
         //add the parents first and then children
         Collections.reverse(childParents);
         Iterator iter = childParents.iterator();        
         if(parentIndex != -1)
         {
            while(iter.hasNext())
               orderedTypes.add(++parentIndex, iter.next());
         }
         else
         {
            while(iter.hasNext())
               orderedTypes.add(iter.next());
         }         
      }
      
      return orderedTypes.iterator();
   }
   
   /**
    * Gets the type element of the specified type in supplied list of types. 
    * Uses the id of type for comparison.
    * 
    * @param types the list of types (<code>PSCatalogResult</code>), assumed not
    * <code>null</code> or empty.
    * @param type the type to get, assumed not <code>null</code> or empty.
    * 
    * @return the type element if this type exists in the list, otherwise <code>
    * null</code>.
    */
   private PSCatalogResult getType(List types, String type)
   {
      Iterator iter = types.iterator();
      while(iter.hasNext())
      {
         PSCatalogResult elemType = (PSCatalogResult)iter.next();
         if(elemType.getID().equals(type))
            return elemType;
      }
      
      return null;
   }
   
   /**
    * Gets the index of specified type in supplied list of types. Uses the id of
    * type for comparison.
    * 
    * @param types the list of types (<code>PSCatalogResult</code>), assumed not
    * <code>null</code>, may be empty.
    * @param type the type to check, assumed not <code>null</code> or empty.
    * 
    * @return the index if this type exists in the list, otherwise -1.
    */
   private int getIndex(List types, String type)
   {
      Iterator iter = types.iterator();
      int i = 0;
      while(iter.hasNext())
      {
         PSCatalogResult elemType = (PSCatalogResult)iter.next();
         if(elemType.getID().equals(type))
            return i;
         i++;
      }      
      return -1;
   }
   
   /**
    * Updates the mappings and the types to show in this panel.
    * 
    * @param idMappings the id mappings to show, assumed not to be <code>null
    * </code>
    */
   private void updateMappingsAndTypes(Iterator idMappings)
   {
      //group them according to their element type (object type)
      m_objTypeIdMappings = new HashMap();
      while(idMappings.hasNext())
      {
         PSIdMapping mapping = (PSIdMapping)idMappings.next();
         List objTypeIdMappings = 
            (List)m_objTypeIdMappings.get(mapping.getObjectType());
         if(objTypeIdMappings == null)
         {
            objTypeIdMappings = new ArrayList();
            m_objTypeIdMappings.put(mapping.getObjectType(), objTypeIdMappings);
         }
         objTypeIdMappings.add(mapping);
      }

      m_showTypes = new Vector();
      //Fill the types to show in the list-box
      Iterator iter = m_types.iterator();
      while(iter.hasNext())
      {      
         PSCatalogResult type = (PSCatalogResult)iter.next();
         if(m_isShowAll)
         {
            m_showTypes.add(type);
            if( !m_objTypeIdMappings.containsKey(type.getID()) )
               m_objTypeIdMappings.put(type.getID(), new ArrayList());                     
         } 
         else if(m_objTypeIdMappings.containsKey(type.getID()))
            m_showTypes.add(type); //adds only if this type has some mappings to show
      }
   }
   
   /**
    * Initializes the panel framework.
    * 
    * @param editSource if <code>true</code> user will be allowed to 
    * add/edit/delete the mappings, otherwise not.
    * @param editMappings if <code>true</code> user will be allowed to edit the
    * mappings, otherwise not. Ignored if <code>editSource</code> is <code>true
    * </code> and allows editing mappings.
    */
   private void init(boolean editSource, boolean editMappings)
   {
      try
      {
         if (null == ms_res)
            ms_res = ResourceBundle.getBundle(
                  getClass().getName() + "Resources", Locale.getDefault() );
      } catch (MissingResourceException e)
      {
         e.printStackTrace();
      }

      setLayout(new BorderLayout());
      setBorder( BorderFactory.createEmptyBorder(10,10,10,10) );       
            
      m_editSource = editSource;
      m_editMappings = editSource || editMappings;

      add(createListPanel(), BorderLayout.NORTH);
      add(createMappingsPanel(), BorderLayout.CENTER);
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
    * Creates the top panel with element types list box and description labels
    * aside.
    * 
    * @return the created panel, never <code>null</code>
    */
   private JPanel createListPanel()
   {
      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
      
      JPanel listPanel = new JPanel();
      listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.X_AXIS));
      listPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
      listPanel.setPreferredSize(new Dimension(200, 100));
      
      String labelStr = getResourceString("elementTypes");
      char mn = getResourceString("elementTypes.mn").charAt(0);
      JLabel elemTypesLabel = new JLabel(labelStr);
      elemTypesLabel.setDisplayedMnemonic(mn);
      elemTypesLabel.setDisplayedMnemonicIndex(labelStr.indexOf(mn));
      
      listPanel.add(elemTypesLabel);
      
      DefaultListModel listModel = new DefaultListModel();
      Iterator types = m_showTypes.iterator();
      while(types.hasNext())
         listModel.addElement(types.next());
      m_typesList = new JList(listModel);
      m_typesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      m_typesList.addListSelectionListener(this);
      JScrollPane listPane = new JScrollPane(m_typesList);
      listPane.setAlignmentY(CENTER_ALIGNMENT);
      elemTypesLabel.setLabelFor(m_typesList);
      
      listPanel.add(Box.createHorizontalStrut(10));
      listPanel.add(listPane);
      listPanel.add(Box.createHorizontalGlue());
 
      JPanel descPanel = new JPanel();      
      descPanel.setLayout(new BoxLayout(descPanel, BoxLayout.Y_AXIS));
      descPanel.setAlignmentY(CENTER_ALIGNMENT);
      descPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
      
      //If mappings are editable, give description to select element type to
      //view and edit otherwise view alone.
      if(m_editMappings)
      {
         descPanel.add(
            new JLabel(getResourceString("genEditDesc"), SwingConstants.LEFT));
      }
      else
      {
         descPanel.add(
            new JLabel(getResourceString("genDesc"), SwingConstants.LEFT));      
      }
      
      //If can add/delete mappings, show buttons description.
      if(m_editSource)
      {
         String descKey = (m_source == null) ? "useButtonsDesc" : 
            "useButtonsSourceDesc";
         descPanel.add(new JLabel(getResourceString(descKey), 
            SwingConstants.LEFT));
      }
      
      //If can edit mappings, show 'how to set target' description.
      if(m_editMappings)
      {
         JLabel mapDescLabel = new JLabel(getResourceString("mapDesc"), 
            SwingConstants.LEFT);
         descPanel.add(mapDescLabel);
         // this label gets cut off, seems like the preferred size does not
         // calculate properly, so pad its length
         Dimension dim = mapDescLabel.getPreferredSize();
         mapDescLabel.setPreferredSize(new Dimension(dim.width + 
            dim.width / 6, dim.height));
      }
      
      panel.add(descPanel, BorderLayout.NORTH);
      panel.add(listPanel, BorderLayout.CENTER);
      
      
      return panel;
   }
   
   /**
    * Creates panel with a table to display mapping of selected element type
    * and buttons to add/edit/delete mappings if it is allowed to do so.
    * 
    * @return the created panel, never <code>null</code>
    */
   private JPanel createMappingsPanel()
   {
      JPanel mappingsPanel = new JPanel();
      mappingsPanel.setLayout(new BorderLayout());
      mappingsPanel.setAlignmentX(LEFT_ALIGNMENT);
      
      JPanel leftPanel = new JPanel();
      leftPanel.setLayout(new BorderLayout());
      leftPanel.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createTitledBorder(
         getResourceString("map")), 
         BorderFactory.createEmptyBorder(5,5,5,5)));
      
      if(ms_mapColumns.isEmpty())
      {
         ms_source = getResourceString("source");
         ms_add = getResourceString("addServer");
         ms_target = getResourceString("target");                  
         ms_mapColumns.add(ms_source);
         ms_mapColumns.add(ms_add);
         ms_mapColumns.add(ms_target);         
      }
         
      m_mapTable = new JTable(new MappingsModel(m_editSource))
      {
         /**
          * Called by swing model before showing the editor for the cell. So
          * overridden to update the target elements that need to be shown in 
          * editor component for target column editor combo-box.
          */
         public TableCellEditor getCellEditor(int row, int col) 
         {  
            MappingsModel model = (MappingsModel)getModel();
            PSIdMapping mapping = model.getIdMapping(row);
            if(model.getColumnName(col).equals(ms_add) ||
               model.getColumnName(col).equals(ms_target))
            {
               if(!validateParentMapped(row))
                  return null;
                  
               if(model.getColumnName(col).equals(ms_target))
               {
                  try {
                     m_targetEditor.removeAllItems();                  
                     List targets = 
                        m_transformsHandler.getUnmappedTargetElements(
                        mapping.getObjectType(), mapping.getParentType(),
                        mapping.getSourceParentId() );
                     //Get current editing target and add it.
                     if(mapping.isMapped() && !mapping.isNewObject())
                        targets.add(model.getValueAt(row, col));                     
                     if(targets.isEmpty())
                     {                     
                        ErrorDialogs.showErrorMessage(getParentWindow(), 
                           getResourceString("noExistTarget"), 
                           getResourceString("information"));
                        return null;
                     }
                     else
                     {
                        Collections.sort(targets);
                        Iterator targetItems = targets.iterator();                      
                        while(targetItems.hasNext())
                           m_targetEditor.addItem( targetItems.next() );
                     }
                  }
                  catch(PSDeployException e)
                  {
                     ErrorDialogs.showErrorMessage(getParentWindow(), 
                        e.getLocalizedMessage(), getResourceString("error"));
                  }           
               }                                       
            }
            return super.getCellEditor(row, col);
         }
      };
      m_mapTable.setSelectionMode(
         ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      m_mapTable.getSelectionModel().addListSelectionListener(this);
      m_mapTable.getTableHeader().setReorderingAllowed(false);
      m_mapTable.getColumn(ms_add).setCellRenderer(
         new AddToServerColumnRenderer());
      if(m_editMappings)
      {
         m_mapTable.getColumn(ms_target).setCellEditor(
            new DefaultCellEditor(m_targetEditor));
      }
      
      JScrollPane pane = new JScrollPane(m_mapTable);
      pane.setPreferredSize(new Dimension(200, 125));
      leftPanel.add(pane, BorderLayout.CENTER);
      mappingsPanel.add(leftPanel, BorderLayout.CENTER);
      
      JPanel buttonPanel = new JPanel();
      buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
      buttonPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
      buttonPanel.add(Box.createVerticalStrut(15));         
      
      if(m_editSource)
      {
         if (m_source != null)
         {
            m_addButton = new UTFixedButton(getResourceString("add"));
            m_addButton.setMnemonic(getResourceString("add.mn").charAt(0));
            m_addButton.addActionListener(new ActionListener() 
            {
               public void actionPerformed(ActionEvent e) 
               {
                  try {
                     getParentWindow().setCursor(
                        Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                     PSCatalogResult selType = 
                        (PSCatalogResult)m_typesList.getSelectedValue();
                     String elementType = selType.getID();               
                     List srcElements = 
                        m_transformsHandler.getUnmappedSourceElements(
                        m_source, elementType);
                     if(srcElements.isEmpty())
                     {
                        ErrorDialogs.showErrorMessage(getParentWindow(), 
                           getResourceString("noMoreSource"), 
                           getResourceString("error"));
                        return;
                     }                  
                     PSIDMappingDialog dlg = new PSIDMappingDialog(
                        (Dialog)getParentWindow(), selType, null, 
                        m_transformsHandler, m_source);
                     dlg.setVisible(true);
                     if(dlg.isOk())
                     {
                        MappingsModel model = (MappingsModel)m_mapTable.getModel();
                        model.addIdMapping(dlg.getIdMapping());
                        m_transformsHandler.addIdMapping(dlg.getIdMapping());
                     } 
                  }
                  catch(PSDeployException ex)
                  {
                     ErrorDialogs.showErrorMessage(getParentWindow(), 
                        ex.getLocalizedMessage(), getResourceString("error"));
                  }
                  finally 
                  {
                     getParentWindow().setCursor(
                        Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                  }
               }
            });
            
            m_editButton = new UTFixedButton(getResourceString("edit"));
            m_editButton.setMnemonic(getResourceString("edit.mn").charAt(0));
            m_editButton.addActionListener(new ActionListener() 
            {
               public void actionPerformed(ActionEvent e) 
               {
                  try {
                     PSCatalogResult selType = 
                        (PSCatalogResult)m_typesList.getSelectedValue();
                     int row = m_mapTable.getSelectedRow();
                     if(row >= 0)
                     {
                        MappingsModel model = (MappingsModel)m_mapTable.getModel();
                        PSIdMapping mapping = model.getIdMapping(row);                                          
                        if(!validateParentMapped(row))
                           return;                 
                           
                        PSIDMappingDialog dlg = new PSIDMappingDialog(
                           (Dialog)getParentWindow(), selType, mapping, 
                           m_transformsHandler, null);
                        dlg.setVisible(true);
                        if(dlg.isOk())
                        {
                           m_isDataModified = true;                     
                           model.fireTableRowsUpdated(row, row);
                        }
                     }
                  }
                  catch(PSDeployException ex)
                  {
                     ErrorDialogs.showErrorMessage(getParentWindow(), 
                        ex.getLocalizedMessage(), getResourceString("error"));
                  }
               }
            });         

            buttonPanel.add(m_addButton);
            buttonPanel.add(Box.createVerticalStrut(10));
            buttonPanel.add(m_editButton);
            buttonPanel.add(Box.createVerticalStrut(10));
         }
         
         m_deleteButton = 
            new UTFixedButton(getResourceString("delete"));
         m_deleteButton.setMnemonic(getResourceString("delete.mn").charAt(0));
         m_deleteButton.addActionListener(new ActionListener() 
         {
            public void actionPerformed(ActionEvent e) 
            {               
               MappingsModel model = (MappingsModel)m_mapTable.getModel();
               int[] rows = m_mapTable.getSelectedRows();
               for (int i = 0; i < rows.length; i++) 
               {
                  PSIdMapping mapping = model.getIdMapping(rows[i]);
                  if(mapping != null)
                     m_transformsHandler.removeIdMapping(mapping);
               }
               model.removeRows(rows);
            }
         });

         buttonPanel.add(m_deleteButton);           
         buttonPanel.add(Box.createVerticalStrut(10));         
      }      

      //add the guess button only if edit is possible.
      if(m_editMappings)
      {
         m_guessButton = 
            new UTFixedButton(getResourceString("guess"));
         m_guessButton.addActionListener(new ActionListener() 
         {
            public void actionPerformed(ActionEvent e) 
            {
               try {
                  getParentWindow().setCursor(Cursor.getPredefinedCursor(
                     Cursor.WAIT_CURSOR));            
                  //If we are allowed to add mappings, create mappings for all 
                  // the source elements that are not yet added by user and 
                  // guess the target for the mappings.
                  MappingsModel model = (MappingsModel)m_mapTable.getModel();            
                  PSCatalogResult selType = 
                     (PSCatalogResult)m_typesList.getSelectedValue();               
                  if(m_editSource && m_source != null)
                  {
                     Iterator mappings = m_transformsHandler.
                        createUnmappedSourceMappings(m_source, selType.getID());
      
                     while(mappings.hasNext())
                        model.addIdMapping((PSIdMapping)mappings.next());
                  }
                  // guess targets for all elements that are not mapped.  
                  int rows = model.getRowCount();
                  List unMappedMappingElements = new ArrayList();            
                  List mappingsList = new ArrayList();
                  for (int i = 0; i < rows; i++) 
                  {
                     PSIdMapping mapping = model.getIdMapping(i);
                     if(mapping != null && !mapping.isMapped())
                     {
                        mappingsList.add(mapping);
                     }
                  }                  
                  
                  List unMappedMappings = m_transformsHandler.guessTarget(
                     mappingsList.iterator());            
                  
                  Set parents = new HashSet();
                  for (int i = 0; i < rows; i++) 
                  {
                     PSIdMapping mapping = model.getIdMapping(i);
                     if(unMappedMappings.contains(mapping))
                     {
                        PSMappingElement el = 
                           (PSMappingElement)model.getValueAt(
                           i, m_mapTable.getColumn(ms_source).getModelIndex());
                        unMappedMappingElements.add(el);
                        parents.add(el.getParentDisplayString());
                     }
                  }
                  
                  m_isDataModified = true;
                  model.fireTableDataChanged();        
                  
                  if(!unMappedMappingElements.isEmpty())
                  {
                     String[] args = new String[4];
                     args[0] = selType.getDisplayText();
                     args[1] = unMappedMappingElements.toString();                  
                     args[2] = m_transformsHandler.getTarget()
                        .getTypeDisplayName(
                           m_transformsHandler.getTarget().getParentType(
                              selType.getID()));                  
                     args[3] = parents.toString();                  
                     ErrorDialogs.showErrorMessage(getParentWindow(), 
                        MessageFormat.format(
                        getResourceString("notGuessedMappings"), args), 
                        getResourceString("information"));
                  }
                  notifyChangeListeners();
               }
               catch(PSDeployException ex)
               {
                  ErrorDialogs.showErrorMessage(getParentWindow(), 
                     ex.getLocalizedMessage(), getResourceString("error"));
               } 
               finally {
                  getParentWindow().setCursor(Cursor.getPredefinedCursor(
                     Cursor.DEFAULT_CURSOR));            
               }
            }
         });      
         buttonPanel.add(m_guessButton); 
         m_guessButton.setMnemonic(getResourceString("guess.mn").charAt(0));
      }
      
      // add button panel if it's not empty
      if (buttonPanel.getComponentCount() > 0)
         mappingsPanel.add(buttonPanel, BorderLayout.EAST);      
         
      return mappingsPanel;   
   }
   
   /**
    * Validate that the parent of the source element represented by the suppled 
    * row is mapped if the source element has a parent.
    * 
    * @param row the row index representing the mapping.
    * 
    * @return <code>true</code> if the source element does not have a parent or 
    * if it has a parent that is mapped, otherwise <code>false</code>
    */
   private boolean validateParentMapped(int row)
   {
      MappingsModel model = (MappingsModel)m_mapTable.getModel();
      PSIdMapping mapping = model.getIdMapping(row);   
      
      if(mapping.getParentType() != null && 
         !m_transformsHandler.getIdMap().isMapped(
         mapping.getSourceParentId(), mapping.getParentType()) )
      {
         PSMappingElement source = (PSMappingElement) model.getValueAt(row,
            m_mapTable.getColumn(ms_source).getModelIndex());
         try { 
            String[] args = { 
               source.getParentName() + "(" + source.getParentId() + ")",
               m_transformsHandler.getTarget().getTypeDisplayName(
               source.getParentType()), 
               source.toString() };
            ErrorDialogs.showErrorMessage(getParentWindow(), 
               MessageFormat.format(getResourceString("mustMapParent"), 
               args), 
               getResourceString("error"));
         }
         catch(PSDeployException ex)
         {
            ErrorDialogs.showErrorMessage(getParentWindow(), 
               ex.getLocalizedMessage(), getResourceString("error"));
         }
         return false;                     
      }    
      
      return true;
   }
   
   /**
    * Validates that all source elements are mapped.
    * 
    * @return <code>true</code> if all mappings are mapped, otherwise <code>
    * false</code>
    */
   public boolean validateMappings()
   {
      // don't stop editing if in the middle of a notification, as a stop 
      // editing has already been triggered and calling it again will reset the
      // cell value to the previous selection
      if (!m_isInNotification)
         stopEditing();
      
      Iterator iter = m_objTypeIdMappings.values().iterator();
      while(iter.hasNext())
      {
         List mappings = (List)iter.next();
         Iterator mapIter = mappings.iterator();
         while(mapIter.hasNext())
         {
            PSIdMapping mapping = (PSIdMapping)mapIter.next();
            if(!mapping.isMapped())
               return false;
         }
      }
      return true;
   }

   /**
    * Gets the list of id mappings represented by this panel.
    * 
    * @return the list of <code>PSIdMapping</code>s that are being edited in 
    * this panel, never <code>null</code>, may be empty.
    */
   public List getIdMappings()
   {
      List mappings = new ArrayList();
      Iterator iter = m_objTypeIdMappings.values().iterator();
      while(iter.hasNext())
         mappings.addAll((List)iter.next());

      return mappings;
   }
   
   /**
    * Stops editing of cells in the Mappings table.
    */
   public void stopEditing()
   {
      if(m_mapTable.isEditing())
      {
         m_mapTable.getCellEditor(m_mapTable.getEditingRow(), 
            m_mapTable.getEditingColumn()).stopCellEditing(); 
      }
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
    * Enables or disables the panel to use. Basically enables or disables the 
    * buttons (Add, Edit, Delete, Guess) used for modifying the mappings list. 
    * Should be called only if this panel is constructed to modify the mappings 
    * list.
    * 
    * @param enable if <code>true</code> the buttons will be enabled, otherwise
    * disabled.
    * 
    * @throws IllegalStateException if the panel is not constructed to modify
    * the mappings list.
    */
   public void setPanelState(boolean enable)
   {
      if(!m_editSource)
         throw new IllegalStateException(
            "This method should not be called if you are not " + 
            "adding/deleting the mappings");
      
      if (m_source != null)
      {
         m_addButton.setEnabled(enable);
         m_editButton.setEnabled(enable);         
      }
      m_deleteButton.setEnabled(enable);
      m_guessButton.setEnabled(enable);                     
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
    * Action method for selection change events in list. Updates the mappings
    * in the map table with the selected element type in the 'Element Types' 
    * list.
    * 
    * @param e the event that characterizes the change, assumed not to be 
    * <code>null</code> as this method will be called by <code>Swing</code> 
    * model when selection changes in the list
    */   
   public void valueChanged (ListSelectionEvent e)
   {
      if(e.getValueIsAdjusting())
         return;

      if(e.getSource() == m_typesList)
      {
         stopEditing();
            
         PSCatalogResult selType = 
            (PSCatalogResult)m_typesList.getSelectedValue();
         List idMappings = null;
         if(selType != null)
            idMappings = (List)m_objTypeIdMappings.get(selType.getID());
         MappingsModel model = (MappingsModel)m_mapTable.getModel();
         model.setData(idMappings);
      }
      else if(e.getSource() == m_mapTable)
      {
         if(m_editSource)
         {
            int[] sel_indices = m_mapTable.getSelectedRows();
            if(sel_indices.length == 0)
            {
               if (m_editButton != null)
                  m_editButton.setEnabled(false);
               m_deleteButton.setEnabled(false);
            }
            else
            {
               m_deleteButton.setEnabled(true);
               if (m_editButton != null)
               {
                  if(sel_indices.length == 1 && 
                     ((MappingsModel)m_mapTable.getModel()).getIdMapping(
                     sel_indices[0]) != null) 
                  {
                     m_editButton.setEnabled(true);
                  }
                  else
                     m_editButton.setEnabled(false);                  
               }
            }
         }
      }
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
      if (!m_isInNotification)
      {
         m_isInNotification = true;  // avoid recursion
         while (listeners.hasNext())
            ((ChangeListener)listeners.next()).stateChanged(e);
         m_isInNotification = false;         
      }
   }
   
   /**
    * The model that should be used with mapping table.
    */
   private class MappingsModel extends AbstractTableModel
   {
      /**
       * Constructs this model.
       * 
       * @param allowEdit if <code>true</code> allows to add/delete mappings, 
       * otherwise not
       * 
       */
      public MappingsModel(boolean allowEdit)
      {         
         m_allowEdit = allowEdit;                  
      }       
      
      /**
       * Sets the id mappings data on this table model.
       * 
       * @param idMappings the list of id mappings, if <code>null</code> an 
       * empty list is set as data.
       */
      public void setData(List idMappings)
      {
         if(idMappings == null)
            m_idMappings.clear();
         else            
            m_idMappings = idMappings;
            
         fireTableChanged(new TableModelEvent(this));
      }

      /**
       * Gets the number of data rows in this table model.
       * 
       * @return the number of rows, may be <code>0</code>
       */
      public int getRowCount()
      {
         if(m_idMappings.size() > MIN_ROWS)
            return m_idMappings.size();
            
         return MIN_ROWS;
      }
      
      /**
       * Gets the number of columns in this table model.
       * 
       * @return the number of rows, may be <code>0</code>
       */
      public int getColumnCount()
      {
         return ms_mapColumns.size();
      }
      
      /**
       * Gets the column name for the supplied column index.
       * 
       * @param col the column index of name to get, must be >= 0 and less than  
       * {@link #getColumnCount() columncount} of this model.
       * 
       * @return the column name, never <code>null</code> or empty.
       * 
       * @throws IllegalArgumentException if column index is invalid.
       */
      public String getColumnName(int col)
      {
         checkColumn(col);       
         return (String)ms_mapColumns.get(col);
      }
   
      /**
       * Gets the column class for each column. Called by Swing model to show 
       * default renderer for the column of the table. 
       * 
       * @param col the column to check for, must be >= 0 and less than  
       * {@link #getColumnCount() columncount} of this model.
       * 
       * @return the column class, <code>Boolean</code> for 'Add to server' 
       * column and <code>String</code> for other columns, never <code>null
       * </code>
       * @throws IllegalArgumentException if column index is invalid.
       */   
      public Class getColumnClass(int col) 
      {
         checkColumn(col);
         
         if(getColumnName(col).equals(ms_add))
            return Boolean.class;
         else
            return String.class;
      }
      
      /**
       * Checks that the supplied row exists in this model.
       * 
       * @param row the row index to check, must be >= 0 and less than 
       * {@link #getRowCount() rowcount} of this model.
       * 
       * @throws IllegalArgumentException if row index is invalid.
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
        * 
        * @throws IllegalArgumentException if column index is invalid.
        */
      private void checkColumn(int col)
      {
         if(col < 0 || col >= getColumnCount())
            throw new IllegalArgumentException("col must be between 0 and " + 
               (getColumnCount()-1) + "inclusive");     
      }
      
      /**
       * Checks whether the supplied cell is editable or not. The cell is 
       * editable if all of the following conditions are satisfied.
       * <ol>
       * <li>The panel is constructed to edit mappings</li>
       * <li>The row represents a mapping (not an empty row)</li>
       * <li>The column of the cell is either 'Add to server' or 'target' column
       * </li>
       * </ol>
       * 
       * @param row the row index of cell, must be >= 0 and less than 
       * {@link #getRowCount() rowcount} of this model.
       * @param col the column index of cell, must be >= 0 and less than 
        * {@link #getRowCount() rowcount} of this model.
       * 
       * @return <code>false</code> 
       * 
       * @throws IllegalArgumentException if any parameter is invalid
       */
      public boolean isCellEditable(int row, int col)
      {
         checkRow(row);
         checkColumn(col);
         
         if(m_editMappings)
         {
            if( row < m_idMappings.size() &&
               (getColumnName(col).equals(ms_target) ||
                getColumnName(col).equals(ms_add)) )
            {
               return true;
            }
         }
         return false;
      }
      
      /**
       * Gets the id mapping of the supplied row.
       * 
       * @param row the row index of mapping to get, assumes be >= 0 and less  
       * than {@link #getRowCount() rowcount} of this model.
       * 
       * @return the mapping, may be <code>null</code> if the row represents an
       * empty row.
       * 
       * @throws IllegalArgumentException if row index is invalid.
       */
      private PSIdMapping getIdMapping(int row)
      {
         if(row < m_idMappings.size())
            return (PSIdMapping)m_idMappings.get(row);
         else
            return null;
      }
      
      /**
       * Gets the value at specified row and column.
       * 
       * @param row the row index of value to get, must be >= 0 and less than 
       * {@link #getRowCount() rowcount} of this model.
       * @param col the column index of value to get, must be >= 0 and less than  
       * {@link #getColumnCount() columncount} of this model.
       * 
       * @throws IllegalArgumentException if any param is invalid.
       */
      public Object getValueAt(int row, int col)
      {
         checkRow(row);
         checkColumn(col); 
        
         PSIdMapping mapping = getIdMapping(row);
            
         if(mapping != null)
         {
            String columnName = getColumnName(col);
            if(columnName.equals(ms_source))
            {     
               PSMappingElement element = new PSMappingElement(
                  mapping.getObjectType(), mapping.getSourceId(), 
                  mapping.getSourceName());
               if(mapping.getParentType() != null)
               {
                  element.setParent(mapping.getParentType(), 
                     mapping.getSourceParentId(), mapping.getSourceParentName());
               }
               return element;                  
            }
            else if(columnName.equals(ms_add))
            {
               return mapping.isNewObject();
            }
            else 
            {
               if(!mapping.isNewObject() && mapping.getTargetId() != null)
               {
                  PSMappingElement element = new PSMappingElement(
                     mapping.getObjectType(), mapping.getTargetId(), 
                     mapping.getTargetName());
                  if(mapping.getParentType() != null)
                  {
                     element.setParent(mapping.getParentType(), 
                        mapping.getTargetParentId(), 
                        mapping.getTargetParentName());
                  }
                  return element;
               }
            }
         }
         
         return null;
      }
      
      /**
       * Sets the value at the specified cell. Called by Swing model when the 
       * cell editor updates the value.
       *
       * @param value value to assign to cell, may be <code>null</code>
       * @param row the row index of value to get, must be >= 0 and less than 
       * {@link #getRowCount() rowcount} of this model.
       * @param col the column index of value to get, must be >= 0 and less than  
       * {@link #getColumnCount() columncount} of this model.
       * 
       * @throws IllegalArgumentException if any parameter is invalid.
       */
       public void setValueAt(Object value, int row, int col) 
       {
         checkRow(row);
         checkColumn(col);
         
         String columnName = getColumnName(col);
         PSIdMapping mapping = getIdMapping(row);            
         if(mapping != null)
         {
            if(columnName.equals(ms_add))
            {
               mapping.setIsNewObject(((Boolean)value).booleanValue());               
            }
            else if(columnName.equals(ms_target))
            {
               PSMappingElement element = (PSMappingElement)value;
               mapping.setIsNewObject(false);
               if(element != null)
               {
                  if(mapping.getParentType() != null)
                  {
                     mapping.setTarget(element.getId(), 
                        element.getName(), element.getParentId(), 
                           element.getParentName());
                  }
                  else
                  {
                     mapping.setTarget(
                        element.getId(), element.getName());
                  }
               }
            }
            else
               throw new IllegalStateException("Source column is not editable");
            m_isDataModified = true;
            fireTableRowsUpdated(row, row);
            notifyChangeListeners();
         }
         else
            throw new IllegalStateException("Empty row is not editable");
         
       }
      
      /**
       * Adds the supplied mapping to this model data.
       * 
       * @param mapping the mapping to add, may not be <code>null</code>
       * 
       * @throws IllegalArgumentException if mapping is <code>null</code>
       * @throws IllegalStateException if the model is not allowed edit.
       */
      public void addIdMapping(PSIdMapping mapping)
      {
         if(mapping == null)
            throw new IllegalArgumentException("mapping may not be null.");
            
         if(!m_allowEdit)
            throw new IllegalStateException(
               "The model does not allow adding new rows");
         m_isDataModified = true;         
         m_idMappings.add(mapping);         
         int size = m_idMappings.size();
         if(m_idMappings.size() < MIN_ROWS)
            fireTableRowsUpdated(size-1, size-1); 
         else
            fireTableRowsInserted(size-1, size-1);
         notifyChangeListeners();
      }
      
      /**
       * Removes the rows corresponding to the indices specifed in the supplied 
       * array.
       * 
       * @param rows the array of row indices to remove, may not be <code>null
       * </code> and indices must be >= 0 and less than {@link #getRowCount() 
       * rowcount} of this model.
       * 
       * @throws IllegalArgumentException if rows is invalid.
       * @throws IllegalStateException if the model is not allowed edit.
       */
      public void removeRows(int[] rows)
      {
         if(rows == null)
            throw new IllegalArgumentException("rows may not be null");
            
         if(!m_allowEdit)
            throw new IllegalStateException(
               "The model does not allow deleting rows");
         
         int prevSize = m_idMappings.size();      
         for (int i = rows.length-1; i >= 0; i--) 
         {
            checkRow(rows[i]);
            if(rows[i] < m_idMappings.size())
               m_idMappings.remove(rows[i]);            
         }
         m_isDataModified = true;         
         fireTableChanged(new TableModelEvent(this, 0, prevSize));
         notifyChangeListeners();
      }

      /**
       * The flag to indicate whether data rows can be added/deleted to/from 
       * this model or not, <code>true</code> to indicate allow modification and
       * vice versa. Initialized in the constructor and never modified after 
       * that.
       */      
      private boolean m_allowEdit;
      
      /**
       * The data mappings of this model, initialized to an empty list, may be
       * modified in the constructor or by calls to <code>
       * addIdMapping(PSIdMapping)</code> or <code>removeRow(int)</code>.
       * Never <code>null</code> after initialization.
       */
      private List m_idMappings = new ArrayList();
      
      /**
       * The constant to indicate the minimum number of rows in model.
       */
      private static final int MIN_ROWS = 9;
   }
   
   /**
    * The renderer to be used with 'Add To Server' column. Displays a <code>
    * JCheckBox</code> if the cell value represents a <code>Boolean</code>, 
    * otherwise a <code>JLabel</code>
    */
   private class AddToServerColumnRenderer implements TableCellRenderer
   {
      //implements interface method to display check-box if the row represents a 
      //mapping otherwise a label.
      public Component getTableCellRendererComponent(JTable table, Object value,
         boolean isSelected, boolean hasFocus, int row, int column)
      {
         Component renderer;
         if (value instanceof Boolean)
         {
            renderer = new JCheckBox();
            ((JCheckBox)renderer).setHorizontalAlignment(SwingConstants.CENTER);
            ((JCheckBox)renderer).setSelected(((Boolean)value).booleanValue());
         }
         else
         {
            renderer = new JLabel();
         }         
         
         if (isSelected) 
         {
            renderer.setForeground(table.getSelectionForeground());
            if(renderer instanceof JCheckBox)
               renderer.setBackground(table.getSelectionBackground());
         }
         else {
            renderer.setForeground(table.getForeground());
            if(renderer instanceof JCheckBox)            
               renderer.setBackground(table.getBackground());
         }

         return renderer;
      }
   }
   
   /**
    * Command used for action event to notify any guess all listener supplied
    * during construction.  
    */
   public static final String GUESS_ALL_ACTION = "guessAllAction";
   
   /**
    * The flag to indicate whether mappings are editable or not, initialized in 
    * <code>init()</code> and is never modified after that. <code>true</code>
    * indicates that the mappings are editable, otherwise not.
    */
   private boolean m_editMappings;
   
   /**
    * The flag to indicate whether mappings can be added/deleted or not, 
    * initialized in <code>init()</code> and is never modified after that. 
    * <code>true</code> indicates that the mappings list can be modifyable, 
    * otherwise not.
    */
   private boolean m_editSource;
   
   /**
    * The flag to indicate whether data is modified in this panel by the user, 
    * after it is set with new data, initialized to <code>false</code> and 
    * updated to <code>true</code> whenever the data modifications occurs to the 
    * <code>m_mapTable</code> model. The flag might be reset by the container of
    * this panel, whenever it updates the data to the server through a call to 
    * <code>dataNotModified()</code> or whenever it modifies data representing by
    * this panel through a call to <code>setData(Iterator, PSTransformsHandler)
    * </code>
    */
   private boolean m_isDataModified = false;
   
   /**
    * The handler that needs to be updated with the changes to the id mappings
    * list currently represented by this panel, initialized to <code>null</code>
    * and modified by calls to <code>setData(Iterator, PSTransformsHandler)
    * </code>. Used to guess or get the target elements for the mapping.
    */
   private PSTransformsHandler m_transformsHandler;
   
   /**
    * The server from which the source elements need to be cataloged for each 
    * element type to map the element on source to target, initialized in the 
    * constructor in case of modifying the mappings list and never <code>null
    * </code> or modified after that. May be <code>null</code> if the panel does
    * not allow adding/editing the mappings.
    */
   private PSDeploymentServer m_source = null;
   
   /**
    * The map of id mappings grouped with object type (<code>String</code>) as 
    * key and <code>List</code> of <code>PSIdMapping</code> of that object type
    * as value, initialized in the constructor and the list of each object type
    * may be modified as user adds/deletes mappings of that type. Will have 
    * entries for each object type specified in <code>m_types</code>. Never 
    * <code>null</code> after it is initialized.
    */
   private Map m_objTypeIdMappings;
   
   /**
    * The table that represents the mappings of the selected type in the <code>
    * m_typesList</code>, initialized in <code>createMappingsPanel(boolean)
    * </code> and never <code>null</code> or modified after that.
    */   
   private JTable m_mapTable;
   
   /**
    * The combo box used to edit the target element of the mapping, initialized
    * in <code>createMappingsPanel(boolean, boolean)</code> and never <code>null
    * </code> after that. The data (target elements) for the editor is set based
    * on the selected element type in <code>m_typesList</code>.
    */
   private JComboBox m_targetEditor = new JComboBox();
   
   /**
    * The list box used for displaying element types, initialized in the <code>
    * init()</code> method and never <code>null</code> or modified after that.
    */
   private JList m_typesList;
   
   /**
    * The button used to display a dialog to add a mapping, initialized in 
    * <code>createMappingsPanel()</code> and never <code>null</code> or modified
    * after that. The state may be modified by a call to {@link 
    * #setPanelState(boolean)}.
    */
   private UTFixedButton m_addButton;
   
   /**
    * The button used to display a dialog to edit the mapping, initialized in 
    * <code>createMappingsPanel()</code> and never <code>null</code> or modified
    * after that. Enabled only if a single row that represents a mapping is 
    * selected in the mappings table, otherwise not. The state may be modified 
    * by a call to {@link #setPanelState(boolean)}.
    */
   private UTFixedButton m_editButton;
   
   /**
    * The button used to display a dialog to delete the mappings, initialized in 
    * the constructor and never <code>null</code> or modified after that. 
    * Enabled only if there are selected rows in the mappings table. The state 
    * may be modified by a call to {@link #setPanelState(boolean)}.
    */
   private UTFixedButton m_deleteButton;
   
   /**
    * The button used to guess targets for unmapped mappings,  initialized in 
    * <code>createMappingsPanel()</code> and never <code>null</code> or modified
    * after that. The state may be modified by a call to {@link 
    * #setPanelState(boolean)}.
    */
   private UTFixedButton m_guessButton;
      
   /**
    * The list with each object identifying an element (object) type, 
    * initialized in the constructor and never <code>null</code>, empty or 
    * modified after that.
    */
   private Vector m_types;   
   
   /**
    * The list of element types that need to be shown in <code>m_typesList
    * </code>, initialized in the constructor and never <code>null</code> or 
    * empty after that. Modified by calls to <code>setData(Iterator, 
    * PSTransformsHandler)</code>
    */
   private Vector m_showTypes;   
   
   /**
    * The flag to indicate whether to show all element types in the list box or 
    * the element types for which the mappings are present, initialized in the
    * constructor and never modified after that. <code>true</code> indicates to
    * show all and vice-versa.
    */
   private boolean m_isShowAll;
   
   /**
    * List of {@link ChangeListener} objects added and removed via 
    * {@link #addChangeListener(ChangeListener)} and 
    * {@link #removeChangeListener(ChangeListener)}.  Never <code>null</code>,
    * may be empty.
    */
   private List m_changeListeners = new ArrayList();
   
   /**
    * Indicates currently notifying to avoid triggering recursive change events.
    */
   private boolean m_isInNotification = false;

   /**
    * The list of column names for a source to target map table, initialized to 
    * an empty list and elements are added while initializing this panel in 
    * <code>createMappingPanel(boolean)</code>, never <code>null</code> or 
    * modified after that.
    */   
   private static Vector ms_mapColumns = new Vector();
   
   /**
    * The resource string to represent the 'Add to Server' column in the map 
    * table, initialized in <code>createMappingPanel(boolean)</code>, never 
    * <code>null</code> or modified after that.
    */
   private static String ms_add;
   
   /**
    * The resource string to represent the 'Source' column in the map table, 
    * initialized in <code>createMappingPanel(boolean)</code>, never <code>null
    * </code> or modified after that.
    */
   private static String ms_source;
   
   /**
    * The resource string to represent the 'Target' column in the map table, 
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

}
