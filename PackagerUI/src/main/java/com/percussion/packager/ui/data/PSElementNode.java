/******************************************************************************
 *
 * [ PSElementNode.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packager.ui.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Lightweight class to represent a deployable element
 * or category and its selection state.
 */
public class PSElementNode
{
   /**
    * 
    * @param name
    * @param isCategory
    */
   public PSElementNode(String name, boolean isCategory)
   {
      m_name = name;
      m_isCategory = isCategory;
   }
   
   /**
    * 
    * @param name
    * @param isCategory
    * @param dependencyId
    * @param objectType
    */
   public PSElementNode(String name, boolean isCategory,
       String dependencyId, String objectType)
   {
      this(name, isCategory);
      m_dependencyId = dependencyId;
      m_objectType = objectType;
   }
   
   /**
    * @return the isSelected
    */
   public boolean isSelected()
   {
      return m_isSelected;
   }

   /**
    * @param isSelected the isSelected to set
    */
   public void setSelected(boolean isSelected)
   {
      m_isSelected = isSelected;
   }
   
   /**
    * Indicates that the element has children.
    * @return <code>true</code> if children exist.
    */
   public boolean hasChildren()
   {
      return !m_children.isEmpty();
   }
   
   /**
    * @return the children
    */
   public Iterator<PSElementNode> getChildren()
   {
      return m_children.iterator();
   }

   /**
    * @param child to add to this element. Can only add if this is
    * a category element.
    */
   public void addChild(PSElementNode child)
   {
      if(!isCategory())
         throw new IllegalStateException("Non category elements cannot have children.");
      m_children.add(child);
   }   

   /* (non-Javadoc)
    * @see java.lang.Object#clone()
    */
   @Override
   protected Object clone()
   {
      PSElementNode clone = new PSElementNode(m_name, m_isCategory);
      clone.m_dependencyId = m_dependencyId;
      clone.m_objectType = m_objectType;
      clone.m_isSelected = m_isSelected;
      clone.m_children = m_children;
      return clone;
   }

   /**
    * @return the isCategory
    */
   public boolean isCategory()
   {
      return m_isCategory;
   }

   /**
    * @return the name
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * @return the dependencyId
    */
   public String getDependencyId()
   {
      return m_dependencyId;
   }

   /**
    * @return the objectType
    */
   public String getObjectType()
   {
      return m_objectType;
   }
   
   /**
    * Flag indicating the element is a category.
    */
   private boolean m_isCategory;
   
   /**
    * Flag indicating that this element is selected.
    * Only non category elements can be selected.
    */
   private boolean m_isSelected;
   
   /**
    * The element name.
    */
   private String m_name;
   
   /**
    * The element dependency id. Will be 
    * <code>null</code> for category elements.
    */
   private String m_dependencyId;
   
   /**
    * The element object type. Will be 
    * <code>null</code> for category elements.
    */
   private String m_objectType;
   
   /**
    * Element children. Only category elements can have
    * children.
    */
   private List<PSElementNode> m_children = 
      new ArrayList<PSElementNode>();

   
   
   
}
