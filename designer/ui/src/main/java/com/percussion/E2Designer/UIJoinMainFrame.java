/* *****************************************************************************
 *
 * [ UIJoinMainFrame.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSBackEndJoin;
import com.percussion.design.objectstore.PSComponent;
import com.percussion.security.PSAuthorizationException;
import com.percussion.util.PSCollection;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.print.Book;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * This class implements the main frame for the join editor. It allows users to
 * drag and drop tables into the editor and drag and drop between the tables to
 * joining columns.
 */
class UIJoinMainFrame extends PSEditorDialog
   implements
      ActionListener,
      MenuListener,
      DropTargetListener,
      Bookable
{

   public UIJoinMainFrame() throws PSServerException, PSAuthorizationException
   {
      super();

      try
      {
         setResizable(true);
         setDefaultCloseOperation(this.HIDE_ON_CLOSE);

         // setup menu
         setJMenuBar(new OptionsMenuBar(this));

         // arbitrarily chosen size
         Dimension d = new Dimension(2000, 2000);

         // create and initialize the content pane
         m_ContentPane = new JDesktopPane();
         m_ContentPane.setPreferredSize(d);
         m_ContentPane.setSize(d);
         m_ContentPane.setBackground(Color.white);
         m_ContentPane.setVisible(true);
         m_ContentPane.setOpaque(true);
         m_ContentPane.setLocation(0, 0);
         m_ContentPane.putClientProperty("JDesktopPane.dragMode", "outline");
         m_ContentPane.addMouseListener(new JoinFrameMouseAdapter());
         m_ContentPane
            .addMouseMotionListener(new JoinFrameMouseMotionAdapter());
         m_dndTarget = new DropTarget(m_ContentPane,
            DnDConstants.ACTION_COPY_OR_MOVE, this);

         // create and initialize the table pane
         m_TablePane = new JLayeredPane();
         m_TablePane.setBackground(Color.black);
         m_TablePane.setPreferredSize(d);
         m_TablePane.setOpaque(true);
         m_TablePane.setVisible(true);
         m_TablePane.add(m_ContentPane, JLayeredPane.DEFAULT_LAYER);

         JScrollPane content = new JScrollPane(m_TablePane);
         content
            .setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
         setContentPane(content);

         // set the frame size based on the previous session
         Dimension size = new Dimension();
         Point pos = new Point();
         int[] windowPos = UserConfig.getConfig().getIntArray(JOIN_WINDOW_POS);
         /*
          * Make window ~60% of screen. This will create a good sized window
          * without overlaying any toolbars/taskbar (for most cases). Then
          * center the screen.
          */
         Dimension screenSize = new Dimension(Toolkit.getDefaultToolkit()
            .getScreenSize());
         size.setSize((screenSize.width * 3 / 5), screenSize.height * 3 / 5);
         pos.setLocation((screenSize.width / 10), screenSize.height / 10);
         setSize(size);
         setLocation(pos);

         if (m_stack == null)
            m_stack = new Vector<PSComponent>();

      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

   }

   public UIJoinMainFrame(Window parent) throws PSServerException, PSAuthorizationException
   {
      super(parent);

      try
      {
         setResizable(true);
         setDefaultCloseOperation(this.HIDE_ON_CLOSE);

         // setup menu
         setJMenuBar(new OptionsMenuBar(this));

         // arbitrarily chosen size
         Dimension d = new Dimension(2000, 2000);

         // create and initialize the content pane
         m_ContentPane = new JDesktopPane();
         m_ContentPane.setPreferredSize(d);
         m_ContentPane.setSize(d);
         m_ContentPane.setBackground(Color.white);
         m_ContentPane.setVisible(true);
         m_ContentPane.setOpaque(true);
         m_ContentPane.setLocation(0, 0);
         m_ContentPane.putClientProperty("JDesktopPane.dragMode", "outline");
         m_ContentPane.addMouseListener(new JoinFrameMouseAdapter());
         m_ContentPane
                 .addMouseMotionListener(new JoinFrameMouseMotionAdapter());
         m_dndTarget = new DropTarget(m_ContentPane,
                 DnDConstants.ACTION_COPY_OR_MOVE, this);

         // create and initialize the table pane
         m_TablePane = new JLayeredPane();
         m_TablePane.setBackground(Color.black);
         m_TablePane.setPreferredSize(d);
         m_TablePane.setOpaque(true);
         m_TablePane.setVisible(true);
         m_TablePane.add(m_ContentPane, JLayeredPane.DEFAULT_LAYER);

         JScrollPane content = new JScrollPane(m_TablePane);
         content
                 .setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
         setContentPane(content);

         // set the frame size based on the previous session
         Dimension size = new Dimension();
         Point pos = new Point();
         int[] windowPos = UserConfig.getConfig().getIntArray(JOIN_WINDOW_POS);
         /*
          * Make window ~60% of screen. This will create a good sized window
          * without overlaying any toolbars/taskbar (for most cases). Then
          * center the screen.
          */
         Dimension screenSize = new Dimension(Toolkit.getDefaultToolkit()
                 .getScreenSize());
         size.setSize((screenSize.width * 3 / 5), screenSize.height * 3 / 5);
         pos.setLocation((screenSize.width / 10), screenSize.height / 10);
         setSize(size);
         setLocation(pos);

         if (m_stack == null)
            m_stack = new Vector<PSComponent>();

      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

   }

   /*
    * (non-Javadoc)
    * 
    * @see java.awt.Window#processWindowEvent(java.awt.event.WindowEvent)
    */
   protected void processWindowEvent(WindowEvent event)
   {
      if (event.getID() == WindowEvent.WINDOW_CLOSING)
      {
         if (!m_bClosing)
         {
            if (JOptionPane.showConfirmDialog(UIJoinMainFrame.this,
               getResources().getString("savequery"), getResources().getString(
                  "savequerytitle"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
            {
               if (!save())
                  return;
            }

            saveWindowPosition();
            UIJoinMainFrame.this.dispose();
         }
      }
   }

   /* (non-Javadoc)
    * @see com.percussion.E2Designer.PSDialog#onCancel()
    */
   @Override
   public void onCancel()
   {
      int result = JOptionPane.showConfirmDialog(UIJoinMainFrame.this,
         getResources().getString("savequery"), getResources().getString(
         "savequerytitle"), JOptionPane.YES_NO_CANCEL_OPTION);
      
      if (result == JOptionPane.CANCEL_OPTION)
         return;
      else if (result == JOptionPane.OK_OPTION)
      {
         if (!save())
            return;
      }
      
      super.onCancel();
   }
   
   /**
    * Mouse adapter to clear selection and draw xor box to select multi objects
    */
   class JoinFrameMouseAdapter extends MouseAdapter
   {
      JoinFrameMouseAdapter()
      {

      }

      /**
       * Clear selection if they did not click on anything, the join object will
       * get this message if they select it
       */
      public void mousePressed(MouseEvent e)
      {
         clearSelection();
         m_ptXorBegin = e.getPoint();
         m_ptXorEnd = null;
         m_rectXor = null;
      }

      /**
       * Now select all in our selection rect
       */
      public void mouseReleased(MouseEvent e)
      {
         if (m_ptXorBegin != null && m_ptXorEnd != null && m_rectXor != null)
         {
            Graphics g = m_ContentPane.getGraphics();
            g.setColor(Color.black);
            g.setXORMode(Color.white);

            //undraw the last one
            g.drawRect(m_rectXor.x, m_rectXor.y, (int) m_rectXor.getWidth(),
               (int) m_rectXor.getHeight());

            selectWithin(m_rectXor);
         }

         m_ptXorBegin = null;
         m_ptXorEnd = null;
         m_rectXor = null;
      }
   }

   class JoinFrameMouseMotionAdapter extends MouseMotionAdapter
   {
      JoinFrameMouseMotionAdapter()
      {

      }

      /**
       * Draw xor box while dragging
       */
      public void mouseDragged(MouseEvent e)
      {
         if (m_ptXorBegin != null)
         {
            Graphics g = m_ContentPane.getGraphics();
            g.setColor(Color.black);
            g.setXORMode(Color.white);

            //undraw the last one
            if (m_rectXor != null)
            {
               g.drawRect(m_rectXor.x, m_rectXor.y, m_rectXor.width,
                  m_rectXor.height);
            }

            //now draw new one
            m_ptXorEnd = e.getPoint();
            int x, y, width, height;

            if (m_ptXorBegin.x < m_ptXorEnd.x)
            {
               x = m_ptXorBegin.x;
               width = m_ptXorEnd.x - m_ptXorBegin.x;
            }
            else
            {
               x = m_ptXorEnd.x;
               width = m_ptXorBegin.x - m_ptXorEnd.x;
            }
            if (m_ptXorBegin.y < m_ptXorEnd.y)
            {
               y = m_ptXorBegin.y;
               height = m_ptXorEnd.y - m_ptXorBegin.y;
            }
            else
            {
               y = m_ptXorEnd.y;
               height = m_ptXorBegin.y - m_ptXorEnd.y;
            }

            m_rectXor = new Rectangle(x, y, width, height);
            g.drawRect(m_rectXor.x, m_rectXor.y, (int) m_rectXor.getWidth(),
               (int) m_rectXor.getHeight());
         }
      }
   }

   /**
    * Options menu bar implementation
    */
   class OptionsMenuBar extends JMenuBar
   {
      public OptionsMenuBar(UIJoinMainFrame listener)
      {
         JMenuItem properties = new JMenuItem(getResources().getString(
            "Properties"));
         properties.addActionListener(listener);
         JMenuItem save = new JMenuItem(getResources().getString(
            "CloseandSaveChanges"));
         save.addActionListener(listener);
         JMenuItem discard = new JMenuItem(getResources().getString(
            "CloseandDiscardChanges"));
         discard.addActionListener(listener);

         JMenu optionsMenu = new JMenu(getResources().getString("Options"));

         optionsMenu.add(save);
         optionsMenu.add(discard);

         JMenuItem table = new JMenuItem(getResources().getString("Table"));
         table.addActionListener(listener);

         JMenu optionsMenu2 = new JMenu(getResources().getString("Insert"));
         optionsMenu2.add(table);

         JMenu optionsMenu3 = new JMenu(getResources().getString("Edit"));
         JMenuItem delete = new JMenuItem(getResources().getString("Clear"));
         delete.addActionListener(listener);
         /*
          * JLS: fix for Rx-04-12-0003 Cannot detect delete key event here, as
          * adding accelerator for this causes erroneous delete key events to be
          * generated by the UIMainFrame as well, likely due to the jdk 1.4 bug
          * (ID 4917669 in bugs.sun.com) - added code to
          * TableInternalFrame.setTheList() to handle the delete key event.
          * Leaving next line commented in case we move to jdk 1.5 and it fixes
          * the problem.
          */
//        delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
         JMenuItem copy = new JMenuItem(getResources().getString("Copy"));
         copy.addActionListener(listener);
         copy.setAccelerator(KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_C, java.awt.Event.CTRL_MASK, true));

         JMenuItem cut = new JMenuItem(getResources().getString("Cut"));
         cut.addActionListener(listener);
         cut.setAccelerator(KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_X, java.awt.Event.CTRL_MASK, true));

         JMenuItem paste = new JMenuItem(getResources().getString("Paste"));
         paste.addActionListener(listener);
         paste.setAccelerator(KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_V, java.awt.Event.CTRL_MASK, true));

         JMenuItem invert = new JMenuItem(getResources().getString("Invert"));
         invert.addActionListener(listener);

         optionsMenu3.add(delete);
         optionsMenu3.add(copy);
         optionsMenu3.add(cut);
         optionsMenu3.add(paste);
         optionsMenu3.addSeparator();
         optionsMenu3.add(invert);
         optionsMenu3.addSeparator();
         optionsMenu3.add(properties);
         optionsMenu3.addMenuListener(listener);

         m_editPopup = new JPopupMenu();
         JMenuItem delete2 = new JMenuItem(getResources().getString("Clear"));
         delete2.addActionListener(listener);
         JMenuItem properties2 = new JMenuItem(getResources().getString(
            "Properties"));
         properties2.addActionListener(listener);
         JMenuItem copy2 = new JMenuItem(getResources().getString("Copy"));
         copy2.addActionListener(listener);
         JMenuItem cut2 = new JMenuItem(getResources().getString("Cut"));
         cut2.addActionListener(listener);
         m_paste = new JMenuItem(getResources().getString("Paste"));
         m_paste.addActionListener(listener);
         JMenuItem invert2 = new JMenuItem(getResources().getString("Invert"));
         invert2.addActionListener(listener);

         m_editPopup.add(delete2);
         m_editPopup.add(copy2);
         m_editPopup.add(cut2);
         m_editPopup.add(m_paste);
         m_editPopup.addSeparator();
         m_editPopup.add(invert2);
         m_editPopup.addSeparator();
         m_editPopup.add(properties2);

         m_helpPopup = new JMenu(E2Designer.getResources().getString(
            "menuHelp"));

         JMenuItem h = new JMenuItem(E2Designer.getResources().getString(
            "menuHelp"));
         h.addActionListener(listener);
         m_helpPopup.add(h);

         add(optionsMenu);
         add(optionsMenu3);
         add(optionsMenu2);
         add(m_helpPopup);

      }
   }

   /**
    * Implement all menu actions
    *  
    */
   public void actionPerformed(ActionEvent e)
   {
      try
      {
         if (e.getActionCommand().equals(E2Designer.getResources().getString(
            "menuHelp")))
         {
            onHelp();
         }

         if (e.getActionCommand().equals(getResources().getString("Properties")))
         {
            editProperties();
         }
         else if (e.getActionCommand().equals(getResources().getString(
            "CloseandSaveChanges")))
         {
            if (save())
               close();
         }
         else if (e.getActionCommand().equals(getResources().getString(
            "CloseandDiscardChanges")))
         {
            close();
         }
         else if (e.getActionCommand().equals(getResources().getString("Table")))
         {
            OSBackendTable tableData = new OSBackendTable();
            BackendTankPropertyDialog initDialog = new BackendTankPropertyDialog(
               this);
            initDialog.setTables(getTablesExceptFor(null));
            UIFigure fake = new UIFigure("Fake", tableData, "fake", 0);
            // only add table if user clicked ok in the dialog
            if (initDialog.onEdit(fake, tableData))
               addTable(fake);
         }
         else if (e.getActionCommand().equals(getResources().getString("Clear")))
         {
            deleteSelection();
         }
         else if (e.getActionCommand().equals(getResources().getString("Cut")))
         {
            pushSelection(true);
         }
         else if (e.getActionCommand().equals(getResources().getString("Copy")))
         {
            pushSelection(false);
         }
         else if (e.getActionCommand().equals(getResources().getString("Paste")))
         {
            popSelection();
         }
         else if (e.getActionCommand().equals(getResources().getString("Invert")))
         {
            if (getNumSelected(true, true) == 1)
            {
               Component[] comps = m_ContentPane.getComponents();
               for (int index = comps.length - 1; index >= 0; index--)
               {
                  if (comps[index] instanceof UIJoinConnector)
                  {
                     UIJoinConnector uic = (UIJoinConnector) comps[index];
                     if (uic.isSelected())
                     {
                        uic.invert();
                        break;
                     }
                  }
               }
            }
         }
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
   }

   // Operations
   /**
    * This function is used to retrieve the JoinFigureFrame so that we can
    * create tables and joins.
    * 
    * @Returns the figure factory of the join editor.
    */
   protected FigureFactory getFigureFactory()

   {
      // this string is internal and doesn't need to be internationalized
      return FigureFactoryManager.getFactoryManager().getFactory(
         sFIGURE_FACTORY);
   }

   /**
    * This function adds a join between 2 tables and is called after the user
    * drags and drops between 2 tables.
    * 
    * @param leftFrame the left joining table
    * @param leftColumn the left column index
    * @param rightFrame the right joining table
    * @param rightColumn the right column index
    */
   public void addJoin(TableInternalFrame leftFrame, int leftColumn,
      TableInternalFrame rightFrame, int rightColumn)
   {
      addJoin(leftFrame, leftColumn, rightFrame, rightColumn, null);
   }

   public void addJoin(TableInternalFrame leftFrame, int leftColumn,
      TableInternalFrame rightFrame, int rightColumn, OSBackendJoin joinData)
   {
      try
      {
         //create the join
         UIJoinConnector uic = (UIJoinConnector) getFigureFactory()
            .createFigure(JoinFigureFactory.JOIN_DIRECTED_CONNECTION);
         uic.setParentFrame(this);
         uic.setGlassPane(m_ContentPane);
         uic.setLeftJoin(leftFrame, leftColumn);
         uic.setRightJoin(rightFrame, rightColumn);
         Point ptScrollOffset = ((JScrollPane) getContentPane()).getViewport()
            .getViewPosition();
         uic.setLocation(ptScrollOffset.x + 20, ptScrollOffset.y + 20);
         uic.setVisible(true);
         uic.calcSize(true);

         m_ContentPane.add(uic, 700);
         m_ContentPane.repaint(uic.getBounds());

         uic.calcSize();
         uic.repaint();

         if (joinData != null)
         {
            uic.setData(joinData);
         }
         else
         {
            //need to set left and right columns
            OSBackendTable leftTableData = (OSBackendTable) leftFrame.getData();
            OSBackendTable rightTableData = 
               (OSBackendTable) rightFrame.getData();
            PSBackEndColumn leftColumnData = null;
            PSBackEndColumn rightColumnData = null;

            if (leftColumn < leftFrame.getListbox().getModel().getSize())
               leftColumnData = new PSBackEndColumn(leftTableData, 
                  leftFrame.getListbox().getModel().getElementAt(
                     leftColumn).toString());

            if (rightColumn < rightFrame.getListbox().getModel().getSize())
               rightColumnData = new PSBackEndColumn(rightTableData, 
                  rightFrame.getListbox().getModel().getElementAt(
                     rightColumn).toString());

            joinData = (OSBackendJoin) uic.getData();
            joinData.setLeftColumn(leftColumnData);
            joinData.setRightColumn(rightColumnData);
         }
      }
      catch (FigureCreationException ex)
      {
         ex.printStackTrace();
      }
      catch (IllegalArgumentException pex)
      {
         pex.printStackTrace();
      }
   }

   /**
    * Finds the connectable figure in the drawing pane that is the highest in
    * the z-order that contains the passed in point and returns it. If none is
    * found, null is returned. If a point is over a part of a figure in which
    * isHit returns <code>false</code>, the next figure in the z-order is
    * tested.
    */
   public UIJoinConnector getConnectable(Point pt)
   {
      Vector comps = Util.findComponentsAt(m_ContentPane, pt);
      boolean bFound = false;
      Component c = null;
      int size = comps.size();
      if (0 == size)
         return null;

      Debug.assertTrue(comps.get(size - 1) == m_ContentPane, 
         E2Designer.getResources(), "MissingParent", null);

      //first check for a connector that wants the mouse
      for (int index = 0; index < size - 1 && !bFound; index++)
      {
         c = (Component) comps.get(index);
         if (c instanceof UIConnectableFigure
            && ((UIJoinConnector) c).isHit(SwingUtilities.convertPoint(
               m_ContentPane, pt, c))
            && ((UIJoinConnector) c).wantsMouse(SwingUtilities.convertPoint(
               m_ContentPane, pt, c)))
         {
            bFound = true;
         }
      }

      //now if nobody wants the mouse find the first that is in the click
      for (int index = 0; index < size - 1 && !bFound; index++)
      {
         c = (Component) comps.get(index);
         if (c instanceof UIConnectableFigure
            && ((UIJoinConnector) c).isHit(SwingUtilities.convertPoint(
               m_ContentPane, pt, c)))
         {
            bFound = true;
         }
      }
      return (UIJoinConnector) c;
   }

   /**
    * Adds the specified figure to the current selection set. This method is
    * provided to give programatic access for debugging purposes. Most selection
    * set modifications will happen via mouse interactions from the user.
    * 
    * @param uic any figure in the window.
    */
   void addToSelection(UIConnectableFigure uic, boolean bAppend)

   {
      if (!bAppend)
         clearSelection();

      if (uic.isSelected())
      {
         // toggle the selection state
         uic.setSelection(false, bAppend);
      }
      else
      {
         uic.setSelection(true, bAppend);
         uic.paintSelectionIndicator(uic.getGraphics());
      }
   }

   /**
    * Clears all of the selected objects to unselected
    */
   public void clearSelection()
   {
      clearSelection(null);
   }

   /**
    * Use this member to see if only one object is selected
    * 
    * @Returns true if one is selected
    */
   public int getNumSelected()
   {
      return getNumSelected(false, false);
   }

   public int getNumSelected(boolean bJoinOnly, boolean bOnlyWithoutFormula)
   {
      int iSelected = 0;
      Component[] comps = m_ContentPane.getComponents();
      for (int index = comps.length - 1; index >= 0; index--)
      {
         if (comps[index] instanceof UIConnectableFigure)
         {
            UIJoinConnector uic = (UIJoinConnector) comps[index];
            if (bOnlyWithoutFormula && uic.hasFormula())
               continue;

            if (uic.isSelected())
               ++iSelected;
         }

         else if (comps[index] instanceof TableInternalFrame)
         {
            TableInternalFrame uic = (TableInternalFrame) comps[index];
            if (uic.isSelected() && !bJoinOnly)
               ++iSelected;
         }
      }

      return iSelected;
   }

   /**
    * Clears all of the selected objects to unselected, then repaints.
    * 
    * @param exceptThis allows you to skip clearing one component
    */
   public void clearSelection(Component exceptThis)
   {
      try
      {
         Component[] comps = m_ContentPane.getComponents();
         for (int index = comps.length - 1; index >= 0; index--)
         {
            if (exceptThis != null && exceptThis == comps[index])
               continue;

            if (comps[index] instanceof UIConnectableFigure)
            {
               UIConnectableFigure uic = (UIConnectableFigure) comps[index];
               // always clear selection so it gets propagated to children
               uic.setSelection(false, false);
            }
            else if (comps[index] instanceof TableInternalFrame)
            {
               TableInternalFrame uic = (TableInternalFrame) comps[index];
               uic.setSelected(false);
            }
         }
      }
      catch (PropertyVetoException p)
      {
         p.printStackTrace();
      }
      repaint();
   }

   public boolean IsTableOnView(OSBackendTable ptableData)
   {
      Component[] comps = m_ContentPane.getComponents();
      boolean bReturn = false;
      for (int index = comps.length - 1; index >= 0; index--)
      {
         if (comps[index] instanceof TableInternalFrame)
         {
            TableInternalFrame uic = (TableInternalFrame) comps[index];
            if (uic.getData() instanceof OSBackendTable)
            {
               OSBackendTable tableData = (OSBackendTable) uic.getData();
               if (tableData.getAlias().equals(ptableData.getAlias()))
               {
                  if (tableData.isSameDatasource(ptableData))
                  {
                     bReturn = true;
                     break;
                  }
               }
            }
         }
      }
      return (bReturn);
   }

   /**
    * Take selected objects and put them on the stack
    */
   public void pushSelection(boolean bRemove)
   {
      m_stack.clear();
      Component[] comps = m_ContentPane.getComponents();
      for (int index = comps.length - 1; index >= 0; index--)
      {
         if (comps[index] instanceof UIJoinConnector)
         {
            UIJoinConnector uic = (UIJoinConnector) comps[index];
            if (uic.isSelected() && uic.getData() != null
               && uic.getData() instanceof OSBackendJoin)
            {
               OSBackendJoin jn = (OSBackendJoin) uic.getData();
               jn.setFigure(uic);
               m_stack.add(jn);

               if (bRemove)
               {
                  if (uic.getArrow() != null)
                     m_ContentPane.remove(uic.getArrow());

                  if (uic.getHand() != null)
                     m_ContentPane.remove(uic.getHand());

                  if (uic.getFormulaLabel() != null)
                     m_ContentPane.remove(uic.getFormulaLabel());

                  m_ContentPane.remove(uic);
                  m_ContentPane.repaint();
               }
            }
         }
         else if (comps[index] instanceof TableInternalFrame)
         {
            TableInternalFrame uic = (TableInternalFrame) comps[index];
            if (uic.isSelected() && uic.getData() != null
               && uic.getData() instanceof OSBackendTable)
            {
               OSBackendTable tbl = (OSBackendTable) uic.getData();
               tbl.setLocation(uic.getLocation());
               tbl.setSize(uic.getSize());
               m_stack.add(tbl);
               if (bRemove)
               {
                  m_ContentPane.remove(uic);
                  m_ContentPane.repaint();
               }
            }
         }
      }
   }

   /**
    * Take stack objects and add them here
    */
   public void popSelection()
   {
      //first do tables
      for (int iObject = 0; iObject < m_stack.size(); ++iObject)
      {
         if (m_stack.get(iObject) instanceof OSBackendTable)
         {
            OSBackendTable tableData = (OSBackendTable) m_stack.get(iObject);
            // check if is the same table that was copy/cut before
            if (IsTableOnView(tableData) == true)
            {
               JOptionPane.showMessageDialog(this, "can not add same table");
               m_stack.remove(iObject);
               continue;
            }
            UIFigure fake = new UIFigure("Fake", tableData, "fake", 0);
            TableInternalFrame frame = addTable(fake);

            if (tableData.getLocation() != null && tableData.getSize() != null)
            {
               // move the new table ten pixels down and right
               Point pt = tableData.getLocation();
               pt.y = (int) pt.getY() + 10;
               pt.x = (int) pt.getX() + 10;
               frame.setLocation(pt);
               frame.setSize(tableData.getSize());
            }
         }
      }
      //then joins
      for (int iObject = 0; iObject < m_stack.size(); ++iObject)
      {
         if (m_stack.get(iObject) instanceof OSBackendJoin)
         {
            OSBackendJoin joinData = (OSBackendJoin) m_stack.get(iObject);
            PSBackEndColumn leftColumn = joinData.getLeftColumn();
            PSBackEndColumn rightColumn = joinData.getRightColumn();

            if (leftColumn != null && rightColumn != null)
            {
               if (leftColumn.getTable() != null
                  && rightColumn.getTable() != null)
               {
                  String leftTableName = leftColumn.getTable().getAlias();
                  String rightTableName = rightColumn.getTable().getAlias();
                  if (leftTableName != null && rightTableName != null)
                  {
                     TableInternalFrame leftFrame = getTableFrame(leftTableName);
                     TableInternalFrame rightFrame = getTableFrame(rightTableName);

                     //find the columns
                     String strLeftColumnName = leftColumn.getColumn();
                     String strRightColumnName = rightColumn.getColumn();

                     if (leftFrame != null && rightFrame != null
                        && strLeftColumnName != null
                        && strRightColumnName != null)
                     {
                        int iLeftColumn = -1;
                        int iRightColumn = -1;
                        for (iLeftColumn = 0; iLeftColumn < leftFrame
                           .getListbox().getModel().getSize(); ++iLeftColumn)
                        {
                           if (strLeftColumnName.equals(leftFrame.getListbox()
                              .getModel().getElementAt(iLeftColumn).toString()))
                              break;
                        }

                        for (iRightColumn = 0; iRightColumn < rightFrame
                           .getListbox().getModel().getSize(); ++iRightColumn)
                        {
                           if (strRightColumnName.equals(rightFrame
                              .getListbox().getModel().getElementAt(
                                 iRightColumn).toString()))
                              break;
                        }

                        addJoin(leftFrame, iLeftColumn, rightFrame,
                           iRightColumn, joinData);
                     }
                  }
               }
            }
         }
      }

      m_stack.clear();
   }

   /**
    * Delete the provided join (connector)
    */
   private void deleteJoin(UIJoinConnector connector)
   {
      if (connector.getArrow() != null)
         m_ContentPane.remove(connector.getArrow());

      if (connector.getFormulaLabel() != null)
         m_ContentPane.remove(connector.getFormulaLabel());

      if (connector.getHand() != null)
         m_ContentPane.remove(connector.getHand());

      m_ContentPane.remove(connector);
   }

   /**
    * Delete all objects that are selected
    */
   public void deleteSelection()
   {
      Component[] comps = m_ContentPane.getComponents();
      for (int index = comps.length - 1; index >= 0; index--)
      {
         if (comps[index] instanceof UIJoinConnector)
         {
            UIJoinConnector uic = (UIJoinConnector) comps[index];
            if (uic.isSelected())
               deleteJoin(uic);
         }
         else if (comps[index] instanceof TableInternalFrame)
         {
            TableInternalFrame uic = (TableInternalFrame) comps[index];
            if (uic.isSelected())
            {
               // first remove all joins
               Component[] currentComps = m_ContentPane.getComponents();
               for (int i = 0; i < currentComps.length; i++)
               {
                  if (currentComps[i] instanceof UIJoinConnector)
                  {
                     UIJoinConnector connector = (UIJoinConnector) currentComps[i];
                     if (uic.equals(connector.getLeftFrame())
                        || uic.equals(connector.getRightFrame()))
                        deleteJoin(connector);
                  }
               }

               // then remove the table
               m_ContentPane.remove(uic);
            }
         }

         m_ContentPane.repaint();
      }
   }

   /**
    * Get table frame by name
    */
   public TableInternalFrame getTableFrame(String strName)
   {
      Component[] comps = m_ContentPane.getComponents();
      for (int index = comps.length - 1; index >= 0; index--)
      {
         if (comps[index] instanceof TableInternalFrame)
         {
            TableInternalFrame uic = (TableInternalFrame) comps[index];
            if (uic.getData() instanceof OSBackendTable)
            {
               OSBackendTable tableData = (OSBackendTable) uic.getData();
               if (tableData.getAlias().equals(strName))
                  return uic;
            }
         }
      }

      return null;
   }

   /**
    * Select all objects within this rectangle, then repaints them.
    */
   public void selectWithin(Rectangle selectRect)
   {
      try
      {
         Component[] comps = m_ContentPane.getComponents();
         for (int index = comps.length - 1; index >= 0; index--)
         {
            if (comps[index] instanceof UIConnectableFigure)
            {
               UIConnectableFigure jcomp = (UIConnectableFigure) comps[index];
               if (selectRect.contains(jcomp.getLocation()))
                  jcomp.setSelection(true, true);
            }

            if (comps[index] instanceof TableInternalFrame)
            {
               TableInternalFrame jcomp = (TableInternalFrame) comps[index];
               if (selectRect.contains(jcomp.getLocation()))
                  jcomp.setSelected(true);
            }
         }
      }
      catch (PropertyVetoException p)
      {
         p.printStackTrace();
      }
      repaint();
   }

   /**
    * IEditor interface methods
    */

   /**
    * This method is called when a data object needs to be visually edited.
    * 
    * @returns if the dialog is modal, returns <code>true</code> if the state
    * of the object was changed by the user, <code>false</code> otherwise. If
    * the editor is not modal, the return value is undefined.
    * 
    * @param figure the screen object that contains the data that this editor
    * knows how to deal with. The actual data object can be obtained from the
    * figure by calling getData() on it.
    * 
    * @param data the data object of the frame containing the figure supplied as
    * the first parameter. This object should not be modified.
    */
   public boolean onEdit(UIFigure figure, final Object data)
   {
      m_figure = figure;
      if (figure.getData() instanceof OSBackendDatatank)
      {
         //get the data for this editor
         m_theData = (OSBackendDatatank) figure.getData();

         addTheTablesAndJoins();
         if (data instanceof PSApplication)
         {
            PSApplication app = (PSApplication) data;
            String title = new String();
            if (figure.getParent() instanceof UIConnectableFigure)
            {
               UIConnectableFigure fig = 
                  (UIConnectableFigure) figure.getParent();
               OSDataset dataset = null;
               if (fig.getData() instanceof OSQueryPipe)
               {
                  OSQueryPipe pipe = (OSQueryPipe) fig.getData();
                  dataset = pipe.getDataset();
                  title = dataset.getRequestor().getRequestPage();
               }
            }

            setTitle(title + " (" + app.getName() + ") "
               + getResources().getString("title"));
            m_TheApp = app;
         }
         else
         {
            m_TheApp = null;
         }
         setVisible(true);
      }
      else
         setVisible(false);

      if (data instanceof PSApplication)
         m_app = (PSApplication) data;

      return false;
   }

   /**
    * Adds the tables and joins from the data to the editor
    */
   private void addTheTablesAndJoins()
   {
      try
      {
         m_ContentPane.removeAll();

         //add the tables
         if (m_theData != null && m_theData.getTables() != null)
         {

            Point oldPT = new Point(20, 20);

            for (int iTables = 0; iTables < m_theData.getTables().size(); iTables++)
            {
               OSBackendTable tableData = (OSBackendTable) m_theData
                  .getTables().get(iTables);

               if (tableData != null)
               {
                  UIFigure fake = new UIFigure("Fake", tableData, "fake", 0);
                  TableInternalFrame frame = addTable(fake);

                  if (tableData.getLocation() != null
                     && tableData.getSize() != null)
                  {
                     Point pt = tableData.getLocation();

                     if (pt.equals(oldPT) && iTables != 0)
                     {
                        pt = findNewPosition(pt);
                        tableData.setLocation(pt);
                     }

                     frame.setLocation(pt);
                     frame.setSize(tableData.getSize());
                  }
               }
            }
         }

         //add the joins
         if (m_theData != null && m_theData.getJoins() != null)
         {
            for (int iJoin = 0; iJoin < m_theData.getJoins().size(); ++iJoin)
            {
               OSBackendJoin joinData = null;
               if (m_theData.getJoins().get(iJoin) instanceof OSBackendJoin)
                  joinData = (OSBackendJoin) m_theData.getJoins().get(iJoin);
               else
                  joinData = new OSBackendJoin((PSBackEndJoin) m_theData
                     .getJoins().get(iJoin));

               Object tempData = m_theData.getJoins().get(iJoin);
               if (joinData != null)
               {
                  PSBackEndColumn leftColumn = joinData.getLeftColumn();
                  PSBackEndColumn rightColumn = joinData.getRightColumn();

                  if (leftColumn != null && rightColumn != null)
                  {
                     if (leftColumn.getTable() != null
                        && rightColumn.getTable() != null)
                     {
                        String leftTableName = leftColumn.getTable().getAlias();
                        String rightTableName = rightColumn.getTable()
                           .getAlias();

                        if (leftTableName != null && rightTableName != null)
                        {
                           TableInternalFrame leftFrame = getTableFrame(leftTableName);
                           TableInternalFrame rightFrame = getTableFrame(rightTableName);

                           //find the columns
                           String strLeftColumnName = leftColumn.getColumn();
                           String strRightColumnName = rightColumn.getColumn();

                           if (strLeftColumnName != null
                              && strRightColumnName != null)
                           {
                              int iLeftColumn = -1;
                              int iRightColumn = -1;

                              boolean foundLeft = false;
                              for (iLeftColumn = 0; iLeftColumn < leftFrame
                                 .getListbox().getModel().getSize(); ++iLeftColumn)
                              {
                                 if (strLeftColumnName.equals(leftFrame
                                    .getListbox().getModel().getElementAt(
                                       iLeftColumn).toString()))
                                 {
                                    foundLeft = true;
                                    break;
                                 }
                              }

                              boolean foundRight = false;
                              for (iRightColumn = 0; iRightColumn < rightFrame
                                 .getListbox().getModel().getSize(); ++iRightColumn)
                              {
                                 if (strRightColumnName.equals(rightFrame
                                    .getListbox().getModel().getElementAt(
                                       iRightColumn).toString()))
                                 {
                                    foundRight = true;
                                    break;
                                 }
                              }

                              /*
                               * we did not find a column to create the joins,
                               * warn the user that he might need to change
                               * that.
                               */
                              if (!foundLeft || !foundRight)
                              {
                                 String left = (String) leftFrame.getListbox()
                                    .getModel().getElementAt(0);
                                 String right = (String) rightFrame
                                    .getListbox().getModel().getElementAt(0);
                                 String catalogerFailed = E2Designer
                                    .getResources()
                                    .getString("catalogerFailed");

                                 if (!left.equals(catalogerFailed)
                                    || !right.equals(catalogerFailed))
                                 {
                                    Object[] args =
                                    {leftTableName, strLeftColumnName,
                                       rightTableName, strRightColumnName};
                                    String msg = MessageFormat.format(
                                       getResources().getString(
                                          "joinColumnNotFound"), args);
                                    JOptionPane.showMessageDialog(this, msg,
                                       E2Designer.getResources().getString(
                                          "Warning"),
                                       JOptionPane.WARNING_MESSAGE);
                                 }
                              }

                              addJoin(leftFrame, iLeftColumn, rightFrame,
                                 iRightColumn, joinData);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
      catch (IllegalArgumentException ex)
      {
         ex.printStackTrace();
      }
   }

   private Point findNewPosition(Point pt)
   {
      Point pRet = new Point(pt);
      int size = m_theData.getTables().size();
      for (int iTables = 0; iTables < size; iTables++)
      {
         OSBackendTable tableData = (OSBackendTable) m_theData.getTables().get(
            iTables);
         if (tableData != null)
         {
            Point t = tableData.getLocation();
            if (t.equals(pt))
            {
               pRet.setLocation(t);
               pRet.x += 20;
               pRet.y += 20;
               while (anyTableAtLocation(pRet))
               {
                  pRet.x += 20;
                  pRet.y += 20;
               }
               break;
            }
         }

      }
      return (pRet);
   }

   private boolean anyTableAtLocation(Point pt)
   {
      boolean bRet = false;

      int size = m_theData.getTables().size();
      for (int count = 0; count < size; count++)
      {
         OSBackendTable tbl = (OSBackendTable) m_theData.getTables().get(count);
         if (tbl != null)
         {
            Point t = tbl.getLocation();
            if (t.equals(pt))
            {
               bRet = true;
               break;
            }
         }
      }
      return (bRet);
   }

   /**
    * Adds the UI table based on the table data
    */
   private TableInternalFrame addTable(UIFigure fake)
   {
      return addTable(fake, 0);
   }

   private TableInternalFrame addTable(UIFigure fake, int index)
   {
      TableInternalFrame m_tableFrame = null;
      try
      {
         Vector columns = new Vector();
         if (fake.getData() != null && fake.getData() instanceof OSBackendTable)
         {
            OSBackendTable tableData = (OSBackendTable) fake.getData();
            columns = tableData.getColumns();
         }

         m_tableFrame = new TableInternalFrame(fake, columns, this);
         m_tableFrame.setBounds(0, 0, 150, 200);
         m_tableFrame.setVisible(true);
         m_tableFrame.setTheGlassPane(m_ContentPane);
         m_ContentPane.add(m_tableFrame, index);
      }
      catch (PropertyVetoException ex)
      {
         ex.printStackTrace();
      }

      return m_tableFrame;
   }

   /**
    * Save our tables and joins to the data
    * 
    * @Returns false if validation fails
    */
   public boolean save()
   {
      try
      {
         if (m_theData != null)
         {

            Vector tables = new Vector(5);
            PSCollection joins = new PSCollection(
               "com.percussion.design.objectstore.PSBackEndJoin");

            Component[] comps = m_ContentPane.getComponents();
            //check the joins
            for (int index = 0; index < comps.length; index++)
            {
               if (comps[index] instanceof UIJoinConnector)
               {
                  UIJoinConnector jcomp = (UIJoinConnector) comps[index];

                  TableInternalFrame left = jcomp.getLeftFrame();
                  TableInternalFrame right = jcomp.getRightFrame();
                  // see if we have a complete set
                  if (right == null || left == null)
                  {
                     JOptionPane.showMessageDialog(this, getResources()
                        .getString("bothsideserr"), getResources().getString(
                        "saveerrtitle"), JOptionPane.ERROR_MESSAGE);

                     return false;
                  }

                  if (right == left) // pointing to the same table?
                  {
                     JOptionPane.showMessageDialog(this, getResources()
                        .getString("sidessameerr"), getResources().getString(
                        "saveerrtitle"), JOptionPane.ERROR_MESSAGE);

                     return false;
                  }
                  // the table that they pointed still on the view?
                  if (IsTableOnView((OSBackendTable) right.getData()) == false
                     || IsTableOnView((OSBackendTable) left.getData()) == false)
                  {
                     comps[index] = null; // no delete this join
                  }
               }
            }

            for (int index = 0; index < comps.length; index++)
            {
               if (comps[index] instanceof UIJoinConnector)
               {
                  UIJoinConnector jcomp = (UIJoinConnector) comps[index];
                  OSBackendJoin jn = (OSBackendJoin) jcomp.getData();
                  jn.release();

                  //need to set left and right columns
                  TableInternalFrame leftFrame = jcomp.getLeftFrame();
                  TableInternalFrame rightFrame = jcomp.getRightFrame();
                  int leftColumn = jcomp.getLeftColumn();
                  int rightColumn = jcomp.getRightColumn();
                  if (leftFrame != null && rightFrame != null
                     && leftFrame.getData() instanceof OSBackendTable
                     && rightFrame.getData() instanceof OSBackendTable)
                  {
                     OSBackendTable leftTableData = (OSBackendTable) leftFrame
                        .getData();
                     OSBackendTable rightTableData = (OSBackendTable) rightFrame
                        .getData();
                     PSBackEndColumn leftColumnData = null;
                     PSBackEndColumn rightColumnData = null;

                     if (leftColumn < leftFrame.getListbox().getModel()
                        .getSize())
                        leftColumnData = new PSBackEndColumn(leftTableData,
                           leftFrame.getListbox().getModel().getElementAt(
                              leftColumn).toString());

                     if (rightColumn < rightFrame.getListbox().getModel()
                        .getSize())
                        rightColumnData = new PSBackEndColumn(rightTableData,
                           rightFrame.getListbox().getModel().getElementAt(
                              rightColumn).toString());

                     jn.setLeftColumn(leftColumnData);
                     jn.setRightColumn(rightColumnData);
                  }

                  joins.add(jn);
               }

               if (comps[index] instanceof TableInternalFrame)
               {
                  TableInternalFrame jcomp = (TableInternalFrame) comps[index];
                  OSBackendTable tbl = (OSBackendTable) jcomp.getData();
                  tbl.setLocation(jcomp.getLocation());
                  tbl.setSize(jcomp.getSize());
                  tables.add(0, tbl);
               }
            }
            PSCollection psTables = new PSCollection(
               "com.percussion.design.objectstore.PSBackEndTable");
            for (int i = 0; i < tables.size(); i++)
               psTables.add(tables.get(i));

            m_theData.setTables(psTables);
            m_theData.setJoins(joins);

            if (m_figure != null)
               m_figure.setData(m_theData);
         }
      }
      catch (IllegalArgumentException ex)
      {
         //ex.printStackTrace();
         PSDlgUtil.showErrorDialog(
            ex.getLocalizedMessage(),
            E2Designer.getResources().getString("OpErrorTitle"));
         return false;
      }
      catch (ClassNotFoundException foundex)
      {
         foundex.printStackTrace();
      }

      return true;
   }

   /**
    * edit properties
    */
   public void editProperties()
   {
      if (getNumSelected() == 1)
      {
         Component[] comps = m_ContentPane.getComponents();
         for (int index = comps.length - 1; index >= 0; index--)
         {
            if (comps[index] instanceof UIJoinConnector)
            {
               UIJoinConnector uic = (UIJoinConnector) comps[index];
               if (uic.isSelected())
               {
                  uic.onEdit(m_TheApp);
                  return;
               }
            }

            else if (comps[index] instanceof TableInternalFrame)
            {
               TableInternalFrame uic = (TableInternalFrame) comps[index];
               if (uic.isSelected())
               {
                  uic.onEdit();
                  return;
               }
            }
         }
      }
   }

   /**
    * IEditor interface method.
    * 
    * @return always returns <code>false</code>
    */
   public boolean isModal()
   {
      return true;
   }

   /**
    * IEditor interface method.
    * 
    * @return Always returns a non-zero cookie.
    */
   public int canClose()
   {
      return 1;
   }

   public void close()
   {
      close(0, true);
   }

   /**
    * Implements IEditor interface method.
    * 
    * @param cookie Ignored
    * 
    * @param bForce Ignored
    * 
    * @return Always <code>true</code>
    */
   public boolean close(int cookie, boolean bForce)
   {

      m_bClosing = true;
      this.dispose();
      m_bClosing = false;

      return true;
   }

   /*
    * Save the new window position/size.
    */
   private void saveWindowPosition()
   {
   }

   /**
    * Uses the simple algorithm for now, always returns <code>true</code>.
    * 
    * @return Always <code>true</code>
    */
   public boolean isDataChanged()
   {
      // TODOph: implement smarter algorithm
      return true;
   }

   /**
    * Implements IEditor interface method.
    * 
    * @param context Is ignored.
    */
   public boolean saveData(Object context)
   {
      return save();
   }

   /**
    * Grey out menu items when they are not needed
    */
   public void menuSelected(MenuEvent e)
   {
      //clear
      getJMenuBar().getMenu(1).getItem(0).setEnabled(getNumSelected() > 0);
      //copy
      getJMenuBar().getMenu(1).getItem(1).setEnabled(getNumSelected() > 0);
      //cut
      getJMenuBar().getMenu(1).getItem(2).setEnabled(getNumSelected() > 0);
      //paste
      getJMenuBar().getMenu(1).getItem(3).setEnabled(m_stack.size() > 0);
      //invert
      getJMenuBar().getMenu(1).getItem(5).setEnabled(
         getNumSelected(true, true) == 1);
      //properties
      getJMenuBar().getMenu(1).getItem(7).setEnabled(getNumSelected() == 1);
   }

   public void menuDeselected(MenuEvent e)
   {

   }

   public void menuCanceled(MenuEvent e)
   {

   }

   /**
    * the popup menu to be used by the right click
    */
   public JPopupMenu getEditPopupMenu(JComponent comp)
   {
      boolean bFormula = false;
      if (comp instanceof UIJoinConnector)
      {
         UIJoinConnector join = (UIJoinConnector) comp;
         bFormula = join.hasFormula();
      }

      if (null == m_stack || 0 == m_stack.size())
         m_paste.setEnabled(false);
      else
         m_paste.setEnabled(true);

      m_editPopup.getComponent(5).setEnabled(
         (comp instanceof UIJoinConnector && !bFormula) ? true : false);

      return m_editPopup;
   }

   /**
    * Drop listener functions
    */
   // DropTargetListener interface implementation
   /**
    * This method performs 1 time checking that needs to be done each time a
    * drag enters the frame. It checks if any custom actions want the drop or if
    * the window wants the drop then caches this information in a flag. It also
    * checks if the component wants the drop if there is a component under the
    * cursor.
    * <p>
    * Components get first crack at a dragging object, then the custom actions,\
    * then the frame itself.
    */
   public void dragEnter(DropTargetDragEvent dtde)
   {
      m_bAcceptDrag = false; // initialize this flag
      try
      {
         DataFlavor flavor = new DataFlavor(FigureTransfer.sUICOBJ_FLAVOR_TYPE,
            FigureTransfer.sUICOBJ_FLAVOR_NAME);

         DataFlavor infoFlavor = new DataFlavor(
            FigureTransfer.sDESCRIPTOR_FLAVOR_TYPE,
            FigureTransfer.sDESCRIPTOR_FLAVOR_NAME);

         if (dtde.isDataFlavorSupported(infoFlavor))
         {
            Debug.assertTrue(dtde.isDataFlavorSupported(flavor), E2Designer
               .getResources(), "MissingFlavor", null);
            Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard()
               .getContents(this);

            m_currentInfo = (DragInfo) t.getTransferData(infoFlavor);

            if (m_currentInfo.getOriginalRef().getId() == PipeFigureFactory.BACKEND_DATATANK_ID)
            {
               boolean bFound = false;

               //make sure we do not already have this table
               Vector tables = getTablesExceptFor(null);
               UIConnectableFigure uic = (UIConnectableFigure) t
                  .getTransferData(flavor);
               if (uic.getData() != null
                  && uic.getData() instanceof OSBackendDatatank)
               {
                  OSBackendDatatank tankData = (OSBackendDatatank) uic
                     .getData();

                  if (tankData.getTables() != null
                     && tankData.getTables().size() == 1
                     && tankData.getTables().get(0) instanceof OSBackendTable)
                  {
                     OSBackendTable tableData = (OSBackendTable) tankData
                        .getTables().get(0);
                     if (tableData.getAlias() != null)
                     {
                        String strAlias = tableData.getAlias();
                        for (int iTable = 0; iTable < tables.size(); ++iTable)
                        {
                           //                           System.out.println("Compare: " + strAlias + tables.get(iTable).toString());
                           if (strAlias.equals(tables.get(iTable).toString()))
                           {
                              bFound = true;
                              break;
                           }
                        }
                     }
                  }
               }

               if (bFound == false)
                  m_bAcceptDrag = true;
               else
                  m_bAcceptDrag = false;
            }
         }
         else
            System.out.println("Flavor not supported");

         if (m_bAcceptDrag)
            dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
         else
            dtde.rejectDrag();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      catch (UnsupportedFlavorException e)
      {
         Debug
            .assertTrue(false, E2Designer.getResources(), "ForgotCBwDI", null);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   public void dragExit(DropTargetEvent dte)
   {
      m_bAcceptDrag = false;
   }

   public void drop(DropTargetDropEvent dtde)
   {
      DataFlavor flavor = null;

      try
      {
         flavor = new DataFlavor(FigureTransfer.sUICOBJ_FLAVOR_TYPE,
            FigureTransfer.sUICOBJ_FLAVOR_NAME);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      if (dtde.isDataFlavorSupported(flavor)
         && (0 != (dtde.getDropAction() & DnDConstants.ACTION_COPY_OR_MOVE)))
      {
         dtde.acceptDrop(dtde.getDropAction());
      }
      else
      {
         dtde.rejectDrop();
         return;
      }

      try
      {
         Transferable transferable = dtde.getTransferable();
         //dtde.dropComplete(true);
         final DropTargetDropEvent finalDtde = dtde;

         final Point pt = dtde.getLocation();
         final UIConnectableFigure uic = (UIConnectableFigure) transferable
            .getTransferData(flavor);
         final DataFlavor infoFlavor = new DataFlavor(
            FigureTransfer.sDESCRIPTOR_FLAVOR_TYPE,
            FigureTransfer.sDESCRIPTOR_FLAVOR_NAME);
         final DragInfo info = (DragInfo) transferable
            .getTransferData(infoFlavor);

         new SwingWorker()
         {
            public Object construct()
            {
               SwingUtilities.invokeLater(new Runnable()
               {
                  public void run()
                  {
                     //set this ui object to have a table not tank
                     if (uic.getData() != null
                        && uic.getData() instanceof OSBackendDatatank)
                     {
                        OSBackendDatatank tank = (OSBackendDatatank) uic
                           .getData();
                        if (tank.getTables() != null
                           && tank.getTables().size() == 1)
                        {
                           UIFigure fake = new UIFigure("Fake", tank
                              .getTables().get(0), "fake", 0);
                           TableInternalFrame frame = addTable(fake);
                           System.out.println("Is resizeable = "
                              + frame.isResizable());
                           frame.setLocation(pt);
                           repaint();
                        }
                     }
                  }
               });

               return "done";
            }

            public void finished()
            {
               // no op
               finalDtde.dropComplete(true);
            }
         };
      }
      catch (Exception e)
      {
         dtde.dropComplete(false);
         e.printStackTrace();
         return;
      }
   }

   public void dragOver(DropTargetDragEvent dtde)
   {
      // see if we are over a component that will accept the drop
      Point pt = dtde.getLocation();
      if (m_bAcceptDrag)
         dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
      else
         dtde.rejectDrag();
   }

   public void dropActionChanged(DropTargetDragEvent dtde)
   {
      // todo: change cursor
      if (m_bAcceptDrag) //dtde.isDataFlavorSupported(m_data))
      {
         dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
      }
      else
      {
         dtde.rejectDrag();
      }
   }

   /**
    * Get all of the tables except for one getting edited
    */
   public Vector getTablesExceptFor(TableInternalFrame frame)
   {
      OSBackendTable tableData = null;
      if (frame != null && frame.getData() != null
         && frame.getData() instanceof OSBackendTable)
         tableData = (OSBackendTable) frame.getData();

      Vector vTables = new Vector();
      String strTable = null;
      if (frame != null)
         strTable = tableData.getAlias();

      Component[] comps = m_ContentPane.getComponents();
      for (int index = comps.length - 1; index >= 0; index--)
      {
         if (comps[index] instanceof TableInternalFrame)
         {
            TableInternalFrame theFrame = (TableInternalFrame) comps[index];
            if (theFrame.getData() != null
               && theFrame.getData() instanceof OSBackendTable)
            {
               OSBackendTable theTableData = (OSBackendTable) theFrame
                  .getData();
               if (theTableData.getAlias() == null)
                  continue;

               String strTheTable = theTableData.getAlias();
               if (strTable != null && strTable.equals(strTheTable))
                  continue;

               vTables.addElement(strTheTable);
            }
         }
      }

      return vTables;
   }

   protected ResourceBundle getResources()
   {
      try
      {
         if (null == m_res)
            m_res = ResourceBundle.getBundle(
               getClass().getName() + "Resources", Locale.getDefault());
      }
      catch (MissingResourceException mre)
      {
         System.out.println(mre);
      }
      return m_res;
   }

   public PSApplication getApp()
   {
      return m_app;
   }

   /**
    * Bookable interface implemention used for printing
    */
   public void appendPrintPages(Book bk)
   {
      if (m_ContentPane == null)
         return;

      PrintPanelPage page = new PrintPanelPage(this);
      Component[] comps = m_ContentPane.getComponents();
      if (comps != null)
      {
         for (int iComp = 0; iComp < comps.length; ++iComp)
         {
            if (comps[iComp] instanceof PageableAndPrintable)
            {
               PageableAndPrintable uic = (PageableAndPrintable) comps[iComp];
               page.add(uic);
            }
         }
      }

      //preview for debuggin
      page.appendPrintPages(bk);
   }

   private static ResourceBundle sm_helpRes = null;
   static
   {
      try
      {
         if (null == sm_helpRes)
            sm_helpRes = ResourceBundle.getBundle(
               "com.percussion.E2Designer.htmlmapping", Locale.getDefault());
      }
      catch (MissingResourceException mre)
      {
         mre.printStackTrace();
      }
   }

   public OSBackendDatatank getData()
   {
      return (m_theData);
   }

   private PSApplication m_TheApp = null;

   static private Vector<PSComponent> m_stack;

   private UIFigure m_figure = null;

   private PSApplication m_app = null;

   private ResourceBundle m_res = null;

   private DropTarget m_dndTarget = null;

   private DropTarget m_dndGlassTarget = null;

   private DragInfo m_currentInfo = null;

   private boolean m_bAcceptDrag = false;

   private OSBackendDatatank m_theData = null;

   private OSBackendDatatank m_theOldData = null;

   private JPopupMenu m_editPopup = null;

   private JMenu m_helpPopup = null;

   private JMenuItem m_paste = null;

   private Point m_ptXorBegin = null;

   private Point m_ptXorEnd = null;

   private Rectangle m_rectXor = null;

   private JDesktopPane m_ContentPane = null;

   private JLayeredPane m_TablePane = null;

   private MouseListener m_figureMouseListener = null;

   private MouseMotionListener m_figureMouseMotionListener = null;

   private static final String sFIGURE_FACTORY = "com.percussion.E2Designer.JoinFigureFactory";

   private boolean m_bClosing = false;

   /**
    * Used to implement the IEditor closing methods.
    **/
   private int m_closeCookie = 1;

   /**
    * Used as indices into array that stores the window position and size in the
    * user configuration.
    */
   private static final int POS_LOCX = 0;

   private static final int POS_LOCY = 1;

   private static final int POS_WIDTH = 2;

   private static final int POS_HEIGHT = 3;

   private static final String JOIN_WINDOW_POS = "JoinWindowPos";
}
