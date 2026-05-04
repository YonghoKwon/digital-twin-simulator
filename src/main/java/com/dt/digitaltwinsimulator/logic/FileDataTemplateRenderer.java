package com.dt.digitaltwinsimulator.logic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FileDataTemplateRenderer {
    public record FileDataRecord(Map<String, String> valuesByName, List<String> orderedValues) {
        public String valueFor(String name, int index) {
            if (valuesByName.containsKey(name)) {
                return valuesByName.get(name);
            }
            if (index < orderedValues.size()) {
                return orderedValues.get(index);
            }
            throw new IllegalArgumentException("No value for template field: " + name);
        }
    }
}
