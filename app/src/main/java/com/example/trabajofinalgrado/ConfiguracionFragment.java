package com.example.trabajofinalgrado;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import androidx.appcompat.app.AppCompatDelegate;

public class ConfiguracionFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        SwitchPreferenceCompat switchOscuro = findPreference("tema_oscuro");
        if (switchOscuro != null) {
            switchOscuro.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean oscuro = (Boolean) newValue;
                AppCompatDelegate.setDefaultNightMode(
                        oscuro ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
                requireActivity().recreate();
                return true;
            });
        }
    }
}