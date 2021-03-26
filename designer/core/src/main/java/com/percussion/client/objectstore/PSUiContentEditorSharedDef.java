/******************************************************************************
*
* [ PSUiContentEditorSharedDef.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
/**
 * 
 */
package com.percussion.client.objectstore;

import com.percussion.design.objectstore.IPSDocument;
import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSUnknownDocTypeException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSCollection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Extends <code>PSContentEditorSharedDef</code> to add
 * extra functionality that allows actions to be set for
 * fields to allow the content editor handler to know when
 * to alter or delete a fields associated table column.
 */
public class PSUiContentEditorSharedDef extends PSContentEditorSharedDef
   implements
      IPSCETableColumnActions
{   

   /*
    * @see com.percussion.design.objectstore.PSContentEditorSharedDef#
    * PSContentEditorSharedDef()
    */
   public PSUiContentEditorSharedDef()
   {
      super();
   }

   /*
    * @see com.percussion.design.objectstore.PSContentEditorSharedDef#
    * PSContentEditorSharedDef(PSCollection)
    */
   public PSUiContentEditorSharedDef(PSCollection fieldGroups)
   {
      super(fieldGroups);
   }

   /*
    * @see com.percussion.design.objectstore.PSContentEditorSharedDef#
    * PSContentEditorSharedDef(Element, IPSDocument, ArrayList)
    */
   public PSUiContentEditorSharedDef(Element sourceNode, IPSDocument parentDoc,
      ArrayList parentComponents) throws PSUnknownNodeTypeException
   {
      super(sourceNode, parentDoc, parentComponents);
   }

   /*
    * @see com.percussion.design.objectstore.PSContentEditorSharedDef#
    * PSContentEditorSharedDef(Document)
    */
   public PSUiContentEditorSharedDef(Document sourceDoc)
      throws PSUnknownDocTypeException, PSUnknownNodeTypeException
   {
      super(sourceDoc);
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
    * Auto generated serial version UID
    */
   private static final long serialVersionUID = -1218626859277264526L;

}
