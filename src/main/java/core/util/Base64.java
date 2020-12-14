package core.util;

import core.exceptions.BadRequestException;

import java.util.List;

import static org.apache.commons.codec.binary.Base64.decodeBase64;

public class Base64 {

    public static byte[] decode(String base64) {
        return decode(base64, null);
    }

    public static byte[] decode(String base64, List<String> allowedTypes) {
        String[] components = base64.split(",");

        if (components.length != 2) {
            throw new BadRequestException("Invalid base64 data");
        }

        String base64Data = components[0];
        String type = base64Data.substring(base64Data.indexOf('/') + 1, base64Data.indexOf(';'));

        if (allowedTypes != null && !allowedTypes.contains(type)) {
            throw new BadRequestException("Invalid file type: " + type);
        }

        return decodeBase64(components[1]);
    }

    public static String type(String base64) {
        String[] components = base64.split(",");

        if (components.length != 2) {
            throw new BadRequestException("Invalid base64 data");
        }

        String base64Data = components[0];
        return base64Data.substring(base64Data.indexOf('/') + 1, base64Data.indexOf(';'));
    }
}
