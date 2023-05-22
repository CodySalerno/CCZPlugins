package com.example.Skills;

import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.EthanApiPlugin.Collections.TileObjects;
import com.example.InteractionApi.InventoryInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.PacketUtils.PacketUtilsPlugin;
import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
import com.google.inject.Inject;
import lombok.SneakyThrows;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.Optional;

@PluginDescriptor(
        name = "Woodcutter",
        description = "Woodcuts until 36 for lost city",
        enabledByDefault = false,
        tags = {"sal"}
)
@PluginDependency(PacketUtilsPlugin.class)
@PluginDependency(EthanApiPlugin.class)
public class WoodcuttingPlugin extends Plugin
{

    @Inject
    Client client;
    int timeout = 0;
    @Override
    @SneakyThrows
    public void startUp()
    {
        timeout = 0;
    }

    @Subscribe
    public void onGameTick(GameTick e)
    {
        if (timeout > 0)
        {
            timeout--;
        }
        if (timeout <= 0)
        {
            woodCut();
        }
    }

    public void woodCut()
    {
        if (client.getRealSkillLevel(Skill.WOODCUTTING) < 15)
        {
            if (client.getLocalPlayer().getAnimation() == 877 || client.getLocalPlayer().getAnimation() == 875 || client.getLocalPlayer().getAnimation() == 873)
            {
                System.out.println("Animating wait to chop next");
                timeout = 2;
                return;
            }
            else
            {
                for (int i = 0; i <4; i++)
                {
                    if (Inventory.getItemAmount(1511) > 0)
                    {
                        System.out.println("Dropping logs");
                        InventoryInteraction.useItem(1511, "Drop");
                    }
                }
                Optional<TileObject> tree = TileObjects.search().withName("Tree").nearestToPlayer();
                if (tree.get() != null)
                {
                    System.out.println("Cut trees " + tree.get().getWorldLocation());
                    TileObjectInteraction.interact(tree.get(), "Chop down");
                    timeout = 5;
                }
            }
        }
        else if (client.getRealSkillLevel(Skill.WOODCUTTING) <= 35)
        {
            Optional<TileObject> tree = TileObjects.search().withName("Oak").nearestToPlayer();
            Optional<Widget> guam = Inventory.search().withId(ItemID.GUAM_LEAF).first();
            Optional<Widget> tar = Inventory.search().withId(ItemID.SWAMP_TAR).first();
            Optional<Widget> pestle = Inventory.search().withId(ItemID.PESTLE_AND_MORTAR).first();
            if (guam.isEmpty() || tar.isEmpty() || pestle.isEmpty())
            {
                EthanApiPlugin.stopPlugin(this);
            }
            if (tree.get() != null)
            {
                if (Inventory.getItemAmount(1521) > 0)
                {
                    InventoryInteraction.useItem(1521, "Drop");
                }
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetOnWidget(guam.get(), tar.get());
                System.out.println("Cut trees " + tree.get().getWorldLocation() + " " + client.getTickCount());
                TileObjectInteraction.interact(tree.get(), "Chop down");
                timeout = 3;
            }
        }
        if (client.getRealSkillLevel(Skill.WOODCUTTING) >= 36)
        {
            EthanApiPlugin.stopPlugin(this);
        }
    }
}
