package com.placements.backend.service;

import com.placements.backend.entity.LeetCodeProblem;
import com.placements.backend.repository.LeetCodeProblemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class LeetCodeProblemImportService {

    private static final Logger log = LoggerFactory.getLogger(LeetCodeProblemImportService.class);

    private final LeetCodeProblemRepository problemRepository;
    private final RestTemplate restTemplate;
    private final TopicCatalog topicCatalog;

    @Value("${app.leetcode.graphql-url:https://leetcode.com/graphql}")
    private String graphqlUrl;

    public LeetCodeProblemImportService(
            LeetCodeProblemRepository problemRepository,
            RestTemplate restTemplate,
            TopicCatalog topicCatalog
    ) {
        this.problemRepository = problemRepository;
        this.restTemplate = restTemplate;
        this.topicCatalog = topicCatalog;
    }

    @Transactional
    public Map<String, Integer> importCatalog() {
        int importedCount = 0;
        int skippedCount = 0;

        try {
            boolean hasMore = true;
            int limit = 100;
            int skip = 0;
            int maxPages = 5; // Fetch up to 500 problems to avoid Cloudflare rate limit blocks
            int page = 0;

            while (hasMore && page < maxPages) {
                log.info("Fetching LeetCode problems from page {} (skip {})", page, skip);
                List<Map<String, Object>> questions = fetchProblemPage(limit, skip);
                if (questions == null || questions.isEmpty()) {
                    break;
                }

                for (Map<String, Object> q : questions) {
                    boolean saved = saveProblem(q);
                    if (saved) {
                        importedCount++;
                    } else {
                        skippedCount++;
                    }
                }

                skip += limit;
                page++;
                if (questions.size() < limit) {
                    hasMore = false;
                }
            }

            if (importedCount == 0 && skippedCount == 0) {
                // If remote API fetch was blocked or empty, run fallback seeder
                log.warn("Remote LeetCode import returned 0 problems. Running local fallback seeder.");
                return seedFallbackProblems();
            }

        } catch (Exception e) {
            log.error("Failed to import LeetCode problems from remote GraphQL API: {}. Running fallback seeder.", e.getMessage());
            return seedFallbackProblems();
        }

        Map<String, Integer> result = new HashMap<>();
        result.put("imported", importedCount);
        result.put("skippedExisting", skippedCount);
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchProblemPage(int limit, int skip) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

        String query = """
            query questionList($categorySlug: String, $limit: Int, $skip: Int, $filters: QuestionListFilterInput) {
              questionList(categorySlug: $categorySlug, limit: $limit, skip: $skip, filters: $filters) {
                totalNum
                data {
                  title
                  titleSlug
                  difficulty
                  isPaidOnly
                  topicTags {
                    name
                    slug
                  }
                }
              }
            }
            """;

        Map<String, Object> variables = new HashMap<>();
        variables.put("categorySlug", "all-code-definition");
        variables.put("limit", limit);
        variables.put("skip", skip);
        variables.put("filters", new HashMap<>());

        Map<String, Object> body = new HashMap<>();
        body.put("query", query);
        body.put("variables", variables);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(graphqlUrl, entity, Map.class);

        if (response.getBody() == null) return Collections.emptyList();

        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        if (data == null) return Collections.emptyList();

        Map<String, Object> questionList = (Map<String, Object>) data.get("questionList");
        if (questionList == null) return Collections.emptyList();

        return (List<Map<String, Object>>) questionList.get("data");
    }

    @SuppressWarnings("unchecked")
    private boolean saveProblem(Map<String, Object> q) {
        String slug = (String) q.get("titleSlug");
        if (slug == null || problemRepository.existsBySlug(slug)) {
            return false;
        }

        String title = (String) q.get("title");
        String difficulty = (String) q.get("difficulty");
        boolean isPaidOnly = (Boolean) q.get("isPaidOnly");

        List<Map<String, Object>> topicTags = (List<Map<String, Object>>) q.get("topicTags");
        if (topicTags == null || topicTags.isEmpty()) {
            return false;
        }

        // Map primary tag: find the first tag that is part of our DSA taxonomy
        String primaryTag = null;
        List<String> allTagsList = new ArrayList<>();
        for (Map<String, Object> tag : topicTags) {
            String tagSlug = (String) tag.get("slug");
            allTagsList.add(tagSlug);
            if (primaryTag == null) {
                // Check if this tag matches any of our 19 DSA topics
                for (String dsaKey : topicCatalog.getDsaTopics()) {
                    if (topicCatalog.getLeetCodeTag(dsaKey).equals(tagSlug)) {
                        primaryTag = tagSlug;
                        break;
                    }
                }
            }
        }

        if (primaryTag == null) {
            // If none matched, just take the first tag slug as primary
            primaryTag = (String) topicTags.get(0).get("slug");
        }

        LeetCodeProblem problem = LeetCodeProblem.builder()
                .slug(slug)
                .title(title)
                .difficulty(difficulty)
                .paidOnly(isPaidOnly)
                .primaryTag(primaryTag)
                .allTags(String.join(",", allTagsList))
                .build();

        problemRepository.save(problem);
        return true;
    }

    private Map<String, Integer> seedFallbackProblems() {
        int imported = 0;
        int skipped = 0;

        // Hardcode a diverse set of real LeetCode problems matching Striver DSA topics
        List<LeetCodeProblem> fallbackList = Arrays.asList(
            // Arrays (array)
            new LeetCodeProblem(null, "two-sum", "Two Sum", "Easy", false, "array", "array,hash-table"),
            new LeetCodeProblem(null, "best-time-to-buy-and-sell-stock", "Best Time to Buy and Sell Stock", "Easy", false, "array", "array,dynamic-programming"),
            new LeetCodeProblem(null, "majority-element", "Majority Element", "Easy", false, "array", "array,hash-table"),
            new LeetCodeProblem(null, "merge-intervals", "Merge Intervals", "Medium", false, "array", "array,sorting"),
            new LeetCodeProblem(null, "3sum", "3Sum", "Medium", false, "array", "array,two-pointers"),
            new LeetCodeProblem(null, "next-permutation", "Next Permutation", "Medium", false, "array", "array"),
            
            // Hashing (hash-table)
            new LeetCodeProblem(null, "contains-duplicate", "Contains Duplicate", "Easy", false, "hash-table", "hash-table,array"),
            new LeetCodeProblem(null, "valid-anagram", "Valid Anagram", "Easy", false, "hash-table", "hash-table,string"),
            new LeetCodeProblem(null, "group-anagrams", "Group Anagrams", "Medium", false, "hash-table", "hash-table,string"),
            new LeetCodeProblem(null, "longest-consecutive-sequence", "Longest Consecutive Sequence", "Medium", false, "hash-table", "hash-table,array"),

            // Sorting (sorting)
            new LeetCodeProblem(null, "sort-colors", "Sort Colors", "Medium", false, "sorting", "sorting,array,two-pointers"),
            new LeetCodeProblem(null, "merge-sorted-array", "Merge Sorted Array", "Easy", false, "sorting", "sorting,array,two-pointers"),

            // Binary Search (binary-search)
            new LeetCodeProblem(null, "binary-search", "Binary Search", "Easy", false, "binary-search", "binary-search,array"),
            new LeetCodeProblem(null, "search-in-rotated-sorted-array", "Search in Rotated Sorted Array", "Medium", false, "binary-search", "binary-search,array"),
            new LeetCodeProblem(null, "find-minimum-in-rotated-sorted-array", "Find Minimum in Rotated Sorted Array", "Medium", false, "binary-search", "binary-search,array"),
            new LeetCodeProblem(null, "median-of-two-sorted-arrays", "Median of Two Sorted Arrays", "Hard", false, "binary-search", "binary-search,array,divide-and-conquer"),

            // Strings (string)
            new LeetCodeProblem(null, "valid-palindrome", "Valid Palindrome", "Easy", false, "string", "string,two-pointers"),
            new LeetCodeProblem(null, "longest-substring-without-repeating-characters", "Longest Substring Without Repeating Characters", "Medium", false, "string", "string,sliding-window"),
            new LeetCodeProblem(null, "longest-palindromic-substring", "Longest Palindromic Substring", "Medium", false, "string", "string,dynamic-programming"),

            // Two Pointers (two-pointers)
            new LeetCodeProblem(null, "container-with-most-water", "Container With Most Water", "Medium", false, "two-pointers", "two-pointers,array"),
            new LeetCodeProblem(null, "remove-duplicates-from-sorted-array", "Remove Duplicates from Sorted Array", "Easy", false, "two-pointers", "two-pointers,array"),

            // Sliding Window (sliding-window)
            new LeetCodeProblem(null, "minimum-window-substring", "Minimum Window Substring", "Hard", false, "sliding-window", "sliding-window,string,hash-table"),

            // Linked List (linked-list)
            new LeetCodeProblem(null, "reverse-linked-list", "Reverse Linked List", "Easy", false, "linked-list", "linked-list"),
            new LeetCodeProblem(null, "linked-list-cycle", "Linked List Cycle", "Easy", false, "linked-list", "linked-list,two-pointers"),
            new LeetCodeProblem(null, "merge-two-sorted-lists", "Merge Two Sorted Lists", "Easy", false, "linked-list", "linked-list,recursion"),
            new LeetCodeProblem(null, "remove-nth-node-from-end-of-list", "Remove Nth Node From End of List", "Medium", false, "linked-list", "linked-list,two-pointers"),

            // Trees (tree)
            new LeetCodeProblem(null, "maximum-depth-of-binary-tree", "Maximum Depth of Binary Tree", "Easy", false, "tree", "tree,depth-first-search"),
            new LeetCodeProblem(null, "invert-binary-tree", "Invert Binary Tree", "Easy", false, "tree", "tree,binary-tree"),
            new LeetCodeProblem(null, "binary-tree-inorder-traversal", "Binary Tree Inorder Traversal", "Easy", false, "tree", "tree,binary-tree"),
            new LeetCodeProblem(null, "same-tree", "Same Tree", "Easy", false, "tree", "tree,depth-first-search"),
            new LeetCodeProblem(null, "binary-tree-level-order-traversal", "Binary Tree Level Order Traversal", "Medium", false, "tree", "tree,breadth-first-search"),

            // BST (binary-search-tree)
            new LeetCodeProblem(null, "validate-binary-search-tree", "Validate Binary Search Tree", "Medium", false, "binary-search-tree", "binary-search-tree,tree"),

            // Graphs (graph)
            new LeetCodeProblem(null, "number-of-islands", "Number of Islands", "Medium", false, "graph", "graph,depth-first-search,breadth-first-search"),
            new LeetCodeProblem(null, "clone-graph", "Clone Graph", "Medium", false, "graph", "graph,hash-table,depth-first-search"),

            // DP (dynamic-programming)
            new LeetCodeProblem(null, "climbing-stairs", "Climbing Stairs", "Easy", false, "dynamic-programming", "dynamic-programming"),
            new LeetCodeProblem(null, "coin-change", "Coin Change", "Medium", false, "dynamic-programming", "dynamic-programming,breadth-first-search"),
            new LeetCodeProblem(null, "longest-increasing-subsequence", "Longest Increasing Subsequence", "Medium", false, "dynamic-programming", "dynamic-programming,array")
        );

        for (LeetCodeProblem p : fallbackList) {
            if (!problemRepository.existsBySlug(p.getSlug())) {
                problemRepository.save(p);
                imported++;
            } else {
                skipped++;
            }
        }

        Map<String, Integer> result = new HashMap<>();
        result.put("imported", imported);
        result.put("skippedExisting", skipped);
        return result;
    }
}
