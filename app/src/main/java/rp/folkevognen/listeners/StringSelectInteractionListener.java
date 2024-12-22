package rp.folkevognen.listeners;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import rp.folkevognen.Folkevognen;
import rp.folkevognen.Interaction;
import rp.folkevognen.Settings;

public class StringSelectInteractionListener extends ListenerAdapter {
    Logger logger = LoggerFactory.getLogger(StringSelectInteractionListener.class);
    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        logger.info(event.getComponentId());
        for (Interaction<StringSelectInteractionEvent> interaction : interactions) {
            if (event.getComponentId().equals(interaction.name)) {
                interaction.function.run(event);
            }
        }
    }

    public static List<Interaction<StringSelectInteractionEvent>> interactions = new ArrayList<>(){{
        add(new Interaction<>(
            "select-folkevognen",
            "Select a folker",
            e -> {
                String folker = e.getValues().get(0);
                // decrement the previous folker and increment the new one
                Settings settings = new Settings();
                if(settings.folkevognen.get(folker) == null){
                    settings.folkevognen.put(folker, 0);
                }
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
