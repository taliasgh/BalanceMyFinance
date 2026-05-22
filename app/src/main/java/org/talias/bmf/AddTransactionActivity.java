package org.talias.bmf;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.talias.bmf.data.AppDatabase;
import org.talias.bmf.data.Transaction;
import org.talias.bmf.data.TransactionDao;
import org.talias.bmf.util.DbExecutor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {

    private TransactionDao transactionDao;
    private EditText editAmount;
    private RadioGroup radioType;
    private Spinner spinnerCategory;
    private TextView textSelectedDate;
    private EditText editNote;

    private final Calendar selectedDate = Calendar.getInstance();
    private final SimpleDateFormat displayDateFormat =
            new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        transactionDao = AppDatabase.getInstance(this).transactionDao();

        editAmount = findViewById(R.id.edit_amount);
        radioType = findViewById(R.id.radio_type);
        spinnerCategory = findViewById(R.id.spinner_category);
        textSelectedDate = findViewById(R.id.text_selected_date);
        editNote = findViewById(R.id.edit_note);
        Button btnPickDate = findViewById(R.id.btn_pick_date);
        Button btnSave = findViewById(R.id.btn_save);

        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.categories,
                android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        updateDateLabel();

        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveTransaction());
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateLabel();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void updateDateLabel() {
        String formatted = displayDateFormat.format(selectedDate.getTime());
        textSelectedDate.setText(getString(R.string.selected_date_format, formatted));
    }

    private void saveTransaction() {
        String amountText = editAmount.getText().toString().trim();
        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            amount = 0;
        }
        if (amount <= 0) {
            Toast.makeText(this, R.string.error_amount_required, Toast.LENGTH_SHORT).show();
            return;
        }

        int checkedId = radioType.getCheckedRadioButtonId();
        if (checkedId == -1) {
            Toast.makeText(this, R.string.error_type_required, Toast.LENGTH_SHORT).show();
            return;
        }

        String type;
        if (checkedId == R.id.radio_income) {
            type = Transaction.TYPE_INCOME;
        } else {
            type = Transaction.TYPE_EXPENSE;
        }

        String category = spinnerCategory.getSelectedItem().toString();
        long dateMillis = selectedDate.getTimeInMillis();
        String note = editNote.getText().toString().trim();

        Transaction transaction = new Transaction(amount, type, category, dateMillis, note);

        DbExecutor.run(() -> {
            transactionDao.insert(transaction);
            runOnUiThread(this::finish);
        });
    }
}
