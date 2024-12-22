package rp.folkevognen;

import java.util.List;

import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Interaction<T extends Event> {
    final public String name;
    final public String description;
    final public CommandData command;
    final public Function<T> function;

    public interface Function<T extends Event> {
        void run(T event);
    }

    public Interaction(String name, String description, Function<T> f) {
        this.name = name;
        this.description = description;
        this.command = Commands.slash(name, description);
        this.function = f;
    }
    public Interaction(String name, String description, List<OptionData> options, Function<T> f) {
        this.name = name;
        this.description = description;
        this.command = Commands.slash(name, description).addOptions(options);
        this.function = f;
    }
    public Interaction(String name, String description, Command.Type type, Function<T> f) {
        this.name = name;
        this.description = description;
        this.command = Commands.context(type, name);
        this.function = f;
    }
}
