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

/*
 * Created on 30.12.2004
 * 
 * TODO To change the template for this generated file go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
package de.byteaction.velocity.editor.compare;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Shell;
import de.byteaction.velocity.vaulttec.ui.VelocityPlugin;

/**
 * @author akmal
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class VelocityCompare
{

    public static int compare(final VelocityInput left, final VelocityInput right, String error)
    {
        Shell shell = new Shell();
        CompareConfiguration cc = new CompareConfiguration();
        CompareEditorInput input = new CompareEditorInput(cc) {

            protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
            {
                // VelocityInput left = new VelocityInput("left",
                // "these\nare\nthe\ncontents");
                // VelocityInput right = new VelocityInput("right",
                // "theasdse\nare\nthe\ncontednts");
                return new DiffNode(left, right);
            }
        };
        input.setTitle("Veloeclipse Compare editor");
        // CompareUI.openCompareEditor(input);
        return openCompareDialog(input, error);
    }

    public static int openCompareDialog(CompareEditorInput input, String error)
    {
        VelocityPlugin plugin = VelocityPlugin.getDefault();
        if (plugin != null) return plugin.openCompareDialog(input, error);
        return 0;
    }
}
