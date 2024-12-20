package rp.folkevognen;

import java.util.List;

import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

interface Function {
    void run(Event event);
}

public class Interaction {
    public String name;
    public String description;
    public CommandData command;
    public Function function;

    public Interaction(String name, String description, Function f) {
        this.name = name;
        this.description = description;
        this.command = Commands.slash(name, description);
        this.function = f;
    }
    public Interaction(String name, String description, Function f, List<OptionData> options) {
        this.name = name;
        this.description = description;
        this.command = Commands.slash(name, description).addOptions(options);
        this.function = f;
    }
}
