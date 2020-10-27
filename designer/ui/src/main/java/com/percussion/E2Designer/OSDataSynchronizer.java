/*[ OSDataSynchronizer.java ]**************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSDataSynchronizer;
import com.percussion.design.objectstore.PSUpdatePipe;
import com.percussion.error.PSIllegalArgumentException;

import java.text.MessageFormat;
import java.util.Properties;

public class OSDataSynchronizer extends PSDataSynchronizer
      implements IGuiLink, IPersist
{
   /**
    * Overrides the default behavior of the base class to allow Inserting.
    */
   public OSDataSynchronizer()
   {
      setInsertingAllowed( true );
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.<p>
    * The owner is not copied by this method. If the caller wants to change
    * owners, they should call <code>setFigure( src.getFigure())</code> after
    * this method completes.
    *
    * @param sync a valid OSDataSynchronizer. If null, a PSIllegalArgumentException is
    * thrown.
    *
    * @throws PSIllegalArgumentException if sync is null
    */
   public void copyFrom( OSDataSynchronizer sync )
         throws PSIllegalArgumentException
   {
      copyFrom((PSDataSynchronizer) sync );
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
   // implementation for IPersist
   public boolean load(PSApplication app, Object store, Properties config)
   {
      if (null == store || null == config)
         throw new IllegalArgumentException();

      if (!(store instanceof PSUpdatePipe))
      {
         String [] astrParams =
         {
            "PSUpdatePipe"
         };
         throw new IllegalArgumentException(MessageFormat.format(
            E2Designer.getResources().getString("IncorrectType"), astrParams));
      }

      try
      {
         PSUpdatePipe pipe = (PSUpdatePipe) store;
         this.copyFrom(pipe.getDataSynchronizer());

         // restore GUI information
         OSLoadSaveHelper.loadOwner(this.getId(), config, m_owner);

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
   // implementation for IPersist
   public void save(PSApplication app, Object store, Properties config)
   {
      if (null == store || null == config)
         throw new IllegalArgumentException();

      if (!(store instanceof PSUpdatePipe))
      {
         String [] astrParams =
         {
            "PSUpdatePipe"
         };
         throw new IllegalArgumentException(MessageFormat.format(
            E2Designer.getResources().getString("IncorrectType"), astrParams));
      }

      System.out.println("...save data synchronizer: parameters are o.k.");
      try
      {
         // store current id temporary and create a new unique id for this object
         int currentId = this.getId();
         this.setId(Util.getUniqueId());

         // save this backendtank into the provided pipe
         PSUpdatePipe pipe = (PSUpdatePipe) store;
         pipe.setDataSynchronizer(this);

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
}

