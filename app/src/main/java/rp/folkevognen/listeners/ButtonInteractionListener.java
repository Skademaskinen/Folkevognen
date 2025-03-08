package rp.folkevognen.listeners;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rp.folkevognen.App;
import rp.folkevognen.Folkevognen;
import rp.folkevognen.Interaction;
import rp.folkevognen.Settings;

public class ButtonInteractionListener extends ListenerAdapter {
    Logger logger = LoggerFactory.getLogger(ButtonInteractionListener.class);
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        logger.info(event.getComponentId());
        for (Interaction<ButtonInteractionEvent> interaction : interactions) {
            if (event.getComponentId().equals(interaction.name)) {
                interaction.function.run(event);
            }
        }
    }

    public static List<Interaction<ButtonInteractionEvent>> interactions = new ArrayList<>(){{
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
        add(new Interaction<>(
            "ping-folkevognen",
            "Ping the current folker",
            e -> {
                var currentFolker = Folkevognen.getCurrentFolker();
                var ping = App.jda.retrieveUserById(currentFolker).complete().getAsMention();
                e.reply(ping + " Folker vognen idag!!!").queue();
            }
        ));
    }};

}
