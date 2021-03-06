package de.farbtrommel.zwitscherwecker;

import android.content.Context;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;

public class QuizSet {
    private Context mContext;
    private HashMap<Integer, Bird> mBirds = new HashMap<Integer, Bird>();

    public QuizSet(Context context) throws IOException, XmlPullParserException {
        mContext = context;
        loadXml();
    }

    /**
     * Load the Bird the whole information.
     * @throws java.io.IOException
     */
    private void loadXml() throws IOException, XmlPullParserException {

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setValidating(false);
        XmlPullParser xrp = factory.newPullParser();
        String filename = "birds@en.xml";
        if (Locale.getDefault().equals(Locale.GERMANY)) {
            filename = "birds@de.xml";
        }
        InputStream raw = mContext.getAssets().open(filename);
        xrp.setInput(raw, null);

        xrp.next();
        int eventType = xrp.getEventType();

        int id = 0;
        Bird bird = null;
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_DOCUMENT) {
                //do nothing
            } else if (eventType == XmlPullParser.START_TAG) {
                if (xrp.getName().equals("birds")) {
                    eventType = xrp.next();
                    continue;
                } else if (xrp.getName().equals("bird")) {
                    if (bird != null)
                        mBirds.put(id, bird);

                    id = Integer.valueOf(xrp.getAttributeValue("", "id"));
                    bird  = new Bird();
                    bird.mId = id;
                } else if (xrp.getName().equals("name")) {
                    eventType = xrp.next();
                    bird.mName = xrp.getText();
                } else if (xrp.getName().equals("sciname")) {
                    eventType = xrp.next();
                    bird.mSciname = xrp.getText();
                } else if (xrp.getName().equals("link")) {
                    eventType = xrp.next();
                    bird.mLink = xrp.getText();
                } else if (xrp.getName().equals("mp3")) {
                    bird.mMp3 = xrp.getAttributeValue("", "src");
                } else if (xrp.getName().equals("abs")) {
                    eventType = xrp.next();
                    bird.mAbs = xrp.getText();
                } else if (xrp.getName().equals("img")) {
                    bird.mImg = xrp.getAttributeValue("", "src");
                    while (true) {
                        eventType = xrp.next();
                        if (eventType == XmlPullParser.START_TAG) {
                            if (xrp.getName().equals("link")) {
                                bird.mLizenz.mOrigin = xrp.getAttributeValue("", "href");
                            } else if (xrp.getName().equals("lizenz")) {
                                bird.mLizenz.mLink = xrp.getAttributeValue("", "href");
                                eventType = xrp.next();
                                bird.mLizenz.mName = xrp.getText();
                            } else if (xrp.getName().equals("author")) {
                                bird.mLizenz.mAuthorLink = xrp.getAttributeValue("", "href");
                                eventType = xrp.next();
                                bird.mLizenz.mAuthor = xrp.getText();
                            }
                        } else if (eventType == XmlPullParser.END_TAG
                                && xrp.getName().equals("img")) {
                            break;
                        }
                    }
                }
            }
            eventType = xrp.next();
        }
        //add the last bird to list
        if (bird != null)
            mBirds.put(id, bird);
    }

    public Bird get(int i) {
        return mBirds.get(i);
    }

    class Bird implements Comparator<Bird> {
        public Integer mId;
        public String mName;
        public Lizenz mLizenz = new Lizenz();
        public String mSciname;
        public String mLink;
        public String mMp3;
        public String mImg;
        public String mAbs;

        @Override
        public int compare(Bird bird, Bird bird2) {
            return bird.mId.compareTo(bird2.mId);
        }

        class Lizenz {
            public String mName;
            public String mLink;
            public String mOrigin;
            public String mAuthor;
            public String mAuthorLink;
        }
    }
}
