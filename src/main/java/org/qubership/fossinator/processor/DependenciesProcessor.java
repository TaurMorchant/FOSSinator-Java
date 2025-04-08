package org.qubership.fossinator.processor;

import com.ximpleware.*;
import org.qubership.fossinator.config.ConfigReader;
import org.qubership.fossinator.config.Dependency;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class DependenciesProcessor implements Processor {
    private final static String GROUP_ID_TAG = "groupId";
    private final static String ARTIFACT_ID_TAG = "artifactId";
    private final static String VERSION_TAG = "version";

    @Override
    public void process(String dir) {
        Path rootDir = Paths.get(dir);

        try (Stream<Path> s = Files.walk(rootDir)) {
            s.filter(path -> path.toString().endsWith("pom.xml")).forEach(this::updateDependencies);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateDependencies(Path filePath) {
        try {
            System.out.println("updateDependencies. filePath = " + filePath.toString());

            byte[] pomContent = Files.readAllBytes(filePath);
            String pomXml = new String(pomContent);

            List<Replacement> replacementsToApply = getReplacementsToApply(pomXml);

            if (!replacementsToApply.isEmpty()) {
                System.out.println("apply");
                replacementsToApply.sort((a, b) -> Long.compare(b.offset, a.offset));

                StringBuilder newPomXml = new StringBuilder(pomXml);
                for (Replacement r : replacementsToApply) {
                    newPomXml.replace(
                            (int) r.offset,
                            (int) r.offset + r.length,
                            r.newValue
                    );
                }

                Files.write(filePath, newPomXml.toString().getBytes());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Replacement> getReplacementsToApply(String pomXml) throws Exception {
        VTDGen vg = new VTDGen();
        vg.setDoc(pomXml.getBytes());
        vg.parse(true);

        VTDNav vn = vg.getNav();
        AutoPilot ap = new AutoPilot(vn);
        ap.selectXPath("/project/dependencies/dependency");

        List<Replacement> replacementsToApply = new ArrayList<>();

        while (ap.evalXPath() != -1) {
            vn.push();

            String currentGroupId = getTagValue(vn, GROUP_ID_TAG);
            String currentArtifactId = getTagValue(vn, ARTIFACT_ID_TAG);

            if (currentGroupId != null && currentArtifactId != null) {
                Dependency depToReplace = ConfigReader.getConfig().getDependency(currentGroupId, currentArtifactId);
                if (depToReplace != null) {
                    TagPosition groupIdPos = getTagPosition(vn, GROUP_ID_TAG);
                    if (groupIdPos != null) {
                        replacementsToApply.add(new Replacement(
                                groupIdPos.offset,
                                groupIdPos.length,
                                depToReplace.getNewGroupId()
                        ));
                    }

                    TagPosition artifactIdPos = getTagPosition(vn, ARTIFACT_ID_TAG);
                    if (artifactIdPos != null) {
                        replacementsToApply.add(new Replacement(
                                artifactIdPos.offset,
                                artifactIdPos.length,
                                depToReplace.getNewArtifactId()
                        ));
                    }

                    TagPosition versionPos = getTagPosition(vn, VERSION_TAG);
                    if (versionPos != null) {
                        replacementsToApply.add(new Replacement(
                                versionPos.offset,
                                versionPos.length,
                                depToReplace.getNewVersion()
                        ));
                    }
                }
            }

            vn.pop();
        }
        return replacementsToApply;
    }

    private static String getTagValue(VTDNav vn, String tagName) throws Exception {
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

    private static TagPosition getTagPosition(VTDNav vn, String tagName) throws Exception {
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

    private record TagPosition(long offset, int length) { }

    private record Replacement(long offset, int length, String newValue) { }
}
