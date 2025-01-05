package org.nyanneko0113.discord_join.discord_commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import org.nyanneko0113.discord_join.manager.SettingManager;
import org.nyanneko0113.discord_join.util.DiscordUtil;

import java.awt.*;

public class Discord_SettingCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String cmd = event.getName();
        String sub_cmd = event.getSubcommandName();
        JDA jda = event.getJDA();
        MessageChannelUnion channel = event.getChannel();
        User user = event.getUser();
        CacheRestAction<PrivateChannel> private_channel = user.openPrivateChannel();
        Member member = event.getMember();

        if ("setting_discordjoin".equalsIgnoreCase(cmd)) {
            if (member.getPermissions().contains(Permission.BAN_MEMBERS)) {
                OptionMapping option_set = event.getOption("setting");
                OptionMapping option_set_arguments = event.getOption("setting_args");

                if (option_set != null) {
                    if (option_set_arguments != null) {
                        if (option_set.getAsString().equalsIgnoreCase("ban_role")) {
                            Role role = DiscordUtil.getJda().getRoleById(option_set_arguments.getAsString());
                            SettingManager.setBanRole(role);

                            EmbedBuilder embed = new EmbedBuilder();
                            embed.addField("成功", "Banロールを" + role.getName() + "に設定しました。", false);
                            embed.setColor(Color.GREEN);
                            event.deferReply(true).addEmbeds(embed.build()).queue();
                        }
                        else if (option_set.getAsString().equalsIgnoreCase("invite_url")) {
                            SettingManager.setInvite(option_set_arguments.getAsString());

                            EmbedBuilder embed = new EmbedBuilder();
                            embed.addField("成功", "招待URLを" + option_set_arguments.getAsString() + " に設定しました。", false);
                            embed.setColor(Color.GREEN);
                            event.deferReply(true).addEmbeds(embed.build()).queue();
                        }
                        else if (option_set.getAsString().equalsIgnoreCase("max_player")) {
                            SettingManager.setMaxPlayer(option_set_arguments.getAsInt());

                            EmbedBuilder embed = new EmbedBuilder();
                            embed.addField("成功",  option_set_arguments.getAsInt() + "に設定しました。", false);
                            embed.setColor(Color.GREEN);
                            event.deferReply(true).addEmbeds(embed.build()).queue();
                        }
                    }
                    else {
                        if (option_set.getAsString().equalsIgnoreCase("get")) {
                            EmbedBuilder embed = new EmbedBuilder();
                            embed.addField("Banロール", SettingManager.getBanRoleID(), false);
                            embed.addField("招待URL", SettingManager.getInvite(), false);
                            embed.addField("認証人数", String.valueOf(SettingManager.getMaxPlayer()), false);
                            embed.setColor(Color.GREEN);
                            event.deferReply().addEmbeds(embed.build()).queue();
                        }
                    }

                }
                else {
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle("引数を設定してください");
                    embed.addField("/setting_discordjoin ban_role <BanロールID>　：Banのロールを設定するコマンド", "使用例：/setting_discordjoin ban_role 0123456789", false);
                    embed.addField("/setting_discordjoin invite_url <URL>　：招待URLを設定するコマンド", "使用例：/setting_discordjoin invite_url https://discord.gg/", false);
                    embed.addField("/setting_discordjoin get　：設定内容を取得するコマンド", " ", false);
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
    }

}
