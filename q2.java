
public class q2 {
    
    // data structure for the list
    public static class Node {
        final char character;
        // we want first 3 node of abc to be protected
        final boolean preotectedABC;
        // we need the next node to be seen by all threads
        // otherwise when one is deleting a node and anoter is inserting a node, we might break the list
        volatile Node next;

        // constructor
        public Node(char character, boolean preotectedABC) {
            this.character = character;
            this.preotectedABC = preotectedABC;
        }
    }

    static Node head;
    // want other threads to see main thread changing this running variable
    static volatile boolean running = true;
    // simple init of circular linked list start with abc nodes
    static void init() {
        Node a = new Node('A', true);
        Node b = new Node('B', true);
        Node c = new Node('C', true);

        a.next = b;
        b.next = c;
        c.next = a;

        head = a;
    }

    public static void main(String[] args) throws InterruptedException {
        // initialize the list
        init();
        // create the threads
        Thread t0 = new Thread(new worker0());
        Thread t1 = new Thread(new worker1());
        Thread t2 = new Thread(new worker2());

        // start the threads
        t0.start();
        t1.start();
        t2.start();

        // want simulation to run for 5 seconds
        Thread.sleep(5000);
        running = false;
        // join the threads
        try {
            t0.join();
            t1.join();
            t2.join();
        } catch (InterruptedException ignored) {}
        // print the final list
        System.out.println();
        printFinalList();
    }








    







    // not much to consider here since it is just traversing the list
    public static class worker0 implements Runnable {
        @Override
        public void run() {
            Node current = head;
            while (running) {
                System.out.print(current.character + " ");
                System.out.flush();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
                current = current.next;
            }
        }
    }




    // make next volatile to ensure all threads see the same next node
    public static class worker1 implements Runnable {
        @Override
        public void run() {
            Node current = head.next;
            Node prev = head;

            while (running) {
                //10% chanec to remove a char
                if (Math.random() < 0.1 && !current.preotectedABC) {
                    // T2 is changing current.next while T1 is reading it
                    prev.next = current.next;
                    current = prev.next;
                }
                // otherwise just move to the next node
                else {
                    prev = current;
                    current = current.next;
                }
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    // potential problems:
    // 1. loss insert: t2 - current.next = newNode; t1 - prev.next = current,next; here we miss a delete
    // 2. lost delete: t1 - prev.next = current,next; t2 - current.next = newNode; here we miss a insert

    public static class worker2 implements Runnable {
        private char randomNonABC() {
            while (true) {
                char c = (char) ('A' + (int)(Math.random() * 26));
                if (c != 'A' && c != 'B' && c != 'C') {
                    return c;
                }
            }
        }

        
        @Override
        public void run() {
            
            Node current = head;

            while (running) {
                if (Math.random() < 0.1) {
                    Node oldNode = current.next;
                    Node newNode = new Node(randomNonABC(), false);
                    newNode.next = oldNode;
                    // T1 is reading this current.next 
                    current.next = newNode;
                }
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ignored) {}
                current = current.next;
            }
        }
    }

    // helper function to print the final list
    static void printFinalList() {
        Node start = head;
        Node cur = start;
    
        do {
            System.out.print(cur.character + " ");
            cur = cur.next;
        } while (cur != start);
    
        System.out.println();
    }
}
