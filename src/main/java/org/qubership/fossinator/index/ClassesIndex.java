package org.qubership.fossinator.index;

import lombok.Setter;

public class ClassesIndex {
    @Setter
    private static Index index;

    public static Index getIndex() {
        return index == null ? new Index() : index;
    }
}
