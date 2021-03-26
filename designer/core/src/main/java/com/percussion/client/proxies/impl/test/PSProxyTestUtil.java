/******************************************************************************
 *
 * [ PSProxyTestUtil.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl.test;

import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.client.objectstore.PSUiTemplateSlot;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.i18n.PSLocale;
import com.percussion.services.filter.data.PSItemFilter;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.services.ui.data.PSHierarchyNode;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.serialization.PSObjectSerializer;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

public class PSProxyTestUtil
{

   private PSProxyTestUtil()
   {
      
   }
   
   
   /**
    * Loads the repository file and serializes it into its
    * object.
    * @param repositoryFile
    * @return
    * @throws PSProxyTestException
    */
   public static Object loadRepository(File repositoryFile) 
      throws PSProxyTestException
   {
      Object obj = null;
      try
      {
         PSXmlSerializationHelper.addType("hierarchy-node", PSHierarchyNode.class);
         PSXmlSerializationHelper.addType("assemblytemplate", PSUiAssemblyTemplate.class);
         PSXmlSerializationHelper.addType("templateslot", PSUiTemplateSlot.class);
         PSXmlSerializationHelper.addType("community", PSCommunity.class);
         PSXmlSerializationHelper.addType("locale", PSLocale.class);
         PSXmlSerializationHelper.addType("item-filter", PSItemFilter.class);
         
         Document doc = PSXmlDocumentBuilder.createXmlDocument(new FileInputStream(
            repositoryFile), false);
          obj = PSObjectSerializer.getInstance()
            .fromXml(doc.getDocumentElement());
          
      }
      catch (Exception e)
      {
         throw new PSProxyTestException(e);
      }
      return obj;
      
   }
   
   /**
    * Saves the repository map object to the file sytem in its
    * XML form.
    * @param obj
    * @param repositoryFile
    * @throws PSProxyTestException
    */
   public static void saveRepository(IPSRepositoryMap obj, File repositoryFile) 
      throws PSProxyTestException
   {
      FileWriter writer = null;
      try
      {
         writer = new FileWriter(repositoryFile);
         writer.write(
            PSObjectSerializer.getInstance().toXmlString(obj));
         writer.flush();
      }
      catch(Exception e)
      {
         throw new PSProxyTestException(e);
      }
      finally
      {
         if (writer != null)
         {
            try
            {
               writer.close();
               writer = null;
            }
            catch (IOException ignore){}
         }

      }
   }  
   
   /**
    * @param key cannot be <code>null</code>.
    * @return the <code>IPSGuid</code> contained in the 
    * <code>PSKey</code> or <code>null</code>.
    */
   public static IPSGuid getGuidFromKey(PSKey key)
   {
      if(key == null)
         throw new IllegalArgumentException("key cannot be null.");
      String guid = key.getPart();
      if(StringUtils.isBlank(guid))
         return null;
      return new PSGuid(guid);
   }
}
