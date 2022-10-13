package com.alecdev.calculator;

// Alec Ames #6843577
// 10/12/2022
// Calculator
// COSC 3P97

import static java.lang.String.format;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // Declare variables
    double num1;
    boolean canClear;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initText();
    }
    // save the contents of textview and num1 while rotating the screen
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble("num1", num1);
        outState.putBoolean("canClear", canClear);
        outState.putString("text", ((TextView) findViewById(R.id.textView)).getText().toString());
    }

    // restore the contents of textview and num1 after rotating
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        num1 = savedInstanceState.getDouble("num1");
        canClear = savedInstanceState.getBoolean("canClear");
        ((TextView) findViewById(R.id.textView)).setText(savedInstanceState.getString("text"));
    }

    // initializes the textview
    public void initText() {
        TextView textView = findViewById(R.id.textView);
        textView.setText("0");
        canClear = true;
    }

    // clears the textview
    public void clear(View view) {
        TextView textView = findViewById(R.id.textView);
        textView.setText("0");
        canClear = true;
    }

    // deletes the last character
    public void delete(View view) {
        TextView textView = findViewById(R.id.textView);
        String text = textView.getText().toString();
        if (text.length() > 0) {
            text = text.substring(0, text.length() - 1);
            textView.setText(text);
        }
    }

    // adds button text to textview
    public void add(View view) {
        TextView textView = findViewById(R.id.textView);
        String text = textView.getText().toString();

        // checks whats in textview and clears if is Error, Infinity, 0, or if canClear is true
        // canClear is essentially a flag used for results
        switch (text) {
            case "Error":
            case "Infinity":
                textView.setText("");
                canClear = false;
                break;
        }
        if (canClear) {
            if (view.getId() == R.id.button0 || view.getId() == R.id.button1 || view.getId() == R.id.button2 || view.getId() == R.id.button3 || view.getId() == R.id.button4 || view.getId() == R.id.button5 || view.getId() == R.id.button6 || view.getId() == R.id.button7 || view.getId() == R.id.button8 || view.getId() == R.id.button9) {
                textView.setText("");
                canClear = false;
            }
        }
        String text2 = textView.getText().toString();
        // replaces last operator if user presses another operator
        if (view.getId() == R.id.buttonPlus || view.getId() == R.id.buttonDecimal || view.getId() == R.id.buttonMultiply || view.getId() == R.id.buttonDivide) {
            if (text2.length() > 0) {
                char lastChar = text2.charAt(text.length() - 1);
                if (lastChar == '.') {
                    return;
                }
                else if (lastChar == '+' || lastChar == '×' || lastChar == '÷') {
                    text2 = text2.substring(0, text2.length() - 1);
                }
            }
        }

        textView.setText(format("%s%s", text2, ((TextView) view).getText()));
        canClear = false;
    }

    // evaluates expression when equals is clicked
    public void equals(View view) {
        TextView textView = findViewById(R.id.textView);
        String text = textView.getText().toString();
        if (text.length() > 0) {
            text = text.replace("×", "*");
            text = text.replace("−", "-");
            text = text.replace("÷", "/");
            text = text.replace("E", "*10^");
        }
        try {
            double result = eval(text);
            canClear = true;
            if (result == (int) result) {
                textView.setText(format("%s", (int) result));
            }
            else {
                // round to 14 decimal places
                textView.setText(format("%s", result));
            }
        } catch (Exception e) {
            textView.setText(R.string.error);
        }
    }

    // evaluates string as math expression using expression, factor, and term grammar
    // cited from https://gist.github.com/javadasoodeh/c06e86601096abc11e9093295c87601d
    private double eval(String text) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < text.length()) ? text.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < text.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(text.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }
                if (eat('^')) x = Math.pow(x, parseFactor());
                return x;
            }
        }.parse();
    }

    // memory functions
    public void save(View view) {
        TextView textView = findViewById(R.id.textView);
        String text = textView.getText().toString();
        if (text.length() > 0) {
            try {
                num1 = Double.parseDouble(text);
                Toast toast= Toast.makeText(getApplicationContext(),
                        "Saved!", Toast.LENGTH_SHORT); toast.show();

            } catch (Exception e) {
                Toast toast= Toast.makeText(getApplicationContext(),
                        "Not saved, invalid number!", Toast.LENGTH_SHORT); toast.show();
            }
        }
    }

    // append saved number to textview if num1 is not 0
    public void recall(View view) {
        TextView textView = findViewById(R.id.textView);
        String text = textView.getText().toString();
        if (num1 != 0) {
            switch (text) {
                case "0":
                case "Infinity":
                case "Error":
                    if (num1 == (int) num1) {
                        textView.setText(format("%s", (int) num1));
                    }
                    else {
                        textView.setText(format("%s", num1));
                    }
                    break;
                default:
                    if (num1 == (int) num1) {
                        textView.setText(format("%s%s", text, (int) num1));
                    }
                    else {
                        textView.setText(format("%s%s", text, num1));
                    }
                    break;
            }
        } else {
            Toast toast= Toast.makeText(getApplicationContext(),
                    "Nothing saved!", Toast.LENGTH_SHORT); toast.show();
        }
    }
}