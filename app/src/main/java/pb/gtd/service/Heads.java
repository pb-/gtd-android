package pb.gtd.service;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

import pb.gtd.Constants;
import pb.gtd.Util;

public class Heads {
    public HashMap<Short, Integer> map = new HashMap<Short, Integer>();

    public Heads() {
    }

    public Heads(JSONObject obj) throws JSONException {
        Iterator<String> keys = obj.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            map.put((short) Util.decodeNum(key, Constants.ORIGIN_LEN), obj.getInt(key));
        }
    }

    public Heads(Heads heads) {
        map = new HashMap<Short, Integer>(heads.map);
    }
}
