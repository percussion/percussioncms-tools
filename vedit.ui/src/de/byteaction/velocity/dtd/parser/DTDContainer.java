package de.byteaction.velocity.dtd.parser;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Represents an item that may contain other items (such as a DTDChoice or a
 * DTDSequence)
 * 
 * @author Mark Wutka
 * @version $Revision: 1.1 $ $Date: 2005/10/26 06:14:07 $ by $Author: akmal88 $
 */
public abstract class DTDContainer extends DTDItem
{

    protected ArrayList items;

    /** Creates a new DTDContainer */
    public DTDContainer()
    {
        items = new ArrayList();
    }

    /** Adds an element to the container */
    public void add(DTDItem item)
    {
        items.add(item);
    }

    /** Removes an element from the container */
    public void remove(DTDItem item)
    {
        items.remove(item);
    }

    /** Returns the elements as a ArrayList (not a clone!) */
    public ArrayList getItemsVec()
    {
        return items;
    }

    /** Returns the elements as an array of items */
    public DTDItem[] getItems()
    {
        DTDItem[] retval = new DTDItem[items.size()];
        items.toArray(retval);
        return retval;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param ob
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public boolean equals(Object ob)
    {
        if (ob == this) { return true; }
        if (!(ob instanceof DTDContainer)) { return false; }
        if (!super.equals(ob)) { return false; }
        DTDContainer other = (DTDContainer) ob;
        return items.equals(other.items);
    }

    /** Stores items in the container */
    public void setItem(DTDItem[] newItems)
    {
        items = new ArrayList(newItems.length);
        for (int i = 0; i < newItems.length; i++)
        {
            items.add(newItems[i]);
        }
    }

    /** Retrieves the items in the container */
    public DTDItem[] getItem()
    {
        DTDItem[] retval = new DTDItem[items.size()];
        items.toArray(retval);
        return retval;
    }

    /** Stores an item in the container */
    public void setItem(DTDItem anItem, int i)
    {
        items.set(i, anItem);
    }

    /** Retrieves an item from the container */
    public DTDItem getItem(int i)
    {
        return (DTDItem) items.get(i);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param out
     *            DOCUMENT ME!
     * 
     * @throws IOException
     *             DOCUMENT ME!
     */
    public abstract void write(PrintWriter out) throws IOException;
}
