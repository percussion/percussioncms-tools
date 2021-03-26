/* *****************************************************************************
 *
 * [ TableInternalFrame.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/

package com.percussion.E2Designer;

import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.beans.PropertyVetoException;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.Vector;

/**
 * The TableInternalFrame extends an internal frame to display a datatanks
 * columns withing a parent frame.
 */
public class TableInternalFrame extends JInternalFrame 
   implements ChangeListener, PageableAndPrintable
{
   /**
    * Construct the frame with an initial vector of table columns.
    * 
    * @param tableName the table name used as frame title
    * @param columnNames a vector of table column names
    */
   public TableInternalFrame(UIFigure figure, Vector columns,
      UIJoinMainFrame mainFrame) throws PropertyVetoException
   {

      super("", true);

      if (figure != null && figure.getData() instanceof OSBackendTable)
      {
         m_tableData = (OSBackendTable) figure.getData();
         if (m_tableData.getTable() != null)
            setTitle(m_tableData.getAlias());
         m_theFigure = figure;
         m_theDialog = new BackendTankPropertyDialog(mainFrame);
      }

      m_mainFrame = mainFrame;
      m_tableColumns = new JList(columns);

      Dimension maxSize = setTheList(columns);

      m_panel2 = new JLayeredPane();
      m_panel2.setBackground(Color.white);
      m_panel2.setPreferredSize(maxSize);
      m_panel2.setSize(maxSize);
      m_panel2.setOpaque(true);

      JPanel gPane = new JPanel();
      gPane.setOpaque(false);
      setGlassPane(gPane);

      JPanel glassPane = new JPanel();
      glassPane.setLayout(null);
      glassPane.setOpaque(false);
      glassPane.setVisible(true);
      glassPane.setLocation(0, 0);
      m_dragPane = glassPane;

      m_panel2.add(glassPane, JLayeredPane.DRAG_LAYER);
      m_panel2.add(m_tableColumns, JLayeredPane.DEFAULT_LAYER);

      JScrollPane content = new JScrollPane(m_panel2);
      m_viewport = content.getViewport();
      m_viewport.setBackground(Color.white);

      setContentPane(content);

      //drag and drop between two lines
      TableMouseAdapter mouseListener = new TableMouseAdapter(this);
      glassPane.addMouseListener(mouseListener);
      glassPane.addMouseMotionListener(mouseListener);
      getRootPane().addMouseListener(new PopupMouseAdapter(this));

      addComponentListener(new TableComponentAdapter(this));
      getViewport().addChangeListener(this);
      setSelected(false);
      addInternalFrameListener(new ProcessEvent(m_mainFrame, this));
   }

   public class ProcessEvent implements InternalFrameListener
   {
      public ProcessEvent(UIJoinMainFrame mainFrame, JInternalFrame frame)
      {
         m_mainFrame = mainFrame;
         m_frame = frame;
      }

      public void internalFrameClosed(InternalFrameEvent e)
      {
      }

      public void internalFrameClosing(InternalFrameEvent e)
      {
      }

      public void internalFrameDeactivated(InternalFrameEvent e)
      {
      }

      public void internalFrameDeiconified(InternalFrameEvent e)
      {
      }

      public void internalFrameIconified(InternalFrameEvent e)
      {
      }

      public void internalFrameOpened(InternalFrameEvent e)
      {
      }

      public void internalFrameActivated(InternalFrameEvent e)
      {
         if (m_mainFrame != null)
            m_mainFrame.clearSelection(m_frame);
      }

      UIJoinMainFrame m_mainFrame = null;

      JInternalFrame m_frame = null;
   }

   public void stateChanged(ChangeEvent e)
   {
      calcView();
   }

   class TableComponentAdapter extends ComponentAdapter
   {
      TableComponentAdapter(TableInternalFrame Frame)
      {
      }

      public void componentResized(ComponentEvent e)
      {
         calcView();
      }
   }

   private void calcView()
   {
      Dimension viewSize = m_viewport.getViewSize();
      Dimension listSize = m_tableColumns.getSize();
      if (viewSize.width >= m_minListWidth)
         listSize.width = viewSize.width;
      m_tableColumns.setSize(listSize);
      m_dragPane.setSize(listSize);
   }

   /**
    * Adapter used for the right click
    */
   class PopupMouseAdapter extends MouseAdapter
   {
      PopupMouseAdapter(TableInternalFrame frame)
      {
         m_theFrame = frame;
      }

      public void mouseClicked(MouseEvent e)
      {
         if (e.isPopupTrigger())
         {
            m_theFrame.getMainFrame().getEditPopupMenu(m_theFrame).show(
               m_theFrame, e.getPoint().x, e.getPoint().y);
         }
         else if (e.getClickCount() > 1)
         {
            m_theFrame.getMainFrame().editProperties();
         }
      }

      public void mousePressed(MouseEvent e)
      {
         if (e.isPopupTrigger())
         {
            m_theFrame.getMainFrame().getEditPopupMenu(m_theFrame).show(
               m_theFrame, e.getPoint().x, e.getPoint().y);
         }
         else if (e.getClickCount() > 1)
         {
            m_theFrame.getMainFrame().editProperties();
         }
      }

      public void mouseReleased(MouseEvent e)
      {
         if (e.isPopupTrigger())
         {
            m_theFrame.getMainFrame().getEditPopupMenu(m_theFrame).show(
               m_theFrame, e.getPoint().x, e.getPoint().y);
         }
         else if (e.getClickCount() > 1)
         {
            m_theFrame.getMainFrame().editProperties();
         }
      }

      private TableInternalFrame m_theFrame = null;
   }

   /**
    * This adapter is used to trap mouse events so that we can draw the xor line
    * when the user drags the mouse.
    */
   class TableMouseAdapter extends EscMouseAdapter
      implements MouseMotionListener
   {
      TableMouseAdapter(TableInternalFrame frame)
      {
         m_theFrame = frame;
      }

      public void mouseClicked(MouseEvent e)
      {
         // If user clicks 2nd mouse button while in middle of drag, ignore it
         if (m_mouseEventStarted)
            return;

         if (e.isPopupTrigger())
         {
            m_theFrame.getMainFrame().getEditPopupMenu(m_theFrame).show(
               m_theFrame, e.getPoint().x, e.getPoint().y);
         }
         else if (e.getClickCount() > 1)
         {
            m_theFrame.getMainFrame().editProperties();
         }
         else
         {
            int index = m_theFrame.getListbox().locationToIndex(e.getPoint());
            if (index != -1)
               m_theFrame.getListbox().setSelectedIndex(index);
         }
      }

      public void mousePressed(MouseEvent e)
      {
         // If user presses 2nd mouse button while in middle of drag, ignore it
         if (m_mouseEventStarted)
            return;

         super.mousePressed(e);
         m_mouseEventStarted = true;
         if (!e.isShiftDown())
            m_theFrame.getMainFrame().clearSelection();

         try
         {
            m_theFrame.setSelected(true);
         }
         catch (PropertyVetoException except)
         {
            except.printStackTrace();
         }

         if (e.isPopupTrigger())
         {
            m_theFrame.getMainFrame().getEditPopupMenu(m_theFrame).show(
               m_theFrame, e.getPoint().x, e.getPoint().y);
            return;
         }
         Point pt = e.getPoint();
         Point glasspt = SwingUtilities.convertPoint(m_theFrame.getDragPane(),
            pt, m_theFrame.getTheGlassPane());

         m_theFrame.setPressedPoint(glasspt);

         Point listpt = SwingUtilities.convertPoint(m_theFrame
            .getTheGlassPane(), glasspt, m_theFrame.getListbox());

         m_theFrame.setPressedIndex(m_theFrame.getListbox().locationToIndex(
            listpt));
      }

      public void mouseReleased(MouseEvent e)
      {
         if (!m_mouseEventStarted)
            return;

         if (!m_dragEntered)
         {
            if (e.isPopupTrigger())
            {
               m_theFrame.getMainFrame().getEditPopupMenu(m_theFrame).show(
                  m_theFrame, e.getPoint().x, e.getPoint().y);
               return;
            }
            else if (e.getClickCount() > 1)
            {
               m_theFrame.getMainFrame().editProperties();
               return;
            }
         }

         if (null != m_theFrame.getCursorComponent())
            m_theFrame.getCursorComponent().setCursor(
               Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

         //erase the line
         Point pressed = m_theFrame.getPressedPoint();
         if (pressed != null)
         {
            Point dragged = m_theFrame.getDraggedPoint();
            Graphics g = m_theFrame.getTheGlassPane().getGraphics();
            g.setColor(Color.black);
            g.setXORMode(Color.white);

            if (dragged != null)
            {
               g.drawLine(pressed.x, pressed.y, dragged.x, dragged.y);
            }
         }

         // erase the hilite
         TableInternalFrame colList = m_theFrame.getDropFrame();
         if (null != colList)
            colList.setSelected(false, null);

         //only if it is not me
         if (!wasEscPressed() && m_theFrame != null
            && m_theFrame.getDropFrame() != null
            && m_theFrame != m_theFrame.getDropFrame())
         {
            //create the join
            m_mainFrame.addJoin(m_theFrame, m_theFrame.getPressedIndex(),
               m_theFrame.getDropFrame(), m_theFrame.getDropIndex());
         }

         m_theFrame.repaint();
         m_theFrame.resetPoints();
         m_theFrame.setCursorComponent(null);
         m_mouseEventStarted = false;
      }

      public void mouseDragged(MouseEvent e)
      {
         if (!m_mouseEventStarted)
            return;

         m_dragEntered = true;
         Component comp = e.getComponent();
         if (comp != m_theFrame.getCursorComponent())
         {
            if (null != m_theFrame.getCursorComponent())
               m_theFrame.getCursorComponent().setCursor(
                  Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            m_theFrame.setCursorComponent(comp);
         }

         Point pressed = m_theFrame.getPressedPoint();
         if (pressed != null)
         {
            Point dragged = m_theFrame.getDraggedPoint();
            Graphics g = m_theFrame.getTheGlassPane().getGraphics();
            g.setColor(Color.black);
            g.setXORMode(Color.white);

            //erase the line if needed
            if (dragged != null)
            {
               g.drawLine(pressed.x, pressed.y, dragged.x, dragged.y);
            }

            dragged = e.getPoint();
            m_theFrame.setDraggedPoint(SwingUtilities.convertPoint(
               m_theFrame.getDragPane(), dragged, 
               m_theFrame.getTheGlassPane()));
            dragged = m_theFrame.getDraggedPoint();
            g.drawLine(pressed.x, pressed.y, dragged.x, dragged.y);
         }

         //are we over a drop?
         Point pt = e.getPoint();

         TableInternalFrame tableFrame = m_theFrame
            .getTableFrame(SwingUtilities.convertPoint(
               m_theFrame.getDragPane(), pt, m_theFrame.getTheGlassPane()));
         // clear column hilite when drag is no longer over col list
         if (m_currentColList != tableFrame)
         {
            if (null != m_currentColList)
               m_currentColList.setSelected(false, null);
            m_currentColList = tableFrame;
         }
         if (tableFrame != null)
         {
            m_theFrame.getCursorComponent().setCursor(
               m_theFrame.getWillCursor());

            //the original
            Point listpoint = SwingUtilities.convertPoint(
               m_theFrame.getDragPane(), pt, tableFrame.getListbox());

            m_theFrame.setDropFrame(tableFrame);
            m_theFrame.setDropIndex(tableFrame.getListbox().locationToIndex(
               listpoint));
            tableFrame.setSelected(true, listpoint);
         }
         else
         {
            m_theFrame.getCursorComponent().setCursor(
               m_theFrame.getWontCursor());
            m_theFrame.setDropFrame(m_theFrame);
         }
      }

      public void mouseMoved(MouseEvent e)
      {
         // we don't care about these
      }

      private TableInternalFrame m_theFrame = null;

      /**
       * Is <code>false</code> until the user starts the drag. This allows us
       * to propertly handle release events.
       */
      private boolean m_dragEntered = false;

      /**
       * Used w/ escape key to stop mouse processing. When the esc key is
       * pressed, we receive 2 mouseRelease events. A proper fix would be to
       * figure out how to cause just a single release event to be sent.
       */
      private boolean m_mouseEventStarted = false;

      /**
       * The column list which is under the dragging cursor.
       */
      private TableInternalFrame m_currentColList = null;
   }

   /**
    * Used to determine if the mouse is inside the listbox
    * 
    * @param point the point to check
    */
   //////////////////////////////////////////////////////////////////////////////
   public boolean isInList(Point point)
   {
      int index = getListbox().locationToIndex(point);
      if (index == -1)
         return false;

      return true;
   }

   /**
    * Hilites the cell under the supplied point by drawing an XOR box around the
    * around it. Any previous hilite is erased first. The previous hilite is
    * always erased when this method is called, whether a new hilite is drawn or
    * not.
    * 
    * @param bSelection If <code>true</code>, attempts to map the supplied
    * point to a list cell. If one is found, it is hilited.
    * 
    * @param point This point is used to determine which list cell to hilite. If
    * it is not over any cell, no hilite is drawn. It is only used if bSelection
    * is <code>true</code> (it can be null in this case).
    */
   public void setSelected(boolean bSelection, Point point)
   {
      Graphics g = getListbox().getGraphics();
      g.setColor(Color.black);
      g.setXORMode(Color.white);

      //if we have drawn a rect - un draw it
      if (m_selectedRect != null)
      {
         g.drawRect((int) m_selectedRect.getX(), (int) m_selectedRect.getY(),
            (int) m_selectedRect.getWidth() - 1, 
            (int) m_selectedRect.getHeight() - 1);
      }
      
      m_selectedRect = null;
      if (bSelection)
      {
         int index = getListbox().locationToIndex(point);
         if (index != -1)
         {
            m_selectedRect = getListbox().getCellBounds(index, index);
            g.drawRect((int) m_selectedRect.getX(),
               (int) m_selectedRect.getY(),
               (int) m_selectedRect.getWidth() - 1, 
               (int) m_selectedRect.getHeight() - 1);
         }
      }
   }

   /**
    * drop a join
    */
   public int drop(Point point)
   {
      Graphics g = getListbox().getGraphics();
      g.setColor(Color.black);
      g.setXORMode(Color.white);

      //if we have drawn a rect - un draw it
      if (m_selectedRect != null)
         g.drawRect((int) m_selectedRect.getX(), (int) m_selectedRect.getY(),
            (int) m_selectedRect.getWidth(), (int) m_selectedRect.getHeight());

      m_selectedRect = null;
      int index = getListbox().locationToIndex(point);
      repaint();
      return index;
   }

   public JList getListbox()
   {
      return m_tableColumns;
   }

   public void setTheGlassPane(JComponent glassPane)
   {
      m_glassPane = glassPane;
   }

   public JComponent getTheGlassPane()
   {
      return m_glassPane;
   }

   public Point getPressedPoint()
   {
      return m_ptPressed;
   }

   public Point getDraggedPoint()
   {
      return m_ptDragged;
   }

   public void setDraggedPoint(Point point)
   {
      m_ptDragged = point;
   }

   public void setPressedPoint(Point point)
   {
      m_ptPressed = point;
   }

   public void resetPoints()
   {
      m_ptPressed = null;
      m_ptDragged = null;
      m_iPressedIndex = -1;
      m_DropTable = null;
   }

   public JComponent getDragPane()
   {
      return m_dragPane;
   }

   public Cursor getWontCursor()
   {
      return ms_cursorWontConnect;
   }

   public Cursor getWillCursor()
   {
      return ms_cursorWillConnect;
   }

   /**
    * The component that most recently had its mouse cursor changed during a
    * drag. It is null except during the drag.
    */
   public Component getCursorComponent()
   {
      return m_cursorSetOnComp;
   }

   public void setCursorComponent(Component cursorSet)
   {
      m_cursorSetOnComp = cursorSet;
   }

   /**
    * Sets a list displayed within a panel, representing table's columns for an
    * inserted table
    * 
    * @param columns vector of the table's columns
    * @return a size of a new list of table columns
    */
   private Dimension setTheList(Vector columns)
   {
      m_tableColumns.removeAll();
      m_tableColumns = new JList(columns);
      //if m_tableColumns has no data fill it with an empty string
      if (m_tableColumns.getFirstVisibleIndex() == -1)
      {
         String empty = "";
         columns.add(empty);
         m_tableColumns.setListData(columns);
      }

      /*
       * JLS: fix for Rx-04-12-0003 Need to detect delete key event here, as
       * adding accelerator for this in the UIJoinMainFrame causes erroneous
       * delete key events to be generated by the UIMainFrame as well, likely
       * due to the jdk 1.4 bug (ID 4917669 in bugs.sun.com) - see
       * UIJoinMainFrame.OptionsMenuBar ctor
       */
      m_tableColumns.addKeyListener(new KeyAdapter()
      {
         public void keyReleased(KeyEvent e)
         {
            if (e.getKeyCode() == KeyEvent.VK_DELETE)
            {
               m_mainFrame.deleteSelection();
            }
         }
      });

      FontMetrics fm = m_tableColumns.getFontMetrics(m_tableColumns.getFont());

      Rectangle cellBounds = m_tableColumns.getCellBounds(0, 0);
      int textHeight = cellBounds.height * columns.size();
      int textWidth = 0;
      Enumeration cols = columns.elements();
      while (cols.hasMoreElements())
      {
         String name = (String) cols.nextElement();
         int width = fm.stringWidth(name);
         if (width > textWidth)
            textWidth = width;
      }
      textWidth += 4; // padding for space on either end of string w/in the cell
      if (0 == textHeight)
         textHeight = 100;
      if (0 == textWidth)
         textWidth = 100;
      m_minListWidth = textWidth;

      Dimension maxSize = new Dimension(textWidth, textHeight);
      // we don't do this because there are bugs in the UI mouse handler
      //setMaximumSize( maxSize );

      m_tableColumns.setVisible(true);
      m_tableColumns.setLocation(0, 0);
      m_tableColumns.setSize(maxSize);
      m_tableColumns.setPreferredSize(maxSize);
      m_tableColumns.setSelectionMode(
         DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      

      return maxSize;

   }

   /**
    * Finds the list figure for this point if there is one
    */
   private TableInternalFrame getTableFrame(Point pt)
   {
      Vector comps = Util.findComponentsAt(getTheGlassPane(), pt);
      boolean bFound = false;
      Component c = null;
      int size = comps.size();
      if (0 == size)
      {
         return null;
      }

      Debug.assertTrue(comps.get(size - 1) == getTheGlassPane(), 
         E2Designer.getResources(), "MissingParent", null);
      for (int index = 0; index < size - 1 && !bFound; index++)
      {
         c = (Component) comps.get(index);
         if (c instanceof TableInternalFrame && c != this)
         {
            if (((TableInternalFrame) c).isInList(SwingUtilities.convertPoint(
               getTheGlassPane(), pt, ((TableInternalFrame) c).getListbox())))
            {
               bFound = true;
            }
         }
      }

      if (!bFound)
         return null;

      return (TableInternalFrame) c;
   }

   public int getPressedIndex()
   {
      return m_iPressedIndex;
   }

   public void setPressedIndex(int index)
   {
      m_iPressedIndex = index;
   }

   public void setDropIndex(int index)
   {
      m_iDropIndex = index;
   }

   public int getDropIndex()
   {
      return m_iDropIndex;
   }

   public TableInternalFrame getDropFrame()
   {
      return m_DropTable;
   }

   public void setDropFrame(TableInternalFrame frame)
   {
      m_DropTable = frame;
   }

   public JViewport getViewport()
   {
      return m_viewport;
   }

   /**
    * Clear the selection in the main frame, pass in this so that We do not
    * clear ourselves
    */
   public void clearSelection()
   {
      m_mainFrame.clearSelection(this);
   }

   public UIJoinMainFrame getMainFrame()
   {
      return m_mainFrame;
   }

   public OSBackendTable getData()
   {
      return m_tableData;
   }

   public void onEdit()
   {
      if (m_theDialog != null && m_theFigure != null && m_tableData != null)
      {
         //construct a list of tables
         Vector tables = new Vector();
         tables = m_mainFrame.getTablesExceptFor(this);
         System.out.println("Existing: " + tables.toString());
         m_theDialog.setTables(tables);

         //get the datasource and the table name before editing
         String datasourceName = m_tableData.getDataSource();
         String tableName = m_tableData.getTable().toString();
         m_theDialog.onEdit(m_theFigure, m_tableData);

         if (m_theFigure.getData() != null
            && m_theFigure.getData() instanceof OSBackendTable)
         {
            OSBackendTable tableData = (OSBackendTable) m_theFigure.getData();
            setTitle(tableData.getAlias());
            repaint();
            /*
             * compare database names as well as table names to determine if we
             * need to update the list of columns
             */
            if (!StringUtils.equals(datasourceName, tableData.getDataSource())
               || !tableName.equals(tableData.getTable().toString()))
            {
               m_panel2.removeAll();

               Vector columns = tableData.getColumns();
               //set the list of the table columns
               Dimension maxSize = setTheList(columns);

               m_panel2.setPreferredSize(maxSize);
               m_panel2.setSize(maxSize);

               m_panel2.add(m_dragPane, JLayeredPane.DRAG_LAYER);
               m_panel2.add(m_tableColumns, JLayeredPane.DEFAULT_LAYER);
               m_panel2.repaint();
               
               // request focus to be ready to handle delete key press  
               m_tableColumns.requestFocus();
            }
         }
      }
   }

   public UIFigure getFigure()
   {
      return m_theFigure;
   }

   public JScrollBar getScrollBar()
   {
      return m_scrollBar;
   }

   /**
    * Implementation of the printing interface
    */
   public int print(Graphics g, PageFormat pf, int pageIndex)
      throws PrinterException
   {
      Point pt = getLocation();
      Component parent = this;
      while (parent != null && !(parent instanceof UIJoinMainFrame))
      {
         parent = parent.getParent();
      }

      if (parent != null && getParent() != null)
         pt = SwingUtilities.convertPoint(getParent(), pt, parent);

      //check for the page
      Point pageLoc = getPrintLocation();
      if (pageLoc != null)
      {
         int iMovex = 0;
         int iMovey = 0;
         if (pageLoc.x > 0)
            iMovex = pageLoc.x * (int) pf.getImageableWidth();

         if (pageLoc.y > 0)
            iMovey = pageLoc.y * (int) pf.getImageableHeight();

         pt.translate(-iMovex, -iMovey);
      }

      //paint into an image so that we can relocate it.
      Image offscreen = createImage(getSize().width, getSize().height);
      if (offscreen != null)
      {
         Graphics og = offscreen.getGraphics();
         og.setClip(0, 0, getSize().width, getSize().height);
         paint(og);
         ImageIcon icon = new ImageIcon(offscreen);
         icon.paintIcon(this, g, pt.x, pt.y);
      }

      return Printable.PAGE_EXISTS;
   }

   public void setPrintLocation(Point pt)
   {
      m_printLocation = pt;
   }

   public Point getPrintLocation()
   {
      return (m_printLocation);
   }

   /**
    * the table list data
    */
   private UIFigure m_theFigure = null;

   private JScrollBar m_scrollBar = null;

   transient private BackendTankPropertyDialog m_theDialog = null;

   private JList m_tableColumns = null;

   private Rectangle m_selectedRect = null;

   private JComponent m_glassPane = null;

   private Point m_ptPressed = null;

   private Point m_ptDragged = null;

   private JComponent m_dragPane = null;

   private int m_iDropIndex = -1;

   private TableInternalFrame m_DropTable = null;

   private Component m_cursorSetOnComp = null;

   private int m_iPressedIndex = -1;

   transient private UIJoinMainFrame m_mainFrame = null;

   private static Cursor ms_cursorWillConnect;

   private static final String sFIGURE_FACTORY = "com.percussion.E2Designer.JoinFigureFactory";

   private OSBackendTable m_tableData;

   private boolean m_bFirst = true;

   private Point m_printLocation = null;

   private JViewport m_viewport = null;

   /**
    * The panel containing the list of table's columns, set in the constructor
    * and modified in {@link #onEdit()}
    */
   private JLayeredPane m_panel2 = null;

   /**
    * This is the minimum pixel width allowed for the list box. We never want
    * the list box to be smaller than its longest string.
    **/
   private int m_minListWidth;

   private static Cursor ms_cursorWontConnect;
   {
      PSResources rb = E2Designer.getResources();
      String strCursorResName = "ConnectCursor";
      ImageIcon icon = ResourceHelper.getIcon(rb, strCursorResName);
      if (null == icon)
         throw new MissingResourceException(E2Designer.getResources()
            .getString("LoadIconFail"), "E2DesignerResources", strCursorResName);
      ms_cursorWillConnect = Toolkit.getDefaultToolkit().createCustomCursor(
         icon.getImage(), ResourceHelper.getPoint(rb, strCursorResName),
         strCursorResName);

      strCursorResName = "NoConnectCursor";
      icon = ResourceHelper.getIcon(rb, strCursorResName);
      if (null == icon)
         throw new MissingResourceException(E2Designer.getResources()
            .getString("LoadIconFail"), "E2DesignerResources", strCursorResName);
      ms_cursorWontConnect = Toolkit.getDefaultToolkit().createCustomCursor(
         icon.getImage(), ResourceHelper.getPoint(rb, strCursorResName),
         strCursorResName);
   }
}
