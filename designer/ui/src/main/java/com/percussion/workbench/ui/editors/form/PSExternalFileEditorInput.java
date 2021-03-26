/******************************************************************************
 *
 * [ PSExternalFileEditorInput.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.percussion.workbench.ui.editors.form;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.editors.text.ILocationProvider;
import org.eclipse.ui.model.IWorkbenchAdapter;

import java.io.File;

/**
 * Andriy: copied from org.eclipse.ui.internal.editors.text.JavaFileEditorInput,
 * to eliminate dependencies on internal packages.
 * @since 3.0
 */
public class PSExternalFileEditorInput implements IPathEditorInput, ILocationProvider {

   /**
    * The workbench adapter which simply provides the label.
    *
    * @since 3.1
    */
   private class WorkbenchAdapter implements IWorkbenchAdapter {
      /*
       * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
       */
      @SuppressWarnings("unused")
      public Object[] getChildren(Object o)
      {
         return null;
      }

      /*
       * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
       */
      @SuppressWarnings("unused")
      public ImageDescriptor getImageDescriptor(Object o) {
         return null;
      }

      /*
       * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
       */
      public String getLabel(Object o) {
         return ((PSExternalFileEditorInput)o).getName();
      }

      /*
       * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
       */
      @SuppressWarnings("unused")
      public Object getParent(Object o) {
         return null;
      }
   }

   private File fFile;
   private WorkbenchAdapter fWorkbenchAdapter= new WorkbenchAdapter();

   public PSExternalFileEditorInput(File file) {
      super();
      fFile= file;
      fWorkbenchAdapter= new WorkbenchAdapter();
   }
   /*
    * @see org.eclipse.ui.IEditorInput#exists()
    */
   public boolean exists() {
      return fFile.exists();
   }

   /*
    * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
    */
   public ImageDescriptor getImageDescriptor() {
      return null;
   }

   /*
    * @see org.eclipse.ui.IEditorInput#getName()
    */
   public String getName() {
      return fFile.getName();
   }

   /*
    * @see org.eclipse.ui.IEditorInput#getPersistable()
    */
   public IPersistableElement getPersistable() {
      return null;
   }

   /*
    * @see org.eclipse.ui.IEditorInput#getToolTipText()
    */
   public String getToolTipText() {
      return fFile.getAbsolutePath();
   }

   /*
    * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
    */
   public Object getAdapter(Class adapter) {
      if (ILocationProvider.class.equals(adapter))
         return this;
      if (IWorkbenchAdapter.class.equals(adapter))
         return fWorkbenchAdapter;
      return Platform.getAdapterManager().getAdapter(this, adapter);
   }

   /*
    * @see org.eclipse.ui.editors.text.ILocationProvider#getPath(java.lang.Object)
    */
   public IPath getPath(Object element) {
      if (element instanceof PSExternalFileEditorInput) {
         PSExternalFileEditorInput input= (PSExternalFileEditorInput) element;
         return Path.fromOSString(input.fFile.getAbsolutePath());
      }
      return null;
   }

    /*
     * @see org.eclipse.ui.IPathEditorInput#getPath()
     * @since 3.1
     */
    public IPath getPath() {
        return Path.fromOSString(fFile.getAbsolutePath());
    }

   /*
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object o) {
      if (o == this)
         return true;

      if (o instanceof PSExternalFileEditorInput) {
         PSExternalFileEditorInput input= (PSExternalFileEditorInput) o;
         return fFile.equals(input.fFile);
      }

        if (o instanceof IPathEditorInput) {
            IPathEditorInput input= (IPathEditorInput)o;
            return getPath().equals(input.getPath());
        }

      return false;
   }

   /*
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      return fFile.hashCode();
   }
}
