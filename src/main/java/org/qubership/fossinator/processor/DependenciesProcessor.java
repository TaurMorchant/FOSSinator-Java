package org.qubership.fossinator.processor;

import com.ximpleware.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class DependenciesProcessor implements Processor {
    @Override
    public void process(String dir) {
        Path rootDir = Paths.get(dir);

        try (Stream<Path> s = Files.walk(rootDir)) {
            s.filter(path -> path.toString().endsWith("pom.xml")).forEach(this::updateDependencies);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //ЭТО РАБОТАЕТ!!!
    public static class DependencyReplacement {
        final String groupId;
        final String artifactId;
        final String newArtifactId;

        public DependencyReplacement(String groupId, String artifactId, String newArtifactId) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.newArtifactId = newArtifactId;
        }
    }

    public static List<DependencyReplacement> replacements = new ArrayList<>() {{
        add(new DependencyReplacement("com.netcracker.cloud.quarkus", "context-propagation", "context-propagation-vlla"));
        add(new DependencyReplacement("com.netcracker.cloud.quarkus", "framework-contexts", "framework-contexts-vlla"));
    }};

    public void updateDependencies(Path filePath) {
        try {
            System.out.println("updateDependencies. filePath = " + filePath.toString());

            // Читаем содержимое файла
            byte[] pomContent = Files.readAllBytes(filePath);
            String pomXml = new String(pomContent);

            // Создаем карту для быстрого поиска замен
            Map<String, Map<String, String>> replacementMap = new HashMap<>();
            for (DependencyReplacement dr : replacements) {
                replacementMap
                        .computeIfAbsent(dr.groupId, k -> new HashMap<>())
                        .put(dr.artifactId, dr.newArtifactId);
            }

            // Инициализируем VTD
            VTDGen vg = new VTDGen();
            vg.setDoc(pomXml.getBytes());
            vg.parse(true);

            VTDNav vn = vg.getNav();
            AutoPilot ap = new AutoPilot(vn);
            ap.selectXPath("/project/dependencies/dependency");

            List<Replacement> replacementsToApply = new ArrayList<>();

            while (ap.evalXPath() != -1) {
                vn.push();

                String currentGroupId = getTagValue(vn, "groupId");
                String currentArtifactId = getTagValue(vn, "artifactId");
                TagPosition artifactIdPos = getTagPosition(vn, "artifactId");
                System.out.println("currentGroupId = " + currentGroupId + ", currentArtifactId = " + currentArtifactId);

                if (currentGroupId != null && currentArtifactId != null &&
                        replacementMap.containsKey(currentGroupId) &&
                        replacementMap.get(currentGroupId).containsKey(currentArtifactId)) {

                    String newArtifactId = replacementMap.get(currentGroupId).get(currentArtifactId);
                    replacementsToApply.add(new Replacement(
                            artifactIdPos.offset,
                            artifactIdPos.length,
                            newArtifactId
                    ));
                }

                vn.pop();
            }

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

    private static class Replacement {
        final long offset;
        final int length;
        final String newValue;

        Replacement(long offset, int length, String newValue) {
            this.offset = offset;
            this.length = length;
            this.newValue = newValue;
        }
    }

    /**
     * Получает значение указанного тега в текущем контексте
     */
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

    /**
     * Получает позицию указанного тега в исходном XML
     */
    private static TagPosition getTagPosition(VTDNav vn, String tagName) throws Exception {
        AutoPilot ap = new AutoPilot(vn);
        ap.selectXPath(tagName);
        try {
            if (ap.evalXPath() != -1) {
                int val = vn.getText();
                if (val != -1) {
                    return new TagPosition(vn.getTokenOffset(val), vn.getTokenLength(val));
                }
            }
            return new TagPosition(-1, -1);
        } finally {
            ap.resetXPath();
        }
    }

    /**
     * Вспомогательный класс для хранения позиции тега
     */
    private static class TagPosition {
        final long offset;
        final int length;

        TagPosition(long offset, int length) {
            this.offset = offset;
            this.length = length;
        }
    }
}
