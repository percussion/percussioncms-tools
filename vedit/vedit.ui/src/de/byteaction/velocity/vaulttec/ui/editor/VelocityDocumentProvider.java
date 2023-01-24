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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import de.byteaction.velocity.scanner.VelocityPartitionScanner;

/**
 * This class provides the IDocuments used by Velocity editors. These IDocuments
 * have an Velocity-aware partition scanner (multi-line comments) attached.
 */
public class VelocityDocumentProvider extends FileDocumentProvider
{

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#createDocument(Object)
     */
    protected IDocument createDocument(Object anElement) throws CoreException
    {
        IDocument document = super.createDocument(anElement);
        if (document != null)
        {
            IDocumentPartitioner partitioner = new DefaultPartitioner(new VelocityPartitionScanner(), VelocityPartitionScanner.TYPES);
            partitioner.connect(document);
            document.setDocumentPartitioner(partitioner);
        }
        return document;
    }
}
