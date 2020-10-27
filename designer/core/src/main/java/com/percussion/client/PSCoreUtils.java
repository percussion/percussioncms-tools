/******************************************************************************
 *
 * [ PSCoreUtils.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.client;

import com.percussion.client.impl.PSReference;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSApplicationFile;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Class with core utility methods.
 */
public class PSCoreUtils
{
   /**
    * Modifies name if it conflicts with any entries in <code>refs</code>
    * until a unique name is found. Uses the same model as the Windows explorer.
    * 
    * @param name The original name. Assumed not <code>null</code> or empty.
    * @param count A recursive counter. The first call should pass -1.
    * @param existingNames The generated name will not match any of these names.
    *    Never <code>null</code>.
    * @return A name that is unique from all names in <code>existingNames</code>,
    *    case-insensitive.
    */
   public static String createCopyName(String name, int count,
      Collection<String> existingNames)
   {
      // first check if there is a conflict
      if (count < 0)
      {
         for (String existingName : existingNames)
         {
            if (existingName.equalsIgnoreCase(name))
               return createCopyName(name, 1, existingNames);
         }
      }

      String testName;
      if (count < 2)
         testName = "Copy_of_" + name;
      else
         testName = "Copy_(" + count + ")_of_" + name; 
      
      for (String existingName : existingNames)
      {
         if (existingName.equalsIgnoreCase(testName))
            return createCopyName(name, ++count, existingNames);
      }
      
      return testName;
   }

   /**
    * Helper method that generates a dummy guid based on the current time in
    * milliseconds.
    * 
    * @param type the object type that the dummy guid should be created for.
    * @return never <code>null</code> always unique.
    */
   public static IPSGuid dummyGuid(PSTypeEnum type)
   {
      // Generate a unique key
      SecureRandom rand = new SecureRandom();
      long uuid = rand.nextInt() & Integer.MAX_VALUE; // We get the next int and
                                                      // not the next long so
      // we conform to the 32bit max
      return new PSGuid(1L, type, uuid);
   }

   /**
    * Helper method that generates a dummy guid based on the current time in
    * milliseconds. Uses the <code>PSTypeEnum.INTERNAL</code> type when
    * creating the guid.
    * 
    * @return never <code>null</code> always unique.
    */
   public static IPSGuid dummyGuid()
   {
      return dummyGuid(PSTypeEnum.INTERNAL);
   }

   /**
    * @param args
    */
   public static void main(String[] args)
   {
      for (int i = 0; i < 10; i++)
      {
         System.out.println(PSCoreUtils.dummyGuid());
      }
   }

   /**
    * Checks if the supplied array is <code>null</code> or any of its elements
    * are <code>null</code> and throws an
    * <code>IllegalArgumentException</code> if so.
    * 
    * @param toCheck May be <code>null</code> or empty. Throws exception if
    * <code>null</code> or any entry is <code>null</code>.
    * 
    * @param objectTypeName Used as part of error message. Assumed not
    * <code>null</code> or empty.
    */
   public static void checkArray(Object[] toCheck, String objectTypeName)
   {
      if (null == toCheck)
      {
         throw new IllegalArgumentException(objectTypeName + " cannot be null");
      }

      for (int i = 0; i < toCheck.length; i++)
      {
         if (toCheck[i] == null)
         {
            throw new IllegalArgumentException(objectTypeName
                  + " cannot have null entries (index = " + i);
         }
      }
   }

   /**
    * Convenience method to create a new reference
    * 
    * @param name cannot be <code>null</code> or empty.
    * @param label cannot be <code>null</code> or empty.
    * @param desc may be <code>null</code> or empty.
    * @param type cannot be <code>null</code>.
    * @param guid may be <code>null</code>.
    * @return the reference
    */
   public static IPSReference createReference(String name, String label,
         String desc, PSObjectType type, IPSGuid guid)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty.");
      if (StringUtils.isBlank(label))
         throw new IllegalArgumentException("label cannot be null or empty.");
      if (type == null)
         throw new IllegalArgumentException("type cannot be null.");
      IPSReference ref = null;
      try
      {
         ref = new PSReference(name, label, desc, type, guid);
      }
      catch (PSModelException e)
      {
         throw new RuntimeException(e);
      }
      return ref;
   }
   
   /**
    * Calls {@link #catalog(PSObjectTypes, boolean, boolean)} as
    * catalog(PSObjectTypes, boolean, <code>true</code>)
    */
   public static List<IPSReference> catalog(PSObjectTypes type, 
      boolean force)
      throws PSModelException
      {
         return catalog(type, force, true);
      }
   
   /**
    * Convenience method to return references for all available items for the
    * specified type.
    * @param type The primary object type
    * @param force forces the cache to be cleared and the items to be called
    * from the server.
    * @param onlyPersisted if <code>true</code> then only persisted object
    * refernces will be returned.
    * @return list of <code>IPSReference</code> objects, never
    * <code>null</code>, may be empty.
    * @throws PSModelException upon error
    */
   public static List<IPSReference> catalog(PSObjectTypes type, 
      boolean force, boolean onlyPersisted)
      throws PSModelException
      {

      IPSCmsModel model = PSCoreFactory.getInstance().getModel(type);
      List<IPSReference> results = new ArrayList<IPSReference>();
      for(IPSReference ref : model.catalog(force))
      {
         if(onlyPersisted && !ref.isPersisted())
            continue;
         results.add(ref);
      }
      return results;

   }

   /**
    * Checks the supplied name for validity in a hierarchical model. A valid
    * name is non-empty and does not contain any of the following characters:
    * {@literal / \ : ? * " < > |}. Spaces are allowed.
    * 
    * @param name Anything allowed.
    * 
    * @return <code>true</code> If the
    * {@link com.percussion.client.impl.PSHierarchyNodeRef#setName(String)}
    * method can be called successfully for a hierarchicaly model,
    * <code>false</code> otherwise.
    * 
    * @see #isValidObjectName(String)
    */
   public static boolean isValidHierarchyName(String name)
   {
      if (StringUtils.isBlank(name))
      {
         return false;
      }
      if (name.indexOf('\\') >= 0 || name.indexOf('/') >= 0
            || name.indexOf(':') >= 0 || name.indexOf('?') >= 0
            || name.indexOf('*') >= 0 || name.indexOf('"') >= 0
            || name.indexOf('>') >= 0 || name.indexOf('<') >= 0
            || name.indexOf('|') >= 0 || name.indexOf('\r') >= 0
            || name.indexOf('\n') >= 0)
      {
         return false;
      }
      return true;
   }

   /**
    * Checks the supplied name for validity in a flat model. A valid name is
    * non-empty and does not contain whitespace.
    * 
    * @param name Anything allowed.
    * 
    * @return <code>true</code> If the
    * {@link com.percussion.client.impl.PSHierarchyNodeRef#setName(String)}
    * method can be called successfully for a flat model, <code>false</code>
    * otherwise.
    * 
    * @see #isValidHierarchyName(String)
    */
   public static boolean isValidObjectName(String name)
   {
      if (StringUtils.isBlank(name))
      {
         return false;
      }
      if (name.indexOf(' ') >= 0 || name.indexOf('\t') >= 0
            || name.indexOf('\r') >= 0 || name.indexOf('\n') >= 0)
      {
         return false;
      }
      return true;
   }
   
   /**
    * Helper method to get the file names from
    * <RxRoot>/rx_resources/images/ContentTypeIcons folder.
    * 
    * @return String array of file names. Never <code>null</code> may be
    *         empty.
    */
   public static String[] getContentTypeIconFileNames()
   {
      List<String> fileNames = new ArrayList<String>();
      PSCoreFactory fact = PSCoreFactory.getInstance();
      try
      {
         IPSCmsModel model = fact.getModel(PSObjectTypes.XML_APPLICATION);
         Collection<IPSReference> refs = model.catalog();
         IPSReference rxref = null;
         for (IPSReference ref : refs)
         {
            if (ref.getName().equals("rx_resources"))
            {
               rxref = ref;
               break;
            }
         }
         PSApplication app = (PSApplication) model.load(rxref, false, false);
         PSApplicationFile appFile = new PSApplicationFile(new File(
               "images/ContentTypeIcons"), true);
         PSObjectStore os = new PSObjectStore(fact.getDesignerConnection());
         Collection<PSApplicationFile> files = os.getApplicationFiles(app,
               appFile);
         Iterator iter = files.iterator();
         while (iter.hasNext())
         {
            PSApplicationFile af = (PSApplicationFile) iter.next();
            if (!af.isFolder())
               fileNames.add(af.getFileName().getName());
         }
      }
      catch (PSModelException e)
      {
         new RuntimeException(e);
      }
      catch (Exception e)
      {
         new RuntimeException(e);
      }
      return fileNames.toArray(new String[fileNames.size()]);
   }
}
