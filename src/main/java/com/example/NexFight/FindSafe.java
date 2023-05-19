package com.example.NexFight;

import net.runelite.api.Client;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class FindSafe
{
    public static Optional<WorldPoint> FindSafe(List<WorldPoint> nex, WorldPoint client, WorldArea notSafe, WorldArea safe, int change, int y)
    {
        Stream<WorldPoint> safePoints;
        safePoints =  nex.stream().filter(x ->
                (x.getX() <= client.getX()+change && x.getX() >= client.getX()-change)
                        && (x.getY() <= client.getY()+y && x.getY() >= client.getY()-y)
                        && !notSafe.contains(x) && safe.contains(x));
        return safePoints.min(Comparator.comparingInt(x -> x.distanceTo(client)));
    }

    public static WorldPoint FindSafe(List<WorldPoint> safePoints, WorldPoint client)
    {
        return safePoints.stream().min(Comparator.comparingInt(x -> x.distanceTo(client))).orElse(null);
    }
}
