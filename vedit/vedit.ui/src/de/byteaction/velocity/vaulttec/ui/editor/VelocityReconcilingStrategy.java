package de.byteaction.velocity.vaulttec.ui.editor;

import de.byteaction.velocity.editor.VelocityEditor;
import de.byteaction.velocity.editor.VelocityFoldingStructureProvider;
import de.byteaction.velocity.vaulttec.ui.VelocityPlugin;
import de.byteaction.velocity.vaulttec.ui.editor.parser.NodeVisitor;
import de.byteaction.velocity.vaulttec.ui.editor.parser.VelocityParser;
import de.byteaction.velocity.vaulttec.ui.model.ITreeNode;
import de.byteaction.velocity.vaulttec.ui.model.ITreeVisitor;
import de.byteaction.velocity.vaulttec.ui.model.Template;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IFileEditorInput;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Reconciler strategy which parses the whole editor's content (a Velocity
 * template) on a document change.
 */
public class VelocityReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension
{

    private VelocityEditor                   fEditor;
    private Template                         fTemplate;
    private Template                         fLastTemplate;
    private String                           fError;
    private IDocument                        fDocument;
    private IProgressMonitor                 fProgressMonitor;
    private VelocityFoldingStructureProvider fFoldingStructureProvider;

    public VelocityReconcilingStrategy(VelocityEditor anEditor)
    {
        fEditor = anEditor;
        fFoldingStructureProvider = new VelocityFoldingStructureProvider(fEditor);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param aDocument
     *            DOCUMENT ME!
     */
    public void setDocument(IDocument aDocument)
    {
        fDocument = aDocument;
        fFoldingStructureProvider.setDocument(fDocument);
        parse();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param aDirtyRegion
     *            DOCUMENT ME!
     * @param aRegion
     *            DOCUMENT ME!
     */
    public void reconcile(DirtyRegion aDirtyRegion, IRegion aRegion)
    {
        parse();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param aPartition
     *            DOCUMENT ME!
     */
    public void reconcile(IRegion aPartition)
    {
        parse();
    }

    private List htmlTags = null;

    private void parse()
    {
        htmlTags = new ArrayList();
        IFile file = ((IFileEditorInput) fEditor.getEditorInput()).getFile();
        String name = file.getName();
        Reader reader = new StringReader(fEditor.getDocument().get());
        Template template = null;
        try
        {
            file.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
            VelocityParser velocityParser = VelocityEditorEnvironment.getParser();
            fError = "";
        }
        catch (Exception e)
        {
            fError = "";
            VelocityPlugin.log(e);
        }
        finally
        {
            try
            {
                reader.close();
            }
            catch (IOException e)
            {
                VelocityPlugin.log(e);
            }
        }
        // Replace saved template with the new parsed one
        synchronized (this)
        {
            if (template != null)
            {
                fTemplate = template;
                // Save last successful parse tree
                fLastTemplate = template;
            } else
            {
                fTemplate = null;
            }
        }
        // Update outline view and display error message in status line
        Display.getDefault().syncExec(new Runnable() {

            public void run()
            {
                fEditor.updateOutlinePage();
                fEditor.displayErrorMessage(fError);
            }
        });
        reconcile();
    }


    /**
     * Returns root elements of current parse tree.
     */
    public Object[] getRootElements()
    {
        return ((fTemplate != null) ? fTemplate.getChildren() : ITreeNode.NO_CHILDREN);
    }

    /**
     * Returns root node of current parse tree.
     */
    public ITreeNode getRootNode()
    {
        return fTemplate;
    }

    /**
     * Returns last successful parse tree.
     */
    public ITreeNode getLastRootNode()
    {
        return fLastTemplate;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#initialReconcile()
     */
    public void initialReconcile()
    {
        reconcile();
    }

    private void reconcile()
    {
        // fFoldingStructureProvider.updateFoldingRegions(fTemplate);
        if (fTemplate != null) htmlTags.add(fTemplate);
        fFoldingStructureProvider.updateFoldingRegions(htmlTags.toArray());
    }

    /**
     * 
     */
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#setProgressMonitor(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void setProgressMonitor(IProgressMonitor monitor)
    {
        // TODO Auto-generated method stub
    }
}
