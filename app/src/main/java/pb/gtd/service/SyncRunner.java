package pb.gtd.service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import pb.gtd.Constants;
import pb.gtd.Util;
import pb.gtd.service.db.Database;
import android.util.Base64;
import android.util.Log;

public class SyncRunner implements Runnable {
	private GTDService service;

	public SyncRunner(GTDService s) {
		service = s;
	}

	@Override
	public void run() {
		try {
			Log.i("sync", "SyncRunner running");

			if (!service.isOnline()) {
				service.requestSync(Constants.RETRY_INTERVAL);
				return;
			}

			Heads localHeads;
			Heads remoteHeads;

			try {
				localHeads = new Heads(service.getDatabase().getHeads());
				remoteHeads = performPullRequest();
				if (remoteHeads != null) {
					performPushRequest(localHeads, remoteHeads);
				}
			} catch (Exception e) {
				service.onSyncError(e.getMessage());
			}

			localHeads = null;
			remoteHeads = null;

			service.requestSync(Constants.SYNC_INTERVAL);

			Log.i("sync", "SyncRunner finished");
		} catch (RuntimeException e) {
			Util.writeCrashReport(e);
		}
	}

	private Heads performPullRequest() throws Exception {
		Database db = service.getDatabase();
		String data = performRequest("pull", db.getHeads().toString());
        int end;

		if (data != null) {
            Heads remoteHeads;
            int off = 0;
			if( data.charAt(0) != '\n' ) {
                off = data.indexOf("\n\n");
                if( off == -1 ) {
                    return null;
                }
                remoteHeads = Heads.parse(data.substring(0, off + 1));
                off += 2;
            } else {
                remoteHeads = new Heads();
                off += 1;
            }

            while( off < data.length() ) {
                DataChunk dc = DataChunk.parse(data.substring(off));
                off += dc.parsedSize;

                db.insertData(dc);
            }

			return remoteHeads;
		} else {
			return null;
		}
	}

	private void performPushRequest(Heads local, Heads remote) throws Exception {
        Database database = service.getDatabase();
		String data = new String();

		int from, diff;
		for (short o : local.map.keySet()) {
			if (remote.map.containsKey(o)) {
				from = remote.map.get(o);
			} else {
				from = 0;
			}

			diff = local.map.get(o) - from;
			if (diff > 0) {
				data += database.selectData(o, from, diff).format();
			}
		}

        if (data.length() > 0) {
            performRequest("push", data);
        }
	}

	private String performRequest(String operation, String content)
			throws Exception {
		try {
			URL url = new URL(service.getServerLocation() + "?op=" + operation);

			Log.i("netio", "request " + url.toString() + " payload chars: "
					+ content.length());

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(5000 /* milliseconds */);
			conn.setConnectTimeout(10000 /* milliseconds */);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			String authToken = Base64.encodeToString((service.getUserName()
					+ ":" + service.getPassword()).getBytes(), Base64.NO_WRAP);
			// Log.i("netio", "auth token is " + authToken);
			conn.addRequestProperty("Authorization", "Basic " + authToken);

			if (content != null) {
				conn.setRequestMethod("POST");
				conn.setDoOutput(true);
				OutputStreamWriter osw = new OutputStreamWriter(
						conn.getOutputStream());
				osw.write(content);
				osw.close();
			}

			conn.connect();

			int status = conn.getResponseCode();
			Log.i("netio", "http response code " + status);
			if (status != 200) {
				throw new Exception("Unexpected response code " + status);
			}

			StringBuilder sb = new StringBuilder();

			InputStream is = conn.getInputStream();
			InputStreamReader isr = new InputStreamReader(is, "UTF-8");
			int n;
			char[] buf = new char[1024];
			while ((n = isr.read(buf)) != -1) {
				sb.append(buf, 0, n);
			}
			isr.close();

			Log.i("netio", "got " + sb.length() + " chars");
			return sb.toString();

		} catch (Exception e) {
			Log.e("netio", "problem: " + e.getClass().getSimpleName() + ": "
					+ e.getMessage());

			throw e;

		}
	}

}
