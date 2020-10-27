/******************************************************************************
 *
 * [ PSComboBoxCellEditor.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.controls;


import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * A cell editor that presents a list of items in a combo box.
 * The cell editor's value is the selected item object.
 * <p>
 * The default label provider used in <code>LabelProvider</code> which
 * calls the <code>toString()</code> method of the element passed to it.
 * </p>
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 * This code was taken from Eclipse's <code>ComboBoxCellEditor</code>
 * and modified so that objects can be used for values instead of strings.
 * Unfortunately Eclipse's <code>ComboBoxCellEditor</code>
 * (and most other widgets)
 * are not intended to be subclassed so modifying the original code is
 * the easiest way to implement this.
 */
public class PSComboBoxCellEditor extends CellEditor {


    /**
     * Creates a new cell editor with no control and no  st of choices. Initially,
     * the cell editor has no cell validator.
     *
     * @see #setStyle(int)
     * @see #create(Composite)
     * @see #setItems(Object[])
     * @see #dispose()
     */
    public PSComboBoxCellEditor() {
        setStyle(ms_defaultStyle);
    }

    /**
     * Creates a new cell editor with a combo containing the given
     * list of choices and parented under the given control. The cell
     * editor value is the zero-based index of the selected item.
     * Initially, the cell editor has no cell validator and
     * the first item in the list is selected.
     *
     * @param parent the parent control
     * @param items the list of strings for the combo box
     */
    public PSComboBoxCellEditor(Composite parent, String[] items) {
        this(parent, items, ms_defaultStyle);
    }

    /**
     * Creates a new cell editor with a combo containing the given
     * list of choices and parented under the given control. The cell
     * editor value is the zero-based index of the selected item.
     * Initially, the cell editor has no cell validator and
     * the first item in the list is selected.
     *
     * @param parent the parent control
     * @param items the list of strings for the combo box
     * @param style the style bits
     */
    public PSComboBoxCellEditor(Composite parent, String[] items, int style) {
        super(parent, style);
        setItems(items);
    }

    /**
     * Returns the list of choices for the combo box
     *
     * @return the list of choices for the combo box
     */
    public Object[] getItems() {
        return m_items.toArray();
    }

    /**
     * Sets the list of choices for the combo box, the objects
     * that are used should have their <code>toString()</code>
     * methods implemented as that is what will show in the
     * combo box if another label provider is not specified.
     *
     * @param items the list of choices for the combo box, may
     * not be <code>null</code>.
     */
    @SuppressWarnings("unchecked")
    public void setItems(Object[] items) {
        Assert.isNotNull(items);
        m_items.clear();
        for(Object item : items)
           m_items.add(item);
        populateComboBoxItems();
    }

    /* (non-Javadoc)
     * Method declared on CellEditor.
     */
    @Override
   protected Control createControl(Composite parent) {

        m_comboBox = new CCombo(parent, getStyle());
        m_comboBox.setFont(parent.getFont());

        m_comboBox.addKeyListener(new KeyAdapter() {
            // hook key pressed - see PR 14201
            @Override
            public void keyPressed(KeyEvent e) {
                keyReleaseOccured(e);
            }
        });

        m_comboBox.addSelectionListener(new SelectionAdapter() {
            @Override
            @SuppressWarnings("unused")
            public void widgetDefaultSelected(SelectionEvent event) {
                applyEditorValueAndDeactivate();
            }

            @Override
            @SuppressWarnings("unused")
            public void widgetSelected(SelectionEvent event) {
                m_selection = m_comboBox.getSelectionIndex();
            }
        });

        m_comboBox.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_ESCAPE
                        || e.detail == SWT.TRAVERSE_RETURN) {
                    e.doit = false;
                }
            }
        });

        m_comboBox.addFocusListener(new FocusAdapter() {
            @Override
            @SuppressWarnings("unused")
            public void focusLost( FocusEvent e) {
                PSComboBoxCellEditor.this.focusLost();
            }
        });
        return m_comboBox;
    }

    /**
     * The <code>PSComboBoxCellEditor</code> implementation of
     * this <code>CellEditor</code> framework method returns
     * the value object of the current selection.
     *
     * @return the value object of the current selection
     */
    @Override
   protected Object doGetValue() 
   {
       if(m_selection == -1 && StringUtils.isBlank(m_comboBox.getText()))
          return null;
       return m_selection == -1
             ?  m_comboBox.getText() : m_items.get(m_selection);
   }


    /* (non-Javadoc)
     * Method declared on CellEditor.
     */
    @Override
   protected void doSetFocus() {
        m_comboBox.setFocus();
    }

    /**
     * The <code>PSComboBoxCellEditor</code> implementation of
     * this <code>CellEditor</code> framework method sets the
     * minimum width of the cell.  The minimum width is 10 characters
     * if <code>comboBox</code> is not <code>null</code> or <code>disposed</code>
     * eles it is 60 pixels to make sure the arrow button and some text is visible.
     * The list of CCombo will be wide enough to show its longest item.
     */
    @Override
   public LayoutData getLayoutData() {
        LayoutData layoutData = super.getLayoutData();
        if ((m_comboBox == null) || m_comboBox.isDisposed())
            layoutData.minimumWidth = 60;
        else {
            // make the comboBox 10 characters wide
            GC gc = new GC(m_comboBox);
            layoutData.minimumWidth = (gc.getFontMetrics()
                    .getAverageCharWidth() * 10) + 10;
            gc.dispose();
        }
        return layoutData;
    }

    /**
     * Sets the label provider for this control
     * @param provider the label provider, cannot be <code>null</code>
     */
    public void setLabelProvider(ILabelProvider provider)
    {
       Assert.isNotNull(provider);
       m_labelProvider = provider;
    }

    /**
     * The <code>PSComboBoxCellEditor</code> implementation of
     * this <code>CellEditor</code> framework method
     * accepts an object.
     *
     * @param value an object that should be in the list of items
     * set for this combo box. If item does not exist or
     * value is <code>null</code> then the selection will
     * be unset.
     */
    @Override
   protected void doSetValue(Object value) {
        Assert.isTrue(m_comboBox != null);
        if(value == null || !m_items.contains(value))
           m_selection = -1;
        else
           m_selection = m_items.indexOf(value);
        m_comboBox.select(m_selection);
        if (m_selection == -1 && value instanceof String)
        {
           m_comboBox.setText((String) value);
        }
    }

    /**
     * Updates the list of choices for the combo box for the current control.
     */
    private void populateComboBoxItems() {
        if (m_comboBox != null && !m_comboBox.isDisposed()) {
            m_comboBox.removeAll();
            for (Object item : m_items)
                m_comboBox.add(m_labelProvider.getText(item));

            setValueValid(true);
            m_selection = 0;
        }
    }

    /**
     * Applies the currently selected value and deactiavates the cell editor
     */
    @SuppressWarnings("unchecked")
    void applyEditorValueAndDeactivate() {
        //  must set the selection before getting value
        m_selection = m_comboBox.getSelectionIndex();
        Object newValue = doGetValue();
        markDirty();
        boolean isValid = isCorrect(newValue);
        setValueValid(isValid);
        if (!isValid) {
            // try to insert the current value into the error message.
            setErrorMessage(MessageFormat.format(getErrorMessage(),
                    new Object[] { m_items.get(m_selection) }));
        }
        fireApplyEditorValue();
        deactivate();
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.viewers.CellEditor#focusLost()
     */
    @Override
   protected void focusLost() {
        if (isActivated()) {
            applyEditorValueAndDeactivate();
        }
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.viewers.CellEditor#keyReleaseOccured(org.eclipse.swt.events.KeyEvent)
     */
    @Override
   protected void keyReleaseOccured(KeyEvent keyEvent)
    {
        if (keyEvent.character == '\u001b') { // Escape character
            fireCancelEditor();
        } else if (keyEvent.character == '\t') { // tab key
            applyEditorValueAndDeactivate();
        }
    }

    /*
     * override to avoid validation errors
     * @see org.eclipse.jface.viewers.CellEditor#isCorrect(java.lang.Object)
     */
    @Override
   @SuppressWarnings("unused")
    protected boolean isCorrect(Object value)
    {
       return true;
    }

    /**
     * The list of items to present in the combo box.
     */
    private List m_items = new ArrayList();

    /**
     * The zero-based index of the selected item.
     */
    int m_selection;

    /**
     * The custom combo box control.
     */
    CCombo m_comboBox;

    /**
     * The label provider for this control, defaults to
     * use <code>LabelProvider</code>.
     */
    private ILabelProvider m_labelProvider = new LabelProvider();

    /**
     * Default ComboBoxCellEditor style
     */
    private static final int ms_defaultStyle = SWT.NONE;
}
