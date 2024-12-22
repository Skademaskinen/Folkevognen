package rp.folkevognen.listeners;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import rp.folkevognen.Interaction;
import rp.folkevognen.Settings;

public class MessageContextInteractionListener extends ListenerAdapter {
    Logger logger = LoggerFactory.getLogger(MessageContextInteractionListener.class);
    @Override
    public void onMessageContextInteraction(MessageContextInteractionEvent event) {
        logger.info(event.getName());
        for (Interaction<MessageContextInteractionEvent> interaction : interactions) {
            if (event.getName().equals(interaction.name)) {
                interaction.function.run(event);
            }
        }
    }

    public static List<Interaction<MessageContextInteractionEvent>> interactions = new ArrayList<>(){{
        add(new Interaction<>(
            "Set Folker",
            "Set the folker",
            Command.Type.MESSAGE,
            e -> {
                // decrement the previous folker and increment the new one
                Settings settings = new Settings();
                if (settings.folkevognen.get(e.getUser().getId()) == null) {
                    settings.folkevognen.put(e.getUser().getId(), 0);
                    settings.write(true);
                }
                settings.folkevognen.put(settings.lastFolker, settings.folkevognen.get(settings.lastFolker) - 1);
                settings.folkevognen.put(e.getUser().getId(), settings.folkevognen.get(e.getUser().getId()) + 1);
                settings.lastFolker = e.getUser().getId();
                settings.write();
                e.reply("Folker set, please refresh the folkevognen").setEphemeral(true).queue();
            }
        ));

        add(new Interaction<>(
            "Add user",
            "Add a user to the folkevognen",
            Command.Type.MESSAGE,
            e -> {
                // add the user to the folkevognen
                Settings settings = new Settings();
                settings.folkevognen.put(e.getTarget().getAuthor().getId(), 0);
                settings.write();
                e.reply("User added to the folkevognen").setEphemeral(true).queue();
            }
        ));

        add(new Interaction<>(
            "Remove user",
            "Remove a user from the folkevognen",
            Command.Type.MESSAGE,
            e -> {
                // remove the user from the folkevognen
                Settings settings = new Settings();
                settings.folkevognen.remove(e.getTarget().getAuthor().getId());
                settings.write();
                e.reply("User removed from the folkevognen").setEphemeral(true).queue();
            }
        ));
    }};
}
