package sk.tuke.bakalarka.recycler_view;

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

public class PhotoItemAdapter extends RecyclerView.Adapter<PhotoItemHolder> {
    protected int recyclerViewId;
    protected final RecyclerViewInterface recyclerViewInterface;
    protected List<PhotoItemModel> _data;
    protected WeakReference<Context> _context;
    public PhotoItemAdapter(List<PhotoItemModel> data, WeakReference<Context> contextReference, RecyclerViewInterface recyclerViewInterface, int recyclerViewId) {
        _context = contextReference;
        _data = data;
        this.recyclerViewInterface = recyclerViewInterface;
        this.recyclerViewId = recyclerViewId;
    }

    public void refreshData(List<PhotoItemModel> data){
        _data = data;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public PhotoItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(_context.get()).inflate(R.layout.clothing_item,parent,false);
        return new PhotoItemHolder(view, recyclerViewInterface, recyclerViewId);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoItemHolder holder, int position) {
        Glide
                .with(_context.get())
                .load(_data.get(position).imageUrl)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        if(_data!=null) { return _data.size(); }
        return 0;
    }
}
