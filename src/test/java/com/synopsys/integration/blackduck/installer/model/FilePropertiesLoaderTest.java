package com.synopsys.integration.blackduck.installer.model;

import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class FilePropertiesLoaderTest {
    @Test
    public void testLoadingProperties() throws IOException {
        Gson gson = new Gson();
        FilePropertiesLoader filePropertiesLoader = new FilePropertiesLoader(gson);

        File propertiesFile = new File(getClass().getClassLoader().getResource("blackDuckConfigEnvProperties.json").getFile());
        FileLoadedProperties fileLoadedProperties = filePropertiesLoader.loadPropertiesFromFile(propertiesFile);

        Assertions.assertNotNull(fileLoadedProperties);
        Assertions.assertNotNull(fileLoadedProperties.getToAdd());
        Assertions.assertNotNull(fileLoadedProperties.getToEdit());
        Assertions.assertFalse(fileLoadedProperties.getToAdd().isEmpty());
        Assertions.assertFalse(fileLoadedProperties.getToEdit().isEmpty());

        ConfigProperties toAddConfigProperties = new ConfigProperties();
        fileLoadedProperties.getToAdd().stream().forEach(toAddConfigProperties::add);

        Assertions.assertTrue(toAddConfigProperties.keySet().contains("HUB_KB_HOST"));
        Assertions.assertEquals(1, toAddConfigProperties.keySet().size());
        Assertions.assertEquals("custom_kb_host", toAddConfigProperties.getValue("HUB_KB_HOST"));

        ConfigProperties toEditConfigProperties = new ConfigProperties();
        fileLoadedProperties.getToEdit().stream().forEach(toEditConfigProperties::add);

        Assertions.assertTrue(toEditConfigProperties.keySet().contains("HUB_PROXY_HOST"));
        Assertions.assertTrue(toEditConfigProperties.keySet().contains("HUB_PROXY_PORT"));
        Assertions.assertTrue(toEditConfigProperties.keySet().contains("HUB_PROXY_SCHEME"));
        Assertions.assertTrue(toEditConfigProperties.keySet().contains("HUB_PROXY_USER"));
        Assertions.assertEquals(4, toEditConfigProperties.keySet().size());
        Assertions.assertEquals("proxy_host", toEditConfigProperties.getValue("HUB_PROXY_HOST"));
        Assertions.assertEquals("123", toEditConfigProperties.getValue("HUB_PROXY_PORT"));
        Assertions.assertEquals("http", toEditConfigProperties.getValue("HUB_PROXY_SCHEME"));
        Assertions.assertEquals("username", toEditConfigProperties.getValue("HUB_PROXY_USER"));
    }

}
