package rp.folkevognen;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

interface Function<T extends Event> {
    void run(T event);
}

public class Interaction<T extends Event> {
    final public String name;
    final public String description;
    final public CommandData command;
    final public Function<T> function;

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

    static Logger logger = LoggerFactory.getLogger(Interaction.class);

    // Static shit
    static List<LayoutComponent> getActionRows(Guild guild){
        return new ArrayList<>(){{
            StringSelectMenu.Builder menu = StringSelectMenu.create("select-folkevognen");
            menu.setPlaceholder("Override this weeks folker");
            Settings settings = new Settings();
            settings.folkevognen.forEach((id, count) -> {
                try{
                    menu.addOption(guild.retrieveMemberById(id).complete().getEffectiveName(), id);
                } catch (Exception e) {
                    logger.error("Failed to retrieve member with id " + id);
                }
            });
            add(ActionRow.of(menu.build()));
            add(ActionRow.of(
                Button.primary("refresh-folkevognen", "Refresh"),
                Button.primary("show-folkevognen", "Show folkevognen"),
                Button.danger("revert-folkevognen", "Revert")
            ));
        }};
    }

    static List<Interaction<SlashCommandInteractionEvent>> slashInteractions = new ArrayList<>(){{
        add(new Interaction<>(
            "ping",
            "Ping the bot",
            e -> e.reply("Pong!")
                .setEphemeral(true)
                .queue()
        ));

        add(new Interaction<>(
            "echo",
            "Echo the message",
            new ArrayList<>(){{
                add(new OptionData(OptionType.STRING, "message", "The message to echo", true));
            }},
            e -> e.reply(
                e.getOption("message").getAsString()
            ).setEphemeral(true).queue()
        ));

        add(new Interaction<>(
            "roll",
            "Roll any number of n sided dice",
            new ArrayList<>(){{
                add(new OptionData(OptionType.STRING, "dice", "The dice to roll", true));
            }},
            e -> {
                String dice = e.getOption("dice").getAsString();
                if (!dice.matches("\\d+d\\d+")) {
                    e.reply("Invalid dice format").setEphemeral(true).queue();
                    return;
                }
                String[] parts = dice.split("d");
                int n = Integer.parseInt(parts[0]);
                int sides = Integer.parseInt(parts[1]);
                int sum = 0;
                for (int i = 0; i < n; i++) {
                    sum += (int)(Math.random() * sides) + 1;
                }
                e.reply("You rolled " + sum).queue();
            }
        ));

        add(new Interaction<>(
            "folkevognen",
            "Get the next person to folk the vogn, and increment their amount",
            e -> e.reply(
                Folkevognen.getCurrentFolker() + " is next to folk the vogn!"
            ).queue()
        ));

        add(new Interaction<>(
            "folkevognen-embed",
            "Creates an embed with a button to refresh it",
            e -> e.replyEmbeds(
                Folkevognen.buildFolkevognEmbed(Folkevognen.getCurrentFolker())
            ).addComponents(
                getActionRows(e.getGuild())
            ).queue()
        ));

        add(new Interaction<>(
            "remove",
            "removes a message a bot has sent by id",
            new ArrayList<>(){{
                add(new OptionData(OptionType.STRING, "id", "The id of the message to remove", true));
            }},
            e -> {
                String id = e.getOption("id").getAsString();
                e.getChannel().deleteMessageById(id).queue();
                e.reply("Deleted message with id " + id).setEphemeral(true).queue();
            }
        ));
    }};

    static List<Interaction<ButtonInteractionEvent>> buttonInteractions = new ArrayList<>(){{
        add(new Interaction<>(
            "refresh-folkevognen",
            "Refresh the folkevognen embed",
            e -> e.editMessageEmbeds(
                Folkevognen.buildFolkevognEmbed(
                    Folkevognen.getCurrentFolker()
                )
            ).queue()
        ));

        add(new Interaction<>(
            "show-folkevognen",
            "Show the folkevognen",
            e -> {
                Settings settings = new Settings();
                StringBuilder folkevognen = new StringBuilder();
                settings.folkevognen.forEach((id, count) -> {
                    folkevognen.append(App.jda.retrieveUserById(id).complete().getAsMention() + ": " + count + "\n");
                });
                e.reply(folkevognen.toString()).setEphemeral(true).queue();
            }
        ));

        add(new Interaction<>(
            "revert-folkevognen",
            "Revert the folkevognen to the previous state",
            e -> {
                Settings settings = new Settings();
                if(settings.lastFolker.equals("")) {
                    e.reply("No folker to revert").setEphemeral(true).queue();
                    return;
                }
                String folkerToBeReverted = settings.lastFolker;
                settings.folkevognen.put(folkerToBeReverted, settings.folkevognen.get(folkerToBeReverted) - 1);
                settings.lastFolkedWeek -= 1;
                settings.lastFolker = "";
                settings.write();
                e.editMessageEmbeds(Folkevognen.buildFolkevognEmbed())
                    .queue();
            }
        ));
    }};

    static List<Interaction<StringSelectInteractionEvent>> selectInteractions = new ArrayList<>(){{
        add(new Interaction<>(
            "select-folkevognen",
            "Select a folker",
            e -> {
                String folker = e.getValues().get(0);
                // decrement the previous folker and increment the new one
                Settings settings = new Settings();
                settings.folkevognen.put(settings.lastFolker, settings.folkevognen.get(settings.lastFolker) - 1);
                settings.folkevognen.put(folker, settings.folkevognen.get(folker) + 1);
                settings.lastFolker = folker;
                settings.write();
                e.editMessageEmbeds(
                    Folkevognen.buildFolkevognEmbed(folker)
                ).queue();
            }
        ));
    }};
}
