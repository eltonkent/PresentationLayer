/*
 * #%L
 * broadleaf-common-thymeleaf
 * %%
 * Copyright (C) 2009 - 2016 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package org.broadleafcommerce.common.web.domain;

import org.springframework.web.servlet.support.BindStatus;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.AttributeValueQuotes;
import org.thymeleaf.model.ICloseElementTag;
import org.thymeleaf.model.IOpenElementTag;
import org.thymeleaf.model.IStandaloneElementTag;
import org.thymeleaf.model.IText;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.spring4.util.FieldUtils;
import org.thymeleaf.standard.expression.Assignation;
import org.thymeleaf.standard.expression.AssignationUtils;
import org.thymeleaf.standard.expression.StandardExpressions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Concrete implementation of utilities that can be done during execution of a processor.
 * The underlying encapsulated object is an {@code ITemplateContext}
 * 
 * @author Jay Aisenbrey (cja769)
 *
 */
public class BroadleafThymeleafContextImpl implements BroadleafThymeleafContext {

    protected ITemplateContext context;
    protected IElementModelStructureHandler modelHandler;
    protected IElementTagStructureHandler tagHandler;

    public BroadleafThymeleafContextImpl(ITemplateContext context, IElementModelStructureHandler modelHandler) {
        this.context = context;
        this.modelHandler = modelHandler;
        this.tagHandler = null;
    }
    
    public BroadleafThymeleafContextImpl(ITemplateContext context, IElementTagStructureHandler tagHandler) {
        this.context = context;
        this.tagHandler = tagHandler;
        this.modelHandler = null;
    }

    @Override
    public Object parseExpression(String value) {
        return StandardExpressions.getExpressionParser(context.getConfiguration())
            .parseExpression(context, value)
            .execute(context);
    }

    @Override
    public List<BroadleafAssignation> getAssignationSequence(String value, boolean allowParametersWithoutValue) {
        List<BroadleafAssignation> assignations = new ArrayList<>();
        for (Assignation assignation : AssignationUtils.parseAssignationSequence(context, value, allowParametersWithoutValue)) {
            assignations.add(new BroadleafAssignationImpl(assignation));
        }
        return assignations;
    }

    public ITemplateContext getThymeleafContext() {
        return this.context;
    }

    @Override
    public BroadleafThymeleafNonVoidElement createNonVoidElement(String tagName, Map<String, String> attributes, boolean useDoubleQuotes) {
        IOpenElementTag open = context.getModelFactory().createOpenElementTag(tagName, attributes, useDoubleQuotes ? AttributeValueQuotes.DOUBLE : AttributeValueQuotes.SINGLE, false);
        ICloseElementTag close = context.getModelFactory().createCloseElementTag(tagName, false, false);
        return new BroadleafThymeleafNonVoidElementImpl(open, close);
    }

    @Override
    public BroadleafThymeleafNonVoidElement createNonVoidElement(String tagName) {
        IOpenElementTag open = context.getModelFactory().createOpenElementTag(tagName);
        ICloseElementTag close = context.getModelFactory().createCloseElementTag(tagName, false, false);
        return new BroadleafThymeleafNonVoidElementImpl(open, close);
    }

    @Override
    public BroadleafThymeleafElement createStandaloneElement(String tagName, Map<String, String> attributes, boolean useDoubleQuotes) {
        IStandaloneElementTag standaloneTag = context.getModelFactory().createStandaloneElementTag(tagName, attributes, useDoubleQuotes ? AttributeValueQuotes.DOUBLE : AttributeValueQuotes.SINGLE, false, true);
        return new BroadleafThymeleafStandaloneElementImpl(standaloneTag);
    }

    @Override
    public BroadleafThymeleafElement createStandaloneElement(String tagName) {
        IStandaloneElementTag standaloneTag = context.getModelFactory().createStandaloneElementTag(tagName);
        return new BroadleafThymeleafStandaloneElementImpl(standaloneTag);
    }

    @Override
    public BroadleafThymeleafElement createTextElement(String text) {
        IText textNode = context.getModelFactory().createText(text);
        return new BroadleafThymeleafTextElementImpl(textNode);
    }

    @Override
    public BroadleafThymeleafModel createModel() {
        return new BroadleafThymeleafModelImpl(context.getModelFactory().createModel());
    }

    @Override
    public void setNodeLocalVariable(BroadleafThymeleafElement element, String key, Object value) {
        if (modelHandler != null) {
            modelHandler.setLocalVariable(key, value);
        } else if (tagHandler != null) {
            tagHandler.setLocalVariable(key, value);
        }
    }

    @Override
    public void setNodeLocalVariables(BroadleafThymeleafElement element, Map<String, Object> variableMap) {
        if (modelHandler != null) {
            for (String key : variableMap.keySet()) {
                modelHandler.setLocalVariable(key, variableMap.get(key));
            }
        } else if (tagHandler != null) {
            for (String key : variableMap.keySet()) {
                tagHandler.setLocalVariable(key, variableMap.get(key));
            }
        }
    }

    @Override
    public Object getVariable(String name) {
        return context.getVariable(name);
    }

    @Override
    public BindStatus getBindStatus(String attributeValue) {
        return FieldUtils.getBindStatus(context, attributeValue);
    }
    
}
