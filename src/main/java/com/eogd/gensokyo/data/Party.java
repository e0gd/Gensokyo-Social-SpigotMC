package com.eogd.gensokyo.data;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Party {

    private final UUID partyId;
    private UUID leader;
    private final List<UUID> members;

    public Party(UUID leader) {
        this.partyId = UUID.randomUUID();
        this.leader = leader;
        this.members = new ArrayList<>();
        this.members.add(leader);
    }

    public UUID getPartyId() {
        return partyId;
    }

    public UUID getLeader() {
        return leader;
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
    }

    public List<UUID> getMembers() {
        return members;
    }

    public void addMember(UUID member) {
        if (!members.contains(member)) {
            members.add(member);
        }
    }

    public void removeMember(UUID member) {
        members.remove(member);
    }

    public void broadcastMessage(String message) {
        for (UUID memberUuid : members) {
            Player player = Bukkit.getPlayer(memberUuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(message);
            }
        }
    }
}