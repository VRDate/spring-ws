/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.server.endpoint;

import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.ws.context.MessageContext;

/**
 * Extension of the {@link AbstractMarshallingPayloadEndpoint} which validates the request payload with {@link
 * Validator}(s). The desired validators can be set using properties, and <strong>must</strong> {@link
 * Validator#supports(Class) support} the request object.
 *
 * @author Arjen Poutsma
 * @since 1.0.2
 */
public abstract class AbstractValidatingMarshallingPayloadEndpoint extends AbstractMarshallingPayloadEndpoint {

    /** Default request object name used for validating request objects. */
    public static final String DEFAULT_REQUEST_NAME = "request";

    private String requestName = DEFAULT_REQUEST_NAME;

    private Validator[] validators;

    /** Return the name of the request object for validation error codes. */
    public final String getRequestName() {
        return requestName;
    }

    /** Set the name of the request object user for validation errors. */
    public final void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    /** Return the primary Validator for this controller. */
    public final Validator getValidator() {
        return (this.validators != null && this.validators.length > 0 ? this.validators[0] : null);
    }

    /**
     * Set the primary {@link Validator} for this endpoint. The {@link Validator} is must support the unmarshalled
     * class. If there are one or more existing validators set already when this method is called, only the specified
     * validator will be kept. Use {@link #setValidators(Validator[])} to set multiple validators.
     */
    public final void setValidator(Validator validator) {
        this.validators = new Validator[]{validator};
    }

    /** Return the Validators for this controller. */
    public final Validator[] getValidators() {
        return validators;
    }

    /** Set the Validators for this controller. The Validator must support the specified command class. */
    public final void setValidators(Validator[] validators) {
        this.validators = validators;
    }

    protected final boolean onUnmarshalRequest(MessageContext messageContext, Object requestObject) throws Exception {
        if (validators != null) {
            Errors errors = new BeanPropertyBindingResult(requestObject, getRequestName());
            for (int i = 0; i < validators.length; i++) {
                ValidationUtils.invokeValidator(validators[i], requestObject, errors);
            }
            if (errors.hasErrors()) {
                return onValidationErrors(messageContext, requestObject, errors);
            }
        }
        return true;
    }

    /**
     * Callback for post-processing validation errors. Called when validator(s) have been specified, and validation
     * fails.
     *
     * @param messageContext the message context
     * @param requestObject  the object unmarshalled from the {@link MessageContext#getRequest() request}
     * @param errors         validation errors holder
     * @return <code>true</code> to continue and call {@link #invokeInternal(Object)}; <code>false</code> otherwise
     */
    protected abstract boolean onValidationErrors(MessageContext messageContext, Object requestObject, Errors errors);
}