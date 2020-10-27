/*[ UIJoinConnector.java ]*****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSApplication;
import com.percussion.util.PSCollection;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.Serializable;
import java.util.MissingResourceException;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This class is used in the join editor to link 2 columns.  It gets created
 * when the user drags and drops between 2 tables. It's container is
 * UIJoinMainFrame.
 */
public class UIJoinConnector extends UIConnector implements ChangeListener
{
   // constructors
   public UIJoinConnector( String strName, Object Data,
         String strEditorClassName, int ID, String strFactoryClassName)
   {
      super( strName, Data, strEditorClassName, ID, strFactoryClassName);

      //we want the mouse for the drag actions
      //I had to move this to this class so that the mouse events get to the
      //list controls used to define tables
      m_theMouseAdapter = new JoinMouseAdapter(this);
      m_theMouseMotionAdapter = new JoinMouseMotionAdapter(this);
      addMouseListener(m_theMouseAdapter);
      addMouseMotionListener(m_theMouseMotionAdapter);
      m_theComponentAdapter = new JoinComponentAdapter(this);
      m_theWindowAdapter = new JoinInternalFrameAdapter(this);

      if(Data instanceof OSBackendJoin)
      {
         OSBackendJoin join = (OSBackendJoin)Data;
         m_bFormula = join.getTranslator() == null ? false : true;
      }

      setOpaque(false);
   }

   /**
    * This class is used to trap mouse events
    */
   private class JoinMouseMotionAdapter extends MouseMotionAdapter
   {
      JoinMouseMotionAdapter(UIJoinConnector theConnector)
      {
         m_theConnector = theConnector;
      }

      /**
       * If the figure under the mouse when it was clicked requested mouse
       * events and supplied a listener, this event is relayed to the figure.
       * The event is passed unchanged.
       */
      public void mouseDragged(MouseEvent e)
      {
         if(m_theConnector.getMouseUIC() != null)
            m_theConnector.getMouseUIC().mouseDragged(SwingUtilities.convertMouseEvent(m_theConnector,
                                                                  e,
                                                                  m_theConnector.getMouseUIC()));
      }

      private UIJoinConnector m_theConnector = null;
   }

   /**
    * This class is used to trap mouse events
    */
   private class JoinMouseAdapter extends MouseAdapter
   {
      JoinMouseAdapter(UIJoinConnector theConnector)
      {
         m_theConnector = theConnector;
      }

      /*
       * When a mousePressed event occurs, the figure under the mouse is
       * queried to determine if it wants the mouse events by calling
       * wantsMouse(). The figure signifies its desire by returning
       * <code>true</code>. After requesting the mouse, all mouse events are sent
       * through a mouseReleased event. After the release, no more events are sent.
       * </p>
       * Only 3 events are relayed: mousePressed, mouseReleased and mouseDragged.
       * Each event is passed unmodified.
       * </p>
       * At least one of the 2 methods to get these listeners must return a valid
       * listener. If not, an assertion is issued.
       */
      public void mousePressed(MouseEvent e)
      {
         UIJoinConnector uic = m_theConnector.getParentFrame().getConnectable(SwingUtilities.convertPoint(m_theConnector,
                                                                                  e.getPoint(),
                                                                                  m_theConnector.getGlassPane()));

         //return if not over a connector
         if(uic == null)
            return;

         m_theConnector.setMouseUIC(uic);
         m_theConnector.getMouseUIC().mousePressed(SwingUtilities.convertMouseEvent(m_theConnector,
                                                                  e,
                                                                  m_theConnector.getMouseUIC()));
      }

      public void mouseClicked(MouseEvent e)
      {
         m_theConnector.mouseClicked(e);
      }


      /**
       * If the figure under the mouse when it was clicked requested mouse
       * events and supplied a listener, this event is relayed to the figure.
       * After this, some local clean up is done so no more mouse events
       * are relayed. The event is passed unmodified.
       */
      public void mouseReleased(MouseEvent e)
      {
         if(m_theConnector.getMouseUIC() != null)
         {
            m_theConnector.getMouseUIC().mouseReleased(SwingUtilities.convertMouseEvent(m_theConnector,
                                                                  e,
                                                                  m_theConnector.getMouseUIC()));
            m_theConnector.setMouseUIC(null);
         }
      }

      private UIJoinConnector m_theConnector = null;
   }

   /**
    * Used to change an endpoint of the join.
    */
   private void addTarget(DropTarget target, boolean bLeft)
   {
      if(bLeft)
         setLeftJoin(target.getTableFrame(), target.getSelection());
      else
         setRightJoin(target.getTableFrame(), target.getSelection());
   }

   /**
    * Handler for mouse released
    */
   private void mouseReleased(MouseEvent e)
   {
      if (null != getCursorComponent())
            getCursorComponent().setCursor(
               Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

      if(m_dropTarget != null && !m_bdragMiddle)
      {
         boolean bLeft = false;
         if(m_bdragLeftSide)
         {
            if(m_bLeftIsLeft)
               bLeft = true;
         }
         else
         {
            if(!m_bLeftIsLeft)
               bLeft = true;
         }

         m_dropTarget.doDrop();
         addTarget(m_dropTarget, bLeft);
         //send in true so that we recalc the middle
         calcSize(true);
         repaint();
      }
      else
      {
         if (null != getMouseListener())
            getMouseListener().mouseReleased(e);

         //could be a failed drag of an end
         if(!m_bdragMiddle)
         {
            calcSize(false);
            repaint();
         }

         //uiconnctor moved the middle point so make the proper adjustment
         restoreMiddle();
      }

      setCursorComponent(null);
      m_dropTarget = null;
      setMouseListener(null);
      setMouseMotionListener(null);

      if(e.isPopupTrigger())
      {
         m_parentFrame.getEditPopupMenu(this).show(this, e.getPoint().x, e.getPoint().y);
      }
   }

   private void mouseClicked(MouseEvent e)
   {
      if(e.isPopupTrigger())
      {
         m_parentFrame.getEditPopupMenu(this).show(this, e.getPoint().x, e.getPoint().y);
      }
      else if( e.getClickCount() > 1)
      {
         m_parentFrame.editProperties();
      }
   }

   /**
    * Handler for mouse pressed
    */
   private void mousePressed(MouseEvent e)
   {
      if(!e.isShiftDown())
         getParentFrame().clearSelection();

      if(!isSelected())
      {
         setSelection(true, e.isShiftDown());
         repaint();
      }

      m_bdragLeftSide = m_bdragMiddle = false;
      setLastMiddle();

      Point framePt = e.getPoint();

      //need to convert mouse event so that we think it is from the glass pane
      MouseEvent newevent = SwingUtilities.convertMouseEvent( this,
                            e,
                            getGlassPane());

      setMouseListener(null);
      setMouseMotionListener(null);

      //is it in a handle? - do not send in converted point
      if (!wantsMouse(framePt))
         return;

      int iHitHandle = getHitHandleIndex(framePt);
      if(iHitHandle == 0)
         m_bdragLeftSide = true;
      else if(iHitHandle < m_handles.size() - 1)
         m_bdragMiddle = true;

      setMouseListener(getMouseListener( framePt, getGlassPane()));
      setMouseMotionListener(getMouseMotionListener( framePt, getGlassPane()));

      if (null != getMouseListener())
         getMouseListener().mousePressed(newevent);

      m_dropTarget = null;

      if(e.isPopupTrigger())
      {
         m_parentFrame.getEditPopupMenu(this).show(this, e.getPoint().x, e.getPoint().y);
      }
   }

   /**
    * Mouse for mouse dragged
    */
   private void mouseDragged(MouseEvent e)
   {
      if (null == getMouseMotionListener())
         return;

      //need to convert mouse event so that we think it is from the glass pane
      MouseEvent newevent = SwingUtilities.convertMouseEvent( this,
                            e,
                            getGlassPane());
      Component comp = e.getComponent();
      if (comp != getCursorComponent())
      {
         if (null != getCursorComponent())
            getCursorComponent().setCursor(
               Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

         setCursorComponent(comp);
      }

      Debug.assertTrue(null != getCursorComponent(), "cursorSetOnComponent unexpectedly null");

      //we don't care if we are dragging the middle
      if(!m_bdragMiddle)
      {
         //if we are over our list box
         Point pt = e.getPoint();
         Point newpt = SwingUtilities.convertPoint( this,
                               pt,
                               getGlassPane());

         TableInternalFrame tableFrame = getTableFrame(newpt);
         if (tableFrame != null)
         {
            getCursorComponent().setCursor(getWillCursor());
            Point listpoint = SwingUtilities.convertPoint(getGlassPane(),
                                               newpt,
                                               tableFrame.getListbox());

            m_dropTarget = new DropTarget(tableFrame, listpoint);
            //select in list box
            m_dropTarget.setSelection(true);
            //move over
            Rectangle rect = tableFrame.getListbox().getBounds();
            newevent.translatePoint(rect.x - listpoint.x,0);
            //remember
            setLastPoint(newevent.getPoint());
         }
         else
         {
            if(m_dropTarget != null)
               m_dropTarget.setSelection(false);

            getCursorComponent().setCursor(getWontCursor());
            m_dropTarget = null;
         }
      }

      getMouseMotionListener().mouseDragged(newevent);
   }

   /**
    * This class is a representation of one side of the join.
    * It includes the table, column index and if it is the
    * left or right join.
    */
   public class JoinSide implements Serializable
   {
      JoinSide(TableInternalFrame tableFrame, int iColumn, boolean bLeft)
      {
         m_tableFrame = tableFrame;
         m_iColumn = iColumn;
         m_bLeft = bLeft;
      }

      JoinSide(JoinSide side)
      {
         m_tableFrame = side.m_tableFrame;
         m_iColumn = side.m_iColumn;
         m_bLeft = side.m_bLeft;
      }

      public TableInternalFrame getFrame()
      {
         return m_tableFrame;
      }

      public int getColumn()
      {
         return m_iColumn;
      }

      private TableInternalFrame m_tableFrame = null;
      private int m_iColumn = -1;
      private boolean m_bLeft = false;
   }

   /**
    * adapter to track the selection of the table
    */
   private class JoinInternalFrameAdapter extends InternalFrameAdapter
   {
      JoinInternalFrameAdapter(UIJoinConnector theConnector)
      {
         m_theConnector = theConnector;
      }

      public void InternalFrameActivated(InternalFrameEvent e)
      {
         //when a table is actiated undo our selection
      //   m_theConnector.setSelection(null);
      }

      private UIJoinConnector m_theConnector = null;
   }

   /**
    * adapter for moves and sizes.  The join needs to resize when
    * the table resizes or when the listbox scrolls.
    */
   private class JoinComponentAdapter extends ComponentAdapter
   {
      JoinComponentAdapter(UIJoinConnector theConnector)
      {
         m_theConnector = theConnector;
      }

      public void componentMoved(ComponentEvent e)
      {
         m_theConnector.calcSize();
         m_theConnector.repaint();
      }

      public void componentResized(ComponentEvent e)
      {
         m_theConnector.calcSize();
         m_theConnector.repaint();
      }

      private UIJoinConnector m_theConnector = null;
   }

   public void stateChanged(ChangeEvent e)
   {
      calcSize();
      repaint();
   }

   /**
    * This class is used to communicate join end points being moved.
    */
   public class DropTarget
   {
      DropTarget(TableInternalFrame tableFrame, Point point)
      {
         m_tableFrame = tableFrame;
         m_dropLocation = point;
      }

      public void doDrop()
      {
         if(m_tableFrame != null && m_dropLocation != null)
         {
            m_iSelected = m_tableFrame.drop(m_dropLocation);
         }
      }

      public void setSelection(boolean bSelected)
      {
         if(m_tableFrame != null && m_dropLocation != null)
         {
            m_tableFrame.setSelected(bSelected, m_dropLocation);
         }
      }

      public TableInternalFrame getTableFrame()
      {
         return m_tableFrame;
      }

      public Point getDropLocation()
      {
         return m_dropLocation;
      }

      public int getSelection()
      {
         return m_iSelected;
      }

      private TableInternalFrame m_tableFrame = null;
      private Point m_dropLocation = null;
      private int m_iSelected = -1;
   }

   /**
    * The pane that is used to draw the xor line during a drag
    */
   public void setGlassPane(JComponent glassPane)
   {
      m_glassPane = glassPane;
   }

   /**
    * The pane that is used to draw the xor line during a drag
    */
   public JComponent getGlassPane()
   {
      return m_glassPane;
   }

   /**
    * The component that most recently had its mouse cursor changed during a
    * drag. It is null except during the the drag.
    */
   public Component getCursorComponent()
   {
      return m_cursorSetOnComp;
   }

   public void setCursorComponent(Component cursorSet)
   {
      m_cursorSetOnComp = cursorSet;
   }

   public static Cursor getWontCursor()
   {
      return ms_cursorWontConnect;
   }

   public static Cursor getWillCursor()
   {
      return ms_cursorWillConnect;
   }

   /**
    * Finds the list figure for this point if there is one
    */
   private TableInternalFrame getTableFrame(Point pt)
   {
      Vector comps = Util.findComponentsAt( getGlassPane(), pt );
      boolean bFound = false;
      Component c = null;
      int size = comps.size();
      if ( 0 == size )
      {
         return null;
      }

      Debug.assertTrue(comps.get( size-1 ) == getGlassPane(), E2Designer.getResources(),
            "MissingParent", null );
      for ( int index = 0; index < size-1 && !bFound; index++ )
      {
         c = (Component) comps.get( index );
         if ( c instanceof TableInternalFrame)
         {
            if(((TableInternalFrame) c).isInList(SwingUtilities.convertPoint( getGlassPane(), pt, ((TableInternalFrame) c).getListbox())))
            {
               bFound = true;
            }
         }
      }

      if(!bFound)
         return null;

      return (TableInternalFrame) c;
   }

   public Point getLastPoint()
   {
      return m_lastPoint;
   }

   public void setLastPoint(Point point)
   {
      m_lastPoint   = point;
   }


   /**
    * sets the left side of the join to a table and column.  The column integer
    * is the index in the table of the column.
    */
   public void setLeftJoin(TableInternalFrame frame, int iColumn)
   {
      //remove listeners from old join
      if(m_leftJoin != null)
      {
         m_leftJoin.getFrame().removeComponentListener(m_theComponentAdapter);
         m_leftJoin.getFrame().getViewport().removeChangeListener(this);
      }

      m_leftJoin = new JoinSide(frame, iColumn, true);
      //add listeners for resizing
      m_leftJoin.getFrame().addComponentListener(m_theComponentAdapter);
      m_leftJoin.getFrame().getViewport().addChangeListener(this);

      //add listener for selection changes
      //m_leftJoin.getFrame().addInternalFrameListener(m_theWindowAdapter);
   }

   /**
    * sets the right side of the join to a table and column.  The column integer
    * is the index in the table of the column.
    */
   public void setRightJoin(TableInternalFrame frame, int iColumn)
   {
      //remove listeners from old join

      if(m_rightJoin != null)
      {
         m_rightJoin.getFrame().removeComponentListener(m_theComponentAdapter);
         m_rightJoin.getFrame().getViewport().removeChangeListener(this);
      }

      m_rightJoin = new JoinSide(frame, iColumn, false);
      //add listeners for resizing
      m_rightJoin.getFrame().addComponentListener(m_theComponentAdapter);
      m_rightJoin.getFrame().getViewport().addChangeListener(this);

      //add listener for selection changes
      //m_rightJoin.getFrame().addInternalFrameListener(m_theWindowAdapter);
   }

   /**
    * Using the left and right tables calculates the drawing points
    */

   public void calcSize()
   {
      calcSize(false);
   }

   public void calcSize(boolean bOnCreate)
   {
      if (m_leftJoin == null || m_rightJoin == null)
      {
         if (m_arrow != null)
            m_arrow.setVisible(false);

         return;
      }

      // there is not much to do if one of the tables is <code>null</code>
      TableInternalFrame leftFrame = m_leftJoin.getFrame();
      TableInternalFrame rightFrame = m_rightJoin.getFrame();
      if (leftFrame == null || rightFrame == null)
         return;

      // we also need valid lists
      JList leftList = leftFrame.getListbox();
      JList rightList = rightFrame.getListbox();
      if (leftList == null || rightList == null)
         return;

      //first find the left and right tables by positions and sizes
      Point leftLoc = leftFrame.getLocation();
      Point rightLoc = rightFrame.getLocation();
      Dimension leftSize = leftFrame.getSize();
      Dimension rightSize = rightFrame.getSize();

      //3 conditions
      //left table is left position
      //right table is left position
      //neither we will draw left - left or right - right
      int iLeftTableRightSide = -1;
      int iLeftTableLeftSide = -1;
      int iRightTableLeftSide = -1;
      int iRightTableRightSide = -1;
      boolean bSideToSide;
      boolean bOldSideToSide = m_bSideToSide;
      bSideToSide = m_bSideToSide = false;
      boolean bLeftIsLeft;
      bLeftIsLeft = m_bLeftIsLeft = false;
      boolean bLeftIsTop = false;

      Rectangle leftColumnBounds = new Rectangle();
      Rectangle leftCellBounds = leftList.getCellBounds(getLeftColumn(), getLeftColumn());
      if (leftCellBounds == null)
         leftCellBounds = new Rectangle();
      leftColumnBounds = SwingUtilities.convertRectangle(leftList,
                                                         leftCellBounds,
                                                         leftFrame);

      Rectangle rightColumnBounds = new Rectangle();
      Rectangle rightCellBounds = rightList.getCellBounds(getRightColumn(), getRightColumn());
      if (rightCellBounds == null)
         rightCellBounds = new Rectangle();
      rightColumnBounds = SwingUtilities.convertRectangle(rightList,
                                                          rightCellBounds,
                                                          rightFrame);

      iLeftTableRightSide = leftLoc.x + leftSize.width + leftColumnBounds.height;
      iRightTableRightSide = rightLoc.x + rightSize.width + rightColumnBounds.height;
      iLeftTableLeftSide = leftLoc.x - leftColumnBounds.height;
      iRightTableLeftSide = rightLoc.x - rightColumnBounds.height;

      //decide which table is to the left positionally
      if(iLeftTableRightSide < iRightTableLeftSide)
         bLeftIsLeft = m_bLeftIsLeft = true;
      else if(iRightTableRightSide < iLeftTableLeftSide)
         bLeftIsLeft = m_bLeftIsLeft = false;
      else
      {
         m_bSideToSide = bSideToSide = true;
         if(leftLoc.y < rightLoc.y)
            bLeftIsTop = m_bLeftIsLeft = true;
      }

      //Look for the location according to the column index
      Point locationPoint = null;
      int iMiddleLeft = leftColumnBounds.y + (leftColumnBounds.height / 2);
      int iMiddleRight = rightColumnBounds.y + (rightColumnBounds.height / 2);
      int iLeftLastRow = leftList.getLastVisibleIndex();
      int iRightLastRow = rightList.getLastVisibleIndex();

      if (iRightLastRow == -1)
         iRightLastRow = rightList.getModel().getSize();

      if (iLeftLastRow == -1)
         iLeftLastRow = leftList.getModel().getSize();

      // change where the line is drawn if the column is not visible
      if (getLeftColumn() < leftList.getFirstVisibleIndex())
         iMiddleLeft = 0;
      else if (getLeftColumn() > iLeftLastRow)
         iMiddleLeft = leftSize.height;

      if (getRightColumn() < rightList.getFirstVisibleIndex())
         iMiddleRight = 0;
      else if (getRightColumn() > iRightLastRow)
         iMiddleRight = rightSize.height;

      int iMiddleLinex = -1;
      Point rightSideBeg = null;
      Point rightSide = null;

      //set the location and end points
      if (bSideToSide)
      {
         if (bLeftIsTop)
         {
            locationPoint = leftFrame.getLocation();
            locationPoint.translate(leftSize.width, iMiddleLeft);

            rightSideBeg = rightFrame.getLocation();
            rightSide = new Point((rightSideBeg.x - locationPoint.x) + rightSize.width,
                             (rightSideBeg.y - locationPoint.y) + iMiddleRight);

         }
         else
         {
            locationPoint = rightFrame.getLocation();
            locationPoint.translate(rightSize.width, iMiddleRight);

            rightSideBeg = leftFrame.getLocation();
            rightSide = new Point((rightSideBeg.x - locationPoint.x) + leftSize.width,
                             (rightSideBeg.y - locationPoint.y) + iMiddleLeft);
         }

         iMiddleLinex = leftSize.width / 2;
      }
      else //take the left table
      {
         if(bLeftIsLeft)
         {
            locationPoint = leftFrame.getLocation();
            locationPoint.translate(leftSize.width, iMiddleLeft);

            rightSideBeg = rightFrame.getLocation();
            rightSide = new Point(rightSideBeg.x - locationPoint.x,
                             (rightSideBeg.y - locationPoint.y) + iMiddleRight);
         }
         else
         {
            locationPoint = rightFrame.getLocation();
            locationPoint.translate(rightSize.width, iMiddleRight);

            rightSideBeg = leftFrame.getLocation();
            rightSide = new Point(rightSideBeg.x - locationPoint.x,
                             (rightSideBeg.y - locationPoint.y) + iMiddleLeft);
         }

         iMiddleLinex = rightSide.x / 2;
      }

      Point oldLocation = getLocation();
      setLocation(locationPoint);
      Point endPoint = new AttachedPoint(rightSide);

      //if we are not being created do not change the middle
      //this will allow us to let the user change the middle
      if (!bOnCreate)
      {
         if (m_points.size() > 1)
         {
            Point middlepoint = null;
            middlepoint = (Point)m_points.get(1);

            //now back up a little
            if (m_ptReference != null)
            {
               oldLocation.translate(m_ptReference.x, m_ptReference.y);

               int iLocationDiffx = locationPoint.x - oldLocation.x;
               int iLocationDiffy = locationPoint.y - oldLocation.y;

               middlepoint.translate(-(m_ptReference.x + iLocationDiffx),
                                          -(m_ptReference.y + iLocationDiffy));
            }

            boolean bChange = true;
            //make sure we do not go beyound the arrow
            if (!bSideToSide)
            {
               if (middlepoint.x < leftColumnBounds.height)
               {
                  bChange = false;
                  iMiddleLinex = leftColumnBounds.height;
               }

               if (middlepoint.x > rightSide.x - rightColumnBounds.height)
               {
                  bChange = false;
                  iMiddleLinex = rightSide.x - rightColumnBounds.height;
               }
            }
            else
            {
               //add buffer incase the bottom table is to the right of top table
               if ((bLeftIsTop && iLeftTableRightSide < iRightTableRightSide) ||
                   (!bLeftIsTop && iRightTableRightSide < iLeftTableRightSide))
               {
                  if (middlepoint.x < rightSide.x + rightColumnBounds.height)
                  {
                     bChange = false;
                     iMiddleLinex = rightSide.x + rightColumnBounds.height;
                  }
               }
               else if ((!bLeftIsTop && iLeftTableRightSide < iRightTableRightSide) ||
                        (bLeftIsTop && iRightTableRightSide < iLeftTableRightSide))
               {
                  if (middlepoint.x < leftColumnBounds.height)
                  {
                     bChange = false;
                     iMiddleLinex = leftColumnBounds.height;
                  }
               }
            }

            if (bChange)
               iMiddleLinex = middlepoint.x;
         }
      }

      m_points.clear();
      m_points.addElement(new AttachedPoint(new Point(0, 0)));

      //middle point
      m_points.addElement(new Point(iMiddleLinex, 0));
      //middle bottom point
      m_points.addElement(new Point(iMiddleLinex, rightSide.y));
      //endpoint
      m_points.addElement(endPoint);

      resizeForPoints();
      Point tempPoint = null;

      if (m_points.size() > 2)
      {
         tempPoint = (Point) m_points.get(2);

         m_ptReference = new Point(tempPoint.x - iMiddleLinex,
                                   tempPoint.y - rightSide.y);
      }

      //add the arrow
      int iArrowSize = leftColumnBounds.height;
      if(iArrowSize % 2 == 0)
         ++iArrowSize;

      boolean bAdd = false;
      if (m_arrow == null)
      {
         m_arrow = new JoinArrow(m_bLeftIsLeft, this);
         //formula label
         m_formulaLabel = new UIFigure("Udfs", getData(), null, 0);
         m_formulaLabel.setLocation(20, 20);

         m_hand = new JoinHand(!m_bLeftIsLeft && !m_bSideToSide, this, iArrowSize * 2);
         bAdd = true;
      }

      int iFormulaWidth = m_formulaLabel.getSize().width;
      int iFormulaHeight = m_formulaLabel.getSize().height / 2;

      m_arrow.setLeft(m_bLeftIsLeft || bSideToSide);
      m_arrow.setVisible(true);
      m_formulaLabel.setVisible(m_bFormula);
      m_hand.setVisible(m_bFormula);

      m_arrow.setSize(iArrowSize, iArrowSize);

      if (m_bLeftIsLeft || (bSideToSide && bLeftIsTop))
      {
         Point pt = new Point((Point)m_points.get(0));
         pt.translate(0,-(iArrowSize / 2 + 1));
         m_arrow.setLocation(SwingUtilities.convertPoint(this,
                                                         pt,
                                                         m_glassPane));

         Point pt2 = new Point((Point)m_points.get(m_points.size() - 1));
         pt2.translate(-iFormulaWidth,-iFormulaHeight);
         m_formulaLabel.setLocation(SwingUtilities.convertPoint(this,
                                                                pt2,
                                                                m_glassPane));
      }
      else
      {
         Point pt = new Point((Point)m_points.get(m_points.size() - 1));
         int iLeftOffset = -iArrowSize;
         if (bSideToSide)
            iLeftOffset = 0;

         pt.translate(iLeftOffset, -(iArrowSize / 2 + 1));
         m_arrow.setLocation(SwingUtilities.convertPoint(this,
                                                         pt,
                                                         m_glassPane));

         Point pt2 = new Point((Point)m_points.get(0));
         pt2.translate(0, -iFormulaHeight);
         m_formulaLabel.setLocation(SwingUtilities.convertPoint(this,
                                                                pt2,
                                                                m_glassPane));
      }

      if (bAdd)
      {
         m_glassPane.add(m_arrow, 400);
         m_glassPane.add(m_formulaLabel, 400);
         m_glassPane.add(m_hand, 400);
      }

      m_arrow.repaint();

      if (m_bFormula)
      {
         m_hand.setLeft(!m_bLeftIsLeft && !m_bSideToSide);
         m_hand.calcSize();
         m_hand.repaint();
      }
   }

   /**
    * Determins if this join has a translation formula which is used
    * to determine the value of the right join columns
    * @Returns true if this join has a formula
    */
   public boolean hasFormula()
   {
      return m_bFormula;
   }

   public void resizeForPoints()
   {
      //now remember the difference in the middle point
      //so that we can adjust later without moving it over
      createHandles();
      Vector oldPoints = new Vector( m_points );
      adjustBounds(oldPoints ); //, new Point(), endPoint);
   }

   /**
    *  Set last middle
    */
   public void setLastMiddle()
   {
      if(m_points.size() > 2)
      {
         m_ptLastMiddle = (Point)m_points.get(2);
      }
   }

   /**
    * Restore middle
    */
   public void restoreMiddle()
   {
      if(m_ptLastMiddle != null)
      {
         if(m_points.size() > 2)
         {
            Point point1 = null;

            Point point2 = null;

            point2 = (Point)m_points.get(2);
            point1 = (Point)m_points.get(1);

            point1.x = m_ptLastMiddle.x;
            point2.x = m_ptLastMiddle.x;
            m_points.setElementAt(point1,1);
            m_points.setElementAt(point2,2);
            resizeForPoints();
         }
      }
   }

   /**
    * Returns a listener to handle mouse messages that occur after a user has
    * clicked on this component and it has returned <code>true</code> when
    * wantsMouse(pt) was called.
    *
    * @param pt location the mouse was clicked, in connector coords
    *
    * @param drawingPane the component where drawing will take place during the
    * drag. Typically, this will be the glass pane of the frame drawing pane.
    * This parameter must match the drawing pane passed to getMouseListener.
    *
    * @see #getMouseMotionListener
    */
   public MouseListener getMouseListener()
   {
      return m_figureMouseListener;
   }

   /**
    * Returns a listener to handle mouse messages that occur after a user has
    * clicked on this component and it has returned <code>true</code> when
    * wantsMouse() was called.
    *
    * @param pt this param is ignored, the value passed to getMouseListener is
    * used
    *
    * @param drawingPane this param is ignored, the value passed to
    * getMouseListener is used
    *
    * @see #getMouseListener
    */
   public MouseMotionListener getMouseMotionListener()
   {
      return m_figureMouseMotionListener;
   }

   public UIJoinMainFrame getParentFrame()
   {
      return m_parentFrame;
   }

   public void setParentFrame(UIJoinMainFrame frame)
   {
      m_parentFrame = frame;
   }

   public void setMouseListener(MouseListener listener)
   {
      m_figureMouseListener = listener;
   }

   public void setMouseMotionListener(MouseMotionListener listener)
   {
      m_figureMouseMotionListener = listener;
   }

   public UIJoinConnector getMouseUIC()
   {
      return m_tempConnector;
   }

   public void setMouseUIC(UIJoinConnector uic)
   {
      m_tempConnector = uic;
   }

   /**
    * Get the arrow object that is drawn on the left side
    */
   public JoinArrow getArrow()
   {
      return m_arrow;
   }

   /**
    * Get the hand object that is drawn if there is a join translation
    */
   public JoinHand getHand()
   {
      return m_hand;
   }

   /**
    * Gets the left handle
    */
   public UIConnector.Handle getLeftHandle()
   {
      UIConnector.Handle h = null;
      // Draw the resize handles, if needed
      if (!m_handles.isEmpty() && isSelected())
      {

         if(m_bLeftIsLeft)
            h = new UIConnector.Handle((UIConnector.Handle)m_handles.get(0));
         else
            h = new UIConnector.Handle((UIConnector.Handle)m_handles.get(m_handles.size() - 1));
      }

      return h;
   }

   /**
    * Handle on edit.  We need to do this here because we want to set the parent frame
    */
   public void onEdit(PSApplication m_TheApp)
   {
      OSBackendDatatank datatank = null;
      if(m_leftJoin != null && m_leftJoin.getFrame() != null)
      {
         try
         {
            datatank = new OSBackendDatatank();
            PSCollection tables = new PSCollection("com.percussion.design.objectstore.PSBackEndTable");
            tables.add(m_leftJoin.getFrame().getData());
            datatank.setTables(tables);
         }
         catch(IllegalArgumentException e)
         {
            e.printStackTrace();
         }
         catch(ClassNotFoundException e)
         {
            e.printStackTrace();
         }
      }

      BackendJoinPropertyDialog edit = new BackendJoinPropertyDialog(datatank);

       edit.onEdit(this,m_TheApp);
      if(getData() instanceof OSBackendJoin)
      {
         OSBackendJoin join = (OSBackendJoin)getData();

      // added this line to fix bug (OSAO-4AQT6P)
      join.setTranslator(edit.isTranslatorEnabled() == true ? edit.getCall() : null);

         m_bFormula = join.getTranslator() == null ? false : true;
      }

      if(m_formulaLabel != null)
      {
         m_formulaLabel.setVisible(m_bFormula);
         m_hand.setVisible(m_bFormula);
         if(m_bFormula)
            m_hand.calcSize();
      }
   }

   /**
    * Invert the left and right tables
    */
   public void invert()
   {
      if(m_leftJoin != null && m_rightJoin != null)
      {
         //ui stuff
         JoinSide left = new JoinSide(m_leftJoin);
         JoinSide right = new JoinSide(m_rightJoin);
         m_leftJoin = right;
         m_rightJoin = left;
         calcSize(false);
         repaint();
      }
   }

   /**
    * Get left frame
    */
   public TableInternalFrame getLeftFrame()
   {
      if(m_leftJoin != null)
         return m_leftJoin.getFrame();

      return null;
   }

   /**
    * Get right frame
    */
   public TableInternalFrame getRightFrame()
   {
      if(m_rightJoin != null)
         return m_rightJoin.getFrame();

      return null;
   }

   /**
    * Get left columnm
    */
   public int getLeftColumn()
   {
      if(m_leftJoin != null)
         return m_leftJoin.getColumn();

      return -1;
   }

   /**
    * Get right frame
    */
   public int getRightColumn()
   {
      if(m_rightJoin != null)
         return m_rightJoin.getColumn();

      return -1;
   }

   /**
    * Get the formula display object that shows that this join has a formula
    */
   public UIFigure getFormulaLabel()
   {
      return m_formulaLabel;
   }

   public void setData( Object Data )
   {
      super.setData(Data);
      if(Data instanceof OSBackendJoin)
      {
         OSBackendJoin join = (OSBackendJoin)Data;
         m_bFormula = join.getTranslator() == null ? false : true;
      }
   }

   /**
    * Get the right columns that are used by the translator
    */
   public Vector getFormulaColumns()
   {
      //to do take the columns from
      //the param values
      Vector vec = new Vector();

      if(getData() instanceof OSBackendJoin)
      {
         OSBackendJoin join = (OSBackendJoin)getData();

         if(join.getTranslator() != null && join.getTranslator().getParamValues() != null)
         {
            for(int iParam = 0; iParam < join.getTranslator().getParamValues().length; ++iParam)
            {

               if(join.getTranslator().getParamValues()[iParam].isBackEndColumn())
               {

                  String column = join.getTranslator().getParamValues()[iParam].getValue().getValueText();
                  StringTokenizer tokens = new StringTokenizer(column, ".");
                  while(tokens.hasMoreTokens())
                     column = tokens.nextToken();

                  vec.addElement(column);
               }
            }
         }
      }

      return vec;
   }

   public JoinSide getRightJoin()
   {
      return(m_rightJoin);
   }

   public JoinSide getLeftJoin()
   {
      return(m_leftJoin);
   }

   /**
   * Function for printing
   */
    public int print(Graphics g, PageFormat pf, int pageIndex)
              throws PrinterException
   {
       Vector points = new Vector();
      Component parent = this;
      while(parent != null && !(parent instanceof UIJoinMainFrame))
      {
         parent = parent.getParent();
      }

      //check for the page
      Point pageLoc = getPrintLocation();
      int iMovex = 0;
      int iMovey = 0;
      if(pageLoc != null)
      {
         if(pageLoc.x > 0)
            iMovex = pageLoc.x * (int)pf.getImageableWidth();

         if(pageLoc.y > 0)
            iMovey = pageLoc.y * (int)pf.getImageableHeight();
      }


      if(parent != null)
      {
         for(int iPoint = 0; iPoint < m_points.size(); ++iPoint)
         {
            Point loc = SwingUtilities.convertPoint(this, (Point)m_points.get(iPoint), parent);
            loc.translate(-iMovex, -iMovey);
            points.add(loc);
         }
      }

      Vector handles = new Vector();
      paintLine(g, points, handles);
      return Printable.PAGE_EXISTS;
   }

   /**
    * These 2 listeners are used to relay mouse events to a figure in the
    * frame that wants them.
    */
   private static UIJoinConnector m_tempConnector = null;
   private MouseListener m_figureMouseListener = null;
   private MouseMotionListener m_figureMouseMotionListener = null;
   private boolean m_bdragLeftSide = false;
   private boolean m_bdragMiddle = false;
   private JComponent m_glassPane = null;
   private DropTarget m_dropTarget = null;
   private JoinSide m_leftJoin = null;
   private JoinSide m_rightJoin = null;
   private Point m_lastPoint = null;
   /**
    * Stores all the points required to draw the line segments that make up the
    * connector. The polyline is drawn by drawing a line between pts 0, 1; then
    * between pt 1 and 2, etc.
    */
   private static final int LINE_WIDTH = 1;
/**
    * If a line segment is shorter than this value, the bounding box will be
    * longer than the segment, otherwise the bounding box is the same len as
    * the segment it surrounds. The value is in pixels.
    */
   private static final int MIN_SEGMENT_LEN = 5;
   /**
    * If a line segment is shorter than MIN_SEGMENT_LEN, the bounding box
    * will be extended beyond the segment on both ends by this amount. The
    * value is in pixels.
    */
   private static final int SEGMENT_BOUNDS_OFFSET = 2;
   private JoinComponentAdapter m_theComponentAdapter = null;
   private JoinInternalFrameAdapter m_theWindowAdapter = null;
   private JoinMouseAdapter m_theMouseAdapter = null;
   private JoinMouseMotionAdapter m_theMouseMotionAdapter = null;
   private Point m_ptReference = null;
   private Point m_ptLastMiddle = null;
   private boolean m_bLeftIsLeft = false;
   private UIJoinMainFrame m_parentFrame = null;
   private JoinArrow m_arrow = null;
   private JoinHand m_hand = null;
   private boolean m_bSideToSide = false;
   private boolean m_bFormula = false;
   private UIFigure m_formulaLabel = null;
   /**
    * The component that most recently had its mouse cursor changed during a
    * drag. It is null except during the the drag.
    */
   private static Component m_cursorSetOnComp = null;
   private static Cursor ms_cursorWillConnect;
   private static Cursor ms_cursorWontConnect;
   {
      PSResources rb = E2Designer.getResources();
      String strCursorResName = "ConnectCursor";
      ImageIcon icon = ResourceHelper.getIcon(rb, strCursorResName);
      if (null == icon)
         throw new MissingResourceException(E2Designer.getResources().
            getString("LoadIconFail"), "E2DesignerResources", strCursorResName);
      ms_cursorWillConnect = Toolkit.getDefaultToolkit().createCustomCursor(
         icon.getImage(), ResourceHelper.getPoint(rb , strCursorResName),
         strCursorResName);

      strCursorResName = "NoConnectCursor";
      icon = ResourceHelper.getIcon(rb, strCursorResName);
      if (null == icon)
         throw new MissingResourceException(E2Designer.getResources().
            getString("LoadIconFail"), "E2DesignerResources", strCursorResName);
      ms_cursorWontConnect = Toolkit.getDefaultToolkit().createCustomCursor(
         icon.getImage(), ResourceHelper.getPoint(rb, strCursorResName),
         strCursorResName);
   }
}
