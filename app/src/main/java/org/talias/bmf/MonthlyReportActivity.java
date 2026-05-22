package org.talias.bmf;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.talias.bmf.data.AppDatabase;
import org.talias.bmf.data.Transaction;
import org.talias.bmf.data.TransactionDao;
import org.talias.bmf.util.DbExecutor;
import org.talias.bmf.util.MonthRange;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MonthlyReportActivity extends AppCompatActivity {

    private TransactionDao transactionDao;
    private Spinner spinnerMonth;
    private Spinner spinnerYear;
    private TextView textIncomeTotal;
    private TextView textExpenseTotal;
    private TextView textNetTotal;
    private TextView textEmpty;
    private ListView listMonthTransactions;
    private final List<Transaction> displayedTransactions = new ArrayList<>();
    private ArrayAdapter<String> listAdapter;
    private int[] yearValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_report);

        transactionDao = AppDatabase.getInstance(this).transactionDao();

        spinnerMonth = findViewById(R.id.spinner_month);
        spinnerYear = findViewById(R.id.spinner_year);
        textIncomeTotal = findViewById(R.id.text_income_total);
        textExpenseTotal = findViewById(R.id.text_expense_total);
        textNetTotal = findViewById(R.id.text_net_total);
        textEmpty = findViewById(R.id.text_empty);
        listMonthTransactions = findViewById(R.id.list_month_transactions);
        Button btnShowReport = findViewById(R.id.btn_show_report);

        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.months,
                android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        setupYearSpinner();

        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listMonthTransactions.setAdapter(listAdapter);

        listMonthTransactions.setOnItemLongClickListener((parent, view, position, id) -> {
            confirmDelete(displayedTransactions.get(position));
            return true;
        });

        btnShowReport.setOnClickListener(v -> loadReport());

        setSpinnersToCurrentDate();
        loadReport();
    }

    private void setupYearSpinner() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        List<String> yearLabels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        for (int year = currentYear - 2; year <= currentYear + 2; year++) {
            yearLabels.add(String.valueOf(year));
            values.add(year);
        }
        yearValues = new int[values.size()];
        for (int i = 0; i < values.size(); i++) {
            yearValues[i] = values.get(i);
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                yearLabels);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);
    }

    private void setSpinnersToCurrentDate() {
        Calendar now = Calendar.getInstance();
        spinnerMonth.setSelection(now.get(Calendar.MONTH));
        int currentYear = now.get(Calendar.YEAR);
        for (int i = 0; i < yearValues.length; i++) {
            if (yearValues[i] == currentYear) {
                spinnerYear.setSelection(i);
                break;
            }
        }
    }

    private int getSelectedMonth() {
        return spinnerMonth.getSelectedItemPosition() + 1;
    }

    private int getSelectedYear() {
        return yearValues[spinnerYear.getSelectedItemPosition()];
    }

    private void loadReport() {
        int year = getSelectedYear();
        int month = getSelectedMonth();
        long start = MonthRange.startOfMonth(year, month);
        long end = MonthRange.startOfNextMonth(year, month);

        DbExecutor.run(() -> {
            double income = transactionDao.sumIncomeBetween(start, end);
            double expense = transactionDao.sumExpenseBetween(start, end);
            double net = income - expense;
            List<Transaction> monthTransactions = transactionDao.getBetween(start, end);

            displayedTransactions.clear();
            displayedTransactions.addAll(monthTransactions);

            List<String> lines = new ArrayList<>();
            for (Transaction t : displayedTransactions) {
                lines.add(DashboardActivity.formatTransaction(t));
            }

            final double incomeFinal = income;
            final double expenseFinal = expense;
            final double netFinal = net;
            final List<String> linesFinal = lines;
            runOnUiThread(() -> updateUi(incomeFinal, expenseFinal, netFinal, linesFinal));
        });
    }

    private void updateUi(double income, double expense, double net, List<String> lines) {
        textIncomeTotal.setText(getString(R.string.income_total_format, income));
        textExpenseTotal.setText(getString(R.string.expense_total_format, expense));
        textNetTotal.setText(getString(R.string.net_total_format, net));

        boolean empty = lines.isEmpty();
        textEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        listMonthTransactions.setVisibility(empty ? View.GONE : View.VISIBLE);

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
                            runOnUiThread(this::loadReport);
                        }))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
