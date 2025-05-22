package sk.tuke.bakalarka.activities.outfits;

import static sk.tuke.bakalarka.tools.DbTools.getUserClothingItemsByIds;
import static sk.tuke.bakalarka.tools.DbTools.removeClothingImageFromStorage;
import static sk.tuke.bakalarka.tools.DbTools.removeItemFromDatabase;
import static sk.tuke.bakalarka.tools.DbTools.updateImageUrl;
import static sk.tuke.bakalarka.tools.ImageTools.base64ToBitmap;
import static sk.tuke.bakalarka.tools.ImageTools.bitmapToBase64;
import static sk.tuke.bakalarka.tools.ImageTools.bitmapToByteArray;
import static sk.tuke.bakalarka.tools.ImageTools.getResizedHeightToScreenWidth;
import static sk.tuke.bakalarka.tools.ImageTools.resizeBitmap;
import static sk.tuke.bakalarka.tools.ParseTools.getClothingItemModels;
import static sk.tuke.bakalarka.tools.ParseTools.parseToImageReference;
import static sk.tuke.bakalarka.tools.ResourcesTools.calculateNumberOfColumns;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;

import sk.tuke.bakalarka.R;
import sk.tuke.bakalarka.activities.closet.ClothingItemFragment;
import sk.tuke.bakalarka.entities.ClothingItem;
import sk.tuke.bakalarka.entities.Outfit;
import sk.tuke.bakalarka.recycler_view.PhotoItemAdapter;
import sk.tuke.bakalarka.recycler_view.PhotoItemHolder;
import sk.tuke.bakalarka.recycler_view.RecyclerViewInterface;
import sk.tuke.bakalarka.tools.DbTools;


public class OutfitDetailFragment extends Fragment implements RecyclerViewInterface {
    private static final String ARG_OUTFIT = "outfit";
    private static final String ARG_IMAGE = "image";
    private Outfit mOutfit;
    private String mImage;
    private String userId;
    private List<ClothingItem> clothingItems;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter<PhotoItemHolder> adapter;
    private RecyclerView.LayoutManager layoutManager;

    private Uri selectedImageUri;
    private Bitmap bitmap;
    private ImageView imageView;

    public OutfitDetailFragment() {
        // Required empty public constructor
    }
    public static OutfitDetailFragment newInstance(String outfit, String image) {
        OutfitDetailFragment fragment = new OutfitDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_OUTFIT, outfit);
        args.putString(ARG_IMAGE, image);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mOutfit = getArguments().getParcelable(ARG_OUTFIT);
            mImage = getArguments().getString(ARG_IMAGE);
        }
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_outfit_detail, container, false);

        AppCompatButton deleteOutfitBtn = view.findViewById(R.id.delete_outfit_btn);
        deleteOutfitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItemFromDatabase(userId,"outfits", String.valueOf(mOutfit.getId()));
                String imageRef = parseToImageReference(userId,"outfits", String.valueOf(mOutfit.getId()));
                removeClothingImageFromStorage(imageRef);
                replaceFragment(new OutfitsFragment());
            }
        });

        AppCompatButton addPhotoBtn = view.findViewById(R.id.add_photo_btn);
        addPhotoBtn.setOnClickListener(v -> checkPermissionAndOpenGallery());

        imageView = view.findViewById(R.id.outfit_image);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(mOutfit != null) {
            showOutfit();
        }
    }

    private void showOutfit() {
        TextView wornTimesTextView = requireView().findViewById(R.id.worn_times);
        TextView occasionTextView = requireView().findViewById(R.id.occasion);
        TextView seasonTextView = requireView().findViewById(R.id.season);

        String wornTimes = String.format("Worn %s times",mOutfit.getTimesWorn());
        String occasion = String.format("%s%s","Occasion: ", mOutfit.getOccasion());
        String season = String.format("%s%s","Season: ", mOutfit.getSeason());

        wornTimesTextView.setText(wornTimes);
        occasionTextView.setText(occasion);
        seasonTextView.setText(season);

        ImageView imageView = requireView().findViewById(R.id.outfit_image);
        if(mImage == null) {
            Glide
                    .with(requireContext())
                    .load(mOutfit.getImageLink())
                    .into(imageView);
        }else{
            //AppCompatButton addPhotoBtn = requireView().findViewById(R.id.add_photo_btn);
            //addPhotoBtn.setText("Change Photo");
            imageView.setImageBitmap(base64ToBitmap(mImage));
        }

        getUserClothingItemsByIds(userId, mOutfit.getClothingItemsIds(), new DbTools.OnUserClothingItemsCallback() {
            @Override
            public void onUserClothingItemsLoaded(List<ClothingItem> clothingItems) {
                OutfitDetailFragment.this.clothingItems = clothingItems;
                showClothingItemsInRecyclerView();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(requireContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showClothingItemsInRecyclerView() {
        recyclerView = requireView().findViewById(R.id.rv_clothes);
        int numberOfColumns = calculateNumberOfColumns(requireContext(),60);
        layoutManager = new GridLayoutManager(getContext(),numberOfColumns);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PhotoItemAdapter(getClothingItemModels(clothingItems),new WeakReference<>(getContext()), OutfitDetailFragment.this,0);
        recyclerView.setAdapter(adapter);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onItemClick(int position, int recyclerViewId) {
        showClothingItemDetail(clothingItems.get(position));
    }


    private void showClothingItemDetail(ClothingItem clothingItem) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("clothingItem", clothingItem);

        ClothingItemFragment clothingItemFragment = new ClothingItemFragment();
        clothingItemFragment.setArguments(bundle);

        replaceFragment(clothingItemFragment);
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
                            uploadImageAndGetUrl(bitmap);
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

    private void uploadImageAndGetUrl(Bitmap bitmap) {
        byte[] bytes = bitmapToByteArray(bitmap);
        DbTools.uploadImageBytesToFirebaseStorage(parseToImageReference(userId,"outfits", String.valueOf(mOutfit.getId())), bytes, requireContext(), new DbTools.OnClothingImageCallback() {
            @Override
            public void onImageUploaded(String imageUrl) {
                updateImageUrl(userId, "outfits",String.valueOf(mOutfit.getId()),imageUrl);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

}