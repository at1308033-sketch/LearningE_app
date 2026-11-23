package com.learninge.app.ui.exam;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.learninge.app.R;
import com.learninge.app.databinding.FragmentExamResourcesBinding;
import com.learninge.app.databinding.ItemExamRowBinding;
import com.learninge.app.ui.common.BaseFragment;
import com.learninge.app.ui.common.FeedbackBannerView;

/**
 * SCR-33 Tài nguyên chính thức: mở trang của đơn vị tổ chức thi trong trình duyệt (US-037).
 */
public class ExamResourcesFragment extends BaseFragment {

    public ExamResourcesFragment() {
        super(R.layout.fragment_exam_resources);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentExamResourcesBinding binding = FragmentExamResourcesBinding.bind(view);
        padStatusBar(binding.headerResources);
        binding.toolbarResources.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        binding.bannerResources.show(FeedbackBannerView.Kind.INFO, getString(R.string.resources_note));
        add(binding, R.string.resource_toeic, R.string.url_toeic);
        add(binding, R.string.resource_ielts, R.string.url_ielts);
        add(binding, R.string.resource_aptis, R.string.url_aptis);
    }

    private void add(FragmentExamResourcesBinding binding, @StringRes int title, @StringRes int url) {
        ItemExamRowBinding row = ItemExamRowBinding.inflate(LayoutInflater.from(binding.llResources.getContext()), binding.llResources, true);
        row.ivRow.setImageResource(R.drawable.ic_info);
        row.tvRowTitle.setText(title);
        row.tvRowNote.setText(R.string.resource_open);
        row.ivRowChevron.setImageResource(R.drawable.ic_external_link);
        row.btnRow.setVisibility(View.GONE);
        row.getRoot().setContentDescription(getString(R.string.resource_description, getString(title)));
        row.getRoot().setOnClickListener(v -> open(v, getString(url)));
    }

    private void open(View anchor, String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException noBrowser) {
            Snackbar.make(anchor, R.string.resource_no_browser, Snackbar.LENGTH_LONG).show();
        }
    }
}
