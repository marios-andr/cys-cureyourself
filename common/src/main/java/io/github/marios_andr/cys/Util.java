package io.github.marios_andr.cys;

public class Util {
    private Util() {}

    public static int calculateTotalExperience(int experienceLevels, float experienceProgress) {
        int exp = 0;
        if (experienceLevels <= 16) {
            exp += experienceLevels * experienceLevels + 6 * experienceLevels;
        } else if (experienceLevels <= 31) {
            exp += (int) (2.5 * experienceLevels * experienceLevels - 40.5 * experienceLevels + 360);
        } else {
            exp += (int) (4.5 * experienceLevels * experienceLevels - 162.5 * experienceLevels + 2220);
        }
        return (int)(exp + experienceProgress * getXpNeededForNextLevel(experienceLevels));
    }

    private static int test(int experienceLevel) {
        if (experienceLevel <= 15)
            return experienceLevel * 2 + 7;
        else if (experienceLevel <= 30)
            return 5 * experienceLevel - 38;
        else
            return 9 * experienceLevel - 158;
    }

    private static int getXpNeededForNextLevel(int experienceLevel) {
        if (experienceLevel >= 30) {
            return 112 + (experienceLevel - 30) * 9;
        } else {
            return experienceLevel >= 15 ? 37 + (experienceLevel - 15) * 5 : 7 + experienceLevel * 2;
        }
    }
}
