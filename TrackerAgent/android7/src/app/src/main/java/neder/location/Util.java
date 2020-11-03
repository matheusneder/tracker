package neder.location;

import java.util.Random;

public class Util {
    private static int sequenceForPackageIds = 0;
    private static Random randomGeneratorForPackageIds = new Random();

    public static String generateSequentialUniqueId() {
        String time = String.format("%016X", System.currentTimeMillis());
        String seq  = String.format("%08X", sequenceForPackageIds++);
        String rand = String.format("%08X", randomGeneratorForPackageIds.nextInt());
        return time + "-" + seq + "-" + rand;
    }
}
