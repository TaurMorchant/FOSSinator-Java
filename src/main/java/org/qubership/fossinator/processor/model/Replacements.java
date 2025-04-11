package org.qubership.fossinator.processor.model;

import java.util.ArrayList;

public class Replacements extends ArrayList<Replacement> {
    public void add(TagPosition tagPosition, String newValue) {
        if (tagPosition != null) {
            add(new Replacement(
                    tagPosition.offset(),
                    tagPosition.length(),
                    newValue
            ));
        }
    }
}
