/******************************************************************************
 *
 * [ UIFlexibleConnectionPoint.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import com.percussion.workbench.ui.editors.form.PSFrameProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.MessageFormat;
import java.util.Vector;

/**
 * This cp notifies the connected object whenever the owner moves but does not
 * control the location of the attached object.
 */
class UIFlexibleConnectionPoint extends UIConnectionPoint
{
   // constructors - all constructors are mirrors of the base class
   public UIFlexibleConnectionPoint( String strImageResource, UIConnectableFigure Owner,
            Point location, int id )
   {
      super( strImageResource, Owner, location, id );
        init();
    }

   public UIFlexibleConnectionPoint( String strImageResource, UIConnectableFigure Owner,
            Point location, int preferredAttachPos, int id )
   {
      super( strImageResource, Owner, location, preferredAttachPos, id);
        init();
    }

   public UIFlexibleConnectionPoint( String strImageResource, UIConnectableFigure Owner,
            Point location, int preferredAttachPos, int id, Object data )
   {
      super( strImageResource, Owner, location, preferredAttachPos, id, data);
        init();
    }

   public UIFlexibleConnectionPoint( String strImageResource, UIConnectableFigure Owner,
            Point location, Dimension offset, int preferredAttachPos, int id , Object data)
   {
      super( strImageResource, Owner, location, offset, preferredAttachPos, id, data);
        init();
    }

   private void init()
   {
       m_slaveAttachOffset = calcSlaveOffset( getPreferredAttachPosition());
      //every flex connection point has the io constraint
      m_attacherConstraints.add(new IOConstraint(this));
   }

   public void setAsInput( boolean bInput )
   {
      m_bInput = bInput;
   }

   public boolean isAnInput()
   {
      return(m_bInput);
   }

   /**
    * Creates the 'attachment' by remembering the slave's reference. While
    * attached, the slave will get notifications whenever the owner of this
    * connector moves.
    */
   public boolean attach( UIConnectableFigure slave )
   {
      System.out.println("Attaching");

      {
         final String [] astrParams =
         {
            "UIFlexibleConnectionPoint"
         };
         Debug.assertTrue(slave instanceof UIConnector, E2Designer.getResources(),
               "IncorrectType", astrParams );
      }

      if (null == slave)
      {
         final Object[] astrParams =
         {
            "UIConnectableFigure"
         };
         throw new IllegalArgumentException( MessageFormat.format(
               E2Designer.getResources().getString( "CantBeNull" ), astrParams ));
      }
      boolean bAllow = !isLocked();
      // remove any current connections
//      detach(true);
      if (bAllow)
      {
         m_attachedFigures.add( slave );
         // send message to the slave indicating location and attach position
         ((UIConnector) slave).setConnectionLocation( this, getSlaveAttachPointOffset());
         System.out.println("Attachment complete");
      }
      return bAllow;
   }

   public boolean detach( UIConnectableFigure slave, boolean bForce )
   {
      System.out.println("Detaching");
      boolean bDetach = !isLocked() || bForce;
      if (bDetach && (m_attachedFigures.size() > 0))
      {
         m_attachedFigures.remove(slave);
      }

      return bDetach;
   }

   /**
   * Connection points can take hold of the mouse
   * UIConnectableFigure will only call us if the pt is within our bounds
   */
   public boolean wantsMouse(Point pt)
   {
      return(true);
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
    * @throws IllegalArgumentException if drawingPane is null, or wantsMouse()
    * returns <code>false</code> on the supplied point
    *
    * @see #getMouseMotionListener
    */
   public MouseListener getMouseListener( Point pt, JComponent drawingPane )
   {
      if (null == drawingPane || !wantsMouse( pt ))
         throw new IllegalArgumentException("null drawing pane or invalid point passed");
      return (MouseListener)new FlexMouseAdapter(drawingPane);
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
   public MouseMotionListener getMouseMotionListener( Point pt, JComponent drawingPane )
   {
      return (MouseMotionListener) new FlexMouseMotionAdapter(drawingPane);
   }


   //Inner classes for mouse events
   public class FlexMouseAdapter extends EscMouseAdapter
   {
      FlexMouseAdapter(JComponent drawingPane)
      {
         m_drawingPane = drawingPane;         
      }
      
      public void mousePressed(MouseEvent e)
      {
         super.mousePressed(e);

         Point pt = e.getPoint();
         Point glasspt = SwingUtilities.convertPoint(e.getComponent(),
                                          pt,
                                          m_drawingPane);
                                    
         UIFigureFrame frame = getFrame((Container)m_drawingPane);
         if(frame != null)
         {
            try
            {
                     m_connector = (UIConnector) getFigureFactory().createFigure(AppFigureFactory.DIRECTED_CONNECTION);
               frame.setMouseCaptured();
            }
            catch (FigureCreationException ex)
            {
               PSDlgUtil.showError(ex, true,
                     E2Designer.getResources().getString("OpErrorTitle")); 
            }
         }
                                          
         setPressedPoint(glasspt);
      }
         
      public void mouseReleased(MouseEvent e)
      {
         //erase
         Point pressed = getPressedPoint();
         if(pressed != null)
         {
            Point dragged = getDraggedPoint();
            Graphics g = m_drawingPane.getGraphics();
            g.setColor(Color.black);
            g.setXORMode(Color.white);

            //erase the line if needed
            if(dragged != null && m_connector != null)
            {
               g.drawLine(pressed.x,pressed.y,dragged.x,dragged.y);
               //now create connector
                     UIConnector connector = m_connector;
               UIFigureFrame frame = getFrame((Container)m_drawingPane);
               //find figure frame
               
               if(frame != null && EscMouseAdapter.wasEscPressed())
                  frame.setCursorToDefault();   
               
               if(frame != null && !EscMouseAdapter.wasEscPressed())
               {
                      frame.add(connector);
                  frame.addToSelection(connector, true);
                  
                  //attach to first side
                      connector.createDynamicConnectionProgrammatic(UIFlexibleConnectionPoint.this, false);
                                             
                  UIConnectionPoint cp = frame.getCurrentPointOver();
                  if(cp != null)
                     connector.createDynamicConnectionProgrammatic(cp, true);                     
                  else                     
                     connector.modifyEndpoint(true, connector.getFirstAttached(), 
                                       SwingUtilities.convertPoint( m_drawingPane,
                                                            dragged, connector ), 
                                    null);
               }
            }
         }

         resetPoints();
      }

      
      private JComponent m_drawingPane = null;
   }

   public class FlexMouseMotionAdapter extends MouseMotionAdapter
   {
      FlexMouseMotionAdapter(JComponent drawingPane)
      {
         m_drawingPane = drawingPane;         
      }

      public void mouseDragged(MouseEvent e)
      {
         Point pressed = getPressedPoint();
         if(pressed != null)
         {
            Point dragged = getDraggedPoint();
            Graphics g = m_drawingPane.getGraphics();
            g.setColor(Color.black);
            g.setXORMode(Color.white);

            //erase the line if needed
            if(dragged != null)
            {
               g.drawLine(pressed.x,pressed.y,dragged.x,dragged.y);
            }
            
            dragged = e.getPoint();
            setDraggedPoint(SwingUtilities.convertPoint(e.getComponent(),
                                             dragged,
                                             m_drawingPane));
            dragged = getDraggedPoint();
            g.drawLine(pressed.x,pressed.y,dragged.x,dragged.y);
         }
         
         
         //are we over a drop?
         /*   Point pt = e.getPoint();
         
         TableInternalFrame tableFrame = m_theFrame.getTableFrame(SwingUtilities.convertPoint(m_theFrame.getDragPane(),
                                                                         pt,
                                                                         m_theFrame.getTheGlassPane()));
         if (tableFrame != null)
         {
            m_theFrame.getCursorComponent().setCursor(m_theFrame.getWillCursor());
            
            //the original
            Point listpoint = SwingUtilities.convertPoint(m_theFrame.getDragPane(),
                                               pt,
                                               tableFrame.getListbox());
            
             
            m_theFrame.setDropFrame(tableFrame);
            m_theFrame.setDropIndex(tableFrame.getListbox().locationToIndex(listpoint));
            tableFrame.setSelected(true, listpoint);   
         }
         else
         {
            m_theFrame.getCursorComponent().setCursor(m_theFrame.getWontCursor());
            m_theFrame.setDropFrame(m_theFrame);
            
            TableInternalFrame tf = m_theFrame.getDropFrame();
            if(tf != null)
               tf.setSelected(false, new Point(0,0));
         }*/
      }
      
      private JComponent m_drawingPane = null;
      
   }

   protected void setPressedPoint(Point pt)
   {
      m_pressedPoint = pt;
   }
   
   protected Point getPressedPoint()
   {
      return(m_pressedPoint);
   }
   
   protected Point getDraggedPoint()
   {
      return(m_draggedPoint);
   }

   protected void setDraggedPoint(Point pt)
   {
      m_draggedPoint = pt;
   }
   
   protected void resetPoints()
   {
      m_pressedPoint = null;
      m_draggedPoint = null;   
      m_connector = null;
   }


    /**
     * Calculates the offset from the ul corner of the connector of the slave
     * attach point based on the supplied preferred attach position. The offset
     * is calculated so the slave attaches to the opposite side of the connector
     * relative to the owner attach position.
     *
     * @return the calculated offset.
    **/
   private Dimension calcSlaveOffset( int preferredAttachPos )
    {
       Dimension offset = new Dimension();
        Dimension size = getSize();
       switch ( preferredAttachPos )
        {
           case POS_LEFT:
            offset.height = size.height / 2;
               break;

           case POS_RIGHT:
               offset.width = size.width;
                offset.height = size.height / 2;
               break;

           case POS_TOP:
               offset.width = size.width / 2;
               break;

           case POS_BOTTOM:
               offset.width = size.width / 2;
                offset.height = size.height;
               break;

            default:
               /* POS_CENTER has no offset, in general, this preferred position
                   is not used for flexible connectors */
               break;
        }
        return offset;
    }


   protected FigureFactory getFigureFactory( )
   {
      return FigureFactoryManager.getFactoryManager( ).getFactory( sFIGURE_FACTORY );
   }

   private static final String sFIGURE_FACTORY =
      "com.percussion.E2Designer.AppFigureFactory";


   private UIFigureFrame getFrame(Container comp)
   {
      while(comp != null && comp.getParent() != null)
      {
         if(comp.getParent() instanceof UIFigureFrame)
         {
            return (UIFigureFrame)comp.getParent();
         }
         if (comp instanceof PSFrameProvider)
         {
            final PSFrameProvider provider = (PSFrameProvider) comp;
            if (provider.getFrame() instanceof UIFigureFrame)
            {
               return (UIFigureFrame) provider.getFrame();
            }
         }
         comp = comp.getParent();
      }

      return(null);
   }

   /**
    * Prepares this object for serialization.
    */
  transient private Vector<UIConnectableFigure> m_tempAttachedFigures = null;
   public void prepareSerialization()
   {
    m_tempAttachedFigures = new Vector<UIConnectableFigure>();

    for (int i=0; i<m_attachedFigures.size(); i++)
    {
      UIConnectableFigure fig = (UIConnectableFigure) m_attachedFigures.get(i);
      if (!fig.areYouSerialized())
        m_tempAttachedFigures.add(fig);
    }

    for (int i=0; i<m_tempAttachedFigures.size(); i++)
      m_attachedFigures.remove(m_tempAttachedFigures.get(i));
   }

   /**
    * Finish up after serialization.
    */
   public void finishSerialization()
   {
    for (int i=0; i<m_tempAttachedFigures.size(); i++)
      m_attachedFigures.add(m_tempAttachedFigures.get(i));
   }

   //////////////////////////////////////////////////////////////////////////////
   // Serializable interface optional implementation
   private void writeObject(ObjectOutputStream stream) throws IOException
   {
    prepareSerialization();
      stream.defaultWriteObject();
    finishSerialization();
   }

   //////////////////////////////////////////////////////////////////////////////
   // Serializable interface optional implementation
   private  void readObject(ObjectInputStream stream) throws IOException,
                                                            ClassNotFoundException
   {
      stream.defaultReadObject();
   }

   // storage
   /**
    * Is this an input or output connector (<code>true</code> means input).
    */
   private boolean m_bInput = false;
   private Point m_pressedPoint = null;
   private Point m_draggedPoint = null;
   private UIConnector m_connector = null;
}
