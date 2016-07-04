package com.simplemobiletools.calendar.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.simplemobiletools.calendar.Constants;
import com.simplemobiletools.calendar.DBHelper;
import com.simplemobiletools.calendar.Formatter;
import com.simplemobiletools.calendar.R;
import com.simplemobiletools.calendar.Utils;
import com.simplemobiletools.calendar.models.Event;

import org.joda.time.DateTime;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EventActivity extends AppCompatActivity implements DBHelper.DBOperationsListener {
    @BindView(R.id.event_start_date) TextView mStartDate;
    @BindView(R.id.event_start_time) TextView mStartTime;
    @BindView(R.id.event_end_date) TextView mEndDate;
    @BindView(R.id.event_end_time) TextView mEndTime;
    @BindView(R.id.event_title) EditText mTitleET;
    @BindView(R.id.event_description) EditText mDescriptionET;

    private DateTime mEventStartDateTime;
    private DateTime mEventEndDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        ButterKnife.bind(this);

        final Intent intent = getIntent();
        if (intent == null)
            return;

        final String dayCode = intent.getStringExtra(Constants.DAY_CODE);
        if (dayCode == null || dayCode.isEmpty())
            return;

        mEventStartDateTime = Formatter.getDateTime(dayCode);
        updateStartDate();
        updateStartTime();

        mEventEndDateTime = Formatter.getDateTime(dayCode);
        updateEndDate();
        updateEndTime();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                saveEvent();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveEvent() {
        final String title = mTitleET.getText().toString().trim();
        if (title.isEmpty()) {
            Utils.showToast(getApplicationContext(), R.string.title_empty);
            mTitleET.requestFocus();
            return;
        }

        final int startTS = (int) (mEventStartDateTime.getMillis() / 1000);
        final int endTS = (int) (mEventEndDateTime.getMillis() / 1000);

        if (startTS > endTS) {
            Utils.showToast(getApplicationContext(), R.string.end_before_start);
            return;
        }

        final String description = mDescriptionET.getText().toString().trim();
        final Event event = new Event(0, startTS, endTS, title, description);
        DBHelper.newInstance(getApplicationContext(), this).insert(event);
    }

    private void updateStartDate() {
        mStartDate.setText(Formatter.getEventDate(mEventStartDateTime));
    }

    private void updateStartTime() {
        mStartTime.setText(Formatter.getEventTime(mEventStartDateTime));
    }

    private void updateEndDate() {
        mEndDate.setText(Formatter.getEventDate(mEventEndDateTime));
    }

    private void updateEndTime() {
        mEndTime.setText(Formatter.getEventTime(mEventEndDateTime));
    }

    @OnClick(R.id.event_start_date)
    public void startDateClicked(View view) {
        new DatePickerDialog(this, startDateSetListener, mEventStartDateTime.getYear(), mEventStartDateTime.getMonthOfYear() - 1,
                mEventStartDateTime.getDayOfMonth()).show();
    }

    @OnClick(R.id.event_start_time)
    public void startTimeClicked(View view) {
        new TimePickerDialog(this, startTimeSetListener, mEventStartDateTime.getHourOfDay(), mEventStartDateTime.getMinuteOfHour(), true)
                .show();
    }

    @OnClick(R.id.event_end_date)
    public void endDateClicked(View view) {
        new DatePickerDialog(this, endDateSetListener, mEventEndDateTime.getYear(), mEventEndDateTime.getMonthOfYear() - 1,
                mEventEndDateTime.getDayOfMonth()).show();
    }

    @OnClick(R.id.event_end_time)
    public void endTimeClicked(View view) {
        new TimePickerDialog(this, endTimeSetListener, mEventEndDateTime.getHourOfDay(), mEventEndDateTime.getMinuteOfHour(), true).show();
    }

    private final DatePickerDialog.OnDateSetListener startDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            dateSet(year, monthOfYear, dayOfMonth, true);
        }
    };

    private TimePickerDialog.OnTimeSetListener startTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            timeSet(hourOfDay, minute, true);
        }
    };

    private DatePickerDialog.OnDateSetListener endDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            dateSet(year, monthOfYear, dayOfMonth, false);
        }
    };

    private TimePickerDialog.OnTimeSetListener endTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            timeSet(hourOfDay, minute, false);
        }
    };

    private void dateSet(int year, int month, int day, boolean isStart) {
        if (isStart) {
            mEventStartDateTime = mEventStartDateTime.withYear(year).withMonthOfYear(month + 1).withDayOfMonth(day);
            updateStartDate();
        } else {
            mEventEndDateTime = mEventEndDateTime.withYear(year).withMonthOfYear(month + 1).withDayOfMonth(day);
            updateEndDate();
        }
    }

    private void timeSet(int hours, int minutes, boolean isStart) {
        if (isStart) {
            mEventStartDateTime = mEventStartDateTime.withHourOfDay(hours).withMinuteOfHour(minutes);
            updateStartTime();
        } else {
            mEventEndDateTime = mEventEndDateTime.withHourOfDay(hours).withMinuteOfHour(minutes);
            updateEndTime();
        }
    }

    @Override
    public void eventInserted() {
        Utils.showToast(getApplicationContext(), R.string.event_added);
        finish();
    }
}
