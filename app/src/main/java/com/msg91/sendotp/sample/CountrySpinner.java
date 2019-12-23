package com.msg91.sendotp.sample;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class CountrySpinner extends android.support.v7.widget.AppCompatSpinner {
    private Map<String, String> mCountries = new TreeMap<String, String>();
    private List<CountryIsoSelectedListener> mListeners = new ArrayList<>();

    public CountrySpinner(Context context) {
        super(context);
    }

    public CountrySpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(String defaultCountry) {
        initCountries();
        List<String> countryList = new ArrayList<String>();

        countryList.addAll(mCountries.keySet());
        countryList.remove(defaultCountry);
        countryList.add(0, defaultCountry);

        ArrayAdapter adapter = new ArrayAdapter<>(getContext(), R.layout.simple_spinner_item, countryList);

        setAdapter(adapter);

        setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                final String selectedCountry = (String) adapterView.getItemAtPosition(position);
                TextView textView = (TextView) view;
                textView.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
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

    public void removeCountryIsoSelectedListener(CountryIsoSelectedListener listener) {
        mListeners.remove(listener);
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
