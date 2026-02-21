package com.eogd.gensokyo.command;

import com.eogd.gensokyo.GensokyoSocialPlugin;
import com.eogd.gensokyo.data.Mail;
import com.eogd.gensokyo.data.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MailCommand implements CommandExecutor {

    private final GensokyoSocialPlugin plugin;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public MailCommand(GensokyoSocialPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以使用邮件指令！");
            return true;
        }

        Player player = (Player) sender;
        PlayerData myData = plugin.getDataManager().getCachedData(player.getUniqueId());

        if (args.length == 0) {
            player.sendMessage("§e=== 邮箱系统帮助 ===");
            player.sendMessage("§a/mail read §7- 读取所有未读邮件");
            player.sendMessage("§a/mail clear §7- 清空所有邮件");
            return true;
        }

        if (args[0].equalsIgnoreCase("read")) {
            List<Mail> mails = myData.getMails();
            if (mails.isEmpty()) {
                player.sendMessage("§e[系统] 你的邮箱空空如也。");
                return true;
            }

            player.sendMessage("§e=== 你的信箱 (" + mails.size() + " 封) ===");
            for (Mail mail : mails) {
                String timeStr = sdf.format(new Date(mail.getTimestamp()));
                player.sendMessage("§8[" + timeStr + "] §f来自 §a" + mail.getSender() + "§f:");
                player.sendMessage("§7" + mail.getContent());
            }
            player.sendMessage("§e===============================");
            player.sendMessage("§7阅读完毕后，可输入 §c/mail clear §7清空邮箱。");
            return true;
        }

        if (args[0].equalsIgnoreCase("clear")) {
            myData.clearMails();
            player.sendMessage("§a[系统] 邮箱已清空！");
            return true;
        }

        player.sendMessage("§c未知指令，请使用 /mail 查看帮助。");
        return true;
    }
}