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
 * Created on 29.12.2004
 * 
 * TODO To change the template for this generated file go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
package de.byteaction.velocity.editor;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;

public class VelocityMarkerResolutionGenerator implements IMarkerResolutionGenerator2
{

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IMarkerResolutionGenerator#getResolutions(org.eclipse.core.resources.IMarker)
     */
    public IMarkerResolution[] getResolutions(IMarker marker)
    {
        IMarkerResolution resolution = new VelocityResolution();
        return new IMarkerResolution[] { resolution };
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IMarkerResolutionGenerator2#hasResolutions(org.eclipse.core.resources.IMarker)
     */
    public boolean hasResolutions(IMarker marker)
    {
        // TODO Auto-generated method stub
        return true;
    }

    private final class VelocityResolution implements IMarkerResolution
    {

        public String getLabel()
        {
            // TODO Auto-generated method stub
            return "not implemented";
        }

        public void run(IMarker marker)
        {
            // runIt((IAction) null);
        }
    }
}
