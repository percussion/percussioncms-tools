/******************************************************************************
 *
 * [ PSUrlEditorInput.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import java.net.URL;

/**
 * Editor input storing url.
 *
 * @see PSRhythmyxPageEditor
 * @author Andriy Palamarchuk
 */
public class PSUrlEditorInput implements IEditorInput
{
   /**
    * Creates new instance of the editor input.
    *
    * @param url the url to provide to an editor. Not <code>null</code>.
    */
   public PSUrlEditorInput(URL url)
   {
      if (url == null)
      {
         throw new IllegalArgumentException("Url must not be null");
      }
      m_url = url;
   }
   
   /**
    * Returns <code>true</code> if the passed parameter is {@link IEditorInput}
    * and can be adapted to {@link URL}.
    * @see java.lang.Object#equals(java.lang.Object) the object to compare with.
    * Can be <code>null</code>.
    */
   @Override
   public boolean equals(Object obj)
   {
      if (obj instanceof IEditorInput)
      {
         final URL url = (URL) ((IEditorInput) obj).getAdapter(URL.class);
         if (url != null && url.equals(m_url))
         {
            return true;
         }
      }
      return false;
   }

   /**
    * Returns hash code of the stored url as hash code of this editor input.
    */
   @Override
   public int hashCode()
   {
      return m_url.hashCode();
   }

   /**
    * Always returns <code>false</code> because
    * editor input associated with this editor is virtual.
    * @see org.eclipse.ui.IEditorInput#exists()
    */
   public boolean exists()
   {
      return false;
   }

   /**
    * Returns default image descriptor for this editor.
    * @return default image descriptor. Never <code>null</code>.
    */
   public ImageDescriptor getImageDescriptor()
   {
      return ImageDescriptor.getMissingImageDescriptor();
   }

   /**
    * Returns {@link URL#getPath()} as name.
    * @see org.eclipse.ui.IEditorInput#getName()
    * @return {@link URL#getPath()}. Never <code>null</code>.
    */
   public String getName()
   {
      return m_url.getPath();
   }

   /**
    * Always returns <code>null</code> because is never persisted.
    * @see org.eclipse.ui.IEditorInput#getPersistable()
    * @return <code>null</code>.
    */
   public IPersistableElement getPersistable()
   {
      // can not be persisted
      return null;
   }

   /**
    * Returns full url string as a tooltip text.
    * @return full url string. Never <code>null</code>.
    */
   public String getToolTipText()
   {
      return m_url.toString();
   }

   /**
    * Besides default adapter manager behavior returns adapter for {@link URL}.
    * @param adapter recognizes only {@link URL}, calls adapter manager
    * for any other class. Never <code>null</code>.
    * @return url for {@link URL} adapter or default adapter manager adapter.
    * <code>null</code> if can't provide value for given adapter.
    */
   public Object getAdapter(Class adapter)
   {
      if (URL.class.equals(adapter))
      {
         return m_url;
      }
      return Platform.getAdapterManager().getAdapter(this, adapter);
   }
   
   /**
    * The URL this input provided to an editor.
    * Not <code>null</code>.
    */
   private final URL m_url;
}
