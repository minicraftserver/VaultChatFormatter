package me.lucko.chatformatter;

import io.papermc.paper.event.player.AsyncChatDecorateEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.regex.Pattern;

public class AsyncChatDecorateListener implements Listener {
    private ChatFormatterPlugin plugin;
    private final Pattern URL_REGEX = Pattern.compile("(https?://)?[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z0-9]{1,10})((/+)[^/ ]*)*", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    public AsyncChatDecorateListener(ChatFormatterPlugin chatFormatterPlugin){
        plugin = chatFormatterPlugin;
    }

    @EventHandler
    public void onAsyncChatDecorateEvent(AsyncChatDecorateEvent e){
        Component message = e.originalMessage();

        if(e.player().hasPermission("vaultchatformatter.minimessageformat") || e.player().isOp()) {
            String plainText = PlainTextComponentSerializer.plainText().serialize(e.originalMessage());
            if(plainText.contains("<") && plainText.contains(">"))
                message = MiniMessage.miniMessage().deserialize(plainText);
        }
        if(e.player().hasPermission("vaultchatformatter.clickablelinks") || e.player().isOp()){
            message = message.replaceText(TextReplacementConfig
                    .builder()
                    .match(URL_REGEX)
                    .replacement((c) -> c.clickEvent(ClickEvent.openUrl(c.content().startsWith("http") ? c.content() : "https://" + c.content())))
                    .build());
        }

        e.result(message);
    }
}
