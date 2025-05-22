package sk.tuke.bakalarka.recycler_view;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import sk.tuke.bakalarka.R;

public class PhotoItemHolder extends RecyclerView.ViewHolder{
    boolean checked = false;
    ImageView imageView;

    public PhotoItemHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface, int recyclerViewId) {
        super(itemView);
        imageView = itemView.findViewById(R.id.image_view);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recyclerViewInterface != null) {
                    int pos = getAdapterPosition();
                    if(pos != RecyclerView.NO_POSITION){
                        recyclerViewInterface.onItemClick(pos, recyclerViewId);
                    }
                }
                //toggleBackground();
            }
        });
    }
    /*
    private void toggleBackground() {
        if(checked) {
            itemView.setBackgroundColor(Color.TRANSPARENT);
        }else {
            itemView.setBackgroundColor(Color.YELLOW);
        }
        checked = !checked;
    }

     */
}
