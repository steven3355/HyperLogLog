import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.cap.EnableCapHandler;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class main {
    public static void main() {

        /*
        Used to get an easy to present estimation using 10000 unique elements and 8 buckets
         */
//        HyperLogLog test = new HyperLogLog(8);
//        for (int i = 0; i < 10000;i++) {
//            test.add(i);
//        }
//        int x = 0;
//        int[] buckets = test.getBuckets();
//        for (int i = 0; i < 8; i++) {
//            System.out.println(buckets[i]);
//            x+= 1<<(buckets[i]);
//        }
//        System.out.println(test.estimate());


        /*
        Used to test error rate using the same data set and different number of partitions
         */
//        for (int i = 1; i < 12; i++) {
//            System.out.println("Bucket size: " + (1<<i));
//            testOneSet(1 << i);
//        }


        /*
        Parse twitch chat
         */
//        estimateTwitchChar();
    }

    /**
     * Bot to parse chat room messages and user names into HLL
     */
    void estimateTwitchChat() {
        final HyperLogLog msgHLL = new HyperLogLog(2048);
        final HyperLogLog usrHLL = new HyperLogLog(2048);
        final Set<String> set = new HashSet<String>();
        ListenerAdapter messageListener = new ListenerAdapter() {
            @Override
            public void onMessage(MessageEvent event) throws Exception {
                super.onMessage(event);
                usrHLL.add(event.getUser().getNick());
                msgHLL.add(event.getMessage());
                set.add(event.getMessage());
            }
        };
        JButton button = new JButton();
        button.setText("Estimate");
        button.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                System.out.println("Estimated Unique Users: " + usrHLL.estimate());
                System.out.println("Actual Unique Messages: " + set.size());
                System.out.println("Estimated Unique Messages: " + msgHLL.estimate());
                System.out.println("Total lines:" + msgHLL.getCount());
                System.out.println("Total String Bytes: " + msgHLL.getTotalStringBytes());
            }

            public void mousePressed(MouseEvent e) {

            }

            public void mouseReleased(MouseEvent e) {

            }

            public void mouseEntered(MouseEvent e) {

            }

            public void mouseExited(MouseEvent e) {

            }
        });
        //Made a button that give the current estimation in the console when pressed.
        JFrame frame = new JFrame("FrameDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(button);
        frame.pack();

        frame.setVisible(true);
//        BasicConfigurator.configure();
        PircBotX myBot = new PircBotX(new Configuration.Builder()
                .setAutoNickChange(false) //Twitch doesn't support multiple users
                .setOnJoinWhoEnabled(false) //Twitch doesn't support WHO command
                .setCapEnabled(true)
                .addCapHandler(new EnableCapHandler("twitch.tv/membership")) //Twitch by default doesn't send JOIN, PART, and NAMES unless you request it, see https://github.com/justintv/Twitch-API/blob/master/IRC.md#membership

                .addServer("irc.twitch.tv")
                .setName("MonkQi") //Your twitch.tv username
                .setServerPassword("oauth:nubvqr6sppize3xr8jipyjhiv33i7r") //Your oauth password from http://twitchapps.com/tmi
                .addAutoJoinChannel("#overwatchleague")
                .addListener(messageListener)
                .buildConfiguration());

        try {
            myBot.startBot();
        } catch (Exception e) {
            e.printStackTrace();
        }

        while(true);
    }
    List<String> shakespearTragedies() {
        List<String> result = new ArrayList<String>();
        result.add("hamlet");
        result.add("coriolanus");
        result.add("cleopatra");
        result.add("julius_caesar");
        result.add("lear");
        result.add("macbeth");
        result.add("timon");
        result.add("titus");
        result.add("romeo_juliet");
        return result;
    }

    /**
     * Test Cardinality of All of Shakespeare's tragedies given an input partition size
     *
     * @param input
     */
    void testOneSet(int input) {
        HyperLogLog test = new HyperLogLog(input);
        int count = 0;
        Set<String> set = new HashSet<String>();
        InputStream is = null;
        List<String> titles = shakespearTragedies();
        for (String title : titles) {
            try {
                URL url = new URL("http://shakespeare.mit.edu/" + title + "/full.html");
                URLConnection con = url.openConnection();
                is = con.getInputStream();
            } catch (Exception e) {
                e.printStackTrace();
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            Document doc = null;
            String text = null;
            do {
                try {
                    line = br.readLine();
                    doc = Jsoup.parse(line);
                    text = doc.body().text();
                } catch (Exception e) {
//                e.printStackTrace();
                }
                Pattern pattern = Pattern.compile("\\w+");
                Matcher matcher = pattern.matcher(text);
                while (matcher.find()) {
                    test.add(matcher.group());
                    set.add(matcher.group());
                    count++;
                }
//                System.out.println(text);
            }
            while (line != null);
        }
        System.out.println("HLL Estimation: " + test.estimate());
        System.out.println("Set size: " + set.size());
        System.out.println("Total word count is: " + count);
    }
}
