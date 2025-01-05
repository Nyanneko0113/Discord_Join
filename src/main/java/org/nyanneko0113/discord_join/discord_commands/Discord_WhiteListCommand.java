package org.nyanneko0113.discord_join.discord_commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;
import org.nyanneko0113.discord_join.Main;
import org.nyanneko0113.discord_join.manager.VerifyManager;
import org.nyanneko0113.discord_join.manager.WhiteListManager;
import org.nyanneko0113.discord_join.util.DiscordUtil;

import java.awt.*;
import java.io.IOException;

public class Discord_WhiteListCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String cmd = event.getName();
        String sub_cmd = event.getSubcommandName();
        JDA jda = event.getJDA();
        MessageChannelUnion channel = event.getChannel();
        User user = event.getUser();
        CacheRestAction<PrivateChannel> private_channel = user.openPrivateChannel();
        Member member = event.getMember();

        if ("server-join".equalsIgnoreCase(cmd)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    OptionMapping option = event.getOption("code");

                    if (option != null) {
                        try {
                            int verify = VerifyManager.removeVerify(option.getAsInt(), user);

                            if (verify == 0) {
                                EmbedBuilder embed = new EmbedBuilder();
                                embed.addField("成功", "認証に成功しました!サーバーに参加することができます。", false);
                                embed.setColor(Color.GREEN);
                                event.deferReply(true).addEmbeds(embed.build()).queue();
                            }
                            else if (verify == 1){
                                EmbedBuilder embed = new EmbedBuilder();
                                embed.addField("失敗", "認証に失敗しました。登録数が上限です。", false);
                                embed.setColor(Color.RED);
                                event.deferReply(true).addEmbeds(embed.build()).queue();
                            }
                            else if (verify == 2) {
                                EmbedBuilder embed = new EmbedBuilder();
                                embed.addField("失敗", "認証に失敗しました。番号が違います。", false);
                                embed.setColor(Color.RED);
                                event.deferReply(true).addEmbeds(embed.build()).queue();
                            }
                            else if (verify == 3) {
                                EmbedBuilder embed = new EmbedBuilder();
                                embed.addField("失敗", "認証に失敗しました。", false);
                                embed.setColor(Color.RED);
                                event.deferReply(true).addEmbeds(embed.build()).queue();
                            }
                        }
                        catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        catch (NumberFormatException e) {
                            EmbedBuilder embed = new EmbedBuilder();
                            embed.addField("失敗", "数字以外のものを入力されています。", false);
                            embed.setColor(Color.RED);
                            event.deferReply(true).addEmbeds(embed.build()).queue();
                        }
                    }
                    else {
                        try {
                            EmbedBuilder embed = new EmbedBuilder();
                            embed.addField("失敗", "コードを入力してください。", false);
                            embed.setColor(Color.RED);
                            event.deferReply(true).addEmbeds(embed.build()).queue();
                        }
                        catch (NumberFormatException e) {
                            EmbedBuilder embed = new EmbedBuilder();
                            embed.addField("失敗", "数字以外のものを入力されています。", false);
                            embed.setColor(Color.RED);
                            event.deferReply(true).addEmbeds(embed.build()).queue();
                        }

                    }
                }
            }.runTask(Main.getInstance());
        }
        else if ("whitelist-add".contains(cmd)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Member member = event.getMember();
                    if (member.getPermissions().contains(Permission.ADMINISTRATOR)) {
                        OptionMapping mcid_option = event.getOption("mcid");
                        OptionMapping user_option = event.getOption("user");

                        if (mcid_option != null || user_option != null) {
                            OfflinePlayer player = Bukkit.getOfflinePlayer(mcid_option.getAsString());

                            try {
                                WhiteListManager.addPlayer(player.getName(), DiscordUtil.getJda().getUserById(user_option.getAsString()));

                                Bukkit.getLogger().info( "[DiscordJoin]" + player.getName() + "をホワイトリストに追加しました（運営による追加）");
                                EmbedBuilder embed = new EmbedBuilder();
                                embed.addField("成功", player.getName() + "をホワイトリストに追加しました", false);
                                embed.setColor(Color.GREEN);
                                event.deferReply(true).addEmbeds(embed.build()).queue();
                            }
                            catch (Exception e) {
                                EmbedBuilder embed = new EmbedBuilder();
                                embed.addField("エラーが発生しました", e.toString(), false);
                                embed.setColor(Color.RED);
                                event.deferReply().addEmbeds(embed.build()).queue();
                            }
                        }
                        else {
                            EmbedBuilder embed = new EmbedBuilder();
                            embed.addField("失敗", "名前を入力してください。", false);
                            embed.setColor(Color.RED);
                            event.deferReply(true).addEmbeds(embed.build()).queue();
                        }
                    }
                    else {
                        EmbedBuilder embed = new EmbedBuilder();
                        embed.addField("エラー", "権限がないためこのコマンドは実行できません。", false);
                        embed.setColor(Color.RED);
                        event.deferReply(true).addEmbeds(embed.build()).queue();
                    }
                }
            }.runTask(Main.getInstance());
        }
        else if ("whitelist-remove".equalsIgnoreCase(cmd)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (member.getPermissions().contains(Permission.BAN_MEMBERS)) {
                        OptionMapping option = event.getOption("mcid");

                        if (option != null) {
                            OfflinePlayer player = Bukkit.getOfflinePlayer(option.getAsString());
                            try {
                                WhiteListManager.removePlayer(player.getName());

                                Bukkit.getLogger().info( "[DiscordJoin]" + player.getName() + "をワイトリストから削除しました");
                                EmbedBuilder embed = new EmbedBuilder();
                                embed.addField("成功", player.getName() + "をホワイトリストから削除しました", false);
                                embed.setColor(Color.GREEN);
                                event.deferReply(true).addEmbeds(embed.build()).queue();
                            }
                            catch (Exception e) {
                                EmbedBuilder embed = new EmbedBuilder();
                                embed.addField("エラーが発生しました", e.toString(), false);
                                embed.setColor(Color.RED);
                                event.deferReply().addEmbeds(embed.build()).queue();
                            }
                        }
                        else {
                            EmbedBuilder embed = new EmbedBuilder();
                            embed.addField("失敗", "名前を入力してください。", false);
                            embed.setColor(Color.RED);
                            event.deferReply(true).addEmbeds(embed.build()).queue();
                        }
                    }
                    else {
                        EmbedBuilder embed = new EmbedBuilder();
                        embed.addField("エラー", "権限がないためこのコマンドは実行できません。", false);
                        embed.setColor(Color.RED);
                        event.deferReply(true).addEmbeds(embed.build()).queue();
                    }
                }
            }.runTask(Main.getInstance());
        }
        else if ("list".equalsIgnoreCase(cmd)) {
            EmbedBuilder embed = new EmbedBuilder();
            String list = StringUtils.join(WhiteListManager.getPlayers(), ",");
            embed.addField("参加できるユーザー", list.replaceFirst(",", ""), false);
            embed.setColor(Color.GREEN);
            event.deferReply(true).addEmbeds(embed.build()).queue();
        }
    }
}
