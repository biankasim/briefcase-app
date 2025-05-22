package sk.tuke.bakalarka.activities.swap.recycler_view;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.lang.ref.WeakReference;
import java.util.List;

import sk.tuke.bakalarka.R;
import sk.tuke.bakalarka.recycler_view.RecyclerViewInterface;

public class SwapAdapter extends RecyclerView.Adapter<SwapHolder>{
    private final RecyclerViewInterface recyclerViewInterface;
    private List<SwapModel> _data;
    private WeakReference<Context> _context;

    public SwapAdapter(List<SwapModel> data, WeakReference<Context> contextReference, RecyclerViewInterface recyclerViewInterface) {
        _context = contextReference;
        _data = data;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    public void refreshData(List<SwapModel> data){
        _data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SwapHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(_context.get()).inflate(R.layout.swap_item,parent,false);
        return new SwapHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull SwapHolder holder, int position) {
        holder.conditionTextView.setText(_data.get(position).condition);
        holder.priceTextView.setText(String.valueOf(_data.get(position).price));
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
