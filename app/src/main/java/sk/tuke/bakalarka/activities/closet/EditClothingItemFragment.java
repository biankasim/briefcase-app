package sk.tuke.bakalarka.activities.closet;

import static sk.tuke.bakalarka.tools.DbTools.updateClothingItemInDatabase;
import static sk.tuke.bakalarka.tools.DbTools.updateImageUrl;
import static sk.tuke.bakalarka.tools.DbTools.uploadImageBytesToFirebaseStorage;
import static sk.tuke.bakalarka.tools.ImageTools.bitmapToBase64;
import static sk.tuke.bakalarka.tools.ImageTools.bitmapToByteArray;
import static sk.tuke.bakalarka.tools.ParseTools.getClothingItem;
import static sk.tuke.bakalarka.tools.ParseTools.parseDateToString;
import static sk.tuke.bakalarka.tools.ParseTools.parseLocalDateToString;
import static sk.tuke.bakalarka.tools.ParseTools.parseToImageReference;
import static sk.tuke.bakalarka.tools.ResourcesTools.setResourcesArrayPosition;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Calendar;

import sk.tuke.bakalarka.R;
import sk.tuke.bakalarka.entities.ClothingItem;
import sk.tuke.bakalarka.tools.DbTools;
import sk.tuke.bakalarka.tools.ImageTools;


public class EditClothingItemFragment extends Fragment {

    private static final String ARG_CLOTHING_ITEM = "clothingItem";
    private ClothingItem mClothingItem;
    private String userId;
    private ImageView imageView;
    private Bitmap bitmap;

    private Calendar selectedDate;
    public EditClothingItemFragment() {
        // Required empty public constructor
    }


    public static EditClothingItemFragment newInstance(String clothingItem) {
        EditClothingItemFragment fragment = new EditClothingItemFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CLOTHING_ITEM, clothingItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mClothingItem = getArguments().getParcelable(ARG_CLOTHING_ITEM);
        }
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        selectedDate = Calendar.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_clothing_item, container, false);


        imageView = view.findViewById(R.id.clothing_image);
        setViews(view);

        //choose photo of clothing from gallery and upload it to firebase storage
        ActivityResultLauncher<Intent> launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            final InputStream imageStream;
                            try {
                                imageStream = requireContext().getContentResolver().openInputStream(data.getData());
                            } catch (FileNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                            bitmap = BitmapFactory.decodeStream(imageStream);
                            bitmap = ImageTools.resizeBitmap(requireContext().getApplicationContext(), bitmap, 240, 320);
                            imageView.setImageBitmap(bitmap);
                        }
                    }
                });
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imageView.setOnClickListener(v -> launcher.launch(galleryIntent));


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

        //setting done button listener
        AppCompatButton doneBtn = view.findViewById(R.id.done_btn);
        doneBtn.setOnClickListener(v -> {
            mClothingItem = getClothingItem(requireContext(), requireView(), mClothingItem.getImageLink(), String.valueOf(mClothingItem.getId()), mClothingItem.getColorName());
            updateClothingItemInDatabase(userId,mClothingItem,requireContext());
            showUpdatedClothingItem();
        });


        return view;
    }

    private void showUpdatedClothingItem() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("clothingItem",mClothingItem);

        if(bitmap != null) { //user edited picture
            uploadImageAndGetUrl(bitmap);
            bundle.putString("image",bitmapToBase64(bitmap));
        }

        //show clothing item
        ClothingItemFragment clothingItemFragment = new ClothingItemFragment();
        clothingItemFragment.setArguments(bundle);
        replaceFragment(clothingItemFragment);
    }

    private void uploadImageAndGetUrl(Bitmap bitmap) {
        if(bitmap == null) {
            return;
        }
        byte[] bytes = bitmapToByteArray(bitmap);
        uploadImageBytesToFirebaseStorage(
                parseToImageReference(userId, "clothes",String.valueOf(mClothingItem.getId())),
                bytes,
                requireContext(),
                new DbTools.OnClothingImageCallback() {
                    @Override
                    public void onImageUploaded(String imageUrl) {
                        updateImageUrl(userId,"clothes" ,String.valueOf(mClothingItem.getId()),imageUrl);

                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }




    private void setViews(View view) {
        imageView = view.findViewById(R.id.clothing_image);
        Glide
                .with(requireContext())
                .load(mClothingItem.getImageLink())
                .into(imageView);

        Spinner typeSpinner = (Spinner) view.findViewById(R.id.type_spinner);
        Spinner colorSpinner = (Spinner) view.findViewById(R.id.color_spinner);
        Spinner patternSpinner = (Spinner) view.findViewById(R.id.pattern_spinner);

        String[] types = getResources().getStringArray(R.array.type_array);
        String[] colors = getResources().getStringArray(R.array.color_categories);
        String[] patterns = getResources().getStringArray(R.array.pattern_array);

        //find the position of type in the array
        setResourcesArrayPosition(types, typeSpinner, mClothingItem.getType());
        setResourcesArrayPosition(colors, colorSpinner, mClothingItem.getColorCategory());
        setResourcesArrayPosition(patterns, patternSpinner, mClothingItem.getPattern());

        EditText compositionEditText = view.findViewById(R.id.composition);
        SwitchCompat selfMadeSwitch = view.findViewById(R.id.self_made_switch);
        TextView priceTextView = view.findViewById(R.id.price);
        TextView dateTextView = view.findViewById(R.id.date_text_view);
        EditText priceEditText = view.findViewById(R.id.edit_text_price);
        TextView date = view.findViewById(R.id.date);


        compositionEditText.setText(mClothingItem.getCompositionString());

        selfMadeSwitch.setChecked(mClothingItem.isSelfMade());
        if(mClothingItem.isSelfMade()) {
            priceEditText.setVisibility(View.GONE);
            priceTextView.setVisibility(View.GONE);
            dateTextView.setText("Creation Date: ");
        }else {
            priceEditText.setText(String.valueOf(mClothingItem.getPurchasePrice()));
        }


        LocalDate dateOfPurchase = mClothingItem.getDateOfPurchase();
        date.setText(parseLocalDateToString(dateOfPurchase));

        RadioGroup washingGroup1 = view.findViewById(R.id.washing_group);
        RadioGroup tumbleDryingGroup = view.findViewById(R.id.tumble_dry_group);
        RadioGroup ironingGroup = view.findViewById(R.id.ironing_group);
        RadioGroup bleachingGroup = view.findViewById(R.id.bleaching_group);
        RadioGroup dryCleaningGroup = view.findViewById(R.id.dry_cleaning_group);
        String washingDescription = mClothingItem.getCare().get("wash");
        String tumbleDryingDescription = mClothingItem.getCare().get("tumble_dry");
        String ironingDescription = mClothingItem.getCare().get("iron");
        String bleachingDescription = mClothingItem.getCare().get("bleach");
        String dryCleaningDescription = mClothingItem.getCare().get("dry_clean");
        setCheckedRadioBtnByContentDescription(washingGroup1, washingDescription);
        setCheckedRadioBtnByContentDescription(tumbleDryingGroup, tumbleDryingDescription);
        setCheckedRadioBtnByContentDescription(ironingGroup, ironingDescription);
        setCheckedRadioBtnByContentDescription(bleachingGroup, bleachingDescription);
        setCheckedRadioBtnByContentDescription(dryCleaningGroup, dryCleaningDescription);

    }
    private static void setCheckedRadioBtnByContentDescription(RadioGroup radioGroup, String contentDescription) {
        int radioCount = radioGroup.getChildCount();
        //iterate through all radio buttons to find the one with the desired content description
        for (int i = 0; i < radioCount; i++) {
            View radioButtonView = radioGroup.getChildAt(i);
            if (radioButtonView instanceof RadioButton) {
                RadioButton radioButton = (RadioButton) radioButtonView;
                if (contentDescription.contentEquals(radioButton.getContentDescription())) {
                    //found the radio button with the desired content description
                    radioButton.setChecked(true);
                    break;
                }
            }
        }
    }


    private void showDatePickerDialog(int viewId) {
        final Calendar currentDate = Calendar.getInstance();
        int year = currentDate.get(Calendar.YEAR);
        int month = currentDate.get(Calendar.MONTH);
        int dayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH);
        selectedDate.set(year,month,dayOfMonth);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireActivity(), new DatePickerDialog.OnDateSetListener() {
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


    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }
}