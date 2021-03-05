/******************************************************************************
 *
 * [ PSExtractorCataloger.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.loader.ui;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSItemChild;
import com.percussion.cms.objectstore.PSItemChildEntry;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSItemFieldMeta;
import com.percussion.cms.objectstore.client.PSContentEditorFieldCataloger;
import com.percussion.cms.objectstore.client.PSLightWeightField;
import com.percussion.cms.objectstore.client.PSRemoteAgent;
import com.percussion.cms.objectstore.client.PSRemoteCataloger;
import com.percussion.cms.objectstore.client.PSRemoteException;
import com.percussion.cms.objectstore.ws.PSClientItem;
import com.percussion.design.objectstore.PSDisplayText;
import com.percussion.design.objectstore.PSEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class is used to catalog info from server.
 */
public class PSExtractorCataloger
{
   /**
    * Constructs an instance from the supplied remote agent.
    *
    * @param remoteAgent The remote agent, assume it is not
    *    <code>null</code>.
    *
    * @throws PSRemoteException if an error occurs while communicate with
    *    the remote server.
    */
   public PSExtractorCataloger(PSRemoteAgent remoteAgent)
      throws PSRemoteException
   {
      m_remoteAgent = remoteAgent;
      createCommunityMap(); // make sure the connection is ok
   }

   /**
    * @return the remote agent, never <code>null</code>.
    */
   public PSRemoteAgent getRemoteAgent()
   {
      return m_remoteAgent;
   }

   /**
    * Get a list of available communities from the remote server.
    *
    * @return An iterator over zero or more <code>PSEntry</code> objects.
    *    Never <code>null</code>, but may be empty. It is sorted by the label 
    *    of the objects if it is not emptry.
    *
    * @throws PSRemoteException if an error occurs.
    */
   public Iterator getCommunities()
      throws PSRemoteException
   {
      createCommunityMap();
      List communities = new ArrayList();
      communities.addAll(m_communityMap.values());
      Collections.sort(communities, new EntryComparator());
      
      return communities.iterator();
   }

   /**
    * Get the community object for the supplied community id.
    *
    * @param communityId The id of the community, it may be <code>null</code>
    *    or empty.
    *
    * @return The community object. It may be <code>null</code> if the
    *    community id does not exit in the remote server.
    *
    * @throws PSRemoteException if an error occurs.
    */
   public PSEntry getCommunity(String communityId)
      throws PSRemoteException
   {
      createCommunityMap();
      return (PSEntry) m_communityMap.get(communityId);
   }

   /**
    * Creates the community map if the map not exist yet. The community map
    * {@link #m_communityMap} is never <code>null</code> after this.
    *
    * @throws PSRemoteException if an error occurs.
    */
   private void createCommunityMap()
      throws PSRemoteException
   {
      if (m_communityMap != null)
         return;

      m_communityMap = new HashMap();

      PSEntry community = null;
      Iterator communities = m_remoteAgent.getCommunities().iterator();
      while (communities.hasNext())
      {
         community = (PSEntry)communities.next();
         List contentTypes = getContentTypes(community);
         List workflows = getWorkflows(community);
         if ((!contentTypes.isEmpty()) && (!workflows.isEmpty()))
            m_communityMap.put(community.getValue(), community);
      }
   }

   /**
    * Get a list of valid content types for the supplied community.
    *
    * @param community The community from where requesting content types,
    *    it may not be <code>null</code>.
    *
    * @return A list of zero or more <code>PSEntry</code>, never
    *    <code>null</code>, but may be empty.
    *
    * @throws PSRemoteException if an error occurs.
    */
   public List getContentTypes(PSEntry community)
      throws PSRemoteException
   {
      if (community == null)
         throw new IllegalArgumentException("community may not be null");

      List contentTypes = (List) m_contentTypesMap.get(community);
      if (contentTypes == null)
      {
         contentTypes = m_remoteAgent.getContentTypes(community);
         Collections.sort(contentTypes, new EntryComparator());
         m_contentTypesMap.put(community, contentTypes);
      }

      return contentTypes;
   }

   /**
    * Get a list of valid workflows for the supplied community.
    *
    * @param community The community from where requesting content types,
    *    it may not be <code>null</code>.
    *
    * @return A list of zero or more <code>PSEntry</code>, never
    *    <code>null</code>, but may be empty. It is sorted by the label 
    *    of the objects if it is not emptry.
    *
    * @throws PSRemoteException if an error occurs.
    */
   public List getWorkflows(PSEntry community)
      throws PSRemoteException
   {
      if (community == null)
         throw new IllegalArgumentException("community may not be null");

      List workflows = (List) m_workflowsMap.get(community);
      if (workflows == null)
      {
         workflows = m_remoteAgent.getWorkflows(community);
         Collections.sort(workflows, new EntryComparator());
         m_workflowsMap.put(community, workflows);
      }

      return workflows;
   }

   /**
    * Get a list of valid transitions for the supplied workflow.
    *
    * @param workflow The workflow from where requesting transitions,
    *    it may not be <code>null</code>.
    *
    * @return A list of zero or more <code>PSEntry</code>, never
    *    <code>null</code>, but may be empty. The value of each
    *    <code>PSEntry</code> is the trigger name of the transition.
    *
    * @throws PSRemoteException if an error occurs.
    */
   public List getTransitions(PSEntry workflow)
      throws PSRemoteException
   {
      if (workflow == null)
         throw new IllegalArgumentException("workflow may not be null");

      List transitions = (List) m_transMap.get(workflow);
      if (transitions == null)
      {
         transitions = m_remoteAgent.getTransitions(workflow);
         m_transMap.put(workflow, transitions);
      }

      return transitions;
   }

   /**
    * Get a list of context variables from the remote server.
    *
    * @return A list over zero or more <code>PSEntry</code> objects. It is
    *    sorted by the label of the objects.
    *
    * @throws PSRemoteException if an error occurs.
    */
   public List getContextVariables()
      throws PSRemoteException
   {
      if (m_ctxVariables == null)
      {
         m_ctxVariables = m_remoteAgent.getContextVariables();
         Collections.sort(m_ctxVariables, new EntryComparator());
      }

      return m_ctxVariables;
   }

   /**
    * Get all field names for the specified content type name.
    *
    * @param contentTypeName The name of the content type, it may not be
    *    <code>null</code> or empty.
    *
    * @return An iterator over zero or more <code>String</code> objects,
    *    never <code>null</code> or empty.
    *
    * @throws PSRemoteException if an error occurs.
    */
   public Iterator getAllFieldNames(String contentTypeName)
      throws PSRemoteException
   {
      // find the content type
      Iterator keys = m_clientItemMap.keySet().iterator();
      PSEntry entry = null;
      PSEntry contentTypeEntry = null;
      while (keys.hasNext())
      {
         entry = (PSEntry) keys.next();
         if (contentTypeName.equalsIgnoreCase(entry.getLabel().getText()))
            contentTypeEntry = entry;
      }

      PSClientItem citem = null;
      if (contentTypeEntry == null)
      {
         citem = m_remoteAgent.newItem(contentTypeName);
         entry = new PSEntry(Integer.toString(citem.getContentId()),
            new PSDisplayText(contentTypeName));
         m_clientItemMap.put(entry, citem);
      }
      else
      {
         citem = (PSClientItem) m_clientItemMap.get(contentTypeEntry);
      }

      return citem.getAllFieldNames();
   }

   /**
    * Get all (system or non-system) fields for a given content type.
    *
    * @param contentType The content-type that contains the requested system
    *    fields, it may not be <code>null</code>.
    *
    * @param getSystemFields <code>true</code> if wants to get a list of
    *    system fields; otherwise it will return a list of non-system fields.
    *
    * @return A list over zero or more <code>PSContentField</code> objects.
    *
    * @throws PSRemoteException if an error occurs.
    */
   public List getFields(PSEntry contentType, boolean getSystemFields)
      throws PSRemoteException
   {
      if (contentType == null)
         throw new IllegalArgumentException("contentType may not be null");

      PSClientItem citem = (PSClientItem) m_clientItemMap.get(contentType);
      if (citem == null)
      {
         citem = m_remoteAgent.newItem(contentType.getLabel().getText());
         m_clientItemMap.put(contentType, citem);
      }

      getLightWeightFields();

      // collecting top level (non-child) fields
      List fields = new ArrayList();
      PSItemField field = null;
      PSContentField cfield = null;
      PSLightWeightField lwField = null;
      String name = null;
      Iterator fieldNames = citem.getAllFieldNames();
      while (fieldNames.hasNext())
      {
         name = (String)fieldNames.next();
         field = citem.getFieldByName(name);
         lwField = (PSLightWeightField) m_wlFieldMap.get(name);
         int sourceType = field.getItemFieldMeta().getSourceType();

         if (getSystemFields &&
             (sourceType == PSItemFieldMeta.SOURCE_TYPE_SYSTEM) )
         {
            cfield = new PSContentField(field, lwField);
            fields.add(cfield);
         }
         else if ( (! getSystemFields) &&
                   (sourceType != PSItemFieldMeta.SOURCE_TYPE_SYSTEM) )
         {
            cfield = new PSContentField(field, lwField);
            fields.add(cfield);
         }
      }

      if (getSystemFields)
         return fields;   // done collecting system fields

      // collecting child fields
      Iterator children = citem.getAllChildren();
      PSItemChild child = null;
      while (children.hasNext())
      {
         child = (PSItemChild) children.next();
         Iterator entries = child.getAllEntries();
         PSItemChildEntry entry = null;
         while (entries.hasNext())
         {
            entry = (PSItemChildEntry) entries.next();
            Iterator childfields = entry.getAllFields();
            while (childfields.hasNext())
            {
               fields.add(childfields.next());
            }
         }
      }

      Collections.sort(fields);

      return fields;
   }

   /**
    * Get all the light weight fields if needed.
    */
   private void getLightWeightFields()
      throws PSRemoteException
   {
      if (m_wlFieldMap == null)
      {
         try
         {
            PSRemoteCataloger rmtCataloger =
               new PSRemoteCataloger(m_remoteAgent.getRemoteRequester());
            PSContentEditorFieldCataloger fieldCatalogger =
               new PSContentEditorFieldCataloger(rmtCataloger, null,
                     PSRemoteCataloger.FLAG_INCLUDE_ALL);

            m_wlFieldMap = fieldCatalogger.getAll();
         }
         catch (PSCmsException e)
         {
            throw new PSRemoteException(e);
         }
      }
   }

   /**
    * This class is used to sort a list of <code>PSEntry</code> objects.
    * See {@link java.util.Collections#sort(List, Comparator)} and
    * {@link java.util.Comparator}.
    */
   private class EntryComparator implements Comparator
   {
      // Implements {@link Comparator#compare(Object, Object)}
      public int compare(Object o1, Object o2)
      {
         if ((!(o1 instanceof PSEntry)) || (!(o2 instanceof PSEntry)))
         {
            throw new IllegalArgumentException(
               "o1 and o2 must be instance of PSEntry");
         }
         
         String label1 = ((PSEntry) o1).getLabel().getText();
         String label2 = ((PSEntry) o2).getLabel().getText();
         
         return label1.compareTo(label2);
      }
      
      /**
       * Implements {@link Comparator#equals(Object)}
       */ 
      @Override
      public boolean equals(Object o)
      {
         // this is not used for sorting the entry list, simply return true
         return true; 
      }

      @Override
      public int hashCode()
      {
         throw new UnsupportedOperationException("Not Implemented");
      }
   }
   

   /**
    * The remote agent used to communicate with the remote server.
    * Initialized by constructor, never <code>null</code> after that.
    */
   private PSRemoteAgent m_remoteAgent;

   /**
    * The community map, which contains a list of available communities
    * from the remote server. The map key is the community id (as
    * <code>String</code>); the map value is the community object (as
    * <code>PSEntry</code>).
    * Initialized by {@link #createCommunityMap()}, never <code>null</code>
    * after that.
    */
   private Map m_communityMap = null;

   /**
    * It maps a community to a list available content types of the community.
    * The map key is the community as <code>PSEntry</code>; the map value is
    * a list (as <code>List</code>) over zero or more <code>PSEntry</code>
    * content-type objects.
    * It never <code>null</code>, but may be empty.
    */
   private Map m_contentTypesMap = new HashMap();

   /**
    * A list of context variables from the remote server. It is a list
    * over zero or more <code>PSEntry</code> objects. Initialized by
    * {@link #getContextVariables()}, never <code>null</code> after that.
    */
   private List m_ctxVariables = null;

   /**
    * It maps a community to a list of valid workflows of the community.
    * The map key is the community as <code>PSEntry</code>; the map value is
    * a list (as <code>List</code>) over zero or more <code>PSEntry</code>
    * workflow objects.
    * It never <code>null</code>, but may be empty.
    */
   private Map m_workflowsMap = new HashMap();

   /**
    * It maps a workflow to a list of valid transitions of the workflow.
    * The map key is the workflow as <code>PSEntry</code>; the map value is
    * a list (as <code>List</code>) over zero or more <code>PSEntry</code>
    * transition objects. The value of the transition object is the trigger
    * or internal name.
    * It never <code>null</code>, but may be empty.
    */
   private Map m_transMap = new HashMap();

   /**
    * It maps a content type to a related empty client item. The map key
    * is the content-type (as <code>PSEntry</code>); the map value is
    * the empty client item (as <code>PSClientItem</code>).
    * It never <code>null</code>, but may be empty.
    */
   private Map m_clientItemMap = new HashMap();

   /**
    * The map for all light weight fields. The map key is the field (internal)
    * name (as String), the map value is the <code>PSLightWeightField</code>.
    */
   private Map m_wlFieldMap = null;
}

