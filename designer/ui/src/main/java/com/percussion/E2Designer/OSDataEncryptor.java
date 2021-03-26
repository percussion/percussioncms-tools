/******************************************************************************
 *
 * [ OSDataEncryptor.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSDataEncryptor;

import java.text.MessageFormat;
import java.util.Properties;

/**
 * A wrapper around the PSDataEncryptor objects that provides all data and its
 * access.
 */
////////////////////////////////////////////////////////////////////////////////
public class OSDataEncryptor extends PSDataEncryptor implements IGuiLink, IPersist
{
  /**
   * Construct an empty encryptor.
   */
   //////////////////////////////////////////////////////////////////////////////
   public OSDataEncryptor()
   {
      // assume the user wants to add security if she is adding this object
      super( true );
   }

  /**
   * Construct the encryptor from a PSDataEncryptor. Copies all properties from
   * the supplied encryptor to this object.
   *
   * @throws NullPointerException if encryptor is null
   *
   */
   //////////////////////////////////////////////////////////////////////////////
   public OSDataEncryptor(PSDataEncryptor encryptor)
   {
      super( encryptor.isSSLRequired() );
      copyFrom( encryptor );
   }

  /**
   * Return status wether or not the default settings are overridden.
   *
   * @return boolean   the status
   */
   //////////////////////////////////////////////////////////////////////////////
   public boolean isDefaultOverridden()
   {
     return m_override;
   }

   /**
    * Instead of the presence of this object acting as the override indicator,
    * we add a flag to do this. The default is <code>true</code> (the thinking
    * being that if the user places the object, he wants to override the app
    * settings).
    *
    * @param bOverride if <code>true</code>, the security settings of the app
    * are overridden, otherwise any settings in this object are ignored.
    */
   public void setDefaultOverridden( boolean bOverride )
   {
      m_override = bOverride;
   }

   public void save( boolean bOverride, boolean bSSLRequired, int keyLength )
   {
      setKeyStrength( keyLength );
      setSSLRequired( bSSLRequired );
      setDefaultOverridden( bOverride );
   }

   /**
    * A convenience method for those who don't like key strength.
    */
   public int getKeyLength()
   {
      return getKeyStrength();
   }

   public void setKeyLength( int keyLength )
   {
      setKeyStrength( keyLength );
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first. <p>
    * The owner is not copied by this method. If the caller wants to change
    * owners, they should call <code>setFigure( src.getFigure())</code> after
    * this method completes.
    *
    * @param encryptor a valid OSDataEncryptor. If null, an IllegalArgumentException is
    * thrown.
    */
   public void copyFrom( OSDataEncryptor encryptor )
   {
      copyFrom((PSDataEncryptor) encryptor );
      setDefaultOverridden( encryptor.isDefaultOverridden());
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

      if (!(store instanceof OSDataset))
      {
         Object [] astrParams =
         {
            "OSDataset"
         };
         throw new IllegalArgumentException(MessageFormat.format(
            E2Designer.getResources().getString("IncorrectType"), astrParams));
      }

    try
    {
       // load data
         OSDataset dataset = (OSDataset) store;
      this.copyFrom(dataset.getDataEncryptor());

      // restore extra GUI data
         String strId = new Integer(this.getId()).toString();
         m_override = false;
         if (config.getProperty(KEY_OVERRIDE + strId) != null)
            m_override = Boolean.parseBoolean(config.getProperty(KEY_OVERRIDE + strId));

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
  // IPersist interface implementation
   public void save(PSApplication app, Object store, Properties config)
   {
      if ( null == store || null == config )
         throw new IllegalArgumentException();

      if (!(store instanceof OSDataset))
      {
         Object [] astrParams =
         {
            "OSDataset"
         };
         throw new IllegalArgumentException(MessageFormat.format(
            E2Designer.getResources().getString("IncorrectType"), astrParams));
      }

    System.out.println("...save encryptor: parameters are o.k.");
      try
    {
         // store current id temporary and create a new unique id for this object
         int currentId = this.getId();
         this.setId(Util.getUniqueId());

         // save this encryptor into the provided dataset
         OSDataset dataset = (OSDataset) store;
      dataset.setDataEncryptor((PSDataEncryptor) this);

      // then store all keys with the new ID created.
         String strId = new Integer(this.getId()).toString();
         config.setProperty(KEY_OVERRIDE + strId, Boolean.toString(m_override));

      // save GUI information
      OSLoadSaveHelper.saveOwner(currentId, this.getId(), config, m_owner);
    }
    catch (Exception e)
    {
       e.printStackTrace();
    }
   }

   //////////////////////////////////////////////////////////////////////////////
   private UIFigure m_owner = null;
  private boolean m_override = true;
  /*
   * the user configuration property keys. the unique ID will be added
   */
   private static final String KEY_OVERRIDE = new String("override");
}

