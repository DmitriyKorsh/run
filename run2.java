import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class run2 {

    static class State {
        int[][] pos;
        int keys;
        int cost;

        State(int[][] pos, int keys, int cost) {
            this.pos = new int[4][2];
            for (int i = 0; i < 4; i++) {
                this.pos[i][0] = pos[i][0];
                this.pos[i][1] = pos[i][1];
            }
            this.keys = keys;
            this.cost = cost;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof State s)) return false;
            return Arrays.deepEquals(this.pos, s.pos) && this.keys == s.keys;
        }

        @Override
        public int hashCode() {
            return Arrays.deepHashCode(pos) * 311 + keys;
        }
    }

    private static int heuristic(int r, int c, Map<Character, int[]> keyPositions) {
        var minDist = Integer.MAX_VALUE;
        for (Map.Entry<Character, int[]> entry : keyPositions.entrySet()) {
            var keyR = entry.getValue()[0];
            var keyC = entry.getValue()[1];
            minDist = Math.min(minDist, Math.abs(r - keyR) + Math.abs(c - keyC));
        }
        return minDist;
    }

    private static int solve(char[][] grid) {
        var m = grid.length;
        var n = grid[0].length;
        Map<Character, int[]> keyPositions = new HashMap<>();
        int[][] starts = new int[4][2];
        var startIndex = 0;
        var totalKeys = 0;

        for (var i = 0; i < m; ++i) {
            for (var j = 0; j < n; ++j) {
                var c = grid[i][j];
                if (c == '@') {
                    starts[startIndex][0] = i;
                    starts[startIndex][1] = j;
                    startIndex++;
                } else if ('a' <= c && c <= 'z') {
                    keyPositions.put(c, new int[]{i, j});
                    totalKeys++;
                }
            }
        }

        var queue = new PriorityQueue<State>(Comparator.comparingInt(s -> s.cost));
        var visited = new HashMap<State, Integer>();
        var initialHeuristic = 0;
        for (var i = 0; i < 4; i++) {
            initialHeuristic += heuristic(starts[i][0], starts[i][1], keyPositions);
        }

        var start = new State(starts, 0, initialHeuristic);
        queue.offer(start);
        visited.put(start, 0);

        while (!queue.isEmpty()) {
            var curr = queue.poll();
            var currSteps = visited.get(curr);

            if (Integer.bitCount(curr.keys) == totalKeys) {
                return currSteps;
            }

            for (var robot = 0; robot < 4; ++robot) {
                var pos = curr.pos[robot];
                for (int[] move : bfs(pos[0], pos[1], curr.keys, grid)) {
                    var newKeys = curr.keys;
                    var x = move[0];
                    var y = move[1];
                    var steps = move[2];
                    var c = grid[x][y];
                    if ('a' <= c && c <= 'z') {
                        newKeys |= (1 << (c - 'a'));
                    }
                    var newPos = new int[4][2];
                    for (var i = 0; i < 4; ++i) {
                        newPos[i][0] = curr.pos[i][0];
                        newPos[i][1] = curr.pos[i][1];
                    }
                    newPos[robot][0] = x;
                    newPos[robot][1] = y;

                    var heuristicValue = 0;
                    for (var i = 0; i < 4; i++) {
                        heuristicValue += heuristic(newPos[i][0], newPos[i][1], keyPositions);
                    }

                    var totalCost = currSteps + steps + heuristicValue;
                    var newState = new State(newPos, newKeys, totalCost);

                    if (!visited.containsKey(newState) || visited.get(newState) > currSteps + steps) {
                        visited.put(newState, currSteps + steps);
                        queue.offer(newState);
                    }
                }
            }
        }

        return -1;
    }

    private static List<int[]> bfs(int startX, int startY, int keys, char[][] grid) {
        var m = grid.length;
        var n = grid[0].length;
        Queue<int[]> queue = new LinkedList<>();
        var visited = new boolean[m][n];
        List<int[]> result = new ArrayList<>();

        queue.offer(new int[]{startX, startY, 0});
        visited[startX][startY] = true;

        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        while (!queue.isEmpty()) {
            var curr = queue.poll();
            var x = curr[0];
            var y = curr[1];
            var dist = curr[2];
            var cell = grid[x][y];

            if ('A' <= cell && cell <= 'Z' && (keys & (1 << (cell - 'A'))) == 0) {
                continue;
            }

            if ('a' <= cell && cell <= 'z' && (keys & (1 << (cell - 'a'))) == 0) {
                result.add(new int[]{x, y, dist});
                continue;
            }

            for (var d : dirs) {
                var nx = x + d[0];
                var ny = y + d[1];
                if (nx >= 0 && ny >= 0 && nx < m && ny < n &&
                        grid[nx][ny] != '#' && !visited[nx][ny]) {
                    visited[nx][ny] = true;
                    queue.offer(new int[]{nx, ny, dist + 1});
                }
            }
        }

        return result;
    }

    private static char[][] getInput() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            lines.add(line);
        }

        char[][] maze = new char[lines.size()][];
        for (int i = 0; i < lines.size(); i++) {
            maze[i] = lines.get(i).toCharArray();
        }

        return maze;
    }

    public static void main(String[] args) throws IOException {
        char[][] data = getInput();
        int result = solve(data);

        if (result == Integer.MAX_VALUE) {
            System.out.println("No solution found");
        } else {
            System.out.println(result);
        }
    }
}
