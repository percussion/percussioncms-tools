/******************************************************************************
 *
 * [ PSExtensionModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.catalogers.CatalogServerExits;
import com.percussion.client.impl.PSReference;
import com.percussion.client.proxies.PSProxyUtils;
import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSExtensionFile;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionDef;
import com.percussion.extension.PSExtensionDefFactory;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.services.security.PSPermissions;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * Provides CRUD and cataloging services for the object type
 * {@link PSObjectTypes#EXTENSION}. Uses base class implementation whenever
 * possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSCmsModelProxy
 * 
 * @version 6.0
 * @since 03-Sep-2005 4:39:27 PM
 */
public class PSExtensionModelProxy extends PSLegacyModelProxy
{
   /**
    * Ctor. Invokes base class version with the object type
    * {@link PSObjectTypes#EXTENSION} and for primary type.
    */
   public PSExtensionModelProxy()
   {
      super(PSObjectTypes.EXTENSION);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#catalog(com.percussion.client.IPSReference)
    */
   public Collection<IPSReference> catalog() throws PSModelException
   {
      Collection<IPSReference> extRefs = new ArrayList<>();

      if (m_extensions == null)
         m_extensions = load();
      
      Iterator iter = m_extensions.keySet().iterator();
      while (iter.hasNext())
      {
         String fqn = (String) iter.next();
         IPSExtensionDef extDef = m_extensions.get(fqn);
         PSExtensionRef extRef = extDef.getRef();
         PSReference newRef = new PSReference();
         newRef.setName(extRef.getFQN());
         newRef.setLabelKey(extRef.getExtensionName());
         newRef.setDescription(extDef
            .getInitParameter(IPSExtensionDef.INIT_PARAM_DESCRIPTION));

         //extensions don't support acls, so fake full access
         newRef.setPermissions(new int[] { PSPermissions.READ.getOrdinal(),
               PSPermissions.UPDATE.getOrdinal(),
               PSPermissions.DELETE.getOrdinal(),
               PSPermissions.OWNER.getOrdinal() });
         assert newRef.getId() == null;
         try
         {
            newRef.setObjectType(new PSObjectType(getPrimaryType(), null));
            extRefs.add(newRef);
         }
         catch (PSModelException e)
         {
            throw new PSModelException(e);
         }
      }

      return extRefs;
   }

   /**
    * Helper method to force load extensions from repository to the cache.
    * 
    * @return the map with all cataloged and initialized extensions, never
    *    <code>null</code>, may be empty.
    * @throws PSModelException if load fails for any reason.
    */
   @SuppressWarnings("unchecked")
   protected synchronized Map<String, IPSExtensionDef> load() 
      throws PSModelException
   {
      Map<String, IPSExtensionDef> extensions = 
         new HashMap<>();
      Vector exitVector = null;
      Exception ex = null;
      try
      {
         exitVector = CatalogServerExits.getCatalog(getConnection(), "Java",
            null, null, true, true);
         Vector jScript = CatalogServerExits.getCatalog(getConnection(),
            "JavaScript", null, null, true, true);
         if (jScript != null)
            exitVector.addAll(jScript);

         if (exitVector != null && !exitVector.isEmpty())
         {
            Iterator iter = exitVector.iterator();
            while (iter.hasNext())
            {
               IPSExtensionDef extDef = (IPSExtensionDef) iter.next();
               extensions.put(extDef.getRef().getFQN(), extDef);
            }
         }
      }
      catch (PSServerException | PSAuthenticationFailedException | PSAuthorizationException | IOException e)
      {
         ex = e;
      }

       if (ex != null)
         throw new PSModelException(ex);
      
      return extensions;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#create(int,
    * java.util.List)
    */
   @SuppressWarnings("unchecked")
   public IPSReference[] create(PSObjectType objType, Collection<String> names,
      List<Object> results) throws PSMultiOperationException
   {
      if (objType != null
         && objType.getPrimaryType() != PSObjectTypes.EXTENSION)
      {
         throw new IllegalArgumentException(
            "Invalid object type specified for the proxy"); //$NON-NLS-1$
      }
      if (names == null || names.isEmpty())
      {
         throw new IllegalArgumentException("names must not be null or empty"); //$NON-NLS-1$
      }
      if (results != null)
         results.clear();
      else
         results = new ArrayList();

      PSReference[] refs = new PSReference[names.size()];
      Iterator iter = names.iterator();
      int i = 0;
      Object[] errResults = new Object[names.size()];
      boolean errorOccured = false;
      while (iter.hasNext())
      {
         String name = (String) iter.next();
         PSExtensionDef def = createNewExtension(name);
         if (results != null)
            results.add(def);
         try
         {
            refs[i] = new PSReference(def.getRef().getFQN(),
                  def.getRef().getExtensionName(),
                  "New Extension", new PSObjectType(getPrimaryType(), null), //$NON-NLS-1$
                  null);
            m_locks.add(def.getRef().getFQN());
         }
         catch (PSModelException e)
         {
            errResults[i] = e;
            errorOccured = true;
         }
         i++;
      }
      if (errorOccured)
      {
         throw new PSMultiOperationException(errResults, names);
      }
      return refs;
   }

   /**
    * Helper method to create a new extension in an acceptible state but may not
    * be valid state.
    * 
    * @param name name fo the extension
    * @return extension defintiion, never <code>null</code>.
    */
   private PSExtensionDef createNewExtension(String name)
   {
      PSExtensionDef def = new PSExtensionDef();
      def.setInitParameter(IPSExtensionDef.INIT_PARAM_REENTRANT, "yes"); //$NON-NLS-1$ //$NON-NLS-2$
      Collection<String> coll = new ArrayList<String>();
      coll.add("dummy"); //$NON-NLS-1$
      def.setInterfaces(coll);
      PSExtensionRef extRef = new PSExtensionRef(name);
      def.setExtensionRef(extRef);
      return def;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#create(java.lang.Object[],
    * java.util.List)
    */
   @SuppressWarnings("unchecked")
   public IPSReference[] create(Object[] sourceObjects, String[] names,
         List results)
      throws PSMultiOperationException
   {
      if (sourceObjects == null || sourceObjects.length == 0)
      {
         throw new IllegalArgumentException(
            "sourceObjects must not be null or empty"); //$NON-NLS-1$
      }
      if (names != null)
      {
         throw new IllegalArgumentException(
               "the extension proxy does not support supplied names");
      }
      if (results == null)
      {
         throw new IllegalArgumentException("results must not be null");
      }
      results.clear();
      PSExtensionDefFactory fact = new PSExtensionDefFactory();
      PSReference[] refs = new PSReference[sourceObjects.length];
      Object[] errResults = new Object[sourceObjects.length];
      boolean errorOccured = false;
      for (int i = 0; i < sourceObjects.length; i++)
      {
         PSExtensionDef def = (PSExtensionDef) sourceObjects[i];
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         Element root = PSXmlDocumentBuilder.createRoot(doc, "root"); //$NON-NLS-1$
         IPSExtensionDef newDef = null;
         errResults[i] = null;// assume it is going to be a success
         try
         {
            newDef = fact.fromXml(fact.toXml(root, def));
            results.add(newDef);
            PSExtensionRef extRef = newDef.getRef();
            refs[i] = new PSReference(extRef.getFQN(),
                  extRef.getExtensionName(),
                  newDef.getInitParameter(IPSExtensionDef.INIT_PARAM_DESCRIPTION),
                  new PSObjectType(getPrimaryType(), null),
                  null);
            m_locks.add(refs[i].getName());
         }
         catch (PSModelException e)
         {
            errResults[i] = e;
            errorOccured = true;
         }
         catch (PSExtensionException e)
         {
            errResults[i] = e;
            errorOccured = true;
         }
      }
      if (errorOccured)
      {
         throw new PSMultiOperationException(errResults, names);
      }
      return refs;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#load(com.percussion.client.IPSReference[],
    * boolean, boolean)
    */
   @SuppressWarnings("unused") 
   public Object[] load(IPSReference[] reference, boolean lock,
      boolean overrideLock) throws PSMultiOperationException, PSModelException
   {
      if (m_extensions == null)
         m_extensions = load();
      Object[] extensions = new Object[reference.length];
      for (int i = 0; i < reference.length; i++)
      {
         IPSReference ref = reference[i];
         extensions[i] = m_extensions.get(ref.getName()).clone();
         if (lock)
         {
            m_locks.add(ref.getName());
            PSProxyUtils.setLockInfo(new IPSReference[]
            {
               ref
            }, false);
         }
      }
      return extensions;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#delete(com.percussion.client.IPSReference[])
    */
   public void delete(IPSReference[] reference)
      throws PSMultiOperationException, PSModelException
   {
      Object[] extDefs = load(reference, true, true);
      remove(extDefs);
      try
      {
         m_extensions = load();
      }
      catch (PSModelException e)
      {
         throw new PSMultiOperationException(e);
      }
   }

   /**
    * @param extDefs
    * @throws PSMultiOperationException
    */
   protected void remove(Object[] extDefs) throws PSMultiOperationException
   {
      if (extDefs == null || extDefs.length == 0)
      {
         throw new IllegalArgumentException("extDefs must not be null or empty"); //$NON-NLS-1$
      }
      PSExtensionRef[] extRefs = new PSExtensionRef[extDefs.length];
      for (int i = 0; i < extDefs.length; i++)
         extRefs[i] = ((IPSExtensionDef) extDefs[i]).getRef();
      Object[] result = new Object[extRefs.length];
      boolean error = false;
      for (int i = 0; i < extRefs.length; i++)
      {
         result[i] = null;
         try
         {
            getObjectStore().removeExtension(extRefs[i]);
         }
         catch (Exception e)
         {
            error = true;
            result[i] = e;
         }
      }
      if (error)
         throw new PSMultiOperationException(result);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#save(java.lang.Object[],
    * boolean)
    */
   public void save(IPSReference[] refs, Object[] data, boolean releaseLock)
      throws PSMultiOperationException
   {
      save(data);
      for (int i = 0; i < refs.length; i++)
      {
         PSExtensionDef def = (PSExtensionDef) data[i];
         PSReference ref = (PSReference) refs[i];
         
         // Since ref has fully qualified name we have to correct the ref name
         // and the entry in the locks.
         m_locks.remove(ref.getName());
         ref.setName(def.getRef().getFQN());
         m_locks.add(ref.getName());
         ref.setLabelKey(def.getRef().getExtensionName());
         ref.setDescription(def.getInitParameter(
            IPSExtensionDef.INIT_PARAM_DESCRIPTION));
         ref.setPersisted();
         if (releaseLock)
         {
            m_locks.remove(ref.getName());
            PSProxyUtils.setLockInfo(new IPSReference[]
            {
               ref
            }, true);
         }
      }
      try
      {
         /*
          * ph: it doesn't seem this should be necessary, but I believe it is
          * done because all extensions are saved as a unit
          */
         m_extensions = load();
      }
      catch (PSModelException e)
      {
         throw new PSMultiOperationException(e);
      }
   }

   /**
    * Saves the supplied data to persistent storage on the server.
    * 
    * @param extDefs The list of extension defs to save, may not be
    * <code>null</code> or empty.
    * 
    * @throws PSMultiOperationException If there are any errors during the
    * save.
    */
   protected void save(Object[] extDefs) throws PSMultiOperationException
   {
      if (extDefs == null || extDefs.length == 0)
      {
         throw new IllegalArgumentException(
            "extDefs must not be null or empty"); //$NON-NLS-1$
      }
      
      Object[] result = new Object[extDefs.length];
      boolean error = false;
      for (int i = 0; i < extDefs.length; i++)
      {
         result[i] = null;
         try
         {
            PSExtensionDef def = (PSExtensionDef) extDefs[i];
            
            // Convert resources to PSExtensionFile objects
            List<PSExtensionFile> extFiles = buildExtensionFiles(def);
            
            // set resource locations based on def's resources
            def.setResourceLocations(buildResourceUrls(
               def.getSuppliedResources()));
            
            getObjectStore().saveExtension(new PSExtensionDefFactory(), def,
               extFiles.iterator(), true);
         }
         catch (Exception e)
         {
            error = true;
            result[i] = e;
         }
      }
      if (error)
         throw new PSMultiOperationException(result);
   }

   /**
    * Looks at all of the files in the supplied collection and creates a list
    * of URLs that can be passed to a class loader to find these resources.
    * For each archive, a URL that contains the file portion of the name
    * is created. For all other files (class, images, etc), a relative directory
    * path ("classes/") is created.  See 
    * {@link IPSExtensionDef#getResourceLocations()} for more info.
    *  
    * @param files An iterator over zero or more <code>File</code>objects 
    * pointing to files on the local drive. May be <code>null</code>. If so, an 
    * empty collection is returned. <code>null</code> objects in the list are 
    * ignored.
    * 
    * @return A collection of URLs to use as the def's resource locations, 
    * never <code>null</code>, may be empty if the supplied <code>files</code>
    * is <code>null</code> or empty.
    *  
    * @throws MalformedURLException If any of the supplied file objects can't
    * be converted to URLs.
    */
   private Collection<URL> buildResourceUrls(Iterator files) 
      throws MalformedURLException
   {
      List<URL> urls = new ArrayList<URL>();
      boolean haveNonArchive = false;
      
      if (files == null)
         return urls;
      
      while ( files.hasNext())
      {
         URL url = (URL) files.next();
         if (url == null)
            continue;
         
         File file = new File(url.getPath());

         if ( isArchive( file ))
            urls.add( new URL("file", "", file.getName()) );
         else
            haveNonArchive = true;
      }

      if ( haveNonArchive )
      {
         URL url = new URL( "file", "", CLASS_DIR );
         urls.add( url );
      }

      return urls;
   }

   /**
    * Converts the files returned by the supplied def's 
    * {@link IPSExtensionDef#getSuppliedResources() getSuppliedResources()}
    * method to extension files based on the classname specified in the def.
    * 
    * @param def The def, assumed not <code>null</code>.
    * 
    * @return A list of files, never <code>null</code>, may be empty if the 
    * supplied def does not specify any resources or if the def does not
    * specify the {@link IPSExtensionDef#INIT_PARAM_CLASSNAME} init parameter.
    * 
    * @throws FileNotFoundException If any resource specified cannot be located.
    */
   private List<PSExtensionFile> buildExtensionFiles(PSExtensionDef def) 
      throws FileNotFoundException
   {
      List<PSExtensionFile> extList = new ArrayList<PSExtensionFile>();
      
      String className = def.getInitParameter(
         IPSExtensionDef.INIT_PARAM_CLASSNAME);
      
      if (StringUtils.isBlank(className))
         return extList;
      
      boolean errorOccurred = false;   // used to cleanup if exception
      try
      {
         Iterator it = def.getSuppliedResources();
         while (it != null && it.hasNext())
         {
            URL url = (URL) it.next();
            File file = new File(url.getPath());

            // build the relative name
            int dotPos = className.lastIndexOf( '.' );
            String pkgName = null;
            String relName = null;
            if ( dotPos > 0 )
               pkgName = className.substring( 0, dotPos );
            
            if ( null == pkgName )
               // not in a package
               if ( file.getName().lastIndexOf( ".class" ) > 0 )
                  // if a class, put in a classes directory
                  relName = CLASS_DIR + "/" + file.getName();
               else
                  relName = file.getName();
               
            else
            {
               // it's in a package
               if ( file.getName().lastIndexOf( ".class" ) > 0 )
               {
                  // not a jar, build the full path for the package
                  pkgName = pkgName.replace( '.', '/' );
                  relName = CLASS_DIR + File.separator + pkgName + "/" + 
                     file.getName();
               }
               else
               {
                  // it's a jar
                  relName = file.getName();
               }
            }
            
            FileInputStream inStream = new FileInputStream( file );
            extList.add( new PSExtensionFile( inStream, new File( relName )));
         }
      }
      catch ( PSIllegalArgumentException e )
      {
         errorOccurred = true;
         throw new IllegalArgumentException( e.getLocalizedMessage());
      }
      catch ( RuntimeException e )
      {
         errorOccurred = true;
         throw e;
      }
      finally
      {
         if ( errorOccurred )
         {
            // close anything we've already opened
            for ( int i = extList.size(); i >= 0; --i )
            {
               PSExtensionFile file = extList.remove(i);
               try
               {
                  file.getContent().getContent().close();
               }
               catch (Throwable t)
               { /* ignore, we're just trying to cleanup */ }
            }
         }
      }
      
      return extList;
   }
   
   /**
    * Checks the supplied file to determine if it is a Java archive (for
    * example: jar).
    *
    * @param file The file to check, assumed not <code>null</code>.
    *
    * @return <code>true</code> if the file is determined to be an archive by
    * looking at its extension, <code>false</code> otherwise.
    */
   private static boolean isArchive( File file )
   {
      String fileName = file.getName();
      int dotPos = fileName.lastIndexOf( '.' );
      String ext = "";
      if ( dotPos > 0 )
         ext = fileName.substring( dotPos + 1 );

      // the extensions that are considered archives
      String [] archiveExt = { "zip", "jar", "cab" };
      boolean found = false;
      for ( int i = 0; i < archiveExt.length; ++i )
         found = found || ext.equalsIgnoreCase( archiveExt[i] );

      return found;
   }   

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#rename(com.percussion.client.IPSReference,
    * java.lang.String, java.lang.Object)
    */
   public void rename(IPSReference ref, String name, Object data)
      throws PSModelException
   {
      IPSExtensionDef extDef = m_extensions.remove(ref.getName());
      if (extDef != null)
      {
         String oldName = extDef.getRef().getExtensionName();
         PSExtensionRef extRef = extDef.getRef();
         extRef.setExtName(name);
         Object[] extDefs = new Object[]
         {
            extDef
         };
         try
         {
            save(extDefs);
         }
         catch (PSMultiOperationException e)
         {
            // Put the old name back
            extRef.setExtName(oldName);
            throw new PSModelException(e);
         }
         finally
         {
            m_extensions.put(extDef.getRef().getFQN(), extDef);
         }
      }
      else
      {
         // It is might not be saved yet, rename in the fly which is required
         // anyway and hence outside of the if loop..
      }
      PSExtensionRef extRef = ((IPSExtensionDef) data).getRef();
      extRef.setExtName(name);
      ((PSReference) ref).setName(extRef.getFQN());
      ((PSReference) ref).setLabelKey(name);
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#releaseLock(com.percussion.client.IPSReference[])
    */
   @SuppressWarnings("unused") //exception
   public void releaseLock(IPSReference[] references)
      throws PSMultiOperationException
   {
      if (references == null)
      {
         throw new IllegalArgumentException("references must not be null");
      }
      for (IPSReference reference : references)
      {
         m_locks.remove(reference.getName());
         PSProxyUtils.setLockInfo(references, true);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#isLocked(com.percussion.client.IPSReference)
    */
   public boolean isLocked(IPSReference ref)
   {
      if (ref == null)
      {
         throw new IllegalArgumentException("ref must not be null");
      }
      return m_locks.contains(ref.getName());
   }
   
   /**
    * Are there any extensions locked?
    * 
    * @return <code>true</code> if there are, <code>false</code> otherwise.
    */
   private boolean hasLocks()
   {
      return !m_locks.isEmpty();
   }

   /* (non-Javadoc)
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#flush(
    *    com.percussion.client.IPSReference)
    */
   @Override
   public void flush(IPSReference ref)
   {
      if (!hasLocks())
         m_extensions = null;

      super.flush(ref);
   }
   
   /**
    * The directory on the server where class files will be stored. 
    */
   private static final String CLASS_DIR = "classes";   

   /**
    * Local cache of all extensions loaded from the server. The key in the map
    * is the fully qualified name of the extension definition and the value is
    * the extension def. Never <code>null</code> after cataloged at least once
    * (i.e. called {@link #catalog()} at least once), may be empty.
    */
   protected Map<String, IPSExtensionDef> m_extensions = null;

   /**
    * Local locks for extensions. This is required since server does not support
    * extension locking. The entry in the set is the fully qualified name of the
    * extension and matches the {@link PSReference#getName()}
    */
   private Set<String> m_locks = new HashSet<String>();
}
