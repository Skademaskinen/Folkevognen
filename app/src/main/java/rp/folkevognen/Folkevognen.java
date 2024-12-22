package rp.folkevognen;

import java.util.Calendar;
import java.util.Map;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class Folkevognen {

    static MessageEmbed buildFolkevognEmbed(String current) {
        String image = App.jda.getSelfUser().getEffectiveAvatarUrl();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setThumbnail(image);
        builder.setTitle("Folkevognen");
        builder.setDescription("Click the button to refresh");
        builder.addField("This weeks folker", App.jda.retrieveUserById(current).complete().getAsMention(), false);
        return builder.build();
    }
    static MessageEmbed buildFolkevognEmbed() {
        String image = App.jda.getSelfUser().getEffectiveAvatarUrl();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setThumbnail(image);
        builder.setTitle("Folkevognen");
        builder.setDescription("Click the button to refresh");
        builder.addField("This weeks folker", "REVERTED", false);
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
