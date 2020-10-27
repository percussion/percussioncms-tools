/******************************************************************************
 *
 * [ UIFigureFrame.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import com.percussion.E2Designer.browser.CatalogEntry;
import com.percussion.E2Designer.browser.FileHierarchyConstraints;
import com.percussion.E2Designer.browser.JavaExitNode;
import com.percussion.E2Designer.browser.SQLHierarchyConstraints;
import com.percussion.E2Designer.browser.TableNode;
import com.percussion.E2Designer.browser.XMLNode;
import com.percussion.client.IPSHierarchyNodeRef;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.PSObjectTypes.DataBaseObjectSubTypes;
import com.percussion.client.catalogers.PSCatalogDatasources;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.extension.PSExtensionDef;
import com.percussion.util.PSCollection;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.editors.form.PSXmlApplicationEditor;
import com.percussion.workbench.ui.editors.form.PSXmlApplicationEditorActionBarContributor;
import com.percussion.workbench.ui.legacy.PSLegacyDnDHelper;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Vector;

import static com.percussion.E2Designer.OSExitCallSet.EXT_TYPE_REQUEST_PRE_PROC;
import static com.percussion.E2Designer.OSExitCallSet.EXT_TYPE_RESULT_DOC_PROC;

/**
 * This is the base class for UIConnectableFigure editors. It provides all the
 * basic functionality that is needed for a drawing type editor such as:
 * <ul>
 * <li>Drag and drop</li>
 * <li>Individual and group selection</li>
 * <li>Editing operations such as cut, copy and paste</li>
 * </ul>
 * This class implements a very specialized editor. It works with any object
 * derived from UIConnectableFigure (UIC). UIC objects can be dragged within or
 * between any 2 windows of the same derived type. UIC figures contain 0 or more
 * connection points which will allow the user to connect certain objects
 * together. The frame window manages cursor feedback and the connections
 * themselves. The cursor will change to indicate whether the figure under the
 * cursor has a connection point that will accept a connection from the figure
 * being dragged.
 * </p>
 * Any figure in the frame may take over control of the mouse. Each time the
 * mouse is clicked in the frame, the figure under the frame is asked if it
 * wants mouse messages (via wantsMouse()). If it responds positivly, mouse
 * events will be passed on to the figure. While mouse events are being relayed,
 * no drag can be initiated. To get the events, the figure must implement
 * getMouseListener() and getMouseMotionListener(). At least one of these
 * methods must return a valid listener.
 * </p>
 * Custom drop actions are supported via the ICustomDropAction interface. Any
 * number of these may be added to the window. When an object is dropped, if the
 * object underneath the drop will not allow a connection or there is no object
 * under the drop point, the custom drop actions will be processed in the order
 * they were added.
 * </p>
 * The mainframe that owns this window will query it to get a list of menu items
 * for dynamic menus. If no items are provided and there are no menu items
 * provided by the mainframe, the menu item will be hidden. When the menu item
 * is selected, an object of the correct type will be added to the upper left
 * corner of the current viewport.
 * 
 * @see UIConnectableFigure
 * @see ICustomDropAction
 */
public abstract class UIFigureFrame extends JInternalFrame
      implements
         ClipboardOwner,
         DragGestureListener,
         DragSourceListener,
         DropTargetListener,
         IDynamicActions,
         ActionListener,
         Bookable,
         Printable
{
   private final static Logger ms_log = Logger.getLogger(UIFigureFrame.class);

   public UIFigureFrame(String strTitle, Dimension d,
         final PSXmlApplicationEditor xmlApplicationEditor)
   {
      // Iconifiable, resizable, closable, maximizable by default
      super(strTitle, true, true, true, true);
      m_xmlApplicationEditor = xmlApplicationEditor;
      // added this handler get around java bug 4194881
      addInternalFrameListener(new InternalFrameAdapter()
      {
         @Override
         public void internalFrameDeiconified(InternalFrameEvent event)
         {
            if (event.getSource() instanceof JInternalFrame)
            {
               JInternalFrame frame = (JInternalFrame) event.getSource();
               frame.toFront();
            }
         }
      });

      // use same icon as the mainframe window
      setFrameIcon(ResourceHelper.getIcon(E2Designer.getResources(),
            "ChildIconFilename"));

      setClosable(true); // allow closing this frame
      setDefaultCloseOperation(DISPOSE_ON_CLOSE); // dispose when closing

      // create and initialize the drawing pane
      m_drawingPane = new JLayeredPane();
      m_drawingPane.setLayout(null);
      m_drawingPane.setMaximumSize(d);
      m_drawingPane.setPreferredSize(d);
      m_drawingPane.setSize(m_drawingPane.getPreferredSize());
      if (isReadOnly())
         m_drawingPane.setBackground(Color.lightGray);
      else
         m_drawingPane.setBackground(Color.white);
      m_drawingPane.setVisible(true);
      m_drawingPane.setOpaque(true);
      m_drawingPane.setLocation(0, 0);

      JPanel gPane = new JPanel();
      gPane.setOpaque(false);
      setGlassPane(gPane);

      m_glassPane = new JPanel()
      {
         /**
          * This method adds the painting of the selection indicators. It is
          * done in the glass pane so the indicator will always be visible, even
          * if the entire component is hidden behind another.
          */
         @Override
         public void paintComponent(Graphics g)
         {
            super.paintComponent(g);
            Component[] comps = m_drawingPane.getComponents();
            for (int index = comps.length - 1; index >= 0; index--)
            {
               if (comps[index] instanceof UIConnectableFigure)
               {
                  UIConnectableFigure uic = (UIConnectableFigure) comps[index];
                  Point offset = uic.getLocation();
                  // put the Graphics context in the coord system of the
                  // component
                  g.translate(offset.x, offset.y);
                  uic.paintSelectionIndicator(g);
                  g.translate(-offset.x, -offset.y);
               }
            }
         }
      };

      m_glassPane.setMaximumSize(d);
      m_glassPane.setPreferredSize(d);
      m_glassPane.setSize(m_glassPane.getPreferredSize());
      m_glassPane.setOpaque(false);
      m_glassPane.setVisible(true);
      m_glassPane.setBackground(Color.yellow);
      m_glassPane.setLocation(0, 0);

      JLayeredPane contentPanel = new JLayeredPane();
      m_contentPanel = contentPanel;
      contentPanel.setBackground(new Color(140, 140, 140));
      contentPanel.setMaximumSize(d);
      contentPanel.setPreferredSize(d);
      contentPanel.setSize(contentPanel.getPreferredSize());
      contentPanel.setOpaque(true);
      contentPanel.add(m_drawingPane, JLayeredPane.DEFAULT_LAYER);
      contentPanel.add(m_glassPane, JLayeredPane.DRAG_LAYER);

      JScrollPane content = new JScrollPane(contentPanel);
      content.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

      setContentPane(content);

      // get the main scroll pane
      m_scrollPane = (JScrollPane) getContentPane();
      m_scrollPane.getViewport().addChangeListener(new ChangeListener()
      {
         public void stateChanged(@SuppressWarnings("unused") ChangeEvent event)
         {
            // reset the paste offset counter
            m_pasteOffsetCounter = 1;
         }
      });

      // Create the drop target. The reference for this is held internally
      // and so is not stored.
      new DropTarget(m_glassPane, DnDConstants.ACTION_COPY_OR_MOVE, this);

      DragSource src = new DragSource();
      src.addDragSourceListener(this);

      m_recognizers.add(src.createDefaultDragGestureRecognizer(m_glassPane,
            DnDConstants.ACTION_COPY_OR_MOVE, this));

      m_glassPane.addMouseListener(new MouseAdapter()
      {
         /*
          * When a mousePressed event occurs, the figure under the mouse is
          * queried to determine if it wants the mouse events by calling
          * wantsMouse(). The figure signifies its desire by returning <code>
          * true </code> . After requesting the mouse, all mouse events are sent
          * through a mouseReleased event. After the release, no more events are
          * sent. </p> Only 3 events are relayed: mousePressed, mouseReleased
          * and mouseDragged. Each event is passed unmodified. </p> At least one
          * of the 2 methods to get these listeners must return a valid
          * listener. If not, an assertion is issued.
          */
         @Override
         public void mousePressed(MouseEvent e)
         {
            // block all mouse events if read-only
            if (isReadOnly())
               return;
            
            m_bMouseCaptured = false;
            m_currentPointOver = null;
            if (e.isPopupTrigger())
            {
               callPopup(e);
               return;
            }

            Point framePt = e.getPoint();
            UIConnectableFigure uic = getConnectable(framePt);
            if (null == uic)
               return;

            Point figurePt = SwingUtilities.convertPoint(m_glassPane,
                  framePt.x, framePt.y, uic);

            addToSelection(uic, e.isShiftDown());

            if (!uic.wantsMouse(figurePt))
               return;

            m_hilite = new HiliteRect(m_glassPane);
            m_mouseOwner = uic;
            m_figureMouseListener = uic.getMouseListener(figurePt, m_glassPane);
            m_figureMouseMotionListener = uic.getMouseMotionListener(figurePt,
                  m_glassPane);

            if (null != m_figureMouseListener)
               m_figureMouseListener.mousePressed(e);

            // init the drag/connect stuff, if needed
            if (m_mouseOwner.isDragPoint(SwingUtilities.convertPoint(
                  m_drawingPane, e.getPoint(), m_mouseOwner)))
            {
               m_draggingComponent = m_mouseOwner;
               UIConnectionPoint cpFrompt = m_draggingComponent
                     .getCPFromPoint(figurePt);

               Properties props = m_draggingComponent.getDragInfoProperties();
               // if we are dragging on a UIConnectableFigure we may need to get
               // the constraints from the cp that we clicked on
               if (m_draggingComponent.getConnectionConstraint() == null
                     && cpFrompt != null
                     && cpFrompt.getAttacherConstraints() != null)
               {
                  props.put(UIConnectableFigure.CONSTRAINTS, cpFrompt
                        .getAttacherConstraints());
               }

               m_currentInfo = new DragInfo(m_draggingComponent, new Point(),
                     new UICIdentifier(m_draggingComponent.getFactoryName(),
                           m_draggingComponent.getId(figurePt)), getE2Server(),
                     props, m_draggingComponent.getDragImage());
               m_bAcceptDrag = true;
               m_bCanBeAttached = true;
            }
         }

         /**
          * If the figure under the mouse when it was clicked requested mouse
          * events and supplied a listener, this event is relayed to the figure.
          * After this, some local clean up is done so no more mouse events are
          * relayed. The event is passed unmodified.
          */
         @Override
         @SuppressWarnings("unchecked")
         public void mouseReleased(MouseEvent e)
         {
            // block all mouse events if read-only
            if (isReadOnly())
               return;

            if (e.isPopupTrigger())
            {
               callPopup(e);
               return;
            }

            if (null == m_mouseOwner)
            {
               // exit drag select mode
               if (m_bInDragSelect)
               {
                  if (null != m_lastSize)
                  {
                     // erase the last line
                     Graphics g = m_glassPane.getGraphics();
                     g.setColor(Color.black);
                     g.setXORMode(Color.white);

                     // create a normalized rect
                     int xoffset = m_lastSize.width >= 0 ? 0 : m_lastSize.width;
                     int yoffset = m_lastSize.height >= 0
                           ? 0
                           : m_lastSize.height;
                     Rectangle rect = new Rectangle(m_dragAnchor.x + xoffset,
                           m_dragAnchor.y + yoffset, 0 == xoffset
                                 ? m_lastSize.width
                                 : -m_lastSize.width, 0 == yoffset
                                 ? m_lastSize.height
                                 : -m_lastSize.height);
                     g.drawRect(rect.x, rect.y, rect.width, rect.height);

                     // find all figues in the selection rect
                     Component[] comps = m_drawingPane.getComponents();
                     boolean bAppend = false;
                     for (int index = comps.length - 1; index >= 0; index--)
                     {
                        UIConnectableFigure uic = (UIConnectableFigure) comps[index];
                        if (uic.isContainedBy(SwingUtilities.convertRectangle(
                              m_glassPane, rect, uic)))
                        {
                           // clear selection on the first add
                           addToSelection(uic, bAppend);
                           bAppend = true;
                        }

                     }
                  }
                  m_bInDragSelect = false;
               }

               return;
            }

            // this needs to happen before we pass the event to the component
            if (!m_bMouseCaptured && isOverFlexibleConnection(e.getPoint()))
            {
               UIConnectableFigure uic = getConnectable(e.getPoint());
               if (null != uic)
               {
                  List<IConnectionConstraint> constraints = null;
                  if (m_currentInfo.getFigureProperties().get(
                        UIConnectableFigure.CONSTRAINTS) != null
                        && m_currentInfo.getFigureProperties().get(
                              UIConnectableFigure.CONSTRAINTS) instanceof PSCollection)
                     constraints = (List<IConnectionConstraint>) m_currentInfo
                           .getFigureProperties().get(
                                 UIConnectableFigure.CONSTRAINTS);

                  UIConnectionPoint cp = uic.getClosestConnector(m_currentInfo
                        .getID().getID(), constraints, SwingUtilities
                        .convertPoint(m_glassPane, e.getPoint(), uic));
                  if (cp != null)
                     m_mouseOwner.createDynamicConnection(cp);
               }
            }

            if (null != m_figureMouseListener)
               m_figureMouseListener.mouseReleased(e);

            // clean up
            m_hilite.clear();
            m_hilite = null;
            m_figureMouseMotionListener = null;
            m_figureMouseListener = null;
            m_mouseOwner = null;
            m_draggingComponent = null;
            if (ms_dragUnderRect != null)
            {
               ms_dragUnderRect.clear();
            }

            m_currentInfo = null;
            if (null != m_cursorSetOnComp)
               m_cursorSetOnComp.setCursor(Cursor
                     .getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            m_cursorSetOnComp = null;
            m_bMouseCaptured = false;
         }

         @Override
         public void mouseClicked(MouseEvent e)
         {
            // block all mouse events if read-only
            if (isReadOnly())
               return;

            if (e.isPopupTrigger())
            {
               callPopup(e);
               return;
            }

            UIConnectableFigure uic = getConnectable(e.getPoint());
            boolean bLockAcquired = false;
            try
            {
               if (!acquireMouseActionLock())
                  return;
               bLockAcquired = true;
               if (e.getClickCount() >= 2)
               {
                  if (!(null != uic && uic.isHit(SwingUtilities.convertPoint(
                        m_glassPane, e.getPoint(), uic))))
                  {
                     // if point clicked did not hit a ConnectableFigure, do
                     // this...
                     onEdit();
                  }
                  else
                  {
                     if (uic.isEditable())
                        uic.onEdit(getData(), SwingUtilities.convertPoint(
                              m_glassPane, e.getPoint(), uic));
                  }
               }
            }
            finally
            {
               if (bLockAcquired)
                  releaseMouseActionLock();
            }
         }

         @Override
         public void mouseExited(@SuppressWarnings("unused") MouseEvent e)
         {
            // block all mouse events if read-only
            if (isReadOnly())
               return;

            E2Designer.getApp().getMainFrame().setStatusMessage(null);
         }
      });

      m_glassPane.addMouseMotionListener(new MouseMotionAdapter()
      {
         /**
          * If the figure under the mouse when it was clicked requested mouse
          * events and supplied a listener, this event is relayed to the figure.
          * The event is passed unchanged.
          */
         @Override
         @SuppressWarnings("unchecked")
         public void mouseDragged(MouseEvent e)
         {
            // block all mouse events if read-only
            if (isReadOnly())
               return;

            if (Debug.isEnabled())
            {
               E2Designer.getApp().getMainFrame().setStatusMessage(
                     e.getPoint().toString());
            }

            if (m_bInDragSelect)
            {
               Graphics g = m_glassPane.getGraphics();
               g.setColor(Color.black);
               g.setXORMode(Color.white);
               if (null != m_lastSize)
               {
                  int xoffset = m_lastSize.width >= 0 ? 0 : m_lastSize.width;
                  int yoffset = m_lastSize.height >= 0 ? 0 : m_lastSize.height;
                  g.drawRect(m_dragAnchor.x + xoffset,
                        m_dragAnchor.y + yoffset, 0 == xoffset
                              ? m_lastSize.width
                              : -m_lastSize.width, 0 == yoffset
                              ? m_lastSize.height
                              : -m_lastSize.height);

               }
               else
                  m_lastSize = new Dimension();
               m_lastSize.setSize(e.getPoint().x - m_dragAnchor.x,
                     e.getPoint().y - m_dragAnchor.y);
               int xoffset = m_lastSize.width >= 0 ? 0 : m_lastSize.width;
               int yoffset = m_lastSize.height >= 0 ? 0 : m_lastSize.height;
               g.drawRect(m_dragAnchor.x + xoffset, m_dragAnchor.y + yoffset,
                     0 == xoffset ? m_lastSize.width : -m_lastSize.width,
                     0 == yoffset ? m_lastSize.height : -m_lastSize.height);
               return;
            }

            if (null == m_figureMouseMotionListener)
               return;
            m_figureMouseMotionListener.mouseDragged(e);

            Component comp = e.getComponent();
            if (comp != m_cursorSetOnComp)
            {
               if (null != m_cursorSetOnComp)
                  m_cursorSetOnComp.setCursor(Cursor
                        .getPredefinedCursor(Cursor.DEFAULT_CURSOR));
               m_cursorSetOnComp = comp;
            }

            Debug.assertTrue(null != m_cursorSetOnComp,
                  "cursorSetOnComponent unexpectedly null");
            if (isOverFlexibleConnection(e.getPoint()))
            {
               m_cursorSetOnComp.setCursor(ms_cursorWillConnect);
               // show the hilite on the cp
               UIConnectableFigure uic = getConnectable(e.getPoint());
               if (null != uic)
               {
                  List<IConnectionConstraint> constraints = null;
                  if (m_currentInfo.getFigureProperties().get(
                        UIConnectableFigure.CONSTRAINTS) != null
                        && m_currentInfo.getFigureProperties().get(
                              UIConnectableFigure.CONSTRAINTS) instanceof PSCollection)
                     constraints = (List<IConnectionConstraint>) m_currentInfo
                           .getFigureProperties().get(
                                 UIConnectableFigure.CONSTRAINTS);

                  UIConnectionPoint cp = uic.getClosestConnector(m_currentInfo
                        .getID().getID(), constraints, SwingUtilities
                        .convertPoint(m_drawingPane, e.getPoint(), uic));

                  if (cp != null)
                  {
                     m_currentPointOver = cp;
                     m_hilite.setRect(new Rectangle(SwingUtilities
                           .convertPoint(uic, cp.getLocation(), m_glassPane),
                           cp.getSize()));
                  }
               }
            }
            else
            {
               m_cursorSetOnComp.setCursor(ms_cursorWontConnect);
               m_hilite.clear();
               m_currentPointOver = null;
            }
         }

         /**
          * If mouse cursor is over a UIC, its tool tip will be displayed.
          */
         @Override
         public void mouseMoved(MouseEvent e)
         {
            // block all mouse events if read-only
            if (isReadOnly())
               return;

            UIConnectableFigure uic = getConnectable(e.getPoint());
            if (null != uic)
            {
               if (null == m_glassPane.getToolTipText() || uic != m_oldUic)
               {
                  m_oldUic = uic;
                  m_glassPane.setToolTipText(uic.getLabelToolTip());
                  ToolTipManager.sharedInstance().mouseEntered(e);
               }
            }
            else
            // when mouse moved off of the figure...
            {
               // remove the tooltip from the glassPane
               m_glassPane.setToolTipText(null);
               m_oldUic = null;
               ToolTipManager.sharedInstance().mouseExited(e);
            }

            if (!m_bInDrop && !m_bInDragSelect && !m_bInDrag)
            {
               final MouseEvent me = e;
               final UIConnectableFigure fuic = uic;
               SwingUtilities.invokeLater(new Runnable()
               {
                  public void run()
                  {
                     Component comp = me.getComponent();
                     if (comp != m_cursorSetOnCompForMove)
                     {
                        m_cursorSetOnCompForMove = comp;
                     }

                     if (null != m_cursorSetOnCompForMove)
                     {
                        // change cursor
                        boolean bWants = false;

                        if (fuic != null)
                        {
                           Point figurePt = SwingUtilities.convertPoint(
                                 m_glassPane, me.getPoint(), fuic);

                           UIConnectionPoint cp = fuic.getCPFromPoint(figurePt);
                           if (cp != null
                                 && cp instanceof UIFlexibleConnectionPoint)
                           {
                              if (!m_cursorSetOnCompForMove.getCursor()
                                    .getName().equals(
                                          ms_cursorWillConnect.getName()))
                              {
                                 m_cursorSetOnCompForMove
                                       .setCursor(Cursor
                                             .getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                              }
                              bWants = true;
                           }
                        }

                        if (!bWants)
                        {
                           if (!m_cursorSetOnCompForMove.getCursor().getName()
                                 .equals(
                                       Cursor.getPredefinedCursor(
                                             Cursor.DEFAULT_CURSOR).getName()))
                           {
                              m_cursorSetOnCompForMove
                                    .setCursor(Cursor
                                          .getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                           }
                        }
                     }
                  }
               });
            }
         }

      });

      // set frame size
      this.setMaximumSize(d);
      this.setPreferredSize(d);
      this.setSize(this.getPreferredSize());
   }

   /**
    * Used by mouseMoved() method to see if the mouse cursor moved off of a
    * Figure and directly onto another Figure.
    */
   protected UIConnectableFigure m_oldUic = null;

   // Properties
   /**
    * This is a convenience method.
    * 
    * @returns the E2 server that this application was created on
    */
   public String getE2Server()
   {
      return E2Designer.getApp().getMainFrame().getE2Server();
   }

   /**
    * Returns <code>true</code> if this object has data that needs to be saved
    * to persistent storage, <code>false</code> otherwise.
    * <p>
    * Should return <code>false</code> unless data object implements the
    * IPersist interface.
    * 
    * @see IPersist
    */
   public boolean hasPersistableData()
   {
      return false;
   }

   // Operations
   /**
    * Returns the figure factory that is used by the derived class.
    */
   abstract protected FigureFactory getFigureFactory();

   /**
    * Returns the data object that is associated with this visual
    * representation.
    * <p>
    * 
    * @return null unless overridden by derived class.
    */
   public Object getData()
   {
      return null;
   }

   /**
    * Provides a special case to perform an action when the user clicks within
    * the area of the frame, but not on an object within the frame. The
    * functionality of this method will be implemented at the extended level of
    * this class. Check the constructor (mouseClicked method) for more
    * information.
    */
   public void onEdit()
   {
      return;
   }
   
   public boolean isMenuViewPropertiesEnabled()
   {
      
      if (getSelected().size() != 1)
      {
         return false;
      }
      final UIConnectableFigure uic = getSelected().get(0);
      // checks for the class name of the editor b/c it is created by
      // Class.newInstance(); therefore, getEditor() will always return
      // null
      return uic == null || uic.getEditorName() != null; 
   }

   /**
    * @return Vector The list of DynamicActionListeners that is relevant to this
    *         InternalFrame.
    */
   public List<Action> getDynamicActionListeners()
   {
      // register for actions in the mainframe
      List<Action> actionListeners = new ArrayList<Action>();
      {
         final Action action = new Action()
         {
            @Override
            public void run()
            {
               cutSelectedFigures();
            }
         };
         action.setId(PSXmlApplicationEditorActionBarContributor.MENU_EDIT_CUT);
         actionListeners.add(action);
      }

      {
         final Action action = new Action()
         {
            @Override
            public void run()
            {
               copySelectedFigures();
            }
         };
         action.setId(PSXmlApplicationEditorActionBarContributor.MENU_EDIT_COPY);
         actionListeners.add(action);
      }

      {
         final Action action = new Action()
         {
            @Override
            public void run()
            {
               pasteSelectedFigures();
            }
         };
         action.setId(PSXmlApplicationEditorActionBarContributor.MENU_EDIT_PASTE);
         actionListeners.add(action);
      }
      {
         final Action action = new Action()
         {
            @Override
            public void run()
            {
               clearSelectedFigures();
            }
         };
         action.setId(PSXmlApplicationEditorActionBarContributor.MENU_EDIT_CLEAR);
         actionListeners.add(action);
      }

      {
         final Action action = new Action()
         {
            @Override
            public void run()
            {
               selectAllFigures();
            }
         };
         action.setId(PSXmlApplicationEditorActionBarContributor.MENU_EDIT_SELECTALL);
         actionListeners.add(action);
      }

      {
         final Action action = new Action()
         {
            @Override
            public void run()
            {
               deselectAllFigures();
            }
         };
         action.setId(PSXmlApplicationEditorActionBarContributor.MENU_EDIT_DESELECTALL);
         actionListeners.add(action);
      }
      
      // registering VIEW/Properties action for the menu item
      // Andriy: disabled as it caused problems during initialization and we
      // currently do not call this and the actions above from menu
      if (false && isMenuViewPropertiesEnabled())
      {
         final Action action = new Action()
         {
            @Override
            public void run()
            {
               E2Designer.getApp().getMainFrame().actionViewProperties();
            }
         };
         action.setId(PSXmlApplicationEditorActionBarContributor.MENU_VIEW_PROPERTIES);
         actionListeners.add(action);
      }
      return actionListeners;
   }

   /**
    * When inserting a figure, the insertion point is translated by this many
    * pixels in the x and y direction before insert.
    */
   private final static int INSERT_OFFSET = 10;

   /**
    * Inserts a new figure into the uuper left corner of the editing window's
    * current viewport. If there is a figure already there, it shifts the insert
    * point down and to the right. This is repeated until the edge of the window
    * is reached, at which point it is inserted.
    * 
    * @param strFigureTypeName an object name that was obtained via the
    *           getFigureNames method, not empty or <code>null</code>.
    * 
    * @return the created figure if successful, <code>null</code> otherwise.
    * 
    * @throws UnsupportedOperationException if strFigureName does not name a
    *            figure that was obtained from <code>getFigureNames()</code>.
    */
   public UIConnectableFigure add(String strFigureTypeName)
   {
      if (null == strFigureTypeName || 0 == strFigureTypeName.trim().length())
         throw new UnsupportedOperationException(
               "Figure name cannot be empty or null.");

      UIConnectableFigure uic = null;
      try
      {
         uic = getFigureFactory().createFigure(strFigureTypeName);

         Rectangle viewBounds = m_scrollPane.getViewport().getViewRect();
         Point offset = viewBounds.getLocation();
         offset.translate(INSERT_OFFSET, INSERT_OFFSET);
         Component[] comps = m_drawingPane.getComponents();
         boolean bDone = false;
         while (!bDone)
         {
            int index;
            for (index = comps.length - 1; index >= 0; index--)
            {
               if (offset.equals(comps[index].getLocation()))
               {
                  offset.translate(INSERT_OFFSET, INSERT_OFFSET);
                  break;
               }
            }
            if (index < 0)
               bDone = true;
            else if (offset.x >= viewBounds.x + viewBounds.width
                  || offset.y >= viewBounds.y + viewBounds.height)
            {
               bDone = true;
            }
         }
         uic.setLocation(offset.x, offset.y);
      }
      catch (Exception e)
      {
         PSDlgUtil.showError(e, true, E2Designer.getResources().getString(
               "OpErrorTitle"));
      }
      return null == uic ? null : add(uic, 0);
   }

   /**
    * Adds a new figure into the editing window's current viewport (the content
    * pane). It is placed at the top of the z-order. Note that this is opposite
    * to the container's add method, which places it at the bottom.
    * 
    * @param uic the figure to add
    * 
    * @returns the passed in figure
    */
   public UIConnectableFigure add(UIConnectableFigure uic)
   {
      return add(uic, 0);
   }

   /**
    * Inserts a new figure into the editing window's current viewport (the
    * content pane). The figure is placed at the z-order level specified by
    * iIndex, 0 being the top.
    * 
    * @param uic the figure to add. If the figure is null, nothing is done and
    *           null is returned.
    * 
    * @param iIndex the position to insert the figure, -1 to insert at the
    *           end/bottom. 0 is the top of the z-order.
    * 
    * @returns the passed in figure, or null if any errors occur
    */
   public UIConnectableFigure add(UIConnectableFigure uic, int iIndex)
   {
      if (null == uic)
         return (null);

      try
      {
         if (-1 == iIndex)
            m_drawingPane.add(uic, getLayer(uic));
         else
            m_drawingPane.add(uic, getLayer(uic), iIndex);
         m_drawingPane.repaint(uic.getBounds());

         return uic;
      }
      catch (Exception e)
      {
         e.printStackTrace();
         return (null);
      }
   }

   // selection handling
   /**
    * Calls getSelectionSet and serializes this object and copies it to the
    * supplied clipboard.
    * 
    * @returns true if successfully copied, false if any problems occur or there
    *          is nothing to copy
    * 
    * @see #getSelectionSet
    * @see #hasSelection
    */
   public boolean copyToClipboard(@SuppressWarnings("unused") Clipboard cb)
   {
      return false;
   }

   /**
    * Checks the system clipboard for an acceptable data flavor. If one is
    * available, the object is deserialized and added to the upper left corner
    * of the window.
    * 
    * @returns true if object successfully pasted, false otherwise
    * 
    * @param cb a clipboard to paste from, may be the system clipboard
    */
   public boolean pasteFromClipboard(@SuppressWarnings("unused") Clipboard cb)
   {
      return false;
   }

   /**
    * Returns true if there are any selected figures in this window. This is
    * typically used to determine if a editing menu items should be grayed.
    */
   public boolean hasSelection()
   {
      return false;
   }

   /**
    * All of the figures that are currently selected are added to a new
    * component, which is returned. This component can then be used in clipboard
    * and DnD operations.
    */
   public JComponent getSelectionSet()
   {
      return null;
   }

   /**
    * Deselects all figures that are selected and repaints them.
    */
   public void clearSelection()
   {
      Component[] comps = m_drawingPane.getComponents();
      for (int index = comps.length - 1; index >= 0; index--)
      {
         if (comps[index] instanceof UIConnectableFigure)
         {
            UIConnectableFigure uic = (UIConnectableFigure) comps[index];
            if (uic.isSelected())
            {
               m_glassPane.repaint(uic.getBounds());
            }
            // always clear selection so it gets propagated to children
            uic.setSelection(false, false);
         }
      }
   }

   /**
    * Returns an array that contains all the figures that are currently
    * selected.
    */
   public UIConnectableFigure[] getSelectedFigures()
   {
      UIConnectableFigure aSelectedFigs[] = new UIConnectableFigure[1];
      aSelectedFigs[0] = null;
      return aSelectedFigs;
   }

   /**
    * Removes all figures that are currently selected (including any attached
    * figures) from this editing window. This will typically be used for
    * keyboard interaction.
    */
   public void cutSelectedFigures()
   {
      pushSelection(true);
   }

   /**
    * Take all selected figures and copy them onto the clipboard
    */
   public void copySelectedFigures()
   {
      pushSelection(false);
   }

   public void pushSelection(boolean bCut)
   {
      // reset the paste offset counter
      m_pasteOffsetCounter = 1;

      /*
       * we need to go through the selections, building a new vector. we'll copy
       * each selected item into the vector, then transfer the entire vector to
       * the clipboard. If we're in Cut mode, we can go back through the
       * selection set and cut the components after the copy. If we do it in the
       * first loop, the item goes invisible, which doesn't look very good when
       * it's pasted :-)
       */
      Vector<UIConnectableFigure> uics = new Vector<UIConnectableFigure>();
      Vector<UIConnectableFigure> comps = getSelected();
      for (int index = comps.size() - 1; index >= 0; index--)
      {
         UIConnectableFigure uic = comps.get(index);
         uics.add(uic);
      }

      if (uics.size() > 0)
      {
         try
         {
            CopyableFigureTransfer ft = new CopyableFigureTransfer(uics);
            Clipboard board = Toolkit.getDefaultToolkit().getSystemClipboard();
            if (board != null)
               board.setContents(ft, this);

            if (bCut)
            {
               for (int index = comps.size() - 1; index >= 0; index--)
               {
                  final UIConnectableFigure uic = comps.get(index);
                  closeFigureEditor(uic);
                  uic.remove();
                  // Add this component to delete list.
                  componentRemoved(uic);
               }
            }
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }
   }

   /**
    * Adjust the provided figures location by xOffset/yOffset.
    * 
    * @param uic the figure
    * @param xOffset the x offset
    * @param yOffset the y offset
    */
   private void adjustFigureLocation(UIConnectableFigure uic, Point delta,
         int xOffset, int yOffset)
   {
      // get viewport location
      Rectangle viewBounds = m_scrollPane.getViewport().getViewRect();
      Point offset = viewBounds.getLocation();

      // set new location
      int currentX = uic.getLocation().x;
      int currentY = uic.getLocation().y;
      offset.translate(currentX - delta.x + xOffset, currentY - delta.y
            + yOffset);
      uic.setLocation(offset.x, offset.y);
   }

   /**
    * Get the top left corner of the smallest possible virtual rectangle around
    * all provided figures.
    * 
    * @param comps a vector of components
    * @return Point the smallest virtual rectangle around all figures
    */
   private Point getFiguresTopLeftCorner(Vector comps)
   {
      Point pt = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
      for (int i = 0; i < comps.size(); ++i)
      {
         if (comps.get(i) instanceof UIConnectableFigure)
         {
            UIConnectableFigure fig = (UIConnectableFigure) comps.get(i);
            int x = fig.getLocation().x;
            int y = fig.getLocation().y;
            if (x < pt.x)
               pt.x = x;
            if (y < pt.y)
               pt.y = y;
         }
      }

      return pt;
   }

   public void pasteSelectedFigures()
   {
      try
      {
         Clipboard board = Toolkit.getDefaultToolkit().getSystemClipboard();
         if (board != null)
         {
            Transferable contents = board.getContents(this);

            if (contents != null)
            {
               DataFlavor copyFlavor = new DataFlavor(
                     FigureTransfer.sUICCOL_FLAVOR_TYPE,
                     FigureTransfer.sUICCOL_FLAVOR_NAME);
               if (contents.isDataFlavorSupported(copyFlavor))
               {
                  final byte[] transferData =
                        (byte[]) contents.getTransferData(copyFlavor);
                  if (transferData == null)
                  {
                     // happens on paste after dragging as application uses DnD
                     //for dragging too
                     return;
                  }
                  java.io.ByteArrayInputStream bIn =
                        new java.io.ByteArrayInputStream(transferData);
                  java.io.ObjectInputStream objIn = new java.io.ObjectInputStream(
                        bIn);

                  Object data = objIn.readObject();
                  if (data instanceof Vector)
                  {
                     Vector uics = (Vector) data;
                     Point delta = getFiguresTopLeftCorner(uics);
                     int offset = m_pasteOffsetCounter++ * PASTE_OFFSET;
                     for (int iuic = 0; iuic < uics.size(); ++iuic)
                     {
                        if (uics.get(iuic) instanceof UIConnectableFigure)
                        {
                           UIConnectableFigure uic = (UIConnectableFigure) uics
                                 .get(iuic);
                           System.out.println("...figure: " + uic.getName()
                                 + "location (x/y): " + uic.getLocation().x
                                 + "/" + uic.getLocation().y);
                           if (!(uic instanceof UIConnector))
                              adjustFigureLocation(uic, delta, offset, offset);

                           add(uic, 0);
                        }
                     }
                  }
               }
            }
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      catch (UnsupportedFlavorException e)
      {
         e.printStackTrace();
      }
      catch (ClassNotFoundException e)
      {
         e.printStackTrace();
      }
   }

   public void selectAllFigures()
   {
      Vector comps = getAll();
      for (int index = comps.size() - 1; index >= 0; index--)
      {
         if (comps.get(index) instanceof UIConnectableFigure)
         {
            UIConnectableFigure uic = (UIConnectableFigure) comps.get(index);
            if (!uic.isSelected())
            {
               uic.setSelection(true, true);
               m_glassPane.repaint(uic.getBounds());
            }
         }
      }
   }

   public void deselectAllFigures()
   {
      Vector comps = getAll();
      for (int index = comps.size() - 1; index >= 0; index--)
      {
         if (comps.get(index) instanceof UIConnectableFigure)
         {
            UIConnectableFigure uic = (UIConnectableFigure) comps.get(index);
            if (uic.isSelected())
            {
               uic.setSelection(false, false);
               m_glassPane.repaint(uic.getBounds());
            }
         }
      }
   }

   /**
    * Removes all figures that are currently selected when either 'Delete' from
    * the keyboard or 'Clear' in the 'Edit' menu action is performed. If one or
    * more resources are found among selected figures, an info message box
    * pops-up asking for a confirmation of the action that is about to be
    * performed.
    */
   public void clearSelectedFigures()
   {
      Vector comps = getSelected();
      // Is there any resource among selected figures?
      boolean foundRes = isResourceSel(comps);
      int option = 0;
      if (foundRes)
      {
         option = PSDlgUtil.showConfirmDialog(E2Designer.getResources()
               .getString("deleteRes"), E2Designer.getResources().getString(
               "deleteConf"), JOptionPane.YES_NO_OPTION,
               JOptionPane.QUESTION_MESSAGE);
         if (option != JOptionPane.YES_OPTION)
         {
            // deselect all currently selected figures, and return
            deselectAllFigures();
            return;
         }
      }

      for (int index = comps.size() - 1; index >= 0; index--)
      {
         if (comps.get(index) instanceof UIConnectableFigure)
         {
            final UIConnectableFigure uic = (UIConnectableFigure) comps.get(index);
            closeFigureEditor(uic);
            // always clear selection so it gets propagated to children
            uic.remove();
            // Add this component to delete list.
            componentRemoved(uic);
         }
      }
      // m_glassPane.repaint(m_glassPane.getBounds());
   }

   /**
    * Closes figure editor if it is open.
    * @param uic the figure to close editor for if it is open.
    * Assumed not <code>null</code>.
    */
   private void closeFigureEditor(final UIConnectableFigure uic)
   {
      if (uic.isEdited() && uic.getEditor() instanceof JInternalFrame)
      {
         m_xmlApplicationEditor.closeTabWithControl(
               (JInternalFrame) uic.getEditor());
      }
   }

   /**
    * Base class implementation does nothing. Derived class may do something
    * relevant.
    * 
    * @param uic UI component just removed from the frame, never
    *           <code>null</code>.
    */
   public void componentRemoved(
         @SuppressWarnings("unused") UIConnectableFigure uic)
   {
      // Do nothing. Derived class may override to do something
   }

   public Vector<UIConnectableFigure> getSelected()
   {
      Vector<UIConnectableFigure> selected = new Vector<UIConnectableFigure>();
      Component[] comps = m_drawingPane.getComponents();
      for (int index = comps.length - 1; index >= 0; index--)
      {
         if (comps[index] instanceof UIConnectableFigure)
         {
            UIConnectableFigure uic = (UIConnectableFigure) comps[index];
            uic.getSelected(selected);
         }
      }

      return selected;
   }

   public Vector getAll()
   {
      Vector<UIConnectableFigure> selected = new Vector<UIConnectableFigure>();
      Component[] comps = m_drawingPane.getComponents();
      for (int index = comps.length - 1; index >= 0; index--)
      {
         if (comps[index] instanceof UIConnectableFigure)
         {
            UIConnectableFigure uic = (UIConnectableFigure) comps[index];
            uic.getAll(selected);
         }
      }

      return selected;
   }

   /**
    * Adds the supplied listener to the list of listeners for this frame. The
    * new listener is added at the end of the list. All registered listeners
    * will get notified when a user drops an object that is not attaching to an
    * existing object. Notification will occur in the order the listeners were
    * added. If the listener is null, no action is taken.
    */
   public void addActionListener(ICustomDropAction listener)
   {
      if (null != listener)
         m_customDropActions.add(listener);
   }

   /**
    * Removes the supplied listener from the registered list. No action is taken
    * if it is not found or the listener is null.
    */
   public void removeActionListener(ICustomDropAction listener)
   {
      if (null != listener)
         m_customDropActions.remove(listener);
   }

   /**
    * Allows derived classes a chance to place different figures in different
    * layers. By default, JLayeredPane.DEFAULT_LAYER is returned. Higher layer
    * numbers will display above lower layer numbers.
    * 
    * @param uic for use by derived classes to make a determination of what
    *           layer to use.
    * 
    * @returns an int layer number wrapped as an object which can be passed as a
    *          constraint to the container add method.
    */
   protected Integer getLayer(
         @SuppressWarnings("unused") UIConnectableFigure uic)
   {
      return JLayeredPane.DEFAULT_LAYER;
   }

   // variables

   // debugging
   /**
    * Adds the specified figure to the current selection set. This method is
    * provided to give programatic access for debugging purposes. Most selection
    * set modifications will happen via mouse interactions from the user.
    * 
    * @param uic any figure in the window.
    * 
    * @throws IllegalArgumentException if uic is not a figure in this window
    */
   void addToSelection(UIConnectableFigure uic, boolean bAppend)
   {
      if (!bAppend)
         clearSelection();

      if (uic.isSelected())
      {
         uic.setSelection(false, bAppend);
         m_glassPane.repaint(uic.getBounds());
      }
      else
      {
         uic.setSelection(true, bAppend);
         uic.paintSelectionIndicator(uic.getGraphics());
         m_glassPane.repaint(uic.getBounds());
      }
   }

   /**
    * Removes the specified figure from the current selection set. This method
    * is provided to give programatic access for debugging purposes. Most
    * selection set modifications will happen via mouse interactions from the
    * user.
    * 
    * @param uic a figure in the current selection set. Should be one of the
    *           figures returned from the getSelectedFigures() method.
    * 
    * @throws IllegalArgumentException if uic is not a selected figure in this
    *            window, (for debugging purposes only)
    */
   void removeFromSelection(@SuppressWarnings("unused") UIConnectableFigure uic)
   {
   }

   /**
    * Returns an array of strings that are names of objects that can be inserted
    * into this editing window, using the add() method.
    * <p>
    * This method is for debugging purposes.
    */
   protected String[] getFigureNames()
   {
      return null;
   }

   // ClipboardOwner interface implementation
   public void lostOwnership(
         @SuppressWarnings("unused") Clipboard cb,
         @SuppressWarnings("unused") Transferable Data)
   {

   }

   // DragGestureListener interface implementation
   public void dragGestureRecognized(DragGestureEvent dge)
   {
      // block all drops if read-only
      if (isReadOnly())
         return;

      /*
       * The problem: click/release on a component in an inactive frame and the
       * component goes into drag mode. This appears to be a bug in the
       * WMouseDragGestureRecognizer class, but I did not track it down
       * completely. What happens in this case is that 2 events come thru in the
       * dge, the mouse pressed, and a mouse exited. When drags start normally,
       * there is never a mouse exited. I use this knowledge to prevent dnd from
       * starting when the click comes while the window is not active. NOTE:
       * This could possibly affect dnd gestures on other platforms.
       * 
       * @TODO (ph) check if it affects other platforms and isolate problem in
       * AWT
       */
      Object[] events = dge.toArray();
      if (events.length > 1
            && MouseEvent.MOUSE_EXITED == ((MouseEvent) events[events.length - 1])
                  .getID())
      {
         return;
      }
      // does the component want to handle the mouse itself
      Point ptOrigin = dge.getDragOrigin();
      UIConnectableFigure uic = getConnectable(ptOrigin);
      if (null == uic)
      {
         // set up for drag select mode
         m_bInDragSelect = true;
         m_dragAnchor = ptOrigin;
         m_lastSize = null;
         return;
      }

      Point click = SwingUtilities.convertPoint(m_drawingPane, ptOrigin.x,
            ptOrigin.y, uic);

      if (null != m_mouseOwner || !uic.isDragPoint(click))
      {
         return;
      }
      boolean bError = true;
      boolean bLockAcquired = false;
      try
      {
         if (!acquireMouseActionLock())
            return;
         bLockAcquired = true;
         uic.dragStart();
         m_draggingComponent = uic;

         Point ptDragOrigin = dge.getDragOrigin();
         Point ptComp = m_draggingComponent.getLocation();
         Container parent = m_draggingComponent.getParent();
         ptComp = SwingUtilities.convertPoint(parent, ptComp, m_glassPane);

         Point ptOffset = new Point(ptDragOrigin.x - ptComp.x, ptDragOrigin.y
               - ptComp.y);
         DragInfo info = new DragInfo(m_draggingComponent, ptOffset,
               new UICIdentifier(m_draggingComponent.getFactoryName(),
                     m_draggingComponent.getId()), getE2Server(),
               m_draggingComponent.getDragInfoProperties(), m_draggingComponent
                     .getDragImage());
         Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();

         ms_dragUnderRect = new HiliteRect(m_glassPane, 1);
         ms_dragUnderRect.setOffset(click);
         Rectangle rect = m_draggingComponent.getBounds();
         rect.setLocation(ptOrigin);
         ms_dragUnderRect.setRect(rect);

         Transferable data = new FigureTransfer(m_draggingComponent, info);
         cb.setContents(data, this);
         dge.startDrag(DragSource.DefaultMoveDrop, null, dge.getDragOrigin(),
               data, this);

         bError = false;
         m_bInDrag = true;
      }
      catch (InvalidDnDOperationException e)
      {
         ms_log.warn(e);
      }
      finally
      {
         if (bError)
         {
            m_draggingComponent = null;
            ms_dragUnderRect.clear();
         }
         if (bLockAcquired)
            releaseMouseActionLock();
      }
   }

   // DragSourceListener interface implementation
   public void dragEnter(@SuppressWarnings("unused") DragSourceDragEvent dsde)
   {
      // Do nothing
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.awt.dnd.DragSourceListener#dragOver(java.awt.dnd.DragSourceDragEvent)
    */
   public void dragOver(DragSourceDragEvent dsde)
   {
      DragSourceContext ctx = dsde.getDragSourceContext();

      int action = dsde.getDropAction();
      if (action == DnDConstants.ACTION_COPY)
      {
         ctx.setCursor(DragSource.DefaultCopyDrop);
      }
      else if (action == DnDConstants.ACTION_MOVE)
      {
         ctx.setCursor(DragSource.DefaultMoveDrop);
      }
      else
      {
         ctx.setCursor(DragSource.DefaultMoveNoDrop);
      }
   }

   public void dropActionChanged(
         @SuppressWarnings("unused") DragSourceDragEvent dsde)
   {
   }

   public void dragExit(@SuppressWarnings("unused") DragSourceEvent dse)
   {
   }

   public void dragDropEnd(DragSourceDropEvent dsde)
   {
      /*
       * We need a SwingWorker here since there is one used in the Drop()
       * method. The Drop method exits and this method may be called before the
       * AWT thread executes the SwingWorker code from the Drop method. Using a
       * SwingWorker here as well forces this code to be executed AFTER the code
       * in the Drop method's SwingWorker.
       */
      final DragSourceDropEvent finaldsde = dsde;
      new SwingWorker()
      {
         @Override
         public Object construct()
         {
            SwingUtilities.invokeLater(new Runnable()
            {
               public void run()
               {

                  try
                  {
                     E2Designer.getApp().getMainFrame().setWaitCursor();
                     if (0 != (m_dropAction & DnDConstants.ACTION_MOVE))
                     {
                        Debug.assertTrue(null != m_draggingComponent,
                                    E2Designer.getResources(),
                                    "DragCompNotSet", null);
                        JComponent parent = (JComponent) m_draggingComponent
                              .getParent();
                        if (!m_bDroppedInSrc)
                        {
                           parent.remove(m_draggingComponent);
                        }
                        parent.repaint(m_draggingComponent.getBounds());
                     }
                     if (finaldsde != null && m_draggingComponent != null)
                     {
                        m_draggingComponent.dragEnd(0 != (finaldsde
                              .getDropAction() & DnDConstants.ACTION_COPY));
                     }

                     m_bDroppedInSrc = false;
                     m_bInDrag = false;
                     m_draggingComponent = null;
                     ms_dragUnderRect.clear();
                  }
                  finally
                  {
                     // clean up
                     m_currentInfo = null;
                     m_dropAction = 0;
                  }

               }
            });

            return "done";
         }

         @Override
         public void interrupt()
         {
            E2Designer.getApp().getMainFrame().clearWaitCursor();
            super.interrupt();
            m_currentInfo = null;
         }

         @Override
         public void finished()
         {
            E2Designer.getApp().getMainFrame().clearWaitCursor();
            m_currentInfo = null;
         }
      };

   }
   
   /**
    * Was this frame loaded read-only?
    * 
    * @return <code>true</code> if read-only, <code>false</code> otherwise.
    */
   protected boolean isReadOnly()
   {
      PSXmlApplicationEditor editor = getXmlApplicationEditor();
      return editor != null && editor.isReadOnly();
   }

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
      m_hilite = new HiliteRect(m_glassPane);
      m_bAcceptDrag = false; // initialize this flag
      m_bCanBeAttached = true;
      maybeInitDragUnderRect(dtde);

      try
      {
         // block all drops if read-only
         if (isReadOnly())
            return;
         
         // does the figure under the cursor want to connect to the dragging
         // fig?
         boolean bCompAcceptDrag = false;

         if (dtde.isDataFlavorSupported(getInfoFlavor()))
         {
            Debug.assertTrue(dtde.isDataFlavorSupported(
                  getConnectableFigureFlavor()),
                  E2Designer.getResources(), "MissingFlavor", null);
            final Transferable t =
               Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);
            m_currentInfo = (DragInfo) t.getTransferData(getInfoFlavor());
            bCompAcceptDrag = pollAnybodyNeedsDrop(dtde);
         }
         else if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
         {
            if (isAcceptableDropFilesList(getFilesFromDnD(dtde)))
            {
               final Transferable t = getTransferableFromFileDropEvent(dtde);
               m_currentInfo = (DragInfo) t.getTransferData(getInfoFlavor());
               bCompAcceptDrag = pollAnybodyNeedsDrop(dtde);
            }
         }
         else if (getDnDHelper().isRecognizedLegacyDrop(dtde))
         {
            final PSUiReference uiRef = getDroppedRef(dtde); 
            if (uiRef == null || uiRef.getReference() == null)
            {
               return;
            }
            final Transferable t = getTransferrableFromDrop(uiRef);
            if (t == null)
            {
               return;
            }
            m_currentInfo = (DragInfo) t.getTransferData(getInfoFlavor());
            bCompAcceptDrag = pollAnybodyNeedsDrop(dtde);
         }
         else
         {
            return;
         }

         decideAcceptDrop(dtde, bCompAcceptDrag);
      }
      catch (UnsupportedFlavorException e)
      {
         Debug.assertTrue(false, E2Designer.getResources(), "ForgotCBwDI", null);
      }
      catch (Exception e)
      {
         ms_log.warn("DnD transfer analysis error. Transfer is rejected", e);
      }
   }

   /**
    * Extracts transferrable from DB drop.
    */
   private Transferable getTransferrableFromDrop(final PSUiReference uiRef)
         throws Exception
   {
      final IPSReference ref = uiRef.getReference();
      final Enum primaryType = ref.getObjectType().getPrimaryType();
      if (primaryType.equals(PSObjectTypes.DB_TYPE))
      {
         final TableNode node = new TableNode(new CatalogEntry());
         node.setInternalName(ref.getName());
         node.setType(SQLHierarchyConstraints.NT_DBOBJ);
         node.setAllowsChildren(false);
         return node.createTransfer(getDatasourceName(ref));
      }
      else if (primaryType.equals(PSObjectTypes.EXTENSION))
      {
         final IPSCmsModel model =
               PSCoreFactory.getInstance().getModel(PSObjectTypes.EXTENSION);
         final PSExtensionDef def =
               (PSExtensionDef) model.load(ref, false, false);
         
         final String extInterface = getBranchExtensionInterface(uiRef);
         final String path;
         final int type;
         if (extInterface == null)
         {
            return null;
         }
         else if (extInterface.equals(EXT_TYPE_RESULT_DOC_PROC))
         {
            path = E2Designer.getResources().getString("POST_JAVAEXIT") + "/";
            type = JavaExitNode.RESULT_DOC_PROC_EXT;
         }
         else if (extInterface.equals(EXT_TYPE_REQUEST_PRE_PROC))
         {
            path = E2Designer.getResources().getString("PRE_JAVAEXIT") + "/";
            type = JavaExitNode.REQUEST_PRE_PROC_EXT;
         }
         else
         {
            return null;
         }

         final JavaExitNode node =
               new JavaExitNode(def, ref.getLabelKey(), path);
         node.setAllowsChildren(false);
         node.setJavaExitType(type);
         return node.createTransfer();
      }
      else
      {
         return null;
      }
   }
   
   /**
    * Returns ui reference passed through drop operation.
    * Returns <code>null</code> if the reference is of the wrong type.
    */
   private PSUiReference getDroppedRef(DropTargetEvent event)
         throws UnsupportedFlavorException, IOException
   {
      final Collection<PSUiReference> nodes = getDnDHelper().getDataFor(event);
      if (nodes.size() != 1)
      {
         return null;
      }
      final PSUiReference uiRef = nodes.iterator().next();
      final IPSReference ref = uiRef.getReference();
      if (ref == null)
      {
         return null;
      }
      final Enum primaryType = ref.getObjectType().getPrimaryType();
      final Enum secondaryType = ref.getObjectType().getSecondaryType();
      return isValidDbType(primaryType, secondaryType)
            || primaryType.equals(PSObjectTypes.EXTENSION) ? uiRef : null;
   }

   /**
    * Returns <code>true</code> if the object type corresponds to DB type
    * which can be dropped.
    */
   private boolean isValidDbType(final Enum primaryType, final Enum secondaryType)
   {
      return primaryType.equals(PSObjectTypes.DB_TYPE) && (
            (secondaryType.equals(DataBaseObjectSubTypes.TABLE))
            || (secondaryType.equals(DataBaseObjectSubTypes.VIEW)));
   }
   
   /**
    * Retrieves the interface name of the branch the extension is in
    * for the specified extension node.
    */
   private String getBranchExtensionInterface(final PSUiReference uiRef)
   {
      PSUiReference parentUiRef = uiRef.getParentNode();
      while (parentUiRef != null)
      {
         final String intr = (String) parentUiRef.getProperty("interface");
         if (StringUtils.isNotBlank(intr))
         {
            return intr;
         }
         parentUiRef = parentUiRef.getParentNode();
      }
      return null;
   }

   /**
    * Retrieves datasource name for the specified database node.
    * 
    * @return The name, never <code>null</code>, will be empty for the 
    * repository datasource.
    */
   private String getDatasourceName(final IPSReference ref)
   {
      IPSHierarchyNodeRef dsRef = (IPSHierarchyNodeRef) ref;
      while (!dsRef.getObjectType().getSecondaryType().equals(
            DataBaseObjectSubTypes.DATASOURCE))
      {
         dsRef = dsRef.getParent();
      }
      String dsName = dsRef.getName();
      if (PSCatalogDatasources.isRepository(dsName))
         dsName = "";
      return dsName;
   }

   /**
    * Convenience method to access {@link PSLegacyDnDHelper}.
    */
   private PSLegacyDnDHelper getDnDHelper()
   {
      return PSLegacyDnDHelper.getInstance();
   }

   /**
    * Constructs transferable from dropped file.
    */
   private Transferable getTransferableFromFileDropEvent(DropTargetEvent dtde)
         throws UnsupportedFlavorException, IOException, MalformedURLException,
            FigureCreationException, PSIllegalArgumentException
   {
      final File file = getFilesFromDnD(dtde).get(0);
      final XMLNode node = new XMLNode(new CatalogEntry(), file.getParent());
      node.setFactoryAndFigureId(file.getName());
      node.setInternalName(file.getName());
      final ImageIcon icon = FileHierarchyConstraints.getIcon(file.getName());
      if(icon != null)
      {
         node.setIcon(icon);
      }
      node.setType(FileHierarchyConstraints.NT_FILE);
      node.setAllowsChildren(false);
      return node.createTransfer(node.getInternalName());
   }

   /**
    * Retrieves list of files from DnD event. Note, for this method to work
    * the event should support {@link DataFlavor#javaFileListFlavor}.
    */
   @SuppressWarnings("unchecked")
   private List<File> getFilesFromDnD(DropTargetEvent dtde)
         throws UnsupportedFlavorException, IOException
   {
      final Transferable transferable;
      if (dtde instanceof DropTargetDragEvent)
      {
         transferable = ((DropTargetDragEvent) dtde).getTransferable();
      }
      else if (dtde instanceof DropTargetDropEvent)
      {
         transferable = ((DropTargetDropEvent) dtde).getTransferable();
      }
      else
      {
         throw new AssertionError("Unexpected event class: " + dtde.getClass());
      }
      
      return (List<File>) transferable.getTransferData(
            DataFlavor.javaFileListFlavor);
   }

   /**
    * Indicates whether provided files list received as part of drop action is
    * acceptable for processing.
    * The list should contain only one file of the acceptable type.
    */
   boolean isAcceptableDropFilesList(List<File> files)
   {
      if (files == null || files.size() != 1)
      {
         return false;
      }
      final File file = files.get(0);
      if (!file.exists() || !file.isFile() || !file.canRead())
      {
         return false;
      }
      final String extension = FilenameUtils.getExtension(file.getAbsolutePath());
      if (StringUtils.isBlank(extension))
      {
         return false;
      }
      for (final String allowedExt : FileHierarchyConstraints.straFILETYPES)
      {
         if (extension.toLowerCase().equals(allowedExt))
         {
            return true;
         }
      }
      return false;
   }

   /**
    * Performs cleanup after drop. Reports back drop success/failure.
    */
   private void decideAcceptDrop(DropTargetDragEvent dtde, boolean bCompAcceptDrag)
   {
      if (m_bAcceptDrag || bCompAcceptDrag)
      {
         // Size drag rectangle according to available data in the drag
         // context, if no data, fall back to a fixed size square
         Rectangle rect = new Rectangle(dtde.getLocation());
         if (m_currentInfo != null && m_currentInfo.getOriginalRef() != null)
         {
            UIConnectableFigure fig = m_currentInfo.getOriginalRef();
            rect.setSize(fig.getSize());
            ms_dragUnderRect.setOffset(m_currentInfo.getOffset());
         }
         else
         {
            rect.setSize(50, 50);
         }
         ms_dragUnderRect.setRect(rect);
         dropActionChanged(dtde);
      }
      else
      {
         dtde.rejectDrag();
      }
   }

   /**
    * Polls involved parties whether they accept drop.
    */
   private boolean pollAnybodyNeedsDrop(DropTargetDragEvent dtde)
   {
      Properties figureProperties = m_currentInfo.getFigureProperties();

      Point ptOrigin = dtde.getLocation();

      // Any custom drop actions that may want drop?
      if (!m_customDropActions.isEmpty())
      {
         Enumeration e = m_customDropActions.elements();
         while (e.hasMoreElements() && !m_bAcceptDrag)
         {
            ICustomDropAction cda = (ICustomDropAction) e.nextElement();
            if (cda.wantsDrop(m_currentInfo.getID()))
            {
               m_bAcceptDrag = true;
            }
         }
      }

      // we do not need to do any of this if the figure
      // can not be connected to anything
      // figureProperties should never be null - UIConnectableFigure
      // implements it
      // CAN_BE_ATTACHED will be null if it can not be attached
      if (figureProperties == null
            || figureProperties
                  .getProperty(UIConnectableFigure.CAN_BE_ATTACHED) == null)
         m_bCanBeAttached = false;

      // does the frame want it?
      if (!m_bAcceptDrag)
      {
         if (0 == getFigureFactory().getName().compareToIgnoreCase(
               m_currentInfo.getID().getFactoryName()))
         {
            m_bAcceptDrag = true;
         }
      }

      // finally, are we over a component that wants it?
      UIConnectableFigure uic = getConnectable(ptOrigin);

      return uic != null
            && uic != m_currentInfo.getOriginalRef()
            && uic.willAcceptConnect(m_currentInfo.getID().getID());
   }

   /**
    * Initializes {@link #ms_dragUnderRect} if it is <code>null</code>.
    */
   private void maybeInitDragUnderRect(DropTargetDragEvent dtde)
   {
      if (ms_dragUnderRect == null)
      {
         ms_dragUnderRect =
            new HiliteRect(dtde.getDropTargetContext().getComponent(), 1);
      }
      else
      {
         ms_dragUnderRect.changePane(dtde.getDropTargetContext().getComponent());
      }
   }

   /**
    * Finds the connectable figure in the drawing pane that is the highest in
    * the z-order that contains the passed in point and returns it. If none is
    * found, null is returned. If a point is over a part of a figure in which
    * isHit returns <code>false</code>, the next figure in the z-order is
    * tested.
    */
   public UIConnectableFigure getConnectable(Point pt)
   {
      Vector comps = Util.findComponentsAt(m_drawingPane, pt);
      int size = comps.size();
      if (0 == size)
         return null;

      Debug.assertTrue(comps.get(size - 1) == m_drawingPane, E2Designer
            .getResources(), "MissingParent", null);

      for (int index = 0; index < size - 1; index++)
      {
         Component c = (Component) comps.get(index);
         if (c instanceof UIConnectableFigure
               && ((UIConnectableFigure) c).isHit(SwingUtilities.convertPoint(
                     m_drawingPane, pt, c)))
         {
            return (UIConnectableFigure) c;
         }
      }

      return null;
   }

   /**
    * If there is a UIFigure at the provided point, it will be returned.
    * 
    * @param pt the point we are looking for a figure at
    * @return UIFigure the figure if found, null otherwise
    */
   public UIFigure getFigure(Point pt)
   {
      Vector comps = Util.findComponentsAt(m_drawingPane, pt);
      int size = comps.size();
      if (0 == size)
         return null;

      for (int index = 0; index < size - 1; index++)
      {
         Component c = (Component) comps.get(index);
         if (c instanceof UIFigure)
            return (UIFigure) c;
      }

      return null;
   }

   /**
    * Checks if the supplied point is over a figure that will accept a
    * connection from the currently dragging figure.
    * 
    * @param pt the point to check, in coord system of m_drawingPane
    * 
    * @return <code>true</code> if the point is over a figure that will accept
    *         a connection from the currently dragging figure, as described in
    *         m_currentInfo.
    */
   public boolean isOverConnection(Point pt)
   {
      return (isOverRigidConnection(pt) || isOverFlexibleConnection(pt));
   }

   /**
    * Checks if the supplied point is over a figure that will accept a rigid
    * connection from the currently dragging figure.
    * 
    * @param pt the point to check, in coord system of m_drawingPane
    * 
    * @return <code>true</code> if the point is over a figure that will accept
    *         a rigid connection from the currently dragging figure, as
    *         described in m_currentInfo.
    */
   protected boolean isOverRigidConnection(Point pt)
   {
      UIConnectableFigure uic = getConnectable(pt);
      final List<IConnectionConstraint> constraints = new ArrayList<IConnectionConstraint>();
      constraints.add(new RigidConnectionConstraint());

      return null != uic
            && null != m_currentInfo
            && uic != m_currentInfo.getOriginalRef() // don't allow connect
            // to self
            && uic.isDragPoint(
                  SwingUtilities.convertPoint(m_drawingPane, pt,uic))
            && uic.willAcceptConnect(
                  m_currentInfo.getID().getID(), constraints);
   }

   /**
    * Checks if the supplied point is over a figure that will accept a flexible
    * connection from the currently dragging figure.
    * 
    * @param pt the point to check, in coord system of m_drawingPane
    * 
    * @return <code>true</code> if the point is over a figure that will accept
    *         a flexible connection from the currently dragging figure, as
    *         described in m_currentInfo.
    */
   protected boolean isOverFlexibleConnection(Point pt)
   {
      UIConnectableFigure uic = getConnectable(pt);

      return ((null != uic)
            && (null != m_currentInfo)
            && (uic != m_currentInfo.getOriginalRef()) // don't allow connect
                                                         // to
            // self
            && (uic.isDragPoint(SwingUtilities.convertPoint(m_drawingPane, pt,
                  uic))) && (uic.willAcceptConnect(m_currentInfo.getID()
            .getID())));
   }

   @SuppressWarnings("unchecked")
   public void dragOver(DropTargetDragEvent dtde)
   {
      // see if we are over a component that will accept the drop
      Point pt = dtde.getLocation();
      boolean bAcceptConnect = isOverRigidConnection(pt);

      if (bAcceptConnect && m_bCanBeAttached)
      {
         UIConnectableFigure uic = getConnectable(pt);

         final String[] astrParams =
         {"getConnectable"};
         Debug.assertTrue(null != uic, E2Designer.getResources(),
               "UnexpectedNullReturn", astrParams);
         Point compPt = SwingUtilities.convertPoint(m_glassPane, pt, uic);
         List<IConnectionConstraint> constraints = null;

         if (m_currentInfo.getFigureProperties().get(
               UIConnectableFigure.CONSTRAINTS) != null
               && m_currentInfo.getFigureProperties().get(
                     UIConnectableFigure.CONSTRAINTS) instanceof List)
            constraints = (List<IConnectionConstraint>) m_currentInfo
                  .getFigureProperties().get(UIConnectableFigure.CONSTRAINTS);

         UIConnectionPoint cp = uic.getClosestConnector(m_currentInfo.getID()
               .getID(), constraints, compPt);
         if (null != cp
               && m_currentInfo.getID().getID() != AppFigureFactory.DIRECTED_CONNECTION_ID)
            m_hilite.setRect(new Rectangle(SwingUtilities.convertPoint(uic, cp
                  .getLocation(), m_glassPane), cp.getSize()));
      }
      else
      {
         if (m_hilite != null)
         {
            m_hilite.clear();
         }
      }

      if (m_bAcceptDrag || bAcceptConnect)
      {
         dropActionChanged(dtde);
         ms_dragUnderRect.reposition(dtde.getLocation());
      }
      else
      {
         m_hilite.clear();
         ms_dragUnderRect.clear();
         dtde.rejectDrag();
      }
   }

   public void dropActionChanged(DropTargetDragEvent dtde)
   {
      if (m_bAcceptDrag)
      {
         if ((DnDConstants.ACTION_COPY_OR_MOVE & dtde.getDropAction()) != 0)
         {
            dtde.acceptDrag(dtde.getDropAction());
         }
         else
         {
            dtde.acceptDrag(DnDConstants.ACTION_MOVE);
         }
      }
      else
      {
         dtde.rejectDrag();
      }
   }

   public void dragExit(@SuppressWarnings("unused") DropTargetEvent dte)
   {
      m_hilite.clear();
      m_hilite = null;
      m_bAcceptDrag = false;
      m_bCanBeAttached = true;
      ms_dragUnderRect.clear();
      m_bInDrop = false;
   }

   public void drop(DropTargetDropEvent dtde)
   {
      m_bInDrop = true;
      ms_dragUnderRect.clear();

      try
      {
         if ((dtde.getDropAction() & DnDConstants.ACTION_COPY_OR_MOVE) != 0
            && (dtde.isDataFlavorSupported(getConnectableFigureFlavor())
                  || dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
                  || dtde.isDataFlavorSupported(PSLegacyDnDHelper.DATA_FLAVOR)))
         {
            dtde.acceptDrop(dtde.getDropAction());
            getXmlApplicationEditor().markEditorDirty();
         }
         else
         {
            dtde.rejectDrop();
            m_bInDrop = false;
            return;
         }

         Point pt = dtde.getLocation();
         final boolean bAcceptConnect = isOverRigidConnection(pt);
         final UIConnectableFigure figure = getConnectable(pt);
         Debug.assertTrue(bAcceptConnect ? figure != null : true,
               E2Designer.getResources(), "UnexpectedNullReturn",
               new String[] {"getConnectable"});

         /*
          * save this for the dragDropEnd event - DragSourceDropEvent returns 0
          * when we get there
          */
         m_dropAction = dtde.getDropAction();

         final boolean bMove = !bAcceptConnect
               && m_bInDrag
               && DnDConstants.ACTION_MOVE == dtde.getDropAction()
               && !m_draggingComponent.isAttached();
         m_bDroppedInSrc = bMove; // m_drawingPane ==
         // m_draggingComponent.getParent();
         final Transferable transferable = getTransferableFromDropEvent(dtde);
         final UIConnectableFigure uic = bMove
               ? m_draggingComponent
               : (UIConnectableFigure) transferable.getTransferData(
                     getConnectableFigureFlavor());

         final DragInfo info =
               (DragInfo) transferable.getTransferData(getInfoFlavor());
         final Container parent = m_bInDrag
               ? m_draggingComponent.getParent()
               : null;
         final Point compPt = SwingUtilities.convertPoint(m_glassPane, pt,
               figure);
         final Point location = new Point(dtde.getLocation());
         /*
          * Correct the location for the initial offset of the mouse click from
          * the upper left corner of the figure and for scroll position.
          */
         Point ptClickOffset = info.getOffset();
         location.translate(-ptClickOffset.x, -ptClickOffset.y);

         // I'm not sure if this is absolutely necessary
         final DropTargetDropEvent finalDtde = dtde;
         E2Designer.getApp().getMainFrame().setWaitCursor();
         new SwingWorker()
         {
            @Override
            public Object construct()
            {
               SwingUtilities.invokeLater(new Runnable()
               {
                  @SuppressWarnings("unchecked")
                  public void run()
                  {
                     if (bAcceptConnect && m_bCanBeAttached)
                     {
                        List<IConnectionConstraint> constraints = null;
                        if (info.getFigureProperties().get(
                              UIConnectableFigure.CONSTRAINTS) != null
                              && info.getFigureProperties().get(
                                    UIConnectableFigure.CONSTRAINTS) instanceof PSCollection)
                           constraints = (List<IConnectionConstraint>) info
                                 .getFigureProperties().get(
                                       UIConnectableFigure.CONSTRAINTS);

                        UIConnectionPoint cp = figure.getClosestConnector(info
                              .getID().getID(), constraints, compPt);

                        if (cp != null)
                        {
                           // see if any CDA wants it too
                           boolean bCustom = false;
                           int iRet = 0;
                           UIConnectableFigure attached = null;
                           if (!m_customDropActions.isEmpty())
                           {
                              Enumeration e = m_customDropActions.elements();
                              while (e.hasMoreElements() && !bCustom)
                              {
                                 ICustomDropAction cda = (ICustomDropAction) e
                                       .nextElement();
                                 iRet = cda.customizeDrop(UIFigureFrame.this,
                                       figure, uic, location);
                                 if (iRet == ICustomDropAction.DROP_ACCEPTED
                                       || iRet == ICustomDropAction.DROP_ACCEPTED_AND_ATTACH_OBJ)
                                 {
                                    bCustom = true;
                                    break;
                                 }
                              }
                           }

                           if (!(bCustom && (iRet == ICustomDropAction.DROP_ACCEPTED_AND_ATTACH_OBJ)))
                           {
                              attached = cp.getAttached();
                              uic.createDynamicConnection(cp);
                           }

                           // TODO: ask user if they want to replace it
                           if (attached != null
                                 && (m_draggingComponent == null || attached != m_draggingComponent))
                              attached.remove();
                        }
                     }
                     else
                     {
                        // see if any CDA wants it first
                        boolean bCustom = false;
                        if (!m_customDropActions.isEmpty())
                        {

                           Enumeration e = m_customDropActions.elements();
                           while (e.hasMoreElements() && !bCustom)
                           {
                              ICustomDropAction cda = (ICustomDropAction) e
                                    .nextElement();
                              int iCustomAct = cda.customizeDrop(
                                    UIFigureFrame.this, figure, uic, location);
                              if (iCustomAct == ICustomDropAction.DROP_ACCEPTED
                                    || iCustomAct == ICustomDropAction.DROP_CANCELED)
                              {
                                 bCustom = true;
                                 if (iCustomAct == ICustomDropAction.DROP_CANCELED)
                                 {
                                    interrupt();
                                    break;
                                 }
                              }
                           }
                        }
                        if (!bCustom)
                        {
                           uic.setLocation(location);

                           // remove and add comp to bring to top of Z-order
                           if (null != parent)
                              parent.remove(uic);

                           clearSelection();
                           add(uic, 0);
                           m_drawingPane.repaint(uic.getBounds());
                        }
                     }

                     m_bInDrop = false;
                  }
               });
               getXmlApplicationEditor().getDisplay().asyncExec(new Runnable()
               {
                  public void run()
                  {
                     getXmlApplicationEditor().getSite().getPage()
                           .activate(getXmlApplicationEditor());
                  }
               });
               return "done";
            }

            @Override
            public void interrupt()
            {
               E2Designer.getApp().getMainFrame().clearWaitCursor();
               super.interrupt();
            }

            @Override
            public void finished()
            {
               E2Designer.getApp().getMainFrame().clearWaitCursor();
               finalDtde.dropComplete(true);
            }
         };
      }
      catch (IOException e)
      {
         E2Designer.getApp().getMainFrame().clearWaitCursor();
         PSDlgUtil.showError(e, false, E2Designer.getResources().getString(
               "OpErrorTitle"));

         dtde.dropComplete(false);
         m_bInDrop = false;

         e.printStackTrace();
      }
      catch (UnsupportedFlavorException e)
      {
         E2Designer.getApp().getMainFrame().clearWaitCursor();
         PSDlgUtil.showError(e, false, E2Designer.getResources().getString(
               "OpErrorTitle"));

         dtde.dropComplete(false);
         m_bInDrop = false;

         e.printStackTrace();
      }
      catch (Exception e)
      {
         E2Designer.getApp().getMainFrame().clearWaitCursor();
         dtde.dropComplete(false);
         m_bInDrop = false;
         PSDlgUtil.showError(e);
      }
      finally
      {
         // clean up
         m_currentInfo = null;
      }
   }

   /**
    * Retrieves transferable from drop event.
    * If the event does not carry recognized data flavors IllegalArgumentException
    * is thrown.
    */
   private Transferable getTransferableFromDropEvent(DropTargetDropEvent dtde)
         throws Exception
   {
      if (dtde.isDataFlavorSupported(getConnectableFigureFlavor()))
      {
         return dtde.getTransferable();
      }
      else if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
      {
         return getTransferableFromFileDropEvent(dtde);
      }
      else if (getDnDHelper().isRecognizedLegacyDrop(dtde))
      {
         
         final PSUiReference uiRef = getDroppedRef(dtde); 
         if (uiRef == null || uiRef.getReference() == null)
         {
            return null;
         }
         return getTransferrableFromDrop(uiRef);
      }
      else
      {
         throw new IllegalArgumentException(
               "Event can provide no recognized flavors. "
               + "Provides only following flavors: "
               + dtde.getCurrentDataFlavorsAsList());
      }
   }

   private DataFlavor getInfoFlavor()
   {
      return new DataFlavor(
            FigureTransfer.sDESCRIPTOR_FLAVOR_TYPE,
            FigureTransfer.sDESCRIPTOR_FLAVOR_NAME);
   }

   // IDynamicActions interface implementation
   public IAction[] getActionItems(@SuppressWarnings("unused") String strActionId)
   {
      return null;
   }

   public boolean hasActionItems(@SuppressWarnings("unused") String strActionId)
   {
      return false;
   }

   /**
    * This function adds the print pages to the print book
    */
   @SuppressWarnings("unchecked")
   public void appendPrintPages(Book bk)
   {
      if (m_drawingPane == null)
         return;

      PrintPanelPage page = new PrintPanelPage(this);
      Component[] comps = m_drawingPane.getComponents();
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

      // preview for debuggin
      page.appendPrintPages(bk);
   }

   public int print(Graphics g, PageFormat pf,
         @SuppressWarnings("unused") int pageIndex)
   {
      g.translate((int) pf.getImageableX(), (int) pf.getImageableY());
      paint(g);

      return Printable.PAGE_EXISTS;
   }

   @Override
   public void paint(Graphics g)
   {
      super.paint(g);
   }

   /**
    * This method should be used in place of getContentPane because the content
    * pane is the scroll pane. This is the pane where the figures should be
    * drawn.
    */
   protected JLayeredPane getDrawingPane()
   {
      return (m_drawingPane);
   }

   /**
    * Searches all child figures looking for open editors, closing all that it
    * finds. The windows are forced to close. Before calling this method, the
    * windows should be given a chance to cancel the close using
    * <code>IEditor.canClose</code>. There is a small chance that the user
    * could change the state of a window between a call to <code>canClose</code>
    * and this method. In this case, those changes will be lost. <p/>This class
    * is provided as a service to derived classes, it is not used by this class.
    * 
    * @param editors A vector containing the list of editors to close. This list
    *           can be obtained by using the <code>getEditingWindows</code>
    *           method.
    */
   protected void closeEditingWindows(Vector editors)
   {
      int editorCount = editors.size();
      for (int i = 0; i < editorCount; ++i)
      {
         ((IEditor) editors.get(i)).close(1, true);
      }
   }

   /**
    * Looks at all children figures, checking each one for an open editor
    * session. If it finds one the editor is added to the vector as an IEditor
    * object. <p/>This class is provided as a service to derived classes, it is
    * not used by this class.
    * 
    * @param c The container from which to get the list of figures to check.
    * 
    * @param bExcludeNoChanges If this flag is <code>true</code>, then open
    *           editors that report no user changes will not be included in the
    *           returned collection.
    * 
    * @returns a Vector containing 0 or more IEditor objects that are currently
    *          editing figures that belong to this window.
    * 
    * @throws IllegalArgumentException If c is null.
    */
   protected Vector getEditingWindows(Container c, boolean bExcludeNoChanges)
   {
      if (null == c)
         throw new IllegalArgumentException();

      Vector<IEditor> editors = new Vector<IEditor>(5);

      Component[] comps = c.getComponents();
      for (int index = comps.length - 1; index >= 0; index--)
      {
         if (!(comps[index] instanceof UIConnectableFigure))
            continue;

         UIConnectableFigure fig = (UIConnectableFigure) comps[index];
         IEditor editor = fig.getEditor();
         if (null == editor)
            continue;

         if (bExcludeNoChanges && !editor.isDataChanged())
            continue;

         editors.add(editor);
      }
      return editors;
   }

   /**
    * Checks all figures to see if they have an open editor. If they do, the
    * editor is sent a message to save its data. <p/>This class is provided as a
    * service to derived classes, it is not used by this class.
    * 
    * @param bClosing Should be <code>true</code> if the app frame is
    *           currently being shutdown. This flag will be passed on to each
    *           editor.
    * 
    * @param editors A list of open editors, obtained using the
    *           <code>getEditingWindows
    * </code> method.
    * 
    * @return <code>true</code> if all editors saved successfully.
    */
   protected boolean saveEditingWindows(Vector editors, boolean bClosing)
   {
      int editorCount = editors.size();
      boolean bSuccess = true;
      for (int i = 0; i < editorCount; ++i)
      {
         bSuccess = bSuccess
               && ((IEditor) editors.get(i)).saveData(Boolean.valueOf(bClosing));
      }
      return bSuccess;
   }

   /**
    * Asks all editors in the supplied list if they can close now.
    * 
    * @return <code>true</code> if all editors can close, <code>false</code>
    *         otherwise
    */
   protected boolean canCloseEditingWindows(Vector editors)
   {
      int editorCount = editors.size();
      boolean bCanCloseAll = true;
      for (int i = 0; i < editorCount; ++i)
      {
         bCanCloseAll = bCanCloseAll
               && 0 != ((IEditor) editors.get(i)).canClose();
      }
      return bCanCloseAll;
   }

   /**
    * Need to customize drop behavior
    */
   public void setMouseOwner(UIConnectableFigure fig)
   {
      m_mouseOwner = fig;
   }

   /**
    * free the resources associated with this window.
    */
   public void closeWindow()
   {
      if (isClosed())
         return;

      // clean up all pointers to avoid cyclical references
      if (m_drawingPane != null)
         m_drawingPane.removeAll();
      if (m_contentPanel != null)
         m_contentPanel.removeAll();
      if (m_recognizers != null)
         m_recognizers.clear();

      dispose();
   }
   
   /**
    * In new Eclipse workbench the frame is considered always selected
    * because unselected frames are just on the invisible tabs.
    */
   @Override
   public boolean isSelected()
   {
      return true;
   }

   // protected storage
   protected JLayeredPane m_drawingPane = null;

   protected JPanel m_glassPane = null;

   public JPanel getTheGlassPane()
   {
      return m_glassPane;
   }

   // private storage
   /**
    * This is temporary storage for the component that is being dragged. It is
    * used to remove the component after it has completed a moving drag and
    * drop. It is always null except during the dnd operation.
    */
   protected UIConnectableFigure m_draggingComponent = null;

   private Vector<DragGestureRecognizer> m_recognizers = new Vector<DragGestureRecognizer>(
         10);

   private boolean m_bAcceptDrag = false;

   private boolean m_bCanBeAttached = true;

   /**
    * While dragging, is this the window that the drag initiated in. This is
    * used to make a shortcut when moving components in the same window. If a
    * comp is dropped in the window it started in, we can just do a move on that
    * comp, rather than creating it from the serialized object. If we do this,
    * then we need to tell the source listener. m_bInDrag is the flag for the
    * target listener, m_bDroppedInSrc is for the src listener.
    */
   private boolean m_bInDrag = false;

   private boolean m_bDroppedInSrc = false;

   /**
    * This object is null except during a drag. During a drag, it references the
    * DragInfo object that has information about the currently dragging uic.
    */
   protected DragInfo m_currentInfo = null;

   private Vector<ICustomDropAction> m_customDropActions = new Vector<ICustomDropAction>(
         10);

   /**
    * These 2 listeners are used to relay mouse events to a figure in the frame
    * that wants them.
    */
   private MouseListener m_figureMouseListener = null;

   private MouseMotionListener m_figureMouseMotionListener = null;

   private HiliteRect m_hilite = null;

   /**
    * The component that most recently had its mouse cursor changed during a
    * drag. It is null except during the the drag.
    */
   private Component m_cursorSetOnComp = null;

   private Component m_cursorSetOnCompForMove = null;

   /**
    * Variables used during drag select mode. The flag indicates that we are in
    * drag select mode. In drag select mode, a rubber-banding box is drawn in
    * the frame window. All figures in this box are selected when the user
    * releases the mouse. The drag anchor is where the user first clicked. The
    * last size is the size of the last drawn XOR box. If a size is negative,
    * that means it was drawn to the left of the anchor point.
    */
   private boolean m_bInDragSelect = false;

   private Dimension m_lastSize = null;

   private Point m_dragAnchor = new Point();

   /**
    * Who is getting the mouse messages. null except when in mouse event relay
    * mode.
    */
   private UIConnectableFigure m_mouseOwner = null;

   private static Cursor ms_cursorWillConnect;

   private static Cursor ms_cursorWontConnect;
   {
      PSResources rb = E2Designer.getResources();
      String strCursorResName = "ConnectCursor";
      ImageIcon icon = ResourceHelper.getIcon(rb, strCursorResName);
      if (null == icon)
         throw new MissingResourceException(E2Designer.getResources()
               .getString("LoadIconFail"), "E2DesignerResources",
               strCursorResName);
      ms_cursorWillConnect = Toolkit.getDefaultToolkit().createCustomCursor(
            icon.getImage(), ResourceHelper.getPoint(rb, strCursorResName),
            strCursorResName);

      strCursorResName = "NoConnectCursor";
      icon = ResourceHelper.getIcon(rb, strCursorResName);
      if (null == icon)
         throw new MissingResourceException(E2Designer.getResources()
               .getString("LoadIconFail"), "E2DesignerResources",
               strCursorResName);
      ms_cursorWontConnect = Toolkit.getDefaultToolkit().createCustomCursor(
            icon.getImage(), ResourceHelper.getPoint(rb, strCursorResName),
            strCursorResName);
   }

   /**
    * A hiliteRect is a thin XOR box drawn around a component to indicate that
    * the component is the focus of some user action. It is designed for for use
    * during mouse operations.
    */
   private class HiliteRect
   {
      /**
       * See {@link #HiliteRect(Component, int) HiliteRect(drawingPane, 2)} for
       * info
       */
      public HiliteRect(Component drawingPane)
      {
         this(drawingPane, 2);
      }

      /**
       * Creates a new hilite rectangle.
       * 
       * @param drawingPane the component where the xor rect will be drawn
       * @param lineWidth the line width to use, must be a positive integer
       * 
       * @throws IllegalArgumentException if drawingPane is null
       */
      public HiliteRect(Component drawingPane, int lineWidth)
      {
         if (lineWidth < 1)
         {
            throw new IllegalArgumentException("Linewidth must be positive");
         }
         if (null == drawingPane)
         {
            final Object[] astrParams =
            {"Component"};
            throw new IllegalArgumentException(MessageFormat.format(E2Designer
                  .getResources().getString("CantBeNull"), astrParams));
         }
         mi_drawingPane = drawingPane;
         mi_lineWidth = lineWidth;
      }

      /**
       * Draws a hilite around the supplied rect in the drawing pane supplied in
       * the constructor, removing the previous one if present. If the supplied
       * rect matches the current rect, nothing is done.
       * 
       * @param rect the rectangle around which the hilite will be drawn, in the
       *           coord system of the pane supplied in the constructor
       */
      public void setRect(Rectangle rect)
      {

         if (null == mi_rectangle || !rect.equals(mi_rectangle))
         {
            clear();
            draw(rect);
            mi_rectangle = rect;
         }
      }

      /**
       * Set the position of the current rectangle. It is an error if this is
       * called before a drawing rectangle is defined.
       * 
       * @param location new location for the current drawing rectangle.
       */
      public void reposition(Point location)
      {
         if (mi_rectangle == null)
         {
            throw new IllegalStateException("No drawing rectangle defined");
         }
         if (!mi_rectangle.getLocation().equals(location))
         {
            clear();
            mi_rectangle.setLocation(location);
            draw(mi_rectangle);
         }
      }

      /**
       * Set the offset for this highlight rectangle. The rectangle is drawn at
       * a position that includes the offset in addition to the rectangle
       * location. Call this before calling any other method that draws the
       * rectangle, i.e. right after construction.
       * 
       * @param offset the offset, may be <code>null</code>, this is
       *           subtracted from the current position to determine the origin
       *           to draw the rectangle from. If <code>null</code> this
       *           indicates no offset.
       */
      public void setOffset(Point offset)
      {
         mi_offset = offset;
      }

      /**
       * Change the component where the rectangle is being drawn. This can be
       * called to reset a global hilight to a new component. Note that the
       * existing rectangle will be cleared if drawn.
       * 
       * @param newpane the new component, must never be <code>null</code>
       */
      public void changePane(Component newpane)
      {
         if (null == newpane)
         {
            final Object[] astrParams =
            {"Component"};
            throw new IllegalArgumentException(MessageFormat.format(E2Designer
                  .getResources().getString("CantBeNull"), astrParams));
         }
         boolean was_drawn = mi_drawn;
         if (was_drawn)
            clear(); // Clear existing rect
         mi_drawingPane = newpane;
         if (was_drawn)
            draw(mi_rectangle);
      }

      /**
       * Removes the last drawn rectangle from the screen.
       */
      public void clear()
      {
         if (isDrawn())
            draw(mi_rectangle);
         mi_drawn = false;
      }

      /**
       * Draws an XOR box around the supplied rect. The thickness of the line
       * used to create the rect is determined by calling getLineWidth().
       * 
       * @param rect the rectangle around which the hilite will be drawn, in the
       *           coord system of the pane supplied in the constructor
       */
      private void draw(Rectangle rect)
      {
         int dx, dy;

         if (mi_offset != null)
         {
            dx = -mi_offset.x;
            dy = -mi_offset.y;
         }
         else
         {
            dx = 0;
            dy = 0;
         }
         Graphics g = mi_drawingPane.getGraphics();
         g.setColor(Color.black);
         g.setXORMode(Color.white);
         Point loc = new Point(rect.getLocation());
         Rectangle box = new Rectangle(rect);
         for (int boxes = getLineWidth(); boxes > 0; boxes--)
         {
            loc.translate(-1, -1);
            box.grow(1, 1);
            g.drawRect(box.x + dx, box.y + dy, box.width - 1, box.height - 1);
         }
         g.dispose();
         mi_drawn = true;
      }

      /**
       * @returns <code>true</code> if the rect is currently painted on the
       *          screen
       */
      public boolean isDrawn()
      {
         return mi_drawn;
      }

      /**
       * @returns the pixel width of the line that is used to draw this rect
       */
      public int getLineWidth()
      {
         return (mi_lineWidth);
      }

      /**
       * Contains the rectangle being drawn. This may be updated many times or
       * just set once. Will be <code>null</code> until the first call to
       * {@link #setRect(Rectangle)}.
       */
      private Rectangle mi_rectangle = null;

      /**
       * Tracks the state of the highlight. Set in {@link #draw(Rectangle)} and
       * reset in {@link #clear()}.
       */
      private boolean mi_drawn = false;

      /**
       * Tracks the current canvas this is drawing to. May be updated many times
       * or set at construction time. Never <code>null</code>.
       */
      private Component mi_drawingPane = null;

      /**
       * Line width to use when drawing, may be overriden in the constructor.
       * This default is considered a reasonable value.
       */
      private int mi_lineWidth = 2;

      /**
       * If specified, determines an offset to subtract from the origin.
       */
      private Point mi_offset = null;
   }

   /**
    * Displays context menu with appropriate menu items.
    * 
    * @param e The mouse event that triggered this call, never <code>null</code>.
    */
   public void callPopup(MouseEvent e)
   {
      if (e == null)
         throw new IllegalArgumentException("e may not be null");
      final JPopupMenu popup = createEditPopup(e);
      if (popup == null)
      {
         return;
      }
      popup.show(e.getComponent(), e.getPoint().x, e.getPoint().y);
   }
   
   /**
    * Returns <code>true</code> if current mouse event {@link #m_thePopupMouseEvent}
    * happened over a figure
    */
   protected boolean isClickedOnFigure()
   {
      return getConnectable(m_thePopupMouseEvent.getPoint()) != null;
   }
   
   /**
    * Creates frame popup menu shown if no other pop-up menu is triggered.
    * @param e the event which triggered this menu. Not <code>null</code>.
    * @return the newly created popup menu or <code>null</code> if the frame
    * does not provide popup menu.
    */
   protected JPopupMenu createEditPopup(MouseEvent e)
   {
      if (e == null)
      {
         throw new IllegalArgumentException("Event should not be null");
      }
      JPopupMenu editPopup = new JPopupMenu();
      JMenuItem properties = new JMenuItem(E2Designer.getResources().getString(
            "menuAppProperties"));
      properties.addActionListener(this);

      m_thePopupMouseEvent = e;

      // add custom menus
      UIConnectableFigure uic = getConnectable(m_thePopupMouseEvent.getPoint());
      if ((isClickedOnFigure() && uic.isHit(SwingUtilities.convertPoint(m_glassPane,
            m_thePopupMouseEvent.getPoint(), uic))))
      {
         if (!uic.isEditable())
            properties.setEnabled(false);

         // add menu actions
         for (final String actionClassName : uic.getMenuActions())
         {
            try
            {
               final Class actionClass = Class.forName(actionClassName);
               final PSFigureAbstractAction action =
                     (PSFigureAbstractAction) actionClass.newInstance();
               action.setData(getData());
               action.setFigure(uic);
               editPopup.add(action);
            }
            catch (ClassNotFoundException ex)
            {
               handleActionLoadingFailure(actionClassName, ex);
            }
            catch (InstantiationException ex)
            {
               handleActionLoadingFailure(actionClassName, ex);
            }
            catch (IllegalAccessException ex)
            {
               handleActionLoadingFailure(actionClassName, ex);
            }
         }

         Enumeration connectors = uic.getConnectionPoints();
         while (connectors.hasMoreElements())
         {
            Object connector = connectors.nextElement();
            if (connector instanceof UIConnectionPoint)
            {
               UIConnectionPoint point = (UIConnectionPoint) connector;
               if (point.getData() != null)
               {
                  JMenuItem requestor = new JMenuItem(point.getPopupName());
                  requestor.addActionListener(this);
                  editPopup.add(requestor);
               }
            }
         }

         // add any aux editor items
         Iterator commands = uic.getAuxEditorCommands();
         while (commands.hasNext())
         {
            String cmd = (String) commands.next();
            JMenuItem auxItem = new JMenuItem(E2Designer.getResources()
                  .getString(cmd));
            auxItem.setActionCommand(cmd);
            auxItem.addActionListener(this);
            editPopup.add(auxItem);
         }
      }

      editPopup.add(properties);

      // if we've got ce XML in the clipboard and click is on empty space, add
      // paste assembler menu items
      if (uic == null && UIContentEditorHandler.clipboardContainsContentType())
      {
         JMenu subItem = new JMenu(
            E2Designer.getResources().getString("pasteAsAssembler"));
         editPopup.add(subItem);

         JMenuItem childItem;
         childItem = new JMenuItem(
            E2Designer.getResources().getString("pasteAssemblerPage"));
         childItem.addActionListener(this);
         subItem.add(childItem);

         childItem = new JMenuItem(
            E2Designer.getResources().getString("pasteAssemblerSnippet"));
         childItem.addActionListener(this);
         subItem.add(childItem);                  
      }      
      return editPopup;
   }

   private void handleActionLoadingFailure(final String actionClassName, Throwable e)
   {
      ms_log.error("Failed to load menu action class " + actionClassName, e);
   }

   @Override
   public void moveToFront()
   {
      getXmlApplicationEditor().showTabWithControl(this);
   }

   /**
    * Listener for popup menu
    */
   public void actionPerformed(ActionEvent e)
   {
      if (null != m_thePopupMouseEvent)
      {
         UIConnectableFigure uic = getConnectable(m_thePopupMouseEvent
               .getPoint());
         if (e.getActionCommand().equals(
               E2Designer.getResources().getString("menuAppProperties")))
         {
            if (!(null != uic && uic.isHit(SwingUtilities.convertPoint(
                  m_glassPane, m_thePopupMouseEvent.getPoint(), uic))))
            {
               // if point clicked did not hit a ConnectableFigure, do this...
               onEdit();
            }
            else
            {
               uic.onEdit(getData());
            }
         }
         else if (e.getActionCommand().equals(
               E2Designer.getResources().getString("pasteAssemblerPage")))
         {
            UIContentEditorHandler.handlePasteAssembler(this,
                  UIContentEditorHandler.PASTE_ASSEMBLER_PAGE,
                  m_thePopupMouseEvent.getPoint());
         }
         else if (e.getActionCommand().equals(
               E2Designer.getResources().getString("pasteAssemblerSnippet")))
         {
            UIContentEditorHandler.handlePasteAssembler(this,
                  UIContentEditorHandler.PASTE_ASSEMBLER_SNIPPET,
                  m_thePopupMouseEvent.getPoint());
         }
         else
         {
            if ((null != uic && uic.isHit(SwingUtilities.convertPoint(
                  m_glassPane, m_thePopupMouseEvent.getPoint(), uic))))
            {
               Enumeration connectors = uic.getConnectionPoints();
               while (connectors.hasMoreElements())
               {
                  Object connector = connectors.nextElement();
                  if (connector instanceof UIConnectionPoint)
                  {
                     UIConnectionPoint point = (UIConnectionPoint) connector;
                     if (point.getData() != null
                           && point.getPopupName().equals(e.getActionCommand()))
                     {
                        point.onEdit(getData());
                     }
                  }
               }
            }

            // see if any aux editors want the event
            uic.onAuxEvent(e.getActionCommand(), getData());
         }

         // get rid of the left over menu
         repaint();
         m_thePopupMouseEvent = null;
      }
   }

   class ConnectionDialog extends JDialog
   {
      ConnectionDialog(Vector connections)
      {
         super(E2Designer.getApp().getMainFrame(), true);
         setTitle(E2Designer.getResources().getString("linktitle"));
         m_connections = connections;
         JPanel panel = new JPanel(new BorderLayout());
         panel.setBorder(new EmptyBorder(5, 5, 5, 5));

         Vector<String> vItems = new Vector<String>();
         for (int iConnection = 0; iConnection < connections.size(); ++iConnection)
         {
            vItems.add("Link" + new Integer(iConnection + 1).toString());
         }

         m_list = new JList(vItems);
         JScrollPane pane = new JScrollPane(m_list);

         panel.add(pane, "Center");
         panel.add(createCommandPanel(), "East");

         // set the default button
         getRootPane().setDefaultButton(m_commandPanel.getOkButton());

         getContentPane().setLayout(new BorderLayout());
         getContentPane().add(panel);
         this.setSize(DIALOG_SIZE);
         setVisible(true);
      }

      private JPanel createCommandPanel()
      {
         m_commandPanel = new UTStandardCommandPanel(this, "",
               SwingConstants.VERTICAL)
         {
            // implement onOk action
            @Override
            public void onOk()
            {
               try
               {
                  if (m_connections != null)
                  {
                     if (m_list.getSelectedIndex() < m_connections.size())
                     {
                        if (m_connections.get(m_list.getSelectedIndex()) instanceof UIFigure)
                        {
                           theFigure = (UIFigure) m_connections.get(m_list
                                 .getSelectedIndex());
                        }
                     }
                  }

                  dispose();
               }
               catch (Exception e)
               {
                  e.printStackTrace();
               }

               dispose();
            }
         };

         JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
         panel.add(m_commandPanel);
         return panel;
      }

      public UIFigure getFigure()
      {
         return theFigure;
      }

      private final Dimension DIALOG_SIZE = new Dimension(300, 100);

      private UTStandardCommandPanel m_commandPanel = null;

      private UIFigure theFigure = null;

      private JList m_list = null;

      private Vector m_connections = null;

   }

   public void setMouseCaptured()
   {
      m_bMouseCaptured = true;
   }

   /**
    * Get the connection point that we are currently over
    */
   public UIConnectionPoint getCurrentPointOver()
   {
      return (m_currentPointOver);
   }

   /**
    * This function resets the the cursor to the default cursor
    */
   public void setCursorToDefault()
   {
      if (null != m_cursorSetOnComp)
      {
         m_cursorSetOnComp.setCursor(Cursor
               .getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
   }

   /**
    * This method is used to support a simple mutex lock. It currently doesn't
    * support waiting for the lock to become available because that's not what
    * is needed here. This mechanism is being used to prevent dnd operations and
    * other mouse operations from conflicting. Once a lock is successfully
    * acquired, this method will return <code>false</code> until
    * <code>releaseMouseActionLock</code> is called.
    * 
    * @return <code>true</code if the lock was acquired, <code>false</code>
    * otherwise
    *
    * @see #releaseMouseActionLock
    */
   private synchronized boolean acquireMouseActionLock()
   {
      if (m_bLocked)
         return false;
      m_bLocked = true;

      return m_bLocked;
   }

   /**
    * Releases a lock successfully acquired using <code>acquireMouseActionLock
    * </code>.
    * This method should only be called if the lock was successfully acquired.
    * After the call completes, a call to acquire will succeed.
    */
   private synchronized void releaseMouseActionLock()
   {
      m_bLocked = false;
   }
   
   /**
    * Returns the Eclipse application editor this frame serves.
    */
   protected PSXmlApplicationEditor getXmlApplicationEditor()
   {
      return m_xmlApplicationEditor;
   }

   /**
    * Searches for a resource among currently selectd figures.
    * 
    * @param comps a vector of currently selected figures @ return is <CODE>
    *           true</CODE> if a resoruce is found,otherwise is <CODE>false
    *           </CODE>
    */
   private boolean isResourceSel(Vector comps)
   {
      boolean resSel = false;
      Enumeration e = comps.elements();
      while (e.hasMoreElements())
      {
         Object obj = e.nextElement();
         if (obj instanceof UIConnectableFigure)
         {
            UIConnectableFigure uic = (UIConnectableFigure) obj;
            if (uic.getId() == AppFigureFactory.DATASET_ID
                  || uic.getId() == AppFigureFactory.BINARY_RESOURCE_ID)
            {
               resSel = true;
               break;
            }
         }
      }

      return resSel;
   }

   /**
    * Data flavor for mime type {@link FigureTransfer#sUICOBJ_FLAVOR_TYPE}
    */
   private DataFlavor getConnectableFigureFlavor()
   {
      return new DataFlavor(
            FigureTransfer.sUICOBJ_FLAVOR_TYPE,
            FigureTransfer.sUICOBJ_FLAVOR_NAME);
   }

   /**
    * Convenience method calling
    * {@link ResourceHelper#getWithMnemonic(String, PSResources, String)}.
    */
   protected String getActionName(final String id)
   {
      return ResourceHelper.getWithMnemonic(
            getResources().getString(id), getResources(), id);
   }

   /**
    * Convenience method returning resources.
    */
   protected E2DesignerResources getResources()
   {
      return E2Designer.getResources();
   }

   /**
    * Generates insert menu item id from figure name.
    */
   protected String getInsertIdFromFigureName(final String strFigureName)
   {
      return "menuInsert" + strFigureName;
   }
   
   // ////////////////////////////////////////////////////////////////////////////
   // class storage
   protected MouseEvent m_thePopupMouseEvent;

   // the main scroll pane. this is the right pane of m_split if it exists or
   // otherwise the content pane
   private JScrollPane m_scrollPane;

   private boolean m_bMouseCaptured;

   private UIConnectionPoint m_currentPointOver;

   private boolean m_bInDrop;

   /**
    * The drag under rectangle is used to track the current drag under effect
    * rectangle. This is initialized when the drag enters the component and is
    * reset to <code>null</code> when the drag is finished or when the drag
    * exits the component. The location is updated in
    * <code>positionDragUnderEffect(DropTargetDragEvent)</code> and is used to  draw
    * the rectangle in <code>drawDragUnderEffect(boolean)</code>.
    */
   static HiliteRect ms_dragUnderRect;

   private JLayeredPane m_contentPanel;

   /*
    * The following constants/counters are used for relocating pasted figures. A
    * pasted figure is relocated in y/x by m_pasteOffsetCounter*PASTE_OFFSET.
    * The paste counter is reset to by the following events: copy new figures to
    * the clipboard and scrolling the viewport. It is incremented by 1 after
    * each paste operation.
    */
   private static int PASTE_OFFSET = 10;

   private int m_pasteOffsetCounter = 1;

   /*
    * A bug has been happening where you double click on an object in the pipe
    * to edit it, but it seems to go into drag mode (cursor shows drag copy),
    * and if you look at the stack, one thread is hung in a native call to
    * create the dialog, the other thread is hung in a call to dnd method. <p>
    * Swing seems happy to provide both double click and drag gesture recognized
    * events from a single double-click/drag. To try and prevent this
    * contention, (which I don't understand), I am adding this flag so if a drag
    * starts the edit won't and vice versa. The flag is protected by its access
    * methods <code> acquireMouseActionLock </code> and <code>
    * releaseMouseActionLock </code> . <p> In addition, it prevents multiple
    * modal dialogs from appearing at the same time.
    */
   private boolean m_bLocked = false;

   /**
    * the jdk does not supply the correct dropAction in the DragDropEnd method
    * through the <code>DragSourceDropEvent.getDropAction</code> method (it
    * always returns <code>0</code>), so it needs to be saved while it is
    * available in the <code>Drop</code> method. Must be static since it is
    * set in the target window and later checked in the source window.
    */
   private static int m_dropAction = 0;
   
   /**
    * Application editor of the frame.
    * Initialized in constructor and not changed after that.
    */
   private final PSXmlApplicationEditor m_xmlApplicationEditor;
}
