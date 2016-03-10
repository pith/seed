package org.seedstack.seed.cli;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.seed.cli.internal.HelpService;

import java.util.ArrayList;
import java.util.List;

public class HelpServiceTest {

    private HelpService underTest;

    @Test
    public void testCommandListDescriptionWithNoCommand() throws Exception {
        underTest = new HelpService();
        String description = underTest.getCommandListDescription();
        Assertions.assertThat(description).isEqualTo("No command found\n");
    }

    @Test
    public void testCommandListDescription() throws Exception {
        List<Class<? extends CommandLineHandler>> commandLineHandlers = new ArrayList<Class<? extends CommandLineHandler>>();
        commandLineHandlers.add(DummyCommandLineHandler.class);
        commandLineHandlers.add(SampleCommandLineHandler.class);
        underTest = new HelpService(commandLineHandlers);
        String description = underTest.getCommandListDescription();
        Assertions.assertThat(description).isEqualTo("Available commands:\n    - dummy: Dummy command\n    - test\n");
    }

    @Test
    public void testCommandDescription() throws Exception {
        List<Class<? extends CommandLineHandler>> commandLineHandlers = new ArrayList<Class<? extends CommandLineHandler>>();
        commandLineHandlers.add(SampleCommandLineHandler.class);
        underTest = new HelpService(commandLineHandlers);
        String description = underTest.getCommandDescription("dummy");
        Assertions.assertThat(description).isEqualTo("usage: dummy [<args>] [options]\n" +
                "    - dummy: Dummy command\n    - test\n");
    }
}
