package io.thorenkoder.android.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.transition.MaterialSharedAxis;
import io.thorenkoder.android.databinding.FragmentCommunityBinding;
import io.thorenkoder.android.util.Constants;

public class CommunityFragment extends Fragment {

  public static final String TAG = CommunityFragment.class.getSimpleName();
  private FragmentCommunityBinding binding;

  public static CommunityFragment newInstance() {
    return new CommunityFragment();
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.Y, false));
    setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.Y, true));
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup viewgroup, Bundle savedInstanceState) {
    binding = FragmentCommunityBinding.inflate(inflater, viewgroup, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    binding.telegramGroupButton.setOnClickListener(v -> openTelegramGroup());
    binding.telegramChannelButton.setOnClickListener(v -> openTelegramChannel());
    binding.bloggerButton.setOnClickListener(v -> openBlogger());
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    this.binding = null;
  }

  void openTelegramChannel() {
    String url = Constants.TELEGRAM_CHANNEL_LINK;
    openUrl(url);
  }

  void openBlogger() {
    String url = Constants.BLOGGER_LINK;
    openUrl(url);
  }

  void openTelegramGroup() {
    String url = Constants.TELEGRAM_GROUP_LINK;
    openUrl(url);
  }

  void openUrl(String url) {
    Intent i = new Intent(Intent.ACTION_VIEW);
    i.setData(Uri.parse(url));
    startActivity(i);
  }
}
