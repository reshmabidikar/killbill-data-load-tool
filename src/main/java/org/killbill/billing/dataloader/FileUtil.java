package org.killbill.billing.dataloader;

import org.killbill.commons.utils.io.ByteStreams;
import org.killbill.commons.utils.io.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class FileUtil {

    public static String toString(final String resourceName) throws IOException {
        final InputStream inputStream = Resources.getResource(resourceName).openStream();
        try {
            return new String(ByteStreams.toByteArray(inputStream), StandardCharsets.UTF_8);
        } finally {
            inputStream.close();
        }
    }
}
