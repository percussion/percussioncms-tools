/*******************************************************************************
 *
 * [ ActionsUsagePanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSDbComponentCollection;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSMenuContext;
import com.percussion.cms.objectstore.PSMenuMode;
import com.percussion.cms.objectstore.PSMenuModeContextMapping;
import com.percussion.guitools.PSSourceTargetListPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * NOTE: While reviewing please keep in mind that no data objects have been
 * used as of yet so ctor and other places may have to be modified but that
 * shouldn't affect the ui in general, hopefully.
 */

/**
 * The usage panel shows all components to define where current menu is used.
 */
public class ActionsUsagePanel extends JPanel implements
    TreeSelectionListener
{
   /**
    * Constructs the panel.
    */
   public ActionsUsagePanel(List list)
   {
      super();
      m_modeColl = (PSDbComponentCollection)list.get(1);
      m_modeContextColl = (PSDbComponentCollection)list.get(2);
      init();
      createNodes();
   }

   private void init()
   {
      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(getClass().getName() + "Resources",
               Locale.getDefault());
      setLayout(new BorderLayout());
      setPreferredSize(new Dimension(500, 500));
      JPanel treePanel = new JPanel();
      treePanel.setLayout(new BorderLayout());

      EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);
      treePanel.setBorder(emptyBorder);
      treePanel.setPreferredSize(new Dimension(400, 200));
      m_tree = new JTree();
      m_tree.addTreeSelectionListener(this);

      m_tree.getSelectionModel().setSelectionMode(
            TreeSelectionModel.SINGLE_TREE_SELECTION);
      m_tree.setEditable(false);
      JScrollPane scrollPane = new JScrollPane(m_tree);
      treePanel.add(scrollPane, BorderLayout.NORTH);
      add(treePanel, BorderLayout.NORTH);
      JPanel jp = new JPanel();
      jp.setPreferredSize(new Dimension(400, 40));
      add(jp, BorderLayout.SOUTH);
   }

   /**
    * Creates the child nodes and appends them to the root node. User objects
    * corresponding to the child nodes are attached to them such that correct
    * ui is displayed on their selection.
    *
    * @param root, root node may not be <code>null</code>.
    */
   private void createNodes()
   {
      if (null == ms_res)
          ms_res = ResourceBundle.getBundle(getClass().getName() + "Resources",
             Locale.getDefault());
      m_srcLabel = ms_res.getString("list.available");
      m_targetLabel =  ms_res.getString("list.used");
      m_srcTrgtPanel = new PSSourceTargetListPanel(
                     m_srcLabel, m_targetLabel, ms_res.getString("btn.enable"),
                     ms_res.getString("btn.disable"), 
                     ms_res.getString("btn.enable.mn"), 
                     ms_res.getString("btn.disable.mn"));

      List menuModeList = new ArrayList();
      Iterator itr = m_modeColl.iterator();
      PSMenuMode mode = null;
      PSTreeObject userObj = null;
      while(itr.hasNext())
      {
         mode = (PSMenuMode)itr.next();
         userObj = new PSTreeObject(mode.getDisplayName(), m_srcTrgtPanel);
         userObj.setDataObject(mode);
         menuModeList.add(userObj);
      }

      m_modeContextList = new ArrayList();
      itr = m_modeContextColl.iterator();
      PSComparablePair pair = null;
      PSMenuContext ctxt = null;
      PSKey pskey = null;
      String skey = null;
      while(itr.hasNext())
      {
         ctxt = (PSMenuContext)itr.next();
         pskey = ctxt.getLocator();
         skey = pskey.getPart(pskey.getDefinition()[0]);
         m_modeContextMap.put(skey, ctxt);
         pair = new PSComparablePair(ctxt.getDisplayName(), ctxt);
         m_modeContextList.add(pair);
      }

      Collections.sort(menuModeList);
      Collections.sort(m_modeContextList);

      PSTreeObject rootUserObj = new PSTreeObject(ms_res.getString(
          "root.name"), new JPanel());
      m_rootNode = new DefaultMutableTreeNode(rootUserObj);


      int sz = menuModeList.size();
      DefaultMutableTreeNode childNode = null;
      for (int k = 0; k < sz; k++)
      {
         userObj = (PSTreeObject)menuModeList.get(k);
         childNode = new DefaultMutableTreeNode(userObj);
         m_rootNode.add(childNode);
      }
      m_tree.setModel(new DefaultTreeModel(m_rootNode));
      m_tree.setSelectionRow(0);
   }

   /**
    * Implements the interface. Based on the node selection the associated
    * ui is displayed.
    *
    * @param e, event generated on tree selection, never <code>null</code>,
    * called by the swing event handling mechanism.
    */
   public void valueChanged(TreeSelectionEvent e)
   {
      m_srcTrgtPanel.getList(true).clearSelection();
      m_srcTrgtPanel.getList(false).clearSelection();
      JTree tree = (JTree)e.getSource();
      DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)
                                        tree.getModel().getRoot();
      DefaultMutableTreeNode nextNode = (DefaultMutableTreeNode)
                                        tree.getLastSelectedPathComponent();
      TreePath oldPath = e.getOldLeadSelectionPath();
      DefaultMutableTreeNode oldNode = null;
      JPanel oldPanel = null;
      PSTreeObject oldUsrObj = null;
      if (oldPath != null)
      {
         oldNode = (DefaultMutableTreeNode)
                   oldPath.getLastPathComponent();
         oldUsrObj = (PSTreeObject)oldNode.getUserObject();
         oldPanel = oldUsrObj.getUIObject();
         if (oldNode != null && (oldNode != rootNode))
            update(oldUsrObj, false);
      }
      if (nextNode == null)
         return;
      PSTreeObject nextNodeInfo = (PSTreeObject)nextNode.getUserObject();
      JPanel pane = nextNodeInfo.getUIObject();
      if (pane != null)
      {
         if (oldPanel != null)
         {
            remove(oldPanel);
            revalidate();
         }
         add(pane, BorderLayout.CENTER);
         if (nextNode != rootNode)
            update(nextNodeInfo, true);
         repaint();
      }
   }

   public boolean update(Object data, boolean isLoad)
   {
      PSAction action = null;
      if (data instanceof PSAction)
      {
         m_action = (PSAction) data;

         TreePath path = m_tree.getSelectionPath();
         if (path != null)
         {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                  path.getLastPathComponent();
            if (node != null && node != m_rootNode)
            {
               PSTreeObject usrObj = (PSTreeObject)node.getUserObject();
               return updateUI(usrObj, m_action.getModeUIContexts(), isLoad);
            }
         }
      }
      else
      {
         PSTreeObject obj = (PSTreeObject)data;
         PSDbComponentCollection ctxt =  m_action.getModeUIContexts();
         return  updateUI(obj, ctxt, isLoad);
      }
      return true;
   }

   private boolean updateUI(PSTreeObject obj, PSDbComponentCollection  coll ,
      boolean isLoad)
   {
      DefaultListModel tmdl = m_srcTrgtPanel.getModel(m_targetLabel);
      DefaultListModel smdl = m_srcTrgtPanel.getModel(m_srcLabel);
      String name = "";
      PSMenuModeContextMapping mapping = null;
      PSComparablePair pair = null;
      PSMenuMode mode = (PSMenuMode)obj.getDataObject();
      PSKey pskey = mode.getLocator();
      String modeid = pskey.getPart(pskey.getDefinition()[0]);
      if (isLoad)
      {
         tmdl.clear();
         Iterator itr = coll.iterator();
         int key = Integer.parseInt(modeid);
         String ctxtId = "";
         String ctxtName = "";
         PSMenuContext ctxt = null;
         //populate target list panel with modecontext based on mode
         while(itr.hasNext())
         {
            mapping = (PSMenuModeContextMapping)itr.next();
            if (Integer.parseInt(mapping.getModeId()) == key)
            {
               ctxtId = mapping.getContextId();
               ctxt = (PSMenuContext)m_modeContextMap.get(ctxtId);
               tmdl.addElement(ctxt.getDisplayName());
            }
         }
         //populate src list panel
         int sz = m_modeContextList.size();
         smdl.clear();
         for (int k = 0; k < sz; k++)
         {
            pair = (PSComparablePair)m_modeContextList.get(k);
            ctxt = (PSMenuContext)pair.getValue();
            ctxtName = ctxt.getDisplayName();
            if (!tmdl.contains(ctxtName))
               smdl.addElement(ctxtName);
         }
      }
      else
      {
         Iterator itr = coll.iterator();
         //remove all entries
         if (tmdl.size() == 0)
         {
            while (itr.hasNext())
            {
               mapping = (PSMenuModeContextMapping)itr.next();
               if (mapping.getModeId().equalsIgnoreCase(modeid))
               {
                  coll.remove(mapping);
                  itr = coll.iterator();
               }
            }
         }
         else
         {
            //remove any deleted mapping
            int sz = tmdl.getSize();
            itr = coll.iterator();
            String ctxtid = "";
            String mid = "";
            PSMenuContext ctxt = null;
            while (itr.hasNext())
            {
               mapping = (PSMenuModeContextMapping)itr.next();
               ctxtid = mapping.getContextId();
               mid = mapping.getModeId();
               if (mid.equalsIgnoreCase(modeid))
               {
                  ctxt = (PSMenuContext)m_modeContextMap.get(ctxtid);
                  for (int k = 0; k < sz; k++)
                  {
                     name = (String)tmdl.get(k);
                     if (ctxt.getDisplayName().equalsIgnoreCase(name))
                        break;
                     if (k == sz - 1)
                     {
                        coll.remove(mapping);
                        itr = coll.iterator();
                     }
                  }
               }
            }
            //add new mappings
            int size = m_modeContextList.size();
            String ctxtName = "";
            PSKey key = null;
            boolean found = false;
            for (int k = 0; k < sz; k++)
            {
               found = false;
               name = (String)tmdl.get(k);
               for (int z = 0; z < size; z++)
               {
                  pair = (PSComparablePair)m_modeContextList.get(z);
                  ctxtName = (String)pair.getKey();
                  if (ctxtName.equalsIgnoreCase(name))
                  {
                     ctxt = (PSMenuContext)pair.getValue();
                     key = ctxt.getLocator();
                     ctxtid = key.getPart(key.getDefinition()[0]);
                     itr = coll.iterator();
                     //happen only when itr is empty for the first time and new
                     //values have to be added. Come back and optimise
                     if (!itr.hasNext())
                     {
                        found = true;
                        mapping = new PSMenuModeContextMapping(mode, ctxt);
                        coll.add(mapping);
                        break;
                     }
                     while (itr.hasNext())
                     {
                        mapping = (PSMenuModeContextMapping)itr.next();
                        if (mapping.getModeId().equalsIgnoreCase(modeid) &&
                           mapping.getContextId().equalsIgnoreCase(ctxtid))
                        {
                           found = true;
                           break;
                        }
                        if (!itr.hasNext())
                        {
                           found = true;
                           mapping = new PSMenuModeContextMapping(mode, ctxt);
                           coll.add(mapping);
                           break;
                        }
                     }
                  }
                  if (found)
                     break;
               }
            }
         }
      }
      return true;
   }



   //test code
 /***  public static void main(String[] arg)
   {
      JFrame f = new JFrame("BoxLayoutDemo");
      Container contentPane = f.getContentPane();
      ActionsUsagePanel ac = new ActionsUsagePanel();
      contentPane.add(ac, BorderLayout.CENTER);
      f.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {
            System.exit(0);
         }
      });
      f.pack();
      f.setVisible(true);
   }*/
   //end
   private String m_srcLabel;
   private String m_targetLabel;
   private PSSourceTargetListPanel m_srcTrgtPanel;
   private PSAction m_action;
   private DefaultMutableTreeNode m_rootNode;
   private JTree m_tree;
   private PSDbComponentCollection m_modeColl;
   private PSDbComponentCollection m_modeContextColl;
   private Map m_modeContextMap = new HashMap();
   private List m_modeContextList;
   /**
    * Resource bundle for this class. Initialized in {@link init()}.
    * It's not modified after that. Never <code>null</code>, equivalent to a
    * variable declared final.
    */
   private static ResourceBundle ms_res;
}
