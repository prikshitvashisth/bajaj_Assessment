# Bajaj Finserv Health

## Approach
- Polls the API 10 times (poll 0–9) with 5s delay between each
- Deduplicates events using `roundId + participant` as unique key
- Aggregates scores per participant
- Submits sorted leaderboard once

## How to Run
javac QuizLeaderboard.java
java QuizLeaderboard

## Result
Diana: 470 | Ethan: 455 | Fiona: 440 | Total: 1365

Output 1
  ╔══════════════════════════════════════════╗
  ║     BAJAJ FINSERV HEALTH ? QUALIFIER     ║
  ║         Quiz Leaderboard Engine          ║
  ║   Reg: RA2311003011498  |  Java 11+      ║
  ╚══════════════════════════════════════════╝

  Polling Quiz API (10 rounds, 5s gap each)

  [18:22:06] Poll  1/10 ? 2 event(s) (all new)
    ? ADD   Diana [R1] +200
    ? ADD   Ethan [R1] +155
  [18:22:12] Poll  2/10 ? 1 event(s) (all new)
    ? ADD   Fiona [R1] +180
  [18:22:17] Poll  3/10 ? 2 event(s) (all new)
    ? ADD   Diana [R2] +95
    ? ADD   Ethan [R2] +210
  [18:22:22] Poll  4/10 ? 1 event(s) (1 dup skipped)
    ? SKIP  Diana [R2] score=95 (duplicate)
  [18:22:27] Poll  5/10 ? 2 event(s) (all new)
    ? ADD   Fiona [R3] +140
    ? ADD   Diana [R3] +175
  [18:22:32] Poll  6/10 ? 1 event(s) (all new)
    ? ADD   Fiona [R2] +120
  [18:22:37] Poll  7/10 ? 2 event(s) (1 dup skipped)
    ? ADD   Ethan [R3] +90
    ? SKIP  Diana [R1] score=200 (duplicate)
  [18:22:42] Poll  8/10 ? 1 event(s) (1 dup skipped)
    ? SKIP  Fiona [R3] score=140 (duplicate)
  [18:22:47] Poll  9/10 ? 2 event(s) (2 dups skipped)
    ? SKIP  Ethan [R2] score=210 (duplicate)
    ? SKIP  Diana [R3] score=175 (duplicate)
  [18:22:52] Poll 10/10 ? 1 event(s) (1 dup skipped)
    ? SKIP  Fiona [R1] score=180 (duplicate)


  ┌─────────────────────────────────────────┐
  │           ?  LEADERBOARD                │
  ├──────┬──────────────────────┬───────────┤
  │ Rank │ Participant          │   Score   │
  ├──────┼──────────────────────┼───────────┤
  │  ?   │ Diana                │    470    │
  │  ?   │ Ethan                │    455    │
  │  ?   │ Fiona                │    440    │
  ├──────┴──────────────────────┼───────────┤
  │          TOTAL SCORE        │    1365   │
  └─────────────────────────────┴───────────┘

  ? Stats: 9 valid events, 6 duplicates removed

  ? Submitting leaderboard...
  Payload: {
  "regNo": "RA2311003011498",
  "leaderboard": [
    {"participant": "Diana", "totalScore": 470},
    {"participant": "Ethan", "totalScore": 455},
    {"participant": "Fiona", "totalScore": 440}
  ]
}

  ┌─────────────────────────────────────────┐
  │         ?  SUBMISSION RESULT            │
  └─────────────────────────────────────────┘
  Server Response: {"regNo":"RA2311003011498","totalPollsMade":30,"submittedTotal":1365,"attemptCount":3}

   Submission received. Verify response above.

output 2 
  ╔══════════════════════════════════════════╗
  ║     BAJAJ FINSERV HEALTH ? QUALIFIER     ║
  ║         Quiz Leaderboard Engine          ║
  ║   Reg: RA2311003011498  |  Java 11+      ║
  ╚══════════════════════════════════════════╝

  Polling Quiz API (10 rounds, 5s gap each)

  [18:46:33] Poll  1/10 ? 0 event(s) (all new)
  [18:46:38] Poll  2/10 ? 0 event(s) (all new)
  [18:46:43] Poll  3/10 ? 0 event(s) (all new)
  [18:46:48] Poll  4/10 ? 0 event(s) (all new)
  [18:46:53] Poll  5/10 ? 0 event(s) (all new)
  [18:46:58] Poll  6/10 ? 0 event(s) (all new)
  [18:47:03] Poll  7/10 ? 0 event(s) (all new)
  [18:47:08] Poll  8/10 ? 0 event(s) (all new)
  [18:47:13] Poll  9/10 ? 0 event(s) (all new)
  [18:47:18] Poll 10/10 ? 0 event(s) (all new)


  ┌─────────────────────────────────────────┐
  │           ?  LEADERBOARD                │
  ├──────┬──────────────────────┬───────────┤
  │ Rank │ Participant          │   Score   │
  ├──────┼──────────────────────┼───────────┤
  ├──────┴──────────────────────┼───────────┤
  │          TOTAL SCORE        │       0   │
  └─────────────────────────────┴───────────┘

  ? Stats: 0 valid events, 0 duplicates removed

  ? Submitting leaderboard...
  Payload: {
  "regNo": "RA2311003011498",
  "leaderboard": [
  ]
}

  ┌─────────────────────────────────────────┐
  │         ?  SUBMISSION RESULT            │
  └─────────────────────────────────────────┘
  Server Response: no available server


   Submission received. Verify response above.

  ══════════════════════════════════════════

  ══════════════════════════════════════════
