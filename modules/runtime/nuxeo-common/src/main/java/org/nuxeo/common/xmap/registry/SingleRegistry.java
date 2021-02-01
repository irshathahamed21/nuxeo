/*
 * (C) Copyright 2020 Nuxeo (http://nuxeo.com/) and others.
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
 *
 * Contributors:
 *     Anahide Tchertchian
 */
package org.nuxeo.common.xmap.registry;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.common.xmap.Context;
import org.nuxeo.common.xmap.XAnnotatedMember;
import org.nuxeo.common.xmap.XAnnotatedObject;
import org.w3c.dom.Element;

/**
 * Registry for a single contribution.
 *
 * @since 11.5
 */
public class SingleRegistry extends AbstractRegistry implements Registry {

    private static final Logger log = LogManager.getLogger(SingleRegistry.class);

    // volatile for double-checked locking
    protected volatile Object contribution;

    // volatile for double-checked locking
    protected volatile boolean enabled = true;

    @Override
    public void initialize() {
        setContribution(null);
        enabled = true;
        super.initialize();
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getContribution() {
        checkInitialized();
        return enabled ? Optional.ofNullable((T) contribution) : Optional.empty();
    }

    protected void setContribution(Object contribution) {
        this.contribution = contribution;
    }

    protected boolean shouldRemove(Context ctx, XAnnotatedObject xObject, Element element, String extensionId) {
        XAnnotatedMember remove = xObject.getRemove();
        if (remove != null && Boolean.TRUE.equals(remove.getValue(ctx, element))) {
            return true;
        }
        return false;
    }

    protected boolean shouldMerge(Context ctx, XAnnotatedObject xObject, Element element, String extensionId) {
        XAnnotatedMember merge = xObject.getMerge();
        if (merge != null && Boolean.TRUE.equals(merge.getValue(ctx, element))) {
            if (contribution != null && xObject.getCompatWarnOnMerge() && !merge.hasValue(ctx, element)) {
                log.warn("A contribution on extension '{}' has been implicitly merged: the compatibility "
                        + "mechanism on its descriptor class '{}' detected it, and the attribute merge=\"true\" "
                        + "should be added to this definition.", extensionId, contribution.getClass().getName());
            }
            return true;
        }
        return false;

    }

    protected boolean onlyHandlesEnablement(Context ctx, XAnnotatedObject xObject, Element element) {
        // checks no children
        if (element.getChildNodes().getLength() > 0) {
            return false;
        }
        // checks no attribute other than id and enablement
        int nbAttr = element.getAttributes().getLength();
        if (nbAttr > 2) {
            return false;
        }
        XAnnotatedMember enable = xObject.getEnable();
        return (enable != null && enable.hasValue(ctx, element));
    }

    /**
     * Returns a positive integer if contribution should be enabled, and a negative one if it should be disabled.
     * <p>
     * Returns 0 if the enablement status should not change.
     */
    protected int shouldEnable(Context ctx, XAnnotatedObject xObject, Element element, String extensionId) {
        XAnnotatedMember enable = xObject.getEnable();
        if (enable != null && enable.hasValue(ctx, element)) {
            Object enabled = enable.getValue(ctx, element);
            if (enabled != null && Boolean.FALSE.equals(enabled)) {
                return -1;
            }
            return 1;
        }
        return 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T doRegister(Context ctx, XAnnotatedObject xObject, Element element, String extensionId) {
        if (shouldRemove(ctx, xObject, element, extensionId)) {
            setContribution(null);
            return null;
        }

        Object contrib;
        if (shouldMerge(ctx, xObject, element, extensionId)) {
            contrib = xObject.newInstance(ctx, element, contribution);
        } else {
            contrib = xObject.newInstance(ctx, element);
        }
        setContribution(contrib);

        int enable = shouldEnable(ctx, xObject, element, extensionId);
        if (enable != 0) {
            this.enabled = enable > 0;
        }

        return (T) contrib;
    }

}
