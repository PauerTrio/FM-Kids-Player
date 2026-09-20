package de.fmdigitalsolutions.kidsplayer;

import org.json.JSONObject;

public class SeriesItem {
    public String id, name, folderUri, coverUri;
    public int progress;

    public SeriesItem(String id, String name, String folderUri, String coverUri, int progress) {
        this.id=id; this.name=name; this.folderUri=folderUri; this.coverUri=coverUri; this.progress=progress;
    }

    public JSONObject toJson() {
        JSONObject o = new JSONObject();
        try {
            o.put("id", id); o.put("name", name); o.put("folderUri", folderUri);
            o.put("coverUri", coverUri == null ? "" : coverUri); o.put("progress", progress);
        } catch(Exception ignored) {}
        return o;
    }

    public static SeriesItem fromJson(JSONObject o) {
        return new SeriesItem(o.optString("id"), o.optString("name"),
                o.optString("folderUri"), o.optString("coverUri"), o.optInt("progress",0));
    }
}
