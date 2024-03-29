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

package de.byteaction.velocity.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import de.byteaction.velocity.vaulttec.ui.IPreferencesConstants;
import de.byteaction.velocity.vaulttec.ui.VelocityPlugin;

/**
 * Velocity runtime settings, e.g. loop counter name.
 */
public class VelocityPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{

    private final String PREFIX = "VelocityPreferences.";

    public VelocityPreferencePage()
    {
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(VelocityPlugin.getDefault().getPreferenceStore());
        setDescription(VelocityPlugin.getMessage(PREFIX + "description"));
    }

    /**
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    protected void createFieldEditors()
    {
        StringFieldEditor counterName = new StringFieldEditor(IPreferencesConstants.VELOCITY_COUNTER_NAME, VelocityPlugin.getMessage(PREFIX + "counterName"), getFieldEditorParent());
        counterName.setEmptyStringAllowed(false);
        addField(counterName);
        DirectiveEditor directives = new DirectiveEditor(IPreferencesConstants.VELOCITY_USER_DIRECTIVES, VelocityPlugin.getMessage(PREFIX + "userDirectives"), getFieldEditorParent());
        addField(directives);
    }

    /**
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench aWorkbench)
    {
    }

    /**
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    public boolean performOk()
    {
        boolean value = super.performOk();
        VelocityPlugin.getDefault().savePluginPreferences();
        return value;
    }
}
