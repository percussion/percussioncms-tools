/******************************************************************************
 *
 * [ PSExtensionModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl.test;

import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.catalogers.CatalogServerExits;
import com.percussion.client.proxies.PSUninitializedConnectionException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionDef;
import com.percussion.extension.PSExtensionDefFactory;
import com.percussion.extension.PSExtensionRef;
import com.percussion.util.IOTools;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * Provides CRUD and cataloging services for the object type
 * {@link PSObjectTypes#EXTENSION}. Uses base class implementation whenever
 * possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSCmsModelProxy
 * 
 * @version 6.0
 * @created 03-Sep-2005 4:39:27 PM
 */
public class PSExtensionModelProxy extends
   com.percussion.client.proxies.impl.PSExtensionModelProxy
{
   /**
    * Constructor. Calls the super.
    */
   public PSExtensionModelProxy() throws PSUninitializedConnectionException
   {
      super();
   }

   /**
    * Helper method to force load extensions from server to the cache.
    * 
    * @return the map with all test extensions, never <code>null</code>, 
    *    may be empty.
    * @throws PSModelException for any error.
    */
   @Override
   protected synchronized Map<String, IPSExtensionDef> load() 
      throws PSModelException
   {
      maybeInitLocalExtensionFile();
      
      Map<String, IPSExtensionDef> extensions = 
         new HashMap<String, IPSExtensionDef>();
      Vector exitVector;
      Exception ex = null;
      try
      {
         exitVector = CatalogServerExits
            .getCatalog(ms_localExtensionFile, true);
         if (exitVector != null && exitVector.size() != 0)
         {
            Iterator iter = exitVector.iterator();
            while (iter.hasNext())
            {
               IPSExtensionDef extDef = (IPSExtensionDef) iter.next();
               extensions.put(extDef.getRef().getFQN(), extDef);
            }
         }
      }
      catch (IOException e)
      {
         ex = e;
      }
      catch (SAXException e)
      {
         ex = e;
      }
      
      if (ex != null)
         throw new PSModelException(ex);
      
      return extensions;
   }

   /**
    * Creates extensions file if it does not exist yet.
    */
   private void maybeInitLocalExtensionFile()
   {
      if (!ms_localExtensionFile.exists())
      {
         OutputStream out = null;
         InputStream in = null;
         try
         {
            out = new FileOutputStream(ms_localExtensionFile);
            in = getClass().getResourceAsStream(EXTENSION_RESOURCE);
            IOTools.copyStream(in, out);
         }
         catch (IOException e)
         {
            throw new AssertionError(e);
         }
         finally
         {
            closeStream(out);
            closeStream(in);
         }
      }
   }

   /**
    * Helper method to close a stream.
    */
   private void closeStream(Closeable out)
   {
      if (out != null)
      {
         try
         {
            out.close();
         }
         catch (IOException e)
         {
            throw new AssertionError(e);
         }
      }
   }

   /**
    * @param extDefs
    * @throws PSMultiOperationException
    */
   @Override
   protected void remove(Object[] extDefs) throws PSMultiOperationException
   {
      if (extDefs == null || extDefs.length == 0)
      {
         throw new IllegalArgumentException("extDefs must not be null or empty"); //$NON-NLS-1$
      }
      PSExtensionRef[] extRefs = new PSExtensionRef[extDefs.length];
      for (int i = 0; i < extDefs.length; i++)
         extRefs[i] = ((IPSExtensionDef) extDefs[i]).getRef();
      for (int i = 0; i < extDefs.length; i++)
      {
         PSExtensionDef extDef = (PSExtensionDef) extDefs[i];
         m_extensions.remove(extDef.getRef().getFQN());
      }
      save(null);
   }

   /**
    * @param extDefs
    * @throws PSMultiOperationException
    */
   @Override
   protected void save(Object[] extDefs) throws PSMultiOperationException
   {
      for (int i = 0; extDefs != null && i < extDefs.length; i++)
      {
         PSExtensionDef def = (PSExtensionDef) extDefs[i];
         m_extensions.put(def.getRef().getFQN(), def);
      }
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc,
         "PSXExtensionHandlerConfiguration"); //$NON-NLS-1$
      root.setAttribute("handlerName", "Java"); //$NON-NLS-1$//$NON-NLS-2$
      Iterator iter = m_extensions.keySet().iterator();
      PSExtensionDefFactory factory = new PSExtensionDefFactory();
      while (iter.hasNext())
      {
         Object key = iter.next();
         PSExtensionDef def = (PSExtensionDef) m_extensions.get(key);
         factory.toXml(root, def);
      }
      Writer out = null;
      try
      {
         out = new FileWriter(ms_localExtensionFile);
         PSXmlDocumentBuilder.write(doc, out);
      }
      catch (UnsupportedEncodingException e)
      {
         //
         e.printStackTrace();
      }
      catch (IOException e)
      {
         //
         e.printStackTrace();
      }
      finally
      {
         if (out != null)
         {
            try
            {
               out.close();
            }
            catch (IOException e)
            {
            }
         }
      }
   }
   
   /**
    * Name of the testing extensions resource.
    */
   private final String EXTENSION_RESOURCE = "Extensions.xml";

   /**
    * Name of the file that holds the Rhythmyx extension definitions. Assumes it
    * exists in the working directory.
    */
   final static File ms_localExtensionFile = new File("extensions_repository.xml"); //$NON-NLS-1$
}
