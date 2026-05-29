package com.jfcardenas.musicwall.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.jfcardenas.musicwall.R
import com.jfcardenas.musicwall.service.CollageWallpaper

class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private var isDirty = false

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = CollageWallpaper.PREFS_NAME
        setPreferencesFromResource(R.xml.preferences, rootKey)

        findPreference<Preference>("refresh_now")?.setOnPreferenceClickListener {
            triggerRefresh()
            true
        }
    }

    override fun onStart() {
        super.onStart()
        isDirty = false
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
        if (isDirty) triggerRefresh()
        super.onStop()
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String?) {
        isDirty = true
    }

    private fun triggerRefresh() {
        requireContext().sendBroadcast(Intent(Intent.ACTION_RUN))
        Toast.makeText(requireContext(), "Generando collage…", Toast.LENGTH_SHORT).show()
        isDirty = false
    }
}
