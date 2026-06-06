package com.example.individual_arif;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // ── Views ──
    private Spinner           spinnerMonth;
    private TextInputLayout   tilUnits;
    private TextInputEditText etUnits;
    private SeekBar           seekBarRebate;
    private TextView          tvRebateValue;
    private MaterialCardView  cardResult;
    private TextView          tvTotalCharges;
    private TextView          tvFinalCost;
    private Button            btnCalculate;
    private Button            btnSave;
    private Button            btnClear;

    // ── State ──
    private double  lastTotalCharges = 0.0;
    private double  lastFinalCost    = 0.0;
    private boolean calculated       = false;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }

        dbHelper = new DatabaseHelper(this);

        bindViews();
        setupMonthSpinner();
        setupRebateSeekBar();
        setupUnitValidation();
        setupButtons();

        // Show result card hidden initially
        cardResult.setVisibility(View.GONE);
    }

    // ─────────────────────── Menu ───────────────────────

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_history) {
            startActivity(new Intent(this, HistoryActivity.class));
            return true;
        } else if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ─────────────────────── Bind ───────────────────────

    private void bindViews() {
        spinnerMonth   = findViewById(R.id.spinnerMonth);
        tilUnits       = findViewById(R.id.tilUnits);
        etUnits        = findViewById(R.id.etUnits);
        seekBarRebate  = findViewById(R.id.seekBarRebate);
        tvRebateValue  = findViewById(R.id.tvRebateValue);
        cardResult     = findViewById(R.id.cardResult);
        tvTotalCharges = findViewById(R.id.tvTotalCharges);
        tvFinalCost    = findViewById(R.id.tvFinalCost);
        btnCalculate   = findViewById(R.id.btnCalculate);
        btnSave        = findViewById(R.id.btnSave);
        btnClear       = findViewById(R.id.btnClear);
    }

    // ─────────────────────── Spinners ───────────────────────

    private void setupMonthSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.months, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                resetResult();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    // ─────────────────────── SeekBar ───────────────────────

    private void setupRebateSeekBar() {
        // 0 – 5 steps
        seekBarRebate.setMax(5);
        seekBarRebate.setProgress(0);
        tvRebateValue.setText("0%");

        seekBarRebate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvRebateValue.setText(progress + "%");
                resetResult();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar)  { }
        });
    }

    // ─────────────────────── Unit validation ───────────────────────

    private void setupUnitValidation() {
        etUnits.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilUnits.setError(null);
                resetResult();
            }
            @Override public void afterTextChanged(Editable s) { }
        });
    }

    // ─────────────────────── Buttons ───────────────────────

    private void setupButtons() {
        btnCalculate.setOnClickListener(v -> performCalculation());
        btnSave.setOnClickListener(v -> saveRecord());
        btnClear.setOnClickListener(v -> clearAll());

        Button btnViewHistory = findViewById(R.id.btnViewHistory);
        btnViewHistory.setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));

        Button btnAbout = findViewById(R.id.btnAbout);
        btnAbout.setOnClickListener(v ->
                startActivity(new Intent(this, AboutActivity.class)));
    }

    // ─────────────────────── Calculation ───────────────────────

    private void performCalculation() {
        if (!validateInputs()) return;

        double units        = Double.parseDouble(etUnits.getText().toString().trim());
        double rebatePercent = seekBarRebate.getProgress();

        double[] result  = BillCalculator.calculate(units, rebatePercent);
        lastTotalCharges = result[0];
        lastFinalCost    = result[1];
        calculated       = true;

        tvTotalCharges.setText(String.format(Locale.getDefault(), "RM %.2f", lastTotalCharges));
        tvFinalCost.setText(String.format(Locale.getDefault(), "RM %.2f", lastFinalCost));
        cardResult.setVisibility(View.VISIBLE);
    }

    private boolean validateInputs() {
        String unitsStr = etUnits.getText() == null ? "" : etUnits.getText().toString().trim();

        if (unitsStr.isEmpty()) {
            tilUnits.setError(getString(R.string.error_units_empty));
            etUnits.requestFocus();
            return false;
        }

        double units;
        try {
            units = Double.parseDouble(unitsStr);
        } catch (NumberFormatException e) {
            tilUnits.setError(getString(R.string.error_units_range));
            etUnits.requestFocus();
            return false;
        }

        if (units < 1 || units > 1000) {
            tilUnits.setError(getString(R.string.error_units_range));
            etUnits.requestFocus();
            return false;
        }

        tilUnits.setError(null);
        return true;
    }

    // ─────────────────────── Save ───────────────────────

    private void saveRecord() {
        if (!calculated) {
            Toast.makeText(this, R.string.error_calculate_first, Toast.LENGTH_SHORT).show();
            return;
        }

        String month        = spinnerMonth.getSelectedItem().toString();
        double units        = Double.parseDouble(etUnits.getText().toString().trim());
        double rebatePercent = seekBarRebate.getProgress();

        long id = dbHelper.insertBill(month, units, rebatePercent, lastTotalCharges, lastFinalCost);

        if (id != -1) {
            Toast.makeText(this, R.string.msg_saved, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to save record.", Toast.LENGTH_SHORT).show();
        }
    }

    // ─────────────────────── Clear ───────────────────────

    private void clearAll() {
        spinnerMonth.setSelection(0);
        etUnits.setText("");
        tilUnits.setError(null);
        seekBarRebate.setProgress(0);
        tvRebateValue.setText("0%");
        cardResult.setVisibility(View.GONE);
        calculated       = false;
        lastTotalCharges = 0.0;
        lastFinalCost    = 0.0;
    }

    private void resetResult() {
        if (calculated) {
            calculated = false;
            cardResult.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) dbHelper.close();
        super.onDestroy();
    }
}
