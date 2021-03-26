/******************************************************************************
 *
 * [ PSContentTypeSystemConfigModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl.test;

import com.percussion.client.IPSReference;
import com.percussion.client.PSErrorCodes;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.PSLockException;
import com.percussion.client.proxies.PSProxyUtils;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.design.objectstore.PSUnknownDocTypeException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Provides CRUD and cataloging services for the object type
 * {@link PSObjectTypes#CONTENT_TYPE_SYSTEM_CONFIG}. Uses base class
 * implementation whenever possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSCmsModelProxy
 * 
 * @version 6.0
 * @created 03-Sep-2005 4:39:27 PM
 */
public class PSContentTypeSystemConfigModelProxy extends PSTestModelProxy
{
   /**
    * Ctor. Invokes base class version with the object type
    * {@link PSObjectTypes#CONTENT_TYPE_SYSTEM_CONFIG} and for main type and
    * <code>null</code> sub type since this object type does not have any sub
    * types.
    */
   public PSContentTypeSystemConfigModelProxy()
   {
      super(PSObjectTypes.CONTENT_TYPE_SYSTEM_CONFIG);
      try
      {

         File existingDef = new File(SYSTEM_DEF_FILE_NAME + ".xml");
         if (existingDef.exists())
         {
            m_def = getPersistedDef(existingDef);
         }
         else
         {
            m_def = getTestDef();
            persistDef();
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   /*
    * @see com.percussion.client.proxies.IPSCmsModelProxy#create(
    * com.percussion.client.PSObjectType, java.util.Collection, java.util.List)
    */
   @SuppressWarnings("unused")
   public IPSReference[] create(final PSObjectType objType,
      final Collection<String> names, final List results)
   {
      throw new UnsupportedOperationException();
   }

   /*
    * @see com.percussion.client.proxies.IPSCmsModelProxy#create(
    * java.lang.Object[], java.util.List)
    */
   @SuppressWarnings("unused")
   @Override
   public IPSReference[] create(Object[] sourceObjects, String[] names,
         List results)
   {
      throw new UnsupportedOperationException();
   }

   /*
    * @see com.percussion.client.proxies.impl.PSSharedFieldModelProxy#rename(
    * com.percussion.client.IPSReference, java.lang.String, java.lang.Object)
    */
   @SuppressWarnings("unused")
   @Override
   public void rename(IPSReference ref, String name, Object data)
      throws PSModelException
   {
      throw new UnsupportedOperationException();
   }

   /*
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#renameLocal(
    * com.percussion.client.IPSReference, java.lang.String, java.lang.Object)
    */
   @SuppressWarnings("unused")
   @Override
   public void renameLocal(IPSReference ref, String name, Object data)
   {
      throw new UnsupportedOperationException();
   }

   /*
    * @see com.percussion.client.proxies.test.impl.PSTestModelProxy#catalog()
    */
   @Override
   public Collection<IPSReference> catalog()
   {
      Collection<IPSReference> results = new ArrayList<IPSReference>();

      IPSReference pRef = PSProxyUtils.getSystemDefReference();
      results.add(pRef);

      return results;
   }

   /*
    * @see com.percussion.client.proxies.test.impl.PSTestModelProxy#delete(
    * com.percussion.client.IPSReference[])
    */
   @SuppressWarnings("unused")
   @Override
   public void delete(IPSReference[] references)
   {
      throw new UnsupportedOperationException();
   }

   /*
    * @see com.percussion.client.proxies.test.impl.PSTestModelProxy#load(
    * com.percussion.client.IPSReference[], boolean, boolean)
    */
   @Override
   public Object[] load(IPSReference[] references, boolean lock,
         @SuppressWarnings("unused") boolean overrideLock) 
      throws PSMultiOperationException
   {

      if (references == null || references.length == 0)
         throw new IllegalArgumentException(
            "reference cannot be null or empty.");
      Object[] results = new Object[references.length];
      boolean error = false;
      for (int i = 0; i < references.length; i++)
      {
         IPSReference ref = references[i];
         if (!ref.getName().equals(SYSTEM_DEF_FILE_NAME))
         {
            error = true;
            results[i] = new PSModelException(PSErrorCodes.RAW, new Object[]
            {
               "Reference contains invalid system def name."
            });
            continue;
         }
         // Attempt to get lock if necessary
         if (lock)
         {
            m_lockHelper.getLock(ref);
         }

         Object obj = m_def;
         if (obj == null)
         {
            results[i] = new PSModelException(PSErrorCodes.RAW, new Object[]
            {
               "System def not found on file system."
            });
            error = true;
            continue;
         }
         try
         {
            obj = clone(obj);
            results[i] = obj;
         }
         catch (Exception e)
         {
            results[i] = e;
            error = true;
         }
      }
      if (error)
         throw new PSMultiOperationException(results);

      return results;
   }

   /*
    * @see com.percussion.client.proxies.test.impl.PSTestModelProxy#save(
    * com.percussion.client.IPSReference[], java.lang.Object[], boolean)
    */
   @Override
   public void save(IPSReference[] refs, Object[] data, boolean releaseLock)
      throws PSMultiOperationException
   {
      Object[] objects = new Object[data.length];
      boolean error = false;
      for (int i = 0; i < data.length; i++)
      {
         if (refs[i].getName().equals(SYSTEM_DEF_FILE_NAME) && m_def != null
            && !m_lockHelper.hasLock(refs[i]))
         {
            try
            {
               throw new PSLockException("save",
                  m_objectPrimaryType.toString(), refs[i].getName());
            }
            catch (PSLockException e)
            {
               error = true;
               objects[i] = e;
            }
         }
         else
         {
            objects[i] = null;
            if (releaseLock)
               m_lockHelper.releaseLock(refs[i]);
            else if (!m_lockHelper.hasLock(refs[i]))
               m_lockHelper.getLock(refs[i]);
         }
      }
      if (error)
         throw new PSMultiOperationException(objects);
      persistDef();

   }

   /*
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#clone(java.lang.Object)
    */
   @Override
   public Object clone(Object source)
   {
      if (!(source instanceof PSContentEditorSystemDef))
         throw new IllegalArgumentException(
            "source must be a PSContentEditorSystemDef object");
      PSContentEditorSystemDef def = null;
      Document doc = ((PSContentEditorSystemDef) source).toXml();
      try
      {
         def = new PSContentEditorSystemDef(doc);
      }
      catch (PSUnknownDocTypeException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (PSUnknownNodeTypeException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return def;
   }

   /**
    * Retrieves the test configs from the xml file.
    * 
    * @return See {@link #getPersistedDef(InputStream)}.
    */
   private PSContentEditorSystemDef getTestDef()
   {
      Class clazz = getClass();
      InputStream is = clazz.getResourceAsStream("systemDef.xml");
      return getPersistedDef(is);
   }

   /**
    * Returns the xml document containing the shared def data.
    * 
    * @return May be <code>null</code> if any problems reading from the 
    * supplied stream.
    */
   private PSContentEditorSystemDef getPersistedDef(InputStream is)
   {

      Document doc = null;
      PSContentEditorSystemDef def = null;
      try
      {
         InputSource source = new InputSource(is);
         doc = PSXmlDocumentBuilder.createXmlDocument(source, false);
         def = new PSContentEditorSystemDef(doc);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      finally
      {
         if (is != null)
            try
            {
               is.close();
            }
            catch (IOException ignore)
            {
            }
      }
      return def;

   }

   private PSContentEditorSystemDef getPersistedDef(File file)
   {
      InputStream is = null;
      try
      {
         is = new FileInputStream(file);
         return getPersistedDef(is);
      }
      catch (FileNotFoundException e)
      {
         e.printStackTrace();
      }
      return null;
   }

   private void persistDef()
   {
      File newFile = new File(SYSTEM_DEF_FILE_NAME + ".xml");
      Document doc = m_def.toXml();
      FileWriter writer = null;
      try
      {
         writer = new FileWriter(newFile);
         writer.write(PSXmlDocumentBuilder.toString(doc));
         writer.flush();
      }
      catch (IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      finally
      {
         try
         {
            if (writer != null)
               writer.close();
         }
         catch (IOException ignore)
         {
         }
      }
   }

   /*
    * @see com.percussion.client.proxies.test.impl.PSTestModelProxy#
    * getRepositoryFile()
    */
   @Override
   protected File getRepositoryFile()
   {
      return null;
   }

   /*
    * @see com.percussion.client.proxies.test.impl.PSTestModelProxy#
    * getRepositoryMap()
    */
   @Override
   protected IPSRepositoryMap getRepositoryMap()
   {
      return null;
   }

   private PSContentEditorSystemDef m_def;

   private static final String SYSTEM_DEF_FILE_NAME = "contentEditorSystemDef";
}
