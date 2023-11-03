package io.thorenkoder.android.util;

import android.text.Editable;
import android.text.TextWatcher;

/** 
* Uility class to override one method of {@link TextWatcher} 
* @author Thoren Koder
*/
public class CallableTextWatcher implements TextWatcher {

	@Override
	public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
	}

	@Override
	public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
	}

	@Override
	public void afterTextChanged(Editable editable) {
	}
}