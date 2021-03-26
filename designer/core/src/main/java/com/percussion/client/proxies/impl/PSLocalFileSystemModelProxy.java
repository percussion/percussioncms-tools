/******************************************************************************
 *
 * [ PSLocalFileSystemModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.IPSReference;
import com.percussion.client.PSErrorCodes;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSMultiOperationStrategy;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes.FileSubTypes;
import com.percussion.client.models.PSLockException;
import com.percussion.client.proxies.IPSCmsModelProxy;
import com.percussion.design.objectstore.PSNotLockedException;
import com.percussion.services.security.IPSAcl;
import com.percussion.services.system.data.PSMimeContentAdapter;
import org.apache.commons.collections.list.TransformedList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.percussion.client.proxies.impl.PSLocalFileSystemHierarchyNodeRef.OBJECT_TYPE_FILE;

/**
 * Provides access to the local file system. Operates on
 * {@link PSMimeContentAdapter} objects whose name is a file name of the file.
 * 
 * @author Andriy Palamarchuk
 */
public class PSLocalFileSystemModelProxy implements IPSCmsModelProxy
{
   /**
    * The default implementation returns a meta data object that conforms to the 
    * following table:
    * <table>
    *   <th>
    *      <td>Method</td>
    *      <td>Return</td>
    *   </th>
    *   <tr>
    *      <td>isCacheable</td>
    *      <td>false</td>
    *   </tr>
    * </table>    
    */
   public IModelInfo getMetaData()
   {
      return new IModelInfo()
      {
         /**
          * @return Always <code>false</code>.
          */
         public boolean isCacheable()
         {
            return false;
         }
      };
   }

   public Collection<IPSReference> catalog()
   {
      throw new UnsupportedOperationException("This model handles a hierarchy");
   }
   
   @SuppressWarnings("unused")
   public IPSReference[] create(PSObjectType objType,
         Collection<String> names, List results)
   {
      throw new UnsupportedOperationException("This model handles a hierarchy");
   }

   @SuppressWarnings("unused")
   public IPSReference[] create(Object[] sourceObjects, String[] names, 
         List<Object> results)
   {
      throw new UnsupportedOperationException("This model handles a hierarchy");
   }

   /**
    * Returns files from the references. The lock parameters are ignored.
    */
   @SuppressWarnings("unchecked")
   public Object[] load(IPSReference[] references,
         @SuppressWarnings("unused") boolean lockForEdit,
         @SuppressWarnings("unused") boolean overrideLock)
         throws PSMultiOperationException
   {
      final PSMultiOperationStrategy transformer =
         new PSMultiOperationStrategy(new PSLoadOperation());
      final List results =
            TransformedList.decorate(new ArrayList(),transformer);
      results.addAll(Arrays.asList(references));
      return transformer.validateResult(results.toArray());
   }

   // see base
   public void rename(IPSReference ref, String name, Object data)
         throws PSModelException
   {
      final PSLocalFileSystemHierarchyNodeRef fileRef =
            (PSLocalFileSystemHierarchyNodeRef) ref;
      final File fileFrom = fileRef.getFile();
      final File fileTo = new File(fileFrom.getParent(), name);
      if (fileTo.exists())
      {
         throw new PSModelException(
               PSErrorCodes.DUPLICATE_NAME,
               new Object[] {fileTo.getAbsolutePath(), ref.getObjectType()});
      }
      
      final boolean moved = fileFrom.renameTo(fileTo);
      if (!moved)
      {
         throw new PSModelException(PSErrorCodes.SAVE_FAILED,
               new Object[] {fileTo.getAbsolutePath()});
      }
      renameLocal(ref, name, data);
   }

   // see base
   public void renameLocal(IPSReference ref, String name, Object data)
   {
      final PSLocalFileSystemHierarchyNodeRef fileRef =
            (PSLocalFileSystemHierarchyNodeRef) ref;
      final File fileFrom = fileRef.getFile();
      final File fileTo = new File(fileFrom.getParent(), name);
      fileRef.setFile(fileTo);
      if (data != null)
      {
         final PSMimeContentAdapter content = (PSMimeContentAdapter) data;
         content.setName(fileTo.getAbsolutePath());
      }
   }

   // see base
   @SuppressWarnings("unchecked")
   public void save(IPSReference[] references, Object[] data,
         @SuppressWarnings("unused") boolean releaseLock)
         throws PSMultiOperationException
   {
      final PSMultiOperationStrategy transformer =
         new PSMultiOperationStrategy(new PSSaveOperation(data));
      final List results =
            TransformedList.decorate(new ArrayList(), transformer);
      results.addAll(Arrays.asList(references));
      transformer.validateResult(results.toArray());
      for (IPSReference ref : references)
      {
         ((PSLocalFileSystemHierarchyNodeRef) ref).setPersisted(true);
      }
   }

   // see base
   @SuppressWarnings("unchecked")
   public void delete(IPSReference[] references) throws PSMultiOperationException
   {
      final PSMultiOperationStrategy transformer =
         new PSMultiOperationStrategy(new PSDeleteOperation());
      final List results =
            TransformedList.decorate(new ArrayList(),transformer);
      results.addAll(Arrays.asList(references));
      transformer.validateResult(results.toArray());
   }

   /**
    * Always <code>false</code>.
    */
   @SuppressWarnings("unused")
   public boolean isLocked(IPSReference ref)
   {
      return false;
   }

   /**
    * Does nothing.
    */
   @SuppressWarnings("unused")
   public void releaseLock(IPSReference[] refs) {}

   /**
    * Does nothing.
    */
   @SuppressWarnings("unused")
   public void flush(IPSReference ref) {}

   /**
    * Returns <code>null</code> ACL for each ref.
    */
   @SuppressWarnings("unused")
   public IPSAcl[] loadAcl(IPSReference[] refs, boolean lock)
   {
      return new IPSAcl[refs.length];
   }

   /**
    * Does nothing.
    */
   @SuppressWarnings("unused")
   public void saveAcl(IPSReference[] ref, IPSAcl[] acl, boolean releaseLock)
   {
   }
   
   /**
    * File loading operation.
    */
   private static final class PSLoadOperation extends
      PSMultiOperationExceptionStrategy
   {
      @Override
      protected Object doTransform(Object obj) throws IOException
      {
         mi_ref = (PSLocalFileSystemHierarchyNodeRef) obj;
         if (mi_ref.getObjectType().getSecondaryType().equals(
            FileSubTypes.FOLDER))
         {
            return null;
         }
         final byte[] buf;
         if ( mi_ref.isPersisted())
            buf = FileUtils.readFileToByteArray(mi_ref.getFile());
         else
            buf = new byte[0];
         final PSMimeContentAdapter content = new PSMimeContentAdapter();
         content.setContent(new ByteArrayInputStream(buf));
         content.setName(mi_ref.getFile().getAbsolutePath());
         return content;
      }

      /**
       * Constructs PSLockException for this operation.
       */
      @SuppressWarnings("unused")
      @Override
      protected PSLockException constructLockException(PSNotLockedException e)
      {
         return new PSLockException("load", mi_ref.getObjectType()
            .getPrimaryType().name(), mi_ref.getName());
      }
      
      private PSLocalFileSystemHierarchyNodeRef mi_ref;
   }

   /**
    * File deletion.
    */
   private static final class PSDeleteOperation extends
      PSMultiOperationExceptionStrategy
   {
      @Override
      protected Object doTransform(Object obj)
            throws IOException, PSModelException
      {
         mi_ref = (PSLocalFileSystemHierarchyNodeRef) obj;
         final File file = mi_ref.getFile();
         if (file.isDirectory())
         {
            FileUtils.deleteDirectory(file);
         }
         else
         {
            file.delete();
         }
         if (file.exists())
         {
            throw new PSModelException(PSErrorCodes.DELETE_FAILED,
                  new Object[] {file.getAbsolutePath()});
         }
         return null;
      }

      /**
       * Constructs PSLockException for this operation.
       */
      @SuppressWarnings("unused")
      @Override
      protected PSLockException constructLockException(PSNotLockedException e)
      {
         return new PSLockException("delete", mi_ref.getObjectType()
            .getPrimaryType().name(), mi_ref.getName());
      }
      
      private PSLocalFileSystemHierarchyNodeRef mi_ref;
   }

   /**
    * Saves file. 
    */
   private static final class PSSaveOperation
         extends PSMultiOperationExceptionStrategy
   {
      private PSSaveOperation(Object[] data)
      {
         super();
         mi_data = data;
      }

      @Override
      protected Object doTransform(Object obj)
            throws PSModelException, IOException
      {
         mi_refIdx++;
         mi_ref = (PSLocalFileSystemHierarchyNodeRef) obj;
         assert mi_ref.getObjectType().equals(OBJECT_TYPE_FILE)
               || mi_data[mi_refIdx] == null;

         final File file = mi_ref.getFile();
         if (mi_ref.getObjectType().getSecondaryType().equals(FileSubTypes.FOLDER))
         {
            file.mkdirs();
            if (!file.exists() || !file.isDirectory())
            {
               throw new PSModelException(PSErrorCodes.SAVE_FAILED,
                     new Object[] {file.getAbsolutePath()});
            }
         }
         else
         {
            final PSMimeContentAdapter content =
                  (PSMimeContentAdapter) mi_data[mi_refIdx];
            final FileOutputStream out = new FileOutputStream(file);
            try
            {
               IOUtils.copy(content.getContent(), out);
            }
            finally
            {
               out.close();
            }
            if (!file.exists() || !file.isFile())
            {
               throw new PSModelException(PSErrorCodes.SAVE_FAILED,
                     new Object[] {file.getAbsolutePath()});
            }
         }
         return null;
      }

      @SuppressWarnings("unused")
      @Override
      protected PSLockException constructLockException(PSNotLockedException e)
      {
         return new PSLockException("save", mi_ref.getObjectType()
               .getPrimaryType().name(), mi_ref.getName());
      }

      /**
       * Currently processed reference.
       */
      private PSLocalFileSystemHierarchyNodeRef mi_ref;

      /**
       * Currently processed reference index.
       */
      private int mi_refIdx = -1;

      /**
       * Actual data to save.
       */
      private final Object[] mi_data;
   }

   /* (non-Javadoc)
    * @see com.percussion.client.proxies.IPSCmsModelProxy#deleteAcl(com.percussion.client.IPSReference[])
    */
   @SuppressWarnings("unused")
   public void deleteAcl(IPSReference[] refs) throws PSMultiOperationException
   {
      //does nothing
   }

   /* (non-Javadoc)
    * @see com.percussion.client.proxies.IPSCmsModelProxy#releaseAclLock(java.lang.Long[])
    */
   @SuppressWarnings("unused")
   public void releaseAclLock(Long[] aclIds) throws PSMultiOperationException
   {
      //does nothing
   }
}
