package pb.gtd.tests;

import android.test.InstrumentationTestCase;
import android.test.MoreAsserts;

import java.security.SecureRandom;

import pb.gtd.Util;

public class IVPackingTest extends InstrumentationTestCase {
    public void testPackUnpackRandom() {
        SecureRandom sr = new SecureRandom();
        for (int i = 0; i < 10000; i++) {
            long timestamp = System.currentTimeMillis();
            byte[] random = new byte[3];
            sr.nextBytes(random);
            random[0] &= 0x3f;
            random[2] &= 0xf0;

            byte[] packed = Util.packIV(timestamp, random);
            assertEquals(timestamp, Util.unpackIVTimestamp(packed));
            MoreAsserts.assertEquals(random, Util.unpackIVRandom(packed));
        }
    }

    public void testPackUnpackFull() {
        long timestamp = 0xffffffffL * 1000 + 0x03e7;
        byte[] random = new byte[]{0x3f, (byte) 0xff, (byte) 0xf0};

        byte[] packed = Util.packIV(timestamp, random);
        assertEquals(timestamp, Util.unpackIVTimestamp(packed));
        MoreAsserts.assertEquals(random, Util.unpackIVRandom(packed));
    }

    public void testPackUnpackEdges() {
        long timestamp = 0x80000001L * 1000 + 0x0201;
        byte[] random = new byte[]{0x20, 0x00, 0x10};

        byte[] packed = Util.packIV(timestamp, random);
        assertEquals(timestamp, Util.unpackIVTimestamp(packed));
        MoreAsserts.assertEquals(random, Util.unpackIVRandom(packed));
    }

    public void testPackUnpackEdgesInverse() {
        long timestamp = 0x7ffffffeL * 1000 + 0x01fe;
        byte[] random = new byte[]{0x1f, (byte) 0xff, (byte) 0xe0};

        byte[] packed = Util.packIV(timestamp, random);
        assertEquals(timestamp, Util.unpackIVTimestamp(packed));
        MoreAsserts.assertEquals(random, Util.unpackIVRandom(packed));
    }
}
