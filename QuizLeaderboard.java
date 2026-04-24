import java.net.URI;
import java.net.http.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class QuizLeaderboard {

    private static final String REG_NO      = "RA2311003011498";
    private static final String BASE_URL    = "https://devapigw.vidalhealthtpa.com/srm-quiz-task";
    private static final int    TOTAL_POLLS = 10;
    private static final int    POLL_DELAY  = 5000;

    private static final String RESET  = "\u001B[0m";
    private static final String CYAN   = "\u001B[36m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED    = "\u001B[31m";
    private static final String BOLD   = "\u001B[1m";
    private static final String PURPLE = "\u001B[35m";

    private final HttpClient          httpClient = HttpClient.newHttpClient();
    private final Set<String>         seen       = new HashSet<>();
    private final Map<String,Integer> scoreBoard = new LinkedHashMap<>();
    private       int                 dupCount   = 0;
    private       int                 validCount = 0;

    public static void main(String[] args) throws Exception {
        new QuizLeaderboard().run();
    }

    private void run() throws Exception {
        printBanner();
        collectAllPolls();
        List<Map.Entry<String,Integer>> leaderboard = buildLeaderboard();
        printLeaderboard(leaderboard);
        String response = submitLeaderboard(leaderboard);
        printResult(response);
    }

    private void collectAllPolls() throws Exception {
        System.out.println(CYAN + "  Polling Quiz API (" + TOTAL_POLLS + " rounds, 5s gap each)\n" + RESET);

        for (int poll = 0; poll < TOTAL_POLLS; poll++) {
            String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            System.out.printf(BOLD + "  [%s] Poll %2d/%d " + RESET, timestamp, poll + 1, TOTAL_POLLS);

            String url  = BASE_URL + "/quiz/messages?regNo=" + REG_NO + "&poll=" + poll;
            String body = fetchGet(url);

            processEvents(body);

            if (poll < TOTAL_POLLS - 1) {
                System.out.print(YELLOW + "  ⏳ Waiting 5s..." + RESET);
                Thread.sleep(POLL_DELAY);
                System.out.print("\r" + " ".repeat(30) + "\r");
            }
        }
    }

    private void processEvents(String json) {
        List<String> events = extractEvents(json);
        int skipped = 0;
        StringBuilder log = new StringBuilder();

        for (String event : events) {
            String participant = extractString(event, "participant");
            String roundId     = extractString(event, "roundId");
            int    score       = extractInt(event, "score");
            String key         = roundId + "§" + participant;

            if (seen.contains(key)) {
                skipped++;
                dupCount++;
                log.append(RED + "    ✗ SKIP  " + RESET)
                   .append(participant).append(" [").append(roundId)
                   .append("] score=").append(score).append(" (duplicate)\n");
            } else {
                seen.add(key);
                scoreBoard.merge(participant, score, Integer::sum);
                validCount++;
                log.append(GREEN + "    ✔ ADD   " + RESET)
                   .append(participant).append(" [").append(roundId)
                   .append("] +").append(score).append("\n");
            }
        }

        String status = (skipped > 0)
                ? RED   + "(" + skipped + " dup" + (skipped > 1 ? "s" : "") + " skipped)" + RESET
                : GREEN + "(all new)" + RESET;
        System.out.println("→ " + events.size() + " event(s) " + status);
        System.out.print(log);
    }

    private List<Map.Entry<String,Integer>> buildLeaderboard() {
        return scoreBoard.entrySet()
                .stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .collect(Collectors.toList());
    }

    private void printLeaderboard(List<Map.Entry<String,Integer>> leaderboard) {
        int total = leaderboard.stream().mapToInt(Map.Entry::getValue).sum();

        System.out.println("\n" + BOLD + PURPLE);
        System.out.println("  ┌─────────────────────────────────────────┐");
        System.out.println("  │           🏆  LEADERBOARD               │");
        System.out.println("  ├──────┬──────────────────────┬───────────┤");
        System.out.println("  │ Rank │ Participant          │   Score   │");
        System.out.println("  ├──────┼──────────────────────┼───────────┤");

        String[] medals = {"🥇", "🥈", "🥉"};
        for (int i = 0; i < leaderboard.size(); i++) {
            String medal = (i < 3) ? medals[i] : "   ";
            String name  = leaderboard.get(i).getKey();
            int    score = leaderboard.get(i).getValue();
            System.out.printf("  │  %s  │ %-20s │  %6d   │%n", medal, name, score);
        }

        System.out.println("  ├──────┴──────────────────────┼───────────┤");
        System.out.printf( "  │          TOTAL SCORE        │  %6d   │%n", total);
        System.out.println("  └─────────────────────────────┴───────────┘" + RESET);
        System.out.println(CYAN + "\n  📊 Stats: " + validCount + " valid events, "
                + dupCount + " duplicates removed" + RESET);
    }

    private String submitLeaderboard(List<Map.Entry<String,Integer>> leaderboard) throws Exception {
        String payload = buildJson(leaderboard);
        System.out.println(YELLOW + "\n  📤 Submitting leaderboard..." + RESET);
        System.out.println(CYAN + "  Payload: " + RESET + payload);
        return fetchPost(BASE_URL + "/quiz/submit", payload);
    }

    private void printResult(String response) {
        System.out.println(BOLD + GREEN);
        System.out.println("  ┌─────────────────────────────────────────┐");
        System.out.println("  │         ✅  SUBMISSION RESULT          │");
        System.out.println("  └─────────────────────────────────────────┘" + RESET);
        System.out.println(CYAN + "  Server Response: " + RESET + response);

        boolean correct = response.contains("\"isCorrect\":true") || response.contains("\"isCorrect\": true");
        if (correct) {
            System.out.println(BOLD + GREEN + "\n  CORRECT! Leaderboard accepted by server." + RESET);
        } else {
            System.out.println(BOLD + YELLOW + "\n   Submission received. Verify response above." + RESET);
        }
        System.out.println(PURPLE + "\n  ══════════════════════════════════════════" + RESET);
    }

    private String fetchGet(String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        return httpClient.send(req, HttpResponse.BodyHandlers.ofString()).body();
    }

    private String fetchPost(String url, String body) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return httpClient.send(req, HttpResponse.BodyHandlers.ofString()).body();
    }

    private String buildJson(List<Map.Entry<String,Integer>> leaderboard) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"regNo\": \"").append(REG_NO).append("\",\n  \"leaderboard\": [\n");
        for (int i = 0; i < leaderboard.size(); i++) {
            sb.append("    {\"participant\": \"").append(leaderboard.get(i).getKey())
              .append("\", \"totalScore\": ").append(leaderboard.get(i).getValue()).append("}");
            if (i < leaderboard.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n}");
        return sb.toString();
    }

    private List<String> extractEvents(String json) {
        List<String> list = new ArrayList<>();
        int idx      = json.indexOf("\"events\"");
        int arrStart = json.indexOf("[", idx);
        int arrEnd   = json.lastIndexOf("]");
        if (arrStart == -1 || arrEnd == -1) return list;
        String arr = json.substring(arrStart + 1, arrEnd);
        int depth = 0, start = -1;
        for (int i = 0; i < arr.length(); i++) {
            char c = arr.charAt(i);
            if (c == '{') { if (depth++ == 0) start = i; }
            else if (c == '}' && --depth == 0 && start != -1) {
                list.add(arr.substring(start, i + 1));
                start = -1;
            }
        }
        return list;
    }

    private String extractString(String json, String key) {
        int i = json.indexOf("\"" + key + "\"");
        if (i == -1) return "";
        int s = json.indexOf('"', json.indexOf(':', i) + 1) + 1;
        return json.substring(s, json.indexOf('"', s));
    }

    private int extractInt(String json, String key) {
        int i = json.indexOf("\"" + key + "\"");
        if (i == -1) return 0;
        int s = json.indexOf(':', i) + 1;
        while (s < json.length() && json.charAt(s) == ' ') s++;
        int e = s;
        while (e < json.length() && Character.isDigit(json.charAt(e))) e++;
        return Integer.parseInt(json.substring(s, e));
    }

    private void printBanner() {
        System.out.println(BOLD + CYAN);
        System.out.println("  ╔══════════════════════════════════════════╗");
        System.out.println("  ║     BAJAJ FINSERV HEALTH — QUALIFIER     ║");
        System.out.println("  ║         Quiz Leaderboard Engine          ║");
        System.out.println("  ║   Reg: RA2311003011498  |  Java 11+      ║");
        System.out.println("  ╚══════════════════════════════════════════╝");
        System.out.println(RESET);
    }
}