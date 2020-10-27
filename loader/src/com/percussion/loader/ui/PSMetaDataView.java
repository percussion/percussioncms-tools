/**[ PSMetaDataView.java ]*****************************************************
 *
 * COPYRIGHT (c) 2002 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 ******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.loader.IPSContentTreeNode;
import com.percussion.loader.PSItemContext;
import com.percussion.loader.PSLoaderUtils;
import com.percussion.loader.objectstore.PSExtractorDef;
import com.percussion.loader.objectstore.PSItemExtractorDef;
import com.percussion.loader.util.PSMapPair;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * This panel shows the meta data properties of a selected tree node.
 */
public class PSMetaDataView extends JPanel implements TreeSelectionListener,
   TreeModelListener
{
   /**
    * Construct the new meta data view panel.
    */
   public PSMetaDataView()
   {
      initPanel();
   }

   /**
    * Initialize the panel.
    */
   private void initPanel()
   {
      m_res = ResourceBundle.getBundle(
         getClass().getName() + "Resources", Locale.getDefault());

      String[] columnNames =
      {
         m_res.getString("column.property"),
         m_res.getString("column.value")
      };
      m_properties = new PSTwoColumnModel(columnNames, PROPERTIES_COUNT);

      List list = m_properties.getList();
      for (int i=0; i<list.size(); i++)
      {
         PSMapPair pair = (PSMapPair) list.get(i);
         pair.setKey(m_res.getString(ROW_RESOURCE_NAMES[i]));
         pair.setValue("");
      }

      BoxLayout box = new BoxLayout(this, BoxLayout.X_AXIS);
      setLayout(box);

      JTable table = new JTable(m_properties);
      add(table);
   }
   
   /**
    * Dispose any resources this view may use.
    */
   public void cleanup()
   {
      List list = m_properties.getList();
      for (int i=0; i<list.size(); i++)
      {
         PSMapPair pair = (PSMapPair) list.get(i);
         pair.setValue("");
      }        
   }

   /**
    * See {@link #TreeSelectionListener} for description.
    */
   public void valueChanged(TreeSelectionEvent e)
   {
      JTree tree = (JTree) e.getSource();
            
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) 
         tree.getLastSelectedPathComponent();
     
      if (node == null) 
         return;

      Object nodeInfo = node.getUserObject();
      
      if (nodeInfo instanceof IPSContentTreeNode)
      {       
         IPSContentTreeNode psnode = (IPSContentTreeNode) nodeInfo;
         // Update the ui with data from this psnode's 
         // itemContext
         PSItemContext item = psnode.getItemContext();      
         updateData(item);         
      }
   }
   
   /**
    * Updates the ui based on <code>item</code>.
    * 
    * @param item PSItemContext data. May be <code>null</code>.
    */
   private void updateData(PSItemContext item)
   {
      // Clear out the info if data is null
      if (item == null)
      {
         List list = m_properties.getList();
         for (int i=0; i<list.size(); i++)
         {
            PSMapPair pair = (PSMapPair) list.get(i);
            pair.setValue("");
         }        
      }
      else
      {
         // Fill out some data
         String strContentType = "";
         String strRevision = "";
         String strLastUpdate = "";
         String strLastScan = "";
         String extractorName = "";

         try
         {
            PSExtractorDef extrDef = item.getExtractorDef();
            if (extrDef != null)
            {
               if (! extrDef.isStaticType())
                  strContentType = PSLoaderUtils.getProperty(
                     PSItemExtractorDef.CONTENT_TYPE_NAME,
                     item.getExtractorDef().getProperties()).getValue();
               
               extractorName = extrDef.getName();
            }

            if (item.getLastLoad() != null)
               strLastUpdate = item.getLastLoad().toString();            

            if (item.getLastScan() != null)
               strLastScan = item.getLastScan().toString();            
         }
         catch (Exception ignore) // if any exception occured, it will simply
         {}                       // display BLANK info, hence no harm to ignore
            
         if (item.getLocator() != null)
         {
            strContentType = Integer.toString(item.getLocator().getId());
            strRevision = Integer.toString(item.getLocator().getRevision());            
         }
    
         List list = m_properties.getList();
         for (int i=0; i<list.size(); i++)
         {
            PSMapPair pair = (PSMapPair) list.get(i);
            String strKey = (String) pair.getKey();
            
            if (strKey.equalsIgnoreCase(m_res.getString("property.contentid")))
            {                             
               pair.setValue(strContentType);
            }
            else if (strKey.equalsIgnoreCase(m_res.getString(
               "property.revision")))
            {
               pair.setValue(strRevision);
            }
            else if (strKey.equalsIgnoreCase(m_res.getString(
               "property.mimetype")))
            {
               pair.setValue(item.getResourceMimeType());
            }
            else if (strKey.equalsIgnoreCase(m_res.getString(
               "property.lastscan")))
            {
               pair.setValue(strLastScan);
            }
            else if (strKey.equalsIgnoreCase(m_res.getString(
               "property.lastupdate")))
            {
               pair.setValue(strLastUpdate);
            }
            else if (strKey.equalsIgnoreCase(m_res.getString(
               "property.extractormatches")))
            {
               pair.setValue(extractorName);
            }
            else if (strKey.equalsIgnoreCase(
               m_res.getString("property.status")))
            {
               pair.setValue(item.getStatus());
            }            
         }
         
         // Refresh the ui
         updateUI();
      }
   }
   
   /**
    * TreeModelListener interface implementation
    */
   /**
    * see {@link javax.swing.event.TreeModelListener} for description
    */
   public void treeNodesChanged(TreeModelEvent e)
   {           
   }

   /**
    * see {@link javax.swing.event.TreeModelListener} for description
    */
   public void treeNodesInserted(TreeModelEvent e)
   {
   }

   /**
    * see {@link javax.swing.event.TreeModelListener} for description
    */
   public void treeNodesRemoved(TreeModelEvent e)
   {
   }

   /**
    * see {@link javax.swing.event.TreeModelListener} for description
    */
   public void treeStructureChanged(TreeModelEvent e)
   {      
      // Threshold
      if (e.getSource() == null)
         return;
      
      // Threshold
      if (!(e.getSource() instanceof IPSContentTreeNode))
         return;

      IPSContentTreeNode n = (IPSContentTreeNode) e.getSource();
      this.updateData(n.getItemContext());
   }


   /**
    * The number of meta data properties shown in this panel.
    */
   public static final int PROPERTIES_COUNT = 7;

   /**
    * Generic table model representing two column editable table. In this class
    * the first column represents name of the property - 'Property' and the
    * second column, the value 'Value'. Initialized in {@link #initPanel()},
    * never code>null</code> or modified after that.
    */
   private PSTwoColumnModel m_properties = null;

   /**
    * The resource bundle of this panel. Initialized in constructor, never
    * <code>null</code> or changed after that.
    */
   private ResourceBundle m_res = null;

   /**
    * An array of row resource names. The array is in the order the rows will
    * be displayed.
    */
   private static final String[] ROW_RESOURCE_NAMES =
   {
      "property.contentid",
      "property.revision",
      "property.mimetype",
      "property.lastscan",
      "property.lastupdate",
      "property.extractormatches",      
      "property.status"
   };
}