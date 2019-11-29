package com.synopsys.integration.blackduck.installer.keystore;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class OpenSslOutputParserTest {
    @Test
    public void testGettingCertificateContents() throws IOException {
        String fullOutput = IOUtils.toString(getClass().getResourceAsStream("/fullOpenSslOutput.txt"), StandardCharsets.UTF_8);
        String expectedOutput = IOUtils.toString(getClass().getResourceAsStream("/desiredOpenSslOutput.txt"), StandardCharsets.UTF_8);

        OpenSslOutputParser openSslOutputParser = new OpenSslOutputParser();
        Assertions.assertEquals(expectedOutput, openSslOutputParser.parseCertificateOutput(fullOutput));
    }

}
