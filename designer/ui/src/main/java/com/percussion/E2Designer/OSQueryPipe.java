/*[ OSQueryPipe.java ]*********************************************************
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
import com.percussion.design.objectstore.PSQueryPipe;

import java.util.Properties;

/**
 * A wrapper around the PSQueryPipe objects that provides all data and its access for
 * the UIQueryPipe objects.
 */
////////////////////////////////////////////////////////////////////////////////
public class OSQueryPipe extends PSQueryPipe implements IOSPipe
{
   /**
    * Default constructor.
    */
   //////////////////////////////////////////////////////////////////////////////
   public OSQueryPipe()
   {
      super( DEFAULT_PIPE_NAME );
   }

   /**
    * Construct a copy from provided source.
    *
    * @param source the source object
    */
  //////////////////////////////////////////////////////////////////////////////
   public OSQueryPipe(PSQueryPipe source)
   {
      super( DEFAULT_PIPE_NAME );
      if (null == source)
         throw new IllegalArgumentException();

      copyFrom((PSQueryPipe) source);
      // copy other members
   }

   /**
    * Construct a copy from provided source.
    *
    * @param source the source object
    */
  //////////////////////////////////////////////////////////////////////////////
   public OSQueryPipe(OSQueryPipe source)
   {
      super( DEFAULT_PIPE_NAME );
      if (null == source)
         throw new IllegalArgumentException();

      copyFrom(source);
      // copy other members
   m_owner = source.m_owner;
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
   public void copyFrom(OSQueryPipe source)
   {
      if (null == source)
         throw new IllegalArgumentException();

      copyFrom((PSQueryPipe) source);

      // copy other members
    m_loadedAtLeastOnce = source.m_loadedAtLeastOnce;
   }

   /**
    * If the current object in the pipe is a PS... object, it is first converted
    * into an OS... object. If the conversion occurs, the new object is set back
    * into the pipe.
    *
    * @return a the current selector as an OSDataSelector object (ALWAYS).
   * @see IOSPipe
   **/
   public PSDataSelector getDataSelector()
   {
      PSDataSelector sel = super.getDataSelector();

      try
      {
         if ( null != sel && !( sel instanceof OSDataSelector ))
         {
            sel = new OSDataSelector( sel );
            setDataSelector( sel );
         }
      }
      catch ( IllegalArgumentException e )
      {
         // this should never happen in this context
         e.printStackTrace();
      }
      return sel;
   }

  //////////////////////////////////////////////////////////////////////////////
  /**
   * Implementation of IOSPipe.
   * @see IOSPipe
   */
  public void setDataSelector( PSDataSelector ds )
  {
    super.setDataSelector( ds );
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

  /** @see IOSPipe */
  public OSDataset getDataset()
  {
    return m_dataset;
  }

  /** @see IOSPipe */
  public String getDatasetName()
   {
      return m_strDatasetName;
   }

  /** @see IOSPipe */
   public void setDatasetName(String strName)
   {
      m_strDatasetName = strName;
   }

  /** @see IOSPipe */
   public String getDatasetDescription()
   {
      return m_strDatasetDescr;
   }

  /** @see IOSPipe */
   public void setDatasetDescription(String strDescr)
   {
      m_strDatasetDescr = strDescr;
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

   // for Dataset properties
   private String m_strDatasetName = null;
   private String m_strDatasetDescr = null;


   private static String DEFAULT_PIPE_NAME = "QueryPipe";
  private OSDataset m_dataset = null;
}


