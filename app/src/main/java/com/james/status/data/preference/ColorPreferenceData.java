package com.james.status.data.preference;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.ColorInt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.dialogs.ColorPickerDialog;
import com.james.status.dialogs.PreferenceDialog;
import com.james.status.utils.PreferenceUtils;
import com.james.status.views.CustomImageView;

public class ColorPreferenceData extends PreferenceData<Integer> {

    public int value;

    public ColorPreferenceData(Context context, Identifier identifier, @ColorInt int defaultValue, OnPreferenceChangeListener<Integer> listener) {
        super(context, identifier, listener);

        Integer value = PreferenceUtils.getIntegerPreference(getContext(), identifier.getPreference());
        if (value == null) value = defaultValue;
        this.value = value;
    }

    @Override
    public ViewHolder getViewHolder(LayoutInflater inflater, ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.item_preference_color, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        TextView title = (TextView) holder.v.findViewById(R.id.title);
        CustomImageView color = (CustomImageView) holder.v.findViewById(R.id.color);

        title.setText(getIdentifier().getTitle());
        color.setImageDrawable(new ColorDrawable(value));

        holder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new ColorPickerDialog(getContext()).setPreference(value).setDefaultPreference(Color.BLACK).setListener(new PreferenceDialog.OnPreferenceListener<Integer>() {
                    @Override
                    public void onPreference(PreferenceDialog dialog, Integer color) {
                        value = color;
                        ((CustomImageView) holder.v.findViewById(R.id.color)).transition(new ColorDrawable(color));

                        PreferenceUtils.PreferenceIdentifier identifier = getIdentifier().getPreference();
                        if (identifier != null)
                            PreferenceUtils.putPreference(getContext(), getIdentifier().getPreference(), color);
                        onPreferenceChange(color);
                    }

                    @Override
                    public void onCancel(PreferenceDialog dialog) {
                    }
                });

                dialog.setTitle(getIdentifier().getTitle());

                dialog.show();
            }
        });
    }

}
