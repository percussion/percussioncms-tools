/* *****************************************************************************
 *
 * [ PSDescriptorTreePanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.loader.IPSLogCodes;
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.PSLoaderRemoteAgent;
import com.percussion.loader.PSLogMessage;
import com.percussion.loader.objectstore.PSConnectionDef;
import com.percussion.loader.objectstore.PSContentLoaderConfig;
import com.percussion.loader.objectstore.PSContentSelectorDef;
import com.percussion.loader.objectstore.PSErrorHandlingDef;
import com.percussion.loader.objectstore.PSFieldTransformationDef;
import com.percussion.loader.objectstore.PSFieldTransformationsDef;
import com.percussion.loader.objectstore.PSItemTransformationsDef;
import com.percussion.loader.objectstore.PSLoaderDef;
import com.percussion.loader.objectstore.PSLoaderDescriptor;
import com.percussion.loader.objectstore.PSLogDef;
import com.percussion.loader.objectstore.PSTransformationDef;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;

/**
 * Panel for displaying the descriptor tree. Each node in the tree represents
 * another panel for editing or selecting the data corresponding to that panel.
 */
public class PSDescriptorTreePanel extends JPanel implements ActionListener,
   ListSelectionListener
{
   /**
    * Creates a descriptor tree.
    *
    * @param config configuration object from which data objects are retrieved,
    *    may not be <code>null</code>.
    * @throws IllegalArgumentException if the parameters are invalid.
    */
   public PSDescriptorTreePanel(PSContentLoaderConfig config)
   {
      this(config, null);
   }

   /**
    * Creates a descriptor tree.
    *
    * @param config configuration object from which data objects are retrieved,
    *    may not be <code>null</code>.
    * @loaderDesc descriptor object from which data objects are retrieved,
    *    may be <code>null</code>.
    * @throws IllegalArgumentException if the parameters are invalid.
    */
   public PSDescriptorTreePanel(PSContentLoaderConfig config,
      PSLoaderDescriptor loaderDesc)
   {
      if (config == null)
         throw new IllegalArgumentException("Loader config cannot be null");

      m_config = config;
      m_loaderDesc = loaderDesc;
      m_edit = (m_loaderDesc != null);
      if (loaderDesc != null)
         init(false);
      else
         init(true);
   }

   /**
    * Initializes this panel.
    *
    * @param isNewDescriptor <code>true</code> if editing a new descriptor,
    *    then the name of the descriptor can be modified; otherwise, the
    *    name of the descriptor cannot be modified.
    */
   private void init(boolean isNewDescriptor)
   {
      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(
            getClass().getName() + "Resources", Locale.getDefault());

      initConstants();
      setLayout(new BorderLayout());
      setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

      PSMainDescriptorPanel root = new PSMainDescriptorPanel(isNewDescriptor);
      if (m_edit)
         root.load(m_loaderDesc.getConnectionDef().toXml(
         PSXmlDocumentBuilder.createXmlDocument()));
      else
         root.load(m_config.getConnectionDef().toXml(
         PSXmlDocumentBuilder.createXmlDocument()));
      if (m_edit)
      {
         String path =  m_loaderDesc.getPath();
         path = path.substring(0, path.lastIndexOf(File.separator));
         root.setPath(path);
         root.setDescriptorName(m_loaderDesc.getName());
      }
      PSTreeObject rootUserObj = new PSTreeObject(root.getPath(), root);
      rootUserObj.setHelpId(HID_MAIN);
      // read the default path.
      DefaultMutableTreeNode top = new
         DefaultMutableTreeNode(rootUserObj);
      createNodes(top);
      m_tree = new JTree(top);
      MouseListener mouseListener = new MouseAdapter()
      {
         /**
          * Populates <code>m_selectedNode</code> with the currently selected
          * node in the <code>m_tree</code> and <code>m_singleSelPane</code> if
          * the selected tree node has {@link #PSSingleSelectionEditorPanel} as
          * the ui object.
          *
          * @param e never <code>null</code>.
          */
         public void mousePressed(MouseEvent e)
         {
            int selRow = m_tree.getRowForLocation(e.getX(), e.getY());
            if (selRow != -1)
            {
               TreePath selPath = m_tree.getPathForLocation(e.getX(), e.getY());
               m_selectedNode =
                  (DefaultMutableTreeNode)selPath.getLastPathComponent();
               PSTreeObject usr = (PSTreeObject)m_selectedNode.getUserObject();
               Object obj = usr.getUIObj();
               if (obj instanceof PSSingleSelectionEditorPanel)
               {
                  m_singleSelPane = (PSSingleSelectionEditorPanel)obj;
               }
            }
         }
      };
      m_tree.addMouseListener(mouseListener);
      m_tree.getSelectionModel().setSelectionMode(
         TreeSelectionModel.SINGLE_TREE_SELECTION);
      m_tree.setEditable(false);
      m_tree.setSelectionRow(0);
      JScrollPane scrollPane = new JScrollPane(m_tree);
      add(scrollPane, BorderLayout.CENTER);
   }

   /**
    * Initializes all the constants used in this panel.
    */
   private void initConstants()
   {
      CONTENT_SELECTOR = PSContentLoaderResources.getResourceString(ms_res,
         "node.contentselector");
      EXTRACTORS = PSContentLoaderResources.getResourceString(ms_res,
         "node.extractors");
      CONTENT_LOADER = PSContentLoaderResources.getResourceString(ms_res,
         "node.contentloader");
      GFT = PSContentLoaderResources.getResourceString(ms_res,
         "node.globalfieldTransf");
      GIT = PSContentLoaderResources.getResourceString(ms_res,
         "node.globalitemTransf");
      LOG = PSContentLoaderResources.getResourceString(ms_res, "node.log");
      ERR = PSContentLoaderResources.getResourceString(ms_res, "node.error");
      STATIC_ITEMS = PSContentLoaderResources.getResourceString(ms_res,
         "node.staticitems");
      ITEMS = PSContentLoaderResources.getResourceString(ms_res, "node.items");
      APPLY = PSContentLoaderResources.getResourceString(ms_res,
         "action.apply");
      ADD = PSContentLoaderResources.getResourceString(ms_res, "action.add");
      REMOVE = PSContentLoaderResources.getResourceString(ms_res,
         "action.remove");
      ITM_TRANS= PSContentLoaderResources.getResourceString(ms_res,
         "node.itemtransf");
      FLD_TRANS = PSContentLoaderResources.getResourceString(ms_res,
         "node.fieldtransf");
      ACTIONS = PSContentLoaderResources.getResourceString(ms_res,
         "node.actions");
   }

   /**
    * Adds a listener for TreeSelection events
    *
    * @param l the TreeSelectionListener that will be notified when a node is
    * selected or deselected. Assumed to be not <code>null</code>.
    */
   public void addTreeSelectionListener(TreeSelectionListener l)
   {
      m_tree.addTreeSelectionListener(l);
   }

   /**
    * Setup the loader nodes for the descriptor tree.
    *
    * @param rootNode The root node of the descriptor tree, assume not
    *    <code>null</code>.
    *
    * @throws PSLoaderException if an error occurs
    */
   private void setupLoaderNodes(DefaultMutableTreeNode rootNode)
      throws PSLoaderException
   {
      if (! ms_hasCachedData)
      {
         PSSingleSelectionEditorPanel ssLoaderPanel =
            new PSSingleSelectionEditorPanel();
         PSTreeObject userObj = new PSTreeObject(CONTENT_LOADER, ssLoaderPanel);
         userObj.setHelpId(HID_CONTENTLOADER);

         ssLoaderPanel.setContentLoaders(m_config.getLoaderDefs());

         ms_cachedData.m_loaderNode = new FolderNode(userObj);
         ms_cachedData.m_loaderPanel = ssLoaderPanel;

      }

      ms_cachedData.m_loaderPanel.removeAllListeners();
      ms_cachedData.m_loaderPanel.addActionListener(this);
      ms_cachedData.m_loaderPanel.addListSelectionListener(this);
      ms_cachedData.m_loaderNode.removeAllChildren();
      rootNode.add(ms_cachedData.m_loaderNode);

      if (m_edit)
      {
         PSLoaderDef def = m_loaderDesc.getLoaderDef();
         PSTreeObject obj = ms_cachedData.m_loaderPanel.setContentLoader(def);
         ms_cachedData.m_loaderNode.add(new DefaultMutableTreeNode(obj));
      }
   }

   /**
    * Setup the selector nodes for the descriptor tree.
    *
    * @param rootNode The root node of the descriptor tree, assume not
    *    <code>null</code>.
    *
    * @throws PSLoaderException if an error occurs
    */
   private void setupSelectorNodes(DefaultMutableTreeNode rootNode)
      throws PSLoaderException
   {
      if (! ms_hasCachedData)
      {
         PSSingleSelectionEditorPanel singleSelecEdPanel =
               new PSSingleSelectionEditorPanel();
         PSTreeObject userObj = new PSTreeObject(CONTENT_SELECTOR,
               singleSelecEdPanel);
          userObj.setHelpId(HID_CONTENTSELECTOR);
         singleSelecEdPanel.setSelectors(m_config.getSelectorDefs());

         ms_cachedData.m_selectorNode = new FolderNode(userObj);
         ms_cachedData.m_selectorPanel = singleSelecEdPanel;
      }

      ms_cachedData.m_selectorPanel.removeAllListeners();
      ms_cachedData.m_selectorPanel.addActionListener(this);
      ms_cachedData.m_selectorPanel.addListSelectionListener(this);
      ms_cachedData.m_selectorNode.removeAllChildren();
      rootNode.add(ms_cachedData.m_selectorNode);

      if (m_edit)
      {
         PSContentSelectorDef def = m_loaderDesc.getContentSelectorDef();
         PSTreeObject obj = ms_cachedData.m_selectorPanel.setSelector(def);

         ms_cachedData.m_selectorNode.add(new DefaultMutableTreeNode(obj));
      }
   }

   /**
    * Setup the error handling node for the descriptor tree.
    *
    * @param rootNode The root node of the descriptor tree, assume not
    *    <code>null</code>.
    *
    * @throws PSLoaderException if other error occurs.
    */
   private void setupErrorHandlingNode(DefaultMutableTreeNode rootNode)
      throws PSLoaderException
   {
      PSErrorHandlingEditorPanel errPane = new PSErrorHandlingEditorPanel();
      PSTreeObject userObj = new PSTreeObject(ERR, errPane);
      userObj.setHelpId(HID_ERR);
      FolderNode errNode = new FolderNode(userObj);
      PSErrorHandlingDef errdef = null;
      if (m_edit)
         errdef = m_loaderDesc.getErrorHandlingDef();
      else
         errdef = m_config.getErrorHandlingDef();

      errPane.load(errdef.toXml(PSXmlDocumentBuilder.createXmlDocument()));
      rootNode.add(errNode);
   }

   /**
    * Setup the global field transformers for the descriptor tree.
    *
    * @param rootNode The root node of the descriptor tree, assume not
    *    <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException if one of the definition contains
    *    unexpected XML format.
    *
    * @throws PSLoaderException if other error occurs.
    */
   private void setupGlobalFieldTransNodes(DefaultMutableTreeNode rootNode)
      throws PSLoaderException
   {
      PSFieldTransformationsDef cfgFtd = m_config.getFieldTransDef();
      PSFieldTransformationsDef descFtd = null;

      if (m_edit)
         descFtd = m_loaderDesc.getFieldTransDef();

      if (cfgFtd != null)
      {
         Iterator cfgItr = cfgFtd.getTransformations();
         if (cfgItr.hasNext() || (descFtd != null &&
            descFtd.getTransformations().hasNext()))
         {
            PSMultiSelectionEditorPanel gftPane = new
               PSMultiSelectionEditorPanel();
            gftPane.setActionListener(this);
            gftPane.setPreferredSize(new Dimension(300, 400));
            PSTreeObject userObj = new PSTreeObject(GFT, gftPane);
            FolderNode gftNode = new FolderNode(userObj);
            rootNode.add(gftNode);
            List descTransLst = null;
            List cfgTransLst =
               cfgFtd.getTransformationsList().getComponentList();
            if (m_edit)
            {
               if (descFtd != null)
               {
                  descTransLst =
                        descFtd.getTransformationsList().getComponentList();
                  gftPane.setTransformers(descTransLst.listIterator(),
                        false, m_edit);
               }
                gftPane.setTransformers(cfgTransLst.listIterator(),
                   false, !m_edit);
                DefaultListModel usedModel = (DefaultListModel)
                   gftPane.getEditedListModel();
                int len = usedModel.size();
                DefaultMutableTreeNode mutNode = null;
                for (int z = 0; z < len; z++)
                {
                   mutNode = new DefaultMutableTreeNode(usedModel.get(z));
                   gftNode.add(mutNode);
                }
            }
            else
            {
               gftPane.setTransformers(cfgTransLst.listIterator(), false,
                  false);
            }
         }
      }
   }

   /**
    * Setup the global item transformers for the descriptor tree.
    *
    * @param rootNode The root node of the descriptor tree, assume not
    *    <code>null</code>.
    *
    * @throws PSLoaderException if other error occurs.
    */
   private void setupGlobalItemTransNodes(DefaultMutableTreeNode rootNode)
      throws PSLoaderException
   {
      PSItemTransformationsDef cfgItd = m_config.getItemTransDef();
      PSItemTransformationsDef descItd = null;
      if (m_edit)
         descItd = m_loaderDesc.getItemTransDef();
      if (cfgItd != null)
      {
         Iterator cfgItr = cfgItd.getTransformations();
         if (cfgItr.hasNext() || (descItd != null &&
            descItd.getTransformations().hasNext()))
         {
            PSMultiSelectionEditorPanel gitPane = new
                  PSMultiSelectionEditorPanel();
            gitPane.setActionListener(this);
            gitPane.setPreferredSize(new Dimension(300, 400));
            PSTreeObject userObj = new PSTreeObject(GIT, gitPane);
            FolderNode gitNode = new FolderNode(userObj);
            rootNode.add(gitNode);
            List descTransLst = null;
            List cfgTransLst =
                  cfgItd.getTransformationsList().getComponentList();
            if (m_edit)
            {
               if (descItd != null)
               {
                  descTransLst =
                        descItd.getTransformationsList().getComponentList();
                  gitPane.setTransformers(descTransLst.listIterator(),
                        true, m_edit);
               }
               gitPane.setTransformers(cfgTransLst.listIterator(),
                  true, !m_edit);
               DefaultListModel usedModel = (DefaultListModel)
                  gitPane.getEditedListModel();
               int len = usedModel.size();
               DefaultMutableTreeNode mutNode = null;
               for (int z = 0; z < len; z++)
               {
                  mutNode = new DefaultMutableTreeNode(usedModel.get(z));
                  gitNode.add(mutNode);
               }
            }
            else
            {
               gitPane.setTransformers(cfgTransLst.listIterator(), true,
                     false);
            }
         }
      }
   }

   /**
    * Setup the log node for the descriptor tree.
    *
    * @param rootNode The root node of the descriptor tree, assume not
    *    <code>null</code>.
    *
    * @throws PSLoaderException if other error occurs.
    */
   private void setupLogNode(DefaultMutableTreeNode rootNode)
      throws PSLoaderException
   {
      PSLogEditorPanel logPane = new PSLogEditorPanel();
      PSTreeObject userObj = new PSTreeObject(LOG, logPane);
      userObj.setHelpId(HID_LOGGING);
      FolderNode logNode = new FolderNode(userObj);
      PSLogDef logDef = null;

      if (m_edit)
         logDef = m_loaderDesc.getLogDef();
      else
         logDef = m_config.getLogDef();

      logPane.load(logDef.toXml(PSXmlDocumentBuilder.createXmlDocument()));
      logPane.setPreferredSize(new Dimension(300, 400));
      rootNode.add(logNode);
   }

   /**
    * Set the nodes for both static and item extractors.
    *
    * @param rootNode The root node of the descriptor, assume not
    *    <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException if one of the definition contains
    *    unexpected XML format.
    *
    * @throws PSLoaderException if other error occurs.
    */
   private void setupExtractorNodes(DefaultMutableTreeNode rootNode)
      throws PSLoaderException,
             PSUnknownNodeTypeException
   {
      if (! ms_hasCachedData)
      {
         PSConfigPanel emptyPane = new PSConfigPanel();
         PSTreeObject userObj = new PSTreeObject(EXTRACTORS, emptyPane);
         FolderNode extractorNode = new FolderNode(userObj);
         ms_cachedData.m_extractorNode = extractorNode;
      }
      rootNode.add(ms_cachedData.m_extractorNode);

      setupStaticExtractorNodes(rootNode);
      setupItemExtractorNodes(rootNode);
   }

   /**
    * Setup the nodes for static extractors.
    *
    * @param rootNode The root node of the descriptor, assume not
    *    <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException if one of the definition contains
    *    unexpected XML format.
    *
    * @throws PSLoaderException if other error occurs.
    */
   private void setupStaticExtractorNodes(DefaultMutableTreeNode rootNode)
      throws PSLoaderException,
             PSUnknownNodeTypeException
   {
      if (! ms_hasCachedData)
      {
         //set up static extractor panel
         PSMultiSelectionEditorPanel multiExtSelPane =
            new PSMultiSelectionEditorPanel();
         multiExtSelPane.setActionListener(this);
         multiExtSelPane.setPreferredSize(new Dimension(300, 400));
         PSTreeObject userObj = new PSTreeObject(STATIC_ITEMS, multiExtSelPane);
         userObj.setHelpId(this.HID_STATIC_EXTRACTOR);

         ms_cachedData.m_staticNode = new FolderNode(userObj);
         ms_cachedData.m_extractorNode.add(ms_cachedData.m_staticNode);
         ms_cachedData.m_staticSelPanel = multiExtSelPane;

         resetAvailableStaticExtractors();
      }
      else
      {
         ms_cachedData.m_staticSelPanel.setActionListener(this);
      }

      // set the static item extractors in its multi-select panel
      // and parent tree node from the descritor
      ms_cachedData.m_staticSelPanel.resetEditedList();
      ms_cachedData.m_staticNode.removeAllChildren();

      if (m_edit)
      {
         Iterator extExtractors = m_loaderDesc.getStaticExtractorDefs();
         ms_cachedData.m_staticSelPanel.setExtractors(extExtractors, true);

         DefaultListModel usedModel = (DefaultListModel)
            ms_cachedData.m_staticSelPanel.getEditedListModel();
         int len = usedModel.size();
         DefaultMutableTreeNode mutNode = null;
         for (int z = 0; z < len; z++)
         {
            mutNode = new DefaultMutableTreeNode(usedModel.get(z));
            ms_cachedData.m_staticNode.add(mutNode);
         }
      }
   }

   /**
    * Reset the cached data when the connection information has been
    * changed. This method can only be called after the dialog box has been
    * fully initialized.
    *
    * @throws PSLoaderException if an error occurs.
    */
   void resetAvailableDefinitions()
      throws PSLoaderException
   {
      resetAvailableStaticExtractors();
      resetAvailableItemExtractors();
   }

   /**
    * Reset the available item extractors from the current configuration
    *
    * @throws PSLoaderException if an error occurs.
    */
   private void resetAvailableItemExtractors()
      throws PSLoaderException
   {
      if (ms_cachedData == null || ms_cachedData.m_itemSelPanel == null)
         throw new IllegalStateException(
            "ms_cacheData and ms_cachedData.m_itemSelPanel must not be null");

      Iterator cfgExtractors = m_config.getItemExtractorDefs();
      try
      {
         ms_cachedData.m_itemSelPanel.setExtractors(cfgExtractors, false);
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSLoaderException(e);
      }
   }

   /**
    * Reset the available static extractors from the current configuration
    *
    * @throws PSLoaderException if an error occurs.
    */
   private void resetAvailableStaticExtractors()
      throws PSLoaderException
   {
      if (ms_cachedData == null || ms_cachedData.m_staticSelPanel == null)
         throw new IllegalStateException(
            "ms_cacheData and ms_cachedData.m_staticSelPanel must not be null");

      Iterator cfgExtractors = m_config.getStaticExtractorDefs();
      try
      {
         ms_cachedData.m_staticSelPanel.setExtractors(cfgExtractors, false);
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSLoaderException(e);
      }
   }

   /**
    * Setup the nodes for item extractors.
    *
    * @param rootNode The root node of the descriptor, assume not
    *    <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException if one of the definition contains
    *    unexpected XML format.
    *
    * @throws PSLoaderException if other error occurs.
    */
   private void setupItemExtractorNodes(DefaultMutableTreeNode rootNode)
      throws PSLoaderException,
             PSUnknownNodeTypeException
   {
      if (! ms_hasCachedData)
      {
         //set up item extractor panel
         PSMultiSelectionEditorPanel multiItemSelPane = new
               PSMultiSelectionEditorPanel();
         multiItemSelPane.setActionListener(this);
         multiItemSelPane.setPreferredSize(new Dimension(300, 400));
         PSTreeObject userObj = new PSTreeObject(ITEMS, multiItemSelPane);
         userObj.setHelpId(HID_ITEMEXTRACTOR);

         FolderNode itemNode = new FolderNode(userObj);
         ms_cachedData.m_extractorNode.add(itemNode);
         ms_cachedData.m_itemNode = itemNode;

         ms_cachedData.m_itemSelPanel = multiItemSelPane;

         resetAvailableItemExtractors();
      }
      else
      {
         ms_cachedData.m_itemSelPanel.setActionListener(this);
      }

      // set the item extractors in its multi-select panel
      // and parent tree node from the descritor
      ms_cachedData.m_itemSelPanel.resetEditedList();
      ms_cachedData.m_itemNode.removeAllChildren();

      if (m_edit)
      {
         Iterator extractors = m_loaderDesc.getItemExtractorDefs();
         ms_cachedData.m_itemSelPanel.setExtractors(extractors, true);

         DefaultListModel editedModel = (DefaultListModel)
            ms_cachedData.m_itemSelPanel.getEditedListModel();
         insertNodes(ms_cachedData.m_itemNode, editedModel, true);
      }
   }

   /**
    * Reset the connection info for the extractor config context. The to be
    * set connection info is either from the descriptor (if it is not
    * <code>null</code>) or from the configuration.
    *
    * @return <code>true</code> if the supplied connection info will be used
    *    as the new connection info by the extractor config context;
    *    <code>false</code> if the existing connection info (that is used by
    *    extractor config context) is the same as the supplied one.
    *
    * @throws PSLoaderException if an error occurs
    */
   private boolean resetConnectionInfo()
      throws PSLoaderException
   {
      PSConnectionDef conn = null;
      if (m_loaderDesc != null)
         conn = m_loaderDesc.getConnectionDef();
      else
         conn = m_config.getConnectionDef();

      return resetConnectionInfo(conn);
   }

   /**
    * Reset the connection info for the extractor config context.
    *
    * @param conn The connection definition that will be used for the
    *    configuration context, assume not <code>null</code>.
    *
    * @return <code>true</code> if the supplied connection info will be used
    *    as the new connection info by the extractor config context;
    *    <code>false</code> if the existing connection info that is used by
    *    extractor config context is the same as the <code>conn</code>.
    *
    * @throws PSLoaderException if an error occurs
    */
   boolean resetConnectionInfo(PSConnectionDef conn)
      throws PSLoaderException
   {
      PSLoaderRemoteAgent remoteAgent = 
         new PSLoaderRemoteAgent(conn);
      PSExtractorConfigContext ctx = PSExtractorConfigContext.getInstance();

      return ctx.setRemoteAgent(remoteAgent);
   }

   /**
    * Creates the tree adding all the nodes to it.
    *
    * @param rootNode, root node of the tree, assumed to be not <code>null
    * </code>
    */
   private void createNodes(DefaultMutableTreeNode rootNode)
   {
      try
      {
         if (! ms_hasCachedData)
         {
            ms_cachedData = new CachedData();
            resetConnectionInfo();
         }
         else
         {
            if (resetConnectionInfo())
               resetAvailableDefinitions();
         }

         setupLoaderNodes(rootNode);
         setupSelectorNodes(rootNode);
         setupErrorHandlingNode(rootNode);
         setupExtractorNodes(rootNode);
         setupGlobalFieldTransNodes(rootNode);
         setupGlobalItemTransNodes(rootNode);
         setupLogNode(rootNode);

         ms_hasCachedData = true;
      }
      catch(PSLoaderException e)
      {
         ErrorDialogs.showErrorDialog(this,
         PSContentLoaderResources.getResourceString(ms_res, e.getMessage()),
         PSContentLoaderResources.getResourceString(ms_res,
         "err.title.loaderexception"), JOptionPane.ERROR_MESSAGE);
         return;
      }
      catch(PSUnknownNodeTypeException f)
      {
         ErrorDialogs.showErrorDialog(this,
         PSContentLoaderResources.getResourceString(
         ms_res, f.getMessage()),
         PSContentLoaderResources.getResourceString(
         ms_res, "error.title.unknownnode"),
         JOptionPane.ERROR_MESSAGE);
         return;
      }
   }

   /**
    * Populates the available list of item or field transformations within the
    * item extractors which have been selected i.e are in the used list.
    *
    * @param transList, List of item {@link PSTransformationDef}or field
    * transformers {@link PSFieldTransformationDef} based on <code>isItem</code>
    * , may not be <code>null</code>, may be empty.
    *
    * @param obj, user object attached to the 'Field Transformers' or 'Item
    * Transformers' node of the corresponding extractor.
    *
    * @param isItem, if <code>true</code> it's item transformer or else field
    * transformer.
    *
    * @throws PSLoaderException, thrown if there is an exception associated with
    * loader application,
    */
   private void putTransInAvailableList(List transList, PSTreeObject
     obj, boolean isItem) throws PSLoaderException
   {
      PSMultiSelectionEditorPanel pane = (PSMultiSelectionEditorPanel)
         obj.getUIObj();
      DefaultListModel mdl = (DefaultListModel)pane.getEditedListModel();
      PSTransformationDef def = null;
      int len = mdl.getSize();
      for (int k = 0; k < len; k++)
      {
         PSTreeObject object = (PSTreeObject)mdl.get(k);
         Iterator itr = transList.listIterator();
         while (itr.hasNext())
         {
            if (isItem)
               def = (PSTransformationDef)itr.next();
            else
               def = (PSFieldTransformationDef)itr.next();
            if (def.getName().equals(object.toString()))
            {
               transList.remove(def);
               break;
            }
         }
      }
      if (isItem)
         pane.setTransformers(transList.listIterator(), true, false);
      else
         pane.setTransformers(transList.listIterator(), false, false);
   }

   /**
    * Gets a list  over an iteration.
    *
    * @param itr, an iteration of objects, may be <code>null</code> or empty.
    *
    * @return list of {@link java.lang.Object} objects. May be empty, never
    * <code>null</code>.
    */
   public static List getList(Iterator itr)
   {
      List list = new ArrayList();
      if (itr == null)
         return list;
      while (itr.hasNext())
         list.add(itr.next());
      return list;
   }

   /**
    * Implements {@link javax.swing.event.ListSelectionListener}. Called by
    * Swing based on the selection in a list in {@link
    *  PSSingleSlectionEditorPanel}.
    *
    * @param evt list selection event, never <code>null</code>. Its source
    *    must be <code>JList</code>.
    */
   public void valueChanged(ListSelectionEvent evt)
   {
      if (evt == null)
         throw new IllegalArgumentException("evt must not be null");
      if (! (evt.getSource() instanceof JList))
         throw new IllegalArgumentException("evt source must be JList");

      if (evt.getValueIsAdjusting())
         return;

      m_singleSelectionList = (JList)evt.getSource();
      PSTreeObject userObj =
         (PSTreeObject)m_singleSelectionList.getSelectedValue();
      if (userObj != null)
      {
         TreePath selectedPath = m_tree.getSelectionPath();
         if (selectedPath == null)
            return;
            
         DefaultMutableTreeNode node = (DefaultMutableTreeNode)
            selectedPath.getLastPathComponent();
         PSTreeObject obj = (PSTreeObject)node.getUserObject();
         if ((obj != null) && 
             (obj.getUIObj() instanceof PSSingleSelectionEditorPanel))
         {
            m_singleSelPane = (PSSingleSelectionEditorPanel)obj.getUIObj();
            m_selectedNode = node;
            m_singleSelPane.setApplyBtnState(true);
         }
      }
   }

   /**
    * Implementation for the ActionListener interface. Called by {@link PSMulti
    * SelectionEditorPanel} in the event 'Add', 'Remove', or 'Apply' buttons
    * are pressed.
    *
    * @param e, never <code>null</code>.
    */
   public void actionPerformed(ActionEvent actionEvent)
   {
      if (m_selectedNode == null)
         return;

      PSTreeObject treeObj = (PSTreeObject)m_selectedNode.getUserObject();
      String cmd = actionEvent.getActionCommand();
      DefaultListModel listModel = null;

      // add definition from multi selection panel
      if (cmd.equals(STATIC_ITEMS) || cmd.equals(ITEMS) || cmd.equals(
         FLD_TRANS)|| cmd.equals(ITM_TRANS))
      {
         Object obj = treeObj.getUIObj();
         if (obj instanceof PSMultiSelectionEditorPanel)
         {
            listModel = (DefaultListModel)((PSMultiSelectionEditorPanel)
               obj).getEditedListModel();
            insertNodes(m_selectedNode, listModel, false);
         }
      }

      // add definition from single selection panel
      if (cmd.equals(CONTENT_SELECTOR) || cmd.equals(CONTENT_LOADER))
      {
         PSTreeObject selectedUserObj =
            (PSTreeObject)m_singleSelectionList.getSelectedValue();

         DefaultTreeModel defTreeModel = (DefaultTreeModel)m_tree.getModel();
         for (Enumeration e = m_selectedNode.children();
            e.hasMoreElements();)
         {
            DefaultMutableTreeNode childNode =
               (DefaultMutableTreeNode)e.nextElement();
            defTreeModel.removeNodeFromParent(childNode);
            PSTreeObject userObj = (PSTreeObject)childNode.getUserObject();
            try
            {
               userObj.getUIObj().reset();
            }
            catch (PSLoaderException ex)
            {
               PSLogMessage   msg = new PSLogMessage(
                  IPSLogCodes.FAILED_RESET_DEFINITION,
                  new Object[] {userObj.getName()});
               Logger.getLogger(getClass()).info(msg);
            }

            e = m_selectedNode.children();
         }
         DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(
            selectedUserObj);
         defTreeModel.insertNodeInto(newNode, m_selectedNode, 0);
         m_singleSelectionList.clearSelection();
         m_singleSelPane.setApplyBtnState(false);
      }

      expand(new TreePath(m_selectedNode.getPath()));
   }

   /**
    * Resets ui of attached to the parent node and all of it's children
    *
    * @param parentNode, assumed to be not <code>null</code>.
    */
   private void reset(DefaultMutableTreeNode parentNode)
   {
      DefaultTreeModel defTreeModel = (DefaultTreeModel)m_tree.getModel();
      for (Enumeration e = parentNode.children(); e.hasMoreElements();)
      {
         DefaultMutableTreeNode childNode =
                              (DefaultMutableTreeNode)e.nextElement();
         defTreeModel.removeNodeFromParent(childNode);
         PSTreeObject userObj = (PSTreeObject)childNode.getUserObject();
         try
         {
            userObj.getUIObj().reset();
         }
         catch (PSLoaderException le)
         {
            PSLogMessage   msg = new PSLogMessage(
               IPSLogCodes.FAILED_RESET_DEFINITION,
               new Object[] {userObj.getName()});
            Logger.getLogger(getClass()).info(msg);
         }
         reset(childNode);
         e = parentNode.children();
      }
   }

   /**
    * Adds nodes to the <code>parentNode</code> and subsequently to the new
    * nodes. The user object for all the nodes is {@link PSTreeObject}.
    *
    * @param parentNode, parent node assumed to be not <code>null</code>.
    *
    * @param mdl default list model providing user objects for creating nodes.
    *    Asseumed to be not <code>null</code>
    *
    * @param edit <code>true</code> if inserting the edited nodes;
    *    <code>false</code> if inserting the configured nodes.
    */
   private void insertNodes(DefaultMutableTreeNode parentNode,
      DefaultListModel mdl, boolean edit)
   {
      DefaultTreeModel defTreeModel = null;
      if (!edit)
      {
         defTreeModel = (DefaultTreeModel)m_tree.getModel();
         reset(parentNode);
      }
      int size = mdl.getSize();
      for (int k = 0; k < size; k++)
      {
         PSTreeObject usrObj = (PSTreeObject)mdl.get(k);
         DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(usrObj);
         if (edit)
            parentNode.add(newNode);
         else
            defTreeModel.insertNodeInto(newNode, parentNode, k);
         List childrenList = usrObj.getChildren();
         if (!childrenList.isEmpty())
         {
            int sz = childrenList.size();
            for (int z = 0; z < sz; z++)
            {
               PSTreeObject childUserObj = (PSTreeObject)childrenList.get(z);

               Object obj = childUserObj.getUIObj();
               if (obj instanceof PSMultiSelectionEditorPanel)
               {
                  PSMultiSelectionEditorPanel childMultiPane =
                        (PSMultiSelectionEditorPanel)obj;
                  DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(
                        childUserObj);
                  if (edit)
                     newNode.add(childNode);
                  else
                     defTreeModel.insertNodeInto(childNode, newNode, z);
                  DefaultListModel childDm =
                        (DefaultListModel)childMultiPane.getEditedListModel();
                  if (childDm != null)
                     insertNodes(childNode, childDm, edit);
               }
            }
         }
      }
   }

   /**
    * Expands all the nodes below the parent node specified.
    *
    * @param tree, containing all the nodes, assumed to be not <code>null</code>.
    * @param parent, a path from an array of nodes, uniquely identifying the
    * path from the root of the tree to a specific node, as returned by the
    * tree's data model.
    */
   public void expand(TreePath parent)
   {
      TreeNode node = (TreeNode)parent.getLastPathComponent();
      if (node.getChildCount() >= 0)
      {
         for (Enumeration e=node.children(); e.hasMoreElements(); )
         {
            TreeNode n = (TreeNode)e.nextElement();
            TreePath path = parent.pathByAddingChild(n);
            expand(path);
         }
      }
      m_tree.expandPath(parent);
   }

   /**
    * Find the path  that matches the specified sequence of names specified as
    * an array.
    *
    * @param tree, tree in which the path is being searched,
    * assumed to be not <code>null</code>
    *
    * @param names, an array String names representing node names, assumed to be
    * not <code>null</code>.
    *
    * @return, path to the node being searched, may be <code>null</code>.
    */
   public static TreePath findByName(JTree tree, String[] names)
   {
      TreeNode root = (TreeNode)tree.getModel().getRoot();
      return findNode(tree, new TreePath(root), names, 0, true);
   }


   /**
    * Find the path  that matches the specified sequence of names specified as
    * an array.
    *
    * @param rootNode root node of the tree being searched, assumed to be not
    * <code>null</code>.
    *
    * @param names, an array String names representing node names, assumed to be
    * not <code>null</code>.
    *
    * @return, path to the node being searched, may be <code>null</code>.
    */
   public static TreePath findByName(TreeNode rootNode, String[] names)
   {
      return findNode(null, new TreePath(rootNode), names, 0, true);
   }

   /**
   * Find the path  that matches the specified sequence of names specified as
   * an array.
   *
   * @param tree, tree in which the path is being searched,
   * may be <code>null</code>
   *
   * @param parent, node from where the search has to begin. Asssumed to be
   * not <code>null</code>.
   *
   * @param nodes, an array objects representing node names or tree nodes,
   * ssumed to be not <code>null</code>.
   *
   * @param depth, specifying the position from where to start the search.
   *
   * @param byName, if <code>true</code>, then the nodes are being searched
   * based on names else by comparing node objects.
   *
   * @return path to the node being searched, may be <code>null</code>.
   */
   private static TreePath findNode(JTree tree, TreePath parent,
      Object[] nodes, int depth, boolean byName)
   {
      TreeNode node = (TreeNode)parent.getLastPathComponent();
      Object o = node;
      // If by name, convert node to a string
      if (byName)
         o = o.toString();
      // If equal, go down the branch
      if (o.equals(nodes[depth]))
      {
         // If at end, return match
         if (depth == nodes.length-1)
            return parent;
         // Traverse children
         if (node.getChildCount() >= 0)
         {
            for (Enumeration e=node.children(); e.hasMoreElements();)
            {
               TreeNode n = (TreeNode)e.nextElement();
               TreePath path = parent.pathByAddingChild(n);
               TreePath result = findNode(tree, path, nodes, depth+1, byName);
               // Found a match
               if (result != null)
                  return result;
            }
         }
      }
      // No match at this branch
      return null;
   }

   /**
    * Class to represent immediate children nodes of root node  as folders even
    * if they are leaf.
    */
   private class FolderNode extends DefaultMutableTreeNode
   {
      /**
       * ctor for creating objects of this class.
       * @param obj, user object attached to this node, assumed to be
       * not <code>null</code>.
       *
       */
      public FolderNode(Object obj)
      {
         super(obj);
      }

      /**
       * Specifies that this node is not a leaf node.
       *
       * @return <code>false</code>
       */
      public boolean isLeaf()
      {
         return false;
      }
   }

   /**
    * If <code>true</code>  an existing descriptor has been loaded for editing.
    * Initialized in ctor.
    */
   private boolean m_edit;

   /**
    * Encapsulates a loader descriptor, it contains all necessary definitions
    * for each content loader operation. Used for loading an existing descriptor.
    * Intialized in {@link #save()}, never <code>null</code> after that,
    * modified everytime {@link #save()} is called.
    */
   private PSLoaderDescriptor m_loaderDesc;

   /**
    * Initialized and modified in {@link #valueChanged(ListSelectionEvent)},
    * whenever an item is selected from {@PSSingleSelectionEditorPanel}. May
    * be <code>null</code> if no items is selected in {@
    * PSSingleSelectionEditorPanel} otherwise not.
    */
   private JList m_singleSelectionList;

   /**
    * Initialized and modified in {@link #init()} in the {@link
    * java.awt.event.MouseListener} attached to <code>m_tree</code> when the
    * selected tree node has {@PSSingleSelectionEditorPanel} as it's ui object.
    * That is the case whenever 'Content Loader' or 'Content Selector' nodes
    * are selected in the tree. May be <code>null</code> if
    * {@PSSingleSelectionEditorPanel} is never selected otherwise not.
    */
   private PSSingleSelectionEditorPanel m_singleSelPane;

   /**
    * Returns the model for the tree.
    *
    * @return, never <code>null</code>.
    */
   public DefaultTreeModel getModel()
   {
      return (DefaultTreeModel)m_tree.getModel();
   }

   /**
    * Adds a listener for TreeWillExpand events.
    * @param l, a TreeWillExpandListener that will be notified when a tree node
    * will be expanded or collapsed (a "negative expansion"), never <code>null
    * </code>.
    */
   public void addTreeWillExpandListener(TreeWillExpandListener l)
   {
      m_tree.addTreeWillExpandListener(l);
   }

   /**
    * Selects the node identified by the specified path.
    *
    * @param path, tree path to be selected, assumed to be not <code>null</code>
    */
   public void setSelectionPath(TreePath path)
   {
      m_tree.setSelectionPath(path);
   }

   /**
    * Maintains a cache of all the user objects which get attached to the tree
    * {@link PSDescriptorTree#actionPerformed(ActionEvent)}. Contents of the map
    * are modified only when 'Add' or 'Remove' buttons are pressed in  {@link
    * PSMultiSelectionEditorPanel}. May be empty, never <code>null</code>.
    */
   private Map m_nodeMap = new HashMap();

   /**
    * Config object for the loader. Initialized in the ctor, never <code>null
    * </code> or modified after that.
    */
   private PSContentLoaderConfig m_config;

   /**
    * Descriptor tree appearing on the left side in the {@link
    * PSContentDescriptorDialog}, it's a non editable, dynamic tree where on
    * clicking a   node a panel is presented for editing or selecting values and
    * nodes might get added through {@link PSMultiSelectionEditorPanel} or
    * {@link PSSingleSelectionEditorPanel}. Initialized in the {@link #init()}
    * and modfied as nodes are added or removed in {@link PSDescriptorTree#
    * actionPerformed(ActionEvent)}. Never <code>null</code>.
    */
   private JTree m_tree;

   /**
    * Node currently selected in the tree. Initialized in the mouselistener
    * attached to <code>m_tree</code>. Never <code>null</code>. Root node is
    * selected by default.
    */
   private DefaultMutableTreeNode m_selectedNode;

   /**
    * Resource bundle for this class. Initialized in the constructor.
    * It's not modified after that. Never <code>null</code>.
    */
   private static ResourceBundle ms_res;

   /**
    * Name of Content Selector node in the tree <code>m_tree</code>. The value
    * is loaded in {@link #initConstants()}.
    */
   public static String CONTENT_SELECTOR;

   /**
    * Name of Content Extractor node in the tree <code>m_tree</code>. The value
    * is loaded in {@link #initConstants()}.
    */
   public static String EXTRACTORS;
   public static String CONTENT_LOADER;

   /**
    * Name of Global Field Transformer node in the tree <code>m_tree</code>.
    * The value is loaded in {@link #initConstants()}.
    */
   public static String GFT;

   /**
    * Name of Global Item Transformer node in the tree <code>m_tree</code>.
    * The value is loaded in {@link #initConstants()}.
    */
   public static String GIT;

   /**
    * Name of Log node in the tree <code>m_tree</code>. The value
    * is loaded in {@link #initConstants()}.
    */
   public static String LOG;

   /**
    * Name of Error node in the tree <code>m_tree</code>. The value
    * is loaded in {@link #initConstants()}.
    */
   public static String ERR;

   /**
    * Name of Field Transformer node in the tree <code>m_tree</code>. The value
    * is loaded in {@link #initConstants()}.
    */
   public static String FLD_TRANS;

   /**
    * Name of Item Transformer node in the tree <code>m_tree</code>. The value
    * is loaded in {@link #initConstants()}.
    */
   public static String ITM_TRANS;

   /**
    * Name of Static Item Extractors node in the tree <code>m_tree</code>.
    * The value is loaded in {@link #initConstants()}.
    */
   public static String STATIC_ITEMS;

   /**
    * Name of Items node in the tree <code>m_tree</code>. The value
    * is loaded in {@link #initConstants()}.
    */
   public static String ITEMS;

   /**
    * Name of Actions node in the tree <code>m_tree</code>. The value
    * is loaded in {@link #initConstants()}.
    */
   public static String ACTIONS;

   /**
    * Actions corresponding to 'Add' button in
    * {@link PSMultiSelectionEditorPanel}. The values are loaded in
    * {@link #initConstants()}
    */
   public static String ADD;

   /**
    * Actions corresponding to 'Remove' button in {@link
    * PSMultiSelectionEditorPanel}. The values are loaded in
    * {@link #initConstants()}
    */
   public static String REMOVE;

   /**
    * Actions corresponding to 'Apply' button in {@link
    * PSMultiSelectionEditorPanel}. The values are loaded in
    * {@link #initConstants()}
    */
   public static String APPLY;
   //help ids
   public static final String HID_MAIN = "mainHelp";
   public static final String HID_CONTENTLOADER = "contentLoaderHelp";
   public static final String HID_RHYTHMYXLOADER = "rhythmyxLoaderHelp";
   public static final String HID_PREVIEWLOADER = "previewLoaderHelp";
   public static final String HID_CONTENTSELECTOR = "contentSelectorHelp";
   public static final String HID_FILESELECTOR = "fileSelectorHelp";
   public static final String HID_LISTSELECTOR = "listSelectorHelp";
   public static final String HID_ITEMEXTRACTOR = "itemExtractorHelp";
   public static final String HID_STATIC_EXTRACTOR = "staticExtractorHelp";
   public static final String HID_PAGE_EXTRACTOR = "pageExtractorHelp";
   public static final String HID_BINARY_EXTRACTOR = "binaryExtractorHelp";
   public static final String HID_XML_EXTRACTOR = "xmlExtractorHelp";
   public static final String HID_XSL_EXTRACTOR = "xslExtractorHelp";
   public static final String HID_LOGGING = "loggingHelp";
   public static final String HID_ERR = "errorHelp";

   /**
    * The cached data for current session. Initialized at the beginning
    * of the {@link #createNodes(DefaultMutableTreeNode)}, never
    * <code>null</code> after that.
    */
   private static CachedData ms_cachedData;

   /**
    * Indicate whether the cached data has been initialized. <code>true</code>
    * if it has been initialized; <code>false</code> if it has not been
    * initialized.
    */
   private static boolean ms_hasCachedData = false;

   /**
    * This class is used to cache constant information for each running session.
    * The constant info includes the constant node and the data relate to
    * the definitions in the configuration.
    */
   private class CachedData
   {
      /**
       * The top level node for extractor (both static and item)
       */
      FolderNode m_extractorNode;

      /**
       * The top level node for static extractors
       */
      FolderNode m_staticNode;

      /**
       * The top level node for item extractors
       */
      FolderNode m_itemNode;

      /**
       * The selection panel for static extractors
       */
      PSMultiSelectionEditorPanel m_staticSelPanel;

      /**
       * The selection panel for item extractors
       */
      PSMultiSelectionEditorPanel m_itemSelPanel;

      /**
       * The top level loader node.
       */
      FolderNode m_loaderNode;

      /**
       * The selection panel for loaders
       */
      PSSingleSelectionEditorPanel m_loaderPanel;

      /**
       * The top level selector node.
       */
      FolderNode m_selectorNode;

      /**
       * The selection panel for selectors
       */
      PSSingleSelectionEditorPanel m_selectorPanel;
   }
}
