# PlacementOS

PlacementOS is a self-hosted, full-stack placement-preparation tracker. It helps computer science students prepare for software engineering placements by generating a customized day-by-day preparation schedule based on available time and selected topics.

The application automatically bridges study materials with verification mechanisms: every DSA item links to a real LeetCode problem, and video items link to curated YouTube playlists. Using a LeetCode solved-problem sync and an in-app YouTube watch-progress tracker, PlacementOS keeps track of your progress automatically.

---

## Features

- **Customized Schedule Generation:** Input your start and end dates along with the DSA topics and core CS subjects you wish to cover, and receive a complete day-by-day plan.
- **Real Resource Matching:** 100% of DSA links map to actual LeetCode problems, and video links resolve to a real YouTube playlist.
- **LeetCode Auto-Sync:** Verifies your solved problems by checking your recent accepted submissions via an unofficial LeetCode GraphQL query and automatically marks matching items as complete.
- **In-App Video Watch Tracking:** Uses the YouTube IFrame Player API to track your watch percentage in real-time, automatically marking video items complete when you have watched at least 90% of the video.
- **Dual-Pane Study Workspace:** Features a sleek sidebar for weekly progress tracking, navigation, and summary statistics, alongside a focus-oriented main pane with countdown timers, task details, and client-side scratchpads.
- **Completely Dependency-Free Frontend:** Zero frameworks, zero bundlers, and zero build steps on the client-side. The frontend is built purely with semantic HTML5, CSS3 Custom Properties, and Vanilla ES2020+ JavaScript.
- **Secure Backend:** A Spring Boot (Java 21) REST API backed by a PostgreSQL database, featuring stateless JWT-based authentication and BCrypt password hashing.

---

## Tech Stack & Dependencies

### Frontend
- **Markup:** Plain HTML5 with semantic layout elements.
- **Styling:** CSS3 variables for native light/dark theme switching.
- **Typography:**
  - *Serif Header Font:* Fraunces (via Google Fonts)
  - *Sans Body Font:* Inter (via Google Fonts)
  - *Monospace Font:* JetBrains Mono (via Google Fonts)
- **Scripting:** Vanilla ES2020+ JavaScript.
- **Integrations:** YouTube IFrame Player API, HTML5 Notification API.

### Backend
- **Language & Runtime:** Java 21 (OpenJDK 21)
- **Framework:** Spring Boot 3.3.4 (`spring-boot-starter-web`, `spring-boot-starter-validation`, `spring-boot-starter-security`, `spring-boot-starter-data-jpa`)
- **Database:** PostgreSQL (with JDBC driver)
- **Security:** Spring Security (Stateless, CORS enabled), BCrypt
- **Token Handling:** JJWT (Java JWT Library) 0.12.6
- **Boilerplate Reduction:** Project Lombok
- **Build Tool:** Apache Maven

---

## System Architecture

```
                 +-------------------+
                 |   Browser (SPA)   |
                 |  HTML / CSS / JS  |
                 +---------+---------+
                           |
                           | HTTPS (REST API / JWT)
                           v
                 +---------+---------+
                 |    Spring Boot    |
                 |    Web Backend    |
                 +----+---------+----+
                      |         |
         JDBC Queries |         | Third-Party API Integrations
                      v         +-------------------------------------+
        +-------------+----+    | - LeetCode GraphQL API              |
        |    PostgreSQL    |    | - YouTube Data API v3               |
        |  (placementos)   |    | - YouTube IFrame API (via Client)   |
        +------------------+    +-------------------------------------+
```

---

## Topic Taxonomy

PlacementOS handles 19 DSA topics and 8 CS subjects. The frontend and backend share this taxonomy to prevent drift.

### DSA Topics & LeetCode Tag Mapping
| Topic Key | LeetCode Tag Slug | Topic Key | LeetCode Tag Slug |
| :--- | :--- | :--- | :--- |
| `ARRAYS` | `array` | `STACK_QUEUE` | `stack` |
| `HASHING` | `hash-table` | `HEAP` | `heap-priority-queue` |
| `SORTING` | `sorting` | `GREEDY` | `greedy` |
| `BINARY_SEARCH` | `binary-search` | `BACKTRACKING` | `backtracking` |
| `STRINGS` | `string` | `TREES` | `tree` |
| `TWO_POINTERS` | `two-pointers` | `BST` | `binary-search-tree` |
| `SLIDING_WINDOW`| `sliding-window` | `GRAPHS` | `graph` |
| `LINKED_LIST` | `linked-list` | `DP` | `dynamic-programming` |
| `RECURSION` | `recursion` | `TRIE` | `trie` |
| `BIT_MANIPULATION`| `bit-manipulation`| | |

### Core CS Subjects (Rotating task-based items)
- `JAVA_OOP`
- `SQL`
- `DBMS`
- `OS`
- `CN`
- `PROJECT`
- `INTERVIEW_PREP`
- `RESUME_PORTFOLIO`

---

## Database Schema

### `users`
| Column | Type | Constraints |
| :--- | :--- | :--- |
| `id` | BIGINT | PK, Identity |
| `email` | VARCHAR | NOT NULL, UNIQUE |
| `password_hash` | VARCHAR | NOT NULL (BCrypt) |
| `leetcode_username`| VARCHAR | Nullable |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() |
| `last_leetcode_sync_at`| TIMESTAMP| Nullable |

### `schedules`
| Column | Type | Constraints |
| :--- | :--- | :--- |
| `id` | BIGINT | PK, Identity |
| `user_id` | BIGINT | FK -> `users(id)`, NOT NULL |
| `start_date` | DATE | NOT NULL |
| `end_date` | DATE | NOT NULL |
| `selected_topics` | VARCHAR(1000) | NOT NULL (comma-separated keys) |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() |

### `schedule_items`
| Column | Type | Constraints |
| :--- | :--- | :--- |
| `id` | BIGINT | PK, Identity |
| `schedule_id` | BIGINT | FK -> `schedules(id)`, NOT NULL |
| `item_date` | DATE | NOT NULL |
| `day_number` | INTEGER | NOT NULL (1-based) |
| `type` | VARCHAR | NOT NULL (enum: `DSA_QUESTION`, `VIDEO`, `TASK`) |
| `title` | VARCHAR(500) | NOT NULL |
| `topic_key` | VARCHAR(100) | NOT NULL |
| `leetcode_slug` | VARCHAR(200) | Nullable |
| `resource_url` | VARCHAR(500) | Nullable |
| `youtube_video_id` | VARCHAR(50) | Nullable |
| `completed` | BOOLEAN | NOT NULL, DEFAULT FALSE |
| `completed_via` | VARCHAR | Nullable (enum: `MANUAL`, `LEETCODE_SYNC`, `VIDEO_WATCHED`) |
| `completed_at` | TIMESTAMP | Nullable |
*Indexes: `(schedule_id, item_date)`, `(leetcode_slug)`*

### `leetcode_problems` (Imported catalog, never hand-entered)
| Column | Type | Constraints |
| :--- | :--- | :--- |
| `id` | BIGINT | PK, Identity |
| `slug` | VARCHAR(200) | NOT NULL, UNIQUE |
| `title` | VARCHAR(300) | NOT NULL |
| `difficulty` | VARCHAR(20) | NOT NULL (enum: `Easy`, `Medium`, `Hard`) |
| `paid_only` | BOOLEAN | NOT NULL |
| `primary_tag` | VARCHAR(100) | NOT NULL (Striver tag slug) |
| `all_tags` | VARCHAR(1000) | Nullable (comma-separated tags) |

### `youtube_videos` (Imported playlist, never hand-entered)
| Column | Type | Constraints |
| :--- | :--- | :--- |
| `id` | BIGINT | PK, Identity |
| `video_id` | VARCHAR(50) | NOT NULL, UNIQUE |
| `title` | VARCHAR(500) | NOT NULL |
| `playlist_position`| INTEGER | NOT NULL, 0-based |
| `matched_topic_key`| VARCHAR(100) | Nullable (best-effort title keyword match) |

---

## Core Scheduling Algorithm

When `POST /api/schedules` is invoked:
1. Validates the date range: must be between 1 day and 2 years.
2. Splitting topics: splits the selected topics into **DSA Topics** and **CS Subjects**.
3. Day-by-day distribution:
   - For each day in the date range, calculate its `day_number` (1-based) and `item_date`.
   - **DSA rotation:** The algorithm rotates through the requested DSA topics one by one per day. For example, if `ARRAYS` and `HASHING` are selected, Day 1 is an `ARRAYS` day, Day 2 is a `HASHING` day, Day 3 is an `ARRAYS` day, etc.
   - For each DSA day:
     - Pull the next unassigned problem from `leetcode_problems` for that topic key, ordered by difficulty (`Easy` -> `Medium` -> `Hard`).
     - A `DSA_QUESTION` item is created for it.
     - On the **first appearance** of that DSA topic in the schedule, the algorithm looks up a matching video from `youtube_videos` (matched by `matched_topic_key` or title similarity) and attaches it as a `VIDEO` item on that day.
   - **CS Subjects rotation:** If the day's index or topic rotation yields a CS Subject, or if no DSA topics are selected, the algorithm rotates through the chosen CS Subjects day-by-day.
     - For a subject day, a `TASK` item is generated with a rotating task description from a pre-defined static pool of tasks for that subject.
   - If the user selected neither or if the day represents a rest day, it lists zero items (and the frontend displays a rest-day card).

---

## REST API Documentation

All request and response bodies are JSON. All endpoints except `/api/auth/**` require `Authorization: Bearer <token>`.

### Authentication
- **`POST /api/auth/register`**
  - *Request:* `{ "email": "user@example.com", "password": "securepassword123" }`
  - *Response (200):* `{ "token": "ey...", "userId": 1, "email": "user@example.com", "leetcodeUsername": null, "lastLeetcodeSyncAt": null }`
  - *Error (409):* Email already registered.
- **`POST /api/auth/login`**
  - *Request:* `{ "email": "user@example.com", "password": "securepassword123" }`
  - *Response (200):* Same shape as register.
  - *Error (401):* Invalid credentials.

### Users
- **`GET /api/users/me`**
  - *Response (200):* `{ "id": 1, "email": "user@example.com", "leetcodeUsername": "LeetUser", "lastLeetcodeSyncAt": "2026-07-10T12:00:00Z" }`
- **`PATCH /api/users/me`**
  - *Request:* `{ "leetcodeUsername": "LeetUser" }`
  - *Response (200):* `{ "leetcodeUsername": "LeetUser" }`
- **`POST /api/users/me/leetcode-sync`**
  - *Description:* Trigger on-demand sync. Fetches user's last ~20 accepted submissions from LeetCode, checks matching uncompleted `DSA_QUESTION` items in all user schedules, marks them complete, and returns the count of marked items.
  - *Response (200):* `{ "itemsAutoCompleted": 3 }`

### Schedules
- **`POST /api/schedules`**
  - *Request:* `{ "startDate": "2026-08-01", "endDate": "2026-09-30", "topics": ["ARRAYS", "HASHING", "SQL"] }`
  - *Response (200):*
    ```json
    {
      "id": 4,
      "startDate": "2026-08-01",
      "endDate": "2026-09-30",
      "topics": ["ARRAYS", "HASHING", "SQL"],
      "createdAt": "2026-07-10T12:00:00Z",
      "items": [
        {
          "id": 101,
          "itemDate": "2026-08-01",
          "dayNumber": 1,
          "type": "VIDEO",
          "title": "Watch: Arrays Pattern Explained",
          "topicKey": "ARRAYS",
          "resourceUrl": "https://www.youtube.com/watch?v=abc123",
          "youtubeVideoId": "abc123",
          "completed": false,
          "completedVia": null
        },
        {
          "id": 102,
          "itemDate": "2026-08-01",
          "dayNumber": 1,
          "type": "DSA_QUESTION",
          "title": "Two Sum",
          "topicKey": "ARRAYS",
          "resourceUrl": "https://leetcode.com/problems/two-sum/",
          "leetcodeSlug": "two-sum",
          "completed": false,
          "completedVia": null
        }
      ]
    }
    ```
- **`GET /api/schedules`**
  - *Response (200):* Returns an array of user's schedules (same details as above).
- **`GET /api/schedules/{id}`**
  - *Response (200):* Returns a single schedule detail object. Returns 404 if schedule doesn't exist or is owned by another user.

### Schedule Items
- **`PATCH /api/items/{id}/progress`**
  - *Request:* `{ "completed": true, "completedVia": "MANUAL" }` *(or `VIDEO_WATCHED`)*
  - *Response (200):* Updated schedule item.

### Admin Imports
- **`POST /api/admin/import/leetcode-catalog`**
  - *Description:* Seed database with Striver DSA sheet problems.
  - *Response (200):* `{ "imported": 2847, "skippedExisting": 153 }`
- **`POST /api/admin/import/youtube-playlist`**
  - *Description:* Seed database with playlist video catalog.
  - *Response (200):* `{ "imported": 125, "skippedExisting": 0, "matchedToTopic": 98 }`

---

## Environment Variables

The backend loads configuration options from environment variables or `application.yml`:

| Environment Variable | Required | Description | Default |
| :--- | :--- | :--- | :--- |
| `DB_USERNAME` | Yes | PostgreSQL username | `postgres` |
| `DB_PASSWORD` | Yes | PostgreSQL password | None |
| `JWT_SECRET` | Yes (Prod) | 256-bit secret key for JWT signing | `dev-only-placeholder-secret-string-at-least-32-chars-long` |
| `ALLOWED_ORIGIN` | Yes | CORS allowed origin (e.g., `http://localhost:5000` or `*`) | `http://localhost:5000` |
| `YOUTUBE_API_KEY` | Yes | YouTube Data API v3 developer key | None |

---

## Client-Side Storage Keys

The frontend uses `localStorage` for visual customizations, reminders, and token persistence:

- `pos_token`: Active JWT authentication token.
- `pos_theme`: Holds active theme state: `'light'` or `'dark'`.
- `pos_notes_v1`: JSON map of per-day notes, keyed by `scheduleId-dayNumber`.
- `pos_reminders_v1`: JSON map of per-item reminder options, keyed by `itemId`.

---

## Installation & Setup

### Prerequisites
1. **Java Development Kit (JDK) 21**
2. **PostgreSQL** database server running locally.
3. A **YouTube Data API Key** (for playlist importing).

### 1. Database Setup
Create a PostgreSQL database named `placementos`:
```sql
CREATE DATABASE placementos;
```

### 2. Backend Environment Variables
Set the required environment variables in your terminal or configuration. For example, in PowerShell:
```powershell
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="yourpassword"
$env:JWT_SECRET="super-secret-key-32-chars-long-or-more-placement-os"
$env:YOUTUBE_API_KEY="your-youtube-api-key"
$env:ALLOWED_ORIGIN="http://localhost:5000"
```

### 3. Build & Run Backend
Navigate to the backend directory and run:
```bash
mvn clean install
mvn spring-boot:run
```
The REST API will be available at `http://localhost:8080`.

### 4. Seed Catalog Data (Admin Only)
Ensure your backend is running, then populate the LeetCode catalog and YouTube playlist:
```bash
curl -X POST http://localhost:8080/api/admin/import/leetcode-catalog -H "Authorization: Bearer <JWT>"
curl -X POST http://localhost:8080/api/admin/import/youtube-playlist -H "Authorization: Bearer <JWT>"
```
*(Note: To get a JWT, register an account first, or use a local utility to run these administrative calls).*

### 5. Running the Frontend
The frontend requires no installation or build step. Simply host the frontend directory using any local web server. For example:
```bash
npx serve -l 5000
```
Open `http://localhost:5000` in your web browser. Make sure this matches the `ALLOWED_ORIGIN` configuration on the backend.
