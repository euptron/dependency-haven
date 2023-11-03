package io.thorenkoder.android.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.util.ArrayList;
import java.util.List;

public class PagerAdapter extends FragmentStateAdapter {
	
	private List<Fragment> fragmentList = new ArrayList<>();

	public PagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
		super(fragmentManager, lifecycle);
	}

	@Override
	public int getItemCount() {
		return fragmentList.size();
	}

	@Override
	public Fragment createFragment(int position) {
		return fragmentList.get(position);
	}

	public void addFragment(Fragment fragment) {
		fragmentList.add(fragment);
	}
}
