package sk.tuke.bakalarka.activities.closet;

import static sk.tuke.bakalarka.tools.CareDrawableFinder.getDrawableFromId;
import static sk.tuke.bakalarka.tools.DbTools.removeClothingImageFromStorage;
import static sk.tuke.bakalarka.tools.DbTools.removeItemFromDatabase;
import static sk.tuke.bakalarka.tools.DbTools.toggleInLaundry;
import static sk.tuke.bakalarka.tools.DbTools.washClothingItem;
import static sk.tuke.bakalarka.tools.ImageTools.base64ToBitmap;
import static sk.tuke.bakalarka.tools.ParseTools.capitalizeFirstLetter;
import static sk.tuke.bakalarka.tools.ParseTools.parseToImageReference;
import static sk.tuke.bakalarka.tools.ParseTools.removeNewline;
import static sk.tuke.bakalarka.tools.StatisticsCalculator.calculatePricePerWear;
import static sk.tuke.bakalarka.tools.StatisticsCalculator.calculateWearPerClean;
import static sk.tuke.bakalarka.tools.StatisticsCalculator.getInClosetFor;
import static sk.tuke.bakalarka.tools.StatisticsCalculator.getTimesWornInLastYear;
import static sk.tuke.bakalarka.tools.StatisticsCalculator.getWearingFrequency;
import static sk.tuke.bakalarka.tools.StatisticsCalculator.parseStatisticsToString;
import static sk.tuke.bakalarka.tools.StatisticsCalculator.roundToTwoDecimal;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;

import sk.tuke.bakalarka.R;
import sk.tuke.bakalarka.activities.swap.AddClothingItemToSwapFragment;
import sk.tuke.bakalarka.entities.ClothingItem;

public class ClothingItemFragment extends Fragment {

    private static final String ARG_CLOTHING_ITEM = "clothingItem";
    private static final String ARG_IMAGE = "image";
    private ClothingItem mClothingItem;
    private String mImage;
    private String userId;

    public ClothingItemFragment() {
        // Required empty public constructor
    }

    public static ClothingItemFragment newInstance(String clothingItem, String image) {
        ClothingItemFragment fragment = new ClothingItemFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CLOTHING_ITEM, clothingItem);
        args.putString(ARG_IMAGE, image);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mClothingItem = getArguments().getParcelable(ARG_CLOTHING_ITEM);
            mImage = getArguments().getString(ARG_IMAGE);
        }
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_clothing_item, container, false);

        SwitchMaterial inLaundrySwitch = view.findViewById(R.id.in_laundry);
        if(mClothingItem.isInLaundry()) {
            inLaundrySwitch.setChecked(true);
        }
        inLaundrySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleInLaundry(userId, String.valueOf(mClothingItem.getId()),isChecked);
                if(!isChecked) { //user unchecked inLaundry
                    washClothingItem(userId, String.valueOf(mClothingItem.getId()));
                }
            }
        });


        AppCompatButton deleteClothingItemBtn = view.findViewById(R.id.delete_clothing_item_btn);
        deleteClothingItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItemFromDatabase(userId,"clothes", String.valueOf(mClothingItem.getId()));
                String imageRef = parseToImageReference(userId,"clothes",String.valueOf(mClothingItem.getId()));
                removeClothingImageFromStorage(imageRef);
                replaceFragment(ClosetFragment.newInstance("-","-","-","-","-","-",false,false));
            }
        });

        AppCompatButton editClothingItemBtn = view.findViewById(R.id.edit_clothing_item);
        editClothingItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("clothingItem", mClothingItem);
                EditClothingItemFragment editClothingItemFragment = new EditClothingItemFragment();
                editClothingItemFragment.setArguments(bundle);
                replaceFragment(editClothingItemFragment);
            }
        });

        AppCompatButton giveAwayClothingItemBtn = view.findViewById(R.id.give_away_btn);
        giveAwayClothingItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("clothingItem", mClothingItem);
                AddClothingItemToSwapFragment addClothingItemToSwapFragment = new AddClothingItemToSwapFragment();
                addClothingItemToSwapFragment.setArguments(bundle);
                replaceFragment(addClothingItemToSwapFragment);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(mClothingItem != null) {
                showClothingItem();
            }
        }
    }


    private void showClothingItem() {
        ImageView imageView = requireView().findViewById(R.id.clothing_image);
        if(mImage == null) {
            Glide
                    .with(requireContext())
                    .load(mClothingItem.getImageLink())
                    .into(imageView);
        }else{
            imageView.setImageBitmap(base64ToBitmap(mImage));
        }

        if(mClothingItem.isSelfMade()) {
            setDetailInLayout(R.id.purchase_price_layout, R.drawable.ic_money, "self-made :)",requireContext(), requireView());
        }else {
            setDetailInLayout(R.id.purchase_price_layout, R.drawable.ic_money, roundToTwoDecimal(mClothingItem.getPurchasePrice()),requireContext(), requireView());
        }
        setDetailInLayout(R.id.materials_layout, R.drawable.ic_material, mClothingItem.getCompositionString(),requireContext(), requireView());
        setDetailInLayout(R.id.in_closet_for_layout, R.drawable.ic_closet_storage, getInClosetFor(mClothingItem),requireContext(), requireView());

        showCare();
        showStats();
    }

    public static void setDetailInLayout(int layoutId, int iconId, String value, Context context, View view) {
        LinearLayout linearLayout = view.findViewById(layoutId);
        TextView textView = linearLayout.findViewById(R.id.text_view);
        ImageView imageView = linearLayout.findViewById(R.id.image_view);
        Drawable drawable = ResourcesCompat.getDrawable(context.getResources(),iconId,null);
        imageView.setBackground(drawable);
        textView.setText(value);
    }
    private void setIconColor(String wearingFrequency, ImageView imageView) {
        if(wearingFrequency == null) {
            return;
        }
        if(wearingFrequency.equals("high")) {
            imageView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#BBFF00")));
        } else if (wearingFrequency.equals("medium")) {
            imageView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFF19")));
        } else if (wearingFrequency.equals("low")) {
            imageView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFF8800")));
        } else if (wearingFrequency.equals("very low")) {
            imageView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E60000")));
        }
    }

    private void showStats() {
        String wearingFrequency = getWearingFrequency(mClothingItem);
        setDetailInLayout(R.id.wearing_frequency_layout,R.drawable.ic_stonks,
                String.format("%s Wearing Frequency",capitalizeFirstLetter(wearingFrequency)),requireContext(), requireView());
        LinearLayout linearLayout = requireView().findViewById(R.id.wearing_frequency_layout);
        ImageView imageView = linearLayout.findViewById(R.id.image_view);
        setIconColor(wearingFrequency,imageView);


        String wearsInLastYear = parseStatisticsToString("Wears In Last Year", getTimesWornInLastYear(mClothingItem));
        setDetailInLayout(R.id.wears_in_last_year_layout,R.drawable.ic_closet_hanger,wearsInLastYear,requireContext(), requireView());

        if(!mClothingItem.isSelfMade()) {
            String pricePerWearText = parseStatisticsToString("Price Per Wear",calculatePricePerWear(mClothingItem));
            setDetailInLayout(R.id.price_per_wear_layout,R.drawable.ic_price_per_wear,pricePerWearText,requireContext(), requireView());
        }else{
            requireView().findViewById(R.id.price_per_wear_layout).setVisibility(View.GONE);
        }

        String wearPerCleanText = parseStatisticsToString("Wear Per Clean",calculateWearPerClean(mClothingItem));
        setDetailInLayout(R.id.wear_per_clean_layout,R.drawable.ic_wear_per_clean,wearPerCleanText,requireContext(), requireView());


    }


    private void showCare() {
        if(mClothingItem.getCare() == null || mClothingItem.getCare().isEmpty()) {
            return;
        }
        String[] care = {"wash","bleach","tumble_dry","iron","dry_clean"};
        LinearLayout linearLayout = requireView().findViewById(R.id.care_layout_drawables);
        int i = 0;
        int nonEmptyValuesCount = 0;
        for(String key : care) {
            String value = mClothingItem.getCare().get(key);
            if(value != null && !value.isEmpty()) {
                nonEmptyValuesCount++;
                ImageView imageView = linearLayout.getChildAt(i).findViewById(R.id.image_view);
                TextView textView = linearLayout.getChildAt(i).findViewById(R.id.text_view);
                Drawable drawable = getDrawableFromId(requireContext(),value);
                imageView.setBackground(drawable);
                textView.setText(value);
            }
            i++;
            if(i>=mClothingItem.getCare().size()) {
                break;
            }
        }
        if(nonEmptyValuesCount==0){//care is empty
            TextView textView = requireView().findViewById(R.id.care_title);
            linearLayout.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
        }

    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }
}