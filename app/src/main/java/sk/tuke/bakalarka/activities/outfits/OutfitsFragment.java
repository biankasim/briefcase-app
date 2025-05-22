package sk.tuke.bakalarka.activities.outfits;

import static sk.tuke.bakalarka.tools.DbTools.getUserClothingItemsByIds;
import static sk.tuke.bakalarka.tools.DbTools.isOutfitPlannedOnDate;
import static sk.tuke.bakalarka.tools.DbTools.logOutfit;
import static sk.tuke.bakalarka.tools.DbTools.updateClothingItemStatistics;
import static sk.tuke.bakalarka.tools.ParseTools.getOutfitModels;
import static sk.tuke.bakalarka.tools.ParseTools.parseDateToString;
import static sk.tuke.bakalarka.tools.ParseTools.parseOutfit;
import static sk.tuke.bakalarka.tools.ResourcesTools.calculateNumberOfColumns;

import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import sk.tuke.bakalarka.R;
import sk.tuke.bakalarka.entities.ClothingItem;
import sk.tuke.bakalarka.recycler_view.PhotoItemHolder;
import sk.tuke.bakalarka.recycler_view.PhotoItemModel;
import sk.tuke.bakalarka.recycler_view.RecyclerViewInterface;
import sk.tuke.bakalarka.entities.Outfit;
import sk.tuke.bakalarka.tools.DbTools;


public class OutfitsFragment extends Fragment implements RecyclerViewInterface {
    private static final String ARG_OCCASION = "occasion";
    private static final String ARG_SEASON = "season";
    private static final String ARG_DATE = "date";
    private String mDate;
    private String mOccasion;
    private String mSeason;

    private String userId;
    private List<Outfit> outfits;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter<PhotoItemHolder> adapter;
    private RecyclerView.LayoutManager layoutManager;
    private Calendar selectedDate;
    public OutfitsFragment() {
        // Required empty public constructor
    }

    public static OutfitsFragment newInstance(String occasion, String season, String date) {
        OutfitsFragment fragment = new OutfitsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_OCCASION, occasion);
        args.putString(ARG_SEASON, season);
        args.putString(ARG_DATE, date);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            mOccasion = getArguments().getString(ARG_OCCASION);
            mSeason = getArguments().getString(ARG_SEASON);
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
        View view = inflater.inflate(R.layout.fragment_outfits, container, false);

        selectedDate = Calendar.getInstance();

        if(userId==null) {
            return view;
        }
        setRecyclerView(view);
        Query query = createQuery();

        //get user outfits
        DbTools.getUserOutfits(query, new DbTools.OnUserOutfitsCallback() {
            @Override
            public void onUserOutfitsLoaded(List<Outfit> outfits) {

                removeOutfitsWithoutPhoto(outfits);
                OutfitsFragment.this.outfits = outfits;

                if(outfits.isEmpty()) {
                    TextView noItemsTextView = view.findViewById(R.id.no_items_text_view);
                    noItemsTextView.setText("No outfits found");
                }else{
                    showItemsInRecyclerView();
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });


        AppCompatButton addOutfitBtn = view.findViewById(R.id.add_outfit_btn);
        addOutfitBtn.setOnClickListener(v -> replaceFragment(new AddOutfitFragment()));

        LinearLayout filterBtn = view.findViewById(R.id.filter_btn);
        filterBtn.setOnClickListener(v -> replaceFragment(new OutfitsFilterFragment()));

        TextView ootdTextView = view.findViewById(R.id.ootd_btn);
        ootdTextView.setOnClickListener(v -> showDatePickerDialog());


        return view;
    }

    private void removeOutfitsWithoutPhoto(List<Outfit> outfits) {
        Iterator<Outfit> iterator = outfits.iterator();
        while (iterator.hasNext()) {
            Outfit outfit = iterator.next();
            Log.i("LINK", outfit.getImageLink());
            if (outfit.getImageLink().equals("null") || outfit.getImageLink().isEmpty()) {
                iterator.remove();
            }
        }
    }


    private void setRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.rv_outfits);
        int numberOfColumns = calculateNumberOfColumns(requireContext(),120);
        layoutManager = new GridLayoutManager(getContext(),numberOfColumns);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new OutfitAdapter(new ArrayList<>(),new WeakReference<>(getContext()), OutfitsFragment.this,0);
        recyclerView.setAdapter(adapter);
    }

    private void showItemsInRecyclerView() {
        List<PhotoItemModel> data = getOutfitModels(outfits);
        adapter = new OutfitAdapter(data,new WeakReference<>(getContext()),OutfitsFragment.this,0);
        recyclerView.setAdapter(adapter);
    }
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack("outfits");
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onItemClick(int position, int recyclerViewId) {
        if(mDate != null) {
            //user previous fragment was planning outfit
            logOutfit(userId, outfits.get(position),mDate,requireContext());
            //update percentiles of worn items
            getUserClothingItemsByIds(userId, outfits.get(position).getClothingItemsIds(), new DbTools.OnUserClothingItemsCallback() {
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
        }else{
            //user previous fragment was outfits startpage
            //show outfit detail
            Outfit outfit = outfits.get(position);
            Bundle bundle = new Bundle();
            bundle.putParcelable("outfit", outfit);
            OutfitDetailFragment outfitDetailFragment = new OutfitDetailFragment();
            outfitDetailFragment.setArguments(bundle);
            replaceFragment(outfitDetailFragment);
        }
    }
    private Query createQuery() {
        Query query = FirebaseFirestore.getInstance().collection("user_clothes")
                    .document(userId).collection("outfits");
        if(getArguments() != null) {
            if(mOccasion != null && !mOccasion.equals("-")) {
                query = query.whereEqualTo("occasion",mOccasion);
            }
            if(mSeason != null && !mSeason.equals("-")) {
                query = query.whereEqualTo("season",mSeason);
            }
            return query;
        }
        return query;
    }

    private void showDatePickerDialog() {
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
                isOutfitPlannedOnDate(userId, date, new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        QuerySnapshot document = task.getResult();
                        if (document != null && !document.isEmpty()) {
                            List<Outfit> outfits = new ArrayList<>();
                            for(QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                outfits.add(parseOutfit(documentSnapshot));
                            }
                            Bundle bundle = new Bundle();
                            bundle.putParcelable("outfit", outfits.get(0));
                            OutfitDetailFragment outfitDetailFragment = new OutfitDetailFragment();
                            outfitDetailFragment.setArguments(bundle);
                            replaceFragment(outfitDetailFragment);
                        } else {
                            Toast.makeText(getActivity(),"there is no outfit planned on that day",Toast.LENGTH_SHORT).show();
                            showDatePickerDialog();
                        }

                    }
                });

            }
        }, year, month, dayOfMonth);

        datePickerDialog.show();
    }
}