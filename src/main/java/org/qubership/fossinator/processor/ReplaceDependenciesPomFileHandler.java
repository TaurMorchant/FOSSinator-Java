package org.qubership.fossinator.processor;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDNav;
import lombok.extern.slf4j.Slf4j;
import org.qubership.fossinator.config.ConfigReader;
import org.qubership.fossinator.config.model.DependencyToReplace;
import org.qubership.fossinator.processor.model.Replacement;
import org.qubership.fossinator.processor.model.Replacements;
import org.qubership.fossinator.processor.model.Tag;
import org.qubership.fossinator.xml.XMLHelper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class ReplaceDependenciesPomFileHandler {
    private final static String GROUP_ID_TAG = "groupId";
    private final static String ARTIFACT_ID_TAG = "artifactId";
    private final static String VERSION_TAG = "version";

    private Replacements replacementsToApply;
    private Map<Tag, String> propertiesToReplace;

    public boolean handle(Path filePath, String pomXml) throws Exception {
        String newPomXml = processPom(pomXml);

        if (!Objects.equals(pomXml, newPomXml)) {
            Files.write(filePath, newPomXml.getBytes());
            return true;
        }
        return false;
    }

    String processPom(String pomXml) throws Exception {
        replacementsToApply = new Replacements();
        propertiesToReplace = new HashMap<>();

        collectDependencyReplacementsToApply(pomXml);

        return applyReplacements(pomXml);
    }

    String applyReplacements(String pomXml) {
        if (!replacementsToApply.isEmpty()) {
            replacementsToApply.sort((a, b) -> Long.compare(b.offset(), a.offset()));

            StringBuilder newPomXml = new StringBuilder(pomXml);
            for (Replacement r : replacementsToApply) {
                newPomXml.replace(
                        (int) r.offset(),
                        (int) r.offset() + r.length(),
                        r.newValue()
                );
            }

            return newPomXml.toString();
        }
        return pomXml;
    }

    void collectDependencyReplacementsToApply(String pomXml) throws Exception {
        VTDNav vn = XMLHelper.getVtdNav(pomXml);

        processDependenciesXpath(vn, "/project/dependencies/dependency");
        processDependenciesXpath(vn, "/project/dependencyManagement/dependencies/dependency");

        if (!propertiesToReplace.isEmpty()) {
            for (Map.Entry<Tag, String> entry : propertiesToReplace.entrySet()) {
                AutoPilot ap = new AutoPilot(vn);
                ap.selectXPath("/project/properties");

                if (ap.evalXPath() != -1) {
                    vn.push();

                    Tag propertyTag = getTagPosition(vn, entry.getKey().getPropertyName());
                    replacementsToApply.add(propertyTag, entry.getValue());

                    vn.pop();
                } else {
                    //there is no property in current pom, looks like it in parent pom => just replace version directly
                    log.warn("Looks like property {} specified in parent pom. Cannot change property value. " +
                             "Property placeholder will be replaced by static value instead: {}",
                            entry.getKey().getPropertyName(), entry.getValue());

                    String valueToReplace = MessageFormat.format(
                            "{0}<!--This version was coming from a property ''{1}'' — please replace the property value manually-->",
                            entry.getValue(), entry.getKey().getPropertyName());
                    replacementsToApply.add(entry.getKey(), valueToReplace);
                }
            }
        }
    }

    void processDependenciesXpath(VTDNav vn, String xpath) throws Exception {
        AutoPilot ap = new AutoPilot(vn);
        ap.selectXPath(xpath);

        while (ap.evalXPath() != -1) {
            vn.push();

            String currentGroupId = getTagValue(vn, GROUP_ID_TAG);
            String currentArtifactId = getTagValue(vn, ARTIFACT_ID_TAG);

            if (currentGroupId != null && currentArtifactId != null) {
                DependencyToReplace depToReplace = ConfigReader.getConfig().getDependency(currentGroupId, currentArtifactId);
                if (depToReplace != null) {
                    Tag groupIdTag = getTagPosition(vn, GROUP_ID_TAG);
                    replacementsToApply.add(groupIdTag, depToReplace.getNewGroupId());

                    Tag artifactIdTag = getTagPosition(vn, ARTIFACT_ID_TAG);
                    if (!depToReplace.isWildcardArtifact()) {
                        replacementsToApply.add(artifactIdTag, depToReplace.getNewArtifactId());
                    }

                    Tag versionTag = getTagPosition(vn, VERSION_TAG);
                    if (versionTag != null) {
                        if (versionTag.isProperty()) {
                            propertiesToReplace.put(versionTag, depToReplace.getNewVersion());
                        } else {
                            replacementsToApply.add(versionTag, depToReplace.getNewVersion());
                        }
                    }
                }
            }

            vn.pop();
        }
    }

    String getTagValue(VTDNav vn, String tagName) throws Exception {
        vn.push();
        AutoPilot ap = new AutoPilot(vn);
        ap.selectXPath(tagName);
        try {
            if (ap.evalXPath() != -1) {
                int val = vn.getText();
                if (val != -1) {
                    return vn.toNormalizedString(val);
                }
            }
            return null;
        } finally {
            ap.resetXPath();
            vn.pop();
        }
    }

    Tag getTagPosition(VTDNav vn, String tagName) throws Exception {
        vn.push();
        AutoPilot ap = new AutoPilot(vn);
        ap.selectXPath(tagName);
        try {
            if (ap.evalXPath() != -1) {
                int val = vn.getText();
                if (val != -1) {
                    String text = vn.toString(val);
                    return new Tag(text, vn.getTokenOffset(val), vn.getTokenLength(val));
                }
            }
            return null;
        } finally {
            ap.resetXPath();
            vn.pop();
        }
    }
}
