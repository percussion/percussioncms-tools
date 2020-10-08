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