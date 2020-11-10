package com.synopsys.integration.blackduck.installer.model;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

public class ConfigPropertiesLoaderTest {
    @Test
    public void testLoadingProperties() throws IOException {
        Gson gson = new Gson();
        ConfigPropertiesLoader configPropertiesLoader = new ConfigPropertiesLoader(gson);

        File propertiesFile = new File(getClass().getClassLoader().getResource("blackduck/blackDuckConfigEnvProperties.json").getFile());
        LoadedConfigProperties loadedConfigProperties = configPropertiesLoader.loadPropertiesFromFile(propertiesFile);

        Assertions.assertNotNull(loadedConfigProperties);
        Assertions.assertNotNull(loadedConfigProperties.getToAdd());
        Assertions.assertNotNull(loadedConfigProperties.getToEdit());
        Assertions.assertFalse(loadedConfigProperties.getToAdd().isEmpty());
        Assertions.assertFalse(loadedConfigProperties.getToEdit().isEmpty());

        ConfigProperties toAddConfigProperties = new ConfigProperties();
        loadedConfigProperties.getToAdd().stream().forEach(toAddConfigProperties::add);

        Assertions.assertTrue(toAddConfigProperties.keySet().contains("HUB_KB_HOST"));
        Assertions.assertEquals(1, toAddConfigProperties.keySet().size());
        Assertions.assertEquals("custom_kb_host", toAddConfigProperties.getValue("HUB_KB_HOST"));

        ConfigProperties toEditConfigProperties = new ConfigProperties();
        loadedConfigProperties.getToEdit().stream().forEach(toEditConfigProperties::add);

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
