package com.github.tianma8023.smscode.app;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.tianma8023.smscode.R;
import com.github.tianma8023.smscode.constant.IPrefConstants;
import com.github.tianma8023.smscode.service.accessibility.SmsCodeAutoInputService;
import com.github.tianma8023.smscode.utils.AccessibilityUtils;
import com.github.tianma8023.smscode.utils.ShellUtils;

public class AutoInputSettingsFragment extends BasePreferenceFragment implements Preference.OnPreferenceChangeListener {

    private HomeActivity mHomeActivity;

    private SwitchPreference mAutoInputPreference;
    private SwitchPreference mAccessibilityModePreference;
    private SwitchPreference mRootModePreference;
    private ListPreference mFocusModePreference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings_auto_input_code);


        mAutoInputPreference = (SwitchPreference) findPreference(IPrefConstants.KEY_ENABLE_AUTO_INPUT_CODE);
        mAutoInputPreference.setOnPreferenceChangeListener(this);

        mAccessibilityModePreference = (SwitchPreference) findPreference(IPrefConstants.KEY_AUTO_INPUT_MODE_ACCESSIBILITY);
        mAccessibilityModePreference.setOnPreferenceChangeListener(this);

        mRootModePreference = (SwitchPreference) findPreference(IPrefConstants.KEY_AUTO_INPUT_MODE_ROOT);
        mRootModePreference.setOnPreferenceChangeListener(this);

        mFocusModePreference = (ListPreference) findPreference(IPrefConstants.KEY_FOCUS_MODE);
        mFocusModePreference.setOnPreferenceChangeListener(this);
        refreshFocusModePreference(mFocusModePreference.getValue());

        refreshEnableAutoInputPreference(mAutoInputPreference.isChecked());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mHomeActivity = (HomeActivity) getActivity();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (IPrefConstants.KEY_ENABLE_AUTO_INPUT_CODE.equals(key)) {
            refreshEnableAutoInputPreference((Boolean) newValue);
        } else if (IPrefConstants.KEY_AUTO_INPUT_MODE_ACCESSIBILITY.equals(key)) {
            onAccessibilityModeSwitched((Boolean) newValue);
        } else if (IPrefConstants.KEY_AUTO_INPUT_MODE_ROOT.equals(key)) {
            onRootModeSwitched((Boolean) newValue);
        } else if (IPrefConstants.KEY_FOCUS_MODE.equals(key)) {
            refreshFocusModePreference((String) newValue);
        } else {
            return false;
        }
        return true;
    }

    private void onAccessibilityModeSwitched(boolean enable) {
        boolean accessibilityEnabled = AccessibilityUtils.checkAccessibilityEnabled(getActivity(),
                AccessibilityUtils.getServiceId(SmsCodeAutoInputService.class));

        if (accessibilityEnabled != enable) {
            new MaterialDialog.Builder(mHomeActivity)
                    .title(enable ? R.string.open_auto_input_accessibility : R.string.close_auto_input_accessibility)
                    .content(enable ? R.string.open_auto_input_accessibility_prompt : R.string.close_auto_input_accessibility_prompt)
                    .positiveText(enable ? R.string.go_to_open : R.string.go_to_close)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            AccessibilityUtils.gotoAccessibility(mHomeActivity);
                        }
                    })
                    .show();
        }
        if (enable) {
            mRootModePreference.setChecked(false);
            mAutoInputPreference.setSummary(R.string.pref_auto_input_mode_accessibility);
        } else {
            mAutoInputPreference.setSummary(R.string.pref_enable_auto_input_code_summary);
        }
    }

    private void onRootModeSwitched(boolean enable) {
        if (enable) {
            new MaterialDialog.Builder(mHomeActivity)
                    .title(R.string.acquire_root_permission)
                    .content(R.string.acquire_root_permission_prompt)
                    .positiveText(R.string.okay)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            ShellUtils.checkRootPermission();
                        }
                    })
                    .show();

            mAccessibilityModePreference.setChecked(false);
            mAutoInputPreference.setSummary(R.string.pref_auto_input_mode_root);
        } else {
            mAutoInputPreference.setSummary(R.string.pref_enable_auto_input_code_summary);
        }
    }

    private void refreshEnableAutoInputPreference(boolean autoInputEnabled) {
        if (!autoInputEnabled) {
            mAutoInputPreference.setSummary(R.string.pref_entry_auto_input_code_summary);
        } else {
            boolean accessibilityModeChecked = mAccessibilityModePreference.isChecked();
            boolean rootModeChecked = mRootModePreference.isChecked();
            int summaryId;
            if (accessibilityModeChecked) {
                summaryId = R.string.pref_auto_input_mode_accessibility;
            } else if (rootModeChecked) {
                summaryId = R.string.pref_auto_input_mode_root;
            } else {
                summaryId = R.string.pref_enable_auto_input_code_summary;
            }
            mAutoInputPreference.setSummary(summaryId);
        }
    }

    private void refreshFocusModePreference(String newValue) {
        if (TextUtils.isEmpty(newValue))
            return;
        CharSequence[] entries = mFocusModePreference.getEntries();
        int index = mFocusModePreference.findIndexOfValue(newValue);
        try {
            mFocusModePreference.setSummary(entries[index]);
        } catch (Exception e) {
            //ignore
        }
    }

}
