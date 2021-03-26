/*[ OSNotifier.java ]**********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSNotifier;
import com.percussion.error.PSIllegalArgumentException;

import java.text.MessageFormat;
import java.util.Properties;

public class OSNotifier extends PSNotifier 
      implements IGuiLink, IPersist
{
   /**
    * @throws PSIllegalArgumentException should never be thrown by this 
    * constructor
    */
   public OSNotifier()
         throws PSIllegalArgumentException
   {
      super( MP_TYPE_SMTP, E2Designer.getResources().getString( "Dummy" ));
   }

   /*
    * @param notifier a valid OSNotifier. If null, a NullPointerException is
    * thrown.
    *
    * @throws NullPointerException if notifier is null
    *
    * @throws PSIllegalArgumentException this should never be thrown 
    */
   public OSNotifier( PSNotifier notifier )
         throws PSIllegalArgumentException
   {
      super( notifier.getProviderType(), notifier.getServer());
      copyFrom( notifier );
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first. <p>
    * The owner is not copied by this method. If the caller wants to change
    * owners, they should call <code>setFigure( src.getFigure())</code> after
    * this method completes.
    *
    * @param notifier a valid OSRequester. If null, an PSIllegalArgumentException is
    * thrown.
    *
    * @throws PSIllegalArgumentException if notifer is null
    */
   public void copyFrom( OSNotifier notifier )
         throws PSIllegalArgumentException
   {
      copyFrom((PSNotifier) notifier );
      // assume object is valid
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

      if (!(store instanceof PSApplication))
      {
         Object[] astrParams =
         {
            "PSApplication"
         };
         throw new IllegalArgumentException(MessageFormat.format(
            E2Designer.getResources().getString("IncorrectType"), astrParams));
      }

    try
    {
      PSApplication application = (PSApplication) store;
      if(application.getNotifier() != null)
           this.copyFrom(application.getNotifier());

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

      if (!(store instanceof PSApplication))
      {
         Object[] astrParams =
         {
            "PSApplication"
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
         PSApplication application = (PSApplication) store;
      application.setNotifier((PSNotifier) this);

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
