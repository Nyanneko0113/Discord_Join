package org.nyanneko0113.discord_join.commands;

import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.ban.BanListType;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.BanEntry;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.ban.ProfileBanList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nyanneko0113.discord_join.Main;
import org.nyanneko0113.discord_join.manager.SettingManager;
import org.nyanneko0113.discord_join.manager.WhiteListManager;
import org.nyanneko0113.discord_join.util.DiscordUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DiscordCommand implements CommandExecutor , TabCompleter {

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
                            WhiteListManager.addPlayer(args[1], DiscordUtil.getJda().getUserById(args[1]));
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
            else if (args[0].equalsIgnoreCase("ban")) {
                @NotNull ProfileBanList ban_list = Bukkit.getBanList(BanListType.PROFILE);
                if (args.length == 1) {
                    send.sendMessage(Main.TEXT_ERROR + "名前を入力してください。（例：/discord_whitelist ban <GuildID> <名前> <理由（指定しない場合は「none.」）>）");
                }
                else {
                    @Nullable OfflinePlayer player = Bukkit.getOfflinePlayer(args[2]);
                    User user = WhiteListManager.getUser(args[2]);
                    @Nullable BanEntry<PlayerProfile> ban_entry = ban_list.addBan(player.getPlayerProfile(), args.length == 3 ? "none" : args[3], (Date) null, null);
                    ban_entry.save();
                    DiscordUtil.getJda().getGuildById(args[1]).addRoleToMember(user, DiscordUtil.getJda().getRoleById(SettingManager.getBanRoleID())).queue();
                    send.sendMessage(Main.TEXT_INFO + args[2] + "をBanし、ロールを付与しました。");
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
