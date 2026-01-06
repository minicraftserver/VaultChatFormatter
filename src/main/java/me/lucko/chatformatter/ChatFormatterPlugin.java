package me.lucko.chatformatter;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.milkbowl.vault.chat.Chat;


import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * A super simple chat formatting plugin using Vault.
 */
public class ChatFormatterPlugin extends JavaPlugin implements Listener {

    /**
     * The current Vault chat implementation registered on the server.
     * Automatically updated as new services are registered.
     */
    private Chat vaultChat = null;
    private AsyncChatListener asyncChat;

    @Override
    public void onEnable() {
        asyncChat = new AsyncChatListener(this);
        saveDefaultConfig();
        asyncChat.reloadConfigValues();
        refreshVault();
        getServer().getPluginManager().registerEvents(asyncChat, this);
        getServer().getPluginManager().registerEvents(new AsyncChatDecorateListener(), this);

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
          commands.registrar().register(
                  Commands.literal("vaultchatformatter")
                          .then(Commands.literal("reload")
                                  .requires(sender -> sender.getSender().isOp() || sender.getSender().hasPermission("vaultchatformatter.reload"))
                                  .executes(cmd -> {
                                      reloadConfig();
                                      asyncChat.reloadConfigValues();
                                      cmd.getSource().getSender().sendPlainMessage("Reloaded successfully.");
                                      return Command.SINGLE_SUCCESS;
                                  })
                          ).build());
        });
    }

    private void refreshVault() {
        Chat vaultChat = getServer().getServicesManager().load(Chat.class);
        if (vaultChat != this.vaultChat) {
            getLogger().info("New Vault Chat implementation registered: " + (vaultChat == null ? "null" : vaultChat.getName()));
        }
        this.vaultChat = vaultChat;
    }

    @EventHandler
    public void onServiceChange(ServiceRegisterEvent e) {
        if (e.getProvider().getService() == Chat.class) {
            refreshVault();
        }
    }

    @EventHandler
    public void onServiceChange(ServiceUnregisterEvent e) {
        if (e.getProvider().getService() == Chat.class) {
            refreshVault();
        }
    }

    protected Chat getVaultChat(){
        return vaultChat;
    }

}