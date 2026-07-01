package me.marioogg.mlogin.spigot.util;

import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntitySoundEffect;
import com.github.retrooper.packetevents.PacketEvents;
import org.bukkit.entity.Player;

public class SoundUtil {

    public static void playConfirmSound(Player player) {
        WrapperPlayServerEntitySoundEffect packet = new WrapperPlayServerEntitySoundEffect(
                Sounds.ENTITY_EXPERIENCE_ORB_PICKUP,
                SoundCategory.MASTER,
                player.getEntityId(),
                1.0f,
                1.0f
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }
}