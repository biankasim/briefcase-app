package sk.tuke.bakalarka.activities.closet;



import static sk.tuke.bakalarka.tools.ColorCategorizer.categorizeColor;
import static sk.tuke.bakalarka.tools.DbTools.updateImageUrl;
import static sk.tuke.bakalarka.tools.ImageTools.bitmapToBase64;
import static sk.tuke.bakalarka.tools.ImageTools.bitmapToByteArray;
import static sk.tuke.bakalarka.tools.ParseTools.parseDateToString;
import static sk.tuke.bakalarka.tools.ParseTools.parseToImageReference;
import static sk.tuke.bakalarka.tools.ResourcesTools.getAlertDialogPref;
import static sk.tuke.bakalarka.tools.ResourcesTools.setAlertDialogPref;
import static sk.tuke.bakalarka.tools.ResourcesTools.setResourcesArrayPosition;
import static sk.tuke.bakalarka.tools.ImageTools.getImagePart;
import static sk.tuke.bakalarka.tools.DbTools.uploadImageBytesToFirebaseStorage;
import static sk.tuke.bakalarka.tools.ParseTools.getClothingItem;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
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
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.JsonObject;


import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sk.tuke.bakalarka.R;
import sk.tuke.bakalarka.api.ApiTools;
import sk.tuke.bakalarka.tools.DbTools;
import sk.tuke.bakalarka.entities.ClothingItem;
import sk.tuke.bakalarka.tools.ImageTools;


public class AddClothingItemFragment extends Fragment {
    private String userId;
    private ClothingItem clothingItem;
    private ImageView imageView;
    private String colorName;
    private Uri selectedImageUri;
    private Bitmap bitmap;
    private int originalWidth;
    private int originalHeight;
    private Calendar selectedDate;
    public AddClothingItemFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null) {
            userId = user.getUid();
        }
        selectedDate = Calendar.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_clothing_item, container, false);

        imageView = view.findViewById(R.id.clothing_image);

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




        //setting self-made switch listener
        SwitchCompat switchMaterial = view.findViewById(R.id.self_made_switch);
        switchMaterial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {

                    ((TextView) requireView().findViewById(R.id.date_text_view)).setText("Creation Date: ");
                    requireView().findViewById(R.id.price).setVisibility(View.GONE);
                    requireView().findViewById(R.id.edit_text_price).setVisibility(View.GONE);
                }else{
                    ((TextView) requireView().findViewById(R.id.date_text_view)).setText("Purchase Date: ");
                    requireView().findViewById(R.id.price).setVisibility(View.VISIBLE);
                    requireView().findViewById(R.id.edit_text_price).setVisibility(View.VISIBLE);
                }
            }
        });

        AppCompatButton datePickerBtn = view.findViewById(R.id.date_picker_btn);
        datePickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog(view.getId());
            }
        });


        //setting add button listener
        AppCompatButton addButton = view.findViewById(R.id.add_clothing_item);
        addButton.setOnClickListener(v -> {
            //add clothing item to database and show it
            clothingItem = getClothingItem(requireContext(),requireView(),null, null, colorName);
            addToDatabaseAsync();
        });


        return view;
    }
    private void addToDatabaseAsync() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(new Runnable() {
            @Override
            public void run() {
                DbTools.addClothingItemToDatabase(userId,clothingItem,requireActivity());
                uploadImageAndGetUrl(bitmap);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        showClothingItemDetail();
                    }
                });
            }
        });
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
                            //resize the chosen photo to image view and show alert
                            bitmap = BitmapFactory.decodeStream(imageStream);
                            resizeAndShowBitmap();
                            if(getAlertDialogPref(requireContext())) {
                                showAlert();
                            }
                            setImageOnTouchListener();
                        }
                    }
                }
            }
    );
    private void resizeAndShowBitmap() {
        originalWidth = bitmap.getWidth();
        originalHeight = bitmap.getHeight();
        bitmap = ImageTools.resizeBitmap(requireContext().getApplicationContext(), bitmap, 240, 320);
        imageView.setImageBitmap(bitmap);
    }


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

    private void showClothingItemDetail() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("clothingItem", clothingItem);
        bundle.putString("image",bitmapToBase64(bitmap));

        ClothingItemFragment clothingItemFragment = new ClothingItemFragment();
        clothingItemFragment.setArguments(bundle);

        replaceFragment(clothingItemFragment);
    }
    private void getColorPositionAndClassifyImage(MotionEvent event) {
        if (bitmap != null) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            if (x < bitmap.getWidth() && y < bitmap.getHeight() && x > 0 && y > 0) {
                //calculate relative x and y (image is resized)
                float xPercentage = ((float) x / bitmap.getWidth());
                float yPercentage = ((float) y / bitmap.getHeight());

                //calculate position of x and y in original image
                int xOriginal = (int) (originalWidth * xPercentage);
                int yOriginal = (int) (originalHeight * yPercentage);


                Toast.makeText(requireContext(), "Click recorded", Toast.LENGTH_SHORT).show();
                Toast.makeText(requireContext(), "AI in progress", Toast.LENGTH_LONG).show();

                classifyImage(selectedImageUri, xOriginal, yOriginal);
            }
        }
    }

    private void setImageOnTouchListener() {
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    //user clicked on dominant color of clothing item
                    getColorPositionAndClassifyImage(event);
                    return true;
                }
                return false;
            }
        });
    }

    private void uploadImageAndGetUrl(Bitmap bitmap) {
        byte[] bytes = bitmapToByteArray(bitmap);
        uploadImageBytesToFirebaseStorage(
                parseToImageReference(userId, "clothes",String.valueOf(clothingItem.getId())),
                bytes,
                requireContext(),
                new DbTools.OnClothingImageCallback() {
            @Override
            public void onImageUploaded(String imageUrl) {
                updateImageUrl(userId,"clothes" ,String.valueOf(clothingItem.getId()),imageUrl);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void showAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage("Please click on the DOMINANT COLOR of clothing item on the image to proceed.")
                .setTitle("Alert")
                .setNeutralButton("Do not show again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setAlertDialogPref(requireContext(), false);
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void classifyImage(Uri imageUri, int x, int y) {
        //api classification
        MultipartBody.Part imagePart = getImagePart(requireContext(),imageUri);
        Call<JsonObject> classificationCall = ApiTools.getApi().getClassification(imagePart,x,y);
        classificationCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.code() == 200) {
                    assert response.body() != null;

                    String type = response.body().get("clothing_type").getAsString();
                    String color = response.body().get("color_name").getAsString();
                    colorName = color;

                    //set spinners values
                    String[] types = getResources().getStringArray(R.array.type_array);
                    String[] colors = getResources().getStringArray(R.array.color_categories);

                    Spinner typeSpinner = (Spinner) requireView().findViewById(R.id.type_spinner);
                    Spinner colorSpinner = (Spinner) requireView().findViewById(R.id.color_spinner);

                    setResourcesArrayPosition(types,typeSpinner,type);
                    setResourcesArrayPosition(colors,colorSpinner,categorizeColor(color.toLowerCase()));

                }else{
                    Toast.makeText(getActivity(), String.valueOf(response.message()), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }


    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }


    private void showDatePickerDialog(int viewId) {
        final Calendar currentDate = Calendar.getInstance();
        int year = currentDate.get(Calendar.YEAR);
        int month = currentDate.get(Calendar.MONTH);
        int dayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH);
        selectedDate.set(year,month,dayOfMonth);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireActivity(),new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                selectedDate = Calendar.getInstance();
                selectedDate.set(year, monthOfYear, dayOfMonth);

                int y = selectedDate.get(Calendar.YEAR);
                int m = selectedDate.get(Calendar.MONTH)+1;
                int d = selectedDate.get(Calendar.DAY_OF_MONTH);

                String date = parseDateToString(y,m,d);
                TextView textView = requireView().findViewById(R.id.date);
                textView.setText(date);
            }
        }, year, month, dayOfMonth);

        datePickerDialog.show();
    }
}