/******************************************************************************
*
* [ PSUiItemDefinition.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.client.objectstore;

import com.percussion.client.IPSReference;
import com.percussion.client.PSObjectTypes;
import com.percussion.cms.objectstore.PSContentType;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Extends <code>PSItemDefinition</code> to add
 * extra functionality that allows actions to be set for
 * fields to allow the content editor handler to know when
 * to alter or delete a fields assosiated table column.
 */
public class PSUiItemDefinition extends PSItemDefinition
   implements
      IPSCETableColumnActions
{

   /**
    * @see com.percussion.cms.objectstore.PSItemDefinition#PSItemDefinition(
    * String, PSContentType, PSContentEditor)
    */
   public PSUiItemDefinition(String appName, PSContentType typeDef,
      PSContentEditor editorDef)
   {
      super(appName, typeDef, editorDef);
   }

   /**
    * @see com.percussion.cms.objectstore.PSItemDefinition#PSItemDefinition(
    * Element)
    */
   public PSUiItemDefinition(Element contentTypeDef)
      throws PSUnknownNodeTypeException
   {
      super(contentTypeDef);
   }
   
   /**
    * Ctor to create from a <code>PSItemDefinition</code>.
    * @param def cannot be <code>null</code>
    */
   public PSUiItemDefinition(PSItemDefinition def)
   {
      super();
      if(def == null)
         throw new IllegalArgumentException("def cannot be null.");
      try
      {
         super.fromXml(def.toXml(PSXmlDocumentBuilder.createXmlDocument()),
            null, null);
      }
      catch (PSUnknownNodeTypeException e)
      {
         // should never happen
         throw new RuntimeException(e);         
      }
   }
   
  
   /* 
    * @see com.percussion.client.objectstore.IPSCETableColumnActions#
    * addColumnAction(com.percussion.design.objectstore.PSField, int)
    */
   public void addColumnAction(PSField field, int action)
   {
      m_actions.put(field, action);
   }
   
   /* 
    * @see com.percussion.client.objectstore.IPSCETableColumnActions#
    * removeColumnAction(com.percussion.design.objectstore.PSField)
    */
   public void removeColumnAction(PSField field)
   {
      m_actions.remove(field);      
   }

   /* 
    * @see com.percussion.client.objectstore.IPSCETableColumnActions#
    * setColumnActions(java.util.Map)
    */
   public void setColumnActions(Map<PSField, Integer> actions)
   {
      m_actions.clear();
      if(actions != null && !actions.isEmpty())
         m_actions.putAll(actions);
   }

   /* 
    * @see com.percussion.client.objectstore.IPSCETableColumnActions#
    * getColumnActions()
    */
   public Map<PSField, Integer> getColumnActions()
   {
      return m_actions;
   }
   
   /**
    * Map of field/actions. Never <code>null</code>, may
    * be empty.
    */
   private Map<PSField, Integer> m_actions = 
      new HashMap<PSField, Integer>();

   /**
    * Templates referred by content type. If not <code>null</code> refer only
    * to this template.
    */
   public Set<IPSReference> getNewTemplates()
   {
      return m_newTemplates;
   }

   /**
    * @see #getNewTemplates()
    * @param newTemplates set to not-<code>null</code> value to overwrite templates.
    * 
    */
   public void setNewTemplates(Set<IPSReference> newTemplates)
   {
      if (newTemplates != null)
      {
         for (final IPSReference r : newTemplates)
         {
            if (!r.isPersisted())
            {
               throw new IllegalArgumentException(
                     "Attempt to associate not persisted template "
                     + r.getName() + " with content type " + getName());
            }
            if (!r.getObjectType().getPrimaryType().equals(
                  PSObjectTypes.TEMPLATE))
            {
               throw new IllegalArgumentException("Provided reference " + r
                     + " has unexpected object type " + r.getObjectType());
            }
         }
      }
      m_newTemplates = newTemplates;
   }

   /**
    * Returns <code>true</code> when {@link #getNewTemplates()} returns
    * not-<code>null</code> and not-empty value indicating that template
    * references should be overwritten with the specified values.
    */
   public boolean areNewTemplatesSpecified()
   {
      return getNewTemplates() != null && !getNewTemplates().isEmpty();
   }

   /**
    * @see #getNewTemplates()
    */
   private Set<IPSReference> m_newTemplates;
}
