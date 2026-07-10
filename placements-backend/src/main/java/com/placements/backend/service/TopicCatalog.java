package com.placements.backend.service;

import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class TopicCatalog {

    private static final Map<String, String> TOPIC_TO_LEETCODE_TAG = new LinkedHashMap<>();
    private static final Map<String, List<String>> TOPIC_TO_VIDEO_KEYWORDS = new LinkedHashMap<>();

    static {
        // Mappings of topic key -> LeetCode tag slug
        TOPIC_TO_LEETCODE_TAG.put("ARRAYS", "array");
        TOPIC_TO_LEETCODE_TAG.put("HASHING", "hash-table");
        TOPIC_TO_LEETCODE_TAG.put("SORTING", "sorting");
        TOPIC_TO_LEETCODE_TAG.put("BINARY_SEARCH", "binary-search");
        TOPIC_TO_LEETCODE_TAG.put("STRINGS", "string");
        TOPIC_TO_LEETCODE_TAG.put("TWO_POINTERS", "two-pointers");
        TOPIC_TO_LEETCODE_TAG.put("SLIDING_WINDOW", "sliding-window");
        TOPIC_TO_LEETCODE_TAG.put("LINKED_LIST", "linked-list");
        TOPIC_TO_LEETCODE_TAG.put("RECURSION", "recursion");
        TOPIC_TO_LEETCODE_TAG.put("BIT_MANIPULATION", "bit-manipulation");
        TOPIC_TO_LEETCODE_TAG.put("STACK_QUEUE", "stack");
        TOPIC_TO_LEETCODE_TAG.put("HEAP", "heap-priority-queue");
        TOPIC_TO_LEETCODE_TAG.put("GREEDY", "greedy");
        TOPIC_TO_LEETCODE_TAG.put("BACKTRACKING", "backtracking");
        TOPIC_TO_LEETCODE_TAG.put("TREES", "tree");
        TOPIC_TO_LEETCODE_TAG.put("BST", "binary-search-tree");
        TOPIC_TO_LEETCODE_TAG.put("GRAPHS", "graph");
        TOPIC_TO_LEETCODE_TAG.put("DP", "dynamic-programming");
        TOPIC_TO_LEETCODE_TAG.put("TRIE", "trie");

        // Keywords used to best-effort match YouTube video titles
        TOPIC_TO_VIDEO_KEYWORDS.put("ARRAYS", Arrays.asList("array", "arrays", "subarray"));
        TOPIC_TO_VIDEO_KEYWORDS.put("HASHING", Arrays.asList("hash", "hashing", "hashmap"));
        TOPIC_TO_VIDEO_KEYWORDS.put("SORTING", Arrays.asList("sorting", "sort", "bubble", "selection", "insertion", "merge", "quick"));
        TOPIC_TO_VIDEO_KEYWORDS.put("BINARY_SEARCH", Arrays.asList("binary search", "binarysearch"));
        TOPIC_TO_VIDEO_KEYWORDS.put("STRINGS", Arrays.asList("string", "strings", "substring"));
        TOPIC_TO_VIDEO_KEYWORDS.put("TWO_POINTERS", Arrays.asList("two pointer", "two pointers", "pointer"));
        TOPIC_TO_VIDEO_KEYWORDS.put("SLIDING_WINDOW", Arrays.asList("sliding window", "slidingwindow"));
        TOPIC_TO_VIDEO_KEYWORDS.put("LINKED_LIST", Arrays.asList("linked list", "linkedlist", "list"));
        TOPIC_TO_VIDEO_KEYWORDS.put("RECURSION", Arrays.asList("recursion", "recursive"));
        TOPIC_TO_VIDEO_KEYWORDS.put("BIT_MANIPULATION", Arrays.asList("bit", "bitwise", "manipulation"));
        TOPIC_TO_VIDEO_KEYWORDS.put("STACK_QUEUE", Arrays.asList("stack", "queue", "stacks", "queues"));
        TOPIC_TO_VIDEO_KEYWORDS.put("HEAP", Arrays.asList("heap", "heaps", "priority queue", "priorityqueue"));
        TOPIC_TO_VIDEO_KEYWORDS.put("GREEDY", Arrays.asList("greedy"));
        TOPIC_TO_VIDEO_KEYWORDS.put("BACKTRACKING", Arrays.asList("backtracking"));
        TOPIC_TO_VIDEO_KEYWORDS.put("TREES", Arrays.asList("tree", "trees", "binary tree"));
        TOPIC_TO_VIDEO_KEYWORDS.put("BST", Arrays.asList("bst", "binary search tree"));
        TOPIC_TO_VIDEO_KEYWORDS.put("GRAPHS", Arrays.asList("graph", "graphs", "dfs", "bfs", "dijkstra", "mst"));
        TOPIC_TO_VIDEO_KEYWORDS.put("DP", Arrays.asList("dp", "dynamic programming", "knapsack"));
        TOPIC_TO_VIDEO_KEYWORDS.put("TRIE", Arrays.asList("trie", "tries"));
    }

    public boolean isDsaTopic(String topicKey) {
        return TOPIC_TO_LEETCODE_TAG.containsKey(topicKey);
    }

    public String getLeetCodeTag(String topicKey) {
        return TOPIC_TO_LEETCODE_TAG.get(topicKey);
    }

    public Set<String> getDsaTopics() {
        return TOPIC_TO_LEETCODE_TAG.keySet();
    }

    public String matchTopicByTitle(String videoTitle) {
        if (videoTitle == null) return null;
        String lowerTitle = videoTitle.toLowerCase();
        
        // Find if any topic keyword is in the title
        // Rank by best match or first match. Let's do a simple check.
        for (Map.Entry<String, List<String>> entry : TOPIC_TO_VIDEO_KEYWORDS.entrySet()) {
            for (String kw : entry.getValue()) {
                if (lowerTitle.contains(kw)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }
}
