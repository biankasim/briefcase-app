package sk.tuke.bakalarka.activities.outfits;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.List;

import sk.tuke.bakalarka.R;
import sk.tuke.bakalarka.recycler_view.PhotoItemAdapter;
import sk.tuke.bakalarka.recycler_view.PhotoItemHolder;
import sk.tuke.bakalarka.recycler_view.PhotoItemModel;
import sk.tuke.bakalarka.recycler_view.RecyclerViewInterface;

public class OutfitAdapter extends PhotoItemAdapter {
    public OutfitAdapter(List<PhotoItemModel> data, WeakReference<Context> contextReference, RecyclerViewInterface recyclerViewInterface, int recyclerViewId) {
        super(data, contextReference, recyclerViewInterface, recyclerViewId);
    }

    @NonNull
    @Override
    public PhotoItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(_context.get()).inflate(R.layout.outfit_item,parent,false);
        return new PhotoItemHolder(view, recyclerViewInterface, recyclerViewId);
    }
}
