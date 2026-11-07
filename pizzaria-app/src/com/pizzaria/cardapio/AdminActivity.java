package com.pizzaria.cardapio;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Pizzeria back office: lists, tracks and confirms payment of orders saved on this device. */
public class AdminActivity extends BaseActivity {

    static final int GREEN = Color.parseColor("#2F6B3F");
    static final int AMBER = Color.parseColor("#A86A12");
    static final String[] FILTERS = {"Todos", "Pagamento pendente", "Em andamento", "Concluídos", "Cancelados"};

    final Locale br = new Locale("pt", "BR");
    final NumberFormat brl = NumberFormat.getCurrencyInstance(br);
    final SimpleDateFormat hour = new SimpleDateFormat("HH:mm", br);
    final SimpleDateFormat full = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", br);

    OrderStore store;
    LinearLayout list, stats, tabs;
    int filter = 0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        store = new OrderStore(this);
        getWindow().setStatusBarColor(INK);
        getWindow().setNavigationBarColor(INK);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(IVORY);

        LinearLayout h = new LinearLayout(this);
        h.setOrientation(LinearLayout.VERTICAL);
        h.setBackgroundColor(INK);
        h.setPadding(dp(20), dp(22), dp(20), dp(16));
        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView over = caps("PAINEL DA PIZZARIA", GOLD);
        top.addView(over, new LinearLayout.LayoutParams(0, -2, 1));
        TextView back = caps("VOLTAR AO CARDÁPIO", Color.parseColor("#BDB3A6"));
        back.setOnClickListener(v -> finish());
        top.addView(back);
        h.addView(top);
        TextView title = text("Pedidos", 30, IVORY, SERIF);
        title.setPadding(0, dp(6), 0, dp(14));
        h.addView(title);
        stats = new LinearLayout(this);
        h.addView(stats);
        root.addView(h);

        HorizontalScrollView hs = new HorizontalScrollView(this);
        hs.setHorizontalScrollBarEnabled(false);
        hs.setBackgroundColor(INK);
        tabs = new LinearLayout(this);
        tabs.setPadding(dp(12), 0, dp(12), 0);
        hs.addView(tabs);
        root.addView(hs);
        for (int i = 0; i < FILTERS.length; i++) addTab(i);

        ScrollView sv = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(16), dp(8), dp(16), dp(24));
        sv.addView(list);
        root.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1));

        setContentView(root);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    TextView caps(String s, int color) {
        TextView t = text(s, 10, color, SANS_MEDIUM);
        t.setLetterSpacing(0.22f);
        return t;
    }

    void addTab(int i) {
        LinearLayout tab = new LinearLayout(this);
        tab.setOrientation(LinearLayout.VERTICAL);
        tab.setPadding(dp(12), dp(12), dp(12), 0);
        TextView t = text(FILTERS[i].toUpperCase(br), 11, MUTED, SANS_MEDIUM);
        t.setLetterSpacing(0.16f);
        tab.addView(t);
        View bar = new View(this);
        LinearLayout.LayoutParams bl = new LinearLayout.LayoutParams(-1, dp(2));
        bl.setMargins(0, dp(10), 0, 0);
        tab.addView(bar, bl);
        tab.setOnClickListener(v -> { filter = i; refresh(); });
        tabs.addView(tab);
    }

    static boolean isToday(long t) {
        Calendar a = Calendar.getInstance(), b = Calendar.getInstance();
        b.setTimeInMillis(t);
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    static boolean cancelled(JSONObject o) { return "Cancelado".equals(o.optString("status")); }
    static boolean done(JSONObject o) { return "Entregue".equals(o.optString("status")); }

    boolean matches(JSONObject o) {
        switch (filter) {
            case 1: return !cancelled(o) && !o.optBoolean("pagamentoConfirmado");
            case 2: return !cancelled(o) && !done(o);
            case 3: return done(o);
            case 4: return cancelled(o);
            default: return true;
        }
    }

    void refresh() {
        for (int i = 0; i < tabs.getChildCount(); i++) {
            LinearLayout tab = (LinearLayout) tabs.getChildAt(i);
            boolean on = i == filter;
            ((TextView) tab.getChildAt(0)).setTextColor(on ? IVORY : MUTED);
            tab.getChildAt(1).setBackgroundColor(on ? GOLD : Color.TRANSPARENT);
        }

        List<JSONObject> all = store.all();
        int today = 0, pending = 0;
        double revenue = 0;
        for (JSONObject o : all) {
            if (cancelled(o)) continue;
            if (isToday(o.optLong("criadoEm"))) {
                today++;
                if (o.optBoolean("pagamentoConfirmado")) revenue += o.optDouble("total");
            }
            if (!o.optBoolean("pagamentoConfirmado")) pending++;
        }
        stats.removeAllViews();
        stats.addView(stat("PEDIDOS HOJE", String.valueOf(today)), statLp(false));
        stats.addView(stat("RECEBIDO HOJE", brl.format(revenue)), statLp(false));
        stats.addView(stat("A CONFIRMAR", String.valueOf(pending)), statLp(true));

        list.removeAllViews();
        int shown = 0;
        for (JSONObject o : all) if (matches(o)) { list.addView(card(o)); shown++; }
        if (shown == 0) {
            TextView empty = text(all.isEmpty()
                    ? "Nenhum pedido registrado ainda.\nOs pedidos finalizados no cardápio aparecem aqui."
                    : "Nenhum pedido nesta categoria.", 14, MUTED, SERIF_ITALIC);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(48), 0, dp(48));
            list.addView(empty, new LinearLayout.LayoutParams(-1, -2));
        }

        LinearLayout tools = new LinearLayout(this);
        tools.setPadding(0, dp(24), 0, 0);
        TextView export = caps("EXPORTAR RELATÓRIO", MUTED);
        TextView pin = caps("ALTERAR SENHA", MUTED);
        for (TextView t : new TextView[]{export, pin}) {
            t.setGravity(Gravity.CENTER);
            t.setPadding(0, dp(10), 0, dp(10));
            tools.addView(t, new LinearLayout.LayoutParams(0, -2, 1));
        }
        export.setOnClickListener(v -> exportReport(all));
        pin.setOnClickListener(v -> changePin());
        list.addView(tools);
    }

    LinearLayout.LayoutParams statLp(boolean last) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1);
        if (!last) lp.setMargins(0, 0, dp(8), 0);
        return lp;
    }

    View stat(String label, String value) {
        LinearLayout s = new LinearLayout(this);
        s.setOrientation(LinearLayout.VERTICAL);
        s.setPadding(dp(12), dp(10), dp(12), dp(10));
        GradientDrawable g = new GradientDrawable();
        g.setColor(CHARCOAL);
        g.setCornerRadius(dp(4));
        s.setBackground(g);
        s.addView(caps(label, GOLD));
        TextView v = text(value, 17, IVORY, SERIF);
        v.setPadding(0, dp(4), 0, 0);
        v.setSingleLine(true);
        s.addView(v);
        return s;
    }

    TextView badge(String s, int color) {
        TextView t = text(s.toUpperCase(br), 9, color, SANS_MEDIUM);
        t.setLetterSpacing(0.18f);
        t.setPadding(dp(8), dp(3), dp(8), dp(3));
        GradientDrawable g = new GradientDrawable();
        g.setStroke(dp(1), color);
        g.setCornerRadius(dp(2));
        t.setBackground(g);
        return t;
    }

    int statusColor(JSONObject o) {
        if (cancelled(o)) return MUTED;
        if (done(o)) return GREEN;
        return WINE;
    }

    static String itemLine(JSONObject it) {
        StringBuilder s = new StringBuilder();
        s.append(it.optInt("quantidade")).append("x ").append(it.optString("nome"));
        if (it.has("tamanho")) {
            s.append(" — ").append(it.optString("tamanho"))
             .append(", ").append(it.optInt("fatias")).append(" fatias");
        }
        return s.toString();
    }

    View card(JSONObject o) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(16), dp(14), dp(16), dp(14));
        GradientDrawable g = new GradientDrawable();
        g.setColor(PAPER);
        g.setStroke(dp(1), LINE);
        g.setCornerRadius(dp(6));
        c.setBackground(g);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(10), 0, 0);
        c.setLayoutParams(lp);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        long when = o.optLong("criadoEm");
        String time = isToday(when) ? "Hoje, " + hour.format(new Date(when)) : full.format(new Date(when));
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.addView(text("Pedido nº " + o.optInt("numero"), 18, INK, SERIF));
        col.addView(text(time + " · " + o.optString("cliente"), 12, MUTED, SANS));
        top.addView(col, new LinearLayout.LayoutParams(0, -2, 1));
        top.addView(badge(o.optString("status"), statusColor(o)));
        c.addView(top);

        LinearLayout items = new LinearLayout(this);
        items.setOrientation(LinearLayout.VERTICAL);
        items.setPadding(0, dp(10), 0, dp(10));
        JSONArray arr = o.optJSONArray("itens");
        if (arr != null) for (int i = 0; i < arr.length(); i++) {
            TextView t = text(itemLine(arr.optJSONObject(i)), 14, CHARCOAL, SANS);
            t.setPadding(0, dp(2), 0, dp(2));
            items.addView(t);
        }
        c.addView(items);

        View line = new View(this);
        line.setBackgroundColor(LINE);
        c.addView(line, new LinearLayout.LayoutParams(-1, dp(1)));

        LinearLayout bottom = new LinearLayout(this);
        bottom.setGravity(Gravity.CENTER_VERTICAL);
        bottom.setPadding(0, dp(10), 0, 0);
        LinearLayout pay = new LinearLayout(this);
        pay.setOrientation(LinearLayout.VERTICAL);
        pay.addView(text(o.optString("pagamento"), 13, CHARCOAL, SANS));
        boolean paid = o.optBoolean("pagamentoConfirmado");
        TextView ps = caps(paid ? "PAGAMENTO CONFIRMADO" : "PAGAMENTO PENDENTE", paid ? GREEN : AMBER);
        ps.setPadding(0, dp(3), 0, 0);
        pay.addView(ps);
        bottom.addView(pay, new LinearLayout.LayoutParams(0, -2, 1));
        bottom.addView(text(brl.format(o.optDouble("total")), 18, WINE, SERIF));
        c.addView(bottom);

        c.setOnClickListener(v -> details(o));
        return c;
    }

    void details(JSONObject o) {
        LinearLayout p = panel();
        p.addView(caps(full.format(new Date(o.optLong("criadoEm"))).toUpperCase(br), GOLD));
        LinearLayout head = new LinearLayout(this);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.setPadding(0, dp(4), 0, dp(8));
        head.addView(text("Pedido nº " + o.optInt("numero"), 24, INK, SERIF), new LinearLayout.LayoutParams(0, -2, 1));
        head.addView(badge(o.optString("status"), statusColor(o)));
        p.addView(head);

        ScrollView sv = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        sv.addView(box);
        p.addView(sv, new LinearLayout.LayoutParams(-1, (int) (getResources().getDisplayMetrics().heightPixels * 0.42)));

        box.addView(section("Cliente"));
        box.addView(info(o.optString("cliente")));
        box.addView(info("Telefone: " + o.optString("telefone")));
        box.addView(info("Endereço: " + o.optString("endereco")));

        box.addView(section("Itens"));
        JSONArray arr = o.optJSONArray("itens");
        if (arr != null) for (int i = 0; i < arr.length(); i++) {
            JSONObject it = arr.optJSONObject(i);
            LinearLayout r = new LinearLayout(this);
            r.setPadding(0, dp(6), 0, dp(6));
            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            col.addView(text(it.optInt("quantidade") + "x " + it.optString("nome"), 15, INK, SERIF));
            String detail = it.has("tamanho")
                    ? "Pizza " + it.optString("tamanho") + " · " + it.optInt("fatias") + " fatias · " + it.optInt("diametro") + " cm"
                    : it.optString("tipo", "Item");
            col.addView(text(detail + " · " + brl.format(it.optDouble("precoUnitario")) + " cada", 12, MUTED, SANS));
            r.addView(col, new LinearLayout.LayoutParams(0, -2, 1));
            r.addView(text(brl.format(it.optDouble("subtotal")), 14, CHARCOAL, SERIF));
            box.addView(r);
        }
        LinearLayout tot = new LinearLayout(this);
        tot.setPadding(0, dp(8), 0, 0);
        tot.addView(text("Total", 16, INK, SERIF), new LinearLayout.LayoutParams(0, -2, 1));
        tot.addView(text(brl.format(o.optDouble("total")), 18, WINE, SERIF));
        box.addView(tot);

        box.addView(section("Pagamento"));
        box.addView(info(o.optString("pagamento")));
        boolean paid = o.optBoolean("pagamentoConfirmado");
        TextView ps = caps(paid ? "CONFIRMADO EM " + full.format(new Date(o.optLong("pagoEm"))).toUpperCase(br) : "AGUARDANDO CONFIRMAÇÃO", paid ? GREEN : AMBER);
        ps.setPadding(0, dp(4), 0, 0);
        box.addView(ps);

        box.addView(section("Histórico"));
        JSONArray hist = o.optJSONArray("historico");
        if (hist != null) for (int i = 0; i < hist.length(); i++) {
            JSONObject e = hist.optJSONObject(i);
            box.addView(info(hour.format(new Date(e.optLong("quando"))) + "  ·  " + e.optString("texto")));
        }

        Dialog d = sheet(p);
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.VERTICAL);
        actions.setPadding(0, dp(14), 0, 0);
        p.addView(actions);

        if (!cancelled(o) && !paid) action(actions, "CONFIRMAR PAGAMENTO", GREEN, () -> {
            put(o, "pagamentoConfirmado", true);
            put(o, "pagoEm", System.currentTimeMillis());
            OrderStore.log(o, "Pagamento confirmado (" + o.optString("pagamento") + ")");
            save(o, d, "Pagamento confirmado");
        });
        int st = indexOf(o.optString("status"));
        if (!cancelled(o) && st < 3) {
            final String next = OrderStore.STATUSES[st + 1];
            action(actions, "MARCAR: " + next.toUpperCase(br), INK, () -> {
                put(o, "status", next);
                OrderStore.log(o, "Status: " + next);
                save(o, d, "Pedido atualizado");
            });
        }

        LinearLayout links = new LinearLayout(this);
        TextView call = caps("LIGAR", MUTED);
        TextView cancel = caps(cancelled(o) ? "EXCLUIR" : "CANCELAR", WINE);
        TextView close = caps("FECHAR", MUTED);
        for (TextView t : new TextView[]{call, cancel, close}) {
            t.setGravity(Gravity.CENTER);
            t.setPadding(0, dp(14), 0, dp(4));
            links.addView(t, new LinearLayout.LayoutParams(0, -2, 1));
        }
        call.setOnClickListener(v -> {
            try { startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + o.optString("telefone")))); }
            catch (Exception ignored) { }
        });
        cancel.setOnClickListener(v -> {
            if (cancelled(o)) {
                store.delete(o.optInt("numero"));
                d.dismiss();
                refresh();
                Toast.makeText(this, "Pedido excluído", Toast.LENGTH_SHORT).show();
            } else {
                put(o, "status", "Cancelado");
                OrderStore.log(o, "Pedido cancelado");
                save(o, d, "Pedido cancelado");
            }
        });
        close.setOnClickListener(v -> d.dismiss());
        p.addView(links);
        d.show();
    }

    static int indexOf(String status) {
        for (int i = 0; i < OrderStore.STATUSES.length; i++) if (OrderStore.STATUSES[i].equals(status)) return i;
        return 0;
    }

    static void put(JSONObject o, String k, Object v) { try { o.put(k, v); } catch (JSONException ignored) { } }

    void save(JSONObject o, Dialog d, String msg) {
        store.update(o);
        d.dismiss();
        refresh();
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    void action(LinearLayout parent, String label, int color, Runnable r) {
        TextView b = pill(label, color, IVORY, true);
        b.setGravity(Gravity.CENTER);
        b.setOnClickListener(v -> r.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, dp(8));
        parent.addView(b, lp);
    }

    TextView info(String s) {
        TextView t = text(s, 14, CHARCOAL, SANS);
        t.setPadding(0, dp(3), 0, dp(1));
        return t;
    }

    void exportReport(List<JSONObject> all) {
        if (all.isEmpty()) { Toast.makeText(this, "Nenhum pedido para exportar", Toast.LENGTH_SHORT).show(); return; }
        StringBuilder sb = new StringBuilder("Relatório de pedidos · Bella Massa\n");
        sb.append("Gerado em ").append(full.format(new Date())).append("\n");
        double paidTotal = 0;
        for (JSONObject o : all) {
            sb.append("\n────────────\nPedido nº ").append(o.optInt("numero"))
              .append(" · ").append(full.format(new Date(o.optLong("criadoEm"))))
              .append("\nStatus: ").append(o.optString("status"))
              .append("\nCliente: ").append(o.optString("cliente")).append(" · ").append(o.optString("telefone"))
              .append("\nEndereço: ").append(o.optString("endereco")).append("\n");
            JSONArray arr = o.optJSONArray("itens");
            if (arr != null) for (int i = 0; i < arr.length(); i++) {
                JSONObject it = arr.optJSONObject(i);
                sb.append("  ").append(itemLine(it)).append(" — ").append(brl.format(it.optDouble("subtotal"))).append("\n");
            }
            boolean paid = o.optBoolean("pagamentoConfirmado");
            sb.append("Total: ").append(brl.format(o.optDouble("total")))
              .append("\nPagamento: ").append(o.optString("pagamento"))
              .append(paid ? " · confirmado em " + full.format(new Date(o.optLong("pagoEm"))) : " · pendente").append("\n");
            if (paid && !cancelled(o)) paidTotal += o.optDouble("total");
        }
        sb.append("\n════════════\nTotal recebido (pagamentos confirmados): ").append(brl.format(paidTotal));
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, "Relatório de pedidos · Bella Massa");
        i.putExtra(Intent.EXTRA_TEXT, sb.toString());
        try { startActivity(Intent.createChooser(i, "Exportar relatório")); } catch (Exception ignored) { }
    }

    void changePin() {
        final SharedPreferences prefs = getSharedPreferences("admin", MODE_PRIVATE);
        LinearLayout p = panel();
        p.addView(caps("SEGURANÇA", GOLD));
        TextView t = text("Alterar senha", 24, INK, SERIF);
        t.setPadding(0, dp(4), 0, dp(4));
        p.addView(t);
        int type = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD;
        final EditText cur = field(p, "SENHA ATUAL", "", "", type);
        final EditText nw = field(p, "NOVA SENHA (4 A 8 DÍGITOS)", "", "", type);
        Dialog d = sheet(p);
        TextView ok = pill("SALVAR", INK, IVORY, true);
        ok.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(18), 0, 0);
        p.addView(ok, lp);
        ok.setOnClickListener(v -> {
            if (!cur.getText().toString().equals(prefs.getString("pin", "1234"))) { cur.setError("Senha incorreta"); return; }
            String n = nw.getText().toString();
            if (n.length() < 4 || n.length() > 8) { nw.setError("Use de 4 a 8 dígitos"); return; }
            prefs.edit().putString("pin", n).apply();
            d.dismiss();
            Toast.makeText(this, "Senha alterada", Toast.LENGTH_SHORT).show();
        });
        d.show();
    }
}
