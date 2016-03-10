package org.seedstack.seed.cli.internal;

import java.util.List;

public class CommandDescription {

    private String name;

    private List<OptionDescription> options;

    static class OptionDescription {

        private String name;
        private String longName;
        private boolean mandatory;
        private int valueCount;
        private char valueSeparator;
        private boolean mandatoryValue;
        private String[] defaultValues;
        String description;
    }
}
