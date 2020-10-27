/******************************************************************************
 *
 * [ PSContentTypeModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSContentTypeModel;
import com.percussion.client.models.PSLockException;
import com.percussion.client.models.impl.PSContentTypeModel;
import com.percussion.client.objectstore.PSUiContentTypeConverter;
import com.percussion.client.objectstore.PSUiItemDefinition;
import com.percussion.client.proxies.IPSCmsModelProxy;
import com.percussion.client.proxies.PSObjectFactory;
import com.percussion.client.proxies.PSProxyUtils;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSApplicationFile;
import com.percussion.design.objectstore.PSContainerLocator;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSContentEditorMapper;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.webservices.content.PSContentType;
import com.percussion.webservices.transformation.impl.PSTransformerFactory;
import org.apache.axis.client.Stub;

import javax.xml.rpc.ServiceException;
import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides CRUD and cataloging services for the object type
 * {@link PSObjectTypes#CONTENT_TYPE}. Uses base class implementation whenever
 * possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSCmsModelProxy
 * 
 * @version 6.0
 * @created 03-Sep-2005 4:39:27 PM
 */
public class PSContentTypeModelProxy extends PSCmsModelProxy
{
   /**
    * Ctor. Invokes base class version with the object type
    * {@link PSObjectTypes#CONTENT_TYPE} and for main type and <code>null</code>
    * sub type since this object type foes not have any sub types.
    */
   public PSContentTypeModelProxy()
   {
      super(PSObjectTypes.CONTENT_TYPE);
      PSTransformerFactory.getInstance().register(
         PSUiContentTypeConverter.class, PSUiItemDefinition.class,
         PSContentType.class);
   }
   
   @Override
   protected void postCreate(Object[] sourceObjects, List<Object> results,
         Object[] refs) throws PSModelException
   {
      final IPSContentTypeModel contentTypeModel = (IPSContentTypeModel)
            PSCoreFactory.getInstance().getModel(PSObjectTypes.CONTENT_TYPE);
      for (int i = 0; i < sourceObjects.length; i++)
      {
         final PSUiItemDefinition def = (PSUiItemDefinition) results.get(i);
         try
         {
            final PSUiItemDefinition sourceDef =
                  (PSUiItemDefinition) sourceObjects[i];
            final IPSReference ref =
                  getRefForItemDef(sourceDef, contentTypeModel);
            
            final Map<IPSReference, Collection<IPSReference>> associations =
                  contentTypeModel.getTemplateAssociations(
                        Collections.<IPSReference>singleton(ref), false, false);
            if (!associations.isEmpty())
            {
               assert associations.size() == 1
                     && associations.containsKey(ref);
               Collection<IPSReference> associatedTemplateRefs = associations
                     .get(ref);
               // remove any type specific templates from the list
               IPSCmsModel templateModel = PSCoreFactory.getInstance()
                     .getModel(PSObjectTypes.TEMPLATE);
               Collection<IPSReference> localTemplates = templateModel
                     .catalog(false, new PSObjectType(PSObjectTypes.TEMPLATE,
                           PSObjectTypes.TemplateSubTypes.LOCAL));
               Iterator iter = associatedTemplateRefs.iterator();
               while (iter.hasNext())
               {
                  if (localTemplates.contains(iter.next()))
                     iter.remove();
               }
               
               def.setNewTemplates(
                     new HashSet<IPSReference>(associatedTemplateRefs));
            }
         }
         catch (PSModelException e)
         {
            results.set(i, null);
            refs[i] = e;
         }
         catch (PSLockException e)
         {
            results.set(i, null);
            refs[i] = e;
         }
      }
   }

   /**
    * Returns reference to the provided item definition.
    * @param def the definition to return reference to.
    * @param model the content type model.
    */
   private IPSReference getRefForItemDef(final PSUiItemDefinition def,
         final IPSContentTypeModel model) throws PSModelException
   {
      for (final IPSReference r : model.catalog())
      {
         if (r.getName().equals(def.getName()))
         {
            return r;
         }
      }
      return PSObjectFactory.objectToReference(def, m_objectPrimaryType, true);
   }

   /* 
    * @see com.percussion.client.proxies.IPSCmsModelProxy#save(
    * com.percussion.client.IPSReference[], java.lang.Object[], boolean)
    */
   @Override
   public void save(IPSReference[] refs, Object[] data, boolean releaseLock)
         throws PSMultiOperationException, PSModelException
   {
      final PSContentTypeModel contentTypeModel = (PSContentTypeModel)
            PSCoreFactory.getInstance().getModel(PSObjectTypes.CONTENT_TYPE);

      //Create or alter the tables and save the object
      Object[] result = new Object[data.length];
      boolean errorOccured = false;
      for (int i = 0; i < data.length; i++)
      {
         try
         {
            PSUiItemDefinition itemDef = (PSUiItemDefinition) data[i];
            //Save content type icon file
            saveContentTypeIconFile(itemDef);
            PSContentEditorPipe pipe = (PSContentEditorPipe) itemDef
                  .getContentEditor().getPipe();
            PSContentEditorMapper cemapper = pipe.getMapper();
            PSFieldSet set = cemapper.getFieldSet();
            PSDisplayMapper mapper = cemapper.getUIDefinition()
                  .getDisplayMapper();
            PSContainerLocator locator = pipe.getLocator();
            PSContentEditorTableHandler th = new PSContentEditorTableHandler(
                  set, mapper, locator,
                  PSContentEditorTableHandler.EDITOR_TYPE_LOCAL,
                  itemDef);
            th.setTableReferences();
            th.createAlterTablesForEditor();

         }
         catch (Exception e)
         {
            result[i] = e;
            errorOccured = true;
         }
      }
      if (errorOccured)
      {
         throw new PSMultiOperationException(result, refs);
      }


      // super save
      try
      {
         /*
          * Do not release the contenttype locks now, they may still be needed
          * to save template associations.
          */
         super.save(refs, data, false);
      }
      catch (PSMultiOperationException e)
      {
         errorOccured = true;
         result = e.getResults();
      }

      // save template associations
      for (int i = 0; i < result.length; i++)
      {
         if (result[i] == null)
         {
            try
            {
               /*
                * Do not release contenttype locks, we will do that at the end
                * of this method.
                */
               PSUiItemDefinition itemDef = (PSUiItemDefinition) data[i];
               maybeSaveNewTemplates(itemDef, refs[i], contentTypeModel,
                     false);
            }
            catch (PSMultiOperationException e)
            {
               assert(e.getResults().length == 1);
               Exception ex = (Exception)e.getResults()[0];
               if(ex != null && (ex instanceof PSModelException))
               {
                  ((PSModelException)ex).setDetail(e.getDetail(0));
               }
               result[i] = ex;
               errorOccured = true;
            }
         }
      }
      
      /*
       * Release the ccontenttype locks if so requested.
       */
      if (releaseLock)
         super.releaseLock(refs);

      if (errorOccured)
      {
         throw new PSMultiOperationException(result, refs);
      }
   }
   
   /**
    * Saves the content type icon file if the icon source is
    * <code>PSContentEditor.ICON_SOURCE_SPECIFIED</code> and icon value is an
    * absolute file. Saves the file to rx_resources/images/ContentTypeIcons
    * folder.
    * 
    * @param def The item def of the content type. Assumed not
    *           <code>null</code>.
    * @throws Exception, if failed to save the file.
    */
   private void saveContentTypeIconFile(PSUiItemDefinition def) throws Exception
   {
      PSContentEditor editor = def.getContentEditor();
      if (editor.getIconSource().equals(PSContentEditor.ICON_SOURCE_SPECIFIED))
      {
         String val = editor.getIconValue();
         File file = new File(val);
         if (file.isAbsolute())
         {
            if (!file.exists())
            {
               editor
                     .setContentTypeIcon(PSContentEditor.ICON_SOURCE_NONE, null);
               return;
            }
            else
            {
               PSCoreFactory fact = PSCoreFactory.getInstance();
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
               PSApplication app = (PSApplication) model.load(rxref, false,
                     false);
               File rxfile = new File(ICON_FOLDER + file.getName());
               PSApplicationFile appFile = new PSApplicationFile(
                     new FileInputStream(file), rxfile);
               PSObjectStore os = new PSObjectStore(fact
                     .getDesignerConnection());
               os.saveApplicationFile(app, appFile, true, true);
               def.getContentEditor().setContentTypeIcon(
                     PSContentEditor.ICON_SOURCE_SPECIFIED, file.getName());
            }

         }
      }
   }


   /**
    * Saves new templates if they are specified.
    * 
    * @param itemDef the object to save. Never <code>null</code>.
    * @param ref reference to the object to save. Never <code>null</code>.
    * @param contentTypeModel content type model.
    * @throws PSMultiOperationException is thrown on associations saving
    *            failure.
    */
   private void maybeSaveNewTemplates(PSUiItemDefinition itemDef,
         final IPSReference ref, final PSContentTypeModel contentTypeModel,
         boolean releaseLock)
         throws PSMultiOperationException
   {
      if (itemDef.areNewTemplatesSpecified())
      {
         Map<IPSReference, Collection<IPSReference>> associations
             = new HashMap<IPSReference, Collection<IPSReference>>();
         associations.put(ref, itemDef.getNewTemplates());
         contentTypeModel.setTemplateAssociations(associations, releaseLock);
         itemDef.setNewTemplates(null);
      }
   }

   @SuppressWarnings("unchecked")
   @Override
   /*
    * Override this to special handle the cloning of the item definition.
    * Leaving to base class leads to a dependency on E2Designer class which is
    * indirectly referenced by item defintion class. This dependency is illegal
    * and is avoided by overriding this instead of using xstream way.
    */
   public Object clone(Object source)
   {
      if(source instanceof PSUiItemDefinition)
      {
         PSUiItemDefinition uiItemDef = (PSUiItemDefinition)source;
         PSUiItemDefinition  cloneUiItemDef = new PSUiItemDefinition(uiItemDef);
         if (uiItemDef.getColumnActions() != null)
         {
            cloneUiItemDef.setColumnActions((Map<PSField, Integer>) super
               .clone(uiItemDef.getColumnActions()));
         }
         if (uiItemDef.getNewTemplates() != null)
         {
            cloneUiItemDef.setNewTemplates((Set<IPSReference>) super
               .clone(uiItemDef.getNewTemplates()));
         }
         return cloneUiItemDef;
         
      }
      return super.clone(source);
   }
   
   /**
    * Overrides base class method to return the SOAP stub appropriate for this
    * type object.
    * 
    * @see PSCmsModelProxy#getSoapBinding(IPSCmsModelProxy.METHOD)
    * 
    */
   @Override
   @SuppressWarnings("unused")
   public Stub getSoapBinding(METHOD method) throws MalformedURLException,
      ServiceException
   {
      return PSProxyUtils.getContentDesignStub();
   }
   
   private static final String ICON_FOLDER = "images/ContentTypeIcons/";
}
