/******************************************************************************
 *
 * [ PSContentLoader.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.loader;

import com.percussion.cms.objectstore.client.PSRemoteException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSException;
import com.percussion.loader.objectstore.PSConnectionDef;
import com.percussion.loader.objectstore.PSExtractorDef;
import com.percussion.loader.objectstore.PSLoaderDef;
import com.percussion.loader.objectstore.PSStaticItemExtractorDef;
import com.percussion.loader.ui.IPSUIPlugin;
import com.percussion.loader.ui.PSConfigPanel;
import com.percussion.loader.ui.PSLoaderEditorPanel;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSObjectStoreUtil;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * See base interface {@link IPSContentLoader}for a more generic description.
 * Plugins implementing this interface fit into the migration/loader model as
 * the loader of content. Each plugin is responsible for uploading content to
 * the configured target system.
 */
public class PSContentLoader implements IPSContentLoader, IPSUIPlugin
{
   /*
    * @see {@link IPSContentLoader#loadContentItem(PSItemContext item)} for
    *      detail.
    */
   public void loadContentItem(PSItemContext itemContext)
      throws PSLoaderException
   {
      // set community for the connection info from the item if needed
      PSExtractorDef extractorDef = itemContext.getExtractorDef();
      String communityID =  extractorDef.getFieldValue(
         IPSHtmlParameters.SYS_COMMUNITYID);
      if (communityID != null && communityID.trim().length() != 0)
         m_remoteAgent.setCommunity(communityID);

      Document itemDoc = itemContext.getStandardItemDoc();

      // assume the item has already been checked out
     PSLocator locator = null;
      try
      {
         locator = m_remoteAgent.updateItem(
            itemDoc.getDocumentElement(), true);
      }
      catch(PSRemoteException re)
      {
         throw new PSLoaderException(re);
      }

      itemContext.setLocator(locator);
   }

   /**
    * @see {@link IPSContentLoader#loadStaticItem(PSItemContext item)} for
    *      detail.
    */
   public void loadStaticItem(PSItemContext item) throws PSLoaderException
   {
      ByteArrayInputStream in = null;
      PSObjectStoreUtil objStoreUtil = null;

      try
      {
         PSExtractorDef def = item.getExtractorDef();
         PSStaticItemExtractorDef staticDef;
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         staticDef = new PSStaticItemExtractorDef(def.toXml(doc));

         byte[] data = item.getStaticData();
         in = new ByteArrayInputStream(data);

         Properties propsLogin = new Properties( );

         propsLogin.setProperty(
               PSObjectStoreUtil.PROPERTY_HOST, m_connection.getServerName());
         propsLogin.setProperty(
               PSObjectStoreUtil.PROPERTY_LOGIN_ID, m_connection.getUser());
         propsLogin.setProperty(
               PSObjectStoreUtil.PROPERTY_LOGIN_PW,
               m_connection.getPassword());
         propsLogin.setProperty(
               PSObjectStoreUtil.PROPERTY_PORT, m_connection.getPort());

         objStoreUtil = new PSObjectStoreUtil(propsLogin);

         // get the application name, the root of targetLoc
         String targetLoc = staticDef.getTargetLocation().replace('\\', '/');
         String appName = targetLoc;
         String relativeToApp = "";
         int i = appName.indexOf('/');
         if ( i > 0 )
         {
            appName = appName.substring(0, i);
            relativeToApp = targetLoc.substring(i + 1);
         }

         // get the file path that is relative to the source location
         String srcLoc = staticDef.getSourceLocation().replace('\\', '/');
         String srcRoot = item.getRootResourceId() + "/" + srcLoc;
         String relativeToSrc = null;
         if (item.getResourceId().startsWith(srcRoot) &&
             item.getResourceId().length() > srcRoot.length())
         {
            relativeToSrc = item.getResourceId().substring(srcRoot.length()+1);
         }
         else
         {  // this may not possible, but just in case it does
            throw new RuntimeException("Fail to get relativeToSrc path");
         }

         // get the file path that is relative to the application root
         String filePath = null;
         if (relativeToApp.length() > 0)
            filePath = relativeToApp + "/" + relativeToSrc;
         else
            filePath = relativeToSrc;

         // send the data to Rhythmyx at: 'appName'/'filePath'
         objStoreUtil.saveApplicationFile(
            appName, in, new File(filePath), true, true);
      }
      catch (PSException pe)
      {
         throw new PSLoaderException(pe);
      }
      catch (Exception e)
      {
         throw new PSLoaderException(
            IPSLoaderErrors.UNEXPECTED_ERROR, e.toString());
      }
      finally
      {
         if (in != null)
         {
            try { in.close(); } catch (Exception e){};
         }
         if (objStoreUtil != null)
         {
            try { objStoreUtil.close(); } catch (Exception e){};
         }
      }
   }


   /**
    * @see {@link IPSPlugin#configure(Element)} for detail.
    */
   public void configure(Element config) throws PSConfigurationException
   {
      try
      {
         m_loaderDef = new PSLoaderDef(config);
      }
      catch (PSLoaderException e)
      {
         PSConfigurationException configEx = new PSConfigurationException(e);
         throw configEx;
      }
      catch (PSException e)
      {
         PSConfigurationException configEx = new PSConfigurationException(e);
         throw configEx;
      }
   }

   /**
    * Implements IPSUIPlugin interface.
    */
   public PSConfigPanel getConfigurationUI()
   {
      return new PSLoaderEditorPanel();
   }

   /**
    * Set connection definition.
    *
    * @param conn The to be set connection definition. It may not be
    *    <code>null</code>.
    *
    * @throws PSLoaderException if an error occurs
    */
   public void setConnectionDef(PSConnectionDef conn) throws PSLoaderException
   {
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");

      m_connection = conn;

      m_remoteAgent = new PSLoaderRemoteAgent(conn);
   }

   /**
    * The loader definition, set by <code>configure</code> only, never
    * <code>null</code> after that.
    */
   private PSLoaderDef m_loaderDef = null;

   /**
    * Initialized by <code>setConnectionDef()</code>, never <code>null</code>
    * after that.
    */
   private PSLoaderRemoteAgent m_remoteAgent = null;

   /**
    * The connection definition. It is <code>null</code> until set by
    * <code>setConnectionDef()</code>.
    */
   private PSConnectionDef m_connection = null;
}
