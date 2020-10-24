package com.msg91.sendotp.sample.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.ContextCompat;

import com.msg91.sendotp.sample.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Copyright (C) sendotp-android - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * <p>
 * Created by Samset on 23,October,2020 at 6:34 PM for sendotp-android.
 * <p>
 * New Delhi,India
 */

public class CountrySpinner extends AppCompatSpinner {
    private Map<String, String> mCountries = new TreeMap<>();
    private List<CountryIsoSelectedListener> mListeners = new ArrayList<>();

    public CountrySpinner(Context context) {
        super(context);
    }

    public CountrySpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(String defaultCountry) {
        initCountries();

        List<String> countryList = new ArrayList<>(mCountries.keySet());
        countryList.remove(defaultCountry);
        countryList.add(0, defaultCountry);

        ArrayAdapter adapter = new ArrayAdapter<>(getContext(), R.layout.simple_spinner_item, countryList);

        setAdapter(adapter);

        setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                final String selectedCountry = (String) adapterView.getItemAtPosition(position);
                TextView textView = (TextView) view;
                textView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                notifyListeners(selectedCountry);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    public void addCountryIsoSelectedListener(CountryIsoSelectedListener listener) {
        mListeners.add(listener);
    }

    private void initCountries() {
        String[] isoCountryCodes = Locale.getISOCountries();
        for (String iso : isoCountryCodes) {
            String country = new Locale("", iso).getDisplayCountry();
            mCountries.put(country, iso);
        }
    }

    private void notifyListeners(String selectedCountry) {
        final String selectedIso = mCountries.get(selectedCountry);
        for (CountryIsoSelectedListener listener : mListeners) {
            listener.onCountryIsoSelected(selectedIso);
        }
    }

    public interface CountryIsoSelectedListener {
        void onCountryIsoSelected(String iso);
    }
}
