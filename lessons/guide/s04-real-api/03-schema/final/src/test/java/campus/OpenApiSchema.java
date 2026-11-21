// Cut one JSON Schema out of an OpenAPI document: the server's own contract becomes the test's schema.
package campus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;

public final class OpenApiSchema {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String COMPONENTS = "#/components/schemas/";

    private OpenApiSchema() {
    }

    // --- keep
    // OpenAPI 3.1 component schemas ARE JSON Schema (draft 2020-12). Only their $ref paths point inside the
    // OpenAPI file (#/components/schemas/X). We copy the schema we want plus every schema it refers to
    // into $defs, rewrite the refs to #/$defs/X, and say which draft it is in $schema.
    public static String of(String openApiJson, String name) throws Exception {
        JsonNode all = MAPPER.readTree(openApiJson).path("components").path("schemas");
        ObjectNode defs = MAPPER.createObjectNode();
        Deque<String> todo = new ArrayDeque<>();
        todo.add(name);
        while (!todo.isEmpty()) {
            String next = todo.pop();
            if (defs.has(next)) {
                continue;
            }
            JsonNode schema = all.get(next).deepCopy();
            rewriteRefs(schema, todo);
            defs.set(next, schema);
        }
        ObjectNode root = MAPPER.createObjectNode();
        root.put("$schema", "https://json-schema.org/draft/2020-12/schema");
        root.put("$ref", "#/$defs/" + name);
        root.set("$defs", defs);
        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(root);
    }

    private static void rewriteRefs(JsonNode node, Deque<String> todo) {
        if (node instanceof ObjectNode object) {
            JsonNode ref = object.get("$ref");
            if (ref != null && ref.asText().startsWith(COMPONENTS)) {
                String target = ref.asText().substring(COMPONENTS.length());
                object.put("$ref", "#/$defs/" + target);
                todo.add(target);
            }
            for (Iterator<Map.Entry<String, JsonNode>> it = object.fields(); it.hasNext(); ) {
                rewriteRefs(it.next().getValue(), todo);
            }
        } else if (node != null && node.isArray()) {
            node.forEach(child -> rewriteRefs(child, todo));
        }
    }
}
