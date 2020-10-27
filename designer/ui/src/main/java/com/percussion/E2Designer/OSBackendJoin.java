/*[ OSBackendJoin.java ]*******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSBackEndJoin;

import java.text.MessageFormat;
import java.util.Properties;

public class OSBackendJoin extends PSBackEndJoin
      implements IGuiLink, IPersist
{

   /*
    *
    */
   public OSBackendJoin( )
   {
      super(null,null);
   }

   /*
    * Creates a new table, taking its properties from the supplied table.
    *
    * @throws NullPointerException if table is null
    */
   public OSBackendJoin( PSBackEndJoin join )
   {
      super( join.getLeftColumn(), join.getRightColumn());
      copyFrom(join );
   }


   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first. <p>
    * The owner is not copied by this method. If the caller wants to change
    * owners, they should call <code>setFigure( src.getFigure())</code> after
    * this method completes.
    *
    * @param table a valid OSBackendTable. 
    */
   public void copyFrom( OSBackendJoin join )
   {
      copyFrom((PSBackEndJoin) join );
   }

   public OSBackendDatatank getBackendDatatank()
   {
      return m_backendDatatank;
   }


   /*************
      IGuiLink interface implementation
   *************/
   public void setFigure( UIFigure fig )
   {
      m_owner = fig;
   }

   public void release()
   {
      m_owner = null;
   }

   public UIFigure getFigure()
   {
      return m_owner;
   }

   //////////////////////////////////////////////////////////////////////////////
  // IPersist interface implementation
   public boolean load(PSApplication app, Object store, Properties config)
   {
      if (null == store || null == config)
         throw new IllegalArgumentException();

      if (!(store instanceof OSBackendDatatank))
      {
         String [] astrParams =
         {
            "OSBackendDatatank"
         };
         throw new IllegalArgumentException(MessageFormat.format(
            E2Designer.getResources().getString("IncorrectType"), astrParams));
      }

    try
    {
         OSBackendDatatank tank = (OSBackendDatatank) store;
      m_backendDatatank = tank;
      System.out.println("...load join");

      return true;
    }
    catch (Exception e)
    {
       e.printStackTrace();
    }

      return false;
   }

   /**
    * Interface method unimplemented.
    * @see com.percussion.E2Designer.IPersist#cleanup(com.percussion.
    * E2Designer.OSApplication)
    */
   public void cleanup(OSApplication app)
   {
   }

   //////////////////////////////////////////////////////////////////////////////
  // IPersist interface implementation
   public void save(PSApplication app, Object store, Properties config)
   {
      if (null == store || null == config)
         throw new IllegalArgumentException();

      if (!(store instanceof OSBackendDatatank))
      {
         String [] astrParams =
         {
            "OSBackendDatatank"
         };
         throw new IllegalArgumentException(MessageFormat.format(
            E2Designer.getResources().getString("IncorrectType"), astrParams));
      }

      try
    {
         // store current id temporary and create a new unique id for this object
         int currentId = this.getId();
         this.setId(Util.getUniqueId());

         // save this backendtank into the provided pipe
         OSBackendDatatank tank = (OSBackendDatatank) store;
      System.out.println("...save join");

      // save GUI information
      OSLoadSaveHelper.saveOwner(currentId, this.getId(), config, m_owner);
    }
    catch (Exception e)
    {
       e.printStackTrace();
    }
   }

   // private storage
   private UIFigure m_owner = null;
  private OSBackendDatatank m_backendDatatank = null;
}


