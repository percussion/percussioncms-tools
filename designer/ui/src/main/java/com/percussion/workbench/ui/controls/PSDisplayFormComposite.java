/******************************************************************************
*
* [ PSDisplayFormComposite.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.controls;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * A composite with a Ui Form in it so it can render
 * some lightly styled XML/HTML text. Used for display only.
 * Another words you can only set the text and not get it back.
 */
public class PSDisplayFormComposite extends Composite
{

   
   /**
    * Create the composite
    * @param parent
    */
   public PSDisplayFormComposite(Composite parent)
   {
      super(parent, SWT.NONE);
      setLayout(new FillLayout());
      FormToolkit toolkit = new FormToolkit(Display.getCurrent());
      toolkit.adapt(this);
      toolkit.paintBordersFor(this);

      m_form = toolkit.createForm(this);
      final Composite body = m_form.getBody();
      body.setLayout(new FillLayout());
      toolkit.paintBordersFor(body);

      m_formText = toolkit.createFormText(body, false);
   }
   
   /**
    * Sets the text for this form. Style tags and anchor tags
    * will be parsed and processed.
    * The following tags are allowed:
    * </p>
    * 
    *
    * <ul>
    * <li><b>p </b>- for defining paragraphs. The following attributes are
    * allowed:
    * <ul>
    * <li><b>vspace </b>- if set to 'false', no vertical space will be added
    * (default is 'true')</li>
    * </ul>
    * </li>
    * <li><b>li </b>- for defining list items. The following attributes are
    * allowed:
    * <ul>
    *
    * <li><b>vspace </b>- the same as with the <b>p </b> tag</li>
    * <li><b>style </b>- could be 'bullet' (default), 'text' and 'image'</li>
    * <li><b>value </b>- not used for 'bullet'. For text, it is the value of the
    * text to rendered as a bullet. For image, it is the href of the image to be
    * rendered as a bullet.</li>
    * <li><b>indent </b>- the number of pixels to indent the text in the list
    * item</li>
    *
    * <li><b>bindent </b>- the number of pixels to indent the bullet itself
    * </li>
    * </ul>
    * </li>
    * </ul>
    * <p>
    * Text in paragraphs and list items will be wrapped according to the width of
    * the control. The following tags can appear as children of either <b>p </b>
    * or <b>li </b> elements:
    * </p><ul>
    *
    * <li><b>img </b>- to render an image. Element accepts attribute 'href' that
    * is a key to the Image set using 'setImage' method.</li>
    * <li><b>a </b>- to render a hyperlink. Element accepts attribute 'href'
    * that will be provided to the hyperlink listeners via HyperlinkEvent object.
    * The element also accepts 'nowrap' attribute (default is false). When set to
    * 'true', the hyperlink will not be wrapped.</li>
    * <li><b>b </b>- the enclosed text will use bold font.</li>
    * <li><b>br </b>- forced line break (no attributes).</li>
    * <li><b>span </b>- the enclosed text will have the color and font specified
    * in the element attributes. Color is provided using 'color' attribute and is
    * a key to the Color object set by 'setColor' method. Font is provided using
    * 'font' attribute and is a key to the Font object set by 'setFont' method.
    * </li>
    *
    * </ul>
    * <p>
    * None of the elements can nest. For example, you cannot have <b>b </b> inside
    * a <b>span </b>. This was done to keep everything simple and transparent.
    * @param text may be <code>null</code> or empty.
    */
   public void setText(String text)
   {      
      if(StringUtils.isBlank(text))
         m_formText.setText("", false, false);
      else
         m_formText.setText(processText(text), true, true);
   }
   
   /**
    * Sets the forms title
    * @param title text may be <code>null</code> or empty.
    */
   public void setTitle(String title)
   {      
      m_form.setText(title);
   }
   
   /**
    * Helper method to process the text into the xml format
    * that the control can handle.
    * @param text
    * @return
    */
   private String processText(String text)
   {
      StringBuilder sb = new StringBuilder();
      sb.append("<form>");
      sb.append(text);
      sb.append("</form>");
      return sb.toString();
   }

   @Override
   public void dispose()
   {
      super.dispose();
   }

   @Override
   protected void checkSubclass()
   {
      // Disable the check that prevents subclassing of SWT components
   }
   
   private FormText m_formText;
   private Form m_form;

}
