package core.swagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SwaggerDynamicFilter {

    private List<String> tags;
    private ObjectNode complete_json_doc;

    public SwaggerDynamicFilter( ObjectNode complete_json_doc, List<String> tags) {
        this.tags = tags;
        this.complete_json_doc = complete_json_doc;

    }

    public ObjectNode parseFilter() {

        this.fixTags(complete_json_doc, tags);
        this.fixPaths(complete_json_doc, tags);

        return complete_json_doc;
    }


    private void fixPaths(ObjectNode object, List<String> allowed_tags) {

        ObjectNode object_paths = (ObjectNode) object.get("paths");

        ObjectNode new_object_paths = Json.newObject();

        object_paths.fieldNames().forEachRemaining(
                (String path) -> {

                    ObjectNode method_api = (ObjectNode) object_paths.get(path);

                    if(method_api.has("post")) {
                        if(decide_forFixPaths(((ObjectNode) method_api.get("post")), allowed_tags)){
                            new_object_paths.set(path, object_paths.get(path));
                        }
                        return;
                    }

                    if(method_api.has("put")) {
                        if(decide_forFixPaths(((ObjectNode) method_api.get("put")), allowed_tags)){
                            new_object_paths.set(path, object_paths.get(path));
                        }
                        return;
                    }

                    if(method_api.has("get")) {
                        if( decide_forFixPaths(((ObjectNode) method_api.get("get")), allowed_tags)){
                            new_object_paths.set(path, object_paths.get(path));
                        }
                        return;
                    }

                    if(method_api.has("delete")) {
                        if(decide_forFixPaths(((ObjectNode) method_api.get("delete")), allowed_tags)){
                            new_object_paths.set(path, object_paths.get(path));
                        }
                    }

                });


        object.set("paths", new_object_paths);

    }

    private boolean decide_forFixPaths(ObjectNode object, List<String> allowed_tags) {

        ArrayNode tags_in_api_object = object.withArray("tags");

        List<String> tags_in_method = new ArrayList<>();

        for(int i = 0; i < tags_in_api_object.size(); i++) {
            tags_in_method.add(tags_in_api_object.get(i).asText());
        }

        return (intersect(allowed_tags, tags_in_method).size() > 0 || intersect(tags_in_method, allowed_tags).size() > 0);
    }

    private List<String> intersect(List<String> A, List<String> B) {
        List<String> rtnList = new LinkedList<>();
        for(String dto : A) {
            if(B.contains(dto)) {
                rtnList.add(dto);
            }
        }
        return rtnList;
    }

    private void fixTags(ObjectNode object, List<String> allowed_tags){

        ArrayNode tags_array = object.withArray("tags");

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode new_tags_array = mapper.createArrayNode();

        for(int i = 0; i < tags_array.size(); i++) {
            if(allowed_tags.contains(tags_array.get(i).get("name").textValue())) {
                new_tags_array.add(tags_array.get(i));
            }
        }

        object.set("tags", new_tags_array);
    }
}
