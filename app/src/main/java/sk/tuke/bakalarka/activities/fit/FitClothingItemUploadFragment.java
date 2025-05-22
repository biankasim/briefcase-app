package sk.tuke.bakalarka.activities.fit;

import static sk.tuke.bakalarka.tools.ResourcesTools.getAlertDialogPref;
import static sk.tuke.bakalarka.tools.ResourcesTools.setAlertDialogPref;
import static sk.tuke.bakalarka.tools.ImageTools.getImagePart;
import static sk.tuke.bakalarka.tools.ResourcesTools.getUserColorPalette;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.JsonObject;

import java.io.FileNotFoundException;
import java.io.InputStream;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sk.tuke.bakalarka.R;
import sk.tuke.bakalarka.api.ApiTools;
import sk.tuke.bakalarka.tools.ImageTools;

public class FitClothingItemUploadFragment extends Fragment {
    private ImageView imageView;
    private Bitmap bitmap;
    private int originalWidth;
    private int originalHeight;
    private Uri selectedImageUri;


    public FitClothingItemUploadFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fit_clothing_item_upload, container, false);

        imageView = view.findViewById(R.id.image_view);

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

        return view;
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
                            originalWidth = bitmap.getWidth();
                            originalHeight = bitmap.getHeight();
                            bitmap = ImageTools.resizeBitmap(requireContext().getApplicationContext(), bitmap, 240, 320);
                            imageView.setImageBitmap(bitmap);
                            if(getAlertDialogPref(requireContext())) {
                                showAlert();
                            }
                            setImageOnTouchListener();
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


    private void classifyImage(Uri imageUri, int x, int y) {

        //api classification

        MultipartBody.Part imagePart = getImagePart(requireContext(),imageUri);

        String userColorPalette = getUserColorPalette(requireContext());

        Call<JsonObject> classificationCall = ApiTools.getApi().getPrediction(imagePart,x,y,userColorPalette);
        if(userColorPalette == null) { //user did not define their color season
            classificationCall = ApiTools.getApi().getClassification(imagePart,x,y);
        }
        classificationCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.code() == 200) {
                    assert response.body() != null;

                    String type = response.body().get("clothing_type").getAsString();
                    String color = response.body().get("color_name").getAsString();
                    boolean colorInPalette = false;
                    if(userColorPalette != null) {
                        colorInPalette = response.body().get("color_in_palette").getAsBoolean();
                    }
                    replaceFragment(FitFragment.newInstance(type,color,colorInPalette));

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

}