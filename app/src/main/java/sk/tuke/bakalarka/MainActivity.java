package sk.tuke.bakalarka;

import static sk.tuke.bakalarka.tools.DbTools.addClothingItemToDatabase;
import static sk.tuke.bakalarka.tools.DbTools.removeSwapItemFromDatabase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import sk.tuke.bakalarka.activities.fit.FitStartPageFragment;
import sk.tuke.bakalarka.activities.outfits.OutfitsStartPageFragment;
import sk.tuke.bakalarka.activities.settings.SettingsFragment;
import sk.tuke.bakalarka.activities.swap.SwapFragment;
import sk.tuke.bakalarka.activities.closet.ClosetFragment;
import sk.tuke.bakalarka.databinding.ActivityMainBinding;
import sk.tuke.bakalarka.entities.ClothingItem;
import sk.tuke.bakalarka.entities.SwapItem;
import sk.tuke.bakalarka.tools.DbTools;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user==null){
            replaceFragment(new SettingsFragment());
            binding.bottomNavigationView.setSelectedItemId(R.id.settings);
        }else {
            userId = user.getUid();
            //checkSwap();
            replaceFragment(new OutfitsStartPageFragment());
        }

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if(item.getItemId() == R.id.outfits) {
                replaceFragment(new OutfitsStartPageFragment());
            }else
            //if(item.getItemId() == R.id.swap) {
            //    replaceFragment(new SwapFragment());
            //}else
            if(item.getItemId() == R.id.closet) {
                replaceFragment(new ClosetFragment());
                replaceFragment(ClosetFragment.newInstance("-","-","-","-","-","-",false,false));
            }else
            if(item.getItemId() == R.id.fit) {
                replaceFragment(new FitStartPageFragment());
            }else
            if(item.getItemId() == R.id.settings) {
                replaceFragment(new SettingsFragment());
            }
            return true;
        });




    }
    /*
    private void checkSwap() {
        DbTools.getSwapItemsAcceptedRequest(userId,new DbTools.OnUserClothingItemsCallback() {
            @Override
            public void onUserClothingItemsLoaded(List<ClothingItem> clothingItems) {
                checkAcceptedSwapRequests(userId,clothingItems);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }



    private void checkAcceptedSwapRequests(String userId, List<ClothingItem> clothingItems) {
            for(ClothingItem clothingItem : clothingItems) {
                SwapItem swapItem = (SwapItem) clothingItem;
                if(Objects.equals(swapItem.getAcceptedRequest(), userId)) {
                    Toast.makeText(this,"swap request was accepted",Toast.LENGTH_SHORT).show();
                    clothingItem.setDateOfPurchase(LocalDate.now());
                    clothingItem.setPurchasePrice(swapItem.getSwapPrice());
                    addClothingItemToDatabase(userId,clothingItem,this);
                    removeSwapItemFromDatabase(userId,String.valueOf(swapItem.getSwapId()),String.valueOf(swapItem.getId()));

                    clothingItems.remove(clothingItem);
                }
            }

    }
    */
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }
}

