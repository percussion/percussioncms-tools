/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package de.byteaction.velocity.editor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.part.MultiPageEditorSite;
import de.byteaction.velocity.vaulttec.ui.VelocityPlugin;

/**
 * An example showing how to create a multi-page vEditor. This example has 3
 * pages:
 * <ul>
 * <li>page 0 contains a nested text vEditor.
 * <li>page 1 allows you to change the font used in page 2
 * <li>page 2 shows the words in page 0 in sorted order
 * </ul>
 */
public class MultiPageEditor extends MultiPageEditorPart implements IResourceChangeListener
{

    /** The text vEditor used in page 0. */
    private VelocityEditor vEditor;
    /** The text widget used in page 2. */
    private String         text;
    /**
     * Creates page 1 of the multi-page vEditor, which allows you to change the
     * font used in page 2.
     */
    List                   tmpFiles = null;
    public Browser         browser  = null;
   private boolean fHidePreviewPage;

    private static class MultiPageVelocityEditorrSite extends MultiPageEditorSite
    {

        public MultiPageVelocityEditorrSite(final MultiPageEditorPart mltiPagEdtr, final IEditorPart edtr)
        {
            super(mltiPagEdtr, edtr);
        }

        public IEditorActionBarContributor getActionBarContributor()
        {
            final MultiPageEditor edtr = (MultiPageEditor) getMultiPageEditor();
            VelocityEditorActionContributor ret = edtr.getContributor();
            return ret;
        }
    }

    public VelocityEditorActionContributor getContributor()
    {
        final IEditorSite site = getEditorSite();
        final VelocityEditorActionContributor ret = (VelocityEditorActionContributor) site.getActionBarContributor();
        return ret;
    }

    protected IEditorSite createSite(final IEditorPart edtr)
    {
        final IEditorSite ret = new MultiPageVelocityEditorrSite(this, edtr);
        return ret;
    }

    /**
     * Creates a multi-page vEditor example.
     */
    public MultiPageEditor()
    {
        super();
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
    }

    /**
     * Creates page 0 of the multi-page vEditor, which contains a text vEditor.
     */
    void createPage0()
    {
        try
        {
            vEditor = new VelocityEditor();
            int index = addPage(vEditor, getEditorInput());
            setPageText(index, "source");
            setPartName(vEditor.getTitle());
            vEditor.setMultiPartEditor(this);
        }
        catch (PartInitException e)
        {
            ErrorDialog.openError(getSite().getShell(), "Error creating nested text vEditor", null, e.getStatus());
        }
    }

    void createPage1()
    {
        createControl(getContainer(), SWT.BOLD);
    }

    /**
     * Creates page 2 of the multi-page vEditor, which shows the sorted text.
     */
    /**
     * Creates the pages of the multi-page vEditor.
     */
    protected void createPages()
    {
        createPage0();
        if (!fHidePreviewPage)
        {
           createPage1();
        }
        else
        {
           CTabFolder folder = (CTabFolder) getContainer();
           folder.setTabHeight(0);
        }
    }

    /**
     * Saves the multi-page vEditor's document.
     */
    public void doSave(IProgressMonitor monitor)
    {
        getEditor(0).doSave(monitor);
    }

    /**
     * Saves the multi-page vEditor's document as another file. Also updates the
     * text for page 0's tab, and updates this multi-page vEditor's input to
     * correspond to the nested vEditor's.
     */
    public void doSaveAs()
    {
        IEditorPart editor = getEditor(0);
        editor.doSaveAs();
        setPageText(0, editor.getTitle());
        setInput(editor.getEditorInput());
    }

    /*
     * (non-Javadoc) Method declared on IEditorPart
     */
    /**
     * The <code>MultiPageEditorExample</code> implementation of this method
     * checks that the input is an instance of <code>IFileEditorInput</code>.
     */
    public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException
    {
        if (!(editorInput instanceof IFileEditorInput)) { throw new PartInitException("Invalid Input: Must be IFileEditorInput"); }
        super.init(site, editorInput);
    }

    /*
     * (non-Javadoc) Method declared on IEditorPart.
     */
    public boolean isSaveAsAllowed()
    {
        return true;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param aClass
     *            DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public Object getAdapter(Class aClass)
    {
        return vEditor.getAdapter(aClass);
    }
    
    /**
     * Get the page count so a manipulator can figure out what's visible
     */
    public int getPageCount()
    {
       return super.getPageCount();
    }

    /**
     * Calculates the contents of page 2 when the it is activated.
     */
    protected void pageChange(int newPageIndex)
    {
        super.pageChange(newPageIndex);
        if (newPageIndex > getPageCount()) return;
        text = vEditor.getDocumentProvider().getDocument(vEditor.getEditorInput()).get();
        showPreview(newPageIndex);
    }

    private void showPreview(int newPageIndex)
    {
        if (newPageIndex == 1)
        {
            // browser.refresh();
            try
            {
                File file = File.createTempFile("temp", ".html");
                OutputStream stream = new FileOutputStream(file);
                PrintWriter writer = new PrintWriter(stream);
                writer.println(text);
                writer.flush();
                stream.close();
                if (tmpFiles == null)
                {
                    tmpFiles = new Vector();
                }
                tmpFiles.add(file.getAbsolutePath());
                if (VelocityPlugin.isBrowserSupported)
                {
                    // browser.setUrl("file:///" + file.getAbsolutePath());
                    browser.setText(text);
                    // browser.refresh();
                    // browser.redraw();
                    // browser.getParent().redraw();
                }
            }
            catch (Exception e)
            {
                VelocityPlugin.log(e);
            }
        }
    }

    public void gotoMarker(IMarker marker)
    {
        setActivePage(0);
        IDE.gotoMarker(getEditor(0), marker);
    }

    public void dispose()
    {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        super.dispose();
    }

    protected void createControl(Composite parent, int style)
    {
        if (VelocityPlugin.isBrowserSupported)
        {
            browser = new Browser(parent, SWT.NONE);
            // browser.setLayoutData(new GridData(GridData.FILL_BOTH));
            // browser.setUrl("http://eclipse.org");
            // browser.redraw();
            int index = addPage(browser);
            setPageText(index, "Preview");
        }
    }

    public void resourceChanged(final IResourceChangeEvent event)
    {
        if (event.getType() == IResourceChangeEvent.PRE_CLOSE)
        {
            Display.getDefault().asyncExec(new Runnable() {

                public void run()
                {
                    IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
                    for (int i = 0; i < pages.length; i++)
                    {
                        if (((FileEditorInput) vEditor.getEditorInput()).getFile().getProject().equals(event.getResource()))
                        {
                            IEditorPart editorPart = pages[i].findEditor(vEditor.getEditorInput());
                            pages[i].closeEditor(editorPart, true);
                        }
                    }
                }
            });
        }
    }

    /**
     * Hide the preview page when creating controls
     * @param hidePreviewPage <code>true</code> if the preview page should not be
     * shown.
     */
   public void hidePreview(boolean hidePreviewPage)
   {
      fHidePreviewPage = hidePreviewPage;
   }
}