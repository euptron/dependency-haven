package io.thorenkoder.android.logging;

import android.content.Context;
import android.view.ViewGroup;
import android.view.View;
import android.widget.TextView;
import android.widget.FrameLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.AsyncListDiffer;
import android.text.SpannableStringBuilder;

import java.util.ArrayList;
import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {

  public LogAdapter() {}
  
  private final AsyncListDiffer<Log> mDiffer = new AsyncListDiffer<Log>(this, DIFF_CALLBACK);
  
  public void submitList(List<Log> newData) {
        mDiffer.submitList(newData);
  }
  
  public static final DiffUtil.ItemCallback<Log> DIFF_CALLBACK = new DiffUtil.ItemCallback<Log>() {
        @Override
        public boolean areItemsTheSame(@NonNull Log oldLog, @NonNull Log newLog) {
            return oldLog.getMessage().equals(newLog.getMessage());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Log oldLog, @NonNull Log newLog) {
            return oldLog.getMessage().equals(newLog.getMessage());
        }
  };
  
  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ViewHolder(new FrameLayout(parent.getContext()));
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    Log log = mDiffer.getCurrentList().get(position);
    SpannableStringBuilder sb = new SpannableStringBuilder();
    sb.append("");
    sb.append(log.getTag());
    sb.append(":");
    sb.append("");
    sb.append(log.getMessage());
    holder.mText.setText(sb);
  }

  @Override
  public int getItemCount() {
    return mDiffer.getCurrentList().size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {

    public TextView mText;

    public ViewHolder(View view) {
      super(view);

      mText = new TextView(view.getContext());
      ((ViewGroup) view).addView(mText);
    }
  }
}
