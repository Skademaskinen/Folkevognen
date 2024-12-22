package rp.folkevognen;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

public class Folkevognen {
    static Logger logger = LoggerFactory.getLogger(Folkevognen.class);

    public static MessageEmbed buildFolkevognEmbed(String current) {
        String image = App.jda.getSelfUser().getEffectiveAvatarUrl();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setThumbnail(image);
        builder.setTitle("Folkevognen");
        builder.setDescription("Click the button to refresh");
        builder.addField("This weeks folker", App.jda.retrieveUserById(current).complete().getAsMention(), false);
        return builder.build();
    }
    public static MessageEmbed buildFolkevognEmbed() {
        String image = App.jda.getSelfUser().getEffectiveAvatarUrl();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setThumbnail(image);
        builder.setTitle("Folkevognen");
        builder.setDescription("Click the button to refresh");
        builder.addField("This weeks folker", "REVERTED", false);
        return builder.build();
    }
    public static String getCurrentFolker() {
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
        settings.lastFolker = name;
        folkevognen.put(name, folkevognen.get(name) + 1);
        settings.write(true);
        return name;

    }
    public static List<LayoutComponent> getActionRows(Guild guild){
        return new ArrayList<>(){{
            try {
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
            } catch (Exception e) {
                logger.error("Failed to create select menu");
            }
            add(ActionRow.of(
                Button.primary("refresh-folkevognen", "Refresh"),
                Button.primary("show-folkevognen", "Show folkevognen"),
                Button.danger("revert-folkevognen", "Revert")
            ));
        }};
    }
}
