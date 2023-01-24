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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;

public class VelocityInput implements ITypedElement, IStreamContentAccessor
{

    String name;
    String contents;

    public VelocityInput(String name, String contents)
    {
        this.name = name;
        this.contents = contents;
    }

    public String getName()
    {
        return name;
    }

    public Image getImage()
    {
        return null;
    }

    public String getType()
    {
        // text file to force text compare for these objects
        return "vm";
    }

    public InputStream getContents() throws CoreException
    {
        return new ByteArrayInputStream(contents.getBytes());
    }
}