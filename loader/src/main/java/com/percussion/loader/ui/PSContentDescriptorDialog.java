/******************************************************************************
 *
 * [ PSContentDescriptorDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.loader.ui;

import com.percussion.cms.objectstore.client.PSRemoteException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.PSLoaderRemoteAgent;
import com.percussion.loader.objectstore.PSComponentList;
import com.percussion.loader.objectstore.PSConnectionDef;
import com.percussion.loader.objectstore.PSContentLoaderConfig;
import com.percussion.loader.objectstore.PSContentSelectorDef;
import com.percussion.loader.objectstore.PSErrorHandlingDef;
import com.percussion.loader.objectstore.PSExtractorDef;
import com.percussion.loader.objectstore.PSFieldTransformationDef;
import com.percussion.loader.objectstore.PSFieldTransformationsDef;
import com.percussion.loader.objectstore.PSItemExtractorDef;
import com.percussion.loader.objectstore.PSItemTransformationsDef;
import com.percussion.loader.objectstore.PSLoaderComponent;
import com.percussion.loader.objectstore.PSLoaderDef;
import com.percussion.loader.objectstore.PSLoaderDescriptor;
import com.percussion.loader.objectstore.PSLogDef;
import com.percussion.loader.objectstore.PSStaticItemExtractorDef;
import com.percussion.loader.objectstore.PSTransformationDef;
import com.percussion.loader.objectstore.PSWorkflowDef;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import org.w3c.dom.Element;

/**
 * The Content Loader Descriptor Setup Dialog provides an interface that allows
 * to configure all variables for a migration process
 */
public class PSContentDescriptorDialog extends PSContentDialog implements
   TreeSelectionListener, TreeWillExpandListener, IPSConfigChangeListener
{
   /**
    * Creates the descriptor dialog with a given configuration object.
    *
    * @param frame owner the <code>Frame</code> from which the dialog is
    *    displayed.
    * @param config, configuration object, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException if the parameters are invalid.
    * @throws PSLoaderException if other error occurs
    */
   public PSContentDescriptorDialog(Frame frame, PSContentLoaderConfig config)
      throws PSLoaderException
   {
      this(frame, config, null);
   }

   /**
    * Creates the descriptor dialog with a given descriptor object.
    *
    * @param frame owner the <code>Frame</code> from which the dialog is
    *    displayed.
    * @param config, configuration object, may not be <code>null</code>.
    * @param loaderDesc, descriptor object, may be <code>null</code>.
    *
    * @throws IllegalArgumentException if the parameters are invalid.
    * @throws PSLoaderException if other error occurs
    */
   public PSContentDescriptorDialog(Frame frame, PSContentLoaderConfig config,
      PSLoaderDescriptor loaderDesc)
      throws PSLoaderException
   {
      super(frame);

      if (config == null)
         throw new IllegalArgumentException("config object cannot be null");

      m_config = config;
      m_loaderDesc = loaderDesc;
      setWaitCursor(frame);
      try
      {
         validateConnectionInfo();
         initDialog();
      }
      finally
      {
         resetCursor();
      }
   }

   /**
    * Validates the connection info from either the descriptor (if exist) or
    * the configuration. The connection info in the descriptor may be set to
    * the connection info if it failed the validation.
    * 
    * @throws PSLoaderException if an error occurs.
    */
   private void validateConnectionInfo()
      throws PSLoaderException
   {
      PSConnectionDef conn = null;
      if (m_loaderDesc != null)
      {
         conn = m_loaderDesc.getConnectionDef();
         try
         {
            validateConnectionInfo(conn);
         }
         catch (PSLoaderException e) 
         {
            // something wrong with the connection info in descriptor
            // then try the connection info in the configuration.
            conn = m_config.getConnectionDef();
            validateConnectionInfo(conn);
            m_loaderDesc.setConnectionDef(conn);
         }
      }
      else
      {
         validateConnectionInfo(m_config.getConnectionDef());
      }
   }
   
   /**
    * Validates the supplied connection info.
    * 
    * @param conn The connection info that will be used to communicate with 
    *    the remote server, assume not <code>null</code>.
    * 
    * @throws PSLoaderException if an error occurs.
    */
   private void validateConnectionInfo(PSConnectionDef conn)
      throws PSLoaderException
   {
      PSLoaderRemoteAgent remoteAgent = 
         new PSLoaderRemoteAgent(conn);
      
      try
      {
        remoteAgent.login();
      }
      catch(PSRemoteException re)
      {
         throw new PSLoaderException(re);
      }
      
   }
   
   /**
    * Set wait cursor to the supplied component.
    * 
    * @param component The component whoes cursor will set to wait cursor,
    *    assume not <code>null</code>.
    */
   private void setWaitCursor(Component component)
   {
      m_currCursor = component.getCursor();
      Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
      component.setCursor(waitCursor);
      
      m_waitCursorComponent = component;
   }
   
   /**
    * Reset the cursor to previous one for the component that has been set
    * to wait cursor by {@link #setWaitCursor(Component)}. It is caller's
    * resposibility to call the {@link #setWaitCursor(Component)} method 
    * before this.
    */
   private void resetCursor()
   {
      if (m_currCursor == null || m_waitCursorComponent == null)
         throw new IllegalStateException(
            "m_currCursor or m_waitCursorComponent must not be null");
         
      m_waitCursorComponent.setCursor(m_currCursor);
      
      m_currCursor = null;
      m_waitCursorComponent = null;
   }
      
   /**
    * Initialises the dialog.
    */
   private void initDialog()
   {
      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(
               getClass().getName() + "Resources", Locale.getDefault());

      JPanel mainPane = new JPanel();
      mainPane.setLayout(new BorderLayout());
      PSContentCommandPanel cmdPanel = new PSContentCommandPanel(this,
         SwingConstants.HORIZONTAL, true);
      if (m_loaderDesc != null)
         m_treePanel  = new PSDescriptorTreePanel(m_config, m_loaderDesc);
      else
         m_treePanel  = new PSDescriptorTreePanel(m_config);
      m_treePanel.addTreeSelectionListener(this);
      m_treePanel.addTreeWillExpandListener(this);

      //default panel; getting root node will be convenient.
      m_rootNode = (DefaultMutableTreeNode)m_treePanel.getModel().getRoot();
      PSTreeObject root = (PSTreeObject)m_rootNode.getUserObject();
      m_helpId = root.getHelpId();
      PSMainDescriptorPanel defaultPane = (PSMainDescriptorPanel)root.getUIObj();
      
      // add listener to the connection panel
      m_connPanel = defaultPane.getConnectionPanel();
      m_connPanel.addChangeListener(this);
      
      m_split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
      m_split.setLeftComponent(m_treePanel);
      m_split.setRightComponent(defaultPane);

      defaultPane.setPreferredSize(new Dimension(300, 400));
      m_treePanel.setPreferredSize(new Dimension(400, 400));
      m_split.setPreferredSize(new Dimension(800, 500));

      mainPane.setBorder(BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));
      cmdPanel.setBorder(BorderFactory.createEmptyBorder( 5, 0, 0, 0 ));
      mainPane.add(m_split, BorderLayout.CENTER);
      mainPane.add(cmdPanel, BorderLayout.SOUTH);

      getContentPane().add(mainPane);
      pack();
      center();
      setResizable(true);
   }

   /**
    * Implements the {@link IPSConfigChangeListener#configurationChanged(
    * PSConfigurationChangeEvent)} interface. 
    * 
    * @param event The change event, it may not be <code>null</code>.
    */
   public void configurationChanged(PSConfigurationChangeEvent event)
   {
      if (event == null)
         throw new IllegalArgumentException("event may not be null");
      
      if (event.getSource() == m_connPanel)
      {
         boolean caughtException = false;
         
         // reset connection info will re-validate all extractor definitions
         // which could take a long time.
         setWaitCursor(this); 
            
         try
         {
            Element connectionEl = m_connPanel.save();
            PSConnectionDef conn = new PSConnectionDef(connectionEl);
            if (m_treePanel.resetConnectionInfo(conn))
            {
               m_treePanel.resetAvailableDefinitions();
               resetExtractors(PSDescriptorTreePanel.STATIC_ITEMS);
               resetExtractors(PSDescriptorTreePanel.ITEMS); 
            }
         }
         catch (Exception e) // this may not possible
         {
            caughtException = true;
            resetCursor();
            ErrorDialogs.showErrorDialog(this,
               PSContentLoaderResources.getResourceString(ms_res,
               "error.msg.revalidate"),
               new Object[] {e.toString()},
               PSContentLoaderResources.getResourceString(ms_res,
               "error.title.revalidate"), JOptionPane.ERROR_MESSAGE);
         }
         
         if (! caughtException)
            resetCursor();
      }
   }

   /**
    * Call reset method for all the extractors under the supplied parent node.
    * 
    * @param parentNode The parent node name of the extractors, assume not
    *    <code>null</code> or empty.
    */   
   private void resetExtractors(String parentNodeName)
   {
      TreePath path = PSDescriptorTreePanel.findByName(m_rootNode,
         new String[]{m_rootNode.toString(), 
         PSDescriptorTreePanel.EXTRACTORS,
         parentNodeName});
      DefaultMutableTreeNode parentNode = 
         (DefaultMutableTreeNode) path.getLastPathComponent();

      DefaultMutableTreeNode node = null;
      List exceptionNodes = new ArrayList();
      PSTreeObject treeObj = null;
      for (int i = 0; i < parentNode.getChildCount(); i++)
      {
         node = (DefaultMutableTreeNode) parentNode.getChildAt(i);
         treeObj = (PSTreeObject)node.getUserObject();
         try
         {
            treeObj.getUIObj().reset();
         }
         catch (Exception e)
         {
            exceptionNodes.add(node);
         }
      }
      
      // remove the exception nodes
      Iterator nodes = exceptionNodes.iterator();
      while (nodes.hasNext())
      {
         node = (DefaultMutableTreeNode) nodes.next();
         parentNode.remove(node);
      }
   }
   
   /**
    * The descriptor tree is walked and all it's content are persisted to disk.
    * If any of the required nodes are missing error dialog is shown.
    *
    * @return <code>true</code> if save was successful, <code>false</code>
    *    otherwise.
    */
   private boolean save()
   {
      PSTreeObject obj = (PSTreeObject)m_rootNode.getUserObject();
      PSMainDescriptorPanel pane = (PSMainDescriptorPanel)obj.getUIObj();
      File descDir =  new File(pane.getPath() + File.separator +
         pane.getDescriptorName());

      try
      {
         obj.setName(pane.getPath());
         m_treePanel.getModel().reload();
         //create folder to save the descriptor
         if (m_loaderDesc == null)
         {
            if (!descDir.exists())
               descDir.mkdirs();
         }
         File file = new File(descDir, pane.getDescriptorName() + ".xml");
         m_saveLoaderDesc = new PSLoaderDescriptor(descDir.getAbsolutePath());
         // set connection defintion
         Element elem = pane.save();
         if (elem == null)
           return false;
         else
            m_saveLoaderDesc.setConnectionDef(new PSConnectionDef(elem));
         int children = m_rootNode.getChildCount();
         DefaultMutableTreeNode node = null;
         for (int k = 0; k < children; k++)
         {
            node = (DefaultMutableTreeNode)m_rootNode.getChildAt(k);
            String name = ((PSTreeObject)node.getUserObject()).toString();
            if (name.equals(PSDescriptorTreePanel.CONTENT_SELECTOR))
            {
               if ( 0 != node.getChildCount())
               {
                  if(!setSelector(node))
                     return false;
               }
               else
               {
                  expandNode(node);
                  ErrorDialogs.showErrorDialog(this,
                     PSContentLoaderResources.getResourceString(ms_res,
                     "error.msg.selectormissing"),
                     PSContentLoaderResources.getResourceString(ms_res,
                     "error.title.selectormissing"), JOptionPane.ERROR_MESSAGE);
                  return false;
               }
            }
            else if (name.equals(PSDescriptorTreePanel.EXTRACTORS))
            {
               if (!setExtractors(node))
                  return false;
            }
            else if (name.equals(PSDescriptorTreePanel.CONTENT_LOADER))
            {
               if (0 != node.getChildCount())
               {
                  if(!setContentLoader(node))
                     return false;
               }
               else
               {
                  expandNode(node);
                  ErrorDialogs.showErrorDialog(this,
                   PSContentLoaderResources.getResourceString(ms_res,
                      "error.msg.loadermissing"),
                      PSContentLoaderResources.getResourceString(ms_res,
                      "error.title.loadermissing"), JOptionPane.ERROR_MESSAGE);
                  return false;
               }
            }
            else if (name.equals(PSDescriptorTreePanel.GFT))
            {
               if (0 != node.getChildCount())
                  if (!setGlobalFieldTrans(node))
                     return false;
            }

            else if (name.equals(PSDescriptorTreePanel.GIT))
            {
               if (0 != node.getChildCount())
                  if (!setGlobalItemTrans(node))
                     return false;
            }
            else if (name.equals(PSDescriptorTreePanel.LOG))
            {
               if (!setLogging(node, getLogFilePath(descDir.getAbsolutePath())))
                  return false;
            }
            else if (name.equals(PSDescriptorTreePanel.ERR))
            {
               if(!setErrHandling(node))
                  return false;
            }
          }

          FileWriter wrt = new FileWriter(file);
          PSXmlDocumentBuilder.write(m_saveLoaderDesc.toXml(
                PSXmlDocumentBuilder.createXmlDocument()) , wrt);
      }
      catch(PSLoaderException e)
      {
         ErrorDialogs.showErrorDialog(this,
            PSContentLoaderResources.getResourceString(ms_res, e.getMessage()),
            PSContentLoaderResources.getResourceString(ms_res,
            "err.title.loaderexception"), JOptionPane.ERROR_MESSAGE);
         return false;
      }
      catch(PSUnknownNodeTypeException f)
      {
         ErrorDialogs.showErrorDialog(this,
            PSContentLoaderResources.getResourceString(ms_res, f.getMessage()),
            PSContentLoaderResources.getResourceString(ms_res,
            "error.title.unknownnode"), JOptionPane.ERROR_MESSAGE);
         return false;
      }
      catch (IOException e)
      {
         ErrorDialogs.showErrorDialog(this,
            PSContentLoaderResources.getResourceString(ms_res,
               "error.msg.filewriteerror") + e.getLocalizedMessage(),
            PSContentLoaderResources.getResourceString(ms_res,
            "err.title.loaderexception"), JOptionPane.ERROR_MESSAGE);
         return false;
      }

      // set user preference to this descrptor after validated everything
      PSUserPreferences.saveLastDescPath(descDir.getAbsolutePath());

      return true;
   }

   /**
    * Get the log file path for a given descriptor directory.
    *
    * @param descDir The descriptor directory, it may not be <code>null</code>
    *    or empty.
    *
    * @return The normalized full path, never <code>null</code> or empty.
    */
   public static String getLogFilePath(String descDir)
   {
      if (descDir == null || descDir.trim().length() == 0)
         throw new IllegalArgumentException("descDir may not be null or empty");

      File dirFile = new File(descDir);
      String name = dirFile.getName();
      String path = dirFile.getAbsolutePath() + File.separator + name + ".log";

      return normalizeFileName(path);
   }

   /**
    * Gets the right help page based on the panel selected.
    *
    * @param helpId the default help id (name of the class), it's not being used.
    *
    * @return help id corresponding to the tab selected. Never <code>null</code>
    * or empty.
    *
    */
   protected String subclassHelpId( String helpId )
   {
     return m_helpId;
   }

   /**
    * Makes the specified node viewable.
    *
    * @param node, node to bew viewed, assumed to be not <code>null</code>.
    */
   private void expandNode(DefaultMutableTreeNode node)
   {
      TreePath path =  new TreePath(node.getPath());
      m_treePanel.expand(path);
      m_treePanel.setSelectionPath(path);
   }

   /**
    * Implements TreeWillExpandListener interface
    */
   public void treeWillExpand(TreeExpansionEvent e)
   {
      //do nothing
   }

   /**
    * Implements TreeWillExpandListener interface
    */
   public void treeWillCollapse(TreeExpansionEvent e) throws ExpandVetoException
   {
      JTree tree = (JTree)e.getSource();
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)
         tree.getLastSelectedPathComponent();
      PSTreeObject obj = (PSTreeObject)node.getUserObject();
      if (!obj.getUIObj().validateContent())
         throw new ExpandVetoException(e);
   }

   /**
    * Overriding the same method in {@link PSContentDialog}. Saves the
    * descriptor if there is no error.
    */
   public void onApply()
   {
      save();
   }

   /**
    * Overriding the same method in {@link com.percussion.guitools.PSDialog}.
    * Saves the descriptor if there is no error and disposes the dialog of.
    */
   public void onOk()
   {
      if (save())
         super.onOk();
   }

   /**
    * Sets the data in the child of 'Content Selector' node, which can be any
    * one of the plugin content selectors, to loader descriptor.
    *
    * @param node containing the selector data. Assumed to be not <code>null</
    * code>.
    *
    * @return <code>true</code> if the data has been set successfully else <code
    * >false</code>.
    *
    * @throw PSLoaderException if there is an error with the loader application.
    *
    * @throw PSUnknownNodeTypeException when fromXml is called in one of
    * implementations and the implementation does not support the specified XML
    * node type
    */
   private boolean setSelector(DefaultMutableTreeNode node)
      throws PSLoaderException, PSUnknownNodeTypeException
   {
      DefaultMutableTreeNode child = (DefaultMutableTreeNode)
         node.getChildAt(0);
      PSTreeObject obj = (PSTreeObject)child.getUserObject();
      Element elem = obj.getUIObj().save();
      if (elem == null)
      {
         expandNode(child);
         return false;
      }

      PSContentSelectorDef selDef = new PSContentSelectorDef(
         obj.getUIObj().save());
      m_saveLoaderDesc.setContentSelectorDef(selDef);

      return true;
   }

   /**
    * Sets the data in the children node of 'Extractors' node to loader
    * descriptor.
    *
    * @param node containing the extractor data. Assumed to be not <code>null
    * </code>.
    *
    * @return <code>true</code> if the data has been set successfully else
    *    <code>false</code>.
    *
    * @throw PSLoaderException if there is an error with the loader application.
    *
    * @throw PSUnknownNodeTypeException when fromXml is called in one of
    * implementations and the implementation does not support the specified XML
    * node type
    */
    private boolean setExtractors(DefaultMutableTreeNode node)
      throws PSLoaderException, PSUnknownNodeTypeException
    {
       boolean val =
         setStaticExtractors((DefaultMutableTreeNode)node.getChildAt(0));
       if (!val)
          return false;

       val = setItemExtractors((DefaultMutableTreeNode)node.getChildAt(1));
       if (!val)
          return false;

       if (m_staticItemCount == 0 && m_internalItemCount == 0)
       {
          expandNode(node);
          ErrorDialogs.showErrorDialog(this,
             PSContentLoaderResources.getResourceString(ms_res,
             "error.msg.extractormissing"),
             PSContentLoaderResources.getResourceString(ms_res,
             "error.title.extractormissing"), JOptionPane.ERROR_MESSAGE);
          return false;
       }

       return true;
    }

    /**
     * Sets the data in the children nodes of 'Static Items' node to loader
     * descriptor.
     *
     * @param node containing the static extractor data. Assumed to be
     * not <code>null</code>.

     * @return <code>true</code> if the data has been set successfully else
     * <code>false</code>.

     * @throw PSLoaderException if there is an error with the loader application.
     *
     * @throw PSUnknownNodeTypeException when fromXml is called in one of
     * implementations and the implementation does not support the specified XML
     * node type
    */
   private boolean setStaticExtractors(DefaultMutableTreeNode node)
      throws PSLoaderException, PSUnknownNodeTypeException
   {
      List list = new ArrayList();
      PSStaticItemExtractorDef extItemDef = null;
      PSTreeObject treeObj = null;
      m_staticItemCount = node.getChildCount();
      DefaultMutableTreeNode childNode = null;
      Element elem = null;
      for (int z = 0; z < m_staticItemCount; z++)
      {
         childNode = (DefaultMutableTreeNode)node.getChildAt(z);
         treeObj = (PSTreeObject)childNode.getUserObject();

         if (! treeObj.getUIObj().validateContent())
         {
             ErrorDialogs.
             showErrorDialog(this,
                PSContentLoaderResources.getResourceString(ms_res,
                "error.msg.validatextractor"),
                new Object[] {treeObj.toString()},
                PSContentLoaderResources.getResourceString(ms_res,
                "error.title.validatextractor"), 
                JOptionPane.ERROR_MESSAGE);
              
            return false;
         }

         elem = treeObj.getUIObj().save();
         if (elem == null)
            return false;

         extItemDef = new PSStaticItemExtractorDef(elem);
         list.add(extItemDef);
      }
      m_saveLoaderDesc.setStaticExtractorDefs(list.listIterator());

      return true;
   }

   /**
    * Sets the data in the children nodes of 'Items' node to loader
    * descriptor.
    *
    * @param node containing the item extractor data. Assumed to be not <code>
    * null</code>.

    * @return <code>true</code> if the data has been set successfully else <code
    * >false</code>.

    * @throw PSLoaderException if there is an error with the loader application.
    *
    * @throw PSUnknownNodeTypeException when fromXml is called in one of
    * implementations and the implementation does not support the specified XML
    * node type
    */
   private boolean setItemExtractors(DefaultMutableTreeNode node)
      throws PSLoaderException, PSUnknownNodeTypeException
   {
      List list = new ArrayList();
      PSItemExtractorDef itemDef = null;
      PSTreeObject treeObj = null;
      PSConfigPanel configPanel = null;
      m_internalItemCount = node.getChildCount();
      DefaultMutableTreeNode childNode = null;
      Element elem = null;
      for (int z = 0; z < m_internalItemCount; z++)
      {
         childNode = (DefaultMutableTreeNode)node.getChildAt(z);
         treeObj = (PSTreeObject)childNode.getUserObject();
         configPanel = treeObj.getUIObj();
         if (! configPanel.validateContent())
         {
             ErrorDialogs.showErrorDialog(this,
                PSContentLoaderResources.getResourceString(ms_res,
                "error.msg.validatextractor"),
                new Object[] {treeObj.toString()},
                PSContentLoaderResources.getResourceString(ms_res,
                "error.title.validatextractor"), 
                JOptionPane.ERROR_MESSAGE);
              
            return false;
         }
         
         elem = configPanel.save();

         itemDef = new PSItemExtractorDef(elem);
         itemDef.getFieldTransformationsList().clear();
         itemDef.getItemTransformationsList().clear();
         int count = childNode.getChildCount();
         DefaultMutableTreeNode itemOrfieldNode = null;
         boolean isItem;
         for (int a = 0; a < count; a++)
         {
             itemOrfieldNode = (DefaultMutableTreeNode)childNode.getChildAt(a);
             if (itemOrfieldNode.toString().equals(
                PSDescriptorTreePanel.ACTIONS))
             {
                setActions(itemOrfieldNode, itemDef);
             }
             else
             {
                if (itemOrfieldNode.toString().equals(
                      PSDescriptorTreePanel.FLD_TRANS))
                   isItem = false;
                else
                   isItem = true;
                if (!setTransformer(itemOrfieldNode, itemDef, isItem))
                   return false;
             }
         }
         list.add(itemDef);
         //not clearing the actions; will do it later on
      }
      m_saveLoaderDesc.setItemExtractorDefs(list.listIterator());

      return true;
   }

   /**
    * Saves the actions under item extractors.
    *
    * @param node containing the actions. Assumed to be not <code>null</code>.
    *
    * @param def, {@link com.percussion.loader.PSExtractorDef} object containing
    * the extractor data. Assumed to be  not <code>null</code>.
    *
    * @throw PSLoaderException if there is an error with the loader application.
    *
    * @throw PSUnknownNodeTypeException when fromXml is called in one of
    * implementations and the implementation does not support the specified XML
    * node type
    */
   private void setActions(DefaultMutableTreeNode node, PSItemExtractorDef def)
      throws PSLoaderException, PSUnknownNodeTypeException
   {
      PSTreeObject usrObject = (PSTreeObject)node.getUserObject();
      PSConfigPanel pane = (PSConfigPanel)usrObject.getUIObj();
      Element elem = pane.save();
      if (elem != null)
         def.setWorkflowDef(new PSWorkflowDef(elem));
   }

   /**
    * Sets the data in the children nodes of 'Field Transformer' or 'Item
    * Transformer' nodes under 'Items' to loader descriptor.
    *
    * @param node containing the field or item transformer data. Assumed to be
    * not <code>null</code>.
    *
    * @param def, {@link com.percussion.loader.PSExtractorDef} object containing
    * the extractor data. Assumed to be  not <code>null</code>

    * @return <code>true</code> if the data has been set successfully else
    *    <code>false</code>.

    * @throw PSLoaderException if there is an error with the loader application.
    *
    * @throw PSUnknownNodeTypeException when fromXml is called in one of
    * implementations and the implementation does not support the specified XML
    * node type
    */
   private boolean setTransformer(DefaultMutableTreeNode node,
      PSItemExtractorDef def, boolean item) throws PSLoaderException,
      PSUnknownNodeTypeException
   {
      List list = new ArrayList();
      if (!getTransformerList(node, list, item))
         return false;

      int len = list.size();
      PSComponentList compList = null;
       if (item)
          compList = def.getItemTransformationsList();
       else
          compList = def.getFieldTransformationsList();
       compList.addComponents(list.listIterator());

       return true;
   }

   /**
    * Sets the data in the children nodes of 'Content Loader' to loader
    * descriptor.
    *
    * @param node containing the loader data. Assumed to be not <code>null
    * </code>.
    *
    * @return <code>true</code> if the data has been set successfully else
    *    <code>false</code>.

    * @throw PSLoaderException if there is an error with the loader application.
    *
    * @throw PSUnknownNodeTypeException when fromXml is called in one of
    * implementations and the implementation does not support the specified XML
    * node type
    */
   private boolean setContentLoader(DefaultMutableTreeNode node)
      throws PSLoaderException, PSUnknownNodeTypeException
   {
      DefaultMutableTreeNode child = (DefaultMutableTreeNode)
         node.getChildAt(0);
      PSTreeObject obj = (PSTreeObject)child.getUserObject();
      Element elem = obj.getUIObj().save();
      if (elem == null)
      {
         expandNode(child);
         return false;
      }
      PSLoaderDef loaderDef = new PSLoaderDef(elem);
      m_saveLoaderDesc.setLoaderDef(loaderDef);

      return true;
   }

   /**
    * Sets the data in the children nodes of 'Global Field Transformers' to
    * loader descriptor.
    *
    * @param node containing the global field transformer data. Assumed to be
    * not <code>null</code>.
    *
    * @return <code>true</code> if the data has been set successfully else <code
    * >false</code>.

    * @throw PSLoaderException if there is an error with the loader application.
    *
    * @throw PSUnknownNodeTypeException when fromXml is called in one of
    * implementations and the implementation does not support the specified XML
    * node type
    */
   private boolean setGlobalFieldTrans(DefaultMutableTreeNode node)
      throws PSLoaderException, PSUnknownNodeTypeException
   {
      PSFieldTransformationsDef fieldTransContainer = new
         PSFieldTransformationsDef();
      List list = new ArrayList();
      if (!getTransformerList(node, list, false))
         return false;

      int len = list.size();
      for (int z = 0; z < len; z++)
         fieldTransContainer.addTransformation((PSTransformationDef)list.get(z));
      m_saveLoaderDesc.setFieldTransDef(fieldTransContainer);

      return true;
   }

   /**
    * Sets the data in the children nodes of 'Global Item Transformers' to
    * loader descriptor.
    *
    * @param node containing the global item transformer data. Assumed to be
    * not <code>null</code>.
    *
    * @return <code>true</code> if the data has been set successfully else <code
    * >false</code>.

    * @throw PSLoaderException if there is an error with the loader application.
    *
    * @throw PSUnknownNodeTypeException when fromXml is called in one of
    * implementations and the implementation does not support the specified XML
    * node type
    */
   private boolean setGlobalItemTrans(DefaultMutableTreeNode node)
      throws PSLoaderException, PSUnknownNodeTypeException
   {
      PSItemTransformationsDef itemTransContainer = new
            PSItemTransformationsDef();List list = new ArrayList();
      if (!getTransformerList(node, list, true))
         return false;

      int len = list.size();
      for (int z = 0; z < len; z++)
         itemTransContainer.addTransformation((PSTransformationDef)list.get(z));
      m_saveLoaderDesc.setItemTransDef(itemTransContainer);

      return true;
   }

   /**
    * Gets the transformation data {@link
    * com.percussion.loader.objectstore.PSTransformationDef} in the <code>node
    * </code> in the <code>list</code>.
    *
    * @param node containing the global transformer data. Assumed to be
    * not <code>null</code>.
    *
    * @return <code>true</code> if the data has been successfully obtained from
    * the <code>node</code>.

    * @throw PSLoaderException if there is an error with the loader application.
    *
    * @throw PSUnknownNodeTypeException when fromXml is called in one of
    * implementations and the implementation does not support the specified XML
    * node type
    */
   private boolean getTransformerList(DefaultMutableTreeNode node, List list,
      boolean isItem)
      throws PSLoaderException, PSUnknownNodeTypeException
   {
      PSTransformationDef transDef = null;
      PSTreeObject treeObj = null;
      int childCount = node.getChildCount();
      DefaultMutableTreeNode childNode = null;
      Element elem = null;
      for (int z = 0; z < childCount; z++)
      {
         childNode = (DefaultMutableTreeNode)node.getChildAt(z);
         treeObj = (PSTreeObject)childNode.getUserObject();
         elem = treeObj.getUIObj().save();
         if (elem == null)
         {
            expandNode(childNode);
            return false;
         }
         if (isItem)
            transDef = new PSTransformationDef(elem);
         else
            transDef = new PSFieldTransformationDef(elem);
         list.add(transDef);
      }

      return true;
   }

   /**
    * Sets the data in the 'Logging' node to loader descriptor.
    *
    * @param node containing log configuration data. Assumed to be
    *    not <code>null</code>.
    *
    * @param logFilePath The "normalized" path of the log file, assume not
    *    <code>null</code> or empty.
    *
    * @return <code>true</code> if the data has been set successfully else <code
    * >false</code>.

    * @throw PSLoaderException if there is an error with the loader application.
    *
    * @throw PSUnknownNodeTypeException when fromXml is called in one of
    * implementations and the implementation does not support the specified XML
    * node type
    */
   private boolean setLogging(DefaultMutableTreeNode node, String logFilePath)
      throws PSLoaderException, PSUnknownNodeTypeException
   {
      PSTreeObject obj = (PSTreeObject)node.getUserObject();
      Element elem = obj.getUIObj().save();
      if (elem == null)
         return false;
      PSLogDef logDef = new PSLogDef(elem);
      logDef.setAppenderParam(PSLogDef.FILE_APPENDER, PSLogDef.FILE,
         logFilePath);
      m_saveLoaderDesc.setLogDef(logDef);
      return true;
   }

   /**
    * Sets the data in the 'Error Handling' node to loader descriptor.
    *
    * @param node containing error handling data. Assumed to be
    * not <code>null</code>.
    *
    * @return <code>true</code> if the data has been set successfully else <code
    * >false</code>.

    * @throw PSLoaderException if there is an error with the loader application.
    *
    * @throw PSUnknownNodeTypeException when fromXml is called in one of
    * implementations and the implementation does not support the specified XML
    * node type
    */
   private boolean setErrHandling(DefaultMutableTreeNode node)
      throws PSLoaderException, PSUnknownNodeTypeException
   {
      PSTreeObject obj = (PSTreeObject)node.getUserObject();
      Element elem = obj.getUIObj().save();
      if (elem == null)
         return false;

      PSErrorHandlingDef errorDef = new PSErrorHandlingDef(elem);
      m_saveLoaderDesc.setErrorHandlingDef(errorDef);

      return true;
   }

   /**
    * Implements TreeSelectionListener interface.
    *
    * @param e never <code>null</code>
    */
   public void valueChanged(TreeSelectionEvent e)
   {
      PSConfigPanel nextConfigPane = null;
      PSTreeObject nextNodeInfo = null;
      try
      {
         JTree tree = (JTree)e.getSource();
         tree.getLastSelectedPathComponent();
         DefaultMutableTreeNode nextNode = (DefaultMutableTreeNode)
               tree.getLastSelectedPathComponent();
         if (nextNode == null)
            return;
         nextNodeInfo = (PSTreeObject)nextNode.getUserObject();
         nextConfigPane = nextNodeInfo.getUIObj();

         TreePath prevPath = e.getOldLeadSelectionPath();
         DefaultMutableTreeNode prevNode = null;
         PSConfigPanel prevConfigPane = null;
         if (prevPath != null)
         {
            prevNode = (DefaultMutableTreeNode)
                       prevPath.getLastPathComponent();
            PSTreeObject prevNodeInfo = (PSTreeObject)prevNode.getUserObject();
            prevConfigPane = prevNodeInfo.getUIObj();

            // validating the previous panel
            if (!prevConfigPane.validateContent())
            {
               if (!nextConfigPane.getClass().getName().equals(
                     prevConfigPane.getClass().getName()))
                  tree.setSelectionPath(prevPath);
               nextConfigPane = prevConfigPane;
               m_split.setRightComponent(nextConfigPane);
               return;
            }

            // update node name if needed
            String name = prevConfigPane.getName();
            if ((name != null) && (! name.equals(prevNodeInfo.getName())))
            {
               if (validateNodeName(prevNode, name))
               {
                  prevNodeInfo.setName(name);
               }
               else // select the previous node and inform user
               {
                  tree.setSelectionPath(prevPath);
                  nextConfigPane = prevConfigPane;
                  m_split.setRightComponent(nextConfigPane);
                  
                  ErrorDialogs.showErrorDialog(this,
                     PSContentLoaderResources.getResourceString(ms_res,
                     "error.msg.validatname"),
                     new Object[] {name},
                     PSContentLoaderResources.getResourceString(ms_res,
                     "error.title.validatname"), JOptionPane.ERROR_MESSAGE);
                  
                  return;
               }
            }

            // reset the field names for field transformer panel
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode)
               nextNode.getParent();
            if (parent != null)
            {
               PSTreeObject fieldObject = 
                  (PSTreeObject) nextNode.getUserObject();
               PSLoaderComponent comp = fieldObject.getDataObj();
               if (comp instanceof PSFieldTransformationDef)
               {
                  String nodeName =
                     ((PSTreeObject)parent.getUserObject()).toString();
                     
                  PSTransformationEditorPanel ted = 
                        (PSTransformationEditorPanel) fieldObject.getUIObj();
                  String oldTarget = ted.getTarget();
                  
                  List fieldNames = new ArrayList();
                  
                  if (nodeName.equals(PSDescriptorTreePanel.GFT))
                  {
                     // get list for global field transformer
                     fieldNames.addAll(getFieldNames(null));
                  }
                  else if (nodeName.equals(PSDescriptorTreePanel.FLD_TRANS))
                  {
                     parent = (DefaultMutableTreeNode)parent.getParent();
                     PSTreeObject parentObj = 
                        (PSTreeObject)parent.getUserObject();
                     PSExtractorDef parentComp = 
                        (PSExtractorDef) parentObj.getDataObj();
                     
                     fieldNames.addAll(getFieldNames(parentComp));
                  }
                  ted.setTargetList(fieldNames);
                  ted.setTarget(oldTarget);
               }
            }
         }
      }
      catch(PSLoaderException ex)
      {
         ErrorDialogs.showErrorDialog(this,
            PSContentLoaderResources.getResourceString(ms_res,
               ex.getLocalizedMessage()),
            PSContentLoaderResources.getResourceString(ms_res,
            "err.title.loaderexception"), JOptionPane.ERROR_MESSAGE);
      }
      m_split.setRightComponent(nextConfigPane);
      m_helpId = nextNodeInfo.getHelpId();
   }

   /**
    * Validating the supplied name for the specified node. This is to make sure
    * all extractor names are unique.
    * 
    * @param node The to be renamed node, assume not <code>null</code>.
    * 
    * @param name The new name for the specified node, assume not 
    *    <code>null</code> or empty.
    *    
    * @return <code>true</code> if the new name is valid; <code>false</code>
    *    otherwise.
    */
   private boolean validateNodeName(DefaultMutableTreeNode node, String name)
   {
      DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
      if (parent == null)
         return true;
         
      String parentName = ((PSTreeObject)parent.getUserObject()).toString();
      if ((!parentName.equals(PSDescriptorTreePanel.STATIC_ITEMS)) &&
          (!parentName.equals(PSDescriptorTreePanel.ITEMS)))
      {
          return true; // don't check if not extractor node.
      }
      
      // make sure the name does not exist in the sibling nodes
      String childName;
      DefaultMutableTreeNode childNode;
      int childCount = parent.getChildCount();
      for (int i=0; i < childCount; i++)
      {
         childNode = (DefaultMutableTreeNode)parent.getChildAt(i);
         childName = childNode.getUserObject().toString();
         if (name.equalsIgnoreCase(childName))
            return false;
      }
      
      return true;
   }
   
   /**
    * Creates a list of field names from a specified content type or all the
    * content types in the current descriptor.
    *
    * @param extractorDef The item extractor definition. It may be 
    *    <code>null</code> if needs the field names from all content types.
    *
    * @return A (sorted) list over zero or more <code>String</code>, never 
    *    <code>null</code>, but may be empty.
    *
    * @throws PSLoaderException if there is problem getting the field names.
    */
   private List getFieldNames(PSExtractorDef extractorDef)
      throws PSLoaderException
   {
      List fieldNames = new ArrayList();
      PSExtractorConfigContext ctx = new PSExtractorConfigContext();
      
      if (extractorDef != null) // get field name for one content type
      {
         Iterator names = 
            ctx.getAllFieldNames(extractorDef.getContentTypeName());
         fieldNames = getList(names);
      }
      else // get field names for all content types in the descriptor
      {
         TreePath path = PSDescriptorTreePanel.findByName(m_rootNode,
            new String[]{m_rootNode.toString(), 
               PSDescriptorTreePanel.EXTRACTORS,
               PSDescriptorTreePanel.ITEMS});
         DefaultMutableTreeNode parent = (DefaultMutableTreeNode)
            path.getLastPathComponent();
            
         int childCount = parent.getChildCount();
         DefaultMutableTreeNode childNode;
         Set nameSet = new HashSet();
         for(int i=0; i < childCount; i++)
         {
            childNode = (DefaultMutableTreeNode) parent.getChildAt(i);
            PSTreeObject treeObj = (PSTreeObject) childNode.getUserObject();
            PSLoaderComponent comp = treeObj.getDataObj();
            if ((comp != null) && (comp instanceof PSExtractorDef))
            {
               PSExtractorDef def = (PSExtractorDef) comp;
               Iterator names = ctx.getAllFieldNames(def.getContentTypeName());
               nameSet.addAll(getList(names));
            }
         }
         fieldNames.addAll(nameSet);
      }
      Collections.sort(fieldNames);
      return fieldNames;
   }

   /**
    * Get a list from an Iterator.
    * 
    * @param itr The to be converted iterator, assume not <code>null</code>.
    * 
    * @return The converted list, never <code>null</code>, but may be empty.
    */
   private List getList(Iterator itr)
   {
      List ls = new ArrayList();
      
      while (itr.hasNext())
      {
         ls.add(itr.next());
      }
      return ls;
   }
   
   /**
    * Returns a combined list of all the list present as values in the <code>map
    * </code>.
    *
    * @param map, map of item extractror name and list of target fields within
    * them. Its a map of (String, List), may be <code>null</code> and or empty.
    *
    * @return list of all the target fields, never <code>null</code>, may be
    * empty.
    */
   private List getList(Map map)
   {
      List glbList = new ArrayList();
      if (map != null && !map.isEmpty())
      {
         Collection c = map.values();
         Iterator itor = c.iterator();
         while (itor.hasNext())
         {
            List list = (List)itor.next();
            Iterator listItor = list.iterator();
            while (listItor.hasNext())
               glbList.add(listItor.next());
         }
      }
      return glbList;
   }

   /**
    * Normalizes the path name to the descriptor. If the path is
    * {dir}\folder1\folder2......foldern\foldern.log then the normalized path
    * becomes {dir}\\folder1\\folder2......foldern\\foldern.log on windows
    * {dir}/folder1/folder2......foldern/foldern.log on Unix like systems
    *
    * @param s, descriptor path, never <code>null</code> or empty.
    *
    * @return, normalized path, never <code>null</code> may be empty.
    */
   public static String normalizeFileName(String s)
   {
       return normalizeFileName(s, File.separator);
   }

   /**
    * see {@link#normalizeFileName(String}.
    *
    * @param s, descriptor path, never <code>null</code> or empty.
    *
    * @param c, path separator, never <code>null</code> or empty.
    *
    * @return, normalized path, never <code>null</code> may be empty.
    */
   public static String normalizeFileName(String s, String c)
   {
      StringBuffer sbf = new StringBuffer();
      if(s == null || s.length() == 0)
         throw new IllegalArgumentException("fileName cannot be null or empty");
      if(c == null || c.length() == 0)
         throw new IllegalArgumentException(
         "path separator cannot be null or empty");

      String osName = System.getProperty("os.name");
      String normalizedSep = "";
      if (osName.toLowerCase().startsWith("win"))
         normalizedSep = File.separator + File.separator;
      else
         normalizedSep = File.separator;
      StringTokenizer tok = new  StringTokenizer(s, c);
      String str = "";
      while (tok.hasMoreTokens())
      {
         str = tok.nextToken();
         if (tok.hasMoreTokens())
            sbf.append(str).append(normalizedSep);
         else
            sbf.append(str);
      }
      return sbf.toString();
   }

   /**
    * Accessor to get the PSLoaderDescriptor object.
    *
    * @return PSLoaderDescriptor. Never <code>null</code>.
    */
   public PSLoaderDescriptor getDescriptor()
   {
      return m_saveLoaderDesc;
   }

   /**
    * Current cursor, It may be <code>null</code>.
    */
   private Cursor m_currCursor = null;
   
   /**
    * Current component whoes cursor has been set to wait cursor, It may be 
    * <code>null</code>.
    */
   private Component m_waitCursorComponent = null;
   
   /**
    * Maps the item extractor name to the list target fields in the field
    * transformers in it. Never <code>null</code>, may be empty. Mappings are
    * added to it in {@link #populateTargetFields(DefaultMutableTreeNode)} and
    * cleared in {@link #valueChanged(TreeSelectionEvent)}.
    */
   private Map m_targetMap = new HashMap();

   /**
    * Indicates the count of internal items in the 'Items' node. Intialized in
    * {@link #setExtractors(DefaultMutableTreeNode)}.
    */
   int m_internalItemCount;

   /**
    * Indicates the count of static items in the 'Static Items' node.
    * Intialized in {@link #setStaticExtractors(DefaultMutableTreeNode)}
    */
   int m_staticItemCount;

   /**
    * Encapsulates a loader descriptor, it contains all necessary definitions
    * for each content loader operation. Used for saving the descriptor.
    * Intialized in {@link #save()}, never <code>null</code> after that,
    * modified everytime {@link #save()} is called.
    */
   private PSLoaderDescriptor m_saveLoaderDesc;

   /**
    * Encapsulates a loader descriptor, it contains all necessary definitions
    * for each content loader operation. Used for loading an existing descriptor.
    * Intialized in {@link #save()}, never <code>null</code> after that,
    * modified everytime {@link #save()} is called.
    */
   private PSLoaderDescriptor m_loaderDesc;

   /**
    * Split pane containing the left and right panel. The left panel shows the
    * tree and the right panel displays node sensitive panel. Initialized in the
    * {@link #initDialog()}, never <code>null</code> or modified after that.
    */
   private JSplitPane m_split;

   /**
    * Encapsulates configuration information for the content loader. Intialized
    * in the ctor, never <code>null</null> after that.
    */
   private PSContentLoaderConfig m_config;

   /**
    * Panel containing the tree. Initialized in the {@link #initDialog()}, never
    * <code>null</code> or modified after that.
    */
   private PSDescriptorTreePanel m_treePanel;

   /**
    * Root node of the tree. Initialized in the {@link #initDialog()}, never
    * <code>null</code> or modified after that.
    */
   private DefaultMutableTreeNode m_rootNode;

   /**
    * Resource bundle for this class. Initialized in {@link init()}.
    * It's not modified after that. Never <code>null</code>, equivalent to a
    * variable declared final.
    */
   private static ResourceBundle ms_res;

   /**
    * Initialized in {@link#initDialog()} and changed in {@link#valueChanged(
    * TreeSelectionEvent)} based on node selection, never <code>null</code> or
    * empty after that.
    */
   private String m_helpId;
   
   /**
    * The connection editor panel object. Initialized by constructor, never
    * <code>null</code> after that.
    */
   private PSConnectionEditorPanel m_connPanel;
}
