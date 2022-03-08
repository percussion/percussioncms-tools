/******************************************************************************
 *
 * [ PSIDTypesPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.deployer.ui;

import com.percussion.deployer.catalog.PSCatalogResult;
import com.percussion.deployer.catalog.PSCatalogResultSet;
import com.percussion.deployer.objectstore.PSApplicationIDTypeMapping;
import com.percussion.deployer.objectstore.PSApplicationIDTypes;
import com.percussion.error.PSDeployException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * The panel used to edit the id types on server for the given set of dependency
 * id types.
 */
public class PSIDTypesPanel extends JPanel implements TreeSelectionListener, 
   ListSelectionListener
{
   /**
    * Constructs the panel with the supplied list of id types to be edited and 
    * the list of allowed literal types.
    * 
    * @param idTypes the list of id types, may not be <code>null</code> or empty
    * @param types the set of literal types, may not be <code>null</code> or 
    * empty.
    * @param server the deployment server on which the application id
    * types will be saved, may not be <code>null</code> and must be connected.
    * @param incompleteOnly if <code>true</code> shows the id type mappings that
    * are incomplete only, otherwise all mappings.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    * @throws PSDeployException if there are any errors communicating with the
    * server.
    */
   public PSIDTypesPanel(Iterator idTypes, PSCatalogResultSet types, 
      PSDeploymentServer server, boolean incompleteOnly) 
         throws PSDeployException
   {      
      if(idTypes == null || !idTypes.hasNext())
         throw new IllegalArgumentException("idTypes may not be null or empty");  
      
      if(types == null || !types.getResults().hasNext())
         throw new IllegalArgumentException("types may not be null or empty");
      
      if (server == null)
         throw new IllegalArgumentException("server may not be null");
      
      if(!server.isConnected())
         throw new IllegalArgumentException("server must be connected.");      
   
      m_server = server;
      m_inCompleteOnly = incompleteOnly;
      
      init(idTypes, types);
   }

   /**
    * Initializes the panel with a tree control in the left panel and a table 
    * with description text area in the right panel.
    * 
    * @throws PSDeployException if there are any errors communicating with the
    * server.
    */
   private void init(Iterator idTypes, PSCatalogResultSet types) 
      throws PSDeployException 
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

      setLayout(new BorderLayout(20,20));
      setBorder(new EmptyBorder(10,10,10,10));

      //The path that should be selected, this should be selected after 
      //completely creating the panels, because selecting the path fires the
      //selection events and those require initialization of panel.      
      List firstIncompletePath = new ArrayList();
      
      add(createLeftPanel(idTypes, firstIncompletePath), BorderLayout.CENTER);
      add(createRightPanel(types), BorderLayout.EAST);
      
      //select the first incomplete node
      if(!firstIncompletePath.isEmpty())
         m_tree.setSelectionPath( new TreePath(firstIncompletePath.toArray()) );
   }
   
   /**
    * Creates the tree panel with the supplied dependency id types. The list of 
    * id types will be grouped by its dependency object types. The following 
    * displays the tree structure.
    * <pre><code>
    * Server Objects
    *      |
    *      --Object Type Name of id types dependency(eg. Application)
    *             |
    *             --Dependency ID (eg. Application name)
    *                    |
    *                    --Resource Name (eg. Requestor Name)
    *                          |
    *                          --Element Name (eg. Exit)
    * </code></pre>
    * Stores the id type mappings for each element in <code>m_idTypeMappings
    * </code> with key as <dependency id>:<resource name>:<element type string>.
    * Expands all incomplete elements by default and updates the <code>
    * firstIncompletePath</code> with the first incomplete element node path if 
    * it finds any incomplete element nodes.
    *                        
    * @param idTypes the list of <code>PSApplicationIDTypes</code> to edit for
    * types, assumed not to be <code>null</code> or empty.
    * @param firstIncompletePath the tree node path for incomplete element node,
    * assumed not <code>null</code>.
    * 
    * @return the panel, never <code>null</code>
    * 
    * @throws IllegalStateException if id type mappings does not exist for the
    * element.
    */
   private JPanel createLeftPanel(Iterator idTypes, List firstIncompletePath)
   {    
      Map depIDTypesMap = new HashMap();
      while(idTypes.hasNext())
      {
         PSApplicationIDTypes idTypeMap = (PSApplicationIDTypes)idTypes.next();
         
         // build composite filter map 
         Map filters = idTypeMap.getChoiceFilters();
         if (filters != null)
            m_choiceFilters.putAll(filters);
            
         Iterator resources = idTypeMap.getResourceList(m_inCompleteOnly);
         //No resources to show, just ignore that id types.
         if(!resources.hasNext()) 
            continue;
            
         String objectTypeName = idTypeMap.getDependency().getObjectTypeName();
         List depIDTypes = (List)depIDTypesMap.get(objectTypeName);
         if(depIDTypes == null)
         {
            depIDTypes = new ArrayList();
            depIDTypesMap.put(objectTypeName, depIDTypes);
         }
         depIDTypes.add(idTypeMap);
      } 
         
      DefaultMutableTreeNode root = new DefaultMutableTreeNode(
         PSDeploymentResources.getResourceString(ms_res, "serverObjects"));
        
      List incompleteNodes = new ArrayList();       
      Iterator entries = depIDTypesMap.entrySet().iterator();
      while(entries.hasNext())
      {
         Map.Entry entry = (Map.Entry)entries.next();
         String objTypeName = (String)entry.getKey();
         List idTypeMaps = (List)entry.getValue();
         DefaultMutableTreeNode objTypeNode = 
            new DefaultMutableTreeNode(objTypeName);
         root.add(objTypeNode);
         Iterator iter = idTypeMaps.iterator();
         while(iter.hasNext())
         {
            PSApplicationIDTypes idTypeMap = (PSApplicationIDTypes)iter.next();
            String depID = idTypeMap.getDependency().getDisplayName();
            DefaultMutableTreeNode objNode = new DefaultMutableTreeNode(
               depID);               
            objTypeNode.add(objNode);
            Iterator resources = idTypeMap.getResourceList(m_inCompleteOnly);
            while(resources.hasNext())
            {
               String resource = (String)resources.next();
               DefaultMutableTreeNode resNode = new DefaultMutableTreeNode(
                  resource);
               objNode.add(resNode);
               Iterator elements = idTypeMap.getElementList(resource, 
                  m_inCompleteOnly);                                    
               if(!elements.hasNext())
                  throw new IllegalStateException(
                     "idTypeMap must have elements for resource <" + 
                     resource + "," + depID + ">");
               while(elements.hasNext())
               {
                  String element = (String)elements.next();
                  
                  DisplayElement eleObj = new DisplayElement(
                     element, PSDeploymentResources.getResourceString(
                     ms_res, element));
                  
                  DefaultMutableTreeNode elemNode = new DefaultMutableTreeNode(
                     eleObj, false);
                  resNode.add(elemNode);
                  
                  //Save it in map for each element with its id type mappings to 
                  //show, later will be used by tree selection listener
                  String key = 
                     depID + KEY_DELIM + resource + KEY_DELIM + element;               
                  List idTypeMappings = new ArrayList();
                  Iterator mappings = idTypeMap.getIdTypeMappings(
                     resource, element, m_inCompleteOnly);
                  if(!mappings.hasNext())
                     throw new IllegalStateException(
                        "idTypeMap must have literal id mappings for element <" 
                        + element + "," + resource + "," + depID + ">");                        
                  while(mappings.hasNext())
                     idTypeMappings.add(mappings.next());
                  m_idTypeMappings.put(key, idTypeMappings);
                  
                  //check whether it is incomplete element
                  if(m_inCompleteOnly)
                     mappings = idTypeMappings.iterator();
                  else                  
                  {
                     mappings = idTypeMap.getIdTypeMappings(
                        resource, element, true);
                  }
                     
                  //add the node if it is incomplete                  
                  if(mappings.hasNext()) 
                     incompleteNodes.add(elemNode);
               }
            }
         }
      }            
      
      DefaultTreeModel model = new DefaultTreeModel(root);
      m_tree = new JTree(model);
      m_tree.addTreeSelectionListener(this);
      JScrollPane pane = new JScrollPane(m_tree);
      pane.setAlignmentX(LEFT_ALIGNMENT);          
    
      //expand the incomplete nodes
      Iterator iter = incompleteNodes.iterator();
      while(iter.hasNext())
      {
         DefaultMutableTreeNode node = (DefaultMutableTreeNode)iter.next();
         m_tree.expandPath(new TreePath(model.getPathToRoot(node.getParent())));
      }       
      //set the first incomplete path to select
      if(!incompleteNodes.isEmpty())
      {
         Object[] path = model.getPathToRoot(
            (DefaultMutableTreeNode)incompleteNodes.get(0));
         for(int i=0; i < path.length; i++)
            firstIncompletePath.add(path[i]);
      } 
      
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.add(new JLabel(
         PSDeploymentResources.getResourceString(ms_res, "treeLabel"), 
         SwingConstants.LEFT));
      panel.add(Box.createVerticalStrut(5));
      panel.add(pane);
      
      return panel;
   }
   
   /**
    * Constructs the right panel with a table and a text area. The table 
    * displays all id type mappings of the selected element in the tree. The 
    * text area displays the selected mapping's context description. Adds 
    * selection listeners and editors to the table. Only the <code>TYPE_COLUMN
    * </code> is editable and the editor is a combo box with supplied list of 
    * literal types.
    * 
    * @param types the list of literal types, assumed not to be <code>null
    * </code> or empty.
    * 
    * @return the panel, never <code>null</code>
    * 
    * @throws PSDeployException if there are any errors retrieving parent types
    * from the server.
    */
   private JPanel createRightPanel(PSCatalogResultSet types) 
      throws PSDeployException
   {
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.setBorder( PSDialog.createGroupBorder(
         PSDeploymentResources.getResourceString(ms_res, "identifyIDTypes")) );
      JLabel label = new JLabel(PSDeploymentResources.getResourceString(ms_res, 
         "tableDesc"), SwingConstants.LEFT);
      label.setPreferredSize(new Dimension(300, 24));
      label.setAlignmentX(LEFT_ALIGNMENT);
      panel.add(label);
      
      //initialize column names
      if(ms_columns.isEmpty())
      {
         ms_columns.add(
            PSDeploymentResources.getResourceString(ms_res, "value"));
         ms_columns.add(
            PSDeploymentResources.getResourceString(ms_res, "type")); 
      }
         
      MappingsTableModel model = new MappingsTableModel(null);
      m_table = new JTable(model);
      m_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      m_table.getTableHeader().setReorderingAllowed(false);
      //add the table selection listeners
      m_table.getSelectionModel().addListSelectionListener(this);
      
      //get the contents of the value cell editor and add none for no type.
      Iterator literalTypes = types.getResults();
      while(literalTypes.hasNext())
      {
         PSCatalogResult result = (PSCatalogResult)literalTypes.next();
         DisplayElement el = new DisplayElement(result.getID(), 
            result.getDisplayText());
         // set any supported parent types
         el.setParentType(m_server.getParentType(result.getID()));
         m_types.add(el);
      }
      m_types.add( new DisplayElement(PSApplicationIDTypeMapping.TYPE_NONE, 
         PSDeploymentResources.getResourceString(ms_res,
         PSApplicationIDTypeMapping.TYPE_NONE)) );
      
      m_typeCombo = new JComboBox(m_types.toArray(
         new DisplayElement[m_types.size()]));
      // handle selection event for types that support parent id
      m_typeCombo.addItemListener(new ItemListener(){
         public void itemStateChanged(ItemEvent e)
         {
            if (e.getStateChange() == ItemEvent.SELECTED)
            {
               Object o = e.getItem();
               if (o instanceof DisplayElement)
               {
                  DisplayElement el = (DisplayElement)o;
                  // if type supports parent id, get user to select a parent
                  if (el.getParentType() != null)
                  {
                     // get current value from first column
                     int row = m_table.getEditingRow();
                     if (row != -1) // make sure row is being edited
                     {
                        String curId = (String)m_table.getModel().getValueAt(
                           row, VALUE_COLUMN);
                        setParent(el, curId);
                     }
                  }
               }
            }
         }
      });
      
      DefaultCellEditor editor = new DefaultCellEditor(m_typeCombo);
      editor.setClickCountToStart(2);
      m_table.getColumn(model.getColumnName(TYPE_COLUMN)).setCellEditor(editor);

      JScrollPane pane = new JScrollPane (m_table);
      pane.setPreferredSize(new Dimension (150, 125));
      pane.setAlignmentX(LEFT_ALIGNMENT);
      panel.add(pane);
      
      panel.add(Box.createVerticalStrut(5));

      m_descText = new JTextArea();
      m_descText.setLineWrap(true);
      m_descText.setWrapStyleWord(true);
      m_descText.setEditable(false);
      JScrollPane textPane = new JScrollPane (m_descText);
      textPane.setAlignmentX(LEFT_ALIGNMENT);
      textPane.setPreferredSize(new Dimension (150, 125));
      textPane.setBorder(PSDialog.createGroupBorder(
         PSDeploymentResources.getResourceString(ms_res, "description")));
      panel.add(textPane);

      return panel;
   }


   /**
    * If the selected node is an element it displays the id type mappings for
    * that element in the table, otherwise none.
    * 
    * @param e the event that characterizes the change, assumed not to be 
    * <code>null</code> as this method will be called by <code>Swing</code> 
    * model when a tree selection changes.
    */
   public void valueChanged(TreeSelectionEvent e)
   {
      //Stops current table data editing.
      stopEditing();
      
      TreePath selPath = e.getPath();      
      DefaultMutableTreeNode selNode = 
         (DefaultMutableTreeNode)selPath.getLastPathComponent();
      List idTypeMappings = null;
      if(selNode.isLeaf())
      {
         String element = ((DisplayElement)selNode.getUserObject()).getId();
         TreePath parentPath = selPath.getParentPath();
         DefaultMutableTreeNode resNode = 
            (DefaultMutableTreeNode)parentPath.getLastPathComponent();
         String resource = (String)resNode.getUserObject();
         parentPath = parentPath.getParentPath();
         DefaultMutableTreeNode objNode = 
            (DefaultMutableTreeNode)parentPath.getLastPathComponent();
            
         String depID = (String)objNode.getUserObject();
         idTypeMappings = (List) m_idTypeMappings.get(
            depID + KEY_DELIM + resource + KEY_DELIM + element);
      }
      
      ((MappingsTableModel)m_table.getModel()).setData(idTypeMappings);
   }
   
   /**
    * Stops editing of cells in the ID Type Mappings table.
    */
   public void stopEditing()
   {
      if(m_table.isEditing())
      {
         m_table.getCellEditor(m_table.getEditingRow(), 
            m_table.getEditingColumn()).stopCellEditing(); 
      }
   }

   /**
    * Action method for selection change event in table. Sets the context of the
    * selected row in the description text area.
    * 
    * @param e the event that characterizes the change, assumed not to be 
    * <code>null</code> as this method will be called by <code>Swing</code> 
    * model when selection changes in the table.
    */   
   public void valueChanged (ListSelectionEvent e)
   {
      if(e.getValueIsAdjusting())
         return;

      stopEditing();
      
      int row = m_table.getSelectedRow();

      //Sets the value in editable text area 
      if(row >= 0)
      {
         MappingsTableModel model = (MappingsTableModel)m_table.getModel();
         PSApplicationIDTypeMapping mapping = model.getIDTypeMapping(row);
         m_descText.setText(mapping.getContextDisplayText());
         
         // filter choices if we have a filter
         List types = (List)m_choiceFilters.get(mapping.getValue());
         if (types != null && !types.isEmpty())
         {
            // save current choice
            Object previousValue = model.getValueAt(row, TYPE_COLUMN);
            
            // clear combo and add appropriate items
            m_typeCombo.removeAllItems();
            Iterator iter = m_types.iterator();
            while(iter.hasNext())
            {
               // add to combo if in filter list or if the "not an id" choice 
               DisplayElement element = (DisplayElement) iter.next();               
               if (types.contains(element.getId()) || 
                  element.getId().equals(PSApplicationIDTypeMapping.TYPE_NONE))
               {
                  m_typeCombo.addItem(element);
               }
            }
            
            // reset previous value if possible
            m_typeCombo.setSelectedItem(previousValue);         
         }
         
      }
      else
         m_descText.setText(""); //clears the description if it has any.
   }
   
   /**
    * Validates that each mapping of each resource element is set with type.
    * 
    * @return <code>true</code> if the validation is successful, otherwise 
    * <code>false</code>
    */
   public boolean validateMappings()
   {
      Iterator mappings = m_idTypeMappings.values().iterator();
      while(mappings.hasNext())
      {
         Iterator elMappings = ((List)mappings.next()).iterator();
         while(elMappings.hasNext())
         {
            PSApplicationIDTypeMapping mapping = 
               (PSApplicationIDTypeMapping)elMappings.next();
            if(mapping.getType().equals(
               PSApplicationIDTypeMapping.TYPE_UNDEFINED)) 
               return false;
         }
      }
      return true;
   }
   
   /**
    * Called to indicate that the IDType mappings underlying this panels
    * data model has been modified externally.
    */
   public void mappingsChanged()
   {
      ((MappingsTableModel)m_table.getModel()).fireTableDataChanged();      
   }
   
   /**
    * Adds a listener to be informed of any changes to any type assignments.
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
    * Prompts the user to select a parent for the currently edited id type
    * mappings.  Checks to see if the type selected by the {@link #m_typeCombo}
    * supports parent ids, and if so, gets all possible parents using that type
    * and the id specified by the supplied element and displays a dialog to the
    * user to select one.  If the type supports parent ids and no possible 
    * parents exist, an error is displayed to the user and the combo box is set
    * to have no selection.
    * 
    * @param el The display element represented by the users selection, assumed 
    * not <code>null</code> and to have a parent type set.
    * @param id The id specified by the value of the currently edited id type
    * mapping, assumed not <code>null</code> or empty.
    */   
   private void setParent(DisplayElement el, String id)
   {
      String parentType = el.getParentType();
      String parentDisplayType = getTypeName(parentType);
      Set parentSet = new HashSet();
      
      // save current selection in case of error
      Object previousValue = m_table.getModel().getValueAt(
         m_table.getEditingRow(), TYPE_COLUMN);
      
      // build set of display elements representing possible parents
      try 
      {
         Iterator elements = m_server.getElementsByType(el.getId());
         while (elements.hasNext())
         {
            PSMappingElement mapEl = (PSMappingElement)elements.next();
            if (!id.equals(mapEl.getId()))
               continue;
            parentSet.add(new DisplayElement(mapEl.getParentId(), 
               mapEl.getParentDisplayString()));
         }
      }
      catch (PSDeployException e) 
      {
         // show error, reset combo and return
         ErrorDialogs.showErrorMessage(getParentWindow(), 
            e.getLocalizedMessage(), ms_res.getString("errorTitle"));
         m_typeCombo.setSelectedItem(previousValue);
         return;
      }
      
      // display dialog and ask user to select
      if (parentSet.isEmpty())
      {
         // display error
         String[] args = {parentType, id};
         ErrorDialogs.showErrorMessage(getParentWindow(), 
            MessageFormat.format(ms_res.getString("noParentAvail"), args), 
            ms_res.getString("errorTitle"));
         m_typeCombo.setSelectedItem(previousValue);
      }
      else
      {
         String parentMsg = MessageFormat.format(ms_res.getString("parentMsg"), 
            new String[] {parentDisplayType, el.getDisplayText(), id});
         parentMsg = ErrorDialogs.cropErrorMessage(parentMsg);
         Object userSel = JOptionPane.showInputDialog(this, 
            parentMsg, ms_res.getString("parentTitle"), 
            JOptionPane.QUESTION_MESSAGE, null, parentSet.toArray(), null);
            
         if(userSel != null)
         {
            // get selected parent and set on the display element
            DisplayElement selectedEl = (DisplayElement)userSel;
            el.setParentId(selectedEl.getId());
         } 
         else
         {
            // none selected, display error
            String[] args = {id};
            ErrorDialogs.showErrorMessage(getParentWindow(), 
               MessageFormat.format(ms_res.getString("noParentSelected"), args), 
               ms_res.getString("errorTitle"));
            m_typeCombo.setSelectedItem(previousValue);
         }
      }
   }

   /**
    * Get the parent window of this panel.
    * 
    * @return The window, or <code>null</code> if there is no parent.
    */   
   private Window getParentWindow()
   {
      if (m_parent == null)
      {
         m_parent = SwingUtilities.getWindowAncestor(this);
      }
      
      return m_parent;
   }
   
   /**
    * Gets the display text for the specified type.
    * 
    * @param type The type to get display text for, assumed not 
    * <code>null</code> or empty.
    * 
    * @return The text, or the supplied <code>type</code> if the text cannot
    * be found.
    */
   private String getTypeName(String type)
   {
      String name = type;
      Iterator types = m_types.iterator();
      while (types.hasNext())
      {
         DisplayElement el = (DisplayElement)types.next();
         if (el.getId().equals(type))
         {
            name = el.getDisplayText();
            break;
         }
      }
      
      return name;
   }
   
   /**
    * The mapping model that represents the id type mappings that need to be set 
    * on the table.
    */
   private class MappingsTableModel extends AbstractTableModel
   {
      /**
       * Constructs the model with the supplied list of id type mappings.
       * 
       * @param idTypeMappingsList the list of id type mappings, if <code>null
       * </code> or empty no data rows exist.
       * 
       */
      public MappingsTableModel(List idTypeMappingsList)
      { 
         setData(idTypeMappingsList);
      }
      
      /**
       * Sets the id type mappings data on this table model.
       * 
       * @param idTypeMappingsList the list of id type mappings, if <code>null
       * </code> or empty no data rows exist.
       */
      public void setData(List idTypeMappingsList)
      {         
         m_idTypeMappings = idTypeMappingsList;
         fireTableChanged(new TableModelEvent(this));         
      }
      
      /**
       * Gets the number of columns in this table model.
       * 
       * @return the number of columns, never <code>0</code>
       */
      public int getColumnCount() 
      {
           return ms_columns.size();
      }
      
      /**
       * Gets the number of data rows in this table model.
       * 
       * @return the number of rows, may be <code>0</code>
       */
      public int getRowCount()
      {
         int rowCount = 0;
         if(m_idTypeMappings != null)
            rowCount = m_idTypeMappings.size();
         return rowCount;
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
         return (String)ms_columns.get(col);
      }
      
      /**
       * Checks whether the supplied cell is editable or not.
       * 
       * @param row the row index of value to get, must be >= 0 and less than 
       * {@link #getRowCount() rowcount} of this model.
       * @param col the column index of value to get, must be >= 0 and less than  
       * {@link #getColumnCount() columncount} of this model.
       * 
       * @return <code>true</code> if the column of the cell is <code>
       * TYPE_COLUMN</code>, otherwise <code>false</code>.
       */
      public boolean isCellEditable(int row, int col)
      {
         checkRow(row);
         checkColumn(col);
         
         return col == TYPE_COLUMN;
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
               
         PSApplicationIDTypeMapping mapping = 
            (PSApplicationIDTypeMapping)m_idTypeMappings.get(row);
            
         if(col == TYPE_COLUMN)
         {
            String type =  mapping.getType();
            
            DisplayElement dispType = null; 
            if(type.equals(PSApplicationIDTypeMapping.TYPE_UNDEFINED))
               dispType = new DisplayElement(type, "");     
            else
            {
               Iterator iter = m_types.iterator();
               while(iter.hasNext())
               {
                  DisplayElement element = (DisplayElement) iter.next();
                  if(element.getId().equals(type))
                  {
                     dispType = element;
                     if (mapping.getParentId() != null)
                        dispType.setParentId(mapping.getParentId());
                     break;
                  }
               }
            }
                     
            if(dispType == null)
               throw new IllegalStateException("mapping type is invalid.");
               
            return dispType;
         }
         else 
            return mapping.getValue();
      }
      
      /**
       * Sets the value at the specified cell. Updates only if it is the type
       * column.
       *
       * @param value value to assign to cell, may not be <code>null</code>
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
         if(value == null)
            throw new IllegalArgumentException("value may not be null.");
            
         if(col == TYPE_COLUMN)
         {
            PSApplicationIDTypeMapping idTypeMapping = getIDTypeMapping(row);
            if(value instanceof DisplayElement)
            {
               DisplayElement el = (DisplayElement)value;
               idTypeMapping.setType(el.getId());
               
               // set parent on the type mapping if supplied by the element
               // if not, will set to null, possibly clearing old value which is 
               // what we want in that case
               idTypeMapping.setParent(el.getParentId(), el.getParentType());
               // new clear the id in the element since its used by each row
               el.setParentId(null);
            }
            notifyChangeListeners();
         }
         else
            throw new IllegalStateException("Only type column is editable");
       }
      
      /**
       * Gets the id type mapping of the supplied row.
       * 
       * @param row the row index of id to get, must be >= 0 and less than 
       * {@link #getRowCount() rowcount} of this model.
       * 
       * @return the id string, never <code>null</code> or empty.
       * 
       * @throws IllegalArgumentException if row index is invalid.
       */
      private PSApplicationIDTypeMapping getIDTypeMapping(int row)
      {
         checkRow(row);
         return (PSApplicationIDTypeMapping)m_idTypeMappings.get(row);
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
       * The list of id type mappings represented by this model, initialized to
       * an empty list and may be modified through a call to <code>
       * setData(List)</code>. May be <code>null</code>.
       */
      private List m_idTypeMappings;
   }
   
   /**
    * Utility class to use as user object for a tree node or element in the list
    * to have unique id underlying with a display text.
    */
   private class DisplayElement
   {
      /**
       * Constructs this object with supplied parameters.
       * 
       * @param id the id of the element, may not be <code>null</code> or empty.
       * @param displayText the display text of the element, may not be <code>
       * null</code> or empty.
       * 
       * @throws IllegalArgumentException if any parameter is invalid.
       */
      public DisplayElement(String id, String displayText)
      {
         if(id == null || id.trim().length() == 0)
            throw new IllegalArgumentException("id may not be null or empty.");
            
         if(displayText == null)
            throw new IllegalArgumentException(
            "displayText may not be null.");
            
         m_id = id;
         m_displayText = displayText;
      }
      
      /**
       * Gets the id of this element.
       * 
       * @return the id, never <code>null</code> or empty.
       */
      public String getId()
      {
         return m_id;
      }
      
      /**
       * Gets the display text of this element.
       * 
       * @return the display text, never <code>null</code> or empty.
       */
      public String getDisplayText()
      {
         return m_displayText;
      }
      
      /**
       * Gets the display text as the string representation of this element.
       * 
       * @return the display text, never <code>null</code>, may be empty.
       */
      public String toString()
      {
         return m_displayText;
      }
      
      //implements equals for this object.
      public boolean equals(Object object)
      {
         boolean isEqual = false;
         if(object instanceof DisplayElement)
         {
            DisplayElement el = (DisplayElement)object;
            if( getId().equals(el.getId()) && 
               getDisplayText().equals(el.getDisplayText()) )
            {
               isEqual = true;
            }
         }         
         return isEqual;  
      }


      /**
       * Generates hash code consistent with {@link #equals(Object)}.
       */
      @Override
      public int hashCode()
      {
         return getIdHashCode() + getDisplayTextHashCode();
      }

      /**
       * {@link #getDisplayText()} hash code or 0 if it is <code>null</code>.
       */
      private int getDisplayTextHashCode()
      {
         return getDisplayText() == null ? 0 : getDisplayText().hashCode();
      }

      /**
       * {@link #getId()} hash code or 0 if it is <code>null</code>.
       */
      private int getIdHashCode()
      {
         return getId() == null ? 0 : getId().hashCode();
      }

      /**
       * Sets the currently specified parent Id of this element.
       * 
       * @param parentId The parent id, may be <code>null</code> to specify
       * no parent, may not be empty.
       * 
       * @throws IllegalArgumentException if <code>parentId</code> is invalid.
       */
      public void setParentId(String parentId)
      {
         if (parentId != null && parentId.trim().length() == 0)
            throw new IllegalArgumentException("parentId may not be empty");
         
         m_parentId = parentId;
      }
      
      /**
       * Sets the parent type of this element.
       * 
       * @param parentType The parent type, may be <code>null</code> to specify
       * no parent type, may not be empty.
       * 
       * @throws IllegalArgumentException if <code>parentType</code> is invalid.
       */
      public void setParentType(String parentType)
      {
         if (parentType != null && parentType.trim().length() == 0)
            throw new IllegalArgumentException("parentType may not be empty");
         
         m_parentType = parentType;
      }
      
      /**
       * Gets the currently specified parent id of this element.
       * 
       * @return The id, may be <code>null</code>, never empty.
       */
      public String getParentId()
      {
         return m_parentId;
      }
      
      /**
       * Gets the currently specified parent type of this element.
       * 
       * @return The type, may be <code>null</code>, never empty.
       */
      public String getParentType()
      {
         return m_parentType;
      }
      
      /**
       * The id representing this element, initialized in the constructor and
       * never <code>null</code> or modified after that.
       */
      private String m_id;
      
      /**
       * The display string representing this element, initialized in the 
       * constructor and never <code>null</code> or modified after that.
       */
      private String m_displayText;
      
      /**
       * The parent id of this element, may be <code>null</code>, never emtpy,
       * modified by calls to {@link #setParentId(String)}
       */
      private String m_parentId = null;
      
      /**
       * The parent type of this element, may be <code>null</code>, never emtpy,
       * modified by calls to {@link #setParentType(String)}
       */
      private String m_parentType;
   }
   
   /**
    * The map of id type mappings for each resource element, with key as the 
    * element identifier (<code>String</code> in the format '<dependency id>:
    * <resource name>:<element id>') and value as <code>Iterator</code> of id 
    * type mappings (<code>PSApplicationIDTypeMapping</code>) of that element.  
    * Initialized to an empty map and entries are added in <code>
    * createLeftPanel(Iterator)</code> and never <code>null</code> or modified 
    * after that.
    */
   private Map m_idTypeMappings = new HashMap();
   
   /**
    * The list of literal types (<code>DisplayElement</code>s) to present to 
    * user to choose from for setting the type, initialized in <code>
    * createRightPanel(Iterator)</code> and never <code>null</code> or modified 
    * after that. 
    */
   private List m_types = new ArrayList();
   
   /**
    * The flag to indicate to display complete id type mappings to edit or not.
    * <code>true</code> to display incomplete mappings only, <code>false</code>
    * to display all mappings. Initialized in the constructor and never modified
    * after that.
    */
   private boolean m_inCompleteOnly;
   
   /**
    * The deployment server on which the id types need to be saved, initialized 
    * in the constructor and never <code>null</code> or modified after that.
    */
   private PSDeploymentServer m_server;
   
   /**
    * The tree to represent the objects that have some literal ids, initialized 
    * in <code>createLeftPanel(Iterator)</code> and never <code>null</code> or 
    * modified after that.
    */
   private JTree m_tree;
 
   /**
    * The table to display the id type mappings for selected resource element in
    * the tree, initialized in <code>createRightPanel(Iterator)</code> and never
    * <code>null</code> or modified after that.
    */
   private JTable m_table;
   
   /**
    * The text area used to display the context description of the selected id 
    * type mapping in the table, initialized in <code>createRightPanel(Iterator)
    * </code> and never <code>null</code> or modified after that.
    */
   private JTextArea m_descText;

   /**
    * The combo box used to specify the type of object the literal id of the
    * selected mapping represents.  Initialized in 
    * <code>createRightPanel(Iterator)</code> and never <code>null</code>after 
    * that.  May be modified by {@link #valueChanged(ListSelectionEvent)}.
    */
   private JComboBox m_typeCombo;
   
   /**
    * The parent window containing this panel.  Initially <code>null</code>, 
    * modified by calls to {@link #getParentWindow()}, may be <code>null</code> 
    * if no parent exists.
    */
   private Window m_parent = null;
   
   /**
    * Map of type filters to use to limit type choices for a mapping.  Never
    * <code>null</code>, populated during construction, may be empty, never 
    * modified after construction.  For details on what the map contains, see
    * {@link PSApplicationIDTypes#getChoiceFilters()}.  
    */
   private Map m_choiceFilters = new HashMap();
   
   /**
    * List of {@link ChangeListener} objects added and removed via 
    * {@link #addChangeListener(ChangeListener)} and 
    * {@link #removeChangeListener(ChangeListener)}.  Never <code>null</code>,
    * may be empty.
    */
   private List m_changeListeners = new ArrayList();
      
   /**
    * The constant to indicate value column, represents the index in the 
    * column names list.
    */
   public static final int VALUE_COLUMN = 0;
   
   /**
    * The constant to indicate type column, represents the index in the 
    * column names list.
    */
   public static final int TYPE_COLUMN = 1;
   
   /**
    * The list of column names in the order of display, filled with column names
    * in <code>createRightPanel(PSCatalogResultSet)</code> and never <code>null
    * </code> modified after that.
    */
   private static List ms_columns = new ArrayList(); 
  
   /**
    * The delimiter used to create the key of <code>m_idTypeMappings</code>.
    */
   private static final String KEY_DELIM = ":";
   
   /**
    * The resource bundle used to get the resource strings of this panel, 
    * initialized in <code>init()</code>, may be <code>null</code> if it could
    * not load, never modified after that.
    */
   private static ResourceBundle ms_res;
}
