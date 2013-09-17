package pb.gtd;

public class AnnotatedCommand extends Command {
	public short origin;
	public int revision;

	public AnnotatedCommand(short origin, int revision, Command c) {
		this(origin, revision, c.op, c.argument);
	}

	public AnnotatedCommand(short origin, int revision, char command,
			String arguments) {
		super(command, arguments);

		this.origin = origin;
		this.revision = revision;
	}

	public String toString() {
		return Util.encodeNum(origin, Constants.ORIGIN_LEN) + ' ' + revision
				+ ' ' + super.toString();
	}

	public String toStringCommand() {
		return super.toString();
	}

	public static AnnotatedCommand parse(String s) {
		short origin = (short) Util.decodeNum(s, Constants.ORIGIN_LEN);
		int start = Constants.ORIGIN_LEN + 2;
		int pos = s.indexOf(' ', start);
		int revision = Integer.parseInt(s.substring(start - 1, pos));
		Command c = Command.parse(s.substring(pos + 1));

		return new AnnotatedCommand(origin, revision, c.op, c.argument);
	}
}
