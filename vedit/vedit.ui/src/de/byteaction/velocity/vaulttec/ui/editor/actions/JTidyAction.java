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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.w3c.tidy.Tidy;
import de.byteaction.velocity.editor.compare.VelocityCompare;
import de.byteaction.velocity.editor.compare.VelocityInput;
import de.byteaction.velocity.preferences.JtidyPreferencePage;
import de.byteaction.velocity.vaulttec.ui.VelocityPlugin;

/**
 * DOCUMENT ME!
 * 
 * @author <a href="mailto:as@byteaction.de">Akmal Sarhan </a>
 * @version $Revision: 1.1 $
 */
public class JTidyAction extends TextEditorAction implements IObjectActionDelegate
{

    public JTidyAction(ResourceBundle aBundle, String aPrefix, ITextEditor anEditor)
    {
        super(aBundle, aPrefix, anEditor);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run()
    {
        final IDocument document = getTextEditor().getDocumentProvider().getDocument(getTextEditor().getEditorInput());
        tidy(document);
    }

    private void tidy(IDocument fDocument)
    {
        Tidy tidy = new Tidy();
        IPreferenceStore preferenceStore = VelocityPlugin.getDefault().getPreferenceStore();
        Properties properties = new Properties();
        for (int i = 0; i < JtidyPreferencePage.JTIDY_PREF.length; i++)
        {
            properties.setProperty(JtidyPreferencePage.JTIDY_PREF[i], String.valueOf(preferenceStore.getBoolean(JtidyPreferencePage.JTIDY_PREF[i])));
        }
        for (int i = 0; i < JtidyPreferencePage.JTIDY_NUMER.length; i++)
        {
            properties.setProperty(JtidyPreferencePage.JTIDY_NUMER[i], String.valueOf(preferenceStore.getInt(JtidyPreferencePage.JTIDY_NUMER[i])));
        }
        properties.setProperty(JtidyPreferencePage.JTIDY_INDENT, preferenceStore.getBoolean(JtidyPreferencePage.JTIDY_INDENT) ? "auto" : "no");
        tidy.setConfigurationFromProps(properties);
        BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(fDocument.get().getBytes()));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream errOut = new ByteArrayOutputStream();
        PrintWriter printWriter = new PrintWriter(errOut, true);
        tidy.setErrout(printWriter);
        tidy.parse(in, out);
        VelocityInput left = new VelocityInput("left", fDocument.get());
        VelocityInput right = new VelocityInput("right", new String(out.toByteArray()));
        BufferedReader rd = new BufferedReader(new StringReader(new String(errOut.toByteArray())));
        String patternStr = "^line";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher("");
        StringBuffer buffer = new StringBuffer();
        // Retrieve all lines that match pattern
        String line = null;
        try
        {
            while ((line = rd.readLine()) != null)
            {
                matcher.reset(line);
                if (matcher.find())
                {
                    buffer.append(line);
                    buffer.append(FormatAction.LINE_SEP);
                }
            }
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String errText = buffer.toString();
        if (VelocityCompare.compare(left, right, errText) == 0) fDocument.set(new String(out.toByteArray()));
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction,
     *      org.eclipse.ui.IWorkbenchPart)
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart)
    {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action)
    {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection)
    {
        // TODO Auto-generated method stub
    }
}
