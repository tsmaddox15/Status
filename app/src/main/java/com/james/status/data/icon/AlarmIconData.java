package com.james.status.data.icon;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.provider.Settings;
import android.support.graphics.drawable.VectorDrawableCompat;

import com.james.status.R;
import com.james.status.data.IconStyleData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AlarmIconData extends IconData<AlarmIconData.AlarmReceiver> {

    private AlarmManager alarmManager;

    public AlarmIconData(Context context) {
        super(context);
        alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
    }

    @Override
    public AlarmReceiver getReceiver() {
        return new AlarmReceiver();
    }

    @Override
    public IntentFilter getIntentFilter() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            return new IntentFilter(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED);
        else
            return new IntentFilter("android.intent.action.ALARM_CHANGED");
    }

    @Override
    public void register() {
        super.register();

        Object alarm = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            alarm = alarmManager.getNextAlarmClock();
        else
            alarm = Settings.System.getString(getContext().getContentResolver(), android.provider.Settings.System.NEXT_ALARM_FORMATTED);

        if (alarm != null)
            onDrawableUpdate(VectorDrawableCompat.create(getContext().getResources(), getIconResource(), getContext().getTheme()));
    }

    @Override
    public int[] getDefaultIconResource() {
        return new int[]{R.drawable.ic_alarm};
    }

    @Override
    public String getTitle() {
        return getContext().getString(R.string.icon_alarm);
    }

    @Override
    public List<IconStyleData> getIconStyles() {
        List<IconStyleData> styles = new ArrayList<>();

        styles.addAll(
                Arrays.asList(
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_default),
                                R.drawable.ic_alarm
                        )
                )
        );

        return styles;
    }

    public class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Object alarm = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                alarm = alarmManager.getNextAlarmClock();
            else
                alarm = Settings.System.getString(getContext().getContentResolver(), android.provider.Settings.System.NEXT_ALARM_FORMATTED);

            if (alarm != null)
                onDrawableUpdate(VectorDrawableCompat.create(getContext().getResources(), getIconResource(), getContext().getTheme()));
            else onDrawableUpdate(null);
        }
    }
}
