package org.nyanneko0113.discord_join.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.nyanneko0113.discord_join.Main;

import javax.security.auth.login.LoginException;
import java.awt.*;

public class DiscordUtil extends ListenerAdapter implements EventListener {

        public static JDA jda;
        private static final String token = Main.getInstance().getConfig().getString("token");

        public static void startBot() throws LoginException, InterruptedException {
            if (jda == null) {
                jda = JDABuilder.createDefault(token)
                        .addEventListeners(new DiscordUtil())
                        .build();
                jda.awaitReady();

                jda.updateCommands()
                        .addCommands(Commands.slash("server-join", "サーバーに参加するコマンド")
                                .addOption(OptionType.STRING, "mcid", "マイクラのID", false))
                        .addCommands(Commands.slash("whitelist-remove", "ホワイトリストから削除するコマンド")
                                .addOption(OptionType.STRING, "mcid", "マイクラのID", false))
                        .addCommands(Commands.slash("info", "このBOTの情報"))
                        .queue();

                Bukkit.getLogger().info("[DiscordUtil] [情報] ボットが起動しました。");
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

            if ("server-join".equalsIgnoreCase(cmd)) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        OptionMapping option = event.getOption("mcid");

                        if (option != null) {
                            OfflinePlayer player = Bukkit.getOfflinePlayer(option.getAsString());

                            player.setWhitelisted(true);
                            Bukkit.reloadWhitelist();
                            player.hasPlayedBefore();

                            Bukkit.getLogger().info(player.getName() + "をワイトリストに追加しました");
                            EmbedBuilder embed = new EmbedBuilder();
                            embed.addField("成功", player.getName() + "をホワイトリストに追加しました", false);
                            embed.setColor(Color.GREEN);
                            channel.sendMessageEmbeds(embed.build()).queue();
                        } else {
                            EmbedBuilder embed = new EmbedBuilder();
                            embed.addField("失敗", "名前を入力してください。", false);
                            embed.setColor(Color.RED);
                            channel.sendMessageEmbeds(embed.build()).queue();
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

                            OfflinePlayer player = Bukkit.getOfflinePlayer(option.getAsString());
                            player.setWhitelisted(false);
                            EmbedBuilder embed = new EmbedBuilder();
                            embed.addField("成功", player.getName() + "をホワイトリストから削除しました", false);
                            embed.setColor(Color.GREEN);
                            channel.sendMessageEmbeds(embed.build()).queue();
                        } else {
                            EmbedBuilder embed = new EmbedBuilder();
                            embed.addField("エラー", "権限がないためこのコマンドは実行できません。", false);
                            embed.setColor(Color.RED);
                            channel.sendMessageEmbeds(embed.build()).queue();
                        }
                    }
                }.runTask(Main.getInstance());
            }
            else if ("info".equalsIgnoreCase(cmd)) {
                EmbedBuilder embed = new EmbedBuilder();
                embed.addField("バージョン", "0.0.1", false);
                embed.addField("制作者", "blockgrass", false);
                embed.setColor(Color.GREEN);
                channel.sendMessageEmbeds(embed.build()).queue();
            }
        }
}
