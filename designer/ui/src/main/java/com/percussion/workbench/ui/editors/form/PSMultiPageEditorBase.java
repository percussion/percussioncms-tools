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
 * [ PSMultiPageEditorBase.java ]
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

import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.help.PSHelpManager;
import com.percussion.workbench.ui.util.PSControlInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.INestableKeyBindingService;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IWorkbenchPartOrientation;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.part.MultiPageEditorSite;

import java.util.ArrayList;

public abstract class PSMultiPageEditorBase extends PSEditorBase
// code borrowed from org.eclipse.ui.part.MultiPageEditorPart
// to add multipage functionality.
{
   /**
    * Logger used in this class.
    */
   private static final Logger ms_log = LogManager.getLogger(PSMultiPageEditorBase.class);

   @Override
   public final void createControl(@SuppressWarnings("unused") Composite comp)
   {
      m_helpManager = new PSHelpManager(this);
      createPages();
      // set the active page (page 0 by default), unless it has already been done
      if (getActivePage() == -1)
          setActivePage(0);
   }
   
   /**
    * Creates and adds a new page containing the given control to this multi-page 
    * editor.  The control may be <code>null</code>, allowing it to be created
    * and set later using <code>setControl</code>.
    *
    * @param control the control, or <code>null</code>
    * @return the index of the new page
    *
    */
   public int addPage(Control control) {
       int index = getPageCount();
       addPage(index, control);
       return index;
   }

   /**
    * Creates and adds a new page containing the given control to this multi-page 
    * editor.  The page is added at the given index.
    * The control may be <code>null</code>, allowing it to be created
    * and set later using <code>setControl</code>.
    *
    * @param index the index at which to add the page (0-based)
    * @param control the control, or <code>null</code>
    */
   public void addPage(int index, Control control) {
       createItem(index, control);
   }

   /**
    * Creates and adds a new page containing the given editor to this multi-page 
    * editor. This also hooks a property change listener on the nested editor.
    *
    * @param editor the nested editor
    * @param input the input for the nested editor
    * @return the index of the new page
    * @exception PartInitException if a new page could not be created
    *
    */
   public int addPage(IEditorPart editor, IEditorInput input)
           throws PartInitException {
       int index = getPageCount();
       addPage(index, editor, input);
       return index;
   }

   /**
    * Creates and adds a new page containing the given editor to this multi-page 
    * editor. The page is added at the given index. 
    * This also hooks a property change listener on the nested editor.
    *
    * @param index the index at which to add the page (0-based)
    * @param editor the nested editor
    * @param input the input for the nested editor
    * @exception PartInitException if a new page could not be created
    *
    */
   @SuppressWarnings("unchecked")
   public void addPage(int index, IEditorPart editor, IEditorInput input)
           throws PartInitException {
       IEditorSite site = createSite(editor);
       // call init first so that if an exception is thrown, we have created no new widgets
       editor.init(site, input);
       Composite parent2 = new Composite(getContainer(), getOrientation(editor));
       parent2.setLayout(new FillLayout());
       editor.createPartControl(parent2);
       editor.addPropertyListener(new IPropertyListener() {
           public void propertyChanged(@SuppressWarnings("unused") Object source,
              int propertyId) {
               PSMultiPageEditorBase.this.handlePropertyChange(propertyId);
           }
       });
       // create item for page only after createPartControl has succeeded
       Item item = createItem(index, parent2);
       // remember the editor, as both data on the item, and in the list of editors (see field comment)
       item.setData(editor);
       m_nestedEditors.add(editor);
   }

   /**
    * Get the orientation of the editor.
    * @param editor
    * @return int the orientation flag
    * @see SWT#RIGHT_TO_LEFT
    * @see SWT#LEFT_TO_RIGHT
    * @see SWT#NONE
    */
   private int getOrientation(IEditorPart editor) {
      if(editor instanceof IWorkbenchPartOrientation)
         return ((IWorkbenchPartOrientation) editor).getOrientation();
      return getOrientation();
   }   

   /**
    * Creates a tab item at the given index and places 
    * the given control in the new item.
    * The item is a PSTabItem with no style bits set.
    *
    * @param index the index at which to add the control
    * @param control is the control to be placed in an item
    * @return a new item
    */
   private PSTabItem createItem(int index, Control control) {
       PSTabItem item = new PSTabItem(getTabFolder(), SWT.NONE, index);
       ScrolledComposite comp = (ScrolledComposite)control.getParent();
       comp.setContent(control);
       Point preferred = control.computeSize(SWT.DEFAULT, SWT.DEFAULT);
       comp.setMinSize(
          preferred.x, preferred.y);
       comp.setExpandHorizontal(true);
       comp.setExpandVertical(true);
       item.setControl(comp);
       item.setData(control);
       return item;
   }
   
   /**
    * Creates an empty container. Creates a CTabFolder with no style bits set, and
    * hooks a selection listener which calls <code>pageChange()</code> whenever
    * the selected tab changes.
    * 
    * @param parent
    *            The composite in which the container tab folder should be
    *            created; must not be <code>null</code>.
    * @return a new container
    */
   @Override
   protected Composite createContainer(Composite parent) {
       // use SWT.FLAT style so that an extra 1 pixel border is not reserved
       // inside the folder
       final CTabFolder newContainer = new CTabFolder(parent, SWT.BOTTOM
               | SWT.FLAT);
       newContainer.addSelectionListener(new SelectionAdapter() {
           @Override
         public void widgetSelected(SelectionEvent e) {
               int newPageIndex = newContainer.indexOf((CTabItem) e.item);
               pageChange(newPageIndex);
           }
       });
       return newContainer;
   }
   
   /**
    * Handles a property change notification from a nested editor.
    * The default implementation simply forwards the change to listeners
    * on this multi-page editor by calling <code>firePropertyChange</code>
    * with the same property id.  For example, if the dirty state of a nested editor
    * changes (property id <code>IEditorPart.PROP_DIRTY</code>), this method
    * handles it by firing a property change event for <code>IEditorPart.PROP_DIRTY</code>
    * to property listeners on this multi-page editor.
    * <p>
    * Subclasses may extend or reimplement this method.
    * </p>
    *
    * @param propertyId the id of the property that changed
    */
   protected void handlePropertyChange(int propertyId) {
       firePropertyChange(propertyId);
   }

   /**
    * Creates the pages of this multi-page editor.
    * <p>
    * Subclasses must implement this method.
    * </p>
    */
   protected abstract void createPages();

   /**
    * Creates the site for the given nested editor.
    * The <code>MultiPageEditorPart</code> implementation of this method creates an 
    * instance of <code>MultiPageEditorSite</code>. Subclasses may reimplement
    * to create more specialized sites.
    *
    * @param editor the nested editor
    * @return the editor site
    */
   protected IEditorSite createSite(IEditorPart editor) {
       return new PSMultiPageEditorSite(this, editor);
   }

   /**
    * The <code>MultiPageEditorPart</code> implementation of this 
    * <code>IWorkbenchPart</code> method disposes all nested editors.
    * Subclasses may extend.
    */
   @Override
   public void dispose()
   {
      disposeParts();
      super.dispose();
   }
   
   /**
    * Dispose all part editors.
    */
   public void disposeParts()
   {
      for (int i=0; i<m_nestedEditors.size(); ++i)
      {
         IEditorPart editor = (IEditorPart) m_nestedEditors.get(i);
         disposePart(editor);
      }

      m_nestedEditors.clear();
   }

   /**
    * Returns the active nested editor if there is one.
    * <p>
    * Subclasses should not override this method
    * </p>
    * 
    * @return the active nested editor, or <code>null</code> if none
    */
   protected IEditorPart getActiveEditor() {
       int index = getActivePage();
       if (index != -1)
           return getEditor(index);
       return null;
   }

   /**
    * Returns the index of the currently active page,
    * or -1 if there is no active page.
    * <p>
    * Subclasses should not override this method
    * </p>
    *
    * @return the index of the active page, or -1 if there is no active page
    */
   protected int getActivePage() {
       CTabFolder tabFolder = getTabFolder();
       if (tabFolder != null && !tabFolder.isDisposed())
           return tabFolder.getSelectionIndex();
       return -1;
   }

   /**
    * Returns the composite control containing this multi-page editor's pages.
    * This should be used as the parent when creating controls for the individual pages.
    * That is, when calling <code>addPage(Control)</code>, the passed control should be
    * a child of this container.
    * <p>
    * Warning: Clients should not assume that the container is any particular subclass
    * of Composite.  The actual class used may change in order to improve the look and feel of
    * multi-page editors.  Any code making assumptions on the particular subclass would thus be broken.
    * </p>
    * <p>
    * Subclasses should not override this method
    * </p>
    *
    * @return the composite, or <code>null</code> if <code>createPartControl</code>
    *   has not been called yet
    */
   protected Composite getContainer() {
       
      return new ScrolledComposite(m_container, SWT.V_SCROLL | SWT.H_SCROLL);
   }
   
   

   /**
    * Returns the control for the given page index, or <code>null</code>
    * if no control has been set for the page.
    * The page index must be valid.
    * <p>
    * Subclasses should not override this method
    * </p>
    *
    * @param pageIndex the index of the page
    * @return the control for the specified page, or <code>null</code> if none has been set
    */
   protected Control getControl(int pageIndex) {
       return getItem(pageIndex).getControl();
   }

   /**
    * Returns the editor for the given page index.
    * The page index must be valid.
    *
    * @param pageIndex the index of the page
    * @return the editor for the specified page, or <code>null</code> if the
    *   specified page was not created with 
    *   <code>addPage(IEditorPart,IEditorInput)</code>
    */
   protected IEditorPart getEditor(int pageIndex) {
       Item item = getItem(pageIndex);
       if (item != null) {
           Object data = item.getData();
           if (data instanceof IEditorPart) {
               return (IEditorPart) data;
           }
       }
       return null;
   }

   /**
    * Returns the tab item for the given page index (page index is 0-based).
    * The page index must be valid.
    *
    * @param pageIndex the index of the page
    * @return the tab item for the given page index
    */
   protected PSTabItem getItem(int pageIndex) {
       return (PSTabItem)getTabFolder().getItem(pageIndex);
   }

   /**
    * Returns the number of pages in this multi-page editor.
    *
    * @return the number of pages
    */
   protected int getPageCount() {
       CTabFolder folder = getTabFolder();
       // May not have been created yet, or may have been disposed.
       if (folder != null && !folder.isDisposed())
           return folder.getItemCount();
       return 0;
   }

   /**
    * Returns the image for the page with the given index,
    * or <code>null</code> if no image has been set for the page.
    * The page index must be valid.
    *
    * @param pageIndex the index of the page
    * @return the image, or <code>null</code> if none
    */
   protected Image getPageImage(int pageIndex) {
       return getItem(pageIndex).getImage();
   }

   /**
    * Returns the text label for the page with the given index.
    * Returns the empty string if no text label has been set for the page.
    * The page index must be valid.
    *
    * @param pageIndex the index of the page
    * @return the text label for the page
    */
   protected String getPageText(int pageIndex) {
       return getItem(pageIndex).getText();
   }    
   
   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#isMultiPage()
    */
   @Override
   protected boolean isMultiPage()
   {
      return true;
   }
   /**
    * Returns the tab folder containing this multi-page editor's pages.
    *
    * @return the tab folder, or <code>null</code> if <code>createPartControl</code>
    *   has not been called yet
    */
   private CTabFolder getTabFolder() {
       return (CTabFolder)m_container;
   }
   
   /**
    * The <code>PSMultiPageEditorBase</code> implementation of this <code>IEditorPart</code>
    * method sets its site to the given site, its input to the given input, and
    * the site's selection provider to a <code>PSMultiPageSelectionProvider</code>.
    * Subclasses may extend this method.
    * 
    * @param site
    *            The site for which this part is being created; must not be <code>null</code>.
    * @param input
    *            The input on which this editor should be created; must not be
    *            <code>null</code>.
    * @throws PartInitException 
    */
   @Override
   public void init(IEditorSite site, IEditorInput input) throws PartInitException
   {
       super.init(site, input);
       site.setSelectionProvider(new PSMultiPageSelectionProvider(this));       
   }
   
   /**
    * Notifies this multi-page editor that the page with the given id has been
    * activated. This method is called when the user selects a different tab.
    * <p>
    * The <code>MultiPageEditorPart</code> implementation of this method 
    * sets focus to the new page, and notifies the action bar contributor (if there is one).
    * This checks whether the action bar contributor is an instance of 
    * <code>MultiPageEditorActionBarContributor</code>, and, if so,
    * calls <code>setActivePage</code> with the active nested editor.
    * This also fires a selection change event if required.
    * </p>
    * <p>
    * Subclasses may extend this method.
    * </p>
    *
    * @param newPageIndex the index of the activated page 
    */
   protected void pageChange(int newPageIndex) {
      
       Control control = getControl(newPageIndex);
       if (control != null) {
           control.setVisible(true);
       }
              
       setFocus();
       IEditorPart activeEditor = getEditor(newPageIndex);
       IEditorActionBarContributor contributor = getEditorSite()
               .getActionBarContributor();
       if (contributor != null
               && contributor instanceof MultiPageEditorActionBarContributor) {
           ((MultiPageEditorActionBarContributor) contributor)
                   .setActivePage(activeEditor);
       }
       if (activeEditor != null) {
          //Workaround for 1GAUS7C: ITPUI:ALL - Editor not activated when restored from previous session
          //do not need second if once fixed
          ISelectionProvider selectionProvider = activeEditor.getSite()
          .getSelectionProvider();
          if (selectionProvider != null) 
          {
             ISelection selection = selectionProvider.getSelection(); 
             if(selection != null)
             {
                SelectionChangedEvent event = new SelectionChangedEvent(
                   selectionProvider, selectionProvider.getSelection());
                ((PSMultiPageSelectionProvider) getSite().getSelectionProvider())
                .fireSelectionChanged(event);
             }
          }
       }
   }
   
   

   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#getPagename(int)
    */
   @Override
   protected String getPagename(int index)
   {
      if(index == -1)
         return null;
      return getPageText(index);
   }

   /**
    * Implemented to return [CLASS NAME]_[TAB NAME KEY] 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#
    * getHelpKey(org.eclipse.swt.widgets.Control)
    */
   @Override
   public String getHelpKey(Control control)
   {
     String baseKey =  super.getHelpKey(control);
     String tabKey = getItem(getActivePage()).getTextKey();
     return baseKey + "_" + tabKey;
   }

   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#
    * initHelpManager()
    */
   @Override
   protected void initHelpManager()
   {
      m_helpManager.registerControls(getTabFolder());
   }   

   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#
    * disableEditorControls()
    */
   @Override
   protected void disableEditorControls()
   {
      int count = getPageCount();
      for(int i = 0; i < count; i++)
      {
         disableAllControls(getControl(i));
      }
   }
   

   /**
    * Disposes the given part and its site.
    * 
    * @param part
    *            The part to dispose; must not be <code>null</code>.
    */
   private void disposePart(final IWorkbenchPart part) {
       Platform.run(new SafeRunnable() {
           public void run() {
               if (part.getSite() instanceof MultiPageEditorSite) {
                   MultiPageEditorSite partSite = (MultiPageEditorSite) part
                           .getSite();
                   partSite.dispose();
               }
               part.dispose();
           }

           @Override
         public void handleException(@SuppressWarnings("unused") Throwable e) {
               //Exception has already being logged by Core. Do nothing.
           }
       });
   }

   /**
    * Removes the page with the given index from this multi-page editor. The
    * controls for the page are disposed of; if the page has an editor, it is
    * disposed of too. The page index must be valid.
    * 
    * @param pageIndex
    *            the index of the page
    * @see MultiPageEditorPart#addPage(Control)
    * @see MultiPageEditorPart#addPage(IEditorPart, IEditorInput)
    */
   public void removePage(int pageIndex) {
       Assert.isTrue(pageIndex >= 0 && pageIndex < getPageCount());
       // get editor (if any) before disposing item
       IEditorPart editor = getEditor(pageIndex);
       // dispose item before disposing editor, in case there's an exception in editor's dispose
       PSTabItem item = getItem(pageIndex);
       Control control = item.getControl();
       if(control != null && !control.isDisposed())
          control.dispose();
       item.dispose();
       // dispose editor (if any)
       if (editor != null) {
           m_nestedEditors.remove(editor);
           disposePart(editor);
       }
       modifyControlInfoPage(pageIndex);
   }
   
   private void modifyControlInfoPage(int page)
   {
      for(PSControlInfo info : m_controlInfo.values())
      {
         
         if(info.getPage() > page)
            info.setPage(info.getPage() - 1);
         
      }
   }

   /**
    * Sets the currently active page.
    *
    * @param pageIndex the index of the page to be activated; the index must be valid
    */
   protected void setActivePage(int pageIndex) {
       Assert.isTrue(pageIndex >= 0 && pageIndex < getPageCount());
       getTabFolder().setSelection(pageIndex);       
   }

   /**
    * Sets the control for the given page index.
    * The page index must be valid.
    *
    * @param pageIndex the index of the page
    * @param control the control for the specified page, or <code>null</code> to clear the control
    */
   protected void setControl(int pageIndex, Control control) {
       getItem(pageIndex).setControl(control);
   }

   /**
    * The <code>MultiPageEditor</code> implementation of this 
    * <code>IWorkbenchPart</code> method sets focus on the active nested editor,
    * if there is one.
    * <p>
    * Subclasses may extend or reimplement.
    * </p>
    */
   @Override
   public void setFocus() {
       setFocus(getActivePage());
   }

   /**
    * Sets focus to the control for the given page. If the page has an editor,
    * this calls its <code>setFocus()</code> method. Otherwise, this calls
    * <code>setFocus</code> on the control for the page.
    * 
    * @param pageIndex
    *            the index of the page
    */
   private void setFocus(int pageIndex) {
       final IKeyBindingService service = getSite().getKeyBindingService();
       if (pageIndex < 0 || pageIndex >= getPageCount()) {
           // There is no selected page, so deactivate the active service.
           if (service instanceof INestableKeyBindingService) {
               final INestableKeyBindingService nestableService = (INestableKeyBindingService) service;
               nestableService.activateKeyBindingService(null);
           } else {
              ms_log.warn(
                    "MultiPageEditorPart.setFocus()   Parent key binding service was not an instance of INestableKeyBindingService.  It was an instance of " + service.getClass().getName() + " instead."); //$NON-NLS-1$ //$NON-NLS-2$
           }
           return;
       }

       final IEditorPart editor = getEditor(pageIndex);
       if (editor != null) {
           editor.setFocus();
           // There is no selected page, so deactivate the active service.
           if (service instanceof INestableKeyBindingService) {
               final INestableKeyBindingService nestableService = (INestableKeyBindingService) service;
               if (editor != null) {
                   nestableService.activateKeyBindingService(editor
                           .getEditorSite());
               } else {
                   nestableService.activateKeyBindingService(null);
               }
           } else {
              ms_log.warn(
                    "MultiPageEditorPart.setFocus()   Parent key binding service was not an instance of INestableKeyBindingService.  It was an instance of " + service.getClass().getName() + " instead."); //$NON-NLS-1$ //$NON-NLS-2$
           }
       } else {
           // There is no selected editor, so deactivate the active service.
           if (service instanceof INestableKeyBindingService) {
               final INestableKeyBindingService nestableService = (INestableKeyBindingService) service;
               nestableService.activateKeyBindingService(null);
           } else {
               ms_log.warn(
                     "MultiPageEditorPart.setFocus()   Parent key binding service was not an instance of INestableKeyBindingService.  It was an instance of " + service.getClass().getName() + " instead."); //$NON-NLS-1$ //$NON-NLS-2$
           }

           // Give the page's control focus.
           final Control control = getControl(pageIndex);
           if (control != null) {
               control.setFocus();
           }
       }
   }

   /**
    * Sets the image for the page with the given index, or <code>null</code>
    * to clear the image for the page.
    * The page index must be valid.
    *
    * @param pageIndex the index of the page
    * @param image the image, or <code>null</code>
    */
   protected void setPageImage(int pageIndex, Image image) 
   {
       getItem(pageIndex).setImage(image);
   }

   /**
    * Sets the text label for the page with the given index.
    * The page index must be valid.
    * The textkey key for tab label must not be null.
    *
    * @param pageIndex the index of the page
    * @param textkey the key for the tab label
    */
   protected void setPageText(int pageIndex, String textkey)
   {
      getItem(pageIndex).setText(textkey);
   }
   
   /**
    * Sets the title for the page with the given index.
    * The page index must be valid.
    * The text label must not be null.
    *
    * @param pageIndex the index of the page
    * @param textkey the key for the title 
    */
   protected void setPageTitle(int pageIndex, String textkey)
   {      
      getItem(pageIndex).setTitle(textkey);
   }
   
   /**
    * Sets the title image for the page with the given index.
    * The page index must be valid.
    * The image label can not be null.
    *
    * @param pageIndex the index of the page
    * @param image the title image
    */
   protected void setPageTitleImage(int pageIndex, Image image)
   {
      getItem(pageIndex).setTitleImage(image);
   }
   
   /**
    * Returns the title for the page with the given index.
    * Returns the empty string if no text label has been set for the page.
    * The page index must be valid.
    *
    * @param pageIndex the index of the page
    * @return the text label for the page
    */
   protected String getPageTitle(int pageIndex)
   {
      return getItem(pageIndex).getTitle();
   }

   /**
    * Returns the index of the page with the given title.
    * @param name of the page for which the index is needed.
    * @param caseSensitive if <code>true</code> page titles are compared
    * with case sensitive otherwise case insensitive comparision is performed. 
    * @return int the index of the first page found with the supplied title or
    * -1 if not found or if title is <code>null</code> or empty.
    */
   protected int getPageIndexByName(String name, boolean caseSensitive)
   {
      if(StringUtils.isBlank(name))
         return -1;
      if(caseSensitive)
      {
         for(int i=0;i<getPageCount();i++)
         {
            if(name.equals(getItem(i).getText()))
               return i;
         }         
      }
      else
      {
         for(int i=0;i<getPageCount();i++)
         {
            if(name.equalsIgnoreCase(getItem(i).getText()))
               return i;
         }         
      }
      return -1;
   }

   /**
    * Returns the image for the page with the given index.
    * Returns null no image has been set for the page.
    * The page index must be valid.
    *
    * @param pageIndex the index of the page
    * @return the text label for the page
    */
   protected Image getPageTitleImage(int pageIndex)
   {
      return getItem(pageIndex).getTitleImage();
   }  
   
   /**
    * Convenience method to get string resource.
    */
   protected static String getMessage(final String key)
   {
      return PSMessages.getString(key);
   }

   /**
    * A simple class to extend CTabItem to add a couple
    * of properties.
    *
    */
   class PSTabItem extends CTabItem
   {

      public PSTabItem(CTabFolder parent, int style, int index)
      {
         super(parent, style, index);
      }

      public PSTabItem(CTabFolder parent, int style)
      {
         super(parent, style);
      }
      
      public String getTitle()
      {
         return mi_title;
      }
      
      public void setTitle(String title)
      {
         if(title == null)
            title = "";
         String titleLabel = PSMessages.stringExists(title) 
         ? PSMessages.getString(title)
            : title;
         mi_title = titleLabel;
      }
      
      public Image getTitleImage()
      {
         return mi_titleImage;
      }
      
      public void setTitleImage(Image image)
      {
         mi_titleImage = image;
      }
      
      /* 
       * @see org.eclipse.swt.custom.CTabItem#setText(java.lang.String)
       */
      @Override
      public void setText(String textkey)
      {
         mi_textKey = textkey;
         String text = PSMessages.stringExists(textkey) 
         ? PSMessages.getString(textkey)
            : textkey;
         super.setText(text);
      }
      
      /**
       * @return the resource key used to retrieve the
       * text for this tab item.
       */
      public String getTextKey()
      {
         return mi_textKey;
      }
      
      private String mi_title = "";
      private Image mi_titleImage;
      private String mi_textKey;
      
   }
   

   /* (non-Javadoc)
    * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(java.lang.Class)
    */
   @Override
   public Object getAdapter(Class adapter)
   {
      IEditorPart part = getActiveEditor();
      
      // Delegate to active editor
      if (part != null)
      {
         Object rval = part.getAdapter(adapter);
         if (rval != null) return rval;
      }
      
      return super.getAdapter(adapter);
   }  

   /**
    * List of nested editors. Element type: IEditorPart.
    * Need to hang onto them here, in addition to using get/setData on the items,
    * because dispose() needs to access them, but widgetry has already been disposed at that point.
    */
   private ArrayList m_nestedEditors = new ArrayList(3);

   
   

}
