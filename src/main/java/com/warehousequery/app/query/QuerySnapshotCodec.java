package com.warehousequery.app.query;

import com.warehousequery.app.model.WarehouseEntry;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

final class QuerySnapshotCodec {
    String encode(QuerySnapshot snapshot) {
        JSONObject root = new JSONObject();
        root.put("schemaVersion", snapshot.schemaVersion());
        root.put("savedAt", snapshot.savedAt().toString());
        root.put("mode", snapshot.mode().name());
        root.put("entryNumbers", new JSONArray(snapshot.entryNumbers()));
        root.put("statusIndex", snapshot.statusIndex());
        root.put("queryEndDate", snapshot.queryEndDate().toString());
        root.put("segments", encodeSegments(snapshot.segments()));
        root.put("rows", encodeRows(snapshot.rows()));
        root.put("requestLog", snapshot.requestLog());
        root.put("responseLog", snapshot.responseLog());
        return root.toString(2);
    }

    QuerySnapshot decode(String content) {
        JSONObject root = new JSONObject(content);
        int schemaVersion = root.getInt("schemaVersion");
        if (schemaVersion != QuerySnapshot.SCHEMA_VERSION) {
            throw new IllegalArgumentException("Unsupported snapshot schema version: " + schemaVersion);
        }
        return new QuerySnapshot(
            schemaVersion,
            Instant.parse(root.getString("savedAt")),
            QueryMode.valueOf(root.getString("mode")),
            decodeStrings(root.getJSONArray("entryNumbers")),
            root.getInt("statusIndex"),
            LocalDate.parse(root.getString("queryEndDate")),
            decodeSegments(root.getJSONArray("segments")),
            decodeRows(root.getJSONArray("rows")),
            root.optString("requestLog", ""),
            root.optString("responseLog", ""));
    }

    private JSONArray encodeSegments(List<QuerySegment> segments) {
        JSONArray array = new JSONArray();
        for (QuerySegment segment : segments) {
            array.put(new JSONObject()
                .put("ordinal", segment.ordinal())
                .put("totalSegments", segment.totalSegments())
                .put("start", segment.start().toString())
                .put("end", segment.end().toString()));
        }
        return array;
    }

    private List<QuerySegment> decodeSegments(JSONArray array) {
        List<QuerySegment> segments = new ArrayList<QuerySegment>();
        for (int index = 0; index < array.length(); index++) {
            JSONObject value = array.getJSONObject(index);
            segments.add(new QuerySegment(
                value.getInt("ordinal"),
                value.getInt("totalSegments"),
                LocalDate.parse(value.getString("start")),
                LocalDate.parse(value.getString("end"))));
        }
        return segments;
    }

    private JSONArray encodeRows(List<WarehouseEntry> rows) {
        JSONArray array = new JSONArray();
        for (WarehouseEntry row : rows) {
            array.put(encodeRow(row));
        }
        return array;
    }

    private List<WarehouseEntry> decodeRows(JSONArray array) {
        List<WarehouseEntry> rows = new ArrayList<WarehouseEntry>();
        for (int index = 0; index < array.length(); index++) {
            rows.add(decodeRow(array.getJSONObject(index)));
        }
        return rows;
    }

    private JSONObject encodeRow(WarehouseEntry row) {
        return new JSONObject()
            .put("jcbh", safe(row.getJcbh()))
            .put("jczyh", safe(row.getJczyh()))
            .put("zyh", safe(row.getZyh()))
            .put("yjrq", safe(row.getYjrq()))
            .put("jcrq", safe(row.getJcrq()))
            .put("lf", safe(row.getLf()))
            .put("bzgg", safe(row.getBzgg()))
            .put("hwmc", safe(row.getHwmc()))
            .put("mt", safe(row.getMt()))
            .put("hh", safe(row.getHh()))
            .put("js", row.getJs())
            .put("tj", row.getTj())
            .put("kctj", row.getKctj())
            .put("mz", row.getMz())
            .put("ts", row.getTs())
            .put("shdw", safe(row.getShdw()))
            .put("kcjs", row.getKcjs())
            .put("bgzt", safe(row.getBgzt()))
            .put("bz", safe(row.getBz()))
            .put("jcid", safe(row.getJcid()))
            .put("inguid", safe(row.getInguid()))
            .put("yyh", safe(row.getYyh()))
            .put("srrq", safe(row.getSrrq()))
            .put("hd", safe(row.getHd()))
            .put("ch", safe(row.getCh()))
            .put("jsy", safe(row.getJsy()))
            .put("jsydh", safe(row.getJsydh()))
            .put("fgsmc", safe(row.getFgsmc()))
            .put("yqqdsj", safe(row.getYqqdsj()))
            .put("hz", safe(row.getHz()))
            .put("yjjs", row.getYjjs())
            .put("yjtj", row.getYjtj())
            .put("yjmz", row.getYjmz())
            .put("kcmz", row.getKcmz())
            .put("xhrq", safe(row.getXhrq()))
            .put("xhrq2", safe(row.getXhrq2()))
            .put("hwmc1", safe(row.getHwmc1()))
            .put("driverdh", safe(row.getDriverdh()))
            .put("cleng", safe(row.getCleng()))
            .put("chengzhong", safe(row.getChengzhong()))
            .put("bz2", safe(row.getBz2()))
            .put("hdmc", safe(row.getHdmc()))
            .put("selected", row.isSelected());
    }

    private WarehouseEntry decodeRow(JSONObject value) {
        WarehouseEntry row = new WarehouseEntry();
        row.setJcbh(value.optString("jcbh", ""));
        row.setJczyh(value.optString("jczyh", ""));
        row.setZyh(value.optString("zyh", ""));
        row.setYjrq(value.optString("yjrq", ""));
        row.setJcrq(value.optString("jcrq", ""));
        row.setLf(value.optString("lf", ""));
        row.setBzgg(value.optString("bzgg", ""));
        row.setHwmc(value.optString("hwmc", ""));
        row.setMt(value.optString("mt", ""));
        row.setHh(value.optString("hh", ""));
        row.setJs(value.optInt("js", 0));
        row.setTj(value.optDouble("tj", 0.0));
        row.setKctj(value.optDouble("kctj", 0.0));
        row.setMz(value.optDouble("mz", 0.0));
        row.setTs(value.optInt("ts", 0));
        row.setShdw(value.optString("shdw", ""));
        row.setKcjs(value.optDouble("kcjs", 0.0));
        row.setBgzt(value.optString("bgzt", ""));
        row.setBz(value.optString("bz", ""));
        row.setJcid(value.optString("jcid", ""));
        row.setInguid(value.optString("inguid", ""));
        row.setYyh(value.optString("yyh", ""));
        row.setSrrq(value.optString("srrq", ""));
        row.setHd(value.optString("hd", ""));
        row.setCh(value.optString("ch", ""));
        row.setJsy(value.optString("jsy", ""));
        row.setJsydh(value.optString("jsydh", ""));
        row.setFgsmc(value.optString("fgsmc", ""));
        row.setYqqdsj(value.optString("yqqdsj", ""));
        row.setHz(value.optString("hz", ""));
        row.setYjjs(value.optInt("yjjs", 0));
        row.setYjtj(value.optDouble("yjtj", 0.0));
        row.setYjmz(value.optDouble("yjmz", 0.0));
        row.setKcmz(value.optDouble("kcmz", 0.0));
        row.setXhrq(value.optString("xhrq", ""));
        row.setXhrq2(value.optString("xhrq2", ""));
        row.setHwmc1(value.optString("hwmc1", ""));
        row.setDriverdh(value.optString("driverdh", ""));
        row.setCleng(value.optString("cleng", ""));
        row.setChengzhong(value.optString("chengzhong", ""));
        row.setBz2(value.optString("bz2", ""));
        row.setHdmc(value.optString("hdmc", ""));
        row.setSelected(value.optBoolean("selected", false));
        return row;
    }

    private List<String> decodeStrings(JSONArray array) {
        List<String> values = new ArrayList<String>();
        for (int index = 0; index < array.length(); index++) {
            values.add(array.getString(index));
        }
        return values;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
