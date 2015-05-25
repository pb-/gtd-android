package pb.gtd.service;

import pb.gtd.Constants;
import pb.gtd.Util;

public class DataChunk {
    public short origin;
    public int offset;
    public String data;
    public int parsedSize;

    public DataChunk(short o, int off, String d) {
        origin = o;
        offset = off;
        data = d;
    }

    public static DataChunk parse(String s) {
        int start = 0;
        int end;

        short origin = (short) Util.decodeNum(s.substring(start, start + Constants.ORIGIN_LEN), Constants.ORIGIN_LEN);
        start += Constants.ORIGIN_LEN + 1;

        end = s.indexOf(' ', start);
        int offset = Integer.parseInt(s.substring(start, end));
        start = end + 1;

        end = s.indexOf('\n', start);
        int len = Integer.parseInt(s.substring(start, end));
        start = end + 1;

        DataChunk dc = new DataChunk(origin, offset, s.substring(start, start + len));
        dc.parsedSize = start + len;
        return dc;
    }

    public String format() {
        String s = new String();
        return Util.encodeNum(origin, Constants.ORIGIN_LEN) + ' ' + offset + ' ' + data.length() + '\n' + data;
    }
}
