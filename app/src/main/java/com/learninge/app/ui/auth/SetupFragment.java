package com.learninge.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.chip.ChipGroup;
import com.learninge.app.R;
import com.learninge.app.databinding.FragmentSetupBinding;
import com.learninge.app.ui.common.BaseFragment;
import com.learninge.app.ui.main.MainActivity;

/**
 * SCR-04 Thiết lập lần đầu (3 bước): cấp độ → mục tiêu → nhắc học.
 */
public class SetupFragment extends BaseFragment {

    private static final int STEP_LEVEL = 0;
    private static final int STEP_GOAL = 1;
    private static final int STEP_REMINDER = 2;

    private int currentStep = STEP_LEVEL;

    public SetupFragment() {
        super(R.layout.fragment_setup);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentSetupBinding binding = FragmentSetupBinding.bind(view);
        padStatusBar(binding.headerSetup);
        showStep(binding, currentStep);

        binding.switchReminder.setOnCheckedChangeListener((btn, checked) ->
                binding.tilReminderTime.setVisibility(checked ? View.VISIBLE : View.GONE));

        binding.etReminderTime.setOnClickListener(v -> showTimePicker(binding));

        binding.btnSetupNext.setOnClickListener(v -> {
            if (currentStep < STEP_REMINDER) {
                currentStep++;
                showStep(binding, currentStep);
            } else {
                // Lưu cài đặt vào SharedPreferences (đơn giản, không cần ViewModel)
                savePrefs(binding);
                startActivity(new Intent(requireContext(), MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                requireActivity().finish();
            }
        });
    }

    private void showStep(FragmentSetupBinding binding, int step) {
        binding.stepLevel.setVisibility(step == STEP_LEVEL ? View.VISIBLE : View.GONE);
        binding.stepGoal.setVisibility(step == STEP_GOAL ? View.VISIBLE : View.GONE);
        binding.stepReminder.setVisibility(step == STEP_REMINDER ? View.VISIBLE : View.GONE);

        // Dots
        binding.dot1.setBackgroundResource(step == STEP_LEVEL
                ? R.drawable.bg_dot_active : R.drawable.bg_dot_inactive);
        binding.dot2.setBackgroundResource(step == STEP_GOAL
                ? R.drawable.bg_dot_active : R.drawable.bg_dot_inactive);
        binding.dot3.setBackgroundResource(step == STEP_REMINDER
                ? R.drawable.bg_dot_active : R.drawable.bg_dot_inactive);

        binding.btnSetupNext.setText(step == STEP_REMINDER
                ? R.string.setup_start : R.string.setup_next);
    }

    private void showTimePicker(FragmentSetupBinding binding) {
        com.google.android.material.timepicker.MaterialTimePicker picker =
                new com.google.android.material.timepicker.MaterialTimePicker.Builder()
                        .setHour(8)
                        .setMinute(0)
                        .setTitleText(R.string.setup_reminder_time)
                        .build();
        picker.addOnPositiveButtonClickListener(v -> {
            String time = String.format(java.util.Locale.getDefault(),
                    "%02d:%02d", picker.getHour(), picker.getMinute());
            binding.etReminderTime.setText(time);
        });
        picker.show(getChildFragmentManager(), "time_picker");
    }

    private void savePrefs(FragmentSetupBinding binding) {
        android.content.SharedPreferences prefs = requireContext()
                .getSharedPreferences("setup", android.content.Context.MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();

        int levelId = binding.chipGroupLevel.getCheckedChipId();
        String level = levelId == R.id.chip_a2 ? "A2" : levelId == R.id.chip_b1 ? "B1" : "A1";
        editor.putString("level", level);

        int goalId = binding.chipGroupGoal.getCheckedChipId();
        int goal = goalId == R.id.chip_goal_10 ? 10 : goalId == R.id.chip_goal_20 ? 20 : 5;
        editor.putInt("daily_goal", goal);

        boolean reminder = binding.switchReminder.isChecked();
        editor.putBoolean("reminder", reminder);
        if (reminder) {
            editor.putString("reminder_time",
                    binding.etReminderTime.getText() != null
                            ? binding.etReminderTime.getText().toString() : "08:00");
        }
        editor.apply();
    }
}
