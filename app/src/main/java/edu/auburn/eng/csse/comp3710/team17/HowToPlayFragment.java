package edu.auburn.eng.csse.comp3710.team17;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class HowToPlayFragment extends Fragment {
    private TextView htpText;
    private Button backToMenuButton;
    private OnMenuButtonClickedListener mListener;

    public static HowToPlayFragment newInstance() {
        HowToPlayFragment fragment = new HowToPlayFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public HowToPlayFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.how_to_play, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        htpText = (TextView) getActivity().findViewById(R.id.howToPlayText);
        backToMenuButton = (Button) getActivity().findViewById(R.id.htpBackToMenuButton);

        backToMenuButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction =
                        getActivity().getSupportFragmentManager().beginTransaction();
                transaction.remove(thisFragment());
                transaction.commit();
                if (mListener != null)
                    mListener.onMenuPressed();

            }
        });

        htpText.setText(R.string.howToPlay);

    }

    public Fragment thisFragment() {
        return this;
    }

    public interface OnMenuButtonClickedListener {
        public void onMenuPressed();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnMenuButtonClickedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
