package pb.gtd.service;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

import pb.gtd.Constants;
import pb.gtd.Util;
import pb.gtd.service.db.Database;

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
                remoteHeads = performPullRequest(localHeads);
                performPushRequest(localHeads, remoteHeads);
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

    private Heads performPullRequest(Heads localHeads) throws Exception {
        JSONObject offs = new JSONObject();
        for (short origin : localHeads.map.keySet()) {
            offs.put(Util.encodeNum(origin, Constants.ORIGIN_LEN), localHeads.map.get(origin));
        }
        JSONObject input = new JSONObject();
        input.put("offs", offs);

        JSONObject json = performRequest("pull", input);

        Heads remoteHeads = new Heads(json.getJSONObject("offs"));
        JSONObject data = json.getJSONObject("data");
        Iterator<String> origins = data.keys();

        while (origins.hasNext()) {
            String origin = origins.next();
            JSONArray value = data.getJSONArray(origin);
            int offset = value.getInt(0);
            String content = value.getString(1);

            DataChunk dc = new DataChunk((short) Util.decodeNum(origin, Constants.ORIGIN_LEN),
                    offset, content);
            service.getDatabase().insertData(dc);
        }

        return remoteHeads;
    }

    private void performPushRequest(Heads local, Heads remote) throws Exception {
        Database database = service.getDatabase();
        JSONObject data = new JSONObject();

        int from, diff;
        for (short o : local.map.keySet()) {
            if (remote.map.containsKey(o)) {
                from = remote.map.get(o);
            } else {
                from = 0;
            }

            diff = local.map.get(o) - from;
            if (diff > 0) {
                DataChunk dc = database.selectData(o, from, diff);
                JSONArray array = new JSONArray();
                array.put(dc.offset);
                array.put(dc.data);
                data.put(Util.encodeNum(o, Constants.ORIGIN_LEN), array);
            }
        }

        if (data.length() > 0) {
            JSONObject json = new JSONObject();
            json.put("data", data);
            performRequest("push", json);
        }
    }

    private JSONObject performRequest(String operation, JSONObject content)
            throws Exception {
        try {
            URL url = new URL("https://" + service.getSyncHost() + ":9002/gtd/" + service
                    .getSyncToken() + '/' + operation);

            Log.i("netio", "request " + url.toString() + " payload chars: "
                    + content.toString().length());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(5000 /* milliseconds */);
            conn.setConnectTimeout(10000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);

            if (content != null) {
                conn.setDoOutput(true);
                OutputStreamWriter osw = new OutputStreamWriter(
                        conn.getOutputStream());
                osw.write(content.toString());
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
            return new JSONObject(sb.toString());

        } catch (Exception e) {
            Log.e("netio", "problem: " + e.getClass().getSimpleName() + ": "
                    + e.getMessage());

            throw e;

        }
    }

}
