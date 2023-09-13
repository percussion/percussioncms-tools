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

package de.byteaction.velocity.vaulttec.ui.editor.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import de.byteaction.velocity.vaulttec.ui.VelocityPlugin;
import de.byteaction.velocity.vaulttec.ui.VelocityPluginImages;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.1 $
 * @author <a href="mailto:as@byteaction.de">Akmal Sarhan </a>
 */
public class CollapseAllAction extends Action
{

    private AbstractTreeViewer fViewer;

    public CollapseAllAction(AbstractTreeViewer aViewer)
    {
        fViewer = aViewer;
        setText(VelocityPlugin.getMessage("VelocityEditor.CollapseAllAction.label"));
        setToolTipText(VelocityPlugin.getMessage("VelocityEditor.CollapseAllAction.tooltip"));
        VelocityPluginImages.setLocalImageDescriptors(this, "collapseall.gif");
    }

    /**
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run()
    {
        fViewer.collapseAll();
    }
}
