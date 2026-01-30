import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.util.concurrent.ThreadLocalRandom;


public class q1 {
    

    // parameters and their default values
    public static String imagebase = "icon" ; // base name of the input images; actual names append "1.png", "2.png" etc.
    public static int threads = 1; // number of threads to use
    public static int outputheight = 2048; // output image height
    public static int outputwidth = 2048; // output image width
    public static int n = 100; // icons added/changed
    public static final int ICONS = 8; // total number of icons

    // icons image
    public static BufferedImage[] icons = new BufferedImage[ICONS];

    // output image
    public static BufferedImage imgout;

    // print out command-line parameter help and exit
    public static void help(String s) {
        System.out.println("Could not parse argument \""+s+"\".  Please use only the following arguments:");
        System.out.println(" -w outputimagewidth (integer; current=\""+outputwidth+"\")");
        System.out.println(" -h outputimageheight (integer; current=\""+outputheight+"\")");
        System.out.println(" -t threads (integer value >=1; current=\""+threads+"\")");
        System.out.println(" -n changes (integer value >=1; current=\""+n+"\")");
        System.exit(1);
    }

    // process command-line options
    public static void opts(String[] args) {
        int i = 0;

        try {
            for (;i<args.length;i++) {
                if (i==args.length-1)
                    help(args[i]);
                if (args[i].equals("-h")) {
                    outputheight = Integer.parseInt(args[i+1]);
                } else if (args[i].equals("-w")) {
                    outputwidth = Integer.parseInt(args[i+1]);
                } else if (args[i].equals("-t")) {
                    threads = Integer.parseInt(args[i+1]);
                } else if (args[i].equals("-n")) {
                    n = Integer.parseInt(args[i+1]);
                } else {
                    help(args[i]);
                }
                // an extra increment since our options consist of 2 pieces
                i++;
            }
        } catch (Exception e) {
            System.err.println(e);
            help(args[i]);
        }
    }

    // main.  we allow an IOException in case the image loading/storing fails.
    public static void main(String[] args) throws IOException {
        // process options
        opts(args);

        // read in one of the input images (icons).  You'll need to expand/duplicate this code to read
        // different icons and find their sizes.
        for (int i=1;i<=ICONS;i++) {
            icons[i-1] = ImageIO.read(new File(imagebase+i+".png"));
        }

        // create an output image
        BufferedImage outputimage = new BufferedImage(outputwidth,outputheight,BufferedImage.TYPE_INT_ARGB);

        // demonstrate copying an icon into the output image.
        // this is not the only way to read or modify image pixels, but it is a starting point
        for (int i=0;i<icons[0].getWidth();i++) {
            for (int j=0;j<icons[0].getHeight();j++) {
                outputimage.setRGB(i,j,icons[0].getRGB(i,j));
            }
        }

        // Write out the image
        File outputfile = new File("outputimage.png");
        ImageIO.write(outputimage, "png", outputfile);

        //test
        worker worker = new worker();
        for (int i = 0; i < threads; i++) {
            Thread thread = new Thread(worker);
            thread.start();
        }

    }




    // idea: we are tring to access the same outputimage thus this should be our shared memory object
    // for each thread:
    // 1. select a random icon
    // 2. select a random position 
    // 3. varify the icon --add with no overlap  --replace an existing icon
    // 4. execute or give up and go back to step 1

    // for each thread if it is doing its work --> in C.S. --> take the lock

    // for timing use System.currentTimeMillis() and output the time, plot time vs t


    // here first we make a runnable class
    public static class worker implements Runnable {
        @Override
        public void run() {
            // here we use the thread local random instead of the Random class
            ThreadLocalRandom random = ThreadLocalRandom.current();
            // pick a random icon
            int icon = random.nextInt(ICONS);
            System.out.println("Picked icon: " + icon);
        }
    }


}
