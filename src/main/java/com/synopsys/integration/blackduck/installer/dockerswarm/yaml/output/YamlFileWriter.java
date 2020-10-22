package com.synopsys.integration.blackduck.installer.dockerswarm.yaml.output;

import java.io.IOException;
import java.util.Collection;

import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.DockerSecret;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.DockerService;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.GlobalSecrets;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.YamlFile;
import com.synopsys.integration.blackduck.installer.dockerswarm.yaml.model.YamlLine;

public class YamlFileWriter {

    public static final void write(YamlWriter writer, YamlFile yamlFile) throws IOException {
        YamlLine versionLine = yamlFile.getVersionLine();
        YamlLine servicesLine = yamlFile.getServicesLine();
        Collection<DockerService> services = yamlFile.getServices();
        GlobalSecrets globalSecrets = yamlFile.getGlobalSecrets();
        boolean allServicesCommented = yamlFile.allServicesCommented();
        boolean allSecretsCommented = yamlFile.getGlobalSecrets().allSecretsCommented();
        versionLine.uncomment();
        servicesLine.uncomment();
        globalSecrets.uncomment();

        if (allServicesCommented) {
            servicesLine.comment();
        }
        if (allSecretsCommented) {
            globalSecrets.comment();
        }

        if (allSecretsCommented && allServicesCommented) {
            versionLine.comment();
        }

        writeYamlLine(writer, versionLine);
        writeYamlLine(writer, servicesLine);
        for (DockerService service : services) {
            writeService(writer, service);
        }
        writeGlobalSecrets(writer, globalSecrets);
    }

    private static void writeService(YamlWriter writer, DockerService service) throws IOException {
        writeYamlBlock(writer, service.getLinesInBlock());
    }

    private static void writeYamlBlock(YamlWriter writer, Collection<YamlLine> lines) throws IOException {
        for (YamlLine line : lines) {
            writeYamlLine(writer, line);
        }
    }

    private static void writeGlobalSecrets(YamlWriter writer, GlobalSecrets globalSecrets) throws IOException {
        writeYamlLine(writer, globalSecrets);
        for (DockerSecret secret : globalSecrets.getSecrets()) {
            for (YamlLine secretLine : secret.getLinesInBlock()) {
                writeYamlLine(writer, secretLine);
            }
        }
    }

    private static void writeYamlLine(YamlWriter writer, YamlLine line) throws IOException {
        writer.writeLine(line.toString());
    }
}
