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

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import de.byteaction.velocity.vaulttec.ui.VelocityPluginImages;
import de.byteaction.velocity.vaulttec.ui.model.Directive;
import de.byteaction.velocity.vaulttec.ui.model.ITreeNode;

/**
 * Standard label provider for Velocity template elements.
 */
public class VelocityOutlineLabelProvider extends LabelProvider
{

    /**
     * @see ILabelProvider#getImage(Object)
     */
    public Image getImage(Object anElement)
    {
        if (anElement instanceof Directive)
        {
            int type = ((Directive) anElement).getType();
            String name;
            if (type < Directive.TYPE_MACRO_CALL)
            {
                name = VelocityPluginImages.IMG_OBJ_SYSTEM_DIRECTIVE;
            } else if (type == Directive.TYPE_MACRO_CALL)
            {
                name = VelocityPluginImages.IMG_OBJ_MACRO;
            } else
            {
                name = VelocityPluginImages.IMG_OBJ_USER_DIRECTIVE;
            }
            return VelocityPluginImages.get(name);
        }
        return null;
    }

    /**
     * @see ILabelProvider#getText(Object)
     */
    public String getText(Object anElement)
    {
        return ((ITreeNode) anElement).getName();
    }
}
