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
