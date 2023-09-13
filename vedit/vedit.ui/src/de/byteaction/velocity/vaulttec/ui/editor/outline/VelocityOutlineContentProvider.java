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

package de.byteaction.velocity.vaulttec.ui.editor.outline;

import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import de.byteaction.velocity.editor.VelocityEditor;
import de.byteaction.velocity.vaulttec.ui.model.ITreeNode;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.1 $
 * @author <a href="mailto:as@byteaction.de">Akmal Sarhan </a>
 */
public class VelocityOutlineContentProvider implements ITreeContentProvider
{

    public static final String VELOCITY_TEMPLATE = "__velocity_template";
    private VelocityEditor     fEditor;
    private IPositionUpdater   fPositionUpdater;

    public VelocityOutlineContentProvider(VelocityEditor anEditor)
    {
        fEditor = anEditor;
        fPositionUpdater = new DefaultPositionUpdater(VELOCITY_TEMPLATE);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer aViewer, Object anOldInput, Object aNewInput)
    {
        if (anOldInput != aNewInput)
        {
            if (anOldInput != null)
            {
                IDocument document = fEditor.getDocumentProvider().getDocument(anOldInput);
                if (document != null)
                {
                    try
                    {
                        document.removePositionCategory(VELOCITY_TEMPLATE);
                    }
                    catch (BadPositionCategoryException e)
                    {
                    }
                    document.removePositionUpdater(fPositionUpdater);
                }
            }
            if (aNewInput != null)
            {
                IDocument document = fEditor.getDocumentProvider().getDocument(aNewInput);
                if (document != null)
                {
                    document.addPositionCategory(VELOCITY_TEMPLATE);
                    document.addPositionUpdater(fPositionUpdater);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose()
    {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement)
    {
        return fEditor.getRootElements();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object anElement)
    {
        return (anElement instanceof ITreeNode) ? ((ITreeNode) anElement).getChildren() : ITreeNode.NO_CHILDREN;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    public Object getParent(Object anElement)
    {
        return (anElement instanceof ITreeNode) ? ((ITreeNode) anElement).getParent() : null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren(Object anElement)
    {
        return (anElement instanceof ITreeNode) ? ((ITreeNode) anElement).hasChildren() : false;
    }
}
