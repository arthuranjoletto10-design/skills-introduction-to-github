package com.pizzaria.cardapio;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity {

    static final int RED = Color.parseColor("#C62828");
    static final int DARK = Color.parseColor("#3E2723");
    static final int CREAM = Color.parseColor("#FFF8E1");
    static final int GREEN = Color.parseColor("#2E7D32");
    static final int GRAY = Color.parseColor("#6D4C41");

    static class Item {
        final String emoji, name, desc; final double price; final boolean pizza;
        Item(String emoji, String name, String desc, double price, boolean pizza) {
            this.emoji = emoji; this.name = name; this.desc = desc; this.price = price; this.pizza = pizza;
        }
    }

    static final String[] SIZES = {"Broto (4 fatias)", "Média (6 fatias)", "Grande (8 fatias)"};
    static final String[] SIZE_SHORT = {"Broto", "Média", "Grande"};
    static final double[] SIZE_MULT = {0.7, 0.85, 1.0};

    final Map<String, List<Item>> menu = new LinkedHashMap<>();
    final Map<String, Integer> cart = new LinkedHashMap<>();
    final Map<String, Double> cartPrice = new LinkedHashMap<>();
    final NumberFormat brl = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    LinearLayout list, tabs;
    Button cartButton;
    String current;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        buildMenu();
        getWindow().setStatusBarColor(RED);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(CREAM);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setBackgroundColor(RED);
        header.setPadding(dp(16), dp(18), dp(16), dp(14));
        TextView title = text("🍕 Pizzaria Bella Massa", 24, Color.WHITE, true);
        TextView sub = text("Cardápio digital • Brasil, Itália e EUA no forno a lenha", 13, Color.parseColor("#FFE0B2"), false);
        header.addView(title); header.addView(sub);
        root.addView(header);

        HorizontalScrollView hs = new HorizontalScrollView(this);
        hs.setHorizontalScrollBarEnabled(false);
        hs.setBackgroundColor(Color.WHITE);
        tabs = new LinearLayout(this);
        tabs.setPadding(dp(8), dp(8), dp(8), dp(8));
        hs.addView(tabs);
        root.addView(hs);

        ScrollView sv = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(12), dp(8), dp(12), dp(12));
        sv.addView(list);
        root.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1));

        cartButton = new Button(this);
        cartButton.setTextColor(Color.WHITE);
        cartButton.setTextSize(16);
        cartButton.setAllCaps(false);
        cartButton.setBackground(round(GREEN, 0));
        cartButton.setOnClickListener(v -> showCart());
        root.addView(cartButton, new LinearLayout.LayoutParams(-1, dp(56)));

        setContentView(root);
        for (String c : menu.keySet()) addTab(c);
        select(menu.keySet().iterator().next());
        updateCart();
    }

    void buildMenu() {
        List<Item> br = new ArrayList<>();
        br.add(new Item("🧀", "Calabresa", "Calabresa fatiada, cebola, azeitonas e mussarela", 49.90, true));
        br.add(new Item("🧀", "Mussarela", "Mussarela, rodelas de tomate, orégano e azeitonas", 44.90, true));
        br.add(new Item("🍗", "Frango com Catupiry", "Frango desfiado temperado coberto com Catupiry original", 54.90, true));
        br.add(new Item("🥚", "Portuguesa", "Presunto, ovos, cebola, ervilha, palmito, azeitonas e mussarela", 54.90, true));
        br.add(new Item("🍖", "Moda da Casa", "Lombo canadense, bacon, champignon, milho e catupiry", 59.90, true));
        br.add(new Item("🌽", "Milho com Bacon", "Milho verde, bacon crocante e mussarela", 49.90, true));
        br.add(new Item("🥩", "Carne Seca com Catupiry", "Carne seca desfiada, cebola roxa e Catupiry", 62.90, true));
        br.add(new Item("🧅", "Baiana", "Calabresa moída apimentada, ovos, cebola e pimenta", 52.90, true));
        br.add(new Item("🧀", "Quatro Queijos", "Mussarela, provolone, parmesão e Catupiry", 56.90, true));
        br.add(new Item("🥓", "Atum", "Atum sólido, cebola, azeitonas e mussarela", 52.90, true));
        menu.put("🇧🇷 Brasileiras", br);

        List<Item> it = new ArrayList<>();
        it.add(new Item("🍅", "Margherita", "Molho San Marzano, mussarela de búfala, manjericão fresco e azeite", 52.90, true));
        it.add(new Item("🍅", "Marinara", "Molho de tomate, alho, orégano e azeite extravirgem", 42.90, true));
        it.add(new Item("🥓", "Diavola", "Salame picante, pimenta calabresa e mussarela", 56.90, true));
        it.add(new Item("🍄", "Quattro Stagioni", "Presunto cotto, alcachofra, cogumelos e azeitonas pretas", 62.90, true));
        it.add(new Item("🥬", "Parma e Rúcula", "Presunto de Parma, rúcula, tomate-cereja e lascas de grana padano", 68.90, true));
        it.add(new Item("🧀", "Quattro Formaggi", "Mussarela, gorgonzola, fontina e parmigiano reggiano", 59.90, true));
        it.add(new Item("🍆", "Capricciosa", "Presunto, cogumelos, alcachofra, ovo e azeitonas", 61.90, true));
        it.add(new Item("🌿", "Pesto Genovese", "Pesto de manjericão, mussarela, tomate seco e pinoli", 58.90, true));
        menu.put("🇮🇹 Italianas", it);

        List<Item> us = new ArrayList<>();
        us.add(new Item("🍕", "Pepperoni", "Muito pepperoni crocante sobre mussarela e molho de tomate", 56.90, true));
        us.add(new Item("🍍", "Hawaiian", "Presunto, abacaxi caramelizado e mussarela", 54.90, true));
        us.add(new Item("🍖", "BBQ Chicken", "Frango, molho barbecue, cebola roxa e coentro", 58.90, true));
        us.add(new Item("🥩", "Meat Lovers", "Pepperoni, bacon, linguiça, presunto e carne moída", 66.90, true));
        us.add(new Item("🍔", "Cheeseburger", "Carne moída, cheddar, picles, cebola e molho especial", 62.90, true));
        us.add(new Item("🌶️", "Buffalo Chicken", "Frango ao molho buffalo, mussarela e molho ranch", 59.90, true));
        us.add(new Item("🫑", "Supreme", "Pepperoni, linguiça, pimentões, cebola, cogumelos e azeitonas", 64.90, true));
        menu.put("🇺🇸 Americanas", us);

        List<Item> sw = new ArrayList<>();
        sw.add(new Item("🍫", "Brigadeiro", "Chocolate ao leite com granulado", 46.90, true));
        sw.add(new Item("🍌", "Romeu e Julieta", "Goiabada cascão derretida com queijo minas", 44.90, true));
        sw.add(new Item("🍌", "Banana com Canela", "Banana, açúcar, canela e leite condensado", 42.90, true));
        sw.add(new Item("🍓", "Chocolate com Morango", "Chocolate meio amargo e morangos frescos", 52.90, true));
        sw.add(new Item("🥜", "Nutella com Morango", "Creme de avelã e morangos frescos", 56.90, true));
        sw.add(new Item("🍪", "Prestígio", "Chocolate ao leite com coco ralado", 46.90, true));
        menu.put("🍫 Doces", sw);

        List<Item> dr = new ArrayList<>();
        dr.add(new Item("🥤", "Guaraná Antarctica 2L", "Refrigerante gelado", 13.90, false));
        dr.add(new Item("🥤", "Coca-Cola 2L", "Refrigerante gelado", 14.90, false));
        dr.add(new Item("🥫", "Refrigerante Lata 350ml", "Coca-Cola, Guaraná, Fanta ou Sprite", 6.90, false));
        dr.add(new Item("🍊", "Suco Natural 500ml", "Laranja, limão, maracujá ou abacaxi com hortelã", 11.90, false));
        dr.add(new Item("💧", "Água Mineral 500ml", "Com ou sem gás", 4.90, false));
        dr.add(new Item("🍺", "Cerveja Long Neck", "Puro malte gelada (venda para maiores de 18 anos)", 10.90, false));
        dr.add(new Item("🍷", "Taça de Vinho Tinto", "Chianti italiano", 24.90, false));
        menu.put("🥤 Bebidas", dr);
    }

    void addTab(String cat) {
        TextView t = text(cat, 14, DARK, true);
        t.setPadding(dp(14), dp(8), dp(14), dp(8));
        t.setTag(cat);
        t.setOnClickListener(v -> select(cat));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.setMargins(dp(4), 0, dp(4), 0);
        tabs.addView(t, lp);
    }

    void select(String cat) {
        current = cat;
        for (int i = 0; i < tabs.getChildCount(); i++) {
            TextView t = (TextView) tabs.getChildAt(i);
            boolean on = cat.equals(t.getTag());
            t.setBackground(round(on ? RED : Color.parseColor("#FBE9E7"), 20));
            t.setTextColor(on ? Color.WHITE : DARK);
        }
        list.removeAllViews();
        for (Item item : menu.get(cat)) list.addView(card(item));
    }

    View card(Item item) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        card.setBackground(round(Color.WHITE, 14));
        card.setElevation(dp(2));
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(-1, -2);
        clp.setMargins(0, dp(6), 0, dp(6));
        card.setLayoutParams(clp);

        TextView emo = text(item.emoji, 34, DARK, false);
        emo.setGravity(Gravity.CENTER);
        card.addView(emo, new LinearLayout.LayoutParams(dp(56), -2));

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dp(8), 0, dp(8), 0);
        info.addView(text(item.name, 17, DARK, true));
        TextView d = text(item.desc, 13, GRAY, false);
        d.setPadding(0, dp(2), 0, dp(4));
        info.addView(d);
        String price = item.pizza ? "a partir de " + brl.format(item.price * SIZE_MULT[0]) : brl.format(item.price);
        info.addView(text(price, 15, RED, true));
        card.addView(info, new LinearLayout.LayoutParams(0, -2, 1));

        Button add = new Button(this);
        add.setText("+");
        add.setTextSize(22);
        add.setTextColor(Color.WHITE);
        add.setBackground(round(RED, 24));
        add.setOnClickListener(v -> onAdd(item));
        card.addView(add, new LinearLayout.LayoutParams(dp(48), dp(48)));
        card.setOnClickListener(v -> onAdd(item));
        return card;
    }

    void onAdd(Item item) {
        if (!item.pizza) { addToCart(item.name, item.price); return; }
        String[] opts = new String[SIZES.length];
        for (int i = 0; i < SIZES.length; i++) opts[i] = SIZES[i] + " — " + brl.format(item.price * SIZE_MULT[i]);
        new AlertDialog.Builder(this)
                .setTitle(item.emoji + " " + item.name + "\nEscolha o tamanho")
                .setItems(opts, (dlg, which) ->
                        addToCart(item.name + " (" + SIZE_SHORT[which] + ")", round2(item.price * SIZE_MULT[which])))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    static double round2(double v) { return Math.round(v * 100) / 100.0; }

    void addToCart(String key, double price) {
        cart.put(key, (cart.containsKey(key) ? cart.get(key) : 0) + 1);
        cartPrice.put(key, price);
        updateCart();
        Toast.makeText(this, key + " adicionado ao pedido", Toast.LENGTH_SHORT).show();
    }

    double total() {
        double t = 0;
        for (Map.Entry<String, Integer> e : cart.entrySet()) t += e.getValue() * cartPrice.get(e.getKey());
        return t;
    }

    int count() { int c = 0; for (int q : cart.values()) c += q; return c; }

    void updateCart() {
        int c = count();
        cartButton.setText(c == 0 ? "🛒 Seu pedido está vazio" : "🛒 Ver pedido (" + c + " itens) • " + brl.format(total()));
    }

    void showCart() {
        if (cart.isEmpty()) { Toast.makeText(this, "Adicione itens do cardápio", Toast.LENGTH_SHORT).show(); return; }
        ScrollView sv = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(16), dp(8), dp(16), dp(8));
        sv.addView(box);
        final AlertDialog[] dlg = new AlertDialog[1];
        Runnable[] render = new Runnable[1];
        render[0] = () -> {
            box.removeAllViews();
            for (String key : new ArrayList<>(cart.keySet())) {
                LinearLayout row = new LinearLayout(this);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(0, dp(6), 0, dp(6));
                LinearLayout col = new LinearLayout(this);
                col.setOrientation(LinearLayout.VERTICAL);
                col.addView(text(key, 15, DARK, true));
                col.addView(text(cart.get(key) + " x " + brl.format(cartPrice.get(key)), 13, GRAY, false));
                row.addView(col, new LinearLayout.LayoutParams(0, -2, 1));
                row.addView(small("−", v -> {
                    int q = cart.get(key) - 1;
                    if (q <= 0) { cart.remove(key); cartPrice.remove(key); } else cart.put(key, q);
                    updateCart();
                    if (cart.isEmpty()) dlg[0].dismiss(); else render[0].run();
                }));
                row.addView(small("+", v -> { cart.put(key, cart.get(key) + 1); updateCart(); render[0].run(); }));
                box.addView(row);
            }
            TextView tot = text("Total: " + brl.format(total()), 18, RED, true);
            tot.setPadding(0, dp(12), 0, 0);
            box.addView(tot);
            if (dlg[0] != null) dlg[0].setTitle("🛒 Seu pedido (" + count() + " itens)");
        };
        render[0].run();
        dlg[0] = new AlertDialog.Builder(this)
                .setTitle("🛒 Seu pedido (" + count() + " itens)")
                .setView(sv)
                .setPositiveButton("Finalizar pedido", (d, w) -> finish_order())
                .setNeutralButton("Limpar", (d, w) -> { cart.clear(); cartPrice.clear(); updateCart(); })
                .setNegativeButton("Continuar", null)
                .create();
        dlg[0].show();
    }

    void finish_order() {
        StringBuilder sb = new StringBuilder("🍕 Pedido - Pizzaria Bella Massa\n\n");
        for (Map.Entry<String, Integer> e : cart.entrySet())
            sb.append(e.getValue()).append("x ").append(e.getKey()).append(" — ")
              .append(brl.format(e.getValue() * cartPrice.get(e.getKey()))).append("\n");
        sb.append("\nTotal: ").append(brl.format(total()));
        final String msg = sb.toString();
        new AlertDialog.Builder(this)
                .setTitle("✅ Pedido confirmado!")
                .setMessage(msg + "\n\nDeseja enviar o pedido (WhatsApp, SMS etc.)?")
                .setPositiveButton("Enviar", (d, w) -> {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_TEXT, msg);
                    try { startActivity(Intent.createChooser(i, "Enviar pedido")); } catch (Exception ignored) { }
                    cart.clear(); cartPrice.clear(); updateCart();
                })
                .setNegativeButton("Fechar", (d, w) -> { cart.clear(); cartPrice.clear(); updateCart(); })
                .show();
    }

    Button small(String label, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(18);
        b.setTextColor(Color.WHITE);
        b.setBackground(round(RED, 18));
        b.setOnClickListener(l);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(40), dp(40));
        lp.setMargins(dp(4), 0, 0, 0);
        b.setLayoutParams(lp);
        return b;
    }

    TextView text(String s, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    GradientDrawable round(int color, int radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radiusDp));
        return g;
    }

    int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }
}
