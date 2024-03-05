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
