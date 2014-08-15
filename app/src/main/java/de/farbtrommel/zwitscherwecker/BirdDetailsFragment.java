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

public class BirdDetailsFragment extends Fragment implements View.OnClickListener {
    public static final String ARG_BIRD_ID = "BIRD_ID";
    public static final String ARG_BIRD_ABS = "BIRD_ABS";
    public static final String ARG_BIRD_NAME = "BIRD_NAME";
    public static final String ARG_BIRD_WIKI_LINK = "BIRD_WIKI_LINK";
    public static final String ARG_BIRD_IMG_LINK = "BIRD_IMG_LINK";

    private int mId;
    private String mAbs;
    private TextView mLblAbs;
    private String mName;
    private TextView mLblName;
    private String mWikiLink;
    private Button mBtnMore;
    private String mImgLink;
    private ImageView mImgView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bird_details, container, false);

        Bundle bundle = this.getArguments();
        if (bundle != null && bundle.containsKey(ARG_BIRD_ID)) {
            mId = bundle.getInt(ARG_BIRD_ID);
            //Abstract
            mAbs = bundle.getString(ARG_BIRD_ABS);
            mLblAbs = (TextView) view.findViewById(R.id.quiz_details_abs);
            mLblAbs.setText(mAbs);
            //Name
            mName = bundle.getString(ARG_BIRD_NAME);
            mLblName = (TextView) view.findViewById(R.id.quiz_details_right);
            mLblName.setText(mLblName.getText() + " " + mName + ".");

            mWikiLink = bundle.getString(ARG_BIRD_WIKI_LINK);
            mBtnMore = (Button) view.findViewById(R.id.quiz_details_more);
            mBtnMore.setOnClickListener(this);

            mImgLink = bundle.getString(ARG_BIRD_IMG_LINK);
            mImgView = (ImageView) view.findViewById(R.id.quiz_details_img);
            mImgView.setOnClickListener(this);
            int drawableId = getResources()
                    .getIdentifier("original_" + mId, "drawable",
                            getActivity().getPackageName());
            mImgView.setImageDrawable(getResources().getDrawable(drawableId));
        }

        // Inflate the layout for this fragment
        return view;

    }

    @Override
    public void onClick(View view) {
        Uri uri = null;
        if (view.getId() == R.id.quiz_details_more) {
            uri = Uri.parse(mWikiLink);
        } else if (view.getId() == R.id.quiz_details_img) {
            uri = Uri.parse(mImgLink);
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}
