package sk.tuke.bakalarka.activities.swap.recycler_view;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import sk.tuke.bakalarka.R;
import sk.tuke.bakalarka.recycler_view.RecyclerViewInterface;

public class SwapHolder extends RecyclerView.ViewHolder{
    ImageView imageView;
    TextView priceTextView;
    TextView conditionTextView;

    public SwapHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
        super(itemView);
        imageView = itemView.findViewById(R.id.image_view);
        priceTextView = itemView.findViewById(R.id.price_text_view);
        conditionTextView = itemView.findViewById(R.id.condition_text_view);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recyclerViewInterface != null) {
                    int pos = getAdapterPosition();
                    if(pos != RecyclerView.NO_POSITION){
                        recyclerViewInterface.onItemClick(pos,0);
                    }
                }
            }
        });
    }
}
