/******************************************************************************
 *
 * [ PSSharedFieldsModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl.test;

import com.percussion.client.IPSReference;
import com.percussion.client.PSDuplicateNameException;
import com.percussion.client.PSErrorCodes;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.client.models.PSLockException;
import com.percussion.client.objectstore.PSUiContentEditorSharedDef;
import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Provides CRUD and cataloging services for the object type
 * {@link PSObjectTypes#SHARED_FIELDS}. Uses base class implementation
 * whenever possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSCmsModelProxy
 * 
 * @version 6.0
 * @created 03-Sep-2005 4:39:27 PM
 */
public class PSSharedFieldsModelProxy extends PSTestModelProxy
{
   /**
    * Ctor. Invokes base class version with the object type
    * {@link PSObjectTypes#SHARED_FIELDS} and for main type and
    * <code>null</code> sub type since this object type does not have any sub
    * types.    
    */
   public PSSharedFieldsModelProxy()
   {
      super(PSObjectTypes.SHARED_FIELDS);
      try
      {
         loadDefs();
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
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public IPSReference[] create(
      final PSObjectType objType, final Collection<String> names,
      final List results)
   {
      if(objType == null || !objType.equals(m_objectPrimaryType))
         throw new IllegalArgumentException("objType is invalid."); //$NON-NLS-1$
      if(names == null)
         throw new IllegalArgumentException("names cannot be null."); //$NON-NLS-1$
      if(results == null)
         throw new IllegalArgumentException("results cannot be null."); //$NON-NLS-1$
      IPSReference[] refs = new IPSReference[names.size()];
      int idx = -1;
      for(String name : names)
      {         
         PSUiContentEditorSharedDef def = new PSUiContentEditorSharedDef();
         def.copyFrom(getTestDef());
         refs[++idx] = createReference(name);
         results.add(def);
         m_lockHelper.getLock(refs[idx]);
        
      }
      return refs;
   }

   /* 
    * @see com.percussion.client.proxies.IPSCmsModelProxy#create(
    * java.lang.Object[], java.util.List)
    */
   @Override
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public IPSReference[] create(Object[] sourceObjects, String[] names,
         List results)
   {
      if(sourceObjects == null || sourceObjects.length == 0)
         throw new IllegalArgumentException(
            "sourceObjects cannot be null or empty."); //$NON-NLS-1$
      if (names != null && names.length != sourceObjects.length)
      {
         throw new IllegalArgumentException(
               "names must have same length as sourceObjects if supplied");
      }
      if(results == null)
         throw new IllegalArgumentException("results cannot be null."); //$NON-NLS-1$
      IPSReference[] refs = new IPSReference[sourceObjects.length];
      results.clear();
      for(int i = 0; i < sourceObjects.length; i++)
      {
         if(sourceObjects[i] instanceof PSContentEditorSharedDef)
         {
            try
            {
               
               PSUiContentEditorSharedDef def = 
                  (PSUiContentEditorSharedDef)clone(sourceObjects[i]);
                              
               results.add(def);
               String name;
               if (names == null || StringUtils.isBlank(names[i]))
                  name = getUniqueName(SHARED_DEF_GENERIC_FILE_NAME);
               else
                  name = names[i];
               refs[i] = createReference(name);
               m_lockHelper.getLock(refs[i]);
               
            }
            catch (Exception e)
            {
               throw new RuntimeException(e);
            }
            
         }
         else
         {
            throw new IllegalArgumentException(
                  "sourceObjects must be instances of PSContentEditorSharedDef"); //$NON-NLS-1$
         }
      }
      return refs;
   }
   
   /* 
    * @see com.percussion.client.proxies.impl.PSSharedFieldModelProxy#rename(
    * com.percussion.client.IPSReference, java.lang.String, java.lang.Object)
    */
   @Override
   public void rename(IPSReference ref, String name, Object data)
      throws PSModelException
   {
      // we may not know about the data object if it hasn't been persisted
      if (m_defs.containsKey(name))
         throw new PSModelException(new PSDuplicateNameException(name,
            PSObjectTypeFactory.getType((Enum) m_objectPrimaryType)));
      PSUiContentEditorSharedDef def = m_defs.get(ref.getName());
      if (def != null)
      {
         m_defs.remove(def);
         m_defs.put(name, def);
         saveDefs();
      }
      renameLocal(ref, name, data);
   }   
   
   /*
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#renameLocal(
    * com.percussion.client.IPSReference, java.lang.String, java.lang.Object)
    */
   @Override
   public void renameLocal(IPSReference ref, String name, Object data)
   {
      ((PSReference) ref).setName(name);      
   }

   /* 
    * @see com.percussion.client.proxies.test.impl.PSTestModelProxy#catalog()
    */
   @Override
   public Collection<IPSReference> catalog()
   {
      Collection<IPSReference> results = new ArrayList<IPSReference>();
      Iterator<String> it = m_defs.keySet().iterator();
      while(it.hasNext())
      {         
         PSReference pRef = (PSReference)createReference(it.next());
         pRef.setPersisted();
         results.add(pRef);         
      }
      return results;
   }

   /* 
    * @see com.percussion.client.proxies.test.impl.PSTestModelProxy#delete(
    * com.percussion.client.IPSReference[])
    */
   @Override
   public void delete(IPSReference[] references)
   {
      for (int i = 0; i < references.length; i++)
      {
         IPSReference ref = references[i];         
         m_defs.remove(ref.getName());
         m_lockHelper.releaseLock(ref);
      }    
      saveDefs();      
   }

   /* 
    * @see com.percussion.client.proxies.test.impl.PSTestModelProxy#load(
    * com.percussion.client.IPSReference[], boolean, boolean)
    */
   @Override
   public Object[] load(IPSReference[] references, boolean lock, boolean overrideLock)
      throws PSMultiOperationException
   {
           
      if(references == null || references.length == 0)
         throw new IllegalArgumentException(
            "reference cannot be null or empty.");
      Object[] results = new Object[references.length];
      boolean error = false;
      for(int i = 0; i < references.length; i++)
      {
         IPSReference ref = references[i];
         // Attempt to get lock if necessary
         if(lock)
         {
            m_lockHelper.getLock(ref);            
         }
         // Retrieve the object from the repository
         Object obj = 
            m_defs.get(ref.getName());
         if(obj == null)
         {
            results[i] = new PSModelException(PSErrorCodes.RAW, new Object[]
               {
                  "Shared def not found on file system."
               });
            error = true;
            continue;
         }
         try
         {
            obj = clone(obj);
            results[i] = obj;            
         }
         catch(Exception e)
         {
            results[i] = e;
            error = true;
         }
      }
      if(error)
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
         if (m_defs.get(refs[i].getName()) != null
               && !m_lockHelper.hasLock(refs[i]))
         {
            try
            {
               throw new PSLockException("save", m_objectPrimaryType.toString(), 
                  refs[i].getName());
            }
            catch (PSLockException e)
            {
               error = true;
               objects[i] = e;
            }
         }
         else
         {
            ((PSReference)refs[i]).setPersisted();
            m_defs.put(refs[i].getName(), (PSUiContentEditorSharedDef)data[i]);
            
            objects[i] = null;
            if (releaseLock)
               m_lockHelper.releaseLock(refs[i]);
            else if (!m_lockHelper.hasLock(refs[i]))
               m_lockHelper.getLock(refs[i]);
         }
      }
      if (error)
         throw new PSMultiOperationException(objects);      
      saveDefs();
      
   }   
   
   /* 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#clone(java.lang.Object)
    */
   @Override
   public Object clone(Object source)
   {
      if(!(source instanceof PSUiContentEditorSharedDef))
         throw new IllegalArgumentException(
            "source must be a PSContentEditorSharedDef object");
      return ((PSUiContentEditorSharedDef)source).clone();
   }

   /**
    * Load the defs from the file system.
    */
   @SuppressWarnings("unchecked")
   private void loadDefs()
   {
      m_defs.clear();
      File defDir = new File(SHARED_DEF_DIR);
      if (!defDir.exists() || !defDir.isDirectory())
      {
         defDir.mkdirs();
         m_defs.put(getUniqueName(SHARED_DEF_GENERIC_FILE_NAME), getTestDef());
         // and save to file system
         saveDefs();
      }
      else
      {
         
         File[] children = defDir.listFiles(new XmlFileFilter());
         for(File child : children)
         {
            m_defs.put(child.getName(), getPersistedDef(child));
         }
         
      }
   } 
   
   /**
    * Creates a new reference based on the name passed in for the
    * def.
    * @param name assumed not <code>null</code>.
    * @return the new reference, should never be <code>null</code>
    */
   private IPSReference createReference(String name)
   {
      IPSReference ref = null;
      try
      {
         ref = new PSReference(name, name, name, PSObjectTypeFactory
            .getType((Enum)m_objectPrimaryType), null);
      }
      catch (PSModelException e)
      {
         // should never get here
         e.printStackTrace();
      }
      return ref;
   }
   
   /**
    * Save the defs to the file system.
    */
   private void saveDefs()
   {
      File defDir = new File(SHARED_DEF_DIR);
      if (!defDir.exists() || !defDir.isDirectory())
      {
         defDir.mkdirs();
      }
      else
      {
         for(File child : defDir.listFiles(new XmlFileFilter()))
         {
            child.delete();
         }
      }
      Iterator<String> it = m_defs.keySet().iterator();
      while(it.hasNext())
      {
         String key = it.next();
         persistDef(key, m_defs.get(key));
      }      
   }
   
   
   /**
    * Retrieves the test configs from the xml file.
    * @return
    */
   private PSUiContentEditorSharedDef getTestDef()
   {
      Class clazz = getClass();
      InputStream is = clazz.getResourceAsStream("sharedDefs.xml");
      return getPersistedDef(is);
   }
   
   /**
    * Returns the xml document containing the shared def
    * data.
    * @return
    */
   private PSUiContentEditorSharedDef getPersistedDef(InputStream is)
   {
            
      Document doc = null;
      PSUiContentEditorSharedDef def = null;
      try
      {
         InputSource source = new InputSource(is);
         doc = PSXmlDocumentBuilder.createXmlDocument(source, false);
         Element root = doc.getDocumentElement();         
         def =  new PSUiContentEditorSharedDef(root, null, null);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }      
      finally
      {
         if(is != null)
            try
            {
               is.close();
            }
            catch (IOException ignore){}
      }
      return def;
      
   }
   
   private PSUiContentEditorSharedDef getPersistedDef(File file)
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
   
   private void persistDef(String filename, PSUiContentEditorSharedDef def)
   {
      File newFile = 
         new File(SHARED_DEF_DIR + File.separator + filename + ".xml");
      Document doc = def.toXml();      
      FileWriter writer = null;
      try
      {
         writer = new FileWriter(newFile);
         writer.write(
            PSXmlDocumentBuilder.toString(doc));
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
            if(writer != null)
               writer.close();
         }
         catch(IOException ignore){}
      }
   }
   
   /**
    * Insures that the passed in name is unique by appending an integer
    * if needed.
    * @param name
    * @return
    */
   private String getUniqueName(String name)
   {
      return getUniqueName(name, -1);
   }
   
   private String getUniqueName(String name, int postfix)
   {
      String fullname = name;
      if(postfix > 0)
         fullname += postfix;
      if(m_defs.containsKey(fullname))
         return getUniqueName(name, Math.max(1, postfix + 1));
      return fullname;
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
   
   /**
    * File filter that only allows .xml files
    */
   class XmlFileFilter implements FileFilter
   {
      public boolean accept(File pathname)
      {
         String filename = pathname.getName().toLowerCase();
         int pos = filename.lastIndexOf('.');
         if(pathname.isFile() && pos != -1 && pos < filename.length() - 1 )
         {
            if("xml".equals(filename.substring(pos + 1)))
               return true;
         }
         return false;
      }
   }   
   
  
   private Map<String, PSUiContentEditorSharedDef> m_defs = 
      new HashMap<String, PSUiContentEditorSharedDef>();
     
   private static final String SHARED_DEF_DIR = "sharedDefs";
   private static final String SHARED_DEF_GENERIC_FILE_NAME = "rx_sharedDef";


   

  

   
}
