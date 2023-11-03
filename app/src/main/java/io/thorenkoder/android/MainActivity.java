package io.thorenkoder.android;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import io.thorenkoder.android.databinding.ActivityMainBinding;
import io.thorenkoder.android.fragments.MainFragment;

public class MainActivity extends AppCompatActivity {

  private ActivityMainBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    if (getSupportFragmentManager().findFragmentByTag(MainFragment.TAG) == null) {
      getSupportFragmentManager()
          .beginTransaction()
          .replace(binding.fragmentContainer.getId(), MainFragment.newInstance())
          .commitNow();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    binding = null;
  }
}
