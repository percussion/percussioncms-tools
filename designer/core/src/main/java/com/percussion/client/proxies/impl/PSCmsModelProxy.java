/******************************************************************************
 *
 * [ PSCmsModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSErrorCodes;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.impl.PSReference;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.proxies.IPSCmsModelProxy;
import com.percussion.client.proxies.PSObjectFactory;
import com.percussion.client.proxies.PSProxyUtils;
import com.percussion.client.proxies.impl.PSWebServicesProxyConfig.Operation;
import com.percussion.client.proxies.impl.PSWebServicesProxyConfig.SetMethod;
import com.percussion.client.webservices.PSWebServicesConnection;
import com.percussion.security.xml.PSSecureXMLUtils;
import com.percussion.services.data.IPSCloneTuner;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.security.IPSAcl;
import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.data.PSAclImpl;
import com.percussion.webservices.common.PSObjectSummary;
import com.percussion.webservices.common.PSObjectSummaryLocked;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSErrorResultsFault;
import com.percussion.webservices.faults.PSErrorsFault;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.faults.PSLockFault;
import com.percussion.webservices.faults.PSNotAuthorizedFault;
import com.percussion.webservices.systemdesign.DeleteAclsRequest;
import com.percussion.webservices.systemdesign.LoadAclsRequest;
import com.percussion.webservices.systemdesign.SaveAclsRequest;
import com.percussion.webservices.systemdesign.SaveAclsResponsePermissions;
import com.percussion.webservices.systemdesign.SystemDesignSOAPStub;
import com.percussion.webservices.transformation.PSTransformationException;
import com.thoughtworks.xstream.XStream;
import org.apache.axis.AxisFault;
import org.apache.axis.client.Stub;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.rpc.ServiceException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Abstract base class for classes implementing the interface
 * {@link com.percussion.client.proxies.IPSCmsModelProxy}. All object model
 * independent functionality is implemeneted in this class.
 * 
 * @version 6.0
 * @created 03-Sep-2005 4:39:27 PM
 */
public abstract class PSCmsModelProxy implements IPSCmsModelProxy
{
   /**
    * Default ctor used by test implementations.
    */
   protected PSCmsModelProxy()
   {
   }

   /**
    * Ctor taking the primary type of the object the services are required. A
    * model proxy is per primary object type and hence has a constructor that
    * takes the primary object type.
    * 
    * @param objectType Enumeration value of <code>PSObjectType</code>. must
    * not be <code>null</code>.
    */
   public PSCmsModelProxy(IPSPrimaryObjectType objectType)
   {
      m_objectPrimaryType = objectType;
   }

   /**
    * The default implementation returns a meta data object that conforms to the
    * following table: <table>
    * <th>
    * <td>Method</td>
    * <td>Return</td>
    * </th>
    * <tr>
    * <td>isCacheable</td>
    * <td>true</td>
    * </tr>
    * </table>
    */
   public IModelInfo getMetaData()
   {
      return new IModelInfo()
      {
         /**
          * @return Always <code>true</code>.
          */
         public boolean isCacheable()
         {
            return true;
         }
      };
   }

   /**
    * This is implemented here for Hierarchal models where this method throws
    * the UnsuportedOperationException. See interface for supported
    * implementation details.
    * <p>
    * If this method is overridden, the derived class must call the
    * {@link #configureReferenceSecurity(IPSReference[])} method.
    * 
    * @throws PSMultiOperationException
    * 
    * @see IPSCmsModelProxy#create(PSObjectType, Collection, List)
    */
   @SuppressWarnings("unchecked")
   public IPSReference[] create(PSObjectType objType, Collection<String> names,
      List results) throws PSMultiOperationException, PSModelException
   {
      if (objType == null)
      {
         throw new IllegalArgumentException("objType must not be null");
      }
      if (names == null || names.size() == 0)
      {
         throw new IllegalArgumentException("names must not be null or empty"); //$NON-NLS-1$
      }
      if (results == null)
      {
         throw new IllegalArgumentException("results must not be null"); //$NON-NLS-1$
      }
      Operation operation = PSWebServicesProxyConfig.getInstance()
         .getOperation(m_objectPrimaryType.toString(), "create");
      if (operation == null)
      {
         throw new RuntimeException(
            "create() method must be configured in the config file"); //$NON-NLS-1$
      }
      Throwable ex = null;
      Class responseClass = operation.getResponse().loadClass();
      try
      {
         boolean redo = false;
         do
         {
            redo = false;
            try
            {
               Stub bindingObj = getSoapBinding(METHOD.CREATE);
               Method create = bindingObj.getClass().getMethod(
                  operation.getMethodName(), new Class[]
                  {
                     String[].class
                  });
               Object createdObjects = create.invoke(bindingObj, new Object[]
               {
                  names.toArray(new String[0])
               });
               Object[] result = (Object[]) PSProxyUtils.convert(responseClass,
                  createdObjects);
               postCreate(objType, result);
               for (int i = 0; i < result.length; i++)
                  results.add(result[i]);
      
               IPSReference[] refs = PSObjectFactory.objectToReference(result,
                  (IPSPrimaryObjectType) objType.getPrimaryType(), true);
               configureReferenceSecurity(refs);
               return refs;
            }
            catch (InvocationTargetException e)
            {
               try
               {
                  if (e.getTargetException() instanceof PSInvalidSessionFault)
                  {
                     PSCoreFactory.getInstance().reconnect();
                     redo = true;
                  }
                  else
                  {
                     ex = PSProxyUtils.extractMultiOperationException(
                        (IPSReference[]) null, e, METHOD.CREATE);
                  }
               }
               catch (Exception e1)
               {
                  ex = e1;
               }
            }
         } while (redo);
      }
      catch (IllegalAccessException e)
      {
         ex = e;
      }
      catch (SecurityException e)
      {
         ex = e;
      }
      catch (NoSuchMethodException e)
      {
         ex = e;
      }
      catch (IllegalArgumentException e)
      {
         ex = e;
      }
      catch (MalformedURLException e)
      {
         ex = e;
      }
      catch (PSTransformationException e)
      {
         ex = e;
      }
      catch (ServiceException e)
      {
         ex = e;
      }
      
      if (ex != null)
         processAndThrowException(names.size(), ex);
      
      // will never get here
      return null;
   }


   /**
    * Derived classes can override this method to modify the resulting objects
    * to match their object type. The base implementation does nothing.
    * 
    * @param objType The type of the newly created objects. The framework will
    * never supply <code>null</code>.
    * 
    * @param results The newly created objects. The framework will never
    * supply <code>null</code> or empty and each entry is valid.
    */
   @SuppressWarnings("unused")
   protected void postCreate(PSObjectType objType, Object[] results)
   {
   }
   
   /**
    * This method is generally called after a reference for a newly created
    * object has been created. It sets the lock info and permissions. All
    * permissions are given.
    * 
    * @param refs The newly creasted references. Never <code>null</code>. Each
    * entry must be an instance of <code>PSReference</code>. Otherwise, the
    * caller must handle these configurations themselves.
    */
   static void configureReferenceSecurity(IPSReference[] refs)
   {
      if (null == refs)
      {
         throw new IllegalArgumentException("refs cannot be null");  
      }
      PSProxyUtils.setLockInfo(refs, false);
      for (IPSReference ref : refs)
      {
         ((PSReference) ref).setPermissions(new int[] {
               PSPermissions.READ.getOrdinal(),
               PSPermissions.UPDATE.getOrdinal(),
               PSPermissions.DELETE.getOrdinal() });
      }
   }

   /**
    * @param ex Never <code>null</code>.
    */
   protected static void logError(Throwable ex)
   {
      if (null == ex)
      {
         throw new IllegalArgumentException("ex cannot be null");  
      }
      String msg = "";
      if (ex instanceof AxisFault)
      {
         msg = ((AxisFault) ex).toString();
         ms_log.error(msg, ex);
      }
      else if (ex instanceof PSMultiOperationException)
      {
         Object[] results = ((PSMultiOperationException) ex).getResults();
         int count = 0;
         for (Object o : results)
         {
            if (o instanceof Throwable)
               count++;
         }
         
         ms_log.error(MessageFormat.format(
               "Multi-operation exception occurred: {0} successes, {1} failures. Stack traces follow",
               results.length - count, count));
         for (Object o : results)
         {
            if (o instanceof Throwable)
            {
               if (o instanceof AxisFault)
               {
                  msg = ((AxisFault) o).toString();
                  ms_log.error(msg, (AxisFault) o);
               }
               else
                  ms_log.error(o);
            }
         }
      }
      else
         ms_log.error(ex);
   }

   // see the interface
   public IPSReference[] create(Object[] sourceObjects, String[] names, 
         List<Object> results)
      throws PSMultiOperationException, PSModelException
   {
      if (sourceObjects == null || sourceObjects.length == 0)
      {
         throw new IllegalArgumentException(
            "sourceObjects must not be null or empty");
      }
      if (results == null)
      {
         throw new IllegalArgumentException("results must not be null");
      }
      
      results.clear();
      for (int i = 0; i < sourceObjects.length; i++)
      {
         if (!(sourceObjects[i] instanceof IPSCloneTuner))
         {
            throw new UnsupportedOperationException(
               "this object does not implement IPSCloneTuner interface");
         }
      }

      // Returned results: references for good ones, exceptions for bad ones
      // if there are any exceptions it whill be passed in multi-operation
      // exception, otherwise it is returned as result.
      // Exceptions in this array should correspond to null elements in
      // results list
      final Object[] refs = new Object[sourceObjects.length];

      for (int i = 0; i < sourceObjects.length; i++)
      {
         Object obj = clone(sourceObjects[i]);
         results.add(obj);
         refs[i] = PSObjectFactory.objectToReference(obj, m_objectPrimaryType, false);
      }

      assignNewNames(names, results, refs);
      assignNewIds(sourceObjects, results, refs);
      postCreate(sourceObjects, results, refs);
      maybeThrowMultiOperationException(refs);
      
      final IPSReference[] resultRefs = new IPSReference[refs.length];
      for (int i = 0; i < refs.length; i++)
      {
         resultRefs[i] = (IPSReference) refs[i];
      }
      return resultRefs;
   }

   /**
    * Finish processing of objects in {@link #create(Object[], String[], List)}.
    * If processing of any elements fails corresponding elements of
    * <code>results</code> and <code>refs</code> parameters
    * All 3 passed in collections/arrays should have the same size.
    *
    * @param sourceObjects the original objects to be cloned.
    * Never <code>null</code> or empty, elements are not <code>null</code>.
    * @param results the clones created from <code>sourceObjects</code>.
    * Never <code>null</code> or empty.
    * Contains <code>nulls</code> for elements processing of which is failed.
    * @param refs references to the cloned objects. Contains exceptions
    * for the elements processing of which is failed.
    * Never <code>null</code> or empty.
    * 
    * @throws PSModelException If a problem occurs that prevents providing
    * information about the individuals. For example, the server can no longer
    * be reached.
    */
   @SuppressWarnings("unused")
   protected void postCreate(Object[] sourceObjects, List<Object> results,
         Object[] refs) throws PSModelException
   {
      assert results.size() == sourceObjects.length;
      assert refs.length == sourceObjects.length;
   }

   /**
    * Throws {@link PSMultiOperationException} if any of the objects
    * in the array are exceptions.
    * @param results the array of objects to check.
    * @throws PSMultiOperationException thrown if any exception was found in the
    * provided array. Contains the provided array.
    */
   private void maybeThrowMultiOperationException(Object[] results)
         throws PSMultiOperationException
   {
      for (final Object obj : results)
      {
         if (obj instanceof Throwable)
         {
            throw new PSMultiOperationException(results);
         }
      }
   }

   /**
    * Generates new ids and assigns them names to the newly cloned result
    * objects, references for these objects.
    * 
    * @param sourceObjects the original objects to be cloned.
    * @param results the clones to be returned.
    * @param refs the references to the clones. On input must contain only
    * references.
    * On output the the references are updated with new information,
    * some elements of the array with can be replaced with exceptions
    * if processing of the particular element failed.
    * @throws PSModelException If a problem occurs that prevents providing
    * information about the individuals. For example, the server can no longer
    * be reached.
    */
   private void assignNewIds(Object[] sourceObjects, List<Object> results,
         final Object[] refs) throws PSModelException
   {
      Object[] newRefs;
      try
      {
         final List<String> newNames = new ArrayList<String>();
         for (final Object ref : refs)
         {
            newNames.add(((IPSReference) ref).getName());
         }
         newRefs = create(((IPSReference) refs[0]).getObjectType(), newNames,
               new ArrayList()/* we ignore this anyway */);
      }
      catch (PSMultiOperationException e)
      {
         newRefs = e.getResults();
      }
      assert newRefs.length == sourceObjects.length;

      // set object ids/exceptions
      for (int i = 0; i < newRefs.length; i++)
      {
         if (newRefs[i] instanceof IPSReference)
         {
            final IPSReference newRef = (IPSReference) newRefs[i];
            ((IPSCloneTuner) results.get(i)).tuneClone(newRef.getId().longValue());
            // regenerate refs from the tuned clones
            refs[i] = PSObjectFactory.objectToReference(
                  results.get(i), m_objectPrimaryType, true);
         }
         else
         {
            results.set(i, null);
            refs[i] = newRefs[i];
         }
      }
   }

   /**
    * Assigns the names to the newly cloned result objects, references for these
    * objects.
    *
    * @param names the names provided to {@link #create(Object[], String[], List)}.
    * @param results the newly cloned objects. Never <code>null</code>.
    * @param refs new references. At this stage must contain only valid refs.
    */
   private void assignNewNames(String[] names, List<Object> results,
         final Object[] refs)
   {
      final List<String> existingNames = new ArrayList<String>();
      try
      {
         final Collection<IPSReference> handles = getModel().catalog();
         for (IPSReference ref : handles)
            existingNames.add(ref.getName());
      }
      catch (PSModelException e)
      {
         //ignore it for now, will be caught by the server if a problem
      }

      for (int i = 0; i < refs.length; i++)
      {
         PSReference ref = (PSReference) refs[i];
         String name;
         if (names == null || StringUtils.isBlank(names[i]))
         {
            name = PSCoreUtils.createCopyName(
                  ref.getName(), -1, existingNames);
         }
         else
         {
            name = names[i];
         }
         ref.setName(name);
         PSObjectFactory.setName(results.get(i), name);
      }
   }

   // see interface for details
   public Collection<IPSReference> catalog() throws PSModelException
   {
      Operation operation = PSWebServicesProxyConfig.getInstance()
         .getOperation(m_objectPrimaryType.toString(), "catalog");
      if (operation == null)
      {
         // throw new PSModelException(PSErrorCodes.RAW);
         return new ArrayList<IPSReference>(0);
      }
      
      Throwable ex = null;
      Class requestClass = operation.getRequest().loadClass();
      try
      {
         boolean redo = false;
         do
         {
            redo = false;
            try
            {
               Stub bindingObj = getSoapBinding(METHOD.CATALOG);
               Object requestObj = requestClass.newInstance();
               Method find = bindingObj.getClass().getMethod(
                  operation.getMethodName(), new Class[]
                  {
                     requestClass
                  });
               PSObjectSummary[] results = (PSObjectSummary[]) find.invoke(
                  bindingObj, new Object[]
                  {
                     requestObj
                  });
               
               // Should we guess the type from result.getType() string?
               PSObjectType type = PSObjectTypeFactory.getType(
                  m_objectPrimaryType, m_objectSecondaryType);
               return objectSummaryArrayToReferenceCollection(results, type);
            }
            catch (InvocationTargetException e)
            {
               try
               {
                  if (e.getTargetException() instanceof PSInvalidSessionFault)
                  {
                     PSCoreFactory.getInstance().reconnect();
                     redo = true;
                  }
                  else
                     ex = e.getTargetException();
               }
               catch (Exception e1)
               {
                  ex = e1;
               }
            }
         } while (redo);
      }
      catch (InstantiationException e)
      {
         ex = e;
      }
      catch (IllegalAccessException e)
      {
         ex = e;
      }
      catch (SecurityException e)
      {
         ex = e;
      }
      catch (NoSuchMethodException e)
      {
         ex = e;
      }
      catch (IllegalArgumentException e)
      {
         ex = e;
      }
      catch (MalformedURLException e)
      {
         ex = e;
      }
      catch (ServiceException e)
      {
         ex = e;
      }
      
      if (ex != null)
         processAndThrowException(ex);
      
      // will never get here

      return new ArrayList<IPSReference>();
   }

   /**
    * Helper method to convert an array of {@link PSObjectSummary} objects to a
    * colletion of {@link IPSReference} objects.
    * 
    * @param results array of {@link PSObjectSummary} objects to convert, may be
    * <code>null</code> or empty.
    * @param objType objectType to set for each reference in the collection,
    * must not be <code>null</code>
    * @return collection of {@link IPSReference} objects, never
    * <code>null</code> may be empty.
    * @throws PSModelException if could not convert the summary object to
    * appropriate object type.
    */
   protected Collection<IPSReference> objectSummaryArrayToReferenceCollection(
      PSObjectSummary[] results, PSObjectType objType) throws PSModelException
   {
      if (results == null)
      {
         throw new IllegalArgumentException("results must not be null");
      }
      if (objType == null)
      {
         throw new IllegalArgumentException("objType must not be null");
      }
      Collection<IPSReference> coll = new ArrayList<IPSReference>();
      if (results != null && results.length > 0)
      {
         for (PSObjectSummary result : results)
         {
            PSReference ref = new PSReference();
            ref.setDescription(result.getDescription());
            ref.setId(new PSDesignGuid(result.getId()));
            ref.setLabelKey(result.getLabel());
            ref.setName(result.getName());
            ref.setObjectType(objType);
            ref.setPersisted();
            PSObjectSummaryLocked lockInfo = result.getLocked();
            if (lockInfo != null)
            {
               ref.setLockUserName(lockInfo.getLocker());
               ref.setLockSessionId(lockInfo.getSession());
            }
            int[] permissions = result.getPermissions();
            if (permissions == null)
               permissions = new int[0];
            ref.setPermissions(permissions);
            coll.add(ref);
         }
      }
      return coll;
   }

   // see interface for details
   public Object[] load(IPSReference[] reference, boolean lock,
      boolean overrideLock) throws PSMultiOperationException, PSModelException
   {
      if (reference == null || reference.length == 0)
      {
         return null;
      }

      long[] ids = new long[reference.length];
      for (int i = 0; i < reference.length; i++)
      {
         IPSReference ref = reference[i];
         ids[i] = PSProxyUtils.getDesignId(ref.getId());
      }

      Operation operation = PSWebServicesProxyConfig.getInstance()
         .getOperation(m_objectPrimaryType.toString(), "load");
      if (operation == null)
      {
         throw new PSMultiOperationException(PSErrorCodes.RAW);
      }
      Throwable ex = null;
      Class requestClass = operation.getRequest().loadClass();
      Class responseClass = operation.getResponse().loadClass();
      try
      {
         boolean redo = false;
         do
         {
            redo = false;
            try
            {
               Stub bindingObj = getSoapBinding(METHOD.LOAD);
               Object requestObj = requestClass.newInstance();
      
               SetMethod[] setMethods = operation.getRequest().getSetMethods();
               Method setId = requestClass.getMethod(setMethods[0].getName(),
                  new Class[]
                  {
                     long[].class
                  });
      
               setId.invoke(requestObj, new Object[]
               {
                  ids
               });
      
               Method setLock = requestClass.getMethod(setMethods[1].getName(),
                  setMethods[1].getParamClasses());
               setLock.invoke(requestObj, new Object[]
               {
                  Boolean.valueOf(lock)
               });
      
               Method setOverideLock = requestClass.getMethod(
                  setMethods[2].getName(), setMethods[2].getParamClasses());
               setOverideLock.invoke(requestObj, new Object[]
               {
                  Boolean.valueOf(overrideLock)
               });
      
               Method load = bindingObj.getClass().getMethod(
                  operation.getMethodName(), new Class[]
                  {
                     requestClass
                  });
               Object[] results = (Object[]) load.invoke(bindingObj, new Object[]
               {
                  requestObj
               });
               if (lock)
               {
                  // Update the lockinfo
                  for (IPSReference ref : reference)
                  {
                     ((PSReference) ref).setLockSessionId(PSCoreFactory.getInstance()
                        .getClientSessionId());
                     ((PSReference) ref).setLockUserName(PSCoreFactory.getInstance()
                        .getConnectionInfo().getUserid());
                  }
               }
               return (Object[]) PSProxyUtils.convert(responseClass, results);
            }
            catch (InvocationTargetException e)
            {
               try
               {
                  if (e.getTargetException() instanceof PSInvalidSessionFault)
                  {
                     PSCoreFactory.getInstance().reconnect();
                     redo = true;
                  }
                  else
                  {
                     ex = PSProxyUtils.extractMultiOperationException(reference,
                        e, METHOD.LOAD);
                  }
               }
               catch (Exception e1)
               {
                  ex = e1;
               }
            }
         } while (redo);
      }
      catch (InstantiationException e)
      {
         ex = e;
      }
      catch (IllegalAccessException e)
      {
         ex = e;
      }
      catch (SecurityException e)
      {
         ex = e;
      }
      catch (NoSuchMethodException e)
      {
         ex = e;
      }
      catch (IllegalArgumentException e)
      {
         ex = e;
      }
      catch (MalformedURLException e)
      {
         ex = e;
      }
      catch (PSTransformationException e)
      {
         ex = e;
      }
      catch (ServiceException e)
      {
         ex = e;
      }
      
      if (ex != null)
      {
         if (ex instanceof PSMultiOperationException)
         {
            Object[] errors = ((PSMultiOperationException) ex).getResults();
            String sessionId = PSCoreFactory.getInstance().getClientSessionId();
            String userId = PSCoreFactory.getInstance().getConnectionInfo()
                  .getUserid(); 
            for (int i = 0; i < errors.length; i++)
            {
               if (lock && !(errors[i] instanceof Throwable))
               {
                  ((PSReference) reference[i]).setLockSessionId(sessionId);
                  ((PSReference) reference[i]).setLockUserName(userId);
               }
            }
         }

         processAndThrowException(reference.length, ex);
      }
      
      // will never get here
      return new Object[0];
   }

   // see interface for details
   public void delete(IPSReference[] reference)
      throws PSMultiOperationException, PSModelException
   {
      long[] ids = new long[reference.length];
      for (int i = 0; i < reference.length; i++)
      {
         IPSReference ref = reference[i];
         ids[i] = PSProxyUtils.getDesignId(ref.getId());
      }

      Operation operation = PSWebServicesProxyConfig.getInstance()
         .getOperation(m_objectPrimaryType.toString(), "delete");
      if (operation == null)
      {
         throw new PSMultiOperationException(PSErrorCodes.RAW);
      }
      Throwable ex = null;
      Class requestClass = operation.getRequest().loadClass();
      try
      {
         boolean redo = false;
         do
         {
            redo = false;
            try
            {
               Stub bindingObj = getSoapBinding(METHOD.DELETE);
               Object requestObj = requestClass.newInstance();
               SetMethod[] setMethods = operation.getRequest().getSetMethods();
               Method setId = requestClass.getMethod(setMethods[0].getName(),
                  new Class[]
                  {
                     new long[0].getClass()
                  });
      
               setId.invoke(requestObj, new Object[]
               {
                  ids
               });
               Method delete = bindingObj.getClass().getMethod(
                  operation.getMethodName(), new Class[]
                  {
                     requestClass
                  });
               delete.invoke(bindingObj, new Object[]
               {
                  requestObj
               });
            }
            catch (InvocationTargetException e)
            {
               try
               {
                  if (e.getTargetException() instanceof PSInvalidSessionFault)
                  {
                     PSCoreFactory.getInstance().reconnect();
                     redo = true;
                  }
                  else
                  {
                     ex = PSProxyUtils.extractMultiOperationException(reference,
                        e, METHOD.DELETE);
                  }
               }
               catch (Exception e1)
               {
                  ex = e1;
               }
            }
         } while (redo);
      }
      catch (InstantiationException e)
      {
         ex = e;
      }
      catch (IllegalAccessException e)
      {
         ex = e;
      }
      catch (SecurityException e)
      {
         ex = e;
      }
      catch (NoSuchMethodException e)
      {
         ex = e;
      }
      catch (IllegalArgumentException e)
      {
         ex = e;
      }
      catch (MalformedURLException e)
      {
         ex = e;
      }
      catch (ServiceException e)
      {
         ex = e;
      }
      
      if (ex != null)
         processAndThrowException(reference.length, ex);
   }

   public void save(IPSReference[] refs, Object[] data, boolean releaseLock)
      throws PSMultiOperationException, PSModelException
   {
      if (refs == null || refs.length == 0)
      {
         throw new IllegalArgumentException("refs must not be null or empty");
      }
      if (data == null || data.length != refs.length)
      {
         throw new IllegalArgumentException("data array must not be null and "
            + "must match with the reference array size");
      }

      Operation operation = PSWebServicesProxyConfig.getInstance()
         .getOperation(m_objectPrimaryType.toString(), "save");
      if (operation == null)
      {
         throw new PSMultiOperationException(PSErrorCodes.RAW);
      }
      Throwable ex = null;
      Class requestClass = operation.getRequest().loadClass();
      try
      {
         boolean redo = false;
         do
         {
            redo = false;
            try
            {
               Stub bindingObj = getSoapBinding(METHOD.SAVE);
               Object requestObj = requestClass.newInstance();
               SetMethod[] setMethods = operation.getRequest().getSetMethods();
               Method setObject = requestClass.getMethod(setMethods[0].getName(),
                  setMethods[0].getParamClasses());
      
               Object[] params = (Object[]) PSProxyUtils.convert(setMethods[0]
                  .getParamClasses()[0], data);
      
               Object newParams = Array.newInstance(
                  setMethods[0].getParamClasses()[0].getComponentType(),
                  params.length);
      
               System.arraycopy(params, 0, newParams, 0, params.length);
      
               setObject.invoke(requestObj, newParams);
      
               Method setReleaseLock = requestClass.getMethod(
                  setMethods[1].getName(), setMethods[1].getParamClasses());
               setReleaseLock.invoke(requestObj, new Object[]
               {
                  Boolean.valueOf(releaseLock)
               });
      
               Method save = bindingObj.getClass().getMethod(
                  operation.getMethodName(), new Class[]
                  {
                     requestClass
                  });
               save.invoke(bindingObj, new Object[]
               {
                  requestObj
               });
      
               for (IPSReference ref : refs)
               {
                  PSReference psref = (PSReference)ref;
                  psref.setPersisted();
                  psref.setLabelKey(getModel().getLabelKey(ref));
                  psref.setDescription(getModel().getDescription(ref));
                  psref.setObjectType(getModel().getObjectType(ref));
               }
               if (releaseLock)
                  PSProxyUtils.setLockInfo(refs, true);
            }
            catch (InvocationTargetException e)
            {
               try
               {
                  if (e.getTargetException() instanceof PSInvalidSessionFault)
                  {
                     PSCoreFactory.getInstance().reconnect();
                     redo = true;
                  }
                  else
                  {
                     ex = PSProxyUtils.extractMultiOperationException(refs, e,
                        METHOD.SAVE);
                  }
               }
               catch (Exception e1)
               {
                  ex = e1;
               }
            }
         } while (redo);
      }
      catch (InstantiationException e)
      {
         ex = e;
      }
      catch (IllegalAccessException e)
      {
         ex = e;
      }
      catch (SecurityException e)
      {
         ex = e;
      }
      catch (NoSuchMethodException e)
      {
         ex = e;
      }
      catch (IllegalArgumentException e)
      {
         ex = e;
      }
      catch (PSTransformationException e)
      {
         ex = e;
      }
      catch (MalformedURLException e)
      {
         ex = e;
      }
      catch (ServiceException e)
      {
         ex = e;
      }
      
      if (ex != null)
      {
         if (ex instanceof PSMultiOperationException)
         {
            Object[] errors = ((PSMultiOperationException) ex).getResults();
            List<IPSReference> validRefs = new ArrayList<IPSReference>();
            for (int i = 0; i < errors.length; i++)
            {
               if (!(errors[i] instanceof Throwable))
               {
                  ((PSReference) refs[i]).setPersisted();
                  validRefs.add(refs[i]);
               }
            }
               
            if (releaseLock)
            {
               PSProxyUtils.setLockInfo(validRefs
                     .toArray(new IPSReference[validRefs.size()]), true);
            }
         }
         
         processAndThrowException(refs.length, ex);
      }
   }

   /**
    * See {@link IPSCmsModelProxy#releaseLock(IPSReference[])} for 
    * documentation.
    * If a <code>PSModelException</code> is thrown the implementation 
    * guarantees that non of the supplied objects were unlocked.
    */
   @SuppressWarnings("unused")
   public void releaseLock(IPSReference[] references)
      throws PSMultiOperationException, PSModelException
   {
      if (references == null || references.length == 0)
         return;

      Exception ex = null;
      try
      {
         boolean redo = false;
         do
         {
            redo = false;
            try
            {
               SystemDesignSOAPStub bindingObj = 
                  PSProxyUtils.getSystemDesignStub();
               long[] requestObj = new long[references.length];
               for (int i = 0; i < references.length; i++)
               {
                  requestObj[i] = PSProxyUtils.getDesignId(
                     references[i].getId());
               }
               bindingObj.releaseLocks(requestObj);
               PSProxyUtils.setLockInfo(references, true);
            }
            catch (PSInvalidSessionFault e)
            {
               try
               {
                  PSCoreFactory.getInstance().reconnect();
                  redo = true;
               }
               catch (Exception e1)
               {
                  ex = e1;
               }
            }
         } while (redo);
      }
      catch (MalformedURLException e)
      {
         ex = e;
      }
      catch (ServiceException e)
      {
         ex = e;
      }
      catch (PSContractViolationFault e)
      {
         ex = e;
      }
      catch (RemoteException e)
      {
         ex = e;
      }
      
      if (ex != null)
         processAndThrowException(ex);
   }

   /**
    * Does nothing. The derived class must override this to clear up any cache.
    * 
    * @see com.percussion.client.proxies.IPSCmsModelProxy#flush(com.percussion.client.IPSReference)
    */
   @SuppressWarnings("unused")
   public void flush(IPSReference ref)
   {
   }

   // see interface
   public Object[] loadAcl(IPSReference[] refs, boolean lock)
      throws PSMultiOperationException, PSModelException
   {
      if (refs == null || refs.length == 0)
      {
         throw new IllegalArgumentException("refs must not be null or empty");
      }
      long[] objectIds = new long[refs.length];
      for (int i = 0; i < refs.length; i++)
      {
         IPSReference ref = refs[i];
         objectIds[i] = PSProxyUtils.getDesignId(ref.getId());
      }
      Throwable ex = null;
      try
      {
         boolean redo = false;
         do
         {
            redo = false;
            try
            {
               SystemDesignSOAPStub binding = PSProxyUtils.getSystemDesignStub();
               LoadAclsRequest request = new LoadAclsRequest(objectIds, lock, false);
               com.percussion.webservices.system.PSAclImpl[] acls = binding
                  .loadAcls(request);
               return (Object[]) PSProxyUtils.convert(PSAclImpl[].class, acls);
            }
            catch (PSInvalidSessionFault e)
            {
               try
               {
                  PSCoreFactory.getInstance().reconnect();
                  redo = true;
               }
               catch (Exception e1)
               {
                  ex = e1;
               }
            }
         } while (redo);
      }
      catch (MalformedURLException e)
      {
         ex = e;
      }
      catch (ServiceException e)
      {
         ex = e;
      }
      catch (PSContractViolationFault e)
      {
         ex = e;
      }
      catch (PSNotAuthorizedFault e)
      {
         ex = e;
      }
      catch (PSErrorResultsFault e)
      {
         ex = PSProxyUtils.extractMultiOperationException(refs, METHOD.LOAD, e);
      }
      catch (PSLockFault e)
      {
         ex = PSProxyUtils.convertFault(e, METHOD.LOAD.toString(), 
            refs[0].getObjectType().getPrimaryType().toString(), 
            refs[0].getName());
      }
      catch (RemoteException e)
      {
         ex = e;
      }
      catch (PSTransformationException e)
      {
         ex = e;
      }
      
      if (ex != null)
         processAndThrowException(refs.length, ex);
      
      // will never get here
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.IPSCmsModelProxy#saveAcl(com.percussion.client.IPSReference[],
    * com.percussion.services.security.IPSAcl[], boolean)
    */
   public void saveAcl(IPSReference[] ref, IPSAcl[] acl, boolean releaseLock)
      throws PSMultiOperationException, PSModelException
   {
      if (ref == null)
      {
         throw new IllegalArgumentException("ref must not be null");
      }
      if (acl == null)
      {
         throw new IllegalArgumentException("acl must not be null");
      }
      if (ref.length != acl.length)
      {
         throw new IllegalArgumentException(
            "ref collection size and acl collection size must be same");
      }
      SystemDesignSOAPStub binding;
      Throwable ex = null;
      try
      {
         boolean redo = false;
         do
         {
            redo = false;
            try
            {
               binding = PSProxyUtils.getSystemDesignStub();
               SaveAclsRequest req = new SaveAclsRequest();
               com.percussion.webservices.system.PSAclImpl[] wsAcl = 
                  (com.percussion.webservices.system.PSAclImpl[]) PSProxyUtils.convert(
                     com.percussion.webservices.system.PSAclImpl[].class,
                     acl);
               req.setPSAclImpl(wsAcl);
               req.setRelease(releaseLock);
      
               SaveAclsResponsePermissions[] permissions = binding.saveAcls(req);
               for (int i = 0; i < permissions.length; i++)
               {
                  ((PSReference) ref[i]).setPermissions(permissions[i]
                     .getPermission());
               }
            }
            catch (PSInvalidSessionFault e)
            {
               try
               {
                  PSCoreFactory.getInstance().reconnect();
                  redo = true;
               }
               catch (Exception e1)
               {
                  ex = e1;
               }
            }
         } while (redo);
      }
      catch (MalformedURLException e)
      {
         ex = e;
      }
      catch (ServiceException e)
      {
         ex = e;
      }
      catch (PSTransformationException e)
      {
         ex = e;
      }
      catch (PSContractViolationFault e)
      {
         ex = e;
      }
      catch (PSNotAuthorizedFault e)
      {
         ex = e;
      }
      catch (PSErrorsFault e)
      {
         ex = PSProxyUtils.extractMultiOperationException(null, METHOD.SAVE, e);
      }
      catch (PSLockFault e)
      {
         ex = PSProxyUtils.convertFault(e, METHOD.SAVE.toString(), 
            ref[0].getObjectType().getPrimaryType().toString(), 
            ref[0].getName());
      }
      catch (RemoteException e)
      {
         ex = e;
      }
      
      if (ex != null)
         processAndThrowException(ref.length, ex);
   }
   
   /**
    * See {@link PSProxyUtils#processAndThrowException(Throwable, Logger)}
    * for documentation.
    */
   protected static void processAndThrowException(Throwable ex)
      throws PSModelException
   {
      PSProxyUtils.processAndThrowException(ex, ms_log);
   }

   /**
    * See {@link PSProxyUtils#processAndThrowException(int, Throwable, Logger)}
    * for documentation.
    */
   protected static void processAndThrowException(int refCount, Throwable ex)
      throws PSMultiOperationException, PSModelException
   {
      PSProxyUtils.processAndThrowException(refCount, ex, ms_log);
   }

   // see interface
   public void deleteAcl(IPSReference[] owners)
      throws PSMultiOperationException, PSModelException
   {
      if (owners == null || owners.length == 0)
      {
         throw new IllegalArgumentException("owners must not be null or empty");
      }
      Throwable ex = null;
      try
      {
         boolean redo = false;
         do
         {
            redo = false;
            try
            {
               // Load ACLs to get the ACL ids. Do we need an acl delete interface
               // with object refs???
               Object[] acls = loadAcl(owners, false);
               long[] ids = new long[acls.length];
               for (int i = 0; i < acls.length; i++)
                  ids[i] = ((IPSAcl) acls[i]).getId();
      
               SystemDesignSOAPStub binding = PSProxyUtils.getSystemDesignStub();
               DeleteAclsRequest request = new DeleteAclsRequest();
               request.setId(ids);
               binding.deleteAcls(request);
            }
            catch (PSInvalidSessionFault e)
            {
               try
               {
                  PSCoreFactory.getInstance().reconnect();
                  redo = true;
               }
               catch (Exception e1)
               {
                  ex = e1;
               }
            }
         } while (redo);
      }
      catch (MalformedURLException e)
      {
         ex = e;
      }
      catch (ServiceException e)
      {
         ex = e;
      }
      catch (PSContractViolationFault e)
      {
         ex = e;
      }
      catch (PSNotAuthorizedFault e)
      {
         ex = e;
      }
      catch (PSErrorResultsFault e)
      {
         ex = PSProxyUtils.extractMultiOperationException(null, METHOD.DELETE,
            e);
      }
      catch (PSErrorsFault e)
      {
         ex = PSProxyUtils.extractMultiOperationException(null, METHOD.DELETE,
            e);
      }
      catch (PSLockFault e)
      {
         ex = PSProxyUtils.convertFault(e, METHOD.DELETE.toString(), 
            owners[0].getObjectType().getPrimaryType().toString(), 
            owners[0].getName());
      }
      catch (RemoteException e)
      {
         ex = e;
      }
      
      if (ex != null)
         processAndThrowException(owners.length, ex);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.IPSCmsModelProxy#releaseAclLoc(Long[])
    */
   @SuppressWarnings("unused")
   public void releaseAclLock(Long[] aclIds) throws PSMultiOperationException,
      PSModelException
   {
      if (aclIds == null || aclIds.length == 0)
         return;

      long[] ids = new long[aclIds.length];
      for (int i = 0; i < ids.length; i++)
         ids[i] = aclIds[i];
      Exception ex = null;
      try
      {
         boolean redo = false;
         do
         {
            redo = false;
            try
            {
               SystemDesignSOAPStub bindingObj = 
                  PSProxyUtils.getSystemDesignStub();
               bindingObj.releaseLocks(ids);
            }
            catch (PSInvalidSessionFault e)
            {
               try
               {
                  PSCoreFactory.getInstance().reconnect();
                  redo = true;
               }
               catch (Exception e1)
               {
                  ex = e1;
               }
            }
         } while (redo);
      }
      catch (MalformedURLException e)
      {
         ex = e;
      }
      catch (ServiceException e)
      {
         ex = e;
      }
      catch (PSContractViolationFault e)
      {
         ex = e;
      }
      catch (RemoteException e)
      {
         ex = e;
      }
      
      if (ex != null)
         processAndThrowException(ex);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.IPSCmsModelProxy#isLocked(com.percussion.client.IPSReference)
    */
   public boolean isLocked(IPSReference ref) throws PSModelException
   {
      if (ref == null)
         throw new IllegalArgumentException("ref must not be null");

      Exception ex = null;
      try
      {
         boolean redo = false;
         do
         {
            redo = false;
            try
            {
               SystemDesignSOAPStub bindingObj = PSProxyUtils.getSystemDesignStub();
               long[] requestObj = new long[]
               {
                  PSProxyUtils.getDesignId(ref.getId())
               };
               PSObjectSummary[] results = bindingObj.isLocked(requestObj);
               PSObjectSummaryLocked locked = (results[0] == null) ? null
                  : results[0].getLocked();
               if (locked != null)
               {
                  ((PSReference) ref).setLockSessionId(locked.getSession());
                  ((PSReference) ref).setLockUserName(locked.getLocker());
                  return true;
               }
            }
            catch (PSInvalidSessionFault e)
            {
               try
               {
                  PSCoreFactory.getInstance().reconnect();
                  redo = true;
               }
               catch (Exception e1)
               {
                  ex = e1;
               }
            }
         } while (redo);
      }
      catch (MalformedURLException e)
      {
         ex = e;
      }
      catch (ServiceException e)
      {
         ex = e;
      }
      catch (PSContractViolationFault e)
      {
         ex = e;
      }
      catch (RemoteException e)
      {
         ex = e;
      }
      
      if (ex != null)
         processAndThrowException(ex);

      return false;
   }

   /* (non-Javadoc)
    * @see com.percussion.client.proxies.IPSCmsModelProxy#rename(IPSReference, 
    *    String, Object)
    */
   public void rename(IPSReference ref, String name, Object data)
      throws PSModelException
   {
      if (name == null || name.length() == 0)
         throw new IllegalArgumentException("name must not be null or empty");

      if (ref == null)
         throw new IllegalArgumentException("ref cannot be null");

      String currentName = ref.getName();
      boolean alreadyLocked = 
         PSCoreFactory.getInstance().getLockHelper().isLockedToMe(ref);
      try
      {
         Object obj = data;
         if (ref.isPersisted())
         {
            Object[] objects = load(new IPSReference[] { ref }, 
               !alreadyLocked, false);
            obj = objects[0];
            
            renameLocal(ref, name, obj);
         }
         
         // we have to rename the local copy as well as the copy being sent to
         // the server
         renameLocal(ref, name, data);
         
         if (ref.isPersisted())
            save(new IPSReference[] { ref }, new Object[] { obj }, 
               !alreadyLocked);
      }
      catch (Exception e)
      {
         // we must revert local renames on any error
         ((PSReference) ref).setName(currentName);
         renameData(data, currentName);
         
         if (!alreadyLocked)
         {
            // we must unlock if specifically locked for this operation
            try
            {
               releaseLock(new IPSReference[] { ref });
            }
            catch (PSMultiOperationException ex)
            {
               // ignore, we want to return the exception why the rename failed
            }
         }
         
         logError(e);
         throw new PSModelException(e);
      }
   }

   /* (non-Javadoc)
    * @see com.percussion.client.proxies.IPSCmsModelProxy#renameLocal(
    *    IPSReference, String, Object)
    */
   public void renameLocal(IPSReference ref, String name, Object data)
   {
      if (name == null || name.length() == 0)
         throw new IllegalArgumentException("name must not be null or empty");

      if (ref == null)
         throw new IllegalArgumentException("ref cannot be null");

      ((PSReference) ref).setName(name);
      renameData(data, name);
   }
   
   /**
    * Renames the supplied data object with the supplied name.
    * 
    * @param data the data object to rename, may be <code>null</code> in which
    *    case this does nothing. 
    * @param name the new name, assumed not <code>null</code> or empty.
    */
   private void renameData(Object data, String name)
   {
      if (data == null)
         return;
      
      Operation operation = PSWebServicesProxyConfig.getInstance().getOperation(
         m_objectPrimaryType.toString(), "rename");
      Class objectClass = operation.loadClass();
      
      Throwable ex = null;
      try
      {
         Method m = objectClass.getMethod("setName", 
            new Class[] { String.class });
         m.invoke(data, new Object[] { name });
      }
      catch (SecurityException e)
      {
         ex = e;
      }
      catch (NoSuchMethodException e)
      {
         ex = e;
      }
      catch (IllegalArgumentException e)
      {
         ex = e;
      }
      catch (IllegalAccessException e)
      {
         ex = e;
      }
      catch (InvocationTargetException e)
      {
         ex = e.getTargetException();
      }
      
      if (ex != null)
         logError(ex);
   }

   /**
    * Equivalent to {@link PSCoreFactory#getWebServicesConnection()} except that
    * this will never return a <code>null</code>.
    * 
    * @return webservices connection, never <code>null</code>.
    */
   protected PSWebServicesConnection getWebServicesConnection()
   {
      PSWebServicesConnection conn = PSCoreFactory.getInstance()
         .getWebServicesConnection();
      if (conn == null)
         throw new RuntimeException("Connection not initialized");
      return conn;
   }

   /**
    * Is the type supplied supported by this proxy? Subclass normally overrides
    * this.
    * 
    * @param type object type to test, must not be <code>null</code>.
    * @return always <code>true</code>.
    */
   @SuppressWarnings("unused")
   public boolean isTypeSupported(PSObjectType type)
   {
      return true;
   }

   /**
    * Access method for the object type which was used in the ctor.
    * 
    * @return supported object type never <code>null</code>.
    */
   protected IPSPrimaryObjectType getObjectPrimaryType()
   {
      return m_objectPrimaryType;
   }

   /**
    * Get the CMS model this proxy is associated with.
    * 
    * @return associated CMS model, never <code>null</code>.
    */
   protected IPSCmsModel getModel()
   {
      try
      {
         return PSCoreFactory.getInstance()
            .getModel((Enum) m_objectPrimaryType);
      }
      catch (PSModelException e)
      {
         throw new RuntimeException(e.getLocalizedMessage());
      }
   }

   /**
    * Makes an exact clone of the suplied source. The fills only the serialzable
    * properties defined in the .betwixt files for the object. The subclass must
    * do additional changes to the object to make it a valid clone or new copy.
    * 
    * @param source source object to clone, must not be <code>null</code>.
    * @return cloned object whose every serializable property is the same as
    * that of the source.
    */
   public Object clone(Object source)
   {
      if (source == null)
      {
         throw new IllegalArgumentException("source must not be null");
      }
      XStream xs = PSSecureXMLUtils.getSecuredXStream();
      Object obj = xs.fromXML(xs.toXML(source));
      return obj;
   }

   /**
    * Derived class should return the appropriate SOAP Stub object.
    * 
    * @param method
    * 
    * @return must not be <code>null</code>
    */
   @SuppressWarnings("unused")
   protected Stub getSoapBinding(@SuppressWarnings("unused")
   METHOD method) throws MalformedURLException, ServiceException
   {
      throw new UnsupportedOperationException("Base class must override this"); //$NON-NLS-1$
   }

   /**
    * The primary object type for which this proxy is instantiated, Never
    * <code>null</code>. Initialized in the ctor.
    */
   protected IPSPrimaryObjectType m_objectPrimaryType;

   /**
    * The secondary object type for which this proxy is intended to work for,
    * May be <code>null</code>. Initialized in the ctor.
    */
   protected Enum m_objectSecondaryType;

   /**
    * Logger object to log any errors.
    */
   private static Logger ms_log = LogManager.getLogger(PSCmsModelProxy.class);
}
