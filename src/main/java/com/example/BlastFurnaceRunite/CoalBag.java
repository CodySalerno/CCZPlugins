package com.example.BlastFurnaceRunite;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CoalBag
{
    private static final int UNKNOWN_AMOUNT = -1;
    private static final int EMPTY_AMOUNT = 0;

    private static final Pattern BAG_EMPTY_MESSAGE = Pattern.compile("^The coal bag is (?:now\\s)?empty\\.");
    private static final Pattern BAG_ONE_OR_MANY_MESSAGE = Pattern.compile("^The coal bag (?:still\\s)?contains ([\\d]+|one) pieces? of coal\\.");

    private static int storedAmount;

    private static void setAmount(int amount)
    {
        storedAmount = amount;
    }

    private static void setEmptyAmount()
    {
        storedAmount = EMPTY_AMOUNT;
    }

    public static void setUnknownAmount()
    {
        storedAmount = UNKNOWN_AMOUNT;
    }

    public static String getAmount()
    {
        return String.valueOf(storedAmount);
    }

    public static int updateAmount(String message)
    {
        final Matcher emptyMatcher = BAG_EMPTY_MESSAGE.matcher(message);
        if (emptyMatcher.matches())
        {
            return 0;
        }
        else
        {
            final Matcher oneOrManyMatcher = BAG_ONE_OR_MANY_MESSAGE.matcher(message);
            if (oneOrManyMatcher.matches())
            {
                final String match = oneOrManyMatcher.group(1);
                if (match.equals("one"))
                {
                    return 1;
                } else
                {
                    return  Integer.parseInt(match);
                }
            }
        }
        return 0;
    }

    public static boolean isUnknown()
    {
        return storedAmount == UNKNOWN_AMOUNT;
    }

    public static boolean isEmpty()
    {
        return storedAmount == EMPTY_AMOUNT;
    }
}
