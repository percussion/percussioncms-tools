/******************************************************************************
 *
 * [ PSExtractorConfigContext.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.loader.ui;

import com.percussion.cms.objectstore.client.PSRemoteAgent;
import com.percussion.cms.objectstore.client.PSRemoteException;
import com.percussion.design.objectstore.PSDisplayText;
import com.percussion.design.objectstore.PSEntry;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.loader.IPSLogCodes;
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.PSLogMessage;
import com.percussion.loader.objectstore.PSComponentList;
import com.percussion.loader.objectstore.PSExtractorDef;
import com.percussion.loader.objectstore.PSFieldProperty;
import com.percussion.loader.objectstore.PSStaticItemExtractorDef;
import com.percussion.loader.objectstore.PSTransitionDef;
import com.percussion.loader.objectstore.PSWorkflowDef;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is used as the repository for the edited extractor definition.
 * It also contains various cataloger and accessor methods for the extractor
 * editors. The catalogged information will be cached as a singleton object,
 * so that all extractors will share the same cache. It caches only one server
 * info at a time, the server connection info is set by
 * {@link #setRemoteAgent(PSRemoteAgent)}.
 */
public class PSExtractorConfigContext
{

   /**
    * Get a new object of this class. Caller is responsible to
    * call {@link #setRemoteAgent(PSRemoteAgent)} and
    * {@link #setExtractorDef(PSExtractorDef)} before call any other methods
    * within this class.
    *
    * @return the new object, never <code>null</code>.
    */
   public static PSExtractorConfigContext getInstance()
   {
      return new PSExtractorConfigContext();
   }

   /**
    * Constructs an instance from a remote agent. Caller is responsible to call
    * {@link #setExtractorDef(PSExtractorDef)} afterwards.
    *
    * @param    remoteAgent The remote agent, which is used to communicate
    *           with the remote server. It may not be <code>null</code>.
    *
    * @return <code>true</code> if the supplied remote agent will be used
    *    to communicate with the remote server; <code>false</code> if the
    *    supplied remote agent will not be used because its connection info
    *    is the same as the existing one in this object.
    *
    * @throws PSLoaderException if an error occurs while communicate with
    *    the remote server.
    */
   public boolean setRemoteAgent(PSRemoteAgent remoteAgent)
      throws PSLoaderException
   {
      if (remoteAgent == null)
         throw new IllegalArgumentException("remoteAgent may not be null");

      return createCatalogger(remoteAgent);
   }

   /**
    * Set the extractor definition.
    *
    * @param extractorDef The to be set extractor definition, it may not be
    *    <code>null</code> or empty.
    *
    * @return <code>true</code> if it is a valid definition; <code>false</code>
    *    if some of the properties are invlid.
    *
    * @throws PSLoaderException if an error occurs
    */
   public boolean setExtractorDef(PSExtractorDef extractorDef)
      throws PSLoaderException
   {
      if (extractorDef == null)
         throw new IllegalArgumentException("extractorDef may not be null");

      m_itemExtractorDef = null;
      m_staticExtractorDef = null;
      m_extractorDef = extractorDef;

      if (m_extractorDef.isStaticType())
      {
         m_staticExtractorDef = new StaticExtractorDef();
         return m_staticExtractorDef.populateExtractorDef();
      }
      else
      {
         m_itemExtractorDef = new ItemExtractorDef();
         return m_itemExtractorDef.populateExtractorDef();
      }
   }

   /**
    * Updates the extractor definition from its repository, re-validating
    * the current extractor definition. This should be done after the
    * connection information is modified for the current descriptor, which
    * contains the extractor definition.
    *
    * @return <code>true</code> if the updated extractor definition is still
    *    valid; <code>false</code> otherwise.
    *
    * @throws IllegalStateException if {@link #validateExtractorState()}
    *    failed the validating.
    *
    * @throws PSLoaderException if an error occurs
    */
   public boolean resetExtractor()
      throws PSLoaderException
   {
      validateExtractorState();

      if (validateExtractorDef())
      {
         m_extractorDef = getUpdatedExtractorDef();
         return setExtractorDef(m_extractorDef);
      }
      else
      {
         return false;
      }
   }

   /**
    * Get the current context variable for the static extractor definition.
    *
    * @return The context variable, it may be <code>null</code> if it is
    *    not defined.
    *
    * @throws IllegalStateException if {@link #validateExtractorState()}
    *    failed the validating.
    */
   public PSEntry getContextVariable()
   {
      validateExtractorState();

      return m_staticExtractorDef.getContextVariable();
   }

   /**
    * Set the current context variable for the static extractor definition.
    *
    * @param cntVariable The to be set content variable, it may not be
    *    <code>null</code>.
    *
    * @throws IllegalStateException if {@link #validateExtractorState()}
    *    failed the validating.
    */
   public void setContextVariable(PSEntry cntVariable)
   {
      if (cntVariable == null)
         throw new IllegalArgumentException("cntVariable may not be null");

      validateExtractorState();

      m_staticExtractorDef.setContextVariable(cntVariable);
   }

   /**
    * Get the (original) extractor definition which is from
    * {@link #setExtractorDef(PSExtractorDef)}.
    *
    * @return The extractor definition, never <code>null</code>. If the
    *    definition is static type, then it is an instance of
    *    {@link com.percussion.loader.objectstore.PSStaticItemExtractorDef}.
    *
    * @throws IllegalStateException if the extractor definition has not
    *    been set yet.
    */
   public PSExtractorDef getExtractorDef()
   {
      if (m_extractorDef == null)
         throw new IllegalStateException("m_extractorDef has not been set yet");

      if (m_extractorDef.isStaticType() &&
          (!(m_extractorDef instanceof PSStaticItemExtractorDef)))
      {
         throw new IllegalStateException("m_extractorDef is static type, " +
            "but is not an instance of PSStaticItemExtractorDef");
      }

      return m_extractorDef;
   }

   /**
    * Get the updated extractor definition, which may be different from the
    * original one.
    *
    * @return The updated extractor definition, never <code>null</code>.
    *
    * @throws IllegalStateException if {@link #validateExtractorState()}
    *    failed the validating.
    */
   public PSExtractorDef getUpdatedExtractorDef()
   {
      validateExtractorState();

      if (m_extractorDef.isStaticType())
      {
         return m_staticExtractorDef.getUpdatedExtractorDef();
      }
      else
      {
         return m_itemExtractorDef.getUpdatedExtractorDef();
      }

   }

   /**
    * Validating current content of the extractor definition.
    *
    * @return <code>true</code> if the extractor definition contains enough
    *    valid information for extracting items; <code>false</code> otherwise.
    */
   public boolean validateExtractorDef()
   {
      if (m_extractorDef.isStaticType())
      {
         return m_staticExtractorDef.validateExtractorDef();
      }
      else
      {
         return m_itemExtractorDef.validateExtractorDef();
      }
   }

   /**
    * Get the current community. The called must set the extractor definition
    * first.
    *
    * @return The current community, never <code>null</code>.
    *
    * @throws IllegalStateException if {@link #validateExtractorState()}
    *    failed the validating.
    */
   public PSEntry getCommunity()
   {
      validateExtractorState();
      return m_itemExtractorDef.getCommunity();

   }

   /**
    * Set a new community. Do nothing if it is the same with the current
    * community; otherwise, all data (content-type, fields, workflow, ...etc)
    * will be reset to <code>null</code> or empty.
    *
    * @param community  The to be set community, it may not be<code>null</code>.
    *
    * @throws IllegalStateException if {@link #validateExtractorState()}
    *    failed the validating.
    */
   public void setCommunity(PSEntry community)
   {
      validateExtractorState();
      if (community == null)
         throw new IllegalArgumentException("community may not be null");

      m_itemExtractorDef.setCommunity(community);

   }

   /**
    * Make sure the current state of the context is valid for either
    * item or static extractor.
    *
    * @throws IllegalStateException if the remote agent (for the catalogger)
    * has not been set or the repository of the extractor definition has not
    * been set yet.
    */
   public void validateExtractorState()
   {
      if (ms_cataloger == null)
         throw new IllegalStateException("ms_cataloger has not been set");

      if (m_extractorDef.isStaticType())
      {
         if (m_staticExtractorDef == null)
            throw new IllegalStateException(
               "Community is not defined for static extractor");
      }
      else
      {
         if (m_itemExtractorDef == null)
            throw new IllegalStateException(
              "m_itemExtractorDef has not been set");
      }
   }

   /**
    * Get a list of communities from the remote server.
    *
    * @return An iterator over zero or more <code>PSEntry</code> objects,
    *    never <code>null</code>, but may be empty.
    *
    * @throws IllegalStateException if {@link #validateExtractorState()}
    *    failed the validating.
    *
    * @throws PSLoaderException if an error occurs.
    */
   public Iterator getCommunities()
      throws PSLoaderException
   {
      try
      {
          return ms_cataloger.getCommunities();
      }
      catch(PSRemoteException re)
      {
          throw new PSLoaderException(re);
      }
   }


   /**
    * Get the current content type that is defined by the extractor definition.
    *
    * @return The content type, it may be <code>null</code> if the
    *    content type is invalid or unknown.
    *
    * @throws IllegalStateException if {@link #validateExtractorState()}
    *    failed the validating.
    */
   public PSEntry getContentType()
   {
      validateExtractorState();

      return m_itemExtractorDef.getContentType();
   }

   /**
    * Set a new content type. Do nothing if the new content type is the same
    * as the current one; otherwise, the system and non-system field lists will
    * be set to empty.
    *
    * @param    contentType The to be set content type, it may not be
    *    <code>null</code>.
    *
    * @throws IllegalStateException if {@link #validateExtractorState()}
    *    failed the validating.
    */
   public void setContentType(PSEntry contentType)
   {
      if (contentType == null)
         throw new IllegalArgumentException("contentType may not be null");
      validateExtractorState();

      m_itemExtractorDef.setContentType(contentType);
   }

   /**
    * Get a list of content types that are valid within the current community.
    *
    * @return An iterator over zero or more <code>PSEntry</code> objects,
    *    never <code>null</code>, but may be empty.
    *
    * @throws IllegalStateException if {@link #validateExtractorState()}
    *    failed the validating.
    *
    * @throws PSLoaderException if an error occurs.
    */
   public Iterator getContentTypes()
      throws PSLoaderException
   {
      validateExtractorState();

      try
      {
         PSEntry community = getCommunity();
         return ms_cataloger.getContentTypes(community).iterator();
      }
      catch(PSRemoteException re)
      {
          throw new PSLoaderException(re);
      }
   }

   /**
    * Get system or non-system fields which are defined in the current content
    * type. User must first call {@link #setExtractorDef(PSExtractorDef)}.
    *
    * @param getSystemFields <code>true</code> if need to get a list of
    *    system fields of the current content type; otherwise, return a list
    *    of non-system fields.
    *
    * @return An iterator over zero or more <code>PSFieldProperty</code>.
    *
    * @throws IllegalStateException if {@link #validateExtractorState()}
    *    failed the validating.
    */
   public Iterator getDefinedFields(boolean getSystemFields)
   {
      validateExtractorState();

      return m_itemExtractorDef.getFields(getSystemFields);
   }

   /**
    * Convenient method to get a list of fields for the supplied content type.
    * Calls to <code>ms_cataloger.getFields(contentType, getSystemFields)
    * </code>, log and return empty list if an error occurs.
    *
    * @param contentType The specified content type, assume it is not
    *    <code>null</code>.
    *
    * @param getSystemFields <code>true</code> if wants to get a list of
    *    system fields of the specified content type; otherwise, return a list
    *    of non-system fields.
    *
    * @return A list of zero or more <code>PSContentField</code> objects.
    *    It is empty if an exception occurs.
    */
   private List getAvailableFields2(PSEntry contentType,
      boolean getSystemFields)
   {
      try
      {
         return ms_cataloger.getFields(contentType, getSystemFields);
      }
      catch (PSRemoteException e)
      {
         // log and return empty list
         Logger.getLogger(getClass()).error(e.getLocalizedMessage());

         String[] args = {contentType.getLabel().getText()};
         PSLogMessage   msg = new PSLogMessage(
            IPSLogCodes.FAILED_GET_CONTENTTYPE_FIELDS,
            args, PSLogMessage.LEVEL_ERROR);
         Logger.getLogger(getClass()).error(msg);

         return new ArrayList();
      }
   }

   /**
    * Get all (system or non-system) fields for the supplied content type.
    *
    * @param contentType The specified content type, it may not be
    *    <code>null</code>.
    *
    * @param getSystemFields <code>true</code> if wants to get a list of
    *    system fields of the specified content type; otherwise, return a list
    *    of non-system fields.
    *
    * @return An iterator over zero or more <code>PSContentField</code> objects.
    *    It will not include the fields for community-id and workflow-id.
    */
   public Iterator getAvailableFields(PSEntry contentType,
      boolean getSystemFields)
   {
      if (getSystemFields)
      {
         List fields = new ArrayList();
         PSContentField field;

         Iterator allFields =
            getAvailableFields2(contentType, getSystemFields).iterator();
         while (allFields.hasNext())
         {
            field = (PSContentField) allFields.next();
            boolean isReturnedField =
               (!field.getFieldName().equals(IPSHtmlParameters.SYS_COMMUNITYID))
               &&
               (!field.getFieldName().equals(IPSHtmlParameters.SYS_WORKFLOWID));

            if (isReturnedField)
            {
               fields.add(field);
            }
         }

         return fields.iterator();

      }
      else
      {
         return getAvailableFields2(contentType, getSystemFields).iterator();
      }
   }

   /**
    * Set the system or non-system fields for the current item extractor.
    *
    * @param fields The to be set fields. It is a list over zero or more
    *    <code>PSFieldProperty</code> objects. It may not be <code>null</code>.
    *
    * @param setSystemFields <code>true</code> if wants to set the system
    *    fields for the current content type; otherwise, set the non-system
    *    fields.
    *
    * @throws IllegalStateException if {@link #validateExtractorState()}
    *    failed the validating.
    */
   public void setDefinedFields(List fields, boolean setSystemFields)
   {
      if (fields == null)
         throw new IllegalArgumentException("fields may not be null");

      validateExtractorState();

      m_itemExtractorDef.setFields(fields, setSystemFields);
   }

   /**
    * Get the workflow that is defined in the current item extractor.
    *
    * @return the workflow, it may be <code>null</code> if workflow is not
    *    defined or invalid.
    *
    * @throws IllegalStateException if {@link #validateExtractorState()}
    *    failed the validating.
    */
   public PSEntry getWorkflow()
   {
      validateExtractorState();

      return m_itemExtractorDef.getWorkflow();
   }

   /**
    * Set the workflow for the current item extractor.
    *
    * @param workflow  The to be set workflow, it may be <code>null</code>.
    *
    * @throws IllegalStateException if {@link #validateExtractorState()}
    *    failed the validating.
    */
   public void setWorkflow(PSEntry workflow)
   {
      validateExtractorState();

      m_itemExtractorDef.setWorkflow(workflow);
   }

   /**
    * Get a list of specified transitions.
    *
    * @param whichTrans The transition set, it must be one of the
    *    <code>PSWorkflowDef.TRANS_XXX</code> values.
    *
    * @return An iterator over zero or more <code>PSEntry</code> objects.
    *    Never <code>null</code>, but may be empty.
    *
    * @throws IllegalStateException if {@link #validateExtractorState()}
    *    failed the validating.
    */
   public Iterator getTransitions(int whichTrans)
   {
      validateExtractorState();
      PSWorkflowDef.validateTransSet(whichTrans);

      return m_itemExtractorDef.getTransitions(whichTrans);
   }

   /**
    * Set the specified transitions for the current workflow definition.
    *
    * @param transitions The to be set transitions, it may not be
    *    <code>null</code>.
    *
    * @param whichTrans The transition set, it must be one of the
    *    <code>PSWorkflowDef.TRANS_XXX</code> values.
    *
    * @throws IllegalStateException if {@link #validateExtractorState()}
    *    failed the validating.
    */
   public void setTransitions(List transitions, int whichTrans)
   {
      validateExtractorState();
      PSWorkflowDef.validateTransSet(whichTrans);

      if (transitions == null)
         throw new IllegalArgumentException("transitions may not be null");

      m_itemExtractorDef.setTransitions(transitions, whichTrans);
   }

   /**
    * Get all field name for the specified content type.
    *
    * @param contentType The name of the content type, it may not be
    *    <code>null</code> or empty.
    *
    * @return An iterator over zero or more <code>String</code> objects,
    *    never <code>null</code>, but may by empty.
    *
    * @throws PSLoaderException if an error occurs.
    */
   public Iterator getAllFieldNames(String contentType)
      throws PSLoaderException
   {
      if (contentType == null || contentType.trim().length() == 0)
         throw new IllegalArgumentException(
            "contentType may not be null or empty");
      try
      {
          return ms_cataloger.getAllFieldNames(contentType);
      }
      catch(PSRemoteException re)
      {
          throw new PSLoaderException(re);
      }
   }

   /**
    * Get a list of workflows which are valid in the current community.
    *
    * @return An iterator over zero or more <code>PSEntry</code>, never
    *    <code>null</code>, but may be empty.
    *
    * @throws IllegalStateException if {@link #validateExtractorState()}
    *    failed the validating.
    *
    * @throws PSLoaderException if an error occurs while communicate with the
    *    remote server.
    */
   public Iterator getWorkflows()
      throws PSLoaderException
   {
      validateExtractorState();

      try
      {
         PSEntry community = getCommunity();
         return ms_cataloger.getWorkflows(community).iterator();
      }
      catch(PSRemoteException re)
      {
          throw new PSLoaderException(re);
      }
   }

   /**
    * Get a list of valid transitions for the supplied workflow
    *
    * @param  workflow  The workflow from where requesting the transitions,
    *    it may not be <code>null</code>.
    *
    * @return An iterator over zero or more <code>PSEntry</code>, never
    *    <code>null</code>, but may be empty. The value of each
    *    <code>PSEntry</code> is the trigger name of the transition.
    *
    * @throws PSLoaderException if an error occurs.
    */
   public Iterator getTransitions(PSEntry workflow)
      throws PSLoaderException
   {
      if (workflow == null)
         throw new IllegalArgumentException("workflow may not be null");

      Iterator it = null;
      try
      {
          it = ms_cataloger.getTransitions(workflow).iterator();
      }
      catch(PSRemoteException re)
      {
          throw new PSLoaderException(re);
      }
      return it;
   }

   /**
    * Get a list of context variables from the remote server.
    *
    * @return An iterator over zero or more <code>PSEntry</code> objects,
    *    never <code>null</code>, but may be empty.
    *
    * @throws PSLoaderException if an error occurs.
    */
   public Iterator getContextVariables()
      throws PSLoaderException
   {
      try
      {
          return ms_cataloger.getContextVariables().iterator();
      }
      catch(PSRemoteException re)
      {
          throw new PSLoaderException(re);
      }
   }

   /**
    * Creates a new catalogger from the supplied remote agent if needed.
    * Do nothing if the given remote agent is the same as the current one, so
    * that the cached data can be re-userd later.
    *
    * @param remoteAgent The remote agent, assume not <code>null</code>.
    *
    * @return <code>true</code> if the supplied remote agent will be used
    *    to communicate with the remote server; <code>false</code> if the
    *    supplied remote agent will not be used because its connection info
    *    is the same as the one that is currently used by this object.
    *
    * @throws PSLoaderException if an error occurs while communicate with
    *    the remote server.
    */
   private boolean createCatalogger(PSRemoteAgent remoteAgent)
      throws PSLoaderException
   {
      boolean useTheRemoteAgent = true;

      try
      {

         if (ms_cataloger == null)
         {
            ms_cataloger = new PSExtractorCataloger(remoteAgent);
         }
         else // discard the old, create a new one if server connection info
         {    // is different between the new remote agent and the current one.

            if (! ms_cataloger.getRemoteAgent().equals(remoteAgent))
               ms_cataloger = new PSExtractorCataloger(remoteAgent);
            else
               useTheRemoteAgent = false; // same connection info.
         }
      }
      catch(PSRemoteException re)
      {
          throw new PSLoaderException(re);
      }

      return useTheRemoteAgent;
   }

   /**
    * The edited extractor definition. Initialized by constructor, never
    * <code>null</code> after that.
    */
   private PSExtractorDef m_extractorDef;

   /**
    * The singleton catalogger object. It is initialized or reset by the
    * {@link #setRemoteAgent()}, never <code>null</code> after that.
    */
   private static PSExtractorCataloger ms_cataloger = null;

   /**
    * The repository for item extractor definition. It contains valid data only.
    * Initialized by {@link #setExtractorDef(PSExtractorDef)}. It may be
    * <code>null</code> if the current extractor def is static type.
    */
   private ItemExtractorDef m_itemExtractorDef = null;

   /**
    * The repository for static extractor definition. It contains valid data
    * only. Initialized by {@link #setExtractorDef(PSExtractorDef)}. It may
    * be <code>null</code> if current extractor def is not static type.
    */
   private StaticExtractorDef m_staticExtractorDef = null;

   /**
    * This is the repository for the static extractor definition.
    * It validates the definition and ignore the invalid entries if there is
    * any.
    */
   private class StaticExtractorDef
   {
      /**
       * Populating current extractor definition to this object. This method
       * also converts the <code>m_extractorDef</code> to an instance of
       * <code>PSStaticItemExtractorDef</code>.
       *
       * @return <code>true</code> if the context variable in the
       *    extractor definition are valid; <code>false</code> otherwise.
       *
       * @throws PSLoaderException if an error occured.
       */
      private boolean populateExtractorDef()
         throws PSLoaderException
      {
         boolean valid = false;
         m_contextVariable = null;

         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         Element defEl = m_extractorDef.toXml(doc);
         PSStaticItemExtractorDef def = null;
         try
         {
            def = new PSStaticItemExtractorDef(defEl);
         }
         catch (PSUnknownNodeTypeException e)
         {
            throw new PSLoaderException(e);
         }

         PSEntry defVar = new PSEntry(def.getTargetLocation(),
            new PSDisplayText(def.getContextVariableName()));

         // validating the defined context variable
         PSEntry var = null;
         Iterator variables = getContextVariables();
         while (variables.hasNext())
         {
            var = (PSEntry) variables.next();
            if (var.equals(defVar))
            {
               m_contextVariable = var;
               valid = true;
               break;
            }
         }

         m_extractorDef = def; // convert to static definition for later use.

         return valid;
      }

      /**
       * Set the context variable.
       *
       * @param variable The to be set context variable, assume it is not
       *    <code>null</code>.
       */
      private void setContextVariable(PSEntry variable)
      {
         m_contextVariable = variable;
      }

      /**
       * See {@link PSExtractorConfigContext#getContextVarialbe()} for
       * its description.
       */
      private PSEntry getContextVariable()
      {
         return m_contextVariable;
      }

      /**
       * See {@link PSExtractorConfigContext#validateExtractorDef()} for its
       * description.
       */
      private boolean validateExtractorDef()
      {
         return m_contextVariable != null;
      }


      /**
       * Get the updated extractor definition.
       *
       * @return the extractor def, never <code>null</code>.
       *
       * @throws IllegalStateException if the content variable is
       *    <code>null</code>.
       */
      private PSExtractorDef getUpdatedExtractorDef()
      {
         if (m_contextVariable == null)
            throw new IllegalStateException(
               "Context variable has not been set yet");

         // already converted to static definition in #populateExtractorDef()
         PSStaticItemExtractorDef def =
            (PSStaticItemExtractorDef)m_extractorDef;

         try
         {
            def.setTargetLocation(m_contextVariable.getValue());
         }
         catch (PSLoaderException e) // this is not possible
         {
            throw new RuntimeException("Unexpected exception: " + e.toString());
         }

         def.setContextVariableName(m_contextVariable.getLabel().getText());

         return def;
      }

      /**
       * The defined context variable. It may be <code>null</code> if the
       * original one is invalid or has not been set yet.
       */
      private PSEntry m_contextVariable = null;
   }

   /**
    * This class contains the data that are specific for item extractor
    * definition, such as community, content-type, fields and workflow.
    * All data are validated when populating the current extractor definition
    * to the instance of this class. The invalidated data will be ignored if
    * there is any.
    */
   private class ItemExtractorDef
   {
      /**
       * Populating current extractor definition to this object.
       *
       * @return <code>true</code> if the data in the extractor definition are
       *    valid; <code>false</code> otherwise.
       *
       * @throws PSLoaderException if an error occured while communicating to
       *    the remote server.
       */
      private boolean populateExtractorDef()
         throws PSLoaderException
      {
         boolean valid = false;

         reset();
         try
         {
            if (populateCommunity())
            {
               if (populateContentType())
               {
                  boolean validFields = populateFields();

                  if (populateWorkflow() && validFields)
                     valid = true;
               }
            }
         }
         catch(PSRemoteException re)
         {
            throw new PSLoaderException(re);
         }

         return valid;
      }

      /**
       * Set a new community. Do nothing if it is the same with the current
       * community; otherwise, all data (content-type, fields, workflow, ...etc)
       * will be reset to <code>null</code> or empty.
       *
       * @param community  The to be set community, assume not
       *    <code>null</code>.
       */
      private void setCommunity(PSEntry community)
      {
         if (m_community.equals(community))
            return;

         reset();
         m_community = community;
      }

      /**
       * Set a new content type. Do nothing if the new content type is the same
       * as the current one; otherwise, the system and non-system field lists
       * will be set to empty.
       *
       * @param    contentType The to be set content type, assume it is not
       *    <code>null</code>.
       */
      private void setContentType(PSEntry contentType)
      {
         if (null != m_contentType && m_contentType.equals(contentType))
            return;

         m_contentType = contentType;
         m_systemFields = new ArrayList();
         m_nonSystemFields = new ArrayList();
      }

      /**
       * Reset or cleanup data, set the data to <code>null</code> or empty.
       * This should be called before populating a new extractor definition.
       */
      private void reset()
      {
         m_community = null;
         m_contentType = null;
         m_workflow = null;

         m_transitions = new List[PSWorkflowDef.MAX_TRANS];
         for (int i=0; i < PSWorkflowDef.MAX_TRANS; i++)
            m_transitions[i] = new ArrayList();

         m_systemFields = new ArrayList();
         m_nonSystemFields = new ArrayList();
      }

      /**
       * Populates the fields that are defined in the current extractor
       * definition.
       *
       * @return <code>true</code> if completed the process without any error;
       *    <code>false</code> if one of the field is invalid in the
       *    current content-type.
       *
       * @throws PSRemoteException if an error occurs while communicating with
       *    the remote server.
       */
      private boolean populateFields()
         throws PSRemoteException
      {
         boolean valid = true;
         List systemFields = ms_cataloger.getFields(m_contentType, true);
         List nonSystemFields = ms_cataloger.getFields(m_contentType, false);

         Iterator fields = m_extractorDef.getFieldProperties();
         PSFieldProperty prop = null;
         while (fields.hasNext())
         {
            prop = (PSFieldProperty) fields.next();
            if (containsField(systemFields, prop))
            {
               // skip sys_workflow and sys_communityid
               if ((!prop.getName().equals(IPSHtmlParameters.SYS_COMMUNITYID))
                && (!prop.getName().equals(IPSHtmlParameters.SYS_WORKFLOWID)))
               {
                  m_systemFields.add(prop);
               }
            }
            else if (containsField(nonSystemFields, prop))
            {
               m_nonSystemFields.add(prop);
            }
            else // invalid field, log it
            {
               Object[] args = {prop.getName(),
                     m_contentType.getLabel().getText()};
               PSLogMessage msg = new PSLogMessage(
                  IPSLogCodes.INVALID_FIELD_NAME, args,
                  PSLogMessage.LEVEL_WARN);
               Logger.getLogger(this.getClass()).warn(msg);

               valid = false;
            }
         }

         return valid;
      }

      /**
       * Determines whether the specified list of fields contains the supplied
       * field property.
       *
       * @param fields The list of fields, zero or more <code>PSContentField
       *    </code> objects. Assume not <code>null</code>.
       *
       * @param prop The field property, assume not <code>null</code>.
       *
       * @return <code>true</code> if the name of the field property exist in
       *    the list of fields; <code>false</code> otherwise.
       */
      private boolean containsField(List fieldList, PSFieldProperty prop)
      {
         PSContentField field = null;
         Iterator fields = fieldList.iterator();
         while (fields.hasNext())
         {
            field = (PSContentField) fields.next();
            if (field.getFieldName().equalsIgnoreCase(prop.getName()))
               return true;
         }

         return false;
      }

      /**
       * See {@link PSExtractorConfigContext#getFields(boolean)} for its
       * description.
       */
      private Iterator getFields(boolean getSystemFields)
      {
         if (getSystemFields)
            return m_systemFields.iterator();
         else
            return m_nonSystemFields.iterator();
      }

      /**
       * See {@link PSExtractorConfigContext#setFields(List)} for its
       * description.
       */
      private void setFields(List fields, boolean setSystemFields)
      {
         if (setSystemFields)
            m_systemFields = fields;
         else
            m_nonSystemFields = fields;
      }

      /**
       * Set the current community, which is either from the extractor
       * definition if there is a valid "sys_communityid" field property;
       * otherwise it is the default community of the login user. The login
       * user is defined in the remote agent
       * (see {@link #PSExtractorConfigContext(PSRemoteAgent)}).
       *
       * @return <code>true</code> if extractor def does not contain an invalid
       *    community id; <code>false</code> otherwise.
       *
       * @throws PSRemoteException if an error occurs while communicating with
       *    the remote server.
       */
      private boolean populateCommunity()
         throws PSRemoteException
      {
         PSEntry community = null;
         boolean valid = true;

         // try to get the community defined in extractor first.
         String id = m_extractorDef.getFieldValue(
            IPSHtmlParameters.SYS_COMMUNITYID);
         if (id != null)
         {
            community = ms_cataloger.getCommunity(id);
            if (community == null) // log the invalid community id
            {
               Object[] args = {id, m_extractorDef.getName()};
               PSLogMessage msg = new PSLogMessage(
                  IPSLogCodes.INVALID_COMMUNITY_ID, args,
                  PSLogMessage.LEVEL_WARN);
               Logger.getLogger(this.getClass()).warn(msg);

               valid = false;
            }
         }

         if (community == null)
            community = ms_cataloger.getRemoteAgent().getDefaultUserCommunity();

         m_community = community;

         return valid;
      }

      /**
       * Set the content type from the current extractor definition.
       *
       * @return <code>true</code> if set the content type without any error;
       *    <code>false</code> if the content type of the extractor definition
       *    is invalid.
       *
       * @throws PSLoaderException if an error occured while communicating with
       *    the remote server.
       */
      private boolean populateContentType()
         throws PSLoaderException
      {
         PSEntry contentType = null;
         String name = null;
         try
         {
            name = m_extractorDef.getContentTypeName();
         }
         catch (PSLoaderException e) // should not happen
         {
            throw new IllegalStateException(e.toString());
         }

         Iterator ctypes = getContentTypes();
         while (ctypes.hasNext())
         {
            contentType = (PSEntry) ctypes.next();
            if (name.equalsIgnoreCase(contentType.getLabel().getText()))
            {
               m_contentType = contentType;
               return true; // found it, it is a valid content type
            }
         }

         // Unable to find the content type, log it and return false
         PSEntry community = getCommunity();
         Object[] args = {name, community.toString()};
         PSLogMessage msg = new PSLogMessage(
            IPSLogCodes.INVALID_CONTENTTYPE_NAME, args,
            PSLogMessage.LEVEL_WARN);
         Logger.getLogger(this.getClass()).warn(msg);

         return false;
      }

      /**
       * Set the current workflow from the current extractor definition.
       *
       * @return <code>true</code> if the workflow has been set without error;
       *    <code>false</code> if the extractor definition contains an invalid
       *    workflow id.
       *
       * @throws PSRemoteException if an error occured while communicating with
       *    the remote server.
       */
      private boolean populateWorkflow()
         throws PSRemoteException
      {
         boolean valid = true;
         m_workflow = null;

         // try to get the community defined in extractor first.
         String id = m_extractorDef.getFieldValue(
            IPSHtmlParameters.SYS_WORKFLOWID);
         String name = null;
         if (m_extractorDef.getWorkflowDef() != null)
            name = m_extractorDef.getWorkflowDef().getName();

         if (id != null || name != null) // find the matching workflow
         {
            Iterator workflows =
               ms_cataloger.getWorkflows(m_community).iterator();
            PSEntry workflow = null;
            while (workflows.hasNext())
            {
               workflow = (PSEntry) workflows.next();
               if (id != null)   // test id only if defined
               {
                  if (workflow.getValue().equals(id))
                  {
                     m_workflow = workflow;
                     break;
                  }
               }                // test name if id is not defined
               else if (workflow.getLabel().getText().equalsIgnoreCase(name))
               {
                  m_workflow = workflow;
                  break;
               }
            }

            if (m_workflow == null) // log the invalid workflow id
            {
               PSLogMessage msg = null;

               if (id != null)
               {
                  Object[] args = {id, m_community.getLabel().getText()};
                  msg = new PSLogMessage(IPSLogCodes.INVALID_WORKFLOW_ID, args,
                     PSLogMessage.LEVEL_WARN);
               }
               else
               {
                  Object[] args = {name, m_community.getLabel().getText()};
                  msg = new PSLogMessage(IPSLogCodes.INVALID_WORKFLOW_NAME,
                     args, PSLogMessage.LEVEL_WARN);
               }
               Logger.getLogger(this.getClass()).warn(msg);

               valid = false;
            }
            else
            {
               valid = populateTransitions();
            }
         }

         return valid;
      }

      /**
       * Validating and set the transitions for the current workflow definition.
       *
       * @return <code>true</code> if the transitions has been populated
       *    without error; <code>false</code> if the extractor definition
       *    contains an invalid tarnsitions, which will be ignored.
       *
       * @throws PSRemoteException if an error occured while communicating with
       *    the remote server.
       */
      private boolean populateTransitions()
         throws PSRemoteException
      {
         boolean valid = true;

         PSWorkflowDef wfDef = m_extractorDef.getWorkflowDef();
         if ( wfDef != null)
         {
            for (int i=0; i < PSWorkflowDef.MAX_TRANS; i++)
            {
               PSComponentList transDefs = wfDef.getTransitions(i);
               if ((!transDefs.isEmpty()) &&
                   (!populateTransitions(m_transitions[i], transDefs)))
               {
                  valid = false;
               }
            }
         }
         return valid;
      }

      /**
       * Validating and set the specified transitions from the current workflow
       * definition.
       *
       * @param transitions The transition list which will be populated with
       *    the transition definitions. Assume not <code>null</code>.
       *
       * @param transDefList The list of transition definitions that will be
       *    validated. Assume not <code>null</code>.
       *
       * @return <code>true</code> if the transitions has been populated
       *    without error; <code>false</code> if the extractor definition
       *    contains an invalid tarnsitions, which will be ignored.
       *
       * @throws PSRemoteException if an error occured while communicating with
       *    the remote server.
       */
      private boolean populateTransitions(List transitions,
         PSComponentList transDefList)
         throws PSRemoteException
      {
         boolean valid = true;
         List validTrans = ms_cataloger.getTransitions(m_workflow);
         Iterator transDefs = transDefList.getComponents();
         transitions.clear();

         PSTransitionDef transDef = null;
         PSEntry trans = null;
         while (transDefs.hasNext())
         {
            transDef = (PSTransitionDef) transDefs.next();
            trans = containsTransition(validTrans.iterator(), transDef);
            if (trans != null)
            {
               transitions.add(trans);
            }
            else // invalid transition definition, log it.
            {
               Object[] args = {transDef.getTrigger(),
                     m_workflow.getLabel().getText()};
               PSLogMessage msg = new PSLogMessage(
                  IPSLogCodes.INVALID_TRANSITION_TRIGGER, args,
                  PSLogMessage.LEVEL_WARN);
               Logger.getLogger(this.getClass()).warn(msg);

               valid = false;
            }
         }

         return valid;
      }

      /**
       * Get the updated or edited version of the extractor definition.
       *
       * @return The extractor definition, never <code>null</code>.
       */
      private PSExtractorDef getUpdatedExtractorDef()
      {
         // update the content type property
         com.percussion.loader.objectstore.PSProperty ctypeProperty = null;

         try
         {
            ctypeProperty = m_extractorDef.getContentTypeProperty();
            ctypeProperty.setValue(m_contentType.getLabel().getText());
         }
         catch (PSLoaderException e) // this is not possible
         {
            throw new IllegalStateException("Unknown error: " + e.toString());
         }

         // updates all the field properties
         List fields = new ArrayList();
         fields.addAll(m_systemFields);
         fields.addAll(m_nonSystemFields);

         PSFieldProperty community = new PSFieldProperty(
            IPSHtmlParameters.SYS_COMMUNITYID,
            m_community.getValue(),
            PSFieldProperty.VALUE_TYPE_NUMBER);
         fields.add(community);

         if (m_workflow != null)
         {
            PSFieldProperty workflow = new PSFieldProperty(
               IPSHtmlParameters.SYS_WORKFLOWID,
               m_workflow.getValue(),
               PSFieldProperty.VALUE_TYPE_NUMBER);
            fields.add(workflow);
         }
         m_extractorDef.setFieldProperties(fields.iterator());

         // updates workflow transitions
         if ((m_workflow != null) && (! isEmptyTransition()))
         {
            PSWorkflowDef wfDef = new PSWorkflowDef(
               m_workflow.getLabel().getText());

            for (int i=0; i < PSWorkflowDef.MAX_TRANS; i++)
            {
               PSComponentList transDefs = wfDef.getTransitions(i);
               if (! m_transitions[i].isEmpty())
                  addTransitionDefs(m_transitions[i], transDefs);
            }

            m_extractorDef.setWorkflowDef(wfDef);
         }
         else
         {
            m_extractorDef.setWorkflowDef(null);
         }

         return m_extractorDef;
      }

      /**
       * Determines if there is any defined transitions
       *
       * @return <code>true</code> if there is at least one defined transition;
       *    <code>false</code> otherwise.
       */
      private boolean isEmptyTransition()
      {
         for (int i=0; i < PSWorkflowDef.MAX_TRANS; i++)
         {
            if (! m_transitions[i].isEmpty())
               return false;
         }
         return true;
      }

      /**
       * Adds the defined transitions into the supplied transition definition
       * list.
       *
       * @param transitions The defined transitions, a list over one of more
       *    <code>PSEntry</code> objects, assume not <code>null</code>.
       *
       * @param transDefs The repository for the transition definition list.
       */
      private void addTransitionDefs(List transitions,
         PSComponentList transDefs)
      {
         PSTransitionDef transDef = null;
         PSEntry trans = null;
         Iterator transIt = transitions.iterator();
         while (transIt.hasNext())
         {
            trans = (PSEntry) transIt.next();
            transDef = new PSTransitionDef(trans.getLabel().getText(),
               trans.getValue());
            transDefs.addComponent(transDef);
         }
      }

      /**
       * See {@link PSExtractorConfigContext#getTransitions()} for its
       * description.
       */
      private Iterator getTransitions(int whichTrans)
      {
         return m_transitions[whichTrans].iterator();
      }

      /**
       * See {@link PSExtractorConfigContext#setTransitions(int)} for its
       * description.
       */
      private void setTransitions(List transitions, int whichTrans)
      {
         m_transitions[whichTrans] = transitions;
      }

      /**
       * See {@link PSExtractorConfigContext#getContentType()} for its
       * description.
       */
      private PSEntry getContentType()
      {
         return m_contentType;
      }

     /**
       * See {@link PSExtractorConfigContext#validateExtractorDef()} for its
       * description.
       */
      private boolean validateExtractorDef()
      {
         return m_contentType != null;
      }

     /**
       * See {@link PSExtractorConfigContext#getWorkflow()} for its
       * description.
       */
      private PSEntry getWorkflow()
      {
         return m_workflow;
      }

      /**
       * See {@link PSExtractorConfigContext#setWorkflow(PSEntry)} for its
       * description.
       */
      private void setWorkflow(PSEntry workflow)
      {
         m_workflow = workflow;
      }

      /**
       * Determines whether the supplied list of transitions contains the
       * given transition definition.
       *
       * @param transitions The list of transitions, zero or more
       *    <code>PSEntry</code> objects. Assume not <code>null</code>.
       *
       * @param transDef The to be tested transition, assume not
       *    <code>null</code>.
       *
       * @return The transition object if found one in the list;
       *    return <code>null</code> if not find.
       */
      private PSEntry containsTransition(Iterator transitions,
         PSTransitionDef transDef)
      {
         PSEntry trans = null;
         while (transitions.hasNext())
         {
            trans = (PSEntry) transitions.next();
            if (trans.getValue().equalsIgnoreCase(transDef.getTrigger()))
               return trans;
         }
         return null;
      }


      /**
       * Get current community.
       *
       * @return The current community, never <code>null</code>.
       */
      private PSEntry getCommunity()
      {
         return m_community;
      }

      /**
       * Current community, initialized by {@link #populateCommunity()}.
       * never <code>null</code> after that. Initialized or modified by
       * {@link #populateCommunity()}, never <code>null</code> after that.
       */
      private PSEntry m_community = null;

      /**
       * Current content type. It may be <code>null</code> if it is not defined.
       */
      private PSEntry m_contentType = null;

      /**
       * The current workflow of the extractor. It may be <code>null</code>
       * if it is not defined.
       */
      private PSEntry m_workflow = null;

      /**
       * The defined transitions for the current workflow. Never
       * <code>null</code>, but may be empty. Each element is a list of zero or
       * more <code>PSEntry</code> objects.
       */
      private List[] m_transitions = new List[3];

      /**
       * The list of system fields that is defined in the current extractor
       * definition. Never <code>null</code>, but may be empty. It is a list of
       * zero or more <code>PSFieldProperty</code> objects.
       */
      private List m_systemFields = new ArrayList();

      /**
       * The list of non-system fields that is defined in the current extractor
       * definition. Never <code>null</code>, but may be empty. It is a list of
       * zero or more <code>PSFieldProperty</code> objects.
       */
      private List m_nonSystemFields = new ArrayList();

   }


}
