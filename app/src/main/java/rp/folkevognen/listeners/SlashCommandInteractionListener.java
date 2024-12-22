package rp.folkevognen.listeners;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import rp.folkevognen.Folkevognen;
import rp.folkevognen.Interaction;

public class SlashCommandInteractionListener extends ListenerAdapter {
    Logger logger = LoggerFactory.getLogger(SlashCommandInteractionListener.class);
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        logger.info(event.getName());
        for (Interaction<SlashCommandInteractionEvent> interaction : interactions) {
            if (event.getName().equals(interaction.name)) {
            interaction.function.run(event);
            }
        }
    }
    public static List<Interaction<SlashCommandInteractionEvent>> interactions = new ArrayList<>(){{
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
                Folkevognen.getActionRows(e.getGuild())
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

        add(new Interaction<>(
            "help",
            "Show a list of commands",
            e -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle("Commands");
                for (Interaction<SlashCommandInteractionEvent> interaction : interactions) {
                    builder.addField(interaction.name, interaction.description, false);
                }
                e.replyEmbeds(builder.build()).setEphemeral(true).queue();
            }
        ));
        add(new Interaction<>(
            "help-buttons",
            "Show a list of buttons",
            e -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle("Buttons");
                for (Interaction<ButtonInteractionEvent> interaction : ButtonInteractionListener.interactions) {
                    builder.addField(interaction.name, interaction.description, false);
                }
                e.replyEmbeds(builder.build()).setEphemeral(true).queue();
            }
        ));
        add(new Interaction<>(
            "help-selects",
            "Show a list of selects",
            e -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Selects");
            for (Interaction<StringSelectInteractionEvent> interaction : StringSelectInteractionListener.interactions) {
                builder.addField(interaction.name, interaction.description, false);
            }
            e.replyEmbeds(builder.build()).setEphemeral(true).queue();
            }
        ));
        add(new Interaction<>(
            "help-usercontext",
            "Show a list of user context menus",
            e -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle("User context menus");
                for (Interaction<UserContextInteractionEvent> interaction : UserContextInteractionListener.interactions) {
                    builder.addField(interaction.name, interaction.description, false);
                }
            }
        ));

        add(new Interaction<>(
            "help-messagecontext",
            "Show a list of message context menus",
            e -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Message context menus");
            for (Interaction<MessageContextInteractionEvent> interaction : MessageContextInteractionListener.interactions) {
                builder.addField(interaction.name, interaction.description, false);
            }
            e.replyEmbeds(builder.build()).setEphemeral(true).queue();
            }
        ));
    }};

}
