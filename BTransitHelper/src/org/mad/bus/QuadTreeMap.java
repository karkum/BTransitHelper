package org.mad.bus;

import java.util.Collection;
import java.util.LinkedList;

/**
 * A recursive implementation of a node-based tree used to partition a
 * two-dimensional domain by recursively subdividing it into four quadrants. The
 * keys maintained by this tree are decomposed into an x-coordinate and a
 * y-coordinate.
 * 
 * @author Michael D. Naper, Jr. <MichaelNaper.com>
 * @version 2012.08.20
 * 
 * @param <V>
 *          The type of mapped values.
 */
public class QuadTreeMap<V> {

  // Default leaf node capacity; keep in sync with constructor Javadoc
  private static final int DEFAULT_BUCKET_SIZE = 4;

  // Difference tolerance for checking double-typed values for equality
  private static final double EPSILON = 1e-9;

  // The domain represented by this tree
  private final RectangularDomain treeDomain;

  // Maximum capacity of a leaf node before the node is split
  private final int bucketSize;

  // Number of key-value mappings in this tree
  private int size;

  // Root node of this tree
  private TreeNode root;

  /**
   * Constructs a new, empty instance of {@code QuadTreeMap} with the specified
   * domain bounds and leaf-node bucket size.
   * 
   * @param lowerX
   *          The lower x-bound of this map's domain.
   * @param upperX
   *          The upper x-bound of this map's domain.
   * @param lowerY
   *          The lower y-bound of this map's domain.
   * @param upperY
   *          The upper y-bound of this map's domain.
   * @param bucketSize
   *          The bucket size of the tree's leaf-nodes.
   */
  public QuadTreeMap(int lowerX, int upperX, int lowerY,
      int upperY, int bucketSize) {
    if (lowerX > upperX) {
      throw new IllegalArgumentException(
          "lowerX must be less than or equal to upperX.");
    }
    if (lowerY > upperY) {
      throw new IllegalArgumentException(
          "lowerY must be less than or equal to upperY.");
    }
    if (bucketSize < 1) {
      throw new IllegalArgumentException(
          "bucketSize must be a positive integer.");
    }

    treeDomain = new RectangularDomain(lowerX, upperX, lowerY, upperY);
    this.bucketSize = bucketSize;
    size = 0;
    root = FlyweightNode.getInstance();
  }

  /**
   * Constructs a new, empty instance of {@code QuadTreeMap} with the specified
   * domain bounds and default leaf-node bucket size, which is 4.
   * 
   * @param lowerX
   *          The lower x-bound of this map's domain.
   * @param upperX
   *          The upper x-bound of this map's domain.
   * @param lowerY
   *          The lower y-bound of this map's domain.
   * @param upperY
   *          The upper y-bound of this map's domain.
   */
  public QuadTreeMap(int lowerX, int upperX, int lowerY, int upperY) {
    this(lowerX, upperX, lowerY, upperY, DEFAULT_BUCKET_SIZE);
  }

  /**
   * Associates the specified value with the specified key in this map. If the
   * map previously contained a mapping for the key, the old value is replaced.
   * 
   * @param x
   *          The x-coordinate of the key with which the specified value is to
   *          be associated.
   * @param y
   *          The y-coordinate of the key with which the specified value is to
   *          be associated.
   * @param value
   *          The value to be associated with the specified key.
   * @return The previous value associated with the specified key, or
   *         {@code null} if there was no mapping. (A {@code null} return can
   *         also indicate that the map previously associated {@code null} with
   *         the specified key.)
   */
  public V put(double x, double y, V value) {
    Key key = new Key(x, y);
    if (!isElementOfTreeDomain(key)) {
      return null;
    }

    TreeElement<V> element = new TreeElement<V>(key, value);
    Value oldValue = new Value();
    root = put(element, root, treeDomain, oldValue);
    if (!oldValue.isSet()) {
      size++;
    }
    return oldValue.getValue();
  }

  /**
   * Helper recursive method for inserting the specified {@link TreeElement}
   * into the specified subtree.
   * 
   * @param element
   *          The element to be inserted.
   * @param sRoot
   *          The root of the subtree for which to insert the element.
   * @param sDomain
   *          The domain represented by the specified subtree.
   * @param oldValue
   *          The wrapper for storing the previous value associated with the key
   *          of the specified element.
   * @return The new root of the specified subtree.
   */
  public final TreeNode put(TreeElement<V> element, TreeNode sRoot,
      RectangularDomain sDomain, Value oldValue) {
    assert element != null : "element is null.";
    assert sRoot != null : "sRoot is null.";
    assert sDomain != null : "sDomain is null.";
    assert oldValue != null : "oldValue is null.";

    if (sRoot instanceof FlyweightNode) {
      return new LeafNode<V>(element);
    } else if (sRoot instanceof QuadTreeMap.LeafNode) {
      @SuppressWarnings("unchecked")
      LeafNode<V> node = (LeafNode<V>) sRoot;
      if (node.size() < bucketSize) {
        TreeElement<V> oldElement = node.add(element);
        if (oldElement != null) {
          oldValue.setValue(oldElement.getValue());
        }
        return node;
      } else {
        Collection<TreeElement<V>> storedElements = node.getElements();
        TreeNode replacementNode = new InternalNode();
        for (TreeElement<V> storedElement : storedElements) {
          replacementNode = put(storedElement, replacementNode, sDomain,
              oldValue);
        }
        replacementNode = put(element, replacementNode, sDomain, oldValue);
        return replacementNode;
      }
    } else {
      InternalNode node = (InternalNode) sRoot;
      double lowerX = sDomain.getLowerX();
      double upperX = sDomain.getUpperX();
      double lowerY = sDomain.getLowerY();
      double upperY = sDomain.getUpperY();
      double midX = (lowerX + upperX) / 2;
      double midY = (lowerY + upperY) / 2;
      RectangularDomain newDomain;
      switch (element.getKey().quadrantRelativeTo(midX, midY)) {
        case NW:
          newDomain = new RectangularDomain(lowerX, midX, lowerY, midY);
          node.setNW(put(element, node.getNW(), newDomain, oldValue));
          break;
        case NE:
          newDomain = new RectangularDomain(midX + EPSILON, upperX, lowerY,
              midY);
          node.setNE(put(element, node.getNE(), newDomain, oldValue));
          break;
        case SW:
          newDomain = new RectangularDomain(lowerX, midX - EPSILON, midY,
              upperY);
          node.setSW(put(element, node.getSW(), newDomain, oldValue));
          break;
        case SE:
          newDomain = new RectangularDomain(midX, upperX, midY + EPSILON,
              upperY);
          node.setSE(put(element, node.getSE(), newDomain, oldValue));
          break;
        default:
          String errorMessage = "Key " + element.getKey()
              + " returns an invalid quadrant relative to (" + midX + ", "
              + midY + ").";
          System.err.println(errorMessage);
          throw new IllegalArgumentException(errorMessage);
      }
      return node;
    }
  }

  /**
   * Returns {@code true} if this map contains a mapping for the specified key.
   * 
   * @param x
   *          The x-coordinate of the key whose presence in this map is to be
   *          tested.
   * @param y
   *          The y-coordinate of the key whose presence in this map is to be
   *          tested.
   * @return {@code true} if this map contains a mapping for the specified key.
   */
  public boolean containsKey(double x, double y) {
    Key key = new Key(x, y);
    return getElement(key) != null;
  }

  /**
   * Returns the value to which the specified key is mapped, or {@code null} if
   * this map contains no mapping for the key.
   * 
   * A return value of {@code null} does not necessarily indicate that the map
   * contains no mapping for the key; it's also possible that the map explicitly
   * maps the key to {@code null}. The {@link containsKey} operation may be used
   * to distinguish these two cases.
   * 
   * @param x
   *          The x-coordinate of the key whose associated value is to be
   *          returned.
   * @param y
   *          The y-coordinate of the key whose associated value is to be
   *          returned.
   * @return The value to which the specified key is mapped, or {@code null} if
   *         this map contains no mapping for the key.
   */
  public V get(double x, double y) {
    Key key = new Key(x, y);
    TreeElement<V> element = getElement(key);
    return (element == null) ? null : element.getValue();
  }

  /**
   * Returns the {@link TreeElement} containing the specified key, or
   * {@code null} if this map contains no {@link TreeElement} with the key.
   * 
   * @param key
   *          The key whose associated {@link TreeElement} is to be returned.
   * @return The {@link TreeElement} containing the specified key, or
   *         {@code null} if this map contains no mapping for the key.
   */
  private final TreeElement<V> getElement(Key key) {
    assert key != null : "key is null.";

    return getElement(key, root, treeDomain);
  }

  /**
   * Helper recursive method for returning the {@link TreeElement} containing
   * the specified key, or {@code null} if this map contains no
   * {@link TreeElement} with the key.
   * 
   * @param key
   *          The key whose associated {@link TreeElement} is to be returned.
   * @param sRoot
   *          The root of the subtree for which to get the mapped element.
   * @param sDomain
   *          The domain represented by the specified subtree.
   * @return The {@link TreeElement} containing the specified key, or
   *         {@code null} if this map contains no mapping for the key.
   */
  private final TreeElement<V> getElement(Key key, TreeNode sRoot,
      RectangularDomain sDomain) {
    assert key != null : "key is null.";
    assert sRoot != null : "sRoot is null.";
    assert sDomain != null : "sDomain is null.";

    if (sRoot instanceof FlyweightNode) {
      return null;
    } else if (sRoot instanceof QuadTreeMap.LeafNode) {
      @SuppressWarnings("unchecked")
      LeafNode<V> node = (LeafNode<V>) sRoot;
      return node.get(key);
    } else {
      InternalNode node = (InternalNode) sRoot;
      double lowerX = sDomain.getLowerX();
      double upperX = sDomain.getUpperX();
      double lowerY = sDomain.getLowerY();
      double upperY = sDomain.getUpperY();
      double midX = (lowerX + upperX) / 2;
      double midY = (lowerY + upperY) / 2;
      RectangularDomain newDomain;
      switch (key.quadrantRelativeTo(midX, midY)) {
        case NW:
          newDomain = new RectangularDomain(lowerX, midX, lowerY, midY);
          return getElement(key, node.getNW(), newDomain);
        case NE:
          newDomain = new RectangularDomain(midX + EPSILON, upperX, lowerY,
              midY);
          return getElement(key, node.getNE(), newDomain);
        case SW:
          newDomain = new RectangularDomain(lowerX, midX - EPSILON, midY,
              upperY);
          return getElement(key, node.getSW(), newDomain);
        case SE:
          newDomain = new RectangularDomain(midX, upperX, midY + EPSILON,
              upperY);
          return getElement(key, node.getSE(), newDomain);
        default:
          String errorMessage = "Key " + key
              + " returns an invalid quadrant relative to (" + midX + ", "
              + midY + ").";
          System.err.println(errorMessage);
          throw new IllegalArgumentException(errorMessage);
      }
    }
  }

  /**
   * Returns a collection of all values whose associated key is within the
   * specified circular domain.
   * 
   * @param x
   *          The x-coordinate of the search domain origin.
   * @param y
   *          The y-coordinate of the search domain origin.
   * @param radius
   *          The radius of the search domain.
   * @return A collection of all values whose associated key is within the
   *         specified circular domain.
   */
  public Collection<V> get(double x, double y, double radius) {
    if (radius < 0) {
      throw new IllegalArgumentException(
          "radius must be a non-negative number.");
    }

    return get(new CircularDomain(x, y, radius));
  }

  /**
   * Returns a collection of all values whose associated key is within the
   * specified rectangular domain.
   * 
   * @param lowerX
   *          The lower x-bound of the search domain's region.
   * @param upperX
   *          The upper x-bound of the search domain's region.
   * @param lowerY
   *          The lower y-bound of the search domain's region.
   * @param upperY
   *          The upper y-bound of the search domain's region.
   * @return A collection of all values whose associated key is within the
   *         specified rectangular domain.
   */
  public Collection<V> get(double lowerX, double upperX, double lowerY,
      double upperY) {
    if (lowerX > upperX) {
      throw new IllegalArgumentException(
          "lowerX must be less than or equal to upperX.");
    }
    if (lowerY > upperY) {
      throw new IllegalArgumentException(
          "lowerY must be less than or equal to upperY.");
    }

    return get(new RectangularDomain(lowerX, upperX, lowerY, upperY));
  }

  /**
   * Returns a collection of all values whose associated key is within the
   * specified {@link Domain}.
   * 
   * @param searchDomain
   *          The search {@link Domain}.
   * @return A collection of all values whose associated key is within the
   *         specified {@link Domain}.
   */
  private final Collection<V> get(Domain searchDomain) {
    assert searchDomain != null;

    if (!searchDomain.intersects(treeDomain)) {
      return new LinkedList<V>();
    }

    Collection<TreeElement<V>> elements = new LinkedList<TreeElement<V>>();
    getElements(elements, searchDomain, root, treeDomain);
    Collection<V> values = new LinkedList<V>();
    for (TreeElement<V> element : elements) {
      values.add(element.getValue());
    }
    return values;
  }

  /**
   * Adds all {@link TreeElement}s containing keys within the specified
   * {@link Domain} to the specified {@link Collection}.
   * 
   * @param elements
   *          The {@link Collection} to add the {@link TreeElement}s to.
   * @param searchDomain
   *          The search {@link Domain}.
   * @param sRoot
   *          The root of the subtree for which to get the mapped elements.
   * @param sDomain
   *          The {@link Domain} represented by the specified subtree.
   */
  private final void getElements(Collection<TreeElement<V>> elements,
      Domain searchDomain, TreeNode sRoot, RectangularDomain sDomain) {
    assert elements != null : "elements is null.";
    assert searchDomain != null : "searchDomain is null";
    assert sRoot != null : "sRoot is null.";
    assert sDomain != null : "sDomain is null.";

    if (sRoot instanceof FlyweightNode) {
      // no elements to add
    } else if (sRoot instanceof QuadTreeMap.LeafNode) {
      @SuppressWarnings("unchecked")
      LeafNode<V> node = (LeafNode<V>) sRoot;
      Collection<TreeElement<V>> nodeElements = node.getElements();
      for (TreeElement<V> nodeElement : nodeElements) {
        Key key = nodeElement.getKey();
        if (searchDomain.containsAsMember(key.getX(), key.getY())) {
          elements.add(nodeElement);
        }
      }
    } else {
      InternalNode node = (InternalNode) sRoot;
      double lowerX = sDomain.getLowerX();
      double upperX = sDomain.getUpperX();
      double lowerY = sDomain.getLowerY();
      double upperY = sDomain.getUpperY();
      double midX = (lowerX + upperX) / 2;
      double midY = (lowerY + upperY) / 2;
      RectangularDomain newDomain;
      if (searchDomain.intersects(newDomain = new RectangularDomain(lowerX,
          midX, lowerY, midY))) {
        getElements(elements, searchDomain, node.getNW(), newDomain);
      }
      if (searchDomain.intersects(newDomain = new RectangularDomain(midX + 1,
          upperX, lowerY, midY))) {
        getElements(elements, searchDomain, node.getNE(), newDomain);
      }
      if (searchDomain.intersects(newDomain = new RectangularDomain(lowerX,
          midX, midY + 1, upperY))) {
        getElements(elements, searchDomain, node.getSW(), newDomain);
      }
      if (searchDomain.intersects(newDomain = new RectangularDomain(midX + 1,
          upperX, midY + 1, upperY))) {
        getElements(elements, searchDomain, node.getSE(), newDomain);
      }
    }
  }

  /**
   * Removes the mapping for the specified key from this map, if present.
   * 
   * @param x
   *          The x-coordinate of the key for which the mapping should be
   *          removed.
   * @param y
   *          The y-coordinate of the key for which the mapping should be
   *          removed.
   * @return The previous value associated with the specified key, or
   *         {@code null} if there was no mapping. (A {@code null} return can
   *         also indicate that the map previously associated {@code null} with
   *         the specified key.)
   */
  public V remove(double x, double y) {
    Key key = new Key(x, y);
    if (!isElementOfTreeDomain(key)) {
      return null;
    }

    Value oldValue = new Value();
    root = remove(key, root, treeDomain, oldValue);
    if (oldValue.isSet()) {
      size--;
    }
    return oldValue.getValue();
  }

  /**
   * Helper recursive method for removing the mapping for the specified key, if
   * present.
   * 
   * @param key
   *          Key for which mapping should be removed.
   * @param sRoot
   *          The root of the subtree for which to remove the mapped element.
   * @param sDomain
   *          The {@link Domain} represented by the specified subtree.
   * @param oldValue
   *          The wrapper for storing the previous value associated with the key
   *          of the specified element.
   * @return The new root of the specified subtree.
   */
  private final TreeNode remove(Key key, TreeNode sRoot,
      RectangularDomain sDomain, Value oldValue) {
    assert key != null : "key is null.";
    assert sRoot != null : "sRoot is null.";
    assert sDomain != null : "sDomain is null.";
    assert oldValue != null : "oldValue is null.";

    if (sRoot instanceof FlyweightNode) {
      // No mapping for the specified key found
      return sRoot;
    } else if (sRoot instanceof QuadTreeMap.LeafNode) {
      @SuppressWarnings("unchecked")
      LeafNode<V> node = (LeafNode<V>) sRoot;
      TreeElement<V> removedElement = node.remove(key);
      if (removedElement != null) {
        oldValue.setValue(removedElement.getValue());
        if (!node.isEmpty()) {
          return node;
        } else {
          return FlyweightNode.getInstance();
        }
      } else {
        // No mapping for the specified key found
        return node;
      }
    } else {
      InternalNode node = (InternalNode) sRoot;
      double lowerX = sDomain.getLowerX();
      double upperX = sDomain.getUpperX();
      double lowerY = sDomain.getLowerY();
      double upperY = sDomain.getUpperY();
      double midX = (lowerX + upperX) / 2;
      double midY = (lowerY + upperY) / 2;
      RectangularDomain newDomain;
      switch (key.quadrantRelativeTo(midX, midY)) {
        case NW:
          newDomain = new RectangularDomain(lowerX, midX, lowerY, midY);
          node.setNW(remove(key, node.getNW(), newDomain, oldValue));
          break;
        case NE:
          newDomain = new RectangularDomain(midX + EPSILON, upperX, lowerY,
              midY);
          node.setNE(remove(key, node.getNE(), newDomain, oldValue));
          break;
        case SW:
          newDomain = new RectangularDomain(lowerX, midX - EPSILON, midY,
              upperY);
          node.setSW(remove(key, node.getSW(), newDomain, oldValue));
          break;
        case SE:
          newDomain = new RectangularDomain(midX, upperX, midY + EPSILON,
              upperY);
          node.setSE(remove(key, node.getSE(), newDomain, oldValue));
          break;
        default:
          String errorMessage = "Key " + key
              + " returns an invalid quadrant relative to (" + midX + ", "
              + midY + ").";
          System.err.println(errorMessage);
          throw new IllegalArgumentException(errorMessage);
      }
      if (!hasUnderflow(node)) {
        return node;
      } else {
        return retractSubtree(node);
      }
    }
  }

  /**
   * Returns {@code true} if underflow has occurred for the specified
   * {@link InternalNode}.
   * 
   * @param node
   *          The internal node whose underflow condition is to be tested.
   * @return {@code true} if underflow has occurred for the specified
   *         {@link InternalNode}.
   */
  @SuppressWarnings("unchecked")
  private final boolean hasUnderflow(InternalNode node) {
    assert node != null : "node is null.";

    if (node.getNW() instanceof QuadTreeMap.InternalNode
        || node.getNE() instanceof QuadTreeMap.InternalNode
        || node.getSW() instanceof QuadTreeMap.InternalNode
        || node.getSE() instanceof QuadTreeMap.InternalNode) {
      return false;
    } else {
      int size = 0;
      TreeNode childNode;
      LeafNode<V> leafNode;
      if ((childNode = node.getNW()) instanceof QuadTreeMap.LeafNode) {
        leafNode = (LeafNode<V>) childNode;
        size += leafNode.size();
      }
      if (size <= bucketSize
          && (childNode = node.getNE()) instanceof QuadTreeMap.LeafNode) {
        leafNode = (LeafNode<V>) childNode;
        size += leafNode.size();
      }
      if (size <= bucketSize
          && (childNode = node.getSW()) instanceof QuadTreeMap.LeafNode) {
        leafNode = (LeafNode<V>) childNode;
        size += leafNode.size();
      }
      if (size <= bucketSize
          && (childNode = node.getSE()) instanceof QuadTreeMap.LeafNode) {
        leafNode = (LeafNode<V>) childNode;
        size += leafNode.size();
      }
      return size <= bucketSize;
    }
  }

  /**
   * Retracts the specified subtree and merges the {@link TreeElement}s in the
   * specified {@link InternalNode}'s child nodes into a {@link LeafNode}.
   * 
   * @param sRoot
   *          The {@link InternalNode} whose child nodes' {@link TreeElement}s
   *          are to be merged.
   * @return A {@link LeafNode} storing all {@link TreeElement}s stored by the
   *         specified {@link InternalNode}'s child nodes.
   */
  @SuppressWarnings("unchecked")
  private final LeafNode<V> retractSubtree(InternalNode sRoot) {
    assert sRoot != null : "sRoot is null.";

    LeafNode<V> replacementNode = new LeafNode<V>();
    TreeNode childNode;
    Collection<TreeElement<V>> leafElements;
    if ((childNode = sRoot.getNW()) instanceof QuadTreeMap.LeafNode) {
      leafElements = ((LeafNode<V>) childNode).getElements();
      for (TreeElement<V> element : leafElements) {
        replacementNode.add(element);
      }
    }
    if ((childNode = sRoot.getNE()) instanceof QuadTreeMap.LeafNode) {
      leafElements = ((LeafNode<V>) childNode).getElements();
      for (TreeElement<V> element : leafElements) {
        replacementNode.add(element);
      }
    }
    if ((childNode = sRoot.getSW()) instanceof QuadTreeMap.LeafNode) {
      leafElements = ((LeafNode<V>) childNode).getElements();
      for (TreeElement<V> element : leafElements) {
        replacementNode.add(element);
      }
    }
    if ((childNode = sRoot.getSE()) instanceof QuadTreeMap.LeafNode) {
      leafElements = ((LeafNode<V>) childNode).getElements();
      for (TreeElement<V> element : leafElements) {
        replacementNode.add(element);
      }
    }
    return replacementNode;
  }

  /**
   * Returns {@code true} if this key is an element of this tree's
   * {@link Domain}.
   * 
   * @param key
   *          The key whose membership in the {@link Domain} is to be tested.
   * @return {@code true} if this key is an element of this tree's
   *         {@link Domain}.
   */
  private final boolean isElementOfTreeDomain(Key key) {
    assert key != null : "key is null.";

    return treeDomain.containsAsMember(key.getX(), key.getY());
  }

  /**
   * Removes all of the data elements from this {@code QuadTreeMap}. The
   * {@code QuadTreeMap} will be empty after this method returns.
   */
  public void clear() {
    root = FlyweightNode.getInstance();
    size = 0;
  }

  /**
   * Returns {@code true} if this {@code QuadTreeMap} contains no data elements.
   * In other words, returns {@code true} if the size of this
   * {@code QuadTreeMap} is zero.
   * 
   * @return {@code true} if this {@code QuadTreeMap} contains no elements.
   */
  public boolean isEmpty() {
    return size == 0;
  }

  /**
   * Returns the number of data elements in this {@code QuadTreeMap}.
   * 
   * @return The number of elements in this {@code QuadTreeMap}.
   */
  public int size() {
    return size;
  }

  /**
   * Returns the maximum capacity of a leaf node before the node is split.
   * 
   * @return The maximum capacity of a leaf node before the node is split.
   */
  public int bucketSize() {
    return bucketSize;
  }

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("[");
    buildString(root, stringBuilder);
    stringBuilder.append("]");
    return stringBuilder.toString();
  }

  /**
   * Builds a complete listing of the contents of the specified subtree nodes
   * when traversed in a pre-order fashion (i.e., north-west, north-east,
   * south-west, south-east) in the specified {@link StringBuilder}.
   * 
   * @param sRoot
   *          The node that roots the subtree.
   * @param stringBuilder
   *          The {@link StringBuilder} building the string representation.
   */
  private final void buildString(TreeNode sRoot, StringBuilder stringBuilder) {
    assert sRoot != null : "sRoot is null.";
    assert stringBuilder != null : "stringBuilder is null.";

    if (sRoot instanceof FlyweightNode) {
      // No elements to traverse
    } else if (sRoot instanceof QuadTreeMap.LeafNode) {
      if (stringBuilder.length() > 1) {
        stringBuilder.append(", ");
      }
      stringBuilder.append(sRoot);
    } else {
      InternalNode node = (InternalNode) sRoot;
      buildString(node.getNW(), stringBuilder);
      buildString(node.getNE(), stringBuilder);
      buildString(node.getSW(), stringBuilder);
      buildString(node.getSE(), stringBuilder);
    }
  }

  /**
   * A domain representing a bounded set of xy-coordinates.
   */
  private static interface Domain {

    /**
     * Returns {@code true} if the specified xy-coordinates are an element of
     * this domain.
     * 
     * @param x
     *          The x-coordinate whose membership in this domain is to be
     *          tested.
     * @param y
     *          The y-coordinate whose membership in this domain is to be
     *          tested.
     * @return {@code true} if the specified xy-coordinates are an element of
     *         this domain.
     */
    boolean containsAsMember(double x, double y);

    /**
     * Returns {@code true} if this domain and the specified
     * {@link RectangularDomain} intersect. More formally, returns {@code true}
     * if the set intersection of this domain and the specified
     * {@link RectangularDomain}s are not empty.
     * 
     * @param other
     *          A {@link RectangularDomain} to check against for intersection.
     * @return {@code true} if this domain and the specified
     *         {@link RectangularDomain} intersect.
     */
    boolean intersects(RectangularDomain other);
  }

  /**
   * A rectangular domain representing a bounded set of xy-coordinates.
   */
  private static final class RectangularDomain implements Domain {

    // This domain's lower x-bound
    private double lowerX;

    // This domain's upper x-bound
    private double upperX;

    // This domain's lower y-bound
    private double lowerY;

    // This domain's upper y-bound
    private double upperY;

    /**
     * Constructs a new {@code RectangularDomain} with the specified lower and
     * upper bounds.
     * 
     * @param lowerX
     *          The lower x-bound.
     * @param upperX
     *          The upper x-bound.
     * @param lowerY
     *          The lower y-bound.
     * @param upperY
     *          The upper y-bound.
     */
    public RectangularDomain(double lowerX, double upperX, double lowerY,
        double upperY) {
      assert lowerX <= upperX : "lowerX must be less than or equal to upperX.";
      assert lowerY <= upperY : "lowerY must be less than or equal to upperY.";

      this.lowerX = lowerX;
      this.upperX = upperX;
      this.lowerY = lowerY;
      this.upperY = upperY;
    }

    /**
     * Returns the lower x-bound of this domain.
     * 
     * @return The lower x-bound of this domain.
     */
    public double getLowerX() {
      return lowerX;
    }

    /**
     * Returns the upper x-bound of this domain.
     * 
     * @return The upper x-bound of this domain.
     */
    public double getUpperX() {
      return upperX;
    }

    /**
     * Returns the lower y-bound of this domain.
     * 
     * @return The lower y-bound of this domain.
     */
    public double getLowerY() {
      return lowerY;
    }

    /**
     * Returns the upper y-bound of this domain.
     * 
     * @return The upper y-bound of this domain.
     */
    public double getUpperY() {
      return upperY;
    }

    public boolean containsAsMember(double x, double y) {
      return x >= lowerX && x <= upperX && y >= lowerY && y <= upperY;
    }

    public boolean intersects(RectangularDomain other) {
      assert other != null : "other is null.";

      return lowerX < other.getUpperX() && upperX > other.getLowerX()
          && lowerY < other.getUpperY() && upperY > other.getLowerY();
    }
  }

  /**
   * A circular domain representing a bounded set of xy-coordinates.
   */
  private static final class CircularDomain implements Domain {

    // This domain's origin's x-coordinate
    private double x;

    // This domain's origin's y-coordinate
    private double y;

    // This domain's radius
    private double radius;

    /**
     * Constructs a new {@code CircularDomain} with the specified origin and
     * radius.
     * 
     * @param x
     *          The origin's x-coordinate.
     * @param y
     *          The origin's y-coordinate.
     * @param radius
     *          The radius.
     */
    public CircularDomain(double x, double y, double radius) {
      assert radius >= 0 : "radius must be a non-negative number.";

      this.x = x;
      this.y = y;
      this.radius = radius;
    }

    /**
     * Returns the x-coordinate of the origin of this domain.
     * 
     * @return The x-coordinate of the origin of this domain.
     */
    public double getX() {
      return x;
    }

    /**
     * Returns the y-coordinate of the origin of this domain.
     * 
     * @return The y-coordinate of the origin of this domain.
     */
    public double getY() {
      return y;
    }

    /**
     * Returns the radius of this domain.
     * 
     * @return The radius of this domain.
     */
    public double getRadius() {
      return radius;
    }

    public boolean containsAsMember(double x, double y) {
      double distanceX = x - this.x;
      double distanceY = y - this.y;
      double distanceSquared = distanceX * distanceX + distanceY * distanceY;
      return distanceSquared <= radius * radius;
    }

    public boolean intersects(RectangularDomain other) {
      assert other != null : "other is null.";

      double rectangleLowerX = other.getLowerX();
      double rectangleUpperX = other.getUpperX();
      double rectangleLowerY = other.getLowerY();
      double rectangleUpperY = other.getUpperY();

      double closestX = x < rectangleLowerX ? rectangleLowerX
          : x > rectangleUpperX ? rectangleUpperX : x;
      double closestY = y < rectangleLowerY ? rectangleLowerY
          : y > rectangleUpperY ? rectangleUpperY : y;
      return containsAsMember(closestX, closestY);
    }
  }

  /**
   * Represents a quadrant relative to a specified location.
   */
  public enum Quadrant {

    NE("north-east"), NW("north-west"), SW("sount-west"), SE("south-east");

    // The name of the quadrant
    private final String name;

    /**
     * Constructs a new {@code Quadrant} with the specified name.
     * 
     * @param name
     *          The name of the quadrant.
     */
    private Quadrant(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  /**
   * A generic quadtree node from which all other nodes are derived.
   */
  private interface TreeNode {}

  /**
   * A quadtree flyweight node, referenced for all empty leaf nodes.
   */
  private static final class FlyweightNode implements TreeNode {

    // Singleton instance for the lifetime of the program
    private static final FlyweightNode INSTANCE = new FlyweightNode();

    private FlyweightNode() {
      // disable instantiation from external classes
    }

    /**
     * Returns a singleton instance of {@code FlyweightNode}.
     * 
     * @return A singleton instance of {@code FlyweightNode}.
     */
    public static FlyweightNode getInstance() {
      return INSTANCE;
    }

    @Override
    public String toString() {
      return "E";
    }
  }

  /**
   * A quadtree internal node, which stores references to all of its child
   * nodes.
   */
  private static final class InternalNode implements TreeNode {

    // This node's child nodes
    private TreeNode NW, NE, SW, SE;

    /**
     * Constructs a new instance of {@code InternalNode} with the specified
     * child node references.
     * 
     * @param NW
     *          The north-west child node.
     * @param NE
     *          The north-east child node.
     * @param SW
     *          The south-west child node.
     * @param SE
     *          The south-east child node.
     */
    public InternalNode(TreeNode NW, TreeNode NE, TreeNode SW, TreeNode SE) {
      setNE(NW);
      setNW(NE);
      setSW(SW);
      setSE(SE);
    }

    /**
     * Constructs a new instance of {@code InternalNode} with
     * {@link FlyweightNode} child node references.
     */
    public InternalNode() {
      this(FlyweightNode.getInstance(), FlyweightNode.getInstance(),
          FlyweightNode.getInstance(), FlyweightNode.getInstance());
    }

    /**
     * Returns this node's north-west child node.
     * 
     * @return This node's north-west child node.
     */
    public TreeNode getNW() {
      return NW;
    }

    /**
     * Sets this node's north-west child node.
     * 
     * @param NE
     *          The north-west child node.
     */
    public void setNW(TreeNode NW) {
      assert NW != null : "NW is null.";

      this.NW = NW;
    }

    /**
     * Returns this node's north-east child node.
     * 
     * @return This node's north-east child node.
     */
    public TreeNode getNE() {
      return NE;
    }

    /**
     * Sets this node's north-east child node.
     * 
     * @param NE
     *          The north-east child node.
     */
    public void setNE(TreeNode NE) {
      assert NE != null : "NE is null.";

      this.NE = NE;
    }

    /**
     * Returns this node's south-west child node.
     * 
     * @return This node's south-west child node.
     */
    public TreeNode getSW() {
      return SW;
    }

    /**
     * Sets this node's south-west child node.
     * 
     * @param NE
     *          The south-west child node.
     */
    public void setSW(TreeNode SW) {
      assert SW != null : "SW is null.";

      this.SW = SW;
    }

    /**
     * Returns this node's south-east child node.
     * 
     * @return This node's south-east child node.
     */
    public TreeNode getSE() {
      return SE;
    }

    /**
     * Sets this node's south-east child node.
     * 
     * @param SE
     *          The south-east child node.
     */
    public void setSE(TreeNode SE) {
      assert SE != null : "SE is null.";

      this.SE = SE;
    }

    @Override
    public String toString() {
      return "I";
    }
  }

  /**
   * A quadtree leaf node, which stores a container of {@link TreeElement}s.
   */
  private static final class LeafNode<V> implements TreeNode {

    // Container storing key-value mappings
    private Collection<TreeElement<V>> elements;

    /**
     * Constructs a new instance of {@code LeafNode} with the specified initial
     * {@link TreeElement}.
     * 
     * @param element
     *          The element to be stored in the node.
     */
    public LeafNode(TreeElement<V> element) {
      this();
      add(element);
    }

    /**
     * Constructs a new, empty instance of {@code LeafNode}.
     * 
     * Note that references to empty leaf nodes should be replaced with
     * references to {@link FlyweightNode}.
     */
    public LeafNode() {
      elements = new LinkedList<TreeElement<V>>();
    }

    /**
     * Stores the specified {@link TreeElement} in this node. If the node
     * previously contained an element with an identical key, the old element is
     * replaced.
     * 
     * @param element
     *          The element to be stored in this node.
     * @return The previous {@link TreeElement} associated with the key of the
     *         specified {@link TreeElement}, or {@code null} if there was no
     *         mapping.
     */
    public TreeElement<V> add(TreeElement<V> element) {
      TreeElement<V> oldElement = remove(element.getKey());
      elements.add(element);
      return oldElement;
    }

    /**
     * Returns the {@link TreeElement} stored in this node with the specified
     * key, or {@code null} if this node contains no {@link TreeElement} with
     * the key.
     * 
     * @param key
     *          The key whose associated {@link TreeElement} is to be returned.
     * @return A {@link TreeElement} containing the specified key, or
     *         {@code null} if this node contains no {@link TreeElement} with
     *         the key.
     */
    public TreeElement<V> get(Key key) {
      assert key != null : "key is null.";

      double x = key.getX();
      double y = key.getY();
      for (TreeElement<V> storedElement : elements) {
        Key storedKey = storedElement.getKey();
        if (Math.abs(x - storedKey.getX()) < EPSILON
            && Math.abs(y - storedKey.getY()) < EPSILON) {
          return storedElement;
        }
      }
      return null;
    }

    /**
     * Removes and returns the {@link TreeElement} stored in this node with the
     * specified key, if present.
     * 
     * @param key
     *          The key whose associated {@link TreeElement} is to be removed.
     * @return The previous {@link TreeElement} associated with the specified
     *         key, or {@code null} if there was no mapping.
     */
    public TreeElement<V> remove(Key key) {
      assert key != null : "key is null.";

      TreeElement<V> element = get(key);
      if (element != null) {
        elements.remove(element);
      }
      return element;
    }

    /**
     * Returns a {@link Collection} of all {@link TreeElement}s stored by this
     * node.
     * 
     * @return A {@link Collection} of all {@link TreeElement}s stored by this
     *         node.
     */
    public Collection<TreeElement<V>> getElements() {
      LinkedList<TreeElement<V>> copy = new LinkedList<TreeElement<V>>();
      copy.addAll(elements);
      return copy;
    }

    /**
     * Returns the number of {@link TreeElement}s stored by this node.
     * 
     * @return The number of {@link TreeElement}s stored by this node.
     */
    public int size() {
      return elements.size();
    }

    /**
     * Returns {@code true} if this node contains no {@link TreeElement}s.
     * 
     * @return {@code true} if this node contains no {@link TreeElement}s.
     */
    public boolean isEmpty() {
      return elements.isEmpty();
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      for (TreeElement<V> element : elements) {
        if (sb.length() > 0) {
          sb.append(", ");
        }
        sb.append(element);
      }
      return sb.toString();
    }
  }

  /**
   * A key, which stores an x-coordinate and a y-coordinate.
   */
  private static final class Key {

    // The x-coordinate of the key
    private final double x;

    // The y-coordinate of the key
    private final double y;

    /**
     * Constructs a new {@code Key} with the specified x-coordinate and
     * y-coordinate.
     * 
     * @param x
     *          The x-coordinate of the key.
     * @param y
     *          The y-coordinate of the key.
     */
    public Key(double x, double y) {
      this.x = x;
      this.y = y;
    }

    /**
     * Returns the x-coordinate of this key.
     * 
     * @return The x-coordinate of this key.
     */
    public double getX() {
      return x;
    }

    /**
     * Returns the y-coordinate of this key.
     * 
     * @return The y-coordinate of this key.
     */
    public double getY() {
      return y;
    }

    /**
     * Returns a {@link Quadrant} describing whether this object is north-east
     * of, north-west of, south-west of, or south-east of the specified
     * coordinates. Objects located at either of the specified coordinates
     * (i.e., the object is strictly north, south, east, or west of the given
     * coordinates) are considered to be in the nearest direction which is
     * counterclockwise from the object. Objects located at both of the
     * specified coordinates (i.e., the object is at the given coordinates) are
     * considered to be in the north-west quadrant.
     * 
     * @param x
     *          The x-coordinate to compare to.
     * @param y
     *          The y-coordinate to compare to.
     * @return A {@link Quadrant} describing the relative location of this
     *         object compared to the specified coordinates.
     */
    Quadrant quadrantRelativeTo(double x, double y) {
      if (this.getX() > x && this.getY() <= y) {
        return Quadrant.NE;
      } else if (this.getX() >= x && this.getY() > y) {
        return Quadrant.SE;
      } else if (this.getX() < x && this.getY() >= y) {
        return Quadrant.SW;
      } else { // this.getX() <= x && this.getY() <= y
        return Quadrant.NW;
      }
    }

    @Override
    public String toString() {
      return "(" + x + ", " + y + ")";
    }
  }

  /**
   * A wrapper for a value.
   */
  private final class Value {

    // The value being wrapped
    private V value;

    // Flag indicating that a value has been set (used to differentiate between
    // a non-set value and a null value)
    private boolean set;

    /**
     * Returns the value being wrapped, or {@code null} if no value has been
     * set.
     * 
     * @return The value being wrapped, or {@code null} if no value has been
     *         set.
     */
    public V getValue() {
      return value;
    }

    /**
     * Sets the value being wrapped.
     * 
     * @param value
     *          The value to be wrapped.
     */
    public void setValue(V value) {
      if (set) {
        throw new IllegalStateException("value has already been set.");
      }
      this.value = value;
      set = true;
    }

    /**
     * Returns {@code true} if this wrapper's value has been set.
     * 
     * @return {@code true} if this wrapper's value has been set.
     */
    public boolean isSet() {
      return set;
    }
  }

  /**
   * A tree element, which stores a key and its corresponding value.
   */
  private static final class TreeElement<V> {

    // Key with which the value is to be associated
    private Key key;

    // Value to be associated with the key
    private V value;

    /**
     * Constructs a new instance of {@code TreeElement} with the specified key
     * and value pair.
     * 
     * @param key
     *          The key with which the value is to be associated.
     * @param value
     *          The value to be associated with the key.
     */
    public TreeElement(Key key, V value) {
      assert key != null : "key is null.";

      this.key = key;
      this.value = value;
    }

    /**
     * Returns the key with which the value is to be associated.
     * 
     * @return The key with which the value is to be associated.
     */
    public Key getKey() {
      return key;
    }

    /**
     * Returns the value to be associated with the key.
     * 
     * @return The value to be associated with the key.
     */
    public V getValue() {
      return value;
    }

    @Override
    public String toString() {
      return "[key=" + key + ", value=" + value + "]";
    }
  }
}
