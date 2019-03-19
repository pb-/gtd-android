package pb.gtd;

public interface Constants {
    int ORIGIN_LEN = 2;
    int NUM_LEN = 3;

    int SYNC_INTERVAL = 15 * 60 * 1000;
    int RETRY_INTERVAL = 5 * 60 * 1000;
    int SYNC_DELAY = 10 * 1000;

    String ACTION_SYNCDONE = "syncdone";
    String ACTION_SYNCERROR = "syncerror";
}
