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

package de.byteaction.velocity.vaulttec.ui.editor;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import de.byteaction.velocity.editor.VelocityEditor;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.1 $
 * @author <a href="mailto:as@byteaction.de">Akmal Sarhan </a>
 */
public class VelocityTextHover implements ITextHover
{

    private VelocityEditor fEditor;

    public VelocityTextHover(VelocityEditor anEditor)
    {
        fEditor = anEditor;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param aTextViewer
     *            DOCUMENT ME!
     * @param aRegion
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getHoverInfo(ITextViewer aTextViewer, IRegion aRegion)
    {
        return fEditor.getDefinitionLine(aRegion);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param aTextViewer
     *            DOCUMENT ME!
     * @param anOffset
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public IRegion getHoverRegion(ITextViewer aTextViewer, int anOffset)
    {
        return new Region(anOffset, 0);
    }
}
