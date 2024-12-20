package rp.folkevognen;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

class App extends ListenerAdapter {
    public static JDA jda;
    static List<Interaction> interactions = new ArrayList<Interaction>(){{
        add(new Interaction (
            "ping", 
            "Ping the bot",
            (e) -> {
                SlashCommandInteractionEvent event = (SlashCommandInteractionEvent)e;
                event.reply("Pong!").setEphemeral(true).queue();
            }
        ));
        add(new Interaction (
            "echo", 
            "Echo the message",  
            (e) -> {
                SlashCommandInteractionEvent event = (SlashCommandInteractionEvent)e;
                event.reply(event.getOption("message").getAsString()).setEphemeral(true).queue();
            },
            new ArrayList<>(){{
                add(new OptionData(OptionType.STRING, "message", "The message to echo", true));
            }}
        ));
        add(new Interaction (
            "roll",
            "Roll any number of n sided dice",
            (e) -> {
                SlashCommandInteractionEvent event = (SlashCommandInteractionEvent)e;
                String dice = event.getOption("dice").getAsString();
                if (!dice.matches("\\d+d\\d+")) {
                    event.reply("Invalid dice format").setEphemeral(true).queue();
                    return;
                }
                String[] parts = dice.split("d");
                int n = Integer.parseInt(parts[0]);
                int sides = Integer.parseInt(parts[1]);
                int sum = 0;
                for (int i = 0; i < n; i++) {
                    sum += (int)(Math.random() * sides) + 1;
                }
                event.reply("You rolled " + sum).queue();
            },
            new ArrayList<>(){{
                add(new OptionData(OptionType.STRING, "dice", "The dice to roll", true));
            }}
        ));
        add(new Interaction (
            "folkevognen",
            "Get the next person to folk the vogn, and increment their amount",
            (e) -> {
                SlashCommandInteractionEvent event = (SlashCommandInteractionEvent)e;
                Settings settings = new Settings();
                // check if the year and week have changed, otherwise return last folked person
                int week = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
                int year = Calendar.getInstance().get(Calendar.YEAR);
                if (week == settings.lastFolkedWeek && year == settings.lastFolkedYear) {
                    // no need to folk the vogn
                    event.reply(settings.lastFolker + " folked the vogn this week!").queue();
                    return;
                }
                Map<String, Integer> folkevognen = settings.folkevognen;
                // find the name of the person with the lowest count
                String name = folkevognen.entrySet().stream()
                    .min((a, b) -> a.getValue() - b.getValue())
                    .get().getKey();
                // increment their count
                folkevognen.put(name, folkevognen.get(name) + 1);
                settings.write(folkevognen, name);
                event.reply(name + " is next to folk the vogn!").queue();
            }
        ));
    }};
    private static Logger logger = LoggerFactory.getLogger(App.class);


    static void main() {
        Settings settings = new Settings();
        jda = JDABuilder.createDefault(settings.token)
            .addEventListeners(new App())
            .build();
        jda.updateCommands()
            .addCommands(interactions.stream()
                .map(interaction -> interaction.command)
                .collect(Collectors.toList()))
            .queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        logger.info("Received command: " + event.getName());
        for(OptionMapping option : event.getOptions()) {
            logger.info("Option: " + option.getName() + " = " + option.getAsString());
        }
        for (Interaction interaction : interactions) {
            if (event.getName().equals(interaction.name)) {
                interaction.function.run(event);
            }
        }
    }
}
