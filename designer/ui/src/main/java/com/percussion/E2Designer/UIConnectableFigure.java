/******************************************************************************
 *
 * [ UIConnectableFigure.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Vector;

/**
 * A connectable figure is a figure that allows 2 instances to connect in a
 * master-slave relationship. The master contains a connection point for each
 * object that it wants to connect to. This connection point has a list of IDs
 * that limit what objects can connect.
 * <p>
 * When a master object is operated upon, all connected objects behave as if
 * they were part of the master. For example, deleting the master object causes
 * all connected objects to be deleted. Editing the properties of the master
 * object, however, only edits its properties, not the properties of all
 * connected objects.
 * <p>
 * Because of the model being used (connected objects are added as components
 * to the master object), UIConnectableFigure will only support a single dynamic
 * connection. This prevents us from having multiple pipes connected to a single
 * datatank.
 *
 * @see UIConnectionPoint
 */
public class UIConnectableFigure extends UIFigure implements ISelectable
{
   // constructors
   /**
    * Creates a new object with the supplied name.
    *
    * @param strName internal name of the new object, must not be null. The user
    * displayable name and image will be obtained from the E2DesignerResources 
    * bundle using this value as the key and the ResourceHelper class. If no 
    * image is found, then the derived class is responsible for handling the
    * paint messages.
    *
    * @param Data the object that contains the data that this UI object is
    * representing. This may be null if there is no associated data.
    *
    * @param strEditorClassName the object by this name will be called when the user wants
    * to edit the properties of this object. Typically this will be a dialog.
    * This may be null if there are no user editable properties. It is ignored
    * if Data is null. If it is null, a default message is displayed stating
    * that no properties are editable.
    *
    * @param ID the unique identifier for this object. It should be unique 
    * within the 
    *
    * @throws IllegalArgumentException if strName or Data is null
    * @throws MissingResourceException if strImageResource can't be found/opened
    */
   public UIConnectableFigure( String strName, Object Data,
         String strEditorClassName, int ID, String strFactoryClassName)
   {
      super( strName, Data, strEditorClassName, ID);
      m_strFactoryClassName = strFactoryClassName;
      /* override base class to add a perimeter around the image. This is where
         we will draw our selection outline */
      if (!isOwnerDrawn() && !hasImage())
      {

         final Object[] astrParams = {strName};
         throw new MissingResourceException( MessageFormat.format( 
               E2Designer.getResources().getString("MissingResourceKey"), astrParams ),
               "UIConnectableFigure", strName);
      }
      if (hasImage())
      {
         setBaseImageBorderWidth( HILITE_WIDTH );

         Point off = ResourceHelper.getPoint( E2Designer.getResources(), strName );
         m_imageCenterOffset = new Dimension( off.x, off.y );
      }
   }

   /* We need to leave a small space to paint the selection indicator box */
   private static final int HILITE_WIDTH = 1;

   // properties
   /**
    * Allows derived classes to override the default behavior of this class, which
    * is to load an image and display it in the JPanel.
    *
    * @returns <code>true</code> if the figure is drawing itself rather than
    * supplying an image. By default, <code>false</code> is returned.
    */
   public boolean isOwnerDrawn()
   {
      return(false);
   }


   /**
    * @returns <code>true</code> if this figure has any dynamic attachments,
    * otherwise <code>false</code> is returned.
    */
   public boolean isAttached()
   {
      return null == m_dynamicConnections ? false : !m_dynamicConnections.isEmpty();
   }

   /**
    * @returns the name of the factory that created this figure
    */
   public String getFactoryName()
   {
      return(m_strFactoryClassName);
   }

   // ISelectable interface implementation
   public boolean setSelection( boolean bSelect, boolean bAppend )
   {
      boolean bOldSelectionState = m_bSelected;
      if ( /*bSelect &&*/ !bAppend )
      {
         // notify all attached figures
         for ( int index = m_connectionPoints.size()-1; index >= 0; index-- )
         {
            UIConnectionPoint cp = m_connectionPoints.get(index);

            if ( cp.isAttached())
            {
               boolean bSelected = cp.getAttached().isSelected();repaint();
               cp.getAttached().setSelection( false, false );
               if ( bSelected )
                  cp.getAttached().repaint();
            }

         }
      }
      m_bSelected = bSelect;
      return bOldSelectionState;
   }

   /**
    * A selected object will indicate this state by showing an indicator in
    * its visual display. The default state on construction is false.
    *
    * @returns <code>true</code> if currently selected, otherwise <code>false</code> is returned.
    */
   public boolean isSelected( )
   {
      return m_bSelected;
   }

   /**
    * Returns whether or not this object is serialized.
    *
    * @returns <code>true</code> if serialized, otherwise <code>false</code> is returned.
    */
   public boolean areYouSerialized()
   {
    return isSelected();
   }

    /**
     * @return <code>true</code> if the supplied point is over the main object
     * or any of its owned connection points, which may be outside the main
     * figure.
    **/
    @Override
   public boolean isHit(Point pt)
    {
       boolean bHit = super.isHit( pt );
        if (!bHit)
           bHit = isInConnectionPoint( pt );
        return bHit;
    }

   /**
    * @param pt the point to check, in component coordinates
    *
    * @returns <code>true</code> if the passed in point is over a section of
    * the component that can be dragged/dropped. By default, if the point is
    * over the component or any of its connectors, <code>true</code> is returned.
    */
   public boolean isDragPoint(Point pt)
   {
      return isHit( pt );
   }


    /**
     * Checks all connection points owned by this figure to see if any of them
     * contain the supplied point.
     *
     * @param pt the point to check, in component coordinates
     *
     * @return <code>true</code> if any of them do, <code>false</code> otherwise
    **/
    public boolean isInConnectionPoint( Point pt )
    {
        UIConnectionPoint cp = getCPFromPoint( pt );
        return null != cp;
    }


   /**
   * Overriding onEdit so that we can pass it along to rigid children
   */
   public boolean onEdit( Object frameData, Point pt )
   {
      UIFigure child = getChildFromPoint(pt);
      if(child != null)
       return (child.onEdit(frameData));

     //if the label has an editor call it
    if(pt != null && m_labelEditor != null &&
       m_label != null && m_label.getBounds().contains(pt))
      return m_labelEditor.onEdit(frameData);

    return this.onEdit(frameData);
   }

   /**
    * Returns true if the object wants to handle the mouse rather than the
    * frame during a drag and drop. For example, a resizable object does not
    * want to go into drag and drop mode if the user initiates a drag of a
    * resize handle. By default, false is returned.
    */
   public boolean wantsMouse( Point pt )
   {
      UIConnectionPoint cp = getCPFromPoint(pt);
      if(cp != null)
      {
         return(cp.wantsMouse(pt));
      }

      return false;
   }

   /**
   * Returns a child from a point
   */
   public UIFigure getChildFromPoint(Point pt)
   {
      for ( int index = m_rigidChildren.size()-1; index >= 0; index-- )
      {
         UIFigure fig = m_rigidChildren.get(index);

          if(fig.getBounds().contains(pt))
         {
            return(fig);
         }
      }
      
      return(null);
   }
   
   /**
   * Returns a connection point from a point
   */
   public UIConnectionPoint getCPFromPoint(Point pt)
   {
      for ( int index = m_connectionPoints.size()-1; index >= 0; index-- )
      {
         UIConnectionPoint cp = m_connectionPoints.get(index);

         if (!cp.isAttached())
         {
             if(cp.getBounds().contains(pt))
            {
               return(cp);
            }
         }
      }
      
      return(null);
   }
   
   /**
    * Returns a listener to handle mouse messages that occur after a user has
    * clicked on this component and it has returned <code>true</code> when
    * wantsMouse() was called.
    *
    * @param drawingPane the component where drawing will take place during the
    * drag. Typically, this will be the glass pane of the frame drawing pane.
    * This parameter must match the drawing pane passed to getMouseListener.
    *
    * @returns a mouse listener that handles mousePressed and mouseReleased
    * events, or null if unsupported.
    *
    * @throws IllegalArgumentException if this method is supported and 
    * drawingPane is null 
    *
    * @see #getMouseMotionListener
    */
   public MouseListener getMouseListener( Point pt, JComponent drawingPane )
   {
      UIConnectionPoint cp = getCPFromPoint(pt);
      if(cp != null)
         return(cp.getMouseListener(pt, drawingPane));

      return(null);
   }

   /**
    * Returns a listener to handle mouse messages that occur after a user has
    * clicked on this component and it has returned <code>true</code> when
    * wantsMouse() was called.   getMouseListener must be called before this
    * method is called or unpredictable behavior will result.
    *
    * @param drawingPane the component where drawing will take place during the
    * drag. Typically, this will be the glass pane of the frame drawing pane.
    * This parameter must match the drawing pane passed to getMouseListener.
    *
    * @returns a mouse listener that handles mouseDragged events, or null if 
    * unsupported.
    *
    * @throws IllegalArgumentException if this method is supported and 
    * drawingPane is null 
    *
    * @see #getMouseListener
    */
   public MouseMotionListener getMouseMotionListener(Point pt, JComponent drawingPane )
   {
      UIConnectionPoint cp = getCPFromPoint(pt);
      if(cp != null)
         return(cp.getMouseMotionListener(pt, drawingPane));

      return(null);
   }


   // operations
   /**
    * Overrides base class to paint selection indicator.
    */
   @Override
   public void paint(Graphics g)
   {
//      System.out.println( "entering uic paint" );
      super.paint(g);
   }

   /**
    * Paint the figure's image if there is one. We do this here instead of in
    * the paint() method so children get painted properly.
    */
   @Override
   protected void paintComponent(Graphics g)
   {
//      System.out.println( "entering uic component paint" );

      super.paintComponent(g);
   }


   /**
    * Paints a colored box around the base image if the figure's selected 
    * property is <code>true</code>, otherwise it does nothing. The message
    * is propagated to all figures attached to any of this figure's connection
    * points.
    */
   public void paintSelectionIndicator( Graphics g )
   {
//      System.out.println( "entering uic indicator paint" );
      // pass message to all connected slaves
      for ( int index = m_connectionPoints.size()-1; index >= 0; index-- )
      {
         UIConnectionPoint cp = m_connectionPoints.get(index);
         if ( cp.isAttached())
         {
            Point offset = cp.getAttached().getLocation();
            g.translate( offset.x, offset.y );
            cp.getAttached().paintSelectionIndicator( g );
            g.translate( -offset.x, -offset.y );
         }
            
      }

      //only draw the selection bounds if the figure frame 
      //is active
      UIFigureFrame frame = getFigureFrame();
      boolean bFrameSelected = true;
      if(frame != null && !frame.isSelected())
         bFrameSelected = false;
         
      // now do our own
      if ( isSelected() && bFrameSelected)
      {
         g.setColor( Color.red );
         Point loc = getBaseImageLocation();
         Dimension size = getBaseImageSize();
         g.drawRect( loc.x-1, loc.y-1, size.width+1, size.height+1 );
      }
   }

   /**
    * Adds the passed in connection point to the list of connectors on this
    * object. Connection points will accept connections from UIConnectableFigures
    * that have an acceptable ID. Each connection point will accept a single
    * connection. If the connection point is already added, it will not be 
    * added again. The location of the cp should be set to the desired location
    * relative to the image, assuming the image ul corner is at 0,0.
    *
    * @param cp valid connection point
    *
    * @throws IllegalArgumentException if cp is null
    *
    * @see #willAcceptConnect(int)
    */
   public void addConnectionPoint( UIConnectionPoint cp )
   {
      if (null == cp)
      {
         final Object[] astrParams =
         {
            "UIConnectionPoint"
         };
         throw new IllegalArgumentException( MessageFormat.format(
               E2Designer.getResources().getString( "CantBeNull" ), astrParams )); 
      }
      
      if (!m_connectionPoints.contains(cp))
      {
         m_connectionPoints.add(cp);
         // adjust the cp for the border around the image
         Point loc = cp.getLocation();
         Point offset = getBaseImageLocation();
         loc.translate(offset.x, offset.y);
         cp.setLocation(loc);

         add(cp, 0);
      }
   }

   /**
    * Notification method, called at the beginning of a DnD operation
    * by the frame window containing the component. It is called after the 
    * frame recognizes that a drag was initiated but before it does anything.
    * By default, no action is taken. Derived classes may use these for 
    * customizations.
    */
   public void dragStart()
   {
  }

   /**
    * Notification method, called at the end of a DnD operation by the frame
    * window containing the component. It is called after all drag operations
    * have been completed, including removing this component from its container
    * if the user moved the component to another window. By default, no action is taken.
    * Derived classes may use these for customizations.
    *
    * @param bCopy <code>true</code> if the DnD operation was a copy. It is
    * <code>false</code> if the operation was a move.
    */
   public void dragEnd(boolean bCopy)
   {
    if (bCopy || !isAttached())
      return;

    for (int i=0; i<m_dynamicConnections.size(); i++)
    {
      UIConnectionPoint cp = m_dynamicConnections.get(i);
      cp.detach(this, true);
    }
  }

   /**
    * Returns a list of all connection points so they can be enumerated.
    */
   public Enumeration getConnectionPoints( )
   {
      return m_connectionPoints.elements( );
   }

   public boolean removeDynamicConnection( UIConnectionPoint cp )
   {
      System.out.println( "entering: removeDynamicConnection" );
      boolean bSuccess = cp.detach( this, false );
      if (bSuccess)
      {
         m_dynamicConnections.remove(cp);
         if ( m_dynamicConnections.isEmpty())
            setAttachOffset( new Dimension());
      }
         
      return (bSuccess);
   }
   
   /**
    * Creates a connection to the passed in connector, cp. The new dynamic
    * connection is placed at attachPt. </p>
    * This object must be notified via <b>disconnect()</b> when the object it is
    * attached to is destroyed. </p>
    * Derived classes can override setAttachOffset to modify the behavior. By
    * default, the location of this object, when it has a dynamic attachment,
    * is set relative to the attach position. This offset is set by this 
    * method by calling the aforementioned method. 
    *
    * @returns <code>true</code> if successful, <code>false</code> if
    * connection point is locked
    *
    * @param cp a valid connection point
    *
    * @param attachPt location of the connection point, relative to the upper left
    * corner of this object. If point is null, it gets attached to the 
    * preferred position of the cp.
    *
    * @throws IllegalArgumentException if cp is null
    */
   public boolean createDynamicConnection( UIConnectionPoint cp, Point attachPt )
   {
      if (null == cp)
      {
         final Object[] astrParams = 
         {
            "UIConnectionPoint"
         };
         throw new IllegalArgumentException( MessageFormat.format(
               E2Designer.getResources().getString( "CantBeNull" ), astrParams ));
      }

      if (null == attachPt)
      {
         return createDynamicConnection(cp);
      }
//      Debug.assert( m_dynamicConnections.isEmpty(), E2Designer.getResources(),
//            "ConnectingWhileConn", null );
      m_dynamicConnections.add(cp);
      setAttachOffset(new Dimension(attachPt.x, attachPt.y));
      return cp.attach(this);
   }


   /**
    * This is overridden so we can send a message to all dynamically connected
    * objects that we may have changed size/position.
   **/
   @Override
   protected void adjustBounds()
   {
      super.adjustBounds();
      if ( !isAttached())
         return;

      int connections = m_dynamicConnections.size();
      for ( int i = 0; i < connections; ++i )
      {
         UIConnectionPoint cp = m_dynamicConnections.get(i);
         cp.boundsChanged();
      }
   }

   /**
    * If a UIC is connected thru a rigid connector to this figure, and that
    * uic changes size, it will notify its connection point, which will relay
    * the message here. We need to resize ourselves so all our children are
    * optimally enclosed.
   **/
   public void boundsChanged()
   {
      adjustBounds();
   }

   /**
    * When this figure is attached to another, this offset may be set depending
    * on the preferred attach position. It is cleared when the connection
    * is removed.
    */
   protected void setAttachOffset(Dimension offset)
   {
//      System.out.println( "Attach offset set to " + offset.toString());
      m_attachOffset.setSize(offset);
   }

   /**
    * To properly attach to a UIRigidConnector, use this offset when setting
    * the location of this figure inside the master container. For example, if
    * attaching to a connection point with a preferred attach pos of POS_LEFT,
    * and this figure is 50x50, this method would return (50, 25) (the attach
    * would be placed on the right middle of this figure).
    */
   public Dimension getAttachOffset()
   {
      return m_attachOffset;
   }


   /**
    * This is a notification method that is called by connection points when
    * their master has moved. By default, nothing needs to be done. Derived
    * classes may wish to override this method.
    *
    * @param cp The connection point that received the location changed
    * message.
    *
    * @param offset The new location, in the coord system of this figure's
    * parent.
    */
   public void connectionPointLocChanged(
         @SuppressWarnings("unused") UIConnectionPoint cp,
         @SuppressWarnings("unused") Dimension offset )
   {
      // no op
   }


   /**
    * Overrides the base class so all static connectors can be notified of the
    * location change. The connection points are notified before this component
    * is actually moved.
    *
    * @param location The new position of the upper left corner of this component,
    * the coord system of the parent of this comp.
    */
   @Override
   public void setLocation(Point location)
   {
      setLocation( location.x, location.y );
   }


   /**
    * Overrides the base class so all static connectors can be notified of the
    * location change. The connection points are notified before this component
    * is actually moved.
    *
    * @param x The new x coordinate of the upper left hand corner
    *
    * @param y The new y coordinate of the upper left hand corner
    */
   @Override
   public void setLocation( int x, int y )
   {

//      System.out.println( "Overriding setLocation: " + newLoc.toString());

      // If this is called in a base class constructor, there's nothing for us to do
      if ( null != m_attachOffset )
      {
         Point loc = new Point( x, y );
         loc.translate(-m_attachOffset.width, -m_attachOffset.height);
         Point currentLoc = getLocation();
         // send message to all static connectors
         Dimension delta = new Dimension(loc.x - currentLoc.x, loc.y - currentLoc.y);
         for (int index = m_connectionPoints.size()-1; index >= 0; index--)
         {
//            System.out.println("UIC notifying cp " + Integer.toString(index));
            UIConnectionPoint cp = m_connectionPoints.get(index);
            cp.locationChanged( delta );
         }
      }

      super.setLocation( x, y );
   }

   /**
    * Behaves same as its overloaded brother, except it calculates the position
    * based on the preferred position of the passed in cp.
    */
   public boolean createDynamicConnection( UIConnectionPoint cp)
   {
      if (null == cp)
      {
         final Object[] astrParams =
         {
            "UIConnectionPoint"
         };
         throw new IllegalArgumentException( MessageFormat.format(
               E2Designer.getResources().getString("CantBeNull" ), astrParams));
      }

      Point preferredLoc = getPreferredAttachPos( cp );
      /* Adjust for figures that have off-center attach positions */
      Dimension centerOffset = getImageCenterOffset();
      preferredLoc.translate( centerOffset.width, centerOffset.height );
      return createDynamicConnection(cp, preferredLoc );
   }


   protected Point getPreferredAttachPos( UIConnectionPoint cp )
   {
      Dimension sizeBounding = getBaseImageSize();
      Point imageLocation = getBaseImageLocation();

      int RelPos = cp.getPreferredAttachPosition();
      Point attachPt = new Point();

      // clear bits that aren't top or bottom
      int VerticalPos = RelPos
            & (UIConnectionPoint.POS_TOP | UIConnectionPoint.POS_BOTTOM);
      if (0 == VerticalPos)
         // Default
         attachPt.y = sizeBounding.height / 2;
      else if (0 == (~UIConnectionPoint.POS_TOP & VerticalPos))
         attachPt.y = sizeBounding.height - HILITE_WIDTH;
      else if (0 == (~UIConnectionPoint.POS_BOTTOM & VerticalPos))
         attachPt.y = HILITE_WIDTH-1;
      else
         attachPt.y = sizeBounding.height / 2;
      // adjust for image offset
      attachPt.y += imageLocation.y;

      // clear bits that aren't left or right
      int HorizPos = RelPos
            & (UIConnectionPoint.POS_LEFT | UIConnectionPoint.POS_RIGHT);
      if (0 == HorizPos)
         // Default
         attachPt.x = sizeBounding.width / 2;
      else if (0 == (~UIConnectionPoint.POS_LEFT & HorizPos))
         attachPt.x = sizeBounding.width - HILITE_WIDTH;
      else if (0 == (~UIConnectionPoint.POS_RIGHT & HorizPos))
         attachPt.x = HILITE_WIDTH-1;
      else
         attachPt.x = sizeBounding.width / 2;
      // adjust for image offset
      attachPt.x += imageLocation.x;

      return attachPt;
   }

   /**
    * This is a notification method for use by UIConnectionPoint. It should be
    * called whenever the connector wants to detach from this figure.
    * Removes the supplied dynamic connection from the list of connections, but
    * does not notify the connection point.
    */
   public void detach(UIConnectionPoint cp)
   {
      m_dynamicConnections.remove(cp);
   }

   /**
    * Returns a list of all dynamic connections so they can be enumerated.
    */
   public Enumeration getDynamicConnections( )
   {
      return m_dynamicConnections.elements( );
   }

   /**
    * Remove myself from my parent.
    */
   public void remove()
   {
      if(getParent() != null)
      {
         setVisible(false);
      // Removing the dynamic connections of all the UIConnectors attached to
      // this figure.
      for (int i = 0; i < m_connectionPoints.size(); i++)
      {
        UIConnectionPoint cPoint = m_connectionPoints.get(i);
        int count = cPoint.getAttachedFigureCount();
        for (int j = 0; j < count; j++)
        {
          UIConnectableFigure uic = cPoint.getAttached( 0 );
          if ( AppFigureFactory.DIRECTED_CONNECTION_ID == uic.getId())
            uic.removeDynamicConnection(cPoint);
        }
      }

      if (isAttached())
      {
          while(m_dynamicConnections.size() > 0)
          removeDynamicConnection(m_dynamicConnections.get(0));
      }

         getParent().remove(this);
      }
   }

   /**
    * Scans list of static connection points and asks each one if it will accept a
    * connection from an object with the supplied ID.
    *
    * @param ID an identifier for the figure asking to connect
    *
    * @param constraint a constraint that may further limit who is allowed to connect,
    * may be null
    *
    * @returns <code>true</code> if any static connector on this object will accept a
    * connection and is not locked, <code>false</code> otherwise
    */
   public boolean willAcceptConnect(int ID, List<IConnectionConstraint> constraint)
   {
      Enumeration points = getConnectionPoints();
      boolean bFound = false;
      while (points.hasMoreElements() && !bFound)
      {
         UIConnectionPoint cp = (UIConnectionPoint) points.nextElement();
         if (cp.willAllowAttach( ID, constraint ))
            bFound = true;
      }
//      System.out.println((bFound ? "Accepting" : "Rejecting")
//            + " connection for id " + Integer.toString(ID));
      return bFound;
   }


   public boolean willAcceptConnect( int ID )
   {
      return willAcceptConnect( ID, null );
   }

   public List<IConnectionConstraint> getConnectionConstraint()
   {
//      System.out.println("getConnectionConstraint in UIConnectableFigure.");
      return null;
   }

   /**
    * Scans the list of connection points and finds the one closest to the
    * supplied point.
    *
    * @param ID unique identifier for a UI object
    *
    * @param constraint if not null, an object used by the connection point in
    * determining whether a figure with the supplied ID will be allowed to
    * connect.
    *
    * @param pt location against which connector distance is measured,
    * relative to upper left corner of object
    *
    * @returns a connector that will connect to an object with the supplied ID
    * or null if none will.
    */
   public UIConnectionPoint getClosestConnector( int ID, List<IConnectionConstraint> constraint, Point pt )
   {
//      System.out.println("get closest connector");
      if (m_connectionPoints.isEmpty())
         return null;

      Vector<UIConnectionPoint> allowedConnectors = new Vector<UIConnectionPoint>(4);

      // first find connectors that can connect, then find closest one
      
      for (int index = m_connectionPoints.size()-1; index >= 0; index--)
      {
         UIConnectionPoint cp = m_connectionPoints.get(index);
         if (cp.willAllowAttach( ID, constraint ))
            allowedConnectors.add(cp);
      }
      if (allowedConnectors.isEmpty())
         return (null);
      if (1 == allowedConnectors.size())
         return allowedConnectors.get(0);

      // find closest one to drop point
//      System.out.println( "drop pt = " + pt.toString());

      int index = allowedConnectors.size() - 1;
//      System.out.println("Found " + Integer.toString(index+1) + " matching pts");
      UIConnectionPoint closest = allowedConnectors.get(index--);
      Point tryPoint = closest.getLocation();
      Dimension offset = closest.getOwnerAttachPointOffset();
      tryPoint.translate( offset.width, offset.height );
//      System.out.println( "Try pt = " + tryPoint.toString());
      double distance = pointDistance( pt, tryPoint );

      for ( ; index >= 0; index-- )
      {
         UIConnectionPoint test = allowedConnectors.get(index);
         tryPoint = test.getLocation();
         offset = test.getOwnerAttachPointOffset();
//         System.out.println("attach offset = " + offset.toString());
         tryPoint.translate( offset.width, offset.height );
//         System.out.println( "Try pt = " + tryPoint.toString());
         double newDistance = pointDistance( pt, tryPoint );
//         System.out.println("current d = " + String.valueOf(distance)
//               + "; new d = " + String.valueOf(newDistance));
         if ( newDistance < distance )
         {
            distance = newDistance;
            closest = test;
//            System.out.println("found closer, attachPos = "
//                  + Integer.toString(test.getPreferredAttachPosition()));
         }
      }
      return closest;
   }

   /**
    * Scans the list of connection points and finds the one that matches the
   * id we are looking for. Returns null if not found.
    *
    * @param id a unique connection point id
    *
    * @return UIConnectionPoint the connection point found or null
    */
   public UIConnectionPoint getConnectionPoint(int id)
   {
     for (int i=0; i<m_connectionPoints.size(); i++)
       if (m_connectionPoints.elementAt(i).getConnectionPointId() == id)
         return m_connectionPoints.elementAt(i);

     return null;
   }

   private double pointDistance( Point pt1, Point pt2 )
   {
      int len = pt1.x - pt2.x;
      int height = pt1.y - pt2.y;
      return Math.sqrt(len * len + height * height);
   }

   /**
    * The type is used in the figure factory to create the figures
    */
   public void setType(String strType)
   {
      m_strType = strType;
   }
   
   public String getType()
   {
      return m_strType;   
   }
   
   /**
    * Functions to suppor recursive retreival of objects
    */
   public void getSelected(Vector<UIConnectableFigure> uics)
   {
      getAll(uics, true);
   }
   
   public void getAll(Vector<UIConnectableFigure> uics)
   {
      getAll(uics, false);
   }
   
   private void getAll(Vector<UIConnectableFigure> uics, boolean bOnlySelected)
   {
      if(bOnlySelected)
      {
         if(isSelected())
         {
            uics.add(this);
            return;
         }
      }
      else
         uics.add(this);

    // If this figure is a pipe, there is no need to propagate the selection
    // highlight down to its connected children (eg: mapper). Otherwise,
    // continue selection highlight (via uic.getAll() method) propagation.
    /*
    if ( this.getName().equals(PipeFigureFactory.QUERY_PIPE) ||
         this.getName().equals(PipeFigureFactory.UPDATE_PIPE) )
      return;
    */

      Component [] comps = getComponents();
      for ( int index = comps.length-1; index >= 0; index-- )
      {
         if ( comps[index] instanceof UIConnectableFigure )
         {
            UIConnectableFigure uic = (UIConnectableFigure) comps[index];
            if(bOnlySelected)
            {
               if(uic.isSelected())
               {
                  uics.add(uic);
               }
               else
               {
                  uic.getAll(uics, bOnlySelected);
               }
            }
            else
            {
               uics.add(uic);
               uic.getAll(uics, bOnlySelected);
            }
         }                                 
      }
   }

   /**
   * This function is used to decide if this object can be edited
   */
   public boolean isEditable()
   {
      if(getData() == null ||
         getEditorName() == null ||
         getEditorName().length() == 0)
            return(false);

      return(true);
   }


   /**
   * Set the canBeAttached setting
   */
   public void setCanBeAttached(boolean bCan)
   {
      m_bCanBeAttached = bCan;
   }

   /**
   * This function returns properties needed by the UIFigureFrame
   * during drag and drop.
   */
   public Properties getDragInfoProperties()
   {
      Properties props = new Properties();
      //if there is no CAN_BE_ATTACHED property
      //then this object can not be attachment
      if(m_bCanBeAttached)
         props.setProperty(CAN_BE_ATTACHED, "1");
         
      //we need to know the constraints during drag and drop
      if(getConnectionConstraint() != null)
         props.put(CONSTRAINTS, getConnectionConstraint());

      return(props);
   }
   
   public static String CAN_BE_ATTACHED = "CanBeAttached";
   public static String CONSTRAINTS = "Constraints";

   /**
   * Add a rigid child.  Rigid children have a location and can be edited but
   * can not be moved.
   */
   public void addRigidChild(UIFigure fig, Point location)
   {
      m_rigidChildren.add(fig);
      if(location != null)
         fig.setLocation(location);

      add(fig);
   }

  /**
   * When attaching to a rigid connection point that has a preferred attach
   * position of POS_CENTER, the image will be translated by this amount
   * when attaching.
  **/
  public Dimension getImageCenterOffset()
  {
      return ( null == m_imageCenterOffset ? new Dimension() : m_imageCenterOffset );
  }

   /**
    * Prepares this object for serialization.
    */
  transient private Vector<UIConnectionPoint> m_tempDynamicConnections = null;
   public void prepareSerialization()
   {
    m_tempDynamicConnections = new Vector<UIConnectionPoint>();

    for (int i=0; i<m_dynamicConnections.size(); i++)
    {
      UIConnectionPoint cp = m_dynamicConnections.get(i);
      UIConnectableFigure fig = cp.getOwner();
      if (!fig.areYouSerialized())
        m_tempDynamicConnections.add(cp);
    }

    for (int i=0; i<m_tempDynamicConnections.size(); i++)
      m_dynamicConnections.remove(m_tempDynamicConnections.get(i));
   }

   /**
    * Finish up after serialization.
    */
   public void finishSerialization()
   {
    for (int i=0; i<m_tempDynamicConnections.size(); i++)
      m_dynamicConnections.add(m_tempDynamicConnections.get(i));
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

  //////////////////////////////////////////////////////////////////////////////
   // variables
  /**
   * The list of static connections points (UIConnectionPoint) that receives
   * connection from m_dynamicConnections connection points.
   */
   private Vector<UIConnectionPoint> m_connectionPoints = new Vector<UIConnectionPoint>(5);
   private Vector<UIFigure> m_rigidChildren = new Vector<UIFigure>(0);

  /**
   * The list of UIConnectionPoints that ATTACHES to static connection points
   * (represented by m_connectionPoints). These MUST be cleared for an attached
   * figure to be removed. Not all figures has this member. An example figure
   * that uses this list is the Connector, which is also represented by
   * UIConnector object.
   */
   protected Vector<UIConnectionPoint> m_dynamicConnections = new Vector<UIConnectionPoint>(1);
   private boolean m_bSelected = false;

   /**
    * The class name of the factory that created this object. This name, along
    * with the figure ID should provide a unique identifier for all connectable
    * objects of any type.
    */
   private String m_strFactoryClassName = null;

    /**
     * This offset is used when attaching to a connector that has a preferred
     * attach position of POS_CENTER. It allows the image to override what is
     * considered the 'center' of the image. When attached to such a cp, the
     * uic will be attached to the center translated by this value. Its value
     * is set at construction and used when setting the m_attachOffset variable.
     * Use getImageCenterOffset() to read this value.
    **/
    protected Dimension m_imageCenterOffset = null;

   /**
    * The offset of the dynamic attachment point, from the upper left corner
    * of the figure; in comp coords.
    */
   protected Dimension m_attachOffset = new Dimension();

   protected String m_strType = null;

   /**
   * This field indicates if this object can be attached to another
   */
   protected boolean m_bCanBeAttached = true;
}


