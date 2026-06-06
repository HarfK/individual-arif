package com.example.individual_arif;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Displays all saved bill records in a RecyclerView (ListView-style list).
 * Shows Month and Final Cost only.
 * Each row is clickable – opens DetailActivity.
 */
public class HistoryActivity extends AppCompatActivity {

    private RecyclerView    recyclerView;
    private TextView        tvEmpty;
    private BillAdapter     adapter;
    private DatabaseHelper  dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Toolbar with back arrow
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_history);
        }

        dbHelper     = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty      = findViewById(R.id.tvEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        loadRecords();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh list when returning from DetailActivity (edit/delete)
        loadRecords();
    }

    private void loadRecords() {
        List<BillRecord> records = dbHelper.getAllBills();

        if (records.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }

        adapter = new BillAdapter(this, records);
        adapter.setOnItemClickListener(record -> {
            Intent intent = new Intent(HistoryActivity.this, DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_BILL_ID, record.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

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
