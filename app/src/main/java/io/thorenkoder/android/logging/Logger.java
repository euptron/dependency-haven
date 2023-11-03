package io.thorenkoder.android.logging;

import android.app.Activity;
import android.text.style.ForegroundColorSpan;
import android.text.Spannable;
import android.text.SpannableString;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import java.util.ArrayList;
import java.util.List;

public class Logger {

  private boolean mAttached;

  private LogViewModel model;
  private ViewModelStoreOwner activity;

  public void attach(ViewModelStoreOwner activity) {
    this.activity = activity;
    model = new ViewModelProvider(activity).get(LogViewModel.class);
    mAttached = true;
  }

  public void d(String tag, String message) {
    if (!mAttached) {
      return;
    }
    add(new Log(tag, message));
  }

  public void e(String tag, String message) {
    if (!mAttached) {
      return;
    }
    Spannable messageSpan = new SpannableString(message);
    messageSpan.setSpan(
        new ForegroundColorSpan(0xffff0000),
        0,
        message.length(),
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

    add(new Log(tag, messageSpan));
  }

  public void w(String tag, String message) {
    if (!mAttached) {
      return;
    }
    Spannable messageSpan = new SpannableString(message);
    messageSpan.setSpan(
        new ForegroundColorSpan(0xffff7043),
        0,
        message.length(),
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

    add(new Log(tag, messageSpan));
  }

  public void p(String tag, String message) {
    if (!mAttached) {
      return;
    }
    Spannable tagSpan = new SpannableString(tag);
    tagSpan.setSpan(
        new ForegroundColorSpan(0xFF0D47A1), 0, tag.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

    add(new Log(tagSpan, message));
  }

  private void add(Log log) {
    ArrayList<Log> currentList = model.getLogs().getValue();
    if (currentList == null) {
      currentList = new ArrayList<>();
    }
    currentList.add(log);
    model.getLogs().postValue(currentList);
  }

  public void clear() {
    model.getLogs().setValue(new ArrayList<>());
  }
}
