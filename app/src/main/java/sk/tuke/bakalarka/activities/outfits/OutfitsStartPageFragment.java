package sk.tuke.bakalarka.activities.outfits;

import static sk.tuke.bakalarka.tools.DbTools.getUserClothingItemsByIds;
import static sk.tuke.bakalarka.tools.DbTools.isOutfitPlannedOnDate;
import static sk.tuke.bakalarka.tools.DbTools.unlogOutfit;
import static sk.tuke.bakalarka.tools.DbTools.updateClothingItemStatistics;
import static sk.tuke.bakalarka.tools.ParseTools.parseDateToString;
import static sk.tuke.bakalarka.tools.ParseTools.parseOutfit;

import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.List;

import sk.tuke.bakalarka.R;
import sk.tuke.bakalarka.entities.ClothingItem;
import sk.tuke.bakalarka.entities.Outfit;
import sk.tuke.bakalarka.tools.DbTools;


public class OutfitsStartPageFragment extends Fragment {

    private Calendar selectedDate;
    private String userId;
    public OutfitsStartPageFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedDate = Calendar.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            userId = user.getUid();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_outfits_start_page, container, false);


        AppCompatButton myOutfitsBtn = view.findViewById(R.id.my_outfits_btn);
        AppCompatButton planOutfitBtn = view.findViewById(R.id.plan_outfit_btn);
        AppCompatButton unplanOutfitBtn = view.findViewById(R.id.unplan_outfit_btn);

        myOutfitsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new OutfitsFragment());
            }
        });

        planOutfitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(FirebaseAuth.getInstance().getCurrentUser() == null) {
                    Toast.makeText(getActivity(),"You must be signed in to continue",Toast.LENGTH_SHORT).show();
                    return;
                }
                showDatePickerDialog(v.getId());
            }
        });
        unplanOutfitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(FirebaseAuth.getInstance().getCurrentUser() == null) {
                    Toast.makeText(getActivity(),"You must be signed in to continue",Toast.LENGTH_SHORT).show();
                    return;
                }
                showDatePickerDialog(v.getId());
            }
        });
        return view;
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
                check(viewId, date);


            }
        }, year, month, dayOfMonth);

        datePickerDialog.show();
    }

    private void check(int viewId, String date) {
        isOutfitPlannedOnDate(userId, date, new OnCompleteListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                QuerySnapshot document = task.getResult();
                Outfit plannedOutfit = null;
                if(document != null && !document.isEmpty()) {
                    plannedOutfit = parseOutfit(document.getDocuments().get(0));
                }


                //user clicked on plan outfit
                if(viewId == R.id.plan_outfit_btn) {
                    if (plannedOutfit == null) {
                        replaceFragment(PlanOutfitDialogFragment.newInstance(date));
                    } else {
                        Toast.makeText(getActivity(),"Some outfit is already planned on that day",Toast.LENGTH_SHORT).show();
                        showDatePickerDialog(viewId);
                    }
                }

                //user clicked on unplan outfit
                if(viewId == R.id.unplan_outfit_btn) {
                    if (plannedOutfit != null) {
                        unlogOutfit(userId,plannedOutfit,date,requireContext());
                        getUserClothingItemsByIds(userId, plannedOutfit.getClothingItemsIds(), new DbTools.OnUserClothingItemsCallback() {
                            @Override
                            public void onUserClothingItemsLoaded(List<ClothingItem> clothingItems) {
                                for(ClothingItem clothingItem : clothingItems) {
                                    updateClothingItemStatistics(userId,clothingItem);
                                }
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(requireContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        });
                        replaceFragment(new OutfitsStartPageFragment());
                    } else {
                        Toast.makeText(getActivity(),"There is no outfit planned on that day",Toast.LENGTH_SHORT).show();
                    }
                }
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