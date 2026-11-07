package com.pizzaria.cardapio;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** Persists orders on the device as JSON in SharedPreferences. */
public class OrderStore {

    public static final String[] STATUSES = {"Recebido", "Em preparo", "Saiu para entrega", "Entregue", "Cancelado"};

    private static final String PREFS = "pedidos";
    private static final String KEY = "lista";
    private static final String SEQ = "seq";

    private final SharedPreferences prefs;

    public OrderStore(Context c) { prefs = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE); }

    public List<JSONObject> all() {
        List<JSONObject> out = new ArrayList<>();
        try {
            JSONArray a = new JSONArray(prefs.getString(KEY, "[]"));
            for (int i = a.length() - 1; i >= 0; i--) out.add(a.getJSONObject(i)); // newest first
        } catch (JSONException ignored) { }
        return out;
    }

    /** Assigns a sequential number and timestamp, saves, and returns the order number. */
    public synchronized int add(JSONObject order) {
        int number = prefs.getInt(SEQ, 0) + 1;
        try {
            order.put("numero", number);
            order.put("criadoEm", System.currentTimeMillis());
            order.put("status", STATUSES[0]);
            order.put("pagamentoConfirmado", false);
            JSONArray hist = new JSONArray();
            hist.put(event("Pedido recebido"));
            order.put("historico", hist);
            JSONArray a = new JSONArray(prefs.getString(KEY, "[]"));
            a.put(order);
            prefs.edit().putString(KEY, a.toString()).putInt(SEQ, number).apply();
        } catch (JSONException ignored) { }
        return number;
    }

    public synchronized void update(JSONObject changed) {
        try {
            JSONArray a = new JSONArray(prefs.getString(KEY, "[]"));
            for (int i = 0; i < a.length(); i++) {
                if (a.getJSONObject(i).getInt("numero") == changed.getInt("numero")) { a.put(i, changed); break; }
            }
            prefs.edit().putString(KEY, a.toString()).apply();
        } catch (JSONException ignored) { }
    }

    public synchronized void delete(int number) {
        try {
            JSONArray a = new JSONArray(prefs.getString(KEY, "[]"));
            JSONArray n = new JSONArray();
            for (int i = 0; i < a.length(); i++) if (a.getJSONObject(i).getInt("numero") != number) n.put(a.get(i));
            prefs.edit().putString(KEY, n.toString()).apply();
        } catch (JSONException ignored) { }
    }

    public static JSONObject event(String text) {
        JSONObject e = new JSONObject();
        try { e.put("quando", System.currentTimeMillis()); e.put("texto", text); } catch (JSONException ignored) { }
        return e;
    }

    public static void log(JSONObject order, String text) {
        try {
            JSONArray h = order.optJSONArray("historico");
            if (h == null) { h = new JSONArray(); order.put("historico", h); }
            h.put(event(text));
        } catch (JSONException ignored) { }
    }
}
