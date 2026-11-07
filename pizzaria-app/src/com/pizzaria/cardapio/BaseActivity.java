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

public abstract class BaseActivity extends Activity {

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
