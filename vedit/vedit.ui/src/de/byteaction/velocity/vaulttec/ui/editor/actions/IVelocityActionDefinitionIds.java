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

/**
 * Defines the definition IDs for the Velocity editor actions.
 */
public interface IVelocityActionDefinitionIds
// extends IJavaEditorActionDefinitionIds
{

    /**
     * Action definition ID of the 'Navigate -> Go To Definition' action.
     */
    public static final String GOTO_DEFINITION = "de.byteaction.velocity.vaulttec.ui.edit.goto.definition";
    public static final String TOGGLE_COMMENT  = "de.byteaction.velocity.vaulttec.ui.edit.toggle.comment";
    public static final String JTIDY           = "de.byteaction.velocity.vaulttec.ui.edit.jtidy";
    public static final String FORMAT          = "de.byteaction.velocity.vaulttec.ui.edit.format";
}
