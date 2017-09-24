package com.example.kystatham.nytimessearch.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.kystatham.nytimessearch.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class FilterActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private final String ARTS = "Arts";
    private final String FASHION_STYLE = "Fashion & Style";
    private final String SPORTS = "Sports";
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yy");

    public List<String> checkedNewsDesks = new ArrayList<>();
    public Date date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    public void onFilterSave(View view) {
        Spinner sortOrder = (Spinner) findViewById(R.id.spnSort);
        Intent data = new Intent();
        data.putExtra("sort", sortOrder.getSelectedItem().toString().toLowerCase());
        data.putExtra("news_desk", getStringExtraFromChecked(checkedNewsDesks));
        data.putExtra("begin_date", date);
        setResult(RESULT_OK, data);
        finish();
    }

    public String getStringExtraFromChecked(List<String> checked) {
        if (checked.size() == 0) return "";
        StringBuilder checkedString = new StringBuilder("(");
        for (int i = 0; i < checked.size(); i++) {
            checkedString.append("\"");
            checkedString.append(checked.get(i));
            checkedString.append("\"");
            if (i != checked.size() - 1) {
                checkedString.append(" ");
            }
        }
        checkedString.append(")");
        return checkedString.toString();
    }

    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();

        switch (view.getId()) {
            case R.id.cbArts:
                if (checked) {
                    checkedNewsDesks.add(ARTS);
                } else {
                    checkedNewsDesks.remove(ARTS);
                }
                break;
            case R.id.cbFashionStyle:
                if (checked) {
                    checkedNewsDesks.add(FASHION_STYLE);
                } else {
                    checkedNewsDesks.remove(FASHION_STYLE);
                }
                break;
            case R.id.cbSports:
                if (checked) {
                    checkedNewsDesks.add(SPORTS);
                } else {
                    checkedNewsDesks.remove(SPORTS);
                }
                break;
        }
    }

    public void showDatePickerDialog(View v) {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        final Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, monthOfYear);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        date = c.getTime();
        ((EditText) findViewById(R.id.etBeginDate)).setText(simpleDateFormat.format(date));
    }
}
