/*[ UIConnectionPoint.java ]***************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Vector;

/**
 * Object that supports connectivity between 2 or more UIConnectableFigure objects. Each
 * object type in the window has a unique identifier. The connection point (CP)
 * maintains a list of IDs that should be allowed to attach. It also maintains
 * references to the attached objects, passing on various commands to the
 * attached objects (for example, moving and painting). 
 */
public abstract class UIConnectionPoint extends UIFigure
{
   // constructors
   /**
    * Creates a connection point whose preferred position is POS_CENTER. 
    * After creating the point, allowed ids must be added before any object 
    * is allowed to connect.
    *
    * @param strImageResource resource key to get the filename for this object. 
    * The key must contain a filename for a valid gif. ResourceHelper class is
    * used to load the image. 
    *
    * @param Owner owner of this connection point, it cannot be null. If it
    * is null, an IllegalArgumentException is thrown.
    *
    * @param location the position of the attach point. The position of the 
    * connector relative to this point is determined by the preferred attach
    * type. The position is set such that a line coming in from the preferred
    * attach position would hit the attach pt before touching any other part 
    * of the connector (except for POS_CENTER, which puts the attach point in
    * the center of the connector). This can be overridden in other constructors by 
    * supplying a specific offset. The location is relative to the owners 
    * coordinate system.
   *
   * @param cpId an id for the connection point. This id must be unique within
   * the figure the connection point is added to.
    *
    * @throws MissingResourceException - thrown if the supplied image filename
    * does not reference a valid image file.
    *
    * @throws IllegalArgumentException if Owner is not a valid UIConnectableFigure
    */
   public UIConnectionPoint( String strImageResource, UIConnectableFigure Owner,
            Point location, int cpId)
   {
      this(strImageResource, Owner, location, new Dimension(INVALID_WIDTH, 0),
           POS_CENTER, cpId);
   }

   /**
    * Similar to simplest constructor, except it allows the caller to specify
    * the preferred attach position as well.
    */
   public UIConnectionPoint( String strImageResource, UIConnectableFigure Owner,
            Point location, int preferredAttachPos, int cpId)
   {
      this(strImageResource, Owner, location, new Dimension(INVALID_WIDTH, 0),
             preferredAttachPos, cpId);
   }

   public UIConnectionPoint( String strImageResource, UIConnectableFigure Owner,
            Point location, int preferredAttachPos, int cpId, Object data)
   {
      this(strImageResource, Owner, location, new Dimension(INVALID_WIDTH, 0),
             preferredAttachPos, cpId, data);
   }
   
   public UIConnectionPoint( String strImageResource, UIConnectableFigure Owner,
            Point location, Dimension offset, int preferredAttachPos , int cpId)
   {
      this(strImageResource, Owner, location, offset, preferredAttachPos, cpId, null);   
   }
   
   /**
    * Same as the basic constructor, but allows the caller to provide an offset.
    * The offset changes where the image is drawn.
    *
    * @param offset how much the upper left corner of the image is shifted 
    * before it is drawn. Other constructors default to (0, 0). When specified
    * by other constructors, the true offset of the image from the specified 
    * location is dependent on the preferred attach postion. If the attach
    * position is center, then the image is drawn with its center at the 
    * specified location. If the user specifies a value, no adjustments are
    * made for the preferred attach position.
    */
   public UIConnectionPoint( String strImageResource, UIConnectableFigure Owner,
            Point location, Dimension offset, int preferredAttachPos , int cpId, Object data)
   {
      super( strImageResource, data, null, 0 );
         
      m_attacheeConstraints = new ArrayList<IConnectionConstraint>();
      m_attacherConstraints = new ArrayList<IConnectionConstraint>();

         //every connection point has the id constraint
      m_attacheeConstraints.add(new IDConstraint(this));

         m_cpId = cpId;
      if (null == Owner)
      {
         final Object[] astrParams = 
         {
            "UIConnectableFigure"
         };
         throw new IllegalArgumentException( MessageFormat.format( 
               E2Designer.getResources().getString( "CantBeNull" ), astrParams )); 
      }

      ImageIcon image = ResourceHelper.getIcon( E2Designer.getResources(),
                     strImageResource );
      if (null == image)
         throw new MissingResourceException(
               E2Designer.getResources().getString("MissingResourceString"),
               "UIConnectionPoint", strImageResource);

      setPreferredAttachPosition(preferredAttachPos);
      m_owner = Owner;
      if ( INVALID_WIDTH == offset.width )
      {
         if ( POS_CENTER == preferredAttachPos )
            m_ownerAttachOffset.setSize( image.getIconWidth()/2, image.getIconHeight()/2 );
         else if ( POS_LEFT == preferredAttachPos )
            m_ownerAttachOffset.setSize( image.getIconWidth()-1, image.getIconHeight()/2 );
         else if ( POS_TOP == preferredAttachPos )
            m_ownerAttachOffset.setSize( image.getIconWidth()/2, 0 );
         else if ( POS_RIGHT == preferredAttachPos )
            m_ownerAttachOffset.setSize( 0, image.getIconHeight()/2 );
         else if ( POS_BOTTOM == preferredAttachPos )
            m_ownerAttachOffset.setSize( image.getIconWidth()/2, (image.getIconHeight()-1) );
         else
            Debug.assertTrue( false, E2Designer.getResources(), "UnsupportedCombo", null );
      }
      else
      {
         m_ownerAttachOffset.setSize( offset );
      }

      // override the location
      Point pt = new Point(location.x - m_ownerAttachOffset.width, location.y - m_ownerAttachOffset.height );
//      System.out.println("cp offset = " + m_offset.toString());
//      System.out.println("setting cp loc: " + pt.toString());
      setLocation( pt );

   }

   public void setLocation( Point pt )
   {
      setLocation( pt.x, pt.y );
   }

   /**
    * Override base class to trap location changes within the owner component.
    * A message is sent to the attached object so it can redraw itself.
   **/
   public void setLocation( int x, int y )
   {
      Point originalLoc = getLocation();
      super.setLocation( x, y );
      locationChanged( new Dimension( x - originalLoc.x, y - originalLoc.y ));
   }

   // properties
   /**
    * Returns true if the connector is currently locked, otherwise false is
    * returned. A locked connector will not allow attachments or
    * detachments. The connector is unlocked when created.
    */
   public boolean isLocked( )
   {
      return m_bLocked;
   }

   /**
    * Sets the lock property of this object to locked if bLockState is true,
    * otherwise clears the lock property. A locked connector will not accept
    * attachments or allow detachments.
    *
    * @param bLockState pass in <code>true</code> to lock the connector, 
    * <code>false</code> to unlock the connector.
    *
    * @returns the previous state of the lock
    */
   public boolean setLocked( boolean bLockState )
   {
      boolean bOldState = m_bLocked;
      m_bLocked = bLockState;
      return bOldState;
   }

   /**
    * @returns <code>true</code> if there is an attached figure, otherwise,
    * <code>false</code> is returned
    */
   public boolean isAttached()
   {
      return(null != getAttached());
   } 

   /**
    * Returns the current size of the slop around the object. The slop is the
    * extra space around the object that is added to the object's size when
    * determining if a mouse click hits the object. Slop is >= 0. The default
    * setting is 0.
    */
   public int getSlop( )
   {
      return m_iSlop;
   }

   /**
    * Returns connection point id set during constuction.
    */
   public int getConnectionPointId()
   {
      return m_cpId;
   }

   /**
    * Sets the slop for this object. iSlop is added to all sides of the object
    * before checking for a mouse hit.
    *
    * @param iSlop amount to enlarge the object when doing hit processing
    * for mouse clicks.
    *
    * @throws IllegalArgumentException if iSlop is < 0
    */
   public void setSlop( int iSlop )
   {
      if (iSlop < 0)
         throw new IllegalArgumentException( 
               E2Designer.getResources().getString( "NegativeNotAllowed" ));
      m_iSlop = iSlop;
   }

   /**
    * Returns the current owner of this connector. The owner is always valid.
    */
   public UIConnectableFigure getOwner( )
   {
      return m_owner;
   }

   /**
    * Checks ID against the list of allowed IDs and returns <code>true</code> if it is
    * found on the list, otherwise <code>false</code> is returned.
    *
    * @param ID unique, non-zero identifier for an object that will be allowed
    * to attach to this connection point.
    *
    * @param constraint an object that can be used by derived classes to aid
    * in making their determination. By default, it is ignored.
    */
   public boolean willAllowAttach( int ID, List<IConnectionConstraint> constraint )
   {
//      System.out.println("Checking constraints");

      //first check the attacher's constaints
      if(constraint != null)
      {
//         System.out.println("constraints not null");

         for(int iConstraint = 0; iConstraint < constraint.size(); ++iConstraint)
         {
            IConnectionConstraint rConstraint = constraint.get(iConstraint);
            if(rConstraint != null)
            {
//               System.out.println("constraint not null");

               if(!rConstraint.acceptConnection(ID, this))
                  return(false);
            }
         }
      }

      //now check our constraints
      for(int iConstraint = 0; iConstraint < m_attacheeConstraints.size(); ++iConstraint)
      {
         IConnectionConstraint rConstraint = (IConnectionConstraint)m_attacheeConstraints.get(iConstraint);
         if(rConstraint != null)
         {
            if(!rConstraint.acceptConnection(ID, null))
               return(false);
         }
      }

      return(true);
   }

   /**
    * Each connection point
    */
   public void setOffset(Point pt)
   {

   }

   /**
    * @return The offset from the upper left corner of this connector where
    * the owner should attach. This value is used to know where to draw the
    * connector relative to the owner.
   **/
   public Dimension getOwnerAttachPointOffset()
   {
      return ( m_ownerAttachOffset );
   }

   /**
    * @return The offset from the upper left corner of this connector where
    * the slave should attach. This value is used to know where to draw the
    * slave relative to the connector.
   **/
   public Dimension getSlaveAttachPointOffset()
   {
      return ( null == m_slaveAttachOffset ? m_ownerAttachOffset : m_slaveAttachOffset );
   }


   // operations
   /**
    * This is a notification message sent by the owner of this connection that
    * the owner has moved. The connection point then passes it on to any
    * connections it knows about.
    *
    * @param offset The distance moved, in pixels. Positive x,y is right, down.
    */
   public void locationChanged( Dimension offset )
   {
      if ( null != m_attachedFigures && m_attachedFigures.size() > 0 )
      {
         // send message to all attachees
         int size = m_attachedFigures.size();
         for ( int index = 0; index < size; index++ )
         {
            UIConnectableFigure  uic = (UIConnectableFigure) m_attachedFigures.get( index );
            uic.connectionPointLocChanged(this, offset);
         }
//         System.out.println("Changing location: " + offset.toString());
      }
   }


   /**
    * Should be called when a slave figure changes size. The message will
    * be relayed to the master figure. By default, no message is relayed.
    * Derived classes can propagate the message if needed.
   **/
   public void boundsChanged()
   {
      // no op
   }

   /**
    * Adds ID to the list of IDs that will be allowed to connect to this
    * connection point. If the ID is already in the list, it is not added
    * a second time.
    *
    * @param ID unique identifier for an object that will be allowed to attach
    * to this connection point
    */
   public void addAllowedId( int ID )
   {
      if (willAllowAttach( ID, null ))
         return;
      m_allowedIDs.add(new Integer(ID));
   }

   /**
    * Removes ID from the list of IDs that can attach to this connection point.
    * If the ID is not found in the list, nothing is done.
    *
    * @param ID unique identifier, previously set with addAllowedId(), that is
    * removed from the list
    */
   public void removeAllowedId( int ID )
   {
      int index = getIndexOfID(ID);
      if (index > 0)
         remove(index);
   }

   public int getAttachedFigureCount()
   {
      return m_attachedFigures.size();
   }

   /**
    * @throws IndexOutOfBounds if index is less than 0 or greater than or equal
    * <code>getAttachedFigureCount</code>
    */
   public UIConnectableFigure getAttached( int index )
   {
      return (UIConnectableFigure) m_attachedFigures.get( index );
   }

   /**
    * Returns the first connection point in the list, or null if no attachments
    * are present.
    */
   public UIConnectableFigure getAttached( )
   {
      if ( m_attachedFigures.size() > 0 )
         return (UIConnectableFigure) m_attachedFigures.get( 0 );
      else
         return null;
   }

   /**
    * Position flags. Used by [get,set]PreferredAttachPosition(). Can be
    * combined together to make up to 9 standard positions.
    */
   public static final int POS_LEFT   = 1<<0;
   public static final int POS_TOP    = 1<<1;
   public static final int POS_RIGHT  = 1<<2;
   public static final int POS_BOTTOM = 1<<3;
   /* Shortcut for center */
   public static final int POS_CENTER = POS_LEFT | POS_TOP | POS_RIGHT | POS_BOTTOM;

   /**
    * Returns a flag indicating where this connector's 'hot-spot' attachment 
    * is. The attaching figure should position itself on its owner so it makes
    * the 'best looking' attachment. For example, if this connector was on the 
    * right end of the object, it would want the attaching figure to attach to
    * its right side, so this method would return POS_RIGHT. The attaching figure
    * should position the connection on the left/center side of itself.
    * <p>
    * The default value is centered (POS_LEFT | POS_TOP | POS_RIGHT | POS_BOTTOM).
    */
   public int getPreferredAttachPosition( )
   {
      return m_preferredAttachPos;
   }

   /**
    * Sets the position that is most appropriate for attachment. Any flags that
    * aren't recognized are ignored. If no valid flags are passed, POS_CENTER
    * is set. If an invalid flag is supplied and debugging is enabled, an 
    * assertion is thrown. 
    *
    * @param Pos an or'd value of flags of the form POS_...
    *
    * @see #getPreferredAttachPosition
    */
   public void setPreferredAttachPosition( int Pos )
   {
      Debug.assertTrue(0 != ( Pos & POS_CENTER ), E2Designer.getResources(), 
            "InvalidPosFlags", null );
      if ( 0 == ( Pos & POS_CENTER ))
         Pos = POS_CENTER;
      m_preferredAttachPos = Pos;
   }


   // operations

   public void paint(Graphics g)
   {
      super.paint(g);
//      if (!isAttached())
//         m_image.paintIcon(this, g, 0, 0);
   }

   /**
    * If the connector is not locked, creates an attachment to the passed in
    * connection point.
    *
    * @returns true if successfully attached, false
    * if couldn't attach because the connection point is locked
    *
    * @param slave object that is connecting to this one.
    *
    * @throws IllegalArgumentException if slave is null
    */
   public abstract boolean attach( UIConnectableFigure slave );


   /**
    * Removes the unlocked connection that is attached to the supplied slave.
    * If the slave is not attached, nothing is done and <code>true</code> is
    * returned.
    *
    * @param bForce performs the detach even if the connector is locked
    *
    * @returns <code>true</code> if the detach was completed, <code>false</code>
    * otherwise
    */
   public abstract boolean detach( UIConnectableFigure slave, boolean bForce );
   
   /**
    * Removes all connection points if the connector is unlocked.
    *
    * @param bForce performs the detach even if the connector is locked
    *
    * @returns <code>true</code> if the detach was completed, <code>false</code>
    * otherwise
    */
   public boolean detachAll( boolean bForce )
   {
      if ( isLocked() && !bForce  )
         return false;

      // walk list and call detach for each one.
      int size = m_attachedFigures.size();
      boolean bSuccess = true;
      for ( int index = 0; index < size; index++ )
      {
         UIConnectableFigure uic = (UIConnectableFigure) m_attachedFigures.get( index );
         bSuccess &= detach( uic, true );
      }
      return bSuccess;
   } 

   // implementation
   /**
    * Returns the index of the ID in the vector. If the ID is not found, -1 is
    * returned. Index value starts at 0.
    */
   public int getIndexOfID(int ID)
   {
      int index = -1;
      int count = m_allowedIDs.size();
      int i=0; 
      if (count > 0)
      {
         for (; i < count; i++)
         {
            Integer element = (Integer)m_allowedIDs.get(i);
            if (ID == element.intValue())
            {
               break;   // terminate loop
            }
         }
      }
      if (i < count)
         index = i;
      return(index);
      
   }

   /**
    * Popup menu functions
    */
   public String getPopupName()
   {
      return m_strPopupName;
   }
   
   public void setPopupName(String name)
   {
      m_strPopupName = name;   
   }
   
   /**
   * Connection points can take hold of the mouse
   */
   public boolean wantsMouse(Point pt)
   {
      return(false);
   }

   public MouseListener getMouseListener( Point pt, JComponent drawingPane )
   {
      return(null);

   }

   public MouseMotionListener getMouseMotionListener( Point pt, JComponent drawingPane )
   {
      return null;
   }

   /**
   * The attachee constraints are the rules that allow a conection point
   * to be attached to
   */
   public List<IConnectionConstraint> getAttacheeConstraints()
   {
      return m_attacheeConstraints;
   }

   /**
   *  Add an attache constraint
   */
   public void addAttacheeConstraints(IConnectionConstraint constraint)
   {
      m_attacheeConstraints.add(constraint);
   }
   
   /**
   * The attacher constraints are the rules that allow a conection point
   * to attach to someone
   */
   public List<IConnectionConstraint> getAttacherConstraints()
   {
      return m_attacherConstraints;
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

   /**
    * Used by constructors to indicate that the dimension they pass in for the offset
    * comes from them rather than a user when they delegate to the main 
    * constructor. Any value that a user wouldn't pass in is ok.
    */ 
   protected static final int INVALID_WIDTH = -99999999;

   // storage
   /**
    * Derived classes should store all references to attached figures
    * in this variable. When a figure is detached, it should be removed.
    */
   protected Vector<UIConnectableFigure> m_attachedFigures =
      new Vector<UIConnectableFigure>(5);
   
   /**
    * Each connector has 2 points where attachments are considered to exist.
     * The point where this connector attaches to the owner and the point where
     * it attaches to the slave. The next 2 variables indicate where each
     * 'hot spot' is, relative to the ul corner of the connector. By default,
     * the owner attach location is calculated based on the preferred attach
    * position, and the slave attach location is the same as the owner's
     * (indicated by a null slave attach location). Base classes can override
     * this behavior.
    */
   protected Dimension m_ownerAttachOffset = new Dimension();
   protected Dimension m_slaveAttachOffset = null;

   private boolean m_bLocked = false;
   private int m_iSlop = 0;
   private UIConnectableFigure m_owner = null;
   private int m_preferredAttachPos = POS_CENTER;

   /**
    * The allowed IDs are kept in this vector. The initial size was arbitrarily
    * chosen to handle most connectable objects.
    */
   private Vector<Integer> m_allowedIDs = new Vector<Integer>(2);

   /**
    * This ID is used by the FigureFactories and set while creating a connection
   * point. This will never change during the life time of this object.
   * Its used to provide a unique ID over all connection points within the
   * figure it is created.
    */ 
   private int m_cpId = 0;
   
   /**
    * This string is used as the name of the menu item for the properties.
    */
   String m_strPopupName = new String("Point Properties");
   

   // connection point ID's: DO NOT change this definitions when adding new ID's.
  // otherwise we might loose the backwards compatibility
  public static final int CP_ID_PAGE_TANK = 0;
  public static final int CP_ID_BACKEND_TANK = 1;
  public static final int CP_ID_MAPPER = 2;
  /////////////// changed for transplant /////////////////////////////
  public static final int CP_ID_JAVA_EXIT = 3;
  /////////////// changed for transplant /////////////////////////////
  public static final int CP_ID_SELECTOR = 4;
  public static final int CP_ID_SYNCHRONIZER = 5;
  public static final int CP_ID_ENCRYPTOR = 6;
  public static final int CP_ID_RESULT_PAGER = 7;
  public static final int CP_ID_TRANSACTION_MGR = 8;
  public static final int CP_ID_LEFT = 9;
  public static final int CP_ID_RIGHT = 10;
  public static final int CP_ID_BOTTOM = 11;
  public static final int CP_ID_RIGHT_1 = 12;
  protected List<IConnectionConstraint> m_attacheeConstraints;
  protected List<IConnectionConstraint> m_attacherConstraints;
}


