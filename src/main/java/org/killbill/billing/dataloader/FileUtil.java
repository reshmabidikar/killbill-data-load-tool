/*
 * Copyright 2014-2024 The Billing Project, LLC
 *
 * The Billing Project licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.killbill.billing.dataloader;

import org.killbill.commons.utils.io.ByteStreams;
import org.killbill.commons.utils.io.Resources;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class FileUtil {

    public static String toString(final String resourceName, boolean readFromClassPath) throws IOException {
        final InputStream inputStream;
        if (readFromClassPath) {
            inputStream = Resources.getResource(resourceName).openStream();
        } else {
            inputStream = new FileInputStream(resourceName);
        }
        try {
            return new String(ByteStreams.toByteArray(inputStream), StandardCharsets.UTF_8);
        } finally {
            inputStream.close();
        }
    }
}
