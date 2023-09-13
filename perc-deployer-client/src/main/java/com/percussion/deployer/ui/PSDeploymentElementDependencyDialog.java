/******************************************************************************
 *
 * [ PSDeploymentElementDependencyDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.deployer.ui;

import com.percussion.UTComponents.UTFixedButton;
import com.percussion.deployer.client.IPSDeployConstants;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDependencyContext;
import com.percussion.deployer.objectstore.PSDependencyTreeContext;
import com.percussion.deployer.objectstore.PSDeployableElement;
import com.percussion.deployer.objectstore.PSExportDescriptor;
import com.percussion.deployer.objectstore.PSUserDependency;
import com.percussion.error.PSDeployException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSListPanel;
import com.percussion.guitools.PSPropertyPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * The dialog that allows to package the deployable elements in the export 
 * descriptor with its dependencies or ancestors.
 */
public class PSDeploymentElementDependencyDialog  extends 
   PSDeploymentWizardDialog implements TreeWillExpandListener, MouseListener
{
   /**
    * Constructs this dialog.
    * 
    * Constructs this dialog calling {@link 
    * PSDeploymentWizardDialog#PSDeploymentWizardDialog(Frame, 
    * PSDeploymentServer, int, int) super(parent, server, step, sequence)}.
    * Additional parameter is described below.
    *  
    * @param isTypical if <code>true</code> the dialog is shown in 'typical' 
    * mode, otherwise in 'custom' mode. 
    */
   public PSDeploymentElementDependencyDialog(Frame parent, 
      PSDeploymentServer deploymentServer, int step, int sequence, 
      boolean isTypical)
   {
      super(parent, deploymentServer, step, sequence);
      
      m_isTypical = isTypical;  
      
      initDialog();    
   }
   
   
   /**
    * Creates the dialog framework with border layout keeping the description 
    * panel on north, controls panel on center and command panel on south.
    */
   private void initDialog()
   {
      JPanel panel = new JPanel();
      getContentPane().add(panel);
      panel.setLayout(new BorderLayout(10, 20));
      EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);
      panel.setBorder(emptyBorder);
      
      setTitle(getResourceString("title"));
      
      int typicalSteps = 2;
      int customSteps = 0;
      try 
      {
         typicalSteps = Integer.parseInt(getResourceString("typicalStepCount"));
         if(!m_isTypical)
         {
            customSteps = 1; //initialize to default value
            customSteps = Integer.parseInt(getResourceString("customStepCount"));
         }
      }
      catch (NumberFormatException ex) 
      {
         //uses the default  
      }
      String[] description = new String[typicalSteps + customSteps];
      for (int i = 1; i <= typicalSteps; i++) 
      {
         description[i-1] = getResourceString("typicalDesc" + i);
      }
      for (int i = 1; i <= customSteps; i++) 
      {
         description[typicalSteps+i-1] = getResourceString("customDesc" + i);
      }
      
      JPanel descPanel = createDescriptionPanel(
         getResourceString("descTitle"), description);
      panel.add(descPanel, BorderLayout.NORTH);
      
      m_listPanel = new PSListPanel(getResourceString("packagesLabel"), 
         PSListPanel.ALIGN_RIGHT, false); 
      JPanel listPanel = new JPanel(new BorderLayout());
      listPanel.setBorder(BorderFactory.createEtchedBorder(
            EtchedBorder.RAISED));
      listPanel.add(m_listPanel, BorderLayout.CENTER);
       
      panel.add(listPanel, BorderLayout.CENTER);
      
      panel.add(createCommandPanel(true), BorderLayout.SOUTH);
   }

   /**
    * Adds the tabs for each deployable element in the descriptor. See <code>
    * super.init()</code> for more description.
    */
   @SuppressWarnings("unchecked")
   protected void init() 
   {
      m_treeCtx = new PSDependencyTreeContext();
      getOwner().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      m_exportDescriptor = (PSExportDescriptor)m_descriptor;
      try {
         m_listPanel.removeAll();
         Iterator packages = m_exportDescriptor.getPackages();      
         while(packages.hasNext())
         {
            PSDeployableElement element = (PSDeployableElement)packages.next();
            addPanel(element);
         }
      }
      catch(PSDeployException e)
      {
         ErrorDialogs.showErrorMessage(getOwner(), e.getLocalizedMessage(), 
            getResourceString("errorTitle"));
      }
      pack();      
      center();    
      setResizable(true);
      getOwner().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));      
   }
   
   /**
    * Loads all local dependencies recursively of the element if the element is 
    * not yet loaded with its local dependencies.
    * 
    * @param element the element for which to load dependencies, assumed not to
    * be <code>null</code>
    * 
    * @throws PSDeployException if an error occurs loading dependencies
    */
   private void loadDependencies(PSDependency element) throws PSDeployException
   {
      PSDependencyHelper.loadAllLocalDependencies(element, m_deploymentServer);
   }
   
   /**
    * Loads the ancestors of the element if the element is not yet loaded 
    * with its ancestors. Displays an error message if loading fails.
    * 
    * @param element the element for which to load ancestors, assumed not to
    * be <code>null</code>
    * 
    * @throws PSDeployException if an error occurs loading ancestors
    */
   private void loadAncestors(PSDependency element) throws PSDeployException
   {
      if(element.getAncestors() == null)
      {
         m_deploymentServer.getDeploymentManager().loadAncestors(
            element);
      }
   }
   
   /**
    * The dependency panel that needs to be shown in each tab of this dialog.
    */
   private class PSDependencyPanel extends JPanel implements ActionListener, 
      TreeSelectionListener
   {
      /**
       * Constructs the panel with supplied deployable element.
       * 
       * @param element the deployable element represented by the tree of this
       * panel, may not be <code>null</code>
       * 
       * @throws IllegalArgumentException if element is <code>null</code>
       */
      public PSDependencyPanel(PSDeployableElement element)
      {
         if(element == null)
            throw new IllegalArgumentException("element may not be null.");
            
         m_depElement = element;
         initPanel();
      }
      
      /**
       * Initializes the panel with a tree and a panel with buttons to edit id 
       * types and set user dependencies. The buttons panel is added only if the
       * dialog container of this panel is invoked in 'Custom' mode. Adds 
       * listeners to the tree and the buttons.
       */
      private void initPanel()
      {
         setLayout(new BorderLayout(20, 20));
         setBorder(new EmptyBorder(20,20,20,20));         

         PSPropertyPanel descPanel = new PSPropertyPanel();
         m_descriptionField = new JTextField(m_depElement.getDescription());
         descPanel.addPropertyRow(getResourceString("eleDescription"), 
            new JComponent[]{m_descriptionField}, m_descriptionField,
            getResourceString("eleDescription.mn").charAt(0), null);
         add(descPanel, BorderLayout.NORTH);      
            
         m_depTree = new PSDependencyTree(m_depElement, true, m_treeCtx);         
         //Add a tree expansion listener to act on the node expansion to display
         //message dialogs to the user or to load the dependencies.
         m_depTree.addTreeWillExpandListener(
            PSDeploymentElementDependencyDialog.this);
            
         //Add a mouse listener to act on check-box node selection.
         m_depTree.addMouseListener(PSDeploymentElementDependencyDialog.this);
         m_depTree.addKeyListener(new KeyAdapter() 
            {
               public void keyPressed(KeyEvent e)
               {
                  if (e.getKeyCode() == KeyEvent.VK_SPACE)
                  {
                     if(e.getSource() instanceof PSDependencyTree)
                     {
                        e.consume();
                        PSDependencyTree tree = 
                           (PSDependencyTree) e.getSource();
                        TreePath path = tree.getSelectionPath();
                        if (path != null)
                        {
                           handleNodeEvent(tree, path);                           
                        }
                     }
                  } 
               }
            });
         JScrollPane pane = new JScrollPane(m_depTree);
         add(pane, BorderLayout.CENTER);
         
         JPanel bottomPanel = new JPanel();
         bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
         
         JPanel buttonPanel = new JPanel();
         buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));         
         buttonPanel.add(Box.createHorizontalGlue());                  

         m_userDepButton = new UTFixedButton(
            getResourceString("userDep"), new Dimension(160, 24)); 
         m_userDepButton.setEnabled(false);                          
         m_userDepButton.addActionListener(this); 
         buttonPanel.add(m_userDepButton);
         buttonPanel.add(Box.createHorizontalGlue());         
         bottomPanel.add(buttonPanel);
         bottomPanel.add(Box.createVerticalStrut(10));
         
         JPanel legendPanel = new JPanel(new BorderLayout());
         legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.X_AXIS));
         JLabel iconLabel = new JLabel(getResourceString("multipleDepsLegend"), 
            PSDeploymentClient.getImageLoader().getImage(
               PSDeploymentClient.getResources().getString(
                  "gif_showMulti")), JLabel.LEADING);
         legendPanel.add(iconLabel);
         legendPanel.add(Box.createHorizontalGlue());
         bottomPanel.add(legendPanel);
         
         add(bottomPanel, BorderLayout.SOUTH);            
         
         //Add a tree selection listener to update the state of 
         //'User Dependencies' button based on the selected dependency.
         m_depTree.addTreeSelectionListener(this);            
      }
      
      /**
       * Saves the description of the deployable element.
       */
      public void saveData()
      {
         m_depElement.setDescription(m_descriptionField.getText());
      }
      
      /**
       * Gets the deployable element represented by this panel.
       * 
       * @return the element, never <code>null</code>
       */
      public PSDeployableElement getDeployableElement()
      {
         return m_depElement;
      }
      
      /**
       * Makes the 'User Dependencies' button enabled only if the selected
       * node represents a <code>PSDependency</code> object and supports user
       * dependencies. Does nothing if the button is not initialized or shown
       * in the panel (Note:happens in typical mode).
       * 
       * @param e the event that characterizes the change, assumed not to be 
       * <code>null</code> as this method will be called by <code>Swing</code> 
       * model when a tree selection changes.
       */
      public void valueChanged(TreeSelectionEvent e)
      {
         if(m_userDepButton == null)
            return;
            
         TreePath selPath = e.getPath();
         DefaultMutableTreeNode selNode = 
            (DefaultMutableTreeNode)selPath.getLastPathComponent();
         if(selNode.getUserObject() instanceof PSDependency)
         {
            PSDependency dep = (PSDependency)selNode.getUserObject();
            if(dep.supportsUserDependencies())
            {
               m_userDepButton.setEnabled(true);
               return;
            }
         }
         m_userDepButton.setEnabled(false);
      }

      /**
       * Action method for the 'User Dependencies' button. Displays a dialog to 
       * set or edit user dependencies of the selected dependency in the tree.
       * 
       * @param e the action event, assumed not to be <code>null</code> as this 
       * method will be called by <code>Swing</code> model when an action event 
       * occurs
       */      
      public void actionPerformed(ActionEvent e)
      {
         if(e.getSource() == m_userDepButton)
         {
            updateUserDependencies();
         }
      }
      
      /**
       * Displays a dialog to add/update user dependencies for the selected 
       * dependency node in the tree. Should be called only if the dependency
       * supports user dependencies. Adds/updates all user dependencies added
       * in the dialog to the selected dependency and includes them by default.
       */      
      @SuppressWarnings("unchecked")
      private void updateUserDependencies()
      {
         TreePath selPath = m_depTree.getSelectionPath();
         if(selPath != null)
         {
            DefaultMutableTreeNode selNode = 
               (DefaultMutableTreeNode)selPath.getLastPathComponent();
            if(selNode.getUserObject() instanceof PSDependency)
            {
               PSDependency dep = (PSDependency)selNode.getUserObject();
               boolean isEditable = m_depTree.isEditableDependencyNode(selNode); 
               
               // get all dependencies of this node before adding user 
               //dependencies
               if( !dep.getObjectType().equals(
                  IPSDeployConstants.DEP_OBJECT_TYPE_CUSTOM)
                  && dep.getDependencies() == null)
               {
                  try {
                     // Get id types that need to be identified for this 
                     // dependency
                     Iterator idTypes = PSDependencyHelper.getIDTypes(
                        dep, m_deploymentServer, true);
                     if(idTypes.hasNext())
                     {                  
                        // Get id type mappings for this dependency and display 
                        // the dialog only if it has resources with incomplete 
                        // mappings.
                        if(!showIDTypesDialog(dep.getDisplayName(), 
                              idTypes, true, true))
                        {
                           JOptionPane.showMessageDialog(this, 
                              ErrorDialogs.cropErrorMessage(
                              getResourceString("mustIdentifyIDTypesMsg")), 
                              getResourceString("identifyIDTypesTitle"),
                              JOptionPane.INFORMATION_MESSAGE);                           
                           return;
                        }
                     }
                     loadDependencies(dep);
                  }
                  catch(PSDeployException e)
                  {
                     ErrorDialogs.showErrorMessage(
                        PSDeploymentElementDependencyDialog.this, 
                        e.getLocalizedMessage(), 
                        getResourceString("errorTitle"));
                     return;
                  }
               }
            
               PSUserDependencyDialog dlg = new PSUserDependencyDialog(
                  PSDeploymentElementDependencyDialog.this, 
                  m_deploymentServer, dep);
               dlg.setVisible(true);
               if (!dlg.isOk())
                  return;
               
               Iterator userDeps = 
                  dep.getDependencies(PSDependency.TYPE_USER);              
               while(userDeps.hasNext())
               {
                  PSUserDependency userDep = 
                     (PSUserDependency)userDeps.next();
                  if(userDep.canBeIncludedExcluded())
                     userDep.setIsIncluded(true);
               }                  
               DefaultMutableTreeNode depNode;
               if(selNode.getChildCount() > 0 &&
                  ((DefaultMutableTreeNode)selNode.getFirstChild()).
                  getUserObject().toString().equals(
                  PSDependencyTree.ms_dependencies) )
               {  
                  depNode = (DefaultMutableTreeNode)selNode.getFirstChild();
                  m_depTree.removeUserDependencies(depNode);                     
               }
               else
               {                  
                  depNode = new DefaultMutableTreeNode(
                     PSDependencyTree.ms_dependencies, true);
                  selNode.insert(depNode, 0); //insert that as the first node
               }
               m_depTree.addDependencyNodes(
                  depNode, dep, PSDependency.TYPE_USER, isEditable);
               boolean hasUserDeps = depNode.getChildCount() > 0;
               if (!hasUserDeps)
               {
                  // no user dependencies, remove the node
                  depNode.removeFromParent();                  
               }
               ((DefaultTreeModel)m_depTree.getModel()).
                  nodeStructureChanged(selNode);
               //expand user-defined node to see all added dependencies.
               if (hasUserDeps)
               {
                  m_depTree.expandPath(new TreePath(
                     ((DefaultMutableTreeNode) depNode.getLastChild())
                        .getPath()));                  
               }
               
               // re-select the node
               m_depTree.setSelectionPath(selPath);
            }
         }
      }
      
      /**
       * The deployable element/package represented by tree in this panel, 
       * initialized in the constructor and never <code>null</code> or modified 
       * after that.
       */
      private PSDeployableElement m_depElement;
      
      /**
       * The field to enter description for the package, initialized in <code> 
       * initPanel()</code> and never <code>null</code> or modified after that.
       */
      private JTextField m_descriptionField;
      
      /**
       * The tree to display the deployable element with its dependencies and 
       * ancestors,  initialized in <code>initPanel()</code> and never <code>
       * null</code> after that. The child nodes will be added as user expands
       * the tree.
       */
      private PSDependencyTree m_depTree;
      
      /**
       * The button to set user dependencies on a dependency, initialized in 
       * <code>initPanel()</code> only if the container dialog is constructed in 
       * custom mode and never <code>null</code> or modified after that.
       */
      private UTFixedButton m_userDepButton = null;
   }
   
   /**
    * Gets the deployable element from each panel and updates the list of 
    * packages in the order specified by user in this dialog to the descriptor 
    * supplied. Calls super's <code>onNext()</code> to hide the dialog.
    */
   @SuppressWarnings("unchecked")
   public void onNext()
   {
      List packages = new ArrayList();
      List noIncludedDepPackages = new ArrayList();
      getPanelData(packages, noIncludedDepPackages);
      
      if(!noIncludedDepPackages.isEmpty())
      {
         int option = JOptionPane.showConfirmDialog(this, 
            ErrorDialogs.cropErrorMessage( MessageFormat.format(
            getResourceString("noIncludedDepMsg"), 
            new String[] {noIncludedDepPackages.toString()}) ), 
            getResourceString("noIncludedDepTitle"),
            JOptionPane.YES_NO_OPTION);            
         if(option == JOptionPane.YES_OPTION)
         {
            if(packages.isEmpty())
            {
               ErrorDialogs.showErrorMessage(this, 
                  getResourceString("noElementsToArchive"), 
                  getResourceString("errorTitle"));
               return;
            }
         }
         else
            return;
      }
      m_exportDescriptor.setPackages(packages.iterator());  
               
      super.onNext(); 
   }

   /**
    * Retrieves data from each panel and adds it to the two supplied lists.
    * 
    * @param packages The list to which each {@link PSDeployableElement} that
    * has included child dependencies is added.  Assumed not <code>null</code>.
    * @param noIncludedDepPackages The list to which each 
    * {@link PSDeployableElement} that does not have any included child 
    * dependencies is added.  Assumed not <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private void getPanelData(List packages, List noIncludedDepPackages)
   {
      Iterator panels = m_listPanel.getPanels();
      while (panels.hasNext())
      {
         PSDependencyPanel depPanel = (PSDependencyPanel)panels.next();
         depPanel.saveData();
         PSDeployableElement element = depPanel.getDeployableElement();
         if(element.getChildCount(true) > 0 )
            packages.add(element);     
         else
            noIncludedDepPackages.add(element);
      }
   }


   // see base class
   public Object getDataToSave()
   {
      return null;
   }

   // see base class
   @SuppressWarnings("unchecked")
   public void onBack()
   {
      List packages = new ArrayList();
      List noIncludedDepPackages = new ArrayList();
      getPanelData(packages, noIncludedDepPackages);
      m_exportDescriptor.setPackages(packages.iterator());

      setShouldUpdateUserSettings(true);
      super.onBack();
   }
   
   /**
    * Action method for an expansion event on the dependency tree used in this
    * dialog. Gets the dependencies and ancestors of the expanding dependency 
    * node, if they are not loaded with the dependencies checks whether the 
    * dependency supports id types and if it supports checks that its ids are 
    * identified. If they are not identified then it prompts the user to 
    * identify the types for that dependency and loads the dependencies. Then 
    * adds the child nodes accordingly. Traverses the tree from the root if the 
    * loaded dependency or ancestor is already represented in the tree as 
    * dependency or ancestor of some other node to update its state.
    * 
    * @param e the tree expand event, assumed not to be <code>null</code>
    * as Swing model calls this method with an event when tree expansion event
    * occurs.
    */
   @SuppressWarnings("unchecked")
   public void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException
   {
      if(e.getSource() instanceof PSDependencyTree)
      {
         PSDependencyTree tree = (PSDependencyTree)e.getSource();
         TreePath path = e.getPath();
         DefaultMutableTreeNode node = 
            (DefaultMutableTreeNode)path.getLastPathComponent();
         try 
         {            
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));                     
            if(node.getUserObject() instanceof PSDependency) //PSDependencyNode
            {
               if(!node.isRoot() && 
                  node.getUserObject() instanceof PSDeployableElement)
               {
                  JOptionPane.showMessageDialog(this, 
                     ErrorDialogs.cropErrorMessage(
                     getResourceString("expandDepElementMsg")), 
                     getResourceString("expandDepElementTitle"),
                     JOptionPane.INFORMATION_MESSAGE);
                  return;
               }
               
               PSDependency dependency = (PSDependency)node.getUserObject();            
               if(dependency.getDependencies() == null)
               {
                  if(dependency.isDeployable())
                  {
                     //Get id types that need to be identified for this dependency
                     Iterator idTypes = PSDependencyHelper.getIDTypes(
                        dependency, m_deploymentServer, true);
                     if(idTypes.hasNext())
                     {                  
                        //Get id type mappings for this dependency and display the
                        //dialog only if it has resources with incomplete mappings.
                        if(!showIDTypesDialog(dependency.getDisplayName(), 
                              idTypes, true, true))
                        {
                           JOptionPane.showMessageDialog(this, 
                              ErrorDialogs.cropErrorMessage(
                              getResourceString("mustIdentifyIDTypesMsg")), 
                              getResourceString("identifyIDTypesTitle"),
                              JOptionPane.INFORMATION_MESSAGE);
                              
                           throw new ExpandVetoException(e,
                                 "id types must be identified");
                        }
                     }
                  }
                  loadDependencies(dependency);
               }               
            }
            else if( node.getUserObject().toString().equals(
               PSDependencyTree.ms_ancestors) )
            {
               //actual dependency for whom we should get ancestors
               DefaultMutableTreeNode dependencyNode = 
                  tree.getParentDependencyNode(node); 
               if(dependencyNode == null)
                  throw new IllegalStateException("invalid tree structure");               
               PSDependency dependency = 
                  (PSDependency)dependencyNode.getUserObject();
               PSDependency parentDependency = 
                     tree.getParentDependency(dependencyNode);                               
                     
               //if ancestors are not loaded, load them and remove the parent
               //dependency.
               if(dependency.getAncestors() == null)
               {
                  loadAncestors(dependency);               
                  Iterator ancestors = dependency.getAncestors();
                  while(ancestors.hasNext())
                  {
                     PSDependency ancestor = (PSDependency)ancestors.next();
                     if( parentDependency != null && 
                        ancestor.getKey().equals(parentDependency.getKey()) )
                     {
                        ancestors.remove();
                     }
                  }  

               }
               //If the ancestor nodes are not yet created, create them
               if(dependency.getAncestors().hasNext() && 
                  node.getChildCount() == 0)
               {                  
                  tree.addAncestorNodes(parentDependency, dependency, node);                            
               }
            }                     

         }
         catch (PSDeployException ex) 
         {
            ErrorDialogs.showErrorMessage(this, ex.getLocalizedMessage(), 
               getResourceString("errorTitle"));
            throw new ExpandVetoException(e,
               "could not load dependencies or ancestors");
         }            
         finally
         {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));        
         }
      }
   }

   //nothing to implement
   public void treeWillCollapse(TreeExpansionEvent e) 
   {
   }
   
 
   /**
    * Action method for a mouse click event on the dependency tree used in this
    * dialog. Interprets the mouse-click event on a shared or user-defined 
    * dependency node and ancestor node (check-box nodes) as the check or 
    * uncheck events depending on its current state. Displays a confirmation 
    * dialog to add/remove the tab if the dependency is a deployable element 
    * depending on its state. Displays a dialog with a message to identify the
    * literal id types if the dependency or local dependencies of the element 
    * supports id types.
    * 
    * @param e the mouse event, assumed not to be <code>null</code>
    * as Swing model calls this method with an event when tree expansion event
    * occurs.
    */
   public void mouseClicked(MouseEvent e)
   {  
      if(e.getSource() instanceof PSDependencyTree)
      {
         PSDependencyTree tree = (PSDependencyTree)e.getSource();
         TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());

         if(path != null) 
         {
            Rectangle bounds = tree.getPathBounds(path);
            //consider only if it is a mouse-click event on the node renderer.
            if(e.getX() >= bounds.getX() && 
               e.getX() <= (bounds.getX() + bounds.getWidth()) )
            {
               handleNodeEvent(tree, path);
            }
         }
      }
   }
   
   /**
    * Handles an event on the dependency tree (either a mouse click or space bar 
    * press on a dependency node)
    * 
    * @param tree The tree on which the event occured, assumed not 
    * <code>null</code>
    * @param path The path on which to act, assumed not <code>null</code> and to
    * be valid for the supplied <code>tree</code>.
    */
   @SuppressWarnings("unchecked")
   private void handleNodeEvent(PSDependencyTree tree, TreePath path)
   {
      DefaultMutableTreeNode node = 
         (DefaultMutableTreeNode)path.getLastPathComponent();
      
      if (node != null && !node.isRoot() && 
         node.getUserObject() instanceof PSDependency)
      {   
         try {      
            PSDependency element = (PSDependency)node.getUserObject();
            
            PSDependencyContext depCtx = m_treeCtx.getDependencyCtx(
               element);
            if (depCtx== null)
            {
               // a bug if no ctx found
             JOptionPane.showConfirmDialog(this, 
                "Selected dependency missing from tree context", 
                "Error",
                JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
            }
            
            if(depCtx.canBeSelected() && 
               tree.isEditableDependencyNode(node))
            {
               setCursor(Cursor.getPredefinedCursor(
                  Cursor.WAIT_CURSOR));
               boolean isIncluded = element.isIncluded();                                  
               if(element instanceof PSDeployableElement)
               {                                     
                  //if user is including this, check to see if it has
                  //already been included and display a message if it
                  // has.  Otherwise, show a confirmation dialog 
                  //and then display ID Types dialog and then add the 
                  //new tab and set that as included. 
                  //else if we are unchecking, confirm with the user to 
                  // remove the element from the archive and delete the 
                  // tab and set the element as not included.
                  if(isIncluded) 
                  {
                     // can't remove self
                     if (element.getKey().equals(
                        tree.getRootElement().getKey()))
                     {
                        //cantRemoveDepElementMsg
                        JOptionPane.showMessageDialog(this, 
                           ErrorDialogs.cropErrorMessage(
                           getResourceString(
                              "cantRemoveDepElementMsg")), 
                           getResourceString("removeDepElementTitle"),
                           JOptionPane.INFORMATION_MESSAGE);
                           return;
                     }
                     
                     //already included, so it is removing
                     int option = JOptionPane.showConfirmDialog(this, 
                        ErrorDialogs.cropErrorMessage(
                        getResourceString("removeDepElementMsg")), 
                        getResourceString("removeDepElementTitle"),
                        JOptionPane.YES_NO_OPTION);
                     if(option == JOptionPane.NO_OPTION)
                        return;
                     setDepIncluded(depCtx, !isIncluded);
                     removePanel((PSDeployableElement)element);
                  }
                  else
                  {
                     int option = JOptionPane.showConfirmDialog(this, 
                        ErrorDialogs.cropErrorMessage(
                        getResourceString("includeDepElementMsg")), 
                        getResourceString("includeDepElementTitle"),
                        JOptionPane.YES_NO_OPTION);
                     if(option == JOptionPane.NO_OPTION)
                        return;
                                                   
                     Iterator idTypes = PSDependencyHelper.getIDTypes(
                        element, m_deploymentServer, m_isTypical);
                     if(idTypes.hasNext())
                     {
                        if(!showIDTypesDialog(element.getDisplayName(), 
                           idTypes, m_isTypical, true))
                        {
                           JOptionPane.showMessageDialog(this, 
                              ErrorDialogs.cropErrorMessage(
                              getResourceString(
                                 "mustIdentifyIDTypesMsg")), 
                              getResourceString(
                                 "identifyIDTypesTitle"),
                              JOptionPane.INFORMATION_MESSAGE);
                              return;
                        }
                     }
                     setDepIncluded(depCtx, !isIncluded);
                     insertPanel((PSDeployableElement)element.clone());                                
                  }
               }
               else
               {
                  //if user is including this, check for id types are  
                  //set for this dependency or not and
                  //then display id types dialog if not.
                  if(!isIncluded)
                  {
                     Iterator idTypes = PSDependencyHelper.getIDTypes(
                        element, m_deploymentServer, true);
                     if(idTypes.hasNext())
                     {
                        if(!showIDTypesDialog(element.getDisplayName(), 
                           idTypes, true, true))
                        {
                           JOptionPane.showMessageDialog(this, 
                              ErrorDialogs.cropErrorMessage(
                              getResourceString("mustIdentifyIDTypesMsg")), 
                              getResourceString("identifyIDTypesTitle"),
                              JOptionPane.INFORMATION_MESSAGE);
                              return;
                        }
                     }
                   }                           
                  setDepIncluded(depCtx, !isIncluded);
               }
            }  
         }
         catch(PSDeployException ex)
         {
            ErrorDialogs.showErrorMessage(this, 
               ex.getLocalizedMessage(), 
               getResourceString("errorTitle"));
         }
         finally
         {
            setCursor(Cursor.getPredefinedCursor(
               Cursor.DEFAULT_CURSOR));   
         }
         ((DefaultTreeModel)tree.getModel()).nodeChanged(node);
      }
   }
   
   /**
    * Attempts to set the supplied context as included/excluded and displays an 
    * error if it fails.
    * 
    * @param depCtx The context to set, assumed not <code>null</code>
    * @param isIncluded <code>true</code> to include it, <code>false</code> to
    * excluded it.
    */
   private void setDepIncluded(PSDependencyContext depCtx, boolean isIncluded)
   {
      if (!depCtx.setIncluded(isIncluded))
      {
         // a bug if we couldn't select it
         JOptionPane.showConfirmDialog(this, "Selection " +
            "change invalid for the selected dependency", 
            "Error",
            JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);                            
      }
   }
   
   //nothing to implement
   public void mousePressed(MouseEvent e) {}

   //nothing to implement
   public void mouseReleased(MouseEvent e) {}

   //nothing to implement
   public void mouseEntered(MouseEvent e) {}

   //nothing to implement
   public void mouseExited(MouseEvent e) {}

   /**
    * Displays the id types dialog with the supplied parameters only if the list
    * of id types that needs to be shown is not empty. 
    * 
    * @param identifier the identifier to use in the dialog, assumed not <code>
    * null</code> or empty.
    * @param idTypes the list of <code>PSApplicationIDTypes</code> for the 
    * dependencies that supports id types, assumed not <code>null</code> or 
    * empty.
    * @param incompleteOnly if <code>true</code> shows only incomplete id type
    * mappings, otherwise all.
    * @param informUser if <code>true</code> informs user with a message before
    * displaying the dialog.
    * 
    * @return <code>true</code> if the user returned from the dialog by clicking
    * OK, otherwise <code>false</code>
    * 
    * @throws PSDeployException if exception happens getting id types for the 
    * dependencies.
    */
   @SuppressWarnings("unchecked")
   private boolean showIDTypesDialog(String identifier, Iterator idTypes, 
      boolean incompleteOnly, boolean informUser) throws PSDeployException
   {
      boolean isSuccess = true;
      
      if(informUser)
      {
         JOptionPane.showMessageDialog(this, 
            ErrorDialogs.cropErrorMessage(
            getResourceString("identifyIDTypesMsg")), 
            getResourceString("identifyIDTypesTitle"),
            JOptionPane.INFORMATION_MESSAGE);
      }
                        
      PSIDTypesDialog dlg = new PSIDTypesDialog(this,
         m_deploymentServer, identifier, idTypes, true, incompleteOnly);
      dlg.setVisible(true);
      if(!dlg.isOk())
         isSuccess = false;

      return isSuccess;
   }
   
   /**
    * Adds a panel for supplied element. Loads dependencies and ancestors of the
    * element and updates the dependencies/ancestors that are deployable 
    * elements inclusion state to true if they are already added in this panel.
    * Leaves the currently selected panel as the selection, or the first panel
    * if none have been selected.
    * 
    * @param element the element to add tab, assumed not to be <code>null</code>
    * 
    * @throws PSDeployException if an error loading the dependencies or 
    * ancestors.
    */
   private void addPanel(PSDeployableElement element) throws PSDeployException
   {

      String title = element.getDisplayIdentifier();
      //get or load dependencies/ancestors
      loadDependencies(element); 
      
      m_listPanel.addPanel(title, new PSDependencyPanel(element));
   }
   
   /**
    * Inserts a new panel to represent the supplied element just before the 
    * current selected panel, making it the selected panel.
    * 
    * @param element the element for new tab, assumed not to be <code>null</code>
    * 
    * @throws PSDeployException if an error loading the dependencies.
    */
   private void insertPanel(PSDeployableElement element) 
      throws PSDeployException
   {
      String title = element.getDisplayIdentifier();
      //get or load dependencies
      loadDependencies(element);
      
      m_listPanel.addPanel(title, new PSDependencyPanel(element), 
         m_listPanel.getSelectedIndex(), true);
   }
   
   /**
    * Removes the tab representing this element if it exists. Uses case 
    * sensitive comparison of element's keys.
    * 
    * @param element the deployable element, assumed not to be <code>null</code>
    */
   @SuppressWarnings("unchecked")
   private void removePanel(PSDeployableElement element)
   {
      PSDependencyPanel depPanel = getPanel(element);
      if (depPanel != null)
      {
         m_listPanel.removePanel(depPanel);
         
         // check local and prompt for decision
         boolean removeLocal = true;
         Map localMap = m_treeCtx.checkRemoveLocal(
            depPanel.getDeployableElement());
         if (localMap != null)
         {
            // build sets of pkg and dep names
            Set depSet = new HashSet();
            Set pkgSet = new HashSet();
            Iterator entries = localMap.entrySet().iterator();
            while (entries.hasNext())
            {
               Entry entry = (Entry)entries.next();
               String pkgKey = (String)entry.getKey();               
               pkgSet.add(m_treeCtx.getPackage(pkgKey).getDisplayIdentifier());
               Iterator deps = ((List)entry.getValue()).iterator();
               while (deps.hasNext())
               {
                  depSet.add(
                     ((PSDependency)deps.next()).getDisplayIdentifier());
               }
            }

            int option = JOptionPane.showConfirmDialog(this, 
               ErrorDialogs.cropErrorMessage(
                  MessageFormat.format(
                     getResourceString("removeLocalMsg"), 
                     new Object[] {depSet, pkgSet})), 
               getResourceString("removeLocalTitle"),
               JOptionPane.YES_NO_OPTION);
               
            if(option == JOptionPane.NO_OPTION)
               removeLocal = false;             
         }
         m_treeCtx.removePackage(depPanel.getDeployableElement(), removeLocal);
      }
         
   }
   
   /**
    * Gets the panel representing the supplied element.  Uses case sensitive 
    * comparison of element's keys.
    * 
    * @param element The element, assumed not <code>null</code>.
    * 
    * @return The corresponding panel, or <code>null</code> if a matching panel
    * is not found.
    */
   @SuppressWarnings("unchecked")
   private PSDependencyPanel getPanel(PSDeployableElement element)
   {
      PSDependencyPanel panel = null;
      Iterator panels = m_listPanel.getPanels();
      while (panels.hasNext() && panel == null)
      {
         PSDependencyPanel test = (PSDependencyPanel)panels.next();
         if(test.getDeployableElement().getKey().equals(element.getKey()))
            panel = test;
      }
      
      return panel;
   }
   
   
   /**
    * The list panel to hold a panel for each element, initialized in 
    * <code>initDialog()</code> and never <code>null</code> after that. The 
    * panels get added in <code>init()</code> according to the descriptor and as 
    * the user includes a deployable element while traversing the tree. The 
    * panels may be removed if user unchecks the deployable element that is 
    * included.
    */
   private PSListPanel m_listPanel;

   /**
    * The dialog invoked mode setting, initialized in the constructor and never
    * modified after that. <code>true</code> to indicate typical mode, <code>
    * false</code> to indicate custom mode. The 'Edit ID Types' and 'User 
    * Dependencies' buttons in <code>PSDependencyPanel</code> won't be shown in 
    * typical mode.
    */
   private boolean m_isTypical;
   
   /**
    * The descriptor passed into <code>onShow(PSDescriptor)</code>, casted to 
    * an export descriptor for convenience in {@link #init()}. Never <code>null
    * </code> after it is initialized and descriptor state will change in <code>
    * onNext()</code>
    */
   private PSExportDescriptor m_exportDescriptor = null;
   
      
   /**
    * Tree context used to handle cross-pacakge dependency state.  
    * Initialized during construction, never <code>null</code> or modified
    * after that.
    */
   private PSDependencyTreeContext m_treeCtx; 
}
