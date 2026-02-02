

public class q2 {
    
    public static class Node {
        char character;
        volatile Node next;

        public Node(char character) {
            this.character = character;
            this.next = null;
        }
    }

    static Node tail;

    static void init() {
        Node a = new Node('A');
        Node b = new Node('B');
        Node c = new Node('C');

        a.next = b;
        b.next = c;
        c.next = a;

        tail = c;
    }

    public static void main(String[] args) {
        init();
    }
}
