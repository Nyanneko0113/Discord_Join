package org.nyanneko0113.discord_join;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEvent;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.nyanneko0113.discord_join.events.PlayerJoinListener;
import org.nyanneko0113.discord_join.manager.WhiteListManager;
import org.nyanneko0113.discord_join.util.DiscordUtil;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class Main extends JavaPlugin implements CommandExecutor, TabCompleter {

    private static final String TEXT_INFO = ChatColor.AQUA + "[DiscordJoin] " + ChatColor.RESET;
    private static final String TEXT_ERROR = ChatColor.RED + "[エラー] " + ChatColor.RESET;

    @Override
    public void onEnable() {

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);

        registerCommands();
        saveDefaultConfig();
        try {
            DiscordUtil.startBot();
        } catch (LoginException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        getLogger().info("プラグインが起動しました");
    }

    private void registerCommands() {
        getCommand("discord_whitelist").setExecutor(this);
    }

    @Override
    public void onDisable() {
        DiscordUtil.stopBot();
    }

    public static Main getInstance() {
        return getPlugin(Main.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender send, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            send.sendMessage(Main.TEXT_INFO + "以下のコマンドが利用可能です。" + "\n" +
                    "/discord_whitelist add <mcid>：ホワイトリストを追加するコマンド" + "\n" +
                    "/discord_whitelist remove <mcid>：ホワイトリストを削除するコマンド" + "\n" +
                    "/discord_whitelist list：ホワイトリストのリストを表示するコマンド");
        }
        else {
            if (args[0].equalsIgnoreCase("add")) {
                try {
                    if (args.length == 1) {
                        send.sendMessage(Main.TEXT_ERROR + "名前を入力してください。（例：/discord_whitelist add Blockgrass）");
                    }
                    else {
                        if (!WhiteListManager.getPlayers().contains(args[1])) {
                            WhiteListManager.addPlayer(args[1]);
                            send.sendMessage(Main.TEXT_INFO + args[1] + "をホワイトリストから追加しました");
                        }
                        else {
                            send.sendMessage(Main.TEXT_ERROR + "そのプレイヤーは既に登録されています。");
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else if (args[0].equalsIgnoreCase("remove")) {
                try {
                    if (args.length == 1) {
                        send.sendMessage(Main.TEXT_ERROR + "名前を入力してください。（例：/discord_whitelist remove Blockgrass）");
                    }
                    else {
                        if (WhiteListManager.getPlayers().contains(args[1])) {
                            WhiteListManager.removePlayer(args[1]);
                            send.sendMessage(Main.TEXT_INFO + args[1] + "をホワイトリストから削除しました");
                        }
                        else {
                            send.sendMessage(Main.TEXT_ERROR + "そのプレイヤーは登録されていません。");
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else if (args[0].equalsIgnoreCase("list")) {
                if (WhiteListManager.getPlayers().isEmpty()) {
                    send.sendMessage(Main.TEXT_INFO + "参加できるプレイヤーはいません。");
                }
                else {
                    String list = StringUtils.join(WhiteListManager.getPlayers(), ",");
                    if (list.startsWith(",")) {
                        list = list.replaceFirst(",", "");
                    }
                    send.sendMessage(Main.TEXT_INFO + "参加できるプレイヤー：" + list);
                }
            }
            else if (args[0].equalsIgnoreCase("json_read")) {
                try {
                    send.sendMessage(Main.TEXT_INFO + "JSONの状態：" + WhiteListManager.getJson().toString());
                }
                catch (Exception exception) {
                    send.sendMessage(Main.TEXT_ERROR + "エラーが発生しました：" + exception);
                }

            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if(cmd.getName().equalsIgnoreCase("discord_whitelist")){
            if (args.length == 1) {
                if (args[0].isEmpty()) {
                    return Arrays.asList("add","remove", "list");
                } else {
                    if ("add".startsWith(args[0]) && "remove".startsWith(args[0]) && "list".startsWith(args[0])) {
                        return Arrays.asList("add","remove", "list");
                    }
                    else if("add".startsWith(args[0])){
                        return Collections.singletonList("add");
                    }
                    else if("remove".startsWith(args[0])){
                        return Collections.singletonList("remove");
                    }
                    else if("list".startsWith(args[0])){
                        return Collections.singletonList("list");
                    }
                }
            }
        }
        return null;
    }

}
