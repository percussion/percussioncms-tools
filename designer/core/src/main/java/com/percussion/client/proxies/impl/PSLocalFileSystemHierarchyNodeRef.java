/******************************************************************************
 *
 * [ PSLocalFileSystemHierarchyNodeRef.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.IPSHierarchyNodeRef;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSHierarchyManager;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Reference to a {@link File} for {@link PSLocalFileSystemHierarchyModelProxy}.
 * Unmodifiable.
 * @author Andriy Palamarchuk
 */
public class PSLocalFileSystemHierarchyNodeRef implements IPSHierarchyNodeRef 
{
   /**
    * Convenience method that calls
    * {@link #PSLocalFileSystemHierarchyNodeRef(File, boolean, boolean) 
    * this(file, directory, <code>true</code>)}.
    */
   public PSLocalFileSystemHierarchyNodeRef(final File file,
         final boolean directory)
   {
      this(file, directory, true);
   }
   /**
    * Creates new reference for the specified file.
    * Can't be <code>null</code>.
    * @param file the referenced file
    * @param directory indicates whether the file is directory.
    * @param persisted Has the data for this object been stored permanently? 
    */
   public PSLocalFileSystemHierarchyNodeRef(final File file,
         final boolean directory, boolean persisted)
   {
      m_container = directory;
      setPersisted(persisted);
      setFile(file);
      // check for root so we don't accidentally access drives
      assert !isRoot() || directory : "Root must be directory"; 
      assert isRoot() || !file.exists() || (file.isDirectory() == directory)
            : "Declared directory should match to the real one";
   }

   /**
    * Equal only when files they refer to are equal.
    */
   @Override
   public boolean equals(Object o)
   {
      if (!(o instanceof PSLocalFileSystemHierarchyNodeRef))
      {
         return false;
      }
      return getFile().equals(((PSLocalFileSystemHierarchyNodeRef) o).getFile());
   }

   /**
    * Returns hash code of the file it refers to.
    */
   @Override
   public int hashCode()
   {
      return getFile().hashCode();
   }


   //see interface
   public void setManager(IPSHierarchyManager mgr)
   {
      if ( null == mgr)
      {
         throw new IllegalArgumentException("mgr cannot be null");  
      }
      m_manager = mgr;
   }
   
   //see interface
   public IPSHierarchyManager getManager()
   {
      return m_manager;
   }

   /**
    * Just returns result of {@link #equals(Object)}.
    */
   public boolean referencesSameObject(IPSReference other)
   {
      return equals(other);
   }
   
   @Override
   public String toString()
   {
      return getClass().getName() + "[" + getFile() + "]";
   }

   /**
    * Returns <code>true</code> if referenced file is a directory.
    */
   public boolean isContainer()
   {
      return m_container;
   }

   /**
    * Whether this is top-level file.
    */
   private boolean isRoot()
   {
      return getParent() == null;
   }

   // see base class
   public String getPath()
   {
      return getPathFromFile(getFile());
   }

   /**
    * Generates path for the provided file.
    * @param file file to generate path from. If <code>null</code> empty string
    * is returned.
    */
   private String getPathFromFile(final File file)
   {
      return file == null
            ? ""
            : getPathFromFile(file.getParentFile()) + "/" + file.getName(); 
   }

   public IPSHierarchyNodeRef getParent()
   {
      final File parentFile = getFile().getParentFile();
      return parentFile == null
            ? null
            : new PSLocalFileSystemHierarchyNodeRef(parentFile, true); 
   }

   public Collection<IPSHierarchyNodeRef> getChildren()
   {
      final List<IPSHierarchyNodeRef> children = new ArrayList<IPSHierarchyNodeRef>();
      if (getFile().listFiles() == null)
      {
         return children;
      }
      for (final File child : getFile().listFiles())
      {
         children.add(new PSLocalFileSystemHierarchyNodeRef(child, child.isDirectory()));
      }
      return children;
   }

   public String getDescription()
   {
      return (isContainer() ? "Directory:" : "File:")
            + "[" + getFile().getPath() + "]";
   }

   /**
    * Returns <code>null</code>.
    * @see com.percussion.client.IPSReference#getId()
    */
   public IPSGuid getId()
   {
      return null;
   }

   /**
    * Returns file name.
    */
   public String getName()
   {
      String name;
      if (StringUtils.isBlank(getFile().getName()))
      {
         // this happens for root dirs on Windows.
         name = getFile().getPath();
      }
      else
      {
         name = getFile().getName();
      }
      name = name.replace('\\', '/');
      if (name.length() > 1 && name.endsWith("/"))
      {
         name = name.substring(0, name.length()-1);
      }
      return name;
   }

   public String getLabelKey()
   {
      return getFile().getName();
   }

   public PSObjectType getObjectType()
   {
      return isContainer() ? OBJECT_TYPE_FOLDER : OBJECT_TYPE_FILE;
   }

   public IPSCmsModel getParentModel()
   {
      try
      {
         return PSCoreFactory.getInstance().getModel(PSObjectTypes.LOCAL_FILE);
      }
      catch (PSModelException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Always read-only.
    * @see com.percussion.client.IPSReference#isReadOnly()
    */
   public boolean isReadOnly()
   {
      return true;
   }

   /**
    * Indicates whether this object is permanently stored.
    * 
    * @param persisted <code>true</code> to indicate it is, <code>false</code>
    * otherwise.
    */
   public void setPersisted(boolean persisted)
   {
      m_persisted = persisted;
   }

   /**
    * Always persisted.
    * @see com.percussion.client.IPSReference#isPersisted()
    */
   public boolean isPersisted()
   {
      return m_persisted ;
   }

   /**
    * Always returns big value to encourage refreshing of any
    * local file system references.
    * @see com.percussion.client.IPSReference#getAge()
    */
   public long getAge()
   {
      return DateUtils.MILLIS_PER_DAY / DateUtils.MILLIS_PER_SECOND;
   }

   /* (non-Javadoc)
    * @see com.percussion.client.IPSReference#getLockUserName()
    */
   public String getLockUserName()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see com.percussion.client.IPSReference#getLockSessionId()
    */
   public String getLockSessionId()
   {
      return null;
   }
   
   /* (non-Javadoc)
    * @see com.percussion.client.IPSReference#isLocked()
    */
   public boolean isLocked()
   {
      return getLockSessionId() != null;
   }
   
   /* (non-Javadoc)
    * @see com.percussion.client.IPSReference#getPermissions()
    */
   public int[] getPermissions()
   {
      // XXX Auto-generated method stub
      return new int[0];
   }

   /**
    * The referenced file. 
    */
   public File getFile()
   {
      return m_file;
   }

   /**
    * Changes the referenced file.
    */
   public void setFile(final File file)
   {
      assert file != null;
      m_file = file;
   }
   
   public String getLocalDescription()
   {
      return getDescription();
   }

   public String getLocalLabelKey()
   {
      return getLabelKey();
   }

   public String getLocalName()
   {
      return getName();
   }
   
   /**
    * The referenced file.
    */
   private File m_file;
   
   /**
    * Whether the file is directory.
    */
   private boolean m_container;

   /**
    * Set by the {@link #setManager(IPSHierarchyManager)} method, then
    * never <code>null</code>. Used to manage node child relationships.
    */
   private IPSHierarchyManager m_manager;
   
   /**
    * Does this object exist in the permanent store? Defaults to
    * <code>false</code>.
    */
   private boolean m_persisted = false;
   
   /**
    * Object type - file. Singleton instance.
    */
   public static final PSObjectType OBJECT_TYPE_FILE =
         new PSObjectType(PSObjectTypes.LOCAL_FILE, PSObjectTypes.FileSubTypes.FILE);

   /**
    * Object type - folder. Singleton instance.
    */
   public static final PSObjectType OBJECT_TYPE_FOLDER =
         new PSObjectType(PSObjectTypes.LOCAL_FILE, PSObjectTypes.FileSubTypes.FOLDER);
   /**
    * For serialization.
    */
   private static final long serialVersionUID = 1L;
   
}
