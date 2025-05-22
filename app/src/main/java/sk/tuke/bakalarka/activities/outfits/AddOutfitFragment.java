package sk.tuke.bakalarka.activities.outfits;


import static sk.tuke.bakalarka.tools.DbTools.addOutfitToDatabase;
import static sk.tuke.bakalarka.tools.DbTools.updateImageUrl;
import static sk.tuke.bakalarka.tools.ImageTools.bitmapToBase64;
import static sk.tuke.bakalarka.tools.ImageTools.bitmapToByteArray;
import static sk.tuke.bakalarka.tools.ImageTools.getResizedHeightToScreenWidth;
import static sk.tuke.bakalarka.tools.ImageTools.resizeBitmap;
import static sk.tuke.bakalarka.tools.ParseTools.parseToImageReference;
import static sk.tuke.bakalarka.tools.ResourcesTools.getScreenWidth;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sk.tuke.bakalarka.R;
import sk.tuke.bakalarka.recycler_view.PhotoItemAdapter;
import sk.tuke.bakalarka.recycler_view.PhotoItemHolder;
import sk.tuke.bakalarka.recycler_view.PhotoItemModel;
import sk.tuke.bakalarka.recycler_view.RecyclerViewInterface;
import sk.tuke.bakalarka.entities.ClothingItem;
import sk.tuke.bakalarka.entities.Outfit;
import sk.tuke.bakalarka.tools.DbTools;

public class AddOutfitFragment extends Fragment implements RecyclerViewInterface {
    private static final List<String> typeOrder = Arrays.asList(
            "top", "longsleeve", "skirt", "shorts", "pants", "dress", "outwear", "shoes"
    );
    private static final String ARG_DATE = "date";
    private String mDate;
    private String imageUrl;
    private List<ClothingItem> clothingItems;
    private String userId;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter<PhotoItemHolder> adapter;
    private RecyclerView.LayoutManager layoutManager;
    private HashSet<String> selectedClothingItems;
    private Uri selectedImageUri;
    private String outfitId;
    private Bitmap bitmap;
    private ImageView imageView;
    private Outfit outfit;

    private LinearLayout selectedClothingItemsLayout;
    public AddOutfitFragment() {
        // Required empty public constructor
    }
    public static AddOutfitFragment newInstance(String date) {
        AddOutfitFragment fragment = new AddOutfitFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DATE, date);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            mDate = getArguments().getString(ARG_DATE);
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user==null) {
            Toast.makeText(getActivity(),"You must be signed in to continue",Toast.LENGTH_SHORT).show();
            return;
        }
        userId = user.getUid();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_outfit, container, false);

        selectedClothingItemsLayout = view.findViewById(R.id.selected_clothing_items_layout);
        if(userId==null) {
            return view;
        }
        clothingItems = new ArrayList<>();
        selectedClothingItems = new HashSet<>();

        //set recycler view
        recyclerView = view.findViewById(R.id.rv_clothing_items);
        setRecyclerView(recyclerView, 0);

        imageView = view.findViewById(R.id.outfit_image);

        AppCompatButton addPhotoBtn = view.findViewById(R.id.add_photo_btn);
        addPhotoBtn.setOnClickListener(v -> checkPermissionAndOpenGallery());


        //get clothing items
        Query query = FirebaseFirestore.getInstance().collection("user_clothes")
                .document(userId).collection("clothes")
                .orderBy("type");
        DbTools.getUserClothingItems(query, new DbTools.OnUserClothingItemsCallback() {
            @Override
            public void onUserClothingItemsLoaded(List<ClothingItem> clothingItems) {
                AddOutfitFragment.this.clothingItems = clothingItems;
                AddOutfitFragment.this.clothingItems.sort(Comparator.comparingInt(item -> typeOrder.indexOf(item.getType())));
                //show clothes images in recycler view
                List<PhotoItemModel> data = getClothingItemModels(clothingItems);
                adapter = new PhotoItemAdapter(data,new WeakReference<>(getContext()), AddOutfitFragment.this, 0);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });



        AppCompatButton addOutfitBtn = view.findViewById(R.id.add_outfit_btn);
        addOutfitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //save outfit to database and show outfit
                outfit = getOutfit();
                outfitId = String.valueOf(outfit.getId());
                addToDatabaseAsync();
            }
        });


        return view;
    }

    private void addToDatabaseAsync() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(new Runnable() {
            @Override
            public void run() {
                addOutfitToDatabase(userId,outfit,requireActivity());
                uploadImageAndGetUrl(bitmap);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        showOutfitDetail(outfit);
                    }
                });
            }
        });
    }

    private void checkPermissionAndOpenGallery() {
        //check permission to image gallery
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_MEDIA_IMAGES) ==
                PackageManager.PERMISSION_GRANTED) {
            //start intent
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            choosePhotoFromGalleryLauncher.launch(galleryIntent);

        } else { //request permission to read images gallery based on sdk version
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED);
            }else
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private ActivityResultLauncher<Intent> choosePhotoFromGalleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //user chose photo from gallery
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            selectedImageUri = data.getData();
                            final InputStream imageStream;
                            try {
                                imageStream = requireContext().getContentResolver().openInputStream(data.getData());
                            } catch (FileNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                            bitmap = BitmapFactory.decodeStream(imageStream);
                            int screenWidth = getScreenWidth(requireContext());
                            int height = getResizedHeightToScreenWidth(bitmap.getWidth(), bitmap.getHeight(), screenWidth);
                            bitmap = resizeBitmap(requireContext(),bitmap,screenWidth,height);
                            imageView.setImageBitmap(bitmap);
                        }
                    }
                }
            }
    );


    private ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (result) {
                        //start intent
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        choosePhotoFromGalleryLauncher.launch(galleryIntent);
                    } else {
                        Toast.makeText(requireContext(),"You cannot upload picture without giving permission",Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );



    private void showOutfitDetail(Outfit outfit) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("outfit",outfit);
        bundle.putString("image",bitmapToBase64(bitmap));
        OutfitDetailFragment outfitDetailFragment = new OutfitDetailFragment();
        outfitDetailFragment.setArguments(bundle);
        replaceFragment(outfitDetailFragment);
    }
    private Outfit getOutfit() {
        Outfit outfit = new Outfit();
        Spinner occasionSpinner = (Spinner) requireView().findViewById(R.id.ocassion_spinner);
        Spinner seasonSpinner = (Spinner) requireView().findViewById(R.id.season_spinner);

        String occasion = occasionSpinner.getSelectedItem().toString();
        String season = seasonSpinner.getSelectedItem().toString();

        outfit.setClothingItems(selectedClothingItems);

        outfit.setOccasion(occasion);
        outfit.setSeason(season);
        outfit.setImageLink(imageUrl);

        if(mDate != null) { //user's previous fragment was planing outfit
            outfit.getDatesWorn().add(mDate);
        }
        outfit.setId(outfit.hashCode());
        return outfit;
    }

    @Override
    public void onItemClick(int position, int recyclerViewId) {
        //save clothing item id
        String clickedClothingId = "";
        if(clothingItems != null && !clothingItems.isEmpty()) {
            if(clothingItems.get(position) != null) {
                clickedClothingId = String.valueOf(clothingItems.get(position).getId());

                //clothing item was clicked before
                if(selectedClothingItems.contains(clickedClothingId)) {
                    selectedClothingItems.remove(clickedClothingId);
                    //remove clicked clothing from top layout
                    ImageView imageViewToRemove = selectedClothingItemsLayout.findViewById(Integer.parseInt(clickedClothingId));
                    if (imageViewToRemove != null) {
                        selectedClothingItemsLayout.removeView(imageViewToRemove);
                    }
                }else {
                    selectedClothingItems.add(clickedClothingId);
                    //show image of selected item at the top layout
                    ImageView selectedClothingImageView = new ImageView(requireContext());
                    LinearLayout.LayoutParams params = new LinearLayout
                            .LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);

                    selectedClothingImageView.setLayoutParams(params);
                    selectedClothingItemsLayout.addView(selectedClothingImageView);
                    Glide
                            .with(requireContext())
                            .load(clothingItems.get(position).getImageLink())
                            .into(selectedClothingImageView);
                    selectedClothingImageView.setId(Integer.parseInt(clickedClothingId));
                }
            }
        }
    }
    private void setRecyclerView(RecyclerView recyclerView, int recyclerViewId) {
        layoutManager = new GridLayoutManager(getContext(),5);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PhotoItemAdapter(new ArrayList<>(),new WeakReference<>(getContext()), AddOutfitFragment.this, recyclerViewId);
        recyclerView.setAdapter(adapter);
    }



    private static List<PhotoItemModel> getClothingItemModels(List<ClothingItem> clothingItems) {
        List<PhotoItemModel>  clothingItemModels = new ArrayList<>();
        for(ClothingItem clothingItem : clothingItems) {
            PhotoItemModel clothingItemModel = new PhotoItemModel(clothingItem.getImageLink());
            clothingItemModels.add(clothingItemModel);
        }
        return clothingItemModels;
    }


    private void uploadImageAndGetUrl(Bitmap bitmap) {
        byte[] bytes = bitmapToByteArray(bitmap);
        DbTools.uploadImageBytesToFirebaseStorage(parseToImageReference(userId,"outfits",outfitId), bytes, requireContext(), new DbTools.OnClothingImageCallback() {
            @Override
            public void onImageUploaded(String imageUrl) {
                updateImageUrl(userId, "outfits",outfitId,imageUrl);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }


}