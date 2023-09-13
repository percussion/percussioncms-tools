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

package de.byteaction.velocity.vaulttec.ui.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import de.byteaction.velocity.vaulttec.ui.editor.VelocityEditorEnvironment;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.1 $
 * @author <a href="mailto:as@byteaction.de">Akmal Sarhan </a>
 */
public class Directive extends AbstractTreeNode implements IBlock
{

    public static final int      TYPE_SET            = 0;
    public static final int      TYPE_IF             = 1;
    public static final int      TYPE_ELSE           = 2;
    public static final int      TYPE_ELSEIF         = 3;
    public static final int      TYPE_END            = 4;
    public static final int      TYPE_FOREACH        = 5;
    public static final int      TYPE_INCLUDE        = 6;
    public static final int      TYPE_PARSE          = 7;
    public static final int      TYPE_MACRO          = 8;
    public static final int      TYPE_STOP           = 9;
    public static final int      TYPE_MACRO_CALL     = 10;
    public static final int      TYPE_USER_DIRECTIVE = 11;
    public static final String[] DIRECTIVES          = { "#set", "#if", "#else", "#elseif", "#end", "#foreach", "#include", "#parse", "#macro", "#stop" };
    public static final String[] INDENT_DIRECTIVES   = { "if", "else", "elseif", "foreach", "macro" };
    private int                  fType;
    private String               fName;
    /** List of parameters of Velocity macro */
    private ArrayList            fParameters;
    private String               fId;
    protected Vector             fDirectives         = new Vector();

    public Directive(int aType, String aName, String anId, ITreeNode aParent, int aStartLine, int anEndLine)
    {
        super(aParent, aStartLine, anEndLine);
        fName = aName;
        fType = aType;
        fId = anId;
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
     * @param aName
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public static int getType(String aName)
    {
        for (int i = 0; i < DIRECTIVES.length; i++)
        {
            if (Directive.DIRECTIVES[i].equals(aName)) { return i; }
        }
        if (VelocityEditorEnvironment.getParser().isUserDirective(aName)) { return TYPE_USER_DIRECTIVE; }
        return TYPE_MACRO_CALL;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getId()
    {
        return fId;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param aParameter
     *            DOCUMENT ME!
     */
    public void addParameter(String aParameter)
    {
        if (fParameters == null)
        {
            fParameters = new ArrayList();
        }
        fParameters.add(aParameter);
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public ArrayList getParameters()
    {
        return fParameters;
    }

    /**
     * @see de.byteaction.velocity.vaulttec.ui.model.IBlock#addDirective(de.byteaction.velocity.vaulttec.ui.model.Directive)
     */
    public void addDirective(Directive aDirective)
    {
        fDirectives.add(aDirective);
    }

    /**
     * @see de.byteaction.velocity.vaulttec.ui.model.ITreeNode#getName()
     */
    public String getName()
    {
        return ((fType < TYPE_MACRO_CALL) ? (DIRECTIVES[fType] + ((fType != TYPE_ELSE) ? (" (" + fName + ")") : "")) : ("#" + fName));
    }

    /**
     * @see de.byteaction.velocity.vaulttec.ui.model.ITreeNode#hasChildren()
     */
    public boolean hasChildren()
    {
        return !fDirectives.isEmpty();
    }

    /**
     * @see de.byteaction.velocity.vaulttec.ui.model.ITreeNode#getChildren()
     */
    public Object[] getChildren()
    {
        return fDirectives.toArray();
    }

    /**
     * @see de.byteaction.velocity.vaulttec.ui.model.ITreeNode#accept(de.byteaction.velocity.vaulttec.ui.model.ITreeVisitor)
     */
    public boolean accept(ITreeVisitor aVisitor)
    {
        boolean more = true;
        // Visit all embedded directives of this directive
        Iterator iter = fDirectives.iterator();
        while (more && iter.hasNext())
        {
            more = ((ITreeNode) iter.next()).accept(aVisitor);
        }
        // Finally visit this directive
        if (more)
        {
            more = aVisitor.visit(this);
        }
        return more;
    }

    /**
     * @see de.byteaction.velocity.vaulttec.ui.model.ITreeNodeInfo#getUniqueID()
     */
    public String getUniqueID()
    {
        return getName() + ":" + getStartLine();
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String toString()
    {
        return getUniqueID() + ":" + getEndLine() + " with directive(s) " + fDirectives;
    }
}
