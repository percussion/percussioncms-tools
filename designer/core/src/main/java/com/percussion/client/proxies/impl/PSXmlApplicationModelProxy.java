/******************************************************************************
 *
 * [ PSXmlApplicationModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.PSObjectTypes.XmlApplicationSubTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.client.models.PSLockException;
import com.percussion.client.objectstore.PSUiApplication;
import com.percussion.client.proxies.IPSXmlApplicationConverter;
import com.percussion.client.proxies.PSProxyUtils;
import com.percussion.client.proxies.PSXmlApplicationConverterProvider;
import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSApplicationFile;
import com.percussion.design.objectstore.PSApplicationType;
import com.percussion.design.objectstore.PSLockedException;
import com.percussion.design.objectstore.PSNonUniqueException;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSNotLockedException;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.design.objectstore.PSUnknownDocTypeException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.design.objectstore.PSValidationException;
import com.percussion.design.objectstore.PSVersionConflictException;
import com.percussion.error.PSException;
import com.percussion.security.PSAuthenticationFailedExException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides CRUD and cataloging services for the object type
 * {@link PSObjectTypes#XML_APPLICATION}. Uses base class implementation
 * whenever possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSCmsModelProxy
 * 
 * @version 6.0
 * @created 03-Sep-2005 4:39:27 PM
 */
public class PSXmlApplicationModelProxy extends PSLegacyModelProxy
{
   /**
    * Ctor. Invokes base class version with the object type
    * {@link PSObjectTypes#XML_APPLICATION} and for primary type.
    */
   public PSXmlApplicationModelProxy()
   {
      super(PSObjectTypes.XML_APPLICATION);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSLegacyModelProxy#catalog(com.percussion.client.IPSReference)
    */
   public Collection<IPSReference> catalog() throws PSModelException
   {
      Collection<IPSReference> coll = new ArrayList<IPSReference>();
      Properties props = new Properties();
      props.put("name", "");
      props.put("description", "");
      props.put("appType", "");
      props.put("id", "");

      try
      {
         Enumeration apps = getObjectStore().getApplicationSummaries(props, true);
         while (apps.hasMoreElements())
         {
            Properties appProps = (Properties) apps.nextElement();
            String name = appProps.getProperty("name");
            String desc = appProps.getProperty("description");
            String type = appProps.getProperty("appType");
            final int id = Integer.parseInt(appProps.getProperty("id"));
            final PSObjectType objectType = PSObjectTypeFactory.getType(
                  getPrimaryType(), findByAppType(type));
            PSReference ref = new PSReference(name, name,
                  desc, objectType,
                  new PSGuid(0, PSTypeEnum.LEGACY_CHILD, id));
            ref.setPersisted();
            coll.add(ref);
         }
      }
      catch (PSAuthenticationFailedException e)
      {
         throw new PSModelException(e);
      }
      catch (PSServerException e)
      {
         throw new PSModelException(e);
      }
      catch (PSModelException e)
      {
         throw new PSModelException(e);
      }

      return coll;
   }

   @SuppressWarnings("unchecked")
   public IPSReference[] create(PSObjectType objType, Collection<String> names,
      List results)
   {
      if (results == null)
         results = new ArrayList<PSApplication>();
      else
         results.clear();
      List<IPSReference> refs = new ArrayList<IPSReference>();// PSReference[count];
      for (String name : names)
      {
         try
         {
            PSApplication app = getConverter().convert(
                  getObjectStore().createApplication());
            app.setName(name);
            if (objType.getSecondaryType() 
                  == PSObjectTypes.XmlApplicationSubTypes.SYSTEM)
            {
               app.setApplicationType(PSApplicationType.SYSTEM);
            }
            else
            {
               assert (objType.getSecondaryType() 
                     == PSObjectTypes.XmlApplicationSubTypes.USER);
               app.setApplicationType(PSApplicationType.USER);
            }
            results.add(app);
            refs.add(new PSReference(app.getName(), app.getName(),
                  app.getDescription(), objType, null));
         }
         catch (PSServerException e)
         {
            throw new RuntimeException(e);
         }
         catch (PSAuthorizationException e)
         {
            throw new RuntimeException(e);
         }
         catch (PSAuthenticationFailedException e)
         {
            throw new RuntimeException(e);
         }
         catch (PSModelException e)
         {
            throw new RuntimeException(e);
         }
         catch (PSException e)
         {
            throw new RuntimeException(e);
         }
      }
      IPSReference[] refArray = refs.toArray(new IPSReference[0]);
      PSCmsModelProxy.configureReferenceSecurity(refArray);
      return refArray;
   }

   @SuppressWarnings("unchecked")
   public IPSReference[] create(Object[] sourceObjects, String[] names, 
      List results) throws PSModelException
   {
      if (sourceObjects == null || sourceObjects.length == 0)
      {
         throw new IllegalArgumentException(
            "sourceObjects must not be null or empty");
      }

      if (names != null && names.length != sourceObjects.length)
      {
         throw new IllegalArgumentException(
               "names must have same length as sourceObjects if supplied");
      }

      checkInstanceType(sourceObjects, PSApplication.class);
      PSReference[] refs = new PSReference[sourceObjects.length];
      if (results == null)
         results = new ArrayList<PSApplication>();
      else
         results.clear();

      List<String> existingNames = new ArrayList<String>();
      try
      {
         Collection<IPSReference> handles = getModel().catalog();
         for (IPSReference ref : handles)
            existingNames.add(ref.getName());
      }
      catch (PSModelException e)
      {
         // ignore it for now, will be caught by the server if a problem
      }

      int tempId = ms_tempId.incrementAndGet();
      for (int i=0; i<sourceObjects.length; i++)
      {
         PSApplication app = (PSApplication) sourceObjects[i];
         
         // clone the app
         try
         {
            final PSApplication newApp = getConverter().convert(
               getObjectStore().createApplication());
            /*
             * We must use a unique temporary id to make sure that our
             * locking mechanism works fine with multiple applications copied
             * at a time.
             */
            final int id = tempId--;
            newApp.fromXml(app.toXml());
            newApp.setId(id);
            newApp.setEnabled(false);

            final PSUiApplication newUiApp = 
               new PSUiApplication(newApp.toXml());
            newUiApp.setApplicationFiles(getApplicationFiles(app));
            
            final String name;
            if (names == null || StringUtils.isBlank(names[i]))
               name = PSCoreUtils.createCopyName(newUiApp.getName(), -1, 
                  existingNames);
            else
               name = names[i];
            newUiApp.setName(name);
            newUiApp.setRequestRoot(name);

            final PSObjectType objectType = PSObjectTypeFactory.getType(
               PSObjectTypes.XML_APPLICATION, 
               findByAppType(app.getApplicationType()));
            refs[i] = new PSReference(newUiApp.getName(), newUiApp.getName(),
               newUiApp.getDescription(), objectType,
               new PSGuid(0, PSTypeEnum.LEGACY_CHILD, id));

            results.add(newUiApp);
         }
         catch (PSUnknownDocTypeException e)
         {
            // this cannot happen theoretically since we roundtripped
            // (toXml and fromXml) right here to create the application object
            throw new RuntimeException(e.getLocalizedMessage());
         }
         catch (PSUnknownNodeTypeException e)
         {
            // this cannot happen theoretically since we roundtripped
            // (toXml and fromXml) right here to create the application object
            throw new RuntimeException(e.getLocalizedMessage());
         }
         catch (PSException e)
         {
            throw new RuntimeException(e);
         }
      }
      
      return refs;
   }

   public Object[] load(IPSReference[] reference, boolean lock,
      boolean overrideLock) throws PSMultiOperationException
   {
      if (reference == null || reference.length == 0)
      {
         throw new IllegalArgumentException(
            "reference must not be null or empty");
      }
      Object[] apps = new PSApplication[reference.length];
      Object[] res = new Object[reference.length];
      boolean error = false;
      for (int i = 0; i < reference.length; i++)
      {
         Exception ex = null;
         IPSReference ref = reference[i];
         try
         {
            PSApplication app = getConverter().convert( 
               getObjectStore().getApplication(ref.getName(), lock,
                     overrideLock));
            apps[i] = app;
            res[i] = app;
            if (lock)
            {
               PSProxyUtils.setLockInfo(new IPSReference[]
               {
                  ref
               }, false);
            }
         }
         catch (PSServerException e)
         {
            ex = e;
         }
         catch (PSAuthorizationException e)
         {
            ex = e;
         }
         catch (PSAuthenticationFailedException e)
         {
            ex = e;
         }
         catch (PSLockedException e)
         {
            ex = new PSLockException("load",
                  ref.getObjectType().getPrimaryType().name(),ref.getName());
         }
         catch (PSNotFoundException e)
         {
            ex = e;
         }
         catch (PSException e)
         {
            ex = e;
         }
         if (ex != null)
         {
            error = true;
            res[i] = ex;
         }
      }
      if (error)
      {
         throw new PSMultiOperationException(res, reference);
      }
      return apps;
   }

   public void delete(IPSReference[] reference)
   {
      if (reference == null || reference.length == 0)
      {
         throw new IllegalArgumentException(
            "reference must not be null or empty");
      }
      for (int i = 0; i < reference.length; i++)
      {
         IPSReference ref = reference[i];
         try
         {
            getObjectStore().removeApplication(ref.getName());
         }
         catch (PSServerException e)
         {
            // XXX Auto-generated catch block
            e.printStackTrace();
         }
         catch (PSAuthorizationException e)
         {
            // XXX Auto-generated catch block
            e.printStackTrace();
         }
         catch (PSAuthenticationFailedException e)
         {
            // XXX Auto-generated catch block
            e.printStackTrace();
         }
         catch (PSLockedException e)
         {
            // XXX Auto-generated catch block
            e.printStackTrace();
         }
      }
   }

   public void save(IPSReference[] refs, Object[] data, boolean releaseLock)
      throws PSMultiOperationException
   {
      if (refs == null || refs.length == 0)
      {
         throw new IllegalArgumentException("refs must not be null or empty");
      }
      if (data == null || data.length == 0)
      {
         throw new IllegalArgumentException("data must not be null or empty");
      }

      checkInstanceType(data, PSApplication.class);
      Object[] res = new Object[data.length];
      boolean error = false;
      for (int i = 0; i < data.length; i++)
      {
         PSApplication app = (PSApplication) data[i];
         final PSReference ref = (PSReference) refs[i];
         Exception ex = null;
         res[i] = null;
         try
         {
            final boolean isNew = !ref.isPersisted();
            if (isNew)
            {
               assert !app.isEnabled(); 
            }
            
            // don't validate new one because it is possible it hasn't been
            // initialized yet
            final boolean validate = !isNew;
            getObjectStore().saveApplication(app, false, validate, isNew);
            if (app instanceof PSUiApplication)
            {
               // save the supplied application files
               PSUiApplication uiApp = (PSUiApplication) app;
               for (PSApplicationFile file : uiApp.getApplicationFiles())
                  getObjectStore().saveApplicationFile(app, file, true, false);
               
               /*
                * We can only use the files input steams once. Remove them
                * so we don't try to use them multiple times.
                */
               uiApp.setApplicationFiles(null);
            }
            ref.setPersisted();
            /*
             * We create the correct application id with the first save. We
             * must update the temporary id with the real one.
             */
            ref.setId(new PSGuid(0, PSTypeEnum.LEGACY_CHILD, app.getId()));

            if (releaseLock)
            {
               getObjectStore().releaseApplicationLock(app);
               PSProxyUtils.setLockInfo(new IPSReference[]
               {
                  ref
               }, true);
            }
         }
         catch (PSLockedException e)
         {
            ex = e;
         }
         catch (PSServerException e)
         {
            ex = e;
         }
         catch (PSAuthorizationException e)
         {
            ex = e;
         }
         catch (PSAuthenticationFailedException e)
         {
            ex = e;
         }
         catch (PSNotLockedException e)
         {
            ex = e;
         }
         catch (PSNonUniqueException e)
         {
            ex = e;
         }
         catch (PSValidationException e)
         {
            ex = e;
         }
         catch (PSVersionConflictException e)
         {
            ex = e;
         }
         if (ex != null)
         {
            res[i] = ex;
            error = true;
         }
      }
      if (error)
      {
         throw new PSMultiOperationException(res, refs);
      }
   }
   
   /**
    * Get a copy of all application files for the supplied application.
    * 
    * @param app the application for which to get all application files,
    *    assumed not <code>null</code>.
    * @return all application files associated with the supplied application,
    *    never <code>null</code>, may be empty.
    * @throws PSServerException for any unspecified server error.
    * @throws PSAuthorizationException if the user is not authorized.
    * @throws PSAuthenticationFailedException if the user is not authenticated.
    * @throws PSValidationException for any validation errors.
    */
   private Collection<PSApplicationFile> getApplicationFiles(PSApplication app) 
      throws PSServerException, PSAuthorizationException, 
         PSAuthenticationFailedException, PSValidationException
   {
      try
      {
         PSObjectStore os = getObjectStore();
         
         Collection<PSApplicationFile> applicationFiles = 
            new ArrayList<PSApplicationFile>();
         for (final String file : os.getApplicationFiles(app.getRequestRoot()))
         {
            final PSApplicationFile psFile = new PSApplicationFile(new File(file));
            applicationFiles.add(os.loadApplicationFile(app, psFile));
         }
         
         return applicationFiles;
      }
      catch (PSNotLockedException e)
      {
         // should never happen
         throw new RuntimeException(e);
      }
   }

   public void rename(IPSReference ref, String name, Object data)
      throws PSModelException
   {
      if (ref == null)
      {
         throw new IllegalArgumentException("ref must not be null");
      }
      if (name == null || name.trim().length() == 0)
      {
         throw new IllegalArgumentException("name must not be null or empty");
      }
      name = name.trim();
      try
      {
         getObjectStore().renameApplication(ref.getName(), name);
         PSReference reference = (PSReference) ref;
         reference.setName(name);
         if (data != null)
         {
            PSApplication app = (PSApplication) data;
            app.setName(name);
         }
      }
      catch (PSServerException e)
      {
         throw new PSModelException(e);
      }
      catch (PSAuthorizationException e)
      {
         throw new PSModelException(e);
      }
      catch (PSAuthenticationFailedException e)
      {
         throw new PSModelException(e);
      }
      catch (PSLockedException e)
      {
         throw new PSModelException(e);
      }
      catch (PSNonUniqueException e)
      {
         throw new PSModelException(e);
      }
      catch (PSNotFoundException e)
      {
         throw new PSModelException(e);
      }
   }

   /**
    * Extends locks for the application with given refernces. If extending lock
    * fails for any reason it throws exception.
    * 
    * @param references app references to extend the lockd for, must not be
    * <code>null</code> or empty.
    * @throws PSMultiOperationException if operation fails. Each entry in the
    * object will be <code>null</code> for success and the exception for
    * failure.
    */
   public void extendLock(IPSReference[] references)
      throws PSMultiOperationException
   {
      if (references == null || references.length == 0)
      {
         throw new IllegalArgumentException(
            "references must not be null or empty");
      }
      PSApplication[] apps = (PSApplication[]) load(references, false, false);
      boolean error = false;
      Object[] res = new Object[references.length];
      for (int i = 0; i < apps.length; i++)
      {
         Exception ex = null;
         PSApplication app = apps[i];
         res[i] = null;
         try
         {
            getObjectStore().extendApplicationLock(app);
         }
         catch (PSServerException e)
         {
            ex = e;
         }
         catch (PSAuthorizationException e)
         {
            ex = e;
         }
         catch (PSAuthenticationFailedException e)
         {
            ex = e;
         }
         catch (PSLockedException e)
         {
            ex = e;
         }
         if (ex != null)
         {
            res[i] = ex;
            error = true;
         }
      }
      if (error)
      {
         throw new PSMultiOperationException(res, references);
      }
   }

   public boolean isLocked(IPSReference ref) throws PSModelException
   {
      if (ref == null)
         throw new IllegalArgumentException("ref must not be null");

      PSReference psref = (PSReference) ref;
      
      Properties props = new Properties();
      props.put("name", "");
      props.put("lockerName", "");
      props.put("lockerSession", "");

      Enumeration apps;
      Exception ex = null;
      try
      {
         apps = getObjectStore().getApplicationSummaries(props, false, 
            ref.getName());
         if (apps.hasMoreElements()) 
         {
            Properties appProps = (Properties) apps.nextElement();
            psref.setLockUserName(appProps.getProperty("lockerName"));
            psref.setLockSessionId(appProps.getProperty("lockerSession"));
         }
         
         if (apps.hasMoreElements())
         {
            // a bug, should only have gotten one
            RuntimeException re = new RuntimeException(
               "Multiple results found cataloging app: " + ref.getName());
            throw new PSModelException(re);
         }
         
         return (psref.getLockUserName() != null);
      }
      catch (PSAuthenticationFailedException e)
      {
         ex = e;
      }
      catch (PSServerException e)
      {
         ex = e;
      }

      throw new PSModelException(ex);
   }

   public void releaseLock(IPSReference[] refs)
      throws PSMultiOperationException
   {
      if (refs == null || refs.length == 0)
      {
         throw new IllegalArgumentException(
            "refs must not be null or empty");
      }
      List<IPSReference> persistedRefs = new ArrayList<IPSReference>();
      for (IPSReference reference : refs)
      {
         if(reference.isPersisted())
            persistedRefs.add(reference);
      }
      if(persistedRefs.isEmpty())
      {
         //Nothing to unlock, return
         return;
      }
      IPSReference[] references = persistedRefs.toArray(new IPSReference[0]);
      PSApplication[] apps = (PSApplication[]) load(references, false, false);
      boolean error = false;
      Object[] res = new Object[references.length];
      for (int i = 0; i < apps.length; i++)
      {
         PSApplication app = apps[i];
         Exception ex = null;
         res[i] = null;
         try
         {
            getObjectStore().releaseApplicationLock(app);
            PSProxyUtils.setLockInfo(new IPSReference[]
            {
               references[i]
            }, true);
         }
         catch (PSServerException e)
         {
            ex = e;
         }
         catch (PSAuthorizationException e)
         {
            ex = e;
         }
         catch (PSAuthenticationFailedException e)
         {
            ex = e;
         }
         catch (PSLockedException e)
         {
            ex = e;
         }
         if (ex != null)
         {
            res[i] = ex;
            error = true;
         }
      }
      if (error)
      {
         throw new PSMultiOperationException(res, refs);
      }
   }
   
   /**
    * Convenience method to access converter. 
    */
   private IPSXmlApplicationConverter getConverter()
   {
      return PSXmlApplicationConverterProvider.getInstance().getConverter();
   }
   
   /**
    * Gets the application summaries from the server and returns true if the
     * application is running on the server. (Checks for the isActive property
     * on the returned enumeration.)
    * @throws PSModelException if data retrieval from the server fails
     *
     *@returns true if the application is running (has been started) on the
     * server.
     */
   public boolean isAppRunningOnServer(IPSReference ref) throws PSModelException
   {
      Properties props = new Properties();
      props.put("name", "");
      props.put("isActive", "");

      boolean active = false;
      try
      {
         Enumeration e = getObjectStore().getApplicationSummaries(props);
         while (e.hasMoreElements())
         {
            final Properties pr = (Properties) e.nextElement();
            final String name = pr.getProperty("name");
            if (name.equals(ref.getName()))
            {
               String status = pr.getProperty("isActive");
               active = status.equals("yes");
               break;
            }
         }
      }
      catch (PSAuthenticationFailedException e)
      {
         throw new PSModelException(e);
      }
      catch (PSServerException e)
      {
         // this is fine
      }

      return active;
   }

   /**
    * Starts/stops the application. Bypasses the change.
    */
   public void toggleStatus(IPSReference ref, PSApplication cachedApp)
         throws PSModelException, PSAuthorizationException
   {
      if (!ref.isPersisted() && cachedApp == null)
      {
         throw new IllegalArgumentException(
               "If method is called for new application the object " +
               "must be provided");
      }
      final IPSReference[] refArray = new IPSReference[] {ref}; 
      boolean alreadyLocked = getCoreFactory().getLockHelper().isLockedToMe(ref);
      try
      {
         final boolean currentEnabled = checkAppIsEnabled(ref, cachedApp);
         if (ref.isPersisted())
         {
            final Object[] results = load(refArray, !alreadyLocked, false);
            final PSApplication loadedApp = (PSApplication) results[0];
            
            // if app failed to start or was abnormally terminated, the enabled
            // state of the application object may be out of synch with the actual
            // running state of the app.  Fix it before calling the toggle method.
            loadedApp.setEnabled(currentEnabled);
            loadedApp.setEnabled(!currentEnabled);
            save(refArray, new Object[] {loadedApp}, !alreadyLocked);
            if (cachedApp != null)
            {
               // correct revision of the cached version
               cachedApp.setRevision(
                     loadedApp.getRevisionHistory().getLatestRevision());
            }
         }
         if (cachedApp != null)
         {
            cachedApp.setEnabled(currentEnabled);
            cachedApp.setEnabled(!currentEnabled);
         }
      }
      catch (PSMultiOperationException e)
      {
         PSCmsModelProxy.logError(e);
         final Object resultE = e.getResults()[0];
         if (resultE instanceof PSModelException)
         {
            throw (PSModelException) resultE;
         }
         else if (resultE instanceof PSAuthorizationException)
         {
            throw (PSAuthorizationException) resultE;
         }
         else if (resultE instanceof Throwable)
         {
            throw new PSModelException((Throwable) resultE);
         }
         else
         {
            throw new PSModelException(e);
         }
      }
      catch (PSModelException e)
      {
         final Throwable th = e.getCause();
         if (th instanceof PSAuthenticationFailedExException
               || th instanceof PSAuthorizationException)
         {
            final PSException ex = (PSException) th;
            throw new PSAuthorizationException(
                  ex.getErrorCode(), ex.getErrorArguments());
         }
         throw e;
      }
   }

   /**
    * Indicates whether the app is really enabled. Requests server if necessary.
    */
   private boolean checkAppIsEnabled(IPSReference ref, PSApplication cachedApp)
         throws PSModelException
   {
      final boolean currentEnabled;
      if (ref.isPersisted())
      {
         currentEnabled = isAppRunningOnServer(ref);
      }
      else
      {
         currentEnabled = cachedApp.isEnabled();
      }
      return currentEnabled;
   }

   /**
    * The element corresponding to the provided application type (value
    * returned by {@link PSApplication#getApplicationType()}).
    */
   private XmlApplicationSubTypes findByAppType(final String appType)
   {
      final boolean isSystem =
         StringUtils.equals(appType, XmlApplicationSubTypes.SYSTEM.toString());
      return isSystem
            ? XmlApplicationSubTypes.SYSTEM
            : XmlApplicationSubTypes.USER;
   }

   /**
    * The element corresponding to the provided application type (value
    * returned by {@link PSApplication#getApplicationType()}).
    */
   private XmlApplicationSubTypes findByAppType(final PSApplicationType appType)
   {
      return findByAppType(appType.toString());
   }

   /**
    * Convenience method to access core factory.
    */
   private PSCoreFactory getCoreFactory()
   {
      return PSCoreFactory.getInstance();
   }
   
   /**
    * Used to create temporary ids for app copies. 10M was chosen arbitrarily as
    * a starting point. These ids only exist for the duration of the wb session.
    */
   private static AtomicInteger ms_tempId = new AtomicInteger(1000000);
}
