package pb.gtd;

public interface Constants {
	public final static int ORIGIN_LEN = 2;
	public final static int NUM_LEN = 3;

	public final static int SYNC_INTERVAL = 15 * 60 * 1000;
	public final static int RETRY_INTERVAL = 5 * 60 * 1000;
	public final static int SYNC_DELAY = 10 * 1000;

	public final static String ACTION_SYNCDONE = "syncdone";
	public final static String ACTION_SYNCERROR = "syncerror";

	public final static String SERVER_HOSTNAME = "your.host.name";
	public final static byte[] SERVER_CERT = new byte[] { };
}
