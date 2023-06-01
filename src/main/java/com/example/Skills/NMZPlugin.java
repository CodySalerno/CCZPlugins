package com.example.Skills;

import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.Collections.NPCs;
import com.example.EthanApiPlugin.Collections.TileObjects;
import com.example.EthanApiPlugin.Collections.Widgets;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.InventoryInteraction;
import com.example.InteractionApi.NPCInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.PacketUtils.PacketUtilsPlugin;
import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
import com.google.inject.Inject;
import lombok.SneakyThrows;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;

import javax.print.DocFlavor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@PluginDescriptor(
        name = "NMZ",
        description = "NMZ prayer flicker logs out when dream ends.",
        enabledByDefault = false,
        tags = {"sal"}
)
@PluginDependency(PacketUtilsPlugin.class)
@PluginDependency(EthanApiPlugin.class)
public class NMZPlugin extends Plugin
{

    NPC dominic;
    @Inject
    Client client;
    int timeout = 0;
    List<Integer> npcs = new ArrayList<Integer>();

    @Override
    @SneakyThrows
    public void startUp()
    {
        //hard mode mobs (dh)
        npcs.add(6328);
        npcs.add(6332);
        npcs.add(6326);
        npcs.add(6319);
        npcs.add(6320);
        //normal mobs
        npcs.add(7895);
        npcs.add(6380);
        npcs.add(6383);
        npcs.add(8528);
        npcs.add(6393);

        timeout = 0;
    }


    @Subscribe
    public void onGameTick(GameTick e)
    {
        dominic = NPCs.search().withId(1120).first().orElse(null);
        Optional<Widget> Overload4 = Inventory.search().withId(ItemID.OVERLOAD_4).first();
        Optional<Widget> Overload3 = Inventory.search().withId(ItemID.OVERLOAD_3).first();
        Optional<Widget> Overload2 = Inventory.search().withId(ItemID.OVERLOAD_2).first();
        Optional<Widget> Overload1 = Inventory.search().withId(ItemID.OVERLOAD_1).first();
        Optional<Widget> superCombat4 = Inventory.search().withId(ItemID.DIVINE_SUPER_COMBAT_POTION4).first();
        Optional<Widget> superCombat3 = Inventory.search().withId(ItemID.DIVINE_SUPER_COMBAT_POTION3).first();
        Optional<Widget> superCombat2 = Inventory.search().withId(ItemID.DIVINE_SUPER_COMBAT_POTION2).first();
        Optional<Widget> superCombat1 = Inventory.search().withId(ItemID.DIVINE_SUPER_COMBAT_POTION1).first();
        Optional<Widget> rockCake = Inventory.search().withId(ItemID.DWARVEN_ROCK_CAKE_7510).first();
        Optional<TileObject> specOrb = TileObjects.search().withId(26264).first();
        Optional<NPC> closest = NPCs.search().idInList(npcs).nearestToPlayer();

        Widget logout = client.getWidget(182,8);
        if (dominic != null)
        {
            System.out.println("logging out");
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetAction(logout,"Logout");
            EthanApiPlugin.stopPlugin(this);
        }
        if (client.getVarbitValue(Varbits.QUICK_PRAYER) == 1)
        {
            togglePrayer();
        }
        togglePrayer();
        if (client.getBoostedSkillLevel(Skill.HITPOINTS) > 57)
        {
            if (rockCake.isPresent())
            {
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetAction(rockCake.get(), "Guzzle");
            }
            return;
        }
        if (client.getBoostedSkillLevel(Skill.HITPOINTS) > 51)
        {
            if (rockCake.isPresent())
            {
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetAction(rockCake.get(), "Eat");
            }
            return;
        }
        if (client.getRealSkillLevel(Skill.ATTACK) >= client.getBoostedSkillLevel(Skill.ATTACK) && client.getBoostedSkillLevel(Skill.HITPOINTS) == 51)
        {
            if (Overload1.isPresent())
            {
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetAction(Overload1.get(), "Drink");
            }
            else if (Overload2.isPresent())
            {
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetAction(Overload2.get(), "Drink");
            }
            else if (Overload3.isPresent())
            {
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetAction(Overload3.get(), "Drink");
            }
            else if (Overload4.isPresent())
            {
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetAction(Overload4.get(), "Drink");
            }
            else if (superCombat1.isPresent())
            {
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetAction(superCombat1.get(), "Drink");
            }
            else if (superCombat2.isPresent())
            {
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetAction(superCombat2.get(), "Drink");
            }
            else if (superCombat3.isPresent())
            {
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetAction(superCombat3.get(), "Drink");
            }
            else if (superCombat4.isPresent())
            {
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetAction(superCombat4.get(), "Drink");
            }
        }

        if (timeout > 0)
        {
            timeout--;
        }
        if (timeout == 0)
        {
            if (specOrb.isPresent())
            {
                MousePackets.queueClickPacket();
                TileObjectInteraction.interact(specOrb.get(), "Activate");
                timeout = 5;
                return;
            }
            if (client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT) >= 500)
            {
                if (Inventory.getItemAmount(ItemID.GRANITE_MAUL_24225) > 0)
                {
                    InventoryInteraction.useItem(ItemID.GRANITE_MAUL_24225, "Wield");
                }
                Optional<Widget> f = Widgets.search().withAction("Use").nameContains("Special Attack").first();
                if (client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT) >= 1000)
                {
                    if (f.isPresent())
                    {
                        MousePackets.queueClickPacket();
                        WidgetPackets.queueWidgetAction(f.get(), "Use");
                    }
                }
                if (f.isPresent())
                {
                    MousePackets.queueClickPacket();
                    WidgetPackets.queueWidgetAction(f.get(), "Use");
                }
                NPCInteraction.interact(closest.get(), "Attack");
                timeout = 1;
                return;
            }
            if (client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT) < 500)
            {
                if (Inventory.getItemAmount(ItemID.DRAGON_SCIMITAR) > 0)
                {
                    InventoryInteraction.useItem(ItemID.DRAGON_SCIMITAR, "Wield");
                }
                if (Inventory.getItemAmount(ItemID.DHAROKS_GREATAXE) > 0)
                {
                    InventoryInteraction.useItem(ItemID.DHAROKS_GREATAXE, "Wield");
                }
                else if (Inventory.getItemAmount(ItemID.DHAROKS_GREATAXE_0) > 0)
                {
                    InventoryInteraction.useItem(ItemID.DHAROKS_GREATAXE_0, "Wield");
                }
                else if (Inventory.getItemAmount(ItemID.DHAROKS_GREATAXE_25) > 0)
                {
                    InventoryInteraction.useItem(ItemID.DHAROKS_GREATAXE_25, "Wield");
                }
                else if (Inventory.getItemAmount(ItemID.DHAROKS_GREATAXE_50) > 0)
                {
                    InventoryInteraction.useItem(ItemID.DHAROKS_GREATAXE_50, "Wield");
                }
                else if (Inventory.getItemAmount(ItemID.DHAROKS_GREATAXE_75) > 0)
                {
                    InventoryInteraction.useItem(ItemID.DHAROKS_GREATAXE_75, "Wield");
                }
                else if (Inventory.getItemAmount(ItemID.DHAROKS_GREATAXE_100) > 0)
                {
                    InventoryInteraction.useItem(ItemID.DHAROKS_GREATAXE_100, "Wield");
                }
                else if (Inventory.getItemAmount(ItemID.DHAROKS_GREATAXE_25516) > 0)
                {
                    InventoryInteraction.useItem(ItemID.DHAROKS_GREATAXE_25516, "Wield");
                }
                NPCInteraction.interact(closest.get(), "Attack");
                timeout = 1;
            }
        }


    }

    public void togglePrayer()
    {
        MousePackets.queueClickPacket();
        WidgetPackets.queueWidgetActionPacket(1, WidgetInfo.MINIMAP_QUICK_PRAYER_ORB.getPackedId(), -1, -1);
    }
}
