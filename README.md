

Notion link for Infinispan—https://lucky-magic-a56.notion.site/Infinspan-Distribured-Cache-24dc08a2a962802099a8ddcd5f194845

## Internal storing of cache objects in Heap memory.

Infinispan is a distributed in-memory key/value data store and cache. It can be used as a cache to store frequently accessed data in memory, which can help improve application performance by reducing the need to access slower storage systems like databases or file systems.

When using Infinispan as a cache, objects are typically stored in heap memory. Heap memory is the portion of memory allocated to a Java application for dynamic memory allocation. Infinispan uses this memory to store cached objects, allowing for fast access and retrieval.

We can define the maximum size of the cache and configure eviction policies to manage memory usage. When the cache reaches its maximum size, Infinispan can evict (remove) the least recently used or least frequently used objects to make room for new ones.

### Example of configuring Infinispan cache in heap memory:

```xml
<infinispan>
    <cache-container name="student-cache-container">
        <local-cache name="studentsCache">
            <memory max-count="1000" when-full="REMOVE"/>
            <expiration lifespan="600000"/>
        </local-cache>
    </cache-container>
</infinispan>

<!-- Leave 25% of heap for application logic -->
<!-- If -Xmx4g, use max 3GB for caches -->
<memory storage="HEAP" size="3GB" when-full="REMOVE"/>
<memory storage="OFF_HEAP" size="4GB" when-full="REMOVE"/>
<memory storage="BINARY" size="512MB" when-full="REMOVE"/>
```
In Infinispan, BINARY storage refers to how objects are stored in memory in their serialized (binary) form rather than as Java objects.


### BINARY Storage Explanation
When we configure storage="BINARY", Infinispan:
1. Serializes objects - Converts Java objects to byte arrays before storing them 
2. Stores serialized form - Keeps the binary representation in memory instead of the actual Java object 
3. Deserializes on access - Converts back to Java objects when retrieved

### Benefits of BINARY Storage
1. Memory efficiency - Serialized objects often consume less memory than their Java object equivalents 
2. Garbage collection relief - Fewer objects in the JVM heap means less GC pressure 
3. Consistent memory usage - Predictable memory footprint regardless of object complexity

### OFF_HEAP Storage Explanation
When we configure storage="OFF_HEAP", Infinispan:
1. Allocates memory outside the JVM heap - Uses native memory managed by Infinispan
2. Stores serialized objects - Similar to BINARY, objects are stored in their serialized form
3. Manages memory directly - Infinispan handles allocation and deallocation of off-heap memory    


## How Infinispan Cache is Stored in Heap

private EmbeddedCacheManager cacheManager; // Reference on stack

Cache uses data structures similar to ConcurrentHashMap or HashMap, which are heap-allocated

``` text
JVM Heap Memory:
┌─────────────────────────────────────────────────┐
│ CacheManager Object                             │
│ ┌─────────────────────────────────────────────┐ │
│ │ cacheRegistry: Map<String, Cache>           │ │
│ │ "studentsCache" -> [ref to StudentCache]    │ │ ← Cache name mapping
│ │ "subjectsCache" -> [ref to SubjectCache]    │ │ 
│ └─────────────────────────────────────────────┘ │
│                                                 │
│ StudentCache Instance                           │
│ ┌─────────────────────────────────────────────┐ │
│ │ Internal Student Buckets Array              │ │
│ │ ┌─────┬─────┬─────┬─────┬─────┬─────┬─────┐ │ │
│ │ │ [0] │ [1] │ [2] │ [3] │ [4] │ ... │[15] │ │ │ ← Student data only
│ │ └─────┴─────┴─────┴─────┴─────┴─────┴─────┘ │ │
│ │ Student Entries:                            │ │
│ │ • "student:123" -> Student(John)            │ │
│ │ • "student:456" -> Student(Jane)            │ │
│ └─────────────────────────────────────────────┘ │
│                                                 │
│ SubjectCache Instance                           │
│ ┌─────────────────────────────────────────────┐ │
│ │ Internal Subject Buckets Array              │ │
│ │ ┌─────┬─────┬─────┬─────┬─────┬─────┬─────┐ │ │
│ │ │ [0] │ [1] │ [2] │ [3] │ [4] │ ... │[15] │ │ │ ← Subject data only
│ │ └─────┴─────┴─────┴─────┴─────┴─────┴─────┘ │ │
│ │ Subject Entries:                            │ │
│ │ • "subject:math" -> Subject(Mathematics)    │ │
│ │ • "subject:physics" -> Subject(Physics)     │ │
│ └─────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────┘
```
### Cache Entry Mapping Works

cache.put("student:123", new Student("John", "john@example.com"));

```java
String key = "student:123";
int hash = key.hashCode(); // e.g., 1234567890
int bucketIndex = hash & (buckets.length - 1); // e.g., index 2
buckets[bucketIndex].add(new CacheEntry(key, studentObject));
```

### Node Creation
The cache creates a Node object in heap:
```java
class Node<K,V> {
    final int hash;        // Hash of the key
    final K key;           // Reference to key object
    V value;               // Reference to value object  
    Node<K,V> next;        // Reference to next node (collision handling)
}
``` 
### Storage in Bucket
The node is placed in the calculated bucket index in the internal array.

**Stack Contains:**
Method parameters: cache, key, value variables

Local variables: References to objects

Return addresses: Method call information

**Heap Contains:**
CacheManager object: The actual manager instance

Cache object: The actual cache instance with its internal array

Bucket array: Array of Node references

Node objects: Container objects holding hash, key ref, value ref

Key objects: Actual key data (e.g., String "student:123")

Value objects: Actual cached data (e.g., Student instances)


### Calculation of Heap Memory Size

```java
// Example calculation:
// Cache size: 1 million entries
// Average object size: 1KB
// Total cache memory: ~1GB
// Recommended heap: 2-3GB (leave room for application data and GC overhead)
//-Xms2g -Xmx3g
```


