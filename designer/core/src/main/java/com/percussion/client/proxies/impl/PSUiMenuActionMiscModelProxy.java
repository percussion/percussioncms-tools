/******************************************************************************
 *
 * [ PSUiMenuActionMiscModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
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
import com.percussion.client.impl.PSReference;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSActionParameter;
import com.percussion.cms.objectstore.PSActionVisibilityContext;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSMenuContext;
import com.percussion.cms.objectstore.PSMenuMode;
import com.percussion.cms.objectstore.PSName;
import com.percussion.cms.objectstore.client.PSRemoteCataloger;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.util.PSRemoteRequester;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PSUiMenuActionMiscModelProxy extends PSReadOnlyModelProxy
{

   public PSUiMenuActionMiscModelProxy()
   {
      super(PSObjectTypes.UI_ACTION_MENU_MISC);
   }

   /* 
    * @see com.percussion.client.proxies.impl.PSReadOnlyModelProxy#load(
    * com.percussion.client.IPSReference[], boolean, boolean)
    */
   @SuppressWarnings("unchecked")
   @Override
   public Object[] load(IPSReference[] reference, 
      boolean lock, 
      @SuppressWarnings("unused") boolean overrideLock) 
      throws PSMultiOperationException
   {
      if(lock)
         throw new UnsupportedOperationException(
            "Cannot load object as locked.");
      List results = new ArrayList();
      boolean error = false;
      for(IPSReference ref : reference)
      {
         Object obj = null;
         try
         {
            obj = findObject(ref);
            if(obj == null)
            {
               error = true;
               results.add(new PSModelException(
                  new Exception("Could not load object: " + ref.getName())));
            }
            else
            {
               results.add(clone(obj));
            }
         }
         catch (PSModelException e)
         {
            error = true;
            results.add(e);
         }
      }
      
      if(error)
         throw new PSMultiOperationException(results.toArray(), reference);
      return results.toArray();
   }

   /* 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#catalog()
    */
   @Override
   public Collection<IPSReference> catalog() throws PSModelException
   {
      Collection<IPSReference> results = new ArrayList<IPSReference>();
      //Modes
      PSObjectType type = new PSObjectType(
         PSObjectTypes.UI_ACTION_MENU_MISC,
         PSObjectTypes.UiActionMenuMiscSubTypes.MODES);
      for(PSMenuMode mode : getMenuModes())
      {
         results.add(createReference(mode, type));
      }
      //Contexts
      type = new PSObjectType(
         PSObjectTypes.UI_ACTION_MENU_MISC,
         PSObjectTypes.UiActionMenuMiscSubTypes.CONTEXTS);
      for(PSMenuContext context : getMenuContexts())
      {
         results.add(createReference(context, type));
      }
      //Context params
      type = new PSObjectType(
         PSObjectTypes.UI_ACTION_MENU_MISC,
         PSObjectTypes.UiActionMenuMiscSubTypes.CONTEXT_PARAMETERS);
      for(String param : getContextParams())
      {
         results.add(createReference(param, type));
      }
      //Visibility contexts
      try
      {
         type = new PSObjectType(
            PSObjectTypes.UI_ACTION_MENU_MISC,
            PSObjectTypes.UiActionMenuMiscSubTypes.VISIBILITY_CONTEXTS);
         for(PSPair<String, String> pair : getVisibilityContexts())
         {
            results.add(createReference(pair, type));
         }
      }
      catch (Exception e)
      {
         throw new PSModelException(e);
      }
      
      return results;
   }
      
   /* 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#clone(java.lang.Object)
    */
   @SuppressWarnings("unchecked")
   @Override
   public Object clone(Object source)
   {
      if(source == null)
         return null;
      if(source instanceof PSMenuMode)
      {
         return ((PSMenuMode)source).clone();
      }
      else if (source instanceof PSMenuContext)
      {
         return ((PSMenuContext)source).clone();
      }
      else if(source instanceof String)
      {
         //immutable no need to return a clone, just return
         // original string
         return source;
      }
      else if(source instanceof Collection)
      {
         Collection<PSPair<String, String>> coll = 
            (Collection<PSPair<String, String>>)source;
         Collection<PSPair<String, String>> newColl =
            new ArrayList<PSPair<String, String>>(coll.size());
         for(PSPair<String, String> pair : coll)
         {
            newColl.add(
               new PSPair<String, String>(pair.getFirst(), pair.getSecond()));
         }
         return newColl;
      }
      return null;
   }

      
   /**
    * Attempts to find the object by reference
    * @param ref cannot be <code>null</code>.
    * @return the object or <code>null</code> if not found.
    */
   protected Object findObject(IPSReference ref) throws PSModelException
   {
      if(ref == null)
         throw new IllegalArgumentException("ref cannot be null.");
      Object obj = null;
      PSObjectType type = ref.getObjectType();
      if(type.getSecondaryType() == PSObjectTypes.UiActionMenuMiscSubTypes.MODES)
      {
         for(PSMenuMode mode : getMenuModes())
         {
            int id = ref.getId().getUUID();
            int objId = mode.getLocator().getPartAsInt();
            if(id == objId)
            {
               obj = mode;
               break;
            }
         }
      }
      else if(type.getSecondaryType() == 
         PSObjectTypes.UiActionMenuMiscSubTypes.CONTEXTS)
      {
         for(PSMenuContext context : getMenuContexts())
         {
            int id = ref.getId().getUUID();
            int objId = context.getLocator().getPartAsInt();
            if(id == objId)
            {
               obj = context;
               break;
            }
         }
      }
      else if(type.getSecondaryType() == 
         PSObjectTypes.UiActionMenuMiscSubTypes.CONTEXT_PARAMETERS)
      {
         return ref.getName();
      }
      else if(type.getSecondaryType() == 
         PSObjectTypes.UiActionMenuMiscSubTypes.VISIBILITY_CONTEXTS)
      {
         obj =  getVCMap().get(ref.getLabelKey());
      }
      return obj;
   }
   
   /**
    * Creates a reference from the object passed in 
    * @param obj either a <code>PSMenuMode</code> or <code>PSMenuContext</code>
    * object. Cannot be <code>null</code>.
    * @param type the object type, cannot be <code>null</code>.
    * @return the reference object, never <code>null</code>.
    * @throws PSModelException upon any error.
    */
   protected IPSReference createReference(PSName obj, PSObjectType type)
      throws PSModelException
   {
      if(obj == null)
         throw new IllegalArgumentException("obj cannot be null.");
      if(type == null)
         throw new IllegalArgumentException("type cannot be null.");
      int id = obj.getLocator().getPartAsInt();
      IPSGuid guid = new PSGuid(0L, PSTypeEnum.INTERNAL, id);
      PSReference ref =   new PSReference(
         StringUtils.deleteWhitespace(obj.getName()), obj.getDisplayName(),
         obj.getDescription(), type, guid);
      ref.setPersisted();
      return ref;
   }
   
   /**
    * Creates a reference from the object passed in 
    * @param obj a <code>String</code> object. Cannot be <code>null</code>
    * or empty.
    * @param type the object type, cannot be <code>null</code>.
    * @return the reference object, never <code>null</code>.
    * @throws PSModelException upon any error.
    */
   protected IPSReference createReference(String obj, PSObjectType type)
   throws PSModelException
   {
      if(StringUtils.isBlank(obj))
         throw new IllegalArgumentException("obj cannot be null or empty.");
      if(type == null)
         throw new IllegalArgumentException("type cannot be null.");
      
      PSReference ref =  new PSReference(
         StringUtils.deleteWhitespace(obj), obj, obj, type, null);
      ref.setPersisted();
      return ref;
   }
   
   /**
    * Creates a reference from the object passed in 
    * @param obj a <code>PSPair<String, String></code> object. Cannot be 
    * <code>null</code> or empty.
    * @param type the object type, cannot be <code>null</code>.
    * @return the reference object, never <code>null</code>.
    * @throws PSModelException upon any error.
    */
   protected IPSReference createReference(PSPair<String, String> obj, PSObjectType type)
   throws PSModelException
   {
      if(obj == null)
         throw new IllegalArgumentException("obj cannot be null.");
      if(type == null)
         throw new IllegalArgumentException("type cannot be null.");
      int id = Integer.parseInt(obj.getFirst());
      IPSGuid guid = new PSGuid(0L, PSTypeEnum.INTERNAL, id);
      PSReference ref =  new PSReference(
         StringUtils.deleteWhitespace(obj.getSecond()), obj.getSecond(),
         obj.getSecond(), type, guid);
      ref.setPersisted();
      return ref;
   }
   
   /**
    * @return collection of all menu modes, never <code>null</code>, may be
    * empty.
    */
   protected Collection<PSMenuMode> getMenuModes() 
      throws PSModelException
   {        
      Collection<PSMenuMode> results = new ArrayList<PSMenuMode>();         
      try
      {
         Element[] elems = getComponentProxy().load(PSMenuMode.getComponentType(
            PSMenuMode.class), null);
         for(Element el : elems)
         {
            if(el != null)
            {
               results.add(new PSMenuMode(el));
            }
         }
      }
      catch (PSCmsException e)
      {
         throw new PSModelException(e);
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSModelException(e);
      }         
      
      return results;
   }
   
   /**
    * @return collection of all menu contexts, never <code>null</code>, may be
    * empty.
    */
   protected Collection<PSMenuContext> getMenuContexts() 
      throws PSModelException
   {
      
      Collection<PSMenuContext> results = new ArrayList<PSMenuContext>();
      
      try
      {
         Element[] elems = getComponentProxy().load(
            PSMenuContext.getComponentType(
               PSMenuContext.class), null);
         for(Element el : elems)
         {
            if(el != null)
            {
               results.add(new PSMenuContext(el));
            }
         }
      }
      catch (PSCmsException e)
      {
         throw new PSModelException(e);
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSModelException(e);
      }  
      
      return results;
   }
   
   /**
    * @return collection of all unique action context params
    * @throws PSModelException upon error
    */
   protected Collection<String> getContextParams() throws PSModelException
   {      
      Collection<String> results = new ArrayList<String>();
      for(PSAction action : getActions())
      {            
         Iterator params = action.getParameters().iterator();
         while (params.hasNext())
         {
            PSActionParameter param = (PSActionParameter) params.next();
            String value = param.getValue();
            if (value != null && value.startsWith("$") &&
               !results.contains(value))
               results.add(value);
         }
      }
      
      return results;
   }
   
   /**
    * @return a collection of all actions
    * @throws PSModelException upon error
    */
   private Collection<PSAction> getActions() throws PSModelException
   {
      Collection<PSAction> results = new ArrayList<PSAction>();
      
      try
      {
         Element[] elems = getComponentProxy().load(
            PSAction.getComponentType(
               PSAction.class), null);
         for(Element el : elems)
         {
            if(el != null)
            {
               results.add(new PSAction(el));
            }
         }
      }
      catch (PSCmsException e)
      {
         throw new PSModelException(e);
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSModelException(e);
      } 
      
      return results;
   }
   
   /**
    * @return collection of <code>PSPair</code> objects containing the
    * id's and names of all visiblility contexts.
    * @throws IOException upon any Io error.
    * @throws SAXException upon an Xml parsing error.
    */
   @SuppressWarnings("unchecked")
   protected Collection<PSPair<String, String>> getVisibilityContexts() 
      throws IOException, SAXException
   {      
      Collection<PSPair<String, String>> results =
         new ArrayList<PSPair<String, String>>();
      Map params = new HashMap();
      params.put(RXLOOKUP_KEY_PARAM, VISIBILITY_CONTEXTS_LOOKUP_KEY);
      getCatalog(results, LOOKUP_RESOURCE, params);
      return results; 
   }
   
   /**
    * Gets a map of all possible mapping options for each visibility
    * context.
    * @return a map of all visibility options, using the id as the keys.
    * Never <code>null</code>, may be empty.
    */
   protected Map<String, Collection<PSPair<String, String>>> getVCMap()
   {
      Map<String, Collection<PSPair<String, String>>> map =
         new HashMap<String, Collection<PSPair<String, String>>>();
      try
      {
         Collection<PSPair<String, String>> vContexts = 
            getVisibilityContexts();
         for(PSPair<String, String> vContext : vContexts)
         {
            int idx = Integer.parseInt(vContext.getFirst());
            String ctx = vContext.getSecond();
            Collection<PSPair<String, String>> coll = null;        
               switch(idx)
               {
                  // The values in the case statements are the values from
                  // the RXLOOKUP table, so any modifications to the table
                  // must be reflected here
                  case 1:
                     coll = getAssignmentTypes();
                     break;
                  case 3:
                     coll = getContentTypes();
                     break;
                  case 4:
                     coll = getObjectTypes();
                     break;
                  case 5:
                     coll = getClientContexts();
                     break;
                  case 6:
                     coll = getCheckoutStatus();
                     break;
                  case 7:
                     coll = getRoles();
                     break;
                  case 8:
                     coll = getLocales();
                     break;
                  case 9:
                     coll = getWorkflows();
                     break;
                  case 10:
                     coll = getPublishableContexts();
                     break;
                  case 11:
                     coll = getFolderSecurityContexts();
                     break;            
               }
            
            
            if(coll != null)
               map.put(ctx, coll);
         }
      }
      catch (IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (SAXException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return map;
   }
   
   /**
    * @return collection of <code>PSPair</code> objects containing the
    * id's and names of all assignment types.
    * @throws IOException upon any Io error.
    * @throws SAXException upon an Xml parsing error.
    */
   @SuppressWarnings("unchecked")
   protected Collection<PSPair<String, String>> getAssignmentTypes() 
      throws IOException, SAXException
   {     
      Collection<PSPair<String, String>> results = 
         new ArrayList<PSPair<String, String>>();
      Map params = new HashMap();
      params.put(RXLOOKUP_KEY_PARAM, ASSIGNMENT_TYPES_CONTEXTS_LOOKUP_KEY);
      getCatalog(results, LOOKUP_RESOURCE, params);
      
      return results;
   }
   
   /**
    * @return collection of <code>PSPair</code> objects containing the
    * id's and names of all communities.
    */
   protected Collection<PSPair<String, String>> getCommunities()
   {      
      Collection<PSPair<String, String>> results = 
         new ArrayList<PSPair<String, String>>();
      try
      {
         PSCoreFactory factory = PSCoreFactory.getInstance();
         PSRemoteRequester remoteRequester = factory.getRemoteRequester();
         
         Document resultXML = 
            remoteRequester.getDocument(COMMUNITIES_RESOURCE, null);
         if (resultXML != null)
         {
            NodeList nodes = resultXML.getElementsByTagName(
            "list");
            int length = nodes.getLength();
            Node node = null;
            Node listNode;
            String name;
            String id;
            for(int k = 0; k < length; k++)
            {
               listNode = nodes.item(k);
               node = ((Element)listNode).getElementsByTagName(
               "communityname").item(0);
               name = node.getFirstChild().getNodeValue();
               
               node = ((Element)listNode).getElementsByTagName(
               "communityid").item(0);
               id = node.getFirstChild().getNodeValue();
               results.add(new PSPair<String, String>(id, name));
            }
         }
      }
      catch (Exception e )
      {
         e.printStackTrace();
      }
      
      return results;
   }
   
   /**
    * @return collection of <code>PSPair</code> objects containing the
    * id's and names of all content types.
    * @throws IOException upon any Io error.
    * @throws SAXException upon an Xml parsing error.
    */
   protected Collection<PSPair<String, String>> getContentTypes() 
      throws IOException, SAXException
   {         
      Collection<PSPair<String, String>> results = 
         new ArrayList<PSPair<String, String>>();   
      PSCoreFactory factory = PSCoreFactory.getInstance();
      PSRemoteRequester remoteRequester = factory.getRemoteRequester();
      Document doc = remoteRequester.getDocument(CONTENTTYPES_RESOURCE, null);
      Element elem = doc.getDocumentElement();
      NodeList nL = elem.getElementsByTagName(PSXCONTENTTYPE);
      int sz = nL.getLength();
      Element psct = null;
      PSPair<String, String> pair = null;
      String key = null;
      String value = null;
      for (int k = 0; k < sz; k++)
      {
         psct = (Element)nL.item(k);
         key = psct.getElementsByTagName(ID).item(0)
         .getFirstChild().getNodeValue();
         value = psct.getElementsByTagName(NAME).item(0)
         .getFirstChild().getNodeValue();
         pair = new PSPair<String, String>(key, value);
         results.add(pair);
      }        
      
      return results;
   }
   
   /**
    * @return collection of <code>PSPair</code> objects containing the
    * id's and names of all object types.
    * @throws IOException upon any Io error.
    * @throws SAXException upon an Xml parsing error.
    */
   protected Collection<PSPair<String, String>> getObjectTypes() 
      throws IOException, SAXException
   {
      Collection<PSPair<String, String>> results = 
         new ArrayList<PSPair<String, String>>();
      getCatalog(results, OBJECTTYPES_RESOURCE, null);
      
      return results;
   }
   
   /**
    * @return collection of <code>PSPair</code> objects containing the
    * id's and names of all client contexts.
    */
   protected Collection<PSPair<String, String>> getClientContexts()
   {
      Collection<PSPair<String, String>> coll = 
         new ArrayList<PSPair<String, String>>();
      coll.add(new PSPair<String, String>("dnd", "Drag and Drop"));
      coll.add(new PSPair<String, String>("popupMenus","Popup menus"));
      coll.add(new PSPair<String, String>("singleSelect","Single selection"));
      coll.add(new PSPair<String, String>("multiSelect","Multiple selection"));
      
      return coll;
   }
   
   /**
    * @return collection of <code>PSPair</code> objects containing the
    * id's and names of all checkout statuses.
    * @throws IOException upon any Io error.
    * @throws SAXException upon an Xml parsing error.
    */
   @SuppressWarnings("unchecked")
   protected Collection<PSPair<String, String>> getCheckoutStatus() 
      throws IOException, SAXException
   {      
      Collection<PSPair<String, String>> results = 
         new ArrayList<PSPair<String, String>>();
      Map params = new HashMap();
      params.put(RXLOOKUP_KEY_PARAM, CHECKOUT_STATUS_CONTEXTS_LOOKUP_KEY);
      getCatalog(results, LOOKUP_RESOURCE, params);
      
      return results;
   }
   
   /**
    * @return collection of <code>PSPair</code> objects containing the
    * id's and names of all roles.
    * @throws IOException upon any Io error.
    * @throws SAXException upon an Xml parsing error.
    */
   protected Collection<PSPair<String, String>> getRoles() 
      throws IOException, SAXException
   {               
      Collection<PSPair<String, String>> results = 
         new ArrayList<PSPair<String, String>>();
      getCatalog(results, ROLES_RESOURCE, null);
      
      return results;
   }
   
   /**
    * @return collection of <code>PSPair</code> objects containing the
    * id's and names of all locales.
    * @throws IOException upon any Io error.
    * @throws SAXException upon an Xml parsing error.
    */
   protected Collection<PSPair<String, String>> getLocales() 
      throws IOException, SAXException
   {
      Collection<PSPair<String, String>> results =
         new ArrayList<PSPair<String, String>>();         
      getCatalog(results, LOCALES_RESOURCE, null);
      
      return results;
   }
   
   /**
    * @return collection of <code>PSPair</code> objects containing the
    * id's and names of all workflows.
    * @throws IOException upon any Io error.
    * @throws SAXException upon an Xml parsing error.
    */
   protected Collection<PSPair<String, String>> getWorkflows() 
      throws IOException, SAXException
   {
      Collection<PSPair<String, String>> results = 
         new ArrayList<PSPair<String, String>>();         
      getCatalog(results, WORKFLOW_RESOURCE, null);
      
      return results;
   }
   
   /**
    * @return collection of <code>PSPair</code> objects containing the
    * id's and names of all publishable contexts.
    * @throws IOException upon any Io error.
    * @throws SAXException upon an Xml parsing error.
    */
   @SuppressWarnings("unchecked")
   protected Collection<PSPair<String, String>> getPublishableContexts() 
      throws IOException, SAXException
   {
      Collection<PSPair<String, String>> results = 
         new ArrayList<PSPair<String, String>>();
      Map params = new HashMap();
      params.put(RXLOOKUP_KEY_PARAM, PUBLISHABLE_CONTEXTS_LOOKUP_KEY);         
      getCatalog(results, LOOKUP_RESOURCE, params);
      
      return results;
   }
   
   /**
    * @return collection of <code>PSPair</code> objects containing the
    * id's and names of all folder security contexts.
    * @throws IOException upon any Io error.
    * @throws SAXException upon an Xml parsing error.
    */
   @SuppressWarnings("unchecked")
   protected Collection<PSPair<String, String>> getFolderSecurityContexts() 
      throws IOException, SAXException
   {
      Collection<PSPair<String, String>> results = 
         new ArrayList<PSPair<String, String>>();
      Map params = new HashMap();
      params.put(RXLOOKUP_KEY_PARAM, FOLDER_SECURITY_CONTEXTS_LOOKUP_KEY);
      getCatalog(results, LOOKUP_RESOURCE, params);
      
      return results;
   }
   
   /**
    * Makes a lookup type request from to the resource specified using
    * the passed in params if specified. The passed in collection is populated
    * with the returned <code>PSPair</code> objects from the lookup.
    * @param coll assumed not <code>null</code>.
    * @param resource name of the resource, assumed not <code>null</code> or
    * empty.
    * @param params the params passed into the request, may be <code>null</code>
    * or empty.
    * @throws IOException upon any Io error.
    * @throws SAXException upon an Xml parsing error.
    */
   private void getCatalog(
      Collection<PSPair<String, String>> coll, String resource, Map params) 
   throws IOException, SAXException
   {
      
      PSCoreFactory factory = PSCoreFactory.getInstance();
      PSRemoteRequester remoteRequester = factory.getRemoteRequester();
      
      Document doc = remoteRequester.getDocument(resource, params);
      if (doc != null)
         populateCollection(coll, doc);
      
   }
   
   /**
    * Retrieves a component processor proxy
    * @return the proxy, never <code>null</code>.
    * @throws PSCmsException 
    */
   private PSComponentProcessorProxy getComponentProxy() throws PSCmsException
   {
      if(m_compProxy == null)
      {
         PSCoreFactory factory = PSCoreFactory.getInstance();
         PSRemoteRequester appReq = factory.getRemoteRequester();
         new PSRemoteCataloger(appReq);

         m_compProxy = new PSComponentProcessorProxy(
            PSComponentProcessorProxy.PROCTYPE_REMOTE, appReq);
      }
      return m_compProxy;
   }
   
   /**
    * Populates the passed in collection with pairs from a lookup
    * @param coll assumed not <code>null</code>.
    * @param doc assumed not <code>null</code>.
    */
   private static void populateCollection(
      Collection<PSPair<String, String>> coll, Document doc)
   {
      Element elem = doc.getDocumentElement();
      NodeList nL = elem.getElementsByTagName(PSXENTRY);
      int sz = nL.getLength();
      Element psxentry = null;
      PSPair<String, String> pair = null;
      String key = null;
      String value = null;
      for (int k = 0; k < sz; k++)
      {
         psxentry = (Element)nL.item(k);
         key = psxentry.getElementsByTagName(KEY).item(0).
            getFirstChild().getNodeValue();
         value = psxentry.getElementsByTagName(VALUE).item(0).
            getFirstChild().getNodeValue();
         pair = new PSPair<String, String>(key, value);
         coll.add(pair);
      }
   }
   
   /**
    * These are the internal values for all the visibility contexts. They
    * correspond to:
    * <ol>
    *    <li>Assignment Type</li>
    *    <li>Community</li>
    *    <li>Content Type</li>
    *    <li>Object Type</li>
    *    <li>Client Context</li>
    *    <li>Checkout Status</li>
    *    <li>Roles</li>
    *    <li>Locales</li>
    *    <li>Workflows</li>
    *    <li>Publishable</li>
    *    <li>Folder Security</li>
    * </ol>
    * These values are obtained from the RXLOOKUP table, 
    * using key={@link #VISIBILITY_CONTEXTS_LOOKUP_KEY}.  
    * We use them to map the method used to catalog the values 
    * associated with the context.
    */
   protected static final String[] VISIBILITY_CONTEXTS =
   {
      PSActionVisibilityContext.VIS_CONTEXT_ASSIGNMENT_TYPE,
      PSActionVisibilityContext.VIS_CONTEXT_CONTENT_TYPE,
      PSActionVisibilityContext.VIS_CONTEXT_OBJECT_TYPE,
      PSActionVisibilityContext.VIS_CONTEXT_CLIENT_CONTEXT,
      PSActionVisibilityContext.VIS_CONTEXT_CHECKOUT_STATUS,
      PSActionVisibilityContext.VIS_CONTEXT_ROLES_TYPE,
      PSActionVisibilityContext.VIS_CONTEXT_LOCALES_TYPE,
      PSActionVisibilityContext.VIS_CONTEXT_WORKFLOWS_TYPE,
      PSActionVisibilityContext.VIS_CONTEXT_PUBLISHABLE_TYPE,
      PSActionVisibilityContext.VIS_CONTEXT_FOLDER_SECURITY
   };   
   
   
   private PSComponentProcessorProxy m_compProxy;
   
   // XMLNODENAMES
   private static final String PSXENTRY = "PSXEntry";
   private static final String KEY = "Value";
   private static final String VALUE = "PSXDisplayText";
   private static final String PSXCONTENTTYPE = "PSXContentType";
   private static final String ID = "id";
   private static final String NAME = "name"; 
   
   /**
    * The key for the possible visibility contexts for action menus. This is
    * a key into the RXLOOKUP table. See also {@link #VISIBILITY_CONTEXTS}.
    */
   private static final String VISIBILITY_CONTEXTS_LOOKUP_KEY = "157";

   /**
    * The key for the possible publishable context values for action menus.
    * This is a key into the RXLOOKUP table.
    * See also {@link #VISIBILITY_CONTEXTS}.
    */
   private static final String PUBLISHABLE_CONTEXTS_LOOKUP_KEY = "172";

   /**
    * The key for the possible Assignment Types context values for action menus.
    * This is a key into the RXLOOKUP table. 
    * See also {@link #VISIBILITY_CONTEXTS}.
    */
   private static final String ASSIGNMENT_TYPES_CONTEXTS_LOOKUP_KEY = "121";

   /**
    * The key for the possible Checkout Status context values for action menus.
    * This is a key into the RXLOOKUP table. 
    * See also {@link #VISIBILITY_CONTEXTS}.
    */
   private static final String CHECKOUT_STATUS_CONTEXTS_LOOKUP_KEY = "168";

   /**
    * The key for the possible folder security context values for action menus.
    * This is a key into the RXLOOKUP table. 
    * See also {@link #VISIBILITY_CONTEXTS}.
    */
   private static final String FOLDER_SECURITY_CONTEXTS_LOOKUP_KEY = "24";

   /**
    * The HTML parameter name used to supply the key parameter to the lookup
    * resource for global keywords from the RXLOOKUP table. Never
    * <code>null</code>, empty or changed.
    */
   private static final String RXLOOKUP_KEY_PARAM = "key";
   
   //Resources
   private static final String LOOKUP_RESOURCE = "sys_ceSupport/lookup";
   
   /**
    * Resource location for communities
    */
   private static final String COMMUNITIES_RESOURCE = 
      "sys_cmpCommunities/communities.xml";
   
   private static final String CONTENTTYPES_RESOURCE =
      "sys_psxContentEditorCataloger/getContentTypes";
   
   private static final String OBJECTTYPES_RESOURCE =
      "sys_psxContentEditorCataloger/getObjectTypes";

   /**
    * Resource location for Roles.
    */
   private static final String ROLES_RESOURCE =
      "sys_psxContentEditorCataloger/RolesLookup";

   /**
    * Resource location for Locales.
    */
   private static final String LOCALES_RESOURCE =
      "sys_psxContentEditorCataloger/LocaleLookup";

   /**
    * Resource location for Workflow.
    */
   private static final String WORKFLOW_RESOURCE =
      "sys_psxContentEditorCataloger/WorkflowLookup";


}
