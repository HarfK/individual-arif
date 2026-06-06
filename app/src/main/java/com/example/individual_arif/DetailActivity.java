package com.example.individual_arif;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

/**
 * Shows full details of a single bill record.
 * Allows the user to EDIT or DELETE the record.
 * When editing, recalculates charges using BillCalculator.
 */
public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_BILL_ID = "bill_id";

    // ── Views – view mode ──
    private TextView tvDetailMonth;
    private TextView tvDetailUnits;
    private TextView tvDetailRebate;
    private TextView tvDetailTotal;
    private TextView tvDetailFinal;

    // ── Views – edit mode ──
    private Spinner           spinnerEditMonth;
    private TextInputLayout   tilEditUnits;
    private TextInputEditText etEditUnits;
    private SeekBar           seekEditRebate;
    private TextView          tvEditRebateValue;
    private MaterialCardView  cardEditResult;
    private TextView          tvEditTotal;
    private TextView          tvEditFinal;

    // ── Containers ──
    private View viewModeLayout;
    private View editModeLayout;

    // ── Buttons ──
    private Button btnEdit;
    private Button btnDelete;
    private Button btnUpdate;
    private Button btnCancel;
    private Button btnRecalculate;

    // ── Data ──
    private long           billId;
    private BillRecord     record;
    private DatabaseHelper dbHelper;
    private double         editTotalCharges;
    private double         editFinalCost;
    private boolean        editCalculated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_detail);
        }

        billId   = getIntent().getLongExtra(EXTRA_BILL_ID, -1L);
        dbHelper = new DatabaseHelper(this);

        bindViews();
        setupEditMonthSpinner();
        setupEditSeekBar();

        if (billId == -1L) {
            Toast.makeText(this, "Invalid record.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        record = dbHelper.getBillById(billId);
        if (record == null) {
            Toast.makeText(this, "Record not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        populateViewMode();
        showViewMode();

        btnEdit.setOnClickListener(v -> enterEditMode());
        btnDelete.setOnClickListener(v -> confirmDelete());
        btnRecalculate.setOnClickListener(v -> recalculate());
        btnUpdate.setOnClickListener(v -> updateRecord());
        btnCancel.setOnClickListener(v -> {
            showViewMode();
            populateViewMode();
        });
    }

    // ─────────────────────── Bind ───────────────────────

    private void bindViews() {
        // View mode
        tvDetailMonth  = findViewById(R.id.tvDetailMonth);
        tvDetailUnits  = findViewById(R.id.tvDetailUnits);
        tvDetailRebate = findViewById(R.id.tvDetailRebate);
        tvDetailTotal  = findViewById(R.id.tvDetailTotal);
        tvDetailFinal  = findViewById(R.id.tvDetailFinal);
        viewModeLayout = findViewById(R.id.viewModeLayout);
        btnEdit        = findViewById(R.id.btnEdit);
        btnDelete      = findViewById(R.id.btnDelete);

        // Edit mode
        spinnerEditMonth  = findViewById(R.id.spinnerEditMonth);
        tilEditUnits      = findViewById(R.id.tilEditUnits);
        etEditUnits       = findViewById(R.id.etEditUnits);
        seekEditRebate    = findViewById(R.id.seekEditRebate);
        tvEditRebateValue = findViewById(R.id.tvEditRebateValue);
        cardEditResult    = findViewById(R.id.cardEditResult);
        tvEditTotal       = findViewById(R.id.tvEditTotal);
        tvEditFinal       = findViewById(R.id.tvEditFinal);
        editModeLayout    = findViewById(R.id.editModeLayout);
        btnRecalculate    = findViewById(R.id.btnRecalculate);
        btnUpdate         = findViewById(R.id.btnUpdate);
        btnCancel         = findViewById(R.id.btnCancel);
    }

    // ─────────────────────── Spinner / SeekBar for edit ───────────────────────

    private void setupEditMonthSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.months, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEditMonth.setAdapter(adapter);
    }

    private void setupEditSeekBar() {
        seekEditRebate.setMax(5);
        seekEditRebate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvEditRebateValue.setText(progress + "%");
                editCalculated = false;
                cardEditResult.setVisibility(View.GONE);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar)  { }
        });
    }

    // ─────────────────────── Populate / mode switch ───────────────────────

    private void populateViewMode() {
        tvDetailMonth.setText(record.getMonth());
        tvDetailUnits.setText(String.format(Locale.getDefault(), "%.0f kWh", record.getUnits()));
        tvDetailRebate.setText(String.format(Locale.getDefault(), "%.0f%%", record.getRebate()));
        tvDetailTotal.setText(String.format(Locale.getDefault(), "RM %.2f", record.getTotalCharges()));
        tvDetailFinal.setText(String.format(Locale.getDefault(), "RM %.2f", record.getFinalCost()));
    }

    private void showViewMode() {
        viewModeLayout.setVisibility(View.VISIBLE);
        editModeLayout.setVisibility(View.GONE);
    }

    private void showEditMode() {
        viewModeLayout.setVisibility(View.GONE);
        editModeLayout.setVisibility(View.VISIBLE);
    }

    private void enterEditMode() {
        // Pre-fill edit fields from current record
        String[] months = getResources().getStringArray(R.array.months);
        for (int i = 0; i < months.length; i++) {
            if (months[i].equals(record.getMonth())) {
                spinnerEditMonth.setSelection(i);
                break;
            }
        }
        etEditUnits.setText(String.format(Locale.getDefault(), "%.0f", record.getUnits()));
        int rebateInt = (int) record.getRebate();
        seekEditRebate.setProgress(rebateInt);
        tvEditRebateValue.setText(rebateInt + "%");
        cardEditResult.setVisibility(View.GONE);
        editCalculated = false;
        showEditMode();
    }

    // ─────────────────────── Recalculate (edit mode) ───────────────────────

    private void recalculate() {
        String unitsStr = etEditUnits.getText() == null ? "" : etEditUnits.getText().toString().trim();

        if (unitsStr.isEmpty()) {
            tilEditUnits.setError(getString(R.string.error_units_empty));
            return;
        }
        double units;
        try {
            units = Double.parseDouble(unitsStr);
        } catch (NumberFormatException e) {
            tilEditUnits.setError(getString(R.string.error_units_range));
            return;
        }
        if (units < 1 || units > 1000) {
            tilEditUnits.setError(getString(R.string.error_units_range));
            return;
        }
        tilEditUnits.setError(null);

        double rebate = seekEditRebate.getProgress();
        double[] result = BillCalculator.calculate(units, rebate);
        editTotalCharges = result[0];
        editFinalCost    = result[1];
        editCalculated   = true;

        tvEditTotal.setText(String.format(Locale.getDefault(), "RM %.2f", editTotalCharges));
        tvEditFinal.setText(String.format(Locale.getDefault(), "RM %.2f", editFinalCost));
        cardEditResult.setVisibility(View.VISIBLE);
    }

    // ─────────────────────── Update ───────────────────────

    private void updateRecord() {
        if (!editCalculated) {
            Toast.makeText(this, R.string.error_calculate_first, Toast.LENGTH_SHORT).show();
            return;
        }

        String month  = spinnerEditMonth.getSelectedItem().toString();
        double units  = Double.parseDouble(etEditUnits.getText().toString().trim());
        double rebate = seekEditRebate.getProgress();

        int rows = dbHelper.updateBill(billId, month, units, rebate,
                editTotalCharges, editFinalCost);

        if (rows > 0) {
            record = dbHelper.getBillById(billId);
            Toast.makeText(this, R.string.msg_updated, Toast.LENGTH_SHORT).show();
            populateViewMode();
            showViewMode();
        } else {
            Toast.makeText(this, "Update failed.", Toast.LENGTH_SHORT).show();
        }
    }

    // ─────────────────────── Delete ───────────────────────

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_msg)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    dbHelper.deleteBill(billId);
                    Toast.makeText(this, R.string.msg_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    // ─────────────────────── Back ───────────────────────

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) dbHelper.close();
        super.onDestroy();
    }
}
