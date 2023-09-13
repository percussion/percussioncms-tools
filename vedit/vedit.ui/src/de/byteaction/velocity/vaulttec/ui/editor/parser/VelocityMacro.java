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

package de.byteaction.velocity.vaulttec.ui.editor.parser;

/**
 * Container used to store Velocity macro information.
 */
public class VelocityMacro
{

    protected String   fName;
    protected String[] fArguments;
    protected String   fTemplate;

    public VelocityMacro(String aName, String[] anArguments, String aTemplate)
    {
        fName = aName;
        fArguments = anArguments;
        fTemplate = aTemplate;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getName()
    {
        return fName;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String[] getArguments()
    {
        return fArguments;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getTemplate()
    {
        return fTemplate;
    }
}
