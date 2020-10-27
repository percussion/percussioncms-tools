/******************************************************************************
 *
 * [ PSMenuAllowedEntriesNodeHandler.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.handlers;

import com.percussion.client.IPSReference;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSChildActions;
import com.percussion.cms.objectstore.PSMenuChild;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.workbench.ui.PSUiReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

/**
 * Handler for a node that is a menu so it can process adding/removing menu 
 * entries. Since the menu entries are direct children of the node, this 
 * handler must manage both link processing (menus with menus/menu entries as
 * children) and node processing (the menu itself.) This class overrides the
 * basic operations and delegates to the base class for link processing and 
 * handles menu object processing itself.
 * 
 * @author paulhoward
 */
@SuppressWarnings("unchecked") //PSChildActions iterator
public class PSMenuAllowedEntriesNodeHandler extends PSLinkNodeHandler
{

   public PSMenuAllowedEntriesNodeHandler(Properties props, String iconPath,
         PSObjectType[] allowedTypes)
   {
      super(props, iconPath, allowedTypes);
   }

   @Override
   protected Collection<IPSReference> doSaveAssociations(IPSReference menuRef,
         Collection<IPSReference> entryRefs) throws Exception
   {
      IPSCmsModel menuModel = getModel(PSObjectTypes.UI_ACTION_MENU);
      boolean locked = false;
      try
      {
         PSAction menuData = (PSAction) menuModel.load(menuRef, true, false);
         locked = true;
         PSChildActions children = menuData.getChildren();
         Set<IPSReference> existingEntries = new HashSet<IPSReference>();
         for (Iterator<PSMenuChild> iter = children.iterator(); iter.hasNext();)
         {
            String id = iter.next().getChildActionId();
            IPSReference childRef = menuModel
                  .getReference(new PSDesignGuid(PSTypeEnum.ACTION, Long
                        .parseLong(id)));
         
            if (childRef == null)
            {
               ms_logger.info(MessageFormat.format(
                     "Parent menu {0} had reference to child id {1}, which was "
                     + "not found while adding associations. Removing from " 
                     + "parent.", menuData.getName(), id));
            }
            else
               existingEntries.add(childRef);
         }
         
         long parentId = menuRef.getId().longValue();
         //compare supplied against existing
         for (Iterator<IPSReference> iter = entryRefs.iterator(); iter.hasNext();)
         {
            IPSReference ref = iter.next();
            if (!existingEntries.contains(ref))
            {
               PSMenuChild child = new PSMenuChild(ref.getId().longValue(),
                     ref.getName(), parentId);
               children.add(child);
            }
            else
               iter.remove();
         }
         
         if (entryRefs.size() > 0)
         {
            menuModel.save(menuRef, true);
            locked = false;
         }
         return entryRefs;
      }
      finally
      {
         if (locked)
            menuModel.releaseLock(menuRef);
      }
   }

   @Override
   protected Collection<IPSReference> doDeleteAssociations(IPSReference menuRef,
         Collection<IPSReference> entryRefs) throws Exception
   {
      IPSCmsModel menuModel = getModel(PSObjectTypes.UI_ACTION_MENU);
      boolean locked = false;
      try
      {
         PSAction menuData = (PSAction) menuModel.load(menuRef, true, false);
         locked = true;
         PSChildActions children = menuData.getChildren();
         Collection<PSMenuChild> toRemove = new ArrayList<PSMenuChild>();
         for (Iterator<PSMenuChild> iter = children.iterator(); iter.hasNext();)
         {
            PSMenuChild child = iter.next();
            String id = child.getChildActionId();
            IPSReference childRef = menuModel
                  .getReference(new PSDesignGuid(PSTypeEnum.ACTION, Long
                        .parseLong(id)));
         
            if (childRef == null)
            {
               ms_logger.info(MessageFormat.format(
                     "Parent menu {0} had reference to child id {1}, which was "
                     + "not found while removing associations. Removing from " 
                     + "parent.", menuData.getName(), id));
            }
            else
            {
               if (entryRefs.contains(childRef))
                  toRemove.add(child);
            }
         }
         for (PSMenuChild child : toRemove)
         {
            children.remove(child);
         }
         
         menuModel.save(menuRef, true);
         locked = false;
         return entryRefs;
      }
      finally
      {
         if (locked)
            menuModel.releaseLock(menuRef);
      }
   }

   /**
    * We override to replace the base class behavior with the default behavior
    * of {@link PSDeclarativeNodeHandler}.
    */
   @SuppressWarnings("unused")
   @Override
   public boolean supportsCopy(PSUiReference node)
   {
      return super.isOptionEnabled(HandlerOptions.COPYABLE);
   }

   /**
    * Dispatches to different handlers depending on whether the supplied node is
    * a reference (indicating it is a child entry) or an instance (indicating an
    * action>.) Handles a mixture of node types (i.e. refs and instances.)
    */
   @Override
   public void handleDelete(Collection<PSUiReference> nodes)
   {
      //split into 2 sets, refs and non-refs
      Collection<PSUiReference> links = new ArrayList<PSUiReference>();
      Collection<PSUiReference> menus = new ArrayList<PSUiReference>();
      for (PSUiReference node : nodes)
      {
         if (node.isReference())
            links.add(node);
         else
            menus.add(node);
      }
      
      super.handleDelete(links);
      doHandleDelete(menus);
   }

   /**
    * Makes the determination based on whether the supplied node is a reference
    * (indicating it is a child entry) or an instance (indicating an action>.)
    * 
    * @return <code>true</code> if <code>node</code> is a reference, otherwise
    * it depends on the declarative def.
    */
   @SuppressWarnings("unused")
   @Override
   public boolean supportsDelete(PSUiReference node)
   {
      return node.isReference() ? true : super
            .isOptionEnabled(HandlerOptions.DELETABLE);
   }
   
   /**
    * The logging target for all instances of this class. Never
    * <code>null</code>.
    */
   private static Log ms_logger = LogFactory
         .getLog(PSMenuAllowedEntriesNodeHandler.class);
}
