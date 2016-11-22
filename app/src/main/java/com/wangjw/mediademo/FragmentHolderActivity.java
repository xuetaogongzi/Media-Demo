package com.wangjw.mediademo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;


/**
 * Created by Devin.Fu on 11/16/15.
 * 容纳一个fragment，类似于FragmentDialog.
 */
public class FragmentHolderActivity extends AppCompatActivity {

	public final static String KEY_FRAGMENT_TYPE = "fragment_type";
	public final static String KEY_FRAGMENT_DATA = "fragment_data";

	private Fragment mFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_fragment_holder);

		if (savedInstanceState == null) {

			Intent tIntent = getIntent();
			Class<?> tFragmentClass = (Class<?>) tIntent.getSerializableExtra(KEY_FRAGMENT_TYPE);
			Bundle tData = tIntent.getBundleExtra(KEY_FRAGMENT_DATA);

			try {
				mFragment = (Fragment) tFragmentClass.newInstance();
			} catch (Exception e) {
				throw new IllegalArgumentException("Fragment holded in FragmentHolderActivity " +
						"must be subclass of Fragment.");
			}

			if (mFragment != null && tData != null)
				mFragment.setArguments(tData);

			if (mFragment != null) {
				getSupportFragmentManager().beginTransaction()
						.add(R.id.fragmentHolderActivity_container_fl, mFragment).commitAllowingStateLoss();
			}
		}
	}
}
