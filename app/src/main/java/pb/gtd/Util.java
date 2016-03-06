package pb.gtd;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Random;

public class Util {
    public final static String alpha =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static int decodeNum(String num, int len) {
        int n = 0;
        int m = 1;

        for (int i = 0; i < len; i++) {
            n += alpha.indexOf(num.charAt(i)) * m;
            m *= alpha.length();
        }

        return n;
    }

    public static String encodeNum(int num, int len) {
        char[] s = new char[len];

        for (int i = 0; i < len; i++) {
            s[i] = alpha.charAt(num % alpha.length());
            num /= alpha.length();
        }

        return new String(s);
    }

    public static int genNum(int len) {
        Random r = new Random();
        return r.nextInt((int) Math.pow(alpha.length(), len));
    }

    public static byte[] packIV(long timestamp, byte[] random) {
        byte[] iv = new byte[8];

        long seconds = timestamp / 1000;
        short msec = (short) (timestamp % 1000);

        iv[0] = (byte) (seconds >> 24);
        iv[1] = (byte) (seconds >> 16);
        iv[2] = (byte) (seconds >> 8);
        iv[3] = (byte) seconds;
        iv[4] = (byte) (msec >> 2);
        iv[5] = (byte) (((msec & 3) << 6) | (random[0] & 0x3f));
        iv[6] = random[1];
        iv[7] = (byte) (random[2] & 0xf0); // we use only 60 bits for the iv.

        return iv;
    }

    public static long unpackIVTimestamp(byte[] packed) {
        long seconds = (long) (packed[0] & 0xff) << 24 | (packed[1] & 0xff) << 16 |
                (packed[2] & 0xff) << 8 | (packed[3] & 0xff);
        short msec = (short) ((packed[4] & 0xff) << 2 | (packed[5] & 0xff) >> 6);

        return seconds * 1000 + msec;
    }

    public static byte[] unpackIVRandom(byte[] packed) {
        return new byte[]{(byte) (packed[5] & 0x3f), packed[6], packed[7]};
    }

    public static void writeCrashReport(RuntimeException re) {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            String now = new Date().toString();
            File f = null;

            f = new File(
                    Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "GTD Crash " + now + ".txt");

            Log.d("sync", "crash file in " + f.getAbsolutePath());
            try {
                OutputStreamWriter osw = new OutputStreamWriter(
                        new FileOutputStream(f));

                re.printStackTrace(new PrintWriter(osw));
                if (re.getCause() != null) {
                    re.getCause().printStackTrace(new PrintWriter(osw));
                }

                osw.close();

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        throw re;
    }
}
