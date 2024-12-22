package rp.folkevognen;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import rp.folkevognen.listeners.ButtonInteractionListener;
import rp.folkevognen.listeners.MessageContextInteractionListener;
import rp.folkevognen.listeners.SlashCommandInteractionListener;
import rp.folkevognen.listeners.StringSelectInteractionListener;
import rp.folkevognen.listeners.UserContextInteractionListener;

public class App {
    public static JDA jda;
    private static Logger logger = LoggerFactory.getLogger(App.class);


    static void main() throws InterruptedException {
        Settings settings = new Settings();
        jda = JDABuilder.createDefault(settings.token)
            .addEventListeners(
                new SlashCommandInteractionListener(),
                new ButtonInteractionListener(),
                new StringSelectInteractionListener(),
                new MessageContextInteractionListener(),
                new UserContextInteractionListener()
            )
            .build();
        jda.updateCommands()
            .addCommands(SlashCommandInteractionListener.interactions.stream()
                .map(interaction -> interaction.command)
                .collect(Collectors.toList()))
            .addCommands(MessageContextInteractionListener.interactions.stream()
                .map(interaction -> interaction.command)
                .collect(Collectors.toList()))
            .addCommands(UserContextInteractionListener.interactions.stream()
                .map(interaction -> interaction.command)
                .collect(Collectors.toList()))
            .queue();
        jda.awaitReady();
        logger.info("Bot connected with user " + jda.getSelfUser().getEffectiveName());
    }
    

}
