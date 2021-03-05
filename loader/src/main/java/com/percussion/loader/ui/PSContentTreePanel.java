/*[ PSContentTreePanel.java ]***************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.guitools.ErrorDialogs;
import com.percussion.loader.IPSContentSelector;
import com.percussion.loader.IPSContentTree;
import com.percussion.loader.IPSContentTreeNode;
import com.percussion.loader.IPSItemExtractor;
import com.percussion.loader.IPSLogCodes;
import com.percussion.loader.IPSStatusListener;
import com.percussion.loader.PSContentLoaderMgr;
import com.percussion.loader.PSItemContext;
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.PSLoaderUtils;
import com.percussion.loader.PSLogMessage;
import com.percussion.loader.PSPluginFactory;
import com.percussion.loader.PSStatusEvent;
import com.percussion.loader.objectstore.PSConnectionDef;
import com.percussion.loader.objectstore.PSContentSelectorDef;
import com.percussion.loader.objectstore.PSExtractorDef;
import com.percussion.loader.objectstore.PSLoaderDescriptor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;

/**
 * A panel with one main component. A tree that represents the content
 * selected by a <code>IPSContentSelector</code>.
 */
public class PSContentTreePanel extends JPanel
   implements ActionListener, IPSStatusListener, TreeSelectionListener
{
   /**
    * Constructor with no model hook up
    * @param a reference to the parent {@link PSMainFrame} object.
    * May not be <code>null</code>.
    */
   public PSContentTreePanel(PSMainFrame parent)
   {
      super();
      if(null == parent)
         throw new IllegalArgumentException("Parent frame cannot be null.");

      m_parent = parent;

      if (ms_res == null)
         ms_res = ResourceBundle.getBundle(getClass().getName() + "Resources",
            Locale.getDefault());
   }

   /**
    * Initialize the panel and load the model.
    *
    * @param def the objectstore defintion of the selection that
    *    is desired. Never <code>null</code>.
    *
    * @param tree IPSContentTree that contains the data.
    *    Never <code>null</code>
    *
    * @throws IllegalArgumentException if <code>def</code> is
    *    <code>null</code>
    */
   public void init(PSLoaderDescriptor def, IPSContentTree tree)
      throws PSLoaderException
   {
      if (def == null)
         throw new IllegalArgumentException(
            "def must not be null");

      if (tree == null)
         throw new IllegalArgumentException(
            "tree must not be null");

      setDescriptorInfo(def);

      setLayout(new BorderLayout());

      try
      {
         m_model = new PSUIContentTreeModel(def.getContentSelectorDef(),
            tree);
      }
      catch (Exception e)
      {
         throw new IllegalArgumentException(
            "def must be a valid PSContentSelectorDef");
      }

      // Initialize the tree components
      initTree(def);

      add(m_tree);
   }

   /**
    * Method to dispose of any resources this class may be using.
    */
   public void cleanup()
   {
      m_model = null;
      removeAll();
   }

   /**
    * Sets the descriptor info from a loader descriptor.
    *
    * @param desc, loader descriptor, cannot be <code>null</code>.
    *
    * @throws PSLoaderException, if there is a problem getting the selector.
    */
   private void setDescriptorInfo(PSLoaderDescriptor desc)
      throws PSLoaderException
   {
      if (desc == null)
         throw new IllegalArgumentException("desc cannot be null");

      PSContentSelectorDef selectorDef = desc.getContentSelectorDef();
      PSPluginFactory factory = PSPluginFactory.getInstance();

      m_selector = factory.newContentSelector(selectorDef);
      m_connDef = desc.getConnectionDef();
   }

   /**
    * Updates the panel and refreshes.
    *
    * @param def the objectstore defintion of the selection that
    *    is desired. Never <code>null</code>.
    *
    * @param tree IPSContentTree that contains the data.
    *    Never <code>null</code>
    *
    * @throws IllegalArgumentException if <code>def</code> is
    *    <code>null</code>
    */
   public void update(PSLoaderDescriptor def, IPSContentTree tree) throws
      PSLoaderException
   {
      if (def == null)
         throw new IllegalArgumentException(
            "def must not be null");

      if (tree == null)
         throw new IllegalArgumentException(
            "tree must not be null");
      setDescriptorInfo(def);

      if (m_tree == null || m_model == null)
      {
         init(def, tree);
      }
      else
      {
         try
         {
            m_model = new PSUIContentTreeModel(def.getContentSelectorDef(),
               tree);
         }
         catch (Exception e)
         {
            throw new IllegalArgumentException(
               "def must be a valid PSContentSelectorDef");
         }

         m_tree.setModel(m_model);
      }
   }

   /**
    * Initializes all tree related components.
    *
    * @param def A PSLoaderDescriptor used to extractor
    *    all mime types that are defined per extractor.
    *    Never <code>null</code>
    */
   protected void initTree(PSLoaderDescriptor def)
   {
      if (def == null)
         throw new IllegalArgumentException(
            "def must not be null");

      m_tree = new JTree(m_model);
      m_tree.getSelectionModel().setSelectionMode(
         TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
      m_tree.setEditable(false);
      m_tree.setShowsRootHandles(true);
      m_tree.setExpandsSelectedPaths(true);
      m_tree.addTreeSelectionListener(this);
      m_tree.setCellRenderer(new ContentTreeRenderer());

      //Create the popup menu.
      setupPopupMenu((PSLoaderDescriptor) def, true);

      //Add listener to components that can bring up popup menus.
      MouseListener pop = new ContentPopupListener();
      m_tree.addMouseListener(pop);

      // Refresh listeners
      for (int i=0; i<m_listeners.size(); i++)
      {
         m_tree.addTreeSelectionListener(
            (TreeSelectionListener) m_listeners.get(i));
      }

      // Refresh listeners on model
      for (int i=0; i<m_modelListeners.size(); i++)
      {
         m_model.addTreeModelListener(
            (TreeModelListener) m_modelListeners.get(i));
      }
   }

   /**
    * Add a listener to <code>m_tree</code>.
    *
    * @param l Never <code>null</code>.
    */
   public void addContentTreeListener(TreeSelectionListener l)
   {
      if (l == null)
         throw new IllegalArgumentException(
            "l must not be null");

      if (!m_listeners.contains(l))
         m_listeners.add(l);
   }

   /**
    * Add a listener to <code>m_tree</code>.
    *
    * @param l Never <code>null</code>.
    */
   public void addContentTreeModelListener(TreeModelListener l)
   {
      if (l == null)
         throw new IllegalArgumentException(
            "l must not be null");

      if (!m_modelListeners.contains(l))
         m_modelListeners.add(l);
   }

   /**
    * Convenience method to retreive the selected item context
    * from the tree.
    *
    * @return IPSContentTreeNode may be <code>null</code>
    */
   public IPSContentTreeNode getSelectedNode()
   {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)
         m_tree.getLastSelectedPathComponent();

      if (node == null)
         return null;

      Object obj = node.getUserObject();

      if (obj instanceof IPSContentTreeNode)
      {
         return (IPSContentTreeNode) obj;
      }

      return null;
   }

   /**
    * Convenience method to retreive the trees selected nodes
    * as a list of item contexts.
    * @return list of {@link IPSContentTreeNode} objects. Never
    * <code>null</code>, may be empty.
    */
   public List getSelectedNodes()
   {
      Map selectionMap = new HashMap();
      TreePath[] selectedPaths = m_tree.getSelectionPaths();
      DefaultMutableTreeNode node = null;
      for(int i = 0; i < selectedPaths.length; i++)
      {
         node = (DefaultMutableTreeNode)selectedPaths[i].getLastPathComponent();
         addSelectedNode(node, selectionMap);
      }
      return new ArrayList(selectionMap.values());

   }

   /**
    * Adds the node to the passed in list if it is a tree leaf. If it is a
    * folder than it recurses into it to automatically select each node.
    * @param node the node to be added. May be <code>null</code>.
    * @param map holding selected <code>IPSContentTreeNode<code> objects and
    * using the <code>PSItemContext</code> resource id as the keys.
    * May not be <code>null</code>.
    */
   private void addSelectedNode(DefaultMutableTreeNode node, Map map)
   {

      if(null == node)
         return;
      if(null == map)
         throw new IllegalArgumentException("Map cannot be null.");

      // If this is a folder then we are selecting all child nodes
      // below this folder
      if(!node.isLeaf())
      {
         Enumeration children = node.children();
         while(children.hasMoreElements())
         {
            node = (DefaultMutableTreeNode)children.nextElement();
            addSelectedNode(node, map);
         }
      }
      else
      {
         Object obj = node.getUserObject();
         if(obj instanceof IPSContentTreeNode)
         {
            PSItemContext item = ((IPSContentTreeNode)obj).getItemContext();
            map.put(item.getResourceId(), obj);
         }
      }

   }

   /**
    * see {@link #TreeSelectionListener} for description.
    */
   public void valueChanged(TreeSelectionEvent e)
   {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)
         m_tree.getLastSelectedPathComponent();

      if (node == null)
         return;

   }

   /**
    * Method to 'select' visually the node that contains the
    * following <code>item</code>.
    *
    * @param item PSItemContext. Never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>item</code> is
    *    invalid.
    */
   public void highlightNode(PSItemContext item)
   {
      if (item == null)
         throw new IllegalArgumentException(
            "item must not be null");

      // Threshold
      if (m_model == null)
         return;

      DefaultMutableTreeNode node = m_model.getTreeNodeFromItem(item);

      if (node != null)
         m_tree.setSelectionPath(new TreePath(node.getPath()));
      else
         clearTreeSelection();
   }

   /**
    * Method to 'clear' visually the current tree selection
    */
   public void clearTreeSelection()
   {
      m_tree.clearSelection();
   }

   /**
    * Sets the model of data displayed by this tree.
    *
    * @param def the objectstore defintion of the selection that
    *    is desired. Never <code>null</code>.
    *
    * @param tree IPSContentTree that contains the data.
    *    Never <code>null</code>
    *
    * @throws IllegalArgumentException if any invalid parameters.
    */
   public void refreshModel(PSContentSelectorDef def, IPSContentTree tree)
   {
      if (def == null)
         throw new IllegalArgumentException(
            "def must not be null");

      if (tree == null)
         throw new IllegalArgumentException(
            "tree must not be null");

      try
      {
         // Refresh the model
         m_model = new PSUIContentTreeModel(def, tree);
      }
      catch (Exception e)
      {
         throw new IllegalArgumentException(
            "def must be a valid PSContentSelectorDef");
      }

      // Update the tree
       m_tree.setModel(m_model);
   }

   // implementation for ActionListener
   public void actionPerformed(ActionEvent e)
   {
      String strCommand = e.getActionCommand();

      if (strCommand.equalsIgnoreCase(
         ms_res.getString("menu.text.exclude")))
      {
         onExclude();
      }
      else if (strCommand.equalsIgnoreCase(
         ms_res.getString("menu.text.upload")))
      {
         onUpload();
      }
      else if (strCommand.equalsIgnoreCase(
         ms_res.getString("menu.text.preview")))
      {
         onPreview();
      }
      else if (strCommand.equalsIgnoreCase(
          ms_res.getString("menu.text.reload")))
      {
         onReload();
      }
       else if (strCommand.equalsIgnoreCase(
          ms_res.getString("menu.text.reloadNew")))
      {
         onReloadNewItem();
      }
      else if (strCommand.indexOf(ASSIGN_CT) >= 0)
      {
         onAssignExtractor(strCommand);
      }
   }

   /**
    * Action cmd for assign type or extractor.
    *
    * @param strCmd the command string. Assume it is not <code>null</code> or
    *    empty, and is prefixed with "ASSIGN_CT;"
    */
   private void onAssignExtractor(String strCmd)
   {
      if (strCmd == null || strCmd.trim().length() == 0)
         throw new IllegalArgumentException(
            "strCmd must not be null");
      if (m_selector == null || m_connDef == null)
         throw new IllegalStateException(
            "m_selector or m_connDef can not be null");

      // Parse out the new extractor name
      int nIndex = strCmd.indexOf(';');
      String newName = strCmd.substring(nIndex + 1, strCmd.length());

      PSExtractorDef def =
         PSLoaderUtils.getExtractorDefFromName(
         PSMainFrame.getFrameNoAction().getDescriptor(), newName);

      PSPluginFactory factory = PSPluginFactory.getInstance();
      IPSItemExtractor extractor = null;

      try
      {
         extractor = factory.newItemExtractor(def);
      }
      catch (PSLoaderException e)
      {
         String errorMsg = MessageFormat.format(
            ms_res.getString("error.msg.validateExtractorException"),
            new Object[] {e.getLocalizedMessage()});

         ErrorDialogs.showErrorDialog(this, errorMsg,
            ms_res.getString("error.title.validateExtractor"),
            JOptionPane.ERROR_MESSAGE);
         return;
      }

      Iterator selections = getSelectedNodes().iterator();
      boolean isRecognized = false;
      boolean hasErrors = false;
      boolean isErrorMessage = def.isStaticType();
      // Iterate through all selected items and attempt to assign the
      // extractor
      while(selections.hasNext())
      {
         IPSContentTreeNode node = (IPSContentTreeNode)selections.next();
         PSItemContext item = node.getItemContext();
         isRecognized = true;

         // No need to change items that already have this extractor defined
         if (!item.getStatus().equals(PSItemContext.STATUS_EXCLUDED)
            && item.getExtractorDef().equals(def))
            continue;

         if (extractor.containsInstances(item) < 1)
         {
            isRecognized = false;
            hasErrors = true;
         }
         // Set the extractor if this is either a non static extractor
         // or a static extractor that recognizes the selector item
         if((def.isStaticType() && isRecognized) || !def.isStaticType())
         {
            item.setStatus(PSItemContext.STATUS_NEW);
            item.setExtractorDef(def);
         }
         if(!isRecognized && def.isStaticType())
         {
            // Log error message for non recognized items for a static
            // extractor
            String[] args = {newName, item.getResourceId()};
            PSLogMessage   msg = new PSLogMessage(
               IPSLogCodes.ERROR_ASSIGN_EXTRACTOR,
               args, item, PSLogMessage.LEVEL_ERROR);
            Logger.getLogger(this.getClass()).error(msg);
         }
         else if(!isRecognized)
         {
            // Log warning message for non recognized items for a non static
            // extractor
            String[] args = {newName, item.getResourceId()};
            PSLogMessage msg = new PSLogMessage(
               IPSLogCodes.WARNING_ASSIGN_EXTRACTOR,
               args, item, PSLogMessage.LEVEL_WARN);
            Logger.getLogger(this.getClass()).error(msg);
         }

      }

      // Update tree
      updateSelectedNode();

      // Display message telling user there was warnings or errors
      if(hasErrors)
      {
         String errorMsg = null;
         String errorTitle = null;
         int msgType = 0;

         if(isErrorMessage)
         {
             errorMsg =  ms_res.getString("error.msg.errorsDuringAssign");
             errorTitle = ms_res.getString("error.title.error.assignType");
             msgType = JOptionPane.ERROR_MESSAGE;
         }
         else
         {
             errorMsg =  ms_res.getString("error.msg.warningsDuringAssign");
             errorTitle = ms_res.getString("error.title.warn.assignType");
             msgType = JOptionPane.WARNING_MESSAGE;
         }

         ErrorDialogs.showErrorDialog(this, errorMsg, errorTitle, msgType);

      }


   }

   /**
    * Action cmd for preview.
    */
   private void onPreview()
   {
      // @todo do we need the node object for preview ???
      IPSContentTreeNode n = getSelectedNode();

      if (n == null)
         return;

      PSMainFrame.getFrameNoAction().actionPerformed(
         new ActionEvent(n, PREVIEW_ITEM_FROM_TREE,
         PREVIEW_ITEM_FROM_TREE_CMD));
   }

   /**
    * Action cmd for uploading.
    */
   private void onUpload()
   {
      m_parent.onUpload(PSContentLoaderMgr.UPLOAD_AS_NEEDED,
                        getSelectedNodes());
   }

   /**
    * Action cmd for reload
    */
   private void onReload()
   {
      m_parent.onUpload(PSContentLoaderMgr.UPLOAD_AS_MODIFIED,
                        getSelectedNodes());
   }

   /**
    * Action cmd for reload new Item
    */
   private void onReloadNewItem()
   {
      m_parent.onUpload(PSContentLoaderMgr.UPLOAD_AS_NEW,
                        getSelectedNodes());
   }

   /**
    * Action cmd for exclude.
    */
   private void onExclude()
   {
      Iterator selections = getSelectedNodes().iterator();

      while(selections.hasNext())
     {
         IPSContentTreeNode node = (IPSContentTreeNode)selections.next();
         node.getItemContext().setStatus(PSItemContext.STATUS_EXCLUDED);
     }

     updateSelectedNode();

   }

   /**
    * Updating and refresh currently selected node of the tree.
    */
   public void updateSelectedNode()
   {

      TreePath path = m_tree.getSelectionPath();
      IPSContentTreeNode n = getSelectedNode();

      // This will fire events to any listeners
      if (n != null && path != null)
         m_model.onSendStructureEvent(n, path);
   }


   // implementation for IPSStatusListener
   public void statusChanged(PSStatusEvent event)
   {
      switch (event.getProcessId())
      {
         case PSStatusEvent.PROCESS_SCANNING:
         case PSStatusEvent.PROCESS_FIXING_LINKS:
         case PSStatusEvent.PROCESS_LOADING_CONTENTS:
         case PSStatusEvent.PROCESS_MANAGER:
         case PSStatusEvent.PROCESS_MARKING_TREE:
         default:
           break;
      }
   }

   /**
    * Inner class renderer for the content tree.
    *
    * @todo this class will switch between icons for excluded,
    *    changed, unchanged, new node ...
    */
   class ContentTreeRenderer extends DefaultTreeCellRenderer
   {

      public ContentTreeRenderer()
      {
         super();

         m_changedNodeIcon = new ImageIcon(getClass().getResource(
            PSContentLoaderResources.getResourceString(ms_res,
            "node_changed_gif")));

         m_unchangedNodeIcon = new ImageIcon(getClass().getResource(
            PSContentLoaderResources.getResources().getString(
            "node_unchanged_gif")));

         m_excludedNodeIcon = new ImageIcon(getClass().getResource(
            PSContentLoaderResources.getResources().getString(
            "node_excluded_gif")));

         m_errorNodeIcon = new ImageIcon(getClass().getResource(
            PSContentLoaderResources.getResources().getString(
            "node_error_gif")));

         m_newNodeIcon = new ImageIcon(getClass().getResource(
            PSContentLoaderResources.getResources().getString(
            "node_new_gif")));
      }

      // see base class for information
      public Component getTreeCellRendererComponent(
         JTree tree,
         Object value,
         boolean sel,
         boolean expanded,
         boolean leaf,
         int row,
         boolean hasFocus)
      {

         super.getTreeCellRendererComponent(
            tree, value, sel,
            expanded, leaf, row,
            hasFocus);

         if (value instanceof DefaultMutableTreeNode)
         {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) value;

            if (n != null)
            {
               Object nodeInfo = n.getUserObject();

               if (nodeInfo instanceof IPSContentTreeNode)
               {
                  IPSContentTreeNode psn = (IPSContentTreeNode) nodeInfo;
                  PSItemContext ic = psn.getItemContext();

                  try
                  {
                     setText(PSLoaderUtils.getFile(ic.getResourceId()));

                     String strStatus = ic.getStatus();

                     if (strStatus.equalsIgnoreCase(
                        PSItemContext.STATUS_EXCLUDED))
                     {
                        setIcon(m_excludedNodeIcon);
                     }
                     if (strStatus.equalsIgnoreCase(
                        PSItemContext.STATUS_ERROR))
                     {
                        setIcon(m_errorNodeIcon);
                     }
                     else if (strStatus.equalsIgnoreCase(
                        PSItemContext.STATUS_MODIFIED))
                     {
                        setIcon(m_changedNodeIcon);
                     }
                     else if (strStatus.equalsIgnoreCase(
                        PSItemContext.STATUS_NEW))
                     {
                        setIcon(m_newNodeIcon);
                     }
                     else if (strStatus.equalsIgnoreCase(
                        PSItemContext.STATUS_UNCHANGED))
                     {
                        setIcon(m_unchangedNodeIcon);
                     }
                  }
                  catch (Exception ignore)
                  {}
               }
            }
         }
         return this;
      }

      /**
       * Icons used be this renderer for displaying the status
       * of a given PSItemContext. Initialized in ctor
       */
      private Icon m_newNodeIcon = null;
      private Icon m_changedNodeIcon = null;
      private Icon m_unchangedNodeIcon = null;
      private Icon m_excludedNodeIcon = null;
      private Icon m_errorNodeIcon = null;

   }

   /**
    * Popup menu for tree.
    */
   class ContentPopupListener extends MouseAdapter
   {
      public void mousePressed(MouseEvent e)
      {
         showPopup(e);
      }

      public void mouseReleased(MouseEvent e)
      {
         showPopup(e);
      }

      private void showPopup(MouseEvent e)
      {
         if (e.isPopupTrigger())
         {

            TreePath nPath = m_tree.getPathForLocation(e.getX(), e.getY());

            // Set the selection if we are right clicking on an item and
            // that item is not already selected. We also don't set the
            // selection if the control button is down.
            if(!arrayContains(m_tree.getSelectionPaths(), nPath)
               && !e.isControlDown())
            {
               m_tree.setSelectionPath(nPath);
            }

            setupPopupMenu(null, false);
            m_popupMenu.show(e.getComponent(), e.getX(), e.getY());

         }
      }

      /**
       * Determines if the specified object is contained in the passed in array.
       * @param array the array to search through, may be <code>null</code>.
       * @param obj the object to look for, may be <code>null</code>.
       * @return <code>true</code> if the array contains the object, else
       * <code>false</code>.
       */
      private boolean arrayContains(Object[] array, Object obj)
      {
         if( null == array || null == obj)
            return false;
         for(int i = 0; i < array.length; i++)
         {
            if(array[i] == obj)
              return true;
         }
         return false;
      }
   }

   /**
    * Customize the popup content tree menu based on the
    * nodes selected.
    *
    * @param def PSLoaderDescriptor. May be <code>null</code>
    *
    * @param init a flag that indicates if we are initializing
    * the popup for the first time.
    */
   private void setupPopupMenu(PSLoaderDescriptor def, boolean init)
   {
      m_popupMenu.removeAll();

      JMenuItem exItem = new JMenuItem(ms_res.getString("menu.text.exclude"));
      exItem.addActionListener(this);
      m_popupMenu.add(exItem);

      JMenuItem prevItem = new JMenuItem(ms_res.getString("menu.text.preview"));
      prevItem.addActionListener(this);
      m_popupMenu.add(prevItem);

      // load the content types
      if (def != null && init)
      {
         m_contentTypesMenu =
            new JMenu(ms_res.getString("menu.text.assignType"));

         Iterator r = PSLoaderUtils.getAvailableExtractorNames(def);
         int nCount = 0;

         while (r.hasNext())
         {
            String strContentType = (String) r.next();
            JMenuItem i = new JMenuItem(strContentType);
            i.setActionCommand(ASSIGN_CT + ";" + strContentType);
            i.addActionListener(this);
            m_contentTypesMenu.add(i);
            nCount++;
         }

         if (nCount == 0)
         {
            m_contentTypesMenu.setEnabled(false);
         }
      }

      m_popupMenu.add(m_contentTypesMenu);

      JMenuItem upItem = new JMenuItem(ms_res.getString("menu.text.upload"));
      upItem.addActionListener(this);
      m_popupMenu.add(upItem);

      JMenuItem reloadItem =
         new JMenuItem(ms_res.getString("menu.text.reload"));
      reloadItem.addActionListener(this);
      m_popupMenu.add(reloadItem);

      JMenuItem reloadNewItem =
         new JMenuItem(ms_res.getString("menu.text.reloadNew"));
      reloadNewItem.addActionListener(this);
      m_popupMenu.add(reloadNewItem);

      if(!init)
      {
         TreePath[] paths = m_tree.getSelectionPaths();
         // Check for selected folders
         boolean hasFolder = false;

         for(int i = 0; i < paths.length; i++)
         {
            if(!((DefaultMutableTreeNode)paths[i].getLastPathComponent()).isLeaf())
            {
               hasFolder = true;
               break;
            }
         }


         // Determine if we have selected multiple nodes or a folder
         boolean isMultiSelect = (m_tree.getSelectionCount() > 1 || hasFolder);

         PSItemContext item = null;
         // Try to get the item content if we don't have multiple selections
         if (!isMultiSelect)
         {
            DefaultMutableTreeNode node =
               (DefaultMutableTreeNode)m_tree.getSelectionPath().getLastPathComponent();
            Object userObj = node.getUserObject();
            if(null != userObj && userObj instanceof IPSContentTreeNode)
               item = ((IPSContentTreeNode)userObj).getItemContext();
         }

         // A directory (structural model only this is true)
         if (isMultiSelect)
         {
            prevItem.setEnabled(false);
         }

         if (null != item
            && item.getStatus().equalsIgnoreCase(PSItemContext.STATUS_EXCLUDED))
         {
            exItem.setVisible(false);
            prevItem.setEnabled(false);
            upItem.setEnabled(false);
            reloadItem.setEnabled(false);
            reloadNewItem.setEnabled(false);
         }
         else
         {
            m_contentTypesMenu.setEnabled(true);
         }
     }

   }

   /**
    * Connection information of the current descriptor. Initialized in
    * setDescriptorInfo, never <code>null</code> after that.
    */
   private PSConnectionDef m_connDef;

   /**
    * Content selector, initialized in setDescriptorInfo, never
    * <code>null</code> after that.
    */
   IPSContentSelector m_selector;

   /**
    * JTree for this panel. Never <code>null</code>.
    */
   private JTree m_tree = new JTree();

   /**
    * Popup menu for content tree. Never <code>null</code>
    */
   private JPopupMenu m_popupMenu = new JPopupMenu();

   /**
    * Reference to the parent frame. Initialized in the ctor. Never
    * <code>null</code> after that.
    */
   private PSMainFrame m_parent;

   /**
    * a <code>PSUIContentTreeModel</code> for <code>m_tree</code> model.
    * Initialized in constructor. Never <code>null</code>.
    * May be updated via <code>setupModel</code>
    */
   private PSUIContentTreeModel m_model;

   /**
    * List of tree listeners. Never <code>null</code> may be empty.
    */
   private ArrayList m_listeners = new ArrayList();

   /**
    * List of tree model listeners. Never <code>null</code> may be empty.
    */
   private ArrayList m_modelListeners = new ArrayList();

   /**
    * Resource bundle for this class. Initialised in the constructor.
    * It's not modified after that. May be <code>null</code> if it could not
    * load the resource properties file.
    */
   private static ResourceBundle ms_res = null;

   /**
    * Menu that is a list of assignable content types. Initialized in
    * <code>setupPopupMenu</code>. Never <code>null</code>
    */
   private JMenu m_contentTypesMenu = null;

   /**
    * Action constant name used for assigning content types.
    */
   public static final String ASSIGN_CT = "AssignContentTypeCmdPrefix";

   /**
    * The root node name constant.
    */
   public static final String ROOTNODE_STRING = "Servers";

   /**
    * Action constant name used for uploading one item.
    */
   public static final String UPLOAD_ITEM_FROM_TREE_CMD
      = "UploadItemFromTreeCmd";

   /**
    * Action constant name used for uploading all items.
    */
   public static final String UPLOAD_ALL_FROM_TREE_CMD
      = "UploadAllFromTreeCmd";

   /**
    * Action constant name used for previewing one item in the default browser.
    */
   public static final String PREVIEW_ITEM_FROM_TREE_CMD
      = "PreviewItemFromTreeCmd";

   /**
    * Action constant id used for previewing one item in the default browser.
    */
   public static final int PREVIEW_ITEM_FROM_TREE = 3001;

   /**
    * Action constant id used for uploading all items.
    */
   public static final int UPLOAD_ALL_FROM_TREE = 3002;

   /**
    * Action constant id used for uploading one item.
    */
   public static final int UPLOAD_ITEM_FROM_TREE = 3003;
}