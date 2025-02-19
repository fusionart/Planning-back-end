package com.monbat.planning.utils.enums;

import lombok.Getter;

@Getter
public enum Warehouse {
    PERSONIFICATION("Персонификация"),
    FORMATION("Формовка"),
    ASSEMBLY("Монтажен цех"),
    MAIN("Основен цех");

    private final String label;

    Warehouse(String label) {
        this.label = label;
    }
}
