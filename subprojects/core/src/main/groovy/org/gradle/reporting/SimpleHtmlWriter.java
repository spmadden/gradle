/*
 * Copyright 2013 the original author or authors.
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

package org.gradle.reporting;

import org.gradle.api.internal.xml.SimpleMarkupWriter;

import java.io.IOException;
import java.io.Writer;

public class SimpleHtmlWriter extends SimpleMarkupWriter {

    public SimpleHtmlWriter(Writer writer) throws IOException {
        this(writer, null);
    }

    public SimpleHtmlWriter(Writer writer, String indent) throws IOException {
        super(writer, indent);
        writeHtmlHeader();
    }

    private void writeHtmlHeader() throws IOException {
        writeRaw("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
    }
}
