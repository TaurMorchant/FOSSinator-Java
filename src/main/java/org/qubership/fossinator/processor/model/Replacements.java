package org.qubership.fossinator.processor.model;

import java.util.ArrayList;

public class Replacements extends ArrayList<Replacement> {
    public void add(Tag tag, String newValue) {
        if (tag != null) {
            add(new Replacement(
                    tag.offset(),
                    tag.length(),
                    newValue
            ));
        }
    }
}
