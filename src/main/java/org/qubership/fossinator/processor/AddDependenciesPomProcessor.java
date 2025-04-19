package org.qubership.fossinator.processor;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;
import lombok.extern.slf4j.Slf4j;
import org.qubership.fossinator.config.ConfigReader;
import org.qubership.fossinator.config.model.Dependency;
import org.qubership.fossinator.config.model.DependencyToAdd;
import org.qubership.fossinator.xml.XMLHelper;

import java.nio.file.Path;
import java.text.MessageFormat;

import static org.qubership.fossinator.Constants.POM_FILE_NAME;

@Slf4j
public class AddDependenciesPomProcessor extends AbstractPomFileProcessor {
    private final static String GET_DEPENDENCY_XPATH_PATTERN = "/project/dependencies/dependency[groupId=''{0}'' and artifactId=''{1}'']";

    @Override
    public boolean shouldBeExecuted() {
        return !ConfigReader.getConfig().getDependenciesToAdd().isEmpty();
    }

    @Override
    public String getFileSuffix(){
        return POM_FILE_NAME;
    }

    @Override
    boolean processPom(Path filePath, String pomXml) throws Exception {
        VTDNav vn = XMLHelper.getVtdNav(pomXml);
        AutoPilot ap = new AutoPilot(vn);
        XMLModifier xm = new XMLModifier(vn);

        boolean updated = false;
        for (DependencyToAdd dependencyToAdd : ConfigReader.getConfig().getDependenciesToAdd()) {
            Dependency checkedDependency =  dependencyToAdd.getIfDependencyExists();
            String xpath = MessageFormat.format(GET_DEPENDENCY_XPATH_PATTERN, checkedDependency.getGroupId(), checkedDependency.getArtifactId());
            ap.selectXPath(xpath);

            if (ap.evalXPath() != -1) {

                String newDependencyXml = getNewDependencyXml(dependencyToAdd.getAddDependency());

                xm.insertAfterElement(newDependencyXml);

                updated = true;
            }
        }

        if (updated) {
            xm.output(filePath.toString());
        }

        return updated;
    }

    String getNewDependencyXml(Dependency dependency) {
        StringBuilder result = new StringBuilder("\n\t\t<dependency>\n");
        result.append("\t\t\t<groupId>").append(dependency.getGroupId()).append("</groupId>\n");
        result.append("\t\t\t<artifactId>").append(dependency.getArtifactId()).append("</artifactId>\n");
        if (dependency.getVersion() != null) {
            result.append("\t\t\t<version>").append(dependency.getVersion()).append("</version>\n");
        }
        if (dependency.getScope() != null) {
            result.append("\t\t\t<scope>").append(dependency.getScope()).append("</scope>\n");
        }
        result.append("\t\t</dependency>");
        return result.toString();
    }
}
