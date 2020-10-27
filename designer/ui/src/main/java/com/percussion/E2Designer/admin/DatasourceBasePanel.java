/******************************************************************************
 *
 * [ PSDatasourceBasePanel.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.design.objectstore.PSJdbcDriverConfig;
import com.percussion.services.datasource.PSHibernateDialectConfig;
import com.percussion.utils.container.IPSJndiDatasource;
import com.percussion.utils.jdbc.PSDatasourceResolver;

import java.awt.*;
import java.util.List;

/**
 * Base class for datasource configuration panels.
 */
public abstract class DatasourceBasePanel extends TableEditorBasePanel
   implements ITabDataHelper
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   /**
    * Construct the panel.
    * 
    * @param parent The parent frame, may be <code>null</code>.
    * @param config The server config, used to get and set driver configs, may
    * not be <code>null</code>.
    * @param dialects The hibernate dialect mappings, may not be 
    * <code>null</code>.
    * @param datasources The JNDI datasources, may not be <code>null</code>.
    * @param resolver The datasource resolver, may not be <code>null</code>.
    */
   public DatasourceBasePanel(Frame parent, ServerConfiguration config, 
      PSHibernateDialectConfig dialects, List<IPSJndiDatasource> datasources, 
      PSDatasourceResolver resolver)
   {
      if (config == null)
         throw new IllegalArgumentException("config may not be null");
      if (dialects == null)
         throw new IllegalArgumentException("dialects may not be null");      
      if (datasources == null)
         throw new IllegalArgumentException("datasources may not be null");
      if (resolver == null)
         throw new IllegalArgumentException("resolver may not be null");
      
      m_parent = parent;
      m_hibernateDialects = dialects;
      m_driverConfigs = config.getServerConfiguration().getJdbcDriverConfigs();
      m_resolver = resolver;
      m_datasources = datasources;
      
      initPanel();
   }

   /* (non-Javadoc)
    * @see com.percussion.E2Designer.admin.ITabDataHelper#saveTabData()
    */
   public boolean saveTabData()
   {
      if (isModified())
      {
         AppletMainDialog.setRestartRequired();
         setModified(false);
      }
      
      return true;
   }

   /* (non-Javadoc)
    * @see com.percussion.E2Designer.admin.TableEditorBasePanel#getKeyPrefix()
    */
   @Override
   protected String getKeyPrefix()
   {
      return "datasources.";
   }

   /**
    * The server config supplied during ctor, never <code>null</code>
    * after that.
    */
   protected ServerConfiguration m_serverConfig;
   
   /**
    * The hibernate dialects supplied during ctor, never <code>null</code>
    * after that.
    */
   protected PSHibernateDialectConfig m_hibernateDialects;
   
   /**
    * The driver configs supplied during ctor, never <code>null</code>
    * after that.
    */
   protected List<PSJdbcDriverConfig> m_driverConfigs;
   
   /**
    * List of JNDI datasources supplied during ctor, never <code>null</code>
    * after that.
    */
   protected List<IPSJndiDatasource> m_datasources;
   
   /**
    * Resolver supplied during ctor, never <code>null</code> after that.
    */
   protected PSDatasourceResolver m_resolver;
   
   /**
    * The parent frame supplied during ctor, never <code>null</code>
    * after that.
    */
   protected Frame m_parent;
   
}

