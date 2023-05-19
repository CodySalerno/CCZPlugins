package HelperFunctions;

import net.runelite.api.Client;
import net.runelite.api.Skill;

public class GetLevel
{
    public static int levelfinder(Skill skill, Client client)
    {
        int level = client.getRealSkillLevel(skill);
        System.out.println("You are level " + level);
        return level;
    }

}
