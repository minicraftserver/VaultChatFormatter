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

    public AsyncChatListener(ChatFormatterPlugin chatFormatterPlugin){
        plugin = chatFormatterPlugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onAsyncChat(AsyncChatEvent e){
        e.renderer((source, sourceDisplayName, message, viewer) -> Component.text()
                .append(formatRank(e.getPlayer()))
                .append(message) // object must be used otherwise the message isn't signed
                .build());
    }

    private Component formatRank(Player player) {
        String messageFormat = plugin.getPublicChatFormat();

        if (plugin.getVaultChat() != null) {
            messageFormat = plugin.replaceAll(ChatFormatterPlugin.PREFIX_PLACEHOLDER_PATTERN, messageFormat, () -> plugin.getVaultChat().getPlayerPrefix(player));
            messageFormat = plugin.replaceAll(ChatFormatterPlugin.SUFFIX_PLACEHOLDER_PATTERN, messageFormat, () -> plugin.getVaultChat().getPlayerSuffix(player));
        }
        messageFormat = plugin.replaceAll(ChatFormatterPlugin.NAME_PLACEHOLDER_PATTERN, messageFormat, player::getName);

        return MiniMessage.miniMessage().deserialize(messageFormat);
    }
}