package me.lucko.chatformatter;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AsyncChatListener implements Listener {
    private ChatFormatterPlugin plugin;

    // Format placeholders
    private static final String NAME_PLACEHOLDER = "{name}";
    private static final String PREFIX_PLACEHOLDER = "{prefix}";
    private static final String SUFFIX_PLACEHOLDER = "{suffix}";

    // Format placeholder patterns
    private static final Pattern NAME_PLACEHOLDER_PATTERN = Pattern.compile(NAME_PLACEHOLDER, Pattern.LITERAL);
    private static final Pattern PREFIX_PLACEHOLDER_PATTERN = Pattern.compile(PREFIX_PLACEHOLDER, Pattern.LITERAL);
    private static final Pattern SUFFIX_PLACEHOLDER_PATTERN = Pattern.compile(SUFFIX_PLACEHOLDER, Pattern.LITERAL);

    /** The default format */
    private static final String DEFAULT_FORMAT = "<" + PREFIX_PLACEHOLDER + NAME_PLACEHOLDER + SUFFIX_PLACEHOLDER + "> ";

    /** The format used by this chat formatter instance */
    private String format;

    public AsyncChatListener(ChatFormatterPlugin chatFormatterPlugin){
        plugin = chatFormatterPlugin;
    }

    protected void reloadConfigValues() {
        this.format = plugin.getConfig().getString("format", DEFAULT_FORMAT);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onAsyncChat(AsyncChatEvent e){
        e.renderer((source, sourceDisplayName, message, viewer) -> Component.text()
                .append(formatRank(e.getPlayer()))
                .append(message) // object must be used otherwise the message isn't signed
                .build());
    }

    private Component formatRank(Player player) {
        String messageFormat = this.format;

        if (plugin.getVaultChat() != null) {
            messageFormat = replaceAll(PREFIX_PLACEHOLDER_PATTERN, messageFormat, () -> plugin.getVaultChat().getPlayerPrefix(player));
            messageFormat = replaceAll(SUFFIX_PLACEHOLDER_PATTERN, messageFormat, () -> plugin.getVaultChat().getPlayerSuffix(player));
        }
        messageFormat = replaceAll(NAME_PLACEHOLDER_PATTERN, messageFormat, player::getName);

        return MiniMessage.miniMessage().deserialize(messageFormat);
    }

    /**
     * Equivalent to {@link String#replace(CharSequence, CharSequence)}, but uses a
     * {@link Supplier} for the replacement.
     *
     * @param pattern the pattern for the replacement target
     * @param input the input string
     * @param replacement the replacement
     * @return the input string with the replacements applied
     */
    private static String replaceAll(Pattern pattern, String input, Supplier<String> replacement) {
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.replaceAll(Matcher.quoteReplacement(replacement.get()));
        }
        return input;
    }
}