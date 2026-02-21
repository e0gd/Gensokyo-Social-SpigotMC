package com.eogd.gensokyo.command;

import com.eogd.gensokyo.GensokyoSocialPlugin;
import com.eogd.gensokyo.api.GensokyoAPI;
import com.eogd.gensokyo.data.Party;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PartyCommand implements CommandExecutor {

    private final GensokyoSocialPlugin plugin;
    // 邀请映射
    private final Map<UUID, UUID> partyInvites;

    public PartyCommand(GensokyoSocialPlugin plugin) {
        this.plugin = plugin;
        this.partyInvites = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以使用队伍指令！");
            return true;
        }

        Player player = (Player) sender;
        UUID playerUuid = player.getUniqueId();
        Party currentParty = GensokyoAPI.getPlayerParty(player);

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                if (currentParty != null) {
                    player.sendMessage("§c[系统] 你已经在一个队伍中了！请先退出当前队伍。");
                    return true;
                }
                Party newParty = new Party(playerUuid);
                plugin.getDataManager().getActiveParties().put(newParty.getPartyId(), newParty);
                player.sendMessage("§a[系统] 成功创建了队伍！你可以使用 /party invite <玩家名> 邀请他人。");
                break;

            case "invite":
                if (args.length < 2) {
                    player.sendMessage("§c用法: /party invite <玩家名>");
                    return true;
                }
                if (currentParty == null) {
                    player.sendMessage("§c[系统] 你还没有队伍！请先使用 /party create 创建队伍。");
                    return true;
                }
                if (!currentParty.getLeader().equals(playerUuid)) {
                    player.sendMessage("§c[系统] 只有队长可以邀请玩家！");
                    return true;
                }
                int maxSize = plugin.getConfig().getInt("settings.party-max-size", 3);
                if (currentParty.getMembers().size() >= maxSize) {
                    player.sendMessage("§c[系统] 队伍人数已达上限 (" + maxSize + "人)！");
                    return true;
                }
                Player targetToInvite = Bukkit.getPlayerExact(args[1]);
                if (targetToInvite == null || !targetToInvite.isOnline()) {
                    player.sendMessage("§c[系统] 找不到该玩家或玩家不在线！");
                    return true;
                }
                if (targetToInvite.getUniqueId().equals(playerUuid)) {
                    player.sendMessage("§c[系统] 你不能邀请你自己！");
                    return true;
                }
                if (GensokyoAPI.getPlayerParty(targetToInvite) != null) {
                    player.sendMessage("§c[系统] 该玩家已经在一个队伍中了！");
                    return true;
                }

                partyInvites.put(targetToInvite.getUniqueId(), playerUuid);
                player.sendMessage("§a[系统] 已向 " + targetToInvite.getName() + " 发送了队伍邀请。");
                targetToInvite.sendMessage("§e[系统] 玩家 §a" + player.getName() + " §e邀请你加入TA的队伍！");
                targetToInvite.sendMessage("§e[系统] 输入 §a/party accept §e来接受邀请。");
                break;

            case "accept":
                if (currentParty != null) {
                    player.sendMessage("§c[系统] 你已经在一个队伍中了！");
                    return true;
                }
                if (!partyInvites.containsKey(playerUuid)) {
                    player.sendMessage("§c[系统] 你没有收到任何待处理的队伍邀请！");
                    return true;
                }
                UUID leaderUuid = partyInvites.remove(playerUuid);
                Player leaderPlayer = Bukkit.getPlayer(leaderUuid);
                Party targetParty = null;
                if (leaderPlayer != null) {
                    targetParty = GensokyoAPI.getPlayerParty(leaderPlayer);
                }
                if (targetParty == null || !targetParty.getLeader().equals(leaderUuid)) {
                    player.sendMessage("§c[系统] 邀请已失效或队伍已解散！");
                    return true;
                }
                int checkMaxSize = plugin.getConfig().getInt("settings.party-max-size", 3);
                if (targetParty.getMembers().size() >= checkMaxSize) {
                    player.sendMessage("§c[系统] 该队伍人数已满！");
                    return true;
                }
                targetParty.addMember(playerUuid);
                targetParty.broadcastMessage("§a[队伍] 玩家 " + player.getName() + " 加入了队伍！");
                break;

            case "leave":
                if (currentParty == null) {
                    player.sendMessage("§c[系统] 你不在任何队伍中！");
                    return true;
                }
                currentParty.removeMember(playerUuid);
                player.sendMessage("§a[系统] 你已离开队伍。");
                if (currentParty.getMembers().isEmpty()) {
                    plugin.getDataManager().getActiveParties().remove(currentParty.getPartyId());
                } else if (currentParty.getLeader().equals(playerUuid)) {
                    // 移交队长
                    UUID newLeader = currentParty.getMembers().get(0);
                    currentParty.setLeader(newLeader);
                    Player newLeaderPlayer = Bukkit.getPlayer(newLeader);
                    currentParty.broadcastMessage("§e[队伍] 队长已离开，" + (newLeaderPlayer != null ? newLeaderPlayer.getName() : "新成员") + " 被任命为新队长！");
                } else {
                    currentParty.broadcastMessage("§e[队伍] 玩家 " + player.getName() + " 离开了队伍。");
                }
                break;

            case "kick":
                if (args.length < 2) {
                    player.sendMessage("§c用法: /party kick <玩家名>");
                    return true;
                }
                if (currentParty == null || !currentParty.getLeader().equals(playerUuid)) {
                    player.sendMessage("§c[系统] 只有队长可以踢人！");
                    return true;
                }
                Player targetToKick = Bukkit.getPlayerExact(args[1]);
                if (targetToKick == null) {
                    player.sendMessage("§c[系统] 找不到该玩家！");
                    return true;
                }
                if (targetToKick.getUniqueId().equals(playerUuid)) {
                    player.sendMessage("§c[系统] 你不能踢你自己！请使用 /party leave。");
                    return true;
                }
                if (!currentParty.getMembers().contains(targetToKick.getUniqueId())) {
                    player.sendMessage("§c[系统] 该玩家不在你的队伍中！");
                    return true;
                }
                currentParty.removeMember(targetToKick.getUniqueId());
                targetToKick.sendMessage("§c[系统] 你已被踢出队伍！");
                currentParty.broadcastMessage("§e[队伍] 玩家 " + targetToKick.getName() + " 被队长踢出了队伍。");
                break;

            case "list":
                if (currentParty == null) {
                    player.sendMessage("§c[系统] 你不在任何队伍中！");
                    return true;
                }
                player.sendMessage("§e=== 当前队伍成员 ===");
                for (UUID memberUuid : currentParty.getMembers()) {
                    String role = memberUuid.equals(currentParty.getLeader()) ? "§c[队长] " : "§a[队员] ";
                    Player memPlayer = Bukkit.getPlayer(memberUuid);
                    String name = (memPlayer != null) ? memPlayer.getName() : "离线玩家";
                    player.sendMessage(role + "§f" + name);
                }
                break;

            default:
                sendHelp(player);
                break;
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§e=== 队伍系统帮助 ===");
        player.sendMessage("§a/party create §7- 创建一个队伍");
        player.sendMessage("§a/party invite <玩家> §7- 邀请玩家加入");
        player.sendMessage("§a/party accept §7- 接受队伍邀请");
        player.sendMessage("§a/party leave §7- 离开当前队伍");
        player.sendMessage("§a/party kick <玩家> §7- 将玩家踢出队伍 (仅队长)");
        player.sendMessage("§a/party list §7- 查看队伍成员");
    }
}