package com.visualiser.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.visualiser.dto.Step;
import com.visualiser.service.BstService;
import com.visualiser.service.PathfindingService;
import com.visualiser.service.SortingService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AlgoWsHandler extends TextWebSocketHandler {

    private final PathfindingService pathService;
    private final SortingService sortService;
    private final BstService bstService;
    private final ObjectMapper mapper = new ObjectMapper();
    private BstService.Node bstRoot = null;

    public AlgoWsHandler(PathfindingService pathService, SortingService sortService, BstService bstService) {
        this.pathService = pathService;
        this.sortService = sortService;
        this.bstService = bstService;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage msg) throws Exception {
        JsonNode root = mapper.readTree(msg.getPayload());
        String domain = root.path("domain").asText();

        if ("path".equals(domain)) {
            String algo = root.path("algo").asText("dijkstra");
            int sr = root.path("sr").asInt(5);
            int sc = root.path("sc").asInt(5);
            int er = root.path("er").asInt(15);
            int ec = root.path("ec").asInt(25);
            JsonNode gridNode = root.path("grid");
            int rows = gridNode.size();
            int cols = gridNode.get(0).size();
            int[][] grid = new int[rows][cols];
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    grid[r][c] = gridNode.get(r).get(c).asInt();
                }
            }
            List<Step> steps = pathService.run(algo, grid, sr, sc, er, ec);
            send(session, Map.of("domain", "path", "steps", steps));
            return;
        }

        if ("sort".equals(domain)) {
            String algo = root.path("algo").asText("bubble");
            JsonNode arrNode = root.path("array");
            int[] arr = new int[arrNode.size()];
            for (int i = 0; i < arrNode.size(); i++) arr[i] = arrNode.get(i).asInt();
            List<Step> steps = sortService.run(algo, arr);
            send(session, Map.of("domain", "sort", "steps", steps));
            return;
        }

        if ("bst".equals(domain)) {
            String act = root.path("action").asText("insert");
            if ("reset".equals(act)) {
                bstRoot = null;
                send(session, Map.of("domain", "bst", "steps", List.of()));
                return;
            }
            if ("insert".equals(act)) {
                int val = root.path("val").asInt();
                List<Step> steps = bstService.insert(bstRoot, val);
                bstRoot = insertNode(bstRoot, val);
                send(session, Map.of("domain", "bst", "steps", steps));
                return;
            }
            if ("traverse".equals(act)) {
                String order = root.path("order").asText("inorder");
                List<Step> steps = bstService.traverse(bstRoot, order);
                send(session, Map.of("domain", "bst", "steps", steps));
            }
        }
    }

    private BstService.Node insertNode(BstService.Node node, int val) {
        if (node == null) return new BstService.Node(val);
        if (val < node.val) node.left = insertNode(node.left, val);
        else if (val > node.val) node.right = insertNode(node.right, val);
        return node;
    }

    private void send(WebSocketSession session, Map<String, Object> data) throws Exception {
        session.sendMessage(new TextMessage(mapper.writeValueAsString(data)));
    }
}
