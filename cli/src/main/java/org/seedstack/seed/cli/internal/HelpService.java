package org.seedstack.seed.cli.internal;

import org.seedstack.seed.cli.CliCommand;
import org.seedstack.seed.cli.CommandLineHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HelpService {

    public static final String NO_COMMAND_FOUND = "No command found\n";
    public static final String TAB = "    ";
    private final Map<String, Class<? extends CommandLineHandler>> commandLineHandlers = new HashMap<String, Class<? extends CommandLineHandler>>();

    public HelpService() {
    }

    public HelpService(List<Class<? extends CommandLineHandler>> commandLineHandlers) {
        for (Class<? extends CommandLineHandler> commandLineHandlerClass : commandLineHandlers) {
            CliCommand annotation = commandLineHandlerClass.getAnnotation(CliCommand.class);
            if (annotation != null) {
                this.commandLineHandlers.put(annotation.value(), commandLineHandlerClass);
            }
        }
    }

    public String getCommandListDescription() {
        if (commandLineHandlers.isEmpty()) {
            return NO_COMMAND_FOUND;
        }
        return computeListDescription();
    }

    private String computeListDescription() {
        StringBuilder stringBuilder = new StringBuilder("Available commands:\n");
        for (Class<? extends CommandLineHandler> commandLineHandlerClass : commandLineHandlers.values()) {
            CliCommand annotation = commandLineHandlerClass.getAnnotation(CliCommand.class);
            if (annotation != null) {
                stringBuilder.append(TAB).append("- ").append(annotation.value());
                if (annotation.description() != null && !annotation.description().equals("")) {
                    stringBuilder.append(": ").append(annotation.description());
                }
                stringBuilder.append("\n");
            }
        }
        return stringBuilder.toString();
    }

    public String getCommandDescription(String name) {
        Class<? extends CommandLineHandler> commandLineHandlerClass = commandLineHandlers.get(name);
        if (commandLineHandlerClass == null) {
            return "command " + name + " not found.\n";
        }

        return null;
    }

    private String computeDescription(Class<? extends CommandLineHandler> commandLineHandlerClass) {
        StringBuilder stringBuilder = new StringBuilder("usage: \n");
        CliCommand cliCommand = commandLineHandlerClass.getAnnotation(CliCommand.class);
        stringBuilder.append(cliCommand.value()).append("\n");

        stringBuilder.append(cliCommand.value());
        return stringBuilder.toString();
    }
}
