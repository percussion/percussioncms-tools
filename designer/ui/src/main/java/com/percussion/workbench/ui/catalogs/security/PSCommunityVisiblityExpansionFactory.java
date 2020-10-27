/******************************************************************************
*
* [ PSCommunityVisiblityExpansionFactory.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.catalogs.security;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.PSSecurityUtils;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.workbench.ui.IPSCatalog;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.catalogs.PSCatalogFactoryBase;
import com.percussion.workbench.ui.util.PSUiUtils;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor.InheritedProperties;
import com.percussion.workbench.ui.views.hierarchy.xmlbind.Catalog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PSCommunityVisiblityExpansionFactory extends PSCatalogFactoryBase
{

   public PSCommunityVisiblityExpansionFactory(
      InheritedProperties contextProps, PSHierarchyDefProcessor proc,
      Catalog type)
   {
      super(contextProps, proc, type);
   }
   

   /* 
    * @see com.percussion.workbench.ui.IPSCatalogFactory#createCatalog(
    * com.percussion.workbench.ui.PSUiReference)
    */
   public IPSCatalog createCatalog(PSUiReference parent)
   {
      return new BaseCataloger(parent)
      {
         @SuppressWarnings({"unused","unchecked"})
         public List<PSUiReference> getEntries(boolean force)
            throws PSModelException
         {
            final Object[] results = new Object[1];
            final PSModelException[] errors = new PSModelException[1];

            BusyIndicator.showWhile(Display.getCurrent(), new Runnable()
            {
               public void run()
               {
                  try
                  {
                     String[] types = 
                        getContextProperty(OBJECT_TYPE).split(" ");
                     if (types.length == 0)
                     {
                        results[0] = new ArrayList<PSUiReference>();
                        errors[0] = null;
                        return;
                     }
                     
                     Enum primary = PSObjectTypes.valueOf(
                        types[0].split(":")[0]);
                     PSTypeEnum type = PSObjectTypeFactory
                           .convertPrimaryTypeToServerType(primary);
                     PSUiReference community = getAncestorNode(
                        PSObjectTypes.COMMUNITY);
                     
                     //can't expand anything until it has been saved at least once
                     if (!community.getReference().isPersisted())
                     {
                        String msg = PSMessages.getString(
                           "PSCommunityVisiblityExpansionFactory.notPersistedYet.message");
                        String title = PSMessages.getString(
                           "common.warning.title");
                        MessageDialog.openWarning(PSUiUtils.getShell(), 
                           title, msg);
                        
                        results[0] = Collections.emptyList();
                        errors[0] = null;
                        return;
                     }
                     
                     List<IPSReference> refs = 
                        PSSecurityUtils.getVisibilityByCommunity(
                           community.getReference(), type);
                     filterByType(refs, types);
                     
                     results[0] = createNodes(refs);
                     errors[0] = null;
                  }
                  catch (PSModelException e)
                  {
                     errors[0] = e;
                  }
               }
            });
            
            if (errors[0] != null)
               throw errors[0];
            
            return (List<PSUiReference>) results[0];
         }
         
         private void filterByType(List<IPSReference> refs, String[] types)
         {
            List<String> allowed = new ArrayList<String>();
            for(String type : types)
               allowed.add(type);
            for(int i = refs.size() - 1; i >= 0; i--)
            {
               if(!allowed.contains(refs.get(i).getObjectType().toString()))
                  refs.remove(i);
            }
         }
         
      };
   }
   
   private static final String OBJECT_TYPE = "objectType";
}
