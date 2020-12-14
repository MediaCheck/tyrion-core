package core.swagger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import core.network.NetworkStatus;

import java.util.List;

@ApiModel(value = "ProjectShort_Reference", description = "Model of Reference")
public class Swagger_Mongo_Short_Reference {

        public Swagger_Mongo_Short_Reference(ObjectId id, String name, String description){
            this.id = id.toString();
            this.name = name;
            this.description = description;
        }

        public Swagger_Mongo_Short_Reference(ObjectId id, String name, String description, List<String> tags){
            this.id = id.toString();
            this.name = name;
            this.description = description;
            this.tags = tags;
        }

        public Swagger_Mongo_Short_Reference(ObjectId id, String name, String description, List<String> tags, NetworkStatus status){
            this.id = id.toString();
            this.name = name;
            this.description = description;
            this.tags = tags;
            this.online_state = status;
        }

        public String name;
        public String description;
        public String id;
        public List<String> tags;

        @ApiModelProperty(value = "Only for Special Object type like Server, Instance, HW")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonProperty()
        public NetworkStatus online_state;

}
