package me.lucko.chatformatter;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.chat.Chat;


import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A super simple chat formatting plugin using Vault.
 */
public class ChatFormatterPlugin extends JavaPlugin implements Listener {
    protected static final Pattern URL_REGEX = Pattern.compile("(https?://)?[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z0-9]{1,10})((/+)[^/ ]*)*", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    // Format placeholders
    protected static final String NAME_PLACEHOLDER = "{name}";
    protected static final String PREFIX_PLACEHOLDER = "{prefix}";
    protected static final String SUFFIX_PLACEHOLDER = "{suffix}";

    // Format placeholder patterns
    protected static final Pattern NAME_PLACEHOLDER_PATTERN = Pattern.compile(NAME_PLACEHOLDER, Pattern.LITERAL);
    protected static final Pattern PREFIX_PLACEHOLDER_PATTERN = Pattern.compile(PREFIX_PLACEHOLDER, Pattern.LITERAL);
    protected static final Pattern SUFFIX_PLACEHOLDER_PATTERN = Pattern.compile(SUFFIX_PLACEHOLDER, Pattern.LITERAL);

    /** The default format */
    protected static final String DEFAULT_FORMAT = "<" + PREFIX_PLACEHOLDER + NAME_PLACEHOLDER + SUFFIX_PLACEHOLDER + "> ";

    /** The format used by this chat formatter instance */
    private String public_chat_format;

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
        reloadConfigValues();
        refreshVault();
        getServer().getPluginManager().registerEvents(asyncChat, this);
        getServer().getPluginManager().registerEvents(new AsyncChatDecorateListener(), this);
        getServer().getPluginManager().registerEvents(new AsyncChatCommandDecorateListener(), this);

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
          commands.registrar().register(
                  Commands.literal("vaultchatformatter")
                          .then(Commands.literal("reload")
                                  .requires(sender -> sender.getSender().isOp() || sender.getSender().hasPermission("vaultchatformatter.reload"))
                                  .executes(cmd -> {
                                      reloadConfig();
                                      reloadConfigValues();
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

    protected void reloadConfigValues() {
        this.public_chat_format = getConfig().getString("public-chat-format", DEFAULT_FORMAT);
    }

    protected  String getPublicChatFormat() {
        return public_chat_format;
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
    protected String replaceAll(Pattern pattern, String input, Supplier<String> replacement) {
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.replaceAll(Matcher.quoteReplacement(replacement.get()));
        }
        return input;
    }
}