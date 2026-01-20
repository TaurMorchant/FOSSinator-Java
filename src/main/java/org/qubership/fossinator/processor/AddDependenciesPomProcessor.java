package org.qubership.fossinator.processor;

import com.ximpleware.*;
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
    private static final  String GET_DEPENDENCY_XPATH_PATTERN = "/project/dependencies/dependency[groupId=''{0}'' and artifactId=''{1}'']";
    private static final  String GET_DEPENDENCY_IN_DMANAGEMENT_XPATH_PATTERN = "/project/dependencyManagement/dependencies/dependency[groupId=''{0}'' and artifactId=''{1}'']";

    @Override
    public boolean shouldBeExecuted() {
        return !ConfigReader.getConfig().getDependenciesToAdd().isEmpty();
    }

    @Override
    public String getFileSuffix() {
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
            lookForDependencyBy(GET_DEPENDENCY_XPATH_PATTERN, checkedDependency, ap);
            int result = ap.evalXPath();
            boolean isDMExist = false;
            if (result == -1) {
                lookForDependencyBy(GET_DEPENDENCY_IN_DMANAGEMENT_XPATH_PATTERN, checkedDependency, ap);
                result = ap.evalXPath();
                isDMExist = true;
            }

            if (result != -1) {

                String newDependencyXml = getNewDependencyXml(dependencyToAdd.getAddDependency(), isDMExist);

                xm.insertAfterElement(newDependencyXml);

                updated = true;
            }
        }

        if (updated) {
            xm.output(filePath.toString());
        }

        return updated;
    }

    private void lookForDependencyBy(String getDependencyXpathPattern, Dependency checkedDependency, AutoPilot ap) throws XPathParseException {
        String xpath = MessageFormat.format(getDependencyXpathPattern, checkedDependency.getGroupId(), checkedDependency.getArtifactId());
        ap.selectXPath(xpath);
    }

    String getNewDependencyXml(Dependency dependency, boolean isDMExist) {
        String tabs = isDMExist ? "\t\t\t\t" : "\t\t\t";
        StringBuilder result = new StringBuilder((isDMExist ? "\n\t\t\t" : "\n\t\t") + "<dependency>\n");
        result.append(tabs).append("<groupId>").append(dependency.getGroupId()).append("</groupId>\n");
        result.append(tabs).append("<artifactId>").append(dependency.getArtifactId()).append("</artifactId>\n");
        if (dependency.getVersion() != null) {
            result.append(tabs).append("<version>").append(dependency.getVersion()).append("</version>\n");
        }
        if (dependency.getType() != null) {
            result.append(tabs).append("<type>").append(dependency.getType()).append("</type>\n");
        }
        if (dependency.getScope() != null) {
            result.append(tabs).append("<scope>").append(dependency.getScope()).append("</scope>\n");
        }
        result.append(isDMExist ? "\t\t\t" : "\t\t").append("</dependency>");
        return result.toString();
    }
}
