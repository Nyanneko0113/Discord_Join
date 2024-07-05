package org.nyanneko0113.discord_join.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.nyanneko0113.discord_join.Main;
import org.nyanneko0113.discord_join.manager.VerifyManager;
import org.nyanneko0113.discord_join.manager.WhiteListManager;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.stream.Collectors;

public class DiscordUtil extends ListenerAdapter implements EventListener {

        public static JDA jda;
        private static final String token = Main.getInstance().getConfig().getString("token");

        public static void startBot() throws LoginException, InterruptedException {
            if (jda == null) {
                jda = JDABuilder.createDefault(token)
                        .addEventListeners(new DiscordUtil())
                        .build();
                jda.awaitReady();

                CommandListUpdateAction commands = jda.updateCommands();
                commands.addCommands(Commands.slash("server-join", "サーバーに参加するコマンド")
                                .addOption(OptionType.STRING, "code", "認証コード", false))
                        .addCommands(Commands.slash("whitelist-add", "ホワイトリストを追加するコマンド")
                                .addOption(OptionType.STRING, "mcid", "マイクラのID", false))
                        .addCommands(Commands.slash("whitelist-remove", "ホワイトリストから削除するコマンド")
                                .addOption(OptionType.STRING, "mcid", "マイクラのID", false))
                        .addCommands(Commands.slash("info", "このBOTの情報"))
                        .addCommands(Commands.slash("help", "ヘルプコマンド"))
                        .addCommands(Commands.slash("list", "参加できるユーザー"))
                        .queue();

                Bukkit.getLogger().info("[DiscordUtil] [情報] ボットが起動しました。");
                Bukkit.getLogger().info("[DiscordUtil] [情報] 利用可能なコマンド：" + commands.complete().stream().map(Command::getName).collect(Collectors.toSet()));
            }
        }

        public static void stopBot() {
            if (jda != null) {
                jda.shutdownNow();
            }
            else {
                Bukkit.getLogger().info("[DiscordUtil] [エラー] ボットが起動されていないためシャットダウンできません。");
            }
        }

        @Override
        public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
            String cmd = event.getName();
            String sub_cmd = event.getSubcommandName();
            JDA jda = event.getJDA();
            MessageChannelUnion channel = event.getChannel();
            User user = event.getUser();
            CacheRestAction<PrivateChannel> private_channel = user.openPrivateChannel();

            if ("server-join".equalsIgnoreCase(cmd)) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        OptionMapping option = event.getOption("code");

                        if (option != null) {
                            try {
                                boolean verify = VerifyManager.removeVerify(option.getAsInt());

                                if (verify) {
                                    EmbedBuilder embed = new EmbedBuilder();
                                    embed.addField("成功", "認証に成功しました!サーバーに参加することができます。", false);
                                    embed.setColor(Color.GREEN);
                                    event.deferReply(true).addEmbeds(embed.build()).queue();
                                }
                                else {
                                    EmbedBuilder embed = new EmbedBuilder();
                                    embed.addField("失敗", "認証に失敗しました", false);
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
                            OptionMapping option = event.getOption("mcid");

                            if (option != null) {
                                OfflinePlayer player = Bukkit.getOfflinePlayer(option.getAsString());

                                try {
                                    WhiteListManager.addPlayer(player.getName());

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
                        Member member = event.getMember();
                        if (member.getPermissions().contains(Permission.ADMINISTRATOR)) {
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
            else if ("info".equalsIgnoreCase(cmd)) {
                EmbedBuilder embed = new EmbedBuilder();
                embed.addField("バージョン", "0.0.2", false);
                embed.addField("制作者", "blockgrass", false);
                embed.setColor(Color.GREEN);
                event.deferReply().addEmbeds(embed.build()).queue();
            }
            else if ("list".equalsIgnoreCase(cmd)) {
                EmbedBuilder embed = new EmbedBuilder();
                String list = StringUtils.join(WhiteListManager.getPlayers(), ",");
                embed.addField("参加できるユーザー", list.replaceFirst(",", ""), false);
                embed.setColor(Color.GREEN);
                event.deferReply(true).addEmbeds(embed.build()).queue();
            }
            else if ("help".equalsIgnoreCase(cmd)) {
                EmbedBuilder embed = new EmbedBuilder();
                embed.addField("/server-join <mcid>", "サーバーに参加するコマンド", false);
                embed.setColor(Color.GREEN);
                event.deferReply().addEmbeds(embed.build()).queue();
            }
        }
}
