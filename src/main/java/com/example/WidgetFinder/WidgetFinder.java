package com.example.WidgetFinder;

import com.example.EthanApiPlugin.*;
import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.Collections.TileObjects;
import com.example.EthanApiPlugin.Collections.Widgets;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.PacketUtils.PacketUtilsPlugin;
import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
import com.google.common.primitives.Shorts;
import com.google.inject.Inject;
import net.runelite.api.*;
import net.runelite.api.annotations.Varbit;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GrandExchangeSearched;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetTextAlignment;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;

import java.sql.SQLOutput;
import java.util.*;
import java.util.stream.Collectors;

@PluginDescriptor(
        name = "Find Widget",
        description = "widget tested",
        enabledByDefault = false,
        tags = {"sal"}
)
@PluginDependency(PacketUtilsPlugin.class)
@PluginDependency(EthanApiPlugin.class)
public class WidgetFinder extends Plugin
{

    @Inject
    WidgetPackets widgetPackets;
    @Inject
    MousePackets mousePackets;
    @Inject
    EthanApiPlugin api;
    NPC nex;
    @Inject
    Client client;
    @Subscribe
    public void onGameTick(GameTick e)
    {

        Optional<Widget> f = Widgets.search().withAction("Use").nameContains("Special Attack").first();
        if (f.isPresent())
        {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetAction(f.get(), "Use");
            EthanApiPlugin.stopPlugin(this);
        }
        else
        {
            System.out.println("null");
        }
        /*
        MousePackets.queueClickPacket();
        WidgetPackets.queueWidgetAction(f, "Use");
        EthanApiPlugin.stopPlugin(this);

         */

    }


    @Subscribe
    public void onGrandExchangeSearched(GrandExchangeSearched event)
    {
    }

    public void updateGrandExchangeResults()
    {

    }

    @Subscribe
    public void onChatMessage(ChatMessage e)
    {

    }
}
