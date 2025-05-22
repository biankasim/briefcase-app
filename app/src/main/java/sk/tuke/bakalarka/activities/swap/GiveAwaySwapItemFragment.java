package sk.tuke.bakalarka.activities.swap;


import static sk.tuke.bakalarka.tools.DbTools.removeItemFromDatabase;
import static sk.tuke.bakalarka.tools.DbTools.setAcceptedSwapRequest;


import android.os.Bundle;


import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Map;

import sk.tuke.bakalarka.R;
import sk.tuke.bakalarka.entities.SwapItem;

public class GiveAwaySwapItemFragment extends Fragment {

    private static final String ARG_SWAP_ITEM = "swapItem";
    private SwapItem mSwapItem;
    private Spinner usersRequestSpinner;

    public GiveAwaySwapItemFragment() {
        // Required empty public constructor
    }

    public static GiveAwaySwapItemFragment newInstance(String swapItem) {
        GiveAwaySwapItemFragment fragment = new GiveAwaySwapItemFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SWAP_ITEM, swapItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSwapItem = getArguments().getParcelable(ARG_SWAP_ITEM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_give_away_swap_item, container, false);



        //get user emails from requests and set spinner
        ArrayList<String> userEmails = new ArrayList<>();
        ArrayList<String> userIds = new ArrayList<>();
        if(mSwapItem.getRequests() != null) {
            for (Map.Entry<String, String> requests : mSwapItem.getRequests().entrySet()) {
                userEmails.add(requests.getKey());
                userIds.add(requests.getValue());
            }
        }

        AppCompatButton confirmBtn = view.findViewById(R.id.confirm_btn);
        usersRequestSpinner = view.findViewById(R.id.users_request_spinner);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(requireActivity(), android.R.layout.simple_spinner_item, userEmails);
        usersRequestSpinner.setAdapter(spinnerArrayAdapter);

        if(userIds.isEmpty()) {
            usersRequestSpinner.setVisibility(View.GONE);
            confirmBtn.setVisibility(View.GONE);
            TextView textView = new TextView(requireContext());
            textView.setText("No one sent request");
            LinearLayout linearLayout = view.findViewById(R.id.linear_layout);
            linearLayout.addView(textView);
        }






        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //move clothing item from one user to another
                if(!userIds.isEmpty()) {
                    String newUserId = userIds.get(usersRequestSpinner.getSelectedItemPosition());
                    String oldUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    removeItemFromDatabase(oldUserId, "clothes", String.valueOf(mSwapItem.getId()));
                    setAcceptedSwapRequest(newUserId, String.valueOf(mSwapItem.getSwapId()));
                    replaceFragment(new SwapFragment());
                }
            }
        });

        return view;
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }
}