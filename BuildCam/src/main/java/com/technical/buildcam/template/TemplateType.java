package com.technical.buildcam.template;

public enum TemplateType {
    ORBIT("orbit", "Generates a 360-degree circular camera orbit around the target position."),
    SPIRAL("spiral", "Generates an ascending or descending helical camera path."),
    FLYBY("flyby", "Generates a linear side pass past the target building or redstone area."),
    TOPDOWN("topdown", "Generates a top-down aerial scan path over a perimeter or structure.");

    private final String key;
    private final String description;

    TemplateType(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }

    public static TemplateType fromString(String text) {
        if (text == null) return null;
        for (TemplateType type : values()) {
            if (type.key.equalsIgnoreCase(text) || type.name().equalsIgnoreCase(text)) {
                return type;
            }
        }
        return null;
    }
}
