package org.qubership.fossinator.processor.model;

public record Replacement(long offset, int length, String newValue) {
}
