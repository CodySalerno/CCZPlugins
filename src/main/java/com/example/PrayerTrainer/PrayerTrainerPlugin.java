package com.example.PrayerTrainer;

import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.EthanApiPlugin.Inventory;
import com.example.EthanApiPlugin.NPCs;
import com.example.EthanApiPlugin.TileObjects;
import com.example.InteractionApi.InventoryInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.PacketUtils.PacketUtilsPlugin;
import com.example.Packets.MousePackets;
import com.example.Packets.NPCPackets;
import com.example.Packets.ObjectPackets;
import com.example.Packets.WidgetPackets;
import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.*;
import net.runelite.client.util.HotkeyListener;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

@PluginDescriptor(
        name = "Prayer Trainer",
        description = "Wildy Prayer Trainer",
        enabledByDefault = false,
        tags = {"sal"}
)
@Slf4j
@PluginDependency(PacketUtilsPlugin.class)
@PluginDependency(EthanApiPlugin.class)
public class PrayerTrainerPlugin extends Plugin
{
    TileObject altar;
    int timeout = 0;
    Widget dbones;
    Widget dbonesNoted;
    @Inject
    Client client;
    int previousLevel = -1;
    Widget logout;
    NPC mage;

    @Override
    @SneakyThrows
    public void startUp()
    {
        timeout = 0;
    }

    @Subscribe
    public void onGameTick(GameTick e)
    {
        logout = client.getWidget(182,8);
        altar = TileObjects.search().nameContains("altar").first().orElse(null);
        dbones = Inventory.search().withId(536).first().orElse(null);
        dbonesNoted = Inventory.search().withId(537).first().orElse(null);
        mage = NPCs.search().withId(7995).first().orElse(null);
        timeout--;
        if (timeout <= 0)
        {
            if (dbones == null)
            {
                System.out.println("null bones");
            }
            if (dbones != null)
            {
                if (altar != null)
                {
                    MousePackets.queueClickPacket();
                    ObjectPackets.queueWidgetOnTileObject(dbones, altar);
                }
            }
            Widget npcDialogOptions = client.getWidget(219, 1);
            if (dbones == null && altar != null && dbonesNoted != null && npcDialogOptions == null)
            {
                MousePackets.queueClickPacket();
                NPCPackets.queueWidgetOnNPC(mage, dbonesNoted);
                timeout = 9;
                //EthanApiPlugin.stopPlugin(this);
            }
            if (npcDialogOptions != null)
            {
                System.out.println("exchanging all");
                MousePackets.queueClickPacket();
                WidgetPackets.queueResumePause(14352385, 3);
                return;
            }
        }
        logout();
    }

    private void logout()
    {
        Widget wildernessLevel = client.getWidget(WidgetInfo.PVP_WILDERNESS_LEVEL);
        int level = -1;
        if (wildernessLevel != null && !wildernessLevel.getText().equals("")) {
            try {
                if (wildernessLevel.getText().contains("<br>")) {
                    String text = wildernessLevel.getText().split("<br>")[0];
                    level = Integer.parseInt(text.replaceAll("Level: ", ""));
                } else {
                    level = Integer.parseInt(wildernessLevel.getText().replaceAll("Level: ", ""));
                }
            } catch (NumberFormatException ex) {
                //ignore
            }
        }
        if (previousLevel != -1 && level == -1)
        {
            previousLevel = -1;
        }
        for (Player player : client.getPlayers()) {
            int lowRange = client.getLocalPlayer().getCombatLevel() - level;
            int highRange = client.getLocalPlayer().getCombatLevel() + level;
            //System.out.println("high: " + highRange + " low: " + lowRange);
            if (player.equals(client.getLocalPlayer()))
            {
                continue;
            }
            if (player.getCombatLevel() >= lowRange && player.getCombatLevel() <= highRange)
            {
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetAction(logout,"Logout");
                return;
            }
        }
    }
}
