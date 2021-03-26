/******************************************************************************
 *
 * [ UIRigidConnectionPoint.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import java.awt.*;
import java.text.MessageFormat;

/**
 * This connection point controls where the attached object is painted. If the
 * owner of this cp moves, the attached object moves with it. It limits
 * the number of simultaneous connections to 1.
 */
class UIRigidConnectionPoint extends UIConnectionPoint
{

   // constructors - all constructors are mirrors of the base class
   public UIRigidConnectionPoint(String strImageResource, UIConnectableFigure Owner,
            Point location, int id)
   {
      super( strImageResource, Owner, location, id);
   }

   public UIRigidConnectionPoint(String strImageResource, UIConnectableFigure Owner,
            Point location, int preferredAttachPos, int id)
   {
      super( strImageResource, Owner, location, preferredAttachPos, id);
   }

   public UIRigidConnectionPoint(String strImageResource, UIConnectableFigure Owner,
            Point location, Dimension offset, int preferredAttachPos, int id)
   {
      super(strImageResource, Owner, location, offset, preferredAttachPos, id);
   }


   /**
    * Creates the 'attachment' by adding the slave to the container that owns
    * this connector, at the position specified by the preferred attach position
    * of the connector.   If a connector is already present, it is detached and 
    * removed first.
    */
   @Override
   public boolean attach(UIConnectableFigure slave)
   {
      if (null == slave)
      {
         final Object[] astrParams = 
         {
            "UIConnectableFigure"
         };
         throw new IllegalArgumentException( MessageFormat.format( 
               E2Designer.getResources().getString("CantBeNull"), astrParams));
      }
      boolean bAllow = !isLocked();

      if (bAllow)
      {
       OSExitCallSet origSet=null;
       OSExitCallSet addSet=null;

         // remove the attached figure first
         if ( m_attachedFigures.size() > 0 )
         {
          UIConnectableFigure uic = m_attachedFigures.get(0);
        if( getOwner() instanceof ResizableObject )
        {
           origSet = (OSExitCallSet) uic.getData();
        }
         m_attachedFigures.remove( uic );
            uic.removeDynamicConnection( this );
         }
         m_attachedFigures.add( slave );

         Point location = new Point( getLocation());
            Dimension slaveAttachOffset = getSlaveAttachPointOffset();
         location.translate( slaveAttachOffset.width, slaveAttachOffset.height );
         Dimension slaveOffset = slave.getAttachOffset();
         location.translate( -slaveOffset.width, -slaveOffset.height );
         slave.setLocation( location );
         getOwner().add( slave, 0 );
         getOwner().repaint( slave.getBounds());
      // if is an resizable object then notify
      if( getOwner() instanceof ResizableObject )
      {
         if(origSet != null )
         {
            UIConnectableFigure uic = m_attachedFigures.get(0);
            addSet = (OSExitCallSet) slave.getData();
            if (!origSet.equals(addSet))
            {
               origSet.append(addSet);
            }
            uic.setData(origSet);
          }
          ResizableObject obj=(ResizableObject)getOwner();
          obj.updateBitmap(); // notify the change
      }
       setVisible( false );
//         System.out.println( "slave added to owner container" );
      }

      return bAllow;
   }

   /**
    * A notification method that tells the connection pt to cleanup. 
    */
   @Override
   public boolean detach(UIConnectableFigure slave, boolean bForce)
   {
      boolean bDetach = !isLocked() || bForce;
      if (bDetach)
      {
         m_attachedFigures.remove( slave );
         setVisible(true);
      // if an resizableObject
      if( getOwner() instanceof ResizableObject )
      {

           ResizableObject obj=(ResizableObject)getOwner();
           obj.updateBitmap(); // notify it
      }
      }

      return bDetach;
   }

   /**
    * This method is overridden so that parents can resize themselves if children
    * change in size.
   **/
   @Override
   public void boundsChanged()
   {
      getOwner().boundsChanged ();
   }

   /**
    * Prepares this object for serialization.
    */
   @Override
   public void prepareSerialization()
   {
    // nothing to do
   }

   /**
    * Finish up after serialization.
    */
   @Override
   public void finishSerialization()
   {
    // nothing to do
   }

   // storage
}
