package org.talias.bmf;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.talias.bmf.data.AppDatabase;
import org.talias.bmf.data.Transaction;
import org.talias.bmf.data.TransactionDao;
import org.talias.bmf.util.DbExecutor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private TransactionDao transactionDao;
    private TextView textBalance;
    private TextView textEmpty;
    private ListView listRecentTransactions;
    private final List<Transaction> displayedTransactions = new ArrayList<>();
    private ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dashboard_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        transactionDao = AppDatabase.getInstance(this).transactionDao();

        textBalance = findViewById(R.id.text_balance);
        textEmpty = findViewById(R.id.text_empty);
        listRecentTransactions = findViewById(R.id.list_recent_transactions);

        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listRecentTransactions.setAdapter(listAdapter);

        listRecentTransactions.setOnItemLongClickListener((parent, view, position, id) -> {
            confirmDelete(displayedTransactions.get(position));
            return true;
        });

        Button btnAdd = findViewById(R.id.btn_add_transaction);
        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddTransactionActivity.class)));

        Button btnReport = findViewById(R.id.btn_monthly_report);
        btnReport.setOnClickListener(v ->
                startActivity(new Intent(this, MonthlyReportActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboard();
    }

    private void loadDashboard() {
        DbExecutor.run(() -> {
            List<Transaction> all = transactionDao.getAllNewestFirst();
            double balance = 0;
            for (Transaction t : all) {
                if (Transaction.TYPE_INCOME.equals(t.type)) {
                    balance += t.amount;
                } else {
                    balance -= t.amount;
                }
            }

            displayedTransactions.clear();
            int limit = Math.min(10, all.size());
            for (int i = 0; i < limit; i++) {
                displayedTransactions.add(all.get(i));
            }

            List<String> lines = new ArrayList<>();
            for (Transaction t : displayedTransactions) {
                lines.add(formatTransaction(t));
            }

            final double balanceFinal = balance;
            final List<String> linesFinal = lines;
            runOnUiThread(() -> updateUi(balanceFinal, linesFinal));
        });
    }

    private void updateUi(double balance, List<String> lines) {
        textBalance.setText(getString(R.string.balance_format, balance));

        boolean empty = lines.isEmpty();
        textEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        listRecentTransactions.setVisibility(empty ? View.GONE : View.VISIBLE);

        listAdapter.clear();
        listAdapter.addAll(lines);
        listAdapter.notifyDataSetChanged();
    }

    private void confirmDelete(Transaction transaction) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_dialog_title)
                .setMessage(R.string.delete_dialog_message)
                .setPositiveButton(R.string.delete_confirm, (dialog, which) ->
                        DbExecutor.run(() -> {
                            transactionDao.delete(transaction);
                            runOnUiThread(this::loadDashboard);
                        }))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    static String formatTransaction(Transaction t) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
        String date = dateFormat.format(new Date(t.dateMillis));
        String typeSign = Transaction.TYPE_INCOME.equals(t.type) ? "+" : "-";
        return String.format(Locale.getDefault(), "%s  %s  %s  %.2f", date, t.category, typeSign, t.amount);
    }
}
