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

import java.util.ResourceBundle;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;
import de.byteaction.velocity.editor.VelocityEditor;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.1 $
 * @author <a href="mailto:as@byteaction.de">Akmal Sarhan </a>
 */
public class GotoDefinitionAction extends TextEditorAction
{

    public GotoDefinitionAction(ResourceBundle aBundle, String aPrefix, ITextEditor anEditor)
    {
        super(aBundle, aPrefix, anEditor);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run()
    {
        VelocityEditor editor = (VelocityEditor) getTextEditor();
        ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();
        if (!selection.isEmpty())
        {
            editor.gotoDefinition(new Region(selection.getOffset(), selection.getLength()));
        }
    }
}
