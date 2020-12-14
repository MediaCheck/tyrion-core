package core.storage;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

public class RetrievedItem {

    private final ResponseBytes<GetObjectResponse> responseBytes;

    public RetrievedItem(ResponseBytes<GetObjectResponse> responseBytes) {
        this.responseBytes = responseBytes;
    }

    public String asText() {
        return this.responseBytes.asUtf8String();
    }

    public JsonNode asJson() {
        return Json.parse(this.responseBytes.asUtf8String());
    }

    public byte[] asBytes() {
        return this.responseBytes.asByteArray();
    }
}
