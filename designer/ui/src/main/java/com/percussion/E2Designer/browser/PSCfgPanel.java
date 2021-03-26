/******************************************************************************
 *
 * [ PSCfgPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.guitools.IPSTableModel;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Abstract class that represents a particular relationship config panel.
 * Extracted it from {@link PSRelationshipEditorDialog} for easier comprehension.
 */
abstract class PSCfgPanel extends JPanel
{
   /**
    * Ctor.
    *
    * @param view name of the view, never <code>null</code>.
    */
   public PSCfgPanel(String view, PSRelationshipEditorDialog owner)
   {
      if (view == null)
         throw new IllegalArgumentException("view may not be null");

      mi_view = view;
      mi_owner = owner;
   }

   /**
    * Sets default layout and creates any default comps. such as description box.
    */
   public void preInitPanel()
   {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

      //create shared description panel
      mi_sharedDescription = new JTextArea();
      mi_sharedDescription.setLineWrap(true);
      mi_sharedDescription.setWrapStyleWord(true);

      mi_sharedDescriptionPane = new JScrollPane (mi_sharedDescription);

      mi_sharedDescriptionPane.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createTitledBorder(mi_owner.getResourceString("description")),
         BorderFactory.createBevelBorder(BevelBorder.LOWERED)));

      mi_sharedDescriptionPane.setPreferredSize(new Dimension (300, 180));
   }


   /**
    * Derived class is reponsible to appropriately layout this panel.
    */
   public abstract void initPanel();

   /**
    * Adds any default comps at the end, also sets default size and titled border.
    */
   public void postInitPanel()
   {
      if (mi_sharedDescriptionPane == null)
         throw new IllegalStateException("mi_systemPropsDescPane must be initialized");

      //add shared description as a last comp.
      add(mi_sharedDescriptionPane);

      //make them all of the same size
      setPreferredSize(new Dimension(350, 400));

      //create an empty title border
      setBorder(BorderFactory.createTitledBorder(""));
   }

   /**
    * Load data into the panel's UI components from a given config instance.
    * @param cfg config instance data to show in the view,
    * never <code>null</code>.
    */
   public void updateViewFromData(PSRelationshipConfig cfg)
   {
      //first of all set the current config!
      mi_cfg = cfg;

      //set border title
      setBorderTitle();
   }

   /**
    * Returns view name.
    * @return view name, never <code>null</code>.
    */
   public String getView()
   {
      return mi_view;
   }

   /**
    * Validates view data.
    * @return returns <code>true</code> if this view has a valid data.
    */
   public abstract boolean validateViewData();

   /**
    * Validates current view and updates data from current view panel to the
    * configuration. The derived class is responsible to save appropriate data
    * that this panel represents. If validation fails then the error dialog is
    * presented to the used and save is not performed.
    */
   public abstract void updateDataFromView();

   /**
    * An instance of the config that this panel shows. Initialized by the
    * {@link #updateViewFromData(PSRelationshipConfig)}
    * and never <code>null</code> after that.
    */
   protected PSRelationshipConfig mi_cfg;

   /**
    * View name. Initialized by the ctor and never <code>null</code> after that.
    */
   protected String mi_view;
   
   /**
    * Owner of the panel
    */
   protected PSRelationshipEditorDialog mi_owner;

   /**
    * Sets border title for this panel. Can only be called from updateViewFromData
    * and after mi_cfg has been already set.
    */
   protected void setBorderTitle()
   {
      String title = mi_view;

      if (mi_cfg==null)
         throw new IllegalStateException("This can only be called from updateViewFromData");

      if (title.equals(PSRelationshipEditorDialog.RELATIONSHIP))
         title = mi_cfg.getLabel();

      Border b = (TitledBorder)getBorder();

      if (b instanceof TitledBorder)
         ((TitledBorder)b).setTitle(title);
      else
         throw new IllegalStateException("Must have a TitledBorder.");
   }

   /**
     * Props description text area. Initialized in <code>preInitPanel()</code>
     * and never <code>null</code> after that.
     */
   protected JTextArea mi_sharedDescription;

   /**
    * Shared descr. pane.  Initialized in <code>preInitPanel()</code>
    * and never <code>null</code> after that.
    */
   private JScrollPane mi_sharedDescriptionPane;

   /**
    * Creates a default IPSTableModelKeyAdapter.
    * @return never <code>null</code>.
    */
   protected KeyAdapter getIPSTableModelKeyAdapter()
   {
      if (mi_IPSTableModelKeyAdapter!=null)
         return mi_IPSTableModelKeyAdapter;

      mi_IPSTableModelKeyAdapter = new KeyAdapter()
      {
         //listens to Delete, CTL-U, CTL-D key events to delete, move up, move
         //down. If the model does not allow those actions it does nothing.
         public void keyReleased(KeyEvent event)
         {
            JTable table = (JTable)event.getSource();

            TableModel model = table.getModel();

            if(!(model instanceof IPSTableModel))
               return;

            IPSTableModel rsConfigModel =
               (IPSTableModel)model;
            int[] rows = table.getSelectedRows();
            if (event.getKeyCode() == KeyEvent.VK_DELETE)
            {
               if(rsConfigModel.allowRemove() && rows != null)
               {
                  rsConfigModel.removeRows(rows);
                  table.setRowSelectionInterval(rows[0], rows[rows.length-1]);
               }
            }
            else if(rsConfigModel.allowMove() && rows != null)
            {
               if(event.isControlDown() &&
                   event.getKeyCode() == KeyEvent.VK_U && rows[0] != 0)
               {
                  //destination is before the first index
                  int dest = rows[0]-1;
                  rsConfigModel.moveRow(rows[0], rows[rows.length-1], dest);
                  table.setRowSelectionInterval(dest, dest+rows.length-1);
               }
               else if(event.isControlDown() &&
                   event.getKeyCode() == KeyEvent.VK_D &&
                      rows[rows.length-1] < model.getRowCount()-1)
               {
                  //destination is after the next of last index
                  int dest = rows[rows.length-1]+2;

                  rsConfigModel.moveRow(rows[0], rows[rows.length-1], dest);

                  table.setRowSelectionInterval(dest-rows.length, dest-1);
               }
            }
          }
      };

      return mi_IPSTableModelKeyAdapter;
   }

   /**
    * Default table key adapter.
    */
   KeyAdapter mi_IPSTableModelKeyAdapter;
 }
