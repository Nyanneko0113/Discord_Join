package org.nyanneko0113.discord_join.discord_commands;

import io.papermc.paper.ban.BanListType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.ban.ProfileBanList;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nyanneko0113.discord_join.Main;
import org.nyanneko0113.discord_join.manager.SettingManager;
import org.nyanneko0113.discord_join.manager.VerifyManager;
import org.nyanneko0113.discord_join.manager.WhiteListManager;
import org.nyanneko0113.discord_join.util.DiscordUtil;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Discord_Command extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String cmd = event.getName();
        String sub_cmd = event.getSubcommandName();
        JDA jda = event.getJDA();
        MessageChannelUnion channel = event.getChannel();
        User user = event.getUser();
        CacheRestAction<PrivateChannel> private_channel = user.openPrivateChannel();
        Member member = event.getMember();

        if ("ban".equalsIgnoreCase(cmd)) {
            OptionMapping option_player = event.getOption("mcid");
            OptionMapping option_reason = event.getOption("ban_reason");
            OfflinePlayer player = Bukkit.getOfflinePlayer(option_player.getAsString());
            @NotNull ProfileBanList ban_list = Bukkit.getServer().getBanList(BanListType.PROFILE);

            if (member.getPermissions().contains(Permission.BAN_MEMBERS)) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (!WhiteListManager.getUsers(player.getName()).isEmpty()) {

                                if (option_reason != null) {
                                    ban_list.addBan(player.getPlayerProfile(), option_reason.getAsString(), (Date) null, null).save();
                                }
                                else {
                                    ban_list.addBan(player.getPlayerProfile(), "banned.", (Date) null, null).save();
                                }

                                if (player.isOnline()) ((Player)player).kickPlayer("あなたはBanされました。");
                                User user_player = WhiteListManager.getUser(player.getName());

                                if (SettingManager.getBanRoleID() != null) {
                                    event.getGuild().ban(user_player, 0, TimeUnit.DAYS).queue();

                                    EmbedBuilder embed = new EmbedBuilder();
                                    embed.addField("成功", player.getName() + "をBanし、ロールを追加しました。", false);
                                    embed.setColor(Color.GREEN);
                                    event.deferReply(true).addEmbeds(embed.build()).queue();
                                }
                                else {
                                    EmbedBuilder embed = new EmbedBuilder();
                                    embed.addField("一部成功", player.getName() + "をBanしましたが、ロールが設定されていないためロールを追加できませんでした。", false);
                                    embed.setColor(Color.YELLOW);
                                    event.deferReply(true).addEmbeds(embed.build()).queue();
                                }
                            }
                            else {
                                EmbedBuilder embed = new EmbedBuilder();
                                embed.addField("エラー", "認証していないユーザーのためBanできませんでした。", false);
                                embed.setColor(Color.RED);
                                event.deferReply(true).addEmbeds(embed.build()).queue();
                            }

                        }
                    }.runTask(Main.getInstance());
            }
            else {
                EmbedBuilder embed = new EmbedBuilder();
                embed.addField("エラー", "権限がないためこのコマンドは実行できません。", false);
                embed.setColor(Color.RED);
                event.deferReply(true).addEmbeds(embed.build()).queue();
            }
        }
        else if ("userinfo".equalsIgnoreCase(cmd)) {
            if (member.getPermissions().contains(Permission.BAN_MEMBERS)) {
                OptionMapping option_player = event.getOption("mcid");

                User discord_user = WhiteListManager.getUser(option_player.getAsString());
                List<Guild.Ban> discord_banlist = event.getGuild().retrieveBanList().stream().toList();
                boolean mc_banlist = Bukkit.getBanList(BanListType.PROFILE).isBanned(Bukkit.getOfflinePlayer(option_player.getAsString()).getPlayerProfile());

                boolean discord_check = discord_banlist.stream().anyMatch(a -> a.getUser().getName().equalsIgnoreCase(discord_user.getName()));
                EmbedBuilder embed = new EmbedBuilder();
                embed.addField("このプレイヤーはDiscordBanされてるか？", discord_check ? "はい" : "いいえ", false);
                embed.addField("このプレイヤーはMinecraftBanされてるか？", mc_banlist ? "はい" : "いいえ", false);
                embed.setColor(Color.GREEN);
                event.deferReply().addEmbeds(embed.build()).queue();
            }
            else {
                EmbedBuilder embed = new EmbedBuilder();
                embed.addField("エラー", "権限がないためこのコマンドは実行できません。", false);
                embed.setColor(Color.RED);
                event.deferReply(true).addEmbeds(embed.build()).queue();
            }
        }
        else if ("find_name".equalsIgnoreCase(cmd)) {
            if (member.getPermissions().contains(Permission.BAN_MEMBERS)) {
                OptionMapping option_type = event.getOption("type");
                OptionMapping option_name = event.getOption("name");

                if (option_type.getAsString().equalsIgnoreCase("discord")) {
                    User user_name = DiscordUtil.getJda().retrieveUserById(option_name.getAsString()).complete();
                    Set<OfflinePlayer> players = WhiteListManager.getPlayers(user_name);
                    Set<String> players_name = players.stream().map(OfflinePlayer::getName).collect(Collectors.toSet());
                    String list = StringUtils.join(players_name, ",");

                    EmbedBuilder embed = new EmbedBuilder();
                    embed.addField("このユーザー（Discord:" + user_name.getName() + ")のMinecraftアカウント", list, false);
                    embed.setColor(Color.GREEN);
                    event.deferReply().addEmbeds(embed.build()).queue();
                }
                else if (option_type.getAsString().equalsIgnoreCase("mc")) {
                    Set<User> users = WhiteListManager.getUsers(option_name.getAsString());
                    Set<String> users_name = users.stream().map(User::getName).collect(Collectors.toSet());

                    String list = StringUtils.join(users_name, ",");

                    EmbedBuilder embed = new EmbedBuilder();
                    embed.addField("このプレイヤー（Minecraft:" + option_name.getAsString() + ")のDiscordアカウント", list, false);
                    embed.setColor(Color.GREEN);
                    event.deferReply().addEmbeds(embed.build()).queue();
                }
            }
            else {
                EmbedBuilder embed = new EmbedBuilder();
                embed.addField("エラー", "権限がないためこのコマンドは実行できません。", false);
                embed.setColor(Color.RED);
                event.deferReply(true).addEmbeds(embed.build()).queue();
            }
        }
        else if ("info".equalsIgnoreCase(cmd)) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.addField("バージョン", "0.0.2（2024/8/3）", false);
            embed.addField("制作者", "blockgrass", false);
            embed.setColor(Color.GREEN);
            event.deferReply().addEmbeds(embed.build()).queue();
        }
        else if ("help".equalsIgnoreCase(cmd)) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.addField("/server-join <mcid>", "サーバーに参加するコマンド", false);
            embed.setColor(Color.GREEN);
            event.deferReply().addEmbeds(embed.build()).queue();
        }
    }
}
