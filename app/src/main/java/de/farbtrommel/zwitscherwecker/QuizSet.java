package de.farbtrommel.zwitscherwecker;

import android.content.Context;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by simon_000 on 02.08.2014.
 */
public class QuizSet {
    private Context _context;
    private HashMap<Integer,Bird> birds = new HashMap<Integer,Bird>();
    public QuizSet(Context context) throws IOException, XmlPullParserException {
        _context = context;
        loadXml();
    }

    /**
     * Load the Bird the whole information
     * @throws java.io.IOException
     */
    private void loadXml() throws IOException, XmlPullParserException {
        //XmlResourceParser xrp = _context.getAssets().openXmlResourceParser("birds.xml");
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setValidating(false);
        XmlPullParser xrp = factory.newPullParser();
        InputStream raw =_context.getAssets().open("birds.xml");
        xrp.setInput(raw, null);

        xrp.next();
        int eventType = xrp.getEventType();

        int id=0;
        Bird bird = null;
        while (eventType != XmlPullParser.END_DOCUMENT){
            if(eventType == XmlPullParser.START_DOCUMENT){
                //do nothing
            }
            else if(eventType == XmlPullParser.START_TAG){
                if(xrp.getName().equals("birds")) {
                    eventType = xrp.next();
                    continue;
                }
                else if(xrp.getName().equals("bird")) {
                    if(bird != null)
                        birds.put(id, bird);

                    id = Integer.valueOf(xrp.getAttributeValue("","id"));
                    bird  = new Bird();
                    bird.id = id;
                }
                else if(xrp.getName().equals("name")) {
                    eventType = xrp.next();
                    bird.name = xrp.getText();
                }
                else if(xrp.getName().equals("sciname")) {
                    eventType = xrp.next();
                    bird.sciname = xrp.getText();
                }
                else if(xrp.getName().equals("link")) {
                    eventType = xrp.next();
                    bird.link = xrp.getText();
                }
                else if(xrp.getName().equals("mp3")) {
                    bird.mp3 = xrp.getAttributeValue("","src");
                }
                else if(xrp.getName().equals("abs")) {
                    eventType = xrp.next();
                    bird.abs = xrp.getText();
                }
                else if(xrp.getName().equals("img")) {
                    bird.img = xrp.getAttributeValue("", "src");
                    while(true){
                        eventType = xrp.next();
                        if(eventType == XmlPullParser.START_TAG) {
                            if (xrp.getName().equals("link")) {
                                bird.lizenz.origin = xrp.getAttributeValue("", "href");
                            } else if (xrp.getName().equals("lizenz")) {
                                bird.lizenz.link = xrp.getAttributeValue("", "href");
                                eventType = xrp.next();
                                bird.lizenz.name = xrp.getText();
                            } else if (xrp.getName().equals("author")) {
                                bird.lizenz.author_link = xrp.getAttributeValue("", "href");
                                eventType = xrp.next();
                                bird.lizenz.author = xrp.getText();
                            }
                        }
                        else if(eventType == XmlPullParser.END_TAG && xrp.getName().equals("img")){
                            break;
                        }
                    }
                }
            }
            eventType = xrp.next();
        }
        //add the last bird to list
        if(bird != null)
            birds.put(id, bird);
    }

    public Bird get(int i){
        return birds.get(i);
    }
    class Bird implements Comparator<Bird>{
        public Integer id;
        public String name;
        public Lizenz lizenz = new Lizenz();
        public String sciname;
        public String link;
        public String mp3;
        public String img;
        public String abs;

        @Override
        public int compare(Bird bird, Bird bird2) {
            return bird.id.compareTo(bird2.id);
        }

        class Lizenz{
            public String name;
            public String link;
            public String origin;
            public String author;
            public String author_link;
        }
    }
}
