/******************************************************************************
 *
 * [ PSCatalogedVisibilityContexts.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.E2Designer;
import com.percussion.cms.objectstore.PSActionVisibilityContext;
import com.percussion.util.PSRemoteRequester;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PSCatalogedVisibilityContexts
{
   public static List getCommunities()
   {
      if (ms_communitiesList == null)
      {
         ms_communitiesList = CommunitiesCataloger.getAllCommunities();
         Collections.sort(ms_communitiesList);
      }
      return ms_communitiesList;
   }

   public static List getVisibilityContextNames() throws SAXException,
      IOException
   {
      if (ms_vcNamesList.size() == 0)
      {
         Map map = new HashMap();
         map.put(RXLOOKUP_KEY_PARAM, VISIBILITY_CONTEXTS_LOOKUP_KEY);
         Document doc = m_remoteReq.getDocument(ATCSVC_RESOURCE, map);
         if (doc != null)
            populateList(ms_vcNamesList, doc);
         Collections.sort(ms_vcNamesList);
      }
      return ms_vcNamesList;
   }

   public static Map getVCMap() throws SAXException, IOException
   {
      int sz = ms_vcNamesList.size();
      String vcName = "";
      for (int k = 0; k < sz; k++)
      {
         // order-insensitive processing
         vcName = (String)((PSComparablePair) ms_vcNamesList.get(k)).getKey();
         if (vcName.equalsIgnoreCase(VISIBILITY_CONTEXTS[0]))
            m_vcMap.put(vcName, getAssignmentTypes());
         else if (vcName.equalsIgnoreCase(VISIBILITY_CONTEXTS[1]))
            m_vcMap.put(vcName, getContentTypes());
         else if (vcName.equalsIgnoreCase(VISIBILITY_CONTEXTS[2]))
            m_vcMap.put(vcName, getObjectTypes());
         else if (vcName.equalsIgnoreCase(VISIBILITY_CONTEXTS[3]))
            m_vcMap.put(vcName, getClientContexts());
         else if (vcName.equalsIgnoreCase(VISIBILITY_CONTEXTS[4]))
            m_vcMap.put(vcName, getCheckoutStatus());
         else if (vcName.equalsIgnoreCase(VISIBILITY_CONTEXTS[5]))
            m_vcMap.put(vcName, getRoles());
         else if (vcName.equalsIgnoreCase(VISIBILITY_CONTEXTS[6]))
            m_vcMap.put(vcName, getLocales());
         else if (vcName.equalsIgnoreCase(VISIBILITY_CONTEXTS[7]))
            m_vcMap.put(vcName, getWorkflows());
         else if (vcName.equalsIgnoreCase(VISIBILITY_CONTEXTS[8]))
            m_vcMap.put(vcName, getPublishableContexts());
         else if (vcName.equalsIgnoreCase(VISIBILITY_CONTEXTS[9]))
            m_vcMap.put(vcName, getFolderSecurityContexts());
      }

      return m_vcMap;
   }

   /**
    * Returns the list of all of the roles on the server to be used with
    * visibility contexts.
    *
    * @return list of all roles in the system.  Never <code>null</code> may
    * be empty.
    *
    * @throws SAXException if a problem fetching the roles xml doc.
    *
    * @throws IOException if a problem fetching the roles xml doc.
    */
   public static List getRoles() throws SAXException, IOException
   {
      /**
       * TODO: This class should be smarter.  As it is currently implemented:
       * a new getXXX needs to be created, the getVCMap() needs to be modified,
       * and fields need to be added each time a new visibility context
       * is added.  This class should be refactored to be more generic, consider
       * using a factory method that takes an IPSVisibilityContext, and the
       * objects that implement this have all of the instructions needed by the
       * this class.
       */
      if (ms_rolesList.size() == 0)
      {
         Map map = new HashMap();
         Document doc = m_remoteReq.getDocument(ROLES_RESOURCE, map);
         if (doc != null)
            populateList(ms_rolesList, doc);
         Collections.sort(ms_rolesList);
      }
      return ms_rolesList;
   }

   /**
    * Returns the list of all of the locales in the system to be used with
    * visibility contexts.
    *
    * @return list of all of the locales in the system.  Never <code>null</code>
    * may be empty.
    *
    * @throws SAXException if a problem fetching the locales xml doc.
    *
    * @throws IOException if a problem fetching the locales xml doc.
    */
   public static List getLocales() throws SAXException, IOException
   {
      if (ms_localesList.size() == 0)
      {
         Map map = new HashMap();
         Document doc = m_remoteReq.getDocument(LOCALES_RESOURCE, map);
         if (doc != null)
            populateList(ms_localesList, doc);
         Collections.sort(ms_localesList);
      }
      return ms_localesList;
   }

   /**
    * Returns the list of all of the workflows in the system to be used with
    * visibility contexts.
    *
    * @return list of all workflows in the system.  Never <code>null</code> may
    * be empty.
    *
    * @throws SAXException if a problem fetching the workflow xml doc.
    *
    * @throws IOException if a problem fetching the workflow xml doc.
    */
   public static List getWorkflows() throws SAXException, IOException
   {
      if ( ms_workflowList.size() == 0)
      {
         Map map = new HashMap();
         Document doc = m_remoteReq.getDocument(WORKFLOW_RESOURCE, map);
         if (doc != null)
            populateList(ms_workflowList, doc);
         Collections.sort(ms_workflowList);
      }
      return ms_workflowList;
   }

   /**
    * Get the the list of publishable context values. The values are lookued up
    * from the global RXLOOKUP keword table during the first call to this
    * method. The cached list is returned in all further calls.
    *
    * @return a list of publishable context values, never <code>null</code>,
    *    may be empty.
    * @throws SAXException on all errors while parsing the lookup document.
    * @throws IOException on any I/O error accurring during the keyword lookup.
    */
   public static List getPublishableContexts() throws SAXException, IOException
   {
      if (ms_publishableContexts.size() == 0)
      {
         Map params = new HashMap();
         params.put(RXLOOKUP_KEY_PARAM, PUBLISHABLE_CONTEXTS_LOOKUP_KEY);
         Document doc = m_remoteReq.getDocument(ATCSVC_RESOURCE, params);
         if (doc != null)
            populateList(ms_publishableContexts, doc);

         Collections.sort(ms_publishableContexts);
      }

      return ms_publishableContexts;
   }

   /**
    * Get the the list of Folder Security context values. The values are
    * looked up from the global RXLOOKUP keword table during the first call to
    * this method. The cached list is returned in all further calls.
    *
    * @return a list of folder security context values (sorted in ascending
    * dictionary order), never <code>null</code>, may be empty.
    *
    * @throws SAXException on all errors while parsing the lookup document.
    * @throws IOException on any I/O error accurring during the keyword lookup.
    */
   public static List getFolderSecurityContexts()
      throws SAXException, IOException
   {
      if (ms_folderSecurityContexts.size() == 0)
      {
         Map params = new HashMap();
         params.put(RXLOOKUP_KEY_PARAM, FOLDER_SECURITY_CONTEXTS_LOOKUP_KEY);
         Document doc = m_remoteReq.getDocument(ATCSVC_RESOURCE, params);
         if (doc != null)
            populateList(ms_folderSecurityContexts, doc);

         Collections.sort(ms_folderSecurityContexts);
      }

      return ms_folderSecurityContexts;
   }

   public static List getAssignmentTypes() throws SAXException, IOException
   {
      if (ms_assingmentTypesList.size() == 0)
      {
         Map map = new HashMap();
         map.put(RXLOOKUP_KEY_PARAM, "13");
         Document doc = m_remoteReq.getDocument(ATCSVC_RESOURCE, map);
         if (doc != null)
            populateList(ms_assingmentTypesList, doc);
         Collections.sort(ms_assingmentTypesList);
      }
      return ms_assingmentTypesList;
   }

   public static List getContentTypes() throws SAXException, IOException
   {
      if (ms_contentTypesList.size() == 0)
      {
         Map map = new HashMap();
         Document doc = m_remoteReq.getDocument(CONTENTTYPES_RESOURCE, map);
         Element elem = doc.getDocumentElement();
         NodeList nL = elem.getElementsByTagName(PSXCONTENTTYPE);
         int sz = nL.getLength();
         Element psct = null;
         PSComparablePair pair = null;
         String key = null;
         String value = null;
         for (int k = 0; k < sz; k++)
         {
            psct = (Element)nL.item(k);
            key = psct.getElementsByTagName(ID).item(0).getFirstChild().getNodeValue();
            value = psct.getElementsByTagName(NAME).item(0).getFirstChild().getNodeValue();
            pair = new PSComparablePair(key, value);
            ms_contentTypesList.add(pair);
         }
         Collections.sort(ms_contentTypesList);
      }
      return ms_contentTypesList;
   }

   public static List getObjectTypes() throws SAXException, IOException
   {
      if (ms_objectTypeList.size() == 0)
      {
         Map map = new HashMap();
         Document doc = m_remoteReq.getDocument(OBJECTTYPES_RESOURCE, map);
         if (doc != null)
            populateList(ms_objectTypeList, doc);
         Collections.sort(ms_objectTypeList);
      }
      return ms_objectTypeList;
   }

   public static List getCheckoutStatus() throws SAXException, IOException
   {
      if (ms_checkoutStatusListList.size() == 0)
      {
         Map map = new HashMap();
          map.put(RXLOOKUP_KEY_PARAM, "21");
         Document doc = m_remoteReq.getDocument(ATCSVC_RESOURCE, map);
         if (doc != null)
            populateList(ms_checkoutStatusListList, doc);
         Collections.sort(ms_checkoutStatusListList);
      }
      return ms_checkoutStatusListList;
   }

   public static List getClientContexts()
   {
      if (ms_clientContextsList.size() == 0)
      {
         for (int i=0; i < CLIENT_CONTEXTS.length; i++)
         {
            ms_clientContextsList.add(new PSComparablePair(
                  CLIENT_CONTEXTS[i][0], CLIENT_CONTEXTS[i][1]));
         }
         Collections.sort(ms_clientContextsList);
      }
      return ms_clientContextsList;
   }

   private static void populateList(List list, Document doc)
   {
      Element elem = doc.getDocumentElement();
      NodeList nL = elem.getElementsByTagName(PSXENTRY);
      int sz = nL.getLength();
      Element psxentry = null;
      PSComparablePair pair = null;
      String key = null;
      String value = null;
      for (int k = 0; k < sz; k++)
      {
         psxentry = (Element)nL.item(k);
         key = psxentry.getElementsByTagName(KEY).item(0).getFirstChild().getNodeValue();
         value = psxentry.getElementsByTagName(VALUE).item(0).getFirstChild().getNodeValue();
         pair = new PSComparablePair(key, value);
         list.add(pair);
      }
   }

   private static final String ATCSVC_RESOURCE = "sys_ceSupport/lookup";
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

   /**
    * Client contexts. An array of pairs. Each pair has the key as the first
    * value and the display label as the 2nd value.
    */
   private static final String[][] CLIENT_CONTEXTS =
   {
      //todo: ph - i18n the label
      {"dnd", "Drag and Drop"},
      {"popupMenus","Popup menus"},
      {"singleSelect","Single selection"},
      {"multiSelect","Multiple selection"}
   };

   /**
    * List of all of the roles in the system.  Never <code>null</code> and
    * should not be empty.
    */
   private static List ms_rolesList = new ArrayList();

   /**
    * List of all of the locales in the system.  Never <code>null</code> and
    * should not be empty.
    */
   private static List ms_localesList = new ArrayList();

   /**
    * List of all of the workflows in the system.  Never <code>null</code> and
    * should not be empty.
    */
   private static List ms_workflowList = new ArrayList();

   /**
    * A list of publishable context values, initialized in the first call to
    * {@link #getPublishableContexts()}, never changed after that. Never
    * <code>null</code>, may be empty.
    */
   private static List ms_publishableContexts = new ArrayList();

   /**
    * A list of folder security context values, initialized in the first call to
    * {@link #getFolderSecurityContexts()}, never changed after that. Never
    * <code>null</code>, may be empty.
    */
   private static List ms_folderSecurityContexts = new ArrayList();

   private static List ms_clientContextsList = new ArrayList();
   private static List ms_assingmentTypesList = new ArrayList();
   private static List ms_contentTypesList = new ArrayList();
   private static List ms_checkoutStatusListList = new ArrayList();
   private static List ms_objectTypeList = new ArrayList();
   private static List ms_vcNamesList = new ArrayList();
   private static List ms_communitiesList = null;
   private static PSRemoteRequester m_remoteReq = new PSRemoteRequester(
      E2Designer.getLoginProperties());
   private static Map m_vcMap = new HashMap();

   /**
    * These are the internal values for all the visibility contexts. They
    * correspond to:
    * <ol>
    *    <li>Assignment Type</li>
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
    * These values are obtained from the RXLOOKUP table, key=20. We use them
    * to map to the method used to catalog the values associated with the
    * context.
    */
   private static final String[] VISIBILITY_CONTEXTS =
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

   /**
    * The key for the possible visibility contexts for action menus. This is
    * a key into the RXLOOKUP table. See also {@link #VISIBILITY_CONTEXTS}.
    */
   private static final String VISIBILITY_CONTEXTS_LOOKUP_KEY = "157";

   /**
    * The key for the possible publishable context values for action menus.
    * This is a key into the RXLOOKUP table. See also
    * {@link #VISIBILITY_CONTEXTS}.
    */
   private static final String PUBLISHABLE_CONTEXTS_LOOKUP_KEY = "172";

   /**
    * The key for the possible folder security context values for action menus.
    * This is a key into the RXLOOKUP table. See also
    * {@link #VISIBILITY_CONTEXTS}.
    */
   private static final String FOLDER_SECURITY_CONTEXTS_LOOKUP_KEY = "24";

   /**
    * The HTML parameter name used to supply the key parameter to the lookup
    * resource for global keywords from t the RXLOOKUP table. Never
    * <code>null</code>, empty or changed.
    */
   private static final String RXLOOKUP_KEY_PARAM = "key";

   //XMLNODENAMES
   private static final String PSXENTRY = "PSXEntry";
   private static final String KEY = "Value";
   private static final String VALUE = "PSXDisplayText";
   private static final String PSXCONTENTTYPE = "PSXContentType";
   private static final String ID = "id";
   private static final String NAME = "name";
}
