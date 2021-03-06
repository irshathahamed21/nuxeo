/*
 * (C) Copyright 2018 Nuxeo (http://nuxeo.com/) and others.
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
 *     Antoine Taillefer <ataillefer@nuxeo.com>
 */
package org.nuxeo.ecm.platform.rendition.publisher;

import java.util.Collections;
import java.util.List;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.rendition.extension.RenditionProvider;
import org.nuxeo.ecm.platform.rendition.service.RenditionDefinition;
import org.nuxeo.ecm.platform.rendition.service.RenditionDefinitionProvider;

/**
 * Simple {@link RenditionProvider} to test rendition publication relying on a {@link RenditionDefinitionProvider}.
 *
 * @since 10.10
 */
public class PublicationRenditionProvider implements RenditionProvider {

    @Override
    public boolean isAvailable(DocumentModel doc, RenditionDefinition def) {
        return true;
    }

    @Override
    public List<Blob> render(DocumentModel doc, RenditionDefinition definition) {
        Blob blob = Blobs.createBlob(doc.getTitle(), "text/plain");
        return Collections.singletonList(blob);
    }

}
