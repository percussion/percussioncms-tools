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

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.1 $
 * @author <a href="mailto:as@byteaction.de">Akmal Sarhan </a>
 */
public interface ITreeNode
{

    public static final Object[] NO_CHILDREN = new Object[0];

    String getName();

    Object getParent();

    boolean hasChildren();

    Object[] getChildren();

    String getUniqueID();

    int getStartLine();

    int getEndLine();

    /**
     * Visitor design pattern.
     * @see ITreeVisitor#visit(ITreeNodeInfo)
     */
    boolean accept(ITreeVisitor aVisitor);
}
