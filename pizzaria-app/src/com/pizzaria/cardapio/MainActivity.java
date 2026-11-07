package com.pizzaria.cardapio;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.content.SharedPreferences;
import android.text.InputType;
import android.widget.EditText;
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

    // Palette
    static final int INK = Color.parseColor("#151312");
    static final int CHARCOAL = Color.parseColor("#2A2624");
    static final int IVORY = Color.parseColor("#FAF7F2");
    static final int PAPER = Color.WHITE;
    static final int GOLD = Color.parseColor("#B08D57");
    static final int GOLD_SOFT = Color.parseColor("#E9DFCC");
    static final int WINE = Color.parseColor("#7A1F2B");
    static final int MUTED = Color.parseColor("#7B736B");
    static final int LINE = Color.parseColor("#E7E1D8");

    static final Typeface SERIF = Typeface.create("serif", Typeface.NORMAL);
    static final Typeface SERIF_BOLD = Typeface.create("serif", Typeface.BOLD);
    static final Typeface SERIF_ITALIC = Typeface.create("serif", Typeface.ITALIC);
    static final Typeface SANS = Typeface.create("sans-serif", Typeface.NORMAL);
    static final Typeface SANS_MEDIUM = Typeface.create("sans-serif-medium", Typeface.NORMAL);

    static class Item {
        final String name, desc, tag; final double price; final boolean pizza;
        Item(String name, String desc, double price, boolean pizza, String tag) {
            this.name = name; this.desc = desc; this.price = price; this.pizza = pizza; this.tag = tag;
        }
    }

    static class Category {
        final String title, subtitle; final List<Item> items = new ArrayList<>();
        Category(String title, String subtitle) { this.title = title; this.subtitle = subtitle; }
        Category add(String n, String d, double p, String tag) { items.add(new Item(n, d, p, true, tag)); return this; }
        Category drink(String n, String d, double p) { items.add(new Item(n, d, p, false, null)); return this; }
    }

    static final String[] SIZES = {"Broto", "Média", "Grande"};
    static final String[] SIZE_INFO = {"4 fatias · 25 cm", "6 fatias · 30 cm", "8 fatias · 35 cm"};
    static final double[] SIZE_MULT = {0.7, 0.85, 1.0};

    final List<Category> menu = new ArrayList<>();
    final Map<String, Integer> cart = new LinkedHashMap<>();
    final Map<String, Double> cartPrice = new LinkedHashMap<>();
    final NumberFormat brl = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    LinearLayout list, tabs;
    ScrollView scroll;
    LinearLayout cartBar;
    TextView cartCount, cartTotal;
    int current = 0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        buildMenu();
        Window w = getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.setStatusBarColor(INK);
        w.setNavigationBarColor(INK);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(IVORY);

        root.addView(header());

        HorizontalScrollView hs = new HorizontalScrollView(this);
        hs.setHorizontalScrollBarEnabled(false);
        hs.setBackgroundColor(INK);
        tabs = new LinearLayout(this);
        tabs.setPadding(dp(12), 0, dp(12), 0);
        hs.addView(tabs);
        root.addView(hs);

        scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(20), dp(8), dp(20), dp(24));
        scroll.addView(list);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        root.addView(cartBar());

        setContentView(root);
        for (int i = 0; i < menu.size(); i++) addTab(i);
        select(0);
        updateCart();
    }

    View header() {
        LinearLayout h = new LinearLayout(this);
        h.setOrientation(LinearLayout.VERTICAL);
        h.setGravity(Gravity.CENTER_HORIZONTAL);
        h.setBackgroundColor(INK);
        h.setPadding(dp(20), dp(28), dp(20), dp(18));

        TextView over = text("PIZZERIA  ·  FORNO A LENHA", 11, GOLD, SANS_MEDIUM);
        over.setLetterSpacing(0.3f);
        h.addView(over);

        TextView title = text("Bella Massa", 34, IVORY, SERIF);
        title.setPadding(0, dp(6), 0, dp(4));
        h.addView(title);

        View rule = new View(this);
        rule.setBackgroundColor(GOLD);
        LinearLayout.LayoutParams rl = new LinearLayout.LayoutParams(dp(48), dp(1));
        rl.setMargins(0, dp(6), 0, dp(10));
        h.addView(rule, rl);

        TextView sub = text("Receitas clássicas do Brasil, da Itália e dos Estados Unidos", 13, Color.parseColor("#BDB3A6"), SERIF_ITALIC);
        sub.setGravity(Gravity.CENTER);
        h.addView(sub);
        return h;
    }

    View cartBar() {
        cartBar = new LinearLayout(this);
        cartBar.setGravity(Gravity.CENTER_VERTICAL);
        cartBar.setBackgroundColor(INK);
        cartBar.setPadding(dp(20), dp(12), dp(20), dp(12));

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        cartCount = text("", 11, GOLD, SANS_MEDIUM);
        cartCount.setLetterSpacing(0.2f);
        cartTotal = text("", 20, IVORY, SERIF);
        col.addView(cartCount);
        col.addView(cartTotal);
        cartBar.addView(col, new LinearLayout.LayoutParams(0, -2, 1));

        TextView btn = pill("VER PEDIDO", GOLD, INK, true);
        btn.setOnClickListener(v -> showCart());
        cartBar.addView(btn);
        return cartBar;
    }

    void buildMenu() {
        menu.add(new Category("Brasileiras", "Os sabores que conquistaram as pizzarias de São Paulo")
            .add("Calabresa", "Calabresa artesanal fatiada, cebola roxa, azeitonas pretas e mussarela", 49.90, "Clássica")
            .add("Mussarela", "Mussarela de primeira, rodelas de tomate, orégano e azeitonas", 44.90, null)
            .add("Frango com Catupiry", "Frango desfiado e temperado, coberto com Catupiry original", 54.90, "Mais pedida")
            .add("Portuguesa", "Presunto, ovos, cebola, ervilha, palmito, azeitonas e mussarela", 54.90, "Clássica")
            .add("Carne Seca com Catupiry", "Carne seca desfiada na manteiga de garrafa, cebola roxa e Catupiry", 62.90, null)
            .add("Moda da Casa", "Lombo canadense, bacon, champignon, milho e Catupiry", 59.90, "Da casa")
            .add("Quatro Queijos", "Mussarela, provolone, parmesão e Catupiry", 56.90, null)
            .add("Milho com Bacon", "Milho verde, bacon crocante e mussarela", 49.90, null)
            .add("Baiana", "Calabresa moída apimentada, ovos, cebola e pimenta dedo-de-moça", 52.90, "Picante")
            .add("Atum", "Atum sólido, cebola, azeitonas e mussarela", 52.90, null));

        menu.add(new Category("Italianas", "Massa de fermentação natural de 48 horas, ao estilo napolitano")
            .add("Margherita", "Molho de tomate San Marzano, mussarela de búfala, manjericão e azeite extravirgem", 52.90, "Clássica")
            .add("Marinara", "Tomate San Marzano, alho, orégano siciliano e azeite extravirgem", 42.90, "Vegana")
            .add("Diavola", "Salame picante, pimenta calabresa e fior di latte", 56.90, "Picante")
            .add("Quattro Formaggi", "Fior di latte, gorgonzola, fontina e parmigiano reggiano", 59.90, null)
            .add("Parma e Rúcula", "Prosciutto di Parma, rúcula, tomate-cereja e lascas de grana padano", 68.90, "Especial")
            .add("Quattro Stagioni", "Presunto cotto, alcachofra, cogumelos e azeitonas pretas", 62.90, null)
            .add("Capricciosa", "Presunto cotto, cogumelos, alcachofra, ovo e azeitonas", 61.90, null)
            .add("Pesto Genovese", "Pesto de manjericão, fior di latte, tomate seco e pinoli", 58.90, "Vegetariana"));

        menu.add(new Category("Americanas", "Inspiradas nas pizzarias de Nova York e Chicago")
            .add("Pepperoni", "Pepperoni crocante em abundância sobre mussarela e molho de tomate", 56.90, "Mais pedida")
            .add("Meat Lovers", "Pepperoni, bacon, linguiça, presunto e carne moída", 66.90, null)
            .add("BBQ Chicken", "Frango, molho barbecue defumado, cebola roxa e coentro", 58.90, null)
            .add("Supreme", "Pepperoni, linguiça, pimentões, cebola, cogumelos e azeitonas", 64.90, null)
            .add("Cheeseburger", "Carne moída, cheddar, picles, cebola e molho especial", 62.90, null)
            .add("Buffalo Chicken", "Frango ao molho buffalo, mussarela e molho ranch", 59.90, "Picante")
            .add("Hawaiian", "Presunto, abacaxi caramelizado e mussarela", 54.90, null));

        menu.add(new Category("Doces", "Para encerrar com delicadeza")
            .add("Brigadeiro", "Chocolate belga ao leite com granulado", 46.90, "Mais pedida")
            .add("Romeu e Julieta", "Goiabada cascão derretida com queijo minas", 44.90, "Clássica")
            .add("Chocolate com Morango", "Chocolate meio amargo e morangos frescos", 52.90, null)
            .add("Avelã com Morango", "Creme de avelã e morangos frescos", 56.90, null)
            .add("Banana com Canela", "Banana, açúcar mascavo, canela e leite condensado", 42.90, null)
            .add("Prestígio", "Chocolate ao leite com coco fresco ralado", 46.90, null));

        menu.add(new Category("Bebidas", "Seleção de refrigerantes, sucos e vinhos")
            .drink("Taça de Vinho Tinto", "Chianti Classico DOCG, Toscana", 24.90)
            .drink("Garrafa de Vinho Tinto", "Chianti Classico DOCG, 750 ml", 129.00)
            .drink("Cerveja Long Neck", "Puro malte, 355 ml · venda para maiores de 18 anos", 10.90)
            .drink("Suco Natural", "Laranja, limão, maracujá ou abacaxi com hortelã · 500 ml", 11.90)
            .drink("Guaraná Antarctica", "Garrafa 2 litros", 13.90)
            .drink("Coca-Cola", "Garrafa 2 litros", 14.90)
            .drink("Refrigerante em Lata", "Coca-Cola, Guaraná, Fanta ou Sprite · 350 ml", 6.90)
            .drink("Água Mineral", "Com ou sem gás · 500 ml", 4.90));
    }

    void addTab(int i) {
        LinearLayout tab = new LinearLayout(this);
        tab.setOrientation(LinearLayout.VERTICAL);
        tab.setGravity(Gravity.CENTER_HORIZONTAL);
        tab.setPadding(dp(14), dp(12), dp(14), 0);
        TextView t = text(menu.get(i).title.toUpperCase(new Locale("pt", "BR")), 12, MUTED, SANS_MEDIUM);
        t.setLetterSpacing(0.18f);
        tab.addView(t);
        View bar = new View(this);
        LinearLayout.LayoutParams bl = new LinearLayout.LayoutParams(-1, dp(2));
        bl.setMargins(0, dp(10), 0, 0);
        tab.addView(bar, bl);
        tab.setOnClickListener(v -> select(i));
        tabs.addView(tab);
    }

    void select(int idx) {
        current = idx;
        for (int i = 0; i < tabs.getChildCount(); i++) {
            LinearLayout tab = (LinearLayout) tabs.getChildAt(i);
            boolean on = i == idx;
            ((TextView) tab.getChildAt(0)).setTextColor(on ? IVORY : MUTED);
            tab.getChildAt(1).setBackgroundColor(on ? GOLD : Color.TRANSPARENT);
        }
        Category c = menu.get(idx);
        list.removeAllViews();

        TextView h = text(c.title, 28, INK, SERIF);
        h.setPadding(0, dp(20), 0, 0);
        list.addView(h);
        TextView s = text(c.subtitle, 14, MUTED, SERIF_ITALIC);
        s.setPadding(0, dp(4), 0, dp(4));
        list.addView(s);
        if (c.items.get(0).pizza) {
            TextView note = text("Preços referentes à pizza grande. Tamanhos broto e média disponíveis.", 12, MUTED, SANS);
            note.setPadding(0, dp(2), 0, dp(8));
            list.addView(note);
        }
        for (int i = 0; i < c.items.size(); i++) list.addView(row(c.items.get(i), i < c.items.size() - 1));
        scroll.scrollTo(0, 0);
    }

    View row(Item item, boolean divider) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(0, dp(18), 0, 0);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.BOTTOM);
        TextView name = text(item.name, 19, INK, SERIF);
        top.addView(name, new LinearLayout.LayoutParams(0, -2, 1));
        TextView price = text(brl.format(item.price), 17, WINE, SERIF);
        price.setPadding(dp(12), 0, 0, 0);
        top.addView(price);
        wrap.addView(top);

        if (item.tag != null) {
            TextView tag = text(item.tag.toUpperCase(new Locale("pt", "BR")), 9, GOLD, SANS_MEDIUM);
            tag.setLetterSpacing(0.2f);
            tag.setPadding(dp(8), dp(3), dp(8), dp(3));
            GradientDrawable g = new GradientDrawable();
            g.setStroke(dp(1), GOLD);
            g.setCornerRadius(dp(2));
            tag.setBackground(g);
            LinearLayout.LayoutParams tl = new LinearLayout.LayoutParams(-2, -2);
            tl.setMargins(0, dp(6), 0, 0);
            wrap.addView(tag, tl);
        }

        LinearLayout bottom = new LinearLayout(this);
        bottom.setGravity(Gravity.CENTER_VERTICAL);
        bottom.setPadding(0, dp(6), 0, dp(18));
        TextView d = text(item.desc, 14, MUTED, SANS);
        d.setLineSpacing(dp(3), 1f);
        bottom.addView(d, new LinearLayout.LayoutParams(0, -2, 1));
        TextView add = pill("ADICIONAR", Color.TRANSPARENT, INK, false);
        LinearLayout.LayoutParams al = new LinearLayout.LayoutParams(-2, -2);
        al.setMargins(dp(16), 0, 0, 0);
        bottom.addView(add, al);
        add.setOnClickListener(v -> onAdd(item));
        wrap.addView(bottom);

        if (divider) {
            View line = new View(this);
            line.setBackgroundColor(LINE);
            wrap.addView(line, new LinearLayout.LayoutParams(-1, dp(1)));
        }
        return wrap;
    }

    // ---------- Dialogs ----------

    Dialog sheet(View content) {
        Dialog d = new Dialog(this);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(content);
        Window w = d.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            w.setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.9), -2);
        }
        return d;
    }

    LinearLayout panel() {
        LinearLayout p = new LinearLayout(this);
        p.setOrientation(LinearLayout.VERTICAL);
        p.setPadding(dp(24), dp(24), dp(24), dp(20));
        GradientDrawable g = new GradientDrawable();
        g.setColor(IVORY);
        g.setCornerRadius(dp(6));
        p.setBackground(g);
        return p;
    }

    void onAdd(Item item) {
        if (!item.pizza) { addToCart(item.name, item.price); return; }
        LinearLayout p = panel();
        TextView over = text("ESCOLHA O TAMANHO", 11, GOLD, SANS_MEDIUM);
        over.setLetterSpacing(0.25f);
        p.addView(over);
        TextView t = text(item.name, 24, INK, SERIF);
        t.setPadding(0, dp(4), 0, dp(12));
        p.addView(t);
        Dialog d = sheet(p);
        for (int i = 0; i < SIZES.length; i++) {
            final int s = i;
            LinearLayout opt = new LinearLayout(this);
            opt.setGravity(Gravity.CENTER_VERTICAL);
            opt.setPadding(dp(16), dp(14), dp(16), dp(14));
            GradientDrawable g = new GradientDrawable();
            g.setColor(PAPER);
            g.setStroke(dp(1), LINE);
            g.setCornerRadius(dp(4));
            opt.setBackground(g);
            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            col.addView(text(SIZES[i], 17, INK, SERIF));
            col.addView(text(SIZE_INFO[i], 12, MUTED, SANS));
            opt.addView(col, new LinearLayout.LayoutParams(0, -2, 1));
            opt.addView(text(brl.format(round2(item.price * SIZE_MULT[i])), 16, WINE, SERIF));
            LinearLayout.LayoutParams ol = new LinearLayout.LayoutParams(-1, -2);
            ol.setMargins(0, 0, 0, dp(8));
            p.addView(opt, ol);
            opt.setOnClickListener(v -> {
                addToCart(item.name + " · " + SIZES[s], round2(item.price * SIZE_MULT[s]));
                d.dismiss();
            });
        }
        TextView cancel = text("CANCELAR", 12, MUTED, SANS_MEDIUM);
        cancel.setLetterSpacing(0.2f);
        cancel.setGravity(Gravity.CENTER);
        cancel.setPadding(0, dp(12), 0, dp(4));
        cancel.setOnClickListener(v -> d.dismiss());
        p.addView(cancel);
        d.show();
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
        cartBar.setVisibility(c == 0 ? View.GONE : View.VISIBLE);
        cartCount.setText((c == 1 ? "1 ITEM" : c + " ITENS") + " NO PEDIDO");
        cartTotal.setText(brl.format(total()));
    }

    void showCart() {
        if (cart.isEmpty()) return;
        LinearLayout p = panel();
        TextView over = text("SEU PEDIDO", 11, GOLD, SANS_MEDIUM);
        over.setLetterSpacing(0.25f);
        p.addView(over);
        TextView title = text("Resumo", 24, INK, SERIF);
        title.setPadding(0, dp(4), 0, dp(8));
        p.addView(title);

        ScrollView sv = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        sv.addView(box);
        p.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1));

        TextView tot = text("", 22, WINE, SERIF);
        LinearLayout totRow = new LinearLayout(this);
        totRow.setGravity(Gravity.CENTER_VERTICAL);
        totRow.setPadding(0, dp(14), 0, dp(16));
        totRow.addView(text("Total", 18, INK, SERIF), new LinearLayout.LayoutParams(0, -2, 1));
        totRow.addView(tot);
        p.addView(totRow);

        Dialog d = sheet(p);
        Runnable[] render = new Runnable[1];
        render[0] = () -> {
            box.removeAllViews();
            for (String key : new ArrayList<>(cart.keySet())) {
                LinearLayout row = new LinearLayout(this);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(0, dp(12), 0, dp(12));
                LinearLayout col = new LinearLayout(this);
                col.setOrientation(LinearLayout.VERTICAL);
                col.addView(text(key, 16, INK, SERIF));
                col.addView(text(brl.format(cart.get(key) * cartPrice.get(key)), 13, MUTED, SANS));
                row.addView(col, new LinearLayout.LayoutParams(0, -2, 1));
                row.addView(stepper("−", v -> {
                    int q = cart.get(key) - 1;
                    if (q <= 0) { cart.remove(key); cartPrice.remove(key); } else cart.put(key, q);
                    updateCart();
                    if (cart.isEmpty()) d.dismiss(); else render[0].run();
                }));
                TextView qty = text(String.valueOf(cart.get(key)), 16, INK, SERIF);
                qty.setGravity(Gravity.CENTER);
                row.addView(qty, new LinearLayout.LayoutParams(dp(32), -2));
                row.addView(stepper("+", v -> { cart.put(key, cart.get(key) + 1); updateCart(); render[0].run(); }));
                box.addView(row);
                View line = new View(this);
                line.setBackgroundColor(LINE);
                box.addView(line, new LinearLayout.LayoutParams(-1, dp(1)));
            }
            tot.setText(brl.format(total()));
            // keep list compact
            int max = (int) (getResources().getDisplayMetrics().heightPixels * 0.45);
            box.measure(View.MeasureSpec.makeMeasureSpec(dp(300), View.MeasureSpec.AT_MOST), View.MeasureSpec.UNSPECIFIED);
            sv.getLayoutParams().height = Math.min(box.getMeasuredHeight(), max);
            sv.requestLayout();
        };
        render[0].run();

        TextView confirm = pill("FINALIZAR PEDIDO", INK, IVORY, true);
        confirm.setGravity(Gravity.CENTER);
        confirm.setOnClickListener(v -> { d.dismiss(); checkout(); });
        p.addView(confirm, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout links = new LinearLayout(this);
        links.setPadding(0, dp(8), 0, 0);
        TextView clear = text("ESVAZIAR", 12, MUTED, SANS_MEDIUM);
        TextView back = text("CONTINUAR", 12, MUTED, SANS_MEDIUM);
        for (TextView l : new TextView[]{clear, back}) {
            l.setLetterSpacing(0.2f);
            l.setGravity(Gravity.CENTER);
            l.setPadding(0, dp(12), 0, dp(4));
            links.addView(l, new LinearLayout.LayoutParams(0, -2, 1));
        }
        clear.setOnClickListener(v -> { cart.clear(); cartPrice.clear(); updateCart(); d.dismiss(); });
        back.setOnClickListener(v -> d.dismiss());
        p.addView(links);
        d.show();
    }

    static final String[] PAYMENTS = {"Pix", "Cartão de crédito", "Cartão de débito", "Dinheiro", "Vale-refeição"};
    static final String[] PAYMENT_INFO = {"Chave enviada na confirmação", "Maquininha na entrega", "Maquininha na entrega", "Informe se precisa de troco", "Sodexo, Alelo, VR e Ticket"};

    EditText field(LinearLayout parent, String label, String hint, String value, int type) {
        TextView l = text(label, 10, MUTED, SANS_MEDIUM);
        l.setLetterSpacing(0.2f);
        l.setPadding(0, dp(10), 0, dp(4));
        parent.addView(l);
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setText(value);
        e.setTextSize(15);
        e.setTextColor(INK);
        e.setHintTextColor(Color.parseColor("#B5ADA3"));
        e.setTypeface(SANS);
        e.setInputType(type);
        e.setSingleLine(true);
        e.setPadding(dp(14), dp(12), dp(14), dp(12));
        GradientDrawable g = new GradientDrawable();
        g.setColor(PAPER);
        g.setStroke(dp(1), LINE);
        g.setCornerRadius(dp(4));
        e.setBackground(g);
        parent.addView(e, new LinearLayout.LayoutParams(-1, -2));
        return e;
    }

    TextView section(String s) {
        TextView t = text(s, 18, INK, SERIF);
        t.setPadding(0, dp(18), 0, 0);
        return t;
    }

    void checkout() {
        final SharedPreferences prefs = getSharedPreferences("cliente", MODE_PRIVATE);
        LinearLayout p = panel();
        TextView over = text("FINALIZAR PEDIDO", 11, GOLD, SANS_MEDIUM);
        over.setLetterSpacing(0.25f);
        p.addView(over);
        TextView title = text("Entrega e pagamento", 24, INK, SERIF);
        title.setPadding(0, dp(4), 0, dp(4));
        p.addView(title);

        ScrollView sv = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        sv.addView(box);
        p.addView(sv, new LinearLayout.LayoutParams(-1, (int) (getResources().getDisplayMetrics().heightPixels * 0.55)));

        int text = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS;
        box.addView(section("Seus dados"));
        final EditText name = field(box, "NOME COMPLETO", "Como devemos chamar você?", prefs.getString("nome", ""), text | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        final EditText phone = field(box, "TELEFONE", "(11) 99999-9999", prefs.getString("telefone", ""), InputType.TYPE_CLASS_PHONE);

        box.addView(section("Endereço de entrega"));
        final EditText street = field(box, "RUA E NÚMERO", "Ex.: Rua Augusta, 1500", prefs.getString("rua", ""), text | InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS);
        final EditText hood = field(box, "BAIRRO", "Ex.: Consolação", prefs.getString("bairro", ""), text);
        final EditText comp = field(box, "COMPLEMENTO (OPCIONAL)", "Apto, bloco, ponto de referência", prefs.getString("complemento", ""), text);

        box.addView(section("Forma de pagamento"));
        final int[] chosen = {prefs.getInt("pagamento", 0)};
        final List<LinearLayout> opts = new ArrayList<>();
        final List<View> dots = new ArrayList<>();
        final LinearLayout changeBox = new LinearLayout(this);
        changeBox.setOrientation(LinearLayout.VERTICAL);
        final Runnable[] refresh = new Runnable[1];
        for (int i = 0; i < PAYMENTS.length; i++) {
            final int idx = i;
            LinearLayout opt = new LinearLayout(this);
            opt.setGravity(Gravity.CENTER_VERTICAL);
            opt.setPadding(dp(14), dp(12), dp(14), dp(12));
            View dot = new View(this);
            opt.addView(dot, new LinearLayout.LayoutParams(dp(16), dp(16)));
            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setPadding(dp(14), 0, 0, 0);
            col.addView(text(PAYMENTS[i], 16, INK, SERIF));
            col.addView(text(PAYMENT_INFO[i], 12, MUTED, SANS));
            opt.addView(col, new LinearLayout.LayoutParams(0, -2, 1));
            LinearLayout.LayoutParams ol = new LinearLayout.LayoutParams(-1, -2);
            ol.setMargins(0, dp(8), 0, 0);
            box.addView(opt, ol);
            opt.setOnClickListener(v -> { chosen[0] = idx; refresh[0].run(); });
            opts.add(opt);
            dots.add(dot);
        }
        box.addView(changeBox);
        final EditText change = field(changeBox, "TROCO PARA (OPCIONAL)", "Ex.: R$ 100,00", "", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        refresh[0] = () -> {
            for (int i = 0; i < opts.size(); i++) {
                boolean on = i == chosen[0];
                GradientDrawable g = new GradientDrawable();
                g.setColor(on ? Color.parseColor("#FBF6EC") : PAPER);
                g.setStroke(dp(1), on ? GOLD : LINE);
                g.setCornerRadius(dp(4));
                opts.get(i).setBackground(g);
                GradientDrawable dg = new GradientDrawable();
                dg.setShape(GradientDrawable.OVAL);
                dg.setStroke(dp(on ? 5 : 1), on ? WINE : MUTED);
                dg.setColor(PAPER);
                dots.get(i).setBackground(dg);
            }
            changeBox.setVisibility(PAYMENTS[chosen[0]].equals("Dinheiro") ? View.VISIBLE : View.GONE);
        };
        refresh[0].run();

        LinearLayout totRow = new LinearLayout(this);
        totRow.setGravity(Gravity.CENTER_VERTICAL);
        totRow.setPadding(0, dp(14), 0, dp(14));
        totRow.addView(text("Total", 18, INK, SERIF), new LinearLayout.LayoutParams(0, -2, 1));
        totRow.addView(text(brl.format(total()), 22, WINE, SERIF));
        p.addView(totRow);

        Dialog d = sheet(p);
        TextView confirm = pill("CONFIRMAR PEDIDO", INK, IVORY, true);
        confirm.setGravity(Gravity.CENTER);
        confirm.setOnClickListener(v -> {
            String n = name.getText().toString().trim(), ph = phone.getText().toString().trim();
            String st = street.getText().toString().trim(), hd = hood.getText().toString().trim();
            String cp = comp.getText().toString().trim();
            if (n.isEmpty()) { name.setError("Informe seu nome"); name.requestFocus(); return; }
            if (ph.length() < 8) { phone.setError("Informe um telefone válido"); phone.requestFocus(); return; }
            if (st.isEmpty()) { street.setError("Informe rua e número"); street.requestFocus(); return; }
            if (hd.isEmpty()) { hood.setError("Informe o bairro"); hood.requestFocus(); return; }
            prefs.edit().putString("nome", n).putString("telefone", ph).putString("rua", st)
                 .putString("bairro", hd).putString("complemento", cp).putInt("pagamento", chosen[0]).apply();
            String pay = PAYMENTS[chosen[0]];
            String ch = change.getText().toString().trim();
            if (pay.equals("Dinheiro") && !ch.isEmpty()) pay += " (troco para R$ " + ch + ")";
            String addr = st + " — " + hd + (cp.isEmpty() ? "" : " (" + cp + ")");
            d.dismiss();
            finishOrder(n, ph, addr, pay);
        });
        p.addView(confirm, new LinearLayout.LayoutParams(-1, -2));
        TextView back = text("VOLTAR AO PEDIDO", 12, MUTED, SANS_MEDIUM);
        back.setLetterSpacing(0.2f);
        back.setGravity(Gravity.CENTER);
        back.setPadding(0, dp(14), 0, dp(4));
        back.setOnClickListener(v -> { d.dismiss(); showCart(); });
        p.addView(back);
        d.show();
    }

    void finishOrder(String clientName, String phone, String address, String payment) {
        StringBuilder sb = new StringBuilder("Pedido · Bella Massa Pizzeria\n\n");
        for (Map.Entry<String, Integer> e : cart.entrySet())
            sb.append(e.getValue()).append("x ").append(e.getKey()).append(" — ")
              .append(brl.format(e.getValue() * cartPrice.get(e.getKey()))).append("\n");
        sb.append("\nTotal: ").append(brl.format(total()));
        sb.append("\n\nCliente: ").append(clientName).append("\nTelefone: ").append(phone)
          .append("\nEndereço: ").append(address).append("\nPagamento: ").append(payment);
        final String msg = sb.toString();

        LinearLayout p = panel();
        p.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView over = text("PEDIDO CONFIRMADO", 11, GOLD, SANS_MEDIUM);
        over.setLetterSpacing(0.25f);
        p.addView(over);
        TextView t = text("Grazie mille", 26, INK, SERIF_ITALIC);
        t.setPadding(0, dp(6), 0, dp(6));
        p.addView(t);
        TextView body = text(clientName + ", seu pedido de " + brl.format(total()) + " será entregue em " + address + ".\nPagamento: " + payment + ".\n\nEnvie-o para a pizzaria pelo aplicativo de sua preferência.", 14, MUTED, SANS);
        body.setGravity(Gravity.CENTER);
        body.setPadding(0, 0, 0, dp(18));
        p.addView(body);
        Dialog d = sheet(p);
        TextView send = pill("ENVIAR PEDIDO", INK, IVORY, true);
        send.setGravity(Gravity.CENTER);
        send.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT, msg);
            try { startActivity(Intent.createChooser(i, "Enviar pedido")); } catch (Exception ignored) { }
            d.dismiss();
        });
        p.addView(send, new LinearLayout.LayoutParams(-1, -2));
        TextView close = text("FECHAR", 12, MUTED, SANS_MEDIUM);
        close.setLetterSpacing(0.2f);
        close.setPadding(0, dp(14), 0, dp(4));
        close.setOnClickListener(v -> d.dismiss());
        p.addView(close);
        d.setOnDismissListener(x -> { cart.clear(); cartPrice.clear(); updateCart(); });
        d.show();
    }

    // ---------- Helpers ----------

    TextView pill(String label, int bg, int fg, boolean filled) {
        TextView t = text(label, 11, fg, SANS_MEDIUM);
        t.setLetterSpacing(0.2f);
        t.setPadding(dp(16), dp(10), dp(16), dp(10));
        GradientDrawable g = new GradientDrawable();
        g.setColor(bg);
        if (!filled) g.setStroke(dp(1), INK);
        g.setCornerRadius(dp(2));
        t.setBackground(g);
        if (filled) t.setPadding(dp(20), dp(14), dp(20), dp(14));
        return t;
    }

    View stepper(String label, View.OnClickListener l) {
        TextView t = text(label, 16, INK, SANS);
        t.setGravity(Gravity.CENTER);
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setStroke(dp(1), GOLD);
        t.setBackground(g);
        t.setOnClickListener(l);
        t.setLayoutParams(new LinearLayout.LayoutParams(dp(32), dp(32)));
        return t;
    }

    TextView text(String s, int sp, int color, Typeface tf) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        t.setTypeface(tf);
        return t;
    }

    int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }
}
