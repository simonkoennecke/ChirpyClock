package de.farbtrommel.zwitscherwecker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by simon_000 on 06.08.2014.
 */
public class BirdDetailsFragment extends Fragment implements View.OnClickListener{
    public static String ARG_BIRD_ID = "BIRD_ID";
    public static String ARG_BIRD_ABS = "BIRD_ABS";
    public static String ARG_BIRD_NAME = "BIRD_NAME";
    public static String ARG_BIRD_WIKI_LINK = "BIRD_WIKI_LINK";
    public static String ARG_BIRD_IMG_LINK = "BIRD_IMG_LINK";

    private int id;
    private String abs;
    private TextView _lblAbs;
    private String name;
    private TextView _lblName;
    private String wikiLink;
    private Button _btnMore;
    private String imgLink;
    private ImageView _imgView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.fragment_bird_details, container, false);

        Bundle bundle = this.getArguments();
        if(bundle != null && bundle.containsKey(ARG_BIRD_ID)) {
            id = bundle.getInt(ARG_BIRD_ID);
            //Abstract
            abs = bundle.getString(ARG_BIRD_ABS);
            _lblAbs = (TextView) V.findViewById(R.id.quiz_details_abs);
            _lblAbs.setText(abs);
            //Name
            name = bundle.getString(ARG_BIRD_NAME);
            _lblName = (TextView) V.findViewById(R.id.quiz_details_right);
            _lblName.setText(_lblName.getText() + " " + name + ".");

            wikiLink = bundle.getString(ARG_BIRD_WIKI_LINK);
            _btnMore = (Button) V.findViewById(R.id.quiz_details_more);
            _btnMore.setOnClickListener(this);

            imgLink = bundle.getString(ARG_BIRD_IMG_LINK);
            _imgView = (ImageView) V.findViewById(R.id.quiz_details_img);
            _imgView.setOnClickListener(this);
            int drawableId = getResources().getIdentifier("original_" + id, "drawable", getActivity().getPackageName());
            _imgView.setImageDrawable(getResources().getDrawable(drawableId));
        }

        // Inflate the layout for this fragment
        return V;

    }

    @Override
    public void onClick(View view) {
        Uri uri=null;
        if(view.getId() == R.id.quiz_details_more){
            uri = Uri.parse(wikiLink);
        }
        else if(view.getId() == R.id.quiz_details_img){
            uri = Uri.parse(imgLink);
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}
