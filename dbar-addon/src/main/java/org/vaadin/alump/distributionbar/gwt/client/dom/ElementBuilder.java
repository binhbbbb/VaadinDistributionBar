/**
 * ElementBuilder.java (DistributionBar)
 * 
 * Copyright 2012 Vaadin Ltd, Sami Viitanen <alump@vaadin.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.vaadin.alump.distributionbar.gwt.client.dom;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.vaadin.alump.distributionbar.gwt.client.GwtDistributionBar;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import javax.tools.Tool;

/**
 * This is help class for GwtDistributionBar and so is designed to be used only
 * by it. It's split to own class to allow later totally browser specific
 * versions. For now there is single if that is done browser specific so there
 * is no need for own class.
 */
public class ElementBuilder {

    protected static final String PART_CLASSNAME = GwtDistributionBar.CLASSNAME + "-part";
    protected static final String PART_CLASSNAME_PREFIX = PART_CLASSNAME + "-";
    protected static final String PART_VALUE_CLASSNAME = GwtDistributionBar.CLASSNAME
            + "-value";
    protected static final String UNINITIALIZED_VALUE_CLASSNAME = GwtDistributionBar.CLASSNAME
            + "-uninitizalized";

    //private final static Logger LOGGER = Logger.getLogger(ElementBuilder.class.getName());

    /**
     * Parent is needed to get access to DOM tree
     */
    protected GwtDistributionBar parent;

    /**
     * Tool tip presenter. Constructed in setParent.
     */
    protected ToolTipPresenter tooltip;

    protected ToolTipPresenter.TooltipClassNameProvider tooltipClassNameProvider;

    /**
     * Empty constructor for GWT.create. setParent must be called always after
     * this to have instance correctly initialized.
     */
    public ElementBuilder() {

    }

    public void setTooltipClassNameProvider(ToolTipPresenter.TooltipClassNameProvider provider) {
        tooltipClassNameProvider = provider;
    }

    private ToolTipPresenter getToolTip() {
        if (tooltip == null) {
            tooltip = new ToolTipPresenter();
            if(tooltipClassNameProvider != null) {
                tooltip.setTooltipClassNameProvider(tooltipClassNameProvider);
            }
            parent.addDomHandler(tooltip, MouseOutEvent.getType());
            parent.addDomHandler(tooltip, MouseOverEvent.getType());
            parent.addDomHandler(tooltip, MouseMoveEvent.getType());
        }

        return tooltip;
    }

    /**
     * This function must be called after constructor. It's left out of
     * constructor to allow replacement creation easily with GWT.create()
     * 
     * @param parent
     *            Parent using builder. Used to access DOM tree.
     */
    public void setParent(GwtDistributionBar parent) {
        this.parent = parent;
    }

    /**
     * Return full width of root element. Used to calculate width of parts.
     * 
     * @return Full width of the root element in pixels.
     */
    public int getFullWidth() {
        return parent.getElement().getOffsetWidth();
    }

    /**
     * Get element where parts are added
     * 
     * @return
     */
    public Element getParentElementForParts() {
        return parent.getElement();
    }

    /**
     * Other part elements must be siblings to this element. If not you have to
     * override functions above this.
     * 
     * @return Element for first part which has other parts as siblings
     */
    public Element getFirstPartElement() {
        return getParentElementForParts().getFirstChildElement();
    }

    /**
     * Initialize root element of distribution bar
     * 
     * @return Root element initialized
     */
    public Element initRootElement() {

        Element element = parent.getElement();

        if (element != null) {

            getToolTip().clearAllToolTips();

            Element child = null;
            while ((child = element.getFirstChildElement()) != null) {
                element.removeChild(child);
            }

        } else {
            element = Document.get().createDivElement();
            element.setClassName(GwtDistributionBar.CLASSNAME);
            parent.setRootElement(element);
        }

        return element;
    }

    /**
     * Generate class names for element
     * 
     * @param element
     *            Element that will get new class names
     * @param index
     *            Index of part (useful for left/middle/right)
     * @param parts
     *            Number of parts totally (useful for left/middle/right)
     * @param extraStyleName
     *            Extra style name(s) defined by used
     */
    public void setPartClassNames(Element element, int index, int styleIndex, int parts, String extraStyleName) {
        String styleName = GwtDistributionBar.CLASSNAME;

        // Allows styling like rounded corners
        if(parts == 1) {
            styleName += "-only";
        } else if (index == 0) {
            styleName += "-left";
        } else if (index == (parts - 1)) {
            styleName += "-right";
        } else {
            styleName += "-middle";
        }

        element.setClassName(styleName);
        element.addClassName(PART_CLASSNAME);
        element.addClassName(PART_CLASSNAME_PREFIX + String.valueOf(styleIndex + 1));
        if(extraStyleName != null) {
            element.addClassName(extraStyleName);
        }
    }

    /**
     * Adds new DOM element under widget
     * 
     * @param index
     *            Index for added part
     * @param parts
     *            Number of parts
     * @param size
     *            Size of part added
     * @param title
     *            Title of part added
     */
    public void addPartElement(int index, int parts, double size, String title) {

        final Element element = createPartElement();
        setPartClassNames(element, index, index, parts, null);
        element.setAttribute("title", title);

        Element textElem = Document.get().createSpanElement();
        textElem.setClassName(PART_VALUE_CLASSNAME);
        textElem.setInnerText(String.valueOf(size));

        element.appendChild(textElem);

        getParentElementForParts().appendChild(element);
    }

    /**
     * Add warning text shown if distribution bar is used uninitialized
     */
    public void addUninitializedWarning() {

        Element element = createPartElement();

        Element content = Document.get().createSpanElement();
        content.setClassName(UNINITIALIZED_VALUE_CLASSNAME);
        content.setInnerText("uninitialized");
        element.appendChild(content);

        getParentElementForParts().appendChild(element);
    }

    /**
     * Get root element of part with index
     * 
     * @param index
     *            Index of part [0..N]. Use only valid indexes.
     * @return Element of part.
     */
    public Element getPartElement(int index) {
        Element element = getFirstPartElement();

        for (int i = 0; i < index && element != null; ++i) {
            element = element.getNextSiblingElement();
        }

        return element;
    }

    /**
     * Change part title to given string
     * 
     * @param index
     *            Index of part modified
     * @param title
     *            New title (can be empty)
     */
    public void changePartTitle(int index, String title) {
        Element element = getPartElement(index);

        if (element != null) {
            if (title.isEmpty()) {
                if (element.hasAttribute("title")) {
                    element.removeAttribute("title");
                }
            } else {
                String safe = SafeHtmlUtils.htmlEscape(title);
                element.setAttribute("title", safe);
            }
        }
    }

    public void changePartTooltip(int index, String content) {
        Element element = getPartElement(index);

        if (element != null) {
            if (content.isEmpty()) {
                getToolTip().removeToolTip(element);
            } else {
                getToolTip().setToolTip(element, content);
            }
        }
    }

    public void changePartStyleName(int index, int styleIndex, int parts, String styleName) {
        Element element = getPartElement(index);

        if (element != null) {
            setPartClassNames(element, index, styleIndex, parts, styleName);
        }
    }

    /**
     * Update DOM presentation of distribution. This has to be called always
     * after changes are done to parts.
     */
    public void updateParts(List<Double> sizes, Map<Integer,String> captions, double minElementWidth) {

        double totalSize = parent.totalSize();

        Element element = getFirstPartElement();
        int totalWidth = getFullWidth();

        if(totalWidth < minElementWidth * (double)sizes.size()) {
            minElementWidth = (double)totalWidth / (double)sizes.size();
        }

        for (int i = 0; (i < sizes.size()) && (element != null); ++i) {

            double size = sizes.get(i);

            String caption = captions.get(i);
            if(caption == null) {
                caption = String.valueOf(size);
            }

            setPartElementSize(element, size, totalSize, sizes.size(),
                    totalWidth, minElementWidth, caption);

            Element nextElement = element.getNextSiblingElement();
            element = nextElement;
        }
    }

    /**
     * Set size (width) for part element
     * 
     * @param part
     *            Part element
     * @param size
     *            New size
     * @param total
     *            Sum of all sizes
     * @param part
     *            How many parts there are
     * @param parentSize
     *            Size of parent in pixels
     * @param minElementSize
     *            Minimum width of part with value
     * @param caption
     *            Caption shown in element
     */
    public void setPartElementSize(Element part, double size, double total,
            double parts, double parentSize, double minElementSize, String caption) {

        double elementSize = 0.0;
        Style.Unit elementSizeUnit = Unit.PX;

        if (parentSize <= 0.0 || total == 0.0) {
            elementSize = Math.floor(100.0 / parts);
            elementSizeUnit = Unit.PCT;
        } else {
            final double minTotalSize = minElementSize * parts;
            final double availableSize = parentSize - minTotalSize;

            elementSize = minElementSize + size / total * availableSize;
        }

        setPartElementWidth(part, elementSize, elementSizeUnit);
        setPartElementValueText(part, caption);
    }

    /**
     * Change the width of part element
     * 
     * @param element
     *            Part element changed
     * @param width
     *            New width value
     * @param unit
     *            New width unit
     */
    public void setPartElementWidth(Element element, double width, Style.Unit unit) {

        element.getStyle().setWidth(width, unit);
    }

    /**
     * Simple function that return element used to represent part.
     * 
     * @return Element used to represent the element
     */
    public Element createPartElement() {
        // if (BrowserInfo.get().isIE6() || BrowserInfo.get().isIE7()) {
        // return Document.get().createSpanElement();
        // } else {
        return Document.get().createDivElement();
        // }
    }

    /**
     * Change the text presentation of part size in part element
     * 
     * @param element
     *            Part element given
     * @param text
     *            Text shown
     */
    public void setPartElementValueText(Element element, String text) {
        Element textElem = element.getFirstChildElement();
        textElem.setInnerText(text);
    }

}
