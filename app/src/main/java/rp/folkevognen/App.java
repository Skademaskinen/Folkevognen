package rp.folkevognen;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

class App extends ListenerAdapter {
    public static JDA jda;
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
                String folker = getCurrentFolker();
                event.reply(folker + " is next to folk the vogn!").queue();
            }
        ));
        add(new Interaction (
            "folkevognen-embed",
            "Creates an embed with a button to refresh it",
            (e) -> {
                SlashCommandInteractionEvent event = (SlashCommandInteractionEvent)e;
                event.replyEmbeds(buildFolkevognEmbed(getCurrentFolker()))
                    .addComponents(getActionRows(event.getGuild()))
                    .queue();
            }
        ));
        add(new Interaction (
            "refresh-folkevognen",
            "Refresh the folkevognen embed",
            (e) -> {
                ButtonInteractionEvent event = (ButtonInteractionEvent)e;
                String folker = getCurrentFolker();
                event.editMessageEmbeds(buildFolkevognEmbed(folker))
                    .queue();
            }
        ));
        add(new Interaction (
            "show-folkevognen",
            "Show the folkevognen",
            (e) -> {
            ButtonInteractionEvent event = (ButtonInteractionEvent)e;
            Settings settings = new Settings();
            StringBuilder folkevognen = new StringBuilder();
            settings.folkevognen.forEach((id, count) -> {
                folkevognen.append(event.getJDA().retrieveUserById(id).complete().getAsMention() + ": " + count + "\n");
            });
            event.reply(folkevognen.toString()).setEphemeral(true).queue();
            }
        ));
        add(new Interaction (
            "revert-folkevognen",
            "Revert the folkevognen to the previous state",
            (e) -> {
                ButtonInteractionEvent event = (ButtonInteractionEvent)e;
                Settings settings = new Settings();
                String folkerToBeReverted = settings.lastFolker;
                settings.folkevognen.put(folkerToBeReverted, settings.folkevognen.get(folkerToBeReverted) - 1);
                settings.lastFolkedWeek -= 1;
                settings.lastFolkedYear -= 1;
                settings.write();
                event.editMessageEmbeds(buildFolkevognEmbed("REVERTED"))
                    .queue();
                event.reply("Reverted the folkevognen").setEphemeral(true).queue();
            }
        ));
        add(new Interaction (
            "select-folkevognen",
            "Select a folker",
            (e) -> {
            StringSelectInteractionEvent event = (StringSelectInteractionEvent)e;
            String folker = event.getValues().get(0);
            // decrement the previous folker and increment the new one
            Settings settings = new Settings();
            settings.folkevognen.put(settings.lastFolker, settings.folkevognen.get(settings.lastFolker) - 1);
            settings.folkevognen.put(folker, settings.folkevognen.get(folker) + 1);
            settings.lastFolker = folker;
            settings.write();
            event.editMessageEmbeds(buildFolkevognEmbed(folker))
                .queue();
            }
        ));
        add(new Interaction (
            "remove",
            "removes a message a bot has sent by id",
            (e) -> {
                SlashCommandInteractionEvent event = (SlashCommandInteractionEvent)e;
                String id = event.getOption("id").getAsString();
                event.getChannel().deleteMessageById(id).queue();
                event.reply("Deleted message with id " + id).setEphemeral(true).queue();
            },
            new ArrayList<>(){{
                add(new OptionData(OptionType.STRING, "id", "The id of the message to remove", true));
            }}
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
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        logger.info("Received button: " + event.getComponentId());
        for (Interaction interaction : interactions) {
            if (event.getComponentId().equals(interaction.name)) {
            interaction.function.run(event);
            }
        }
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        logger.info("Received select: " + event.getComponentId());
        for (Interaction interaction : interactions) {
            if (event.getComponentId().equals(interaction.name)) {
                interaction.function.run(event);
            }
        }
    }

    static MessageEmbed buildFolkevognEmbed(String current) {
        String image = jda.getSelfUser().getEffectiveAvatarUrl();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setThumbnail(image);
        builder.setTitle("Folkevognen");
        builder.setDescription("Click the button to refresh");
        builder.addField("This weeks folker", jda.retrieveUserById(current).complete().getAsMention(), false);
        return builder.build();
    }
    static String getCurrentFolker() {
        Settings settings = new Settings();
        int week = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        if (week == settings.lastFolkedWeek && year == settings.lastFolkedYear) {
            return settings.lastFolker;
        }
        Map<String, Integer> folkevognen = settings.folkevognen;
        String name = folkevognen.entrySet().stream()
            .min((a, b) -> a.getValue() - b.getValue())
            .get().getKey();
        folkevognen.put(name, folkevognen.get(name) + 1);
        settings.write(folkevognen, name);
        return name;

    }
}
