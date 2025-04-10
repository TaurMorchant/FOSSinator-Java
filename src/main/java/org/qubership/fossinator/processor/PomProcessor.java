package org.qubership.fossinator.processor;

import com.ximpleware.*;
import lombok.extern.slf4j.Slf4j;
import org.qubership.fossinator.config.ConfigReader;
import org.qubership.fossinator.config.Dependency;
import org.qubership.fossinator.processor.model.Replacement;
import org.qubership.fossinator.processor.model.Replacements;
import org.qubership.fossinator.processor.model.TagPosition;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
public class PomProcessor implements Processor {
    private final static String GROUP_ID_TAG = "groupId";
    private final static String ARTIFACT_ID_TAG = "artifactId";
    private final static String VERSION_TAG = "version";

    private final static String POM_FILE_NAME = "pom.xml";

    @Override
    public void process(String dir) {
        Path dirPath = Paths.get(dir);

        try (Stream<Path> s = Files.walk(dirPath)) {
            s.filter(path -> path.toString()
                    .endsWith(POM_FILE_NAME))
                    .forEach(this::processPomFile);
        } catch (IOException e) {
            log.error("Error while processing files in dir {}", dir);
        }
    }

    public void processPomFile(Path filePath) {
        try {
            log.info("updateDependencies. filePath = {}", filePath.toString());

            byte[] pomContent = Files.readAllBytes(filePath);
            String pomXml = new String(pomContent);

            String newPomXml = processPom(pomXml);

            if (!Objects.equals(pomXml, newPomXml)) {
                Files.write(filePath, newPomXml.getBytes());
            }
        } catch (Exception e) {
            log.error("Error while processing pom.xml {}", filePath.toString());
        }
    }

    String processPom(String pomXml) throws Exception{
        Replacements replacementsToApply = getDependencyReplacementsToApply(pomXml);

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

    Replacements getDependencyReplacementsToApply(String pomXml) throws Exception {
        VTDNav vn = getVtdNav(pomXml);

        Replacements replacementsToApply = new Replacements();

        processDependenciesXpath(replacementsToApply, vn, "/project/dependencies/dependency");
        processDependenciesXpath(replacementsToApply, vn, "/project/dependencyManagement/dependencies/dependency");

        return replacementsToApply;
    }

    VTDNav getVtdNav(String pomXml) throws ParseException {
        VTDGen vg = new VTDGen();
        vg.setDoc(pomXml.getBytes());
        vg.parse(true);

        return vg.getNav();
    }

    void processDependenciesXpath(Replacements replacements, VTDNav vn, String xpath) throws Exception {
        AutoPilot ap = new AutoPilot(vn);
        ap.selectXPath(xpath);

        while (ap.evalXPath() != -1) {
            vn.push();

            String currentGroupId = getTagValue(vn, GROUP_ID_TAG);
            String currentArtifactId = getTagValue(vn, ARTIFACT_ID_TAG);

            if (currentGroupId != null && currentArtifactId != null) {
                Dependency depToReplace = ConfigReader.getConfig().getDependency(currentGroupId, currentArtifactId);
                if (depToReplace != null) {
                    TagPosition groupIdPos = getTagPosition(vn, GROUP_ID_TAG);
                    replacements.add(groupIdPos, depToReplace.getNewGroupId());

                    TagPosition artifactIdPos = getTagPosition(vn, ARTIFACT_ID_TAG);
                    if (!depToReplace.isAnyArtifact()) {
                        replacements.add(artifactIdPos, depToReplace.getNewArtifactId());
                    }

                    TagPosition versionPos = getTagPosition(vn, VERSION_TAG);
                    replacements.add(versionPos, depToReplace.getNewVersion());
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

    TagPosition getTagPosition(VTDNav vn, String tagName) throws Exception {
        vn.push();
        AutoPilot ap = new AutoPilot(vn);
        ap.selectXPath(tagName);
        try {
            if (ap.evalXPath() != -1) {
                int val = vn.getText();
                if (val != -1) {
                    return new TagPosition(vn.getTokenOffset(val), vn.getTokenLength(val));
                }
            }
            return null;
        } finally {
            ap.resetXPath();
            vn.pop();
        }
    }
}
