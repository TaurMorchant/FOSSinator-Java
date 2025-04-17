package org.qubership.fossinator.processor.model;

public record Tag(String value, long offset, int length) {
    public boolean isProperty() {
        return value.startsWith("$");
    }

    public String getPropertyName() {
        return value.substring(2, value.length() - 1);
    }
}
