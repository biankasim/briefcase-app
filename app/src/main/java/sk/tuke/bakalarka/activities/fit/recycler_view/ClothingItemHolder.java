package sk.tuke.bakalarka.activities.fit.recycler_view;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import sk.tuke.bakalarka.R;

public class ClothingItemHolder extends RecyclerView.ViewHolder {
    ImageView imageView;
    TextView textView;
    public ClothingItemHolder(@NonNull View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.image_view);
        textView = itemView.findViewById(R.id.wearing_frequency);
    }
}
