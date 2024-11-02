package me.lucko.chatformatter;

import net.milkbowl.vault.chat.Chat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
        getServer().getPluginManager().registerEvents(new AsyncChatDecorateListener(this), this);
    }

    private void refreshVault() {
        Chat vaultChat = getServer().getServicesManager().load(Chat.class);
        if (vaultChat != this.vaultChat) {
            getLogger().info("New Vault Chat implementation registered: " + (vaultChat == null ? "null" : vaultChat.getName()));
        }
        this.vaultChat = vaultChat;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 0 && args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            asyncChat.reloadConfigValues();

            sender.sendMessage("Reloaded successfully.");
            return true;
        }

        return false;
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
