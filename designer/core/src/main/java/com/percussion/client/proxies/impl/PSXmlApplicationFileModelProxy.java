/******************************************************************************
 *
 * [ PSXmlApplicationFileModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSMultiOperationStrategy;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.PSObjectTypes.FileSubTypes;
import com.percussion.client.PSObjectTypes.XmlApplicationSubTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.client.models.PSLockException;
import com.percussion.client.proxies.IPSCmsModelProxy;
import com.percussion.conn.PSServerException;
import com.percussion.content.IPSMimeContent;
import com.percussion.content.PSMimeContentAdapter;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSApplicationFile;
import com.percussion.design.objectstore.PSLockedException;
import com.percussion.design.objectstore.PSNotLockedException;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.design.objectstore.PSValidationException;
import com.percussion.error.PSIllegalStateException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import org.apache.commons.collections.list.TransformedList;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Formatter;
import java.util.List;

/**
 * Provides CRUD and cataloging services for the object type
 * {@link PSObjectTypes#XML_APPLICATION_FILE}. Uses base class implementation
 * whenever possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSCmsModelProxy
 * 
 * @version 6.0
 * @created 03-Sep-2005 4:39:27 PM
 */
public class PSXmlApplicationFileModelProxy extends PSLegacyModelProxy
{
   /**
    * Ctor. Invokes base class version with the object type
    * {@link PSObjectTypes#XML_APPLICATION_FILE} and for primary type.
    */
   public PSXmlApplicationFileModelProxy()
   {
      super(PSObjectTypes.XML_APPLICATION_FILE);
   }

   /**
    * Ctor. Invokes base class version.
    */
   public PSXmlApplicationFileModelProxy(IPSPrimaryObjectType primaryType)
   {
      super(primaryType);
   }

   // see base class
   @SuppressWarnings("unused") //exception
   public Collection<IPSReference> catalog()
      throws PSModelException
   {
      throw new UnsupportedOperationException("This model handles a hierarchy");
   }

   // see base class
   @SuppressWarnings("unused")
   public IPSReference[] create(PSObjectType objType, Collection<String> names,
      List results)
      throws PSMultiOperationException, PSModelException
   {
      throw new UnsupportedOperationException("This model handles a hierarchy");
   }

   /**
    * @see IPSCmsModelProxy#create(Object[], String[], List)
    */
   @SuppressWarnings("unused")
   public IPSReference[] create(Object[] sourceObjects, String[] names,
         List results)
   {
      throw new UnsupportedOperationException("This model handles a hierarchy");
   }

   // see base class
   @SuppressWarnings("unchecked")
   public Object[] load(IPSReference[] references, boolean lockForEdit,
      boolean overrideLock) throws PSMultiOperationException
   {
      final PSMultiOperationStrategy transformer = new PSMultiOperationStrategy(
         new PSLoadOperation(this, getObjectStore(), lockForEdit, overrideLock));
      final List results = TransformedList.decorate(new ArrayList(),
         transformer);
      results.addAll(Arrays.asList(references));
      return transformer.validateResult(results.toArray());
   }

   // see base class
   @SuppressWarnings("unchecked")
   public void delete(IPSReference[] references)
      throws PSMultiOperationException
   {
      final PSMultiOperationStrategy transformer = new PSMultiOperationStrategy(
         new PSDeleteOperation(getObjectStore()));
      final List results = TransformedList.decorate(new ArrayList(),
         transformer);
      results.addAll(Arrays.asList(references));
      transformer.validateResult(results.toArray());
   }

   @SuppressWarnings("unchecked")
   public void save(final IPSReference[] refs, final Object[] data,
      final boolean releaseLock) throws PSMultiOperationException
   {
      assert refs.length == data.length;

      final PSMultiOperationStrategy transformer = new PSMultiOperationStrategy(
         new PSSaveOperation(releaseLock, data, getObjectStore()));
      final List resultRefs = TransformedList.decorate(new ArrayList(),
         transformer);
      resultRefs.addAll(Arrays.asList(refs));
      transformer.validateResult(resultRefs.toArray());
   }

   // see base class
   public void rename(IPSReference ref, String name, Object data)
      throws PSModelException
   {
      final PSXmlApplicationFileHierarchyRef fileRef = (PSXmlApplicationFileHierarchyRef) ref;
      assert fileRef.getParent() != null;
      try
      {
         final PSApplicationFile file = new PSApplicationFile(new File(fileRef
            .getFilePath()), fileRef.isContainer());
         getObjectStore().renameApplicationFile(fileRef.getApplication(), file,
            name, false,true);
         if (data != null)
         {
            ((PSMimeContentAdapter) data).setName(name);
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
      catch (PSNotLockedException e)
      {
         throw new PSModelException(e);
      }
      catch (PSValidationException e)
      {
         throw new PSModelException(e);
      }
      fileRef.unlock();
      fileRef.setName(name);
      fileRef.lock();
   }

   // see base class
   @SuppressWarnings("unchecked")
   public void releaseLock(IPSReference[] references)
      throws PSMultiOperationException
   {
      final PSMultiOperationExceptionStrategy operation = new PSReleaseLockOperation(
         getObjectStore());
      final PSMultiOperationStrategy transformer = new PSMultiOperationStrategy(
         operation);
      final List results = TransformedList.decorate(new ArrayList(),
         transformer);
      results.addAll(Arrays.asList(references));
      transformer.validateResult(results.toArray());
   }

   // see base class
   public boolean isLocked(IPSReference ref) throws PSModelException
   {
      // application file is considered to be locked if the file node
      // was locked and the file application is locked on the server
      final PSXmlApplicationFileHierarchyRef fileRef =
         (PSXmlApplicationFileHierarchyRef) ref;
        return fileRef.isLocked();
   }

   /**
    * Checks whether the application owning the file is locked.
    * Updates file reference locking information with information from the
    * application.
    *
    * @param fileRef the reference to check whether its application is locked.
    * Assumed not <code>null</code>.
    * @return <code>true</code> if the application is locked.
    * @throws PSModelException on underlying model failure.
    */
   boolean checkApplicationLock(final PSXmlApplicationFileHierarchyRef fileRef)
         throws PSModelException
   {
      final PSApplication application = fileRef.getApplication();
      final PSReference appRef =
         new PSReference(application.getName(), null, null,
               new PSObjectType(PSObjectTypes.XML_APPLICATION,
                     XmlApplicationSubTypes.USER), null);

      // transfer lock data from the application ref to the file ref
      final boolean applicationLocked =
            new PSXmlApplicationModelProxy().isLocked(appRef);
      fileRef.setLockUserName(appRef.getLockUserName());
      fileRef.setLockSessionId(appRef.getLockSessionId());
      return applicationLocked;
   }

   private static final class PSReleaseLockOperation extends
      PSMultiOperationExceptionStrategy
   {
      private PSReleaseLockOperation(PSObjectStore objectStore)
      {
         super();
         m_objectStore = objectStore;
      }

      @Override
      protected Object doTransform(Object obj) throws Exception
      {
         m_ref = (PSXmlApplicationFileHierarchyRef) obj;
         final boolean wasLocked = m_ref.isLocked();
         // unlock the ref first to see whether the tree still remains locked
         // because of other refs in the tree
         m_ref.unlock();
         if (!m_ref.isTreeLocked())
         {
            boolean operationFailed = true;
            try
            {
               m_objectStore.releaseApplicationLock(m_ref.getApplication());
               operationFailed = false;
            }
            finally
            {
               if (operationFailed && wasLocked)
               {
                  m_ref.lock();
               }
            }
         }
         return null;
      }

      @SuppressWarnings("unused")
      @Override
      protected PSLockException constructLockException(PSNotLockedException e)
      {
         return new PSLockException("releaseLock", m_ref.getObjectType()
            .getPrimaryType().name(), m_ref.getName());
      }

      /**
       * Reference to object store, initialized in the ctor, never
       * <code>null</code> after that.
       */
      private final PSObjectStore m_objectStore;

      /**
       * Currently processed reference.
       */
      private PSXmlApplicationFileHierarchyRef m_ref;
   }

   private static final class PSSaveOperation extends
      PSMultiOperationExceptionStrategy
   {
      private PSSaveOperation(boolean lock, Object[] data,
         final PSObjectStore objectStore)
      {
         super();
         m_lock = lock;
         m_data = data;
         m_objectStore = objectStore;
      }

      @Override
      protected Object doTransform(Object obj) throws Exception
      {
         m_refIdx++;
         m_ref = (PSXmlApplicationFileHierarchyRef) obj;
         assert m_ref.getParent() != null;
         PSApplicationFile file = null;
         if(m_ref.getObjectType().getSecondaryType().equals(FileSubTypes.FOLDER))
         {
            file = new PSApplicationFile(new File(m_ref.getFilePath()), true);
         }
         else
         {
            com.percussion.services.system.data.PSMimeContentAdapter data =
               (com.percussion.services.system.data.PSMimeContentAdapter)
               m_data[m_refIdx];
            PSMimeContentAdapter buf = new PSMimeContentAdapter(
                  data.getContent(), 
                  data.getMimeType(), 
                  data.getTransferEncoding(), 
                  data.getCharacterEncoding(), 
                  data.getContentLength());
            file = new PSApplicationFile(buf, new File(m_ref.getFilePath()));
         }
         final boolean wasLocked = m_ref.isLocked();
         if (m_lock)
         {
            m_ref.unlock();
         }
         boolean operationFailed = true;
         try
         {
            String beforeHash = null;
            final PSApplicationFile file2 = new PSApplicationFile(new File(m_ref
                    .getFilePath()));
            if(StringUtils.isNotEmpty(m_ref.getHash())) {
               IPSMimeContent contentBefore = m_objectStore.loadApplicationFile(
                       m_ref.getApplication(), file2).getContent();
               if (!(contentBefore instanceof
                       com.percussion.services.system.data.PSMimeContentAdapter)) {
                  com.percussion.services.system.data.PSMimeContentAdapter buf =
                          new com.percussion.services.system.data.PSMimeContentAdapter();
                  if (StringUtils.isNotBlank(contentBefore.getCharEncoding()))
                  {
                     buf.setCharacterEncoding(contentBefore.getCharEncoding());
                  }
                  buf.setMimeType(contentBefore.getMimeType());
                  buf.setContent(contentBefore.getContent());
                  beforeHash = getSha1((ByteArrayInputStream) buf.getContent(), buf.getCharacterEncoding());
               } else {
                  beforeHash = getSha1((ByteArrayInputStream) contentBefore.getContent(), contentBefore.getCharEncoding());
               }

               if(!m_ref.getHash().equals(beforeHash)) {
                  throw new RuntimeException("The file you are trying to edit has been modified, "
                          + "please copy your changes, reopen the file and merge your changes manually.");
               }
            }

            m_objectStore.saveApplicationFile(m_ref.getApplication(), file,
                    true, m_lock && !m_ref.isTreeLocked(), true);
            final PSApplicationFile file3 = new PSApplicationFile(new File(m_ref
                    .getFilePath()));
            IPSMimeContent contentAfter = m_objectStore.loadApplicationFile(
                    m_ref.getApplication(), file3).getContent();
            if (!(contentAfter instanceof
                    com.percussion.services.system.data.PSMimeContentAdapter)) {
               com.percussion.services.system.data.PSMimeContentAdapter buf =
                       new com.percussion.services.system.data.PSMimeContentAdapter();
               if (StringUtils.isNotBlank(contentAfter.getCharEncoding()))
               {
                  buf.setCharacterEncoding(contentAfter.getCharEncoding());
               }
               buf.setMimeType(contentAfter.getMimeType());
               buf.setContent(contentAfter.getContent());
               m_ref.setHash(getSha1((ByteArrayInputStream) buf.getContent(), buf.getCharacterEncoding()));
            } else {
               m_ref.setHash(getSha1((ByteArrayInputStream) contentAfter.getContent(), contentAfter.getCharEncoding()));
            }
            operationFailed = false;
         }
         finally
         {
            if (operationFailed && wasLocked)
            {
               m_ref.lock();
            }
         }
         m_ref.setPersisted();
         return null;
      }

      @SuppressWarnings("unused")
      @Override
      protected PSLockException constructLockException(PSNotLockedException e)
      {
         return new PSLockException("save", m_ref.getObjectType()
            .getPrimaryType().name(), m_ref.getName());
      }

      /**
       * Currently processed reference.
       */
      private PSXmlApplicationFileHierarchyRef m_ref;

      /**
       * Currently processed reference index.
       */
      private int m_refIdx = -1;

      /**
       * Whether to lock the nodes.
       */
      private final boolean m_lock;

      /**
       * Actual data to save.
       */
      private final Object[] m_data;

      /**
       * Reference to object store, initialized in the ctor
       */
      private final PSObjectStore m_objectStore;
   }

   private static final class PSDeleteOperation extends
      PSMultiOperationExceptionStrategy
   {
      private PSDeleteOperation(PSObjectStore objectStore)
      {
         m_objectStore = objectStore;
      }

      @Override
      protected Object doTransform(Object obj) throws PSServerException,
         PSAuthorizationException, PSAuthenticationFailedException,
         PSNotLockedException, PSValidationException
      {
         m_ref = (PSXmlApplicationFileHierarchyRef) obj;
         assert m_ref.getParent() != null;
         final PSApplicationFile file = new PSApplicationFile(new File(m_ref
            .getFilePath()));
         final boolean wasLocked = m_ref.isLocked();
         m_ref.unlock();
         boolean operationFailed = true;
         try
         {
            m_objectStore.removeApplicationFile(m_ref.getApplication(), file,
               !m_ref.isTreeLocked(),true);
            operationFailed = false;
         }
         finally
         {
            if (operationFailed && wasLocked)
            {
               m_ref.lock();
            }
         }
         return null;
      }

     @SuppressWarnings("unused")
     @Override
      protected PSLockException constructLockException(PSNotLockedException e)
      {
         return new PSLockException("delete", m_ref.getObjectType()
            .getPrimaryType().name(), m_ref.getName());
      }

      /**
       * Reference to object store, initialized in the ctor, never
       * <code>null</code> after that.
       */
      private PSObjectStore m_objectStore;

      /**
       * Currently processed reference.
       */
      private PSXmlApplicationFileHierarchyRef m_ref;
   }

   /**
    * Resource file loading operation.
    */
   private static final class PSLoadOperation extends
      PSMultiOperationExceptionStrategy
   {
      private PSLoadOperation(
            PSXmlApplicationFileModelProxy model,
            PSObjectStore objectStore,
            final boolean lockForEdit, final boolean overrideLock)
      {
         m_model = model;
         m_objectStore = objectStore;
         m_lockForEdit = lockForEdit;
         m_overrideLock = overrideLock;
      }

      @Override
      protected Object doTransform(Object obj) throws PSServerException,
         PSAuthorizationException, PSAuthenticationFailedException,
         PSNotLockedException, PSValidationException, PSIllegalStateException,
         PSLockedException, PSModelException
      {
         m_ref = (PSXmlApplicationFileHierarchyRef) obj;
         if (m_ref.getObjectType().getSecondaryType().equals(
            PSObjectTypes.FileSubTypes.FOLDER))
         {
            return null;
         }
         assert m_ref.getParent() != null;
         final PSApplicationFile file = new PSApplicationFile(new File(m_ref
            .getFilePath()));
         final IPSMimeContent content = m_objectStore.loadApplicationFile(
            m_ref.getApplication(), file).getContent();
         if (m_lockForEdit)
         {
            m_ref.lock();
            // check whether the application is locked also updates
            // file reference with lock information
            if (!m_model.isLocked(m_ref))
            {
               throw new PSNotLockedException(IPSObjectStoreErrors.LOCK_NOT_HELD,
                     new Object[] {m_ref.getApplication().getName()});
            }
         }

         //convert to consistent file object type
         if (!(content instanceof 
               com.percussion.services.system.data.PSMimeContentAdapter))
         {
            com.percussion.services.system.data.PSMimeContentAdapter buf = 
               new com.percussion.services.system.data.PSMimeContentAdapter();
            if (StringUtils.isNotBlank(content.getCharEncoding()))
            {
               buf.setCharacterEncoding(content.getCharEncoding());
            }
            buf.setMimeType(content.getMimeType());
            buf.setContent(content.getContent());
            m_ref.setHash(getSha1((ByteArrayInputStream) buf.getContent(), buf.getCharacterEncoding()));
            return buf;
         }
         m_ref.setHash(getSha1((ByteArrayInputStream) content.getContent(), content.getCharEncoding()));
         return content;
      }

      /**
       * Constructs PSLockException for this operation.
       */
      @SuppressWarnings("unused")
      @Override
      protected PSLockException constructLockException(PSNotLockedException e)
      {
         return new PSLockException("load", m_ref.getObjectType()
            .getPrimaryType().name(), m_ref.getName());
      }

      /**
       * Reference to object store, initialized in the ctor, never
       * <code>null</code> after that.
       */
      private PSObjectStore m_objectStore;

      /**
       * Currently processed reference.
       */
      private PSXmlApplicationFileHierarchyRef m_ref;

      /**
       * Whether to lock the loaded objects for edit.
       */
      final boolean m_lockForEdit;

      /**
       * Whether to override the lock.
       */
      final boolean m_overrideLock;

      /**
       * Model using this operation.
       */
      private final PSXmlApplicationFileModelProxy m_model;
   }
   /**
    * Creates an SHA-1 string of the supplied input stream.
    * @param inputStream Assumed not <code>null</code>.
    * @return The 40 char string representing the SHA-1.
    */
   private static String getSha1(ByteArrayInputStream inputStream, String encoding)
   {
      try
      {
         String s = IOUtils.toString(inputStream, encoding);
         inputStream.reset();
         MessageDigest md = MessageDigest.getInstance("SHA-1");
         byte[] sha1hash = new byte[40];
         md.update(s.getBytes(encoding), 0, s.length());
         sha1hash = md.digest();
         Formatter formatter = new Formatter();
         for (byte b : md.digest(sha1hash))
         {
            formatter.format("%02x", b);
         }
         return formatter.toString();
      }
      catch (NoSuchAlgorithmException e)
      {
         //shouldn't happen
         throw new RuntimeException(e);
      }
      catch (UnsupportedEncodingException e)
      {
         //shouldn't happen
         throw new RuntimeException(e);
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Object type - file. Singleton instance.
    */
   public static final PSObjectType OBJECT_TYPE_FILE = new PSObjectType(
      PSObjectTypes.XML_APPLICATION_FILE, PSObjectTypes.FileSubTypes.FILE);

   /**
    * Object type - folder. Singleton instance.
    */
   public static final PSObjectType OBJECT_TYPE_FOLDER = new PSObjectType(
      PSObjectTypes.XML_APPLICATION_FILE, PSObjectTypes.FileSubTypes.FOLDER);

   /**
    * Object type - folder. Singleton instance.
    */
   public static List<Integer> ms_hashList = new ArrayList<Integer>();
}
