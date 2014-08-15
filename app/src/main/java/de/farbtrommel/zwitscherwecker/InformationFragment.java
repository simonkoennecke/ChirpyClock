package de.farbtrommel.zwitscherwecker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link InformationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link InformationFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class InformationFragment extends Fragment implements View.OnClickListener,
        AdapterView.OnItemClickListener {

    private OnFragmentInteractionListener mListener;

    public InformationFragment() {
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
        View v = inflater.inflate(R.layout.fragment_information, container, false);
        TabHost tabHost = (TabHost) v.findViewById(R.id.tabHost);
        tabHost.setup();
        TabHost.TabSpec spec1 = tabHost.newTabSpec(
                getResources().getString(R.string.info_tab_title_author));
        spec1.setIndicator(getResources().getString(R.string.info_tab_title_author));
        spec1.setContent(R.id.Author);


        TabHost.TabSpec spec2 = tabHost.newTabSpec(
                getResources().getString(R.string.info_tab_title_credentials));
        spec2.setIndicator(getResources().getString(R.string.info_tab_title_credentials));
        spec2.setContent(R.id.Credentials);

        TabHost.TabSpec spec3 = tabHost.newTabSpec(
                getResources().getString(R.string.info_tab_title_privacy));
        spec3.setIndicator(getResources().getString(R.string.info_tab_title_privacy));
        spec3.setContent(R.id.Privacy);

        tabHost.addTab(spec1);
        tabHost.addTab(spec2);
        tabHost.addTab(spec3);

        //listViewCredentials
        ListView list = (ListView) v.findViewById(R.id.listViewCredentials);
        list.setOnItemClickListener(this);
        return v;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
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

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String[] urls = getResources().getStringArray(R.array.credentials_link_list);

        Uri uri = Uri.parse(urls[i]);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and mName
        void onFragmentInteraction(Uri uri);
    }

}
