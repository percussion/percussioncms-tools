/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/******************************************************************************
 *
 * [ PSMultiPageEditorSite.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.INestableKeyBindingService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.commands.SlaveCommandService;
import org.eclipse.ui.internal.contexts.NestableContextService;
import org.eclipse.ui.internal.expressions.ActivePartExpression;
import org.eclipse.ui.internal.handlers.NestableHandlerService;
import org.eclipse.ui.internal.services.ServiceLocator;
import org.eclipse.ui.part.MultiPageEditorSite;
import org.eclipse.ui.part.MultiPageSelectionProvider;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.services.IServiceScopes;


/**
 * Note: This class is effectively a clone of {@link MultiPageEditorSite}. The
 * original is not used because we want our editor hierarchy to pass from
 * {@link PSEditorBase} to {@link PSMultiPageEditorBase}. 
 */
public class PSMultiPageEditorSite implements IEditorSite
{
   /**
    * Logger for this class.
    */
   private static final Logger ms_log = LogManager.getLogger(PSMultiPageEditorSite.class);

   /**
    * Creates a site for the given editor nested within the given multi-page editor.
    *
    * @param multiPageEditor the multi-page editor
    * @param editor the nested editor
    */
   public PSMultiPageEditorSite(PSMultiPageEditorBase multiPageEditor,
           IEditorPart editor) {
       Assert.isNotNull(multiPageEditor);
       Assert.isNotNull(editor);
       this.m_multiPageEditor = multiPageEditor;
       this.m_editor = editor;
       
       final IServiceLocator parentServiceLocator = multiPageEditor.getSite();
       m_serviceLocator = new ServiceLocator(parentServiceLocator,null,null);

       initializeDefaultServices();
   }

    /**
     * Initialize the slave services for this site.
     */
    private void initializeDefaultServices() {
        final Expression defaultExpression = new ActivePartExpression(
                m_multiPageEditor);

        final IHandlerService parentService = (IHandlerService) m_serviceLocator
                .getService(IHandlerService.class);
        final IHandlerService slave = new NestableHandlerService(parentService,
                defaultExpression);
        m_serviceLocator.registerService(IHandlerService.class, slave);

        final IContextService parentContext = (IContextService) m_serviceLocator
                .getService(IContextService.class);
        final IContextService context = new NestableContextService(
                parentContext, defaultExpression);
        m_serviceLocator.registerService(IContextService.class, context);

        final ICommandService parentCommandService = (ICommandService) m_serviceLocator
                .getService(ICommandService.class);

        final ICommandService commandService = new SlaveCommandService(
                parentCommandService,  IServiceScopes.MPESITE_SCOPE,
                this);
        m_serviceLocator.registerService(ICommandService.class, commandService);
    }

   /**
    * Dispose the contributions.
    */
   public void dispose() {
       // Remove myself from the list of nested key binding services.
       if (m_service != null) {
           IKeyBindingService parentService = getEditor().getSite()
                   .getKeyBindingService();
           if (parentService instanceof INestableKeyBindingService) {
               INestableKeyBindingService nestableParent = (INestableKeyBindingService) parentService;
               nestableParent.removeKeyBindingService(this);
           }
           m_service = null;
       }
   }

   /**
    * The <code>MultiPageEditorSite</code> implementation of this 
    * <code>IEditorSite</code> method returns <code>null</code>,
    * since nested editors do not have their own action bar contributor.
    * 
    * @return <code>null</code>
    */
   public IEditorActionBarContributor getActionBarContributor() {
       return null;
   }

   /**
    * The <code>MultiPageEditorSite</code> implementation of this 
    * <code>IEditorSite</code> method forwards to the multi-page editor
    * to return the action bars.
    * 
    * @return The action bars from the parent multi-page editor.
    */
   public IActionBars getActionBars() {
       return m_multiPageEditor.getEditorSite().getActionBars();
   }   

   /**
    * Returns the nested editor.
    *
    * @return the nested editor
    */
   public IEditorPart getEditor() {
       return m_editor;
   }

   /**
    * The <code>MultiPageEditorSite</code> implementation of this 
    * <code>IWorkbenchPartSite</code> method returns an empty string since the
    * nested editor is not created from the registry.
    * 
    * @return An empty string.
    */
   public String getId() {
       return ""; //$NON-NLS-1$
   }

   /* (non-Javadoc)
    * Method declared on IEditorSite.
    */
   public IKeyBindingService getKeyBindingService() {
       if (m_service == null) {
           m_service = getMultiPageEditor().getEditorSite()
                   .getKeyBindingService();
           if (m_service instanceof INestableKeyBindingService) {
               INestableKeyBindingService nestableService = (INestableKeyBindingService) m_service;
               m_service = nestableService.getKeyBindingService(this);

           } else {
               /* This is an internal reference, and should not be copied by
                * client code.  If you are thinking of copying this, DON'T DO 
                * IT.
                */
              ms_log.warn(
                    "MultiPageEditorSite.getKeyBindingService()   Parent key binding service was not an instance of INestableKeyBindingService.  It was an instance of " + m_service.getClass().getName() + " instead."); //$NON-NLS-1$ //$NON-NLS-2$
           }
       }

       return m_service;
   }

   /**
    * Returns the multi-page editor.
    *
    * @return the multi-page editor
    */
   public PSMultiPageEditorBase getMultiPageEditor() {
       return m_multiPageEditor;
   }

   /**
    * The <code>MultiPageEditorSite</code> implementation of this 
    * <code>IWorkbenchPartSite</code> method forwards to the multi-page editor to
    * return the workbench page.
    * 
    * @return The workbench page in which this editor site resides.
    */
   public IWorkbenchPage getPage() {
       return getMultiPageEditor().getSite().getPage();
   }

   /**
    * The <code>MultiPageEditorSite</code> implementation of this 
    * <code>IWorkbenchPartSite</code> method returns an empty string since the
    * nested editor is not created from the registry.
    * 
    * @return An empty string. 
    */
   public String getPluginId() {
       return ""; //$NON-NLS-1$
   }

   /**
    * The <code>MultiPageEditorSite</code> implementation of this 
    * <code>IWorkbenchPartSite</code> method returns an empty string since the
    * nested editor is not created from the registry.
    * 
    * @return An empty string.
    */
   public String getRegisteredName() {
       return ""; //$NON-NLS-1$
   }

   /**
    * Returns the selection changed listener which listens to the nested editor's selection
    * changes, and calls <code>handleSelectionChanged</code>.
    *
    * @return the selection changed listener
    */
   private ISelectionChangedListener getSelectionChangedListener() {
       if (m_selectionChangedListener == null) {
           m_selectionChangedListener = new ISelectionChangedListener() {
               public void selectionChanged(SelectionChangedEvent event) {
                   PSMultiPageEditorSite.this.handleSelectionChanged(event);
               }
           };
       }
       return m_selectionChangedListener;
   }

   /**
    * The <code>MultiPageEditorSite</code> implementation of this 
    * <code>IWorkbenchPartSite</code> method returns the selection provider 
    * set by <code>setSelectionProvider</code>.
    * 
    * @return The current selection provider.
    */
   public ISelectionProvider getSelectionProvider() {
       return m_selectionProvider;
   }

   /**
    * The <code>MultiPageEditorSite</code> implementation of this 
    * <code>IWorkbenchPartSite</code> method forwards to the multi-page editor to
    * return the shell.
    * 
    * @return The shell in which this editor site resides.
    */
   public Shell getShell() {
       return getMultiPageEditor().getSite().getShell();
   }

   /**
    * The <code>MultiPageEditorSite</code> implementation of this 
    * <code>IWorkbenchPartSite</code> method forwards to the multi-page editor to
    * return the workbench window.
    * 
    * @return The workbench window in which this editor site resides.
    */
   public IWorkbenchWindow getWorkbenchWindow() {
       return getMultiPageEditor().getSite().getWorkbenchWindow();
   }

   /**
    * Handles a selection changed event from the nested editor.
    * The default implementation gets the selection provider from the
    * multi-page editor's site, and calls <code>fireSelectionChanged</code>
    * on it (only if it is an instance of <code>MultiPageSelectionProvider</code>),
    * passing a new event object.
    * <p>
    * Subclasses may extend or reimplement this method.
    * </p>
    *
    * @param event the event
    */
   protected void handleSelectionChanged(SelectionChangedEvent event) {
       ISelectionProvider parentProvider = getMultiPageEditor().getSite()
               .getSelectionProvider();
       if (parentProvider instanceof MultiPageSelectionProvider) {
           SelectionChangedEvent newEvent = new SelectionChangedEvent(
                   parentProvider, event.getSelection());
           ((MultiPageSelectionProvider) parentProvider)
                   .fireSelectionChanged(newEvent);
       }
   }

   /**
    * The <code>MultiPageEditorSite</code> implementation of this 
    * <code>IWorkbenchPartSite</code> method forwards to the multi-page editor for
    * registration.
    * 
    * @param menuID The identifier for the menu.
    * @param menuMgr The menu manager
    * @param selProvider The selection provider.
    */
   public void registerContextMenu(String menuID, MenuManager menuMgr,
           ISelectionProvider selProvider) {
       getMultiPageEditor().getSite().registerContextMenu(menuID, menuMgr, selProvider);
   }

   /**
    * The <code>MultiPageEditorSite</code> implementation of this 
    * <code>IWorkbenchPartSite</code> method forwards to the multi-page editor for
    * registration.
    * 
    * @param menuManager The menu manager
    * @param selProvider The selection provider.
    */
   public void registerContextMenu(MenuManager menuManager,
           ISelectionProvider selProvider) {
       getMultiPageEditor().getSite().registerContextMenu(menuManager,
               selProvider);
   }

   /**
    * The <code>MultiPageEditorSite</code> implementation of this
    * <code>IWorkbenchPartSite</code> method remembers the selection provider,
    * and also hooks a listener on it, which calls
    * <code>handleSelectionChanged</code> when a selection changed event
    * occurs.
    * 
    * @param provider The selection provider.
    */
   public void setSelectionProvider(ISelectionProvider provider) {
       ISelectionProvider oldSelectionProvider = m_selectionProvider;
       m_selectionProvider = provider;
       if (oldSelectionProvider != null) {
           oldSelectionProvider
                   .removeSelectionChangedListener(getSelectionChangedListener());
       }
       if (m_selectionProvider != null) {
           m_selectionProvider
                   .addSelectionChangedListener(getSelectionChangedListener());
       }
   }

   public void progressEnd(@SuppressWarnings("unused") Job job) {
       // Do nothing
   }

   public void progressStart(@SuppressWarnings("unused") Job job) {
       // Do nothing
   }

   /* (non-Javadoc)
    * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
    */
   public Object getAdapter(@SuppressWarnings("unused") Class adapter) {
       return null;
   }

   /* (non-Javadoc)
    * @see org.eclipse.ui.IWorkbenchPartSite#getPart()
    */
   public IWorkbenchPart getPart() {
       return m_editor;
   }
   
    public final Object getService(final Class key) {
        return m_serviceLocator.getService(key);
    }
   
   public boolean hasService(Class key)
   {
        return m_serviceLocator.hasService(key);
   }

   public final void registerContextMenu(final String menuId,
         final MenuManager menuManager,
         final ISelectionProvider selectionProvider,
         final boolean includeEditorInput) {
      getMultiPageEditor().getEditorSite().registerContextMenu(menuId,
            menuManager, selectionProvider, includeEditorInput);
   }

   public final void registerContextMenu(final MenuManager menuManager,
         final ISelectionProvider selectionProvider,
         final boolean includeEditorInput) {
      getMultiPageEditor().getEditorSite().registerContextMenu(menuManager,
            selectionProvider, includeEditorInput);
   }
   
   /**
    * The nested editor.
    */
   private IEditorPart m_editor;

   /**
    * The multi-page editor.
    */
   private PSMultiPageEditorBase m_multiPageEditor;

   /**
    * The selection provider; <code>null</code> if none.
    * @see MultiPageEditorSite#setSelectionProvider(ISelectionProvider)
    */
   private ISelectionProvider m_selectionProvider = null;

   /**
    * The selection change listener, initialized lazily; <code>null</code>
    * if not yet created.
    */
   private ISelectionChangedListener m_selectionChangedListener = null;

   /**
    * The cached copy of the key binding service specific to this multi-page
    * editor site.  This value is <code>null</code> if it is not yet
    * initialized.
    */
   private IKeyBindingService m_service = null;
   
    /**
     * The local service locator for this multi-page editor site. This value is
     * never <code>null</code>.
     */
    private final ServiceLocator m_serviceLocator;
}
