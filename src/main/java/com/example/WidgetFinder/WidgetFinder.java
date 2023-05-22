package com.example.WidgetFinder;

import com.example.EthanApiPlugin.*;
import com.example.EthanApiPlugin.Collections.TileObjects;
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
        System.out.println(client.getLocalPlayer().getAnimation());

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
