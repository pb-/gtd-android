package pb.gtd.command;

import pb.gtd.Constants;
import pb.gtd.Util;

public class AnnotatedCommand {
	public short origin;
	public int revision;
    public Command cmd;

	public AnnotatedCommand(short origin, int revision, Command cmd) {
		this.origin = origin;
        this.revision = revision;
        this.cmd = cmd;
	}

	public String toString() {
		return Util.encodeNum(origin, Constants.ORIGIN_LEN) + ' ' + revision
				+ ' ' + cmd.toString();
	}

	public static AnnotatedCommand parse(String s) {
		short origin = (short) Util.decodeNum(s, Constants.ORIGIN_LEN);
		int start = Constants.ORIGIN_LEN + 2;
		int pos = s.indexOf(' ', start);
		int revision = Integer.parseInt(s.substring(start - 1, pos));
		EncryptedCommand c = EncryptedCommand.parse(s.substring(pos + 1));

		return new AnnotatedCommand(origin, revision, c);
	}
}
