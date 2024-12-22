package rp.folkevognen;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

class App extends ListenerAdapter {
    public static JDA jda;
    private static Logger logger = LoggerFactory.getLogger(App.class);


    static void main() throws InterruptedException {
        Settings settings = new Settings();
        jda = JDABuilder.createDefault(settings.token)
            .addEventListeners(new App())
            .build();
        jda.updateCommands()
            .addCommands(Interaction.slashInteractions.stream()
                .map(interaction -> interaction.command)
                .collect(Collectors.toList()))
            .queue();
        jda.awaitReady();
        logger.info("Bot connected with user " + jda.getSelfUser().getEffectiveName());
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        logger.info("Received command: " + event.getName());
        for(OptionMapping option : event.getOptions()) {
            logger.info("Option: " + option.getName() + " = " + option.getAsString());
        }
        for (Interaction<SlashCommandInteractionEvent> interaction : Interaction.slashInteractions) {
            if (event.getName().equals(interaction.name)) {
                interaction.function.run(event);
            }
        }
    }
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        logger.info("Received button: " + event.getComponentId());
        for (Interaction<ButtonInteractionEvent> interaction : Interaction.buttonInteractions) {
            if (event.getComponentId().equals(interaction.name)) {
            interaction.function.run(event);
            }
        }
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        logger.info("Received select: " + event.getComponentId());
        for (Interaction<StringSelectInteractionEvent> interaction : Interaction.selectInteractions) {
            if (event.getComponentId().equals(interaction.name)) {
                interaction.function.run(event);
            }
        }
    }

}
