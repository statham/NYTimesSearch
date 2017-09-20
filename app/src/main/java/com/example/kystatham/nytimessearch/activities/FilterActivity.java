package com.example.kystatham.nytimessearch.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.example.kystatham.nytimessearch.R;

public class FilterActivity extends AppCompatActivity {

    private final String ARTS = "arts";
    private final String FASHION_STYLE = "fashion & style";
    private final String SPORTS = "sports";

    public String checkedBox = "";

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
        data.putExtra("news_desk", checkedBox);
        setResult(RESULT_OK, data);
        finish();
    }

    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();

        if (!checked) {
            checkedBox = "";
        }

        switch (view.getId()) {
            case R.id.cbArts:
                if (checked) {
                    checkedBox = ARTS;
                    ((CheckBox) findViewById(R.id.cbFashionStyle)).setChecked(false);
                    ((CheckBox) findViewById(R.id.cbSports)).setChecked(false);
                }
                break;
            case R.id.cbFashionStyle:
                if (checked) {
                    checkedBox = FASHION_STYLE;
                    ((CheckBox) findViewById(R.id.cbArts)).setChecked(false);
                    ((CheckBox) findViewById(R.id.cbSports)).setChecked(false);
                }
                break;
            case R.id.cbSports:
                if (checked) {
                    checkedBox = SPORTS;
                    ((CheckBox) findViewById(R.id.cbFashionStyle)).setChecked(false);
                    ((CheckBox) findViewById(R.id.cbArts)).setChecked(false);
                }
                break;
        }
    }
}
