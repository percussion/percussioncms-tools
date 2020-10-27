/*******************************************************************************
 *
 * [ ActionsVisibilityPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSActionVisibilityContext;
import com.percussion.cms.objectstore.PSActionVisibilityContexts;
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
 * The Visibility tab shows all components needed to define the menu visibility
 * at runtime.
 */
public class ActionsVisibilityPanel extends JPanel implements
    TreeSelectionListener
{
   /**
    * Constructs the panel.
    */
   public ActionsVisibilityPanel()
   {
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
      PSTreeObject rootUserObj = new PSTreeObject(ms_res.getString(
            "root.name"), new JPanel());
      m_rootNode = new
         DefaultMutableTreeNode(rootUserObj);
      m_srcLabel = ms_res.getString("list.show");
      m_targetLabel = ms_res.getString("list.hide");
      m_srcTrgtPanel = new PSSourceTargetListPanel(m_srcLabel, m_targetLabel,
         ms_res.getString("btn.hide"), ms_res.getString("btn.show"), 
         ms_res.getString("btn.hide.mn"), ms_res.getString("btn.show.mn"));
      try
      {
         List ctxNamelist =
            PSCatalogedVisibilityContexts.getVisibilityContextNames();
         m_catalogSet = PSCatalogedVisibilityContexts.getVCMap();
         int sz = ctxNamelist.size();
         DefaultMutableTreeNode childNode = null;
         PSTreeObject usrObj = null;
         PSComparablePair pair = null;
         String vcName = "";
         for (int k = 0; k < sz; k++)
         {
            pair = (PSComparablePair)ctxNamelist.get(k);
            vcName = (String)pair.getValue();
            usrObj = new PSTreeObject(vcName, m_srcTrgtPanel);
            /* Store the visibility context key in the tree object, then
               use the local copy of m_catalogSet to get the values when
               needed. This allows us to get either the display labels or
               internal names as needed. */
            usrObj.setDataObject(pair.getKey());
            childNode = new DefaultMutableTreeNode(usrObj);
            m_rootNode.add(childNode);
         }
      }
      catch(Exception e)
      {
         e.printStackTrace();
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
      if (data instanceof PSAction)
      {
         m_action = (PSAction) data;
         
         TreePath path = m_tree.getSelectionPath();
         if (path != null)
         {
            DefaultMutableTreeNode node = 
               (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node != null && !node.isRoot())
            {
               PSTreeObject usrObj = (PSTreeObject) node.getUserObject();
               return updateUI(usrObj, 
                  m_action.getVisibilityContexts().getContext(
                     (String) usrObj.getDataObject()), isLoad);
            }
         }
      }
      else
      {
         PSTreeObject obj = (PSTreeObject) data;
         PSActionVisibilityContext ctxt = 
            m_action.getVisibilityContexts().getContext(
               (String) obj.getDataObject());

         return  updateUI(obj, ctxt, isLoad);
      }
      
      return true;
   }

   private boolean updateUI(PSTreeObject obj, PSActionVisibilityContext ctxt ,
      boolean isLoad)
   {
      DefaultListModel tmdl = m_srcTrgtPanel.getModel(m_targetLabel);
      DefaultListModel smdl = m_srcTrgtPanel.getModel(m_srcLabel);
      //key is stored in tree object
      String ctxKey = (String) obj.getDataObject();
      if (isLoad)
      {
         // fill in right side list w/ all entries currently in ctx
         tmdl.clear();
         if (ctxt != null)
         {
            Iterator itr = ctxt.iterator();
            while(itr.hasNext())
            {
               String entryId = (String)itr.next();
               String label = getLabel(ctxKey, entryId);
               if (label.length() == 0)
                  label = entryId;
               tmdl.addElement(label);
            }
         }

         // fill in the left side w/ all entries in list that aren't in ctx
         List entries = getCatalog(ctxKey);
         int sz = tmdl.size();
         int size = entries.size();
         PSComparablePair pair = null;
         smdl.clear();
         for (int k = 0; k < size; k++)
         {
            pair = (PSComparablePair)entries.get(k);
            if ((ctxt == null || !ctxt.contains((String) pair.getKey())))
               smdl.addElement((String) pair.getValue());
         }
      }
      else
      {
         //save entries in right side to context
         int sz = tmdl.size();
         if (ctxt == null && sz != 0)
         {
            String strArr[] = new String[sz];
            for (int k = 0; k < sz; k++)
            {
               String label = (String) tmdl.get(k);
               strArr[k] = getKey(ctxKey, label);
               /* If we couldn't find a label when the panel was loaded, we
                  deal with that case here. */
               if (strArr[k].length() == 0)
                  strArr[k] = label;
            }
            PSActionVisibilityContexts c = m_action.getVisibilityContexts();
            c.addContext(ctxKey, strArr);
         }
         else
         {
            if (sz == 0)
               m_action.getVisibilityContexts().removeContext(ctxKey);
            else
            {
               List newList = new ArrayList();
               for (int k = 0; k < sz; k++)
               {
                  String label = (String) tmdl.get(k);
                  String entryKey = getKey(ctxKey, label);
                  /* If we couldn't find a label when the panel was loaded, we
                     deal with that case here. */
                  if (entryKey.length() == 0)
                     entryKey = label;
                  ctxt.add(entryKey);
                  newList.add(entryKey);
               }
               //remove all entries that are in ctx but not in new list
               Iterator curValues = ctxt.iterator();
               while (curValues.hasNext())
               {
                  String currentEntry = (String) curValues.next();
                  if (!newList.contains(currentEntry))
                  {
                     ctxt.remove(currentEntry);
                     curValues = ctxt.iterator();
                  }
               }
            }
         }
      }

      return true;
   }



   public boolean validateData()
   {
      return true;
   }

   /**
    * Returns the list of allowed entries for the supplied visibility context
    * key. These values are sorted in ascending alpha order and suitable for
    * display to the end user. m_catalogSet is used to perform the mapping.
    *
    * @param visibilityCtxKey The internal name of the visibility context of
    *    interest. Assumed not <code>null</code> or empty.
    *
    * @return Never <code>null</code>.
    */
   private List getLabels(String visibilityCtxKey)
   {
      List catalog = getCatalog(visibilityCtxKey);
      List labels = new ArrayList();
      Iterator entries = catalog.iterator();
      while (entries.hasNext())
         labels.add(((PSComparablePair) entries.next()).getValue());
      return labels;
   }

   /**
    * Maps a particular entry for a particular context back to its internal
    * value. m_catalogSet is used to perform the mapping.
    *
    * @param visibilityCtxKey The internal name for the visibility context
    *    which contains entryLabel as one of its possible values. Assumed
    *    not <code>null</code> or empty.
    *
    * @param entryLabel A display label for one of the possible values
    *    in the catalog for visibilityCtxKey. Assumed not <code>null</code>
    *    or empty.
    *
    * @return The internal name for the label. "" if it can't be found.
    */
   private String getKey(String visibilityCtxKey, String entryLabel)
   {
      List catalog = getCatalog(visibilityCtxKey);
      return getKey(catalog, entryLabel);
   }

   /**
    * Maps a particular entry for a particular context from its internal name
    * to its display name. m_catalogSet is used to perform the mapping.
    *
    * @param visibilityCtxKey The internal name for the visibility context
    *    which contains entryKey as one of its possible values. Assumed
    *    not <code>null</code> or empty.
    *
    * @param entryKey The internal name for one of the possible values
    *    in the catalog for visibilityCtxKey. Assumed not <code>null</code>
    *    or empty.
    *
    * @return The display label for the entry. "" if it can't be found.
    */
   private String getLabel(String visibilityCtxKey, String entryKey)
   {
      List catalog = getCatalog(visibilityCtxKey);
      return getValue(catalog, entryKey);
   }


   /**
    * Looks through all pairs in catalog, comparing the key of each entry to
    * the supplied key until a case-insensitive match is found or the end of
    * the catalog is reached.
    *
    * @param catalog Contains PSComparablePair entries. Assumed not <code>
    *    null</code>. Assumes that each entry has a non-<code>null</code> key
    *    and value.
    *
    * @param key Assumed not <code>null</code>.
    *
    * @return The value associated with this key, "" if not found.
    */
   private String getValue(List catalog, String key)
   {
      Iterator entries = catalog.iterator();
      while (entries.hasNext())
      {
         PSComparablePair entry = (PSComparablePair) entries.next();
         if (((String) entry.getKey()).equalsIgnoreCase(key))
            return (String) entry.getValue();
      }
      return "";
   }


   /**
    * Looks through all pairs in catalog, comparing the value of each entry to
    * the supplied value until a case-insensitive match is found or the end of
    * the catalog is reached.
    *
    * @param catalog Contains PSComparablePair entries. Assumed not <code>
    *    null</code>. Assumes that each entry has a non-<code>null</code> key
    *    and value.
    *
    * @param value Assumed not <code>null</code>.
    *
    * @return The key associated with this value, "" if not found.
    */
   private String getKey(List catalog, String value)
   {
      Iterator entries = catalog.iterator();
      while (entries.hasNext())
      {
         PSComparablePair entry = (PSComparablePair) entries.next();
         if (((String) entry.getValue()).equalsIgnoreCase(value))
            return (String) entry.getKey();
      }
      return "";
   }

   /**
    * Returns the list of PSComparablePair objects for the supplied context.
    * Obtained from m_catalogSet.
    *
    * @param visibilityCtxKey The internal name for the visibility context
    *    for which you want the catalog. Assumed not <code>null</code> or empty.
    *
    * @return Never <code>null</code>. The returned list must be treated
    *    read-only, the owner does not take ownership.
    *
    * @throws IllegalStateException If a catalog is not found.
    */
   private List getCatalog(String visibilityCtxKey)
   {
      List catalog = (List) m_catalogSet.get(visibilityCtxKey);
      if (null == catalog)
      {
         throw new IllegalStateException("No catalog found for "
               + visibilityCtxKey);
      }

      return catalog;
   }

   //test code
  /** public static void main(String[] arg)
   {
      JFrame f = new JFrame("BoxLayoutDemo");
      Container contentPane = f.getContentPane();
      ActionsVisibilityPanel ac = new ActionsVisibilityPanel();
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

   private PSSourceTargetListPanel m_srcTrgtPanel;
   private String m_srcLabel;
   private String m_targetLabel;
   private PSAction m_action;


   /**
    * This map contains all of the lists that contain all of the entries for
    * all of the visibility contexts. Each entry has a key which is the
    * internal identifier for the visibility context. The value is a List,
    * which contains PSComparablePair entries. Each of these guys contains
    * a key that is the internal name of the entry and the value is the
    * display label.
    * <p>Set in createNodes(), then never changed or modified.
    */
   private Map m_catalogSet;

   /**
   * A generic tree whoes model is specified by {@link#setModel(TreeModel)}.
   * Initialized in {@link#init()}, never <code>null</code> or modified after
   * that.
   */
    private JTree m_tree;

   private DefaultMutableTreeNode m_rootNode;

   /**
    * Resource bundle for this class. Initialized in {@link init()}.
    * It's not modified after that. Never <code>null</code>, equivalent to a
    * variable declared final.
    */
   private static ResourceBundle ms_res;
}
