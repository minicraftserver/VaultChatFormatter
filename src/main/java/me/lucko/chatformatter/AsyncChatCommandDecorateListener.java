package me.lucko.chatformatter;

import io.papermc.paper.event.player.AsyncChatCommandDecorateEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.ARGBLike;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class AsyncChatCommandDecorateListener implements Listener {
    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(priority = EventPriority.NORMAL)
    public void onAsyncChatCommandDecorate(AsyncChatCommandDecorateEvent e){
        Component message = e.originalMessage();

        if(e.player().hasPermission("vaultchatformatter.clickablelinks") || e.player().isOp()){
            message = message.replaceText(TextReplacementConfig
                    .builder()
                    .match(ChatFormatterPlugin.URL_REGEX)
                    .replacement((c) -> c.clickEvent(ClickEvent.openUrl(c.content().startsWith("http") ? c.content() : "https://" + c.content())))
                    .build());
        }

        message = message.color(TextColor.fromHexString("#ededed"));

        e.result(message);
    }

}
