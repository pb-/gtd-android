package pb.gtd;

import java.util.HashMap;
import java.util.Map;

public class Heads {
	public HashMap<Short, Integer> map;

	public Heads() {
		map = new HashMap<Short, Integer>();
	}

	public Heads(Heads heads) {
		map = new HashMap<Short, Integer>(heads.map);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (Map.Entry<Short, Integer> entry : map.entrySet()) {
			sb.append(Util.encodeNum(entry.getKey(), Constants.ORIGIN_LEN));
			sb.append(' ');
			sb.append(entry.getValue());
			sb.append('\n');
		}

		return sb.toString();
	}

	public static Heads parse(String s) {
		Heads heads = new Heads();
		int off = 0;

		while (off < s.length()) {
			short origin = (short) Util.decodeNum(s.substring(off),
					Constants.ORIGIN_LEN);
			int newline = s.indexOf('\n', off);
			int rev = Integer.parseInt(s.substring(off + Constants.ORIGIN_LEN
					+ 1, newline));
			heads.map.put(origin, rev);

			off = newline + 1;
		}

		return heads;
	}
}
