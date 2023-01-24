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

import java.io.IOException;
import java.io.Writer;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

/**
 * Dummy implementation of a Velocity user directive. It only provides a name
 * and a type but no rendering.
 */
public class VelocityDirective extends Directive
{

    private String fName;
    private int    fType;

    public VelocityDirective(String aName, int aType)
    {
        fName = aName;
        fType = aType;
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
    public int getType()
    {
        return fType;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param aContext
     *            DOCUMENT ME!
     * @param aWriter
     *            DOCUMENT ME!
     * @param aNode
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * 
     * @throws IOException
     *             DOCUMENT ME!
     * @throws ResourceNotFoundException
     *             DOCUMENT ME!
     * @throws ParseErrorException
     *             DOCUMENT ME!
     * @throws MethodInvocationException
     *             DOCUMENT ME!
     */
    public boolean render(InternalContextAdapter aContext, Writer aWriter, Node aNode)
    {
        return true;
    }
}
