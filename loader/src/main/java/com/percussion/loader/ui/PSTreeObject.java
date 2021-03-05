/*[ PSTreeObject.java ]********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.loader.objectstore.PSLoaderComponent;
import com.percussion.loader.objectstore.PSLoaderNamedComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * User object attached to the the tree in {@link PSDescriptor}. On cliicking a
 * node in the tree the {@link PSConfigPanel} is displayed in the {@link
 * PSContentDescriptorDialog} in the right.
 */
public class PSTreeObject
{
   /**
    * Creates the PSTreeObject.
    *
    * @param name, name of this object, never <code>null</code> or empty.
    *
    * @param loaderComp Loader component, base class for all the objectstore
    * classes, represents the data utilised by <code>configPane</code> for
    * initialization and all the data edited in the <code>configPane</code> gets
    * persisted in <code>loaderComp</code>. May be <code>null</code>.
    *
    * @param configPane panel for this user object. May be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>name</code> is not valid.
    */
   public PSTreeObject(String name, PSLoaderComponent loaderComp, PSConfigPanel
      configPane)
   {
      if (name == null || name.length() == 0)
         throw new IllegalArgumentException("name cannot be null or empty");
      m_name = name;
      m_loaderComp = loaderComp;
      m_configPane = configPane;
   }

   /**
    * Creates the PSTreeObject.
    *
    * @param name, name of this object, never <code>null</code> or empty.
    *
    * @param configPane panel for this user object. May be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>name</code> is not valid.
    */
   public PSTreeObject(String name, PSConfigPanel configPane)
   {
      this(name, null, configPane);
   }

   /**
    * Gets the objectstore object.
    *
    * @return data object for this user object. May be <code>null</code>.
    */
   public PSLoaderComponent getDataObj()
   {
      return m_loaderComp;
   }

   /**
    * Gets the panel for this object.
    *
    * @return panel for this object. May be <code>null</code>.
    */
   public PSConfigPanel getUIObj()
   {
      return m_configPane;
   }

   /**
    * Convenience method, calls {@link #getName()}
    */
   public String toString()
   {
      return getName();
   }

   /**
    * Gets the name of the object.
    * 
    * @return the name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }
   
   /**
    * Adds other user object which are <code>PSTreeObject</code> as children to
    * this object.
    *
    * @param obj, child object, may be <code>null</code>.
    */
   public void addChildren(PSTreeObject obj)
   {
      /**
       * The reason other objects are being added as children and not
       * directly got from the tree because the objects may be moved from used
       * list (in PSMultiSelectionEditorPanel) to the available list and then
       * back to used list, now the two objects, the one attached to tree and
       * one in the used list are different. So while saving, objects from
       * tree are used but for attaching getChildren(); is used.
       *
       */
      if (obj != null)
         m_list.add(obj);
   }

   /**
    * Gets the list of children attached to this user object.
    *
    * @return A list over zero or more <code>PSTreeObject</code>, may be
    * empty, never <code>null</code>.
    */
   public List getChildren()
   {
      return m_list;
   }

   /**
    * Sets the name for this object.
    *
    * @param name The to be set name of the object. It may not be 
    *    <code>null</code> or empty.
    */
   public void setName(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name cannot be null");

      // set the component name if needed
      if (m_loaderComp instanceof PSLoaderNamedComponent)
      {
         PSLoaderNamedComponent comp = (PSLoaderNamedComponent) m_loaderComp;
         if (m_name.equals(comp.getName()))
         comp.setName(name);
      }
      
      m_name = name;
   }

   /**
    * Sets the help id for this object to bring up the context sensitive help.
    *
    * @param id, if <code>null</code> or empty, it silently returns.
    */
   public void setHelpId(String id)
   {
      if (id == null || id.length() == 0)
         return;
      m_helpId = id;
   }

   /**
    * Gets the help id.
    *
    * @return, help id is never <code>null</code>, may be empty.
    */
   public String getHelpId()
   {
      if (m_helpId == null)
         return "";
      else
         return m_helpId;
   }

   /**
    * Help id initialized in {@link#setHelpId(String)}, never <code>null</code>
    * or empty after that.
    */
   private String m_helpId;

   /**
    * List for holding children of this object. Never <code>null</code> or
    * modified after that.
    */
   private List m_list = new ArrayList();

   /**
    * Base class of the objectore object used by this class.
    * Initialized in the ctor. Never <code>null</code>, modified only when
    * 'OK' or 'Apply' button is hit in {@link PSContentDescriptorDialog}.
    */
   private PSLoaderComponent m_loaderComp;

   /**
    * Panel to be displayed when a tree node containing this object is clicked.
    * Intialized in the ctor and never <code>null</code> or modified after that.
    */
   private PSConfigPanel m_configPane;

   /**
    * Name of this object. Initialized in ctor and never
    * <code>null</code> or modified or empty after that.
    */
   private String m_name;
}
