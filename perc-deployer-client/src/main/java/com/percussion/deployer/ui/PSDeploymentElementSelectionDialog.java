/******************************************************************************
 *
 * [ PSDeploymentElementSelectionDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.deployer.ui;

import com.percussion.UTComponents.UTFixedButton;
import com.percussion.deployer.catalog.PSCatalogResult;
import com.percussion.deployer.client.IPSDeployConstants;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDeployableElement;
import com.percussion.deployer.objectstore.PSExportDescriptor;
import com.percussion.error.PSDeployException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.utils.collections.PSIteratorUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

/**
 * Dialog to allow selection of objects to include in the export. May also
 * save these as part of an export descriptor for reuse.
 */
public class PSDeploymentElementSelectionDialog extends PSDeploymentWizardDialog
   implements ListSelectionListener, TreeSelectionListener, 
   TreeWillExpandListener
{

   /**
    * Constructs this dialog calling {@link 
    * PSDeploymentWizardDialog#PSDeploymentWizardDialog(Frame, 
    * PSDeploymentServer, int, int) super(parent, server, step, sequence)}.
    */
   public PSDeploymentElementSelectionDialog(Frame parent, 
      PSDeploymentServer deploymentServer, int step, int sequence)
   {
      super(parent, deploymentServer, step, sequence);
      
      initDialog();
   }
   
   /**
    * Creates the dialog framework with border layout keeping the description 
    * panel on north, controls panel on center and command panel on south.
    */
   protected void initDialog()
   {
      JPanel panel = new JPanel();
      getContentPane().add(panel);
      panel.setLayout(new BorderLayout(10, 20));
      EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);
      panel.setBorder(emptyBorder);
      
      setTitle(getResourceString("title"));
      
      JPanel descPanel = createDescriptionPanel(
         getResourceString("descTitle"), 
         new String[] {getResourceString("description")} );
      panel.add(descPanel, BorderLayout.NORTH);
      
      //controls panel in the center of the dialog.
      JPanel centerPanel = new JPanel();
      centerPanel.setBorder(
            BorderFactory.createTitledBorder(getResourceString("elementSelection")));

      centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));   
      centerPanel.setAlignmentX(LEFT_ALIGNMENT);
      panel.add(centerPanel, BorderLayout.CENTER);
      
      //the left panel with available elements tree
      centerPanel.add(Box.createHorizontalGlue());
      centerPanel.add(Box.createHorizontalStrut(20));
      centerPanel.add(createAvailableElementsPanel());
      centerPanel.add(Box.createHorizontalStrut(20));
      centerPanel.add(Box.createHorizontalGlue());
      
      //the panel with add and remove buttons
      JPanel addRemovePanel = new JPanel();      
      addRemovePanel.setLayout(new BoxLayout(addRemovePanel, BoxLayout.Y_AXIS));
      addRemovePanel.add(Box.createVerticalGlue());
      m_addButton = new UTFixedButton(
         getResourceString("add"), new Dimension(100,24));
      m_addButton.setMnemonic(getResourceString("add.mn").charAt(0));
      m_addButton.setEnabled(false);
      m_addButton.addActionListener(new ActionListener() 
      {
         public void actionPerformed(ActionEvent e) 
         {
            onAdd();
         }
      });
      addRemovePanel.add(m_addButton);
      addRemovePanel.add(Box.createVerticalStrut(10));
      m_removeButton = new UTFixedButton(
         getResourceString("remove"), new Dimension(100,24));
      m_removeButton.setMnemonic(getResourceString("remove.mn").charAt(0));
      m_removeButton.setEnabled(false);         
      m_removeButton.addActionListener(new ActionListener() 
      {
         public void actionPerformed(ActionEvent e) 
         {
            onRemove();
         }
      });
      addRemovePanel.add(m_removeButton);
      addRemovePanel.add(Box.createVerticalGlue());      
      centerPanel.add(addRemovePanel);
      centerPanel.add(Box.createHorizontalStrut(20));
      centerPanel.add(Box.createHorizontalGlue());
      
      //the panel with elements to package list
      centerPanel.add(createPackageElementsPanel());
      centerPanel.add(Box.createHorizontalStrut(20));
      centerPanel.add(Box.createHorizontalGlue());
      
      panel.add(createCommandPanel(true), BorderLayout.SOUTH);
      
      pack();      
      center();    
      setResizable(true);
   }
   
   /**
    * Creates the panel with the 'Available Elements' label and tree enclosed in 
    * a scrollpane. Adds this object as a 'TreeWillExpandListener' to the tree.
    * 
    * @return the panel, never <code>null</code>
    */
   @SuppressWarnings("unchecked")
   private JPanel createAvailableElementsPanel()
   {
      JPanel panel = new JPanel(new BorderLayout());
      panel.add(new JLabel(getResourceString("availElements"), 
         SwingConstants.LEFT), BorderLayout.NORTH);
      
      DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(
         getResourceString("elements"));
      rootNode.add( new JTree.DynamicUtilTreeNode(
         getResourceString("cmsElements"), new Vector()) );
      rootNode.add( new JTree.DynamicUtilTreeNode(
         getResourceString("customElements"), new Vector()) );
      DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
      m_availElementsTree = new JTree(treeModel);
      m_availElementsTree.getSelectionModel().setSelectionMode(
         TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
      m_availElementsTree.addTreeWillExpandListener(this);
      m_availElementsTree.addTreeSelectionListener(this);
      m_availElementsTree.setCellRenderer(new TreeElementCellRenderer());
      
      JScrollPane treePane = new JScrollPane(m_availElementsTree);
      treePane.setPreferredSize(new Dimension(200, 200));
      treePane.setAlignmentX(LEFT_ALIGNMENT);
      panel.add(treePane, BorderLayout.CENTER);
      
      panel.setAlignmentX(LEFT_ALIGNMENT);      
      return panel;
   }
   
   /**
    * Creates the panel with 'Elements to package' label and a list box enclosed
    * in scroll pane. Adds this object as a selection listener to the elements 
    * in the list box.
    * 
    * @return the panel, never <code>null</code>
    */
   private JPanel createPackageElementsPanel()
   {
      JPanel panel = new JPanel(new BorderLayout());
      panel.add(new JLabel(getResourceString("packElements"), 
         SwingConstants.LEFT), BorderLayout.NORTH);
      
      m_packElementsList = new JList(new DefaultListModel());
      m_packElementsList.setSelectionMode(
         ListSelectionModel.SINGLE_INTERVAL_SELECTION);
      m_packElementsList.addListSelectionListener(this);
      
      JScrollPane listPane = new JScrollPane(m_packElementsList);
      listPane.setPreferredSize(new Dimension(150, 200));
      listPane.setAlignmentX(LEFT_ALIGNMENT);
      panel.add(listPane, BorderLayout.CENTER);
      
      panel.setAlignmentX(LEFT_ALIGNMENT);      
      return panel;
   }

   /**
    * Adds the elements in the descriptor to the 'Elements to package' list.
    * See <code>super.init()</code> for more description.
    */
   @SuppressWarnings("unchecked")
   protected void init()
   {
      m_exportDescriptor = (PSExportDescriptor)m_descriptor;
      DefaultListModel model = (DefaultListModel)m_packElementsList.getModel();
      model.removeAllElements();
      Iterator packages = m_exportDescriptor.getPackages();
      
      while(packages.hasNext())
         addElementSorted((PSDeployableElement) packages.next(), model);      
   }
   
   /**
    * Overridden to display appropriate message to the user if the descriptor 
    * supplied to this dialog has some deployable elements that are missing (no 
    * longer exists on the server) or that are modified (the dependencies or 
    * ancestors of them are modified). This is done in case of displaying the 
    * dialog to the user, not on hiding. Calls <code>super.setVisible(boolean)
    * </code> to display/hide the dialog.
    * 
    * @param flag if <code>true</code> the dialog is shown and the information
    * messages are presented if they are required, othewise hides the dialog.
    */
   @SuppressWarnings("unchecked")
   public void setVisible(final boolean flag)
   {
      SwingUtilities.invokeLater(new Runnable()
      {
         public void run()
         {
            if(flag)
            {
               if(m_exportDescriptor.getMissingPackages().hasNext())
               {
                  StringBuffer missPackages = new StringBuffer();
                  Iterator iter = m_exportDescriptor.getMissingPackages();
                  while(iter.hasNext())
                  {
                     if(missPackages.length() != 0)
                        missPackages.append(",");
                     missPackages.append(iter.next());
                  }
                  
                  // now clear the list
                  m_exportDescriptor.setMissingPackages(
                     PSIteratorUtils.emptyIterator());
                  
                  String msg = MessageFormat.format( 
                     getResourceString("missPackMsg"), 
                     new String[] {missPackages.toString()} );
                     
                  JOptionPane.showMessageDialog(
                     PSDeploymentElementSelectionDialog.this, 
                     ErrorDialogs.cropErrorMessage(msg), 
                     getResourceString("missingPackages"), 
                     JOptionPane.INFORMATION_MESSAGE);
               }
               
               if(m_exportDescriptor.getModifiedPackages().hasNext())
               {
                  StringBuffer modPackages = new StringBuffer();
                  Iterator iter = m_exportDescriptor.getModifiedPackages();
                  int count = 0;
                  while(iter.hasNext())
                  {
                     if(modPackages.length() != 0)
                     {
                        modPackages.append(",");
                        count++;
                        if ( count%10 == 0 )
                           modPackages.append("\n");
                     }
                     modPackages.append(iter.next());
                  }
                  
                  // now clear the list
                  m_exportDescriptor.setModifiedPackages(
                     PSIteratorUtils.emptyIterator());
                  
                  String msg = MessageFormat.format( 
                     getResourceString("modPackMsg"), 
                     new String[] {modPackages.toString()} );
                     
                  JOptionPane.showMessageDialog(
                     PSDeploymentElementSelectionDialog.this, 
                     ErrorDialogs.cropErrorMessage(msg), 
                     getResourceString("modifiedPackages"), 
                     JOptionPane.INFORMATION_MESSAGE);
               }
            }
         }
      });
      super.setVisible(flag);
   }
   
   /**
    * Action method for tree expansion event. If the expanded node is a node 
    * that allows children, but not loaded with children ('CMS Elements' or 
    * 'Custom Elements' or the element type (eg:'Editor', 'Assembler') nodes) 
    * gets the data for its children from the server and creates the nodes for 
    * them and sets as children to the expanding node. Uses <code>
    * PSCatalogResult</code> as the user object for element type nodes and 
    * <code>PSDeployableElement</code> for the actual elements of a particular 
    * type. Displays an error message if an exception happens in getting child 
    * data for the node.
    *
    * @param event the tree expansion event, assumed not to be <code>null</code>
    * as Swing model calls this method with an event when tree expansion event
    * occurs.
    */
   @SuppressWarnings("unchecked")
   public void treeWillExpand(TreeExpansionEvent event)
   {
      DefaultMutableTreeNode node = 
         (DefaultMutableTreeNode)event.getPath().getLastPathComponent();
         
      try {
         setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));                     
         if(node.getAllowsChildren() && node.getChildCount() == 0)
         {            
            if(node.getUserObject().toString().equals(
               getResourceString("cmsElements")))
            {
               Iterator elTypes = 
                  m_deploymentServer.getCMSElementTypes().getResults();                  
               while(elTypes.hasNext())
               {
                  node.add( new JTree.DynamicUtilTreeNode(
                     elTypes.next(), new Vector()) );
               }
            }
            else if(node.getUserObject().toString().equals(
               getResourceString("customElements")))
            {
               Iterator elTypes = 
                  m_deploymentServer.getCustomElementTypes().getResults();                  
               while(elTypes.hasNext())
               {
                  node.add( new JTree.DynamicUtilTreeNode(
                     elTypes.next(), new Vector()) );
               }
            }
            else if(node.getUserObject() instanceof PSCatalogResult) 
            { //this is element type node
               PSCatalogResult type = (PSCatalogResult)node.getUserObject();               
               String reqType;
               //if it is a custom element type node then request type should be 
               //'Custom/type'
               if( ((DefaultMutableTreeNode)node.getParent()).getUserObject().
                  toString().equals(getResourceString("customElements")))
               {
                  reqType = IPSDeployConstants.DEP_OBJECT_TYPE_CUSTOM + "/" + 
                     type.getID();
               }
               else
               {
                  reqType = type.getID();
               }
               Iterator elements = getSortedIterator(
                  m_deploymentServer.getDeploymentManager().
                  getDeployableElements(reqType));                                                   
               while(elements.hasNext())
               {
                  DefaultMutableTreeNode elNode = 
                     new DefaultMutableTreeNode(elements.next(), false);
                  node.add(elNode);
               }
            }            
         }
      }
      catch(PSDeployException e)
      {
         //display error message
         ErrorDialogs.showErrorMessage(this, e.getLocalizedMessage(), 
            getResourceString("errorTitle"));
      }
      finally
      {
         setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));        
      }

   }

   /**
    * Gets the sorted deployable elements according to its natural order of 
    * sorting. See {@link 
    * PSDependency#compareTo(Object obj)} for
    * the order of sorting.
    * 
    * @param elements the list of deployable elements to be sorted, assumed not
    * <code>null</code>, may be empty.
    * 
    * @return sorted elements, never <code>null</code>, may be empty if supplied
    * list is empty.
    */
   @SuppressWarnings("unchecked")
   private Iterator getSortedIterator(Iterator elements)
   {
      Set sortedElements = new TreeSet();
      while(elements.hasNext())
         sortedElements.add(elements.next());
      return sortedElements.iterator();
   }
   
   //nothing to implement
   public void treeWillCollapse(TreeExpansionEvent event)
   {
   }
   
   /**
    * Called when selection changes in 'Available Elements' tree. 'Add' button 
    * is enabled only if all selected node are deployable elements that can be
    * added to 'Elements to package' list and are not already added.
    * 
    * @param e the event that characterizes the change, assumed not to be <code>
    * null</code> as this method will be called by <code>Swing</code> model when
    * a tree selection changes.
    */
   public void valueChanged(TreeSelectionEvent e)
   {
      Object source = e.getSource();
      if(source == m_availElementsTree)
      {
         updateAddButtonState();
      }
   }
   
   /**
    * Updates the add button state 
    */
   private void updateAddButtonState()
   {
      //enable 'add' only if any avaiable selections have been made
      TreePath[] selPaths = m_availElementsTree.getSelectionPaths();
      boolean validSelections = true;
      
      if(selPaths == null)
         validSelections = false;

      for (int i = 0; validSelections && i < selPaths.length; i++)
      {
         DefaultMutableTreeNode node = 
            (DefaultMutableTreeNode)selPaths[i].getLastPathComponent();
         if (!(node.getUserObject() instanceof PSDeployableElement) || 
            doesPackageListContain((PSDeployableElement)node.getUserObject())) 
         {
            validSelections = false;
         }
      }
      
      m_addButton.setEnabled(validSelections);
   }

   /**
    * Called when selection changes in 'Elements to package' list. 'Remove' 
    * button is enabled only if there is selection in the list. 'Up' button is
    * enabled only if there is selection and top-most selected index is not at 
    * the top of the list. 'Down' button is enabled only if there is selection 
    * and bottom-most selected index is not at the bottom of the list.
    * 
    * @param e the event that characterizes the change, assumed not to be <code>
    * null</code> as this method will be called by <code>Swing</code> model when
    * selection changes in list.
    */
   public void valueChanged(ListSelectionEvent e)
   {
      int[] selIndices = m_packElementsList.getSelectedIndices();
      if(selIndices.length == 0)
      {
         m_removeButton.setEnabled(false);
      }
      else
      {
         m_removeButton.setEnabled(true);
      }
   }
   
   /**
    * Validates and saves dialog state into the descriptor and then calls
    * super's <code>onNext()</code> to hide the dialog.
    */
   public void onNext()
   {
      if(validateData())
      {
         savePackages();  
         super.onNext();       
      }
   }
   
   // see base class
   public Object getDataToSave()
   {
      return null;
   }

   // see base class
   public void onBack()
   {
      savePackages();
      setShouldUpdateUserSettings(true);
      super.onBack();
   }
   
   /**
    * Validates that there is at least one element exists in the 'Elements to 
    * package' list to add it to the descriptor. Displays message to the user
    * that there should atleast one element in the list to continue.
    * 
    * @return <code>true</code> if the validation succeeds, otherwise <code>
    * false</code>
    */
   protected boolean validateData()
   {
      DefaultListModel packElementModel = 
            (DefaultListModel)m_packElementsList.getModel();
      if(packElementModel.isEmpty())
      {
         ErrorDialogs.showErrorMessage(this, 
            getResourceString("noElements"),  
            getResourceString("errorTitle") );
         return false;
      }
      return true;
   }
   
   /**
    * Saves the selected packages to the descriptor
    */
   @SuppressWarnings("unchecked")
   private void savePackages()
   {
      DefaultListModel packElementModel = 
         (DefaultListModel)m_packElementsList.getModel();
      List packages = new ArrayList();
      for (int i = 0; i < packElementModel.getSize(); i++) 
      {
         PSDeployableElement element = 
            (PSDeployableElement)packElementModel.getElementAt(i);
         element.setIsIncluded(true);
         packages.add(element);
      }
      m_exportDescriptor.setPackages(packages.iterator());       
   }

   /**
    * Adds the selected node in the 'Available Elements' tree (<code>
    * m_availElementsTree</code>) if the node is a deployable element to 
    * 'Elements to package' list (<code>m_packElementsList</code>) and refreshes
    * the node to represent the node as already added node. If the node is 
    * already added to the list, it does nothing. If the adding element type is
    * <code>TYPE_COMMUNITY</code>, displays a dialog to user to choose from one
    * of the options 'Community Definition' and 'Entire Community'. If user 
    * chooses the first one it adds the community element alone, otherwise it 
    * gets the all self-deployable shared dependencies of the community and adds
    * those. If user cancels from the dialog, nothing is added. Displays an 
    * error message and adds community element alone if an exception happens 
    * getting the dependencies. 
    */  
   @SuppressWarnings("unchecked")
   private void onAdd()
   {
      TreePath[] selPaths = m_availElementsTree.getSelectionPaths();
      if(selPaths == null)
         return;
      
      boolean addFullCommunity = false;
      boolean didPromptCommunity = false;
      // build map of nodes to elements, test community option as we go
      Map nodeMap = new LinkedHashMap();
      for (int i = 0; i < selPaths.length; i++)
      {
         DefaultMutableTreeNode node = 
            (DefaultMutableTreeNode)selPaths[i].getLastPathComponent();
         if(node.getUserObject() instanceof PSDeployableElement)
         {
            PSDeployableElement element = 
               (PSDeployableElement)node.getUserObject();
            if(!doesPackageListContain(element))
            {
               if(element.getObjectType().equals(
                  IPSDeployConstants.TYPE_COMMUNITY) && !didPromptCommunity)
               {
                  // display option pane for adding the community element.
                  String commDef = getResourceString("commDef");
                  String entComm = getResourceString("entComm");
                  String[] options = new String[] {commDef, entComm};
                  String dlgTitle = getResourceString("commAddTitle");    
                  String dlgMsg = MessageFormat.format(
                     getResourceString("commAddMsg"), options);                     
                  
                  Object userSel = JOptionPane.showInputDialog(this, dlgMsg,
                     dlgTitle, JOptionPane.QUESTION_MESSAGE, null, 
                     options, commDef);
                  if(userSel != null)
                  {
                     if(commDef == userSel)
                        addFullCommunity = false;
                     else
                        addFullCommunity = true;
                     
                     didPromptCommunity = true;
                  }
                  else
                  {
                     // user cancelled, stop here
                     return;
                  }
               }
               
               nodeMap.put(node, element);
            }
         }
      }
      
      if (nodeMap.isEmpty())
         return;
      
      DefaultListModel model = 
         (DefaultListModel)m_packElementsList.getModel();
      DefaultTreeModel treeModel = 
         (DefaultTreeModel)m_availElementsTree.getModel();  
      Iterator entries = nodeMap.entrySet().iterator();
      while (entries.hasNext())
      {
         // walk each element and add to list model.  If a community, add full
         // community if user requested it
         Entry entry = (Entry) entries.next();
         DefaultMutableTreeNode node = (DefaultMutableTreeNode)entry.getKey();
         PSDeployableElement element = (PSDeployableElement)entry.getValue();
         if (element.getObjectType().equals(IPSDeployConstants.TYPE_COMMUNITY) 
            && addFullCommunity)
         {
            addEntireCommunity(element, model);
         }
         else
         {
            addElementSorted(element, model);            
         }
         
         //refresh the node
         treeModel.nodeChanged(node);
         
         // update the add button state
         updateAddButtonState();
      }       
   }
   
   /**
    * Gets all shared dependencies of the supplied element and its local 
    * dependencies and adds them to the supplied model to display in the list. 
    * If the shared dependency is a self-deployable element it adds it to the 
    * model to represent it as a separate package, otherwise it includes the 
    * dependency. Loads the local dependencies of the included dependency.
    * 
    * @param element the community element, assumed not <code>null</code>
    * @param model the list model to which the elements need to be added, 
    * assumed not <code>null</code>
    */
   @SuppressWarnings("unchecked")
   private void addEntireCommunity(PSDeployableElement element, 
      DefaultListModel model)
   {
      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      try
      {
         PSDependencyHelper.loadAllLocalDependencies(element,
            m_deploymentServer);

         Iterator dependencies = element.getDependencies();
         while (dependencies.hasNext())
         {
            PSDependency dep = (PSDependency) dependencies.next();
            if (dep.getDependencyType() == PSDependency.TYPE_LOCAL)
            {
               if (dep.getDependencies() == null)
                  m_deploymentServer.getDeploymentManager().loadDependencies(
                     dep);
               Iterator childDeps = dep.getDependencies();
               while (childDeps.hasNext())
               {
                  PSDependency childDep = (PSDependency) childDeps.next();
                  if (childDep.getDependencyType() == PSDependency.TYPE_SHARED)
                  {
                     if (childDep instanceof PSDeployableElement)
                     {
                        PSDeployableElement child = 
                           (PSDeployableElement) childDep;
                        addCommunityDependency(child, model);
                     }
                     else
                     {
                        PSDependencyHelper.loadAllLocalDependencies(childDep,
                           m_deploymentServer);
                        if (childDep.canBeIncludedExcluded())
                           childDep.setIsIncluded(true);
                     }
                  }
               }
            }
            else if (dep.getDependencyType() == PSDependency.TYPE_SHARED)
            {
               if (dep instanceof PSDeployableElement)
               {
                  PSDeployableElement child = (PSDeployableElement) dep;
                  addCommunityDependency(child, model);
               }
               else
               {
                  PSDependencyHelper.loadAllLocalDependencies(dep,
                     m_deploymentServer);
                  if (dep.canBeIncludedExcluded())
                     dep.setIsIncluded(true);
               }
            }
         }
      }
      catch (PSDeployException e)
      {
         ErrorDialogs.showErrorMessage(this, e.getLocalizedMessage(),
            getResourceString("errorTitle"));
      }
      addElementSorted(element, model);
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));      
   }
   
   /**
    * Adds a community dependency to the supplied model if it is a deployable
    * element that has not already been added to the model.
    *  
    * @param dep The deployable element to add, assumed not <code>null</code>.
    * @param model The model to which it may be added, assumed not 
    * <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private void addCommunityDependency(PSDeployableElement dep, 
      DefaultListModel model)
   {
      if (!dep.getObjectType().equals(
         IPSDeployConstants.TYPE_COMPONENT))
      {
         // build set of current element keys - this is not efficient to do each
         // time, but this is not commonly used functionality, and the number of
         // items in the list is unlikely to be that large
         Set curDeps =  new HashSet();
         for (int i = 0; i < model.size(); i++)
            curDeps.add(((PSDeployableElement)model.get(i)).getKey());

         // now check to see if it's been added.
         if (!curDeps.contains(dep.getKey()))
            addElementSorted(dep, model);
      }
   }
   
   /**
    * Checks whether the supplied element exists in the 'Elements to package'
    * list by case sensitive comparison of its key.
    * 
    * @param element the element to check, assumed not to be <code>null</code>
    * 
    * @return <code>true</code> if it exists, otherwise <code>false</code>
    */
   private boolean doesPackageListContain(PSDeployableElement element)
   {
      ListModel packElementModel = m_packElementsList.getModel();
      for (int i = 0; i < packElementModel.getSize(); i++) 
      {
         PSDeployableElement listEl = 
            (PSDeployableElement)packElementModel.getElementAt(i);
         if(element.getKey().equals(listEl.getKey()))
            return true;
      }
      
      return false;
   }
   
   /**
    * Called when user removes's an object from the 'Elements to package' list. 
    * Removes the selected elements from the list and refreshes the visible 
    * nodes in the tree to refresh its state as not added if any of them are
    * removed.
    */
   private void onRemove()
   {
      DefaultListModel packElementModel = 
         (DefaultListModel)m_packElementsList.getModel();
      int[] selIndices = m_packElementsList.getSelectedIndices();
      for (int i = selIndices.length-1; i >= 0 ; i--) 
      {
         packElementModel.removeElementAt(selIndices[i]);
      }
      
      refreshVisibleTreeNodes();
   }
   
   /**
    * Refreshes the visible nodes in the tree to refresh its state 
    * (added/not added).
    */
   private void refreshVisibleTreeNodes()
   {
      DefaultTreeModel treeModel = 
         (DefaultTreeModel)m_availElementsTree.getModel();
      int count = m_availElementsTree.getRowCount();
      for(int i = 0; i <count; i++)
      {
         TreePath path = m_availElementsTree.getPathForRow(i);
         DefaultMutableTreeNode visNode = 
            (DefaultMutableTreeNode)path.getLastPathComponent();
         if(visNode.isLeaf())
            treeModel.nodeChanged(visNode);   
      }
   }
   
   /**
    * Adds an element to the supplied model in sorted order.
    *  
    * @param dep The deployable element to add, assumed not <code>null</code>.
    * @param model The model to which it is added, assumed not
    * <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private void addElementSorted(PSDeployableElement de, DefaultListModel model)
   {
      List depElements = new ArrayList();
      depElements.add(de);
      
      for (int i = 0; i < model.size(); i++)
      {
         depElements.add(model.get(i)); 
      }
      
      // sort the elements
      Collections.sort(depElements, ms_sortComp);
           
      // clear the existing elements
      model.clear();
            
      // add the sorted elements
      for (Object o : depElements)
      {
         model.addElement(o);
      }
   }
   
   /**
    * The renderer used with 'Available Elements' tree to set different 
    * foreground color (cyan) for the deployable elements that are already 
    * exists in 'Elements to package' list.
    */
   private class TreeElementCellRenderer extends DefaultTreeCellRenderer
   {
      public Component getTreeCellRendererComponent(JTree tree,
         Object value, boolean selected, boolean expanded, boolean leaf, 
         int row, boolean hasFocus)
      {
         super.getTreeCellRendererComponent(tree, value, selected, expanded,
            leaf, row, hasFocus);

         DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
         if(node.getUserObject() instanceof PSDeployableElement)
         {
            PSDeployableElement element = 
               (PSDeployableElement)node.getUserObject();
            if( doesPackageListContain(element) )
               setForeground(Color.cyan);
            setText(element.getDisplayName());
         }
         
         return this;
      }
   }
   
   /**
    * The tree which displays the available deployable elements of the 
    * deployment server, initialized in <code>createAvialableElementsPanel()
    * </code> and never <code>null</code> after that. The tree nodes gets added
    * as user expands the tree.
    */
   private JTree m_availElementsTree;
   
   /**
    * The list box which displays all the deployable elements that are to be 
    * packaged, initialized in <code>createPackageElementsPanel()</code> and 
    * never <code>null</code> after that. The elements will be added to the list
    * as user adds or in <code>init()</code> if the descriptor supplied to this 
    * dialog has elements.
    */
   private JList m_packElementsList;
   
   /**
    * The button used to add the selected deployable element in the <code>
    * m_availElementsTree</code> tree to <code>m_packElementsList</code> list, 
    * initialized in <code>initDialog()</code> and never <code>null</code> after
    * that. This will be disabled if there is no selection in the tree or if the
    * selected element is not a deployable element or the selected deployable
    * element is already added to the list.
    */ 
   private UTFixedButton m_addButton;
   
   /**
    * The button used to remove the selected elements in the <code>
    * m_packElementsList</code> list, initialized in <code>initDialog()</code> 
    * and never <code>null</code> after that. This will be disabled if there is 
    * no selection in the list.
    */
   private UTFixedButton m_removeButton;

   /**
    * The descriptor passed into <code>onShow(PSDescriptor)</code>, casted to 
    * an export descriptor for convenience in {@link #init()}. Never <code>null
    * </code> after it is initialized and descriptor state will change in <code>
    * onNext()</code>
    */
   private PSExportDescriptor m_exportDescriptor = null;
   
   /**
    * Comparator for PSDeployableElement.  Compares based on object type name,
    * then display name, then dependency id.
    */
   private static final Comparator<PSDeployableElement> ms_sortComp =
      new Comparator<PSDeployableElement>()
      {
         public int compare(PSDeployableElement e1, PSDeployableElement e2)
         {
            int result = e1.getObjectTypeName().compareToIgnoreCase(
                  e2.getObjectTypeName());
            if (result == 0)
            {
               result = e1.getDisplayName().compareToIgnoreCase(
                     e2.getDisplayName());
            }
            if (result == 0)
            {
               result = e1.getDependencyId().compareToIgnoreCase(
                     e2.getDependencyId());
            }

            return result;
         }
      };
}
