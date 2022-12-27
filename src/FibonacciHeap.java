/**
 * FibonacciHeap
 *
 * An implementation of a Fibonacci Heap over integers.
 */
public class FibonacciHeap
{
    private static int linksCnt = 0; // A static variable that's used as a counter to the number of links performed in every FibonacciHeap instance.
    private static int cutsCnt = 0; // A static variable that's used as a counter to the number of cuts performed in every FibonacciHeap instance.

    private HeapNode firstNode = null; // A field that points to a HeapNode that's the root of the first tree in the heap.
    private HeapNode minNode = null; // A field that points to a HeapNode that has the minimum key value in the heap (the Node is also a root of a tree).
    private int size = 0; // A field that stores the number of nodes in the heap.
    private int treesCnt = 0; // A field that stores the number of trees in the heap.
    private int marksCnt = 0; // A field that stores the number of marked nodes in the heap.

    /**
     * public boolean isEmpty()
     *
     * Returns true if and only if the heap is empty.
     *
     * Time complexity of O(1) in the worst case.
     */
    public boolean isEmpty()
    {
        return firstNode == null; // If the pointer is null then there aren't any trees in the heap, and the heap is empty.
    }

    /**
     * public HeapNode insert(int key)
     *
     * Creates a node (of type HeapNode) which contains the given key, and inserts it into the heap.
     * The added key is assumed not to already belong to the heap.
     *
     * Returns the newly created node.
     *
     * Time complexity of O(1) in the worst case.
     */
    public HeapNode insert(int key)
    {
        HeapNode newNode = new HeapNode(key); // Create the new node to insert to the tree.
        if (!isEmpty()){
            setPrevNext(newNode, firstNode); // If the tree isn't empty then use setPrevNext to set it as the previous node of the first tree in the heap.
        }
        firstNode = newNode; // Set the new node as the first tree in the heap.
        updateMin(newNode); // Update the minimum node in the heap if needed.
        size++; // Add 1 to the heap size counter.
        treesCnt++; // Add 1 to the trees counter.
        return newNode; // Return the inserted node.
    }

    /**
     * private void setPrevNext(HeapNode nodeToSetAsPrev, HeapNode node)
     * Sets the nodeToSetAsPrev as the previous node of node, while updating the necessary pointers to maintain the circular linked list
     *
     * Time complexity of O(1) in the worst case.
     */
    private void setPrevNext(HeapNode nodeToSetAsPrev, HeapNode node){
        nodeToSetAsPrev.next = node; // Set the next of nodeToSetAsPrev to be node/
        nodeToSetAsPrev.prev = node.prev; // Set the prev of nodeToSetAsPrev to be the prev of node.
        node.prev = nodeToSetAsPrev; // Set the prev of node to be nodeToSetAsPrev.
        nodeToSetAsPrev.prev.next = nodeToSetAsPrev; // Set the next of the original prev of node to be nodeToSetAsPrev.
    }

    /**
     * private void connectNodes(HeapNode prevNode, HeapNode nextNode)
     * Sets the prevNode to be the prev of nextNode and vice versa.
     *
     * Time complexity of O(1) in the worst case.
     */
    private void connectNodes(HeapNode prevNode, HeapNode nextNode){
        prevNode.next = nextNode;
        nextNode.prev = prevNode;
    }

    /**
     * public void updateMin(HeapNode node)
     * Checks if the given node is the minimum node in the heap, and updates minNode if necessary.
     *
     * Time complexity of O(1) in the worst case.
     */
    private void updateMin(HeapNode node){
        if (minNode == null){ // If minNode points to null then minNode should be updated to the given node.
            minNode = node;
        }
        if (node.key < minNode.key){ // If the given node's key is less than minNode's key, then miNode should point to the given node instead.
            minNode = node;
        }
    }

    /**
     * public void deleteMin()
     *
     * Deletes the node containing the minimum key.
     *
     * Time complexity of O(n) in the worst case.
     */
    public void deleteMin()
    {
        HeapNode node = minNode;
        if (node.child != null){ // If the node has children.
            HeapNode firstChild = node.child; // Create a pointer to the node's first child.
            HeapNode lastChild = firstChild.prev; // Create a pointer to the node's last child.
            do { //Unmark all the children, and set their parent pointer to null, as they're going to be roots after the deletion.
                firstChild.isMarked = false;
                firstChild.parent = null;
                firstChild = firstChild.next;
            } while (firstChild != node.child); // Run through every node until the pointer is back to the first node.
            if (node.next == node){ // If the node is the root of the only tree in the heap.
                firstNode = firstChild; // Set the first child of the deleted node to be the first child of the tree.
                minNode = firstNode; // Set the minNode pointer to point at firstNode (the pointer will be updated to the actual min in consolidate later).
            }
            else { // If the node is not the only tree in the heap.
                connectNodes(node.prev, firstChild); // Set the next of the last tree to point at the first child of the deleted node.
                connectNodes(lastChild, node.next); // Set the prev of the first tree to point at the first child of the deleted node.
                if (firstNode == node){ // If the deleted node is the first root in the heap.
                    firstNode = firstChild; // Set the firstNode pointer to point at the second node in the tree.
                }
            }
        }
        else{ // If the deleted node doesn't have children.
            if (node.next == node){ // If the node is the root of the only tree in the heap.
                firstNode = null; // Set the pointers to null, as the heap is now empty.
                minNode = null; // Set the pointers to null, as the heap is now empty.
            }
            else { // If the node is not the only tree in the heap.
                connectNodes(node.prev, node.next); // Set the next of the prev of the node to be the prev of the node and vice versa.
                if (firstNode == node){ // If the deleted node is the first root in the heap.
                    firstNode = node.next; // Set the first root in the heap to be the next of the deleted node.
                }
            }
        }
        if (!isEmpty()){ // If the heap isn't empty then use consolidate to consolidate the trees in the heap.
            consolidate();
        }
        size--; // Decrease the size of the tree by 1.
    }

    /**
     * public void consolidate()
     *
     * Consolidates all the trees with the same rank into one tree.
     * Goes over all the trees in the heap, and in the index which is equal to their rank add them to an array.
     * If there is already a tree in that index, merge them into one, where one is the son of the other.
     * Then, inserts all the trees in the array into a new heap.
     *
     * Time complexity of O(n) in the worst case.
     */
    public void consolidate(){
        HeapNode[] trees = new HeapNode[(int)(Math.log(size)/Math.log(1.61803398875) + 1)];
        HeapNode node = firstNode;
        firstNode.prev.next = null;
        do {
            HeapNode nextNode = node.next;
            int nodeRank = node.rank;
            if (trees[nodeRank] == null){
                trees[nodeRank] = node;
                node = nextNode;
            }
            else{
                HeapNode smallKeyNode = node.key < trees[nodeRank].key ? node : trees[nodeRank];
                HeapNode largeKeyNode = node.key > trees[nodeRank].key ? node : trees[nodeRank];
                if (smallKeyNode.child == null){
                    largeKeyNode.prev = largeKeyNode;
                    largeKeyNode.next = largeKeyNode;
                }
                else {
                    setPrevNext(largeKeyNode, smallKeyNode.child);
                }
                smallKeyNode.child = largeKeyNode;
                largeKeyNode.parent = smallKeyNode;
                smallKeyNode.rank++;
                smallKeyNode.next = nextNode;
                node = smallKeyNode;
                trees[nodeRank] = null;
                linksCnt++;
            }
        } while (node != null);
        firstNode = null;
        treesCnt = 0;
        for (int i = 0; i < trees.length; i++) {
            if (trees[i] != null){
                if (firstNode == null){
                    firstNode = trees[i];
                    firstNode.next = firstNode;
                    firstNode.prev = firstNode;
                    minNode = firstNode;
                }
                else{
                    setPrevNext(trees[i], firstNode);
                    if (trees[i].key < minNode.key){
                        minNode = trees[i];
                    }
                }
                treesCnt++;
            }
        }
    }

    /**
     * public HeapNode findMin()
     *
     * Returns the node of the heap whose key is minimal, or null if the heap is empty.
     *
     * Time complexity of O(1) in the worst case.
     */
    public HeapNode findMin()
    {
        return minNode;
    }

    /**
     * public void meld (FibonacciHeap heap2)
     *
     * Melds heap2 with the current heap.
     *
     * Time complexity of O(1) in the worst case.
     */
    public void meld(FibonacciHeap heap2)
    {
        size += heap2.size;
        treesCnt += heap2.treesCnt;
        marksCnt += heap2.marksCnt;
        if (this.isEmpty()){
            this.firstNode = heap2.firstNode;
            this.minNode = heap2.minNode;
        }
        else if (!heap2.isEmpty()) {
            HeapNode heap1LastNode = this.firstNode.prev;
            HeapNode heap2LastNode = heap2.firstNode.prev;
            connectNodes(heap1LastNode, heap2.firstNode);
            connectNodes(heap2LastNode, this.firstNode);
            updateMin(heap2.minNode);
        }
    }

    /**
     * public int size()
     *
     * Returns the number of elements in the heap.
     *
     * Time complexity of O(1) in the worst case.
     */
    public int size()
    {
        return size;
    }

    /**
     * public int[] countersRep()
     *
     * Return an array of counters. The i-th entry contains the number of trees of order i in the heap.
     * An empty heap returns an empty array.
     *
     * The function finds the maximum rank tree in the heap, and creates an array with that length.
     * Then it goes over all of the trees and adds their rank to the array in the corresponding index.
     *
     * Time complexity of O(n) in the worst case.
     */
    public int[] countersRep()
    {
        if (isEmpty()){
            return new int[0];
        }
        HeapNode curNode = firstNode;
        int maxRank = 0;
        do {
            if (curNode.rank > maxRank){
                maxRank = curNode.rank;
            }
            curNode = curNode.next;
        } while (curNode != firstNode);

        int[] output = new int[maxRank + 1];

        do {
            output[curNode.rank]++;
            curNode = curNode.next;
        } while (curNode != firstNode);
        return output;
    }

    /**
     * public void delete(HeapNode x)
     *
     * Deletes the node x from the heap.
     * It is assumed that x indeed belongs to the heap.
     *
     * Time complexity of O(n) in the worst case.
     */
    public void delete(HeapNode x)
    {
        decreaseKey(x, x.key - minNode.key + 1);
        deleteMin();
    }

    /**
     * public void decreaseKey(HeapNode x, int delta)
     *
     * Decreases the key of the node x by a non-negative value delta. The structure of the heap should be updated
     * to reflect this change (for example, the cascading cuts procedure should be applied if needed).
     *
     * Time complexity of O(n) in the worst case.
     */
    public void decreaseKey(HeapNode x, int delta)
    {
        x.key -= delta;
        if (x.key < minNode.key){
            minNode = x;
        }
        if (!x.isRoot() && x.key < x.parent.key){
            do {
                HeapNode parentNode = x.parent;
                cut(x);
                x = parentNode;
            } while(!x.isRoot() && x.isMarked);
            if (!x.isRoot()) {
                x.isMarked = true;
                marksCnt++;
            }
        }
    }

    /**
     * private void cut(HeapNode node)
     *
     * The function disconnects the node from its parent.
     *
     * Time complexity of O(1) in the worst case.
     */
    private void cut(HeapNode node){
        HeapNode parentNode = node.parent;
        if (node.next == node){ // If node is only child.
            parentNode.child = null;
        }
        else{
            if (parentNode.child == node) { // If node is pointed by his parent.
                parentNode.child = node.next;
            }
            connectNodes(node.prev, node.next);
        }
        parentNode.rank--;
        node.parent = null;
        if (node.isMarked){
            node.isMarked = false;
            marksCnt--;
        }
        setPrevNext(node, firstNode);
        firstNode = node;
        treesCnt++;
        cutsCnt++;
    }

    /**
     * public int potential()
     *
     * This function returns the current potential of the heap, which is:
     * Potential = #trees + 2*#marked
     *
     * In words: The potential equals to the number of trees in the heap
     * plus twice the number of marked nodes in the heap.
     *
     * Time complexity of O(1) in the worst case.
     */
    public int potential()
    {
        return treesCnt + (2*marksCnt);
    }

    /**
     * public static int totalLinks()
     *
     * This static function returns the total number of link operations made during the
     * run-time of the program. A link operation is the operation which gets as input two
     * trees of the same rank, and generates a tree of rank bigger by one, by hanging the
     * tree which has larger value in its root under the other tree.
     *
     * Time complexity of O(1) in the worst case.
     */
    public static int totalLinks()
    {
        return linksCnt;
    }

    /**
     * public static int totalCuts()
     *
     * This static function returns the total number of cut operations made during the
     * run-time of the program. A cut operation is the operation which disconnects a subtree
     * from its parent (during decreaseKey/delete methods).
     *
     * Time complexity of O(1) in the worst case.
     */
    public static int totalCuts()
    {
        return cutsCnt;
    }

    /**
     * public static int[] kMin(FibonacciHeap H, int k)
     *
     * This static function returns the k smallest elements in a Fibonacci heap that contains a single tree.
     *
     * The function creates a new FibonacciHeap, and inserts into it a new node with the min value of the given heap.
     * Then it inserts the minimal node key (in the new heap) into the array.
     * It then inserts the children of the inserted node (in the original heap) into the new heap.
     *
     * Time complexity of O(k*deg(H)) in the worst case (deg(H) is the degree of the only tree in H).
     */
    public static int[] kMin(FibonacciHeap H, int k)
    {
        int[] output = new int[k];
        FibonacciHeap sorterHeap = new FibonacciHeap();
        HeapNode node = sorterHeap.insert(H.minNode.key);
        node.originNode = H.minNode;
        for (int i = 0; i < k; i++) {
            node = sorterHeap.findMin().originNode;
            output[i] = node.key;
            sorterHeap.deleteMin();
            if (node.child != null) {
                HeapNode child = node.child;
                HeapNode firstChild = node.child;
                do {
                    node = sorterHeap.insert(child.key);
                    node.originNode = child;
                    child = child.next;
                } while (child != firstChild);
            }
        }
        return output;
    }

    /**
     * public class HeapNode
     *
     * Each node has next and previous nodes, which form a circular linked list.
     * Info can be easily added with a new field, getInfo and setInfo, but wasn't added because the data structure doesn't need it to function.
     *
     */
    public static class HeapNode{

        public int key; // Key of the node.
        public int rank; // Rank of the node, calculated by the number of children it has.
        public boolean isMarked; // True iff one of its children have been cut from the node.
        public HeapNode child; // Child of the node.
        public HeapNode next; // Next node in the list.
        public HeapNode prev; // Previous node in the list.
        public HeapNode parent; // Parent of the node.
        public HeapNode originNode; // Only used in kMin, to keep track of the original node of the node inserted into the sorterHeap.

        /**
         *  public int getKey()
         *
         *  Constructor for HeapNode.
         *
         *  Creates a node with the given key, rank 0, and not marked.
         *  The pointers next and prev are set to itself, which means the node is part of a list with only itself when created.
         *  Parent is set to null when created.
         *
         *  Time complexity of O(1) in the worst case.
         */
        public HeapNode(int key) {
            this.key = key;
            this.rank = 0;
            this.isMarked = false;
            this.prev = this;
            this.next = this;
        }

        /**
         *  public int getKey()
         *
         *  Returns the key of the current node.
         *
         *  Time complexity of O(1) in the worst case.
         */
        public int getKey() {
            return this.key;
        }

        /**
         *  public boolean isRoot()
         *
         *  Returns true if the node is a root node, which means it has no parent.
         *
         *  Time complexity of O(1) in the worst case.
         */
        public boolean isRoot(){
            return parent == null;
        }
    }
}