/******************************************************************************
 *
 * [ PSEditorInput.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui;

import com.percussion.client.IPSReference;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * <code>IEditorInput</code> is a light weight descriptor of editor input,
 * like a file name but more abstract.  It is not a model.  It is a 
 * description of the model source for an <code>IEditorPart</code>.
 * 
 * A method named {@link #getReference()} exists to get a reference to 
 * the object that will be edited. It is the editor's responsibility to verify
 * that the reference represent the appropriate object type for the editor.
 * @see org.eclipse.ui.IEditorInput for more detail
 */
public class PSEditorInput implements IEditorInput
{
   /**
    * Ctor 
    * @param ref <code>IPSReference</code> to the object to be edited,
    * cannot be <code>null</code>.
    * @param name the editor name, cannot be <code>null</code> or empty.
    * @param tooltip the tooltip that is seen when mousing over the editor's
    * tab. May be <code>null</code> or empty.
    * @param image the image that will be seen in the editors tab, cannot
    * be <code>null</code>.
    * @param persistableElement persistable element, can be <code>null</code> if 
    * this editor input cannot be persisted
    * @param exists flag indicating if an editor exists. 
    */
   public PSEditorInput(
      IPSReference ref,
      String name,
      String tooltip,
      ImageDescriptor image,
      IPersistableElement persistableElement,
      boolean exists      
      )
   {
      if(ref == null)
         throw new IllegalArgumentException("The reference cannot be null.");
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException(
            "The name cannot be null or empty.");
      if(tooltip == null)
         tooltip = "";
      if(image == null)
         throw new IllegalArgumentException(
            "The image descriptor cannot be null.");
      m_reference = ref;
      m_name = name;
      m_tooltip = WordUtils.wrap(tooltip.trim(), 120);
      m_imageDesc = image;
      m_persistable = persistableElement;
      m_exists = exists;
   }  

   /* 
    * @see org.eclipse.ui.IEditorInput#exists()
    */
   public boolean exists()
   {
     return m_exists;
   }

   /* 
    * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
    */
   public ImageDescriptor getImageDescriptor()
   {
      return m_imageDesc;
   }

   /* 
    * @see org.eclipse.ui.IEditorInput#getName()
    */
   public String getName()
   {
      return m_name;
   }

   /* 
    * @see org.eclipse.ui.IEditorInput#getPersistable()
    */
   public IPersistableElement getPersistable()
   {
      return m_persistable;
   }

   /* 
    * @see org.eclipse.ui.IEditorInput#getToolTipText()
    */
   public String getToolTipText()
   {
      return m_tooltip;
   }

   /**
    * Our implementation will always return <code>null</code>
    * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
    * @return always <code>null</code>.
    */
   @SuppressWarnings("unused")
   public Object getAdapter(Class adapter)
   {
      return null;
   }
   
   /**
    * Returns the <code>IPSReference</code> that represents the
    * object that will be modified by the editor that this 
    * <code>IEditorInput</code> is passed to.
    * @return the object <code>IPSReference</code>, never <code>null</code>.
    */
   public IPSReference getReference()
   {
      return m_reference;
   }
   
   /* 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if(!(obj instanceof PSEditorInput)) {
         return false;
       }
       if (this == obj) {
         return true;
       }
       PSEditorInput other = (PSEditorInput) obj;
       return new EqualsBuilder()
          .append(m_name, other.getName())
          .append(m_tooltip, other.getToolTipText())
          .append(m_imageDesc, other.getImageDescriptor())
          .append(m_reference, other.getReference())
          .isEquals();
   }
   
   /* 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return new HashCodeBuilder().
         append(m_name).
         append(m_tooltip).
         append(m_imageDesc.hashCode()).
         append(m_reference.hashCode()).
         toHashCode();      
   }  
     
   /**
    * Exists flag, set in ctor
    */
   private boolean m_exists;
   
   /**
    * The image descriptor, initialized in ctor, never <code>null</code>
    * after that.
    */
   private ImageDescriptor m_imageDesc;
   
   /**
    * The editor name, initialized in ctor, never <code>null</code> or
    * empty after that.
    */
   private String m_name;
   
   /**
    * Set in ctor, may be <code>null</code>
    * @see org.eclipse.ui.IEditorInput#getPersistable()
    */
   private IPersistableElement m_persistable;
   
   /**
    * The editor tooltip, initialized in ctor, never <code>null</code> or
    * empty after that.    
    */
   private String m_tooltip;   
   
   /**
    * The reference to the object that will be edited, initialized in ctor,
    * never <code>null</code> after that.
    */
   private IPSReference m_reference;

}
