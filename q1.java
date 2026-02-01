import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.util.concurrent.ThreadLocalRandom;


public class q1 {
    

    // parameters and their default values
    public static String imagebase = "icon" ; // base name of the input images; actual names append "1.png", "2.png" etc.
    public static int threads = 8; // number of threads to use
    public static int outputheight = 2048; // output image height
    public static int outputwidth = 2048; // output image width
    public static int n = 100; // icons added/changed
    public static final int ICONS = 8; // total number of icons

    // locks not working deprecated
    // static final Object IMG_LOCK = new Object();
    static final Object COUNT_LOCK = new Object();
    
    // tile lock
    static Object[] tilelocks;
    static final int tileCols = 16;
    static final int tileRows = 16;
    static int tileW, tileH;

    static void initTileLocks() {
        tileW = (outputwidth  + tileCols - 1) / tileCols;   
        tileH = (outputheight + tileRows - 1) / tileRows;
    
        tilelocks = new Object[tileCols * tileRows];
        for (int i = 0; i < tilelocks.length; i++) tilelocks[i] = new Object();
    }
    // deprecated
    // static int singleTileIndexOrMinus1(int x, int y, int w, int h) {
    //     if (x < 0 || y < 0 || x + w > outputwidth || y + h > outputheight) return -1;
    
    //     int tx0 = Math.min((x) / tileW, tileCols - 1);
    //     int ty0 = Math.min((y) / tileH, tileRows - 1);
    //     int tx1 = Math.min((x + w - 1) / tileW, tileCols - 1);
    //     int ty1 = Math.min((y + h - 1) / tileH, tileRows - 1);
    
    //     if (tx0 != tx1 || ty0 != ty1) return -1; // crosses tile boundary
    //     return ty0 * tileCols + tx0;
    // }





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
    public static void main(String[] args) throws IOException, InterruptedException {
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
        

        imgout = outputimage;

        // Write out the image
        File outputfile = new File("outputimage.png");
        
        // init tile locks
        initTileLocks();


        // set timer
        long startTime = System.currentTimeMillis();
        //test
        Thread[] ts = new Thread[threads];
        
        for (int i = 0; i < threads; i++) {
            ts[i] = new Thread(new worker());
            ts[i].start();
        }

        for (int i = 0; i < threads; i++) {
            ts[i].join();
        }
        System.out.println(System.currentTimeMillis() - startTime);

        ImageIO.write(imgout, "png", outputfile);
        
        

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
            while (true) {
                // decrement the count
                synchronized (COUNT_LOCK) {
                    if (n <= 0) return;
                    n--;
                }
                while (true) {
                    // pick a random icon
                    int icon = random.nextInt(ICONS);
                    // get the width and height of the icon
                    int width = icons[icon].getWidth();
                    // get the height of the icon
                    int height = icons[icon].getHeight();

                    // pick a random tile
                    int tx = random.nextInt(tileCols);
                    int ty = random.nextInt(tileRows);

                    // get the left and top of the tile
                    int left = tx * tileW;
                    int top  = ty * tileH;

                    // get the maximum x and y of the icon
                    int maxX = Math.min(left + tileW - width,  outputwidth - width);
                    int maxY = Math.min(top  + tileH - height, outputheight - height);

                    // if the icon can't fit in this tile, continue
                    if (maxX < left || maxY < top) continue; 

                    int x = left + random.nextInt(maxX - left + 1);
                    int y = top  + random.nextInt(maxY - top  + 1);

                    int idx = ty * tileCols + tx; // guaranteed single tile
                    if (idx == -1) {
                        System.err.println("idx is -1");
                        continue;
                    }


                    synchronized (tilelocks[idx]) {
                        // System.err.println(idx);
                        // // varify the icons
                        boolean posvalid = true;
                        // top & bottom edges
                        for (int i = 0; i < width && posvalid; i++) {
                            if (imgout.getRGB(x + i, y) != 0) posvalid = false;
                            if (imgout.getRGB(x + i, y + height - 1) != 0) posvalid = false;
                        }

                        // left & right edges (skip corners)
                        for (int j = 1; j < height - 1 && posvalid; j++) {
                            if (imgout.getRGB(x, y + j) != 0) posvalid = false;
                            if (imgout.getRGB(x + width - 1, y + j) != 0) posvalid = false;
                        }
                        if (!posvalid) {
                            continue;
                        }
    
                        // add the icon to the output image
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                              int iconPixel = icons[icon].getRGB(i, j); 
                              imgout.setRGB(x + i, y + j, iconPixel);   
                            }
                          }
                        }
                        break;
                    }
                }
            }
        }
    }


                
            
        

