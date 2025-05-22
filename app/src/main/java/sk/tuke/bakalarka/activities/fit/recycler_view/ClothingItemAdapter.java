package sk.tuke.bakalarka.activities.fit.recycler_view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.lang.ref.WeakReference;
import java.util.List;

import sk.tuke.bakalarka.R;

public class ClothingItemAdapter extends RecyclerView.Adapter<ClothingItemHolder> {
    private List<ClothingItemModel> _data;
    private WeakReference<Context> _context;
    public ClothingItemAdapter(List<ClothingItemModel> data, WeakReference<Context> contextReference) {
        this._data = data;
        this._context = contextReference;
    }
    @NonNull
    @Override
    public ClothingItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(_context.get()).inflate(R.layout.clothing_item_fit,parent,false);
        return new ClothingItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClothingItemHolder holder, int position) {
        Glide
                .with(_context.get())
                .load(_data.get(position).imageUrl)
                .into(holder.imageView);
        holder.textView.setText(_data.get(position).wearingFrequency);
    }

    @Override
    public int getItemCount() {
        if(_data!=null) { return _data.size(); }
        return 0;
    }
}
