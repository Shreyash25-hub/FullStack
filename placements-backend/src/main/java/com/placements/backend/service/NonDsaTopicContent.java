package com.placements.backend.service;

import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class NonDsaTopicContent {

    private final Map<String, List<String>> subjectTasks = new LinkedHashMap<>();

    public NonDsaTopicContent() {
        // Initialize tasks for each of the 8 CS subjects
        
        // 1. JAVA_OOP
        subjectTasks.put("JAVA_OOP", Arrays.asList(
            "Review OOP Basics: Inheritance, Polymorphism, Encapsulation, and Abstraction.",
            "Write a custom demonstration of runtime vs compile-time polymorphism in Java.",
            "Understand abstract classes vs interfaces. When to use which in design patterns?",
            "Study Access Modifiers and their scopes (private, default, protected, public).",
            "Explore Method Overriding vs Overloading and rules concerning exceptions.",
            "Understand the 'super' and 'this' keywords and constructor chaining.",
            "Study Java memory management: Stack vs Heap, garbage collection basics.",
            "Review 'final' keyword with variables, methods, and classes.",
            "Implement a Singleton pattern with double-checked locking in Java.",
            "Practice custom exceptions and try-catch-finally block rules.",
            "Understand String, StringBuilder, and StringBuffer differences and memory pools.",
            "Study Java generics and wildcard boundaries (extends/super)."
        ));

        // 2. SQL
        subjectTasks.put("SQL", Arrays.asList(
            "Review basic SELECT queries, WHERE filters, and logical operators.",
            "Practice GROUP BY, HAVING, and aggregation functions (SUM, AVG, COUNT, MIN, MAX).",
            "Master JOINS: INNER, LEFT, RIGHT, FULL, and SELF JOINs.",
            "Study subqueries (correlated vs non-correlated) and EXISTS vs IN operators.",
            "Write queries using SQL window functions: ROW_NUMBER(), RANK(), DENSE_RANK().",
            "Learn how to use Common Table Expressions (CTEs) for recursive queries.",
            "Practice DDL (CREATE, ALTER, DROP) and DML (INSERT, UPDATE, DELETE) statements.",
            "Review SQL Constraints: PRIMARY KEY, FOREIGN KEY, UNIQUE, CHECK, NOT NULL.",
            "Solve 3 medium SQL query preparation problems on LeetCode/HackerRank.",
            "Understand SQL Union, Union All, Intersect, and Except operations.",
            "Learn about SQL Indexes and how they speed up query execution.",
            "Practice transaction control commands: COMMIT, ROLLBACK, SAVEPOINT."
        ));

        // 3. DBMS
        subjectTasks.put("DBMS", Arrays.asList(
            "Understand Database architecture: 2-tier vs 3-tier models.",
            "Study ER Diagrams: Entities, Attributes, Relationships, and Cardinality.",
            "Deep dive into Normalization: 1NF, 2NF, 3NF, and BCNF with examples.",
            "Learn ACID properties of transactions and their importance.",
            "Explore transaction isolation levels: Read Uncommitted, Read Committed, Repeatable Read, Serializable.",
            "Study concurrency control: Lock-based protocols and two-phase locking (2PL).",
            "Understand indexing types: B-Tree, B+ Tree, Hash Indexing.",
            "Review Database recovery techniques: Log-based recovery, checkpoints.",
            "Compare SQL vs NoSQL databases: Use cases, advantages, limitations.",
            "Study DBMS storage: Row-oriented vs Column-oriented databases.",
            "Learn about Deadlocks in databases: Detection, prevention, and resolution.",
            "Study physical database design, partition schemas, and sharding."
        ));

        // 4. OS
        subjectTasks.put("OS", Arrays.asList(
            "Review OS basics: Kernel, System Calls, Monolithic vs Microkernel.",
            "Understand Process vs Thread: Memory layout, context switching.",
            "Study Process Scheduling algorithms: FCFS, SJF, Round Robin, Priority.",
            "Master Process Synchronization: Critical Section, Semaphores, Mutex.",
            "Understand Classical Synchronization problems: Producer-Consumer, Dining Philosophers.",
            "Study Deadlock conditions (Coffman) and prevention/avoidance (Banker's Algorithm).",
            "Review Memory Management: Paging, Segmentation, Virtual Memory.",
            "Practice Page Replacement algorithms: FIFO, LRU, Optimal.",
            "Learn about Thrashing, fragmentation (internal/external), and compaction.",
            "Study Disk Scheduling algorithms: FCFS, SSTF, SCAN, LOOK.",
            "Review Linux command line basics and process management signals.",
            "Explore Inter-Process Communication (IPC): Pipes, Shared Memory, Message Queues."
        ));

        // 5. CN
        subjectTasks.put("CN", Arrays.asList(
            "Study OSI Model vs TCP/IP Protocol Suite: Functions of each layer.",
            "Understand IP Addressing: IPv4 vs IPv6, Subnetting, Classless routing (CIDR).",
            "Review Transmission media and Data Link layer protocols (ARP, RARP).",
            "Deep dive into Transport Layer: TCP vs UDP header structures and differences.",
            "Understand TCP Connection lifecycle: Three-way handshake, connection termination.",
            "Study TCP Congestion Control and Flow Control (Sliding Window).",
            "Review Application Layer protocols: HTTP, HTTPS, DNS, FTP, SMTP, DHCP.",
            "Learn how DNS lookup works step-by-step.",
            "Explore Network security: Symmetric/Asymmetric encryption, SSL/TLS handshake.",
            "Study Routing algorithms: Link State (OSPF) vs Distance Vector (RIP).",
            "Understand NAT (Network Address Translation) and its role in routing.",
            "Review network commands: ping, traceroute, nslookup, netstat."
        ));

        // 6. PROJECT
        subjectTasks.put("PROJECT", Arrays.asList(
            "Draft a comprehensive architecture diagram for your main portfolio project.",
            "Review project dependencies and document their version requirements.",
            "Write a clean README file explaining how to run and deploy the project locally.",
            "Audit project security: Check for exposed API keys, weak auth, SQL injections.",
            "Optimize database queries: Add indexes to slow-performing read operations.",
            "Implement logging (Logback/SLF4J) across all service methods.",
            "Refactor code to implement SOLID design principles.",
            "Add unit tests using JUnit/Mockito to verify core business logic.",
            "Configure Docker containerization (Dockerfile + docker-compose) for the project.",
            "Conduct manual end-to-end testing of user registration and core flows.",
            "Record a short 3-minute video demonstration highlighting key features.",
            "Write API documentation (Swagger/OpenAPI spec) for client integrations."
        ));

        // 7. INTERVIEW_PREP
        subjectTasks.put("INTERVIEW_PREP", Arrays.asList(
            "Prepare your self-introduction (elevator pitch) for SDE job interviews.",
            "Practice answering 'Tell me about a challenging bug you fixed' using the STAR method.",
            "Prepare explanations for all core design decisions in your project.",
            "Review top 20 behavioral interview questions (leadership, conflict, growth).",
            "Mock-explain a complex algorithm (e.g. Dijkstra's) out loud as if to an interviewer.",
            "Prepare questions to ask the interviewer about culture and technical challenges.",
            "Practice solving an algorithmic puzzle on paper first before coding.",
            "Read up on the target company's engineering blog and recent tech stack developments.",
            "Practice mock whiteboard system design: Design a URL Shortener.",
            "Review Big-O complexity for all standard data structure operations.",
            "Practice explaining how HashMap works internally in Java.",
            "Refine your verbal communication: Speak clearly, structure your thoughts, ask clarifying questions."
        ));

        // 8. RESUME_PORTFOLIO
        subjectTasks.put("RESUME_PORTFOLIO", Arrays.asList(
            "Update resume template (make it 1 page, clean PDF, single-column format).",
            "Quantify project accomplishments (e.g., 'reduced latency by 30% using caching').",
            "Ensure GitHub profile is active, pin top projects, and write clear descriptions.",
            "Update LinkedIn profile with latest projects, skills, and certifications.",
            "Proofread resume for typos, spelling mistakes, or generic descriptions.",
            "Audit project repository: Clean up unused files and format codebase.",
            "Verify all project links on resume (GitHub, live demo) are functioning properly.",
            "Add a clean license and contributing guidelines to open-source portfolios.",
            "Format the Portfolio page or personal website to look polished and premium.",
            "Ask a peer or mentor to do a quick review of your SDE resume.",
            "Make sure your contact email and phone number are visible and up-to-date.",
            "Tailor resume bullet points to highlight match for junior SDE roles."
        ));
    }

    public boolean hasSubject(String topicKey) {
        return subjectTasks.containsKey(topicKey);
    }

    public String getTaskForSubject(String topicKey, int index) {
        List<String> tasks = subjectTasks.get(topicKey);
        if (tasks == null || tasks.isEmpty()) {
            return "Study " + topicKey + " core concepts and practice questions.";
        }
        return tasks.get(Math.abs(index) % tasks.size());
    }

    public Set<String> getSubjects() {
        return subjectTasks.keySet();
    }
}
