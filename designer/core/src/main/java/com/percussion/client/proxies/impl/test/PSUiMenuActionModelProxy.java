/******************************************************************************
 *
 * [ PSUiMenuActionModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
/**
 * 
 */
package com.percussion.client.proxies.impl.test;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreMessages;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSChildActions;
import com.percussion.cms.objectstore.PSDbComponent;
import com.percussion.services.catalog.PSTypeEnum;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.List;

/**
 * @author erikserating
 *
 */
public class PSUiMenuActionModelProxy extends PSComponentTestModelProxy
{

   public PSUiMenuActionModelProxy()
   {
      this(PSObjectTypes.UI_ACTION_MENU);
   }
   
   /**
    * @param type
    */
   public PSUiMenuActionModelProxy(PSObjectTypes type)
   {
      super(type);
      try
      {
         loadFromRepository();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
     
   }

   /* 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#
    * rename(com.percussion.client.IPSReference, java.lang.String, java.lang.Object)
    */
   @Override
   public void rename(IPSReference ref, String name, Object data)
   {
      // we may not know about the data object if it hasn't been persisted
      PSAction action = (PSAction) m_repositoryMap.get(ref);
      if (action != null)
      {
         action.setName(name);
         try
         {
            PSProxyTestUtil.saveRepository(m_repositoryMap, ms_repository);
         }
         catch (Exception e)
         {
            throw new RuntimeException("Error saving to repository file "
               + ms_repository, e);
         }
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
      if (data != null)
      {
         ((PSAction) data).setName(name);
      }
   }

   /* 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#create(
    * java.lang.Object[], java.util.List)
    */
   @SuppressWarnings("unchecked")
   @Override
   public IPSReference[] create(Object[] sourceObjects, String[] names,
         List results)
   {
      if(sourceObjects == null || sourceObjects.length == 0)
         throw new IllegalArgumentException(
            "sourceObjects cannot be null or empty.");
      if (names != null && names.length != sourceObjects.length)
      {
         throw new IllegalArgumentException(
               "names must have same length as sourceObjects if supplied");
      }
      if(results == null)
         throw new IllegalArgumentException("results cannot be null.");
      IPSReference[] refs = new IPSReference[sourceObjects.length];
      results.clear();
      for(int i = 0; i < sourceObjects.length; i++)
      {
         if(sourceObjects[i] instanceof PSAction)
         {
            try
            {
               PSAction action = (PSAction)((PSAction)sourceObjects[i]).clone();               
               action.setLocator(PSAction.createKey(String.valueOf(PSCoreUtils
                     .dummyGuid(PSTypeEnum.ACTION).getUUID())));
                  
               String name;
               if (names == null || StringUtils.isBlank(names[i]))
               {
                  name = PSCoreMessages.getString("common.prefix.copyof") //$NON-NLS-1$
                     + action.getName();
               }
               else
                  name = names[i];
               action.setName(name);
               results.add(action);
               refs[i] = objectToReference(action);
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
                  "sourceObjects must be instances of PSKeyword");
         }
      }
      return refs;
   }

   /* 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#create(
    * com.percussion.client.PSObjectType, java.util.Collection, java.util.List)
    */
   @SuppressWarnings("unchecked")
   @Override
   public IPSReference[] create(PSObjectType objType,
         Collection<String> names, List results)
   {
      if (objType == null
         || !objType.getPrimaryType().equals(m_objectPrimaryType))
         throw new IllegalArgumentException("objType is invalid.");
      if(names == null)
         throw new IllegalArgumentException("names cannot be null.");
      if(results == null)
         throw new IllegalArgumentException("results cannot be null.");
      IPSReference[] refs = new IPSReference[names.size()];
      int idx = -1;
      for(String name : names)
      {
         PSAction action = createNewObject(name, -1);
         Enum secondary = objType.getSecondaryType();
         action.setClientAction(false);
         if (secondary == PSObjectTypes.UiActionMenuSubTypes.MENU_CASCADING_USER
               || secondary == PSObjectTypes.UiActionMenuSubTypes.MENU_DYNAMIC_USER)
         {
            action.setMenuType(PSAction.TYPE_MENU);
            action.setMenuDynamic(secondary 
                  == PSObjectTypes.UiActionMenuSubTypes.MENU_DYNAMIC_USER);
         }
         else if (secondary == 
            PSObjectTypes.UiActionMenuSubTypes.MENU_ENTRY_USER)
         {
            action.setMenuType(PSAction.TYPE_MENUITEM);
            action.setClientAction(false);
         }
         else
         {
            throw new UnsupportedOperationException(
                  "Creating system (client) actions is not supported.");
         }
         refs[++idx] = objectToReference(action);
         assert(refs[idx].getObjectType().equals(objType));
         m_lockHelper.getLock(refs[idx]);
         results.add(action);
      }
      return refs;
   }
   
   /**
    * Load the existing objects from the repository.
    * @throws PSProxyTestException  
    */
   @SuppressWarnings("unchecked")
   protected void loadFromRepository() throws PSProxyTestException 
   {
      m_repositoryMap.clear();
      if (!ms_repository.exists())
      {
         // If repository does not exist
         // create a few to start with
         String[] names = new String[]{
            "System Menu", "Cascade Menu", "Dynamic Menu", "User Menu Entry-submenu", "System Menu Entry"};
         long[] ids = new long[]{
            10, 20, 30, 40, 50};
         PSAction cascadeMenu = null;
         for (int i = 0; i < ids.length; i++)
         {            
            PSAction action = createNewObject(names[i], ids[i]);
            switch (i)
            {
               case 0:
               case 1:
               case 2:
                  action.setMenuType("MENU");
                  if (i == 0)
                     action.setClientAction(true);
                  if (i == 1)
                  {
                     action.setClientAction(false);
                     action.setMenuDynamic(false);
                     cascadeMenu = action;
                  }
                  else if (i == 2)
                  {
                     action.setClientAction(false);
                     action.setMenuDynamic(true);
                     action.setURL("http://Rhythmyx/testaction");
                  }
                  break;
               case 3:
                  action.setMenuType("MENUITEM");
                  action.setClientAction(false);
                  PSChildActions childEntries = cascadeMenu.getChildren();
                  childEntries.add(action);
                  break;
               case 4:
                  action.setMenuType("MENUITEM");
                  action.setClientAction(true);
                  break;
            }
            m_repositoryMap.put(objectToReference(action), action);
         }
         // and save to repository
         saveRepository(m_repositoryMap, ms_repository);
      }
      else
      {
         m_repositoryMap = 
            (PSRepositoryMap)loadRepository(ms_repository);
      }
   }
      
   /* 
    * @see com.percussion.client.proxies.test.impl.PSTestModelProxy#
    * createNewObject(java.lang.String)
    */
   protected PSAction createNewObject(String name, long id)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty");
      if (id < 1)
         id = new SecureRandom().nextInt() & Integer.MAX_VALUE;
      PSAction action = null;
      try
      {
         action = new PSAction(name, name);
         action.setLocator(PSAction.createKey(String.valueOf(id)));
         action.setDescription(PSCoreMessages
               .getString("common.prefix.description") //$NON-NLS-1$
               + " " + name); //$NON-NLS-1$
      }
      catch (Exception ignore)
      {
      }
      return action;      
   }
   
   
   
   /* 
    * @see com.percussion.client.proxies.test.impl.PSTestModelProxy#
    * getRepositoryFile()
    */
   @Override
   protected File getRepositoryFile()
   {
      return ms_repository;
   }

   /* 
    * @see com.percussion.client.proxies.test.impl.PSTestModelProxy#
    * getRepositoryMap()
    */
   @Override
   protected IPSRepositoryMap getRepositoryMap()
   {
      return m_repositoryMap;
   }
   
   @Override
   protected Object newComponentInstance()
   {
      PSDbComponent comp = null;
      
         comp =  new PSAction("Dummy", "Dummy");
      
      return comp;
   }
   
   /**
    * Name of the repository file.
    */
   public static final String REPOSITORY_XML = "UiActionMenu_repository.xml";

   /**
    * Repository file name this test proxy uses. The file will be created
    * in the root directory for the workbench if one does not exist. It will use
    * the existing one if one exists.
    */
   static private File ms_repository = new File(REPOSITORY_XML);

   /**
    * Map of all object from the repository. Filled during initialization of the
    * proxy.
    */
   protected PSRepositoryMap m_repositoryMap = 
      new PSRepositoryMap();

  

}
