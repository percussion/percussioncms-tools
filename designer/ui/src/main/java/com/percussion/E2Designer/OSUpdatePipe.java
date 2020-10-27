/*[ OSUpdatePipe.java ]********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSBackEndDataTank;
import com.percussion.design.objectstore.PSDataMapper;
import com.percussion.design.objectstore.PSDataSelector;
import com.percussion.design.objectstore.PSUpdatePipe;

import java.util.Properties;

/**
 * A wrapper around the PSUpdatePipe objects that provides all data and its access for
 * the UIQueryPipe objects.
 */
////////////////////////////////////////////////////////////////////////////////
public class OSUpdatePipe extends PSUpdatePipe implements IOSPipe
{
   /**
    * Default constructor.
    */
   //////////////////////////////////////////////////////////////////////////////
   public OSUpdatePipe()
   {
      super( DEFAULT_PIPE_NAME );
   }

   /**
    * Construct a copy from provided source.
    *
    * @param source the source object
    */
  //////////////////////////////////////////////////////////////////////////////
   public OSUpdatePipe(PSUpdatePipe source)
   {
      super( DEFAULT_PIPE_NAME );
      if (null == source)
         throw new IllegalArgumentException();

      copyFrom(source);
   }

   /**
    * Construct a copy from provided source.
    *
    * @param source the source object
    */
  //////////////////////////////////////////////////////////////////////////////
   public OSUpdatePipe(OSUpdatePipe source)
   {
      super( DEFAULT_PIPE_NAME );
      if (null == source)
         throw new IllegalArgumentException();

      copyFrom(source);
   }

   /**
    * Copy the provided source into this. <p>
    * The owner is not copied by this method. If the caller wants to change
    * owners, they should call <code>setFigure( src.getFigure())</code> after
    * this method completes.
    *
    * @param source the source object
    */
   //////////////////////////////////////////////////////////////////////////////
   public void copyFrom(OSUpdatePipe source)
   {
      if (null == source)
         throw new IllegalArgumentException();

      copyFrom((PSUpdatePipe) source);

      // copy other members
    m_loadedAtLeastOnce = source.m_loadedAtLeastOnce;
   }

  /**
   * Implementation of IOSPipe; No-op.
   * @see IOSPipe
   */
  public PSDataSelector getDataSelector()
  {
    return null;
  }

  /**
   * Implementation of IOSPipe; No-op.
   * @see IOSPipe
   */
  public void setDataSelector( PSDataSelector ds )
  {
    throw new IllegalArgumentException("DataSelector cannot be set from UpdatePipe");
  }

  /**
   * Implementation of IOSPipe and overrides method from PSPipe.
   * @see IOSPipe
   */
   public PSBackEndDataTank getBackEndDataTank()
   {
      PSBackEndDataTank tank = super.getBackEndDataTank();
      try
      {
         if ( null != tank && !(tank instanceof OSBackendDatatank))
         {
            tank = new OSBackendDatatank(tank);
            setBackEndDataTank(tank);
         }
      }
      catch ( IllegalArgumentException e )
      {
         // this should never happen in this context
         e.printStackTrace();
      }
      return tank;
   }

  /**
   * Implementation of IOSPipe.
   * @see IOSPipe
   */
  public void setBackEndDataTank( PSBackEndDataTank tank )
  {
    try {
      super.setBackEndDataTank( tank );
    }
    catch ( IllegalArgumentException e )
    {
      throw new IllegalArgumentException( e.getLocalizedMessage() );
    }
  }


  /**
   * Implementation of IOSPipe interface and overrides method from PSPipe.
   * @see IOSPipe
   */
  public PSDataMapper getDataMapper()
  {
    PSDataMapper mapper = super.getDataMapper();
    try
    {
      if ( null != mapper && !(mapper instanceof OSDataMapper) )
      {
        mapper = new OSDataMapper(mapper);
        setDataMapper(mapper);
      }
    }
    catch ( IllegalArgumentException e )
    {
      // this should never happen in this context
      e.printStackTrace();
    }
    return mapper;
  }

  /**
   * Implementation of IOSPipe interface.
   * @see IOSPipe
   */
  public void setDataMapper( PSDataMapper mapper )
  {
    try {
      super.setDataMapper( mapper );
    }
    catch (IllegalArgumentException e)
    {
      throw new IllegalArgumentException( e.getLocalizedMessage() );
    }
  }

   /**
    * @returns a string that represents the pipe type
   * @see IOSPipe
    */
   //////////////////////////////////////////////////////////////////////////////
   public String getType( )
   {
      return E2Designer.getResources().getString( DEFAULT_PIPE_NAME );
   }

  /**
   * Implementation of IOSPipe interface.
   * @see IOSPipe
   */
   public String getDatasetName()
   {
      return m_strDatasetName;
   }

  /**
   * Implementation of IOSPipe interface.
   * @see IOSPipe
   */
   public void setDatasetName(String strName)
   {
      m_strDatasetName = strName;
   }

  /**
   * Implementation of IOSPipe interface.
   * @see IOSPipe
   */
   public String getDatasetDescription()
   {
      return m_strDatasetDescr;
   }

  /**
   * Implementation of IOSPipe interface.
   * @see IOSPipe
   */
   public void setDatasetDescription(String strDescr)
   {
      m_strDatasetDescr = strDescr;
   }

  /**
   * Implementation of IOSPipe interface.
   * @see IOSPipe
   */
  public OSDataset getDataset()
  {
    return m_dataset;
  }


   /**
    * Indicate we loaded the editor at least once.
    */
   //////////////////////////////////////////////////////////////////////////////
   public void loadedAtLeastOnce()
   {
      m_loadedAtLeastOnce = true;
   }

   /**
    * Have we loaded this pipe at least once?
    */
   //////////////////////////////////////////////////////////////////////////////
   public boolean isLoadedAtLeastOnce()
   {
      return m_loadedAtLeastOnce;
   }


   //////////////////////////////////////////////////////////////////////////////
   // IGuiLink interface implementation
  /** @see IGuiLink */
   public void setFigure(UIFigure fig)
   {
      m_owner = fig;
   }

  /** @see IGuiLink */
   public void release()
   {
      m_owner = null;
   }

  /** @see IGuiLink */
   public UIFigure getFigure()
   {
      return m_owner;
   }

   //////////////////////////////////////////////////////////////////////////////
  // IPersist interface implementation
  /** @see IPersist */
   public boolean load(PSApplication app, Object store, Properties config)
   {
    if (store instanceof OSDataset)
    {
      m_dataset = (OSDataset)store;
    }

     OSPipeHelper os = new OSPipeHelper();
    return os.load(this, store, config, m_owner);
   }

   /**
    * Interface method unimplemented.
    * @see com.percussion.E2Designer.IPersist#cleanup(com.percussion.
    * E2Designer.OSApplication)
    */
   public void cleanup(OSApplication app)
   {
   }

   /** @see IPersist */
   public void save(PSApplication app, Object store, Properties config)
   {
     OSPipeHelper os = new OSPipeHelper();
    os.save(this, store, config, m_owner);
   }


   //////////////////////////////////////////////////////////////////////////////
   // for IGuiLink
   private UIFigure m_owner = null;
  private boolean m_loadedAtLeastOnce = false;

   private static String DEFAULT_PIPE_NAME = "UpdatePipe";

   // for Dataset properties
   private String m_strDatasetName = null;
   private String m_strDatasetDescr = null;

  private OSDataset m_dataset = null;
}


