package pb.gtd.tests;

import android.test.InstrumentationTestCase;

import pb.gtd.Constants;
import pb.gtd.Util;
import pb.gtd.service.DataChunk;

public class DataChunkTest extends InstrumentationTestCase {
    public void testFormatParse() {
        DataChunk dc = new DataChunk((short) 500, 1234, "abc\n");

        String expected = Util.encodeNum(dc.origin, Constants.ORIGIN_LEN) + " 1234 4\nabc\n";
        assertEquals(expected, dc.format());

        DataChunk dce = DataChunk.parse(expected);
        assertEquals(dce.parsedSize, 14);
        assertEquals(dc.data, "abc\n");
        assertEquals(dc.offset, 1234);
        assertEquals(dc.origin, 500);
    }
}
